/********************************************************************************/
/*										*/
/*		MintMaster.java 						*/
/*										*/
/*	Class for communicating with Mint server master 			*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Redistribution and use in source and binary forms, with or without		 *
 *  modification, are permitted provided that the following conditions are met:  *
 *										 *
 *  + Redistributions of source code must retain the above copyright notice,	 *
 *	this list of conditions and the following disclaimer.			 *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *	this list of conditions and the following disclaimer in the		 *
 *	documentation and/or other materials provided with the distribution.	 *
 *  + Neither the name of the Brown University nor the names of its		 *
 *	contributors may be used to endorse or promote products derived from	 *
 *	this software without specific prior written permission.		 *
 *										 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE	 *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE	 *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE	 *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 	 *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF	 *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS	 *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN	 *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)	 *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE	 *
 *  POSSIBILITY OF SUCH DAMAGE. 						 *
 *										 *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/MintMaster.java,v 1.29 2017/06/23 20:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintMaster.java,v $
 * Revision 1.29  2017/06/23 20:54:54  spr
 * Use public file locking class.
 *
 * Revision 1.28  2014/08/29 15:10:27  spr
 * Fix up memory size
 *
 * Revision 1.27  2014/06/12 01:06:30  spr
 * Minor updates
 *
 * Revision 1.26  2013/11/15 02:38:56  spr
 * Fix imports.
 *
 * Revision 1.25  2013-06-11 23:14:31  spr
 * Code cleanup.
 *
 * Revision 1.24  2012-11-04 02:10:45  spr
 * Code cleanup
 *
 * Revision 1.23  2012-05-22 00:42:06  spr
 * Formatting changes.
 *
 * Revision 1.22  2012-03-21 23:54:25  spr
 * Code cleanup
 *
 * Revision 1.21  2011-06-17 12:31:30  spr
 * Use qualified registry name.
 *
 * Revision 1.20  2011-06-16 17:45:38  spr
 * Use registry names rather than values.
 *
 * Revision 1.19  2011-05-27 19:32:42  spr
 * Change copyrights.
 *
 * Revision 1.18  2010-08-20 20:58:24  spr
 * Add logging and options for port numbers
 *
 * Revision 1.17  2010-08-09 17:10:31  spr
 * Check that we are using all local addresses.
 *
 * Revision 1.16  2010-08-06 01:28:11  spr
 * Use 127.0.0.1 rather than localhost
 *
 * Revision 1.15  2010-08-04 22:01:53  spr
 * Master server not ready for localhost.
 *
 * Revision 1.14  2010-04-29 18:46:04  spr
 * Ensure path is quoted for windows.
 *
 * Revision 1.13  2010-02-26 21:05:33  spr
 * Formatting issues and minor additions
 *
 * Revision 1.12  2009-10-02 00:18:27  spr
 * Import clean up.
 *
 * Revision 1.11  2009-09-17 01:58:59  spr
 * Use IVY setup and IvyExec.runJava for running mint (for windows).
 *
 * Revision 1.10  2009-06-04 18:50:41  spr
 * Use ivyJava call when necessary.
 *
 * Revision 1.9  2009-04-12 01:54:17  spr
 * Let the connect to master time out rather than hang forever.
 *
 * Revision 1.8  2009-03-20 01:57:33  spr
 * Use environment variables if present.
 *
 * Revision 1.7  2008-11-24 23:36:22  spr
 * Provide for rmi registry based lookup.
 *
 * Revision 1.6  2008-11-12 13:46:17  spr
 * Minor cleanups and fixes.
 *
 * Revision 1.5  2006-12-01 03:22:50  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.4  2006/07/10 14:52:20  spr
 * Code cleanup.
 *
 * Revision 1.3  2005/11/07 21:09:26  spr
 * Proper static calls.
 *
 * Revision 1.2  2005/09/19 23:32:00  spr
 * Fixups based on findbugs.
 *
 * Revision 1.1  2005/07/08 23:32:53  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint;


import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.exec.IvyExecQuery;
import edu.brown.cs.ivy.file.IvyFileLocker;

import java.io.*;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.util.*;



public class MintMaster implements MintConstants {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static Socket		master_socket = null;





/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

static String getMasterFile()
{
   return MINT_MASTER_FILE;
}



/********************************************************************************/
/*										*/
/*	Methods to register a server with the master socket			*/
/*										*/
/********************************************************************************/

