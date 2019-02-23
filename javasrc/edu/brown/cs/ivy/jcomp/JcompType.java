/********************************************************************************/
/*										*/
/*		JcompType.java							*/
/*										*/
/*	Representation of a Java type						*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2007, Brown University, Providence, RI.				 *
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


package edu.brown.cs.ivy.jcomp;


import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 *	This class represents a type from semantic resolution.	Each type
 *	has a unique object, and two instances of the type will have the
 *	same object.
 **/

abstract public class JcompType implements JcompConstants {





/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

static JcompType createPrimitiveType(PrimitiveType.Code pt,JcompType eqv)
{
   return new PrimType(pt,eqv);
}


static JcompType createVariableType(String nm)
{
   return new VarType(nm);
}

static JcompType createWildcardType()
{
   return new WildcardType();
}



static JcompType createBinaryClassType(String nm,String sgn)
{
   return new BinaryClassType(nm,sgn);
}


static JcompType createCompiledClassType(String nm)
{
   return new CompiledClassType(nm);
}



static JcompType createBinaryInterfaceType(String nm,String sgn)
{
   return new BinaryInterfaceType(nm,sgn);
}


static JcompType createCompiledInterfaceType(String nm)
{
   return new CompiledInterfaceType(nm);
}



static JcompType createBinaryAnnotationType(String nm,String sgn)
{
   return new BinaryAnnotationType(nm,sgn);
}


static JcompType createCompiledAnnotationType(String nm)
{
   return new CompiledAnnotationType(nm);
}


static JcompType createBinaryEnumType(String nm,String sgn)
{
   return new BinaryEnumType(nm,sgn);
}

static JcompType createEnumType(String nm)
{
   return new EnumType(nm);
}





static JcompType createParameterizedType(JcompType jt,List<JcompType> ptl,SortedMap<String,JcompType> outers,JcompTyper typer)
{
   if (outers != null && !outers.isEmpty()) {
      SortedMap<String,JcompType> nouters = new TreeMap<>();
      for (JcompType oty = jt; oty != null; oty = oty.getOuterType()) {
         List<String> vars = JcompGenerics.getTypeVariableNames(oty.getSignature(),true);        
         if (vars != null) {
            for (String s : vars) {
               if (outers.get(s) != null) {
                  nouters.put(s,outers.get(s));
                }
             }
          }
       }
      if (nouters.isEmpty()) outers = null;
      else outers = nouters;
    }
      
   if ((ptl == null || ptl.isEmpty()) && (outers == null || outers.isEmpty())) return jt;
      
   ParamType pttyp = new ParamType(typer,jt,ptl,outers);
   JcompType ntyp = typer.fixJavaType(pttyp);
   if (ntyp != pttyp) return ntyp;
   
   if (pttyp.getName().length() > 1024) 
      System.err.println("PARAMETERIZED TYPE TOO LONG");
   
   // typer.applyParameters(pttyp);

   return pttyp;
}



static JcompType createUnionType(List<JcompType> tps)
{
   StringBuffer buf = new StringBuffer();
   for (JcompType jt : tps) {
      if (buf.length() > 0) buf.append("|");
      buf.append(jt.getName());
    }
   return new UnionType(buf.toString(),tps);
}


static JcompType createIntersectionType(List<JcompType> tps)
{
   StringBuffer buf = new StringBuffer();
   for (JcompType jt : tps) {
      if (buf.length() > 0) buf.append("&");
      buf.append(jt.getName());
    }
   return new IntersectionType(buf.toString(),tps);
}


static public JcompType createArrayType(JcompType jt)
{
   return new ArrayType(jt);
}



public static JcompType createMethodType(JcompType jt,List<JcompType> aty,boolean varargs,
	String signature)
{
   return new MethodType(jt,aty,varargs,signature);
}



static JcompType createAnyClassType()
{
   return new AnyClassType();
}



static JcompType createErrorType()
{
   return new ErrorType();
}


static public JcompType createFunctionRefType(JcompType methodtype,JcompType nstype,JcompSymbol sym)
{
   return new FunctionRefType(methodtype,nstype,sym,null);
}

static JcompType createFunctionRefType(JcompType mtyp,JcompType nstype,JcompSymbol sym,JcompSymbol rsym)
{
   return new FunctionRefType(mtyp,nstype,sym,rsym);
}




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String type_name;
private JcompScope assoc_scope;
private JcompType assoc_type;
private JcompSymbol assoc_symbol;
private boolean is_abstract;
private boolean inner_nonstatic;
private String type_signature;
private Map<JcompType,JcompType> parent_map;





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected JcompType(String s)
{
   type_name = s;
   assoc_scope = null;
   assoc_type = null;
   assoc_symbol = null;
   is_abstract = false;
   inner_nonstatic = false;
   type_signature = null;
   parent_map = null;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

/**
 *	Return the full name of the type
 **/

public String getName() 		{ return type_name; }


/**
 *	Return true if this is a primitive type
 **/

public boolean isPrimitiveType()		{ return false; }


/**
 *	Return true if this is type boolean
 **/

public boolean isBooleanType()		{ return false; }


/**
 *	Return true if this is a primitive numeric type
 **/

public boolean isNumericType()		{ return false; }


public boolean isFloatingType() 	{ return false; }
public boolean isIntType()		{ return false; }
public boolean isCategory2()		{ return false; }

public boolean isCharType()		{ return false; }
public boolean isFloatType()		{ return false; }
public boolean isDoubleType()		{ return false; }
public boolean isShortType()		{ return false; }
public boolean isByteType()		{ return false; }
public boolean isLongType()		{ return false; }
public boolean isBroaderType(JcompType t) { return false; }
public boolean isComplete()		{ return true; }


/**
 *	Return true if this is the type void
 **/

public boolean isVoidType()		{ return false; }


public boolean isJavaLangObject()	{ return false; }
public boolean isStringType()		{ return false; }


/**
 *	Return true if this is an array type
 **/

public boolean isArrayType()			{ return false; }


/**
 *	Return true if this is a parameterized type.
 **/

public boolean isParameterizedType()		{ return false; }


/**
 *	Return true if this is a class type.
 **/

public boolean isClassType()			{ return false; }


/**
 *	Return true if this is an interface type.
 **/

public boolean isInterfaceType()		{ return false; }



/**
 *	Return method type if this is a functional interface
 **/

public JcompType getFunctionalType()	       { return null; }


/**
 *	Return true if this is an enumeration type
 **/

public boolean isEnumType()			{ return false; }


/**
 *	Return true if this is a type variable (part of a parameterized type).
 **/

public boolean isTypeVariable() 		{ return false; }

public boolean isWildcardType()                 { return false; }

/**
 *	Return true is this is a function reference type
 **/

public boolean isFunctionRef()			{ return false; }


/**
 *	Return true if this type represents a method.
 **/

public boolean isMethodType()			{ return false; }


/**
 *	Return true if this type is the unique type reprensenting an error.
 **/

public boolean isErrorType()			{ return false; }


/**
 *	Return true if this type is known (i.e. a Java	builtin or user context type)
 **/

public boolean isBinaryType()			{ return false; }
public boolean isKnownType()			{ return isBinaryType(); }


/**
 *	Return true if this type can be almost anything (e.g. the type of null).
 **/

public boolean isAnyType()			{ return false; }


/**
 *	Return true if this is a union type.
 **/

public boolean isUnionType()			{ return false; }


/**
 *	Return true if this is an intersection type.
 **/

public boolean isIntersectionType()		{ return false; }

/**
 *	Return true if this type is not known.
 **/

public boolean isCompiledType() 		{ return false; }
public boolean isUnknown()			{ return isCompiledType(); }


/**
 *	Return true if this type is a class type inheriting from Throwable.
 **/

public boolean isThrowable()			{ return false; }


/**
 *	Return true if this type is abstract (abstract class or an interface).
 **/

public boolean isAbstract()			{ return is_abstract; }

void setAbstract(boolean fg)			{ is_abstract = fg; }



/**
 *	Return true if this type is undefined.
 **/

public boolean isUndefined()			{ return false; }

void setUndefined(boolean fg)			{ }

/**
 *	Return true if this type is an annotation type
 **/

public boolean isAnnotationType()		{ return false; }


boolean isBaseKnown()				{ return false; }


/**
 *	Return true if this type comes from the user's context
 **/

public boolean isContextType()			{ return false; }

void setContextType(boolean fg) 		{ }


/**
 *	Return the base type associated with this type.  For an array this is the
 *	type of the array elements. For a method, it is the return type.  For a
 *	parameterized type it is the base type.
 **/

public JcompType getBaseType()			{ return null; }



/**
 *	Return the super type for a class type.
 **/

public JcompType getSuperType() 	{ return null; }


/**
 *	Return the set of interfaces implemented by a class type or extended by
 *	an interface type.
 **/

public Collection<JcompType> getInterfaces()	{ return null; }


/**
 *	Return the symbol corresponding to this type if there is one.
 **/

public JcompSymbol getDefinition()		{ return assoc_symbol; }

void setDefinition(JcompSymbol js)		{ assoc_symbol = js; }


/**
 *	Return the component types for this type.  For a method type this is the
 *	set of parameter types.  For a parameterized type, it is the set of
 *	type parameters.
 **/

public List<JcompType> getComponents()		{ return null; }



public SortedMap<String,JcompType> getOuterComponents() 
{
   SortedMap<String,JcompType> rslt = null;
   
   rslt = addToMap(rslt,getLocalComponents());
   
   JcompType oty = getOuterType();
   if (oty != null) {
      rslt = addToMap(rslt,oty.getOuterComponents());
    }
   
   JcompType sty = getSuperType();
   if (sty != null) {
      rslt = addToMap(rslt,sty.getOuterComponents());
    }
  
   /**************
  Collection<JcompType> ityps = getInterfaces();
   if (ityps != null) {
      for (JcompType ity : ityps) {
         rslt = addToMap(rslt,ity.getOuterComponents());
       }
    }
   **************/
   
   return rslt;
}


protected SortedMap<String,JcompType> getLocalComponents()
{
   return null;
}


private SortedMap<String,JcompType> addToMap(SortedMap<String,JcompType> orig,
      Map<String,JcompType> add)
{
   if (add == null || add.isEmpty()) return orig;
   if (orig == null) orig = new TreeMap<>(add);
   for (Map.Entry<String,JcompType> ent : add.entrySet()) {
      orig.putIfAbsent(ent.getKey(),ent.getValue());
    }
   
   return orig;
}



public boolean isVarArgs()			{ return false; }

JcompType getKnownType(JcompTyper typr) {
   return typr.findType("java.lang.Object");
}

public void defineAll(JcompTyper typer)
{
   if (getSuperType() != null) {
      getSuperType().defineAll(typer);
    }
   if (getOuterType() != null) {
      getOuterType().defineAll(typer);
    }
}

void addInterface(JcompType jt) 		{ }
void setSuperType(JcompType jt) 		{ }
void setOuterType(JcompType jt) 		{ }

void setScope(JcompScope js)			{ assoc_scope = js; }
public JcompScope getScope()			{ return assoc_scope; }

public Map<String,JcompType> getFields()
{
   return getFields(null);
}


public Map<String,JcompType> getFields(JcompTyper typer)
{
   if (typer != null) defineAll(typer);

   Map<String,JcompType> rslt = new HashMap<String,JcompType>();
   if (assoc_scope != null) assoc_scope.getFields(rslt);
   return rslt;
}



/**
 *	Return the type associated with this one.  This provides a mapping from
 *	primitive types to their object counterparts to handle automatic boxing.
 **/

public JcompType getAssociatedType()		{ return assoc_type; }

void setAssociatedType(JcompType jt)		{ assoc_type = jt; }



public String getSignature()			{ return type_signature; }

protected void setSignature(String s)
{
   type_signature = s;
}



/**
 *	Return true if this is a non-static inner type (i.e. has a this$0 field
 *	and extra constructor parameter
 **/

public boolean needsOuterClass()		{ return inner_nonstatic; }

public JcompType getOuterType() 		{ return null; }


void setInnerNonStatic(boolean fg)		{ inner_nonstatic = fg; }



public JcompType resetType(JcompTyper typer)
{
   JcompType ntyp = typer.findSystemType(getName());
   if (ntyp != null) return ntyp;
   return this;
}




/********************************************************************************/
/*										*/
/*	Creation methods							*/
/*										*/
/********************************************************************************/

/**
 *	Create an AST node representing this type.
 **/

public Type createAstNode(AST ast)
{
   return ast.newSimpleType(JcompAst.getQualifiedName(ast,getName()));
}


/**
 *	Create an AST node representing the default value of this type.
 **/

public Expression createDefaultValue(AST ast)
{
   return null;
}


/**
 *	Create an AST node representing a non-null defualt value of this type.
 **/

public Expression createNonNullValue(AST ast)
{
   return createDefaultValue(ast);
}


public JcompSymbol lookupField(JcompTyper typs,String id)
{
   return lookupField(typs,id,0);
}


JcompSymbol lookupField(JcompTyper typs,String id,int lvl)
{
   if (assoc_scope != null) return assoc_scope.lookupVariable(id);

   return null;
}


JcompSymbol lookupField(JcompTyper typs,String id,JcompType base,int lvl)
{
   return lookupField(typs,id,lvl);
}


final public JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,ASTNode n)
{
   return lookupMethod(typer,id,atyps,this,n);
}


final public JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps)
{
   return lookupMethod(typer,id,atyps,this,null);
}



protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,JcompType basetype,ASTNode n)
{
   if (assoc_scope != null) return assoc_scope.lookupMethod(id,atyps,this,n);

   return null;
}


public List<JcompSymbol> lookupMethods(JcompTyper typer,String id,String aid)
{
   defineAll(typer);

   List<JcompSymbol> rslt = new ArrayList<>();
   if (assoc_scope != null) {
      for (JcompSymbol js : assoc_scope.getDefinedMethods()) {
	 if (js.getName().equals(id) ||
	       (aid != null && js.getName().equals(aid))) {
	    rslt.add(js);
	  }
       }
    }

   return rslt;
}

final List<JcompSymbol> lookupStatics(JcompTyper typer,String id)
{
   return lookupStatics(typer,id,this);
}


protected List<JcompSymbol> lookupStatics(JcompTyper typer,String id,JcompType basetype)
{
   if (assoc_scope != null) return assoc_scope.lookupStatics(id);

   return null;
}

protected Set<JcompSymbol> lookupAbstracts(JcompTyper typer)
{
   if (assoc_scope != null) return assoc_scope.lookupAbstracts(typer);

   return new HashSet<JcompSymbol>();
}



/**
 *	Return true if this type is compatible with the given type.  Compatability means
 *	that either the types are identical or there is an automatic conversion from one
 *	to another (i.e. autoboxing, super type from sub type, etc.).
 **/

public boolean isCompatibleWith(JcompType jt)
{
   if (jt == this) return true;
   if (isClassType()) {
      if (jt.getName().equals("java.lang.Object")) return true;
    }

   return false;
}


public boolean isAssignCompatibleWith(JcompType jt)
{
   return isCompatibleWith(jt);
}



public List<JcompSymbol> isConformableFrom(JcompTyper typer,JcompType typ)
{
   if (isCompatibleWith(typ)) return null;

   return typ.isConformableTo(typer,this);
}



List<JcompSymbol> isConformableTo(JcompTyper typer,JcompType typ)
{
   return null;
}


public boolean isDerivedFrom(JcompType jt)
{
   if (jt == null) return false;

   while (jt.isParameterizedType())
      jt = jt.getBaseType();

   if (jt == this) return true;
   if (isPrimitiveType()) {
      if (!jt.isPrimitiveType()) return false;
      if (jt.isCompatibleWith(this)) return true;
      return false;
    }
   if (getSuperType() != null && getSuperType().isDerivedFrom(jt)) return true;
   else if (getSuperType() == null && jt.isJavaLangObject()) return true;
   if (getInterfaces() != null) {
      for (JcompType ity : getInterfaces()) {
	 if (ity.isDerivedFrom(jt)) return true;
       }
    }

   if (isArrayType() && jt.isArrayType()) {
      if (getBaseType().isDerivedFrom(jt.getBaseType())) return true;
    }

   return false;
}


public boolean definesAllNeededMethods(JcompTyper typer)	{ return true; }


public static JcompType mergeTypes(JcompTyper typr,JcompType jt1,JcompType jt2)
{
   if (jt1 == null) return jt2;
   if (jt2 == null) return jt1;
   if (jt1 == jt2) return jt1;

   if (jt1.isCompatibleWith(jt2)) return jt2;
   if (jt2.isCompatibleWith(jt1)) return jt1;

   if (!jt1.isPrimitiveType() && !jt2.isPrimitiveType()) {
      JcompType jt = findCommonParent(typr,jt1,jt2);
      if (jt != null) return jt;
    }
   else if (jt1.isPrimitiveType() && !jt2.isPrimitiveType()) {
      JcompType jt1a = jt1.getBaseType();
      if (jt1a != null) return mergeTypes(typr,jt1a,jt2);
    }
   else if (!jt1.isPrimitiveType() && jt2.isPrimitiveType()) {
      JcompType jt2a = jt2.getBaseType();
      if (jt2a != null) return mergeTypes(typr,jt1,jt2a);
    }

   System.err.println("INCOMPATIBLE MERGE: " + jt1 + " & " + jt2);

   return jt1;
}



private static JcompType findCommonParent(JcompTyper typr,JcompType jt1,JcompType jt2)
{
   jt1.getInterfaces();
   jt2.getInterfaces();
   JcompType best = typr.findSystemType("java.lang.Object");

   return best;
}


private static JcompType findCommonParent(JcompTyper typer,List<JcompType> base)
{
   if (base.size() == 0) {
      return typer.findSystemType("java.lang.Object");
    }
   for (JcompType t1 = base.get(0); t1 != null; t1 = t1.getSuperType()) {
      boolean compat = true;
      for (int i = 1; i < base.size(); ++i) {
	  compat &= base.get(i).isCompatibleWith(t1);
       }
      if (compat) return t1;
    }

   // should we find all common interfaces here???

   return typer.findSystemType("java.lang.Object");
}



public JcompType getCommonParent(JcompTyper typer,JcompType t2)
{
   if (t2 == this) return t2;

   synchronized (this) {
      if (parent_map == null) parent_map = new HashMap<>();
    }

   boolean setother = false;
   JcompType jt = null;
   synchronized (parent_map) {
      jt = parent_map.get(t2);
      if (jt == null) {
	 jt = computeCommonParent(typer,t2);
	 parent_map.put(t2,jt);
	 setother = true;
       }
    }

   if (setother) {
      t2.setCommonParent(this,jt);
    }

   return jt;
}



private void setCommonParent(JcompType dt,JcompType rslt)
{
   synchronized (this) {
      if (parent_map == null) parent_map = new HashMap<>();
    }
   synchronized (parent_map) {
      parent_map.put(dt,rslt);
    }
}


private JcompType computeCommonParent(JcompTyper typer,JcompType t2)
{
   if (this == t2) return this;

   if (isInterfaceType() && t2.isInterfaceType()) return findCommonInterface(typer,t2,null);
   else if (isInterfaceType()) {
      JcompType t3 = t2.findCommonClassInterface(this);
      if (t3 != null) return t3;
    }
   else if (t2.isInterfaceType()) {
      JcompType t3 = findCommonClassInterface(t2);
      if (t3 != null) return t3;
    }

   if (isArrayType() && t2.isArrayType()) return findCommonArray(typer,t2);
   else if (isArrayType() || t2.isArrayType())
      return typer.findSystemType("java.lang.Object");

   if (isDerivedFrom(t2)) return t2;
   else if (t2.isDerivedFrom(this)) return this;

   for (JcompType st = getSuperType(); st != null; st = st.getSuperType()) {
      if (st == t2 || t2.isDerivedFrom(st)) return st;
    }
   for (JcompType st = t2.getSuperType(); st != null; st = st.getSuperType()) {
      if (st == this || isDerivedFrom(st)) return st;
    }

   return typer.findSystemType("java.lang.Object");
}


private JcompType findCommonInterface(JcompTyper typer,JcompType t2,Set<JcompType> done)
{
   if (isDerivedFrom(t2)) return t2;
   else if (t2.isDerivedFrom(this)) return this;

   if (done == null) done = new HashSet<>();

   JcompType st = getSuperType();
   if (st != null) {
      if (!done.contains(st)) {
	 done.add(st);
	 JcompType c = st.findCommonInterface(typer,t2,done);
	 if (c.isInterfaceType()) return c;
       }
    }
   if (getInterfaces() != null) {
      for (JcompType it : getInterfaces()) {
	 if (done.contains(it)) continue;
	 done.add(it);
	 JcompType c = it.findCommonInterface(typer,t2,done);
	 if (c.isInterfaceType()) return c;
       }
    }

   return typer.findSystemType("java.lang.Object");
}


private JcompType findCommonClassInterface(JcompType typ)
{
   if (isDerivedFrom(typ)) {
      return this;
    }
   return null;
}


private JcompType findCommonArray(JcompTyper typer,JcompType t2)
{
   if (this == t2) return this;

   JcompType rslt = null;

   JcompType ac1 = getBaseType();
   JcompType ac2 = t2.getBaseType();

   if (isDerivedFrom(t2)) return t2;
   else if (t2.isDerivedFrom(this)) rslt = this;
   else if (ac1.isDerivedFrom(ac2)) rslt = t2;
   else if (ac2.isDerivedFrom(ac1)) rslt = this;
   else {
      JcompType fdt = ac1.getCommonParent(typer,ac2);
      rslt = typer.findArrayType(fdt);
    }

   return rslt;
}

/********************************************************************************/
/*										*/
/*	Known item methods							*/
/*										*/
/********************************************************************************/

