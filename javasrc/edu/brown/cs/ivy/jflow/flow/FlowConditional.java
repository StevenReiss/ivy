/********************************************************************************/
/*										*/
/*		FlowConditional.java						*/
/*										*/
/*	Methods to handle implications of evalution on values			*/
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

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_CodeAttribute;
import com.ibm.jikesbt.BT_ConstantIntegerIns;
import com.ibm.jikesbt.BT_ConstantLongIns;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_FieldRefIns;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_JumpIns;
import com.ibm.jikesbt.BT_JumpOffsetIns;
import com.ibm.jikesbt.BT_LoadLocalIns;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodRefIns;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_Opcodes;
import com.ibm.jikesbt.BT_StoreLocalIns;

import java.util.HashMap;
import java.util.Map;



class FlowConditional implements JflowConstants, BT_Opcodes
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private FlowField	field_handler;
private Map<BT_Ins,AccessSafety> safe_access;
private Map<BT_Method,Map<Integer,AccessSafety>> call_access;




/********************************************************************************/
/*										*/
/*	Constructor								*/
/*										*/
/********************************************************************************/

FlowConditional(FlowMaster jm,FlowControl fc,FlowField ff)
{
   jflow_master = jm;
   field_handler = ff;
   safe_access = new HashMap<BT_Ins,AccessSafety>();
   call_access = new HashMap<BT_Method,Map<Integer,AccessSafety>>();
}



/********************************************************************************/
/*										*/
/*	Handle branches 							*/
/*										*/
/********************************************************************************/

StateBase handleImplications(FlowQueue wq,int ino,StateBase st0,TestBranch brslt)
{
   BT_JumpOffsetIns joins = (BT_JumpOffsetIns) wq.getInstruction(ino);
   int act = 1;

   switch (joins.opcode) {
      case opc_if_icmpeq : case opc_if_icmpne :
      case opc_if_icmplt : case opc_if_icmpge : case opc_if_icmpgt : case opc_if_icmple :
	 act = 2;
	 break;
      default :
	 break;
    }

   boolean inst = !wq.getMethod().getMethod().isStatic();
   WhereItem where = getWhere(wq,ino-1,act);

   if (brslt != TestBranch.NEVER) {
      StateBase st1 = st0;
      if (where != null && brslt != TestBranch.ALWAYS) {
	 switch (joins.opcode) {
	    case opc_ifnull :
	       st1 = where.setNull(st0,true,inst);
	       break;
	    case opc_ifnonnull :
	       st1 = where.setNonNull(st0,true,false,inst);
	       break;
	    case opc_ifeq :
	    case opc_if_icmpeq :
	       st1 = where.setEqual(st0,true,inst);
	       break;
	    case opc_ifne :
	    case opc_if_icmpne :
	       st1 = where.setNonEqual(st0,true,inst);
	       break;
	  }
       }
      wq.mergeState(st1,wq.getIndex(joins.target));
    }

   if (brslt == TestBranch.ALWAYS) return null;

   if (where != null && brslt != TestBranch.NEVER) {
      switch (joins.opcode) {
	 case opc_ifnull :
	    st0 = where.setNonNull(st0,false,false,inst);
	    break;
	 case opc_ifnonnull :
	    st0 = where.setNull(st0,false,inst);
	    break;
	 case opc_ifeq :
	 case opc_if_icmpeq :
	    st0 = where.setNonEqual(st0,false,inst);
	    break;
	 case opc_ifne :
	 case opc_if_icmpne :
	    st0 = where.setEqual(st0,false,inst);
	    break;
       }
    }

   return st0;
}




/********************************************************************************/
/*										*/
/*	Handle accesses (if it works, then the result must be non-null		*/
/*										*/
/********************************************************************************/

