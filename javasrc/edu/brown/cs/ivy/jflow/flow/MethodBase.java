/********************************************************************************/
/*										*/
/*		MethodBase.java 						*/
/*										*/
/*	Class to hold method information during flow analysis			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/MethodBase.java,v 1.18 2018/08/02 15:10:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MethodBase.java,v $
 * Revision 1.18  2018/08/02 15:10:17  spr
 * Fix imports.
 *
 * Revision 1.17  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.16  2011-04-13 02:24:05  spr
 * Add debugging.
 *
 * Revision 1.15  2009-09-17 01:57:20  spr
 * Fix a few minor bugs (poll, interfaces); code cleanup for Eclipse.
 *
 * Revision 1.14  2009-06-04 18:50:27  spr
 * Handle special cased methods correctly.
 *
 * Revision 1.13  2008-11-12 13:45:39  spr
 * Eclipse fixups.
 *
 * Revision 1.12  2007-12-13 20:21:17  spr
 * Fix exception handling for built in methods.
 *
 * Revision 1.11  2007-08-10 02:10:39  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.10  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.9  2007-02-27 18:53:29  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.8  2007-01-03 14:04:59  spr
 * Fix imports
 *
 * Revision 1.7  2007-01-03 03:24:18  spr
 * Modifications to handle incremental update.
 *
 * Revision 1.6  2006-12-01 03:22:46  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.5  2006-08-03 12:34:51  spr
 * Ensure fields of unprocessed classes handled correctly.
 *
 * Revision 1.4  2006/07/23 02:25:02  spr
 * Minor bug fixes and speed ups.
 *
 * Revision 1.3  2006/07/10 14:52:17  spr
 * Code cleanup.
 *
 * Revision 1.2  2006/07/03 18:15:23  spr
 * Efficiency improvements; inlining options.
 *
 * Revision 1.1  2006/06/21 02:18:34  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.flow;


import edu.brown.cs.ivy.cinder.CinderManager;
import edu.brown.cs.ivy.jflow.JflowMethod;
import edu.brown.cs.ivy.jflow.JflowMethodData;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_ClassVector;
import com.ibm.jikesbt.BT_CodeAttribute;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_Opcodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;



public class MethodBase implements JflowMethod, BT_Opcodes
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private BT_Method for_method;
private StateBase start_state;
private ValueBase result_set;
private Map<BT_Field,ValueBase> result_fields;
private ValueBase exception_set;
private int num_adds;
private int num_result;
private boolean is_clone;
private boolean is_arg0;
private boolean is_proto;
private boolean change_unique;
private MethodSpecial special_data;
private int	inline_counter;
private JflowMethodData method_data;
private boolean can_exit;

private Map<BT_Ins,Set<JflowMethod>>	      replace_map;
private Map<BT_Ins,SourceBase>		      array_map;
private Map<BT_Ins,SourceBase>		      source_map;
private Map<BT_Ins,SourceBase>		      modelsrc_map;
private Map<BT_Ins,Map<BT_Method,MethodBase>> call_map;
private Map<BT_Ins,Set<MethodBase>>	      proto_map;
private Map<BT_Ins,FlowCallSite>	      site_map;
private Set<BT_Ins>			      ignore_set;

private static Map<BT_Method,ValueBase> native_map = new HashMap<>();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MethodBase(FlowMaster jm,BT_Method bm,int ct)
{
   jflow_master = jm;
   for_method = bm;
   special_data = MethodSpecial.getSpecial(bm);
   inline_counter = ct;
   BT_CodeAttribute code = bm.getCode();
   BT_MethodSignature sgn = bm.getSignature();

   if (bm.isVoidMethod()) result_set = null;
   else result_set = jflow_master.anyValue(sgn.returnType);
   result_fields = null;
   exception_set = null;

   int idx = 0;
   int mxl = 0;
   if (code != null) mxl = code.maxLocals;
   else {
      mxl = 1;
      for (int i = 0; i < sgn.types.size(); ++i) {
	 BT_Class bc = sgn.types.elementAt(i);
	 if (bc == BT_Class.getDouble() || bc == BT_Class.getLong()) ++mxl;
	 ++mxl;
       }
    }

   start_state = new StateBase(mxl);
   if (bm.isInstanceMethod() || bm.isConstructor()) {
      ValueBase cv = jflow_master.anyValue(bm.getDeclaringClass());
      if (jflow_master.isStartMethod(this)) cv = jflow_master.nativeValue(bm.getDeclaringClass());
      cv = cv.forceNonNull();
      start_state.setLocal(idx++,cv);
    }
   for (int i = 0; i < sgn.types.size(); ++i) {
      BT_Class bc = sgn.types.elementAt(i);
      ValueBase cv = jflow_master.anyValue(bc);
      if (i == 0 && for_method.isMain()) cv = jflow_master.mainArgs();
      start_state.setLocal(idx++,cv);
      if (cv.isCategory2()) ++idx;
    }
   num_adds = 0;
   num_result = 0;

   is_clone = false;
   is_arg0 = false;
   is_proto = false;
   can_exit = false;
   change_unique = false;

   if (bm.fullName().equals("java.lang.Object.clone")) {
      is_clone = true;
      num_result++;
    }
   else if (special_data != null) {
      is_arg0 = special_data.returnsArg0();
      ValueBase rv = special_data.getReturnValue(for_method,null,0);
      addResult(rv,null);
      can_exit = special_data.getExits();
      if (rv != null) {
	 ValueBase ev = null;
	 for (Enumeration<?> e = bm.declaredExceptions().elements(); e.hasMoreElements(); ) {
	    BT_Class ec = (BT_Class) e.nextElement();
	    ValueBase ecv = jflow_master.nativeValue(ec);
	    ecv = ecv.forceNonNull();
	    ev = ecv.mergeValue(ev);
	  }
	 exception_set = ev;
       }
    }

   replace_map = new HashMap<>(4);
   array_map = new HashMap<>(4);
   source_map = new HashMap<>(4);
   modelsrc_map = new HashMap<>(4);
   call_map = new HashMap<>(4);
   proto_map = new HashMap<>(4);
   site_map = new HashMap<>(4);
   ignore_set = null;

   method_data = jflow_master.createMethodData(this);
}




MethodBase(FlowMaster jm,BT_Method bm)
{
   jflow_master = jm;
   for_method = bm;
   special_data = MethodSpecial.getSpecial(bm);
   inline_counter = 0;

   result_set = null;
   result_fields = null;
   exception_set = null;
   start_state = null;
   num_adds = 0;
   num_result = 0;
   is_clone = false;
   is_arg0 = false;
   replace_map = null;
   array_map = null;
   source_map = null;
   modelsrc_map = null;
   call_map = null;
   proto_map = null;
   can_exit = false;

   method_data = null;
}




/********************************************************************************/
/*										*/
/*	Access Methods								*/
/*										*/
/********************************************************************************/

