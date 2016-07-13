/********************************************************************************/
/*										*/
/*		JflowFlags.java 						*/
/*										*/
/*	Class to use to define and maintain value flags in Jflow		*/
/*										*/
/********************************************************************************/
/*	Copyright 2006 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2006, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/JflowFlags.java,v 1.2 2007-08-10 02:10:32 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JflowFlags.java,v $
 * Revision 1.2  2007-08-10 02:10:32  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.1  2007-05-04 02:07:15  spr
 * Add new flags class.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow;






public abstract class JflowFlags
{



/********************************************************************************/
/*										*/
/*	Constants								*/
/*										*/
/********************************************************************************/

public static final short	NON_NULL = 0;			// known non-null
public static final short	MUST_BE_NULL = 0x1;		// known null
public static final short	CAN_BE_NULL = 0x2;		// possibly null
public static final short	NULL = CAN_BE_NULL | MUST_BE_NULL;
public static final short	TEST_NULL = 0x4;		// checked for null
public static final short	SET_NULL = 0x8; 		// null explicitly set
public static final short	USE_DIRECT = 0x10;		// used without checking for null
public static final short	NEW_NULL = NULL | SET_NULL;

private static final short	AND_FLAGS = MUST_BE_NULL;




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public static boolean mustBeNull(short fgs)
{
   return (fgs & MUST_BE_NULL) != 0;
}



public static boolean canBeNull(short fgs)
{
   return (fgs & CAN_BE_NULL) != 0;
}



public static boolean testForNull(short fgs)
{
   return (fgs & TEST_NULL) != 0;
}


public static boolean nullExplicitlySet(short fgs)
{
   return (fgs & SET_NULL) != 0;
}



/********************************************************************************/
/*										*/
/*	Setting methods 							*/
/*										*/
/********************************************************************************/

public static short merge(short fg1,short fg2)
{
   short fgs = (short) ((fg1 | fg2) & ~AND_FLAGS);
   short fga = (short) (fg1 & fg2 & AND_FLAGS);

   fgs |= fga;

   return fgs;
}



public static short forceNonNull(short fgs)
{
   return (short) (fgs & ~(NULL | SET_NULL));
}


public static short fixup(short fgs)
{
   if ((fgs & MUST_BE_NULL) != 0) fgs |= CAN_BE_NULL;

   return fgs;
}



}	// end of class JflowFlags




/* end of JflowFlags.java */
