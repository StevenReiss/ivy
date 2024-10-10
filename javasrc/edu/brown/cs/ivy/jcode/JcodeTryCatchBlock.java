/********************************************************************************/
/*										*/
/*		JcodeTryCatchBlock.java 					*/
/*										*/
/*	Definitions for a try-catch block					*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
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
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH  HE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.ivy.jcode;


import org.objectweb.asm.Label;


public final class JcodeTryCatchBlock
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcodeMethod for_method;
private Label start_label;
private Label end_label;
private Label handler_label;
private String data_type;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcodeTryCatchBlock(JcodeMethod m,Label start,Label end,Label handler,String typ)
{
   for_method = m;
   start_label = start;
   end_label = end;
   handler_label = handler;
   data_type = typ;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public JcodeInstruction getStart()
{
   return for_method.findInstruction(start_label);
}


public JcodeInstruction getEnd()
{
   return for_method.findInstruction(end_label);
}


public JcodeInstruction getHandler()
{
   return for_method.findInstruction(handler_label);
}


public JcodeDataType getException()
{
   if (data_type == null) return null;
   return for_method.getFactory().findClassType(data_type);
}



}	// end of class JcodeTryCatchBlock

