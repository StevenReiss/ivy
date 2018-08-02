/********************************************************************************/
/*										*/
/*		ModelGenerator.java						*/
/*										*/
/*	Routines to build event generation automata from code			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/model/ModelGenerator.java,v 1.6 2015/11/20 15:09:15 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ModelGenerator.java,v $
 * Revision 1.6  2015/11/20 15:09:15  spr
 * Reformatting.
 *
 * Revision 1.5  2009-09-17 01:57:28  spr
 * Code cleanup for eclipse.
 *
 * Revision 1.4  2007-08-10 02:10:45  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.3  2006-08-03 12:35:31  spr
 * Remove excess lines.
 *
 * Revision 1.2  2006/07/10 14:52:19  spr * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:37  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.model;

import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowEvent;
import edu.brown.cs.ivy.jflow.JflowMaster;
import edu.brown.cs.ivy.jflow.JflowMethod;
import edu.brown.cs.ivy.jflow.JflowModel;
import edu.brown.cs.ivy.jflow.JflowSource;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_ClassVector;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_Opcodes;

import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;



class ModelGenerator implements JflowModel.Main, JflowConstants, BT_Opcodes
{




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ModelMaster model_master;

private Set<JflowMethod> start_set;
private Map<JflowMethod,ModelMethod> complete_calls;

private ModelThread thread_model;

private Set<JflowMethod> empty_calls;
private Set<JflowMethod> event_calls;
private Set<JflowMethod> methods_todo;
private Map<JflowMethod,Boolean> return_set;

private Map<String,JflowMethod> callback_methods;

private static final int	CHET_BRANCH_SIZE = 5;
private static JflowSource return_source = null;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelGenerator(ModelMaster mm)
{
   model_master = mm;
   empty_calls = null;
   event_calls = null;
   complete_calls = null;
   methods_todo = null;
   start_set = null;
   return_set = null;
   thread_model = null;
   callback_methods = new HashMap<String,JflowMethod>();

   if (return_source == null) return_source = model_master.getProgramReturnSource();
}




/********************************************************************************/
/*										*/
/*	Processing routines							*/
/*										*/
/********************************************************************************/

