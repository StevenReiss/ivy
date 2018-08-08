/********************************************************************************/
/*										*/
/*		CinderPatcher.java						*/
/*										*/
/*	Class for actually doing patching of a class file			*/
/*										*/
/********************************************************************************/
/*	Copyright 1997 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 1997, Brown University, Providence, RI.				 *
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
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,	 *
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS 	 *
 *  SOFTWARE.									 *
 *										 *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/cinder/CinderPatcher.java,v 1.16 2018/08/02 15:09:29 spr Exp $ */


/*********************************************************************************
 *
 * $Log: CinderPatcher.java,v $
 * Revision 1.16  2018/08/02 15:09:29  spr
 * Fix imports
 *
 * Revision 1.15  2009-09-17 01:54:51  spr
 * Enable patching at line number level.
 *
 * Revision 1.14  2008-03-14 12:25:18  spr
 * Fixes for java 1.6; code cleanup.
 *
 * Revision 1.13  2007-08-10 02:10:02  spr
 * Cleanups from Eclipse
 *
 * Revision 1.12  2006-12-01 03:22:37  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.11  2006-08-30 20:19:10  spr
 * Better checking for superclass call in constructors.
 *
 * Revision 1.10  2006/07/10 14:52:09  spr
 * Code cleanup.
 *
 * Revision 1.9  2006/02/21 17:06:09  spr
 * Remove unused code.
 *
 * Revision 1.8  2005/07/08 20:56:58  spr
 * Upgrade patching to handle constructors; add call to create local variable.
 *
 * Revision 1.7  2005/05/11 22:20:08  spr
 * More updates for java 5.0.
 *
 * Revision 1.6  2005/05/10 17:18:16  spr
 * Updates for java 5.0.
 *
 * Revision 1.5  2005/05/07 22:25:39  spr
 * Updates for java 5.0
 *
 * Revision 1.4  2004/11/09 20:32:54  spr
 * Formatting changes.
 *
 * Revision 1.3  2004/05/05 02:28:08  spr
 * Update import lists using eclipse.
 *
 * Revision 1.2  2003/12/17 21:23:42  spr
 * Add start class callback at the start of a class.
 *
 * Revision 1.1  2003/03/29 03:40:25  spr
 * Move CINDER interface to JikesBT from Bloom to Ivy.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.cinder;



import com.ibm.jikesbt.BT_AttributeVector;
import com.ibm.jikesbt.BT_BasicBlockMarkerIns;
import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_CodeAttribute;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_FieldRefIns;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_JumpIns;
import com.ibm.jikesbt.BT_LineNumberAttribute;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodRefIns;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_NewArrayIns;
import com.ibm.jikesbt.BT_NewIns;
import com.ibm.jikesbt.BT_Opcodes;
import com.ibm.jikesbt.BT_SwitchIns;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;




class CinderPatcher
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private CinderPatchType patch_type;
private int		block_count;
private int		patch_delta;
private int		patch_index;
private int		block_start;
private int		fallthru_block;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CinderPatcher(CinderPatchType pt)
{
   patch_type = pt;
   patch_delta = 0;
   patch_index = 0;
   block_start = 0;
   fallthru_block = -1;

   patch_type.getInstrumenter().setPatcher(this);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

int getCurrentOffset()				{ return patch_delta; }
int getCurrentIndex()				{ return patch_index; }



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

boolean setup(BT_Class cls,String file,String outf)
{
   patch_delta = 0;
   patch_index = 0;

   return patch_type.getInstrumenter().setupFiles(cls,file,outf);
}



void finish()
{
   patch_type.getInstrumenter().finish();
}




void patch(BT_Class bc)
{
   boolean chng = true;
   Set<BT_Method> done = new HashSet<BT_Method>();

   patch_type.getInstrumenter().startClass(bc);

   while (chng) {
      chng = false;
      for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
	 BT_Method bm = (BT_Method) e.nextElement();
	 if (!done.contains(bm)) {
	    done.add(bm);
	    patch(bc,bm);
	    chng = true;
	  }
       }
    }

   patch_type.getInstrumenter().finishClass(bc);
}



void patch(BT_Class cl,BT_Method bm)
{
   block_count = 0;
   patch_delta = 0;
   patch_index = 0;
   block_start = 0;
   fallthru_block = -1;

   if (!patch_type.getInstrumenter().setupMethod(cl,bm)) return;

// System.out.println("CINDER: BEGIN method " + bm.fullName());

   BT_CodeAttribute bc = bm.getCode();
   if (bc != null) {
      BT_InsVector iv = bc.ins;
      BT_AttributeVector av = bc.attributes;
      BT_LineNumberAttribute lns = (BT_LineNumberAttribute) av.getAttribute("LineNumberTable");
      patch_type.getInstrumenter().setLineTable(lns);
      patch(bm,iv);
      bc.attributes.removeAttribute("LocalVariableTypeTable");
      cl.attributes.removeAttribute("EnclosingMethod");
      bc.attributes.removeAttribute("StackMapTable");


    }

   patch_type.getInstrumenter().finishMethod();
}



private void patch(BT_Method bm,BT_InsVector iv)
{
   for (patch_index = 0; patch_index < iv.size(); ++patch_index) {
      if (patch_index == 0) {
	 if (bm.isConstructor()) {
	    BT_Class bc = bm.getDeclaringClass();
	    BT_Class bc0 = bc.getSuperClass();
	    for (int i = 0; i < iv.size()-1; ++i) {
	       BT_Ins bi = iv.elementAt(i);
	       if (bi.opcode == BT_Opcodes.opc_invokespecial) {
		  BT_Method tbm = bi.getMethodTarget();
		  BT_Class tbc = tbm.getDeclaringClass();
		  if (tbm.isConstructor() && (tbc == bc || tbc == bc0)) {
		     patch_index = i+1;
		     break;
		   }
		}
	     }
	    if (patch_index == 0 && bc0 != null) {
	       System.err.println("CINDER: Superclass call not found in constructor " + bm);
	     }
	  }
	 patch_index += patchEntry(bm,iv,patch_index);
	 patch_index += patchBlock(bm,iv,patch_index);
       }
      BT_Ins bi = iv.elementAt(patch_index);

      patch_index += patchAny(bm,iv,patch_index);

      if (bi.isFieldAccessIns()) {
	 if (bi.opcode == BT_Opcodes.opc_getstatic || bi.opcode == BT_Opcodes.opc_getfield) {
	    patch_index += patchAccess(bm,iv,patch_index);
	  }
	 else {
	    patch_index += patchWrite(bm,iv,patch_index);
	  }
       }
      else if (bi.isNewIns() || bi.opcode == BT_Opcodes.opc_newarray) {
	 patch_index += patchNew(bm,iv,patch_index);
       }
      else if (bi.isAThrowIns() || bi.isReturnIns()) {
	 fallthru_block = -1;
	 patch_index += patchReturn(bm,iv,patch_index);
       }
      else if (bi instanceof BT_BasicBlockMarkerIns) {
	 patch_index += patchBlock(bm,iv,patch_index);
       }
      else if (bi.opcode == BT_Opcodes.opc_monitorenter) {
	patch_index += patchSync(true,bm,iv,patch_index);
      }
      else if (bi.opcode == BT_Opcodes.opc_monitorexit) {
	patch_index += patchSync(false,bm,iv,patch_index);
      }
      else if (bi instanceof BT_JumpIns) {
	 BT_JumpIns jins = (BT_JumpIns) bi;
	 if (bi.opcode == BT_Opcodes.opc_goto || bi.opcode == BT_Opcodes.opc_goto_w)
	    fallthru_block = -1;
	 noteBranch(iv,jins.target);
       }
      else if (bi instanceof BT_SwitchIns) {
	 BT_SwitchIns sins = (BT_SwitchIns) bi;
	 noteBranch(iv,sins.def);
	 for (int j = 0; j < sins.targets.length; ++j) {
	    noteBranch(iv,sins.targets[j]);
	  }
       }
      else if (bi instanceof BT_MethodRefIns) {
	 patch_index += patchCall(bm,iv,patch_index);
       }
    }

   /**************
   System.err.println("START METHOD " + bm);
   for (int i = 0; i < iv.size(); ++i) {
      System.err.println("PATCH " + i + " " + iv.elementAt(i));
    }
   **************/
}