StateBase handleAccess(FlowQueue wq,int ino,StateBase st0)
{
   int act = 0;

   BT_Ins ins = wq.getInstruction(ino);
   boolean inst = !wq.getMethod().getMethod().isStatic();

   switch (ins.opcode) {
      case opc_getfield :
      case opc_arraylength :
      case opc_monitorenter :
	 break;
      case opc_putfield :
      case opc_aaload :
      case opc_baload : case opc_caload : case opc_daload : case opc_faload :
      case opc_iaload : case opc_laload : case opc_saload :
	 act = 1;
	 break;

      case opc_aastore :
	 act = 2;
	 break;

      case opc_invokeinterface :
      case opc_invokespecial :
      case opc_invokevirtual :
	 BT_MethodRefIns mrins = (BT_MethodRefIns) ins;
	 BT_Method mthd = mrins.getTarget();
	 if (!mthd.isInstanceMethod() || mthd.isConstructor()) return st0;
	 act = mthd.getSignature().types.size();
	 break;

      default :
	 return st0;
    }

   int sino = skipWhere(wq,ino-1,act);

   WhereItem where = getWhere(wq,sino,1);

   if (where != null) {
      st0 = where.setNonNull(st0,false,true,inst);
    }

   return st0;
}




/********************************************************************************/
/*										*/
/*	Handle field access							*/
/*										*/
/*	Check if field value is assumed to be non-null				*/
/*										*/
/********************************************************************************/

boolean handleFieldAccess(FlowQueue wq,int ino,ValueBase obj)
{
   AccessSafety as = checkSafeAccess(wq.getMethod().getMethod(),ino);

   boolean chng = obj.markFieldAccess(as);

   return chng;
}




/********************************************************************************/
/*										*/
/*	Handle array access							*/
/*										*/
/*	Check if array elements are assumed to be non-null			*/
/*										*/
/********************************************************************************/

boolean handleArrayAccess(FlowQueue wq,int ino,ValueBase arr)
{
   if (arr == null) return false;

   ValueBase cnts = arr.getArrayContents();
   if (cnts == null) return false;
   if (!cnts.canBeNull()) return false;

   BT_Ins ins = wq.getInstruction(ino);

   if (ins.opcode != opc_aaload) return false;

   AccessSafety as = checkSafeAccess(wq.getMethod().getMethod(),ino);

   boolean chng = arr.markArrayAccess(as);

   return chng;
}



/********************************************************************************/
/*										*/
/*	General routines for handling safe access analysis			*/
/*										*/
/********************************************************************************/

AccessSafety checkSafeAccess(BT_Method bm,int ino)
{
   BT_CodeAttribute cod = bm.getCode();
   if (cod == null) return AccessSafety.NONE;
   BT_InsVector iv = cod.ins;
   if (iv == null) return AccessSafety.NONE;

   BT_Ins ins = iv.elementAt(ino);
   AccessSafety as = safe_access.get(ins);
   if (as == null) {
      // this might vary based on the type of instruction in ins
      as = checkSafeAccess(bm,ino+1,0);
      safe_access.put(ins,as);
    }
   return as;
}



