/********************************************************************************/
/*										*/
/*		SourceModel.java						*/
/*										*/
/*	General superclass for sources derived from the user model		*/
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

import edu.brown.cs.ivy.jflow.JflowModelSource;
import edu.brown.cs.ivy.xml.IvyXmlWriter;




class SourceModel extends SourceBase
{




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private JflowModelSource model_source;
private MethodBase for_method;
private int ins_number;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SourceModel(FlowMaster jm,JflowModelSource src,MethodBase cm,int ino)
{
   super(jm);

   model_source = src;
   for_method = cm;
   ins_number = ino;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public JflowModelSource getModelSource()		{ return model_source; }





/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw,String cid)
{
   xw.begin("SOURCE");
   xw.field("METHOD",for_method.getMethodName());
   xw.field("SIGNATURE",for_method.getMethodSignature());
   xw.field("INSTANCE",for_method.getInstanceNumber());
   xw.field("INSTRUCTION",ins_number);
   xw.field("LINE",jflow_master.findLineNumber(for_method,ins_number));
   xw.field("ID","JFLOWSRC_" + getId());
   if (cid != null) xw.field("CID",cid);
   String f = for_method.getMethodClass().getSourceFile();
   if (f != null) xw.field("FILE",f);
   xw.field("TYPE",model_source.getModelSourceType());
   model_source.outputLocalXml(xw);
   xw.end();
}

void outputLocalXml(IvyXmlWriter xw)	        { }




@Override public String toString() {
   return "Model " + model_source.toString() + " @ " + for_method.getMethodName() + "::" + ins_number;
}



}	// end of class SourceModel





/* end of SourceModel.java */
