/************************************************************************/
/*									*/
/*		ivy_unixsocket.H					 */
/*									*/
/*	Interface to sockets for solaris/linux (unix in general)	*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_unixsocket.H,v 1.3 2008-05-07 21:16:07 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_unixsocket.H,v $
 * Revision 1.3  2008-05-07 21:16:07  spr
 * C++ updates for modified libraries.
 *
 * Revision 1.2  2005/07/08 20:58:28  spr
 * Minor fixups and additions.
 *
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_UNIXSOCKET_ALREADY_INCLUDED
#define IVY_UNIXSOCKET_ALREADY_INCLUDED





#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <pwd.h>
#include <grp.h>
#include <fcntl.h>
#include <netdb.h>
#include <unistd.h>



namespace Ivy {



/************************************************************************/
/*									*/
/*	IvySocketInfo -- basic socket wrapper			*/
/*									*/
/************************************************************************/


class IvySocketInfo {

public:
   typedef int Error;

private:
   int socket_id;
   Error last_error;

public:
   IvySocketInfo()			{ socket_id = -1; last_error = 0; }
   IvySocketInfo(int fid)		{ socket_id = fid; last_error = 0; }
   virtual ~IvySocketInfo()		{ }

   bool create() {
      socket_id = socket(AF_INET,SOCK_STREAM,0);
      return socket_id >= 0;
    }

   void close() {
      ::close(socket_id);
      socket_id = -1;
    }

   int send(const void * buf,int bln) {
      int i = ::send(socket_id,(const char *) buf,bln,0);
      if (i < 0) last_error = errno;
      return i;
    }

   int send(const string& str) {
      return send(str.c_str(),str.length());
    }

   int receive(void * buf,int bln) {
      int i = ::recv(socket_id,(char *) buf,bln,0);
      if (i < 0) last_error = errno;
      return i;
    }

   StdString readLine() {
      char buf[10240];
      int j = 0;
      for ( ; ; ) {
	 int i = ::recv(socket_id,&buf[j],1,0);
	 if (i < 0) {
	    last_error = errno;
	    break;
	  }
	 if (i != 0) {
	    if (buf[j] == '\n') {
	       buf[j] = 0;
	       break;
	     }
	  }
	 j += i;
	 if (j >= 10230) break;
       }
      buf[j] = 0;
      return buf;
    }

   inline bool bind(int port = 0);
   inline bool getSockName(char * host,int& port);
   inline bool enableNonblocking(bool fg);
   inline bool listen(int n = 5);
   inline bool accept(IvySocketInfo * new_socket);
   inline bool connect(const char * host,int port);

   Error getLastError() 		{ return last_error; }

   bool createServer(int port = 0) {
      if (!create()) return false;
      if (!bind(port)) return false;
      if (!listen()) return false;
      return true;
    }

   inline bool createClient(const char * host,int port = 0);

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

   int getFid() const				{ return socket_id; }
};




/************************************************************************/
/*									*/
/*	Implementations 						*/
/*									*/
/************************************************************************/


inline bool
IvySocketInfo::bind(int port)
{
   sockaddr_in name;
   int nln = sizeof(name);

   memset(&name,0,nln);
   name.sin_family = AF_INET;
   name.sin_port = htons(port);

   if (::bind(socket_id,(struct sockaddr *) &name,nln) < 0) {
      last_error = errno;
      return false;
    }

   return true;
}



inline bool
IvySocketInfo::getSockName(char * host,int& port)
{
   sockaddr_in name;
   socklen_t nln = sizeof(name);
   char hbuf[1024];
   hostent * he;

   if (getsockname(socket_id,(struct sockaddr *)&name,&nln) < 0) {
      last_error = errno;
      return false;
    }

   if (name.sin_addr.s_addr == 0) {
      gethostname(hbuf,1024);
      strcpy(host,hbuf);
      he = gethostbyname(hbuf);
      name.sin_addr.s_addr = * ((int *) he->h_addr);
    }
   else {
      he = gethostbyaddr((char *) &name,nln,AF_INET);
      strcpy(host,he->h_name);
    }

   port = ntohs(name.sin_port);

   return true;
}



inline bool
IvySocketInfo::enableNonblocking(bool fg)
{
   if (socket_id < 0) return false;

   int i = fcntl(socket_id,F_GETFL,0);
   int j = 0;

#ifdef O_NONBLOCK
   j |= O_NONBLOCK;
#endif
#ifdef O_NDELAY
   j |= O_NDELAY;
#endif

   if (fg) i |= j;
   else i &= ~j;

   if (fcntl(socket_id,F_SETFL,i) < 0) {
      last_error = errno;
      return false;
    }

   return true;
}



inline bool
IvySocketInfo::listen(int n)
{
   if (::listen(socket_id,n) < 0) {
      last_error = errno;
      return false;
    }

   return true;
}



inline bool
IvySocketInfo::accept(IvySocketInfo * news)
{
   sockaddr addr;
   socklen_t addrlen = sizeof(addr);
   int ns;

   if (news == NULL) return FALSE;

   ns = ::accept(socket_id,&addr,&addrlen);

   if (ns < 0) {
      last_error = errno;
      return false;
    }

   news->socket_id = ns;

   return true;
}



inline bool
IvySocketInfo::connect(const char * host,int port)
{
   sockaddr_in name;
   struct hostent hbuf;
   int herrno;
   char buf[1024];

   memset(&name,0,sizeof(name));
   name.sin_family = AF_INET;
   name.sin_port = htons(port);

   hostent * he = gethostbyname_r(host,&hbuf,buf,1024,&herrno);
   if (he != NULL) {
      name.sin_addr.s_addr = *((int *) he->h_addr);
    }
   else {
      int addr = (int) inet_addr(host);
      if (addr == -1) return false;
      name.sin_addr.s_addr = addr;
    }

   while (::connect(socket_id,(sockaddr *) &name,sizeof(name)) < 0) {
      if (errno != EINPROGRESS) {
	 last_error = errno;
	 return false;
       }
    }

   return true;
}



inline bool
IvySocketInfo::createClient(const char * host,int port)
{
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




}	// end of namespace ivy



#endif


/* end of ivy_unixsocket.H */
