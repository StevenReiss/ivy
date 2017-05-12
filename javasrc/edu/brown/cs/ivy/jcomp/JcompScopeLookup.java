/********************************************************************************/
/*										*/
/*		JcompScopeLookup.java					*/
/*										*/
/*	Class to handle scope-based lookup for a compilation unit		*/
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



import java.util.*;


class JcompScopeLookup implements JcompConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<String,List<VarElement>> var_names;
private Map<String,List<MethodElement>> method_names;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompScopeLookup()
{
   var_names = new HashMap<String,List<VarElement>>();
   method_names = new HashMap<String,List<MethodElement>>();
}



/********************************************************************************/
/*										*/
/*	Variable methods							*/
/*										*/
/********************************************************************************/

void defineVar(JcompSymbol s,JcompScope js)
{
   List<VarElement> lve = var_names.get(s.getName());
   if (lve == null) {
      lve = new ArrayList<VarElement>();
      var_names.put(s.getName(),lve);
    }
   lve.add(new VarElement(s,js));
}




JcompSymbol lookupVariable(String nm,JcompScope js)
{
   List<VarElement> lve = var_names.get(nm);
   if (lve == null) return null;
   while (js != null) {
      for (VarElement ve : lve) {
	 if (ve.getScope() == js) return ve.getSymbol();
       }
      js = js.getParent();
    }

   return null;
}



Collection<JcompSymbol> getDefinedFields(JcompScope js)
{
   Collection<JcompSymbol> rslt = new ArrayList<JcompSymbol>();

   for (List<VarElement> lve : var_names.values()) {
      for (VarElement ve : lve) {
	 if (ve.getScope() == js && ve.getSymbol().isFieldSymbol()) rslt.add(ve.getSymbol());
       }
    }

   return rslt;
}




private static class VarElement {

   private JcompSymbol for_symbol;
   private JcompScope for_scope;

   VarElement(JcompSymbol s,JcompScope scp) {
      for_symbol = s;
      for_scope = scp;
    }

   JcompScope getScope()		{ return for_scope; }
   JcompSymbol getSymbol()		{ return for_symbol; }

}	// end of subclass VarElement





/********************************************************************************/
/*										*/
/*	Method symbol methods							*/
/*										*/
/********************************************************************************/

void defineMethod(JcompSymbol js,JcompScope scp)
{
   List<MethodElement> lme = method_names.get(js.getName());
   if (lme == null) {
      lme = new ArrayList<MethodElement>();
      method_names.put(js.getName(),lme);
    }
   MethodElement useme = null;
   for (MethodElement me : lme) {
      if (me.getScope() == scp) {
	 useme = me;
	 break;
       }
    }
   if (useme == null) {
      useme = new MethodElement(scp);
      lme.add(useme);
    }
   useme.add(js);
}




JcompSymbol lookupMethod(String id,JcompType aty,JcompScope js)
{
   List<MethodElement> lme = method_names.get(id);
   if (lme == null) {
      return null;
    }
   while (js != null) {
      for (MethodElement me : lme) {
	 if (me.getScope() == js) {
	    JcompSymbol bestms = null;
	    for (JcompSymbol ms : me.getMethods()) {
	       if (aty.isCompatibleWith(ms.getType())) {
		  if (bestms == null) bestms = ms;
		  else if (isBetterMethod(aty,ms.getType(),bestms.getType()))
		     bestms = ms;
		}
	     }
	    if (bestms != null) return bestms;
	  }
       }
      js = js.getParent();
    }

   return null;
}


private static boolean isBetterMethod(JcompType ctyp,JcompType m1,JcompType m2)
{
   List<JcompType> args = ctyp.getComponents();
   List<JcompType> m1args = m1.getComponents();
   List<JcompType> m2args = m2.getComponents();
   if (m1args.size() != m2args.size()) {
      if (m1args.size() == args.size()) return true;
      else return false;
    }
   if (m1args.size() != args.size()) return false;
   for (int i = 0; i < args.size(); ++i) {
      JcompType t0 = args.get(i);
      JcompType t1 = m1args.get(i);
      JcompType t2 = m2args.get(i);
      if (t1 == t2) continue;
      if (t0 == t1) return true;
      if (t0 == t2) return false;
      // should check subtypes here as well
    }
   return false;
}



List<JcompSymbol> lookupStatics(String id,JcompScope jscp)
{
   List<JcompSymbol> rslt = null;
   List<MethodElement> lme = method_names.get(id);
   if (lme != null) {
      for (MethodElement me : lme) {
	 for (JcompSymbol js : me.getMethods()) {
	    if (!js.isStatic()) continue;
	    if (rslt == null) rslt = new ArrayList<JcompSymbol>();
	    rslt.add(js);
	  }
       }
    }

   List<VarElement> lve = var_names.get(id);
   if (lve != null) {
      for (VarElement ve : lve) {
	 JcompSymbol js = ve.getSymbol();
	 if (!js.isStatic()) continue;
	 if (rslt == null) rslt = new ArrayList<JcompSymbol>();
	 rslt.add(js);
       }
    }

   return rslt;
}


void getFields(Map<String,JcompType> flds,JcompScope scope)
{
   for (Map.Entry<String,List<VarElement>> ent : var_names.entrySet()) {
      for (VarElement ve : ent.getValue()) {
	 if (ve.getScope() == scope) {
	    JcompSymbol js = ve.getSymbol();
	    if (js.isFieldSymbol()) {
	       flds.put(js.getFullName(),js.getType());
	     }
	  }
       }
    }
}




Set<JcompSymbol> lookupAbstracts(JcompTyper typer)
{
   Set<JcompSymbol> rslt = new HashSet<JcompSymbol>();
   for (List<MethodElement> lme : method_names.values()) {
      for (MethodElement me : lme) {
	 for (JcompSymbol js : me.getMethods()) {
	    if (js.isStatic() || !js.isAbstract()) continue;
	    rslt.add(js);
	  }
       }
    }

   return rslt;
}





Collection<JcompSymbol> getDefinedMethods(JcompScope js)
{
   Collection<JcompSymbol> rslt = new ArrayList<JcompSymbol>();

   for (List<MethodElement> lme : method_names.values()) {
      for (MethodElement me : lme) {
	 if (me.getScope() == js) rslt.addAll(me.getMethods());
       }
    }

   return rslt;
}




private static class MethodElement {

   private JcompScope for_scope;
   private Collection<JcompSymbol> for_methods;

   MethodElement(JcompScope scp) {
      for_scope = scp;
      for_methods = new ArrayList<JcompSymbol>();
    }

   void add(JcompSymbol js)		      { for_methods.add(js); }

   JcompScope getScope()			      { return for_scope; }
   Collection<JcompSymbol> getMethods()       { return for_methods; }

}	// end of subclass MethodElement





}	// end of class JcompScopeLookup




/* end of JcompScopeLookup.java */
