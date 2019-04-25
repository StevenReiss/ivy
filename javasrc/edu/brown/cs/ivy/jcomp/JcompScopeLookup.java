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



import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;


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




JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType aty,JcompScope js,JcompType basetype,ASTNode n)
{
   List<MethodElement> lme = method_names.get(id);
   if (lme == null) {
      return null;
    }
   else lme = new ArrayList<>(lme);
   
   while (js != null) {
      for (MethodElement me : lme) {
	 if (me.getScope() == js) {
	    JcompSymbol bestms = null;
	    for (JcompSymbol ms : me.getMethods()) {
               if (basetype != null && n != null) {
                  if (!JcompType.checkProtections(ms,basetype,n)) continue;
                }
               if (aty == null) bestms = ms;
	       else if (aty.isCompatibleWith(ms.getType())) {
		  if (bestms == null) bestms = ms;
		  else if (JcompScope.isBetterMethod(aty,ms,bestms))
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



JcompSymbol lookupExactMethod(String id,JcompType aty,JcompScope js)
{
   List<MethodElement> lme = method_names.get(id);
   if (lme == null) {
      return null;
    }
   
   if (js != null) {
      for (MethodElement me : lme) {
	 if (me.getScope() == js) {
	    for (JcompSymbol ms : me.getMethods()) {
	       if (aty.equals(ms.getType())) {
                  return ms;
		}
	     }
	  }
       }
    }
   
  // System.err.println("Couldn't find method " + id + " " + aty);
   
   return null;
}






List<JcompSymbol> lookupStatics(String id,JcompScope jscp)
{
   List<JcompSymbol> rslt = null;
   
   if (id == null) {
      Set<JcompSymbol> r2 = new HashSet<>();
      for (String s : method_names.keySet()) {
         List<JcompSymbol> r1 = lookupStatics(s,jscp);
         if (r1 != null) r2.addAll(r1);
       }
      for (String s : var_names.keySet()) {
         List<JcompSymbol> r1 = lookupStatics(s,jscp);
         if (r1 != null) r2.addAll(r1);
       }
      if (r2.size() == 0) return null;
      rslt = new ArrayList<>(r2);
      return rslt;
    }
   
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
      for_methods = new ArrayList<>();
    }

   synchronized void add(JcompSymbol js)   { for_methods.add(js); }

   JcompScope getScope()			      { return for_scope; }
   synchronized Collection<JcompSymbol> getMethods()       { 
      return new ArrayList<>(for_methods);
   }

}	// end of subclass MethodElement





}	// end of class JcompScopeLookup




/* end of JcompScopeLookup.java */
