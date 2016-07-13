/************************************************************************/
/*									*/
/*		ivy_winthread.H 					*/
/*									*/
/*	Interface to Windows threads					*/
/*									*/
/************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 2005, Brown University, Providence, RI.				 *
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
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_winthread.H,v 1.1 2005/06/28 17:22:12 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_winthread.H,v $
 * Revision 1.1  2005/06/28 17:22:12  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_WINTHREAD_ALREADY_INCLUDED
#define IVY_WINTHREAD_ALREADY_INCLUDED



#include <afxwin.h>
#include <afxmt.h>
#include <ivy_datatypes.H>



namespace Ivy {




/********************************************************************************/
/*										*/
/*	External Definitions							*/
/*										*/
/********************************************************************************/


extern "C" {
   inline UINT IVY__default_creation_rtn(void * p);
}



/********************************************************************************/
/*										*/
/*	IvyThread -- basic thread structure					*/
/*										*/
/********************************************************************************/


class IvyThread {

private:
   CWinThread * win_thread;

public:
   IvyThread() {
      win_thread = NULL;
    }

   virtual ~IvyThread() {
      delete win_thread;
    }

   int create(size_t stack_size = 0,void * stack_base = NULL) {
      win_thread = AfxBeginThread(IVY__default_creation_rtn,this,
				     THREAD_PRIORITY_NORMAL,stack_size,
				     CREATE_SUSPENDED,NULL);
      if (win_thread == NULL) return 0;
      win_thread->m_bAutoDelete = false;
      win_thread->ResumeThread();
      return 1;
    }

public:
   int isValid() const {
      DWORD sts;
      if (win_thread == NULL) return false;
      if (win_thread->m_hThread == NULL) return false;
      if (!::GetExitCodeThread(win_thread->m_hThread,&sts)) return false;
      if (sts != STILL_ACTIVE) return false;
      return true;
    }

   int waitFor(void ** status_ptr = NULL) {
      DWORD sts;
      if (win_thread == NULL) return 0;
      if (win_thread->m_hThread == NULL) return 0;
      for ( ; ; ) {
	 if (!::GetExitCodeThread(win_thread->m_hThread,&sts)) return 0;
	 if (sts == STILL_ACTIVE) {
	    ::WaitForSingleObject(win_thread->m_hThread,INFINITE);
	  }
	 else {
	    if (status_ptr != NULL) *status_ptr = (void *) sts;
	    break;
	  }
       }
      return 1;
    }

   virtual void * run() 				{ return NULL; }
};




inline UINT
IVY__default_creation_rtn(void * ud)
{
   IvyThread * ft = (IvyThread *) ud;
   return (UINT) ft->run();
}




/********************************************************************************/
/*										*/
/*	Semaphores								*/
/*										*/
/********************************************************************************/


class IvySemaphore {

private:
   CSemaphore * mfc_sema;
   CSingleLock * mfc_lock;

public:
   IvySemaphore() {
      mfc_sema = NULL;
      mfc_lock = NULL;
    }
   ~IvySemaphore()				{ destroy(); }

   int create(unsigned int count = 0,bool process_share = false) {
      mfc_sema = new CSemaphore(count,1024);
      mfc_lock = new CSingleLock(mfc_sema);
      return true;
    }
   int createThread(unsigned int count = 0)	{ return create(count,false); }
   int createProcess(unsigned int count = 0)	{ return create(count,true); }

   int destroy() {
      if (mfc_sema != NULL) delete mfc_sema;
      if (mfc_lock != NULL) delete mfc_lock;
      mfc_sema = NULL;
      mfc_lock = NULL;
      return true;
    }

   int wait() volatile				{ return mfc_lock->Lock(); }
   int P()					{ return wait(); }
   int tryWait() volatile			{ return mfc_lock->Lock(0); }
   int post() volatile				{ return mfc_lock->Unlock(); }
   int V()					{ return post(); }

   int count() {
      if (mfc_lock->IsLocked()) return 1;
      return 0;
    }
};



/********************************************************************************/
/*										*/
/*	Mutexes 								*/
/*										*/
/********************************************************************************/


class IvyMutex {

private:
   CMutex * mfc_mutex;
   CSingleLock * mfc_lock;

public:
   IvyMutex() {
      mfc_mutex = NULL;
      mfc_lock = NULL;
    }
   ~IvyMutex()				{ destroy(); }

   int create(bool process = false) {
      mfc_mutex = new CMutex();
      mfc_lock = new CSingleLock(mfc_mutex);
      return true;
    }

   int createThread()				{ return create(false); }
   int createProcess()				{ return create(true); }

   int destroy() {
      if (mfc_mutex != NULL) delete mfc_mutex;
      if (mfc_lock != NULL) delete mfc_lock;
      mfc_mutex = NULL;
      mfc_lock = NULL;
      return true;
    }

   int lock()					{ return mfc_lock->Lock(); }
   int tryLock()				{ return mfc_lock->Lock(0); }
   int unlock() 				{ return mfc_lock->Unlock(); }
};



}	// end of namespace Ivy


#endif



/* end of ivy_winthread.H */
