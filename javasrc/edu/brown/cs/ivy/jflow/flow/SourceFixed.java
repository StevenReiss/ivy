/********************************************************************************/
/*										*/
/*		SourceFixed.java						*/
/*										*/
/*	Source representing a native or fixed value				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/SourceFixed.java,v 1.8 2015/11/20 15:09:14 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SourceFixed.java,v $
 * Revision 1.8  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.7  2013/09/24 01:06:56  spr
 * Minor fix
 *
 * Revision 1.6  2007-01-03 14:04:59  spr
 * Fix imports
 *
 * Revision 1.5  2007-01-03 03:24:18  spr
 * Modifications to handle incremental update.
 *
 * Revision 1.4  2006-12-01 03:22:47  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.3  2006-08-30 00:43:51  spr
 * Fix bugs with mutable sources.
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


import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_Class;



class SourceFixed extends SourceObject
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private boolean is_mutable;
private ValueBase base_value;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SourceFixed(FlowMaster jm,BT_Class bc,boolean mut)
{
   super(jm,bc);
   base_value = null;
   is_mutable = mut;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public boolean isNative()			{ return true; }



@Override public ValueBase getArrayValues(ValueBase aidx)
{
   ValueBase cv = super.getArrayValues(aidx);
   if (cv != null) return cv;

   if (getDataType().isArray() && base_value == null) {
      String s = getDataType().getName();
      int idx = s.lastIndexOf('[');
      s = s.substring(0,idx);
      BT_Class bty = BT_Class.forName(s);
      if (is_mutable || bty.isAbstract()) base_value = jflow_master.mutableValue(bty);
      else base_value = jflow_master.nativeValue(bty);
    }

   return base_value;
}




/********************************************************************************/
/*										*/
/*	Methods to support mutable native values				*/
/*										*/
/********************************************************************************/

@Override public SourceBase mutateTo(BT_Class type)
{
   if (is_mutable && type.isDerivedFrom(getDataType())) {
      if (type.isInterface() || type.isAbstract())
	 return jflow_master.createMutableSource(type);
      return jflow_master.createFixedSource(type);
    }
   if (getDataType().isInterface() || getDataType().isAbstract()) {
      if (type.isInterface()) {
	 BT_Class c = findChildForInterface(getDataType(),type);
	 if (c != null) return jflow_master.createFixedSource(c);
       }
      else if (type.isDerivedFrom(getDataType())) return jflow_master.createFixedSource(type);
    }
   else if (is_mutable && getDataType() == BT_Class.findJavaLangObject()) {
      if (type.isInterface() || type.isAbstract()) {
	 return jflow_master.createMutableSource(type);
       }
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw,String id)
{
   xw.begin("SOURCE");
   xw.field("TYPE","FIXED");
   xw.field("CLASS",getDataType().getName());
   if (id != null) xw.field("CID",id);
   xw.end();
}




@Override public String toString()
{
   StringBuffer buf = new StringBuffer();

   buf.append("Fixed");
   if (is_mutable) buf.append("*");
   buf.append(" ");
   buf.append(getDataType().getName());

   return buf.toString();
}





}	// end of class SourceFixed




/* end of SourceFixed.java */

