/********************************************************************************/
/*										*/
/*		MintServer.java 						*/
/*										*/
/*	Main program implmentation of the Mint message server			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/server/MintServer.java,v 1.15 2018/08/02 15:10:30 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintServer.java,v $
 * Revision 1.15  2018/08/02 15:10:30  spr
 * Fix imports.
 *
 * Revision 1.14  2016/03/22 13:10:05  spr
 * Formatting
 *
 * Revision 1.13  2015/11/20 15:09:20  spr
 * Reformatting.
 *
 * Revision 1.12  2013/11/15 02:39:11  spr
 * Fix imports
 *
 * Revision 1.11  2011-05-27 19:32:45  spr
 * Change copyrights.
 *
 * Revision 1.10  2011-05-18 23:32:59  spr
 * Fixes for mint web interface.
 *
 * Revision 1.9  2011-05-17 01:05:07  spr
 * Mint server to support web-scale messages.
 *
 * Revision 1.8  2010-08-20 20:58:34  spr
 * Add logging and options for port numbers
 *
 * Revision 1.7  2010-08-09 17:10:38  spr
 * Check that we are using all local addresses.
 *
 * Revision 1.6  2010-08-04 22:02:01  spr
 * Master server not ready for localhost.
 *
 * Revision 1.5  2010-07-01 21:56:16  spr
 * Clear socket when done
 *
 * Revision 1.4  2007-08-10 02:11:14  spr
 * Cleanups from eclipse; bug fixes to avoid deadlock.
 *
 * Revision 1.3  2006-12-01 03:22:51  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.2  2006/02/21 17:06:28  spr
 * Upgrade interface to use Element instead of Node for XML.
 *
 * Revision 1.1  2005/07/08 23:33:08  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint.server;


import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.mint.MintConnect;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintLogger;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;



public class MintServer implements MintConstants
{



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   MintServer ms = new MintServer(args);

   ms.process();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String file_name;
private String host_name;
private int port_number;
private SocketThread socket_thread;
private boolean debug_output;
private int	num_special;

private Collection<MintServerConnection> active_connections;
private Map<Integer,MintServerMessage> active_messages;
private MintPolicyManager policy_manager;
private MintContextManager context_manager;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private MintServer(String [] args)
{
   try {
      host_name = InetAddress.getLocalHost().getHostName();
    }
   catch (UnknownHostException e) { host_name = "localhost"; }

   debug_output = false;
   port_number = 0;
   socket_thread = null;
   file_name = MINT_DEFAULT_SERVICE_NAME + "_" + System.getProperty("user.name") +
      "_" + host_name;

   active_connections = new HashSet<MintServerConnection>();
   num_special = 0;
   active_messages = new HashMap<Integer,MintServerMessage>();
   policy_manager = new MintPolicyManager(this);
   context_manager = new MintContextManager(this);

   String file;

   String base = System.getProperty("edu.brown.cs.ivy.mint.mintdir");
   if (base == null) base = System.getProperty("user.home") + File.separator + ".Mint";

   file = base + File.separator + MINT_XML_DEFAULT_FILE;
   policy_manager.addPolicyFile(file);

   file = IvyFile.expandName("$(IVY)/lib/mint/" + MINT_XML_DEFAULT_FILE);
   policy_manager.addPolicyFile(file);

   file = System.getProperty(MINT_XML_ENVIRONMENT);
   policy_manager.addPolicyFile(file);

   String flag = System.getProperty("edu.brown.cs.ivy.mint.debug");
   if (flag == null) flag = System.getenv("BROWN_IVY_MINT_DEBUG");

   if (flag != null) {
      if (flag.startsWith("t") || flag.startsWith("T") || flag.startsWith("1") ||
	     flag.startsWith("y") || flag.startsWith("Y"))
	 debug_output = true;
    }

   scanArgs(args);
}



/********************************************************************************/
/*										*/
/*	Argument processing methods						*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   int ct = args.length;

   for (int i = 0; i < ct; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-p") && i+1 < ct) {            // -port <#>
	    port_number = Integer.parseInt(args[++i]);
	  }
	 else if (args[i].startsWith("-P") && i+1 < ct) {       // -Policy <file>
	    policy_manager.addPolicyFile(args[++i]);
	  }
	 else if (args[i].startsWith("-d")) {                   // -debug
	    debug_output = true;
	  }
	 else {
	    MintLogger.log("Illegal argument: " + args[i]);
	  }
       }
      else {
	 file_name = args[i];
       }
    }
}



/********************************************************************************/
/*										*/
/*	Main loop processing methods						*/
/*										*/
/********************************************************************************/

