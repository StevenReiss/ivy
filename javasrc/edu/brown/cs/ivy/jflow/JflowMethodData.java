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

void setAssociation(AssociationType typ,BT_Ins ins,JflowValue value);

JflowValue getAssociation(AssociationType typ,BT_Ins ins);

void clear();


void updateValues(Map<JflowValue,JflowValue> valupdates);




}	// end of class JflowMethodData




/* end of JflowMethodData.java */
