/********************************************************************************/
/*										*/
/*		ModelThread.java						*/
/*										*/
/*	Class to contain information about thread behavior during checking	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/model/ModelThread.java,v 1.5 2015/11/20 15:09:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ModelThread.java,v $
 * Revision 1.5  2015/11/20 15:09:16  spr
 * Reformatting.
 *
 * Revision 1.4  2007-08-10 02:10:45  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.3  2006-12-01 03:22:49  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.2  2006/07/10 14:52:19  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:37  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.model;


import edu.brown.cs.ivy.jflow.*;

import java.util.*;



class ModelThread implements JflowModel.Threads, JflowConstants
{




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ModelMaster	model_master;
private ModelGenerator		event_model;
private Map<JflowMethod,ModelThreadState> thread_set;
private boolean 	for_global;

private static int		stack_depth = 2;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelThread(ModelMaster mm,ModelGenerator mdl)
{
   model_master = mm;
   event_model = mdl;
   thread_set = new HashMap<JflowMethod,ModelThreadState>();
   for_global = false;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public JflowModel.Node getStartNode(JflowMethod fm)
{
   return thread_set.get(fm);
}


void setGlobal()			{ for_global = true; }
void setEventModel(ModelGenerator g)	{ event_model = g; }



/********************************************************************************/
/*										*/
/*	Method to setup all potential threads					*/
/*										*/
/********************************************************************************/

void setupThreadModels()
{
   Set<JflowModel.Node> used = new HashSet<JflowModel.Node>();
   Set<JflowMethod> thrds = new HashSet<JflowMethod>();

   for (JflowModel.Method em : event_model.getStartMethods()) {
      JflowModel.Node nd = em.getStartNode();
      findThreads(nd,used,thrds);
    }

   for (JflowMethod fm : thrds) {
      ModelThreadState start = createThreadModel(fm);
      thread_set.put(fm,start);
    }
}



private void findThreads(JflowModel.Node nd,Set<JflowModel.Node> used,Set<JflowMethod> thrds)
{
   if (used.contains(nd)) return;
   used.add(nd);

   JflowMethod cm = nd.getCall();
   if (cm != null) {
      if (nd.isAsync()) thrds.add(cm);
      JflowModel.Method em = event_model.findMethod(cm);
      JflowModel.Node sn = em.getStartNode();
      findThreads(sn,used,thrds);
    }

   for (JflowModel.Node nn : nd.getTransitions()) {
      findThreads(nn,used,thrds);
    }
}




/********************************************************************************/
/*										*/
/*	Methods to build a single thread model					*/
/*										*/
/********************************************************************************/

ModelThreadState createThreadModel(JflowMethod fm)
{
   LinkedList<WorkItem> workq = new LinkedList<WorkItem>();
   Map<ThreadState,ModelThreadState> done = new HashMap<ThreadState,ModelThreadState>();

   if (model_master.doDebug()) System.err.println("Create thread model for " + fm);

   JflowModel.Method em = event_model.findMethod(fm);
   if (em == null) {
      System.err.println("JFLOW: Can't find method code for " + fm);
      return null;
    }

   JflowModel.Node en = em.getStartNode();

   ModelThreadState start = new ModelThreadState(model_master,en);
   ThreadState model = new ThreadState(en,null);

   workq.add(new WorkItem(start,model));

   while (!workq.isEmpty()) {
      WorkItem wi = workq.removeFirst();
      workOn(wi.getThreadState(),wi.getModelState(),workq,done);
    }

   simplify(start);

   return start;
}



private void workOn(ModelThreadState st,ThreadState mdl,LinkedList<WorkItem> workq,
		       Map<ThreadState,ModelThreadState> done)
{
   if (model_master.doDebug()) System.err.println("Thread work on " + st.getName() + " @ " + mdl.getModelNode().getName());

   ModelThreadState nst = done.get(mdl);
   if (nst != null) {
      st.addTransition(nst);
      return;
    }

   if (mdl.needsNewState()) {
      nst = new ModelThreadState(model_master,mdl.getModelNode());
      st.addTransition(nst);
      st = nst;
    }

   done.put(mdl,st);

   JflowMethod fm = mdl.getCall();
   if (fm != null) {
      JflowModel.Method em = event_model.findMethod(fm);
      Vector<ThreadState> v = mdl.getCallStack();
      if (v == null) v = new Vector<ThreadState>();
      else v = new Vector<ThreadState>(v);
      v.add(mdl);
      workq.addFirst(new WorkItem(st,new ThreadState(em.getStartNode(),v)));
    }
   else if (mdl.isReturn()) {
      Vector<ThreadState> v = mdl.getCallStack();
      if (v == null) return;
      // new v is never used -- why not just pick off the top element here
      v = new Vector<ThreadState>(v);
      int ln = v.size()-1;
      ThreadState top = v.elementAt(ln);
      v.removeElementAt(ln);
      handleTransitions(st,top,workq);
    }
   else {
      handleTransitions(st,mdl,workq);
    }
}



