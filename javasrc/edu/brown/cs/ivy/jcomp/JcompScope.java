/********************************************************************************/
/*										*/
/*		JcompScope.java 					*/
/*										*/
/*	Class to represent an abstract scope					*/
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



import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodReference;

import java.util.*;


abstract public class JcompScope implements JcompConstants {



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public JcompScope getParent()		{ return null; }




/********************************************************************************/
/*										*/
/*	Variable methods							*/
/*										*/
/********************************************************************************/

abstract void defineVar(JcompSymbol s);

void defineDupVar(JcompSymbol s)
{
   defineVar(s);
}

abstract JcompSymbol lookupVariable(String nm);



/********************************************************************************/
/*										*/
/*	Method definition methods						*/
/*										*/
/********************************************************************************/

JcompSymbol defineMethod(String nm,MethodDeclaration n)
{
   JcompSymbol js = JcompSymbol.createSymbol(n);

   defineMethod(js);

   return js;
}


JcompSymbol defineLambda(LambdaExpression n)
{
   JcompSymbol js = JcompSymbol.createSymbol(n);
   
   defineMethod(js);
   
   return js;
}


JcompSymbol defineReference(MethodReference n)
{
   JcompSymbol js = JcompSymbol.createSymbol(n);
   
   defineMethod(js);
   
   return js;
}





abstract void defineMethod(JcompSymbol js);

abstract JcompSymbol lookupMethod(String id,JcompType aty);
abstract List<JcompSymbol> lookupStatics(String id);
abstract void getFields(Map<String,JcompType> flds);
abstract Set<JcompSymbol> lookupAbstracts(JcompTyper typer);

public Collection<JcompSymbol> getDefinedMethods()	      { return null; }
public Collection<JcompSymbol> getDefinedFields()	      { return null; }



}	// end of abstract class JcompScope



/* end of JcompScope.java */