boolean generateAutomata()
{
   empty_calls = new HashSet<JflowMethod>();
   event_calls = new HashSet<JflowMethod>();
   complete_calls = new LinkedHashMap<JflowMethod,ModelMethod>();
   methods_todo = new LinkedHashSet<JflowMethod>();
   return_set = new HashMap<JflowMethod,Boolean>();

   start_set = new HashSet<JflowMethod>();
   for (JflowMethod cm : model_master.getStartMethods()) {
      methods_todo.add(cm);
      start_set.add(cm);
    }

   while (!methods_todo.isEmpty()) {
      Iterator<JflowMethod> it = methods_todo.iterator();
      if (!it.hasNext()) break;
      JflowMethod cm = it.next();
      it.remove();
      if (!empty_calls.contains(cm) && !complete_calls.containsKey(cm)) {
	 if (!model_master.checkUseMethod(cm) || !model_master.isMethodAccessible(cm.getMethod()) ||
		cm.getMethod().isAbstract()) {
	    if (model_master.doDebug()) {
	       System.err.println("Ignore method: " + cm.getMethodName() + " " +
				      cm.getMethodSignature() + " " +
				      model_master.checkUseMethod(cm) + " " +
				      model_master.isMethodAccessible(cm.getMethod()));
	     }
	    empty_calls.add(cm);
	  }
	 else {
	    ModelBuilder bld = new ModelBuilder(model_master,this,cm);
	    complete_calls.put(cm,null);
	    ModelMethod cs = bld.createAutomata();
	    if (cs == null) empty_calls.add(cm);
	    else complete_calls.put(cm,cs);
	  }
       }
    }

   Set<JflowMethod> workq = new LinkedHashSet<JflowMethod>();
   for (Map.Entry<JflowMethod,ModelMethod> ent : complete_calls.entrySet()) {
      JflowMethod bm = ent.getKey();
      ModelMethod cs = ent.getValue();
      if (cs == null) continue;
      Set<JflowModel.Node> states = simplify(cs.getStartState());
      int ctr = 0;
      boolean hasrtn = false;
      for (JflowModel.Node st1 : states) {
	 if (st1.getEvent() != null) event_calls.add(bm);
	 else if (st1.getFieldSet() != null) event_calls.add(bm);
	 if (!hasrtn && st1.getReturnValue() != null) hasrtn = true;
	 JflowMethod cm = st1.getCall();
	 if (cm != null) {
	    if (event_calls.contains(cm)) event_calls.add(bm);
	    else {
	       ++ctr;
	       ModelMethod ncs = complete_calls.get(cm);
	       if (ncs != null) ncs.addUser(bm);
	       else System.err.println("Call to " + cm.getMethodName() + " not found");
	     }
	  }
       }
      return_set.put(bm,Boolean.valueOf(hasrtn));

      if (model_master.doDebug()) {
	 System.err.println("First pass method " + bm.getMethodName() + " " +
			       bm.getMethodSignature() + " " + ctr + " " +
			       states.size() + " " + event_calls.contains(bm));
       }
      if (ctr == 0) {
	 if (!event_calls.contains(bm)) empty_calls.add(bm);
       }
      else {
	 workq.add(bm);
       }
    }
   if (model_master.doDebug()) System.err.println("Work queue size = " + workq.size());
   if (event_calls.size() == 0) return false;

   Set<JflowMethod> returnused = null;
   boolean chng = true;
   while (chng) {
      chng = false;
      returnused = new HashSet<JflowMethod>();
      while (!workq.isEmpty()) {
	 Iterator<JflowMethod> it = workq.iterator();
	 if (!it.hasNext()) break;
	 JflowMethod bm = it.next();
	 it.remove();
	 ModelMethod cs = complete_calls.get(bm);
	 boolean chkit = !event_calls.contains(bm);

	 if (cs == null || empty_calls.contains(bm)) continue;

	 int ctr = 0;
	 Set<JflowModel.Node> states = simplify(cs.getStartState());
	 boolean mchng = false;
	 boolean hasrtn = false;
	 for (JflowModel.Node st1 : states) {
	    if (!hasrtn && st1.getReturnValue() != null) hasrtn = true;
	    JflowMethod cm = st1.getCall();
	    if (cm != null) {
	       if (st1.getUseReturn()) returnused.add(cm);
	       if (!event_calls.contains(cm)) {
		  ++ctr;
		}
	       else if (chkit) {
		  if (model_master.doDebug()) {
		     System.err.println("Method required: " + bm.getMethodName() + " " +
					   bm.getMethodSignature() + " :: " + cm.getMethodName() +
					   " " + cm.getMethodSignature());
		   }
		  event_calls.add(bm);
		  chkit = false;
		  mchng = true;
		}
	     }
	  }

	 if (return_set.get(bm) != Boolean.FALSE) {
	    return_set.put(bm,Boolean.valueOf(hasrtn));
	    if (!hasrtn) mchng = true;
	  }

	 if (ctr == 0 && !event_calls.contains(bm)) {
	    empty_calls.add(bm);
	    mchng = true;
	  }

	 if (model_master.doDebug()) System.err.println("Consider method " + bm.getMethodName() + " " + bm.getMethodSignature() + " " + ctr + " " + states.size() + " " + mchng);

	 if (mchng) {
	    for (Iterator<?> it1 = cs.getUsers(); it1.hasNext(); ) {
	       JflowMethod cm = (JflowMethod) it1.next();
	       if (model_master.doDebug()) System.err.println("\tQueue " + cm.getMethodName() + " " + cm.getMethodSignature());
	       workq.add(cm);
	     }
	  }
       }

      for (Map.Entry<JflowMethod,ModelMethod> ent : complete_calls.entrySet()) {
	 JflowMethod bm = ent.getKey();
	 ModelMethod cs = ent.getValue();
	 if (cs != null && !empty_calls.contains(bm) && !event_calls.contains(bm)) {
	    empty_calls.add(bm);
	    chng = true;
	    for (Iterator<JflowMethod> it1 = cs.getUsers(); it1.hasNext(); ) {
	       JflowMethod cm = it1.next();
	       if (model_master.doDebug()) System.err.println("\tQueue " + cm.getMethodName() + " " + cm.getMethodSignature());
	       workq.add(cm);
	     }
	  }
       }
    }

   for (JflowMethod cm : empty_calls) {
      complete_calls.remove(cm);
    }

   chng = true;
   boolean needsync = true;
   while (chng) {
      chng = false;
      boolean nextsync = false;
      for (ModelMethod em : complete_calls.values()) {
	 Set<JflowModel.Node> sts = simplify(em.getStartState());
	 if (!needsync) {
	    for (JflowModel.Node nms : sts) {
	       ModelState ms = (ModelState) nms;
	       if (ms.getWaitType() != ModelWaitType.NONE) {
		  ms.clearCall();
		  chng = true;
		}
	     }
	  }
	 else if (!nextsync) {
	    for (JflowModel.Node ms : sts) {
	       if (ms.getCall() != null && ms.isAsync()) nextsync = true;
	     }
	  }
	 if (return_set.get(em.getMethod()) != Boolean.FALSE && returnused != null && 
		!returnused.contains(em.getMethod())) {
	    for (JflowModel.Node nms : sts) {
	       ModelState ms = (ModelState) nms;
	       if (ms.getReturnValue() != null) {
		  ms.clearCall();
		  chng = true;
		}
	     }
	    return_set.put(em.getMethod(),Boolean.FALSE);
	  }
       }
      if (nextsync != needsync) chng = true;
      needsync = nextsync;
    }

   for (ModelMethod em : complete_calls.values()) {
      ModelMinimizer.minimize(model_master,em.getStartState());
    }

   ModelSynch msynch = new ModelSynch(complete_calls.values());
   msynch.establishPartitions();

   if (model_master.doDebug()) {
      IvyXmlWriter nxw = new IvyXmlWriter(new OutputStreamWriter(System.err));
      nxw.begin("DEBUG");
      outputEvents(nxw);
      outputProgram(nxw);
      nxw.end();
      nxw.flush();
    }

   boolean retfg = true;

   Collection<JflowEvent> c = model_master.getRequiredEvents();
   if (c != null && !c.isEmpty()) {
      Set<JflowEvent> evts = new HashSet<JflowEvent>();
      for (ModelMethod cs : complete_calls.values()) {
	 Set<JflowModel.Node> states = simplify(cs.getStartState());
	 for (JflowModel.Node ms : states) {
	    JflowEvent ev = ms.getEvent();
	    if (ev != null) evts.add(ev);
	  }
       }
      for (JflowEvent ev : c) {
	 if (!evts.contains(ev)) {
	    System.err.println("JFLOW: Required event " + ev + " not found");
	    retfg = false;
	    break;
	  }
       }
    }

   empty_calls = null;
   event_calls = null;
   methods_todo = null;
   return_set = null;

   return retfg;
}