public int getInstanceNumber()			{ return inline_counter; }

public StateBase getStartState()		{ return start_state; }

public ValueBase getResultSet() 		{ return result_set; }
Map<BT_Field,ValueBase> getResultFields()	{ return result_fields; }

public boolean isClone()			{ return is_clone; }

public boolean returnArg0()			{ return is_arg0; }

@Override public BT_Method getMethod()			{ return for_method; }
@Override public BT_Class getMethodClass()		{ return for_method.getDeclaringClass(); }

@Override public BT_CodeAttribute getCode()		{ return for_method.getCode(); }
@Override public BT_InsVector getCodeVector()
{
   if (for_method.getCode() == null) return null;
   return for_method.getCode().ins;
}


@Override public String getMethodName()
{
   return CinderManager.getMethodName(for_method);
}

@Override public String getMethodSignature()
{
   return CinderManager.getMethodSignature(for_method);
}


@Override public JflowValue getExceptionSet()		{ return exception_set; }

public boolean isPrototype()			{ return is_proto; }
void setPrototype()				{ is_proto = true; }


@Override public boolean isInProject()
{
   return jflow_master.isProjectClass(getMethodClass());
}

@Override public boolean getCanExit()			{ return can_exit; }
void setCanExit()
{
   if (FlowMaster.doDebug()) System.err.println("\tEXIT note for " + getMethodName());
   can_exit = true;
}


void setChangeUnique()				{ change_unique = true; }
boolean getChangeUnique()			{ return change_unique; }



