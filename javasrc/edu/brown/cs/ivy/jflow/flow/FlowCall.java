/********************************************************************************/
/*										*/
/*		FlowCall.java							*/
/*										*/
/*	Methods for handling method calls during flow analysis			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/FlowCall.java,v 1.11 2011-09-12 20:50:21 spr Exp $ */


/*********************************************************************************
 *
 * $Log: FlowCall.java,v $
 * Revision 1.11  2011-09-12 20:50:21  spr
 * Code cleanup.
 *
 * Revision 1.10  2011-04-13 21:03:14  spr
 * Fix bugs in flow analysis.
 *
 * Revision 1.9  2009-09-17 01:57:20  spr
 * Fix a few minor bugs (poll, interfaces); code cleanup for Eclipse.
 *
 * Revision 1.8  2007-08-10 02:10:39  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.7  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.6  2007-02-27 18:53:29  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.5  2006-12-01 03:22:46  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.4  2006-08-03 12:34:51  spr
 * Ensure fields of unprocessed classes handled correctly.
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

import edu.brown.cs.ivy.jflow.*;

import com.ibm.jikesbt.*;

import java.util.*;



class FlowCall implements JflowConstants, BT_Opcodes
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private FlowControl	flow_control;
private Map<MethodBase,Set<MethodBase>> rename_map;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

FlowCall(FlowMaster jm,FlowControl fc)
{
   jflow_master = jm;
   flow_control = fc;
   rename_map = new HashMap<MethodBase,Set<MethodBase>>();
}




/********************************************************************************/
/*										*/
/*     Methods to handle calls							*/
/*										*/
/********************************************************************************/