private AccessSafety checkSafeAccess(BT_Method bm,int ino,int depth)
{
   BT_CodeAttribute cod = bm.getCode();
   if (cod == null) return AccessSafety.NONE;
   BT_InsVector iv = cod.ins;
   if (iv == null) return AccessSafety.NONE;

   BT_JumpIns joins;
   AccessSafety safe = AccessSafety.NONE;

   while (ino >= 0) {
      BT_Ins ins = iv.elementAt(ino);

      int nino = -1;

      switch (ins.opcode) {
	 case opc_aaload :
	 case opc_aastore :
	 case opc_getfield :
	 case opc_monitorenter :
	 case opc_monitorexit :
	 case opc_putfield :
	    safe = safe.merge(AccessSafety.USED);
	    break;
	 case opc_invokeinterface :
	 case opc_invokespecial :
	 case opc_invokevirtual :
	 case opc_invokestatic :
	    BT_Method cbm = ins.getMethodTarget();
	    int ct = cbm.getSignature().types.size();
	    if (cbm.isInstanceMethod() || cbm.isConstructor()) {
	       if (depth == ct) {
		  safe = safe.merge(AccessSafety.USED);
		  break;
		}
	     }
	    int argno = ct - depth - 1;
	    if (argno >= 0) {
	       safe = safe.merge(checkSafeCallAccess(bm,argno));
	     }
	    break;
	 case opc_goto :
	 case opc_goto_w :
	    joins = (BT_JumpOffsetIns) ins;
	    nino = iv.indexOf(joins.target);
	    break;
	 case opc_return :
	 case opc_areturn :
	 case opc_dreturn :
	 case opc_freturn :
	 case opc_ireturn :
	 case opc_lreturn :
	    nino = -2;
	    break;

	 case opc_ifnull :
	 case opc_ifnonnull :
	    safe = safe.merge(AccessSafety.CHECKED);
	    break;

	 case opc_dup :
	    if (depth == 0) {
	       safe = safe.merge(checkSafeAccess(bm,ino+1,0));
	     }
	    break;
	 case opc_dup_x1 :
	    if (depth == 0) {
	       safe = safe.merge(checkSafeAccess(bm,ino+1,0));
	       safe = safe.merge(checkSafeAccess(bm,ino+1,2));
	       nino = -1;
	     }
	    else if (depth == 1) depth = 0;
	    break;
	 case opc_dup_x2 :
	    if (depth == 0) {
	       safe = safe.merge(checkSafeAccess(bm,ino+1,0));
	       safe = safe.merge(checkSafeAccess(bm,ino+1,3));
	       nino = -1;
	     }
	    else if (depth == 1 || depth == 2) depth -= 1;
	    break;
	 case opc_dup2 :
	    if (depth == 0) safe = safe.merge(checkSafeAccess(bm,ino+1,0));
	    else if (depth == 1) safe = safe.merge(checkSafeAccess(bm,ino+1,1));
	    break;
	 case opc_dup2_x1 :
	    if (depth == 0) safe = safe.merge(checkSafeAccess(bm,ino+1,0));
	    else if (depth == 1) safe = safe.merge(checkSafeAccess(bm,ino+1,1));
	    else if (depth == 2) depth -= 2;
	    break;
	 case opc_dup2_x2 :
	    if (depth == 0) safe = safe.merge(checkSafeAccess(bm,ino+1,0));
	    else if (depth == 1) safe = safe.merge(checkSafeAccess(bm,ino+1,1));
	    else if (depth == 2 || depth == 3) depth -= 2;
	    break;

	 case opc_if_acmpeq : case opc_if_acmpne :
	 case opc_if_icmpeq : case opc_if_icmpne :
	 case opc_if_icmplt : case opc_if_icmpge : case opc_if_icmpgt : case opc_if_icmple :
	    // ideally we might want to check code here on both branches
	    // however, java currently doesn't use the stack between basic blocks
	    break;

	 case opc_lookupswitch :
	 case opc_tableswitch :
	    // might want to consider all table elements here
	    // again, java probably doesn't leave anything on the stack before here
	    nino = -2;
	    break;
       }

      int pop = -ins.getPoppedStackDiff();
      if (pop > depth) {
	 break; 	  // item popped off the stack
       }
      depth += ins.getStackDiff();

      if (nino == -1) ino = ino+1;
      else ino = nino;
    }

   return safe;
}




private AccessSafety checkSafeCallAccess(BT_Method bm,int argno)
{
   Map<Integer,AccessSafety> m = call_access.get(bm);
   if (m == null) {
      m = new HashMap<>();
      call_access.put(bm,m);
    }
   AccessSafety as = m.get(argno);
   if (as != null) return as;

   AccessSafety safe = AccessSafety.NONE;

   m.put(argno,safe);				// handle recursion

   BT_CodeAttribute cod = bm.getCode();
   if (cod == null) return safe;
   BT_InsVector iv = cod.ins;
   if (iv == null) return safe;

   int idx = 0;
   BT_MethodSignature ms = bm.getSignature();
   if (bm.isInstanceMethod() || bm.isConstructor()) ++idx;
   for (int i = 0; i < ms.types.size(); ++i) {
      if (i == argno) break;
      idx += ms.types.elementAt(i).getSizeForLocal();
    }

   for (int i = 0; i < iv.size(); ++i) {
      BT_Ins ins = iv.elementAt(i);
      if (ins instanceof BT_LoadLocalIns) {
	 BT_LoadLocalIns llins = (BT_LoadLocalIns) ins;
	 if (llins.localNr == idx) {
	    safe = safe.merge(checkSafeAccess(bm,i));
	  }
       }
    }

   m.put(argno,safe);

   return safe;
}




