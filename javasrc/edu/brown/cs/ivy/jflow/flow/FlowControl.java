/********************************************************************************/
/*										*/
/*		FlowControl.java						*/
/*										*/
/*	Routines to compute object flow in java byte code			*/
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

import edu.brown.cs.ivy.cinder.CinderInstrumenter;
import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowFlags;
import edu.brown.cs.ivy.jflow.JflowMethod;
import edu.brown.cs.ivy.jflow.JflowModelSource;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_ClassRefIns;
import com.ibm.jikesbt.BT_CodeAttribute;
import com.ibm.jikesbt.BT_ConstantClassIns;
import com.ibm.jikesbt.BT_ConstantDoubleIns;
import com.ibm.jikesbt.BT_ConstantFloatIns;
import com.ibm.jikesbt.BT_ConstantIntegerIns;
import com.ibm.jikesbt.BT_ConstantLongIns;
import com.ibm.jikesbt.BT_ConstantStringIns;
import com.ibm.jikesbt.BT_ExceptionTableEntry;
import com.ibm.jikesbt.BT_ExceptionTableEntryVector;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_FieldRefIns;
import com.ibm.jikesbt.BT_IIncIns;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_JumpOffsetIns;
import com.ibm.jikesbt.BT_LoadLocalIns;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodCallSite;
import com.ibm.jikesbt.BT_MethodRefIns;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_MethodVector;
import com.ibm.jikesbt.BT_MultiANewArrayIns;
import com.ibm.jikesbt.BT_NewArrayIns;
import com.ibm.jikesbt.BT_Opcodes;
import com.ibm.jikesbt.BT_StoreLocalIns;
import com.ibm.jikesbt.BT_SwitchIns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;



class FlowControl extends CinderInstrumenter implements JflowConstants, BT_Opcodes
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster	  jflow_master;

private FlowField	field_handler;
private FlowConditional cond_handler;
private FlowArray	array_handler;
private FlowCall	call_handler;

private Set<BT_Method>		    start_methods;
private Map<MethodBase,Set<BT_Ins>> imethod_queue;
private Map<MethodBase,FlowQueue>   imethod_map;

private Set<BT_Class>			     class_setup;
private Map<BT_Class,Collection<MethodBase>> classsetup_map;

private Set<BT_Class>		      staticinit_set;
private Set<BT_Class>		      staticinit_ran;
private Set<BT_Class>		      staticinit_started;
private Set<BT_Ins>		      bad_calls;
private List<MethodBase>	      static_inits;
private Map<BT_Class,Set<MethodBase>> staticinit_queue;

private int		inst_ctr;
private long		start_time;

private Map<String,Set<MethodBase>> callback_set;

private ValueBase	 main_thread;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

FlowControl(FlowMaster cm)
{
   jflow_master = cm;
   inst_ctr = 0;

   start_methods = new HashSet<BT_Method>();

   imethod_queue = new LinkedHashMap<>();
   imethod_map = new HashMap<>();

   bad_calls = new HashSet<>();

   class_setup = new HashSet<>();
   class_setup.add(BT_Class.forName("java.lang.Object"));
   class_setup.add(BT_Class.forName("java.lang.String"));
   class_setup.add(BT_Class.forName("java.lang.Thread"));
   class_setup.add(BT_Class.forName("java.lang.Class"));
   class_setup.add(BT_Class.forName("java.lang.ClassLoader"));
   class_setup.add(BT_Class.forName("java.lang.Boolean"));
   class_setup.add(BT_Class.forName("java.lang.Integer"));
   class_setup.add(BT_Class.forName("java.lang.Long"));
   class_setup.add(BT_Class.forName("java.lang.Short"));
   class_setup.add(BT_Class.forName("java.lang.ThreadGroup"));
   class_setup.add(BT_Class.forName("java.lang.SecurityManager"));
   class_setup.add(BT_Class.forName("java.security.AccessControlContext"));
   class_setup.add(BT_Class.forName("java.security.ProtectionDomain"));
   class_setup.add(BT_Class.forName("java.io.PrintStream"));
   class_setup.add(BT_Class.forName("java.io.FilterOutputStream"));
   class_setup.add(BT_Class.forName("java.io.OutputStream"));
   class_setup.add(BT_Class.forName("java.io.OutputStreamWriter"));
   class_setup.add(BT_Class.forName("java.io.BufferedWriter"));
   class_setup.add(BT_Class.forName("java.io.FilterWriter"));
   class_setup.add(BT_Class.forName("java.io.Writer"));
   class_setup.add(BT_Class.forName("java.io.FileInputStream"));
   class_setup.add(BT_Class.forName("java.io.InputStream"));
   class_setup.add(BT_Class.forName("java.io.UnixFileSystem"));
   class_setup.add(BT_Class.forName("java.util.Properties"));
   class_setup.add(BT_Class.forName("java.util.Hashtable"));
   class_setup.add(BT_Class.forName("java.lang.StringBuilder"));
   class_setup.add(BT_Class.forName("java.lang.StringBuffer"));
   class_setup.add(BT_Class.forName("java.lang.AbstractStringBuilder"));
   class_setup.add(BT_Class.forName("sun.nio.cs.StreamEncoder"));
   class_setup.add(BT_Class.forName("sun.security.provider.PolicyFile"));
   class_setup.add(BT_Class.forName("sun.awt.X11GraphicsEnvironment"));
   class_setup.add(BT_Class.forName("sun.java2d.SunGraphicsEnvironment"));

   classsetup_map = new HashMap<>();

   staticinit_set = new HashSet<>();
   staticinit_set.add(BT_Class.forName("java.lang.System"));
   staticinit_set.add(BT_Class.forName("java.lang.Class"));
   staticinit_ran = new HashSet<>(staticinit_set);
   staticinit_started = new HashSet<>(staticinit_ran);
   static_inits = new Vector<>();
   staticinit_queue = new HashMap<>();

   field_handler = new FlowField(jflow_master,this);
   cond_handler = new FlowConditional(jflow_master,this,field_handler);
   array_handler = new FlowArray(jflow_master,this);
   call_handler = new FlowCall(jflow_master,this);

   start_time = System.currentTimeMillis();

   main_thread = null;

   callback_set = new HashMap<>();
}




/********************************************************************************/
/*										*/
/*	Local access methods							*/
/*										*/
/********************************************************************************/

@Override protected boolean useMethod(BT_Class bc,BT_Method bm)
{
   if (checkStartMethod(bc,bm)) {
      MethodBase cm = jflow_master.findMethod(bm);
      queueMethodStart(cm);
    }

   return true;
}



@Override public int patchMethodEntry(int nargs,BT_Method bm,BT_InsVector iv,int idx)
{
   if (!bm.isSynchronized()) return 0;

   return 0;
}



@Override public void finish()
{
   for ( ; ; ) {
      FlowQueue wq = setupNextFlowQueue();
      if (wq == null) break;
      scanCode(wq);
      while (checkExceptions(wq)) scanCode(wq);
    }
}



