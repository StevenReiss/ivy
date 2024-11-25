/********************************************************************************/
/*										*/
/*		ModelCondition.java						*/
/*										*/
/*	Class representing a condition field for the generated automata 	*/
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


package edu.brown.cs.ivy.jflow.model;


import edu.brown.cs.ivy.jflow.JflowModel;
import edu.brown.cs.ivy.jflow.JflowSource;



class ModelCondition implements JflowModel.Field {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private ConditionType cond_type;
private JflowSource field_source;
private Object field_value;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelCondition(ConditionType ct,JflowSource src)
{
   cond_type = ct;
   field_source = src;
   field_value = null;
}



ModelCondition(JflowSource src,Object val)
{
   cond_type = ConditionType.NONE;
   field_source = src;
   field_value = val;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public ConditionType getConditionType() 	     { return cond_type; }

@Override public JflowSource getFieldSource()		     { return field_source; }

@Override public Object getFieldValue()			     { return field_value; }




/********************************************************************************/
/*										*/
/*	Comparison methods							*/
/*										*/
/********************************************************************************/

@Override public boolean equals(Object o) {
   if (o == null || !(o instanceof ModelCondition)) return false;
   ModelCondition c = (ModelCondition) o;
   if (cond_type != c.cond_type || field_source != c.field_source) return false;

   if (field_value == c.field_value) return true;
   if (field_value != null && field_value.equals(c.field_value)) return true;

   return false;
}



@Override public int hashCode() {
   int hvl = 0;
   if (cond_type != null) hvl += cond_type.hashCode();
   if (field_source != null) hvl += field_source.hashCode();
   if (field_value != null) hvl += field_value.hashCode();
   return hvl;
}




}	// end of class ModelCondition




/* end of ModelCondition.java */