/********************************************************************************/
/*										*/
/*	Methods to find the source of a value					*/
/*										*/
/********************************************************************************/

private WhereItem getWhere(FlowQueue wq,int ino,int act)
{
   if (ino < 0) return null;

   WhereItem where = null;
   BT_Field fld = null;

   BT_LoadLocalIns llins;
   BT_StoreLocalIns slins;
   BT_FieldRefIns frins;

   while (where == null && ino >= 0) {
      BT_Ins ins = wq.getInstruction(ino);
      switch (ins.opcode) {
	 case opc_iload : case opc_iload_0 : case opc_iload_1 : case opc_iload_2 : case opc_iload_3 :
	 case opc_lload : case opc_lload_0 : case opc_lload_1 : case opc_lload_2 : case opc_lload_3 :
	 case opc_aload : case opc_aload_0 : case opc_aload_1 : case opc_aload_2 : case opc_aload_3 :
	    llins = (BT_LoadLocalIns) ins;
	    where = new WhereItem(llins.localNr,fld);
	    break;
	 case opc_dup : case opc_dup_x1 : case opc_dup_x2 : case opc_dup2 : case opc_dup2_x1 :
	 case opc_dup2_x2 :
	    break;
	 case opc_getfield :
	    if (fld != null) return null;
	    frins = (BT_FieldRefIns) ins;
	    fld = frins.getFieldTarget();
	    break;
	 case opc_getstatic :
	    if (fld != null) return null;
	    frins = (BT_FieldRefIns) ins;
	    fld = frins.getFieldTarget();
	    where = new WhereItem(fld);
	    break;
	 case opc_aaload :
	    // possibly handle this
	    return null;
	 case opc_instanceof :
	    // handle instanceof at some point
	    return null;
	 case opc_istore : case opc_istore_0 : case opc_istore_1 : case opc_istore_2 : case opc_istore_3 :
	 case opc_lstore : case opc_lstore_0 : case opc_lstore_1 : case opc_lstore_2 : case opc_lstore_3 :
	 case opc_astore : case opc_astore_0 : case opc_astore_1 : case opc_astore_2 : case opc_astore_3 :
	    slins = (BT_StoreLocalIns) ins;
	    if (ino >= 1) {
	       BT_Ins pins = wq.getInstruction(ino-1);
	       switch (pins.opcode) {
		  case opc_dup : case opc_dup_x1 : case opc_dup_x2 : case opc_dup2 :
		  case opc_dup2_x1 : case opc_dup2_x2 :
		     where = new WhereItem(slins.localNr,fld);
		     break;
		  case opc_isub :
		     // check for iconst 1, dup preceding this and use value-1
		     return null;
		  default :
		     return null;
		}
	     }
	    break;
	 default :
	    return null;
       }
      --ino;
    }

   if (where == null) return null;

   if (act > 1) {
      BT_Ins ins = wq.getInstruction(ino);
      switch (ins.opcode) {
	 case opc_iconst_0 : case opc_iconst_1 :
	 case opc_iconst_2 : case opc_iconst_3 : case opc_iconst_4 : case opc_iconst_5 :
	 case opc_iconst_m1 :
	    BT_ConstantIntegerIns ciins = (BT_ConstantIntegerIns) ins;
	    where.setValue(ciins.value);
	    break;
	 case opc_lconst_0 : case opc_lconst_1 :
	    BT_ConstantLongIns clins = (BT_ConstantLongIns) ins;
	    where.setValue(clins.value);
	    break;
	 case opc_ldc :
	 case opc_ldc_w :
	    if (ins instanceof BT_ConstantIntegerIns) {
	       ciins = (BT_ConstantIntegerIns) ins;
	       where.setValue(ciins.value);
	     }
	    else where = null;
	    break;
	 default :
	    where = null;
	    break;
       }
    }

   return where;
}




