/********************************************************************************/
/*										*/
/*		JflowValue.java 						*/
/*										*/
/*	Representation of a value used in flow analysis 			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/JflowValue.java,v 1.5 2007-08-10 02:10:32 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JflowValue.java,v $
 * Revision 1.5  2007-08-10 02:10:32  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.4  2007-05-04 01:59:52  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.3  2007-02-27 18:53:26  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.2  2006/07/10 14:52:16  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:30  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow;



import com.ibm.jikesbt.BT_Class;

public interface JflowValue extends JflowConstants
{


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

boolean isCategory2();
boolean canBeNull();
boolean mustBeNull();
boolean nullExplicitlySet();
short getFlags();

BT_Class getDataType();

Iterable<JflowSource> getSourceCollection();

Object getProgramValue();

boolean hasFieldSource();
boolean containsSource(JflowSource s);
boolean containsModelSource(JflowModelSource ms);
boolean isEmptySourceSet();

JflowValue merge(JflowValue v);


}	// end of interface JflowValue




/* end of JflowValue.java */
