/************************************************************************/
/*									*/
/*		ivy_memory.H						*/
/*									*/
/*	Memory routine in-line implementations				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_memory.H,v 1.1 2005/06/28 17:22:11 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_memory.H,v $
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_MEMORY_INCLUDED_ALREADY
#define IVY_MEMORY_INCLUDED_ALREADY


namespace Ivy {


#define register


inline void
IVY_copy_region(void * to,const void * from,int ln)
{
   register int i = ((long) to)|((long) from)|ln;

   if ((i&3) == 0) {
      register int * ip = (int *) from;
      register int * op = (int *) to;
      ln /= 4;
      for (i = 0; i < ln; ++i) *op++ = *ip++;
    }
   else {
      register char * ip = (char *) from;
      register char * op = (char *) to;
      for (i = 0; i < ln; ++i) *op++ = *ip++;
    }
}



}	// end of namespace ivy

#endif



/* end of ivy_memory.H */