public static boolean registerSocket(String id,ServerSocket ss)
{
   if (!getMasterSocket()) return false;

   if (!setupServer(id,ss)) return false;

   return true;
}



/********************************************************************************/
/*										*/
/*	Methods to create a socket to a server					*/
/*										*/
/********************************************************************************/

public static Socket findServer(String id,String startargs)
{
   Socket skt = null;

   if (id == null) return null;

   if (!getMasterSocket()) return null;

   String jargs = null;

   for (int i = 0; i < 20; ++i) {
      skt = setupClient(id);
      if (skt != null) break;

      if (i == 0 || i == 10) {
	 if (startargs == null) break;
	 try {
	    File fcmd = new File(MINT_SERVER_START_CMD);
	    if (fcmd.exists()) {
	       new IvyExec("\'" + MINT_SERVER_START_CMD + "' " + startargs);
	       jargs = MINT_SERVER_START_CMD;
	     }
	    else {
	       jargs = null;
	       if (startargs.startsWith("-Debug ")) {
		  jargs = "-Dedu.brown.cs.ivy.mint.debug=true";
		  startargs = startargs.substring(7);
		}
	       String rhost = System.getProperty(MINT_REGISTRY_HOST_PROP);
	       if (rhost != null) {
		  if (jargs == null) jargs = "'-Dedu.brown.cs.ivy.mint.registryhost=" + rhost + "'";
		  else jargs += " '-Dedu.brown.cs.ivy.mint.registryhost=" + rhost + "'";
		}
	       String mhost = System.getProperty(MINT_MASTER_HOST_PROP);
	       if (mhost != null) {
		  if (jargs == null) jargs = "'-Dedu.brown.cs.ivy.mint.master.host=" + mhost + "'";
		  else jargs += " '-Dedu.brown.cs.ivy.mint.master.host=" + mhost + "'";
		}
	       String mport = System.getProperty(MINT_MASTER_PORT_PROP);
	       if (mport != null) {
		  if (jargs == null) jargs = "'-Dedu.brown.cs.ivy.mint.master.port=" + mport + "'";
		  else jargs += " '-Dedu.brown.cs.ivy.mint.master.port=" + mport + "'";
		}
	       IvyExec.ivyJava("edu.brown.cs.ivy.mint.server.MintServer",jargs,startargs);
	     }
	  }
	 catch (IOException e) { break; }
       }

      try {
	 Thread.sleep((i+1)*1000);
       }
      catch (InterruptedException e) { }
    }

   if (skt == null) {
      MintLogger.log("Couldn't start server " + id + " using " + startargs + " :: " + jargs);
    }

   MintLogger.log("Connection to server established for " + skt);

   return skt;
}



/********************************************************************************/
/*										*/
/*	Handle debugging MINT problems						*/
/*										*/
/********************************************************************************/

