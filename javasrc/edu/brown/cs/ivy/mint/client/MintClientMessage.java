/********************************************************************************/
/*										*/
/*		MintClientMessage.java						*/
/*										*/
/*	Message implementation inside a client					*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/client/MintClientMessage.java,v 1.5 2015/11/20 15:09:18 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintClientMessage.java,v $
 * Revision 1.5  2015/11/20 15:09:18  spr
 * Reformatting.
 *
 * Revision 1.4  2011-05-27 19:32:43  spr
 * Change copyrights.
 *
 * Revision 1.3  2007-05-04 02:00:13  spr
 * Fix bugs related to polling.
 *
 * Revision 1.2  2006/02/21 17:06:25  spr
 * Upgrade interface to use Element instead of Node for XML.
 *
 * Revision 1.1  2005/07/08 23:32:57  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint.client;


import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Element;



public class MintClientMessage implements MintMessage, MintConstants
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String message_text;
private Element message_xml;
private MintClient for_client;
private int reply_id;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MintClientMessage(MintClient clnt,String txt,int rid)
{
   for_client = clnt;
   reply_id = rid;
   message_text = txt;
   message_xml = null;
}



MintClientMessage(MintClient clnt,Element xml,int rid)
{
   for_client = clnt;
   reply_id = rid;
   message_text = null;
   message_xml = xml;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getText()
{
   if (message_text == null) {
      message_text = IvyXml.convertXmlToString(message_xml);
    }

   return message_text;
}



@Override public Element getXml()
{
   if (message_xml == null) {
      message_xml = IvyXml.convertStringToXml(message_text);
    }

   return message_xml;
}



/********************************************************************************/
/*										*/
/*	Reply methods								*/
/*										*/
/********************************************************************************/

@Override public void replyTo()
{
   replyTo((String) null);
}


@Override public void replyTo(String r)
{
   if (reply_id != MINT_REPLY_NONE && for_client != null) {
      for_client.generateReply(reply_id,r);
    }
}


@Override public void replyTo(Element xml)
{
   if (reply_id != MINT_REPLY_NONE && for_client != null) {
      for_client.generateReply(reply_id,xml);
    }
}




}	// end of class MintClientMessage




/* end of MintClientMessage.java */
