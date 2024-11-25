/********************************************************************************/
/*										*/
/*		ModelMaster.java						*/
/*										*/
/*	Controller class for model generation					*/
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


import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowControl;
import edu.brown.cs.ivy.jflow.JflowEvent;
import edu.brown.cs.ivy.jflow.JflowMaster;
import edu.brown.cs.ivy.jflow.JflowMethod;
import edu.brown.cs.ivy.jflow.JflowModel;
import edu.brown.cs.ivy.jflow.JflowSource;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_CodeAttribute;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_FieldRefIns;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_JumpOffsetIns;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodCallSite;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_Opcodes;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



public class ModelMaster implements JflowModel, JflowConstants, BT_Opcodes
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private JflowMaster	jflow_master;
private JflowControl	use_factory;
private ModelGenerator	main_model;

private boolean 	do_waits;
private boolean 	do_undeclared_exceptions;
private boolean 	do_synch;
private boolean 	do_auto_fields;
private boolean 	do_ignore_exceptions;
private String		start_class;


private boolean 	do_debug;

private Set<BT_Field>	suggest_fields;
private Set<JflowMethod> always_ignore;
private Set<JflowMethod> ignore_methods;
private Set<JflowSource> field_sources;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public ModelMaster(JflowMaster cm,JflowControl f)
{
   jflow_master = cm;
   use_factory = f;
   main_model = null;
   do_undeclared_exceptions = true;
   do_waits = false;
   do_synch = true;
   do_auto_fields = true;
   do_ignore_exceptions = false;
   suggest_fields = null;
   field_sources = new HashSet<JflowSource>();
   always_ignore = null;
   ignore_methods = null;
}



/********************************************************************************/
/*										*/
/*	Methods to create a model						*/
/*										*/
/********************************************************************************/

@Override public JflowModel.Main createModel()
{
   if (always_ignore == null) always_ignore = findIgnorableMethods(true);

   main_model = new ModelGenerator(this);

   suggest_fields = null;
   field_sources = new HashSet<JflowSource>();

   if (do_auto_fields) addRelevantFields();
   ignore_methods = findIgnorableMethods(false);
   boolean fg = main_model.generateAutomata();
   ignore_methods = null;

   if (!fg) {
      System.err.println("JFLOW: Generate failed");
      return null;
    }

   return main_model;
}





/********************************************************************************/
/*										*/
/*	Option methods								*/
/*										*/
/********************************************************************************/

@Override public void setOption(FlowOption opt,boolean fg)
{
   switch (opt) {
      case DO_WAITS :
	 do_waits = fg;
	 break;
      case DO_UNDECLARED_EXCEPTIONS :
	 do_undeclared_exceptions = fg;
	 break;
      case DO_IGNORE_EXCEPTIONS :
	 do_ignore_exceptions = fg;
	 break;
      case DO_SYNCH :
	 do_synch = fg;
	 break;
      case DO_AUTO_FIELDS :
	 jflow_master.setOption(opt,fg);
	 do_auto_fields = fg;
	 break;
      case DO_DEBUG :
      case DO_DEBUG_MODEL :
	 do_debug = fg;
	 break;
      default:
	 break;
    }
}




boolean doWaits()				{ return do_waits; }
boolean doSynch()				{ return do_synch; }
boolean doUndeclaredExceptions()		{ return do_undeclared_exceptions; }
boolean doIgnoreExceptions()			{ return do_ignore_exceptions; }

boolean doDebug()				{ return do_debug; }

@Override public void setStartClass(String cls)		       { start_class = cls; }




/********************************************************************************/
/*										*/
/*	Flow access methods							*/
/*										*/
/********************************************************************************/

boolean isMethodAccessible(BT_Method bm)
{
   return jflow_master.isMethodAccessible(bm);
}



String getCallbackStart(BT_Method bm)
{
   return jflow_master.getCallbackStart(bm);
}



JflowMethod createMetaMethod(BT_Method bm)
{
   return jflow_master.createMetaMethod(bm);
}



Iterable<JflowMethod> getCallbacks(String id)
{
   return jflow_master.getCallbacks(id);
}



Iterable<JflowMethod> getStaticInitializers()
{
   return jflow_master.getStaticInitializers();
}



Collection<JflowMethod> getStartMethods()
{
   Collection<JflowMethod> starts = jflow_master.getStartMethods();

   if (start_class == null) return starts;

   for (Iterator<JflowMethod> it = starts.iterator(); it.hasNext(); ) {
      JflowMethod jm = it.next();
      if (!jm.getMethodClass().getName().equals(start_class)) {
	 it.remove();
       }
    }

   if (doDebug()) System.err.println("Restrict Start Methods: " + starts);

   return starts;
}



