/********************************************************************************/
/*										*/
/*		SourceField.java						*/
/*										*/
/*	Source class representing a field					*/
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

import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_Field;

import java.util.HashMap;
import java.util.Map;




class SourceField extends SourceBase
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private BT_Field access_field;
private ValueBase for_object;

private static Map<BT_Field,Map<Object,SourceField>> field_sources = new HashMap<BT_Field,Map<Object,SourceField>>();

private final static Object STATIC_FIELD = new Object();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private SourceField(FlowMaster jm,BT_Field fld,ValueBase obj) {
   super(jm);
   access_field = fld;
   for_object = obj;
}




/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

static SourceBase getFieldSourceObject(FlowMaster jm,BT_Field fld,ValueBase obj)
{
   Map<Object,SourceField> m = field_sources.get(fld);
   if (m == null) {
      m = new HashMap<Object,SourceField>();
      field_sources.put(fld,m);
    }

   Object key = (obj == null ? STATIC_FIELD : obj);

   SourceField src = m.get(key);
   if (src == null) {
      if (obj != null) {
	 for (SourceField xsrc : m.values()) {
	    if (xsrc.overlaps(obj)) {
	       src = xsrc;
	       break;
	     }
	  }
       }
      if (src == null) src = new SourceField(jm,fld,obj);
      m.put(key,src);
    }

   return src;
}





/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

private boolean overlaps(ValueBase obj) {
   if (obj == null && for_object == null) return true;
   if (obj == null || for_object == null) return false;
   return for_object.getSourceSet().overlaps(obj.getSourceSet());
}



@Override public BT_Field getFieldSource()		{ return access_field; }




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw,String cid) {
   xw.begin("SOURCE");
   xw.field("TYPE","FIELD");
   xw.field("FIELD",access_field.fullName());
   if (cid != null) xw.field("CID",cid);
   xw.end();
}




@Override public String toString() {
   return "Field " + access_field.fullName();
}




}	// end of class SourceField




/* end of SourceField.java */
