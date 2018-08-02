/********************************************************************************/
/*										*/
/*		JflowDefaultMethodData.java					*/
/*										*/
/*	Holder for values associated with a method during flow analysis 	*/
/*										*/
/********************************************************************************/
/*	Copyright 2006 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2006, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/JflowDefaultMethodData.java,v 1.4 2015/11/20 15:09:13 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JflowDefaultMethodData.java,v $
 * Revision 1.4  2015/11/20 15:09:13  spr
 * Reformatting.
 *
 * Revision 1.3  2007-05-04 01:59:52  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.2  2007-01-03 03:24:15  spr
 * Modifications to handle incremental update.
 *
 * Revision 1.1  2006/07/10 14:52:15  spr
 * Code cleanup.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.jflow;

import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_Opcodes;

import java.util.HashMap;
import java.util.Map;



public class JflowDefaultMethodData implements JflowMethodData, JflowConstants, BT_Opcodes {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JflowMethod	for_method;

private Map<AssociationType,Map<BT_Ins,JflowValue>> data_map;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public JflowDefaultMethodData(JflowMethod jm)
{
   for_method = jm;
   data_map = new HashMap<AssociationType,Map<BT_Ins,JflowValue>>();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

protected JflowMethod getMethod()		{ return for_method; }



@Override public JflowValue getAssociation(AssociationType typ,BT_Ins ins)
{
   Map<BT_Ins,JflowValue> mm = data_map.get(typ);

   if (mm == null) return null;

   return mm.get(ins);
}



/********************************************************************************/
/*										*/
/*	Set methods								*/
/*										*/
/********************************************************************************/

@Override public void setAssociation(AssociationType typ,BT_Ins ins,JflowValue v)
{
   if (!useAssociation(typ,ins,v)) return;

   Map<BT_Ins,JflowValue> mm = data_map.get(typ);
   if (mm == null) {
      mm = new HashMap<>();
      data_map.put(typ,mm);
    }
   mm.put(ins,v);
}



/********************************************************************************/
/*										*/
/*	Checking methods							*/
/*										*/
/********************************************************************************/

protected boolean useAssociation(AssociationType typ,BT_Ins ins,JflowValue v)
{
   return false;
}


/********************************************************************************/
/*										*/
/*	Update methods								*/
/*										*/
/********************************************************************************/

@Override public void clear()
{
   data_map = new HashMap<AssociationType,Map<BT_Ins,JflowValue>>();
}



@Override public void updateValues(Map<JflowValue,JflowValue> updates)
{
   for (Map<BT_Ins,JflowValue> mp : data_map.values()) {
      for (Map.Entry<BT_Ins,JflowValue> ent : mp.entrySet()) {
	 if (updates.containsKey(ent.getValue())) {
	    ent.setValue(updates.get(ent.getValue()));
	  }
       }
    }
}




}	// end of class PabstMethodData




/* end of PabstMethodData.java */