protected JcompSymbol lookupKnownField(JcompTyper typs,String id,JcompType orig)
{
   JcompSymbol js = null;

   if (assoc_scope != null) js = assoc_scope.lookupVariable(id);

   if (js != null && orig != null && js.getType().getSignature() != null) {
      String fsgn = js.getType().getSignature();
      String csgn = getSignature();
      if (csgn == null) csgn = orig.getBaseType().getSignature();
      JcompType ntyp = typs.getParameterizedFieldType(fsgn,csgn,orig);
      if (ntyp != null) {
	
       }
    }

   if (js == null) {
      js = typs.lookupKnownField(getName(),id,orig);
      if (js != null) {
	 assoc_scope.defineVar(js);
       }
    }

   if (js == null) {
      JcompType sup = getSuperType();
      if (sup != null && sup != this) {
	 js = sup.lookupKnownField(typs,id,orig);
	 if (js != null) return js;
       }
      Collection<JcompType> ityps = getInterfaces();
      if (ityps != null) {
	 for (JcompType ity : ityps) {
	    js = ity.lookupKnownField(typs,id,orig);
	    if (js != null) return js;
	  }
       }
    }

   return js;
}



protected JcompSymbol lookupKnownMethod(JcompTyper typs,String id,JcompType mtyp,JcompType basetype,ASTNode n)
{
   JcompSymbol js = null;
   JcompSymbol checkjs = null;

   if (assoc_scope != null) {
      js = assoc_scope.lookupMethod(id,mtyp,basetype,n);
      if (js != null) {
	 JcompType jtyp = js.getType();
	 List<JcompType> jargs = jtyp.getComponents();
	 List<JcompType> margs = mtyp.getComponents();
	 if (jargs.size() != margs.size()) js = null;
	 else {
	    for (int i = 0; i < jargs.size(); ++i) {
	       JcompType m0 = margs.get(i);
	       JcompType j0 = jargs.get(i);
	       while (m0.isParameterizedType()) m0 = m0.getBaseType();
	       while (j0.isParameterizedType()) j0 = j0.getBaseType();
	       if (m0.equals(j0)) continue;
	       if (!margs.get(i).equals(jargs.get(i)) && !margs.get(i).isErrorType()) {
		  checkjs = js;
		  js = null;
		  break;
		}	
	     }
	  }
       }
      if (js != null) return js;
    }

   if (js == null) {
      js = typs.lookupKnownMethod(getName(),id,mtyp,basetype);
      if (js != null && checkjs != null) {
	 List<JcompType> jargs = js.getType().getComponents();
	 List<JcompType> cargs = checkjs.getType().getComponents();
	 if (jargs.size() == cargs.size()) {
	    boolean fnd = true;
	    for (int i = 0; i < jargs.size(); ++i) {
	       if (!jargs.get(i).equals(cargs.get(i))) {
		  fnd = false;
		}
	     }
	    if (fnd) return checkjs;
	  }
       }
      if (js != null && !js.isGenericReturn()) {
	 // this is an unsafe optimization and can be non-optimizing at times
	 // boolean known = true;
	 // for (JcompType xjt : js.getType().getComponents()) {
	    // if (!xjt.isBaseKnown())
	       // known = false;
	  // }
	 // if (!js.getType().getBaseType().isBaseKnown())
	    // known = false;
	
	 // if (known) {
	    // if (basetype == this || basetype == null || basetype.getScope() == null) {
	       // assoc_scope.defineMethod(js);
	     // }
	    // else {
	       // basetype.getScope().defineMethod(js);
	     // }
	  // }
       }
    }

   return js;
}



protected List<JcompSymbol> lookupKnownStatics(JcompTyper typs,String id,JcompType basetype)
{
   List<JcompSymbol> rslt = null;

   if (assoc_scope != null) rslt = assoc_scope.lookupStatics(id);

   List<JcompSymbol> r1 = typs.lookupKnownStatics(getJavaTypeName(),id,basetype);

   if (rslt == null) rslt = r1;
   else if (r1 != null) rslt.addAll(r1);

   return rslt;
}


protected Set<JcompSymbol> lookupKnownAbstracts(JcompTyper typer)
{
   Set<JcompSymbol> rslt;
   if (assoc_scope != null) rslt = assoc_scope.lookupAbstracts(typer);
   else rslt = new HashSet<JcompSymbol>();
   Set<JcompSymbol> r1 = typer.lookupKnownAbstracts(getName());
   rslt.addAll(r1);

   return rslt;

}



/********************************************************************************/
/*										*/
/*	Child type methods							*/
/*										*/
/********************************************************************************/

public Collection<String> getChildTypes()		{ return null; }

void addChild(JcompType chld)				{ }

public JcompType findChildForInterface(JcompTyper typer,JcompType ity)
{
   JcompType fdt0 = null;

   Collection<String> ctyps = getChildTypes();
   if (ctyps != null) {
      try {
	 for (String knm : ctyps) {
	    JcompType k = typer.findType(knm);
	    if (k == null) continue;
	    JcompType r = k.findChildForInterface(typer,ity);
	    if (r != null) {
	       if (fdt0 == null) fdt0 = r;
	       else return ity; // if more than one candidate, return the interface type
	     }
	  }
       }
      catch (ConcurrentModificationException e) {
	 return findChildForInterface(typer,ity);
       }
    }

   if (fdt0 == null) {
      if (!isAbstract() && isDerivedFrom(ity)) fdt0 = this;
    }

   return fdt0;
}






      



void applyParametersToType(JcompTyper typer)                    { }



static protected List<JcompSymbol> getAbstractMethods(JcompType jt)
{
   List<JcompSymbol> rslt = new ArrayList<>();
   if (jt.assoc_scope != null) {
      for (JcompSymbol js : jt.assoc_scope.getDefinedMethods()) {
	 if (js.isAbstract()) rslt.add(js);
       }
    }
   return rslt;
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return getName();
}


/**
 *	Return the java internal (JVM) name for this type.
 **/

abstract public String getJavaTypeName();



/********************************************************************************/
/*										*/
/*	Methods for protection checking 					*/
/*										*/
/********************************************************************************/

String getPackageName()  {
   String nm = getName();
   int idx = nm.lastIndexOf(".");
   if (idx < 0) return "<DEFAULT>";
   for ( ; ; ) {
      String pkg = nm.substring(0,idx);
      idx = pkg.lastIndexOf(".");
      if (idx < 0) return pkg;
      if (idx == pkg.length()-1) {
	 pkg = pkg.substring(0,idx);
       }
      else if (Character.isUpperCase(pkg.charAt(idx+1))) {
	 pkg = pkg.substring(0,idx);
       }
      else return pkg;
    }
}



static boolean checkProtections(JcompSymbol js,JcompType basetype,ASTNode n)
{
   if (js == null || js.isPublic()) return true;
   JcompType fromtype = null;
   List<JcompType> outertypes = null;
   for (ASTNode p = n; p != null; p = p.getParent()) {
      switch (p.getNodeType()) {
	 case ASTNode.TYPE_DECLARATION :
	 case ASTNode.ENUM_DECLARATION :
	 case ASTNode.ANNOTATION_TYPE_DECLARATION :
	    JcompType ctyp = JcompAst.getJavaType(p);
	    if (fromtype == null) fromtype = ctyp;
	    else {
	       if (outertypes == null) outertypes = new ArrayList<>();
	       outertypes.add(ctyp);
	     }
	    break;
       }
    }
   if (fromtype == null) return true;

   JcompType totype = js.getClassType();
   if (totype == null) return true;
   if (totype.isParameterizedType()) totype = totype.getBaseType();
   if (fromtype == totype) return true;
   if (fromtype.getName().startsWith(totype.getName() + ".")) return true;
   if (totype.getName().startsWith(fromtype.getName() + ".")) return true;
   if (outertypes != null) {
      for (JcompType oty : outertypes) {
	 if (totype.getName().startsWith(oty.getName() + ".")) return true;
       }
    }
   if (js.isPrivate()) {
      String nm1 = totype.getName();
      String nm2 = fromtype.getName();
      if (nm2.startsWith(nm1)) return true;
    }
   else if (js.isProtected()) {
      if (fromtype.isCompatibleWith(basetype)) {
	 if (fromtype.isCompatibleWith(totype)) return true;
	 if (outertypes != null) {
	    for (JcompType oty : outertypes) {
	       if (oty.isCompatibleWith(totype)) return true;
	     }
	  }
       }
      if (js.getName().equals("clone") && basetype.isArrayType()) return true;
      String pkg1 = fromtype.getPackageName();
      String pkg2 = totype.getPackageName();
      if (pkg1.equals(pkg2)) return true;
    }
   else {
      String pkg1 = fromtype.getPackageName();
      String pkg2 = totype.getPackageName();
      if (pkg1.equals(pkg2)) return true;
    }

   // System.err.println("ATTEMPT TO ACCESS SYMBOL OUT OF CONTEXT:" + n + " " + js);

   return false;
}


/********************************************************************************/
/*										*/
/*	Primitive types 							*/
/*										*/
/********************************************************************************/

private static class PrimType extends JcompType {

   private PrimitiveType.Code type_code;
   private JcompType object_type;

   PrimType(PrimitiveType.Code tc,JcompType ot) {
      super(tc.toString().toLowerCase());
      type_code = tc;
      object_type = ot;
    }

   @Override public boolean isPrimitiveType()		{ return true; }
   @Override public boolean isBooleanType()		{ return type_code == PrimitiveType.BOOLEAN; }
   @Override public boolean isNumericType() {
      if (type_code == PrimitiveType.BOOLEAN) return false;
      if (type_code == PrimitiveType.VOID) return false;
      return true;
    }

   @Override public boolean isFloatingType() {
      if (type_code == PrimitiveType.FLOAT) return true;
      if (type_code == PrimitiveType.DOUBLE) return true;
      return false;
    }

   @Override public boolean isIntType() 		{ return type_code == PrimitiveType.INT; }
   @Override public boolean isCharType()		{ return type_code == PrimitiveType.CHAR; }
   @Override public boolean isVoidType()		{ return type_code == PrimitiveType.VOID; }
   @Override public boolean isFloatType()		{ return type_code == PrimitiveType.FLOAT; }
   @Override public boolean isDoubleType()		{ return type_code == PrimitiveType.DOUBLE; }
   @Override public boolean isShortType()		{ return type_code == PrimitiveType.SHORT; }
   @Override public boolean isByteType()		{ return type_code == PrimitiveType.BYTE; }
   @Override public boolean isLongType()		{ return type_code == PrimitiveType.LONG; }

   @Override public boolean isCategory2() {
      if (type_code == PrimitiveType.LONG) return true;
      if (type_code == PrimitiveType.DOUBLE) return true;
      return false;
    }

   @Override public boolean isBroaderType(JcompType jt) {
      if (jt == this) return true;
      if (!jt.isPrimitiveType()) return false;
      PrimType pt = (PrimType) jt;
      if (isBooleanType()) return false;
      if (jt.isBooleanType()) return true;
      if (type_code == PrimitiveType.CHAR) return false;
      if (pt.type_code == PrimitiveType.CHAR) return true;
      int ln0 = classLength();
      int ln1 = pt.classLength();
      return ln0 >= ln1;
    }