boolean handleCall(BT_MethodRefIns ins,int ino,StateBase st0,FlowQueue wq)
{
   BT_Method bm = ins.getTarget();
   BT_MethodSignature sgn = bm.getSignature();
   LinkedList<ValueBase> args = new LinkedList<ValueBase>();
   boolean virt = (ins.opcode == opc_invokeinterface || ins.opcode == opc_invokevirtual);
   boolean ifc = (ins.opcode == opc_invokeinterface);
   MethodBase method = wq.getMethod();

   if (bm.getName().equals("iterator") &&
	  bm.getDeclaringClass().getName().contains("S6SolutionSet")) {
      System.err.println("WORK HERE");
    }

   flow_control.initialize(bm.getDeclaringClass());

   if (bm.getCode() == null && bm.isStatic()) {
      BT_Method bm0 = bm.getDeclaringClass().findInheritedMethod(bm.getName(),
							  bm.getSignature(),
							  bm.getDeclaringClass());
      if (bm0 != null && bm0.getCode() != null) bm = bm0;
      if (bm0 == null) {
	 System.err.println("SPECIAL LOOKUP FOR " + bm.getName() + bm.getSignature() + " in " +
			       bm.getDeclaringClass());
       }
    }

   int ct = sgn.types.size();
   for (int i = 0; i < sgn.types.size(); ++i) {
      ValueBase cs = st0.popStack();
      args.addFirst(cs);
      if (cs != null) {
	 switch (ct-i) {
	    case 1 :
	       method.setAssociation(AssociationType.ARG1,ins,cs);
	       break;
	    case 2 :
	       method.setAssociation(AssociationType.ARG2,ins,cs);
	       break;
	    case 3 :
	       method.setAssociation(AssociationType.ARG3,ins,cs);
	       break;
	    case 4 :
	       method.setAssociation(AssociationType.ARG4,ins,cs);
	       break;
	    case 5 :
	       method.setAssociation(AssociationType.ARG5,ins,cs);
	       break;
	    case 6 :
	       method.setAssociation(AssociationType.ARG6,ins,cs);
	       break;
	    case 7 :
	       method.setAssociation(AssociationType.ARG7,ins,cs);
	       break;
	    case 8 :
	       method.setAssociation(AssociationType.ARG8,ins,cs);
	       break;
	    case 9 :
	       method.setAssociation(AssociationType.ARG9,ins,cs);
	       break;
	    default :
	       break;
	  }
       }
    }
   if (bm.isInstanceMethod() || bm.isConstructor()) {
      ValueBase cs = st0.popStack();
      if (cs.isBad()) {
	 System.err.println("JFLOW: Bad value added to call to " + bm.fullName() + " from " +
			       method + " @ " + ino);
       }
      SourceBase src = flow_control.getCallSource(method,ino,ins);
      if (src != null) cs = cs.newSourcedValue(src);
      args.addFirst(cs);
      method.setAssociation(AssociationType.THISARG,ins,cs);
      BT_Method bbm = method.getMethod();
      if (bbm.getDeclaringClass() == BT_Class.findJavaLangObject()) {
	 if (bbm.getName().equals("wait") || bbm.getName().equals("notify") ||
		bbm.getName().equals("notifyAll")) {
	    method.setAssociation(AssociationType.SYNC,ins,cs);
	  }
       }
    }

   if (FlowMaster.doDebug()) {
      int i = 0;
      for (ValueBase cv : args) {
	 System.err.println("\tArg " + (i++) + " = " + cv);
       }
    }

   while (bm.getDeclaringClass().isInterface() && !ifc) {
      BT_MethodVector mv = bm.getParents();
      if (mv != null && mv.size() == 1) {
	 bm = mv.elementAt(0);
       }
      else break;
    }

   if (bm.isInstanceMethod() && ins.opcode == opc_invokevirtual) {
      ValueBase cv = args.getFirst();
      BT_Class a0t = cv.getDataType();
      BT_Class dt = bm.getDeclaringClass();
      if (a0t != null && a0t != dt && a0t.isDescendentOf(dt) && !a0t.isArray()) {
	 BT_Method bm0 = a0t.findInheritedMethod(bm.getName(),bm.getSignature(),a0t);
	 if (bm0 != null && bm0 != bm) {
	    bm = bm0;
	    bm0.addCallSite(ins,method.getMethod());
	  }
       }
    }

   ValueBase rslt = noteMethod(bm,args,virt,ins,ino,method,st0,null);

   if (bm.declaredExceptions().size() > 0) {
      for (JflowMethod jm : method.getAllCalls(ins)) {
	 MethodBase mi = (MethodBase) jm;
	 flow_control.handleThrow(wq,ino,(ValueBase) mi.getExceptionSet(),st0);
       }
    }
   else {
      for (JflowMethod jm : method.getAllCalls(ins)) {
	 MethodBase mi = (MethodBase) jm;
	 ValueBase exvl = (ValueBase) mi.getExceptionSet();
	 if (exvl != null) flow_control.handleThrow(wq,ino,exvl,st0);
       }
    }

   if (bm.fullName().equals("java.lang.System.exit")) {
      flow_control.handleThrow(wq,ino,jflow_master.getExitException(),st0);
      method.setCanExit();
    }

   if (!bm.isVoidMethod()) {
      if (rslt == null || !rslt.goodSourceSet()) return false;
      SourceBase src = flow_control.getModelSource(method,ino,ins,rslt);
      if (src != null) {
	 ValueBase rset = jflow_master.objectValue(sgn.returnType,
							   jflow_master.createSingletonSet(src),
							   JflowFlags.NON_NULL);
	 rslt = rslt.mergeValue(rset);
       }
      method.setAssociation(AssociationType.RETURN,ins,rslt);
      st0.pushStack(rslt);
    }
   else {
      if (rslt == null) return false;
    }

   boolean discard = false;
   if (method.getMethod().isConstructor() || method.getMethod().isInstanceMethod()) {
      if (method.getMethodClass() == bm.getDeclaringClass() && !bm.isStatic()) {
	 if (!st0.getLocal(0).isUnique()) discard = true;
	 else {
	    int ctr = 0;
	    for (JflowMethod jm : method.getAllCalls(ins)) {
	       ++ctr;
	       // note because the object is unique, there can only be one method here
	       MethodBase mb = (MethodBase) jm;
	       if (mb.getResultFields() != null) {
		  for (Map.Entry<BT_Field,ValueBase> ent : mb.getResultFields().entrySet()) {
		     st0.setFieldMap(ent.getKey(),ent.getValue());
		     if (FlowMaster.doDebug())
			System.err.println("\tCopy field: " + ent.getKey() + " " + ent.getValue());
		   }
		}
	     }
	    if (ctr != 1) discard = true;
	  }
       }
    }
   if (discard) {
      st0.discardFields();
      if (FlowMaster.doDebug()) System.err.println("\tDiscard fields");
    }

   return true;
}




