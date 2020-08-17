/********************************************************************************/
/*										*/
/*		ModelBuilder.java						*/
/*										*/
/*	Methods for managing event generation for a single method		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/model/ModelBuilder.java,v 1.8 2018/08/02 15:10:19 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ModelBuilder.java,v $
 * Revision 1.8  2018/08/02 15:10:19  spr
 * Fix imports.
 *
 * Revision 1.7  2018/02/21 16:18:50  spr
 * Formatting
 *
 * Revision 1.6  2007-08-10 02:10:45  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.5  2007-05-04 02:00:03  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.4  2006-12-01 03:22:49  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.3  2006/07/10 14:52:19  spr
 * Code cleanup.
 *
 * Revision 1.2  2006/07/03 18:15:36  spr
 * Fixup reporting values for calls.
 *
 * Revision 1.1  2006/06/21 02:18:37  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.model;

import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowEvent;
import edu.brown.cs.ivy.jflow.JflowMethod;
import edu.brown.cs.ivy.jflow.JflowSource;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_ANewArrayIns;
import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_ClassRefIns;
import com.ibm.jikesbt.BT_ClassVector;
import com.ibm.jikesbt.BT_CodeAttribute;
import com.ibm.jikesbt.BT_ConstantClassIns;
import com.ibm.jikesbt.BT_ConstantDoubleIns;
import com.ibm.jikesbt.BT_ConstantFloatIns;
import com.ibm.jikesbt.BT_ConstantIntegerIns;
import com.ibm.jikesbt.BT_ConstantLongIns;
import com.ibm.jikesbt.BT_ConstantStringIns;
import com.ibm.jikesbt.BT_ExceptionTableEntry;
import com.ibm.jikesbt.BT_ExceptionTableEntryVector;
import com.ibm.jikesbt.BT_FieldRefIns;
import com.ibm.jikesbt.BT_IIncIns;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_JumpOffsetIns;
import com.ibm.jikesbt.BT_LoadLocalIns;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodRefIns;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_MultiANewArrayIns;
import com.ibm.jikesbt.BT_NewArrayIns;
import com.ibm.jikesbt.BT_Opcodes;
import com.ibm.jikesbt.BT_StoreLocalIns;
import com.ibm.jikesbt.BT_SwitchIns;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;



class ModelBuilder implements JflowConstants, BT_Opcodes
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ModelMaster model_master;
private ModelGenerator event_gen;
private ModelState enter_node;
private ModelState exit_node;
private JflowMethod for_method;
private BT_InsVector ins_vector;
private StateMap state_map;
private LinkedList<InsState> work_queue;
private TestBranch branch_result;
private boolean local_values;
private boolean do_synch;
private boolean do_waits;
private List<Object> event_values;

private static final int	JFLOW_BRANCH_SIZE = 5;
private static final int	JFLOW_MAX_VALUE_SIZE = 300;

private static BT_Class thread_class = null;
private static BT_Class system_class = null;

private static boolean check_values = true;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelBuilder(ModelMaster mm,ModelGenerator eg,JflowMethod bm)
{
   model_master = mm;
   event_gen = eg;
   for_method = bm;
   ins_vector = for_method.getCodeVector();
   state_map = null;
   enter_node = null;
   exit_node = null;
   local_values = check_values;
   do_synch = eg.doSynch();
   do_waits = do_synch && model_master.doWaits();
   work_queue = null;
   branch_result = TestBranch.ANY;

   if (ins_vector != null && ins_vector.size() > JFLOW_MAX_VALUE_SIZE) {
      local_values = false;
    }

   if (thread_class == null) thread_class = BT_Class.forName("java.lang.Thread");
   if (system_class == null) system_class = BT_Class.forName("java.lang.System");
}





/********************************************************************************/
/*										*/
/*	Top level method for building the automata				*/
/*										*/
/********************************************************************************/

ModelMethod createAutomata()
{
   enter_node = ModelState.createSimpleState(for_method,model_master.findLineNumber(for_method,JFLOW_START_LINE));
   exit_node = ModelState.createSimpleState(for_method,model_master.findLineNumber(for_method,JFLOW_END_LINE));
   exit_node.setFinal();
   ModelState oexit = exit_node;

   if (model_master.doDebug()) {
      System.err.println("Add automata for " + for_method.getMethodName() + ": " +
			    enter_node.getName() + " -> " + exit_node.getName());
    }

   ModelState sst = enter_node;

   if (for_method.getMethod().isSynchronized()) {
      JflowValue cv = for_method.getThisValue();
      if (cv != null) {
	 sst = addSyncState(0,cv,sst,false);
       }
    }

   JflowEvent evt = model_master.findEvent(for_method,null,true,null);
   sst = addEventState(0,sst,evt);
   Stack<Integer> jsrs = new Stack<>();

   if (event_gen.isStartMethod(for_method)) {
      for (JflowMethod cfm : model_master.getStaticInitializers()) {
	 if (event_gen.isMethodUsed(cfm)) {
	    if (model_master.doDebug())
	       System.err.println("Add static initilizer call " + cfm.getMethodName());
	    ModelState st2 = ModelState.createCallState(for_method,0,cfm,false,null);
	    sst.addTransition(st2);
	    if (model_master.doDebug())
	       System.err.println("Transition (Start) " + sst.getName() + " -> " + st2.getName());
	    sst = st2;
	    event_gen.addMethodTodo(cfm);
	  }
       }
    }

   state_map = new StateMap();
   work_queue = new LinkedList<>();

   if (ins_vector != null) addToQueue(0,sst,jsrs,model_master.getStartState(for_method));

   while (!work_queue.isEmpty()) {
      InsState ist = work_queue.removeFirst();
      processInstruction(ist);
    }

   state_map = null;

   if (for_method.getMethod().isSynchronized()) {
      JflowValue cv = for_method.getThisValue();
      if (cv != null) {
	 exit_node = addSyncState(0,cv,exit_node,true);
       }
    }

   evt = model_master.findEvent(for_method,null,false,null);
   exit_node = addEventState(0,exit_node,evt);

   if (exit_node != oexit) {
      ModelState nexit = ModelState.createSimpleState(for_method,oexit.getLineNumber());
      nexit.setFinal();
      exit_node.addTransition(nexit);
      if (model_master.doDebug())
	 System.err.println("Transition (Exit1) " + exit_node.getName() + " -> " + nexit.getName());
      exit_node = nexit;
    }

   Set<?> nds = event_gen.simplify(enter_node);
   for (Iterator<?> it = nds.iterator(); it.hasNext(); ) {
      ModelState st = (ModelState) it.next();
      if (st.hasNoTransitions() && st.getId() >= enter_node.getId() && st != exit_node) {
	 st.addTransition(exit_node);
	 if (model_master.doDebug())
	    System.err.println("Transition (Final) " + st.getName() + " -> " + exit_node.getName());
       }
    }

   if (model_master.doDebug()) {
      System.err.println("Done add for " + for_method.getMethodName() + " " + enter_node.getName() + " " +
			    enter_node.onlyConnection(exit_node) + " " + nds.size());
    }

   if (enter_node.onlyConnection(exit_node)) {
      return null;
    }

   return new ModelMethod(for_method,enter_node,exit_node);
}