void cleanup()
{
   int mcnt = 0;
   int icnt = 0;
   int bcnt = 0;
   int zcnt = 0;
   Set<BT_Method> used = new HashSet<BT_Method>();
   for (Map.Entry<MethodBase,FlowQueue> ent : imethod_map.entrySet()) {
      MethodBase cm = ent.getKey();
      FlowQueue fq = ent.getValue();
      ++icnt;
      if (!used.contains(cm.getMethod())) ++mcnt;
      used.add(cm.getMethod());
      int mx = fq.getNumInstructions();
      zcnt += mx;
      for (int ino = 0; ino < mx; ++ino) {
	 BT_Ins ins = fq.getInstruction(ino);
	 StateBase st = fq.getState(ins);
	 if (st != null) ++bcnt;
       }
    }

   long ctim = System.currentTimeMillis();
   double ttim = ctim - start_time;
   ttim /= 1000.0;

   System.err.println("JFLOW: Number of total Java methods scanned = " + mcnt);
   System.err.println("JFLOW: Number of total method instances =     " + icnt);
   System.err.println("JFLOW: Number of total byte codes scanned =   " + bcnt);
   System.err.println("JFLOW: Number of total byte codes ignored =   " + (zcnt-bcnt));
   System.err.println("JFLOW: Number of instructions scanned =       " + inst_ctr);
   System.err.println("JFLOW: Number of start methods =              " + start_methods.size());
   System.err.println("JFLOW: Time used =                            " + ttim);
   System.err.println("JFLOW: Number of unknown calls =              " + bad_calls.size());
   for (BT_Ins uc : bad_calls) System.err.println("JFLOW:\t" + uc);

   imethod_queue = null;
   imethod_map = null;
   staticinit_set = null;
   staticinit_ran = null;
   staticinit_started = null;
   bad_calls = null;
   call_handler = null;
   field_handler = null;
   array_handler = null;
}




Iterable<JflowMethod> getStaticInitializers()
{
   Collection<JflowMethod> cm = new ArrayList<JflowMethod>(static_inits);

   return cm;
}




/********************************************************************************/
/*										*/
/*	Methods to scan the code in one routine 				*/
/*										*/
/********************************************************************************/

private void scanCode(FlowQueue wq)
{
   while (!wq.isEmpty()) {
      int ino = wq.getNext();
      try {
	 processInstruction(ino,wq);
       }
      catch (Throwable e) {
	 System.err.println("JFLOW: Problem at instruction " + ino + " of method " +
			       wq.getMethod().getMethodName());
	 System.err.println("JFLOW: Source: " + e.toString());
	 e.printStackTrace();
       }
    }
}