@Override public void outputEvents(IvyXmlWriter xw)
{
   xw.begin("EVENTS");
   for (ModelMethod cs : complete_calls.values()) {
      Set<JflowModel.Node> states = simplify(cs.getStartState());
      ModelState [] starr = states.toArray(new ModelState[states.size()]);
      Arrays.sort(starr);
      for (int i = 0; i < starr.length; ++i) {
	 starr[i].outputEvent(xw);
       }
    }
   xw.end();
}



@Override public void outputProgram(IvyXmlWriter xw)
{
   int nmthd = 0;
   int nstate = 0;

   xw.begin("PROGRAM");
   for (Map.Entry<JflowMethod,ModelMethod> ent : complete_calls.entrySet()) {
      JflowMethod cm = ent.getKey();
      ModelMethod cs = ent.getValue();

      ++nmthd;
      xw.begin("METHODFSA");
      xw.field("METHOD",cm.getMethodName());
      xw.field("SIGNATURE",cm.getMethodSignature());
      xw.field("ENTRY",cs.getStartState().getName());
      xw.field("EXIT",cs.getEndState().getName());
      xw.field("ID",cs.getStartState().getName());
      if (start_set.contains(cm)) xw.field("START",true);

      Set<JflowModel.Node> states = simplify(cs.getStartState());
      ModelState [] starr = states.toArray(new ModelState[states.size()]);
      Arrays.sort(starr);
      nstate += starr.length;

      xw.begin("STATES");
      for (int i = 0; i < starr.length; ++i) {
	 starr[i].outputXml(xw,this);
       }
      xw.end();
      xw.end();
    }

   xw.begin("PGMSTATS");
   xw.field("METHODS",nmthd);
   xw.field("STATES",nstate);
   xw.end();

   xw.end();
}



