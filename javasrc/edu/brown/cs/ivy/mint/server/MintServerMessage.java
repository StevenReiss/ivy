/********************************************************************************/
/*										*/
/*		MintServerMessage.java						*/
/*										*/
/*	Representation of a message in the server				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
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


package edu.brown.cs.ivy.mint.server;


import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Element;



class MintServerMessage implements MintMessage, MintConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private MintServer for_server;
private MintServerConnection for_client;
private String	message_text;
private Element message_xml;
private String best_reply;
private int num_client;
private int num_reply;
private boolean send_done;
private int client_rid;
private int global_rid;
private int message_flags;
private int replies_sent;

private static int reply_counter = 0;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MintServerMessage(MintServer svr,MintServerConnection cl,String tx,int rid,int fgs)
{
   for_server = svr;
   for_client = cl;
   message_text = tx;
   message_xml = IvyXml.convertStringToXml(tx);
   message_flags = fgs;
   best_reply = null;
   num_client = 0;
   num_reply = 0;
   send_done = false;
   client_rid = rid;
   replies_sent = 0;

   if (rid == MINT_REPLY_NONE) global_rid = MINT_REPLY_NONE;
   else if ((fgs & MINT_MSG_NO_REPLY) != 0) global_rid = MINT_REPLY_NONE;
   else {
      synchronized (MintServerMessage.class) {
	 global_rid = ++reply_counter;
       }
    }
}



MintServerMessage(MintServerMessage omsg,Element xml,boolean clone)
{
   for_server = omsg.for_server;
   for_client = omsg.for_client;
   message_text = IvyXml.convertXmlToString(xml);
   message_xml = xml;
   message_flags = omsg.message_flags;
   best_reply = null;
   num_client = 0;
   num_reply = 0;
   send_done = false;
   if (clone) client_rid = omsg.client_rid;
   else client_rid = MINT_REPLY_NONE;
   replies_sent = 0;

   if (client_rid == MINT_REPLY_NONE) global_rid = MINT_REPLY_NONE;
   else if ((message_flags & MINT_MSG_NO_REPLY) != 0) global_rid = MINT_REPLY_NONE;
   else {
      synchronized (MintServerMessage.class) {
	 global_rid = ++reply_counter;
       }
    }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getText() 		{ return message_text; }



@Override public Element getXml() 		{ return message_xml; }



public int getReplyId() 		{ return global_rid; }



public int getLocalReplyId()		{ return client_rid; }



MintServer getServer()			{ return for_server; }


int getMessageFlags()			{ return message_flags; }


MintServerConnection getConnection()	{ return for_client; }



/********************************************************************************/
/*										*/
/*	Client/reply handling methods						*/
/*										*/
/********************************************************************************/

synchronized void addClient()		{ ++num_client; }


synchronized void finishSend() {
   send_done = true;

   if (num_client == num_reply) {
      addClient();
      replyTo((String) null);
    }
}



@Override public void replyTo()
{
   replyTo((String) null);
}



@Override public void replyTo(Element xml)
{
   replyTo(IvyXml.convertXmlToString(xml));
}



@Override public synchronized void replyTo(String reply)
{
   boolean done = false;

   ++num_reply;

   if ((message_flags & MINT_MSG_FIRST_REPLY) != 0) {
      if ((message_flags & MINT_MSG_NON_NULL_ONLY) == 0 || reply != null) {
	 if (replies_sent == 0) {
	    for_client.queueReply(this,reply);
	    replies_sent++;
	    done = true;
	  }
       }
      else if (send_done && num_client == num_reply) {
	 for_client.queueReply(this,null);
	 replies_sent++;
       }
    }
   else if ((message_flags & MINT_MSG_ALL_REPLIES) != 0) {
      if ((message_flags & MINT_MSG_NON_NULL_ONLY) == 0 || reply != null) {
	 for_client.queueReply(this,reply);
	 replies_sent++;
       }
    }
   else if ((message_flags & MINT_MSG_NON_NULL_ONLY) != 0 && reply != null) {
      for_client.queueReply(this,reply);
      replies_sent++;
    }
   else if ((message_flags & MINT_MSG_WAIT_FOR_ALL) != 0) {
      if (reply != null && best_reply == null) best_reply = reply;
      if (send_done && num_client == num_reply) {
	 for_client.queueReply(this,best_reply);
	 replies_sent++;
       }
    }

   if (send_done && num_client == num_reply) done = true;

   if (done && client_rid != MINT_REPLY_NONE) for_client.queueDone(this);

   if (done) for_server.removeMessage(this);
}




}	// end of class MintServerMessage




/* end of MintServerMessage.java */
