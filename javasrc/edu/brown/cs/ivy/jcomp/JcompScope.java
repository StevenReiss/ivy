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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
abstract JcompSymbol lookupExactMethod(String id,JcompType aty);
abstract List<JcompSymbol> lookupStatics(String id);
abstract void getFields(Map<String,JcompType> flds);
abstract Set<JcompSymbol> lookupAbstracts(JcompTyper typer);

public Collection<JcompSymbol> getDefinedMethods()	      { return null; }
public Collection<JcompSymbol> getDefinedFields()	      { return null; }



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

static boolean isBetterMethod(JcompType ctyp,JcompType m1,JcompType m2)
{
   List<JcompType> args = ctyp.getComponents();
   List<JcompType> m1args = m1.getComponents();
   List<JcompType> m2args = m2.getComponents();
   if (m1args.size() != m2args.size()) {
      if (m1args.size() == args.size()) return true;
      else return false;
    }
   if (m1args.size() != args.size()) return false;
   
   int ct1 = 0;
   int ct2 = 0;
   
   for (int i = 0; i < args.size(); ++i) {
      JcompType t0 = args.get(i);
      JcompType t1 = m1args.get(i);
      JcompType t2 = m2args.get(i);
      if (t1 == t2) continue;
      // if (t0 == t1) return true;
      // if (t0 == t2) return false;
      ct1 += typeComparison(t1,t0);
      ct2 += typeComparison(t2,t0);
    }
   if (ct1 < ct2) return true;
   return false;
}



private static int typeComparison(JcompType tto,JcompType tfrom)
{
   if (tto == tfrom) return 0;
   if (tto.isNumericType()) {
      if (!tfrom.isNumericType()) return 20;
      if (tto.isFloatingType()) {
         if (tfrom.isFloatingType()) return 5;
         else return 10;
       }
      else return 5;
    }
   else if (tfrom.isNumericType()) return 10;
   
   // might want to check subtype depth here
   
   return 20;
}




}	// end of abstract class JcompScope



/* end of JcompScope.java */