/********************************************************************************/
/*										*/
/*	Methods for accessing special data					*/
/*										*/
/********************************************************************************/

@Override public boolean getIsAsync(BT_Method bm)
{
   if (special_data != null) return special_data.getIsAsync();

   if (bm != null) {
      MethodSpecial ms = MethodSpecial.getSpecial(bm);
      if (ms != null) return ms.getIsAsync();
    }

   return false;
}



@Override public int getMaxThreads()
{
   int mx = 0;
   if (special_data != null) mx = special_data.getMaxThreads();
   if (mx <= 0) mx = JFLOW_MAX_THREADS;

   return mx;
}



@Override public boolean getDontScan()
{
   if (special_data != null) return special_data.getDontScan();

   return false;
}



@Override public boolean hasSpecial()
{
   return special_data != null;
}




/********************************************************************************/
/*										*/
/*	Incremental update methods						*/
/*										*/
/********************************************************************************/

void clearForUpdate(Collection<SourceBase> bases)
{
   if (bases != null) {
      for (Map.Entry<BT_Ins,SourceBase> ent : source_map.entrySet()) {
	 if (ent.getValue() != null) bases.add(ent.getValue());
       }
      for (Map.Entry<BT_Ins,SourceBase> ent : modelsrc_map.entrySet()) {
	 if (ent.getValue() != null) bases.add(ent.getValue());
       }
      for (Map.Entry<BT_Ins,SourceBase> ent : array_map.entrySet()) {
	 if (ent.getValue() != null) bases.add(ent.getValue());
       }
    }

   array_map = new HashMap<BT_Ins,SourceBase>(4);
   source_map = new HashMap<BT_Ins,SourceBase>(4);
   modelsrc_map = new HashMap<BT_Ins,SourceBase>(4);
   call_map = new HashMap<BT_Ins,Map<BT_Method,MethodBase>>(4);
   proto_map = new HashMap<BT_Ins,Set<MethodBase>>(4);
   site_map = new HashMap<BT_Ins,FlowCallSite>(4);
   ignore_set = null;

   if (method_data != null) method_data.clear();
}



void handleUpdates(Collection<SourceBase> oldsrcs,
		      Map<SourceSet,SourceSet> srcupdates,
		      Map<JflowValue,JflowValue> valupdates)
{
   for (Iterator<Map.Entry<BT_Ins,SourceBase>> it = array_map.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<BT_Ins,SourceBase> ent = it.next();
      if (oldsrcs.contains(ent.getValue())) it.remove();
      else ent.getValue().handleUpdates(oldsrcs,srcupdates,valupdates);
    }

   for (Iterator<Map.Entry<BT_Ins,SourceBase>> it = source_map.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<BT_Ins,SourceBase> ent = it.next();
      if (oldsrcs.contains(ent.getValue())) it.remove();
    }

   for (Iterator<Map.Entry<BT_Ins,SourceBase>> it = modelsrc_map.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<BT_Ins,SourceBase> ent = it.next();
      if (oldsrcs.contains(ent.getValue())) it.remove();
    }

   if (method_data != null) method_data.updateValues(valupdates);
}



/********************************************************************************/
/*										*/
/*	Methods to handle saved maps						*/
/*										*/
/********************************************************************************/

@Override public JflowValue getAssociation(AssociationType typ,BT_Ins ins)
{
   if (method_data == null) return null;

   return method_data.getAssociation(typ,ins);
}



void setAssociation(AssociationType typ,BT_Ins ins,ValueBase cv)
{
   if (method_data != null) method_data.setAssociation(typ,ins,cv);
}




@Override public Set<JflowMethod> getReplacementCalls(BT_Ins ins)
{
   if (replace_map == null) return null;

   return replace_map.get(ins);
}



void addReplacementCall(BT_Ins ins,MethodBase cm)
{
   if (ins == null) return;

   Set<JflowMethod> s = replace_map.get(ins);
   if (s == null) {
      s = new HashSet<JflowMethod>(4);
      replace_map.put(ins,s);
    }
   s.add(cm);
}



@Override public Set<JflowMethod> getAllReplacementCalls()
{
   Set<JflowMethod> s = new HashSet<JflowMethod>();
   for (Set<JflowMethod> s1 : replace_map.values()) {
      s.addAll(s1);
    }
   return s;
}



