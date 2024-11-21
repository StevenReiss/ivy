/********************************************************************************/
/*										*/
/*		FlowQueue.java							*/
/*										*/
/*	Work queue for holding information for a given method			*/
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


package edu.brown.cs.ivy.jflow.flow;

import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_Opcodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;



class FlowQueue implements JflowConstants, BT_Opcodes
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private LinkedList<Integer> work_list;
private MethodBase for_method;
private BT_InsVector ins_vector;
private Map<BT_Ins,StateBase> state_map;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

FlowQueue(MethodBase cm,BT_InsVector iv) {
   for_method = cm;
   ins_vector = iv;
   work_list = new LinkedList<>();
   state_map = new HashMap<>();
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

MethodBase getMethod()			{ return for_method; }

boolean isEmpty()			{ return work_list.isEmpty(); }


StateBase getState(int ino)		 { return getState(ins_vector.elementAt(ino)); }

StateBase getState(BT_Ins ins)	 { return state_map.get(ins); }


BT_Ins getInstruction(int ino)		{ return ins_vector.elementAt(ino); }
int getIndex(BT_Ins ins)		{ return ins_vector.indexOf(ins); }
int getNumInstructions()		{ return ins_vector.size(); }




/********************************************************************************/
/*										*/
/*	Methods to manage the queue						*/
/*										*/
/********************************************************************************/

int getNext()
{
   Integer id = work_list.removeFirst();
   return id.intValue();
}




void mergeState(StateBase st,BT_Ins ins)
{
   mergeState(st,ins_vector.indexOf(ins));
}




void mergeState(StateBase st,int ino)
{
   BT_Ins ins = ins_vector.elementAt(ino);
   StateBase ost = state_map.get(ins);
   if (ost == null) ost = st.cloneState();
   else {
      ost = ost.mergeWithState(st);
      if (ost == null) return;			     // no change
    }
   state_map.put(ins,ost);
   work_list.addFirst(Integer.valueOf(ino));
}




void lookAt(BT_Ins ins) 		{ lookAt(ins_vector.indexOf(ins)); }

void lookAt(int ino) {
   if (ino >= 0 && getState(ino) != null) work_list.addLast(Integer.valueOf(ino));
}



/********************************************************************************/
/*										*/
/*	Incremental update methods						*/
/*										*/
/********************************************************************************/

void handleUpdates(Collection<SourceBase> oldsrcs,
		      Map<SourceSet,SourceSet> srcupdates,
		      Map<JflowValue,JflowValue> valupdates)
{
   List<StateBase> workqueue = new ArrayList<StateBase>();
   Set<StateBase> doneq = new HashSet<StateBase>();
   workqueue.addAll(state_map.values());

   while (!workqueue.isEmpty()) {
      int ln = workqueue.size();
      StateBase sb = workqueue.remove(ln-1);
      if (!doneq.contains(sb)) {
	 doneq.add(sb);
	 sb.handleUpdates(oldsrcs,srcupdates,valupdates,workqueue);
       }
    }
}



}	// end of class FlowQueue



/* end of FlowQueue.java */
