/************************************************************************/
/*									*/
/*		ivy_solaris.H						*/
/*									*/
/*	Definitions for compiling Ivy under solaris			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_solaris.H,v 1.1 2005/06/28 17:22:11 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_solaris.H,v $
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_SOLARIS_ALREADY_INCLUDED
#define IVY_SOLARIS_ALREADY_INCLUDED

#ifdef __sparc

#include <unistd.h>
#include <sys/wait.h>
#include <netinet/in.h>
#include <signal.h>
#include <sys/mman.h>
#include <dlfcn.h>

#define SOLARIS


// #define IOSTREAM_NOT_LARGEFILE	   1


#ifdef __sparcv9
#define SOLARIS64
#endif



/************************************************************************/
/*									*/
/*	Extended types							*/
/*									*/
/************************************************************************/


typedef long long		LongLong;
typedef unsigned long long	ULongLong;



/********************************************************************************/
/*										*/
/*	Network order macros							*/
/*										*/
/********************************************************************************/


#define htonll(x)	(x)
#define ntohll(x)	(x)



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



/* end of ivy_solaris.H */
