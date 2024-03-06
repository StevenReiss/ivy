/********************************************************************************/
/*										*/
/*		minceclient.C							*/
/*										*/
/*	Client implementation for MINCE message service 			*/
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
static const char * const rcsid = "$Header: /pro/spr_cvs/pro/ivy/mince/src/minceclient.C,v 1.2 2006-08-30 00:44:23 spr Exp $";
#endif


/*********************************************************************************
 *
 * $Log: minceclient.C,v $
 * Revision 1.2  2006-08-30 00:44:23  spr
 * No change.
 *
 * Revision 1.1  2005/06/28 17:22:00  spr
 * Move mince from tea to ivy for use in veld, etc.
 *
 *
 ********************************************************************************/

#include "mince_local.H"




/************************************************************************/
/*									*/
/*	External entries						*/
/*									*/
/************************************************************************/


MinceControl
Ivy::Mince::MINCE__createClient(CStdString id,MinceSyncFlag mode,MinceControlHandler hdl)
{
   return new MinceClientInfo(id,mode,hdl);
}




/********************************************************************************/
/*										*/
/*	MinceClient constructors/destructors					*/
/*										*/
/********************************************************************************/


MinceClientInfo::MinceClientInfo(CStdString id,MinceSyncFlag mode,MinceControlHandler hdl)
{
   synch_mode = mode;
   reply_counter = 1;
   pat_counter = 1;
   mince_name = id;
   control_handler = hdl;
   server_socket = NULL;
   error_handler = NULL;

   IvyFileName fn;
   fn.expandName(MINCE_SERVER_START_CMD);
   StdString cmd = fn.name() + " " + id;

   server_socket = MinceMasterInfo::findServer(id,cmd);

   if (server_socket == NULL) {
      cerr << "MINCE: Can't connect to message server" << endl;
      exit(1);
    }

   control_handler->registerFile(this,server_socket->getFid());
}



MinceClientInfo::~MinceClientInfo()				{ }




/********************************************************************************/
/*										*/
/*	Shut down methods							*/
/*										*/
/********************************************************************************/


void
MinceClientInfo::shutDown()
{
   pollReplies();
   while (pollNext(false)) pollReplies();
   pollReplies();

   // tell the MinceControlHanlder to close the server here
}




/********************************************************************************/
/*										*/
/*	Methods for sending a message						*/
/*										*/
/********************************************************************************/


void
MinceClientInfo::send(CStdString msg,MinceReply rply,MinceMessageFlag fgs)
{
   int rid;

   if (fgs == MINCE_MSG_NONE) {
      if (rply == NULL) fgs = MINCE_MSG_NO_REPLY;
      else fgs = MINCE_MSG_FIRST_NON_NULL;
    }

   if (msg.empty()) return;
   if ((fgs & MINCE_MSG_NO_REPLY) != 0) rply = NULL;

   if (rply == NULL) rid = MINCE_REPLY_NONE;
   else {
      rid = reply_counter++;
      MinceMessage mm = new MinceClientMessageInfo(this,msg,rid);
      reply_hash[rid] = new MinceActiveDataInfo(mm,rply);
    }

   StringBuffer outbuf;
   outbuf << MINCE_HEADER_SEND << " " << rid << " " << fgs << "\n";
   outbuf << msg << "\n";
   outbuf << MINCE_TRAILER << "\n";
   server_socket->send(outbuf.getString());
}



/********************************************************************************/
/*										*/
/*	Methods for registering patterns					*/
/*										*/
/********************************************************************************/


void
MinceClientInfo::handleRegister(CStdString pat,IvyXmlNode xml,MinceHandler hdlr)
{
   if (pat.empty() || xml.isNull() || hdlr == NULL) return;

   int pid = pat_counter++;
   pattern_hash[pid] = new MincePatternDataInfo(xml,hdlr);

   StringBuffer outbuf;
   outbuf << MINCE_HEADER_REGISTER << " " << pid << "\n";
   outbuf << pat << "\n";
   outbuf << MINCE_TRAILER << "\n";
   server_socket->send(outbuf.getString());
}



void
MinceClientInfo::removePattern(MinceHandler hdlr)
{
   for (MincePatternTableIter pti = pattern_hash.begin(); pti != pattern_hash.end(); ++pti) {
      MincePatternData pd = pti.data();
      if (pd != NULL && pd->getHandler() == hdlr) {
	 pattern_hash.erase(pti.key());
	 StringBuffer outbuf;
	 outbuf << MINCE_HEADER_UNREGISTER << " " << pti.key() << "\n";
	 outbuf << MINCE_TRAILER << "\n";
	 server_socket->send(outbuf.getString());
	 delete pd;
       }
    }
}



/********************************************************************************/
/*										*/
/*	Methods for handling replies (done via message) 			*/
/*										*/
/********************************************************************************/


void
MinceClientInfo::generateReply(int rid,CStdString r)
{
   if (rid != MINCE_REPLY_NONE) {
      StringBuffer outbuf;
      outbuf << MINCE_HEADER_REPLY << " " << rid << "\n";
      if (!r.empty()) outbuf << r << "\n";
      outbuf << MINCE_TRAILER << "\n";
      server_socket->send(outbuf.getString());
    }
}



/********************************************************************************/
/*										*/
/*	Methods for handling server errors					*/
/*										*/
/********************************************************************************/


void
MinceClientInfo::serverError(CStdString what)
{
   if (error_handler == NULL) {
      cerr << "MINCE: I/O error from the server: " << what << "\n";
      cerr << "MINCE: Shutting down ..." << endl;
      ::exit(0);
    }
   else {
      error_handler->handleServerError(what);
    }
}



