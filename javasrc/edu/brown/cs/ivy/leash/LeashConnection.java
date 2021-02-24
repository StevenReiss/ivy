/********************************************************************************/
/*										*/
/*		LeashConnection.java						*/
/*										*/
/*	Connection to a cocker instance 					*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 * This program and the accompanying materials are made available under the	 *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at								 *
 *	http://www.eclipse.org/legal/epl-v10.html				 *
 *										 *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.ivy.leash;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.exec.IvyExecQuery;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlReader;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

class LeashConnection implements LeashConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

// for local access
private String		analysis_type;
private File		index_directory;

// for remote access
private String		host_name;
private int		port_number;

private boolean 	is_local;
private boolean 	is_active;
private String		database_name;

private Map<File,String> file_contents;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

LeashConnection(String typ,File dir)
{
   analysis_type = typ;
   index_directory = dir;
   host_name = "localhost";
   port_number = 0;
   is_local = true;
   file_contents = null;
   is_active = true;
   database_name = null;
   findActiveHostAndPort();
}


LeashConnection(String host,int port)
{
   analysis_type = null;
   index_directory = null;
   host_name = host;
   port_number = port;
   is_local = false;
   file_contents = new HashMap<>();
   is_active = true;
   database_name = null;
   getServerInformation();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getDatabaseName()
{
   return database_name;
}


boolean isLocal()
{
   return is_local;
}


boolean isActive()
{
   return is_active;
}



/********************************************************************************/
/*										*/
/*	Server action methods							*/
/*										*/
/********************************************************************************/

boolean startServer()
{
   if (is_active) return true;
   if (host_name == null) return false;

   if (!host_name.equals("localhost") && !host_name.equals(IvyExecQuery.getHostName())) {
      return false;
    }

   try {
      String cp = System.getProperty("java.class.path");

      StringBuffer cpbuf = new StringBuffer();

      for (StringTokenizer tok1 = new StringTokenizer(LEASH_CLASS_PATH); tok1.hasMoreTokens(); ) {
	 String fnm = tok1.nextToken();
	 if (fnm.equals("cocker.jar")) {
	    File f1 = IvyFile.expandFile("$(PRO)/cocker/cocker.jar");
	    File f2 = IvyFile.expandFile("$(IVY)/lib/cocker.jar");
	    if (f1.exists()) cpbuf.append(f1.getPath());
	    else cpbuf.append(f2.getPath());
	  }
	 else if (fnm.equals("ivy")) {
	    for (StringTokenizer tok2 = new StringTokenizer(cp,File.pathSeparator); tok2.hasMoreTokens(); ) {
	       String p2 = tok2.nextToken();
	       if (p2.endsWith("ivy.jar") || p2.endsWith("ivy/java") || p2.endsWith("ivy/bin")) {
		  cpbuf.append(p2);
		}
	     }
	  }
	 else {
	    int ct = 0;
	    for (StringTokenizer tok2 = new StringTokenizer(cp,File.pathSeparator); tok2.hasMoreTokens(); ) {
	       String p2 = tok2.nextToken();
	       if (p2.contains(fnm)) {
		  if (ct++ > 0) cpbuf.append(File.pathSeparator);
		  cpbuf.append(p2);
		}
	     }
	  }
	 cpbuf.append(File.pathSeparator);
       }

      StringBuffer cmd = new StringBuffer();
      cmd.append("java");
      cmd.append(" -cp '" + cpbuf.toString() + "'");
      cmd.append(" edu.brown.cs.cocker.cocker.CockerServer");
      if (port_number > 0) cmd.append(" -port " + port_number);
      cmd.append(" -analysis " + analysis_type);
      if (index_directory != null) {
	 cmd.append(" -dir " + index_directory.getPath());
       }

      IvyLog.logI("LEASH","RUN SERVER: " + cmd);

      new IvyExec(cmd.toString());

      for (int i = 0; i < 10; ++i) {
	 try {
	    Thread.sleep(i*1000);
	  }
	 catch (InterruptedException e) { }
	 getServerInformation();
	 if (is_active) return true;
       }

      throw new IOException("Server not started");
    }
   catch (IOException ioe) {
      System.err.println("Could not start server.");
      System.err.println("Error: " + ioe.getMessage());
    }

   return false;
}



