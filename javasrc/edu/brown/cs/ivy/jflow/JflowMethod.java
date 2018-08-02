
/********************************************************************************/
/*										*/
/*		JflowMethod.java						*/
/*										*/
/*	Representation of a method used in flow analysis			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/JflowMethod.java,v 1.4 2007-02-27 18:53:26 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JflowMethod.java,v $
 * Revision 1.4  2007-02-27 18:53:26  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.3  2006/07/10 14:52:15  spr
 * Code cleanup.
 *
 * Revision 1.2  2006/07/03 18:15:11  spr
 * Update flow with inlining options.  Clean up.
 *
 * Revision 1.1  2006/06/21 02:18:29  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow;



import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_CodeAttribute;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_Method;

import java.util.Set;

public interface JflowMethod extends JflowConstants
{


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

BT_Method getMethod();
BT_Class getMethodClass();
BT_CodeAttribute getCode();
BT_InsVector getCodeVector();

String getMethodName();
String getMethodSignature();

boolean getIgnoreBlock(BT_Ins ins);

Set<JflowMethod> getReplacementCalls(BT_Ins ins);
Set<JflowMethod> getAllReplacementCalls();

JflowValue getAssociation(AssociationType typ,BT_Ins ins);

Iterable<JflowMethod> getAllCalls(BT_Ins ins);
int getCallCount(BT_Ins ins);
boolean getIsAsync(BT_Method bm);
int getMaxThreads();
JflowValue getExceptionSet();
boolean hasSpecial();
Iterable<JflowValue> getParameterValues();

boolean isInProject();
JflowValue getThisValue();
boolean getDontScan();

boolean hasResult();
boolean getCanExit();

JflowValue getArgValue(int idx);



}	// end of interface JflowMethod




/* end of JflowMethod.java */




