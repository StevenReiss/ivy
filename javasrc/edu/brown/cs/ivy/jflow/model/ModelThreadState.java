/********************************************************************************/
/*										*/
/*		JflowModel.Node.java					       */
/*										*/
/*	Class to contain information about a state in thread processing 	*/
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


package edu.brown.cs.ivy.jflow.model;


import edu.brown.cs.ivy.jflow.JflowEvent;
import edu.brown.cs.ivy.jflow.JflowMethod;
import edu.brown.cs.ivy.jflow.JflowModel;
import edu.brown.cs.ivy.jflow.JflowSource;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



class ModelThreadState implements JflowModel.Node {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private ModelMaster model_master;
private JflowModel.Node base_node;
private Set<JflowModel.Node> next_nodes;
private int state_id;

private static int state_counter = 0;
private static JflowSource return_source = null;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelThreadState(ModelMaster mm,JflowModel.Node nd) {
   model_master = mm;
   base_node = nd;
   next_nodes = new HashSet<JflowModel.Node>();
   state_id = ++state_counter;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName() {
   return "T_" + state_id + "_" + base_node.getName();
}

@Override public JflowMethod getCall()			{ return base_node.getCall(); }
@Override public JflowEvent getEvent()			{ return base_node.getEvent(); }
@Override public JflowModel.Field getCondition()		{ return base_node.getCondition(); }
@Override public JflowModel.Field getFieldSet()		{ return base_node.getFieldSet(); }
@Override public ModelWaitType getWaitType()		{ return base_node.getWaitType(); }
@Override public Collection<JflowSource> getWaitSet()	{ return base_node.getWaitSet(); }
@Override public boolean isAsync()			{ return base_node.isAsync(); }
@Override public Object getReturnValue()			{ return base_node.getReturnValue(); }
@Override public boolean isCallOnce()			{ return base_node.isCallOnce(); }

@Override public void outputEvent(IvyXmlWriter xw)	{ base_node.outputEvent(xw); }
@Override public void outputLocation(IvyXmlWriter xw)	{ base_node.outputLocation(xw); }

@Override public Iterable<JflowModel.Node> getTransitions() { return next_nodes; }
@Override public boolean isFinal()			{ return next_nodes.isEmpty(); }
@Override public boolean isSimple()			{ return base_node.isSimple(); }
@Override public boolean getUseReturn()			{ return base_node.getUseReturn(); }
@Override public boolean isExit() 			{ return base_node.isExit(); }

ModelState getBaseNode()			{ return (ModelState) base_node; }




/********************************************************************************/
/*										*/
/*	Transition maintenance methods						*/
/*										*/
/********************************************************************************/

@Override public void addTransition(JflowModel.Node ts) {
   if (next_nodes.add(ts)) {
      if (model_master.doDebug()) System.err.println("Add thread transition: " + getName() + " => " + ts.getName());
    }
}

@Override public void removeTransition(JflowModel.Node ts) {
   next_nodes.remove(ts);
   if (model_master.doDebug()) System.err.println("Remove thread transition: " + getName() + " => " + ts.getName());
}


@Override public void fixTransitions(Map<JflowModel.Node,JflowModel.Node> statemap) {
   Set<JflowModel.Node> nn = new HashSet<JflowModel.Node>();
   for (JflowModel.Node s : next_nodes) {
      nn.add(statemap.get(s));
    }
   next_nodes = nn;
}



/********************************************************************************/
/*										*/
/*	Checking methods							*/
/*										*/
/********************************************************************************/

boolean isEmpty() {
   if (!base_node.isSimple()) {
      if (base_node.getCall() != null && !base_node.isAsync()) return true;
      if (base_node.getReturnValue() != null) {
	 if (return_source == null) return_source = model_master.getProgramReturnSource();
	 Iterator<JflowModel.Node> it = next_nodes.iterator();
	 if (it.hasNext() && next_nodes.size() == 1) {
	    ModelThreadState rs = (ModelThreadState) it.next();
	    boolean ok = rs.isSimple() && !rs.isFinal();
	    if (ok) {
	       for (JflowModel.Node mn : rs.getTransitions()) {
		  ModelThreadState nrs = (ModelThreadState) mn;
		  JflowModel.Field f = nrs.getCondition();
		  if (f == null || f.getFieldSource() != return_source) {
		     ok = false;
		     break;
		   }
		}
	     }
	    if (ok) return false;
	  }
	 return true;
       }
      return false;
    }
   if (next_nodes.isEmpty()) return false;

   return true;
}



@Override public boolean isCompatibleWith(JflowModel.Node ts) {
   if (isFinal() != ts.isFinal()) return false;
   if (isAsync() != ts.isAsync()) return false;
   if (getCall() != ts.getCall()) return false;
   if (getEvent() != ts.getEvent()) return false;
   if (getCondition() != null) {
      if (!getCondition().equals(ts.getCondition())) return false;
    }
   else if (ts.getCondition() != null) return false;
   if (getFieldSet() != null) {
      if (!getFieldSet().equals(ts.getFieldSet())) return false;
    }
   else if (ts.getFieldSet() != null) return false;
   if (getWaitType() != ts.getWaitType()) return false;
   if (getWaitType() != ModelWaitType.NONE) {
      if (!getWaitSet().equals(ts.getWaitSet())) return false;
    }
   if (getReturnValue() != null) {
      if (!getReturnValue().equals(ts.getReturnValue())) return false;
    }
   else if (ts.getReturnValue() != null) return false;

   return true;
}



/********************************************************************************/
/*										*/
/*	Merging methods 							*/
/*										*/
/********************************************************************************/

@Override public void mergeLinesFromState(JflowModel.Node n)			{ }




}	// end of class ModelThreadState




/* end of ModelThreadState.java */
