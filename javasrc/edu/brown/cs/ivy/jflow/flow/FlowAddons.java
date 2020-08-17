/********************************************************************************/
/*										*/
/*		FlowAddons.java 						*/
/*										*/
/*	Additional methods available to augment flow analysis			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/FlowAddons.java,v 1.8 2018/08/02 15:10:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: FlowAddons.java,v $
 * Revision 1.8  2018/08/02 15:10:16  spr
 * Fix imports.
 *
 * Revision 1.7  2018/02/21 16:18:43  spr
 * Change by mistake and undo.
 *
 * Revision 1.6  2008-11-12 13:45:39  spr
 * Eclipse fixups.
 *
 * Revision 1.5  2007-08-10 02:10:39  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.4  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.3  2007-02-27 18:53:29  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.2  2006/07/10 14:52:17  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:34  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.flow;

import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowFlags;

import com.ibm.jikesbt.BT_Accessor;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_JumpOffsetIns;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_Opcodes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



class FlowAddons implements JflowConstants, BT_Opcodes
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private Set<BT_Field> extra_fields;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

FlowAddons(FlowMaster jm)
{
   jflow_master = jm;

   extra_fields = new HashSet<BT_Field>();
}



/********************************************************************************/
/*										*/
/*	Methods to handle automatically chosen fields after the fact		*/
/*										*/
/********************************************************************************/

void addFieldChecks(BT_Field fld)
{
   if (extra_fields.contains(fld)) return;
   extra_fields.add(fld);

   SourceBase src = jflow_master.createFieldSource(fld,null);
   SourceSet sset = jflow_master.createSingletonSet(src);
   ValueBase sval = jflow_master.objectValue(fld.type,sset,JflowFlags.NON_NULL);

   Map<BT_Ins,Set<Integer>> done = new HashMap<>();

   for (Enumeration<?> e = fld.accessors.elements(); e.hasMoreElements(); ) {
      BT_Accessor ba = (BT_Accessor) e.nextElement();
      BT_Method bm = ba.from;
      BT_Ins ins = ba.instruction;
      if (ins.opcode == opc_getstatic || ins.opcode == opc_getfield) {
	 BT_InsVector iv = bm.getCode().ins;
	 int ino = iv.indexOf(ins);
	 trackField(iv,ino+1,0,bm,sval,done);
       }
    }
}



private void trackField(BT_InsVector iv,int ino,int stkoff,BT_Method bm,ValueBase sval,
			   Map<BT_Ins,Set<Integer>> done)
{
   BT_JumpOffsetIns joins;

   while (ino >= 0) {
      BT_Ins ins = iv.elementAt(ino);

      Integer inov = Integer.valueOf(ino);
      Set<Integer> offs = done.get(ins);
      if (offs == null) {
	 offs = new HashSet<Integer>(4);
	 done.put(ins,offs);
       }
      if (offs.contains(inov)) return;
      offs.add(inov);

      int nino = -1;

      switch (ins.opcode) {
	 default :
	    if (-ins.getPoppedStackDiff() > stkoff) return;
	    stkoff += ins.getStackDiff();
	    nino = ino+1;
	    break;
	 case opc_return :
	 case opc_areturn :
	 case opc_dreturn :
	 case opc_freturn :
	 case opc_ireturn :
	 case opc_lreturn :
	    break;
	 case opc_dup : case opc_dup_x1 : case opc_dup_x2 : case opc_dup2 : case opc_dup2_x1 :
	 case opc_dup2_x2 :
	    // handle possible duplication
	    // if so, recurse with second copy
	    break;
	 case opc_goto : case opc_goto_w :
	    joins = (BT_JumpOffsetIns) ins;
	    nino = iv.indexOf(joins.target);
	    break;
	 case opc_if_acmpeq : case opc_if_acmpne :
	 case opc_if_icmpeq : case opc_if_icmpne :
	 case opc_if_icmplt : case opc_if_icmpge : case opc_if_icmpgt : case opc_if_icmple :
	 case opc_lookupswitch :
	 case opc_tableswitch :
	    break;

	 case opc_ifeq : case opc_ifne : case opc_iflt : case opc_ifge : case opc_ifgt : case opc_ifle :
	 case opc_ifnonnull : case opc_ifnull :
	    if (stkoff != 0) break;
	    addFieldUse(sval,bm,ins);
	    break;
       }

      ino = nino;
    }
}


/*
 * Add a use of a field 
 *
 */

private void addFieldUse(ValueBase sval,BT_Method bm,BT_Ins ins)
{
   for (MethodBase cm : jflow_master.getAllMethods(bm)) {
      ValueBase cv = (ValueBase) cm.getAssociation(AssociationType.FIELDUSE,ins);
      sval = sval.mergeValue(cv);
      cm.setAssociation(AssociationType.FIELDUSE,ins,sval);
    }
}





}	// end of class FlowAddons



/* end of FlowAddons.java */