/********************************************************************************/
/*										*/
/*	Methods to output a single global model 				*/
/*										*/
/********************************************************************************/

@Override public void outputGlobalProgram(IvyXmlWriter xw,Collection<JflowModel.Main> merges,
				   JflowModel.Editor editor)
{
   xw.begin("GLOBAL");
   Set<JflowModel.Node> states = createGlobalModel(merges,editor);
   ModelState [] starr = states.toArray(new ModelState[states.size()]);
   Arrays.sort(starr);

   xw.begin("STATES");
   for (int i = 0; i < starr.length; ++i) {
      starr[i].outputXml(xw,this);
    }
   xw.end();

   xw.end("GLOBAL");
}




private Set<JflowModel.Node> createGlobalModel(Collection<JflowModel.Main> merges,
						  JflowModel.Editor editor)
{
   ModelThread mth = new ModelThread(model_master,this);
   mth.setGlobal();

   ModelState start = ModelState.createSimpleState(null,0);
   ModelState exit = ModelState.createExitState(null,0);

   addGlobalModel(mth,start,exit);

   if (merges != null) {
      for (JflowModel.Main jm : merges) {
	 if (jm != this) {
	    ModelGenerator mg = (ModelGenerator) jm;
	    mth.setEventModel(mg);
	    mg.addGlobalModel(mth,start,exit);
	  }
       }
    }

   if (editor != null) {
      ModelMinimizer.minimize(model_master,start);
      simplify(start);
      editor.editModel(start);
    }

   ModelMinimizer.stateMinimize(model_master,start);

   return simplify(start);
}



private void addGlobalModel(ModelThread mth,ModelState start,ModelState exit)
{
   for (JflowMethod sm : start_set) {
      ModelThreadState tm = mth.createThreadModel(sm);
      if (tm != null) {
	 Map<ModelThreadState,ModelState> smap = new HashMap<ModelThreadState,ModelState>();
	 ModelState ms = convertThreadState(tm,smap,exit);
	 if (model_master.doDebug()) {
	    System.err.println("Add start transition from " + start.getName() + " => " + ms.getName());
	  }
	 start.addTransition(ms);
       }
    }
}




