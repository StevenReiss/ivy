/********************************************************************************/
/*										*/
/*		MintWebServlet.java						*/
/*										*/
/*	Servlet for handling MINT messages between mint servers 		*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/web/MintWebServlet.java,v 1.11 2018/12/17 14:09:16 spr Exp $ */

/*********************************************************************************
 *
 * $Log: MintWebServlet.java,v $
 * Revision 1.11  2018/12/17 14:09:16  spr
 * Fix up web messaging
 *
 * Revision 1.10  2018/08/02 15:10:32  spr
 * Fix imports.
 *
 * Revision 1.9  2016/10/28 18:31:43  spr
 * Add annotation back in for now.
 *
 * Revision 1.8  2016/07/22 13:27:00  spr
 * Update makefiles for external use.  Remove annotation to get jdeps to work.
 *
 * Revision 1.7  2013/11/15 02:39:13  spr
 * Fix imports
 *
 * Revision 1.6  2012-01-12 01:27:16  spr
 * Avoid possible null pointer
 *
 * Revision 1.5  2011-05-27 19:32:47  spr
 * Change copyrights.
 *
 * Revision 1.4  2011-05-19 23:34:15  spr
 * Fix web connection debugging.
 *
 * Revision 1.3  2011-05-18 23:33:04  spr
 * Fixes for mint web interface.
 *
 * Revision 1.2  2011-05-18 01:02:12  spr
 * Clean up servlet code.
 *
 * Revision 1.1  2011-05-17 01:05:26  spr
 * Mint servlet for messaging.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.mint.web;



import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


@WebServlet(description = "mint web interconnect", urlPatterns = { "/mint" })
public class MintWebServlet extends HttpServlet implements MintWebConstants
{




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private transient Map<String,Map<String,MintWebUser>> known_connections;
private int					reply_counter;
private transient Timer                         connect_timer;

private transient Map<String,ReplyData>         reply_map;

private static final long TIME_OUT_INTERVAL = 30*1000;

private static boolean				do_echo = false;
private static boolean				do_debug = false;

private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public MintWebServlet()
{
   known_connections = new HashMap<String,Map<String,MintWebUser>>();
   reply_map = new HashMap<String,ReplyData>();

   reply_counter = 0;
}



/********************************************************************************/
/*										*/
/*	Servlet methods 							*/
/*										*/
/********************************************************************************/

@Override public void init()
{
   connect_timer = new Timer("MintWebTimeoutChecker");
   connect_timer.schedule(new ConnectionChecker(),1*60*1000,1*60*1000);
   known_connections.clear();
   reply_map.clear();
}


@Override public void destroy()
{
   connect_timer.cancel();
   connect_timer = null;
   known_connections.clear();
   reply_map.clear();
}


@Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)
{
   handleMessage(req,resp);
}


@Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)
{
   handleMessage(req,resp);
}


@Override protected void doPut(HttpServletRequest req,HttpServletResponse resp)
{
   handleMessage(req,resp);
}



/********************************************************************************/
/*										*/
/*	Message handling methods						*/
/*										*/
/********************************************************************************/

private void handleMessage(HttpServletRequest req,HttpServletResponse resp)
{
   String mid = req.getParameter("I");
   String uid = req.getParameter("U");
   String typ = req.getParameter("T");
   String rid = req.getParameter("R");
   String fgs = req.getParameter("F");

   MintWebUser mwu = null;
   MintWebMessage msg = null;
   Collection<MintWebUser> others = null;
   int fg = 0;
   if (fgs != null) fg = Integer.parseInt(fgs);

   mintLog("Handle message " + typ + " " + rid + " " + Thread.currentThread().getName());

   if (typ == null) {
      try {
	 ServletOutputStream sos = resp.getOutputStream();
	 sos.println("PONG");
	 sos.close();
       }
      catch (IOException e) { }
      return;
    }

   if (typ.equals("MSG") || typ.equals("RPLY")) {
      try {
	 msg = new MintWebMessage(req,fg);
       }
      catch (IOException e) {
	 System.err.println("MINT: Problem reading message from web: " + e);
       }
    }

   synchronized (known_connections) {
      Map<String,MintWebUser> um = known_connections.get(mid);
      if (um == null) {
	 um = new HashMap<String,MintWebUser>();
	 known_connections.put(mid,um);
       }
      mwu = um.get(uid);
      if (mwu == null) {
	 mintLog("Start connection " + mid + " " + uid + " " + um.size());
	 mwu = new MintWebUser();
	 um.put(uid,mwu);
       }
      if(!typ.equals("RECV") && !typ.equals("END")) others = new ArrayList<MintWebUser>(um.values());
    }

   if (typ.equals("RECV")) {
      try {
	 mwu.sendPending(resp);
       }
      catch (IOException e) {
	 System.err.println("MINT: Problem sending messages to client: " + e);
       }
    }
   else if (typ.equals("MSG")) {
      if (others == null) others = new ArrayList<MintWebUser>();
      String lid = getReplyId(mwu,rid,others.size()-1,fg);
      int ctr = 0;
      for (MintWebUser owu : others) {
	 if (do_echo || owu != mwu) {
	    owu.queueMessage(lid,msg);
	    ++ctr;
	  }
       }
      if (ctr == 0) {			// handle case where there are no other clients
	 ReplyData rd = getReplyData(lid);
	 while (!rd.isDone()) ;
	 removeReplyData(lid);
       }
    }
   else if (typ.equals("RPLY") || typ.equals("RPLYN")) {
      ReplyData rd = getReplyData(rid);
      if (rd != null) rd.addReply(msg);
    }
   else if (typ.equals("RPLYD")) {
      ReplyData rd = getReplyData(rid);
      if (rd != null && rd.isDone()) {
	 removeReplyData(rid);
       }
    }
   else if (typ.equals("END")) {
      synchronized (known_connections) {
	 Map<String,MintWebUser> um = known_connections.get(mid);
	 if (um != null) {
	    um.remove(uid);
	    mintLog("Remove connection " + mid + " " + uid + " " + um.size());
	    if (um.size() == 0) known_connections.remove(mid);
	  }
       }
    }
}



