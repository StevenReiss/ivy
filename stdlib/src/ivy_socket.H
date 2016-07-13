/************************************************************************/
/*									*/
/*		ivy_socket.H						*/
/*									*/
/*	Interface to sockets						*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_socket.H,v 1.1 2005/06/28 17:22:11 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_socket.H,v $
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_SOCKET_ALREADY_INCLUDED
#define IVY_SOCKET_ALREADY_INCLUDED


#include <ivy_datatypes.H>


#ifndef WIN32
#include "ivy_unixsocket.H"
#else
#include "ivy_winsocket.H"
#endif


#ifdef _REENTRANT
#include <ivy_thread.H>
#endif

namespace Ivy {



/************************************************************************/
/*									*/
/*	Forward type definitions					*/
/*									*/
/************************************************************************/


typedef class IvySocketInfo *	IvySocket;




/************************************************************************/
/*									*/
/*	Basic thread implementations					*/
/*									*/
/************************************************************************/


#ifdef _REENTRANT


typedef class IvyAcceptThreadInfo *		IvyAcceptThread;
typedef class IvyReaderThreadInfo *		IvyReaderThread;
typedef class IvySocketCallbackInfo *	IvySocketCallback;


class IvySocketCallbackInfo {

public:
   virtual inline void handleAccept(IvySocket s);

   virtual void handleMessage(IvySocket s,const char *,int len)     { }

   virtual void handleDoneAccept(IvyAcceptThread th)	{ }
   virtual void handleDoneReader(IvyReaderThread th)	{ }
   virtual void handleAddReader(IvyReaderThread th)	{ }
};




class IvyAcceptThreadInfo : public Ivy::IvyThread {

private:
   IvySocket for_socket;
   IvySocketCallback call_back;

public:
   IvyAcceptThreadInfo(IvySocket s,IvySocketCallback cbk = NULL) {
      for_socket = s;
      call_back = cbk;
      create();
    }

   void * run() {
      IvySocket newsock = NULL;

      for ( ; ; ) {
	 if (newsock == NULL) newsock = new IvySocketInfo();
	 if (for_socket->accept(newsock)) {
	    if (call_back != NULL) call_back->handleAccept(newsock);
	    newsock = NULL;
	  }
	 else if (for_socket->getLastError() == EINTR) ;
	 else break;
       }

      for_socket->close();
      delete for_socket;

      if (call_back != NULL) call_back->handleDoneAccept(this);

      return NULL;
    }
};




class IvyReaderThreadInfo : public Ivy::IvyThread {

private:
   IvySocket for_socket;
   IvySocketCallback call_back;

public:
   IvyReaderThreadInfo(IvySocket s,IvySocketCallback cbk) {
      for_socket = s;
      call_back = cbk;
      create();
    }

   void * run() {
      char buf[10240];
      int bln;

      if (call_back != NULL) call_back->handleAddReader(this);

      for ( ; ; ) {
	 bln = 10239;
	 if (!for_socket->receiveMessage(buf,bln)) break;
	 buf[bln] = 0;
	 if (call_back != NULL) call_back->handleMessage(for_socket,buf,bln);
       }

      for_socket->close();
      delete for_socket;

      if (call_back != NULL) call_back->handleDoneReader(this);

      return NULL;
    }
};



inline void
IvySocketCallbackInfo::handleAccept(IvySocket s)
{
   IvyReaderThread mth = new IvyReaderThreadInfo(s,this);
}



#endif




}	// end of namespace Ivy

#endif



/* end of ivy_socket.H */
