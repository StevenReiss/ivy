/********************************************************************************/
/*										*/
/*		JcompScopeFixed.java					*/
/*										*/
/*	Class to represent a fixed scope					*/
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


class JcompScopeFixed extends JcompScope implements JcompConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<String,JcompSymbol> var_names;
private Map<String,Collection<JcompSymbol>> method_names;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompScopeFixed()
{
   var_names = new HashMap<>();
   method_names = new HashMap<>();
}




/********************************************************************************/
/*										*/
/*	Variable methods							*/
/*										*/
/********************************************************************************/

@Override synchronized void defineVar(JcompSymbol s)
{
   var_names.put(s.getName(),s);
}

@Override synchronized void defineDupVar(JcompSymbol s)
{
   if (var_names.get(s.getFullName()) != null) return;

   var_names.put(s.getFullName(),s);
}





@Override synchronized JcompSymbol lookupVariable(String nm)
{
   return var_names.get(nm);
}



@Override public synchronized Collection<JcompSymbol> getDefinedFields()
{
   Collection<JcompSymbol> rslt = new ArrayList<JcompSymbol>();

   for (JcompSymbol js : var_names.values()) {
      if (js.isFieldSymbol()) rslt.add(js);
    }

   return rslt;
}





/********************************************************************************/
/*										*/
/*	Method definition methods						*/
/*										*/
/********************************************************************************/

@Override synchronized void defineMethod(JcompSymbol js)
{
   Collection<JcompSymbol> ms = method_names.get(js.getName());
   if (ms == null) {
      ms = new ArrayList<JcompSymbol>();
      method_names.put(js.getName(),ms);
    }
   ms.add(js);
}



@Override JcompSymbol lookupMethod(String id,JcompType aty)
{
   Collection<JcompSymbol> ljs;

   ljs = method_names.get(id);
   if (ljs != null) ljs = new ArrayList<JcompSymbol>(ljs);

   if (ljs != null) {
      JcompSymbol bestms = null;
      for (JcompSymbol js : ljs) {
	 if (aty.isCompatibleWith(js.getType())) {
	    if (bestms == null) bestms = js;
	    else if (isBetterMethod(aty,js.getType(),bestms.getType()))
	       bestms = js;
	  }
       }
      if (bestms != null) return bestms;
    }

   return null;
}


@Override synchronized List<JcompSymbol> lookupStatics(String id)
{
   List<JcompSymbol> rslt = null;

   Collection<JcompSymbol> ljs = method_names.get(id);
   if (ljs != null) {
      for (JcompSymbol js : ljs) {
	 if (js != null && js.isStatic()) {
	    if (rslt == null) rslt = new ArrayList<JcompSymbol>();
	    rslt.add(js);
	  }
       }
    }

   JcompSymbol js = var_names.get(id);
   if (js != null && js.isStatic()) {
      if (rslt == null) rslt = new ArrayList<JcompSymbol>();
      rslt.add(js);
    }

   return rslt;
}


@Override void getFields(Map<String,JcompType> flds)
{
   for (Map.Entry<String,JcompSymbol> ent : var_names.entrySet()) {
      JcompSymbol fld = ent.getValue();
      if (fld.isFieldSymbol()) {
	 flds.put(fld.getFullName(),fld.getType());
       }
    }
   if (getParent() != null) getParent().getFields(flds);
}



@Override synchronized Set<JcompSymbol> lookupAbstracts(JcompTyper typer)
{
   Set<JcompSymbol> rslt = new HashSet<JcompSymbol>();

   for (JcompSymbol js : getDefinedMethods()) {
      if (js.isAbstract()) {
	 rslt.add(js);
       }
    }

   return rslt;
}




@Override public synchronized Collection<JcompSymbol> getDefinedMethods()
{
   Collection<JcompSymbol> rslt = new ArrayList<JcompSymbol>();
   for (Collection<JcompSymbol> csm : method_names.values()) {
      rslt.addAll(csm);
    }

   return rslt;
}




}	// end of class JcompScopeFixed



/* end of JcompScopeFixed.java */




































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































