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



import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodReference;
import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.LinkedHashSet;
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

abstract public JcompSymbol lookupVariable(String nm);

public Collection<JcompSymbol> getAllSymbols()
{
   Collection<JcompSymbol> rslt = new LinkedHashSet<>();
   getAllSymbols(rslt);
   for (JcompScope p = getParent(); p != null; p = p.getParent()) {
      p.getAllSymbols(rslt);
    }
   return rslt;
}


abstract protected void getAllSymbols(Collection<JcompSymbol> rslt);



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


JcompSymbol defineMethod(String nm,AnnotationTypeMemberDeclaration n)
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

public JcompSymbol lookupMethod(String id,JcompType aty)
{
   return lookupExactMethod(id,aty);
}

abstract JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType aty,JcompType base,ASTNode n);
abstract JcompSymbol lookupExactMethod(String id,JcompType aty);
abstract List<JcompSymbol> lookupStatics(String id);
abstract void getFields(Map<String,JcompType> flds);
abstract Set<JcompSymbol> lookupAbstracts(JcompTyper typer);

public Collection<JcompSymbol> getDefinedMethods()	      { return null; }
public Collection<JcompSymbol> getDefinedFields()	      { return null; }



/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

static boolean isBetterMethod(JcompType ctyp,JcompSymbol mth1,JcompSymbol mth2)
{
   JcompType m1 = mth1.getType();
   JcompType m2 = mth2.getType();
   List<JcompType> args = ctyp.getComponents();
   List<JcompType> m1args = m1.getComponents();
   List<JcompType> m2args = m2.getComponents();

   if (m1args.size() != m2args.size()) {
      if (m1args.size() == args.size()) return true;
      else return false;
    }
   if (m1args.size() != args.size()) return false;

   if (m1.isVarArgs() && !m2.isVarArgs()) return false;
   if (!m1.isVarArgs() && m2.isVarArgs()) return true;

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
   if (ct1 > ct2) return false;

   if (mth1.isAbstract() && !mth2.isAbstract()) return false;
   if (mth2.isAbstract() && !mth1.isAbstract()) return true;

   int ctx = getTypeDepth(mth1.getClassType(),mth2.getClassType());
   if (ctx == 0) {
      if ((mth1.getModifiers() & Opcodes.ACC_BRIDGE) != 0) return false;
      else if ((mth2.getModifiers() & Opcodes.ACC_BRIDGE) != 0) return true;
      if ((mth1.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0) return false;
      else if ((mth2.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0) return true;
    }
   if (ctx >= 0) return true;
   ctx = getTypeDepth(mth2.getClassType(),mth1.getClassType());
   if (ctx >= 0) return false;

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

   int ct = getTypeDepth(tto,tfrom);
   if (ct > 0) return 11+ct;

   return 20;
}


static int getTypeDepth(JcompType tgt,JcompType tfrom)
{
   if (tgt == tfrom) return 0;
   if (tfrom == null) return -1;
   if (tgt.isParameterizedType() || tfrom.isParameterizedType()) {
      JcompType ptgt = tgt;
      if (tgt.isParameterizedType()) ptgt = tgt.getBaseType();
      JcompType pfrom = tfrom;
      if (tfrom.isParameterizedType()) pfrom = tfrom.getBaseType();
      return getTypeDepth(ptgt,pfrom);
    }
   if (tgt.getName().equals(tfrom.getName())) return 0;

   int ct = getTypeDepth(tgt,tfrom.getSuperType());
   if (ct >= 0) return ct+1;
   if (tfrom.getInterfaces() != null) {
      for (JcompType t0 : tfrom.getInterfaces()) {
	 ct = getTypeDepth(tgt,t0);
	 if (ct >= 0) return ct+1;
       }
    }

   return -1;
}




}	// end of abstract class JcompScope



/* end of JcompScope.java */