void addSynchronizedSet(BT_Ins ins,ValueBase cv)
{
   setAssociation(AssociationType.SYNC,ins,cv);
}




SourceBase getArray(BT_Ins ins) 		{ return array_map.get(ins); }

void setArray(BT_Ins ins,SourceBase a)		{ array_map.put(ins,a); }




boolean isSourceDefined(BT_Ins ins)		{ return source_map.containsKey(ins); }
SourceBase getSource(BT_Ins ins)		{ return source_map.get(ins); }
void setSource(BT_Ins ins,SourceBase s) 	{ source_map.put(ins,s); }





boolean isModelSourceDefined(BT_Ins ins)	{ return modelsrc_map.containsKey(ins); }
SourceBase getModelSource(BT_Ins ins)		{ return modelsrc_map.get(ins); }
void setModelSource(BT_Ins i,SourceBase s)	{ modelsrc_map.put(i,s); }





FlowCallSite getCallSite(BT_Ins ins)
{
   return site_map.get(ins);
}

void addCallSite(BT_Ins ins,FlowCallSite cs)
{
   site_map.put(ins,cs);
}


@Override public JflowValue getThisValue()
{
   if (for_method.isClassMethod() || for_method.isStaticInitializer()) return null;

   return start_state.getLocal(0);
}



@Override public boolean getIgnoreBlock(BT_Ins ins)
{
   if (ignore_set == null) return false;
   if (ignore_set.contains(ins)) return true;
   return false;
}

void addIgnoreBlock(BT_Ins ins)
{
   if (ignore_set == null) ignore_set = new HashSet<BT_Ins>(4);
   ignore_set.add(ins);
}


void clearIgnoreBlocks()
{
   ignore_set = null;
}