   @Override boolean isBaseKnown()			{ return true; }
   @Override JcompType getKnownType(JcompTyper t)	{ return this; }
   @Override public JcompType getBaseType()		{ return object_type; }
   @Override public void setAssociatedType(JcompType jt) {
      object_type = jt;
    }

   @Override public boolean isCompatibleWith(JcompType jt) {
      if (jt == this) return true;
      if (jt == null) return false;
      if (jt.isPrimitiveType()) {
	 PrimType pt = (PrimType) jt;
	 if (type_code == PrimitiveType.BYTE) {
	    if (pt.type_code == PrimitiveType.BYTE || pt.type_code == PrimitiveType.SHORT ||
		   pt.type_code == PrimitiveType.CHAR || pt.type_code == PrimitiveType.INT ||
		   pt.type_code == PrimitiveType.LONG ||
		   pt.type_code == PrimitiveType.FLOAT || pt.type_code == PrimitiveType.DOUBLE)
	       return true;
	  }
	 else if (type_code == PrimitiveType.SHORT || type_code == PrimitiveType.CHAR) {
	    if (pt.type_code == PrimitiveType.SHORT ||
		   pt.type_code == PrimitiveType.CHAR || pt.type_code == PrimitiveType.INT ||
		   pt.type_code == PrimitiveType.LONG ||
		   pt.type_code == PrimitiveType.FLOAT || pt.type_code == PrimitiveType.DOUBLE)
	       return true;
	  }
	 else if (type_code == PrimitiveType.INT) {
	    if (pt.type_code == PrimitiveType.INT ||
		   pt.type_code == PrimitiveType.LONG ||
		   pt.type_code == PrimitiveType.FLOAT || pt.type_code == PrimitiveType.DOUBLE)
	       return true;
	  }
	 else if (type_code == PrimitiveType.LONG) {
	    if (pt.type_code == PrimitiveType.LONG ||
		   pt.type_code == PrimitiveType.FLOAT || pt.type_code == PrimitiveType.DOUBLE)
	       return true;
	  }
	 else if (type_code == PrimitiveType.FLOAT) {
	    if (pt.type_code == PrimitiveType.FLOAT || pt.type_code == PrimitiveType.DOUBLE)
	       return true;
	  }
       }
      else if (jt.getAssociatedType() != null) {
	 JcompType assoctype = jt.getAssociatedType();
	 if (isCompatibleWith(assoctype)) return true;
       }
      else if (object_type != null && jt.getName().startsWith("java.lang.")) {
	 return object_type.isCompatibleWith(jt);
       }
      return false;
    }

   @Override public Type createAstNode(AST ast) {
      return ast.newPrimitiveType(type_code);
    }

   @Override public Expression createDefaultValue(AST ast) {
      if (type_code == PrimitiveType.VOID) return null;
      else if (type_code == PrimitiveType.BOOLEAN) return ast.newBooleanLiteral(false);
      return JcompAst.newNumberLiteral(ast,0);
    }

   @Override public String getJavaTypeName() {
      if (type_code == PrimitiveType.BYTE) return "B";
      if (type_code == PrimitiveType.CHAR) return "C";
      if (type_code == PrimitiveType.DOUBLE) return "D";
      if (type_code == PrimitiveType.FLOAT) return "F";
      if (type_code == PrimitiveType.INT) return "I";
      if (type_code == PrimitiveType.LONG) return "J";
      if (type_code == PrimitiveType.SHORT) return "S";
      if (type_code == PrimitiveType.VOID) return "V";
      if (type_code == PrimitiveType.BOOLEAN) return "Z";
      return "V";
    }

   private int classLength() {
      if (type_code == PrimitiveType.BOOLEAN || type_code == PrimitiveType.BYTE)
	 return 1;
      if (type_code == PrimitiveType.CHAR || type_code == PrimitiveType.SHORT)
	 return 2;
      if (type_code == PrimitiveType.INT || type_code == PrimitiveType.FLOAT)
	 return 4;
      if (type_code == PrimitiveType.LONG || type_code == PrimitiveType.DOUBLE)
	 return 8;
      return 0;
    }
}	// end of subclass PrimType




/********************************************************************************/
/*										*/
/*	Array types								*/
/*										*/
/********************************************************************************/

private static class ArrayType extends JcompType {

   private JcompType base_type;

   ArrayType(JcompType b) {
      super(b.getName() + "[]");
      base_type = b;
    }

   @Override public boolean isArrayType()	 { return true; }
   @Override public JcompType getBaseType()	 { return base_type; }
   @Override JcompType getKnownType(JcompTyper typer) {
      JcompType bt = base_type.getKnownType(typer);
      if (bt == base_type) return this;
      return typer.findArrayType(bt);
    }

   @Override boolean isBaseKnown()		{ return base_type.isBaseKnown(); }

   @Override public boolean isComplete()	{ return base_type.isComplete(); }

   @Override public boolean isCompatibleWith(JcompType jt) {
      if (jt == this) return true;
      if (jt.getName().equals("java.lang.Object")) return true;
      if (jt.isTypeVariable()) return true;
      if (!jt.isArrayType()) return false;
      if (getBaseType() == jt.getBaseType()) return true;
      // if (jt.getBaseType().isTypeVariable()) return true;
      if (getBaseType().isPrimitiveType() ||
	    jt.getBaseType().isPrimitiveType()) return false;
      return getBaseType().isCompatibleWith(jt.getBaseType());
    }

   @Override protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,
	 JcompType basetype,ASTNode n) {
      JcompType jt = typer.findType("java.lang.Object");
      return jt.lookupMethod(typer,id,atyps,basetype,n);
    }

   @SuppressWarnings("unchecked")
   @Override public Type createAstNode(AST ast) {
      int ndim = 1;
      JcompType bt = base_type;
      while (bt.isArrayType()) {
	 bt = bt.getBaseType();
	 ++ndim;
       }
      org.eclipse.jdt.core.dom.ArrayType at = ast.newArrayType(bt.createAstNode(ast));
      while (at.getDimensions() < ndim) {
	 at.dimensions().add(ast.newDimension());
       }
      return at;
      // return ast.newArrayType(base_type.createAstNode(ast));
    }

   @Override public Expression createDefaultValue(AST ast) {
      Expression e1 = ast.newNullLiteral();
      if (base_type.isBinaryType() || base_type.isPrimitiveType()) {
	 Type asttyp = createAstNode(ast);
	 CastExpression e2 = ast.newCastExpression();
	 e2.setExpression(e1);
	 e2.setType(asttyp);
	 e1 = e2;
       }
      return e1;
    }

   @Override public JcompType resetType(JcompTyper typer) {
      JcompType ntyp = base_type.resetType(typer);
      if (ntyp == base_type) return this;
      return typer.findArrayType(ntyp);
    }

   @Override public String getJavaTypeName() {
      return "[" + base_type.getJavaTypeName();
    }

}	// end of subclass ArrayType




/********************************************************************************/
/*										*/
/*	Generic class/interface type						*/
/*										*/
/********************************************************************************/

private static abstract class ClassInterfaceType extends JcompType {

   private JcompType super_type;
   private JcompType outer_type;
   private List<JcompType> interface_types;
   private boolean is_context;
   private List<String> child_types;

   protected ClassInterfaceType(String nm) {
      super(nm);
      super_type = null;
      outer_type = null;
      interface_types = null;
      is_context = false;
      child_types = null;
    }

   @Override void setSuperType(JcompType t) {
      if (t != this && t != null) {
         super_type = t;
         t.addChild(this);
       }
    }

   @Override void setOuterType(JcompType t) {
      outer_type = t;
    }
   @Override public JcompType getOuterType()		{ return outer_type; }

   @Override void addInterface(JcompType t) {
      if (t == null) return;
      if (interface_types == null) interface_types = new ArrayList<JcompType>();
      else if (interface_types.contains(t)) return;
      else if (t == this) return;
      interface_types.add(t);
      t.addChild(this);
    }

   @Override public Collection<JcompType> getInterfaces()	{ return interface_types; }

   @Override public boolean isClassType()			{ return true; }

   @Override boolean isBaseKnown()				{ return isBinaryType(); }

   @Override public JcompType getSuperType()			{ return super_type; }

   @Override public boolean isJavaLangObject() {
      return getName().equals("java.lang.Object");
    }

   @Override public boolean isStringType() {
      return getName().equals("java.lang.String");
   }

   @Override public boolean isCompatibleWith(JcompType jt) {
      if (jt == null) return false;
      if (jt == this) return true;
      if (jt.getName().equals("java.lang.Object")) return true;
      while (jt.isParameterizedType()) jt = jt.getBaseType();
      if (jt == this) return true;
      if (jt.isTypeVariable()) return true;
      if (jt.isUnionType()) {
         for (JcompType uty : jt.getComponents()) {
            if (isCompatibleWith(uty)) return true;
          }
         return false;
       }
      if (jt.isIntersectionType()) {
         for (JcompType uty : jt.getComponents()) {
            if (!isCompatibleWith(uty)) return false;
          }
         return true;
       }
      if (jt.isInterfaceType() && interface_types != null) {
         for (JcompType ity : interface_types) {
            if (ity.isCompatibleWith(jt)) return true;
          }
       }
      if (jt.getName().equals(getName()))
         return true;
      if (super_type != null) {
         if (super_type.isCompatibleWith(jt)) return true;
       }
      if (jt.isPrimitiveType()) {
         JcompType at = getAssociatedType();
         if (at != null) return at.isCompatibleWith(jt);
       }
      return false;
    }

   @Override public List<JcompSymbol> isConformableFrom(JcompTyper typer,JcompType typ)
   {
      if (isCompatibleWith(typ)) return null;
   
      List<JcompSymbol> rslt = typ.isConformableTo(typer,this);
   
      List<JcompType> prms = new ArrayList<JcompType>();
      prms.add(typ);
      JcompType atyp = createMethodType(null,prms,false,null);
      atyp = typer.fixJavaType(atyp);
      JcompSymbol sym = lookupMethod(typer,"<init>",atyp);
   
      if (sym != null && !sym.isPrivate()) {
         if (rslt == null) rslt = new ArrayList<JcompSymbol>();
         rslt.add(sym);
       }
   
      return rslt;
    }