private void handleTransitions(ModelThreadState st,ThreadState mdl,LinkedList<WorkItem> workq)
{
   for (JflowModel.Node en : mdl.getModelNode().getTransitions()) {
      workq.addFirst(new WorkItem(st,new ThreadState(en,mdl.getCallStack())));
    }
}




/********************************************************************************/
/*										*/
/*	Methods to simplify the automata					*/
/*										*/
/********************************************************************************/

private void simplify(ModelThreadState s)
{
   Set<ModelThreadState> done = new HashSet<ModelThreadState>();

   if (model_master.doDebug()) System.err.println("Begin simplify with state " + s.getName());

   simplifyNode(s,done);

   ModelMinimizer.minimize(model_master,ModelMinimizer.MatchType.SUBSET,s);
}



private void simplifyNode(ModelThreadState s,Set<ModelThreadState> done)
{
   if (done.contains(s)) return;
   done.add(s);

   Set<JflowModel.Node> used = new HashSet<JflowModel.Node>();

   boolean chng = true;
   while (chng) {
      chng = false;
      for (JflowModel.Node jmn : s.getTransitions()) {
	 ModelThreadState nst = (ModelThreadState) jmn;
	 used.add(nst);
	 if (nst.isEmpty()) {
	    chng = true;
	    s.removeTransition(nst);
	    for (JflowModel.Node xst : nst.getTransitions()) {
	       if (!used.contains(xst)) {
		  s.addTransition(xst);
		}
	     }
	    break;
	  }
	simplifyNode(nst,done);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Class to hold an item to work on					*/
/*										*/
/********************************************************************************/

class WorkItem {

   private ModelThreadState thread_state;
   private ThreadState model_state;

   WorkItem(ModelThreadState ts,ThreadState ms) {
      thread_state = ts;
      model_state = ms;
    }

   ModelThreadState getThreadState()			{ return thread_state; }
   ThreadState getModelState()				{ return model_state; }

}	// end of subclass WorkItem




/********************************************************************************/
/*										*/
/*	Class to hold information for accessing a thread state			*/
/*										*/
/********************************************************************************/

class ThreadState {

   private JflowModel.Node model_node;
   private Vector<ThreadState> call_stack;

   ThreadState(JflowModel.Node nd,Vector<ThreadState> stk) {
      model_node = nd;
      call_stack = stk;
    }

   JflowModel.Node getModelNode()			{ return model_node; }
   Vector<ThreadState> getCallStack()			{ return call_stack; }

   @Override public int hashCode() {
      int hvl = model_node.hashCode();
      if (call_stack != null) {
	 int dep = call_stack.size();
	 if (dep > stack_depth) dep = stack_depth;
	 for (int i = 0; i < dep; ++i) {
	    hvl += call_stack.elementAt(i).hashCode();
	  }
       }
      return hvl;
    }

   @Override public boolean equals(Object o) {
      if (o == null || !(o instanceof ThreadState)) return false;
      ThreadState ms = (ThreadState) o;
      if (ms.model_node != model_node) return false;
      if (stack_depth > 0) {
	 if (call_stack == null && ms.call_stack == null) return true;
	 if (call_stack == null || ms.call_stack == null) return false;
	 if (call_stack.size() != ms.call_stack.size()) {
	    if (call_stack.size() < stack_depth || ms.call_stack.size() < stack_depth)
	       return false;
	  }
	 int dep = call_stack.size();
	 if (dep > stack_depth) dep = stack_depth;
	 for (int i = 0; i < dep; ++i) {
	    if (!call_stack.elementAt(i).equals(ms.call_stack.elementAt(i))) return false;
	  }
       }
      return true;
    }

   boolean needsNewState() {
      if (model_node.getCall() != null && (for_global || !model_node.isAsync())) return false;
      if (!model_node.isSimple() || model_node.isFinal()) return true;

      return false;
    }

   JflowMethod getCall() {
      if (!for_global && model_node.isAsync()) return null;
      return model_node.getCall();
    }

   boolean isReturn() {
      return model_node.isFinal();
    }

   boolean isExit() {
      return model_node.isExit();
    }

}	// end of subclass ThreadState




}	// end of class ModelThread



/* end of ModelThread.java */