/********************************************************************************/
/*										*/
/*	Methods to handle a callback						*/
/*										*/
/********************************************************************************/

void handleCallback(BT_Method bm,List<ValueBase> args,String cbid)
{
   noteMethod(bm,args,true,null,0,null,null,cbid);
}




/********************************************************************************/
/*										*/
/*	Methods to actually manage a call					*/
/*										*/
/********************************************************************************/

private ValueBase noteMethod(BT_Method bm,List<ValueBase> args,boolean virt,
				BT_MethodRefIns calins,int ino,MethodBase caller,StateBase st,
				String cbid)
{
   ValueBase rslt = null;
   BT_Method orig = bm;
   boolean haveproto = false;

   LinkedList<ValueBase> nargs = checkCall(caller,calins,bm,args);

   if (nargs != null) {
      rslt = flow_control.handleSpecialCases(bm,nargs,caller,calins,ino);
      if (rslt != null) return rslt;
    }

   if (nargs != null) {
      ProtoInfo pi = handlePrototypes(bm,nargs,caller,calins);
      if (pi != null) {
	 rslt = pi.getResult();
	 if (!pi.checkAnyway()) {
	    nargs = null;
	    haveproto = true;
	    virt = false;
	  }
       }
    }

   if (nargs == null && !haveproto) {
      if (FlowMaster.doDebug()) System.err.println("\tIGNORE call to " + bm.fullName());
    }

   if (nargs != null) {
      MethodBase mi = findMethod(caller,calins,bm,nargs);
      mi.addCallbacks(nargs,flow_control);
      Collection<BT_Method> c = mi.replaceWith(nargs,caller,ino);
      if (c == null) return rslt;
      for (BT_Method bm0 : c) {
	 LinkedList<ValueBase> vargs = nargs;
	 if (c.size() > 1) vargs = new LinkedList<ValueBase>(nargs);
	 ValueBase xrslt = mi.fixReplaceArgs(bm0,vargs);
	 if (xrslt != null) rslt = xrslt.mergeValue(rslt);
	 if (calins != null && caller != null) bm0.addCallSite(calins,caller.getMethod());
	 rslt = processMethod(bm,args,virt,calins,ino,caller,st,mi,bm0,vargs,rslt,cbid);
       }
    }
   else if (virt) {
      rslt = checkVirtual(bm,args,calins,ino,caller,st,orig,rslt,cbid);
    }

   return rslt;
}





private ValueBase checkVirtual(BT_Method bm,List<ValueBase> args,
				  BT_MethodRefIns calins,int ino,MethodBase caller,StateBase st,
				  BT_Method orig,ValueBase rslt,String cbid)
{
   ValueBase cv = args.get(0);
   boolean isnative = cv.allNative();
   for (Enumeration<?> e = bm.getKids().elements(); e.hasMoreElements(); ) {
      BT_Method km = (BT_Method) e.nextElement();
      if (km == orig) continue;
      if (isnative && flow_control.isProjectClass(km.getDeclaringClass()) &&
	     !flow_control.isProjectClass(cv.getDataType())) {
	 continue;
       }
      ValueBase srslt = noteMethod(km,args,true,calins,ino,caller,st,cbid);
      if (srslt != null) {
	 if (rslt == null) rslt = srslt;
	 else rslt = rslt.mergeValue(srslt);
       }
    }

   if (rslt == null && bm.getParents().size() == 0 && bm.isAbstract() &&
	  caller != null && args.size() > 0) {
      for (SourceBase cs : cv.getSources()) {
	 BT_Class csc = cs.getDataType();
	 if (csc != null) {
	    BT_Method csm = csc.findInheritedMethod(bm.getName(),bm.getSignature(),csc);
	    if (csm != null && !csm.isAbstract()) {
	       if (FlowMaster.doDebug()) System.err.println("\tADD SPECIAL " + csm);
	       csm.addCallSite(calins,caller.getMethod());
	       ValueBase srslt = noteMethod(csm,args,true,calins,ino,caller,st,cbid);
	       MethodBase kmi = findMethod(caller,calins,csm,null);
	       if (kmi != null && kmi.hasResult()) {
		  if (rslt == null) rslt = srslt;
		  else rslt = rslt.mergeValue(srslt);
		}
	     }
	  }
       }
    }

   return rslt;
}




