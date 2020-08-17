/********************************************************************************/
/*										*/
/*		ProtoBase.java							*/
/*										*/
/*	Base prototype implementation						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/ProtoBase.java,v 1.13 2018/08/02 15:10:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ProtoBase.java,v $
 * Revision 1.13  2018/08/02 15:10:17  spr
 * Fix imports.
 *
 * Revision 1.12  2017/10/24 12:46:27  spr
 * Clean up.
 *
 * Revision 1.11  2011-04-13 21:03:14  spr
 * Fix bugs in flow analysis.
 *
 * Revision 1.10  2009-09-17 01:57:20  spr
 * Fix a few minor bugs (poll, interfaces); code cleanup for Eclipse.
 *
 * Revision 1.9  2008-11-12 13:45:39  spr
 * Eclipse fixups.
 *
 * Revision 1.8  2007-08-10 02:10:39  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.7  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.6  2007-02-27 18:53:29  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.5  2007-01-03 14:04:59  spr
 * Fix imports
 *
 * Revision 1.4  2007-01-03 03:24:18  spr
 * Modifications to handle incremental update.
 *
 * Revision 1.3  2006/07/23 02:25:02  spr
 * Minor bug fixes and speed ups.
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

import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Method;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


abstract class ProtoBase implements JflowConstants
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

protected FlowMaster jflow_master;
private BT_Class	proto_class;



private static Map<Class<?>,Map<BT_Method,Method>> method_map = new HashMap<>();


private static Class<?> [] call_params = new Class<?> [] { BT_Method.class,
							List.class,
							FlowCallSite.class,
							FlowControl.class };




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected ProtoBase(FlowMaster jm,BT_Class bc)
{
   jflow_master = jm;

   proto_class = bc;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

BT_Class getDataType()						{ return proto_class; }

public void setNative() 					{ }




/********************************************************************************/
/*										*/
/*	Null implementations for field and array				*/
/*										*/
/********************************************************************************/

void setField(BT_Field _fld,ValueBase _cv)		 { }

boolean addToField(BT_Field _fld,ValueBase _cv) 	 { return false; }

ValueBase getField(BT_Field _fld)			 { return null; }



boolean setArrayContents(ValueBase _idx,ValueBase _cv)	 { return false; }

ValueBase getArrayContents(ValueBase _idx)		  { return null; }




/********************************************************************************/
/*										*/
/*	Check for relevant methods						*/
/*										*/
/********************************************************************************/

public boolean isMethodRelevant(BT_Method bm)
{
   if (proto_class == null)
      return true;

   BT_Class bc = bm.getDeclaringClass();
   if (proto_class == bc || proto_class.isDescendentOf(bc)) return true;

   return false;
}




/********************************************************************************/
/*										*/
/*	Generic call handler							*/
/*										*/
/********************************************************************************/

public ValueBase handleCall(BT_Method bm,List<ValueBase> args,FlowCallSite from,FlowControl cf)
{
   Map<BT_Method,Method> mmap = method_map.get(getClass());
   if (mmap == null) {
      mmap = new HashMap<BT_Method,Method>();
      method_map.put(getClass(),mmap);
    }

   Method mthd = null;
   if (mmap.containsKey(bm)) mthd = mmap.get(bm);
   else {
      String nm;
      if (bm.isConstructor()) nm = "prototype__constructor";
      else nm = "prototype_" + bm.getName();
      Class<?> c = getClass();
      try {
	 mthd = c.getMethod(nm,call_params);
       }
      catch (NoSuchMethodException _ex) { }
      mmap.put(bm,mthd);
    }

   try {
      if (mthd != null) {
	 if (FlowMaster.doDebug()) {
	    System.err.println("\tUse Prototype call to " + mthd.getDeclaringClass().getName() +
				  "." + mthd.getName() + " [" + hashCode() + "]");
	  }
	 ValueBase rslt = (ValueBase) mthd.invoke(this,new Object [] { bm,args,from,cf });
	 if (FlowMaster.doDebug()) {
	    System.err.println("\tPrototype result = " + rslt);
	  }
	 return rslt;
       }
    }
   catch (Exception e) {
      System.err.println("JFLOW: Problem with prototype call: " + e);
      if (mthd != null)
	 System.err.println("JFLOW: Call to " + mthd.getDeclaringClass().getName() + "." + mthd.getName());
      e.printStackTrace();

    }

   return jflow_master.anyValue(bm.getSignature().returnType);
}




/********************************************************************************/
/*										*/
/*	Return helpers								*/
/*										*/
/********************************************************************************/

protected ValueBase returnAny(BT_Method bm)
{
   return jflow_master.anyValue(bm.getSignature().returnType);
}



protected ValueBase returnNative(BT_Method bm)
{
   return jflow_master.nativeValue(bm.getSignature().returnType);
}



protected ValueBase returnMutable(BT_Method bm)
{
   return jflow_master.mutableValue(bm.getSignature().returnType);
}



protected ValueBase returnTrue()
{
   return jflow_master.rangeValue(BT_Class.getBoolean(),1,1);
}



protected ValueBase returnFalse()
{
   return jflow_master.rangeValue(BT_Class.getBoolean(),0,0);
}



protected ValueBase returnInt(int v)
{
   return jflow_master.rangeValue(BT_Class.getInt(),v,v);
}




protected ValueBase returnInt(int v0,int v1)
{
   return jflow_master.rangeValue(BT_Class.getInt(),v0,v1);
}



protected ValueBase returnNull(BT_Method bm)
{
   return jflow_master.nullValue(bm.getSignature().returnType);
}



protected ValueBase returnVoid()
{
   return jflow_master.anyValue(BT_Class.getVoid());
}



/********************************************************************************/
/*										*/
/*	Methods for incremental update						*/
/*										*/
/********************************************************************************/

void handleUpdates(Collection<SourceBase> oldsrcs,
		      Map<SourceSet,SourceSet> srcupdates,
		      Map<JflowValue,JflowValue> valupdates)
{
}



}	// end of abstract class ProtoBase




/* end of ProtoBase.java */
