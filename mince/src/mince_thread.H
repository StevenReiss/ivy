/********************************************************************************/
/*										*/
/*		mince_thread.H							*/
/*										*/
/*	External definitions for MINCE using thread to handle messages		*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/mince/src/mince_thread.H,v 1.3 2006-08-30 00:44:23 spr Exp $ */

/*********************************************************************************
 *
 * $Log: mince_thread.H,v $
 * Revision 1.3  2006-08-30 00:44:23  spr
 * No change.
 *
 * Revision 1.2  2006/01/30 19:07:03  spr
 * Remove dead threads.
 *
 * Revision 1.1  2005/06/28 17:22:00  spr
 * Move mince from tea to ivy for use in veld, etc.
 *
 *
 ********************************************************************************/


#ifndef MINCE_THREAD_INCLUDED_ALREADY
#define MINCE_THREAD_INCLUDED_ALREADY


#include <mince.H>

#include <ivy_datatypes.H>
#include <ivy_thread.H>
#include <ivy_fileio.H>



namespace Ivy {

namespace Mince {


using namespace Ivy;
using namespace Ivy::Mince;


/************************************************************************/
/*									*/
/*	Forward type definitions					*/
/*									*/
/************************************************************************/


typedef class MinceThreadControlInfo *		MinceThreadControl;
typedef class MinceThreadProcessorInfo *	MinceThreadProcessor;
typedef class MinceThreadReplyProcessorInfo *	MinceThreadReplyProcessor;
typedef class MinceThreadRunInfo *		MinceThreadRun;
typedef class MinceThreadReaderInfo *		MinceThreadReader;

typedef List<MinceThreadRun>			MinceThreadRunList;



/********************************************************************************/
/*										*/
/*	MinceThreadControl -- class for thread-based message access		*/
/*										*/
/********************************************************************************/


class MinceThreadControlInfo : public MinceControlHandlerInfo {

private:
   IvyMutex message_mutex;
   IvySemaphore message_sema;
   IvyMutex reply_mutex;
   IvySemaphore reply_sema;
   MinceThreadProcessor proc_thread;
   MinceThreadReplyProcessor reply_thread;
   MinceThreadReader reader_thread;

public:
   static MinceControl create(MinceSyncFlag fg) {
      StdString id;
      MinceThreadControl tc = new MinceThreadControlInfo();
      MinceControl mc = MINCE__createClient(id,fg,tc);
      tc->setControl(mc,fg);
      return mc;
    }
   static MinceControl create(CStdString id,MinceSyncFlag fg) {
      MinceThreadControl tc = new MinceThreadControlInfo();
      MinceControl mc = MINCE__createClient(id,fg,tc);
      tc->setControl(mc,fg);
      return mc;
    }

public:
   MinceThreadControlInfo() {
      proc_thread = NULL;
      reply_thread = NULL;
      reader_thread = NULL;
      message_mutex.create();
      message_sema.create();
      reply_mutex.create();
      reply_sema.create();
    }

   ~MinceThreadControlInfo() {
      message_mutex.destroy();
      message_sema.destroy();
      reply_mutex.destroy();
      reply_sema.destroy();
    }

   inline void setControl(MinceControl mc,MinceSyncFlag fg);

   void lockMessage()			{ message_mutex.lock(); }
   void unlockMessage() 		{ message_mutex.unlock(); }
   void handleAddMessage()		{ message_sema.V(); }
   bool handleWaitMessage(bool w) {
      int fg;
      if (w) fg = message_sema.wait();
      else fg = message_sema.tryWait();
      return fg == 0;
    }

   void lockReply()			{ reply_mutex.lock(); }
   void unlockReply()			{ reply_mutex.unlock(); }
   void handleAddReply()		{ reply_sema.V(); }
   bool handleWaitReply(bool w) {
      int fg;
      if (w) fg = reply_sema.wait();
      else fg = reply_sema.tryWait();
      return fg == 0;
    }

   inline void registerFile(MinceControl mc,int fd);
};



/************************************************************************/
/*									*/
/*	MinceThreadRun -- thread to handle individual event		*/
/*									*/
/************************************************************************/


class MinceThreadRunInfo : public IvyThread {

private:
   MinceControl mince_control;
   MinceRequest mince_request;

public:
   MinceThreadRunInfo(MinceControl mc,MinceRequest mr) {
      mince_control = mc;
      mince_request = mr;
    }
   ~MinceThreadRunInfo()			{ }