private ValueBase processMethod(BT_Method bm,List<ValueBase> args,boolean virt,
				   BT_MethodRefIns calins,int ino,MethodBase caller,StateBase st,
				   MethodBase mi,BT_Method bm0,LinkedList<ValueBase> nargs,
				   ValueBase rslt,String cbid)
{
   BT_Method orig = bm;

   if (bm0 != bm) {
      MethodBase mi0 = findMethod(caller,calins,bm0,nargs);
      Set<MethodBase> s = rename_map.get(mi0);
      if (s == null) {
	 s = new HashSet<MethodBase>();
	 rename_map.put(mi0,s);
       }
      s.add(mi);
      mi.addReplacementCall(calins,mi0);
      mi = mi0;
      if (mi.getIsAsync(bm)) {
	 ValueBase thvl = nargs.get(0);
	 mi.setThread(thvl);
	 if (rslt == null) rslt = jflow_master.anyValue(BT_Class.getVoid());
       }
    }

   if (FlowMaster.doDebug()) System.err.println("\tUSE call to " + mi.getMethodName());

   flow_control.queueForConstructors(mi,st);

   if (mi.addCall(nargs,flow_control,st)) {
      if (FlowMaster.doDebug()) System.err.println("\tCall change: " + mi.toString());
      if (cbid != null) flow_control.noteCallback(cbid,mi);
      flow_control.queueMethodCall(mi,st);
    }
   if (mi.hasResult()) {
      if (mi.isClone()) {
	 ValueBase cv = nargs.get(0);
	 ValueBase prslt = jflow_master.nativeValue(cv.getDataType());
	 if (prslt != null) rslt = prslt.mergeValue(rslt);
       }
      else if (mi.returnArg0()) {
	 ValueBase prslt = nargs.get(0);
	 if (prslt != null) rslt = prslt.mergeValue(rslt);
       }
      else {
	 ValueBase prslt = mi.getResultSet();
	 if (prslt != null) {
	    if (prslt.isNative()) flow_control.initialize(prslt.getDataType());
	    rslt = prslt.mergeValue(rslt);
	  }
       }
    }
   if (caller != null && mi.getCanExit()) caller.setCanExit();

   if (virt) {
      rslt = checkVirtual(bm,args,calins,ino,caller,st,orig,rslt,cbid);
    }

   return rslt;
}




private LinkedList<ValueBase> checkCall(MethodBase caller,BT_Ins calins,BT_Method bm,List<ValueBase> args)
{
   BT_MethodSignature sgn = bm.getSignature();
   int xid = ((bm.isInstanceMethod() || bm.isConstructor()) ? -1 : 0);
   BT_Class bc;

   LinkedList<ValueBase> nargs = new LinkedList<ValueBase>();
   for (ValueBase cv : args) {
      if (xid < 0) {
	 bc = bm.getDeclaringClass();
	 if (!flow_control.canBeUsed(bc)) return null;
       }
      else bc = sgn.types.elementAt(xid);
      boolean prj = flow_control.isProjectClass(bc);
      if (prj && flow_control.isProjectClass(cv.getDataType())) prj = false;
      ValueBase ncv = cv.restrictByClass(bc,prj);
      if (ncv.mustBeNull()) ncv = jflow_master.nullValue(bc);
      if (xid < 0) {
	 if (ncv.mustBeNull()) return null;
	 ncv = ncv.forceNonNull();
	 ncv = ncv.makeSubclass(bc);
       }
      if (!ncv.goodSourceSet() && !bc.isPrimitive() && !ncv.mustBeNull()) return null;
      nargs.add(ncv);
      ++xid;
    }

   return nargs;
}




/********************************************************************************/
/*										*/
/*	Methods for handling returns						*/
/*										*/
/********************************************************************************/

