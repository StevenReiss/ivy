/********************************************************************************/
/*										*/
/*		SourceObject.java						*/
/*										*/
/*	Source representing an object value of some sort (abstract)		*/
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


import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;

import java.util.HashMap;
import java.util.Map;


abstract class SourceObject extends SourceBase
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private BT_Class		data_type;
private Map<BT_Field,ValueBase> field_map;

private boolean 		array_nonnull;
private boolean 		array_canbenull;
private ValueBase		array_value;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected SourceObject(FlowMaster jm,BT_Class bc)
{
   super(jm);

   data_type = bc;
   field_map = new HashMap<BT_Field,ValueBase>(4);
   array_value = null;
   array_nonnull = false;
   array_canbenull = false;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public BT_Class getDataType()				{ return data_type; }

@Override boolean isArray()					{ return data_type.isArray(); }




/********************************************************************************/
/*										*/
/*	Field management methods						*/
/*										*/
/********************************************************************************/

@Override void setFieldContents(ValueBase cv,BT_Field fld)
{
   field_map.put(fld,cv);
}



@Override boolean addToFieldContents(ValueBase cv,BT_Field fld)
{
   ValueBase s1 = field_map.get(fld);
   if (s1 == null) {
      s1 = jflow_master.initialFieldValue(fld,isNative());
      field_map.put(fld,s1);
    }

   ValueBase s2 = s1.mergeValue(cv);
   if (s2 == s1) return false;

   field_map.put(fld,s2);

   return true;
}



@Override ValueBase getFieldValue(BT_Field fld)
{
   return field_map.get(fld);
}



protected void copyFields(SourceObject toobj)
{
   if (FlowMaster.doDebug()) System.err.println("Copy fields for " + this + " => " + toobj);

   for (Map.Entry<BT_Field,ValueBase> ent : field_map.entrySet()) {
      if (FlowMaster.doDebug()) System.err.println("Copy field " + ent.getKey() + " = " + ent.getValue());
      toobj.setFieldContents(ent.getValue(),ent.getKey());
    }
}




/********************************************************************************/
/*										*/
/*	Array management methods						*/
/*										*/
/********************************************************************************/

@Override void setArrayContents(ValueBase cv)
{
   array_value = cv;
}



@Override boolean addToArrayContents(ValueBase cv,ValueBase idx)
{
   if (cv == null) return false;

   cv = cv.mergeValue(array_value);

   if (cv == array_value) return false;

   array_value = cv;

   return true;
}



@Override ValueBase getArrayValues(ValueBase idx)
{
   if (array_nonnull && !array_canbenull && array_value != null) return array_value.forceNonNull();

   return array_value;
}


@Override boolean markArrayAccess(AccessSafety as)
{
   if (array_value == null) return false;

   boolean chng = false;

   switch (as) {
      case USED :
      case CHECKED_USED :
	 if (array_value.canBeNull() && !array_nonnull && !array_canbenull) {
	    array_nonnull = true;
	    chng = true;
	  }
	 break;
      default:
	 break;
    }

   switch (as) {
      case CHECKED :
      case CHECKED_USED :
	 if (!array_canbenull) {
	    array_canbenull = true;
	    if (array_nonnull) chng = true;
	  }
	 break;
      default:
	 break;
    }

   return chng;
}





}	// end of abstract class SourceObject





/* end of SourceObject.java */
