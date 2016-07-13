/********************************************************************************/
/*										*/
/*		StateBase.java							*/
/*										*/
/*	Basic implementation of a state while doing code analysis		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/StateBase.java,v 1.6 2007-02-27 18:53:29 spr Exp $ */


/*********************************************************************************
 *
 * $Log: StateBase.java,v $
 * Revision 1.6  2007-02-27 18:53:29  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.5  2007-01-03 14:04:59  spr
 * Fix imports
 *
 * Revision 1.4  2007-01-03 03:24:18  spr
 * Modifications to handle incremental update.
 *
 * Revision 1.3  2006-08-30 00:43:51  spr
 * Fix bugs with mutable sources.
 *
 * Revision 1.2  2006/07/10 14:52:18  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:34  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.flow;


import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;

import java.util.*;



class StateBase implements JflowConstants
{




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ValueBase []	local_values;
private Stack<ValueBase> stack_values;
private Map<BT_Field,ValueBase> field_map;
private Map<BT_Field,ValueBase> base_fields;
private Stack<Integer>	return_stack;
private List<StateBase> state_set;
private StateCtor	current_inits;
private ValueBase	active_thread;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

StateBase(int numlocal)
{
   local_values = new ValueBase[numlocal];
   stack_values = new Stack<ValueBase>();
   field_map = new HashMap<BT_Field,ValueBase>(4);
   base_fields = null;
   state_set = null;
   return_stack = null;
   current_inits = null;
   active_thread = null;

   for (int i = 0; i < numlocal; ++i) local_values[i] = null;
}




/********************************************************************************/
/*										*/
/*	Methods for manipulating states 					*/
/*										*/
/********************************************************************************/

StateBase cloneState()
{
   StateBase ns = new StateBase(local_values.length);
   for (int i = 0; i < local_values.length; ++i) ns.local_values[i] = local_values[i];
   for (ValueBase cs : stack_values) {
      ns.stack_values.push(cs);
    }

   if (return_stack == null) ns.return_stack = null;
   else {
      ns.return_stack = new Stack<Integer>();
      ns.return_stack.addAll(return_stack);
    }

   ns.state_set = null;

   ns.base_fields = base_fields;
   ns.field_map = new HashMap<BT_Field,ValueBase>(field_map);
   ns.current_inits = current_inits;
   ns.active_thread = active_thread;

   return ns;
}



void pushStack(ValueBase cv)		 { stack_values.push(cv); }

ValueBase popStack()			 { return stack_values.pop(); }



boolean isCategory2()
{
   ValueBase cv = stack_values.peek();
   return cv.isCategory2();
}


ValueBase getLocal(int idx)		 { return local_values[idx]; }
void setLocal(int idx,ValueBase cv)	 { local_values[idx] = cv; }


boolean addLocal(int idx,ValueBase cv)
{
   ValueBase ov = local_values[idx];

   if (ov == null) local_values[idx] = cv;
   else local_values[idx] = ov.mergeValue(cv);

   return local_values[idx] != ov;
}



ValueBase getFieldMap(BT_Field fld)
{
   ValueBase vb = field_map.get(fld);

   if (vb == null && base_fields != null) vb = base_fields.get(fld);

   return vb;
}


void setFieldMap(BT_Field fld,ValueBase s0)
{
   if (fld.isVolatile() || s0 == null) return;

   field_map.put(fld,s0);
}



void setBaseField(BT_Field fld,ValueBase s0)
{
   if (fld.isVolatile() || s0 == null) return;

   if (base_fields == null) base_fields = new HashMap<BT_Field,ValueBase>();

   base_fields.put(fld,s0);
}



Iterable<BT_Field> getKnownFields()		{ return field_map.keySet(); }


boolean hasKnownFields()			{ return field_map.size() > 0; }


void discardFields()
{
   field_map.clear();
}