   @Override List<JcompSymbol> isConformableTo(JcompTyper typer,JcompType typ) {
      List<JcompType> prms = new ArrayList<JcompType>();
      JcompType atyp = createMethodType(typ,prms,false,null);
      atyp = typer.fixJavaType(atyp);
   
      Set<JcompSymbol> rslt = new HashSet<JcompSymbol>();
      if (getScope() != null) {
         for (JcompSymbol js : getScope().getDefinedMethods()) {
            if (js.isPrivate() || js.getName().contains("<")) continue;
            JcompType mty = js.getType();
            if (typ.isCompatibleWith(mty) && typ == mty.getBaseType()) {
               rslt.add(js);
             }
          }
       }
   
      List<JcompSymbol> cands = typer.findKnownMethods(getName());
      if (cands != null) {
         for (JcompSymbol jsx : cands) {
            if (jsx.getName().contains("<")) continue;
            if (jsx.isPrivate()) continue;
            if (jsx.getType().isCompatibleWith(atyp)) {
               if (jsx.getType().getBaseType() == atyp.getBaseType()) {
        	  rslt.add(jsx);
        	  break;
        	}
             }
          }
       }
   
      if (super_type != null) {
         List<JcompSymbol> nrslt = super_type.isConformableTo(typer,typ);
         if (nrslt != null) {
            rslt.addAll(nrslt);
          }
       }
   
      if (rslt.size() == 0) return null;
   
      return new ArrayList<JcompSymbol>(rslt);
    }

   @Override JcompSymbol lookupField(JcompTyper typs,String id,int lvl) {
      JcompSymbol js = super.lookupField(typs,id,lvl+1);
      if (js != null) return js;
      if (lvl > 20) return null;
      if (super_type != null) js = super_type.lookupField(typs,id,lvl+1);
      if (js != null) return js;
      if (interface_types != null) {
         for (JcompType it : interface_types) {
            js = it.lookupField(typs,id,lvl+1);
            if (js != null) return js;
          }
       }
      if (outer_type != null) {
         js = outer_type.lookupField(typs,id,lvl+1);
         if (js != null) return js;
       }
      return null;
    }

   @Override protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,
         JcompType basetype,ASTNode n) {
      JcompSymbol js = super.lookupMethod(typer,id,atyps,basetype,n);
      if (js != null && id.equals("<init>") && basetype != js.getClassType()) {
         js = null;
       }
      if (js != null && !js.isStatic() && js.getClassType() != this && getOuterType() != null &&
            !needsOuterClass()) {
         js = null;
       }
      if (js != null && !js.isStatic() && js.getClassType() != this && needsOuterClass() && getOuterType() != null) {
         if (super_type != null && super_type != this) {
            JcompSymbol sjs = super_type.lookupMethod(typer,id,atyps,basetype,n);
            if (sjs != null) {
               // prefer supertype method over outer method
               js = sjs;
             }
          }
       }
   
      if (js != null) return js;
      if (super_type != null && super_type != this) {
         super_type.defineAll(typer);
         if (id == null || !id.equals("<init>"))
            js = super_type.lookupMethod(typer,id,atyps,basetype,n);
       }
      else if (!getName().equals("java.lang.Object") && !id.equals("<init>")) {
         JcompType ot = typer.findType("java.lang.Object");
         js = ot.lookupMethod(typer,id,atyps,basetype,n);
       }
      if (js != null) return js;
      if (interface_types != null) {
         if (!isAbstract()) {
            for (JcompType it : interface_types) {
               js = it.lookupMethod(typer,id,atyps,basetype,n);
               if (js != null) return js;
             }
          }
         else {
            for (JcompType it : interface_types) {
               js = it.lookupMethod(typer,id,atyps,basetype,n);
               if (js != null && !js.isAbstract()) return js;
               else if (js != null) {
        	  return js;
        	}
             }
          }
       }
      if (outer_type != null && (id == null || !id.equals("<init>"))) {
         js = outer_type.lookupMethod(typer,id,atyps,basetype,n);
         if (js != null) return js;
       }
      return null;
    }

   @Override protected List<JcompSymbol> lookupStatics(JcompTyper typer,String id,JcompType basetype) {
      List<JcompSymbol> rslt = super.lookupStatics(typer,id,basetype);
      if (super_type != null) {
	 List<JcompSymbol> r1 = super_type.lookupStatics(typer,id,basetype);
	 if (r1 != null) {
	    if (rslt == null) rslt = r1;
	    else rslt.addAll(r1);
	  }
       }

      if (interface_types != null) {
	 for (JcompType it : interface_types) {
	    List<JcompSymbol> r2 = it.lookupStatics(typer,id,basetype);
	    if (r2 != null) {
	       if (rslt == null) rslt = r2;
	       else rslt.addAll(r2);
	     }
	  }
       }

      return rslt;
    }

   @Override public Map<String,JcompType> getFields() {
      Map<String,JcompType> rslt = super.getFields();
      Map<String,JcompType> toadd = new HashMap<String,JcompType>();
      if (super_type != null) {
	 Map<String,JcompType> nf = super_type.getFields();
	 if (nf != null) toadd.putAll(nf);
       }
      if (interface_types != null) {
	 for (JcompType jt : interface_types) {
	    Map<String,JcompType> ntypes = jt.getFields();
	    if (ntypes != null) toadd.putAll(ntypes);
	  }
       }
      for (Map.Entry<String,JcompType> ent : toadd.entrySet()) {
	 String fnm = ent.getKey();
	 if (!rslt.containsKey(fnm)) rslt.put(fnm,ent.getValue());
       }
      return rslt;
    }

   @Override protected Set<JcompSymbol> lookupAbstracts(JcompTyper typer) {
      Set<JcompSymbol> rslt = super.lookupAbstracts(typer);
      Set<JcompSymbol> srslt = new HashSet<JcompSymbol>();
      if (super_type != null) {
	 srslt.addAll(super_type.lookupAbstracts(typer));
       }
      if (interface_types != null) {
	 for (JcompType it : interface_types) {
	    Set<JcompSymbol> r2 = it.lookupAbstracts(typer);
	    srslt.addAll(r2);
	  }
       }
      for (JcompSymbol js : srslt) {
	 JcompSymbol njs = lookupMethod(typer,js.getName(),js.getType());
	 if (njs == null || njs.isAbstract()) rslt.add(js);
       }
      return rslt;
    }

   @Override public Expression createDefaultValue(AST ast) {
      if (getName().equals("java.util.ResourceBundle")) {
	 return handleResourceBundle(ast);
       }
      Expression e1 = ast.newNullLiteral();
      if (isBinaryType()) {
	 String nm = getName();
	 CastExpression e2 = ast.newCastExpression();
	 e2.setExpression(e1);
	 Name tnm = JcompAst.getQualifiedName(ast,nm);
	 Type ty = ast.newSimpleType(tnm);
	 e2.setType(ty);
	 e1 = e2;
       }
      return e1;
    }

   @Override public Expression createNonNullValue(AST ast) { return buildNonNullValue(ast,2); }

   @Override public boolean isContextType()		 { return is_context; }
   @Override void setContextType(boolean fg)		 { is_context = fg; }

   @SuppressWarnings("unchecked")
   private Expression buildNonNullValue(AST ast,int lvl) {
      if (getName().equals("java.util.ResourceBundle")) {
	 return handleResourceBundle(ast);
       }
      else if (getName().equals("java.lang.String")) {
	 StringLiteral sl = ast.newStringLiteral();
	 sl.setLiteralValue(JcompAst.getUniqueString("JCOMP_String_"));
	 return sl;
       }
      JcompSymbol js = findBestConstructor();
      if (js == null) return createDefaultValue(ast);

      ClassInstanceCreation cic = ast.newClassInstanceCreation();
      Name nm = JcompAst.getQualifiedName(ast,getName());
      Type nty = ast.newSimpleType(nm);
      cic.setType(nty);
      for (JcompType aty : js.getType().getComponents()) {
	 Expression aex = null;
	 if (lvl == 0) aex = aty.createDefaultValue(ast);
	 else if (aty.isInterfaceType()) aex = aty.createDefaultValue(ast);
	 // TODO: handle case where aty is abstract
	 else if (aty.getName().equals("java.lang.String")) {
	    StringLiteral sl = ast.newStringLiteral();
	    sl.setLiteralValue(JcompAst.getUniqueString("JCOMP_String_"));
	    aex = sl;
	  }
	 else if (aty instanceof ClassInterfaceType) {
	    ClassInterfaceType cit = (ClassInterfaceType) aty;
	    aex = cit.buildNonNullValue(ast,lvl-1);
	  }
	 else aex = aty.createNonNullValue(ast);
	 cic.arguments().add(aex);
       }

      return cic;
    }

   private Expression handleResourceBundle(AST ast) {
      MethodInvocation mi = ast.newMethodInvocation();
      Name nm = JcompAst.getQualifiedName(ast,"edu.brown.cs.s6.runner.RunnerResourceBundle");
      mi.setExpression(nm);
      SimpleName cl = JcompAst.getSimpleName(ast,"getDummyBundle");
      mi.setName(cl);
      return mi;
    }

   private JcompSymbol findBestConstructor() {
      if (getScope() == null) return null;
      JcompSymbol cnst = null;
      for (JcompSymbol js : getScope().getDefinedMethods()) {
	 if (js.isConstructorSymbol() && isUsableConstructor(js)) {
	    if (isBetterConstructor(js,cnst)) cnst = js;
	  }
       }
      return cnst;
    }


   private boolean isUsableConstructor(JcompSymbol js) {
      if (js.isPrivate()) return false;
      return true;
    }

   private boolean isBetterConstructor(JcompSymbol newjs,JcompSymbol oldjs) {
      if (oldjs == null) return true;
      int newscore = getConstructorScore(newjs);
      int oldscore = getConstructorScore(oldjs);
      if (newscore > oldscore) return true;
      return false;
    }

   private int getConstructorScore(JcompSymbol js) {
      int score = 0;
      JcompType mty = js.getType();
      for (JcompType aty : mty.getComponents()) {
	 if (aty.isPrimitiveType()) score += 10;
	 else if (aty.getName().equals("java.lang.String")) score += 8;
	 else {
	    if (aty.isBinaryType() && !js.getClassType().isBinaryType()) score += 3;
	    if (aty.getScope() != null) {
	       for (JcompSymbol xjs : getScope().getDefinedMethods()) {
		  if (xjs.isConstructorSymbol() && isUsableConstructor(xjs)) {
		     score += 1;
		   }
		}
	     }
	  }

       }
      return score;
    }

   @Override public boolean isThrowable() {
      for (JcompType jt = this; jt != null; jt = jt.getSuperType()) {
	 if (jt.getName().equals("java.lang.Throwable")) return true;
       }
      return false;
    }

   @Override public String getJavaTypeName() {
      if (outer_type == null) {
         return "L" + getName().replace(".","/") + ";";
       }
      String s = outer_type.getJavaTypeName();
      int ln = s.length();
      s = s.substring(1,ln-1);
      String s1 = getName();
      String s3 = s1.replace(".","/");
      if (s3.startsWith(s)) {
         s1 = s1.substring(s.length()).replace(".","$");
       }
      else if (outer_type.isParameterizedType()) {
         String s2 = outer_type.getBaseType().getJavaTypeName();
         int ln2 = s2.length();
         s2 = s2.substring(1,ln2-1);
         if (s3.startsWith(s2)) {
            s1 = s1.substring(s2.length()).replace(".","$");
            // s = s2;     
          }
       }
      else {
         System.err.println("Can't decode java type name");
         return "L" + getName().replace(".","/") + ";";
       }
      
      return "L" + s + s1 + ";";
    }

   @Override public JcompType getFunctionalType() {
      if (!isInterfaceType()) return null;
      Collection<JcompSymbol> syms = getAbstractMethods(this);
      if (syms.size() > 1) return null;
      if (interface_types != null) {
	 for (JcompType jt : interface_types) {
	    Collection<JcompSymbol> ssyms = getAbstractMethods(jt);
	    if (ssyms.size() > 0) {
	       if (syms.size() == 0 && ssyms.size() == 1) syms = ssyms;
	       else return null;
	     }
	  }
       }
      for (JcompSymbol js : syms) {
	 return js.getType();
       }
      return null;
    }

   synchronized void addChild(JcompType jt) {
      if (child_types == null) child_types = new ArrayList<>();
      child_types.add(jt.getName());
    }

   @Override public Collection<String> getChildTypes()	{ return child_types; }

}	// end of innerclass ClassInterfaceType