/********************************************************************************/
/*										*/
/*	Methods for processing instructions for building the automata		*/
/*										*/
/********************************************************************************/

void processInstruction(InsState ist)
{
   int ino = ist.getInstructionNumber();
   ModelState cst = ist.getCurrentState();
   Stack<Integer> jsrs = ist.getJsrStack();
   ValueState ovals = ist.getValueState();
   BT_Ins ins = ins_vector.elementAt(ino);
   int nino = ino+1;

   if (model_master.doDebug()) {
      System.err.println("Work on " + ino + " " + ins + " " + cst.getName() + " @" + getLine(ino));
    }

   branch_result = TestBranch.ANY;
   ValueState vals = maintainValues(ins,ovals);

   JflowEvent evt = model_master.findEvent(for_method,ins,true,event_values);
   cst = addEventState(ino,cst,evt);
   ModelState nst = cst;

   ModelValue cv;
   JflowValue jv;
   BT_SwitchIns sins;

   switch (ins.opcode) {
      case opc_bblock : 			// basic block
	 if (for_method.getIgnoreBlock(ins))
	    return;
	 nst = ModelState.createSimpleState(for_method,getLine(ino));
	 state_map.put(ins,jsrs,nst,vals);
	 break;
      case opc_monitorenter :
	 jv = for_method.getAssociation(AssociationType.SYNC,ins);
	 nst = addSyncState(ino,jv,cst,false);
	 cst = nst;
	 break;
      case opc_monitorexit :
	 jv = for_method.getAssociation(AssociationType.SYNC,ins);
	 nst = addSyncState(ino,jv,cst,true);
	 cst = nst;
	 break;
      case opc_areturn : case opc_dreturn : case opc_freturn : case opc_ireturn :
      case opc_lreturn :
	 if (ovals != null) {
	    cv = ovals.popStack();
	    nst = ModelState.createReturnState(for_method,getLine(ino),cv);
	    cst.addTransition(nst);
	    if (model_master.doDebug())
	       System.err.println("Transition (Return) " + cst.getName() + " -> " + nst.getName());
	    cst = nst;
	  }
	 nst = exit_node;
	 nino = -1;
	 break;
      case opc_return :
	 nst = exit_node;
	 nino = -1;
	 break;
      case opc_athrow :
	 nino = -1;
	 handleThrow(ino,cst,null,jsrs,vals);
	 break;
      case opc_goto : case opc_goto_w :
	 nino = ins_vector.indexOf(((BT_JumpOffsetIns) ins).target);
	 break;
      case opc_lookupswitch : case opc_tableswitch :
	 sins = (BT_SwitchIns) ins;
	 for (int i = 0; i < sins.targets.length; ++i) {
	    addToQueue(sins.targets[i],cst,jsrs,vals);
	  }
	 if (sins.def != null) addToQueue(sins.def,cst,jsrs,vals);
	 break;
      case opc_invokeinterface :
      case opc_invokespecial :
      case opc_invokestatic :
      case opc_invokevirtual :
	 BT_Method bm = ins.getMethodTarget();
	 if (!model_master.checkUseCall(for_method,bm)) break;
	 cst = handleWait(ino,bm,cst);
	 nst = cst;
	 int ctr = for_method.getCallCount(ins);
	 if (ctr > JFLOW_BRANCH_SIZE) {
	    JflowMethod cfm = event_gen.buildMetaCall(for_method,ins,bm);
	    if (cfm != null) {
	       nst = handleCall(ino,cfm,cst,null,jsrs,vals,bm);
	       cst = handleCallback(ino,bm,nst);
	       cst = nst;
	       break;
	     }
	  }
	 for (JflowMethod cfm : for_method.getAllCalls(ins)) {
	    if (nst == cst) nst = null;
	    nst = handleCall(ino,cfm,cst,nst,jsrs,vals,bm);
	  }
	 if (ctr == 0) {
	    if (bm.declaredExceptions().size() > 0 && !model_master.doIgnoreExceptions()) {
	       handleThrow(ino,nst,bm.declaredExceptions(),jsrs,vals);
	     }
	  }
	 cst = handleCallback(ino,bm,nst);
	 cst = nst;
	 break;
      case opc_if_acmpeq : case opc_if_acmpne :
      case opc_if_icmpeq : case opc_if_icmpne :
      case opc_if_icmplt : case opc_if_icmpge : case opc_if_icmpgt : case opc_if_icmple :
	 if (branch_result != TestBranch.NEVER) {
	    addToQueue(((BT_JumpOffsetIns) ins).target,cst,jsrs,vals);
	  }
	 if (branch_result == TestBranch.ALWAYS) nino = -1;
	 break;
      case opc_ifeq : case opc_ifne : case opc_iflt : case opc_ifge : case opc_ifgt : case opc_ifle :
      case opc_ifnonnull : case opc_ifnull :
	 cst = handleIf(ino,ins,cst,jsrs,vals);
	 nst = cst;
	 if (nst == null) nino = -1;
	 break;
      case opc_putstatic :
      case opc_putfield :
	 ModelValue fldval = null;
	 if (ovals != null) fldval = ovals.popStack();
	 nst = handlePut(ino,ins,cst,jsrs,vals,fldval);
	 break;
      case opc_jsr : case opc_jsr_w :
	 BT_JumpOffsetIns joins = (BT_JumpOffsetIns) ins;
	 jsrs = cloneStack(jsrs);
	 jsrs.push(Integer.valueOf(nino));
	 nino = ins_vector.indexOf(joins.target);
	 break;
      case opc_ret :
	 jsrs = cloneStack(jsrs);
	 Integer ivl = jsrs.pop();
	 nino = ivl.intValue();
	 break;
    }

   if (nst != null && nst == cst) {
      cst.extendTo(getLine(ino));
      if (model_master.doDebug()) System.err.println("Extend current block to " + getLine(ino));
    }

   if (nst == exit_node) {
      evt = model_master.findEvent(for_method,ins,false,event_values);
      cst = addEventState(ino,cst,evt);
    }

   if (nst != cst && nst != null) {
      cst.addTransition(nst);
      if (model_master.doDebug())
	 System.err.println("Transition (next) " + cst.getName() + " -> " + nst.getName());
      cst = nst;
    }

   if (nst != null && nst != exit_node) {
      evt = model_master.findEvent(for_method,ins,false,event_values);
      nst = addEventState(ino,cst,evt);
    }

   if (nino >= 0 && nst != null) addToQueue(nino,nst,jsrs,vals);
}



