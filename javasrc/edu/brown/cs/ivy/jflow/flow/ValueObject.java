/********************************************************************************/
/*										*/
/*		ValueObject.java						*/
/*										*/
/*	Representation of an object value for static checking			*/
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

import edu.brown.cs.ivy.jflow.JflowFlags;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_ClassVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;




class ValueObject extends ValueBase
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private short		value_flags;
private Map<BT_Class,ValueBase> restrict_map;
private Map<BT_Class,ValueBase> remove_map;
private ValueBase	nonnull_value;
private ValueBase	testnull_value;

private static Map<BT_Class,Map<BT_Class,BT_Class>> parent_map = new HashMap<>();




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ValueObject(FlowMaster jm,BT_Class typ,SourceSet cs,short flags)
{
   super(jm,typ);

   setSourceSet(cs);
   value_flags = flags;
   restrict_map = null;
   remove_map = null;
   nonnull_value = null;
   testnull_value = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public final boolean canBeNull()		{ return JflowFlags.canBeNull(value_flags); }

@Override public final boolean mustBeNull()		{ return JflowFlags.mustBeNull(value_flags); }

public final boolean testForNull()		{ return JflowFlags.testForNull(value_flags); }

@Override public final boolean nullExplicitlySet()	{ return JflowFlags.nullExplicitlySet(value_flags); }

@Override public final short getFlags()			{ return value_flags; }



/********************************************************************************/
/*										*/
/*	Methods for operations							*/
/*										*/
/********************************************************************************/

@Override public ValueBase restrictByClass(BT_Class typ,boolean pfg)
{
   if (restrict_map == null) restrict_map = new HashMap<BT_Class,ValueBase>(4);

   ValueBase nv = restrict_map.get(typ);
   if (nv == null) {
      SourceSet ns = source_set.restrictByType(typ,pfg);
      if (ns == source_set) {
	 nv = this;
	 if (ns.isEmpty() && mustBeNull() && typ != getDataType()) {
	    nv = jflow_master.nullValue(typ);
	  }
       }
      else if (ns == jflow_master.createEmptySet()) {
	 if (typ.getName().endsWith("SettingsNode")) {
	    System.err.println("Work on settings node");
	  }
	 if (canBeNull()) nv = jflow_master.nullValue(typ);
	 else nv = jflow_master.emptyValue(typ,value_flags);
       }
      else {
	 BT_Class ntyp = getSetType(ns);
	 if (ntyp != null) nv = jflow_master.objectValue(ntyp,ns,value_flags);
	 else if (canBeNull()) nv = jflow_master.nullValue(typ);
	 else nv = jflow_master.objectValue(typ,ns,value_flags);
       }
      restrict_map.put(typ,nv);
    }

   return nv;
}



@Override public ValueBase removeByClass(BT_Class typ)
{
   if (remove_map == null) remove_map = new HashMap<BT_Class,ValueBase>(4);

   ValueBase nv = remove_map.get(typ);
   if (nv == null) {
      SourceSet ns = source_set.removeByType(typ);
      if (ns == source_set) nv = this;
      else if (ns == jflow_master.createEmptySet()) {
	 if (canBeNull()) nv = jflow_master.nullValue();
	 else nv = null;
       }
      else {
	 BT_Class ntyp = getSetType(ns);
	 if (ntyp != null) nv = jflow_master.objectValue(ntyp,ns,value_flags);
	 else if (canBeNull()) nv = jflow_master.nullValue();
	 else nv = null;
       }
      remove_map.put(typ,nv);
    }

   return nv;
}






@Override public ValueBase forceNonNull()
{
   if (!canBeNull()) return this;

   if (nonnull_value == null) {
      short fgs = JflowFlags.forceNonNull(value_flags);
      nonnull_value = jflow_master.objectValue(getDataType(),source_set,fgs);
    }

   return nonnull_value;
}




@Override public ValueBase allowNull()
{
   if (canBeNull()) return this;

   if (nonnull_value == null) {
      nonnull_value = jflow_master.objectValue(getDataType(),source_set,JflowFlags.CAN_BE_NULL);
    }

   return nonnull_value;
}




@Override public ValueBase setTestNull()
{
   if (testForNull()) return this;

   if (testnull_value == null) {
      short nfgs = (short) (getFlags() | JflowFlags.TEST_NULL);
      testnull_value = jflow_master.objectValue(getDataType(),source_set,nfgs);
    }

   return testnull_value;
}



@Override public ValueBase makeSubclass(BT_Class bc)
{
   if (bc != getDataType() && bc.isDerivedFrom(getDataType())) {
      return jflow_master.objectValue(bc,source_set,value_flags);
    }

   return this;
}



@Override public ValueBase performOperation(BT_Class typ,ValueBase rhs,int op)
{
   switch (op) {
      case opc_instanceof :
	 if (canBeNull()) break;
	 ValueBase ncv = restrictByClass(rhs.getDataType(),false);
	 if (ncv.isEmptySourceSet()) return jflow_master.rangeValue(typ,0,0);
	 if (ncv == this) return jflow_master.rangeValue(typ,1,1);
	 break;
    }

   return jflow_master.anyValue(typ);
}



@Override public boolean goodSourceSet()
{
   if (mustBeNull()) return true;
   if (source_set.isEmpty()) return false;

   for (SourceBase src : source_set.getSources()) {
      if (src.getDataType() != null) return true;
    }

   return false;
}



@Override public boolean isNative()
{
   for (SourceBase src : source_set.getSources()) {
      if (src.isNative()) return true;
    }

   return false;
}




@Override public boolean allNative()
{
   int ct = 0;

   for (SourceBase src : source_set.getSources()) {
      if (!src.isNative()) return false;
      ++ct;
    }

   if (ct == 0) return false;

   return true;
}




@Override ValueBase makeNonunique()
{
   boolean chng = false;

   for (SourceBase src : source_set.getSources()) {
      if (src.isUnique()) {
	 chng = true;
	 break;
       }
    }

   if (!chng) return this;

   SourceSet ss = jflow_master.createEmptySet();
   for (SourceBase src : source_set.getSources()) {
      if (src.isUnique()) src = src.makeNonunique();
      SourceSet cset = jflow_master.createSingletonSet(src);
      ss = ss.addToSet(cset);
    }

   return jflow_master.objectValue(getDataType(),ss,value_flags);
}



@Override ValueBase getNonunique()
{
   boolean chng = false;

   for (SourceBase src : source_set.getSources()) {
      SourceBase s1 = src.getNonunique();
      if (s1 != null && s1 != src) {
	 chng = true;
	 break;
       }
    }

   if (!chng) return this;

   SourceSet ss = jflow_master.createEmptySet();
   for (SourceBase src : source_set.getSources()) {
      SourceBase s1 = src.getNonunique();
      if (s1 == null) s1 = src;
      SourceSet cset = jflow_master.createSingletonSet(src);
      ss = ss.addToSet(cset);
    }

   return jflow_master.objectValue(getDataType(),ss,value_flags);
}



@Override boolean isUnique()
{
   int ctr = 0;

   for (SourceBase src : source_set.getSources()) {
      if (src.isUnique()) ++ctr;
      else return false;
    }

   if (ctr == 0) return false;

   return true;
}



/********************************************************************************/
/*										*/
/*	Methods for arrays							*/
/*										*/
/********************************************************************************/

@Override ValueBase getArrayContents()
{
   ValueBase cnts = null;

   for (SourceBase src : source_set.getSources()) {
      ValueBase cv = src.getArrayValues(null);
      if (cv != null) {
	 if (cnts == null) cnts = cv;
	 else cnts = cnts.mergeValue(cv);
       }
    }

   return cnts;
}




@Override boolean markArrayAccess(AccessSafety as)
{
   boolean chng = false;

   for (SourceBase src : source_set.getSources()) {
      chng |= src.markArrayAccess(as);
    }

   return chng;
}



/********************************************************************************/
/*										*/
/*	Methods for handling tests						*/
/*										*/
/********************************************************************************/

@Override public TestBranch branchTest(ValueBase rhs,int op)
{
   if (rhs == null) rhs = this;

   if (!(rhs instanceof ValueObject)) return TestBranch.ANY;

   ValueObject cvo = (ValueObject) rhs;

   TestBranch r = TestBranch.ANY;

   switch (op) {
      case opc_if_acmpeq :
	 if (mustBeNull() && cvo.mustBeNull()) r = TestBranch.ALWAYS;
	 else if (mustBeNull() && !cvo.canBeNull()) r = TestBranch.NEVER;
	 else if (!canBeNull() && cvo.mustBeNull()) r = TestBranch.NEVER;
	 break;
      case opc_if_acmpne :
	 if (mustBeNull() && cvo.mustBeNull()) r = TestBranch.NEVER;
	 else if (mustBeNull() && !cvo.canBeNull()) r = TestBranch.ALWAYS;
	 else if (!canBeNull() && cvo.mustBeNull()) r = TestBranch.ALWAYS;
	 break;
      case opc_ifnonnull :
	 if (mustBeNull()) r = TestBranch.NEVER;
	 else if (!canBeNull()) r = TestBranch.ALWAYS;
	 break;
      case opc_ifnull :
	 if (mustBeNull()) r = TestBranch.ALWAYS;
	 else if (!canBeNull()) r = TestBranch.NEVER;
	 break;
    }

   return r;
}




/********************************************************************************/
/*										*/
/*	Methods to handle merged sets						*/
/*										*/
/********************************************************************************/

@Override public ValueBase mergeValue(ValueBase cv)
{
   if (cv == this || cv == null) return this;

   if (!(cv instanceof ValueObject)) {
      return jflow_master.badValue();
    }

   ValueObject cvo = (ValueObject) cv;

   short fgs = JflowFlags.merge(value_flags,cvo.value_flags);

   SourceSet ns = source_set.addToSet(cvo.source_set);

   if (ns == source_set && (cvo.data_type == data_type || !ns.isEmpty()) &&
	  value_flags == fgs)
      return this;

   if (ns == cvo.source_set && (data_type == cvo.data_type || !ns.isEmpty()) &&
	  cvo.getFlags() == fgs)
      return cvo;

   BT_Class typ = getSetType(ns);
   if (typ == null) typ = findCommonParent(data_type,cvo.data_type);

   return jflow_master.objectValue(typ,ns,fgs);
}



@Override public ValueBase newSourcedValue(SourceBase cs)
{
   SourceSet cset = jflow_master.createSingletonSet(cs);
   cset = source_set.addToSet(cset);
   if (cset == source_set) return this;

   return jflow_master.objectValue(getSetType(cset),cset,value_flags);
}



/********************************************************************************/
/*										*/
/*	Helper functions							*/
/*										*/
/********************************************************************************/

private static BT_Class getSetType(SourceSet cs)
{
   BT_Class typ = null;
   for (SourceBase src : cs.getSources()) {
      BT_Class styp = src.getDataType();
      if (styp != null) {
	 if (typ == null) typ = styp;
	 else typ = findCommonParent(typ,styp);
       }
    }

   return typ;
}





private static BT_Class findCommonParent(BT_Class c1,BT_Class c2)
{
   Map<BT_Class,BT_Class> m1 = parent_map.get(c1);
   if (m1 == null) {
      m1 = new HashMap<BT_Class,BT_Class>();
      parent_map.put(c1,m1);
    }
   BT_Class bc = m1.get(c2);
   if (bc == null) {
      bc = computeCommonParent(c1,c2);
      m1.put(c2,bc);
      Map<BT_Class,BT_Class> m2 = parent_map.get(c2);
      if (m2 == null) {
	 m2 = new HashMap<BT_Class,BT_Class>();
	 parent_map.put(c2,m2);
       }
      m2.put(c1,bc);
    }

   return bc;
}




private static BT_Class computeCommonParent(BT_Class c1,BT_Class c2)
{
   if (c1.isInterface() && c2.isInterface()) return findCommonInterface(c1,c2);
   else if (c1.isInterface()) return findCommonClassInterface(c2,c1);
   else if (c2.isInterface()) return findCommonClassInterface(c1,c2);

   if (c1.isArray() && c2.isArray()) return findCommonArray(c1,c2);
   else if (c1.isArray() || c2.isArray()) return BT_Class.findJavaLangObject();

   if (c1.isDerivedFrom(c2)) return c2;
   else if (c2.isDerivedFrom(c1)) return c1;

   for (BT_Class c0 = c1.getSuperClass(); c0 != null; c0 = c0.getSuperClass()) {
      if (c0 == c2 || c0.isAncestorOf(c2)) return c0;
    }

   for (BT_Class c0 = c2.getSuperClass(); c0 != null; c0 = c0.getSuperClass()) {
      if (c0 == c1 || c0.isAncestorOf(c1)) return c0;
    }

   return BT_Class.findJavaLangObject();
}




private static BT_Class findCommonInterface(BT_Class i1,BT_Class i2)
{
   if (i1.isDerivedFrom(i2)) return i2;
   else if (i2.isDerivedFrom(i1)) return i1;

   BT_ClassVector plst = i1.getParents();
   BT_ClassVector cpar = i1.getCircularParents();

   for (Enumeration<?> e = plst.elements(); e.hasMoreElements(); ) {
      BT_Class p = (BT_Class) e.nextElement();
      if (cpar != null && cpar.contains(p)) continue;
      BT_Class c = findCommonInterface(p,i2);
      if (c.isInterface()) return c;
    }

   return BT_Class.findJavaLangObject();
}



private static BT_Class findCommonClassInterface(BT_Class c1,BT_Class c2)
{
   return c1;
}



private static BT_Class findCommonArray(BT_Class c1,BT_Class c2)
{
   if (c1 == c2) return c1;

   BT_Class rslt;

   String s1 = c1.getName();
   String s2 = c2.getName();
   int i1 = s1.lastIndexOf('[');
   int i2 = s2.lastIndexOf('[');
   s1 = s1.substring(0,i1);
   s2 = s2.substring(0,i2);
   BT_Class ac1 = BT_Class.forName(s1);
   BT_Class ac2 = BT_Class.forName(s2);

   if (c1.isDerivedFrom(c2)) rslt = c2;
   else if (c2.isDerivedFrom(c1)) rslt = c1;
   else if (ac1.isDerivedFrom(ac2)) rslt = c2;
   else if (ac2.isDerivedFrom(ac1)) rslt = c1;
   else {
      BT_Class base = findCommonParent(ac1,ac2);
      String s = base.getName() + "[]";
      rslt = BT_Class.forName(s);
    }

   return rslt;
}



/********************************************************************************/
/*										*/
/*	Methods for field values						*/
/*										*/
/********************************************************************************/

@Override public Object getProgramValue()
{
   if (mustBeNull()) return ValueType.NULL;
   else if (!canBeNull()) return ValueType.NON_NULL;

   return null;
}



/********************************************************************************/
/*										*/
/*	Methods for strings							*/
/*										*/
/********************************************************************************/

@Override public Collection<String> getStringValues()
{
   Collection<String> rslt = null;

   for (SourceBase src : source_set.getSources()) {
      String v = src.getStringValue();
      if (v != null) {
	 if (rslt == null) rslt = new ArrayList<String>();
	 rslt.add(v);
       }
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   StringBuffer rslt = new StringBuffer();
   rslt.append("[");
   rslt.append(data_type.getName());
   if (mustBeNull()) rslt.append(" =null");
   else if (nullExplicitlySet()) rslt.append(" *null");
   else if (canBeNull()) rslt.append(" ?null");
   rslt.append(" :: ");
   rslt.append(hashCode());
   rslt.append(" :: ");
   rslt.append(source_set.size());
   if (source_set.size() == 1) {
      rslt.append(" { ");
      for (SourceBase src : source_set.getSources()) {
	 rslt.append(src.toString());
	 rslt.append(" ");
       }
      rslt.append("} ");
    }
   rslt.append(" ");
   rslt.append(isNative());
   rslt.append("]");
   return rslt.toString();
}





}	// end of class ValueObject




/* end of ValueObject.java */