private ModelState convertThreadState(ModelThreadState ts,Map<ModelThreadState,ModelState> smap,
				   ModelState exit)
{
   if (model_master.doDebug()) System.err.println("Begin convert of " + ts.getName());

   ModelState xs = smap.get(ts);
   if (xs != null) return xs;

   ModelState tbs = ts.getBaseNode();
   if (tbs.getCall() != null) {
      System.err.println("JFLOW: Call node found in collapsed graph");
      System.err.println("\tNODE: " + tbs.getName() + " " + tbs.getCall());
      xs = ModelState.createSimpleState(null,0);
    }
   else if (tbs.getEvent() != null) {
      xs = ModelState.createEventState(tbs.getMethod(),tbs.getLineNumber(),tbs.getEvent());
    }
   else if (tbs.getWaitType() != ModelWaitType.NONE) {
      xs = ModelState.createSimpleState(tbs.getMethod(),tbs.getLineNumber());
    }
   else if (tbs.getCondition() != null) {
      JflowModel.Field mfld = tbs.getCondition();
      xs = ModelState.createCondState(tbs.getMethod(),tbs.getLineNumber(),mfld.getConditionType(),
					mfld.getFieldSource());
    }
   else if (tbs.getFieldSet() != null) {
      JflowModel.Field mfld = tbs.getFieldSet();
      xs = ModelState.createFieldState(tbs.getMethod(),tbs.getLineNumber(),mfld.getFieldSource(),
					  mfld.getFieldValue());
    }
   else {
      xs = ModelState.createSimpleState(tbs.getMethod(),tbs.getLineNumber());
    }

   if (model_master.doDebug()) {
      System.err.println("Convert state " + ts.getName() + " => " + xs.getName());
    }

   smap.put(ts,xs);

   if (xs != exit) {
      if (ts.isFinal() || ts.isExit()) {
	 if (model_master.doDebug()) {
	    System.err.println("Add exit transition from " + xs.getName() + " to " + exit.getName());
	  }
	 xs.addTransition(exit);
       }
      for (JflowModel.Node nxt : ts.getTransitions()) {
	 ModelThreadState nxts = (ModelThreadState) nxt;
	 ModelState newts = convertThreadState(nxts,smap,exit);
	 if (model_master.doDebug()) {
	    System.err.println("Add global transition from " + xs.getName() + " to " +
				  newts.getName());
	  }
	 xs.addTransition(newts);
       }
    }
   else {
      if (model_master.doDebug()) System.err.println("Ignore transitions for " + ts.getName());
    }

   return xs;
}




/********************************************************************************/
/*										*/
/*	JflowModel.Main methods 						*/
/*										*/
/********************************************************************************/

@Override public Iterable<JflowModel.Method> getStartMethods()
{
   Vector<JflowModel.Method> v = new Vector<JflowModel.Method>();

   for (JflowMethod cm : model_master.getStartMethods()) {
      ModelMethod cs = complete_calls.get(cm);
      if (cs != null) v.add(cs);
    }

   return v;
}


@Override public JflowModel.Method findMethod(JflowMethod bm)
{
   ModelMethod cs = complete_calls.get(bm);

   return cs;
}



@Override public JflowModel.Node getThreadStartNode(JflowMethod jm)
{
   if (thread_model == null) return null;
   return thread_model.getStartNode(jm);
}



@Override public JflowModel.Threads createThreadModel()
{
   thread_model = new ModelThread(model_master,this);

   thread_model.setupThreadModels();

   return thread_model;
}



@Override public JflowModel getModel()			{ return model_master; }

@Override public JflowMaster getFlowMaster()		{ return model_master.getFlowMaster(); }




/********************************************************************************/
/*										*/
/*	Builder support methods 						*/
/*										*/
/********************************************************************************/

boolean isStartMethod(JflowMethod fm)	{ return start_set.contains(fm); }



boolean isMethodUsed(JflowMethod fm)	{ return model_master.checkUseMethod(fm); }

boolean isFieldSourceRelevant(JflowSource cs)
{
   return model_master.isFieldSourceRelevant(cs);
}


boolean isMethodIgnored(JflowMethod fm)
{
   if (empty_calls.contains(fm)) return true;

   if (fm.hasSpecial()) return false;

   if (fm.getMethod().isAbstract()) {
      if (complete_calls.get(fm) != null) return false;
      return true;
    }

   return false;
}


void addMethodTodo(JflowMethod fm)
{
   if (!complete_calls.containsKey(fm) && !methods_todo.contains(fm))
      methods_todo.add(fm);
}



JflowMethod buildMetaCall(JflowMethod frm,BT_Ins at,BT_Method tgt)
{
   JflowMethod cfm = model_master.createMetaMethod(tgt);
   ModelMethod state = createCallAutomata(cfm,frm.getAllCalls(at));

   if (state == null) return null;

   complete_calls.put(cfm,state);

   return cfm;
}



boolean doSynch()
{
   return model_master.doSynch();
}


boolean doWaits()
{
   return model_master.doWaits();
}



