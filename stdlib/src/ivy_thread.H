/************************************************************************/
/*									*/
/*		ivy_thread.H						*/
/*									*/
/*	Interface to Posix threads					*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_thread.H,v 1.3 2011-09-12 20:50:48 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_thread.H,v $
 * Revision 1.3  2011-09-12 20:50:48  spr
 * Code cleanup.
 *
 * Revision 1.2  2011-01-11 00:27:15  spr
 * Fixups for lock finding code.
 *
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_THREAD_ALREADY_INCLUDED
#define IVY_THREAD_ALREADY_INCLUDED


#ifndef _REENTRANT
#define _REENTRANT
#endif

#ifdef WIN32
#include <ivy_winthread.H>
#else
#include <ivy_pthread.H>
#endif


namespace Ivy {


class Locker {

private:
   IvyMutex&	   for_mutex;

public:
   Locker(IvyMutex& mtx) : for_mutex(mtx) {
      for_mutex.lock();
    }

   virtual ~Locker() {
      for_mutex.unlock();
    }

};	// end of class Locker



}	// end of namespace Ivy




#endif


/* end of ivy_thread.H */