int findLineNumber(JflowMethod m,int ino)
{
   return jflow_master.findLineNumber(m,ino);
}



JflowSource getModelFieldSource(BT_Field f,JflowValue obj)
{
   if (!use_factory.isFieldTracked(f)) return null;

   return jflow_master.findFieldSource(f,obj);
}


JflowSource getProgramReturnSource()
{
   return jflow_master.findProgramReturnSource();
}


@Override public JflowMaster getFlowMaster()			{ return jflow_master; }



boolean checkUseCall(JflowMethod from,BT_Method to)
{
   return use_factory.checkUseCall(from,to);
}



/********************************************************************************/
/*										*/
/*	User factory methods							*/
/*										*/
/********************************************************************************/

boolean checkUseMethod(JflowMethod m)
{
   if (ignore_methods != null && ignore_methods.contains(m)) return false;

   return use_factory.checkUseMethod(m);
}


Collection<JflowEvent> getRequiredEvents()
{
   return use_factory.getRequiredEvents();
}


JflowEvent findEvent(JflowMethod cm,BT_Ins ins,boolean start,List<Object> vals)
{
   return use_factory.findEvent(this,cm,ins,start,vals);
}


boolean isFieldTracked(BT_Field bf)
{
   return use_factory.isFieldTracked(bf);
}



/********************************************************************************/
/*										*/
/*	Methods to find methods that can be ignored				*/
/*										*/
/********************************************************************************/

private Set<JflowMethod> findIgnorableMethods(boolean always)
{
   Set<JflowMethod> use = new HashSet<JflowMethod>();
   Set<JflowMethod> unk = new HashSet<JflowMethod>();
   Set<JflowMethod> use1 = new HashSet<JflowMethod>();

   Collection<JflowMethod> starts = getStartMethods();

   for (BT_Class bc : jflow_master.getAllClasses()) {
      for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
	 BT_Method bm = (BT_Method) e.nextElement();
	 boolean cb = (getCallbackStart(bm) != null);
	 for (JflowMethod cfm : jflow_master.findAllMethods(bm)) {
	    if (cb || checkUseMethod(cfm)) {
	       if (starts.contains(cfm)) use1.add(cfm);
	       else if (hasEvent(cfm)) use1.add(cfm);
	       else if (cb) use1.add(cfm);
	       else unk.add(cfm);
	     }
	  }
       }
    }

   unk.addAll(use1);

   boolean chng = true;
   while (chng) {
      chng = false;
      for (JflowMethod cm : use1) noteUse(cm,use,unk);
      for (BT_Class bc : jflow_master.getAllClasses()) {
	 for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
	    BT_Method bm = (BT_Method) e.nextElement();
	    if (jflow_master.canBeReplaced(bm)) {
	       for (JflowMethod cfm : jflow_master.findAllMethods(bm)) {
		  if (!use.contains(cfm)) {
		     Set<JflowMethod> rc = cfm.getAllReplacementCalls();
		     for (JflowMethod cm1 : rc) {
			if (use.contains(cm1)) {
			   noteUse(cfm,use,unk);
			   use.add(cfm);
			   chng = true;
			   break;
			 }
		      }
		   }
		}
	     }
	  }
       }
    }

   return unk;
}



private void noteUse(JflowMethod cm,Set<JflowMethod> use,Set<JflowMethod> unk)
{
   unk.remove(cm);
   if (use.contains(cm)) return;
   use.add(cm);

   for (Enumeration<?> e = cm.getMethod().callSites.elements(); e.hasMoreElements(); ) {
      BT_MethodCallSite mcs = (BT_MethodCallSite) e.nextElement();
      for (JflowMethod cfm : jflow_master.findAllMethods(mcs.from)) {
	 if (checkUseMethod(cfm)) noteUse(cfm,use,unk);
       }
    }

   // need to handle replacement calls here ?? (should be done by addCallSite in flow)

   for (Enumeration<?> e = cm.getMethod().getParents().elements(); e.hasMoreElements(); ) {
      BT_Method pbm = (BT_Method) e.nextElement();
      for (JflowMethod cfm : jflow_master.findAllMethods(pbm)) {
	 if (checkUseMethod(cfm)) noteUse(cfm,use,unk);
       }
    }
}



