/********************************************************************************/
/*										*/
/*		ValueState.java 						*/
/*										*/
/*	Holder for state values during model generation 			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/model/ValueState.java,v 1.4 2018/02/21 16:18:50 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ValueState.java,v $
 * Revision 1.4  2018/02/21 16:18:50  spr
 * Formatting
 *
 * Revision 1.3  2007-05-04 02:00:03  spr
 * Update jflow with generic value/source flags.
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

import edu.brown.cs.ivy.jflow.JflowConstants;

import com.ibm.jikesbt.BT_Field;

import java.util.*;


class ValueState implements JflowConstants
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ModelValue []	 local_values;
private Stack<ModelValue> stack_values;
private Map<BT_Field,ModelValue> field_map;
private Stack<Integer>	return_stack;
private List<ValueState> state_set;
private ModelValue	 active_thread;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ValueState(int numlocal)
{
   local_values = new ModelValue[numlocal];
   stack_values = new Stack<ModelValue>();
   field_map = new HashMap<BT_Field,ModelValue>(4);
   state_set = null;
   return_stack = null;
   active_thread = null;

   for (int i = 0; i < numlocal; ++i) local_values[i] = null;
}




/********************************************************************************/
/*										*/
/*	Methods for manipulating states 					*/
/*										*/
/********************************************************************************/

public ValueState cloneState()
{
   ValueState ns = new ValueState(local_values.length);
   for (int i = 0; i < local_values.length; ++i) ns.local_values[i] = local_values[i];
   for (ModelValue cs : stack_values) {
      ns.stack_values.push(cs);
    }

   if (return_stack == null) ns.return_stack = null;
   else {
      ns.return_stack = new Stack<Integer>();
      ns.return_stack.addAll(return_stack);
    }

   ns.state_set = null;

   ns.field_map = new HashMap<BT_Field,ModelValue>(field_map);
   ns.active_thread = active_thread;

   return ns;
}



public void pushStack(ModelValue cv)		 { stack_values.push(cv); }

public ModelValue popStack()			 { return stack_values.pop(); }



public boolean isCategory2()
{
   ModelValue cv = stack_values.peek();
   return cv.isCategory2();
}


public ModelValue getLocal(int idx)		 { return local_values[idx]; }
public void setLocal(int idx,ModelValue cv)	 { local_values[idx] = cv; }


public boolean addLocal(int idx,ModelValue cv)
{
   ModelValue ov = local_values[idx];

   if (ov == null) local_values[idx] = cv;
   else local_values[idx] = ov.mergeValue(cv);

   return local_values[idx] != ov;
}



public ModelValue getFieldMap(BT_Field fld)
{
   return field_map.get(fld);
}


public void setFieldMap(BT_Field fld,ModelValue s0)
{
   if (fld.isVolatile() || s0 == null) return;

   field_map.put(fld,s0);
}



public Iterator<BT_Field> getKnownFields()	{ return field_map.keySet().iterator(); }


public void discardFields()
{
   field_map.clear();
}