private Stack<Integer> cloneStack(Stack<Integer> j)
{
   if (j == null) return null;

   Stack<Integer> nj = new Stack<>();
   nj.addAll(j);

   if (model_master.doDebug() && !j.equals(nj))
      System.err.println("ERROR cloning stack");

   return nj;
}



/********************************************************************************/
/*										*/
/*	Methods for handling value maintenance					*/
/*										*/
/********************************************************************************/

private ValueState maintainValues(BT_Ins ins,ValueState vals)
{
   BT_MethodRefIns mrins;
   BT_MultiANewArrayIns mains;
   BT_LoadLocalIns llins;
   BT_StoreLocalIns slins;
   BT_FieldRefIns frins;
   BT_ConstantIntegerIns ciins;
   BT_ConstantLongIns clins;
   BT_ConstantDoubleIns cdins;
   BT_ConstantFloatIns cfins;
   BT_ConstantStringIns csins;
   BT_IIncIns iiins;
   BT_ClassRefIns crins;
   BT_NewArrayIns nains;
   BT_ANewArrayIns anains;
   BT_Class cls;
   ModelValue s0,s1,s2;

   branch_result = TestBranch.ANY;
   event_values = null;

   if (vals == null || !local_values) return null;

   ValueState st1 = vals.cloneState();

   switch (ins.opcode) {
      case opc_aaload :
	 st1.popStack();
	 s0 = st1.popStack();
	 cls = s0.getDataType();
	 if (cls.getArrayDimensions() > 1) {
	    String sv = cls.getName();
	    sv = sv.substring(0,sv.length()-2);
	    cls = BT_Class.forName(sv);
	  }
	 else if (cls.getArrayDimensions() == 1) cls = cls.arrayType;
	 st1.pushStack(ModelValue.anyObject(cls));
	 break;
      case opc_aastore :
	 st1.popStack();
	 st1.popStack();
	 st1.popStack();
	 break;
      case opc_anewarray :
	 anains = (BT_ANewArrayIns) ins;
	 st1.popStack();
	 st1.pushStack(ModelValue.anyNewObject(anains.getClassTarget()));
	 break;
      case opc_multianewarray :
	 mains = (BT_MultiANewArrayIns) ins;
	 int i0 = -mains.getStackDiff() + 1;
	 for (int i = 0; i < i0; ++i) st1.popStack();
	 cls = mains.getClassTarget();
	 st1.pushStack(ModelValue.anyNewObject(cls));
	 break;
      case opc_newarray :
	 nains = (BT_NewArrayIns) ins;
	 st1.popStack();
	 st1.pushStack(ModelValue.anyNewObject(nains.getClassTarget()));
	 break;
      case opc_arraylength :
	 st1.popStack();
	 st1.pushStack(ModelValue.anyValue(BT_Class.getInt()));
	 break;
      case opc_new :
	 crins = (BT_ClassRefIns) ins;
	 st1.pushStack(ModelValue.anyNewObject(crins.getClassTarget()));
	 break;
      case opc_aconst_null :
	 st1.pushStack(ModelValue.nullValue());
	 break;
      case opc_aload : case opc_aload_0 : case opc_aload_1 : case opc_aload_2 : case opc_aload_3 :
      case opc_dload :
      case opc_dload_0 : case opc_dload_1 : case opc_dload_2 : case opc_dload_3 :
      case opc_fload :
      case opc_fload_0 : case opc_fload_1 : case opc_fload_2 : case opc_fload_3 :
      case opc_iload :
      case opc_iload_0 : case opc_iload_1 : case opc_iload_2 : case opc_iload_3 :
      case opc_lload :
      case opc_lload_0 : case opc_lload_1 : case opc_lload_2 : case opc_lload_3 :
	 llins = (BT_LoadLocalIns) ins;
	 st1.pushStack(st1.getLocal(llins.localNr));
	 break;
      case opc_areturn :
	 s0 = st1.popStack();
	 break;
      case opc_astore : case opc_astore_0 : case opc_astore_1 : case opc_astore_2 : case opc_astore_3 :
	 slins = (BT_StoreLocalIns) ins;
	 s0 = st1.popStack();
	 st1.setLocal(slins.localNr,s0);
	 break;
      case opc_athrow :
	 st1.popStack();
	 break;
      case opc_checkcast :
	 crins = (BT_ClassRefIns) ins;
	 s0 = st1.popStack();
	 s1 = ModelValue.copyValue(crins.getClassTarget(),s0);
	 st1.pushStack(s1);
	 break;
      case opc_dup :
	 st1.handleDup(false,0);
	 break;
      case opc_dup_x1 :
	 st1.handleDup(false,1);
	 break;
      case opc_dup_x2 :
	 st1.handleDup(false,2);
	 break;
      case opc_dup2 :
	 st1.handleDup(true,0);
	 break;
      case opc_dup2_x1 :
	 st1.handleDup(true,1);
	 break;
      case opc_dup2_x2 :
	 st1.handleDup(true,2);
	 break;
      case opc_monitorenter : case opc_monitorexit :
	 s0 = st1.popStack();
	 break;
      case opc_pop :
	 st1.popStack();
	 break;
      case opc_pop2 :
	 if (!st1.isCategory2()) st1.popStack();
	 st1.popStack();
	 break;
      case opc_swap :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 st1.pushStack(s0);
	 st1.pushStack(s1);
	 break;
      case opc_getfield :
	 frins = (BT_FieldRefIns) ins;
	 st1.popStack();
	 st1.pushStack(ModelValue.anyValue(frins.getFieldTarget().type));
	 break;
      case opc_getstatic :
	 frins = (BT_FieldRefIns) ins;
	 st1.pushStack(ModelValue.anyValue(frins.getFieldTarget().type));
	 break;
      case opc_putfield :
	 s0 = st1.popStack();
	 addEventValue(s0);
	 st1.popStack();
	 break;
      case opc_putstatic :
	 s0 = st1.popStack();
	 addEventValue(s0);
	 break;
      case opc_instanceof :
	 s0 = st1.popStack();
	 s0 = s0.performOperation(BT_Class.getBoolean(),s0,ins.opcode);
	 st1.pushStack(s0);
	 break;
      case opc_baload :
	 st1.popStack();
	 st1.popStack();
	 st1.pushStack(ModelValue.anyValue(BT_Class.getByte()));
	 break;
      case opc_caload :
	 st1.popStack();
	 st1.popStack();
	 st1.pushStack(ModelValue.anyValue(BT_Class.getChar()));
	 break;
      case opc_daload :
      case opc_dadd : case opc_ddiv : case opc_dmul : case opc_drem : case opc_dsub :
	 st1.popStack();
	 st1.popStack();
	 st1.pushStack(ModelValue.anyValue(BT_Class.getDouble()));
	 break;
      case opc_faload :
	 st1.popStack();
	 st1.popStack();
	 st1.pushStack(ModelValue.anyValue(BT_Class.getFloat()));
	 break;
      case opc_fadd : case opc_fdiv : case opc_fmul : case opc_frem : case opc_fsub :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = s1.performOperation(BT_Class.getFloat(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_iaload :
	 st1.popStack();
	 st1.popStack();
	 st1.pushStack(ModelValue.anyValue(BT_Class.getInt()));
	 break;
      case opc_iadd : case opc_idiv : case opc_imul : case opc_irem : case opc_isub :
      case opc_iand : case opc_ior : case opc_ixor :
      case opc_ishl : case opc_ishr : case opc_iushr :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = s1.performOperation(BT_Class.getInt(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_laload :
	 st1.popStack();
	 st1.popStack();
	 st1.pushStack(ModelValue.anyValue(BT_Class.getLong()));
	 break;
      case opc_ladd : case opc_ldiv : case opc_lmul : case opc_lrem : case opc_lsub :
      case opc_land : case opc_lor : case opc_lxor :
      case opc_lshl : case opc_lshr : case opc_lushr :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = s1.performOperation(BT_Class.getLong(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_saload :
	 st1.popStack();
	 st1.popStack();
	 st1.pushStack(ModelValue.anyValue(BT_Class.getShort()));
	 break;
      case opc_dcmpg : case opc_dcmpl : case opc_fcmpg : case opc_fcmpl :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = s1.performOperation(BT_Class.getInt(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_bastore : case opc_castore : case opc_dastore : case opc_fastore :
      case opc_iastore : case opc_lastore : case opc_sastore :
	 st1.popStack();
	 st1.popStack();
	 st1.popStack();
	 break;
      case opc_bipush :
      case opc_sipush :
	 ciins = (BT_ConstantIntegerIns) ins;
	 s0 = ModelValue.intValue(BT_Class.getInt(),ciins.value);
	 st1.pushStack(s0);
	 break;
      case opc_dconst_0 : case opc_dconst_1 :
	 cdins = (BT_ConstantDoubleIns) ins;
	 s0 = ModelValue.floatValue(BT_Class.getDouble(),cdins.value);
	 st1.pushStack(s0);
	 break;
      case opc_fconst_0 : case opc_fconst_1 : case opc_fconst_2 :
	 cfins = (BT_ConstantFloatIns) ins;
	 s0 = ModelValue.floatValue(BT_Class.getFloat(),cfins.value);
	 st1.pushStack(s0);
	 break;
      case opc_iconst_0 : case opc_iconst_1 :
      case opc_iconst_2 : case opc_iconst_3 : case opc_iconst_4 : case opc_iconst_5 :
      case opc_iconst_m1 :
	 ciins = (BT_ConstantIntegerIns) ins;
	 s0 = ModelValue.intValue(BT_Class.getInt(),ciins.value);
	 st1.pushStack(s0);
	 break;
      case opc_lconst_0 : case opc_lconst_1 :
	 clins = (BT_ConstantLongIns) ins;
	 s0 = ModelValue.intValue(BT_Class.getLong(),clins.value);
	 st1.pushStack(s0);
	 break;
      case opc_ldc :
      case opc_ldc_w :
	 if (ins.isLoadConstantStringIns()) {
	    csins = (BT_ConstantStringIns) ins;
	    s0 = ModelValue.constantString(csins.value);
	  }
	 else if (ins instanceof BT_ConstantFloatIns) {
	    cfins = (BT_ConstantFloatIns) ins;
	    s0 = ModelValue.floatValue(BT_Class.getFloat(),cfins.value);
	  }
	 else if (ins instanceof BT_ConstantClassIns) {
	    s0 = ModelValue.anyNewObject(BT_Class.forName("java.lang.Class"));
	  }
	 else {
	    ciins = (BT_ConstantIntegerIns) ins;
	    s0 = ModelValue.intValue(BT_Class.getInt(),ciins.value);
	  }
	 st1.pushStack(s0);
	 break;
      case opc_ldc2_w :
	 if (ins instanceof BT_ConstantDoubleIns) {
	    cdins = (BT_ConstantDoubleIns) ins;
	    s0 = ModelValue.floatValue(BT_Class.getDouble(),cdins.value);
	  }
	 else {
	    clins = (BT_ConstantLongIns) ins;
	    s0 = ModelValue.intValue(BT_Class.getLong(),clins.value);
	  }
	 st1.pushStack(s0);
	 break;
      case opc_d2f :
      case opc_fneg :
      case opc_i2f :
      case opc_l2f :
	 s0 = st1.popStack();
	 s2 = s0.performOperation(BT_Class.getFloat(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_d2i :
      case opc_f2i :
      case opc_l2i :
      case opc_ineg :
	 s0 = st1.popStack();
	 s2 = s0.performOperation(BT_Class.getInt(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_iinc :
	 iiins = (BT_IIncIns) ins;
	 s0 = st1.getLocal(iiins.localNr);
	 s1 = ModelValue.intValue(BT_Class.getInt(),iiins.constant);
	 s2 = s0.performOperation(BT_Class.getInt(),s1,ins.opcode);
	 st1.setLocal(iiins.localNr,s2);
	 // st1.setLocal(iiins.localNr,ModelValue.anyValue(BT_Class.getInt()));
	 break;
      case opc_d2l :
      case opc_f2l :
      case opc_i2l :
      case opc_lneg :
	 s0 = st1.popStack();
	 st1.pushStack(s0.performOperation(BT_Class.getLong(),s0,ins.opcode));
	 break;
      case opc_lcmp :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = s1.performOperation(BT_Class.getInt(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_dneg :
      case opc_f2d :
      case opc_i2d :
      case opc_l2d :
	 s0 = st1.popStack();
	 st1.pushStack(s0.performOperation(BT_Class.getDouble(),s0,ins.opcode));
	 break;
      case opc_int2byte :
	 s0 = st1.popStack();
	 st1.pushStack(s0.performOperation(BT_Class.getByte(),s0,ins.opcode));
	 break;
      case opc_int2char :
	 s0 = st1.popStack();
	 st1.pushStack(s0.performOperation(BT_Class.getChar(),s0,ins.opcode));
	 break;
      case opc_int2short :
	 s0 = st1.popStack();
	 st1.pushStack(s0.performOperation(BT_Class.getShort(),s0,ins.opcode));
	 break;
      case opc_nop :
	 break;
      case opc_dreturn :
      case opc_freturn :
      case opc_ireturn :
      case opc_lreturn :
	 s0 = st1.popStack();
	 break;
      case opc_dstore : case opc_dstore_0 : case opc_dstore_1 : case opc_dstore_2 : case opc_dstore_3 :
	 slins = (BT_StoreLocalIns) ins;
	 st1.setLocal(slins.localNr,st1.popStack());
	 st1.setLocal(slins.localNr+1,null);
	 break;
      case opc_fstore : case opc_fstore_0 : case opc_fstore_1 : case opc_fstore_2 : case opc_fstore_3 :
      case opc_istore : case opc_istore_0 : case opc_istore_1 : case opc_istore_2 : case opc_istore_3 :
      case opc_lstore : case opc_lstore_0 : case opc_lstore_1 : case opc_lstore_2 : case opc_lstore_3 :
	 slins = (BT_StoreLocalIns) ins;
	 s0 = st1.popStack();
	 st1.setLocal(slins.localNr,s0);
	 break;
      case opc_goto : case opc_goto_w :
	 break;
      case opc_if_acmpeq : case opc_if_acmpne :
      case opc_if_icmpeq : case opc_if_icmpne :
      case opc_if_icmplt : case opc_if_icmpge : case opc_if_icmpgt : case opc_if_icmple :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 branch_result = s1.branchTest(s0,ins.opcode);
	 break;
      case opc_ifeq : case opc_ifne : case opc_iflt : case opc_ifge : case opc_ifgt : case opc_ifle :
      case opc_ifnonnull : case opc_ifnull :
	 s0 = st1.popStack();
	 branch_result = s0.branchTest(s0,ins.opcode);
	 break;
      case opc_lookupswitch :
      case opc_tableswitch :
	 st1.popStack();
	 break;
      case opc_jsr : case opc_jsr_w :
	 st1.pushStack(ModelValue.badValue());
	 break;
      case opc_ret :
	 break;
      case opc_invokeinterface :
      case opc_invokespecial :
      case opc_invokestatic :
      case opc_invokevirtual :
	 mrins = (BT_MethodRefIns) ins;
	 BT_Method bm = mrins.getTarget();
	 BT_MethodSignature sgn = bm.getSignature();
	 for (int i = 0; i < sgn.types.size(); ++i) {
	    s0 = st1.popStack();
	    prefixEventValue(s0);
	  }
	 if (bm.isInstanceMethod() || bm.isConstructor()) {
	    s0 = st1.popStack();
	    prefixEventValue(s0);
	  }
	 if (!bm.isVoidMethod()) {
	    st1.pushStack(ModelValue.anyValue(sgn.returnType));
	  }
	 break;
      case opc_return :
	 break;
      case opc_bblock : 		// basic block
	 break;
    }

   return st1;
}





/********************************************************************************/
/*										*/
/*	Methods for handling complex instructions				*/
/*										*/
/********************************************************************************/

private ModelState handleCall(int ino,JflowMethod fm,ModelState st0,
				     ModelState st1,Stack<Integer> jsrs,ValueState vals,
				     BT_Method bm)
{
   ModelState st2 = null;

   if (!event_gen.isMethodIgnored(fm)) {
      BT_Ins ins = ins_vector.elementAt(ino);
      Set<?> rc = fm.getReplacementCalls(ins);
      if (rc != null) {
	 if (st1 == null) st1 = st0;
	 return st1;
       }

      if (model_master.doDebug())
	 System.err.println("Call " + fm.getMethodName() + " " + fm.getMethodSignature());

      if (st1 != null && st1 != st0 && st1.getCall() != null) {
	 ModelState stx = ModelState.createSimpleState(for_method,getLine(ino));
	 st1.addTransition(stx);
	 if (model_master.doDebug()) System.err.println("Transition (Call) " + st1.getName() + " -> " + stx.getName());
	 st1 = stx;
       }

      st2 = ModelState.createCallState(for_method,getLine(ino),fm,fm.getIsAsync(bm),ins);
      if (model_master.doDebug()) System.err.println("Transition (Call1) " + st0.getName() + " -> " + st2.getName());

      event_gen.addMethodTodo(fm);

      st0.addTransition(st2);
      if (st1 == null || st1 == st0) st1 = st2;
      else {
	 st2.addTransition(st1);
	 if (model_master.doDebug()) System.err.println("Transition (Call2) " + st2.getName() + " -> " + st1.getName());
       }
    }

   if (st1 == null) st1 = st0;

   if (fm.getMethod().declaredExceptions().size() > 0 && !model_master.doIgnoreExceptions()) {
      handleThrow(ino,st1,fm.getMethod().declaredExceptions(),jsrs,vals);
    }
   else {
      JflowValue excv = fm.getExceptionSet();
      if (excv != null && model_master.doUndeclaredExceptions()) {
	 if (model_master.doDebug()) {
	    System.err.println("Undeclared exception occurs: " + excv);
	  }
	 BT_ClassVector cvec = new BT_ClassVector();
	 cvec.addElement(excv.getDataType());
	 handleThrow(ino,st1,cvec,jsrs,vals);
       }
    }

   return st1;
}




private ModelState handleWait(int ino,BT_Method bm,ModelState st0)
{
   ModelState st1 = st0;

   if (bm.getDeclaringClass() == BT_Class.findJavaLangObject() && do_synch && do_waits) {
      BT_Ins ins = ins_vector.elementAt(ino);
      String nm = bm.getName();
      if (nm.equals("wait")) {
	 int nargs = bm.getSignature().types.size();
	 JflowValue cv = for_method.getAssociation(AssociationType.SYNC,ins);
	 if (cv == null) return st1;
	 ModelWaitType wt = (nargs == 0 ? ModelWaitType.WAIT : ModelWaitType.WAIT_TIMED);
	 st1 = ModelState.createWaitState(for_method,getLine(ino),cv,wt);
	 if (model_master.doDebug()) System.err.println("Transition (Wait) " + st0.getName() + " -> " + st1.getName());
	 st0.addTransition(st1);
       }
      else if (nm.startsWith("notify")) {
	 JflowValue cv = for_method.getAssociation(AssociationType.SYNC,ins);
	 if (cv == null) return st1;
	 ModelWaitType wt = (nm.equals("notifyAll") ? ModelWaitType.NOTIFY_ALL : ModelWaitType.NOTIFY);
	 st1 = ModelState.createWaitState(for_method,getLine(ino),cv,wt);
	 if (model_master.doDebug()) System.err.println("Transition (Notify) " + st0.getName() + " -> " + st1.getName());
	 st0.addTransition(st1);
       }
    }
   else if (bm.getDeclaringClass() == thread_class && do_synch && do_waits) {
      String nm = bm.getName();
      if (nm.equals("join")) {
	 // BUILD A JOIN NODE HERE -- NEED TO IDENTIFY THE THREAD
       }
    }
   else if (bm.getDeclaringClass() == system_class && bm.getName().equals("exit")) {
      st1 = ModelState.createExitState(for_method,getLine(ino));
      st0.addTransition(st1);
    }

   return st1;
}




private ModelState handleCallback(int ino,BT_Method bm,ModelState st0)
{
   ModelState st1 = st0;

   String sid = model_master.getCallbackStart(bm);
   if (sid != null) {
      JflowMethod cm = event_gen.buildCallback(sid);
      if (cm != null) {
	 BT_Ins ins = ins_vector.elementAt(ino);
	 st1 = ModelState.createCallOnceState(for_method,getLine(ino),cm,ins);
       }
    }

   return st1;
}



private void handleThrow(int ino,ModelState st,BT_ClassVector clss,Stack<Integer> jsrs,ValueState vals)
{
   BT_CodeAttribute code = for_method.getCode();
   if (code == null) return;
   BT_ExceptionTableEntryVector exv = code.exceptions;
   if (exv == null) return;
   boolean fnd = false;

   Set<BT_Class> done = new HashSet<BT_Class>();

   for (Enumeration<?> e = exv.elements(); e.hasMoreElements(); ) {
      BT_ExceptionTableEntry ex = (BT_ExceptionTableEntry) e.nextElement();
      int sino = ex.startPC;
      int eino = ex.endPC;
      if (sino < 0) sino = code.ins.indexOf(ex.startPCTarget);
      if (eino < 0) eino = code.ins.indexOf(ex.endPCTarget)+1;
      if (ino >= sino && ino < eino) {
	 if (clss != null) {
	    boolean ok = false;
	    for (Enumeration<?> e1 = clss.elements(); e1.hasMoreElements(); ) {
	       BT_Class bc = (BT_Class) e1.nextElement();
	       if (done.contains(bc)) continue;
	       if (ex.catchType != null) {
		  if (ex.catchType == bc || ex.catchType.isDescendentOf(bc)) {
		     ok = true;
		     done.add(bc);
		   }
		}
	       else {
		  ok = true;
		  done.add(bc);
		}
	     }
	    if (!ok) continue;
	  }
	 ValueState st1 = vals;
	 if (vals != null) {
	    st1 = vals.cloneState();
	    ModelValue v0 = ModelValue.anyValue(BT_Class.forName("java.lang.Throwable"));
	    st1.pushStack(v0);
	  }
	 addToQueue(ex.handlerTarget,st,jsrs,st1);
	 fnd = true;		     // possibly only set if clss != null
	 if (ex.catchType == null) {
	    fnd = true;
	    break;
	  }
	 else if (clss != null && clss.size() == done.size()) {
	    fnd = true;
	    break;
	  }
       }
    }

   if (!fnd && st != exit_node) {
      st.addTransition(exit_node);
      if (model_master.doDebug()) System.err.println("Transition (Throw) " + st.getName() + " -> " + exit_node.getName());
    }
}




private ModelState handleIf(int ino,BT_Ins ins,ModelState cst,Stack<Integer> jsrs,ValueState vals)
{
   BT_JumpOffsetIns joins = (BT_JumpOffsetIns) ins;

   JflowSource fld = null;
   JflowValue jv = for_method.getAssociation(AssociationType.FIELDUSE,ins);
   if (jv != null) {
      for (JflowSource js : jv.getSourceCollection()) {
	 if (model_master.isFieldSourceRelevant(js)) {
	    fld = js;
	    break;
	  }
       }
    }

   if (fld == null && cst.getCall() != null) {
      if (checkUseReturn(ino-1)) {
	 fld = model_master.getProgramReturnSource();
	 cst.setUseReturn(true);
       }
    }

   if (fld != null) {
      ConditionType c1,c2;
      switch (ins.opcode) {
	 case opc_ifeq :
	    c1 = ConditionType.EQ;
	    c2 = ConditionType.NE;
	    break;
	 case opc_ifne :
	    c1 = ConditionType.NE;
	    c2 = ConditionType.EQ;
	    break;
	 case opc_iflt :
	    c1 = ConditionType.LT;
	    c2 = ConditionType.GE;
	    break;
	 case opc_ifge :
	    c1 = ConditionType.GE;
	    c2 = ConditionType.LT;
	    break;
	 case opc_ifgt :
	    c1 = ConditionType.GT;
	    c2 = ConditionType.LE;
	    break;
	 case opc_ifle :
	    c1 = ConditionType.LE;
	    c2 = ConditionType.GT;
	    break;
	 case opc_ifnonnull :
	    c1 = ConditionType.NONNULL;
	    c2 = ConditionType.NULL;
	    break;
	 case opc_ifnull :
	    c1 = ConditionType.NULL;
	    c2 = ConditionType.NONNULL;
	    break;
	 default :
	    c1 = null;
	    c2 = null;
	    break;
       }
      ModelState st1 = null;
      if (branch_result != TestBranch.ALWAYS) {
	 st1 = ModelState.createCondState(for_method,getLine(ino),c2,fld);
	 cst.addTransition(st1);
	 if (model_master.doDebug()) System.err.println("Transition (Ifb) " + cst.getName() + " -> " + st1.getName());
       }
      if (branch_result != TestBranch.NEVER) {
	 ModelState st2 = ModelState.createCondState(for_method,getLine(ino),c1,fld);
	 cst.addTransition(st2);
	 if (model_master.doDebug()) System.err.println("Transition (If) " + cst.getName() + " -> " + st2.getName());
	 addToQueue(joins.target,st2,jsrs,vals);
       }
      cst = st1;
    }
   else {
      if (branch_result != TestBranch.NEVER) addToQueue(joins.target,cst,jsrs,vals);
      if (branch_result == TestBranch.ALWAYS) cst = null;
    }

   return cst;
}




private ModelState handlePut(int ino,BT_Ins ins,ModelState cst,Stack<Integer> _j,ValueState _v,ModelValue fvl)
{
   BT_FieldRefIns frins = (BT_FieldRefIns) ins;

   Object val = null;
   if (fvl != null) val = fvl.getProgramValue();
   if (val == null) {
      JflowValue cv = for_method.getAssociation(AssociationType.FIELDSET,ins);
      if (cv != null) val = cv.getProgramValue();
    }

   JflowValue ov = for_method.getAssociation(AssociationType.THISREF,ins);
   JflowSource fld = model_master.getModelFieldSource(frins.getFieldTarget(),ov);
   if (fld == null) return cst;

   if (val == null) System.err.println("JFLOW: Set field " + frins.getFieldTarget() + " to unknown");

// if (fld == null || val == null) return cst;

   ModelState st1 = ModelState.createFieldState(for_method,getLine(ino),fld,val);

   return st1;
}





/********************************************************************************/
/*										*/
/*	Methods for adding states						*/
/*										*/
/********************************************************************************/

private ModelState addEventState(int ino,ModelState cst,JflowEvent evt)
{
   if (evt == null) return cst;
   ModelState nst = ModelState.createEventState(for_method,getLine(ino),evt);
   if (model_master.doDebug()) System.err.println("Event " + evt + " for state " + nst.getName());
   cst.addTransition(nst);
   if (model_master.doDebug()) System.err.println("Transition (Event) " + cst.getName() + " -> " + nst.getName());
   return nst;
}




private ModelState addSyncState(int ino,JflowValue cv,ModelState cst,boolean endfg)
{
   if (cv == null || !do_synch) return cst;
   ModelWaitType wt = (endfg ? ModelWaitType.END_SYNCH : ModelWaitType.SYNCH);
   ModelState nst = ModelState.createWaitState(for_method,getLine(ino),cv,wt);
   if (model_master.doDebug()) System.err.println("Synch " + cv + " for state " + nst.getName());
   cst.addTransition(nst);
   if (model_master.doDebug()) System.err.println("Transition (Sync) " + cst.getName() + " -> " + nst.getName());
   return nst;
}




private int getLine(int ino)
{
   return model_master.findLineNumber(for_method,ino);
}





/********************************************************************************/
/*										*/
/*	Methods for managing the work queue					*/
/*										*/
/********************************************************************************/

private void addToQueue(BT_Ins ins,ModelState st,Stack<Integer> jsrs,ValueState vals)
{
   addToQueue(ins_vector.indexOf(ins),st,jsrs,vals);
}




private void addToQueue(int ino,ModelState st,Stack<Integer> jsrs,ValueState vals)
{
   if (!local_values) vals = null;

   BT_Ins ins = ins_vector.elementAt(ino);
   ModelState nst = state_map.get(ins,jsrs,vals);
   if (nst != null) {
      st.addTransition(nst);
      if (model_master.doDebug()) System.err.println("Transition (queue) " + st.getName() + " -> " + nst.getName());
    }
   else work_queue.addFirst(new InsState(ino,st,jsrs,vals));
}





/********************************************************************************/
/*										*/
/*	Methods for handling use of returns					*/
/*										*/
/********************************************************************************/

private boolean checkUseReturn(int ino)
{
   int vno = -1;

   while (ino >= 0) {
      BT_Ins lins = ins_vector.elementAt(ino);

      switch (lins.opcode) {
	 case opc_invokeinterface :
	 case opc_invokespecial :
	 case opc_invokestatic :
	 case opc_invokevirtual :
	    if (vno >= 0) return false;
	    BT_MethodRefIns mrins = (BT_MethodRefIns) lins;
	    BT_Method bm = mrins.getTarget();
	    if (!bm.isVoidMethod()) return true;
	    return false;
	 case opc_aload : case opc_aload_0 : case opc_aload_1 : case opc_aload_2 : case opc_aload_3 :
	 case opc_iload : case opc_iload_0 : case opc_iload_1 : case opc_iload_2 : case opc_iload_3 :
	 case opc_lload : case opc_lload_0 : case opc_lload_1 : case opc_lload_2 : case opc_lload_3 :
	    if (vno >= 0) return false;
	    BT_LoadLocalIns llins = (BT_LoadLocalIns) lins;
	    vno = llins.localNr;
	    break;
	 case opc_astore : case opc_astore_0 : case opc_astore_1 : case opc_astore_2 : case opc_astore_3 :
	 case opc_istore : case opc_istore_0 : case opc_istore_1 : case opc_istore_2 : case opc_istore_3 :
	 case opc_lstore : case opc_lstore_0 : case opc_lstore_1 : case opc_lstore_2 : case opc_lstore_3 :
	    BT_StoreLocalIns slins = (BT_StoreLocalIns) lins;
	    if (vno >= 0 && vno == slins.localNr) vno = -1;
	    break;
	 default :
	    return false;
       }

      --ino;
    }

   return false;
}




/********************************************************************************/
/*										*/
/*	Methods for handling pre/post values					*/
/*										*/
/********************************************************************************/

private void addEventValue(ModelValue mv)
{
   if (event_values == null) {
      event_values = new ArrayList<Object>();
    }
   event_values.add(mv.getProgramValue());
}


private void prefixEventValue(ModelValue mv)
{
   if (event_values == null) {
      event_values = new LinkedList<>();
    }
   event_values.add(0,mv.getProgramValue());
}



/********************************************************************************/
/*										*/
/*	Subclass for handling instruction -> state mapping			*/
/*										*/
/********************************************************************************/

private static class StateMap {

   private HashMap<BT_Ins,List<StateData>> base_map;

   StateMap() {
      base_map = new HashMap<BT_Ins,List<StateData>>();
    }

   void put(BT_Ins ins,Stack<Integer> stk,ModelState st,ValueState vals) {
      List<StateData> l = base_map.get(ins);
      if (l == null) {
	 l = new Vector<StateData>();
	 base_map.put(ins,l);
       }
      l.add(new StateData(stk,vals,st));
    }

   ModelState get(BT_Ins ins,Stack<Integer> stk,ValueState vals) {
      List<StateData> l = base_map.get(ins);
      if (l == null) return null;
      for (StateData sd : l) {
	 if (sd.compatible(stk,vals)) return sd.getState();
       }
      return null;
    }

}	// end of subclass StateMap



private static class StateData {

   private Stack<Integer> jsr_stack;
   private ValueState value_state;
   private ModelState result_state;

   StateData(Stack<Integer> stk,ValueState vals,ModelState rslt) {
      jsr_stack = stk;
      if (stk != null && stk.empty()) jsr_stack = null;
      value_state = vals;
      result_state = rslt;
    }

   ModelState getState()			    { return result_state; }

   boolean compatible(Stack<Integer> stk,ValueState vals) {
      if (stk == null || stk.empty()) {
	 if (jsr_stack != null) return false;
       }
      else if (!stk.equals(jsr_stack)) return false;
      if (vals != null && value_state != null && check_values) {
	 if (!value_state.compatibleWith(vals)) return false;
       }
      return true;
    }

}	// end of subclass StateData




/********************************************************************************/
/*										*/
/*	Subclass for holding an instruction state				*/
/*										*/
/********************************************************************************/

private static class InsState {

   private int ins_number;
   private ModelState start_state;
   private Stack<Integer> jsr_stack;
   private ValueState value_state;

   InsState(int ino,ModelState st,Stack<Integer> stk,ValueState vals) {
      ins_number = ino;
      start_state = st;
      jsr_stack = stk;
      value_state = vals;
    }

   int getInstructionNumber()			{ return ins_number; }
   ModelState getCurrentState() 		{ return start_state; }
   Stack<Integer> getJsrStack() 		{ return jsr_stack; }
   ValueState getValueState()			{ return value_state; }

}	// end of subclass InsState




}	// end of class ModelBuilder




/* end of ModelBuilder.java */
