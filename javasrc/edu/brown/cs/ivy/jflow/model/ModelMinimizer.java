/********************************************************************************/
/*										*/
/*		ModelMinimizer.java						*/
/*										*/
/*	Methods to minimize an automata using different matching algorithms	*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 1998, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/model/ModelMinimizer.java,v 1.4 2018/08/02 15:10:19 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ModelMinimizer.java,v $
 * Revision 1.4  2018/08/02 15:10:19  spr
 * Fix imports.
 *
 * Revision 1.3  2015/11/20 15:09:15  spr
 * Reformatting.
 *
 * Revision 1.2  2009-09-17 01:57:28  spr
 * Code cleanup for eclipse.
 *
 * Revision 1.1  2007-08-10 23:47:19  spr
 * Move minimization code to its own class.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.model;


import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;



class ModelMinimizer implements JflowConstants
{



/********************************************************************************/
/*										*/
/*	Public storage								*/
/*										*/
/********************************************************************************/

enum MatchType {
   FULL,
   SUBSET,
   STATE
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ModelMaster	model_master;
private Matcher 	node_matcher;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelMinimizer(ModelMaster mm,MatchType mt)
{
   model_master = mm;
   switch (mt) {
      case FULL :
	 node_matcher = new FullMatcher();
	 break;
      case SUBSET :
	 node_matcher = new SubMatcher();
	 break;
      case STATE :
	 node_matcher = new MinMatcher();
	 break;
    }
}



static void minimize(ModelMaster jm,MatchType mt,JflowModel.Node s)
{
   ModelMinimizer mm = new ModelMinimizer(jm,mt);
   mm.minimize(s);
}



static void minimize(ModelMaster jm,JflowModel.Node s)
{
   ModelMinimizer mm = new ModelMinimizer(jm,MatchType.FULL);
   mm.minimize(s);
}



static void subsetMinimize(ModelMaster jm,JflowModel.Node s)
{
   ModelMinimizer mm = new ModelMinimizer(jm,MatchType.SUBSET);
   mm.minimize(s);
}



static void stateMinimize(ModelMaster jm,JflowModel.Node s)
{
   ModelMinimizer mm = new ModelMinimizer(jm,MatchType.STATE);
   mm.minimize(s);
}



/********************************************************************************/
/*										*/
/*	Fsa minimization methods						*/
/*										*/
/********************************************************************************/

void minimize(JflowModel.Node s)
{
   Vector<LinkedList<JflowModel.Node>> newstates = new Vector<>();
   Set<JflowModel.Node> done = new HashSet<>();
   Map<JflowModel.Node,JflowModel.Node> statemap = new HashMap<>();

   LinkedList<JflowModel.Node> l = getStateList(s,done,null,statemap,s);
   newstates.add(l);
   LinkedList<JflowModel.Node> allst = new LinkedList<>(l);

   boolean chng = true;
   while (chng) {
      chng = false;
      for (int i = 0; i < newstates.size(); ++i) {
	 LinkedList<JflowModel.Node> sl = newstates.elementAt(i);
	 LinkedList<JflowModel.Node> nl = null;
	 JflowModel.Node ns = null;

	 Iterator<JflowModel.Node> it1 = sl.iterator();
	 if (!it1.hasNext()) continue;
	 JflowModel.Node s0 = it1.next();
	 if (model_master.doDebug()) System.err.println("Minimize: Starting state is " + s0.getName());
	 while (it1.hasNext()) {
	    JflowModel.Node s1 = it1.next();
	    if (!node_matcher.matchStates(s0,s1,statemap)) {
	       if (nl == null) {
		  nl = new LinkedList<JflowModel.Node>();
		  ns = s1;
		  newstates.add(nl);
		  if (model_master.doDebug()) System.err.println("Minimize: Start new state for " + s1.getName());
		  chng = true;
		}
	       if (model_master.doDebug() && ns != null) 
		  System.err.println("Minimize: Add new state " + s1.getName() + " to " + ns.getName());
	       nl.add(s1);
	       it1.remove();
	     }
	    else {
	       if (model_master.doDebug()) System.err.println("Minimize: merge state " + s1.getName() + " into " + s0.getName());
	     }
	  }
	 if (nl != null) {
	    for (JflowModel.Node s1 : nl) {
	       statemap.put(s1,ns);
	     }
	  }
	 for (JflowModel.Node s1 : sl) {
	    if (s1 != s0) {
	       if (model_master.doDebug())
		  System.err.println("Merge transitions from " + s1.getName() + " into " + s0.getName());
	       for (JflowModel.Node mn : s1.getTransitions()) {
		  JflowModel.Node xs = statemap.get(mn);
		  s0.addTransition(xs);
		}
	     }
	  }
       }
    }

   if (newstates.size() == allst.size()) return;

   for (JflowModel.Node st : allst) {
      JflowModel.Node nst = statemap.get(st);
      if (nst == st) {
	 st.fixTransitions(statemap);
       }
      else if (model_master.doDebug()) {
	 System.err.println("Minimize: Merge state " + st.getName() + " into " + nst.getName());
       }
    }

   for (Map.Entry<JflowModel.Node,JflowModel.Node> ent : statemap.entrySet()) {
      JflowModel.Node fs = ent.getKey();
      JflowModel.Node ts = ent.getValue();
      if (fs != ts) ts.mergeLinesFromState(fs);
    }
}



private LinkedList<JflowModel.Node> getStateList(JflowModel.Node s,Set<JflowModel.Node> done,LinkedList<JflowModel.Node> l,
				   Map<JflowModel.Node,JflowModel.Node> statemap,JflowModel.Node s0)
{
   if (done.contains(s)) return l;
   done.add(s);

   if (l == null) l = new LinkedList<>();
   l.add(s);
   statemap.put(s,s0);

   for (JflowModel.Node jmn : s.getTransitions()) {
      JflowModel.Node ns = jmn;
      l = getStateList(ns,done,l,statemap,s0);
    }

   return l;
}




/********************************************************************************/
/*										*/
/*	State matching code for various types of simplification 		*/
/*										*/
/********************************************************************************/

private interface Matcher {