private static abstract class BinaryClassInterfaceType extends ClassInterfaceType {

   BinaryClassInterfaceType(String nm,String sgn) {
      super(nm);
      setScope(new JcompScopeFixed());
      setSignature(sgn);
    }

   @Override JcompSymbol lookupField(JcompTyper typs,String id,int lvl) {
      return lookupKnownField(typs,id,null);
    }


   @Override JcompSymbol lookupField(JcompTyper typs,String id,JcompType base,int lvl)
   {
      if (base == null || base == this) return lookupField(typs,id,lvl);

      return lookupKnownField(typs,id,base);
   }

   @Override protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,JcompType basetype,ASTNode n) {
      JcompSymbol js = lookupKnownMethod(typer,id,atyps,basetype,n);
      if (js != null) return js;
      js = super.lookupMethod(typer,id,atyps,basetype,n);
      if (js != null) return js;
      return null;
    }

   @Override protected List<JcompSymbol> lookupStatics(JcompTyper typer,String id,JcompType basetype) {
      return lookupKnownStatics(typer,id,basetype);
    }

   @Override protected Set<JcompSymbol> lookupAbstracts(JcompTyper typer) {
      return lookupKnownAbstracts(typer);
    }
   @Override JcompType getKnownType(JcompTyper typer)	{ return this; }
   @Override public boolean isBinaryType()		{ return true; }

   @Override public void defineAll(JcompTyper typer) {
      typer.defineAll(getName(),getScope());
    }

}	// end of innerclass KnownClassInterfaceType




private static abstract class CompiledClassInterfaceType extends ClassInterfaceType {

   private Set<String> field_names;
   private Map<String,Set<JcompType>> method_names;

   CompiledClassInterfaceType(String nm) {
      super(nm);
      field_names = new HashSet<String>();
      method_names = new HashMap<String,Set<JcompType>>();
    }

   @Override JcompType getKnownType(JcompTyper typer) {
      if (getSuperType() != null) return getSuperType().getKnownType(typer);
   
      JcompType t0 = typer.findType(TYPE_ANY_CLASS);
      if (getInterfaces() != null) {
         for (JcompType ity : getInterfaces()) {
            JcompType t1 = ity.getKnownType(typer);
            if (t1 != t0) return t1;
          }
       }
      return t0;
    }

   @Override JcompSymbol lookupField(JcompTyper typs,String id,int lvl) {
      JcompSymbol js = super.lookupField(typs,id,lvl+1);
      if (js != null) {
	 field_names.add(id);
       }
      return js;
    }

    @Override protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,
	  JcompType basetype,ASTNode n) {
      JcompSymbol js = super.lookupMethod(typer,id,atyps,basetype,n);
      if (js == null && basetype == this) {
	 Set<JcompType> args = method_names.get(id);
	 if (args == null) {
	    args = new HashSet<JcompType>();
	    method_names.put(id,args);
	  }
	 atyps = typer.fixJavaType(atyps);
	 args.add(atyps);
       }
      return js;
    }

    @Override public boolean definesAllNeededMethods(JcompTyper typer) {
       if (isAbstract()) return true;
       Set<JcompSymbol> abs = lookupAbstracts(typer);
       if (abs.isEmpty()) return true;
       return false;
     }


   @Override public boolean isCompiledType()		{ return true; }

   @Override public JcompType resetType(JcompTyper typer) {
      JcompType ntyp = typer.findType(getName());
      if (ntyp != null) return ntyp;
      return this;
    }

}	// end of innerclass UnknownClassInterfaceType



/********************************************************************************/
/*										*/
/*	Class types								*/
/*										*/
/********************************************************************************/

private static class CompiledClassType extends CompiledClassInterfaceType {

   boolean is_undefined;

   CompiledClassType(String nm) {
      super(nm);
      is_undefined = false;
    }

   @Override public boolean isUndefined()		{ return is_undefined; }
   @Override void setUndefined(boolean fg)		{ is_undefined = fg; }

}	// end of subclass UnknownType



private static class BinaryClassType extends BinaryClassInterfaceType {

   BinaryClassType(String nm,String sgn) {
      super(nm,sgn);
    }

}	// end of subclass KnownType



/********************************************************************************/
/*										*/
/*	Interface types 							*/
/*										*/
/********************************************************************************/

private static class CompiledInterfaceType extends CompiledClassInterfaceType {

   CompiledInterfaceType(String nm) {
      super(nm);
    }

   @Override public boolean isInterfaceType()	{ return true; }
   @Override public boolean isAbstract()	{ return true; }

}	// end of subclass UnknownInterfaceType



private static class BinaryInterfaceType extends BinaryClassInterfaceType {

   BinaryInterfaceType(String nm,String sgn) {
      super(nm,sgn);
    }

   @Override public boolean isInterfaceType()	{ return true; }
   @Override public boolean isAbstract()	{ return true; }

}	// end of subclass KnownInterfaceType



/********************************************************************************/
/*										*/
/*	Annotation types							*/
/*										*/
/********************************************************************************/

private static class CompiledAnnotationType extends CompiledClassInterfaceType {

   
   CompiledAnnotationType(String nm) {
      super(nm);
    }

   @Override public boolean isAnnotationType()	{ return true; }
   
   @Override public JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,
         JcompType basetype,ASTNode n) {
      JcompSymbol js = super.lookupMethod(typer,id,atyps,basetype,n);
      if (js != null) return js;
      return js;
    }

}	// end of subclass UnknownAnnotationType



private static class BinaryAnnotationType extends BinaryClassInterfaceType {

   BinaryAnnotationType(String nm,String sgn) {
      super(nm,sgn);
    }

   @Override public boolean isAnnotationType()	{ return true; }

}	// end of subclass KnownAnnotationType



/********************************************************************************/
/*										*/
/*	Enumeration type							*/
/*										*/
/********************************************************************************/

private static class EnumType extends CompiledClassInterfaceType {

   private JcompSymbol	values_method;
   private JcompSymbol	valueof_method;

   EnumType(String nm) {
      super(nm);
      values_method = null;
      valueof_method = null;
    }

   @Override public boolean isEnumType()	{ return true; }

   @Override public JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,
         JcompType basetype,ASTNode n) {
      JcompSymbol js = super.lookupMethod(typer,id,atyps,basetype,n);
      if (js != null) return js;
      if (id.equals("values") && atyps.getComponents().size() == 0) {
         if (values_method == null) {
            JcompType typ1 = typer.findArrayType(this);
            JcompType typ2 = JcompType.createMethodType(typ1,new ArrayList<JcompType>(),false,null);
            typ2 = typer.fixJavaType(typ2);
            int acc = Modifier.PUBLIC | Modifier.STATIC;
            values_method = JcompSymbol.createBinaryMethod("values",typ2,this,acc,null,false);
          }
         return values_method;
       }
      else if (id.equals("valueOf")) {
         if (valueof_method == null) {
            List<JcompType> types = new ArrayList<>();
            types.add(typer.findSystemType("java.lang.String"));
            JcompType typ2 = JcompType.createMethodType(this,types,false,null);
            typ2 = typer.fixJavaType(typ2);
            int acc = Modifier.PUBLIC | Modifier.STATIC;
            valueof_method = JcompSymbol.createBinaryMethod("valueOf",typ2,this,acc,null,false);
          }
         return valueof_method;
       }
   
      return null;
    }

}	// end of subclass EnumType




private static class BinaryEnumType extends BinaryClassInterfaceType {

   BinaryEnumType(String nm,String sgn) {
      super(nm,sgn);
    }

   @Override public boolean isEnumType()	{ return true; }

}	// end of inner class KnownEnumType




/********************************************************************************/
/*										*/
/*	Parameterized type							*/
/*										*/
/********************************************************************************/

private static class ParamType extends ClassInterfaceType {

   private JcompType base_type;
   private SortedMap<String,JcompType> param_values;
   private JcompScope local_scope;
   private List<String> var_names;
   private boolean parameters_applied;

   ParamType(JcompTyper typer,JcompType b,List<JcompType> pl,Map<String,JcompType> outers) {
      super(buildParamName(b,pl,outers));
      while (b.isParameterizedType()) b = b.getBaseType();
      base_type = b;
      setSignature(b.getSignature());
      param_values = new TreeMap<>();
      if (outers != null) param_values.putAll(outers);
      
      var_names = JcompGenerics.getTypeVariableNames(b.getSignature(),false);
      
      if (pl != null && !pl.isEmpty()) {
         for (int i = 0; i < var_names.size(); ++i) {
            if (i < pl.size()) {
               String vnm = var_names.get(i);
               JcompType ptyp = pl.get(i);
               if (ptyp.isWildcardType()) {
                  String nm = b.getName() + "." + vnm;
                  JcompType vtyp = typer.findType(nm);
                  JcompType rtyp = null;
                  if (vtyp != null) rtyp = vtyp.getSuperType();
                  if (rtyp == null) rtyp = typer.findType("java.lang.Object");
                  ptyp = rtyp;
                }
               param_values.put(vnm,ptyp);
               String fullnm = b.getName() + "." + vnm;
               param_values.put(fullnm,ptyp);
             }
          }
       }
      
      for (String s : var_names) {
         if (param_values.get(s) == null) {
            JcompType aty = typer.findType(TYPE_ANY_CLASS);
            param_values.put(s,aty);
            // System.err.println("NULL PARAM TYPE " + s);
          }
       }
      local_scope = new JcompScopeFixed();
      parameters_applied = false;
    }

   @Override public boolean isParameterizedType()	{ return true; }
   @Override public JcompType getBaseType()		{ return base_type; }
   @Override boolean isBaseKnown()			{ return base_type.isBaseKnown(); }
   @Override public boolean isAbstract()		{ return base_type.isAbstract(); }
   @Override public boolean isUndefined() {
      if (base_type.isUndefined()) return true;
      for (JcompType jt : param_values.values()) {
         if (jt.isUndefined()) return true;
       }
      return false;
    }
   @Override public boolean needsOuterClass()		{ return base_type.needsOuterClass(); }
   @Override public JcompType getOuterType()		{ return base_type.getOuterType(); }
   @Override public void setOuterType(JcompType t) {
      base_type.setOuterType(t);
   }
   
