/********************************************************************************/
/*										*/
/*		MintServerMaster.java						*/
/*										*/
/*	Main program implmentation of the Mint server master control		*/
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


package edu.brown.cs.ivy.mint;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;



public final class MintServerMaster implements MintConstants
{



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   MintServerMaster sm = new MintServerMaster();

   sm.process();
}




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SocketThread socket_thread;
private Map<String,SocketWorker> server_hash;

private static boolean do_debug = true;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private MintServerMaster()
{
   server_hash = new HashMap<String,SocketWorker>();
   socket_thread = null;
}



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

private void process()
{
   int port = 0;
   String p = System.getProperty("edu.brown.cs.ivy.mint.master.port");
   if (p == null) p = System.getenv("MINT_MASTER_PORT");
   if (p != null) port = Integer.parseInt(p);

   socket_thread = new SocketThread(port);
   socket_thread.start();
}



private void handleConnection(Socket s)
{
   SocketWorker sw = new SocketWorker(s);
   sw.start();
}



private synchronized void processCommand(String cmd,SocketWorker sw,PrintStream ps)
{
   if (cmd == null) {
      String s = sw.getFile();
      if (s != null) {
	 server_hash.remove(s);
	 if (do_debug) MintLogger.log("MASTER: remove " + s);
       }
    }
   else if (cmd.startsWith("PING")) {
      ps.println("PONG");
    }
   else if (cmd.startsWith("STRT")) {
      StringTokenizer tok = new StringTokenizer(cmd,"\t");
      tok.nextToken();
      String file = tok.nextToken();
      String host = tok.nextToken();
      int port = Integer.parseInt(tok.nextToken());
      if (server_hash.get(file) != null) {
	 if (do_debug) MintLogger.log("MASTER: Previously started: " + cmd);
	 ps.println("NO");
       }
      else {
	 if (do_debug) MintLogger.log("MASTER: Start: " + cmd + " WITH " + sw);
	 server_hash.put(file,sw);
	 sw.setConn(file,host,port);
	 ps.println("OK");
       }
    }
   else if (cmd.startsWith("FIND")) {
      StringTokenizer tok = new StringTokenizer(cmd,"\t");
      tok.nextToken();
      String file = tok.nextToken();
      SocketWorker nw = server_hash.get(file);
      if (nw != null) {
	 if (do_debug) MintLogger.log("MASTER: Found @" + file + "@ " +
					 nw.getHost() + " " + nw.getPort() + " : " + cmd);
	 ps.println("USE\t" + nw.getHost() + "\t" + nw.getPort());
       }
      else {
	 if (do_debug) {
	    MintLogger.log("MASTER: Not found  : @" + file + "@" + cmd);
	    MintLogger.log("MASTER: Contents: " + server_hash);
	  }
	 ps.println("NO");
       }
    }
   else if (cmd.startsWith("LIST")) {
      for (String s : server_hash.keySet()) {
	 ps.println("LIST\t" + s);
       }
      ps.println("END");
    }
   else {
      MintLogger.log("MASTER: Command `" + cmd + "' not recognized");
    }
}




/********************************************************************************/
/*										*/
/*	Thread for listening on the socket					*/
/*										*/
/********************************************************************************/


private class SocketThread extends Thread {

   private ServerSocket server_socket = null;