   boolean matchStates(JflowModel.Node s1,JflowModel.Node s2,Map<JflowModel.Node,JflowModel.Node> statemap);

}	// end of interface matcher




private static class FullMatcher implements Matcher {

   @Override public boolean matchStates(JflowModel.Node s1,JflowModel.Node s2,Map<JflowModel.Node,JflowModel.Node> statemap) {
      if (!s1.isCompatibleWith(s2)) return false;

      Set<JflowModel.Node> t1 = new HashSet<JflowModel.Node>();
      for (JflowModel.Node sx : s1.getTransitions()) {
	 t1.add(statemap.get(sx));
       }

      Set<JflowModel.Node> t2 = new HashSet<JflowModel.Node>();
      for (JflowModel.Node sx : s2.getTransitions()) {
	 t2.add(statemap.get(sx));
       }

      if (!t1.equals(t2)) return false;

      return true;
    }

}	// end of class FullMatch



private static class MinMatcher implements Matcher {

   @Override public boolean matchStates(JflowModel.Node s1,JflowModel.Node s2,Map<JflowModel.Node,JflowModel.Node> _statemap) {
      return s1.isCompatibleWith(s2);
    }

}	// end of class MinMatch



private static class SubMatcher implements Matcher {

   @Override public boolean matchStates(JflowModel.Node s1,JflowModel.Node s2,Map<JflowModel.Node,JflowModel.Node> statemap) {
      if (!s1.isCompatibleWith(s2)) return false;

      Set<JflowModel.Node> t1 = new HashSet<JflowModel.Node>();
      for (JflowModel.Node sx : s1.getTransitions()) {
	 t1.add(statemap.get(sx));
       }

      Set<JflowModel.Node> t2 = new HashSet<JflowModel.Node>();
      for (JflowModel.Node sx : s2.getTransitions()) {
	 t2.add(statemap.get(sx));
       }

      if (t1.containsAll(t2) || t2.containsAll(t1)) return true;

      return false;
    }

}	// end of SubMatch




}	// end of class ModelMinimizer




/* end of ModelMimimizer.java */
