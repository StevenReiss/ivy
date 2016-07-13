/********************************************************************************/
/*										*/
/*		MintWebUser.java						*/
/*										*/
/*	Class representing a single mint server client of the web interconnect	*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Redistribution and use in source and binary forms, with or without           *
 *  modification, are permitted provided that the following conditions are met:  *
 *                                                                               *
 *  + Redistributions of source code must retain the above copyright notice,     *
 *      this list of conditions and the following disclaimer.                    *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *      this list of conditions and the following disclaimer in the              *
 *      documentation and/or other materials provided with the distribution.     *
 *  + Neither the name of the Brown University nor the names of its              *
 *      contributors may be used to endorse or promote products derived from     *
 *      this software without specific prior written permission.                 *
 *                                                                               *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   *
 *  POSSIBILITY OF SUCH DAMAGE.                                                  *
 *                                                                               *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/web/MintWebUser.java,v 1.4 2013/11/15 02:39:13 spr Exp $ */

/*********************************************************************************
 *
 * $Log: MintWebUser.java,v $
 * Revision 1.4  2013/11/15 02:39:13  spr
 * Fix imports
 *
 * Revision 1.3  2011-05-27 19:32:47  spr
 * Change copyrights.
 *
 * Revision 1.2  2011-05-18 23:33:04  spr
 * Fixes for mint web interface.
 *
 * Revision 1.1  2011-05-17 01:05:26  spr
 * Mint servlet for messaging.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.mint.web;



import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;


class MintWebUser implements MintWebConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Queue<MessageData> pending_messages;
private long		last_used;

private long		wait_time = 3*60*1000;
private long		wait_cycle = 60*1000;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MintWebUser()
{
   pending_messages = new LinkedList<MessageData>();
   last_used = System.currentTimeMillis();
}



/********************************************************************************/
/*										*/
/*	Methods to handle sending						*/
/*										*/
/********************************************************************************/

void sendPending(HttpServletResponse resp) throws IOException
{
   long now = System.currentTimeMillis();
   last_used = now;

   synchronized (pending_messages) {
      while (pending_messages.isEmpty()) {
	 long tim = System.currentTimeMillis();
	 last_used = tim;
	 long delta = wait_time - (tim-now);
	 if (delta <= 0) break;
	 if (delta > wait_cycle) delta = wait_cycle;
	 try {
	    pending_messages.wait(delta);
	  }
	 catch (InterruptedException e) { }
       }
    }

   resp.setCharacterEncoding("UTF-8");
   ServletOutputStream sos = resp.getOutputStream();
   for ( ; ; ) {
      MessageData md = null;
      synchronized (pending_messages) {
	 if (pending_messages.isEmpty()) break;
	 md = pending_messages.remove();
       }
      md.outputMessage(sos);
    }

   sos.println("0 0 END 0");

   sos.close();
}



void queueMessage(String rid,MintWebMessage msg)
{
   last_used = System.currentTimeMillis();

   MessageData md = new MessageData(rid,msg,MessageType.MESSAGE);
   queue(md);
}




void queueReply(String rid,MintWebMessage msg)
{
   last_used = System.currentTimeMillis();

   MessageData md = new MessageData(rid,msg,MessageType.REPLY);
   queue(md);
}



void queueReplyDone(String rid)
{
   last_used = System.currentTimeMillis();
   MessageData md = new MessageData(rid,null,MessageType.REPLYDONE);
   queue(md);
}



long getLastUsed()
{
   return last_used;
}



private void queue(MessageData md)
{
   synchronized (pending_messages) {
      pending_messages.add(md);
      pending_messages.notify();
    }
}



/********************************************************************************/
/*										*/
/*	Message data holder							*/
/*										*/
/********************************************************************************/

private class MessageData {

   private String message_id;
   private MintWebMessage message_contents;
   private MessageType message_type;

   MessageData(String id,MintWebMessage msg,MessageType typ) {
      message_id = id;
      message_contents = msg;
      message_type = typ;
    }

   void outputMessage(ServletOutputStream sos) throws IOException {
      if (message_contents == null) sos.print(0);
      else sos.print(message_contents.getLength());
      sos.print(" ");
      sos.print(message_id);
      sos.print(" ");
      sos.print(message_type.toString());
      sos.print(" ");
      if (message_contents == null) sos.print(0);
      else sos.println(message_contents.getFlags());

      if (message_contents != null) message_contents.output(sos);
    }

}	// end of inner class MessageData





}	// end of class MintWebUser




/* end of MintWebUser.java */
