/********************************************************************************/
/*										*/
/*		JcompConstants.java						*/
/*										*/
/*	Constants for Java specific code for JCOMP				*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2007, Brown University, Providence, RI.				 *
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



package edu.brown.cs.ivy.jcomp;

import edu.brown.cs.ivy.jcode.JcodeConstants;

public interface JcompConstants {



/********************************************************************************/
/*										*/
/*	Java attribute names							*/
/*										*/
/********************************************************************************/

String PROP_JAVA_TYPE = "JCOMP$Type";
String PROP_JAVA_SCOPE = "JCOMP$Scope";
String PROP_JAVA_REF = "JCOMP$Ref";
String PROP_JAVA_ETYPE = "JCOMP$ExprType";
String PROP_JAVA_DEF = "JCOMP$Def";
String PROP_JAVA_RESOLVED = "JCOMP$Resolved";
String PROP_JAVA_TYPER = "JCOMP$Typer";
String PROP_JAVA_SOURCE = "JCOMP$Source";
String PROP_JAVA_KEEP = "JCOMP$Keep";
String PROP_JAVA_PROJECT = "JCOMP$Project";





/********************************************************************************/
/*										*/
/*	Special Type names							*/
/*										*/
/********************************************************************************/

String TYPE_ANY_CLASS = "*ANY*";
String TYPE_ERROR = "*ERROR*";

int ASM_API = JcodeConstants.ASM_API;

}	// end of interface JcompConstants



/* end of JcompConstants.java */