private boolean hasEvent(JflowMethod cm)
{
   if (findEvent(cm,null,true,null) != null || findEvent(cm,null,false,null) != null) return true;

   if (cm.getCode() == null) return false;

   BT_InsVector iv = cm.getMethod().getCode().ins;
   for (Enumeration<?> e = iv.elements(); e.hasMoreElements(); ) {
      BT_Ins ins = (BT_Ins) e.nextElement();
      if (findEvent(cm,ins,true,null) != null || findEvent(cm,ins,false,null) != null) return true;
      switch (ins.opcode) {
	 case opc_putstatic :
	 case opc_putfield :
	 case opc_getstatic :
	 case opc_getfield :
	    BT_FieldRefIns frins = (BT_FieldRefIns) ins;
	    if (isFieldTracked(frins.getFieldTarget())) return true;
	    break;
       }
    }

   return false;
}




/********************************************************************************/
/*										*/
/*	Methods to add relevant fields						*/
/*										*/
/********************************************************************************/

private void addRelevantFields()
{
   for (BT_Class bc : jflow_master.getAllClasses()) {
      for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
	 BT_Method bm = (BT_Method) e.nextElement();
	 for (JflowMethod cfm : jflow_master.findAllMethods(bm)) {
	    if (checkUseMethod(cfm)) {
	       if (hasEvent(cfm)) findRelevantFields(cfm);
	     }
	  }
       }
    }
}



private void findRelevantFields(JflowMethod cm)
{
   BT_InsVector iv = cm.getMethod().getCode().ins;
   BT_Field chkfld = null;

   int ct = iv.size();
   for (int idx = 0; idx < ct; ++idx) {
      BT_Ins ins = iv.elementAt(idx);
      switch (ins.opcode) {
	 case opc_getstatic :
	 case opc_getfield :
	    BT_FieldRefIns frins = (BT_FieldRefIns) ins;
	    if (!isFieldTracked(frins.getFieldTarget()))
	       chkfld = frins.getFieldTarget();
	    break;
	 case opc_ifeq :
	 case opc_ifne :
	 case opc_ifnull :
	 case opc_ifnonnull :
	    if (chkfld != null) {
	       testField(chkfld,cm,idx);
	     }
	    chkfld = null;
	    break;
	 default :
	    chkfld = null;
	    break;
       }
    }
}



private void testField(BT_Field fld,JflowMethod cm,int idx)
{
   BT_InsVector iv = cm.getMethod().getCode().ins;
   BT_JumpOffsetIns joins = (BT_JumpOffsetIns) iv.elementAt(idx);

   boolean evt = false;
   int i = idx+2;
   while (i < iv.size()) {
      BT_Ins ins = iv.elementAt(i);
      if (ins.opcode == OPC_BBLOCK) break;
      if (findEvent(cm,ins,true,null) != null || findEvent(cm,ins,false,null) != null) evt = true;
      ++i;
    }

   int bidx = iv.indexOf(joins.target);
   if (i != bidx) {
      for (i = bidx+1; i < iv.size(); ++i) {
	 BT_Ins ins = iv.elementAt(i);
	 if (ins.opcode == OPC_BBLOCK) break;
	 if (findEvent(cm,ins,true,null) != null || findEvent(cm,ins,false,null) != null) evt = true;
       }
    }

   if (evt) {
      if (suggest_fields == null) suggest_fields = new HashSet<BT_Field>();
      if (!suggest_fields.contains(fld)) {
	 System.err.println("JFLOW: Suggest using field " + fld.fullName() + " from " +
			       cm.getMethodName() + " @ " + idx);
	 suggest_fields.add(fld);
	 jflow_master.addFieldChecks(fld);
       }
    }
}




boolean isFieldSourceRelevant(JflowSource cs)
{
   BT_Field bf = cs.getFieldSource();
   if (bf == null) return false;

   if (field_sources != null && field_sources.contains(cs)) return true;

   if (suggest_fields != null && suggest_fields.contains(bf)) return true;

   if (!isFieldTracked(bf)) return false;

   if (field_sources != null) field_sources.add(cs);

   return true;
}




/********************************************************************************/
/*										*/
/*	Handle start set creation						*/
/*										*/
/********************************************************************************/

ValueState getStartState(JflowMethod jm)
{
   int mxl = 0;
   BT_Method bm = jm.getMethod();
   BT_CodeAttribute code = bm.getCode();
   BT_MethodSignature sgn = bm.getSignature();
   if (code != null) mxl = code.maxLocals;
   else {
      mxl = 1;
      for (int i = 0; i < sgn.types.size(); ++i) {
	 BT_Class bc = sgn.types.elementAt(i);
	 mxl += bc.getSizeForLocal();
       }
    }

   ValueState vs = new ValueState(mxl);

   int idx = 0;
   for (JflowValue jv : jm.getParameterValues()) {
      ModelValue mv = ModelValue.createValue(jv);
      vs.setLocal(idx++,mv);
      if (jv.isCategory2()) ++idx;
    }

   return vs;
}



}	// end of class ModelMaster




/* end of ModelMaster.java */