   @Override public boolean isInterfaceType()		{ return base_type.isInterfaceType(); }
   
   @Override public List<JcompType> getComponents() {  
      List<JcompType> comps = new ArrayList<>();
      for (String s : var_names) {
         comps.add(param_values.get(s));
       }
      return comps; 
    }
   
   protected SortedMap<String,JcompType> getLocalComponents()     { return param_values; }
   @Override public String getSignature(){
      String s = super.getSignature();
      if (s != null) return s;
      return base_type.getSignature();
   }
   @Override public JcompType getSuperType() {
      JcompType jty = super.getSuperType();
      if (jty != null) return jty;
      return base_type.getSuperType();
    }
   @Override public Collection<JcompType> getInterfaces() {
      Collection<JcompType> jty = super.getInterfaces();
      if (jty != null) return jty;
      return base_type.getInterfaces();
    }
   

   @Override public boolean isComplete() {
      if (!base_type.isComplete()) return false;
      for (JcompType jt : param_values.values()) {
         if (jt == this) {
            System.err.println("RECURSIVE PARAMETER TYPES");
          }
         if (!jt.isComplete()) return false;
       }
      return true;
    }

   private static String buildParamName(JcompType b,List<JcompType> pl,Map<String,JcompType> outers) {
      List<String> names = null;
      if (b.getSignature() != null) names = JcompGenerics.getTypeVariableNames(b.getSignature(),false);
      
      StringBuffer buf = new StringBuffer();
      buf.append(b.getName());
      int ct = 0;
      if (names != null && names.size() > 0) {
         for (int i = 0; i < names.size(); ++i) {
            String nm = names.get(i);
            JcompType pt = null;
             if (pl != null && pl.size() > i) {
                pt = pl.get(i);
              }
             else if (outers != null) { 
                pt = outers.get(nm);
              }
             if (pt == null)
                pt = createVariableType(nm);
             if (ct++ > 0) buf.append(",");
             else buf.append("<");
             buf.append(pt.getName());
          }
       }
      if (outers != null) {
         for (Map.Entry<String,JcompType> ent : outers.entrySet()) {
            String nm = ent.getKey();
            if (names != null && names.contains(nm)) continue;
            if (ct++ > 0) buf.append(",");
            else buf.append("<");
            buf.append(ent.getKey());
            buf.append("=");
            buf.append(ent.getValue().getName());
          }
       }
      if (ct > 0) buf.append(">");
      
      String rslt = buf.toString();
      
      return rslt;
    }

   @Override public boolean isCompatibleWith(JcompType jt) {
      if (jt == this) return true;
      if (jt == null) return true;
      if (jt.isParameterizedType()) {
         if (!getBaseType().isCompatibleWith(jt.getBaseType())) return false;
         ParamType pt = (ParamType) jt;
         if (param_values.equals(pt.param_values)) return true;
         for (Map.Entry<String,JcompType> ent : param_values.entrySet()) {
            JcompType pt1 = ent.getValue();
            JcompType pt2 = pt.param_values.get(ent.getKey());
            if (!pt1.isCompatibleWith(pt2)) return false;
          }
         return true;
       }
      return getBaseType().isCompatibleWith(jt);
    }

   @Override public boolean isAssignCompatibleWith(JcompType jt) {
      if (jt == this) return true;
      if (jt.isParameterizedType()) {
	 if (!getBaseType().isCompatibleWith(jt.getBaseType())) return false;
	 return true;
       }
      return getBaseType().isCompatibleWith(jt);
    }

   @Override public boolean isDerivedFrom(JcompType jt) {
      return base_type.isDerivedFrom(jt);
    }

   @Override JcompType getKnownType(JcompTyper typer) {
      return base_type.getKnownType(typer);
    }

   @Override JcompSymbol lookupField(JcompTyper typs,String id,int lvl) {
      JcompSymbol js = local_scope.lookupVariable(id);
      if (js != null) return js;
      js = super.lookupField(typs,id,lvl+1);
      if (js != null) return js;
      return base_type.lookupField(typs,id,this,lvl+1);
    }

   @Override protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,
         JcompType basetype,ASTNode n) {
      JcompSymbol js = local_scope.lookupMethod(id,atyps,basetype,n);
      if (js != null) return js;
      js = super.lookupMethod(typer,id,atyps,basetype,n);
      if (js != null) return js;
      JcompType bt = basetype;
      if (bt == this) bt = base_type;
      js = base_type.lookupMethod(typer,id,atyps,bt,n);
      return js;
    }

   @Override protected Set<JcompSymbol> lookupAbstracts(JcompTyper typer) {
      return base_type.lookupAbstracts(typer);
    }

   @Override protected List<JcompSymbol> lookupStatics(JcompTyper typer,String id,JcompType base) {
      return base_type.lookupStatics(typer,id,base);
    }

   @Override public Map<String,JcompType> getFields(JcompTyper typer) {
      return base_type.getFields(typer);
    }

   @Override public JcompScope getScope() {
      return base_type.getScope();
    }

   @Override public synchronized void defineAll(JcompTyper typer) {
      base_type.defineAll(typer);
      if (!parameters_applied) {
         applyParametersToType(typer);
         applyParameters(typer);
         parameters_applied = true;
       }
    }

   @Override public boolean definesAllNeededMethods(JcompTyper typer) {
      return base_type.definesAllNeededMethods(typer);
    }

   @Override @SuppressWarnings("unchecked")
   public Type createAstNode(AST ast) {
      ParameterizedType pt = ast.newParameterizedType(base_type.createAstNode(ast));
      List<ASTNode> l = pt.typeArguments();
      for (String name : var_names) {
         l.add(param_values.get(name).createAstNode(ast));
       }
      return pt;
    }

   @Override public Expression createDefaultValue(AST ast) {
      return ast.newNullLiteral();
    }

   @Override public String getJavaTypeName() {
      String s = base_type.getJavaTypeName();
      s = s.substring(0,s.length()-1);
      s += "<";
      for (String name : var_names) {
         JcompType jt = param_values.get(name);
         if (jt == null) s += "?";
         else s += jt.getJavaTypeName();
       }
      s += ">;";
      return s;
    }

   @Override public JcompType resetType(JcompTyper typer) {
      JcompType nbase = base_type.resetType(typer);
      boolean chng = nbase != base_type;
      List<JcompType> ptyps = new ArrayList<JcompType>();
      SortedMap<String,JcompType> outers = null;
      if (param_values != null) {
         outers = new TreeMap<>();
         for (Map.Entry<String,JcompType> ent : param_values.entrySet()) {
            JcompType jt = ent.getValue();
            JcompType njt = jt.resetType(typer);
            chng |= (njt != jt);
            outers.put(ent.getKey(),njt);
          }
       }
      if (!chng) return this;
      
      return createParameterizedType(nbase,ptyps,outers,typer);
    }

   @Override public JcompType getFunctionalType() {
      if (!isInterfaceType()) return null;
      Collection<JcompSymbol> syms = local_scope.lookupAbstracts(null);
      if (getInterfaces() != null) {
	 for (JcompType jt : getInterfaces()) {
	    Collection<JcompSymbol> ssyms = getAbstractMethods(jt);
	    if (ssyms.size() > 0) {
	       if (syms.size() == 0 && ssyms.size() == 1) syms = ssyms;
	       else return null;
	     }
	  }
       }
      if (syms.size() == 1) {
	 for (JcompSymbol js : syms) {
	    return js.getType();
	  }
       }
      return base_type.getFunctionalType();
    }

   private void applyParameters(JcompTyper typer) {
      
      String nsgn = JcompGenerics.deriveClassTypeSignature(typer,this,param_values);
      if (nsgn != null) setSignature(nsgn);
   
      if (base_type.getScope() != null && base_type.getScope().getDefinedMethods() != null) {
         for (JcompSymbol js : base_type.getScope().getDefinedMethods()) {
            if (js.getClassType() != base_type) continue;
            if (js.getName().equals("<clinit>")) continue;
            if (js.isStatic()) continue;
            js = js.parameterize(typer,this,param_values);
            local_scope.defineMethod(js);
          }
       }
      if (base_type.getScope() != null && base_type.getScope().getDefinedFields() != null) {
         for (JcompSymbol js : base_type.getScope().getDefinedFields()) {
            if (js.getClassType() != base_type) continue;
            if (js.isStatic()) continue;
            js = js.parameterize(typer,this,param_values);
            local_scope.defineVar(js);
          }
       }
    }
   
   @Override void applyParametersToType(JcompTyper typer) 
   {
      String nsgn = JcompGenerics.deriveClassTypeSignature(typer,this,param_values);
      if (nsgn != null) setSignature(nsgn);
      // NOTE THAT THIS REQUIRES PARAMETER MAP BEING CORRECT
      
      // JcompType nsup = JcompGenerics.deriveSupertype(typer,this,param_values);
      JcompType nsup = JcompGenerics.deriveSupertype(typer,this,null);
      setSuperType(nsup);
      Collection<JcompType> nint = JcompGenerics.deriveInterfaceTypes(typer,this,null);
      if (nint != null) {
         for (JcompType nty : nint) {
            addInterface(nty);
          }
       }
   }

}	// end of subclase ParamType



/********************************************************************************/
/*										*/
/*	Type Variable								*/
/*										*/
/********************************************************************************/

private static class VarType extends ClassInterfaceType {


   VarType(String nm) {
      super(nm);
    }

   @Override public boolean isTypeVariable()		{ return true; }
   @Override public boolean isClassType()		{ return true; }
   @Override boolean isBaseKnown()			{ return true; }
   @Override public boolean isComplete() {
      if (getName().equals("?")) return true;
      return false;
   }
   
   @Override void setSuperType(JcompType jt) {
      super.setSuperType(jt);
      // recomputer type name
    }
   @Override void addInterface(JcompType jt) {
      super.addInterface(jt);
      // recompute type name
    }

   @Override public Type createAstNode(AST ast) {
      if (getName().equals("?")) return ast.newWildcardType();
      return super.createAstNode(ast);
    }

   @Override public String getJavaTypeName() {
      return "T" + getName() + ";";
    }

   @Override public JcompType resetType(JcompTyper typer) {
      return this;
    }

   @Override public boolean isCompatibleWith(JcompType jt) {
      return true;
    }

}	// end of subclass VarType



/********************************************************************************/
/*										*/
/*	Wildcard Type		       					*/
/*										*/
/********************************************************************************/

private static class WildcardType extends ClassInterfaceType {
   
   WildcardType() {
      super("?");
    }
   
   @Override public boolean isWildcardType()		{ return true; }
   @Override public boolean isClassType()		{ return true; }
   @Override boolean isBaseKnown()			{ return true; }
   
   @Override void setSuperType(JcompType jt) {
      super.setSuperType(jt);
    }
   
   @Override void addInterface(JcompType jt) {
      super.addInterface(jt);
    }
   