/********************************************************************************/
/*										*/
/*	MinceClientInfo message handling methods				*/
/*										*/
/********************************************************************************/


void
MinceClientInfo::handleMessage(int mid,int rid,IvyXmlNode xml)
{
   queueMessage(new MinceMessageDataInfo(mid,rid,xml));
}



void
MinceClientInfo::handleReply(int rid,IvyXmlNode xml)
{
   queueReply(new MinceReplyDataInfo(rid,xml));
}



void
MinceClientInfo::handleDone(int rid)
{
   queueReply(new MinceDoneDataInfo(rid));
}



void
MinceClientInfo::queueMessage(MinceRequest d)
{
   control_handler->lockMessage();
   message_queue.pushBack(d);
   control_handler->unlockMessage();
   control_handler->handleAddMessage();
}



void
MinceClientInfo::queueReply(MinceRequest d)
{
   switch (synch_mode) {
      case MINCE_SYNC_NONE :
	 break;
      case MINCE_SYNC_SINGLE :
      case MINCE_SYNC_MULTIPLE :
      case MINCE_SYNC_POLL_REPLIES :
	 control_handler->lockReply();
	 reply_queue.pushBack(d);
	 control_handler->unlockReply();
	 control_handler->handleAddReply();
	 break;
      case MINCE_SYNC_REPLIES :
      case MINCE_SYNC_POLL:
	 queueMessage(d);
	 break;
    }
}



/********************************************************************************/
/*										*/
/*	MinceClinet polling interface						*/
/*										*/
/********************************************************************************/


MinceRequest
MinceClientInfo::pollNext(Bool wait)
{
   return getNextMessage(wait);
}



MinceRequest
MinceClientInfo::pollReplies(Bool wait)
{
   return getNextReply(wait);
}



MinceRequest
MinceClientInfo::getNextMessage(Bool wait)
{
   if (!control_handler->handleWaitMessage(wait)) return NULL;

   control_handler->lockMessage();
   MinceRequest d = message_queue.front();
   message_queue.popFront();
   control_handler->unlockMessage();

   return d;
}



MinceRequest
MinceClientInfo::getNextReply(Bool wait)
{
   if (!control_handler->handleWaitReply(wait)) return NULL;

   control_handler->lockReply();
   MinceRequest d = reply_queue.front();
   reply_queue.popFront();
   control_handler->unlockReply();

   return d;
}



/************************************************************************/
/*									*/
/*	Input processing methods					*/
/*									*/
/************************************************************************/


void
MinceClientInfo::processInput(CStdString hdr,CStdString body)
{
   StringTokenizer tok(hdr);

   if (!tok.hasMoreTokens()) return;
   StdString cmd = tok.nextToken();
   if (cmd == MINCE_HEADER_REPLY) {
      if (tok.hasMoreTokens()) {
	 int rid = atoi(tok.nextToken());
	 IvyXmlNode xml = IvyXml::convertStringToXml(body);
	 handleReply(rid,xml);
       }
    }
   else if (cmd == MINCE_HEADER_DONE) {
      if (tok.hasMoreTokens()) {
	 int rid = atoi(tok.nextToken());
	 handleDone(rid);
       }
    }
   else if (cmd == MINCE_HEADER_GET) {
      if (tok.hasMoreTokens()) {
	 int mid = atoi(tok.nextToken());
	 if (tok.hasMoreTokens()) {
	    int rid = atoi(tok.nextToken());
	    IvyXmlNode xml = IvyXml::convertStringToXml(body);
	    handleMessage(mid,rid,xml);
	  }
       }
    }
}




/********************************************************************************/
/*										*/
/*	Message processing methods						*/
/*										*/
/********************************************************************************/


void
MinceClientInfo::process(MinceRequest mr)
{
   if (mr != NULL) mr->process(this);
}



void
MinceClientInfo::processMessage(MinceMessageData md)
{
   MincePatternData pd = pattern_hash[md->getMessageId()];
   MinceClientMessage msg = new MinceClientMessageInfo(this,md->getMessageBody(),md->getReplyId());
   pd->match(msg);
   delete msg;
}



void
MinceClientInfo::processReply(MinceReplyData rd)
{
   MinceActiveData ad = reply_hash[rd->getReplyId()];
   if (ad != NULL) {
      MinceReply mr = ad->getReplyHandler();
      MinceMessage msg = ad->getMessage();
      MinceClientMessage rmsg = new MinceClientMessageInfo(this,rd->getReplyBody(),MINCE_REPLY_NONE);
      mr->handleReply(msg,rmsg);
      delete rmsg;
    }
}




void
MinceClientInfo::processDone(MinceDoneData dd)
{
   MinceActiveData ad = reply_hash[dd->getReplyId()];
   if (ad != NULL) {
      reply_hash.erase(dd->getReplyId());
      delete ad;
    }
}




/************************************************************************/
/*									*/
/*	MincePatternData methods					*/
/*									*/
/************************************************************************/


MincePatternDataInfo::MincePatternDataInfo(IvyXmlNode n,MinceHandler h)
{
   pattern_xml = n;
   use_handler = h;
   pattern_matcher = new MinceMatchSelectorInfo(n);
}



MincePatternDataInfo::~MincePatternDataInfo()
{
   delete pattern_matcher;
}



Bool
MincePatternDataInfo::match(MinceMessage msg)
{
   MinceArguments args = pattern_matcher->matchMessage(msg);
   if (args == NULL) return false;

   if (use_handler != NULL) use_handler->receive(msg,args);

   delete args;

   return true;
};



/* end of minceclient.C */