/********************************************************************************/
/*										*/
/*	Patching methods							*/
/*										*/
/********************************************************************************/

private int patchAccess(BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;

   if (patch_type.getPatchLocalAccess() || patch_type.getPatchRemoteAccess()) {
      BT_FieldRefIns fri = (BT_FieldRefIns) iv.elementAt(idx);
      BT_Field fld = fri.getFieldTarget();
      if (fri.opcode == BT_Opcodes.opc_getstatic) {
	 ctr = patch_type.getInstrumenter().patchStaticAccess(fld,bm,iv,idx);
       }
      else {
	 ctr = patch_type.getInstrumenter().patchFieldAccess(fld,bm,iv,idx);
       }
    }

   patch_delta += ctr;

   return ctr;
}



private int patchWrite(BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;

   if (patch_type.getPatchWrites()) {
      BT_FieldRefIns fri = (BT_FieldRefIns) iv.elementAt(idx);
      BT_Field fld = fri.getFieldTarget();
      if (fri.opcode == BT_Opcodes.opc_putstatic) {
	 ctr = patch_type.getInstrumenter().patchStaticWrite(fld,bm,iv,idx);
       }
      else {
	 ctr = patch_type.getInstrumenter().patchFieldWrite(fld,bm,iv,idx);
       }
    }

   patch_delta += ctr;

   return ctr;
}



