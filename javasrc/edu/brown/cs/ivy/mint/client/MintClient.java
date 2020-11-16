/********************************************************************************/
/*										*/
/*		MintClient.java 						*/
/*										*/
/*	Basic implementation of MintControl for inside a client application	*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/client/MintClient.java,v 1.32 2018/08/02 15:10:28 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintClient.java,v $
 * Revision 1.32  2018/08/02 15:10:28  spr
 * Fix imports.
 *
 * Revision 1.31  2017/03/14 14:00:30  spr
 * Formattig
 *
 * Revision 1.30  2017/02/21 23:13:47  spr
 * Formatting.
 *
 * Revision 1.29  2015/11/20 15:09:18  spr
 * Reformatting.
 *
 * Revision 1.28  2014/06/12 01:06:31  spr
 * Minor updates
 *
 * Revision 1.27  2013/11/15 02:39:07  spr
 * Fix imports
 *
 * Revision 1.26  2013/09/24 01:06:58  spr
 * Minor fix
 *
 * Revision 1.25  2012-06-14 12:39:46  spr
 * Check connections.
 *
 * Revision 1.24  2012-05-22 00:43:02  spr
 * Formatting changes.
 *
 * Revision 1.23  2012-04-26 01:02:41  spr
 * Formating, thread naming
 *
 * Revision 1.22  2012-02-29 01:53:57  spr
 * Code clean up.
 *
 * Revision 1.21  2012-01-12 01:26:25  spr
 * Formatting
 *
 * Revision 1.20  2011-05-27 19:32:43  spr
 * Change copyrights.
 *
 * Revision 1.19  2010-12-08 22:50:30  spr
 * Add thread pool
 *
 * Revision 1.18  2010-12-03 21:57:22  spr
 * Use a thread pool to avoid new thread creation.
 *
 * Revision 1.17  2010-08-20 20:58:28  spr
 * Add logging and options for port numbers
 *
 * Revision 1.16  2010-08-04 22:01:59  spr
 * Master server not ready for localhost.
 *
 * Revision 1.15  2010-07-01 21:55:57  spr
 * Don't allow mint client to be superclassed
 *
 * Revision 1.14  2010-06-01 02:08:39  spr
 * Force load to handle dyvise monitoring of ivy-based apps.
 *
 * Revision 1.13  2010-02-12 00:37:28  spr
 * Move int constants to enums
 *
 * Revision 1.12  2009-09-17 01:59:15  spr
 * Use IVY setup and IvyExec.runJava for running mint (for windows).
 *
 * Revision 1.11  2009-03-20 01:58:00  spr
 * Throw error rather than terminating on server disconnectd.
 *
 * Revision 1.10  2008-11-12 13:46:20  spr
 * Minor cleanups and fixes.
 *
 * Revision 1.9  2008-03-14 12:27:12  spr
 * Synchronize writes.
 *
 * Revision 1.8  2007-09-22 01:47:39  spr
 * Clean up error handling so it isn't synchronized.
 *
 * Revision 1.7  2007-08-10 02:11:09  spr
 * Cleanups from eclipse; bug fixes to avoid deadlock.
 *
 * Revision 1.6  2007-05-04 02:00:13  spr
 * Fix bugs related to polling.
 *
 * Revision 1.5  2006/07/10 14:52:21  spr
 * Code cleanup.
 *
 * Revision 1.4  2006/02/21 17:06:25  spr
 * Upgrade interface to use Element instead of Node for XML.
 *
 * Revision 1.3  2006/01/30 19:05:52  spr
 * Handle sync only on replies option.
 *
 * Revision 1.2  2005/09/02 14:42:46  spr
 * Make threads daemons.
 *
 * Revision 1.1  2005/07/08 23:32:57  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint.client;


import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintConnect;
import edu.brown.cs.ivy.mint.MintControl;
import edu.brown.cs.ivy.mint.MintErrorHandler;
import edu.brown.cs.ivy.mint.MintHandler;
import edu.brown.cs.ivy.mint.MintLogger;
import edu.brown.cs.ivy.mint.MintMaster;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.mint.MintReply;
import edu.brown.cs.ivy.mint.MintSelector;
import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public final class MintClient extends MintControl
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ReaderThread server_reader;
private ProcessThread message_handler;
private ReplyThread reply_handler;
private List<Object> message_queue;
private List<Object> reply_queue;
private MintSyncMode synch_mode;
private PrintWriter server_writer;
private Map<Integer,ActiveInfo> reply_hash;
private Map<Integer,PatternInfo> pattern_hash;
private int reply_counter;
private int pat_counter;
private AtomicInteger thread_counter;
private MintErrorHandler error_handler;
private String mint_name;
private ExecutorService thread_pool;

private static int process_counter;

private Object	write_lock;

static {
   MintConnect.createSelector("<TEST/>");       // force load
}


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public MintClient(String id,MintSyncMode mode)
{
   // MintSyncMode.MULTIPLE DOESN'T WORK FOR REPLY DONE MESSAGES
   if (mode == MintSyncMode.MULTIPLE) mode = MintSyncMode.ONLY_REPLIES;

   synch_mode = mode;
   message_queue = new LinkedList<>();
   reply_queue = new LinkedList<>();
   reply_hash = new HashMap<>();
   pattern_hash = new HashMap<>();
   reply_counter = 1;
   pat_counter = 1;
   mint_name = id;
   write_lock = new Object();
   thread_counter = new AtomicInteger();
   thread_pool = null;

   server_reader = null;
   server_writer = null;
   message_handler = null;
   reply_handler = null;
   error_handler = null;

   MintClientServer mcs = new MintClientServer(id);

   server_writer = mcs.getWriter();
   server_reader = new ReaderThread(mcs.getReader());

   if (server_writer == null) {
      MintLogger.log("Can't establish connection to server");
      throw new Error("MINT: Can't establish connection to server for " + id + " :: " +
			 MintMaster.getServerInfo());
    }

   switch (mode) {
      case NONE :
	 server_reader.start();
	 break;
      case POLL :
      case POLL_REPLIES :
	 break;
      case SINGLE :
      case MULTIPLE :
      case ONLY_REPLIES :
	 server_reader.start();
	 message_handler = new ProcessThread();
	 message_handler.start();
	 reply_handler = new ReplyThread();
	 reply_handler.start();
	 break;
      case REPLIES :
	 server_reader.start();
	 message_handler = new ProcessThread();
	 message_handler.start();
	 break;
    }
}



/********************************************************************************/
/*										*/
/*	Methods to shut down							*/
/*										*/
/********************************************************************************/