/********************************************************************************/
/*										*/
/*	Reply management methods						*/
/*										*/
/********************************************************************************/

private String getReplyId(MintWebUser client,String rid,int ct,int fgs)
{
   String lid = null;

   synchronized (reply_map) {
      lid = Integer.toString(++reply_counter);
      reply_map.put(lid,new ReplyData(client,rid,ct,fgs));
    }

   return lid;
}




private ReplyData getReplyData(String lid)
{
   synchronized (reply_map) {
      return reply_map.get(lid);
    }
}



private void removeReplyData(String lid)
{
   synchronized (reply_map) {
      reply_map.remove(lid);
    }
}



/********************************************************************************/
/*										*/
/*	Methods to remove idle connections					*/
/*										*/
/********************************************************************************/

private void checkConnections()
{
   long now = System.currentTimeMillis();

   synchronized (known_connections) {
      for (Iterator<Map<String,MintWebUser>> it1 = known_connections.values().iterator(); it1.hasNext(); ) {
	 Map<String,MintWebUser> mmap = it1.next();
	 for (Iterator<MintWebUser> it2 = mmap.values().iterator(); it2.hasNext(); ) {
	    MintWebUser mu = it2.next();
	    long lused = mu.getLastUsed();
	    if (lused - now > TIME_OUT_INTERVAL) {
	       it2.remove();
	       mintLog("Web user timeout");
	     }
	  }
	 if (mmap.size() == 0) {
	    it1.remove();
	    mintLog("Connection timeout");
	  }
       }
    }
}



private class ConnectionChecker extends TimerTask {

   @Override public void run() {
      checkConnections();
    }

}	// end of inner class ConnectionChecker



/********************************************************************************/
/*										*/
/*	Holder of reply data							*/
/*										*/
/********************************************************************************/

private class ReplyData {

   private MintWebUser	web_user;
   private String	user_id;
   private int		pending_count;
   private int		message_flags;
   private MintWebMessage  best_reply;
   private boolean	reply_sent;

   ReplyData(MintWebUser u,String id,int ct,int fgs) {
      web_user = u;
      user_id = id;
      pending_count = ct;
      message_flags = fgs;
      best_reply = null;
      reply_sent = false;
      if (message_flags == 0 && user_id != null) message_flags = 0xa;
    }

   MintWebUser getWebUser()			{ return web_user; }
   String getUserId()				{ return user_id; }

   boolean isDone() {
      boolean fg;
      synchronized (this) {
	 --pending_count;
	 fg = pending_count <= 0;
       }
      if (fg) {
	 sendReply(best_reply);
       }
      return fg;
    }

   void addReply(MintWebMessage msg) {
      if (message_flags == 0xa && msg != null) {		  // FIRST NON_NULL
	 sendReply(msg);
       }
      else if (message_flags == 0x2) {
	 sendReply(msg);
       }
      else if (msg != null) {
	 synchronized (this) {
	    if (best_reply == null || best_reply.getLength() < msg.getLength()) best_reply = msg;
	  }
       }
    }

   private void sendReply(MintWebMessage msg) {
      synchronized(this) {
	 if (reply_sent) return;
	 reply_sent = true;
       }
      mintLog("Send reply to " + getUserId());
      getWebUser().queueReply(getUserId(),msg);
    }


}	// end of inner class ReplyData



/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

void mintLog(String msg)
{
   if (do_debug) getServletContext().log("MINT: " + msg);
}



}	// end of class MintWebServlet




/* end of MintWebServlet.java */











