public static String getServerInfo()
{
   StringBuffer buf = new StringBuffer();

   if (master_socket == null) {
      buf.append("NoMasterSocket");
    }
   else {
      buf.append(master_socket.toString());
    }

   buf.append(" ");
   String rnm = System.getProperty(MINT_REGISTRY_HOST_PROP);
   if (rnm == null) rnm = System.getenv(MINT_REGISTRY_HOST_ENV);
   Registry rmireg = null;
   String hnm = IvyExecQuery.getHostName();
   int idx1 = rnm.indexOf(".");
   int idx2 = hnm.indexOf(".");
   if (idx1 > 0 && idx2 > 0 && rnm.substring(idx1).equals(hnm.substring(idx2))) {
      MintLogger.log("Looking up master using rmi on " + rnm);
      try {
	 new OurSocketFactory();
	 rmireg = LocateRegistry.getRegistry(rnm);
       }
      catch (RemoteException e) { }
      catch (Throwable t) {
	 MintLogger.log("Error looking up master",t);
       }
    }
   if (rmireg == null) {
      buf.append("NoRegistry");
    }
   else {
      buf.append(rmireg.toString());
    }

   buf.append(" ");
   File f = new File(getMasterFile());
   buf.append(f);
   buf.append(" ");
   buf.append(f.exists());
   buf.append(" ");
   buf.append(f.canRead());
   buf.append(" ");
   buf.append(f.canWrite());

   File f1 = new File(MINT_MASTER_CMD);
   buf.append(" ");
   buf.append(f1);
   buf.append(" ");
   buf.append(f1.exists());
   buf.append(" ");
   buf.append(f1.canExecute());

   return buf.toString();
}



/********************************************************************************/
/*										*/
/*	Methods to return information on active servers 			*/
/*										*/
/********************************************************************************/

public static String [] listServers()
{
   if (!getMasterSocket()) return null;

   List<String> v = findActiveServers();

   String [] svrs = new String[v.size()];
   v.toArray(svrs);

   return svrs;
}




/********************************************************************************/
/*										*/
/*	Method to get the socket connection to the server master		*/
/*										*/
/********************************************************************************/

