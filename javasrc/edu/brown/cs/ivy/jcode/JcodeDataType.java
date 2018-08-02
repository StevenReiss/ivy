/********************************************************************************/
/*										*/
/*		JcodeDataType.java						*/
/*										*/
/*	Representaiton of java data types					*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
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



package edu.brown.cs.ivy.jcode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class JcodeDataType
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Type	base_type;
private JcodeFactory bcode_factory;
private JcodeDataType super_type;
private Collection<JcodeDataType> iface_types;
private Collection<JcodeDataType> child_types;
private int	modifier_values;
private Map<JcodeDataType,JcodeDataType> parent_map;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcodeDataType(Type t,JcodeFactory factory)
{
   base_type = t;
   bcode_factory = factory;
   super_type = null;
   iface_types = null;
   child_types = null;
   parent_map = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getName() 	      { return base_type.getClassName(); }
public String getDescriptor()
{
   return base_type.getDescriptor();
}

public boolean isArray()
{
   return base_type.getSort() == Type.ARRAY;
}

public boolean isAbstract()
{
   return (modifier_values & Opcodes.ACC_ABSTRACT) != 0;
}


public boolean isStatic()
{
   return Modifier.isStatic(modifier_values);
}


public boolean isCategory2()
{
   switch (base_type.getSort()) {
      case Type.LONG :
      case Type.DOUBLE :
	 return true;
    }
   return false;
}

public boolean isBroaderType(JcodeDataType c)
{
   if (this == c) return true;
   if (base_type.getSort() == Type.BOOLEAN) return false;
   else if (c.base_type.getSort() == Type.BOOLEAN) return true;

   if (base_type.getSort() == Type.CHAR) return false;
   else if (c.base_type.getSort() == Type.CHAR) return true;

   int ln0 = classLength();
   int ln1 = c.classLength();

   return ln0 >= ln1;
}



private int classLength()
{
   switch (base_type.getSort()) {
      case Type.BOOLEAN :
      case Type.BYTE :
	 return 1;
      case Type.SHORT :
      case Type.CHAR :
	 return 2;
      case Type.INT :
      case Type.FLOAT :
	 return 4;
      case Type.LONG :
      case Type.DOUBLE :
	 return 8;
      default :
	 return 4;
    }
}




public boolean isInterface()
{
   return (modifier_values & Opcodes.ACC_INTERFACE) != 0;
}

public JcodeDataType getSuperType()		{ return super_type; }
public Iterable<JcodeDataType> getInterfaces()
{
   List<JcodeDataType> rslt = new ArrayList<JcodeDataType>();
   if (iface_types != null) rslt.addAll(iface_types);
   return rslt;
}



public JcodeDataType getBaseDataType()
{
   if (!isArray()) return null;

   Type t = base_type.getElementType();
   return bcode_factory.findDataType(t);
}



public JcodeDataType getArrayType()
{
   String adesc = "[" + base_type.getDescriptor();
   Type t = Type.getType(adesc);
   return bcode_factory.findDataType(t);
}



public boolean isJavaLangObject()
{
   return base_type.getClassName().equals("java.lang.Object");
}



public boolean isInt()
{
   return base_type == Type.INT_TYPE;
}

public boolean isVoid()
{
   return base_type.getSort() == Type.VOID;
}

public boolean isFloating()
{
   switch (base_type.getSort()) {
      case Type.FLOAT :
      case Type.DOUBLE :
	 return true;
    }
   return false;
}


public boolean isPrimitive()
{
   switch (base_type.getSort()) {
      case Type.VOID :
      case Type.BOOLEAN :
      case Type.CHAR :
      case Type.BYTE :
      case Type.SHORT :
      case Type.INT :
      case Type.FLOAT :
      case Type.LONG :
      case Type.DOUBLE :
	 return true;
    }
   return false;
}




/********************************************************************************/
/*										*/
/*	Type comparison methods 						*/
/*										*/
/********************************************************************************/

void noteModifiers(int mods)
{
   modifier_values = mods;
}



void noteSuperType(String st)
{
   if (st != null) {
      super_type = bcode_factory.findClassType(st);
      super_type.addChild(this);
    }
}


void noteInterfaces(Collection<?> ifs)
{
   if (ifs == null || ifs.size() == 0) return;
   if (iface_types == null) iface_types = new ArrayList<JcodeDataType>();
   for (Object o : ifs) {
      String iname = o.toString();
      JcodeDataType bdt = bcode_factory.findClassType(iname);
      iface_types.add(bdt);
      bdt.addChild(this);
    }
}


private synchronized void addChild(JcodeDataType bdt)
{
   if (child_types == null) child_types = new ArrayList<JcodeDataType>();
   child_types.add(bdt);
}




public boolean isDerivedFrom(JcodeDataType bdt)
{
   if (bdt == null) return false;
   if (bdt == this) return true;

   if (isPrimitive()) {
      if (!bdt.isPrimitive()) return false;
      int st = base_type.getSort();
      switch (bdt.base_type.getSort()) {
	 case Type.CHAR :
	 case Type.SHORT :
	    if (st == Type.CHAR || st == Type.SHORT || st == Type.BYTE) return true;
	    break;
	 case Type.INT :
	    if (st == Type.CHAR || st == Type.SHORT || st == Type.BYTE) return true;
	    break;
	 case Type.LONG :
	    if (st == Type.CHAR || st == Type.SHORT || st == Type.BYTE || st == Type.INT) return true;
	    break;
	 case Type.FLOAT :
	    if (st == Type.CHAR || st == Type.SHORT || st == Type.BYTE || st == Type.INT) return true;
	    break;
	 case Type.DOUBLE :
	    if (st == Type.CHAR || st == Type.SHORT || st == Type.BYTE || st == Type.INT ||
		   st == Type.FLOAT)
	       return true;
	    break;
       }
      return false;
    }

   if (super_type != null && super_type.isDerivedFrom(bdt)) return true;
   if (iface_types != null) {
      for (JcodeDataType ift : iface_types)
	 if (ift.isDerivedFrom(bdt)) return true;
    }

   if (isArray() && bdt.isArray()) {
      if (getBaseDataType().isDerivedFrom(bdt.getBaseDataType())) return true;
    }
   return false;
}



