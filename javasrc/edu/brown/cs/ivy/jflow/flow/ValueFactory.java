/********************************************************************************/
/*										*/
/*		jflow_master.java						*/
/*										*/
/*	Factory for creating (or reusing) values under various conditions	*/
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


import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowFlags;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;




public class ValueFactory implements JflowConstants
{



/********************************************************************************/
/*										*/
/*	Local storage								*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;

private Map<BT_Class,ValueBase> any_map;
private Map<BT_Class,List<ValueInt>> range_map;
private Map<SourceSet,List<ValueObject>> object_map;
private Map<BT_Class,ValueBase> null_map;
private Map<BT_Class,List<ValueObject>> empty_map;

private ValueBase string_value;
private ValueBase null_value;
private ValueBase bad_value;
private ValueBase main_value;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public ValueFactory(FlowMaster jm)
{
   jflow_master = jm;

   any_map = new HashMap<BT_Class,ValueBase>();
   range_map = new WeakHashMap<BT_Class,List<ValueInt>>();
   object_map = new WeakHashMap<SourceSet,List<ValueObject>>();
   null_map = new HashMap<BT_Class,ValueBase>();
   empty_map = new HashMap<BT_Class,List<ValueObject>>();

   string_value = null;
   null_value = null;
   bad_value = null;
   main_value = null;
}



public void resetValues()
{
   any_map = new HashMap<BT_Class,ValueBase>();
   range_map = new HashMap<BT_Class,List<ValueInt>>();
   object_map = new HashMap<SourceSet,List<ValueObject>>();
   null_map = new HashMap<BT_Class,ValueBase>();
   empty_map = new HashMap<BT_Class,List<ValueObject>>();
}



/********************************************************************************/
/*										*/
/*	Methods for building values						*/
/*										*/
/********************************************************************************/

public ValueBase anyValue(BT_Class typ)
{
   if (any_map == null) return null;

   ValueBase cv = any_map.get(typ);
   if (cv == null) {
      if (typ.isPrimitive()) {
	 if (typ == BT_Class.getFloat() || typ == BT_Class.getDouble()) {
	    cv = new ValueFloat(jflow_master,typ);
	  }
	 else if (typ == BT_Class.getVoid()) {
	    cv = new ValueObject(jflow_master,typ,jflow_master.createEmptySet(),JflowFlags.NON_NULL);
	  }
	 else cv = new ValueInt(jflow_master,typ);
       }
      else cv = new ValueObject(jflow_master,typ,jflow_master.createEmptySet(),JflowFlags.CAN_BE_NULL);
      any_map.put(typ,cv);
    }

   return cv;
}



public ValueBase rangeValue(BT_Class typ,long v0,long v1)
{
   if (typ == BT_Class.getFloat() || typ == BT_Class.getDouble()) {
      return anyValue(typ);
    }
   if (v1-v0 > JFLOW_VALUE_MAX_RANGE) {
      return anyValue(typ);
    }

   List<ValueInt> l = range_map.get(typ);
   if (l == null) {
      l = new Vector<ValueInt>();
      range_map.put(typ,l);
    }

   for (ValueInt cvi : l) {
      if (cvi.getMinValue() == v0 && cvi.getMaxValue() == v1) return cvi;
    }

   ValueInt cv = new ValueInt(jflow_master,typ,v0,v1);
   l.add(cv);

   return cv;
}



public ValueBase objectValue(BT_Class typ,SourceSet cs,short flags)
{
   if (cs == jflow_master.createEmptySet()) return emptyValue(typ,flags);

   List<ValueObject> l = object_map.get(cs);
   if (l == null) {
      l = new Vector<ValueObject>();
      object_map.put(cs,l);
    }

   for (ValueObject cvo : l) {
      if (cvo.getDataType() == typ && cvo.getFlags() == flags) return cvo;
    }

   ValueObject cv = new ValueObject(jflow_master,typ,cs,flags);
   l.add(cv);

   return cv;
}



public ValueBase emptyValue(BT_Class typ,short flags)
{
   List<ValueObject> l = empty_map.get(typ);
   if (l == null) {
      l = new Vector<ValueObject>();
      empty_map.put(typ,l);
    }

   for (ValueObject cvo : l) {
      if (cvo.getFlags() == flags) return cvo;
    }

   ValueObject cv = new ValueObject(jflow_master,typ,jflow_master.createEmptySet(),flags);
   l.add(cv);

   return cv;
}




/********************************************************************************/
/*										*/
/*	Methods for building special values					*/
/*										*/
/********************************************************************************/

ValueBase constantString()
{
   if (string_value == null) {
      BT_Class styp = BT_Class.forName("java.lang.String");
      SourceBase ssrc = jflow_master.createFixedSource(styp);
      SourceSet sset = jflow_master.createSingletonSet(ssrc);
      string_value = objectValue(styp,sset,JflowFlags.NON_NULL);
    }

   return string_value;
}



ValueBase constantString(String v)
{
   if (v == null) return constantString();

   BT_Class styp = BT_Class.forName("java.lang.String");
   SourceBase sb = jflow_master.createStringSource(v);
   SourceSet ss = jflow_master.createSingletonSet(sb);

   return objectValue(styp,ss,JflowFlags.NON_NULL);
}




ValueBase mainArgs()
{
   if (main_value == null) {
      BT_Class styp = BT_Class.forName("java.lang.String[]");
      BT_Class strty = BT_Class.forName("java.lang.String");
      SourceBase ssrc = jflow_master.createArraySource(strty,null);
      ValueBase cv = nativeValue(strty);
      cv = cv.forceNonNull();
      ssrc.setArrayContents(cv);
      SourceSet sset = jflow_master.createSingletonSet(ssrc);
      main_value = objectValue(styp,sset,JflowFlags.NON_NULL);
    }

   return main_value;
}



/********************************************************************************/
/*										*/
/*	Methods for handling null and bad values				*/
/*										*/
/********************************************************************************/

public ValueBase nullValue()
{
   if (null_value == null) {
      null_value = emptyValue(BT_Class.findJavaLangObject(),JflowFlags.NEW_NULL);
    }

   return null_value;
}




public ValueBase nullValue(BT_Class typ)
{
   ValueBase cv = null_map.get(typ);
   if (cv == null) {
      cv = emptyValue(typ,JflowFlags.NEW_NULL);
      null_map.put(typ,cv);
    }
   return cv;
}



public ValueBase badValue()
{
   if (bad_value == null) {
      bad_value = new ValueBad(jflow_master);
    }

   return bad_value;
}




/********************************************************************************/
/*										*/
/*	Methods for handling native values					*/
/*										*/
/********************************************************************************/

public ValueBase nativeValue(BT_Class typ)
{
   if (typ.isPrimitive()) return anyValue(typ);

   if (typ.isInterface() || typ.isAbstract() || typ == BT_Class.findJavaLangObject())
      return mutableValue(typ);

   SourceBase src = jflow_master.createFixedSource(typ);
   SourceSet sset = jflow_master.createSingletonSet(src);

   return objectValue(typ,sset,JflowFlags.CAN_BE_NULL);
}




public ValueBase mutableValue(BT_Class typ)
{
   if (typ.isPrimitive()) return anyValue(typ);

   SourceBase src = jflow_master.createMutableSource(typ);
   SourceSet sset = jflow_master.createSingletonSet(src);

   return objectValue(typ,sset,JflowFlags.CAN_BE_NULL);
}




public ValueBase anyObject()
{
   return anyValue(BT_Class.findJavaLangObject());
}



public ValueBase anyNewObject()
{
   ValueObject cvo = (ValueObject) anyObject();
   return cvo.forceNonNull();
}



/********************************************************************************/
/*										*/
/*	Methods for handling field values					*/
/*										*/
/********************************************************************************/

public ValueBase initialFieldValue(BT_Field fld,boolean nat)
{
   BT_Class c = fld.getDeclaringClass();
   if (c.getName().startsWith("java.lang.")) nat = true;
   ValueBase s0 = null;

   if (nat) {
      BT_Class ftyp = fld.type;
      boolean nonnull = false;			// specialize as needed
      if (c.getName().equals("java.lang.System")) {
	 nonnull = true;
	 if (fld.getName().equals("in"))
	    ftyp = BT_Class.forName("java.io.FileInputStream");
	 else if (fld.getName().equals("out") || fld.getName().equals("err"))
	    ftyp = BT_Class.forName("java.io.PrintStream");
       }
      else if (c.getName().equals("java.lang.String")) nonnull = true;
      else if (c.getName().equals("java.lang.ThreadGroup")) nonnull = false;
      else if (c.getName().equals("java.lang.ClassLoader") &&
		  fld.getName().equals("parent")) nonnull = false;
      else if (c.getName().startsWith("java.lang.")) nonnull = true;
      if (ftyp.isAbstract()) s0 = jflow_master.mutableValue(ftyp);
      else s0 = jflow_master.nativeValue(ftyp);
      if (nonnull) s0 = s0.forceNonNull();
    }
   else {
      if (fld.type.isPrimitive()) {
	 if (fld.type == BT_Class.getInt() || fld.type == BT_Class.getChar() ||
		fld.type == BT_Class.getShort() || fld.type == BT_Class.getByte() ||
		fld.type == BT_Class.getLong() || fld.type == BT_Class.getBoolean()) {
	    // s0 = rangeValue(fld.type,0,0);
	    s0 = anyValue(fld.type);
	  }
	 else {
	    s0 = anyValue(fld.type);
	  }
       }
      else {
	 s0 = emptyValue(fld.type,JflowFlags.NEW_NULL);
       }
    }

   return s0;
}



/********************************************************************************/
/*										*/
/*	Methods for updating values on incremental change			*/
/*										*/
/********************************************************************************/

Map<JflowValue,JflowValue> handleSourceSetUpdate(Map<SourceSet,SourceSet> upds)
{
   Map<JflowValue,JflowValue> vupds = new HashMap<>();

   if (upds == null) return vupds;

   for (Map.Entry<SourceSet,SourceSet> ent : upds.entrySet()) {
      SourceSet frmset = ent.getKey();
      SourceSet toset = ent.getValue();
      List<ValueObject> lvo = object_map.get(frmset);
      if (lvo == null) continue;
      for (ValueObject vo : lvo) {
	 ValueBase nvo = objectValue(vo.getDataType(),toset,vo.getFlags());
	 if (nvo != vo) vupds.put(vo,nvo);
       }
    }

   return vupds;
}




}	// end of class ValueFactory




/* end of jflow_master.java */
