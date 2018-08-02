/********************************************************************************/
/*										*/
/*		ModelValue.java 						*/
/*										*/
/*	Holder for values during model generation				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/model/ModelValue.java,v 1.5 2015/11/20 15:09:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ModelValue.java,v $
 * Revision 1.5  2015/11/20 15:09:16  spr
 * Reformatting.
 *
 * Revision 1.4  2007-05-04 02:00:03  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.3  2006-12-01 03:22:49  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.2  2006/07/10 14:52:19  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:37  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.model;

import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowFlags;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



abstract class ModelValue implements JflowConstants, BT_Opcodes
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/




/********************************************************************************/
/*										*/
/*	Static methods to assist in creating values of various types		*/
/*										*/
/********************************************************************************/

static ModelValue anyObject(BT_Class cls)	{ return objectValue(cls,JflowFlags.CAN_BE_NULL,null); }

static ModelValue anyNewObject(BT_Class cls)	{ return objectValue(cls,JflowFlags.NON_NULL,null); }

static ModelValue nullValue()
{
   return objectValue(BT_Class.findJavaLangObject(),JflowFlags.NULL,null);
}

static ModelValue intValue(BT_Class cls,long v) 		{ return rangeValue(cls,v,v); }
static ModelValue floatValue(BT_Class c,double v)		{ return rangeValue(c,v,v); }

static ModelValue constantString(String s)
{
   Set<String> ss = new HashSet<String>(1);
   ss.add(s);
   return objectValue(BT_Class.forName("java.lang.String"),JflowFlags.NON_NULL,ss);
}


static ModelValue anyValue(BT_Class cls)
{
   if (cls.isPrimitive()) {
      if (cls == BT_Class.getVoid()) return badValue();
		return numericValue(cls);
    }
   return objectValue(cls,JflowFlags.CAN_BE_NULL,null);
}


static ModelValue createValue(JflowValue jv)
{
   BT_Class cls = jv.getDataType();

   if (cls.isPrimitive()) {
      Object o = jv.getProgramValue();
      if (o != null && o instanceof Long) {
	 return intValue(cls,((Long) o).longValue());
       }
      return anyValue(cls);
    }

   return objectValue(cls,jv.getFlags(),null);
}



static ModelValue copyValue(BT_Class c,ModelValue mv)
{
   return objectValue(c,mv.getFlags(),mv.getStrings());
}




/********************************************************************************/
/*										*/
/*	Static methods that actually create values				*/
/*										*/
/********************************************************************************/

private static Map<BT_Class,List<ModelValue>> value_map;
private static ModelValue bad_value;

static {
   value_map = new HashMap<BT_Class,List<ModelValue>>();
   bad_value = new ModelBadValue();
}


static ModelValue objectValue(BT_Class c,short flags,Set<String> strings)
{
   List<ModelValue> vl = getValues(c);

   flags = JflowFlags.fixup(flags);

   for (ModelValue mv : vl) {
      if (mv.getFlags() == flags) {
	 if (strings == null) {
	    if (mv.getStrings() == null) return mv;
	  }
	 else if (strings.equals(mv.getStrings())) return mv;
       }
    }

   ModelValue mv = new ModelObjectValue(c,flags,strings);
   vl.add(mv);
   return mv;
}




static ModelValue rangeValue(BT_Class cls,long v,long v1)
{
   List<ModelValue> vl = getValues(cls);

   for (ModelValue mv : vl) {
      if (mv instanceof ModelNumericValue) {
	 ModelNumericValue nv = (ModelNumericValue) mv;
	 if (nv.match(v,v1)) return nv;
       }
    }

   ModelValue mv = new ModelNumericValue(cls,v,v1);
   vl.add(mv);
   return mv;
}



static ModelValue rangeValue(BT_Class cls,double v,double v1)
{
   List<ModelValue> vl = getValues(cls);

   for (ModelValue mv : vl) {
      if (mv instanceof ModelNumericValue) {
	 ModelNumericValue nv = (ModelNumericValue) mv;
	 if (nv.match(v,v1)) return nv;
       }
    }

   ModelValue mv = new ModelNumericValue(cls,v,v1);
   vl.add(mv);
   return mv;
}



static ModelValue numericValue(BT_Class cls)
{
   List<ModelValue> vl = getValues(cls);

   for (ModelValue mv : vl) {
      if (mv instanceof ModelNumericValue) {
	 ModelNumericValue nv = (ModelNumericValue) mv;
	 if (nv.matchAny()) return nv;
       }
    }

   ModelValue mv = new ModelNumericValue(cls);
   vl.add(mv);
   return mv;
}



static ModelValue badValue()
{
   return bad_value;
}



private static List<ModelValue> getValues(BT_Class c)
{
   List<ModelValue> r = value_map.get(c);
   if (r == null) {
      r = new ArrayList<ModelValue>(4);
      value_map.put(c,r);
    }
   return r;
}



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

protected BT_Class	data_type;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelValue(BT_Class typ)
{
   data_type = typ;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

abstract Object getProgramValue();

abstract ModelValue mergeValue(ModelValue mv);


ModelValue performOperation(BT_Class cls,ModelValue arg,int opc)
{
   return anyValue(cls);
}


BT_Class getDataType()				{ return data_type; }

TestBranch branchTest(ModelValue mv,int opc)	{ return TestBranch.ANY; }

boolean isCategory2()				{ return false; }
boolean canBeNull()				{ return false; }
boolean mustBeNull()				{ return false; }
short getFlags()				{ return 0; }
Set<String> getStrings()			{ return null; }




/********************************************************************************/
/*										*/
/*	Subclass to hold bad values						*/
/*										*/
/********************************************************************************/

private static class ModelBadValue extends ModelValue {

   ModelBadValue()			{ super(BT_Class.getVoid()); }

   @Override ModelValue mergeValue(ModelValue mv) { return this; }
   @Override Object getProgramValue()		{ return null; }

}	// end of subclass ModelBadValue




}	// end of class ModelValue



/* end of ModelValue.java */
