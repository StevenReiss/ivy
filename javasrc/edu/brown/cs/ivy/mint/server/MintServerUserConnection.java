/********************************************************************************/
/*										*/
/*		MintServerUserConnection.java					*/
/*										*/
/*	Class to handle a connection to a Mint client				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/server/MintServerUserConnection.java,v 1.8 2018/08/02 15:10:30 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintServerUserConnection.java,v $
 * Revision 1.8  2018/08/02 15:10:30  spr
 * Fix imports.
 *
 * Revision 1.7  2015/11/20 15:09:20  spr
 * Reformatting.
 *
 * Revision 1.6  2015/04/08 13:51:45  spr
 * Code clean up.
 *
 * Revision 1.5  2013/09/24 01:06:59  spr
 * Minor fix
 *
 * Revision 1.4  2012-01-28 03:17:58  spr
 * Formatting
 *
 * Revision 1.3  2011-05-27 19:32:45  spr
 * Change copyrights.
 *
 * Revision 1.2  2011-05-18 23:33:00  spr
 * Fixes for mint web interface.
 *
 * Revision 1.1  2011-05-17 01:05:07  spr
 * Mint server to support web-scale messages.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint.server;


import edu.brown.cs.ivy.mint.MintConnect;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintLogger;
import edu.brown.cs.ivy.mint.MintSelector;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;



class MintServerUserConnection extends Thread implements MintServerConnection, MintConstants
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private MintServer for_server;
private Socket client_socket;
private LinkedList<ClientData> message_queue;
private Map<Integer,MintSelector> active_patterns;
private boolean is_valid;

private int	MAX_QUEUE_SIZE = 1024;


private boolean debug_output;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MintServerUserConnection(MintServer ms,Socket s,boolean dbg)
{
   super("MintServerUserConnection_" + s.getRemoteSocketAddress());

   for_server = ms;
   client_socket = s;
   message_queue = new LinkedList<>();
   active_patterns = new HashMap<>();
   is_valid = true;
   debug_output = dbg;
}



@Override public void finish()				{ }



/********************************************************************************/
/*										*/
/*	Methods for managing the message queue for this client			*/
/*										*/
/********************************************************************************/

@Override public void queueMessage(MintServerMessage msg)
{
   for (Map.Entry<Integer,MintSelector> ent : active_patterns.entrySet()) {
      Integer pid = ent.getKey();
      MintSelector pat = ent.getValue();
      if (pat.testMatchMessage(msg)) {
	 if (debug_output) MintLogger.log("Match " + pat);
	 msg.addClient();
	 addToQueue(new MessageInfo(msg,pid.intValue()));
       }
      else {
	 // if (debug_output) MingLogger.log("NO match " + pat + " :: " + msg.getText());
       }
    }
}




@Override public void queueReply(MintServerMessage msg,String rply)
{
   addToQueue(new ReplyInfo(msg,rply));
}



@Override public void queueDone(MintServerMessage msg)
{
   addToQueue(new DoneInfo(msg));
}



private void addToQueue(ClientData o)
{
   if (debug_output) MintLogger.log("Add to queue: " + message_queue.size() + " " + o);

   synchronized (message_queue) {
      if (message_queue.size() > MAX_QUEUE_SIZE) {
	 MintLogger.log("QUEUE TOO LONG");
       }
      message_queue.addLast(o);
      message_queue.notifyAll();
    }
}


private ClientData getNextMessage()
{
   ClientData cd = null;

   synchronized (message_queue) {
      while (message_queue.size() == 0) {
	 try {
	    message_queue.wait();
	  }
	 catch (InterruptedException e) { }
	 if (!is_valid) return null;
       }

      cd = message_queue.removeFirst();
    }

   return cd;
}



private void markInvalid()
{
   synchronized (message_queue) {
      is_valid = false;
      message_queue.notifyAll();
    }
}



/********************************************************************************/
/*										*/
/*	Methods for handling incoming requests					*/
/*										*/
/********************************************************************************/

private synchronized void processInput(String cmd,String body)
{
   if (cmd == null || cmd.length() == 0) return;

   if (debug_output)
      MintLogger.log("Receive [" + client_socket + "] :\n\t" + cmd + " => " + body);

   StringTokenizer tok = new StringTokenizer(cmd," ");
   String c = tok.nextToken();

   try {
      if (c.equals(MINT_HEADER_SEND) && body != null) {
	 int rid = Integer.parseInt(tok.nextToken());
	 int fgs = Integer.parseInt(tok.nextToken());
	 MintServerMessage msg = new MintServerMessage(for_server,this, body,rid,fgs);
	 for_server.handleMessage(msg);
       }
      else if (c.equals(MINT_HEADER_REGISTER) && body != null) {
	 int pid = Integer.parseInt(tok.nextToken());
	 active_patterns.put(Integer.valueOf(pid),MintConnect.createSelector(body));
       }
      else if (c.equals(MINT_HEADER_UNREGISTER)) {
	 int pid = Integer.parseInt(tok.nextToken());
	 active_patterns.remove(Integer.valueOf(pid));
       }
      else if (c.equals(MINT_HEADER_REPLY)) {
	 int rid = Integer.parseInt(tok.nextToken());
	 for_server.handleReply(rid,body);
       }
      else {
	 MintLogger.log("Unknown command: " + c);
       }
    }
   catch (NoSuchElementException e) { }
}