private static boolean getMasterSocket()
{
   if (master_socket != null) return true;

   Registry rmireg = null;
   String rnm = System.getProperty(MINT_REGISTRY_HOST_PROP);
   if (rnm == null) rnm = System.getenv(MINT_REGISTRY_HOST_ENV);
   if (rnm != null) {
      String hnm = IvyExecQuery.getHostName();
      int idx1 = rnm.indexOf(".");
      int idx2 = hnm.indexOf(".");
      if (idx1 > 0 && idx2 > 0 && rnm.substring(idx1).equals(hnm.substring(idx2))) {
	 MintLogger.log("Looking up master using rmi on " + rnm);
	 try {
	    new OurSocketFactory();
	    rmireg = LocateRegistry.getRegistry(rnm);
	  }
	 catch (RemoteException e) { }
	 catch (Throwable t) {
	    MintLogger.log("Error looking up master",t);
	  }
       }
    }
   if (rmireg != null) {
      try {
	 String hps = null;
	 int ctr = -1;
	 for (String s : rmireg.list()) {
	    if (s.startsWith(MINT_REGISTRY_PREFIX) || s.startsWith("MintMaster[")) {
	       int idx1 = s.indexOf("[");
	       int idx2 = s.indexOf("]");
	       String cs = s.substring(idx2+1);
	       int c = 0;
	       if (cs.length() > 0) c = Integer.parseInt(cs);
	       if (c > ctr) {
		  ctr = c;
		  hps = s.substring(idx1+1,idx2);
		}
	     }
	  }
	 if (hps != null) {
	    int idx1 = hps.indexOf("@");
	    String h1 = hps.substring(0,idx1);
	    h1 = fixHost(h1);
	    int port = Integer.parseInt(hps.substring(idx1+1));
	    master_socket = new Socket(h1,port);
	    if (master_socket != null) return true;
	  }

	 HostPort hp = (HostPort) rmireg.lookup(MINT_REGISTRY_PROP);
	 if (hp != null) {
	    // MintLogger.log("Connection to master at " + hp.getHost() + " " + hp.getPort());
	    String h = fixHost(hp.getHost());
	    master_socket = new Socket(h,hp.getPort());
	    if (master_socket != null) return true;
	  }
       }
      catch (NotBoundException e) { }
      catch (RemoteException e) { }
      catch (IOException e) { }
      catch (Throwable t) {
	 MintLogger.log("Error connecting to master",t);
       }
    }

   System.clearProperty(MINT_REGISTRY_HOST_PROP);

   String fn = getMasterFile();
   File ffn = new File(fn);
   File ffnp = ffn.getParentFile();
   if (!ffnp.exists()) ffnp.mkdirs();

   IvyFileLocker lock = new IvyFileLocker(fn);
   try {
      lock.lock();

      for (int i = 0; i < 20; ++i) {
	 String host = null;
	 int port = 0;

	 String h = System.getProperty(MINT_MASTER_HOST_PROP);
	 if (h == null) h = System.getenv("MINT_MASTER_HOST");
	 String p = System.getProperty(MINT_MASTER_PORT_PROP);
	 if (p == null) p = System.getenv("MINT_MASTER_PORT");

	 if (h != null && p != null) {
	    int pn = Integer.parseInt(p);
	    if (pn != 0) {
	       host = h;
	       port = pn;
	     }
	  }

	 if (host == null) {
	    try {
	       FileReader fr = new FileReader(fn);
	       BufferedReader lnr = new BufferedReader(fr);
	       String hn = lnr.readLine();
	       if (hn != null) {
		  StringTokenizer tok = new StringTokenizer(hn,"\t");
		  if (tok.countTokens() == 2) {
		     host = tok.nextToken().trim();
		     port = Integer.parseInt(tok.nextToken());
		   }
		}
	       lnr.close();
	     }
	    catch (IOException e) {
	     }
	  }

	 if (host != null) {
	    host = fixHost(host);
	    // MintLogger.log("Connection to master at " + host + " " + port);
	    try {
	       Socket s = new Socket();
	       SocketAddress sa = new InetSocketAddress(host,port);
	       s.connect(sa,1000);
	       master_socket = s;
	       System.setProperty(MINT_MASTER_HOST_PROP,host);
	       System.setProperty(MINT_MASTER_PORT_PROP,Integer.toString(port));
	     }
	    catch (IOException e) {
	     }
	  }

	 if (master_socket != null) {
	    break;
	  }

	 if (i == 0) {
	    try {
	       File f = new File(MINT_MASTER_CMD);
	       if (f.exists()) {
		  new IvyExec("'" + MINT_MASTER_CMD + "'");
		}
	       else {
		  IvyExec.ivyJava("edu.brown.cs.ivy.mint.MintServerMaster","-Xmx64m",null);
		}
	     }
	    catch (IOException e) {
	       MintLogger.log("I/O error: " + e.getMessage());
	       break;
	     }
	  }

	 try {
	    Thread.sleep(2000);
	  }
	 catch (InterruptedException e) { }
       }

      if (master_socket == null) MintLogger.log("Couldn't open master server");
    }
   finally {
      lock.unlock();
    }

   if (master_socket == null) return false;

   return true;
}





/********************************************************************************/
/*										*/
/*	Method to tell the master socket about a server 			*/
/*										*/
/********************************************************************************/


private static boolean setupServer(String id,ServerSocket ss)
{
   if (master_socket == null) return false;

   try {
      OutputStream ots = master_socket.getOutputStream();
      PrintStream ps = new PrintStream(ots,true);
      String h = ss.getInetAddress().getHostAddress();
      if (h.equals("0.0.0.0")) {
	 try {
	    h = InetAddress.getLocalHost().getHostAddress();
	  }
	 catch (UnknownHostException e) { }
       }

      ps.println("STRT\t" +  id + "\t" + h + "\t" + ss.getLocalPort());
      ps.flush();

      InputStream ins = master_socket.getInputStream();
      BufferedInputStream bins = new BufferedInputStream(ins);
      InputStreamReader insr = new InputStreamReader(bins);
      BufferedReader lnr = new BufferedReader(insr);

      String rslt = lnr.readLine();
      if (rslt != null && rslt.equals("OK")) return true;
    }
   catch (IOException e) { }

   return false;
}



