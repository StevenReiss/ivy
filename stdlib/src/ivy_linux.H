/************************************************************************/
/*									*/
/*		ivy_linux.H						*/
/*									*/
/*	Definitions for compiling Ivy under linux			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_linux.H,v 1.1 2005/06/28 17:22:11 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_linux.H,v $
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_LINUX_ALREADY_INCLUDED
#define IVY_LINUX_ALREADY_INCLUDED

#ifdef LINUX

#include <fcntl.h>
#include <dlfcn.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <unistd.h>
#include <netdb.h>
#include <netinet/in.h>
#include <signal.h>
#include <sys/mman.h>



/************************************************************************/
/*									*/
/*	Extended types							*/
/*									*/
/************************************************************************/


typedef long long		LongLong;
typedef unsigned long long	ULongLong;




/********************************************************************************/
/*										*/
/*	Handle missing definitions						*/
/*										*/
/********************************************************************************/


#ifndef O_LARGEFILE
#define O_LARGEFILE	0
#endif


#ifndef RTLD_NOLOAD
#define RTLD_NOLOAD	  0
#endif

#define NO_PROCESS_LOCKS



/********************************************************************************/
/*										*/
/*	Handle name changes							*/
/*										*/
/********************************************************************************/

#define fork1	fork

#define pthread_mutexattr_setpshared(x,y)
#define pthread_condattr_setpshared(x,y)




/************************************************************************/
/*									*/
/*	Handle missing functions					*/
/*									*/
/************************************************************************/


typedef long long	hrtime_t;



inline long long
gethrtime()
{
   struct timeval tv;

   gettimeofday(&tv,NULL);

   long long rslt = tv.tv_sec;
   rslt *= 1000000;
   rslt += tv.tv_usec;
   rslt *= 1000;

   return rslt;
}





inline struct hostent *
gethostbyname_r(const char * name, struct hostent * rslt, char *buf, int buflen,
		   int * h_errnop)
{
   struct hostent * result;

   gethostbyname_r(name,rslt,buf,buflen,&result,h_errnop);

   return result;			// is this right
}




inline char *
lltostr(long lval,char * eptr)
{
   if (lval == 0) {
      *--eptr = '0';
    }
   else if (lval > 0) {
      while (lval != 0) {
	 *--eptr = '0' + (lval%10);
	 lval = lval/10;
       }
    }
   else {
      lval = -lval;
      while (lval != 0) {
	 *--eptr = '0' + (lval%10);
	 lval = lval/10;
       }
      *--eptr = '-';
    }

   return eptr;
}




/********************************************************************************/
/*										*/
/*	Handle byte order methods						*/
/*										*/
/********************************************************************************/


inline long long
htonll(long long x)
{
   long long x1 = x & 0x0ffffffffLL;
   x1 = htonl(x1);
   long long x2 = (x >> 32) & 0x0ffffffffLL;
   x2 = htonl(x2);
   return (x2 << 32) | x1;
}



inline long long
ntohll(long long x)
{
   long long x1 = x & 0x0ffffffffLL;
   x1 = ntohl(x1);
   long long x2 = (x >> 32) & 0x0ffffffffLL;
   x2 = ntohl(x2);
   return (x2 << 32) | x1;
}




/************************************************************************/
/*									*/
/*	System-specific routines					*/
/*									*/
/************************************************************************/


inline bool processExists(int pid)
{
   if (kill(pid,0) >= 0) return true;
   return false;
}





#endif
#endif



/* end of ivy_linux.H */
