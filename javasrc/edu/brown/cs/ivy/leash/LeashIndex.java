/********************************************************************************/
/*										*/
/*		LeashIndex.java 						*/
/*										*/
/*	External view of a Cocker Index (or database)				*/
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class LeashIndex implements LeashConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private LeashConnection 	cocker_connect;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public LeashIndex(String typ,File dir)
{
   cocker_connect = new LeashConnection(typ,dir);
}


public LeashIndex(String host,int port)
{
   cocker_connect = new LeashConnection(host,port);
}



/********************************************************************************/
/*										*/
/*	Control methods 							*/
/*										*/
/********************************************************************************/

public boolean remove()
{
   return false;
}


public boolean setup()
{
   return false;
}


public boolean start()
{
   if (cocker_connect.isActive()) return false;
   
   cocker_connect.startServer();
   
   return cocker_connect.isActive();
}

public boolean isActive()
{
   return cocker_connect.isActive();
}


public void stop()
{
   if (!cocker_connect.isActive()) return;
   
   cocker_connect.sendCommand("STOP");
}


public boolean update()
{
   if (!start()) return false;
   
   Element rslt = cocker_connect.sendCommand("UPDATE");
   
   return checkStatus(rslt);
}



/********************************************************************************/
/*										*/
/*	Methods to set files to be indexed					*/
/*										*/
/********************************************************************************/

public boolean monitor(File ... dir)
{
   return fileCommand("MONITOR",dir);
}


public boolean unmonitor(File ... dir)
{
   return fileCommand("UNMONITOR",dir);
}



public boolean whitelist(File ... dir)
{
   return fileCommand("WHITELIST",dir);
}


public boolean blackList(File ... dir)
{
   return fileCommand("BLACKLIST",dir);
}


public List<File> getTopFiles()
{
   Element crslt = cocker_connect.sendCommand("FILES");
   List<File> rslt = new ArrayList<>();
   if (!IvyXml.isElement(crslt,"FILEDATA")) crslt = IvyXml.getChild(crslt,"FILEDATA");
   for (Element inc : IvyXml.children(crslt,"INCLUDE")) {
      String fnm = IvyXml.getAttrString(inc,"NAME");
      File f = new File(fnm);
      rslt.add(f);
    }
   
   return rslt;
}



private boolean fileCommand(String cmd,File [] dir)
{
   if (!isActive() && !start()) return false;
   
   StringBuffer buf = new StringBuffer();
   for (File f : dir) {
      buf.append("<FILE>" + f.getPath() + "</FILE>\n");
    }
   
   Element rslt = cocker_connect.sendCommand(cmd,null,buf.toString());
   return checkStatus(rslt);
}




/********************************************************************************/
/*										*/
/*	Methods to set parameters						*/
/*										*/
/********************************************************************************/

public boolean setIdleTimeout(long time)
{
   CommandArgs args = new CommandArgs("INTERVAL",time);
   Element rslt = cocker_connect.sendCommand("IDLETIME",args,null);
   return checkStatus(rslt);
}


public boolean setAutoUpdateTime(long time,Date d)
{
   CommandArgs args = new CommandArgs("INTERVAL",time);
   if (d != null) args.put("WHEN",d.getTime());
   
   Element rslt = cocker_connect.sendCommand("UPDATETIME",args,null);
   return checkStatus(rslt);
}



/********************************************************************************/
/*										*/
/*	Query methods								*/
/*										*/
/********************************************************************************/

public List<LeashResult> queryStatements(File f,int line,int col)
{
   try {
      String cnts = IvyFile.loadFile(f);
      String loc = "slc:" + line + "," + col;
      return issueQuery("STATEMENTS",loc,cnts);
    }
   catch (IOException e) {
      return null;
    }
}



public List<LeashResult> queryStatements(String filecnts,int line,int col)
{
   String loc = "slc:" + line + "," + col;
   return issueQuery("STATEMENTS",loc,filecnts);
}



private List<LeashResult> issueQuery(String typ,String srcloc,String cnts)
{
   cnts = cnts.replace("]]>","] ]>");

   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("COMMAND");
   xw.field("CMD","CODEQUERY");
   xw.field("TYPE",typ);
   if (srcloc != null) xw.field("DATA",srcloc);
   xw.cdataElement("CODE",cnts);
   xw.end("COMMAND");
   String msg = xw.toString();
   xw.close();
  
   List<LeashResult> ret = new ArrayList<>();
   
   Element rslt = cocker_connect.sendRequest(msg);
   for (Element relt : IvyXml.children(rslt,"FILE")) {
      String file = IvyXml.getText(relt);
      String mloc = IvyXml.getAttrString(relt,"MLOC");
      double score = IvyXml.getAttrDouble(relt,"SCORE",0);
      LeashResult lr = new LeashResult(cocker_connect,file,mloc,score);
      ret.add(lr);
    }
   
   Collections.sort(ret);
   
   return ret;
}

/********************************************************************************/
/*                                                                              */
/*      Utility methods                                                         */
/*                                                                              */
/********************************************************************************/

private boolean checkStatus(Element rslt)
{
   if (rslt == null) return false;
   
   return "OK".equals(IvyXml.getAttrString(rslt,"STATUS"));
}


}	// end of class LeashIndex




/* end of LeashIndex.java */