/********************************************************************************/
/*										*/
/*	Methods to find a server from the master socket 			*/
/*										*/
/********************************************************************************/

private static Socket setupClient(String id)
{
   Socket skt = null;

   if (master_socket == null) return null;

   try {
      OutputStream ots = master_socket.getOutputStream();
      PrintStream ps = new PrintStream(ots,true);
      ps.println("FIND\t" + id);
      ps.flush();

      InputStream ins = master_socket.getInputStream();
      BufferedInputStream bins = new BufferedInputStream(ins);
      InputStreamReader insr = new InputStreamReader(bins);
      BufferedReader lnr = new BufferedReader(insr);

      String rslt = lnr.readLine();
      if (rslt != null && rslt.startsWith("USE\t")) {
	 StringTokenizer tok = new StringTokenizer(rslt);
	 tok.nextToken();
	 String h = tok.nextToken();
	 int p = Integer.parseInt(tok.nextToken());
	 h = fixHost(h);
	 skt = new Socket(h,p);
       }
    }
   catch (IOException e) {
      skt = null;
    }

   return skt;
}



private static String fixHost(String h)
{
   if (h == null) return null;

   try {
      String h1 = InetAddress.getLocalHost().getHostName();
      String h2 = InetAddress.getLocalHost().getHostAddress();
      String h3 = InetAddress.getLocalHost().getCanonicalHostName();

      if (h.equals(h1) || h.equals(h2) || h.equals(h3)) {
	 return "127.0.0.1";
       }
   }
   catch (UnknownHostException e) { }

   return h;
}




/********************************************************************************/
/*										*/
/*	Methods to create a list of active servers				*/
/*										*/
/********************************************************************************/

private static List<String> findActiveServers()
{
   List<String> v = new ArrayList<String>();

   try {
      OutputStream ots = master_socket.getOutputStream();
      PrintStream ps = new PrintStream(ots,true);
      ps.println("LIST");
      ps.flush();

      InputStream ins = master_socket.getInputStream();
      BufferedInputStream bins = new BufferedInputStream(ins);
      InputStreamReader insr = new InputStreamReader(bins);
      BufferedReader lnr = new BufferedReader(insr);

      for ( ; ; ) {
	 String rslt = lnr.readLine();
	 if (rslt == null || rslt.equals("END")) break;
	 if (rslt.startsWith("LIST")) {
	    StringTokenizer tok = new StringTokenizer(rslt,"\t");
	    tok.nextToken();
	    if (tok.hasMoreTokens()) {
	       String svr = tok.nextToken();
	       v.add(svr);
	     }
	  }
       }
    }
   catch (IOException e) { }

   return v;
}







/********************************************************************************/
/*										*/
/*	Socket factory for timeout						*/
/*										*/
/********************************************************************************/

private static class OurSocketFactory extends RMISocketFactory {

   private RMISocketFactory base_factory;

   OurSocketFactory() {
      base_factory = RMISocketFactory.getSocketFactory();
      if (base_factory == null) base_factory = RMISocketFactory.getDefaultSocketFactory();
      if (base_factory instanceof OurSocketFactory) return;
      try {
	 RMISocketFactory.setSocketFactory(this);
       }
      catch (Exception e) {
	 MintLogger.log("Problem setting RMI socket factory",e);
       }
    }

   @Override public ServerSocket createServerSocket(int port) throws IOException {
      ServerSocket ss = base_factory.createServerSocket(port);
      ss.setSoTimeout(3000);
      return ss;
    }

   @Override public Socket createSocket(String host,int port) throws IOException {
      Socket s = base_factory.createSocket(host,port);
      return s;
    }

}	// end of inner class OurSocketFactory



}	// end of class MintMaster



/* end of MintMaster.java */