private int skipWhere(FlowQueue wq,int ino,int act)
{
   int tot = 0;

   while (ino >= 0 && tot < act) {
      BT_Ins ins = wq.getInstruction(ino);
      int diff = ins.getStackDiff();
      tot += diff;
      --ino;
    }

   return ino;
}




/********************************************************************************/
/*										*/
/*	Class representing a value source					*/
/*										*/
/********************************************************************************/

private class WhereItem {

   private int var_number;
   private BT_Field field_name;
   private long int_value;

   WhereItem(int var,BT_Field fld) {
      var_number = var;
      field_name = fld;
      int_value = 0;
    }

   WhereItem(BT_Field fld) {
      var_number = -1;
      field_name = fld;
      int_value = 0;
    }

   void setValue(long v)				{ int_value = v; }

   StateBase setNull(StateBase cs,boolean cln,boolean inst) {
      if (field_name == null) {
	 if (var_number >= 0) {
	    ValueBase cv = cs.getLocal(var_number);
	    if (!cv.mustBeNull()) {
	       if (cln) cs = cs.cloneState();
	       cs.setLocal(var_number,jflow_master.nullValue());
	     }
	  }
       }
      else {
	 if (var_number == 0 && inst) {
	    ValueBase v = cs.getFieldMap(field_name);
	    if (v == null || !v.mustBeNull()) {
	       if (cln) cs = cs.cloneState();
	       cs.setFieldMap(field_name,jflow_master.nullValue());
	     }
	  }
	 // static fields and fields of other than this are trickier to handle
       }

      return cs;
    }

   StateBase setNonNull(StateBase cs,boolean cln,boolean force,boolean inst) {
      if (field_name == null) {
	 if (var_number >= 0) {
	    ValueBase cv = cs.getLocal(var_number);
	    ValueBase cv0 = cv.forceNonNull();
	    if (cv != cv0) {
	       if (cln) cs = cs.cloneState();
	       cs.setLocal(var_number,cv0);
	     }
	  }
       }
      else {
	 // should ensure non-static
	 if (var_number == 0 && inst) {
	    ValueBase cv = cs.getFieldMap(field_name);
	    ValueBase cv1 = field_handler.getFieldInfo(cs,field_name,cs.getLocal(0),true);
	    if (cv == null || cv.canBeNull()) {
	       if (cv == null) cv = cv1;
	       cv = cv.forceNonNull();
	       if (cln) cs = cs.cloneState();
	       cs.setFieldMap(field_name,cv);
	     }
	  }
	 // need to handle statics and field values other than this
       }

      return cs;
    }

   StateBase setEqual(StateBase cs,boolean cln,boolean inst) {
      if (field_name == null) {
	 if (var_number >= 0) {
	    if (cln) cs = cs.cloneState();
	    cs.setLocal(var_number,jflow_master.rangeValue(BT_Class.getInt(),int_value,int_value));
	  }
       }
      else if (var_number == 0 && inst) {
	 ValueBase nv = jflow_master.rangeValue(BT_Class.getInt(),int_value,int_value);
	 if (cs.getFieldMap(field_name) != nv) {
	    if (cln) cs = cs.cloneState();
	    cs.setFieldMap(field_name,nv);
	  }
       }
      // need to handle statics and field values other than this
      return cs;
    }

   StateBase setNonEqual(StateBase cs,boolean cln,boolean inst) {
      if (field_name == null) {
	 // this should be done inside ChetIntValue
       }

      return cs;
    }

}	// end of subclass WhereItem





}	// end of class FlowConditional




/* end of FlowConditional.java */
