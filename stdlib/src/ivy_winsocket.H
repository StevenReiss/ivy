/************************************************************************/
/*									*/
/*		ivy_winsocket.H 					*/
/*									*/
/*	Interface to sockets for windows				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_winsocket.H,v 1.1 2005/06/28 17:22:12 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_winsocket.H,v $
 * Revision 1.1  2005/06/28 17:22:12  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_WINSOCKET_ALREADY_INCLUDED
#define IVY_WINSOCKET_ALREADY_INCLUDED


#include <afxsock.h>


namespace Ivy {



/********************************************************************************/
/*										*/
/*	Socket class implementation using CAsyncSocket				*/
/*										*/
/********************************************************************************/


class IvySocketInfo {

public:
   CAsyncSocket * mfc_socket;

public:
   IvySocketInfo()			{ mfc_socket = new CAsyncSocket(); }
   IvySocketInfo(int fd) {
      mfc_socket = new CAsyncSocket();
      mfc_socket->Attach((SOCKET) fd);
    }
   virtual ~IvySocketInfo()		{ delete mfc_socket; }

   bool create() {
      return mfc_socket->Create();
    }

   void close() {
      mfc_socket->Close();
    }

   int send(const void * buf,int bln) {
      return mfc_socket->Send(buf,bln,0);
    }

   int send(const string& str) {
      return send(str.c_str(),str.length());
    }

   int receive(void * buf,int bln) {
      return mfc_socket->Receive(buf,bln,0);
    }

   inline bool bind(int port = 0)			{ return mfc_socket->Bind(port); }
   inline bool getSockName(char * host,int& port) {
      CString cstr;
      UINT upt;
      int sts = mfc_socket->GetSockName(cstr,upt);
      if (sts != 0) {
	 strcpy(host,cstr);
	 port = upt;
       }
      return sts;
    }

   inline bool listen(int n = 5)			{ return mfc_socket->Listen(n); }
   inline bool accept(IvySocketInfo * new_socket) {
      CAsyncSocket * as = new CAsyncSocket();
      int sts = mfc_socket->Accept(*as);
      if (sts == 0) delete as;
      else new_socket->mfc_socket = as;
      return sts;
    }

   inline bool connect(const char * host,int port)	{ return mfc_socket->Connect(host,port); }

   int getLastError()				{ return mfc_socket->GetLastError(); }

   bool createServer(int port = 0) {
      if (!create()) return false;
      if (!bind(port)) return false;
      if (!listen()) return false;
      return true;
    }

   inline bool createClient(const char * host,int port = 0) {
      char hbuf[1024];
      if (host == NULL) {
	 gethostname(hbuf,1024);
	 host = hbuf;
       }
      if (!create()) return false;
      if (!connect(host,port)) {
	 close();
	 return false;
       }
      return true;
    }

   void sendMessage(const void * buf,int bln) {
      send(&bln,sizeof(bln));
      send(buf,bln);
    }

   void sendMessage(const char * txt) {
      sendMessage(txt,strlen(txt)+1);
    }

   void sendMessage(const string& str) {
      sendMessage(str.c_str());
    }

   bool receiveMessage(void * buf,int& bln) {
      int mln;
      int mct = receive(&mln,sizeof(mln));
      if (mct != sizeof(mln)) return false;
      if (bln < mln) return false;
      mct = receive(buf,mln);
      if (mct != mln) return false;
      bln = mln;
      return true;
    }


   bool enableNonblocking(bool fg) {
      DWORD dfg = fg;
      return mfc_socket->IOCtl(FIONBIO,&dfg);
    }

   int getFid() const			{ return (int) mfc_socket->m_hSocket; }

};




}	// end of namespace Ivy



#endif



/* end of ivy_winsocket.H */