boolean doUndeclaredExceptions()
{
   return model_master.doUndeclaredExceptions();
}



boolean doIgnoreExceptions()
{
   return model_master.doIgnoreExceptions();
}



/********************************************************************************/
/*										*/
/*	Methods to create dummy automata for multiple calls			*/
/*										*/
/********************************************************************************/

private ModelMethod createCallAutomata(JflowMethod cfm,Iterable<JflowMethod> itms)
{
   ModelState st0 = ModelState.createSimpleState(cfm,0);
   ModelState st1 = ModelState.createSimpleState(cfm,0);
   st1.setFinal();
   int ctr = 0;

   for (JflowMethod fm : itms) {
      if (!empty_calls.contains(fm)) {
	 ++ctr;
	 ModelState st2 = ModelState.createCallState(cfm,0,fm,false,null);
	 st0.addTransition(st2);
	 if (model_master.doDebug()) System.err.println("Transition (Calla) " + st0.getName() + " -> " + st2.getName());
	 st2.addTransition(st1);
	 if (model_master.doDebug()) System.err.println("Transition (Callb) " + st2.getName() + " -> " + st1.getName());
	 if (!complete_calls.containsKey(fm) && !methods_todo.contains(fm)) methods_todo.add(fm);
       }
    }

   if (ctr < CHET_BRANCH_SIZE) return null;

   return new ModelMethod(cfm,st0,st1);
}




/********************************************************************************/
/*										*/
/*	Simplification methods							*/
/*										*/
/********************************************************************************/

Set<JflowModel.Node> simplify(ModelState start)
{
   Set<JflowModel.Node> states = new HashSet<JflowModel.Node>();

   updateState(start,states,new HashMap<ModelState,Vector<Set<JflowModel.Node>>>());

   if (removeExtraReturns(states)) {
      states = new HashSet<JflowModel.Node>();
      updateState(start,states,new HashMap<ModelState,Vector<Set<JflowModel.Node>>>());
    }

   return states;
}



private void updateState(ModelState s,Set<JflowModel.Node> done,Map<ModelState,Vector<Set<JflowModel.Node>>> smap)
{
   if (done.contains(s)) return;
   done.add(s);

   boolean rchkok = false;
   JflowMethod cm = s.getCall();
   if (cm != null) {
      if (!cm.getMethod().isVoidMethod() && s.getUseReturn() &&
	     (return_set == null || return_set.get(cm) != Boolean.FALSE)) {
	 rchkok = true;
       }
      else {
	 s.setUseReturn(false);
       }
    }

   Set<JflowModel.Node> nsts = null;
   boolean news = false;
   for (JflowModel.Node nns : s.getTransitions()) {
      ModelState ns = (ModelState) nns;
      Vector<Set<JflowModel.Node>> v = smap.get(ns);
      Set<JflowModel.Node> rs = null;
      Set<JflowModel.Node> mrgset = null;
      if (v != null) {
	 rs = v.elementAt(0);
	 mrgset = v.elementAt(1);
       }
      else {
	 mrgset = new HashSet<JflowModel.Node>();
	 rs = computeFollow(ns,mrgset,rchkok);
	 Vector<Set<JflowModel.Node>> vr = new Vector<Set<JflowModel.Node>>();
	 vr.add(rs);
	 vr.add(mrgset);
	 smap.put(ns,vr);
       }
      if (mrgset != null) {
	 for (JflowModel.Node mms : mrgset) {
	    if (rs == null || !rs.contains(mms)) {
	       s.mergeLinesFromState(mms);
	     }
	  }
       }
      if (nsts == null) nsts = rs;
      else if (news) nsts.addAll(rs);
      else {
	 nsts = new HashSet<JflowModel.Node>(nsts);
	 nsts.addAll(rs);
	 news = true;
       }
    }

   if (cm != null && s.getUseReturn()) {
      boolean usertn = false;
      if (nsts != null) {
	 for (JflowModel.Node ns : nsts) {
	    JflowModel.Field f = ns.getCondition();
	    if (f != null && f.getFieldSource() == return_source) usertn = true;
	  }
       }
      if (!usertn) s.setUseReturn(false);
    }

   nsts = mergeReturns(s,nsts);

   s.setTransitions(nsts);

   if (nsts == null) return;

   for (JflowModel.Node ns : nsts) {
      updateState((ModelState) ns,done,smap);
    }

   mergeConditions(s);
}



