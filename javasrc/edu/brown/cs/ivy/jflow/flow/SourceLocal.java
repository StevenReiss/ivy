/********************************************************************************/
/*										*/
/*		SourceLocal.java						*/
/*										*/
/*	Source representing the result of a new for local flow analysis 	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/SourceLocal.java,v 1.6 2015/11/20 15:09:14 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SourceLocal.java,v $
 * Revision 1.6  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.5  2011-04-13 21:03:14  spr
 * Fix bugs in flow analysis.
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


import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;



class SourceLocal extends SourceObject
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private MethodBase for_method;
private int ins_number;
private boolean is_unique;
private SourceObject non_unique;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SourceLocal(FlowMaster jm,MethodBase cm,int ino,BT_Class cls,boolean uniq)
{
   super(jm,cls);
   for_method = cm;
   ins_number = ino;
   is_unique = uniq;
   non_unique = null;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override MethodBase getSourceMethod()			{ return for_method; }
@Override int getSourceInstruction()			{ return ins_number; }



/********************************************************************************/
/*										*/
/*	Unique source methods							*/
/*										*/
/********************************************************************************/

@Override boolean isUnique()					{ return is_unique; }

@Override SourceBase makeNonunique()
{
   if (!is_unique) return this;

   if (non_unique == null) {
      non_unique = new SourceLocal(jflow_master,for_method,ins_number,getDataType(),false);
      copyFields(non_unique);
    }

   return non_unique;
}


@Override SourceBase getNonunique()				{ return non_unique; }




@Override void setFieldContents(ValueBase cv,BT_Field fld)
{
   if (non_unique != null) non_unique.setFieldContents(cv,fld);
   super.setFieldContents(cv,fld);
}



@Override boolean addToFieldContents(ValueBase cv,BT_Field fld)
{
   boolean fg = false;

   if (non_unique != null) {
      fg |= non_unique.addToFieldContents(cv,fld);
    }

   fg |= super.addToFieldContents(cv,fld);

   return fg;
}


@Override ValueBase getFieldValue(BT_Field fld)
{
   if (non_unique != null) return non_unique.getFieldValue(fld);
   return super.getFieldValue(fld);
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw,String id) {
   xw.begin("SOURCE");
   xw.field("TYPE","NEW");
   if (for_method != null) {
      xw.field("METHOD",for_method.getMethodName());
      xw.field("SIGNATURE",for_method.getMethodSignature());
      xw.field("INSTRUCTION",ins_number);
    }
   xw.field("CLASS",getDataType().getName());
   if (id != null) xw.field("CID",id);
   xw.end();
}



@Override public String toString() {
   String uniq = (is_unique ? "New*" : "New");
   String loc = "NONE";
   if (for_method != null) {
      loc = Integer.toString(ins_number) + " " + for_method.getMethodName();
    }

   return "Local " + uniq + " " + getDataType().getName() + " @ " + loc + " " + hashCode();
}




}	// end of class SourceLocal




/* end of SourceLocal.java */
