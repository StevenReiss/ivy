/************************************************************************/
/*									*/
/*		ivy_apple.H						*/
/*									*/
/*	Definitions for compiling Ivy under Mac OS 10			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_apple.H,v 1.9 2015/02/14 21:22:22 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_apple.H,v $
 * Revision 1.9  2015/02/14 21:22:22  spr
 * Update apple definitions for newer os.
 *
 * Revision 1.8  2013/11/08 01:32:18  spr
 * Updates for newer g++ on mac.
 *
 * Revision 1.7  2011-03-10 02:59:17  spr
 * Updates to handle timers on the mac.
 *
 * Revision 1.6  2007-09-22 02:59:16  spr
 * Remove extra declarations that are now in the apple include files.
 *
 * Revision 1.5  2007-05-12 00:44:51  spr
 * Fix up defs to get them to work on multiple machines.
 *
 * Revision 1.4  2007-05-12 00:35:44  spr
 * fix definitions for mac.
 *
 * Revision 1.3  2006-08-30 00:36:33  spr
 * Update dynamic loading functions.
 *
 * Revision 1.2  2006-08-27 03:11:26  spr
 * Fix includes for mac os/x.
 *
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_APPLE_ALREADY_INCLUDED
#define IVY_APPLE_ALREADY_INCLUDED

#ifdef APPLE

#include <sys/types.h>

#include <sys/types.h>
#include <fcntl.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <unistd.h>
#include <netdb.h>
#include <netinet/in.h>
#include <signal.h>
#include <sys/mman.h>
#include <pthread.h>
#include <dirent.h>
#include <semaphore.h>
#include <strings.h>
#include <dlfcn.h>

#include <mach-o/dyld.h>
#include <mach/clock_types.h>
#include <mach/clock.h>

#include <stdlib.h>



/************************************************************************/
/*									*/
/*	Extended types							*/
/*									*/
/************************************************************************/

typedef unsigned char		u_char;
typedef unsigned short		u_short;
typedef unsigned int		u_int;
typedef unsigned long		u_long;
typedef unsigned short		ushort; 	/* Sys V compatibility */
typedef unsigned int		uint;		/* Sys V compatibility */

typedef long long		LongLong;
typedef unsigned long long	ULongLong;
// typedef int			   socklen_t;


#define NO_RWLOCKS
#define NO_SEMAS



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

#ifndef RTLD_LAZY
#define RTLD_LAZY	  0
#endif



/********************************************************************************/
/*										*/
/*	Handle name changes							*/
/*										*/
/********************************************************************************/

#define fork1	fork

#define pthread_mutexattr_setpshared(x,y)
#define pthread_condattr_setpshared(x,y)


#define h_addr h_addr_list[0]



/************************************************************************/
/*									*/
/*	Handle missing functions					*/
/*									*/
/************************************************************************/


typedef long long	hrtime_t;

// extern double scalb(double,double);

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
   struct hostent * rd = gethostbyname(name);

   if (rd == NULL) return NULL;

   *rslt = *rd;

   return rslt;
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




inline long
lrand48()
{
   return rand();
}


#define lseek64 lseek

inline int getpagesize()			{ return 4096; }



/********************************************************************************/
/*										*/
/*	Handle byte order methods						*/
/*										*/
/********************************************************************************/

#ifndef htonll
inline long long
htonll(long long x)
{
   long long x1 = x & 0x0ffffffffLL;
   x1 = htonl(x1);
   long long x2 = (x >> 32) & 0x0ffffffffLL;
   x2 = htonl(x2);
   return (x2 << 32) | x1;
}
#endif


#ifndef ntohll
inline long long
ntohll(long long x)
{
   long long x1 = x & 0x0ffffffffLL;
   x1 = ntohl(x1);
   long long x2 = (x >> 32) & 0x0ffffffffLL;
   x2 = ntohl(x2);
   return (x2 << 32) | x1;
}
#endif



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



/********************************************************************************/
/*										*/
/*	Missing pthread methods 						*/
/*										*/
/********************************************************************************/

inline int pthread_sigmask(int how,const sigset_t * nset,sigset_t * oset)
{
   return 1;
}


inline int pthread_getconcurrency()
{
   return 1;
}


inline int pthread_setconcurrency(int d)
{ return 1; }


#endif
#endif



/* end of ivy_apple.H */