private Set<JflowModel.Node> computeFollow(ModelState s,Set<JflowModel.Node> done,boolean rchkok)
{
   Set<JflowModel.Node> rslt = new HashSet<JflowModel.Node>();

   if (done == null) done = new HashSet<JflowModel.Node>();
   else done.clear();

   computeFollow1(s,rslt,done,rchkok);

   return rslt;
}



private void computeFollow1(ModelState s,Set<JflowModel.Node> rslt,Set<JflowModel.Node> done,boolean rchkok)
{
   if (done.contains(s)) return;
   done.add(s);

   if (empty_calls != null && s.getCall() != null && empty_calls.contains(s.getCall())) {
      s.clearCall();
    }

   if (s.hasNoTransitions()) {
      if (s.getCondition() != null) s.clearCall();
      else if (s.getFieldSet() != null) s.clearCall();
    }

   if (!rchkok) {
      JflowModel.Field f = s.getCondition();
      if (f != null && f.getFieldSource() == return_source) s.clearCall();
    }

   if (s.getWaitType() == ModelWaitType.SYNCH) {
      Collection<JflowSource> sync = s.getWaitSet();
      HashSet<JflowModel.Node> ndone = new HashSet<JflowModel.Node>();
      HashSet<JflowModel.Node> nrslt = new HashSet<JflowModel.Node>();
      for (JflowModel.Node nns : s.getTransitions()) {
	 ModelState ns = (ModelState) nns;
	 computeFollow1(ns,nrslt,ndone,false);
       }
      boolean ign = true;
      for (JflowModel.Node ns : nrslt) {
	 if (ns.getEvent() == null && ns.getCall() == null &&
		ns.getFieldSet() == null && ns.getCondition() == null &&
		ns.getWaitType() == ModelWaitType.END_SYNCH &&
		ns.getWaitSet().equals(sync)) ;
	 else {
	    ign = false;
	    break;
	  }
       }
      if (ign) {
	 for (JflowModel.Node ns : nrslt) {
	    for (JflowModel.Node nss : ns.getTransitions()) {
	       ModelState ss = (ModelState) nss;
	       computeFollow1(ss,rslt,done,false);
	     }
	  }
	 return;
       }
    }

   if (!s.isSimple() || s.isFinal()) {
      rslt.add(s);
      return;
    }

   for (JflowModel.Node nss : s.getTransitions()) {
      ModelState ns = (ModelState) nss;
      computeFollow1(ns,rslt,done,false);
    }
}




private Set<JflowModel.Node> mergeReturns(ModelState st,Set<JflowModel.Node> s)
{
   if (s == null) return s;

   ModelState rs = null;
   ModelState fs = null;
   Set<JflowModel.Node> rtns = null;

   for (JflowModel.Node nms : s) {
      ModelState ms = (ModelState) nms;
      if (ms.isReturn()) {
	 if (rtns == null) rtns = new HashSet<JflowModel.Node>(4);
	 rtns.add(ms);
	 if (rs == null) rs = ms;
	 else rs = rs.mergeReturn(ms);
       }
      else if (ms.isFinal()) fs = ms;
    }

   if (rtns == null) return s;
   else if (fs == null && rtns.size() == 1) return s;
   else if (fs != null) {			// final state is there, remove returns
      s = new HashSet<JflowModel.Node>(s);
      s.removeAll(rtns);
    }
   else {
      s = new HashSet<JflowModel.Node>(s);
      s.removeAll(rtns);
      if (rs != null && rs.isSimple()) rs = rs.getSingleTransition();
      if (rs != null) s.add(rs);
    }

   if (rtns != null) {
      for (JflowModel.Node mms : rtns) {
	 if (!s.contains(mms)) st.mergeLinesFromState(mms);
       }
    }

   return s;
}