   SocketThread(int port) {
      super("MintMasterSocket");
      try {
         InetAddress lcl = InetAddress.getLocalHost();
         // server_socket = new ServerSocket(port,5,lcl);
         server_socket = new ServerSocket(port,5);
         port = server_socket.getLocalPort();
   
         String fn = MintMaster.getMasterFile();
         File mf = new File(fn);
         FileWriter fw = new FileWriter(mf);
         PrintWriter pw = new PrintWriter(fw);
         mf.setWritable(true,false);
         pw.println(lcl.getHostAddress() + "\t" + port);
         pw.close();
         mf.setWritable(true,false);
         mf.deleteOnExit();
         MintLogger.log("MASTER: Server file set up as " + fn);
   
         try {
            HostPortImpl hpi = new HostPortImpl(lcl.getHostAddress(),port);
            // HostPort hp = (HostPort) UnicastRemoteObject.exportObject(hpi,0);
            Registry r = LocateRegistry.getRegistry();
            // Naming.rebind(MINT_REGISTRY_PROP,hpi);
            System.setProperty("java.rmi.server.codebase","file:///research/ivy/lib/ivy.jar");
            r.rebind(MINT_REGISTRY_PROP,hpi);
            String [] nms = r.list();
            int ctr = 0;
            for (String s : nms) {
               if (s.startsWith(MINT_REGISTRY_PREFIX) || s.startsWith("MintMaster[")) {
        	  try {
        	     r.unbind(s);
        	   }
        	  catch (Throwable t) {
        	     int idx = s.indexOf("]");
        	     String v = s.substring(idx+1);
        	     if (v.length() > 0) {
        		ctr = Math.max(ctr,Integer.parseInt(v)+1);
        	      }
        	   }
        	}
             }
            String nm = MINT_REGISTRY_PREFIX + lcl.getHostAddress() + "@" + port + "]" + ctr;
            r.rebind(nm,hpi);
            System.err.println("RMI SUCCESSFULLY BOUND");
          }
         catch (RemoteException e) {
            MintLogger.log("MASTER: RMI service not available: " + e);
          }
   
         if (do_debug) {
            MintLogger.log("MASTER: Server set up on " + server_socket.toString() +
        		      " " + server_socket.getInetAddress().isAnyLocalAddress() +
        		      " FILE " + fn);
          }
       }
      catch (IOException e) {
         MintLogger.log("MASTER: Server failed: " + e.getMessage());
         System.exit(0);
       }
    }

   @Override public void run() {
      Socket s;
      while (server_socket != null) {
	 try {
	    s = server_socket.accept();
	    handleConnection(s);
	  }
	 catch (SocketTimeoutException e) { }
	 catch (IOException e) {
	    break;
	  }
       }
      MintLogger.log("MASTER: No server socket");
    }

}	// end of subclass SocketThread




/********************************************************************************/
/*										*/
/*	Thread for reading from and replying to the socket			*/
/*										*/
/********************************************************************************/

private class SocketWorker extends Thread {

   private Socket use_socket;
   private String file_name;
   private String host_name;
   private int port_number;

   SocketWorker(Socket s) {
      super("MintWorkerSocket_" + s);
      use_socket = s;
      file_name = null;
      host_name = null;
      port_number = 0;
    }

   @Override public void run() {
      try {
	 InputStream ins = use_socket.getInputStream();
	 BufferedInputStream bins = new BufferedInputStream(ins);
	 InputStreamReader insr = new InputStreamReader(bins);
	 BufferedReader lnr = new BufferedReader(insr);

	 OutputStream ots = use_socket.getOutputStream();
	 BufferedOutputStream bots = new BufferedOutputStream(ots);
	 PrintStream ps = new PrintStream(bots,false);

	 int nullct = 0;
	 for ( ; ; ) {
	    String cmd = null;
	    try {
	       cmd = lnr.readLine();
	     }
	    catch (IOException e) {
	       if (do_debug) MintLogger.log("MASTER: IO error from server: " + e);
	     }
	    if (cmd == null) {			// null/error on wake up from sleep
	       if (nullct++ < 3) continue;
	     }
	    processCommand(cmd,this,ps);
	    if (cmd == null) break;
	    ps.flush();
	    nullct = 0;
	  }
	 lnr.close();
	 ps.close();
	 use_socket.close();
       }
      catch (IOException e) { }
    }

   void setConn(String id,String host,int port) {
      file_name = id;
      host_name = host;
      port_number = port;
    }

   String getFile()			{ return file_name; }
   String getHost()			{ return host_name; }
   int getPort()			{ return port_number; }

}	// end of subclass SocketWorker




/********************************************************************************/
/*										*/
/*	Host-Port class for naming server					*/
/*										*/
/********************************************************************************/

public static class HostPortImpl extends UnicastRemoteObject implements HostPort {

   private String host_name;
   private int port_number;

   private static final long serialVersionUID = 1;


   HostPortImpl(String h,int p) throws RemoteException {
      host_name = h;
      port_number = p;
    }

   @Override public String getHost() throws RemoteException	{ return host_name; }
   @Override public int getPort() throws RemoteException		{ return port_number; }

}	// end of subclass HostPort




}	// end of class MintServerMaster




/* end of MintServerMaster.java */