void handleReturn(StateBase st1,ValueBase s0,MethodBase mi)
{
   BT_Method bm = mi.getMethod();

   if (s0 != null && s0.mustBeNull()) {
      s0 = jflow_master.nullValue(bm.getSignature().returnType);
    }

   if (mi.toString().contains("SolutionSet.iterator")) {
      System.err.println("WORKING HERE");
    }

   boolean fg = mi.addResult(s0,st1);
   flow_control.handleReturnSetup(mi,st1,fg);
   if (fg) {
      if (FlowMaster.doDebug()) System.err.println("\tReturn change: " + mi.toString()+ " " + st1);
      queueReturn(st1,s0,mi);
      Set<MethodBase> s = rename_map.get(mi);
      if (s != null) {
	 for (MethodBase cm0 : s) {
	    queueReturn(st1,s0,cm0);
	    if (FlowMaster.doDebug()) System.err.println("\tRename return change: " + cm0.getMethodName());
	  }
       }
    }
}



private void queueReturn(StateBase st1,ValueBase s0,MethodBase cm)
{
   BT_Method bm = cm.getMethod();

   for (Enumeration<?> e = bm.callSites.elements(); e.hasMoreElements(); ) {
      BT_MethodCallSite mcs = (BT_MethodCallSite) e.nextElement();
      for (MethodBase cfm : jflow_master.getAllMethods(mcs.from)) {
	 if (cfm.getCallsAt(mcs.instruction,cm)) {
	    flow_control.queueMethodChange(cfm,mcs.instruction);
	  }
       }
    }

   BT_MethodVector mv = bm.getParents();
   for (Enumeration<?> e = mv.elements(); e.hasMoreElements(); ) {
      BT_Method pm = (BT_Method) e.nextElement();
      for (MethodBase fm : jflow_master.getAllMethods(pm)) {
	 handleReturn(st1,s0,fm);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Methods for finding proper method					*/
/*										*/
/********************************************************************************/

private MethodBase findMethod(MethodBase caller,BT_MethodRefIns ins,BT_Method bm,List<ValueBase> args)
{
   if (caller == null) return jflow_master.findMethod(bm);

   MethodBase cm = caller.getCallForMethod(ins,bm);
   if (cm != null) return cm;

   InlineType ifg = flow_control.canBeInlined(bm,args);
   cm = jflow_master.findMethod(bm,args,ifg);
   caller.noteCall(ins,cm);

   return cm;
}




/********************************************************************************/
/*										*/
/*	Methods for handling prototype calls					*/
/*										*/
/********************************************************************************/

private ProtoInfo handlePrototypes(BT_Method bm,LinkedList<ValueBase> args,MethodBase caller,BT_Ins ins)
{
   ValueBase rslt = null;
   int nsrc = 0;

   if (!bm.isInstanceMethod() && !bm.isConstructor()) return null;

   ValueBase cv = args.getFirst();
   for (SourceBase cs : cv.getSources()) {
      ProtoBase cp = cs.getPrototype();
      if (cp != null) {
	 if (cp.isMethodRelevant(bm)) {
	    ValueBase nv = cp.handleCall(bm,args,getCallSite(caller,ins),flow_control);
	    MethodBase cm = jflow_master.findPrototypeMethod(bm);
	    caller.notePrototypeCall(ins,cm);
	    if (nv != null) {
	       if (rslt == null) rslt = nv;
	       else rslt = rslt.mergeValue(nv);
	     }
	    else if (FlowMaster.doDebug()) {
	       System.err.println("PROTO RESULT IS NULL");
	     }
	  }
       }
      else if (!cs.isModel()) ++nsrc;
    }

   if (rslt == null && nsrc > 0)
      return null;

   return new ProtoInfo(nsrc > 0,rslt);
}




private static class ProtoInfo {

   private boolean check_anyway;
   private ValueBase return_value;

   ProtoInfo(boolean chk,ValueBase rslt) {
      check_anyway = chk;
      return_value = rslt;
    }

   boolean checkAnyway()			{ return check_anyway; }
   ValueBase getResult()			{ return return_value; }

}	// end of structure ProtoInfo




/********************************************************************************/
/*										*/
/*	Methods for finding/creating a call site for prototypes 		*/
/*										*/
/********************************************************************************/

private FlowCallSite getCallSite(MethodBase cm,BT_Ins ins)
{
   FlowCallSite cs = cm.getCallSite(ins);
   if (cs == null) {
      cs = new FlowCallSite(flow_control,cm,ins);
      cm.addCallSite(ins,cs);
    }

   return cs;
}



}	// end of class FlowCall




/* end of FlowCall.java */
