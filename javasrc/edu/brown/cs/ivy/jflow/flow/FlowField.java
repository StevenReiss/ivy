/*******************************************************************************/
/*										*/
/*		FlowField.java							*/
/*										*/
/*	Methods for handling field values (non-local ones at least)		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/FlowField.java,v 1.9 2018/02/21 16:18:43 spr Exp $ */


/*********************************************************************************
 *
 * $Log: FlowField.java,v $
 * Revision 1.9  2018/02/21 16:18:43  spr
 * Change by mistake and undo.
 *
 * Revision 1.8  2011-04-13 21:03:14  spr
 * Fix bugs in flow analysis.
 *
 * Revision 1.7  2009-06-04 18:50:27  spr
 * Handle special cased methods correctly.
 *
 * Revision 1.6  2007-08-10 02:10:39  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.5  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.4  2007-02-27 18:53:29  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.3  2006-08-03 12:34:51  spr
 * Ensure fields of unprocessed classes handled correctly.
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

import com.ibm.jikesbt.BT_Accessor;
import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_Opcodes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



class FlowField implements JflowConstants, BT_Opcodes
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private FlowControl	flow_control;
private Map<BT_Field,ValueBase> field_map;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

FlowField(FlowMaster jm,FlowControl fc)
{
   jflow_master = jm;
   flow_control = fc;
   field_map = new HashMap<>();
}



/********************************************************************************/
/*										*/
/*	Methods to access field values						*/
/*										*/
/********************************************************************************/

ValueBase getFieldInfo(StateBase st,BT_Field fld,ValueBase base,boolean oref)
{
   ValueBase s0 = null;
   boolean spl = MethodSpecial.isClassSpecial(fld.getDeclaringClass());

   flow_control.initialize(fld.getDeclaringClass());

   if (oref && !spl) {
      s0 = st.getFieldMap(fld);
      if (s0 != null) {
	 if (FlowMaster.doDebug()) System.err.println("\tUse local field map");
	 return s0;
       }
    }

   if (base != null && flow_control.detailField(fld) && !spl) {
      ValueBase iv = null;
      for (SourceBase src : base.getSources()) {
	 ValueBase fv = src.getFieldValue(fld);
	 if (fv != null) {
	    if (s0 == null) s0 = fv;
	    else s0 = s0.mergeValue(fv);
	  }
	 else if (iv == null) {
	    iv = jflow_master.initialFieldValue(fld,base.isNative());
	    if (!iv.mustBeNull()) flow_control.initialize(fld.type);
	  }
       }
      if (s0 == null) {
	 if (iv == null) {
	    iv = jflow_master.initialFieldValue(fld,base.isNative());
	    if (!iv.mustBeNull()) flow_control.initialize(fld.type);
	  }
	 s0 = iv;
       }
      else if (iv != null) {
//	 s0 = s0.mergeValue(iv);
       }

      if (s0 != null) {
	 return s0;
       }
    }

   s0 = field_map.get(fld);
   boolean nat = (base == null ? false : base.isNative()) | spl;

   if (s0 == null) {
      s0 = jflow_master.initialFieldValue(fld,nat);
      if (!s0.mustBeNull()) flow_control.initialize(fld.type);
      field_map.put(fld,s0);
    }
   else if (nat && s0.mustBeNull()) {
      ValueBase s1 = jflow_master.initialFieldValue(fld,nat);
      if (FlowMaster.doDebug()) {
	 System.err.println("\tSPECIAL CASE FIELD " + fld + " " + s0 + " => " + s1);
       }
      if (!s1.mustBeNull()) {
	 s0 = s0.mergeValue(s1);
	 field_map.put(fld,s0);
       }
    }

   return s0;
}




/********************************************************************************/
/*										*/
/*	Methods to set field values						*/
/*										*/
/********************************************************************************/

void addFieldInfo(StateBase st,BT_Field fld,MethodBase mthd,boolean oref,
			     ValueBase s0,ValueBase base,boolean set)
{
   BT_Method bm = mthd.getMethod();
   boolean spl = MethodSpecial.isClassSpecial(fld.getDeclaringClass());

   if (s0.mustBeNull()) {
      s0 = s0.restrictByClass(fld.type,false);
    }

   if (oref || bm.isStaticInitializer()) {
      if (!spl) st.setFieldMap(fld,s0);
    }

   if (bm.isStaticInitializer() && !field_map.containsKey(fld)) {
      if (!spl) field_map.put(fld,s0);
    }

   boolean chng = false;

   if (base != null && flow_control.detailField(fld) && !spl) {
      for (SourceBase src : base.getSources()) {
	 ValueBase vb = src.getFieldValue(fld);
	 if (set && (vb == null || vb.mustBeNull())) src.setFieldContents(s0,fld);
	 else chng |= src.addToFieldContents(s0,fld);
       }
    }

   ValueBase s1 = field_map.get(fld);
   if (set && s1 == null) {
      s1 = s0;
      field_map.put(fld,s0);
    }
   else if (s1 == null) {
      boolean nat = (base == null ? false : base.isNative());
      nat |= spl;
      s1 = jflow_master.initialFieldValue(fld,nat);
      field_map.put(fld,s1);
    }
   ValueBase s2 = s1.mergeValue(s0);
   if (!chng && s2 == s1) return;

   if (FlowMaster.doDebug()) {
      System.err.println("\tChange field value of " + fld + " = " + s2 + " " + spl);
    }

   field_map.put(fld,s2);

   handleFieldChanged(fld);
}