   @Override public Type createAstNode(AST ast) {
      return ast.newWildcardType();
    }
   
   @Override public String getJavaTypeName() {
      return "?";
    }
   
   @Override public JcompType resetType(JcompTyper typer) {
      return this;
    }
   
   @Override public boolean isCompatibleWith(JcompType jt) {
      return true;
    }
   
}	// end of subclass WildcardType







/********************************************************************************/
/*										*/
/*	Method types								*/
/*										*/
/********************************************************************************/

private static class MethodType extends JcompType {

   private JcompType return_type;
   private List<JcompType> param_types;
   private boolean is_varargs;

   MethodType(JcompType rt,Collection<JcompType> atyps,boolean varargs,String sgn) {
      super(buildMethodTypeName(rt,atyps,varargs));
      return_type = rt;
      param_types = new ArrayList<JcompType>();
      if (atyps != null) {
         for (JcompType jt : atyps) {
            if (jt == null) {
               System.err.println("Attempt to create null parameter type");
               jt = JcompType.createAnyClassType();
             }
            param_types.add(jt);
          }
       }
      is_varargs = varargs;
      setSignature(sgn);
    }

   @Override public boolean isMethodType()	       { return true; }
   @Override public JcompType getBaseType()	       { return return_type; }

   @Override public List<JcompType> getComponents()    { return param_types; }
   @Override public boolean isVarArgs() 	       { return is_varargs; }
   @Override public boolean isComplete() {
      if (return_type != null && !return_type.isComplete()) return false;
      for (JcompType jt : param_types)
         if (!jt.isComplete()) return false;
      return true;
    }

   private static String buildMethodTypeName(JcompType r,Collection<JcompType> pl,boolean varargs) {
      StringBuffer buf = new StringBuffer();
      buf.append("(");
      int ct = 0;
      if (pl != null) {
	 for (JcompType p : pl) {
	    if (ct++ > 0) buf.append(",");
	    if (p == null) buf.append("*ANY*");
	    else buf.append(p.getName());
	  }
       }
      // if (varargs) buf.append("...");
      buf.append(")");
      if (r != null) buf.append(r.getName());

      return buf.toString();
    }

   @Override public boolean isCompatibleWith(JcompType jt) {
      if (jt == this) return true;
   
      if (jt == null) return false;
   
      boolean isok = false;
      if (jt.isMethodType()) {
         MethodType mt = (MethodType) jt;
         if (mt.param_types.size() == param_types.size()) {
            isok = true;
            for (int i = 0; i < param_types.size(); ++i) {
               if (param_types.get(i) != null) {
                  JcompType t0 = param_types.get(i);
                  JcompType t1 = mt.param_types.get(i);
                  boolean fg = t0.isCompatibleWith(t1);
                  if (!fg && t0.isParameterizedType() && t1.isParameterizedType()) {
                     t0 = t0.getBaseType();
                     t1 = t1.getBaseType();
                     fg = t0.isCompatibleWith(t1);
                   }
                  isok &= fg;
                }
             }
          }
         if (!isok && mt.is_varargs && param_types.size() >= mt.param_types.size() -1 &&
               mt.param_types.size() > 0) {
            isok = true;
            for (int i = 0; i < mt.param_types.size()-1; ++i) {
               isok &= param_types.get(i).isCompatibleWith(mt.param_types.get(i));
             }
            JcompType rt = mt.param_types.get(mt.param_types.size()-1);
            // shouldn't need to check for array type here
            if (rt.isArrayType()) rt = rt.getBaseType();
            for (int i = mt.param_types.size()-1; i < param_types.size(); ++i) {
               isok &= param_types.get(i).isCompatibleWith(rt);
             }
          }
       }
      else {
         JcompType mt = jt.getFunctionalType();
         if (mt != null) return isCompatibleWith(mt);
       }
      return isok;
    }

   @Override public Type createAstNode(AST ast) 	{ return null; }

   @Override public String getJavaTypeName() {
      String s = "(";
      for (JcompType jt : param_types) {
	 s += jt.getJavaTypeName();
       }
      s += ")";
      if (return_type != null) s += return_type.getJavaTypeName();
      return s;
    }

   @Override public JcompType resetType(JcompTyper typer) {
      JcompType nret = return_type.resetType(typer);
      boolean chng = nret != return_type;
      List<JcompType> nargs = new ArrayList<JcompType>();
      for (JcompType jt : param_types) {
         JcompType njt = jt.resetType(typer);
         chng |= jt != njt;
         nargs.add(njt);
       }
      if (!chng) return this;
      return new MethodType(nret,nargs,is_varargs,getSignature());
    }

}	// end of subclase MethodType




/********************************************************************************/
/*										*/
/*	Special types								*/
/*										*/
/********************************************************************************/

private static class AnyClassType extends JcompType {

   protected AnyClassType() {
      super(TYPE_ANY_CLASS);
    }

   @Override public boolean isClassType()		{ return true; }
   @Override public boolean isInterfaceType()		{ return true; }
   @Override public boolean isAnyType() 		{ return true; }
   @Override boolean isBaseKnown()			{ return true; }
   @Override public JcompType getFunctionalType()	{ return this; }

   @Override public boolean isCompatibleWith(JcompType jt) {
      if (this == jt) return true;
      if (jt.isClassType() || jt.isInterfaceType() || jt.isEnumType() || jt.isArrayType() || jt.isParameterizedType())
         return true;
      return false;
    }

   @Override public Type createAstNode(AST ast) {
      return ast.newSimpleType(JcompAst.getQualifiedName(ast,"java.lang.Object"));
    }

   @Override public String getJavaTypeName()		{ return "Ljava/lang/Object;"; }

}	// end of subclass AnyClassType




private static class ErrorType extends JcompType {

   protected ErrorType() {
      super(TYPE_ERROR);
    }

   @Override public boolean isErrorType()		{ return true; }
   @Override boolean isBaseKnown()			{ return true; }

   @Override public boolean isCompatibleWith(JcompType jt) {
      return true;
    }

   @Override public Type createAstNode(AST ast) {
      return ast.newPrimitiveType(PrimitiveType.VOID);
    }

   @Override public String getJavaTypeName()		{ return "QError"; }

   @Override public JcompType resetType(JcompTyper t) {
      return this;
    }

}	// end of subclass ErrorType



/********************************************************************************/
/*										*/
/*	Union TYpes								*/
/*										*/
/********************************************************************************/

private static class UnionType extends JcompType {

   private List<JcompType> base_types;

   protected UnionType(String nm,List<JcompType> typs) {
      super(nm);
      base_types = new ArrayList<JcompType>(typs);
    }

   @Override public boolean isUnionType()		 { return true; }
   @Override public List<JcompType> getComponents()	 { return base_types; }
   @Override public boolean isComplete() {
      for (JcompType jt : base_types)
	 if (!jt.isComplete()) return false;
      return true;
    }

   @Override public boolean isCompatibleWith(JcompType jt) {
      for (JcompType bt : base_types) {
	 if (bt.isCompatibleWith(jt)) return true;
       }
      return false;
    }

   @Override public String getJavaTypeName() {
      return null;
    }

   @Override protected JcompSymbol lookupMethod(JcompTyper typer,
	 String id,JcompType atyps,JcompType basetype,ASTNode n) {
      JcompType jt = findCommonParent(typer,base_types);
      return jt.lookupMethod(typer,id,atyps,basetype,n);
    }

   @Override public JcompType resetType(JcompTyper typer) {
      boolean chng = false;
      List<JcompType> nbase = new ArrayList<JcompType>();
      for (JcompType jt : base_types) {
	 JcompType njt = jt.resetType(typer);
	 chng |= jt != njt;
	 nbase.add(njt);
       }
      if (!chng) return this;
      return new UnionType(getName(),nbase);
    }

}	// end of inner class UnionType




/********************************************************************************/
/*										*/
/*	Intersection Types							*/
/*										*/
/********************************************************************************/

private static class IntersectionType extends JcompType {

   private List<JcompType> base_types;

   protected IntersectionType(String nm,List<JcompType> typs) {
      super(nm);
      base_types = new ArrayList<JcompType>(typs);
    }

   @Override public boolean isIntersectionType()	 { return true; }
   @Override public List<JcompType> getComponents()	 { return base_types; }

   @Override public boolean isComplete() {
      for (JcompType jt : base_types)
	 if (!jt.isComplete()) return false;
      return true;
    }

   @Override public boolean isCompatibleWith(JcompType jt) {
      for (JcompType bt : base_types) {
	 if (!bt.isCompatibleWith(jt)) return false;
       }
      return true;
    }

   @Override public String getJavaTypeName() {
      return null;
    }

   @Override protected JcompSymbol lookupMethod(JcompTyper typer,
	 String id,JcompType atyps,JcompType basetype,ASTNode n) {
      JcompType jt = findCommonParent(typer,base_types);
      return jt.lookupMethod(typer,id,atyps,basetype,n);
    }

   @Override public JcompType resetType(JcompTyper typer) {
      boolean chng = false;
      List<JcompType> nbase = new ArrayList<JcompType>();
      for (JcompType jt : base_types) {
	 JcompType njt = jt.resetType(typer);
	 chng |= jt != njt;
	 nbase.add(njt);
       }
      if (!chng) return this;
      return new IntersectionType(getName(),nbase);
    }

}	// end of inner class UnionType



private static class FunctionRefType extends JcompType {

   private MethodType method_type;
   private MethodType nonstatic_type;
   private JcompSymbol method_symbol;
   private JcompSymbol ref_symbol;

   FunctionRefType(JcompType ref,JcompType nsref,JcompSymbol sym,JcompSymbol rsym) {
      super("REF$" + ref.getName());
      method_type = (MethodType) ref;
      nonstatic_type = (MethodType) nsref;
      method_symbol = sym;
      ref_symbol = rsym;
    }

   @Override public String getJavaTypeName()		{ return null; }
   @Override public MethodType getFunctionalType() {
      return method_type;
    }
   @Override public boolean isFunctionRef()		{ return true; }

   @Override public JcompType getBaseType() {
      if (ref_symbol == null) return method_type;
      else return ref_symbol.getType();
   }

   @Override public boolean isCompatibleWith(JcompType jt) {
      JcompType mt = jt.getFunctionalType();
      if (mt == null) return false;
     // if (mt.isCompatibleWith(method_type)) return true;
      if (method_type.isCompatibleWith(mt)) return true;
      if (nonstatic_type != null && mt.isCompatibleWith(nonstatic_type)) return true;
      return false;
    }

   @Override public JcompSymbol lookupMethod(JcompTyper typer,String name,JcompType typ,
	 JcompType base,ASTNode n) {
      if (method_symbol != null && method_type.isCompatibleWith(typ)) {
	 return method_symbol;
       }
      if (method_symbol != null && nonstatic_type != null && nonstatic_type.isCompatibleWith(typ)) {
	 return method_symbol;
       }
      return super.lookupMethod(typer,name,typ,base,n);
    }

}	// end of inner class FunctionRefType













}	// end of class JcompType



/* end of JcompType.java */