/********************************************************************************/
/*										*/
/*	Find host/port for directory-based cocker				*/
/*										*/
/********************************************************************************/

private void findActiveHostAndPort()
{
   String nm = ".cocker." + analysis_type;
   File pfile = new File(index_directory,nm);
   host_name = "localhost";
   port_number = 0;

   try (FileReader fr = new FileReader(pfile)) {
      Properties p = new Properties();
      p.load(fr);
      host_name = p.getProperty("host");
      String port = p.getProperty("port");
      if (port != null) {
	 port_number = Integer.parseInt(port);
       }
    }
   catch (IOException e) { }

   if (port_number != 0) {
      getServerInformation();
      if (port_number == 0) is_active = false;
    }
   else is_active = false;
}



/********************************************************************************/
/*										*/
/*	Find server information given host/port 				*/
/*										*/
/********************************************************************************/

private void getServerInformation()
{
   Element pong = sendRequest("<PING/>");
   if (pong == null) {
      is_active = false;
    }
   else {
      is_active = true;
      host_name = IvyXml.getAttrString(pong,"HOST");
      port_number = IvyXml.getAttrInt(pong,"PORT");
      if (analysis_type == null) analysis_type = IvyXml.getAttrString(pong,"TYPE");
      if (index_directory == null) {
	 String dnm = IvyXml.getAttrString(pong,"DIR");
	 if (dnm != null) index_directory = new File(dnm);
       }
      database_name = IvyXml.getAttrString(pong,"DB");
      return;
    }

}



/********************************************************************************/
/*										*/
/*	Send message to cocker and get reply					*/
/*										*/
/********************************************************************************/

Element sendCommand(String cmd)
{
   return sendCommand(cmd,null,null);
}


Element sendCommand(String cmd,CommandArgs args,String body)
{
   try (IvyXmlWriter xw = new IvyXmlWriter()) {
      xw.begin("COMMAND");
      xw.field("CMD",cmd);
      if (args != null) {
	 for (Map.Entry<String,Object> ent : args.entrySet()) {
	    xw.field(ent.getKey(),ent.getValue());
	  }
       }
      if (body != null) xw.xmlText(body);
      xw.end("COMMAND");
      return sendRequest(xw.toString());
    }
}




Element sendRequest(String req)
{
   List<Element> rslts = sendRequests(req,1);
   if (rslts == null || rslts.size() == 0) return null;
   return rslts.get(0);
}



 List<Element> sendRequests(String req,int ct)
{
   if (!is_active) return null;

   List<Element> rslts = new ArrayList<>();

   try (Socket s = new Socket(host_name,port_number)) {
      s.setSoTimeout(LEASH_REQUEST_TIMEOUT);
      PrintWriter pw = new PrintWriter(s.getOutputStream());
      IvyXmlReader xr = new IvyXmlReader(s.getInputStream());
      pw.println(req);
      pw.flush();
      String rslttxt = xr.readXml();
      xr.close();
      pw.close();
      for (int i = 0; i < ct; ++i) {
	 if (rslttxt == null) rslts.add(null);
	 else rslts.add(IvyXml.convertStringToXml(rslttxt));
       }
    }
   catch (IOException e) {
      is_active = false;
      return null;
    }

   return rslts;
}


 /********************************************************************************/
/*										*/
/*	Remote file mehtods							*/
/*										*/
/********************************************************************************/

String getFileContents(File f)
{
   if (is_local) return null;

   String cnts = file_contents.get(f);
   if (cnts != null) return cnts;

   CommandArgs args = new CommandArgs("FILE",f.getPath());
   Element rslt = sendCommand("GETFILE",args,null);
   byte [] arr = IvyXml.getBytesElement(rslt,"CONTENTS");
   if (arr != null && arr.length > 0) {
      cnts = new String(arr);
    }
   if (cnts == null) cnts = "";

   synchronized (file_contents) {
      file_contents.putIfAbsent(f,cnts);
    }

   if (cnts.length() == 0) return null;

   return cnts;
}



}	// end of class LeashConnection




/* end of LeashConnection.java */

