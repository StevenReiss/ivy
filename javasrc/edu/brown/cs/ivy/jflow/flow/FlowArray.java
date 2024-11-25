/********************************************************************************/
/*										*/
/*		FlowArray.java							*/
/*										*/
/*	Methods for handling array values					*/
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
import edu.brown.cs.ivy.jflow.JflowFlags;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_Opcodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;



class FlowArray implements JflowConstants, BT_Opcodes
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private FlowControl	flow_control;
private Map<SourceBase,Map<MethodBase,Set<BT_Ins>>> source_map;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

FlowArray(FlowMaster jm,FlowControl fc)
{
   jflow_master = jm;
   flow_control = fc;
   source_map = new HashMap<SourceBase,Map<MethodBase,Set<BT_Ins>>>();
}



/********************************************************************************/
/*										*/
/*	Methods to handle creating array values 				*/
/*										*/
/********************************************************************************/

ValueBase newArraySet(MethodBase cm,BT_Ins ins,int ino,BT_Class acls,int dim,ValueBase sz)
{
   SourceBase as = cm.getArray(ins);

   if (as == null) {
      SourceBase as1 = null;
      BT_Class bcls = acls;
      for (int i = 0; i < dim; ++i) {
	 as = jflow_master.createArraySource(bcls,sz);
	 sz = null;
	 if (as1 != null) {
	    ValueBase cv = jflow_master.objectValue(bcls,jflow_master.createSingletonSet(as1),JflowFlags.NON_NULL);
	    as.setArrayContents(cv);
	  }
	 String nm = bcls.getName();
	 nm += "[]";
	 bcls = BT_Class.forName(nm);
	 as1 = as;
       }
      cm.setArray(ins,as);
    }

   String nm = acls.getName();
   for (int i = 0; i < dim; ++i) nm += "[]";
   BT_Class arr = BT_Class.forName(nm);

   if (FlowMaster.doDebug()) System.err.println("\tArray set = " + as + " " + acls.getName());

   return jflow_master.objectValue(arr,jflow_master.createSingletonSet(as),JflowFlags.NON_NULL);
}




/********************************************************************************/
/*										*/
/*	Methods to handle load from array					*/
/*										*/
/********************************************************************************/

ValueBase getArraySet(MethodBase cm,BT_Ins ins,ValueBase aset,ValueBase idx)
{
   ValueBase cv = null;
   boolean nat = false;

   for (SourceBase xs : aset.getSources()) {
      if (xs.isArray()) {
	 addReferenceMethod(xs,cm,ins);
	 ValueBase cv1 = xs.getArrayValues(idx);
	 if (cv == null) cv = cv1;
	 else cv = cv.mergeValue(cv1);
       }
      else if (xs.isNative()) nat = true;
    }

   if (cv == null) {			// no sources identified
      BT_Class base;
      switch (ins.opcode) {
	 default :
	 case opc_aaload :
	    base = aset.getDataType();
	    if (base == null || !base.isArray()) base = BT_Class.findJavaLangObject();
	    else {
	       String s = base.getName();
	       int i = s.lastIndexOf('[');
	       s = s.substring(0,i);
	       base = BT_Class.forName(s);
	     }
	    if (nat) cv = jflow_master.nativeValue(base);
	    else cv = jflow_master.nullValue(base);
	    break;
	 case opc_baload :
	    base = BT_Class.getByte();
	    break;
	 case opc_caload :
	    base = BT_Class.getChar();
	    break;
	 case opc_daload :
	    base = BT_Class.getDouble();
	    break;
	 case opc_faload :
	    base = BT_Class.getFloat();
	    break;
	 case opc_iaload :
	    base = BT_Class.getInt();
	    break;
	 case opc_laload :
	    base = BT_Class.getLong();
	    break;
	 case opc_saload :
	    base = BT_Class.getShort();
	    break;
       }
      if (cv == null) cv = jflow_master.anyValue(base);
    }

   return cv;
}





/********************************************************************************/
/*										*/
/*	Methods to handle storing into arrays					*/
/*										*/
/********************************************************************************/

void addToArraySet(ValueBase aset,ValueBase val,ValueBase idx)
{
   if (val.isBad()) return;

   if (FlowMaster.doDebug()) {
      System.err.println("\tAdd to array set " + aset + " [ " + idx + " ] = " + val);
    }

   for (SourceBase cs : aset.getSources()) {
      if (cs.isArray()) {
	 if (cs.addToArrayContents(val,idx)) {
	    noteArrayChange(cs);
	  }
       }
    }
}




/********************************************************************************/
/*										*/
/*	Methods to handle array copy						*/
/*										*/
/********************************************************************************/

void handleArrayCopy(List<ValueBase> args,MethodBase caller,BT_Ins calins)
{
   ValueBase src = null;
   ValueBase dst = null;

   int idx = 0;
   for (Iterator<ValueBase> it = args.iterator(); it.hasNext(); ) {
      ValueBase cv = it.next();
      if (idx == 0) src = cv;
      else if (idx == 2) dst = cv;
      ++idx;
    }

   if (src == null || dst == null) return;

   ValueBase sval = src.getArrayContents();
   if (FlowMaster.doDebug()) System.err.println("\tCOPY SOURCE = " + sval);

   if (sval != null) {
      for (SourceBase ssrc : src.getSources()) {
	 addReferenceMethod(ssrc,caller,calins);
       }

      for (SourceBase dsrc : dst.getSources()) {
	 if (dsrc.addToArrayContents(sval,null)) {
	    noteArrayChange(dsrc);
	  }
       }
    }
}



/********************************************************************************/
/*										*/
/*	Methods to handle changes to array values				*/
/*										*/
/********************************************************************************/

void noteArrayChange(SourceBase as)
{
   Map<MethodBase,Set<BT_Ins>> m = source_map.get(as);
   if (m == null) return;

   for (Map.Entry<MethodBase,Set<BT_Ins>> ent : m.entrySet()) {
      MethodBase cm = ent.getKey();
      Set<BT_Ins> s = ent.getValue();
      for (BT_Ins ins : s) {
	 flow_control.queueMethodChange(cm,ins);
	 if (FlowMaster.doDebug()) System.err.println("\tArray change method " + cm.getMethodName());
       }
    }
}




/********************************************************************************/
/*										*/
/*	handle tracking usage							*/
/*										*/
/********************************************************************************/

private void addReferenceMethod(SourceBase cs,MethodBase bm,BT_Ins ins)
{
   Map<MethodBase,Set<BT_Ins>> m = source_map.get(cs);
   if (m == null) {
      m = new HashMap<MethodBase,Set<BT_Ins>>(4);
      source_map.put(cs,m);
    }
   Set<BT_Ins> s = m.get(bm);
   if (s == null) {
      s = new HashSet<BT_Ins>(4);
      m.put(bm,s);
    }
   s.add(ins);
}





}	// end of class FlowArray




/* end of FlowArray.java */