synchronized Collection<JcodeDataType> getChildTypes()
{
   if (child_types == null) return null;
   
   return new ArrayList<JcodeDataType>(child_types);
}



public JcodeDataType findChildForInterface(JcodeDataType ity)
{
   JcodeDataType fdt0 = null;

   if (child_types != null) {
      for (JcodeDataType k : child_types) {
	 JcodeDataType r = k.findChildForInterface(ity);
	 if (r != null) {
	    if (fdt0 == null) fdt0 = r;
	    else return ity;	// if more than one candidate, return the interface type
	  }
       }
    }

   if (fdt0 == null) {
      if (!isAbstract() && isDerivedFrom(ity)) fdt0 = this;
    }

   return fdt0;
}


public JcodeDataType getReturnType()
{
   return bcode_factory.findDataType(base_type.getReturnType());
}


public JcodeDataType [] getArgumentTypes()
{
   Type [] typs = base_type.getArgumentTypes();
   JcodeDataType [] rslt = new JcodeDataType[typs.length];
   for (int i = 0; i < typs.length; ++i) {
      rslt[i] = bcode_factory.findDataType(typs[i]);
    }
   return rslt;
}




/********************************************************************************/
/*										*/
/*	Compute the common parent of two types					*/
/*										*/
/********************************************************************************/

public JcodeDataType findCommonParent(JcodeDataType t2)
{
   if (t2 == this) return t2;

   synchronized (this) {
      if (parent_map == null) parent_map = new HashMap<JcodeDataType,JcodeDataType>();
    }

   boolean setother = false;

   JcodeDataType bc = null;
   synchronized (parent_map) {
      bc = parent_map.get(t2);
      if (bc == null) {
	 bc = computeCommonParent(t2);
	 parent_map.put(t2,bc);
	 setother = true;
       }
    }

   if (setother) {
      t2.setCommonParent(this,bc);
    }

   return bc;
}



private void setCommonParent(JcodeDataType dt,JcodeDataType rslt)
{
   synchronized (this) {
      if (parent_map == null) parent_map = new HashMap<JcodeDataType,JcodeDataType>();
    }
   synchronized (parent_map) {
      parent_map.put(dt,rslt);
    }
}



private JcodeDataType computeCommonParent(JcodeDataType t2)
{
   if (this == t2) return this;

   if (isInterface() && t2.isInterface()) return findCommonInterface(t2,null);
   else if (isInterface()) return t2.findCommonClassInterface(this);
   else if (t2.isInterface()) return findCommonClassInterface(t2);

   if (isArray() && t2.isArray()) return findCommonArray(t2);
   else if (isArray() || t2.isArray()) return bcode_factory.findJavaType("java.lang.Object");

   if (isDerivedFrom(t2)) return t2;
   else if (t2.isDerivedFrom(this)) return this;

   for (JcodeDataType bdt = super_type; bdt != null; bdt = bdt.super_type) {
      if (bdt == t2 || t2.isDerivedFrom(bdt)) return bdt;
    }

   for (JcodeDataType bdt = t2.super_type; bdt != null; bdt = bdt.super_type) {
      if (bdt == this || isDerivedFrom(bdt)) return bdt;
    }

   return bcode_factory.findJavaType("java.lang.Object");
}



private JcodeDataType findCommonInterface(JcodeDataType i2,Set<JcodeDataType> done)
{
   if (isDerivedFrom(i2)) return i2;
   else if (i2.isDerivedFrom(this)) return this;

   if (done == null) done = new HashSet<JcodeDataType>();
   if (super_type != null) {
      if (!done.contains(super_type)) {
	 done.add(super_type);
	 JcodeDataType c = super_type.findCommonInterface(i2,done);
	 if (c.isInterface()) return c;
       }
    }
   if (iface_types != null) {
      for (JcodeDataType typ : iface_types) {
	 if (done.contains(typ)) continue;
	 done.add(typ);
	 JcodeDataType c = typ.findCommonInterface(i2,done);
	 if (c.isInterface()) return c;
       }
    }

   return bcode_factory.findJavaType("java.lang.Object");
}



private JcodeDataType findCommonClassInterface(JcodeDataType typ)
{
   return this;
}



private JcodeDataType findCommonArray(JcodeDataType t2)
{
   if (this == t2) return this;

   JcodeDataType rslt = null;

   JcodeDataType ac1 = getBaseDataType();
   JcodeDataType ac2 = t2.getBaseDataType();

   if (isDerivedFrom(t2)) rslt = t2;
   else if (t2.isDerivedFrom(this)) rslt = this;
   else if (ac1.isDerivedFrom(ac2)) rslt = t2;
   else if (ac2.isDerivedFrom(ac1)) rslt = this;
   else {
      JcodeDataType fdt = ac1.findCommonParent(ac2);
      rslt = bcode_factory.findJavaType("[" + fdt.getDescriptor());
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
   return base_type.toString();
}



}	// end of class JcodeDataType




/* end of JcodeDataType.java */