public void handleDup(boolean dbl,int lvl)
{
   ModelValue v1 = stack_values.pop();

   if (dbl && v1.isCategory2()) dbl = false;
   if (lvl == 2) {
      ModelValue chk = stack_values.peek();
      if (dbl) {
	 ModelValue x = stack_values.pop();
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
      ModelValue v2 = stack_values.pop();
      stack_values.push(v2);
      stack_values.push(v1);
      stack_values.push(v2);
      stack_values.push(v1);
    }
   else if (lvl == 1 && !dbl) { 	// dup_x1
      ModelValue v2 = stack_values.pop();
      stack_values.push(v1);
      stack_values.push(v2);
      stack_values.push(v1);
    }
   else if (lvl == 1 && dbl) {		// dup2_x1
      ModelValue v2 = stack_values.pop();
      ModelValue v3 = stack_values.pop();
      stack_values.push(v2);
      stack_values.push(v1);
      stack_values.push(v3);
      stack_values.push(v2);
      stack_values.push(v1);
    }
   else if (lvl == 2 && !dbl) { 	 // dup_x2
      ModelValue v2 = stack_values.pop();
      ModelValue v3 = stack_values.pop();
      stack_values.push(v1);
      stack_values.push(v3);
      stack_values.push(v2);
      stack_values.push(v1);
    }
   else if (lvl == 2 && dbl) {		// dup2_x2
      ModelValue v2 = stack_values.pop();
      ModelValue v3 = stack_values.pop();
      ModelValue v4 = stack_values.pop();
      stack_values.push(v2);
      stack_values.push(v1);
      stack_values.push(v4);
      stack_values.push(v3);
      stack_values.push(v2);
      stack_values.push(v1);
    }
}



public void resetStack(ValueState sb)
{
   while (stack_values.size() > sb.stack_values.size()) {
      stack_values.pop();
    }
}



public ModelValue getThread()				 { return active_thread; }

public void setThread(ModelValue thrd)
{
   active_thread = thrd;
}

public boolean mergeThread(ModelValue thrd)
{
   ModelValue othrd = active_thread;
   if (active_thread == null) active_thread = thrd;
   else if (thrd == null) return false;
   else active_thread = active_thread.mergeValue(thrd);

   return active_thread != othrd;
}




/********************************************************************************/
/*										*/
/*	Methods for handling return values					*/
/*										*/
/********************************************************************************/

public void pushReturn(int nino)
{
   if (return_stack == null) return_stack = new Stack<Integer>();
   return_stack.push(new Integer(nino));
}



public int popReturn()
{
   if (return_stack == null) return -1;

   Integer ino = return_stack.pop();
   if (return_stack.empty()) return_stack = null;

   return ino.intValue();
}




public ValueState mergeWithState(ValueState cs)
{
   ValueState ucs = null;

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
	 state_set = new Vector<ValueState>();
	 state_set.add(this);
       }
      for (ValueState scs : state_set) {
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



public boolean compatibleWith(ValueState cs)
{
   if (return_stack == null) {
      if (cs.return_stack != null) return false;
    }
   else if (!return_stack.equals(cs.return_stack)) return false;

   for (int i = 0; i < local_values.length; ++i) {
      ModelValue ov = local_values[i];
      ModelValue nv = cs.local_values[i];
      if (ov == null) ov = nv;
      if (ov != null && !ov.equals(nv)) return false;
    }

   int j0 = stack_values.size();
   int j1 = cs.stack_values.size();
   if (j0 > j1) j0 = j1;
   for (int i = 0; i < j0; ++i) {
      ModelValue oo = stack_values.elementAt(i);
      ModelValue no = cs.stack_values.elementAt(i);
      if (oo == null) oo = no;
      if (no == null) no = oo;
      if (no != null && !no.equals(oo)) return false;
    }

   return true;
}




/********************************************************************************/
/*										*/
/*	Methods for merging states						*/
/*										*/
/********************************************************************************/

private boolean checkMergeWithState(ValueState cs)
{
   boolean change = false;

   for (int i = 0; i < local_values.length; ++i) {
      ModelValue ov = local_values[i];
      ModelValue nv = (ov == null ? cs.local_values[i] : ov.mergeValue(cs.local_values[i]));
      if (nv != ov) {
	 change = true;
	 local_values[i] = nv;
       }
    }

   int j0 = stack_values.size();
   int j1 = cs.stack_values.size();
   if (j0 > j1) j0 = j1;

   for (int i = 0; i < j0; ++i) {
      ModelValue oo = stack_values.elementAt(i);
      ModelValue no = cs.stack_values.elementAt(i);

      if (no == null) no = oo;
      if (oo != null) {
	 no = oo.mergeValue(no);
       }

      if (no != oo) {
	 change = true;
	 stack_values.setElementAt(no,i);
       }
    }

   for (Iterator<Map.Entry<BT_Field,ModelValue>> it = field_map.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<BT_Field,ModelValue> ent = it.next();
      BT_Field fld = ent.getKey();
      ModelValue val = ent.getValue();
      ModelValue nval = cs.getFieldMap(fld);
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

   if (active_thread != cs.active_thread && cs.active_thread != null) {
      if (active_thread == null) {
	 active_thread = cs.active_thread;
	 change = true;
       }
      else {
	ModelValue ntv = active_thread.mergeValue(cs.active_thread);
	if (ntv != active_thread) {
	   active_thread = ntv;
	   change = true;
	 }
      }
    }

   return change;
}



}	// end of class ValueState




/* end of ValueState.java */

