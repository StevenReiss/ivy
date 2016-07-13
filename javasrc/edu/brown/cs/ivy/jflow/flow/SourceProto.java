/********************************************************************************/
/*										*/
/*		SourceProto.java						*/
/*										*/
/*	Class for managing source prototypes					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/SourceProto.java,v 1.5 2015/11/20 15:09:14 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SourceProto.java,v $
 * Revision 1.5  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.4  2007-01-03 14:04:59  spr
 * Fix imports
 *
 * Revision 1.3  2007-01-03 03:24:18  spr
 * Modifications to handle incremental update.
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

import edu.brown.cs.ivy.jflow.JflowValue;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;

import java.util.Collection;
import java.util.Map;



class SourceProto extends SourceBase
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private ProtoBase	proto_handler;

private BT_Class	data_type;
private MethodBase	for_method;
private int		ins_number;
private boolean 	is_mutable;
private boolean 	is_native;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SourceProto(FlowMaster jm,MethodBase cm,int ino,BT_Class cls,ProtoBase proto)
{
   super(jm);

   data_type = cls;
   for_method = cm;
   ins_number = ino;
   proto_handler = proto;
   is_native = false;
   is_mutable = false;
}




SourceProto(FlowMaster jm,BT_Class cls,ProtoBase proto,boolean mutable)
{
   super(jm);

   data_type = cls;
   for_method = null;
   ins_number = -1;
   proto_handler = proto;
   is_mutable = mutable;
   is_native = true;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public BT_Class getDataType()			{ return data_type; }

@Override public boolean isArray()			{ return data_type.isArray(); }

@Override public ProtoBase getPrototype() 		{ return proto_handler; }

@Override public boolean isNative()			{ return is_native; }





/********************************************************************************/
/*										*/
/*	Field methods								*/
/*										*/
/********************************************************************************/

@Override public void setFieldContents(ValueBase cv,BT_Field fld)
{
   proto_handler.setField(fld,cv);
}


@Override public boolean addToFieldContents(ValueBase cv,BT_Field fld)
{
   return proto_handler.addToField(fld,cv);
}



@Override public ValueBase getFieldValue(BT_Field fld)
{
   return proto_handler.getField(fld);
}



/********************************************************************************/
/*										*/
/*	Array methods								*/
/*										*/
/********************************************************************************/

@Override public void setArrayContents(ValueBase cv)
{
   proto_handler.setArrayContents(null,cv);
}



@Override public boolean addToArrayContents(ValueBase cv,ValueBase idx)
{
   return proto_handler.setArrayContents(idx,cv);
}



@Override public ValueBase getArrayValues(ValueBase idx)
{
   return proto_handler.getArrayContents(idx);
}




/********************************************************************************/
/*										*/
/*	Mutation methods							*/
/*										*/
/********************************************************************************/

@Override public SourceBase mutateTo(BT_Class type)
{
   BT_Class rslt = null;
   boolean mut = false;

   if (is_mutable && type.isDerivedFrom(getDataType())) rslt = type;
   if (getDataType().isInterface() || getDataType().isAbstract()) {
      if (type.isInterface()) {
	 BT_Class c = findChildForInterface(getDataType(),type);
	 if (c != null) rslt = c;
       }
      else if (type.isDerivedFrom(getDataType())) rslt = type;
    }
   else if (is_mutable && getDataType() == BT_Class.findJavaLangObject()) {
      if (type.isInterface() || type.isAbstract()) {
	 rslt = type;
	 mut = true;
       }
    }

   if (rslt != null) {
      if (is_mutable)
	 return new SourceProto(jflow_master,rslt,proto_handler,mut);
      else if (FlowMaster.doDebug()) System.err.println("TRY MUTATE PROTOTYPE TO " + rslt);
    }

   return null;
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
   proto_handler.handleUpdates(oldsrcs,srcupdates,valupdates);
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw,String id)
{
   xw.begin("SOURCE");
   xw.field("TYPE","PROTO");
   if (for_method != null) {
      xw.field("METHOD",for_method.getMethodName());
      xw.field("SIGNATURE",for_method.getMethodSignature());
      xw.field("INSTRUCTION",ins_number);
    }
   xw.field("CLASS",getDataType().getName());
   if (id != null) xw.field("CID",id);
   xw.end();
}



@Override public String toString()
{
   StringBuffer buf = new StringBuffer();

   buf.append("Proto New ");
   if (getDataType() != null) buf.append(getDataType().getName());
   else buf.append("<<no type>>");
   buf.append(" @ ");
   buf.append(ins_number);
   if (for_method != null) {
      buf.append(" ");
      buf.append(for_method.getMethodName());
    }

   return buf.toString();
}



}	// end of class SourceProto




/* end of SourceProto.java */