private void processInstruction(int ino,FlowQueue wq)
{
   BT_Ins ins = wq.getInstruction(ino);
   StateBase st1 = wq.getState(ins);
   MethodBase method = wq.getMethod();

   if (FlowMaster.doDebug()) System.err.println("Work on " + ino + " " + ins);

   if (!checkInit(st1,method,ins,ino)) {
      return;
    }

   ++inst_ctr;

   st1 = st1.cloneState();

   ValueBase s0;
   ValueBase s1;
   ValueBase s2;
   int i0;
   boolean oref;
   SourceBase src;
   TestBranch brslt;
   BT_Method bm;
   BT_LoadLocalIns llins;
   BT_StoreLocalIns slins;
   BT_NewArrayIns nains;
   BT_MultiANewArrayIns mains;
   BT_ClassRefIns crins;
   BT_FieldRefIns frins;
   BT_JumpOffsetIns joins;
   BT_MethodRefIns mrins;
   BT_SwitchIns sins;
   BT_ConstantIntegerIns ciins;
   BT_ConstantLongIns clins;
   BT_ConstantStringIns csins;
   BT_IIncIns iiins;

   if (ino == 0 && method.getMethod().isSynchronized()) {
      if (!method.getMethod().isStatic()) {
	 ValueBase cv0 = st1.getLocal(0);
	 method.setAssociation(AssociationType.SYNC,ins,cv0);
	 for (SourceBase sb : cv0.getSources()) sb.setUsedInLock();
       }
    }

   int nino = ino+1;

   switch (ins.opcode) {

/* Array processing instructions */

      case opc_aaload :
	 s2 = st1.popStack();				// index
	 s0 = st1.popStack();
	 method.setAssociation(AssociationType.THISREF,ins,s0);
	 if (s0.mustBeNull()) {
	    if (FlowMaster.doDebug()) System.err.println("\tAttempt to access null array");
	    nino = -1;			// do nothing if access off null
	    break;
	  }
	 if (cond_handler.handleArrayAccess(wq,ino,s0)) noteArrayChange(s0);
	 s1 = array_handler.getArraySet(wq.getMethod(),ins,s0,s2);
	 if (FlowMaster.doDebug()) System.err.println("\tArray " + s0 + " index " + s2 + " == " + s1);
	 st1.pushStack(s1);
	 st1 = cond_handler.handleAccess(wq,ino,st1);
	 break;
      case opc_aastore :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = st1.popStack();
	 method.setAssociation(AssociationType.THISREF,ins,s2);
	 if (s2.mustBeNull()) {
	    if (FlowMaster.doDebug()) System.err.println("\tAttempt to store in null array");
	    nino = -1;			// do nothing if access off null
	    break;
	  }
	 s0 = makeNonunique(s0,st1,method);
	 array_handler.addToArraySet(s2,s0,s1);
	 st1 = cond_handler.handleAccess(wq,ino,st1);
	 break;
      case opc_anewarray :
	 crins = (BT_ClassRefIns) ins;
	 s0 = st1.popStack();
	 initialize(crins.getClassTarget());
	 s1 = array_handler.newArraySet(method,ins,ino,crins.getClassTarget(),1,s0);
	 st1.pushStack(s1);
	 break;
      case opc_multianewarray :
	 mains = (BT_MultiANewArrayIns) ins;
	 i0 = mains.dimensions;
	 BT_Class bcls = mains.getClassTarget();
	 bcls = bcls.arrayType;
	 for (int i = 0; i < i0; ++i) {
	    s0 = st1.popStack();
	  }
	 initialize(bcls);
	 s1 = array_handler.newArraySet(method,ins,ino,bcls,i0,null);
	 st1.pushStack(s1);
	 break;
      case opc_newarray :
	 nains = (BT_NewArrayIns) ins;
	 s0 = st1.popStack();
	 s1 = array_handler.newArraySet(method,ins,ino,nains.getClassTarget(),1,s0);
	 st1.pushStack(s1);
	 break;
      case opc_arraylength :
	 s0 = st1.popStack();
	 method.setAssociation(AssociationType.THISREF,ins,s0);
	 if (s0.mustBeNull()) {
	    if (FlowMaster.doDebug()) System.err.println("\tAttempt to access null array");
	    nino = -1;			// do nothing if access off null
	    break;
	  }
	 s1 = field_handler.getArrayLength(s0);
	 st1.pushStack(s1);
	 st1 = cond_handler.handleAccess(wq,ino,st1);
	 break;

/* Object processing instructions */

      case opc_new :
	 crins = (BT_ClassRefIns) ins;
	 initialize(crins.getTarget());
	 jflow_master.noteClassUsed(crins.getTarget());
	 src = getLocalSource(method,ino,crins);
	 s0 = jflow_master.objectValue(crins.getTarget(),jflow_master.createSingletonSet(src),
					       JflowFlags.NON_NULL);
	 src = getModelSource(method,ino,ins,s0);
	 if (src != null) s0 = s0.getSourcedValue(src);
	 method.setAssociation(AssociationType.NEW,ins,s0);
	 st1.pushStack(s0);
	 break;
      case opc_aconst_null:
	 st1.pushStack(jflow_master.nullValue());
	 break;
      case opc_aload : case opc_aload_0 : case opc_aload_1 : case opc_aload_2 : case opc_aload_3 :
	 llins = (BT_LoadLocalIns) ins;
	 st1.pushStack(st1.getLocal(llins.localNr));
	 break;
      case opc_areturn :
	 s0 = st1.popStack();
	 call_handler.handleReturn(st1,s0,method);
	 nino = -1;
	 break;
      case opc_astore : case opc_astore_0 : case opc_astore_1 : case opc_astore_2 : case opc_astore_3 :
	 slins = (BT_StoreLocalIns) ins;
	 s0 = st1.popStack();
	 st1.setLocal(slins.localNr,s0);
	 if (FlowMaster.doDebug()) System.err.println("\tSet Local " + slins.localNr + " = " + s0);
	 break;
      case opc_athrow :
	 s0 = st1.popStack();
	 method.setAssociation(AssociationType.THROW,ins,s0);
	 if (s0.mustBeNull()) {
	    if (FlowMaster.doDebug()) System.err.println("\tAttempt to access null array");
	    nino = -1;			// do nothing if access off null
	    break;
	  }
	 handleThrow(wq,ino,s0,st1);
	 nino = -1;
	 break;
      case opc_checkcast :
	 crins = (BT_ClassRefIns) ins;
	 s0 = st1.popStack();
	 boolean pfg = isProjectClass(crins.getClassTarget());
	 if (pfg && isProjectClass(s0.getDataType())) pfg = false;
	 if (pfg && s0.getDataType() == BT_Class.findJavaLangObject()) pfg = false;
	 if (pfg && s0.getDataType().isInterface()) pfg = false;
	 s0 = s0.restrictByClass(crins.getClassTarget(),pfg);
	 if (s0.mustBeNull()) s0 = jflow_master.nullValue(crins.getClassTarget());
	 if (!s0.mustBeNull() && s0.getSourceSet().isEmpty()) nino = -1;
	 if (FlowMaster.doDebug()) System.err.println("\tCast result = " + s0);
	 st1.pushStack(s0);
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
      case opc_monitorenter :
	 s0 = st1.popStack();
	 method.setAssociation(AssociationType.SYNC,ins,s0);
	 for (SourceBase sb : s0.getSources()) sb.setUsedInLock();
	 st1 = cond_handler.handleAccess(wq,ino,st1);
	 break;
      case opc_monitorexit :
	 s0 = st1.popStack();
	 method.setAssociation(AssociationType.SYNC,ins,s0);
	 field_handler.handleEndSync(st1,method);
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

/* Field instructions */

      case opc_getfield :
	 oref = false;
	 bm = method.getMethod();
	 frins = (BT_FieldRefIns) ins;
	 s0 = st1.popStack();
	 if (ino > 0 && (bm.isInstanceMethod() || bm.isConstructor())) {
	    if (s0 == st1.getLocal(0)) oref = true;
	  }
	 method.setAssociation(AssociationType.THISREF,ins,s0);
	 if (s0.mustBeNull()) {
	    if (FlowMaster.doDebug()) System.err.println("\tAttempt to access null");
	    nino = -1;			// do nothing if access off null
	    break;
	  }
	 if (cond_handler.handleFieldAccess(wq,ino,s0)) {
	    field_handler.handleFieldChanged(ins.getFieldTarget());
	  }
	 s1 = field_handler.getFieldInfo(st1,frins.getFieldTarget(),s0,oref);
	 src = getFieldSource(method,ino,ins,s0);
	 if (src != null) s1 = s1.getSourcedValue(src);
	 if (FlowMaster.doDebug()) System.err.println("\tField of " + s0 + " = " + s1);
	 st1.pushStack(s1);
	 st1 = cond_handler.handleAccess(wq,ino,st1);
	 break;
      case opc_getstatic :
	 frins = (BT_FieldRefIns) ins;
	 s1 = field_handler.getFieldInfo(st1,frins.getFieldTarget(),null,false);
	 src = getFieldSource(method,ino,ins,null);
	 if (src != null) s1 = s1.getSourcedValue(src);
	 st1.pushStack(s1);
	 break;
      case opc_putfield :
	 oref = false;
	 bm = method.getMethod();
	 frins = (BT_FieldRefIns) ins;
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 method.setAssociation(AssociationType.THISREF,ins,s1);
	 if (s1.mustBeNull()) {
	    if (FlowMaster.doDebug()) System.err.println("\tAttempt to access null");
	    nino = -1;
	    break;
	  }
	 if (bm.isInstanceMethod() || bm.isConstructor()) {
	    if (s1 == st1.getLocal(0)) oref = true;
	  }
	 if (FlowMaster.doDebug()) System.err.println("\tSet Field of " + s1 + " = " + s0);
	 s0 = makeNonunique(s0,st1,method);
	 if (FlowMaster.doDebug()) System.err.println("\tSet Field of " + s1 + " = " + s0);
	 method.setAssociation(AssociationType.FIELDSET,ins,s0);
	 boolean fg = s1.isUnique();
	 if (fg) fg &= isDirect(wq,ino,oref);
	 field_handler.addFieldInfo(st1,frins.getFieldTarget(),method,oref,s0,s1,fg);
	 st1 = cond_handler.handleAccess(wq,ino,st1);
	 break;
      case opc_putstatic :
	 frins = (BT_FieldRefIns) ins;
	 s0 = st1.popStack();
	 if (FlowMaster.doDebug()) System.err.println("\tSet Static Field = " + s0);
	 s0 = makeNonunique(s0,st1,method);
	 method.setAssociation(AssociationType.FIELDSET,ins,s0);
	 field_handler.addFieldInfo(st1,frins.getFieldTarget(),method,false,s0,null,false);
	 break;

/* Arithmetic instructions */

      case opc_instanceof :
	 crins = (BT_ClassRefIns) ins;
	 s0 = st1.popStack();
	 s2 = jflow_master.anyValue(crins.getClassTarget());
	 s1 = s0.performOperation(BT_Class.getInt(),s2,ins.opcode);
	 st1.pushStack(s1);
	 break;
      case opc_baload : case opc_caload : case opc_daload : case opc_faload :
      case opc_iaload : case opc_laload : case opc_saload :
	 s2 = st1.popStack();				// index
	 s0 = st1.popStack();
	 method.setAssociation(AssociationType.THISREF,ins,s0);
	 if (s0.mustBeNull()) {
	    if (FlowMaster.doDebug()) System.err.println("\tAttempt to access null array");
	    nino = -1;			// do nothing if access off null
	    break;
	  }
	 s1 = array_handler.getArraySet(method,ins,s0,s2);
	 if (FlowMaster.doDebug()) System.err.println("\tArray " + s0 + " index " + s2 + " == " + s1);
	 st1.pushStack(s1);
	 st1 = cond_handler.handleAccess(wq,ino,st1);
	 break;
      case opc_dadd : case opc_ddiv : case opc_dmul : case opc_drem : case opc_dsub :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = s1.performOperation(BT_Class.getDouble(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_fadd : case opc_fdiv : case opc_fmul : case opc_frem : case opc_fsub :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = s1.performOperation(BT_Class.getFloat(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_iadd : case opc_idiv : case opc_imul : case opc_irem : case opc_isub :
      case opc_iand : case opc_ior : case opc_ixor :
      case opc_ishl : case opc_ishr : case opc_iushr :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = s1.performOperation(BT_Class.getInt(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_dcmpg : case opc_dcmpl :
      case opc_fcmpg : case opc_fcmpl :
	 st1.popStack();
	 st1.popStack();
	 s0 = jflow_master.rangeValue(BT_Class.getInt(),-1,1);
	 st1.pushStack(s0);
	 break;
      case opc_ladd : case opc_ldiv : case opc_lmul : case opc_lrem : case opc_lsub :
      case opc_land : case opc_lor : case opc_lxor :
      case opc_lshl : case opc_lshr : case opc_lushr :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = s1.performOperation(BT_Class.getLong(),s0,ins.opcode);
	 st1.pushStack(s2);
	 break;
      case opc_bastore : case opc_castore : case opc_dastore : case opc_fastore :
      case opc_iastore : case opc_lastore : case opc_sastore :
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 s2 = st1.popStack();
	 method.setAssociation(AssociationType.THISREF,ins,s2);
	 if (s2.mustBeNull()) {
	    if (FlowMaster.doDebug()) System.err.println("\tAttempt to access null array");
	    nino = -1;			// do nothing if access off null
	    break;
	  }
	 array_handler.addToArraySet(s2,s0,s1);
	 break;
      case opc_bipush :
      case opc_sipush :
	 ciins = (BT_ConstantIntegerIns) ins;
	 s0 = jflow_master.rangeValue(BT_Class.getInt(),ciins.value,ciins.value);
	 st1.pushStack(s0);
	 break;
      case opc_dconst_0 : case opc_dconst_1 :
	 s0 = jflow_master.anyValue(BT_Class.getDouble());
	 st1.pushStack(s0);
	 break;
      case opc_dload :
      case opc_dload_0 : case opc_dload_1 : case opc_dload_2 : case opc_dload_3 :
	 llins = (BT_LoadLocalIns) ins;
	 st1.pushStack(st1.getLocal(llins.localNr));
	 break;
      case opc_fconst_0 : case opc_fconst_1 : case opc_fconst_2 :
	 s0 = jflow_master.anyValue(BT_Class.getFloat());
	 st1.pushStack(s0);
	 break;
      case opc_fload :
      case opc_fload_0 : case opc_fload_1 : case opc_fload_2 : case opc_fload_3 :
	 llins = (BT_LoadLocalIns) ins;
	 st1.pushStack(st1.getLocal(llins.localNr));
	 break;
      case opc_iload :
      case opc_iload_0 : case opc_iload_1 : case opc_iload_2 : case opc_iload_3 :
	 llins = (BT_LoadLocalIns) ins;
	 s0 = st1.getLocal(llins.localNr);
	 st1.pushStack(s0);
	 break;
      case opc_iconst_0 : case opc_iconst_1 :
      case opc_iconst_2 : case opc_iconst_3 : case opc_iconst_4 : case opc_iconst_5 :
      case opc_iconst_m1 :
	 ciins = (BT_ConstantIntegerIns) ins;
	 s0 = jflow_master.rangeValue(BT_Class.getInt(),ciins.value,ciins.value);
	 st1.pushStack(s0);
	 break;
      case opc_lload :
      case opc_lload_0 : case opc_lload_1 : case opc_lload_2 : case opc_lload_3 :
	 llins = (BT_LoadLocalIns) ins;
	 st1.pushStack(st1.getLocal(llins.localNr));
	 break;
      case opc_lconst_0 : case opc_lconst_1 :
	 clins = (BT_ConstantLongIns) ins;
	 s0 = jflow_master.rangeValue(BT_Class.getLong(),clins.value,clins.value);
	 st1.pushStack(s0);
	 break;
      case opc_ldc :
      case opc_ldc_w :
	 if (ins.isLoadConstantStringIns()) {
	    csins = (BT_ConstantStringIns) ins;
	    s0 = jflow_master.constantString(csins.value);
	  }
	 else if (ins instanceof BT_ConstantFloatIns)
	    s0 = jflow_master.anyValue(BT_Class.getFloat());
	 else if (ins instanceof BT_ConstantClassIns) { 	// java 1.5 class constant
	    s0 = jflow_master.nativeValue(BT_Class.forName("java.lang.Class"));
	    s0 = s0.forceNonNull();
	  }
	 else {
	    ciins = (BT_ConstantIntegerIns) ins;
	    s0 = jflow_master.rangeValue(BT_Class.getInt(),ciins.value,ciins.value);
	  }
	 st1.pushStack(s0);
	 break;
      case opc_ldc2_w :
	 if (ins instanceof BT_ConstantDoubleIns)
	    s0 = jflow_master.anyValue(BT_Class.getDouble());
	 else {
	    clins = (BT_ConstantLongIns) ins;
	    s0 = jflow_master.rangeValue(BT_Class.getLong(),clins.value,clins.value);
	  }
	 st1.pushStack(s0);
	 break;
      case opc_d2f :
      case opc_fneg :
      case opc_i2f :
      case opc_l2f :
	 s0 = st1.popStack();
	 st1.pushStack(s0.performOperation(BT_Class.getFloat(),s0,ins.opcode));
	 break;
      case opc_d2i :
      case opc_f2i :
      case opc_l2i :
      case opc_ineg :
	 s0 = st1.popStack();
	 st1.pushStack(s0.performOperation(BT_Class.getInt(),s0,ins.opcode));
	 break;
      case opc_iinc :
	 iiins = (BT_IIncIns) ins;
	 s0 = st1.getLocal(iiins.localNr);
	 s1 = jflow_master.rangeValue(BT_Class.getInt(),iiins.constant,iiins.constant);
	 s2 = s0.performOperation(BT_Class.getInt(),s1,ins.opcode);
	 st1.setLocal(iiins.localNr,s2);
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
	 call_handler.handleReturn(st1,s0,method);
	 nino = -1;
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
	 if (FlowMaster.doDebug()) System.err.println("\tSet Local " + slins.localNr + " = " + s0);
	 break;

/* Branch instructions */

      case opc_goto : case opc_goto_w :
	 joins = (BT_JumpOffsetIns) ins;
	 nino = wq.getIndex(joins.target);
	 break;
      case opc_if_acmpeq : case opc_if_acmpne :
      case opc_if_icmpeq : case opc_if_icmpne :
      case opc_if_icmplt : case opc_if_icmpge : case opc_if_icmpgt : case opc_if_icmple :
	 joins = (BT_JumpOffsetIns) ins;
	 s0 = st1.popStack();
	 s1 = st1.popStack();
	 if (FlowMaster.doDebug()) System.err.println("\tCompare " + s1 + " :: " + s0);
	 brslt = s1.branchTest(s0,ins.opcode);
	 if (brslt != TestBranch.NEVER) wq.mergeState(st1,wq.getIndex(joins.target));
	 if (brslt == TestBranch.ALWAYS) nino = -1;
	 break;
      case opc_ifeq : case opc_ifne : case opc_iflt : case opc_ifge : case opc_ifgt : case opc_ifle :
      case opc_ifnonnull : case opc_ifnull :
	 joins = (BT_JumpOffsetIns) ins;
	 s0 = st1.popStack();
	 method.setAssociation(AssociationType.FIELDUSE,ins,s0);
	 brslt = s0.branchTest(s0,ins.opcode);
	 if (FlowMaster.doDebug()) System.err.println("\tTest value = " + s0);
	 st1 = cond_handler.handleImplications(wq,ino,st1,brslt);
	 if (st1 == null) nino = -1;
	 break;
					
      case opc_lookupswitch :
      case opc_tableswitch :
	 sins = (BT_SwitchIns) ins;
	 st1.popStack();
	 for (int i = 0; i < sins.targets.length; ++i) {
	    wq.mergeState(st1,sins.targets[i]);
	  }
	 if (sins.def != null) wq.mergeState(st1,sins.def);
	 nino = -1;
	 break;


/* subroutine calls */

      case opc_jsr : case opc_jsr_w :
	 joins = (BT_JumpOffsetIns) ins;
	 st1.pushStack(jflow_master.badValue());
	 st1.pushReturn(nino);
	 nino = wq.getIndex(joins.target);
	 break;
      case opc_ret :
	 nino = st1.popReturn();
	 break;

/* Method instructions */

      case opc_invokeinterface :
      case opc_invokespecial :
      case opc_invokestatic :
      case opc_invokevirtual :
	 mrins = (BT_MethodRefIns) ins;
	 if (!call_handler.handleCall((BT_MethodRefIns) ins,ino,st1,wq)) {
	    nino = -1;
	    if (FlowMaster.doDebug()) System.err.println("\tUnknown RETURN value " + bad_calls.contains(ins));
	    bad_calls.add(ins);
	  }
	 else {
	    if (FlowMaster.doDebug() && bad_calls.contains(ins)) {
	       System.err.println("\tRETURN value now known");
	       bad_calls.remove(ins);
	     }
	    JflowValue jv = method.getAssociation(AssociationType.THISARG,ins);
	    if (jv == null || jv.canBeNull()) st1 = cond_handler.handleAccess(wq,ino,st1);
	  }
	 if (mrins.getTarget().getName().equals("exit") &&
		mrins.getTarget().getDeclaringClass().getName().equals("java.lang.System")) {
	    nino = -1;
	  }
	 handleNonuniqueCalls(mrins,st1,method);
	 break;

      case opc_return :
	 s0 = jflow_master.anyValue(BT_Class.getVoid());
	 call_handler.handleReturn(st1,s0,method);
	 nino = -1;
	 break;

      case OPC_BBLOCK : 		// basic block
	 break;

      // handle opc_wide

      default :
	 System.err.println("JFLOW: Op code " + ins.opcode + " not found");
	 break;
    }

   if (nino >= 0) {
      wq.mergeState(st1,nino);
    }
}




/********************************************************************************/
/*										*/
/*	Methods to handle throws and exceptions 				*/
/*										*/
/********************************************************************************/

void handleThrow(FlowQueue wq,int ino,ValueBase vi,StateBase st0)
{
   MethodBase cm = wq.getMethod();
   BT_CodeAttribute code = cm.getCode();
   BT_ExceptionTableEntryVector exv = code.exceptions;
   ValueBase v0 = null;

   if (vi == null) {
      v0 = jflow_master.mutableValue(BT_Class.forName("java.lang.Throwable"));
      v0 = v0.forceNonNull();
    }
   else {
      v0 = vi.forceNonNull();
    }

   for (Enumeration<?> e = exv.elements(); v0 != null && e.hasMoreElements(); ) {
      BT_ExceptionTableEntry ex = (BT_ExceptionTableEntry) e.nextElement();
      int sino = ex.startPC;
      int eino = ex.endPC;
      if (sino < 0) sino = wq.getIndex(ex.startPCTarget);
      if (eino < 0) eino = wq.getIndex(ex.endPCTarget)+1;
      ValueBase v1 = v0;
      if (ex.catchType != null) v1 = v0.restrictByClass(ex.catchType,false);

      if (ino >= sino && ino < eino && !v1.isEmptySourceSet()) {
	 if (ex.catchType == null) v0 = null;
	 BT_Ins ins0 = ex.startPCTarget;
	 StateBase st1 = st0.cloneState();
	 StateBase st2 = wq.getState(ins0);
	 if (st2 == null) {
	    for (int idx1 = sino; idx1 < eino; ++idx1) {
	       BT_Ins insx = wq.getInstruction(idx1);
	       st2 = wq.getState(insx);
	       if (st2 != null) break;
	     }
	  }
	 if (st2 == null) {
	    System.err.println("JFLOW: null state found for " + cm.getMethodName() + " " + 
                  cm.getMethodSignature() + " " + ino + " " + ins0 + " " + 
                  ins0.byteIndex);
	    continue;
	  }
	 st1.resetStack(st2);
	 st1.pushStack(v1);

	 if (FlowMaster.doDebug()) System.err.println("Handle throw to: " + wq.getIndex(ex.handlerTarget));
	 wq.mergeState(st1,ex.handlerTarget);

	 if (vi != null) {
	    BT_Ins insc = ex.handlerTarget;
	    ValueBase cv = (ValueBase) cm.getAssociation(AssociationType.CATCH,insc);
	    if (cv == null) cv = v1;
	    else cv = cv.mergeValue(v1);
	    cm.setAssociation(AssociationType.CATCH,insc,cv);
	  }
       }
    }

   if (v0 != null && !v0.isEmptySourceSet()) {
      handleException(v0,cm);
    }
}




private void handleException(ValueBase s0,MethodBase cm)
{
// BT_ClassVector exc = cm.getMethod().declaredExceptions();
// if (exc.isEmpty()) return;			// must be declared to be used

   if (cm.addException(s0)) {
      if (FlowMaster.doDebug())
	 System.err.println("\tException change: " + cm.toString() + " :: " + s0);
      for (Enumeration<?> e = cm.getMethod().callSites.elements(); e.hasMoreElements(); ) {
	 BT_MethodCallSite mcs = (BT_MethodCallSite) e.nextElement();
	 for (MethodBase cfm : jflow_master.getAllMethods(mcs.from)) {
	    if (cfm.getCallsAt(mcs.instruction,cm)) {
	       if (FlowMaster.doDebug()) System.err.println("\tQueue for exception " + cfm.getMethodName());
	       queueMethodChange(cfm,mcs.instruction);
	     }
	  }
       }
      BT_MethodVector mv = cm.getMethod().getParents();
      for (Enumeration<?> e = mv.elements(); e.hasMoreElements(); ) {
	 BT_Method pm = (BT_Method) e.nextElement();
	 for (MethodBase cpm : jflow_master.getAllMethods(pm)) {
	    handleException(s0,cpm);
	  }
       }
    }
}




private boolean checkExceptions(FlowQueue wq)
{
   MethodBase cm = wq.getMethod();
   BT_CodeAttribute code = cm.getCode();
   BT_ExceptionTableEntryVector exv = code.exceptions;
   boolean chng = false;

   if (!cm.hasResult()) return false;	// don't bother unless we can return

   for (Enumeration<?> e = exv.elements(); e.hasMoreElements(); ) {
      BT_ExceptionTableEntry ex = (BT_ExceptionTableEntry) e.nextElement();
      StateBase st2 = wq.getState(ex.handlerTarget);
      if (st2 == null) {
	 BT_Ins ins0 = ex.startPCTarget;
	 StateBase st1 = wq.getState(ins0);
	 if (st1 == null) continue;
	 st1 = st1.cloneState();
	 BT_Class typ = ex.catchType;
	 ValueBase eval;
	 if (typ == null) {
	    continue;		// used for synchronization w/ errors
	    // typ = BT_Class.forName("java.lang.Throwable");
	    // eval = jflow_master.mutableValue(typ);
	  }
	 else eval = jflow_master.nativeValue(typ);
	 eval = eval.forceNonNull();
	 st1.pushStack(eval);
	 if (FlowMaster.doDebug()) {
	    System.err.println("Handle unknown throw of " + ex.catchType + " to: " +
				  wq.getIndex(ex.handlerTarget));
	  }
	 wq.mergeState(st1,ex.handlerTarget);
	 chng = true;
       }
    }

   return chng;
}




/********************************************************************************/
/*										*/
/*	Routines for managing intermethod analysis				*/
/*										*/
/********************************************************************************/

void handleCallback(BT_Method bm,List<ValueBase> args,String cbid)
{
   call_handler.handleCallback(bm,args,cbid);
}




void handleReturnSetup(MethodBase mi,StateBase st1,boolean chng)
{
   BT_Method bm = mi.getMethod();

   if (chng) {
      if (bm.isStaticInitializer()) {
	 BT_Class bc = bm.getDeclaringClass();
	 staticinit_ran.add(mi.getMethodClass());
	 if (st1 != null) field_handler.setupFields(st1,null,bc);
       }
      else if (bm.isConstructor()) {
	 BT_Class bc = bm.getDeclaringClass();
	 if (!class_setup.contains(bc)) {
	    class_setup.add(bc);
	    if (st1 != null) {
	       ValueBase base = st1.getLocal(0);
	       field_handler.setupFields(st1,base,bc);
	     }
	    recheckConstructors(bc,true);
	  }
	 else {
	    if (st1 != null) {
	       ValueBase base = st1.getLocal(0);
	       field_handler.resetupFields(st1,base,bc);
	     }
	  }
	 if (st1 != null) st1.finishInitialization(bc);
       }
    }
   else if (bm.isConstructor()) {
      BT_Class bc = bm.getDeclaringClass();
      if (st1 != null) {
	 field_handler.updateFields(st1,bc);
	 st1.finishInitialization(bc);
       }
    }
}



ValueBase handleSpecialCases(BT_Method bm,List<ValueBase> nargs,
				MethodBase caller,BT_Ins calins,int ino)
{
   if (jflow_master.getIsArrayCopy(bm)) {
      array_handler.handleArrayCopy(nargs,caller,calins);
      return jflow_master.anyValue(BT_Class.getVoid());
    }

   if (jflow_master.getUseReflection(bm,caller,ino)) {
      // might want to do something here
    }

   return null;
}



void noteCallback(String cbid,MethodBase mi)
{
   if (cbid == null) return;

   Set<MethodBase> s = callback_set.get(cbid);
   if (s == null) {
      s = new HashSet<MethodBase>();
      callback_set.put(cbid,s);
    }
   if (FlowMaster.doDebug()) System.err.println("Add callback " + cbid + ": " + mi);
   s.add(mi);
}



Iterable<JflowMethod> getCallbacks(String cbid)
{
   Set<MethodBase> s = callback_set.get(cbid);
   if (s == null || s.isEmpty()) return null;

   return new ArrayList<JflowMethod>(s);
}



void noteArrayChange(SourceBase src)
{
   array_handler.noteArrayChange(src);
}


void noteArrayChange(ValueBase v)
{
   for (SourceBase src : v.getSources()) {
      noteArrayChange(src);
    }
}



/********************************************************************************/
/*										*/
/*	Methods to get the source for an instruction				*/
/*										*/
/********************************************************************************/


SourceBase getModelSource(MethodBase cfm,int ino,BT_Ins ins,ValueBase base)
{
   SourceBase ns = null;

   if (!cfm.isModelSourceDefined(ins)) {
      JflowModelSource msrc = jflow_master.createModelSource(cfm,ino,ins,base);
      if (msrc != null) {
	 ns = new SourceModel(jflow_master,msrc,cfm,ino);
	 if (FlowMaster.doDebug()) System.err.println("Create MODEL source " + msrc + " :: " + ns);
	 jflow_master.recordModelSource(msrc,ns);
       }
      cfm.setModelSource(ins,ns);
    }
   else ns = cfm.getModelSource(ins);

   return ns;
}



private SourceBase getLocalSource(MethodBase cfm,int ino,BT_ClassRefIns crins)
{
   SourceBase ns;

   if (!cfm.isSourceDefined(crins)) {
      ProtoBase cp = jflow_master.createPrototype(crins.target);
      if (cp != null) {
	 ns = jflow_master.createPrototypeSource(cfm,ino,crins.target,cp);
       }
      else if (jflow_master.useLocalSource(cfm,crins.target)) {
	 ns = jflow_master.createLocalSource(cfm,ino,crins.target,true);
       }
      else {
	 ns = jflow_master.createFixedSource(crins.target);
       }
      cfm.setSource(crins,ns);
    }
   else ns = cfm.getSource(crins);

   return ns;
}




SourceBase getCallSource(MethodBase cfm,int ino,BT_Ins ins)
{
   SourceBase ns = null;

   // note that invoke instructions can have model sources for both
   // the call and the return.	However they do not have a local source.
   // Thus we save the call source as a local source and the return source
   // as a model source.

   if (!cfm.isSourceDefined(ins)) {
      JflowModelSource msrc = jflow_master.createModelSource(cfm,ino,ins,null);
      if (msrc != null) {
	 ns = new SourceModel(jflow_master,msrc,cfm,ino);
	 jflow_master.recordModelSource(msrc,ns);
       }
      cfm.setSource(ins,ns);
    }
   else ns = cfm.getSource(ins);

   return ns;
}



SourceBase getFieldSource(MethodBase cfm,int ino,BT_Ins ins,ValueBase base)
{
   if (jflow_master.isFieldSourceNeeded(cfm,ins)) {
      return (SourceBase) jflow_master.findFieldSource(ins.getFieldTarget(),base);
    }

   return null;
}





int findLineNumber(MethodBase cm,int ino)
{
   if (ino == JFLOW_START_LINE || ino == JFLOW_END_LINE) {
      setLineTable(cm.getMethod());
      if (ino == JFLOW_START_LINE) return getMinLineNumber();
		return getMaxLineNumber();
    }

   return getLineNumber(cm.getMethod(),ino);
}




/********************************************************************************/
/*										*/
/*	Methods to check for starting methods					*/
/*										*/
/********************************************************************************/

private boolean checkStartMethod(BT_Class bc,BT_Method bm)
{
   if (start_methods.contains(bm)) return true;

   if (bm.isPublic()) {
      if (bm.isStatic() && bm.getName().equals("main")) {
	 BT_MethodSignature ms = bm.getSignature();
	 if (bm.isMain() || ms.types.size() == 0) {
	    if (jflow_master.isValidStart(bc)) {
	       start_methods.add(bm);
	       return true;
	     }
	  }
       }
    }

   boolean istest = false;
   if (jflow_master.isValidStart(bc)) {
      for (BT_Class cc = bc; cc != null; cc = cc.getSuperClass()) {
	 if (cc.getName().equals("junit.framework.TestCase")) {
	    istest = true;
	  }
       }
    }

   if (istest) {
      if (bm.isConstructor() ||
	     (bm.getName().startsWith("test") && bm.isPublic() &&
		 bm.getSignature().returnType == BT_Class.getVoid())) {
	 start_methods.add(bm);
	 return true;
       }
    }

   return false;
}



Iterable<BT_Method> getStartMethods()	 { return start_methods; }


boolean isStartMethod(JflowMethod cfm)
{
   return start_methods.contains(cfm.getMethod());
}



/********************************************************************************/
/*										*/
/*	Methods for handling an instruction-oriented method queue		*/
/*										*/
/********************************************************************************/

void queueChangedMethod(BT_Method bm)
{
   for (MethodBase mb : jflow_master.getAllMethods(bm)) {
      imethod_queue.remove(mb);
      imethod_map.remove(mb);
      static_inits.remove(mb);
    }

   if (start_methods.contains(bm)) {
      for (MethodBase mb : jflow_master.getAllMethods(bm)) {
	 queueMethodStart(mb);
       }
    }
   else {
      for (MethodBase mb : jflow_master.getAllMethods(bm)) {
	 queueMethod(mb);
       }
    }

   for (Enumeration<?> e = bm.callSites.elements(); e.hasMoreElements(); ) {
      BT_MethodCallSite mcs = (BT_MethodCallSite) e.nextElement();
      for (MethodBase cfm : jflow_master.getAllMethods(mcs.from)) {
	 if (FlowMaster.doDebug()) System.err.println("\tQueue for method update " + cfm.getMethodName());
	 queueMethodChange(cfm,mcs.instruction);
       }
    }
}




private void queueMethodStart(MethodBase cm)
{
   if (main_thread == null) {
      SourceBase mths = jflow_master.createFixedSource(BT_Class.forName("java.lang.Thread"));
      SourceSet mthst = jflow_master.createSingletonSet(mths);
      main_thread = jflow_master.objectValue(mths.getDataType(),mthst,JflowFlags.NON_NULL);
    }

   cm.setThread(main_thread);
   queueMethod(cm);
}



private void queueMethod(MethodBase bm)
{
   BT_CodeAttribute code = bm.getCode();
   if (code == null) return;

   if (imethod_map.get(bm) == null) {
      noteInitialMethodCall(bm);
    }

   BT_Ins ins0 = code.ins.elementAt(0);
   queueMethod(bm,ins0);
}




void queueMethodCall(MethodBase cm,StateBase st)
{
   BT_Method bm = cm.getMethod();

   if (!bm.isStatic() && !bm.isConstructor() && !bm.isPrivate()) {
      BT_Class bc = cm.getMethodClass();
      if (jflow_master.isProjectClass(bc)) initialize(bc);
      else if (!staticinit_set.contains(bc)) return;
    }

   queueForConstructors(cm,st);

   queueMethod(cm);
}



void noteInitialMethodCall(MethodBase mb)
{
   jflow_master.noteMethodUsed(mb.getMethod());
}



void queueForConstructors(MethodBase cm,StateBase st)
{
   BT_Method bm = cm.getMethod();
   StateBase sst = cm.getStartState();

   if (st != null && !bm.isStatic() && !bm.isConstructor()) {
      if (sst != null && sst.addInitializers(st)) {
	 recheckAllConstructors();
       }
    }
   else if (st != null && sst != null) {
      sst.addInitializers(st);
    }
}




void queueMethodChange(MethodBase cm,BT_Ins ins)
{
   if (imethod_map.get(cm) == null) return;		// ignore if it hasn't been called

   queueMethod(cm,ins);
}



private void queueMethod(MethodBase cm,BT_Ins ins)
{
   if (cm == null) return;

   initialize(cm.getMethodClass());

   Set<BT_Ins> s = imethod_queue.get(cm);
   if (s == null) {
      s = new HashSet<>();
      imethod_queue.put(cm,s);
    }

   s.add(ins);

   if (FlowMaster.doDebug()) {
      System.err.println("\tQueue method " + cm.getMethodName() + " [" + cm.hashCode() + "] /" +
			    cm.getCodeVector().indexOf(ins) + " " + s.size());
    }
}



private FlowQueue setupNextFlowQueue()
{
   MethodBase cm = null;
   BT_CodeAttribute code = null;
   Set<BT_Ins> inset = null;

   while (!imethod_queue.isEmpty()) {
      Iterator<Map.Entry<MethodBase,Set<BT_Ins>>> it = imethod_queue.entrySet().iterator();
      if (!it.hasNext()) return null;
      Map.Entry<MethodBase,Set<BT_Ins>> ent = it.next();
      it.remove();

      cm = ent.getKey();
      inset = ent.getValue();
      code = cm.getCode();
      if (code != null) break;
    }
   if (cm == null || code == null) return null;

   FlowQueue wq = imethod_map.get(cm);

   if (FlowMaster.doDebug()) {
      System.err.println("\nInitialize workqueue for " + cm.getMethodName() + " " +
			    cm.getMethodSignature() + " [" + cm.hashCode() + "] " +
			    (inset == null ? (-1) : inset.size()));
      if (wq == null) {
	 for (Iterator<BT_Class> it = cm.getStartState().getInitializations(); it.hasNext(); ) {
	    BT_Class btc = it.next();
	    System.err.println("\tIn constructor for " + btc.getName());
	  }
       }
    }

   if (wq == null) {
      wq = new FlowQueue(cm,code.ins);
      imethod_map.put(cm,wq);
      StateBase st0 = cm.getStartState();
      if (cm.getMethod().isConstructor()) {
	 BT_Class bc = cm.getMethodClass();
	 if (!class_setup.contains(bc)) {
	    st0.startInitialization(bc);
	    recheckConstructors(bc,false);
	  }
       }
      wq.mergeState(st0,0);
    }
   else if (inset != null) {
      for (BT_Ins ins : inset) {
	 if (ins == code.ins.firstElement()) wq.mergeState(cm.getStartState(),0);
	 if (FlowMaster.doDebug()) {
	    if (wq.getState(ins) != null) {
	       System.err.println("\tAdd instruction " + code.ins.indexOf(ins));
	     }
	  }
	 wq.lookAt(ins);
       }
    }

   return wq;
}




FlowQueue getFlowQueue(MethodBase cm)
{
   if (imethod_map == null) return null;

   return imethod_map.get(cm);
}



/********************************************************************************/
/*										*/
/*	Methods to handle initializers						*/
/*										*/
/********************************************************************************/

void initialize(BT_Class bc)
{
   initialize(bc,false);
}



void initialize(BT_Class bc,boolean fakeinit)
{
   if (!staticinit_set.contains(bc)) {
      if (FlowMaster.doDebug()) System.err.println("\tInitialize " + bc.getName());
      staticinit_set.add(bc);
      for (Enumeration<?> e = bc.getParents().elements(); e.hasMoreElements(); ) {
	 BT_Class pc = (BT_Class) e.nextElement();
	 initialize(pc);
       }
      int hasiniter = 0;
      for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
	 BT_Method nbm = (BT_Method) e.nextElement();
	 if (nbm.isStaticInitializer()) {
	    ++hasiniter;
	    MethodBase cfm = jflow_master.findMethod(nbm);
	    static_inits.add(cfm);
	    queueMethod(cfm);
	  }
       }
      if (hasiniter == 0) {
	 staticinit_ran.add(bc);
	 staticinit_started.add(bc);
       }
      else if (hasiniter > 1) {
	 System.err.println("JFLOW: CLASS " + bc + " has " + hasiniter + " static initializers");
       }
    }
}



boolean canBeUsed(BT_Class bc)
{
   if (staticinit_set.contains(bc)) return true;
   if (class_setup.contains(bc)) return true;
   return false;
}


private boolean getInitializerDone(BT_Class bc)
{
   initialize(bc);

   if (!staticinit_ran.contains(bc)) return false;

   return true;
}



private boolean checkInit(StateBase st,MethodBase cm,BT_Ins ins,int ino)
{
   BT_Class bc = cm.getMethodClass();

   if (ino == 0 && !getInitializerDone(bc)) {
      if (cm.getMethod().isStaticInitializer()) {
	 staticinit_started.add(bc);
	 requeueForInit(staticinit_queue.get(bc));
	 staticinit_queue.remove(bc);
       }
      else {
	 if (!staticinit_started.contains(bc)) {
	    if (FlowMaster.doDebug()) System.err.println("\tClass not initialized requeue");
	    Set<MethodBase> s = staticinit_queue.get(bc);
	    if (s == null) {
	       s = new HashSet<MethodBase>();
	       staticinit_queue.put(bc,s);
	     }
	    s.add(cm);
	    return false;
	  }
       }
    }

   if (ino == 0 && !cm.getMethod().isStatic() && !class_setup.contains(bc)) {
      ValueBase cv = st.getLocal(0);
      if (cv.isNative() && !jflow_master.isProjectClass(bc)) return true;
      if (!st.testDoingInitialization(bc)) {
	 Collection<MethodBase> c = classsetup_map.get(bc);
	 if (c == null) {
	    c = new HashSet<MethodBase>();
	    classsetup_map.put(bc,c);
	  }
	 c.add(cm);
	 if (FlowMaster.doDebug()) {
	    System.err.println("JFLOW: Initialization not finished before call for " + bc);
	  }
	 return false;
       }
    }

   return true;
}



private void requeueForInit(Set<MethodBase> s)
{
   if (s == null) return;

   for (MethodBase cm : s) {
      queueMethod(cm);
    }
}



private void recheckConstructors(BT_Class bc,boolean del)
{
   String what = (del ? "inside" : "finished");

   Collection<MethodBase> s;

   if (del) s = classsetup_map.remove(bc);
   else s = classsetup_map.get(bc);

   if (s != null) {
      for (MethodBase cm1 : s) {
	 queueMethod(cm1);
	 if (FlowMaster.doDebug()) System.err.println("\tQueue for " + what + " constructor " + cm1.getMethodName());
       }
    }
}



private void recheckAllConstructors()
{
   for (BT_Class bc : classsetup_map.keySet()) {
      recheckConstructors(bc,false);
    }
}



/********************************************************************************/
/*										*/
/*	Methods for handling inlining and field detailing			*/
/*										*/
/********************************************************************************/

InlineType canBeInlined(BT_Method bm,List<ValueBase> nargs)
{
   if (start_methods.contains(bm)) return InlineType.NONE;

   return jflow_master.inlineMethod(bm,nargs);
}




boolean detailField(BT_Field bf)
{
   return jflow_master.detailField(bf);
}



boolean isProjectClass(BT_Class bc)
{
   if (bc == null) return false;

   return jflow_master.isProjectClass(bc);
}



/********************************************************************************/
/*										*/
/*	Methods for handling updates						*/
/*										*/
/********************************************************************************/

void handleUpdates(Collection<SourceBase> oldsrcs,
		      Map<SourceSet,SourceSet> srcupdates,
		      Map<JflowValue,JflowValue> valupdates)
{
   for (FlowQueue fq : imethod_map.values()) {
      fq.handleUpdates(oldsrcs,srcupdates,valupdates);
    }
}




/********************************************************************************/
/*										*/
/*	Methods for handling unique (constructor) values			*/
/*										*/
/********************************************************************************/

private ValueBase makeNonunique(ValueBase v,StateBase st,MethodBase m)
{
   ValueBase v1 = v.makeNonunique();
   if (v == v1) return v;

   m.setChangeUnique();
   st.updateUnique(v);

   return v1;
}



private void handleNonuniqueCalls(BT_Ins ins,StateBase st,MethodBase m)
{
   boolean chng = false;
   for (JflowMethod jm : m.getAllCalls(ins)) {
      MethodBase mb = (MethodBase) jm;
      chng |= mb.getChangeUnique();
    }

   if (!chng) return;

   if (st.updateUnique(null)) m.setChangeUnique();
}



private boolean isDirect(FlowQueue wq,int ino,boolean oref)
{
   // check if the given instruction must be executed in this method,
   // that is that if control enters the method it will always get here

   BT_JumpOffsetIns joins;
   BT_SwitchIns sins;

   if (!jflow_master.useCheckDirect()) {
      if (!oref) return false;
      if (!wq.getMethod().getMethod().isConstructor()) return false;
    }

   for (int i = 0; i < ino; ++i) {
      BT_Ins ins = wq.getInstruction(i);
      switch (ins.opcode) {
	 case opc_return :
	 case opc_areturn :
	 case opc_dreturn :
	 case opc_freturn :
	 case opc_ireturn :
	 case opc_lreturn :
	 case opc_athrow :
	    return false;

	 case opc_goto : case opc_goto_w :
	 case opc_if_acmpeq : case opc_if_acmpne :
	 case opc_if_icmpeq : case opc_if_icmpne :
	 case opc_if_icmplt : case opc_if_icmpge : case opc_if_icmpgt : case opc_if_icmple :
	 case opc_ifeq : case opc_ifne : case opc_iflt : case opc_ifge : case opc_ifgt : case opc_ifle :
	 case opc_ifnonnull : case opc_ifnull :
	 case opc_jsr : case opc_jsr_w :
	    joins = (BT_JumpOffsetIns) ins;
	    if (wq.getIndex(joins.target) > ino) return false;
	    break;

	 case opc_lookupswitch :
	 case opc_tableswitch :
	    sins = (BT_SwitchIns) ins;
	    for (int j = 0; j < sins.targets.length; ++j) {
	       if (wq.getIndex(sins.targets[j]) > ino) return false;
	     }
	    if (sins.def != null && wq.getIndex(sins.def) > ino) return false;
	    break;

	 default :
	    break;
       }
    }

   return true;
}




/********************************************************************************/
/*										*/
/*	Instrumenter methods							*/
/*										*/
/********************************************************************************/

void setMethodForMinMax(MethodBase cm)	 { setLineTable(cm.getMethod()); }
int getStartLineNumber()		 { return getMinLineNumber(); }
int getEndLineNumber()			 { return getMaxLineNumber(); }




}	 // end of class FlowControl



/* end of FlowControl.java */

