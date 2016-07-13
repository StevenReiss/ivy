/********************************************************************************/
/*										*/
/*		mincemessage.C							*/
/*										*/
/*	Client message implementation for MINCE message service 		*/
/*										*/
/********************************************************************************/
/*	Copyright 1997 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 1997, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,	 *
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS 	 *
 *  SOFTWARE.									 *
 *										 *
 ********************************************************************************/


#ifndef lint
static const char * const rcsid = "$Header: /pro/spr_cvs/pro/ivy/mince/src/mincemessage.C,v 1.1 2005/06/28 17:22:00 spr Exp $";
#endif


/*********************************************************************************
 *
 * $Log: mincemessage.C,v $
 * Revision 1.1  2005/06/28 17:22:00  spr
 * Move mince from tea to ivy for use in veld, etc.
 *
 *
 ********************************************************************************/


#include "mince_local.H"




/********************************************************************************/
/*										*/
/*	MintClientMessage constructors/destructors				*/
/*										*/
/********************************************************************************/


MinceClientMessageInfo::MinceClientMessageInfo(MinceClient c,CStdString m,int rid)
{
   for_client = c;
   reply_id = rid;
   message_text = m;
}


MinceClientMessageInfo::MinceClientMessageInfo(MinceClient c,IvyXmlNode x,int rid)
{
   for_client = c;
   reply_id = rid;
   message_xml = x;
}



MinceClientMessageInfo::~MinceClientMessageInfo()			{ }



/********************************************************************************/
/*										*/
/*	MinceClientMessage access methods					*/
/*										*/
/********************************************************************************/


CStdString
MinceClientMessageInfo::getText()
{
   if (message_text.empty() && !message_xml.isNull()) {
      message_text = message_xml.convertToString();
    }

   return message_text;
}



IvyXmlNode
MinceClientMessageInfo::getXml()
{
   if (message_xml.isNull() && !message_text.empty()) {
      message_xml = IvyXml::convertStringToXml(message_text);
    }

   return message_xml;
}



/********************************************************************************/
/*										*/
/*	MinceClientMessage reply methods					*/
/*										*/
/********************************************************************************/


void
MinceClientMessageInfo::replyTo()
{
   replyTo("");
}



void
MinceClientMessageInfo::replyTo(CStdString s)
{
   if (reply_id != MINCE_REPLY_NONE && for_client != NULL) {
      for_client->generateReply(reply_id,s);
    }
}



void
MinceClientMessageInfo::replyTo(IvyXmlNode x)
{
   if (reply_id != MINCE_REPLY_NONE && for_client != NULL) {
      for_client->generateReply(reply_id,x);
    }
}




/* end of mincemessage.C */
