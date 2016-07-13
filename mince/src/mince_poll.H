/********************************************************************************/
/*										*/
/*		mince_poll.H							*/
/*										*/
/*	External definitions for MINCE with direct polling only 		*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/mince/src/mince_poll.H,v 1.2 2009-03-20 02:00:47 spr Exp $ */

/*********************************************************************************
 *
 * $Log: mince_poll.H,v $
 * Revision 1.2  2009-03-20 02:00:47  spr
 * Change test code.
 *
 * Revision 1.1  2005/06/28 17:21:59  spr
 * Move mince from tea to ivy for use in veld, etc.
 *
 *
 ********************************************************************************/


#ifndef MINCE_POLL_INCLUDED_ALREADY
#define MINCE_POLL_INCLUDED_ALREADY


#include "mince.H"

#include <ivy_datatypes.H>
#include <ivy_socket.H>
#include <ivy_fileio.H>





namespace Ivy {

namespace Mince {


/************************************************************************/
/*									*/
/*	Forward type definitions					*/
/*									*/
/************************************************************************/


typedef class MincePollControlInfo *	MincePollControl;




/********************************************************************************/
/*										*/
/*	MinceQtControl -- class for poll-based message access			*/
/*										*/
/********************************************************************************/


class MincePollControlInfo : public MinceControlHandlerInfo {

private:
   MinceControl for_control;
   int message_count;
   int reply_count;
   IvyInputFile message_stream;

public:
   static MinceControl create(MinceSyncFlag fg = MINCE_SYNC_POLL) {
      StdString id;
      return create(id,fg);
    }
   static MinceControl create(CStdString id,MinceSyncFlag fg = MINCE_SYNC_POLL) {
      MincePollControl pc = new MincePollControlInfo();
      MinceControl mc = MINCE__createClient(id,fg,pc);
      pc->setControl(mc,fg);
      return mc;
    }

public:
   MincePollControlInfo() {
      message_count = 0;
      reply_count = 0;
      message_stream = NULL;
      for_control = NULL;
    }
   ~MincePollControlInfo() {
    }

   void lockMessage()				{ }
   void unlockMessage() 			{ }
   void handleAddMessage()			{ ++message_count; }
   bool handleWaitMessage(bool w) {
      if (message_count == 0 && w) handlePollData();
      if (message_count == 0) return false;
      --message_count;
      return true;
    }

   void lockReply()				{ }
   void unlockReply()				{ }
   void handleAddReply()			{ ++reply_count; }
   bool handleWaitReply(bool w) {
      if (reply_count == 0 && w) handlePollData();
      if (reply_count == 0) return false;
      --reply_count;
      return true;
    }

   void registerFile(MinceControl mc,int fd)	{
      IvySocketInfo sock(fd);
      sock.enableNonblocking(true);
      message_stream = new IvyInputFileInfo();
      message_stream->open(fd);
    }

   void setControl(MinceControl mc,MinceSyncFlag fg) {
      for_control = mc;
    }

private:
   inline void handlePollData();
};



/************************************************************************/
/*									*/
/*	Method implementations						*/
/*									*/
/************************************************************************/


inline void
MincePollControlInfo::handlePollData()
{
   for ( ; ; ) {
      StdString hdr,s;
      hdr = message_stream->getLine();
      if (message_stream->endOfFile()) {
	 message_stream->clearEndOfFile();
	 break;
       }
      if (!message_stream->isValid()) break;

      StringBuffer body;
      int ct = 0;
      for ( ; ; ) {
	 s = message_stream->getLine();
	 if (!message_stream->isValid()) {
	    for_control->serverError("I/O error reading input stream");
	    break;
	  }
	 else if (message_stream->endOfFile()) {
	    message_stream->clearEndOfFile();
	    continue;			// we must wait here for the rest of the message
	  }
	 if (s.empty() || s == getMinceTrailer()) break;
	 if (ct++ != 0) body.append(' ');
	 body.append(s);
       }
      for_control->processInput(hdr,body.getString());
    }
}




} }	// end of namespace Ivy::Mince



#endif


/* end of mince_poll.H */