/********************************************************************************/
/*										*/
/*	Run method for handling input/output to the socket			*/
/*										*/
/********************************************************************************/

@Override public void run()
{
   if (client_socket == null) return;

   StringBuffer body = null;

   MessageThread mth = new MessageThread();
   mth.start();

   try {
      InputStream ins = client_socket.getInputStream();
      BufferedInputStream bins = new BufferedInputStream(ins);
      InputStreamReader insr = new InputStreamReader(bins);
      BufferedReader lnr = new BufferedReader(insr);

      for ( ; ; ) {
	 String cmd = lnr.readLine();
	 if (cmd == null) break;
	 body = null;
	 boolean ignore = false;
	 for ( ; ; ) {
	    String s = lnr.readLine();
	    if (s == null) break;
	    if (s.equals(MINT_TRAILER)) break;
	    if (ignore) continue;
	    if (body == null) body = new StringBuffer(s);
	    else {
	       try {
		  body.append("\n");
		  body.append(s);
		}
	       catch (OutOfMemoryError e) {
		  MintLogger.log("Message too long for client " + getName());
		  body = null;
		  ignore = true;
		}
	     }
	  }
	 String bs = (body == null ? null : body.toString());

	 processInput(cmd,bs);
       }
    }
   catch (IOException e) { }
   catch (OutOfMemoryError e) {
      MintLogger.log("Message too long for client " + getName());
      if (body != null) {
	 MintLogger.log("Current message: " + body.toString());
       }
    }

   for_server.removeConnection(this);

   markInvalid();

   while (!message_queue.isEmpty()) {
      ClientData cd = message_queue.removeFirst();
      cd.handleClose();
    }
}



/********************************************************************************/
/*										*/
/*	Thread to handle sending messages					*/
/*										*/
/********************************************************************************/

private class MessageThread extends Thread {

   MessageThread() {
      super("MintMessageThread_" + client_socket.getRemoteSocketAddress());
    }

   @Override public void run() {
      try {
         OutputStream ots = client_socket.getOutputStream();
         BufferedOutputStream bots = new BufferedOutputStream(ots);
         OutputStreamWriter osw = new OutputStreamWriter(bots);
         PrintWriter pw = new PrintWriter(osw,true);
   
         for ( ; ; ) {
            ClientData cd = getNextMessage();
            if (cd == null) break;
            if (debug_output) MintLogger.log("Send " + cd);
            cd.sendData(pw);
            if (pw.checkError()) {
               MintLogger.log("Error sending message " + cd);
               addToQueue(cd);
               break;
             }
          }
       }
      catch (IOException e) { }
    }

}	 // end of subclass MessageThread




/********************************************************************************/
/*										*/
/*	Classes to handle the data for sending to the client			*/
/*										*/
/********************************************************************************/

private abstract class ClientData {

   protected MintServerMessage for_message;

   protected ClientData(MintServerMessage m)		{ for_message = m; }

   abstract void sendData(PrintWriter pw);

   void handleClose()					{ }

}


private class MessageInfo extends ClientData {

   private int pattern_id;

   MessageInfo(MintServerMessage msg,int pid) {
      super(msg);
      pattern_id = pid;
    }

   @Override void sendData(PrintWriter pw) {
      pw.println(MINT_HEADER_GET + " " + pattern_id + " " +
        	    for_message.getReplyId());
      
   // pw.println(for_message.getText());
      String s = for_message.getText();
      for (int i = 0; i < s.length(); i += 16384) {
         int len = s.length() - i;
         if (len > 16384) len = 16384;   
         pw.write(s,i,len);
       }
      pw.println();
      
      pw.println(MINT_TRAILER);
      pw.flush();
    }

   @Override void handleClose() {
      if (for_message.getReplyId() != MINT_REPLY_NONE) {
	 for_message.replyTo((String) null);
       }
    }

   @Override public String toString()	{ return "MSG " + pattern_id + " " + for_message.getReplyId() +
				     ": " + for_message.getText(); }


}	// end of subclass MessageInfo



private class ReplyInfo extends ClientData {

   private String reply_text;

   ReplyInfo(MintServerMessage msg,String rply) {
      super(msg);
      reply_text = rply;
    }

   @Override void sendData(PrintWriter pw) {
      if (for_message.getLocalReplyId() != MINT_REPLY_NONE) {
	 pw.println(MINT_HEADER_REPLY + " " + for_message.getLocalReplyId());
	 if (reply_text != null) pw.println(reply_text);
	 pw.println(MINT_TRAILER);
	 pw.flush();
       }
    }

   @Override public String toString()	{ return "REPLY " + for_message.getLocalReplyId() + "," +
				     for_message.getReplyId() + ": " + reply_text; }

}	// end of subclass ReplyInfo



private class DoneInfo extends ClientData {

   DoneInfo(MintServerMessage msg)		{ super(msg); }

   @Override void sendData(PrintWriter pw) {
      if (for_message.getLocalReplyId() != MINT_REPLY_NONE) {
	 pw.println(MINT_HEADER_DONE + " " + for_message.getLocalReplyId());
	 pw.println(MINT_TRAILER);
	 pw.flush();
       }
    }

   @Override public String toString()	{ return "DONE " + for_message.getLocalReplyId() + "," +
				     for_message.getReplyId(); }

}	// end of subclass DoneInfo




}	// end of class MintServeConnection




/* end of MintServerUserConnection.java */
