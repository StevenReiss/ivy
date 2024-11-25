/********************************************************************************/
/*										*/
/*		ModelObjectValue.java						*/
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


package edu.brown.cs.ivy.jflow.model;

import edu.brown.cs.ivy.jflow.JflowFlags;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_ClassVector;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



class ModelObjectValue extends ModelValue
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private short		value_flags;
private Set<String>	string_set;



private static Map<BT_Class,Map<BT_Class,BT_Class>> parent_map;

static {
   parent_map = new HashMap<BT_Class,Map<BT_Class,BT_Class>>();
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelObjectValue(BT_Class typ,short flags,Set<String> strs)
{
   super(typ);

   value_flags = flags;

   if (strs == null) string_set = null;
   else string_set = new HashSet<String>(strs);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public final boolean canBeNull()		{ return JflowFlags.canBeNull(value_flags); }


@Override public final boolean mustBeNull()		{ return JflowFlags.mustBeNull(value_flags); }


@Override final short getFlags()				{ return value_flags; }


@Override public Set<String> getStrings() 		{ return string_set; }




@Override public ModelValue performOperation(BT_Class typ,ModelValue rhs,int op)
{
   return ModelValue.anyValue(typ);
}



/********************************************************************************/
/*										*/
/*	Methods for handling tests						*/
/*										*/
/********************************************************************************/

@Override public TestBranch branchTest(ModelValue rhs,int op)
{
   if (rhs == null) rhs = this;

   if (!(rhs instanceof ModelObjectValue)) return TestBranch.ANY;

   ModelObjectValue cvo = (ModelObjectValue) rhs;

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

@Override public ModelValue mergeValue(ModelValue cv)
{
   if (cv == this || cv == null) return this;

   if (!(cv instanceof ModelObjectValue)) {
      return ModelValue.badValue();
    }

   ModelObjectValue cvo = (ModelObjectValue) cv;

   short flags = JflowFlags.merge(value_flags,cvo.getFlags());

   BT_Class typ = findCommonParent(data_type,cvo.data_type);

   Set<String> ns = string_set;
   if (cvo.string_set != null) {
      if (ns == null || cvo.string_set.containsAll(ns)) ns = cvo.string_set;
      else if (!ns.containsAll(cvo.string_set)) {
	 ns = new HashSet<String>(string_set);
	 ns.addAll(cvo.string_set);
       }
    }

   if (typ == data_type && flags == value_flags) return this;
   if (typ == cvo.data_type && flags == cvo.getFlags() && ns == cvo.string_set)
      return cvo;

   return objectValue(typ,flags,ns);
}



/********************************************************************************/
/*										*/
/*	Helper functions							*/
/*										*/
/********************************************************************************/

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
   if (string_set != null) {
      for (String s : string_set) {
	 return s;
       }
    }

   if (mustBeNull()) return ValueType.NULL;
   else if (!canBeNull()) return ValueType.NON_NULL;

   return null;
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
   else if (canBeNull()) rslt.append(" ?null");
   if (string_set != null) {
      for (String s : string_set) rslt.append(" <<" + s + ">>");
    }
   rslt.append(" :: ");
   rslt.append(hashCode());
   rslt.append("]");
   return rslt.toString();
}





}	// end of class ModelObjectValue




/* end of ModelObjectValue.java */