private int patchNew(BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;

   if (patch_type.getPatchAllocs()) {
      BT_Ins bi = iv.elementAt(idx);
      if (bi instanceof BT_NewArrayIns) {
	 BT_NewArrayIns nai = (BT_NewArrayIns) bi;
	 int typ = nai.getTypeNumber();
	 ctr = patch_type.getInstrumenter().patchBaseArrayAlloc(typ,bm,iv,idx);
       }
      else if (bi instanceof BT_NewIns) {
	 BT_NewIns ni = (BT_NewIns) bi;
	 BT_Class c = ni.getClassTarget();
	 ctr = patch_type.getInstrumenter().patchAlloc(c,bm,iv,idx);
       }
    }

   patch_delta += ctr;

   return ctr;
}



private int patchEntry(BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;

   if (patch_type.getPatchEntry()) {
      BT_MethodSignature ms = bm.getSignature();
      int ct = ms.types.size();
      int ea = patch_type.getPatchEntryArgs();
      if (ea < ct) ct = ea;
      ctr = patch_type.getInstrumenter().patchMethodEntry(ct,bm,iv,idx);
    }

   patch_delta += ctr;

   return ctr;
}



private int patchReturn(BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;

   if (patch_type.getPatchExit()) {
      BT_Ins bi = iv.elementAt(idx);
      if (bi.opcode == BT_Opcodes.opc_return && patch_type.getPatchExitValue()) {
	 ctr = patch_type.getInstrumenter().patchMethodExit(true,bm,iv,idx);
       }
      else {
	 ctr = patch_type.getInstrumenter().patchMethodExit(false,bm,iv,idx);
       }

    }

   patch_delta += ctr;

   return ctr;
}



private int patchBlock(BT_Method bm,BT_InsVector iv,int idx)
{
   if (fallthru_block >= 0) noteBranch(iv,iv.elementAt(idx));

   block_start = idx;
   fallthru_block = idx;

   int ctr = 0;

   if (patch_type.getPatchBasicBlocks()) {
      ctr = patch_type.getInstrumenter().patchStartBlock(block_count++,bm,iv,idx);
    }

   patch_delta += ctr;

   return ctr;
}




private int patchSync(boolean enter,BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;

   if (patch_type.getPatchSynchronization()) {
      ctr = patch_type.getInstrumenter().patchSync(enter,bm,iv,idx);
    }

   patch_delta += ctr;

   return ctr;
}




private int patchCall(BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;

   if (patch_type.getPatchCalls()) {
      BT_MethodRefIns mrins = (BT_MethodRefIns) iv.elementAt(idx);
      BT_Method m = mrins.getTarget();
      ctr = patch_type.getInstrumenter().patchCall(m,bm,iv,idx);
    }

   patch_delta += ctr;

   return ctr;
}



private int patchAny(BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;

   if (patch_type.getPatchAny()) {
      ctr = patch_type.getInstrumenter().patchAny(bm,iv,idx);
    }

   patch_delta += ctr;

   return ctr;
}



private void noteBranch(BT_InsVector iv,BT_Ins ins)
{
   if (block_start >= 0) {
      patch_type.getInstrumenter().noteBlockBranch(iv.elementAt(block_start),ins);
    }
}



}	// end of class CinderPatcher




/* end of CinderPatcher.java */