void handleDup(boolean dbl,int lvl)
{
   ValueBase v1 = stack_values.pop();

   if (dbl && v1.isCategory2()) dbl = false;
   if (lvl == 2) {
      ValueBase chk = stack_values.peek();
      if (dbl) {
	 ValueBase x = stack_values.pop();
	 chk = stack_values.peek();
	 stack_values.push(x);
       }
      if (chk.isCategory2()) lvl = 1;
    }

   if (lvl == 0 && !dbl) {		// dup
      stack_values.push(v1);
      stack_values.push(v1);
    }
   else if (lvl == 0 && dbl) {		// dup2
      ValueBase v2 = stack_values.pop();
      stack_values.push(v2);
      stack_values.push(v1);
      stack_values.push(v2);
      stack_values.push(v1);
    }
   else if (lvl == 1 && !dbl) { 	// dup_x1
      ValueBase v2 = stack_values.pop();
      stack_values.push(v1);
      stack_values.push(v2);
      stack_values.push(v1);
    }
   else if (lvl == 1 && dbl) {		// dup2_x1
      ValueBase v2 = stack_values.pop();
      ValueBase v3 = stack_values.pop();
      stack_values.push(v2);
      stack_values.push(v1);
      stack_values.push(v3);
      stack_values.push(v2);
      stack_values.push(v1);
    }
   else if (lvl == 2 && !dbl) { 	 // dup_x2
      ValueBase v2 = stack_values.pop();
      ValueBase v3 = stack_values.pop();
      stack_values.push(v1);
      stack_values.push(v3);
      stack_values.push(v2);
      stack_values.push(v1);
    }
   else if (lvl == 2 && dbl) {		// dup2_x2
      ValueBase v2 = stack_values.pop();
      ValueBase v3 = stack_values.pop();
      ValueBase v4 = stack_values.pop();
      stack_values.push(v2);
      stack_values.push(v1);
      stack_values.push(v4);
      stack_values.push(v3);
      stack_values.push(v2);
      stack_values.push(v1);
    }
}



void resetStack(StateBase sb)
{
   while (stack_values.size() > sb.stack_values.size()) {
      stack_values.pop();
    }
}



ValueBase getThread()				 { return active_thread; }

void setThread(ValueBase thrd)
{
   active_thread = thrd;
}

boolean mergeThread(ValueBase thrd)
{
   ValueBase othrd = active_thread;
   if (active_thread == null) active_thread = thrd;
   else if (thrd == null) return false;
   else active_thread = active_thread.mergeValue(thrd);

   return active_thread != othrd;
}




/********************************************************************************/
/*										*/
/*	Methods for handling set of active constructors 			*/
/*										*/
/********************************************************************************/

void startInitialization(BT_Class bc)
{
   current_inits = new StateCtor(bc,current_inits);
}

void finishInitialization(BT_Class bc)
{
   if (current_inits != null) current_inits = current_inits.finishCtor(bc);
}


boolean testDoingInitialization(BT_Class bc)
{
   if (current_inits == null) return false;
   return current_inits.testInCtor(bc);
}



boolean addInitializers(StateBase sb)
{
   if (sb.current_inits == null) return false;

   if (current_inits != null) {
      return current_inits.mergeWith(sb.current_inits);
    }

   current_inits = sb.current_inits;

   return true;
}



Iterator<BT_Class> getInitializations()
{
   if (current_inits == null) {
      List<BT_Class> lc = Collections.emptyList();
      return lc.iterator();
    }

   return current_inits.getClasses();
}




/********************************************************************************/
/*										*/
/*	Methods for handling return values					*/
/*										*/
/********************************************************************************/

void pushReturn(int nino)
{
   if (return_stack == null) return_stack = new Stack<Integer>();
   return_stack.push(new Integer(nino));
}



int popReturn()
{
   if (return_stack == null) return -1;

   Integer ino = return_stack.pop();
   if (return_stack.empty()) return_stack = null;

   return ino.intValue();
}




StateBase mergeWithState(StateBase cs)
{
   StateBase ucs = null;

   if (cs.return_stack == null && return_stack == null) {
      ucs = this;
    }
   else if (cs.return_stack == null || return_stack == null) {
      ucs = this;
      return_stack = null;
    }
   else if (return_stack.equals(cs.return_stack)) {
      ucs = this;
    }
   else {
      if (state_set == null) {
	 state_set = new Vector<StateBase>();
	 state_set.add(this);
       }
      for (StateBase scs : state_set) {
	 if (scs.return_stack.equals(cs.return_stack)) {
	    ucs = scs;
	    break;
	  }
       }
      if (ucs == null) {
	 ucs = cs.cloneState();
	 ucs.state_set = state_set;
	 state_set.add(ucs);
	 return ucs;
       }
    }

   if (!ucs.checkMergeWithState(cs)) return null;

   return ucs;
}



boolean compatibleWith(StateBase cs)
{
   if (return_stack == null) {
      if (cs.return_stack != null) return false;
    }
   else if (!return_stack.equals(cs.return_stack)) return false;

   for (int i = 0; i < local_values.length; ++i) {
      ValueBase ov = local_values[i];
      ValueBase nv = cs.local_values[i];
      if (ov == null) ov = nv;
      if (ov != nv) return false;
    }

   int j0 = stack_values.size();
   int j1 = cs.stack_values.size();
   if (j0 > j1) j0 = j1;
   for (int i = 0; i < j0; ++i) {
      ValueBase oo = stack_values.elementAt(i);
      ValueBase no = cs.stack_values.elementAt(i);

      if (oo == null) oo = no;
      if (no == null) no = oo;
      if (no != oo) return false;
    }

   return true;
}




/********************************************************************************/
/*										*/
/*	Methods for merging states						*/
/*										*/
/********************************************************************************/