@Override public Iterable<JflowValue> getParameterValues()
{
   Collection<JflowValue> rslt = new ArrayList<JflowValue>();

   BT_MethodSignature sgn = for_method.getSignature();

   int idx = 0;
   if (for_method.isInstanceMethod() || for_method.isConstructor()) {
      ValueBase vb = start_state.getLocal(idx++);
      rslt.add(vb);
    }
   for (int i = 0; i < sgn.types.size(); ++i) {
      ValueBase vb = start_state.getLocal(idx++);
      rslt.add(vb);
      if (vb.isCategory2()) ++idx;
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Methods to handle calls associated with an instruction			*/
/*										*/
/********************************************************************************/

@Override public int getCallCount(BT_Ins ins)
{
   int ct = 0;

   Map<BT_Method,MethodBase> m = call_map.get(ins);
   if (m != null) ct += m.values().size();

   Set<MethodBase> s = proto_map.get(ins);
   if (s != null) ct += s.size();

   return ct;
}



@Override public Iterable<JflowMethod> getAllCalls(BT_Ins ins)
{
   Map<BT_Method,MethodBase> m = call_map.get(ins);
   Set<MethodBase> s = proto_map.get(ins);

   if (m == null && s == null) {
      Set<JflowMethod> ms = Collections.emptySet();
      return ms;
    }

   ArrayList<JflowMethod> rslt = new ArrayList<JflowMethod>();

   if (m != null) rslt.addAll(m.values());
   else if (s != null) rslt.addAll(s);

   return rslt;
}



public void noteCall(BT_Ins ins,MethodBase cm)
{
   Map<BT_Method,MethodBase> m = call_map.get(ins);
   if (m == null) {
      m = new HashMap<>();
      call_map.put(ins,m);
    }
   m.put(cm.getMethod(),cm);
}



public void notePrototypeCall(BT_Ins ins,MethodBase cm)
{
   Set<MethodBase> s = proto_map.get(ins);
   if (s == null) {
      s = new HashSet<MethodBase>();
      proto_map.put(ins,s);
    }
   s.add(cm);
}



public boolean getCallsAt(BT_Ins ins,MethodBase cm)
{
   Map<BT_Method,MethodBase> m = call_map.get(ins);
   if (m == null) return false;
   return m.get(cm.getMethod()) == cm;
}



public MethodBase getCallForMethod(BT_Ins ins,BT_Method bm)
{
   Map<BT_Method,MethodBase> m = call_map.get(ins);
   if (m == null) return null;
   return m.get(bm);
}



@Override public JflowValue getArgValue(int idx)
{
   BT_MethodSignature sgn = for_method.getSignature();

   if (idx >= sgn.types.size()) return null;

   int vidx = 0;
   if (for_method.isInstanceMethod() || for_method.isConstructor()) ++vidx;
   for (int i = 0; i < idx; ++i) {
      BT_Class bc = sgn.types.elementAt(i);
      vidx += bc.getSizeForLocal();
    }

   return start_state.getLocal(vidx);
}



/********************************************************************************/
/*										*/
/*	Methods to handle results						*/
/*										*/
/********************************************************************************/

@Override public boolean hasResult()
{
   if (num_result > 0 || is_arg0) return true;
   if (for_method.isNative()) {
      result_set = native_map.get(for_method);
      if (result_set == null) {
	 result_set = jflow_master.nativeValue(for_method.getSignature().returnType);
	 native_map.put(for_method,result_set);
       }
      ++num_result;
      return true;
    }
   return false;
}



public final boolean addResult(ValueBase cv,StateBase st)
{
   boolean chng = false;

   if (st != null && (for_method.isInstanceMethod() || for_method.isConstructor())) {
      ValueBase tv = st.getLocal(0);
      if (tv.isUnique()) {
	 if (result_fields == null) result_fields = new HashMap<>();
	 if (FlowMaster.doDebug()) System.err.println("\tSave result fields");
	 for (BT_Field bf : st.getKnownFields()) {
	    ValueBase ov = result_fields.get(bf);
	    ValueBase nv = st.getFieldMap(bf);
	    if (ov != null) nv = nv.mergeValue(ov);
	    if (nv != ov) {			// check for change
	       result_fields.put(bf,nv);
	       if (FlowMaster.doDebug()) System.err.println("\tUpdate result field " + bf + " = " + nv);
	       chng = true;
	     }
	  }
       }
      else {
	 if (result_fields != null) {			// discard if non-unique
	    if (FlowMaster.doDebug()) System.err.println("\tDiscard result fields");
	    result_fields = null;
	    chng = true;
	  }
       }
    }

   if (cv == null) {				// handle void routines
      ++num_result;
      return chng;
    }

   if (result_set == null || num_result == 0) {
      ++num_result;
      result_set = cv;
    }
   else {
      ++num_result;
      if (result_set == cv) return chng;
      ValueBase ns = result_set.mergeValue(cv);
      if (ns == result_set) return chng;
      result_set = ns;
    }

   if (is_arg0) return chng;

   return true;
}




/********************************************************************************/
/*										*/
/*	Methods to handle exceptions						*/
/*										*/
/********************************************************************************/

public boolean addException(ValueBase cv)
{
   if (exception_set == cv || cv == null) return false;
   if (exception_set == null) {
      exception_set = cv;
      return true;
    }
   ValueBase ns = exception_set.mergeValue(cv);
   if (ns == exception_set) return false;
   exception_set = ns;
   return true;
}




/********************************************************************************/
/*										*/
/*	Methods to handle calls 						*/
/*										*/
/********************************************************************************/

public boolean addCall(List<ValueBase> args,FlowControl cfc,StateBase st0)
{
   boolean chng = false;

   int idx = 0;
   for (ValueBase cv : args) {
      ValueBase ov = start_state.getLocal(idx);
      if (!cv.isBad()) {
	 if (num_adds == 0) start_state.setLocal(idx,cv);
	 else chng |= start_state.addLocal(idx,cv);
       }
      if (start_state.getLocal(idx).isBad()) {
	 System.err.println("JFLOW: Bad start state created for local " + idx + " of " +
			       for_method.fullName());
	 start_state.setLocal(idx,ov);
	 // throw new Error("Bad start state");
       }
      if (cv.isCategory2()) ++idx;
      ++idx;
    }

   if (num_adds++ == 0) {
      chng = true;
      if (special_data != null) {
	 for (Iterator<BT_Class> it = special_data.getLoadClasses(); it != null && it.hasNext(); ) {
	    BT_Class sbc = it.next();
	    cfc.initialize(sbc);
	  }
       }
    }

   if (st0 != null) {
      if (start_state != null) {
	 chng |= start_state.mergeThread(st0.getThread());
       }
    }

   ValueBase tv = (ValueBase) getThisValue();
   if (st0 != null && tv != null && tv.isUnique()) {
      for (BT_Field bf : st0.getKnownFields()) {
	 if (!validField(bf,for_method.getDeclaringClass())) continue;
	 ValueBase ov = start_state.getFieldMap(bf);
	 ValueBase nv = st0.getFieldMap(bf);
	 if (nv != null) {
	    if (ov != null) nv = nv.mergeValue(ov);
	    if (nv != ov) {
	       if (FlowMaster.doDebug()) System.err.println("\tSet initial field " + bf + " = " + nv);
	       start_state.setBaseField(bf,nv);
	       chng = true;
	     }
	  }
       }
    }
   else if (start_state.hasKnownFields()) {
      if (FlowMaster.doDebug()) System.err.println("\tDiscard initial fields");
      start_state.discardFields();
      chng = true;
    }

   if (chng && special_data != null && special_data.getDontScan()) chng = false;

   return chng;
}




public void setThread(ValueBase thrd)
{
   start_state.setThread(thrd);
}


private boolean validField(BT_Field f,BT_Class c)
{
   if (f == null) return false;
   while (c != null) {
      if (f.getDeclaringClass() == c) return true;
      c = c.getSuperClass();
    }
   return false;
}



/********************************************************************************/
/*										*/
/*	Methods to handle indirect calls					*/
/*										*/
/********************************************************************************/

public Collection<BT_Method> replaceWith(LinkedList<ValueBase> args,MethodBase caller,int ino)
{
   BT_Method bm = for_method;

   if (special_data == null) return Collections.singletonList(bm);

   String nm0 = special_data.getReplaceName(caller,ino);
   if (nm0 == null) return Collections.singletonList(bm);

   Vector<BT_Method> rslt = new Vector<BT_Method>();
   StringTokenizer tok = new StringTokenizer(nm0);
   while (tok.hasMoreTokens()) {
      String nm = tok.nextToken();
      BT_Method nbm = null;

      int idx = nm.lastIndexOf('.');
      if (idx > 0) {
	 String cls = nm.substring(0,idx);
	 String mthd = nm.substring(idx+1);
	 BT_Class nbc = BT_Class.forName(cls);
	 BT_MethodSignature msgn = bm.getSignature();
	 if (nbc != null) {
	    if (mthd.equals("<init>")) {
	       special_data.getReturnValue(bm,caller,ino);
	       msgn = BT_MethodSignature.create(BT_Class.getVoid(),msgn.types);
	     }
	    try {
	       nbm = nbc.findMethod(mthd,msgn);
	     }
	    catch (NoSuchMethodError _e) {
	       System.err.println("JFLOW: Method " + mthd + " " + bm.getSignature() + " does not exist");
	       System.err.println("JFLOW:    Choices are: ");
	       for (Enumeration<?> en = nbc.getMethods().elements(); en.hasMoreElements(); ) {
		  BT_Method be = (BT_Method) en.nextElement();
		  System.err.println("JFLOW:\t\t" + be.getName() + " " + be.getSignature());
		}
	     }
	  }
       }
      else {
	 ValueBase cv = args.get(0);
	 BT_Class bc = cv.getDataType();
	 if (bc != null) {
	    for (BT_Class xbc = bc; nbm == null && xbc != null; xbc = xbc.getSuperClass()) {
	       try {
		  nbm = xbc.findMethod(nm,"()");
		}
	       catch (NoSuchMethodError _e) { }
	     }
	  }
       }

      if (nbm != null && !nbm.isNative() && !nbm.isAbstract()) {
	 rslt.add(nbm);
       }
    }

   if (rslt.isEmpty()) rslt.add(bm);

   return rslt;
}



public ValueBase fixReplaceArgs(BT_Method bm,LinkedList<ValueBase> args)
{
   ValueBase rslt = null;

   if (bm.isConstructor()) {
      if (special_data != null) {
	 rslt = special_data.getReturnValue(bm,null,-1);
	 args.addFirst(rslt);
       }
    }

   fixArgs(bm,args);

   return rslt;
}



public void addCallbacks(List<ValueBase> args,FlowControl cfc)
{
   if (special_data == null) return;
   Iterator<String> it = special_data.getCallbacks();
   if (it == null) return;

   if (FlowMaster.doDebug()) System.err.println("Check callbacks " + args);
   List<Integer> argnos = special_data.getArgList();
   Vector<ValueBase> nargs = new Vector<ValueBase>();
   for (Integer iv0 : argnos) {
      int i0 = iv0.intValue();
      ValueBase cv = args.get(i0);
      nargs.add(cv);
    }
   ValueBase cv0 = nargs.get(0);
   BT_Class bc = cv0.getDataType();
   if (bc == null) return;

   while (it.hasNext()) {
      String cbn = it.next();
      BT_Method bm = findCallbackMethod(bc,cbn,nargs.size(),true);
      if (bm != null) {
	 Vector<ValueBase> rargs = new Vector<ValueBase>(nargs);
	 fixArgs(bm,rargs);
	 if (FlowMaster.doDebug()) System.err.println("Use callback " + bm + " " + rargs);
	 cfc.handleCallback(bm,rargs,special_data.getCallbackId());
       }
      else {
	 if (FlowMaster.doDebug()) System.err.println("No callback found for " + cbn + " in " + bc);
       }
    }
}




private BT_Method findCallbackMethod(BT_Class bc,String mnm,int asz,boolean intf)
{
   for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
      BT_Method bm = (BT_Method) e.nextElement();
      if (bm.getName().equals(mnm)) {
	 BT_MethodSignature sgn = bm.getSignature();
	 if (bm.isStatic() && sgn.types.size() >= asz) return bm;
	 else if (sgn.types.size()+1 >= asz) return bm;
       }
    }

   BT_Class scls = bc.getSuperClass();
   if  (scls != null) {
      BT_Method bm = findCallbackMethod(scls,mnm,asz,false);
      if (bm != null) return bm;
    }

   if (intf) {
      for (Enumeration<?> e = bc.getParents().elements(); e.hasMoreElements(); ) {
	 BT_Class sc = (BT_Class) e.nextElement();
	 BT_Method bm = findCallbackMethod(sc,mnm,asz,true);
	 if (bm != null) return bm;
       }
    }

   return null;
}




private void fixArgs(BT_Method bm,List<ValueBase> args)
{
   BT_MethodSignature sgn = bm.getSignature();
   BT_ClassVector typs = sgn.types;
   int bct = 0;
   if (bm.isInstanceMethod() || bm.isConstructor()) bct = 1;
   int act = typs.size() + bct;

   while (args.size() > act) {			// remove excess arguments
      args.remove(act);
    }
   if (args.size() < act) {		// add default arguments
      for (int i = args.size(); i < act; ++i) {
	 BT_Class tc = typs.elementAt(i-bct);
	 ValueBase vb = jflow_master.nativeValue(tc);
	 vb = vb.forceNonNull();		// should make this optional
	 args.add(vb);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   BT_MethodSignature sgn = for_method.getSignature();
   StringBuffer buf = new StringBuffer();
   buf.append(for_method.fullName());
   buf.append(sgn.toString());

   if (start_state != null) {
      buf.append(" :: ");
      int act = sgn.types.size();
      int sid = 0;
      if (for_method.isInstanceMethod() || for_method.isConstructor()) sid = -1;
      int idx = 0;
      for (int i = sid; i < act; ++i) {
	 BT_Class bc;
	 if (i < 0) bc = for_method.getDeclaringClass();
	 else bc = sgn.types.elementAt(i);
	 ValueBase cs = start_state.getLocal(idx);
	 if (cs != null) buf.append(cs.toString());
	 else buf.append("null");
	 buf.append(" :: ");
	 if (bc == BT_Class.getDouble() || bc == BT_Class.getLong()) ++idx;
	 ++idx;
       }
    }

   if (!for_method.isVoidMethod() && num_result > 0) {
      buf.append(" => ");
      if (result_set != null) buf.append(result_set.toString());
      else buf.append("null");
    }

   return buf.toString();
}




}	// end of class MethodBase




/* end of MethodBase.java */