private void mergeConditions(ModelState s)
{
   JflowSource src = null;
   Set<JflowModel.Node> nxt = null;
   int ct = 0;

   for (JflowModel.Node nss : s.getTransitions()) {
      ModelState ns = (ModelState) nss;
      JflowModel.Field fc = ns.getCondition();
      ++ct;
      if (fc == null) return;
      if (src == null) {
	 src = fc.getFieldSource();
	 nxt = ns.getTransitionSet();
	 if (nxt == null) return;
       }
      else if (src != fc.getFieldSource()) return;
      else if (nxt != null && !nxt.equals(ns.getTransitionSet())) return;
    }

   if (nxt == null || ct < 2) return;

   for (JflowModel.Node nss : s.getTransitions()) {
      ModelState ns = (ModelState) nss;
      s.mergeLinesFromState(ns);
    }

   s.setTransitions(nxt);
}




private boolean removeExtraReturns(Set<JflowModel.Node> s)
{
   if (s == null) return false;

   boolean fnl = false;
   boolean evt = false;
   Set<JflowModel.Node> rtns = null;
   for (JflowModel.Node nms : s) {
      ModelState ms = (ModelState) nms;
      if (ms.isReturn()) {
	 if (rtns == null) rtns = new HashSet<JflowModel.Node>(4);
	 rtns.add(ms);
       }
      else {
	 if (ms.getEvent() != null) evt = true;
	 for (JflowModel.Node nss : ms.getTransitions()) {
	    ModelState ns = (ModelState) nss;
	    if (ns.isFinal()) fnl = true;
	  }
       }
    }

   if (fnl && rtns != null && !evt) {
      for (JflowModel.Node nms : rtns) {
	 ModelState ms = (ModelState) nms;
	 ms.clearCall();
       }
      return true;
    }

   return false;
}




/********************************************************************************/
/*										*/
/*	Methods to build automaton for callbacks				*/
/*										*/
/********************************************************************************/

JflowMethod buildCallback(String cbid)
{
   Iterable<JflowMethod> it = model_master.getCallbacks(cbid);
   if (it == null) return null;

   JflowMethod m = callback_methods.get(cbid);
   if (m != null) return m;

   try {
      BT_Class dcls = BT_Class.forName("edu.brown.cs.ivy.jflow.JflowDummy");
      BT_Method bm = null;
      try {
	 bm = dcls.findMethod("callbackHandler_" + cbid,"()");
       }
      catch (NoSuchMethodError e) {
	 BT_ClassVector args = new BT_ClassVector();
	 BT_MethodSignature bsg = BT_MethodSignature.create(BT_Class.getVoid(),args);
	 bm = dcls.addStubMethod("callbackHandler_" + cbid,bsg);
       }
      m = model_master.createMetaMethod(bm);
      callback_methods.put(cbid,m);
    }
   catch (Exception e) {
      System.err.println("Problem finding callback method: " + e);
      return null;
    }

   if (model_master.doDebug()) {
      System.err.println("Create CALLBACK " + cbid + " " + m);
    }

   ModelState enter = ModelState.createSimpleState(m,0);
   ModelState exit = ModelState.createSimpleState(m,0);
   ModelState s1 = ModelState.createSimpleState(m,0);
   ModelState s2 = ModelState.createSimpleState(m,0);
   enter.addTransition(s1);
   s2.addTransition(s2);
   s2.addTransition(s1);

   for (JflowMethod mi : it) {
      if (!isMethodIgnored(mi)) {
	 ModelState sc = ModelState.createCallState(m,0,mi,false,null);
	 s1.addTransition(sc);
	 sc.addTransition(s2);
	 addMethodTodo(mi);
       }
    }

   simplify(enter);

   ModelMethod mm = new ModelMethod(m,enter,exit);
   complete_calls.put(m,mm);

   return m;
}




}	// end of class ModelGenerator



/* end of ModelGenerator.java */