private boolean checkMergeWithState(StateBase cs)
{
   boolean change = false;

   for (int i = 0; i < local_values.length; ++i) {
      ValueBase ov = local_values[i];
      ValueBase nv = (ov == null ? cs.local_values[i] : ov.mergeValue(cs.local_values[i]));
      if (nv != ov) {
	 change = true;
	 local_values[i] = nv;
       }
    }

   int j0 = stack_values.size();
   int j1 = cs.stack_values.size();
   if (j0 > j1) j0 = j1;

   for (int i = 0; i < j0; ++i) {
      ValueBase oo = stack_values.elementAt(i);
      ValueBase no = cs.stack_values.elementAt(i);

      if (no == null) no = oo;
      if (oo != null) {
	 no = oo.mergeValue(no);
       }

      if (no != oo) {
	 change = true;
	 stack_values.setElementAt(no,i);
       }
    }

   for (Iterator<Map.Entry<BT_Field,ValueBase>> it = field_map.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<BT_Field,ValueBase> ent = it.next();
      BT_Field fld = ent.getKey();
      ValueBase val = ent.getValue();
      ValueBase nval = cs.getFieldMap(fld);
      if (nval == null) {
	 it.remove();
	 change = true;
       }
      else {
	 nval = val.mergeValue(nval);
	 if (val != nval) {
	    field_map.put(fld,nval);
	    change = true;
	  }
       }
    }

   if (current_inits == null) current_inits = cs.current_inits;
   else current_inits.mergeWith(cs.current_inits);

   if (active_thread != cs.active_thread && cs.active_thread != null) {
      if (active_thread == null) {
	 active_thread = cs.active_thread;
	 change = true;
       }
      else {
	ValueBase ntv = active_thread.mergeValue(cs.active_thread);
	if (ntv != active_thread) {
	   active_thread = ntv;
	   change = true;
	 }
      }
    }

   return change;
}



/********************************************************************************/
/*										*/
/*	Incremental update methods						*/
/*										*/
/********************************************************************************/

void handleUpdates(Collection<SourceBase> oldsrcs,
		      Map<SourceSet,SourceSet> srcupdates,
		      Map<JflowValue,JflowValue> valupdates,
		      List<StateBase> workqueue)
{
   for (int i = 0; i < local_values.length; ++i) {
      if (local_values[i] != null) {
	 JflowValue jv = valupdates.get(local_values[i]);
	 if (jv != null) local_values[i] = (ValueBase) jv;
       }
    }

   for (int i = 0; i < stack_values.size(); ++i) {
      JflowValue jv = stack_values.get(i);
      if (jv != null) {
	 jv = valupdates.get(jv);
	 if (jv != null) stack_values.set(i,(ValueBase) jv);
       }
    }

   for (Map.Entry<BT_Field,ValueBase> ent : field_map.entrySet()) {
      JflowValue jv = ent.getValue();
      if (jv != null) {
	 jv = valupdates.get(jv);
	 if (jv != null) ent.setValue((ValueBase) jv);
       }
    }

   if (active_thread != null) {
      JflowValue jv = valupdates.get(active_thread);
      if (jv != null) active_thread = (ValueBase) jv;
    }

   if (state_set != null) {
      for (StateBase sb : state_set) {
	 if (sb != this) workqueue.add(sb);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Unique value methods							*/
/*										*/
/********************************************************************************/

boolean updateUnique(ValueBase v)
{
   boolean chng = false;

   for (int i = 0; i < local_values.length; ++i) {
      ValueBase v0 = local_values[i];
      ValueBase v1 = updateUniqueValue(v0,v);
      if (v1 != v0) {
	 local_values[i] = v1;
	 chng = true;
       }
    }

   for (ListIterator<ValueBase> it = stack_values.listIterator(); it.hasNext(); ) {
      ValueBase v0 = it.next();
      ValueBase v1 = updateUniqueValue(v0,v);
      if (v1 != v0) {
	 it.set(v1);
	 chng = true;
       }
    }

   for (Map.Entry<BT_Field,ValueBase> ent : field_map.entrySet()) {
      ValueBase v0 = ent.getValue();
      ValueBase v1 = updateUniqueValue(v0,v);
      if (v1 != v0) {
	 ent.setValue(v1);
	 chng = true;
       }
    }

   if (state_set != null) {
      for (StateBase sb : state_set) sb.updateUnique(v);
    }

   return chng;
}



private ValueBase updateUniqueValue(ValueBase v0,ValueBase v)
{
   if (v0 == null) return null;

   ValueBase v1 = v0;
   if (v == null) v1 = v0.getNonunique();
   else if (v == v0) v1 = v.makeNonunique();
   return v1;
}




}	// end of class StateBase




/* end of StateBase.java */