void handleFieldChanged(BT_Field fld)
{
   for (Enumeration<?> e = fld.accessors.elements(); e.hasMoreElements(); ) {
      BT_Accessor ba = (BT_Accessor) e.nextElement();
      switch (ba.instruction.opcode) {
	 case opc_putstatic :
	 case opc_putfield :
	    break;
	 default :
	    for (MethodBase cm : jflow_master.getAllMethods(ba.from)) {
	       flow_control.queueMethodChange(cm,ba.instruction);
	     }
	    break;
       }
    }
}





/********************************************************************************/
/*										*/
/*	Handle end of initialization						*/
/*										*/
/********************************************************************************/

void setupFields(StateBase st,ValueBase base,BT_Class bc)
{
   if (MethodSpecial.isClassSpecial(bc)) return;

   boolean isstatic = (base == null);
   Set<BT_Field> done = new HashSet<BT_Field>();

   for (BT_Field fld : st.getKnownFields()) {
      done.add(fld);
      if (fld.isStatic() == isstatic) {
	 ValueBase oval = field_map.get(fld);
	 ValueBase nval = st.getFieldMap(fld);
	 if (!nval.isEmptySourceSet()) {			// this check won't always work
	    if (base == null || !base.isUnique()) nval = nval.mergeValue(oval);
	    field_map.put(fld,nval);
	    if (base != null && flow_control.detailField(fld)) {
	       for (SourceBase cs : base.getSources()) {
		  cs.setFieldContents(nval,fld);
		}
	     }
	  }
       }
    }

   if (base != null) {
      for (Enumeration<?> e = bc.getFields().elements(); e.hasMoreElements(); ) {
	 BT_Field bf = (BT_Field) e.nextElement();
	 if (!bf.isStatic() && !done.contains(bf) && !field_map.containsKey(bf)) {
	    ValueBase ival = jflow_master.initialFieldValue(bf,base.isNative());
	    field_map.put(bf,ival);
	  }
       }
    }
}




void updateFields(StateBase st,BT_Class bc)
{
   for (Enumeration<?> e = bc.getFields().elements(); e.hasMoreElements(); ) {
      BT_Field bf = (BT_Field) e.nextElement();
      if (!bf.isStatic()) {
	 ValueBase cv = st.getFieldMap(bf);
	 if (cv == null) cv = jflow_master.initialFieldValue(bf,false);
	 ValueBase ov = field_map.get(bf);
	 ValueBase xv = cv.mergeValue(ov);
	 if (xv != ov) {
	    field_map.put(bf,xv);
	  }
       }
    }
}



void resetupFields(StateBase st,ValueBase base,BT_Class bc)
{
   if (MethodSpecial.isClassSpecial(bc)) return;

   Set<BT_Field> done = new HashSet<BT_Field>();

   for (BT_Field fld : st.getKnownFields()) {
      done.add(fld);
      if (!fld.isStatic()) {
	 ValueBase oval = field_map.get(fld);
	 ValueBase nval = st.getFieldMap(fld);
	 if (!nval.isEmptySourceSet()) {			// this check won't always work
	    ValueBase xval = nval;
	    xval = nval.mergeValue(oval);
	    field_map.put(fld,xval);
	    if (base != null && flow_control.detailField(fld)) {
	       for (SourceBase cs : base.getSources()) {
		  if (cs.isUnique()) cs.setFieldContents(nval,fld);
		  else if (cs.getFieldValue(fld) == null) cs.setFieldContents(nval,fld);
		  else cs.addToFieldContents(nval,fld);
		}
	     }
	  }
       }
    }
}


/********************************************************************************/
/*										*/
/*	Handle end of synchronized region					*/
/*										*/
/********************************************************************************/

void handleEndSync(StateBase st,MethodBase bm)
{
   if (bm.getThisValue() != null && st.getLocal(0) != null && st.getLocal(0).isUnique())
      return;

   st.discardFields();
}




/********************************************************************************/
/*										*/
/*	Handle .length field for arrays 					*/
/*										*/
/********************************************************************************/

ValueBase getArrayLength(ValueBase _base)
{
   ValueBase lv = jflow_master.anyValue(BT_Class.getInt());

   return lv;
}



}	// end of class FlowField




/* end of FlowField.java */