@Override public synchronized void shutDown()
{
   pollReplies();
   while (pollNext(false)) pollReplies();
   pollReplies();

   if (server_reader != null) {
      server_reader.interrupt();
      server_reader = null;
    }

   if (message_handler != null) {
      message_handler.interrupt();
      message_handler = null;
    }

   if (reply_handler != null) {
      reply_handler.interrupt();
      reply_handler = null;
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getMintName()		{ return mint_name; }



/********************************************************************************/
/*										*/
/*	Methods for sending a message						*/
/*										*/
/********************************************************************************/

@Override public void send(String msg,MintReply rply,int fgs)
{
   int rid;

   if (fgs == MINT_MSG_NONE) {
      if (rply == null) fgs = MINT_MSG_NO_REPLY;
      else fgs = MINT_MSG_FIRST_NON_NULL;
    }

   if (msg == null) return;

   if ((fgs & MINT_MSG_NO_REPLY) != 0) rply = null;

   synchronized (write_lock) {
      if (rply == null) rid = MINT_REPLY_NONE;
      else {
	 rid = reply_counter++;
	 MintMessage mm = new MintClientMessage(this,msg,rid);
	 reply_hash.put(Integer.valueOf(rid),new ActiveInfo(mm,rply));
       }

      server_writer.println(MINT_HEADER_SEND + " " + rid + " " + fgs);
      server_writer.println(msg);
      server_writer.println(MINT_TRAILER);
    }
}




@Override public void send(Element msg,MintReply rply,int fgs)
{
   if (msg == null) return;

   send(IvyXml.convertXmlToString(msg),rply,fgs);
}



private static class ActiveInfo {

   private MintMessage for_message;
   private MintReply reply_handler;

   ActiveInfo(MintMessage mm,MintReply mr) {
      for_message = mm;
      reply_handler = mr;
    }

   MintMessage getMessage()			{ return for_message; }
   MintReply getReplyHandler()			{ return reply_handler; }

}	// end of subclass ActiveInfo




/********************************************************************************/
/*										*/
/*	Methods to handle pattern registration					*/
/*										*/
/********************************************************************************/

@Override public void register(String pattern,MintHandler hdlr)
{
   handleRegister(pattern,IvyXml.convertStringToXml(pattern),hdlr);
}



@Override public void register(Element pat,MintHandler hdlr)
{
   handleRegister(IvyXml.convertXmlToString(pat),pat,hdlr);
}



private synchronized void handleRegister(String pattern,Element xml,MintHandler hdlr)
{
   if (pattern == null || hdlr == null) return;

   String xpat = IvyXml.convertXmlToString(xml);
   if (pattern.contains("''") || xpat.contains("''")) {
      MintLogger.log("Bad pattern: " + xpat + " :: " + pattern);
    }

   synchronized (write_lock) {
      int pid = pat_counter++;
      pattern_hash.put(Integer.valueOf(pid),new PatternInfo(xml,hdlr));
      server_writer.println(MINT_HEADER_REGISTER + " " + pid);
      server_writer.println(pattern);
      server_writer.println(MINT_TRAILER);
    }
}



private static class PatternInfo {

   private MintHandler use_handler;
   private MintSelector pattern_matcher;

   PatternInfo(Element x,MintHandler hdlr) {
      use_handler = hdlr;
      pattern_matcher = MintConnect.createSelector(x);
    }

   public MintHandler getHandler()		{ return use_handler; }
// public MintSelector getPattern()		{ return pattern_matcher; }
   public boolean match(MintMessage msg) {
      if (pattern_matcher == null) return false;
      MintArguments args = pattern_matcher.matchMessage(msg);
   // MintLogger.log("MATCH " + args + " " + use_handler);
      if (args == null) {
         MintLogger.log("MESSAGE: " + msg.getText());
         MintLogger.log("PATTERN: " + pattern_matcher.getText());
         MintLogger.log("PATTERNX: " + IvyXml.convertXmlToString(pattern_matcher.getXml()));
       }
      if (args == null) return false;
      if (use_handler != null) {
         try {
            use_handler.receive(msg,args);
          }
         catch (Throwable t) {
            MintLogger.log("Problem in MintHandler: " + t);
            t.printStackTrace()   ;
          }
       }
   
      return true;
    }

}	// end of subclass PatternInfo




/********************************************************************************/
/*										*/
/*	Methods to handle pattern unregistration				*/
/*										*/
/********************************************************************************/

@Override public void unregister(MintHandler hdlr)
{
   synchronized (write_lock) {
      for (Iterator<Map.Entry<Integer,PatternInfo>> it = pattern_hash.entrySet().iterator(); it.hasNext(); ) {
	 Map.Entry<Integer,PatternInfo> ent = it.next();
	 Integer id = ent.getKey();
	 PatternInfo pi = ent.getValue();
	 if (pi != null && pi.getHandler() == hdlr) {
	    it.remove();
	    server_writer.println(MINT_HEADER_UNREGISTER + " " + id.intValue());
	    server_writer.println(MINT_TRAILER);
	  }
       }
    }
}



/********************************************************************************/
/*										*/
/*	Methods to generate replies						*/
/*										*/
/********************************************************************************/

void generateReply(int rid,String r)
{
   if (rid == MINT_REPLY_NONE) return;

   synchronized (write_lock) {
      server_writer.println(MINT_HEADER_REPLY + " " + rid);
      if (r != null) server_writer.println(r);
      server_writer.println(MINT_TRAILER);
    }
}



void generateReply(int rid,Element r)
{
   generateReply(rid,IvyXml.convertXmlToString(r));
}



/********************************************************************************/
/*										*/
/*	Methods for error handling						*/
/*										*/
/********************************************************************************/

@Override public void registerErrorHandler(MintErrorHandler hdlr)
{
   error_handler = hdlr;
}



private void serverError(String what)
{
   if (error_handler == null) {
      MintLogger.log("I/O error from the server: " + what);
      MintLogger.log("Shutting down...");
      System.exit(0);
    }
   else {
      error_handler.handleServerError(what);
    }
}




/********************************************************************************/
/*										*/
/*	Methods to handle messages from the server				*/
/*										*/
/********************************************************************************/

private void handleMessage(int mid,int rid,Element xml)
{
   queueMessage(new MessageInfo(mid,rid,xml));
}


private void handleReply(int rid,Element doc)
{
   ReplyInfo r = new ReplyInfo(rid,doc);

   switch (synch_mode) {
      case NONE :
	 break;
      case SINGLE :
      case MULTIPLE :
      case POLL_REPLIES :
      case ONLY_REPLIES :
	 queueReply(r);
	 break;
      case REPLIES :
      case POLL :
	 queueMessage(r);
	 break;
    }
}



private void handleDone(int rid)
{
   DoneInfo di = new DoneInfo(rid);

   switch (synch_mode) {
      case NONE :
	 break;
      case SINGLE :
      case MULTIPLE :
      case POLL_REPLIES :
      case ONLY_REPLIES :
	 queueReply(di);
	 break;
      case REPLIES :
      case POLL :
	 queueMessage(di);
	 break;
    }
}




private synchronized void queueMessage(Object m)
{
   message_queue.add(m);

   notifyAll();
}



private synchronized void queueReply(Object m)
{
   reply_queue.add(m);

   notifyAll();
}




/********************************************************************************/
/*										*/
/*	Asynchronous message processing 					*/
/*										*/
/********************************************************************************/

private void asynchProcessMessage(Object o)
{
   synchronized (this) {
      if (thread_pool == null) {
	 thread_pool = new ThreadPoolExecutor(2,1000,10,TimeUnit.SECONDS,
	       new SynchronousQueue<Runnable>(),new RunThreadFactory());
      }
   }

   thread_pool.execute(new AsynchMessager(o));
   // Thread t = new RunThread(o);
   // t.start();
}



private class RunThreadFactory implements ThreadFactory {

   @Override public Thread newThread(Runnable r) {
      return new Thread(r,"MintRunThread_" + mint_name + "_" + thread_counter.addAndGet(1));
   }

}	// end of inner class RunThreadFactory




private class AsynchMessager implements Runnable {

   private Object process_item;

   AsynchMessager(Object o) {
      process_item = o;
   }

   @Override public void run() {
      processMessage(process_item);
   }

}	// end of inner class AynchMessager




/********************************************************************************/
/*										*/
/*	Polling interface							*/
/*										*/
/********************************************************************************/

@Override public int getNumPolledMessages()
{
   try {
      synchronized (this) {
	 server_reader.readPending(0);
	 return message_queue.size();
       }
    }
   catch (IOException e) {
      serverError(e.getMessage());
    }

   return 0;
}



@Override public boolean pollNext(boolean waitfg)
{
   Object o = getNextMessage(waitfg);
   boolean fg = false;

   if (o != null) {
      processMessage(o);
      fg = true;
    }

   return fg;
}



@Override public void pollReplies()
{
   Object o = getNextReply(false);

   if (o != null) processMessage(o);
}



private Object getNextMessage(boolean waitfg)
{
   Object o = null;

   try {
      synchronized (this) {
	 if (!waitfg && server_reader != null && !server_reader.isActive()) server_reader.readPending(0);

	 while (message_queue.size() == 0) {
	    if (!waitfg) return null;
	    try {
	       if (server_reader == null) return null;
	       else if (server_reader.isActive()) wait(10000);
	       else server_reader.readPending(1);
	     }
	    catch (InterruptedException e) {
	       return null;
	     }
	  }

	 o = message_queue.remove(0);
       }
    }
   catch (IOException e) {
      serverError(e.getMessage());
    }

   return o;
}



private Object getNextReply(boolean waitfg)
{
   Object o = null;

   try {
      synchronized (this) {
	 if (!waitfg && server_reader != null && !server_reader.isActive()) server_reader.readPending(0);

	 while (reply_queue.size() == 0) {
	    if (!waitfg) return null;
	    try {
	       if (server_reader == null) return null;
	       else if (server_reader.isActive()) wait(10000);
	       else server_reader.readPending(1);
	     }
	    catch (InterruptedException e) {
	       return null;
	     }
	  }

	 o = reply_queue.remove(0);
       }
    }
   catch (IOException e) {
      serverError(e.getMessage());
    }

   return o;

}



/********************************************************************************/
/*										*/
/*	Processing interface							*/
/*										*/
/********************************************************************************/

private void processMessage(Object o)
{
   if (o == null) ;
   else if (o instanceof MessageInfo) {
      MessageInfo mi = (MessageInfo) o;
      PatternInfo pi = null;
      synchronized (write_lock) {
	 pi = pattern_hash.get(Integer.valueOf(mi.getMessageId()));
       }
      if (pi != null) {
	 MintClientMessage msg;
	 msg = new MintClientMessage(this,mi.getMessage(),mi.getReplyId());
	 pi.match(msg);
       }
    }
   else if (o instanceof ReplyInfo) {
      ReplyInfo ri = (ReplyInfo) o;
      ActiveInfo ai = reply_hash.get(Integer.valueOf(ri.getReplyId()));
      if (ai == null) return;
      MintReply mr = ai.getReplyHandler();
      MintMessage mm = ai.getMessage();
      MintClientMessage rmsg;
      rmsg = new MintClientMessage(this,ri.getReply(),MINT_REPLY_NONE);
      try {
	 if (mr != null) mr.handleReply(mm,rmsg);
       }
      catch (Throwable t) {
	 MintLogger.log("Problem handling reply: " + t);
	 t.printStackTrace();
       }
    }
   else if (o instanceof DoneInfo) {
      DoneInfo di = (DoneInfo) o;
      ActiveInfo ai = reply_hash.get(Integer.valueOf(di.getDoneId()));
      if (ai == null) return;
      MintReply mr = ai.getReplyHandler();
      try {
	 if (mr != null) mr.handleReplyDone(ai.getMessage());
       }
      catch (Throwable t) {
	 MintLogger.log("Problem handling reply done: " + t);
	 t.printStackTrace();
       }
      reply_hash.remove(Integer.valueOf(di.getDoneId()));
    }
}




/********************************************************************************/
/*										*/
/*	Subclasses for queued information					*/
/*										*/
/********************************************************************************/

private static class MessageInfo {

   private int message_id;
   private int reply_id;
   private Element message_body;

   MessageInfo(int mid,int rid,Element xml) {
      message_id = mid;
      reply_id = rid;
      message_body = xml;
    }

   int getMessageId()			{ return message_id; }
   int getReplyId()			{ return reply_id; }
   Element getMessage() 		{ return message_body; }

}	// end of subclass MessageInfo



private static class ReplyInfo {

   private int reply_id;
   private Element reply_body;

   ReplyInfo(int rid,Element d) {
      reply_id = rid;
      reply_body = d;
    }

   int getReplyId()			{ return reply_id; }
   Element getReply()			{ return reply_body; }

}	// end of subclass ReplyInfo




private static class DoneInfo {

   private int reply_id;

   DoneInfo(int rid)			{ reply_id = rid; }

   int getDoneId()		       { return reply_id; }

}	// end of subclass DoneInfo




/********************************************************************************/
/*										*/
/*	Thread to handle messages						*/
/*										*/
/********************************************************************************/

private class ProcessThread extends Thread {

   ProcessThread() {
      super("MintProcessThread_" + (process_counter == 0 ? "" : Integer.toString(process_counter) + "_") + mint_name);
      ++process_counter;
      setDaemon(true);
    }

   @Override public void run() {
      for ( ; ; ) {
	 Object o = getNextMessage(true);
	 if (o == null && interrupted()) break;
	 if (o == null) continue;
	 if (synch_mode == MintSyncMode.MULTIPLE || synch_mode == MintSyncMode.ONLY_REPLIES) {
	    asynchProcessMessage(o);
	  }
	 else processMessage(o);
       }
    }

}	// end of subclass ProcessThread



private class ReplyThread extends Thread {

   ReplyThread() {
      super("MintReplyThread_" + mint_name);
      setDaemon(true);
    }

   @Override public void run() {
      for ( ; ; ) {
	 Object o = getNextReply(true);
	 if (o == null && interrupted()) break;
	 if (o == null) continue;
	 if (synch_mode == MintSyncMode.MULTIPLE) {
	    asynchProcessMessage(o);
	  }
	 else processMessage(o);
       }
    }

}	// end of subclass ReplyThread




/********************************************************************************/
/*										*/
/*	Thread to read from the server						*/
/*										*/
/********************************************************************************/

private class ReaderThread extends Thread {

   private BufferedReader line_reader;

   ReaderThread(BufferedReader lnr) {
      super("MintReaderThread_" + mint_name);
      setDaemon(true);
      line_reader = lnr;
    }

   boolean isActive() {
      return isAlive() && line_reader != null;
    }

   @Override public void run() {
      if (line_reader == null) return;

      try {
	 for ( ; ; ) {
	    String hdr = line_reader.readLine();
	    if (hdr == null) break;
	    StringBuffer body = null;
	    for ( ; ; ) {
	       String s = line_reader.readLine();
	       if (s == null || s.equals(MINT_TRAILER)) break;
	       if (body == null) body = new StringBuffer(s);
	       else {
		  body.append('\n');
		  body.append(s);
		}
	     }
	    String s = (body == null ? null : body.toString());
	    processItem(hdr,s);
	  }
       }
      catch (InterruptedIOException e) {
	 return;
       }
      catch (IOException e) {
	 serverError(e.getMessage());
	 return;
       }
      serverError("End of file from the server");
    }

   void readPending(int min) throws IOException {
      if (line_reader == null) return;		// terminated
      if (isAlive()) return;			// thread is running, let it do the reads

      for (int ctr = 0; ctr < min || line_reader.ready(); ++ctr) {
	 String hdr = line_reader.readLine();
	 if (hdr == null) {
	    throw new IOException("End of file from server");
	  }
	 StringBuffer body = null;
	 for ( ; ; ) {
	    String s = line_reader.readLine();
	    if (s == null || s.equals(MINT_TRAILER)) break;
	    if (body == null) body = new StringBuffer(s);
	    else {
	       body.append('\n');
	       body.append(s);
	     }
	  }
	 String s = (body == null ? null : body.toString());
	 processItem(hdr,s);
       }
    }

   private void processItem(String hdr,String body) {
      StringTokenizer tok = new StringTokenizer(hdr," ");
      if (!tok.hasMoreTokens()) return;
      String cmd = tok.nextToken();
   
      if (cmd.equals(MINT_HEADER_REPLY)) {
         if (tok.hasMoreTokens()) {
            int rid = Integer.parseInt(tok.nextToken());
            Element xml = IvyXml.convertStringToXml(body);
            handleReply(rid,xml);
          }
       }
      else if (cmd.equals(MINT_HEADER_DONE)) {
         if (tok.hasMoreTokens()) {
            int rid = Integer.parseInt(tok.nextToken());
            handleDone(rid);
          }
       }
      else if (cmd.equals(MINT_HEADER_GET)) {
         if (tok.hasMoreTokens()) {
            int mid = Integer.parseInt(tok.nextToken());
            if (tok.hasMoreTokens()) {
               int rid = Integer.parseInt(tok.nextToken());
               Element xml = IvyXml.convertStringToXml(body);
               handleMessage(mid,rid,xml);
             }
          }
       }
    }

}	// end of subclass ReaderThread




}	// end of class MintClient




/* end of MintClient.java */