private void process()
{
   try {
      // ServerSocket ss = new ServerSocket(port_number,5,InetAddress.getLocalHost());
      ServerSocket ss = new ServerSocket(port_number,5);

      if (!MintConnect.registerSocket(file_name,ss)) return;

      socket_thread = new SocketThread(ss);
      socket_thread.start();
    }
   catch (IOException e) { }
}



private synchronized void handleConnection(Socket s)
{
   if (debug_output)
      MintLogger.log("Handle connection " + s);

   MintServerConnection msc = new MintServerUserConnection(this,s,debug_output);
   active_connections.add(msc);
   msc.start();
}



synchronized void removeConnection(MintServerConnection msc)
{
   active_connections.remove(msc);

   if (active_connections.size() <= num_special) {
      for (MintServerConnection c : active_connections) {
	 c.finish();
       }
      if (debug_output) MintLogger.log("All connections to MintServer dropped");
      System.exit(0);
    }
}



synchronized void handleMessage(MintServerMessage msg)
{
   if (debug_output)
      MintLogger.log("Message(" + msg.getReplyId() + "): " + msg.getText());

   if (policy_manager != null && !policy_manager.filterMessage(msg)) return;
   if (context_manager != null && !context_manager.handleMessage(msg)) return;

   int id = msg.getReplyId();
   if (id > 0) active_messages.put(Integer.valueOf(id),msg);

   for (MintServerConnection msc : active_connections) {
      msc.queueMessage(msg);
    }

   if (policy_manager != null) policy_manager.handleMessage(msg);

   msg.finishSend();
}



synchronized void removeMessage(MintServerMessage msg)
{
   int id = msg.getReplyId();
   if (id > 0) active_messages.remove(Integer.valueOf(id));
}



synchronized void addSpecialConnection(MintServerConnection msc)
{
   active_connections.add(msc);
   ++num_special;
   msc.start();
}


/********************************************************************************/
/*										*/
/*	Methods for handling replies from clients				*/
/*										*/
/********************************************************************************/

void handleReply(int rid,String rply)
{
   MintServerMessage msg = getMessage(rid);

   if (debug_output) MintLogger.log("Reply (" + rid + "): " + rply);

   if (msg != null) msg.replyTo(rply);
}



private synchronized MintServerMessage getMessage(int rid)
{
   return active_messages.get(Integer.valueOf(rid));
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getMintName()			{ return file_name; }




/********************************************************************************/
/*										*/
/*	Thread for listening on the socket					*/
/*										*/
/********************************************************************************/

private class SocketThread extends Thread {

   private ServerSocket server_socket = null;

   SocketThread(ServerSocket ss) {
      super("MintSocketListener");
      server_socket = ss;
      MintLogger.log("MintServer (" + file_name + ") set up on " +
        		server_socket.toString() + " " +
        		server_socket.getInetAddress().isAnyLocalAddress());
    }

   @Override public void run() {
      Socket s;
      while (server_socket != null) {
	 try {
	    s = server_socket.accept();
	    handleConnection(s);
	  }
	 catch (IOException e) { }
       }
    }
   
}	// end of subclass SocketThread




}	// end of class MintServer




/* end of MintServer.java */
