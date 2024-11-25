/********************************************************************************/
/*										*/
/*		SourceArray.java						*/
/*										*/
/*	Source representing an array						*/
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


import edu.brown.cs.ivy.jflow.JflowValue;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_Class;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;



class SourceArray extends SourceBase
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private BT_Class base_class;
private ValueBase array_values;
private Map<Object,ValueBase> value_map;
private ValueBase size_value;
private BT_Class data_type;

private boolean null_stored;
private AccessSafety content_access;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SourceArray(FlowMaster jm,BT_Class acls,ValueBase sz)
{
   super(jm);

   base_class = acls;
   if (sz == null) sz = jflow_master.anyValue(BT_Class.getInt());
   size_value = sz;
   data_type = BT_Class.forName(base_class + "[]");

   if (base_class.isPrimitive()) array_values = jflow_master.rangeValue(base_class,0,0);
   else array_values = jflow_master.nullValue(base_class);
   value_map = new HashMap<Object,ValueBase>();

   null_stored = false;
   content_access = AccessSafety.NONE;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public BT_Class getDataType()		{ return data_type; }

@Override ValueBase getArrayLength()		{ return size_value; }

@Override boolean isArray()			{ return true; }




/********************************************************************************/
/*										*/
/*	Methods for handling contents						*/
/*										*/
/********************************************************************************/

@Override //@ require cv != null;

void setArrayContents(ValueBase cv)
{
   array_values = cv;

   null_stored = cv.canBeNull();
}


@Override boolean addToArrayContents(ValueBase cs,ValueBase idx)
{
   if (cs != null) {
      null_stored |= cs.mustBeNull();
      ValueBase ncs = cs.restrictByClass(base_class,false);
      if (cs != ncs) cs = ncs;
    }

   if (idx != null) {
      Object lv = idx.getProgramValue();
      if (lv != null && lv instanceof Long) {
	 ValueBase nv = cs;
	 ValueBase ov = value_map.get(lv);
	 if (ov != null) nv = ov.mergeValue(nv);
	 value_map.put(lv,nv);
       }
    }

   ValueBase ns = array_values.mergeValue(cs);
   if (ns == array_values) return false;
   array_values = ns;

   return true;
}



@Override ValueBase getArrayValues(ValueBase idx)
{
   if (idx != null) {
      Object lv = idx.getProgramValue();
      if (lv != null && lv instanceof Long) {
	 ValueBase ov = value_map.get(lv);
	 if (ov != null) {
	    return ov;
	  }
       }
    }

   ValueBase rslt = array_values;

   if (content_access.isUsed() && !null_stored) rslt = rslt.forceNonNull();

   return rslt;
}



@Override boolean markArrayAccess(AccessSafety as)
{
   if (null_stored) return false;		// if null stored explicit, leave it

   boolean chng = false;

   if (as.isUsed()) {
      if (array_values.canBeNull() && content_access == AccessSafety.NONE) {
	 content_access = AccessSafety.USED;
	 chng = true;
       }
    }
   if (as.isChecked() && !content_access.isChecked()) {
      content_access = content_access.merge(AccessSafety.CHECKED);
      if (!array_values.canBeNull() && !content_access.isUsed()) chng = true;
    }

   return chng;
}



/********************************************************************************/
/*										*/
/*	Methods for incremental update						*/
/*										*/
/********************************************************************************/

@Override void handleUpdates(Collection<SourceBase> oldsrcs,
		      Map<SourceSet,SourceSet> srcupdates,
		      Map<JflowValue,JflowValue> valupdates)
{
   if (array_values != null) {
      JflowValue jv = valupdates.get(array_values);
      if (jv != null) array_values = (ValueBase) jv;
    }
   for (Map.Entry<Object,ValueBase> ent : value_map.entrySet()) {
      JflowValue jv = ent.getValue();
      if (jv != null) {
	 jv = valupdates.get(jv);
	 if (jv != null) ent.setValue((ValueBase) jv);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw,String id)	{ }


@Override public String toString()
{
   StringBuffer buf = new StringBuffer();
   buf.append("[Array ");
   buf.append(data_type.getName());
   buf.append(" :: ");
   buf.append(array_values.toString());
   buf.append(" ]");
   return buf.toString();
}





}	// end of class SourceArray




/* end of SourceArray.java */

