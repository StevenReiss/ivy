/********************************************************************************/
/*										*/
/*		JflowMethodData.java						*/
/*										*/
/*	User class for holding information associated with a JflowMethod	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/JflowMethodData.java,v 1.4 2007-01-03 14:04:57 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JflowMethodData.java,v $
 * Revision 1.4  2007-01-03 14:04:57  spr
 * Fix imports
 *
 * Revision 1.3  2007-01-03 03:24:15  spr
 * Modifications to handle incremental update.
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



import com.ibm.jikesbt.BT_Ins;

import java.util.Map;



public interface JflowMethodData extends JflowConstants
{



/********************************************************************************/
/*										*/
/*	Set methods								*/
/*										*/
/********************************************************************************/

abstract public void setAssociation(AssociationType typ,BT_Ins ins,JflowValue value);

abstract public JflowValue getAssociation(AssociationType typ,BT_Ins ins);

abstract public void clear();


abstract public void updateValues(Map<JflowValue,JflowValue> valupdates);




}	// end of class JflowMethodData




/* end of JflowMethodData.java */