   void * run() {
      mince_control->process(mince_request);
      return NULL;
    }
};



class MinceThreadDonePred {

public:
   bool operator()(MinceThreadRun mr)	     { return mr == NULL || !mr->isValid(); }

};



/************************************************************************/
/*									*/
/*	MinceThreadProcessor -- process thread				*/
/*									*/
/************************************************************************/


class MinceThreadProcessorInfo : public IvyThread {

private:
   MinceControl for_control;
   MinceSyncFlag synch_mode;
   MinceThreadRunList run_list;

public:
   MinceThreadProcessorInfo(MinceControl mc,MinceSyncFlag fg) {
      for_control = mc;
      synch_mode = fg;
    }
   ~MinceThreadProcessorInfo()				{ }

   void * run() {
      for ( ; ; ) {
	 run_list.remove_if(MinceThreadDonePred());

	 MinceRequest r = for_control->pollNext(true);
	 if (synch_mode == MINCE_SYNC_MULTIPLE) {
	    MinceThreadRun rt = new MinceThreadRunInfo(for_control,r);
	    rt->create();
	    run_list.pushBack(rt);
	  }
	 else for_control->process(r);
       }
      return NULL;
    }
};




/************************************************************************/
/*									*/
/*	MinceThreadReplyProcessor -- process thread for replies 	*/
/*									*/
/************************************************************************/


class MinceThreadReplyProcessorInfo : public IvyThread {

private:
   MinceControl for_control;
   MinceSyncFlag synch_mode;

public:
   MinceThreadReplyProcessorInfo(MinceControl mc,MinceSyncFlag fg) {
      for_control = mc;
      synch_mode = fg;
    }
   ~MinceThreadReplyProcessorInfo()			{ }

   void * run() {
      for ( ; ; ) {
	 // remove finished children here

	 MinceRequest r = for_control->pollReplies(true);
	 if (synch_mode == MINCE_SYNC_MULTIPLE) {
	    MinceThreadRun rt = new MinceThreadRunInfo(for_control,r);
	    rt->create();
	  }
	 else for_control->process(r);
       }
      return NULL;
    }
};



/************************************************************************/
/*									*/
/*	MinceThreadReader -- thread to read from socket 		*/
/*									*/
/************************************************************************/


class MinceThreadReaderInfo : public IvyThread {

private:
   MinceControl for_control;
   IvyInputFile input_file;

public:
   MinceThreadReaderInfo(MinceControl mc,int fid) {
      input_file = new IvyInputFileInfo();
      input_file->open(fid);
      for_control = mc;
    }
   ~MinceThreadReaderInfo() {
      input_file->close();
      delete input_file;
    }

   void * run() {
      for ( ; ; ) {
	 StdString hdr,s;
	 hdr = input_file->getLine();
	 StringBuffer body;
	 int ct = 0;
	 for ( ; ; ) {
	    s = input_file->getLine();
	    if (!input_file->isValid()) {
	       for_control->serverError("I/O error reading input stream");
	       return NULL;
	     }
	    else if (input_file->endOfFile()) {
	       for_control->serverError("EOF from server");
	       return NULL;
	     }
	    if (s == MinceControlHandlerInfo::getMinceTrailer())  break;
	    if (ct++ != 0) body.append('\n');
	    body.append(s);
	  }
	 for_control->processInput(hdr,body.getString());
       }
    }
};




/************************************************************************/
/*									*/
/*	Method implementations						*/
/*									*/
/************************************************************************/


void
MinceThreadControlInfo::setControl(MinceControl mc,MinceSyncFlag fg)
{
   proc_thread = new MinceThreadProcessorInfo(mc,fg);
   proc_thread->create();
   if (fg == MINCE_SYNC_SINGLE || fg == MINCE_SYNC_MULTIPLE || fg == MINCE_SYNC_POLL_REPLIES) {
      reply_thread = new MinceThreadReplyProcessorInfo(mc,fg);
      reply_thread->create();
    }
}


void
MinceThreadControlInfo::registerFile(MinceControl mc,int fd)
{
   reader_thread = new MinceThreadReaderInfo(mc,fd);
   reader_thread->create();
}



} }	// end of namespace Ivy::Mince



#endif


/* end of mince_thread.H */
