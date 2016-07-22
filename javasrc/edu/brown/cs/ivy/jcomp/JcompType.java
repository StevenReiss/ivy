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


import org.eclipse.jdt.core.dom.*;

import java.lang.reflect.Modifier;
import java.util.*;


/**
 *	This class represents a type from semantic resolution.	Each type
 *	has a unique object, and two instances of the type will have the
 *	same object.
 **/

abstract public class JcompType implements JcompConstants {



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



static JcompType createKnownType(String nm)
{
   return new KnownType(nm);
}


static JcompType createUnknownType(String nm)
{
   return new UnknownType(nm);
}



static JcompType createKnownInterfaceType(String nm)
{
   return new KnownInterfaceType(nm);
}


static JcompType createUnknownInterfaceType(String nm)
{
   return new UnknownInterfaceType(nm);
}



static JcompType createEnumType(String nm)
{
   return new EnumType(nm);
}



static JcompType createParameterizedType(JcompType jt,List<JcompType> ptl)
{
   return new ParamType(jt,ptl);
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


static public JcompType createArrayType(JcompType jt)
{
   return new ArrayType(jt);
}



public static JcompType createMethodType(JcompType jt,List<JcompType> aty,boolean varargs)
{
   return new MethodType(jt,aty,varargs);
}



static JcompType createAnyClassType()
{
   return new AnyClassType();
}



static JcompType createErrorType()
{
   return new ErrorType();
}



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


/**
 *	Return true if this is the type void
 **/

public boolean isVoidType()			{ return false; }


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
 *	Return true if this is an enumeration type
 **/

public boolean isEnumType()			{ return false; }


/**
 *	Return true if this is a type variable (part of a parameterized type).
 **/

public boolean isTypeVariable() 	{ return false; }


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

public boolean isKnownType()			{ return false; }


/**
 *	Return true if this type can be almost anything (e.g. the type of null).
 **/

public boolean isAnyType()			{ return false; }


/**
 *	Return true if this is a union type.
 **/

public boolean isUnionType()			{ return false; }


/**
 *	Return true if this type is not known.
 **/

public boolean isUnknown()			{ return false; }


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


boolean isBaseKnown()				{ return false; }


/**
 *	Return true if this type comes from the user's context
 **/

public boolean isContextType()		{ return false; }

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



JcompType getKnownType(JcompTyper typr) {
   return typr.findType("java.lang.Object");
}
public void defineAll(JcompTyper typer) 	{ }

void addInterface(JcompType jt) 		{ }
void setSuperType(JcompType jt) 		{ }
void setOuterType(JcompType jt) 		{ }

void setScope(JcompScope js)			{ assoc_scope = js; }
public JcompScope getScope()				{ return assoc_scope; }


/**
 *	Return the type associated with this one.  This provides a mapping from
 *	primitive types to their object counterparts to handle automatic boxing.
 **/

public JcompType getAssociatedType()		{ return assoc_type; }

void setAssociatedType(JcompType jt)		{ assoc_type = jt; }


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


JcompSymbol lookupField(JcompTyper typs,String id)
{
   return lookupField(typs,id,0);
}


JcompSymbol lookupField(JcompTyper typs,String id,int lvl)
{
   if (assoc_scope != null) return assoc_scope.lookupVariable(id);

   return null;
}


final public JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps)
{
   return lookupMethod(typer,id,atyps,this);
}



protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,JcompType basetype)
{
   if (assoc_scope != null) return assoc_scope.lookupMethod(id,atyps);

   return null;
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


public List<JcompSymbol> isConformableFrom(JcompTyper typer,JcompType typ)
{
   if (isCompatibleWith(typ)) return null;

   return typ.isConformableTo(typer,this);
}



List<JcompSymbol> isConformableTo(JcompTyper typer,JcompType typ)
{
   return null;
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




/********************************************************************************/
/*										*/
/*	Known item methods							*/
/*										*/
/********************************************************************************/

protected JcompSymbol lookupKnownField(JcompTyper typs,String id)
{
   JcompSymbol js = null;

   if (assoc_scope != null) js = assoc_scope.lookupVariable(id);

   if (js == null) {
      js = typs.lookupKnownField(getName(),id);
      if (js != null) {
	 assoc_scope.defineVar(js);
       }
    }

   return js;
}



protected JcompSymbol lookupKnownMethod(JcompTyper typs,String id,JcompType mtyp,JcompType basetype)
{
   JcompSymbol js = null;
   if (id != null && id.equals("indicator"))
      System.err.println("CHECK INDICATOR");

   if (assoc_scope != null) {
      js = assoc_scope.lookupMethod(id,mtyp);
      if (js != null) {
	 JcompType jtyp = js.getType();
	 List<JcompType> jargs = jtyp.getComponents();
	 List<JcompType> margs = mtyp.getComponents();
	 if (jargs.size() != margs.size()) js = null;
	 else {
	    for (int i = 0; i < jargs.size(); ++i) {
	       if (!margs.get(i).equals(jargs.get(i))) js = null;
	     }
	  }
       }
      if (js != null) return js;
    }

   if (js == null) {
      js = typs.lookupKnownMethod(getName(),id,mtyp,basetype);
      if (js != null && !js.isGenericReturn()) {
	 boolean known = true;
	 for (JcompType xjt : js.getType().getComponents()) {
	    if (!xjt.isBaseKnown())
	       known = false;
	  }
	 if (!js.getType().getBaseType().isBaseKnown())
	    known = false;
	 if (known) {
	    if (basetype == this || basetype == null || basetype.getScope() == null) {
	       // this is an unsafe optimization
	       assoc_scope.defineMethod(js);
	     }
	    else {
	       basetype.getScope().defineMethod(js);
	     }
	 }
       }
    }

   return js;
}



protected List<JcompSymbol> lookupKnownStatics(JcompTyper typs,String id,JcompType basetype)
{
   List<JcompSymbol> rslt = null;

   if (assoc_scope != null) rslt = assoc_scope.lookupStatics(id);

   List<JcompSymbol> r1 = typs.lookupKnownStatics(getName(),id,basetype);

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
   @Override public boolean isVoidType()		{ return type_code == PrimitiveType.VOID; }
   @Override boolean isBaseKnown()			{ return true; }
   @Override JcompType getKnownType(JcompTyper t)	{ return this; }
   @Override public JcompType getBaseType()		{ return object_type; }

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

   @Override protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,JcompType basetype) {
      JcompType jt = typer.findType("java.lang.Object");
      return jt.lookupMethod(typer,id,atyps,basetype);
    }

   @Override public Type createAstNode(AST ast) {
      return ast.newArrayType(base_type.createAstNode(ast));
    }

   @Override public Expression createDefaultValue(AST ast) {
      Expression e1 = ast.newNullLiteral();
      if (base_type.isKnownType() || base_type.isPrimitiveType()) {
	 Type asttyp = createAstNode(ast);
	 CastExpression e2 = ast.newCastExpression();
	 e2.setExpression(e1);
	 e2.setType(asttyp);
	 e1 = e2;
       }
      return e1;
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

   protected ClassInterfaceType(String nm) {
      super(nm);
      super_type = null;
      outer_type = null;
      interface_types = null;
      is_context = false;
    }

   @Override void setSuperType(JcompType t) {
      if (t != this) super_type = t;
   }

   @Override void setOuterType(JcompType t)		{ outer_type = t; }

   @Override void addInterface(JcompType t) {
      if (t == null) return;
      if (interface_types == null) interface_types = new ArrayList<JcompType>();
      else if (interface_types.contains(t)) return;
      else if (t == this) return;
      interface_types.add(t);
    }

   @Override public Collection<JcompType> getInterfaces()	{ return interface_types; }

   @Override public boolean isClassType()			{ return true; }

   @Override boolean isBaseKnown()				{ return isKnownType(); }

   @Override public JcompType getSuperType()			{ return super_type; }

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
      if (jt.isInterfaceType() && interface_types != null) {
         for (JcompType ity : interface_types) {
            if (ity.isCompatibleWith(jt)) return true;
          }
       }
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
      JcompType atyp = createMethodType(null,prms,false);
      JcompSymbol sym = lookupMethod(typer,"<init>",atyp);
   
      if (sym != null && !sym.isPrivate()) {
         if (rslt == null) rslt = new ArrayList<JcompSymbol>();
         rslt.add(sym);
       }
   
      return rslt;
    }


   @Override List<JcompSymbol> isConformableTo(JcompTyper typer,JcompType typ) {
      List<JcompType> prms = new ArrayList<JcompType>();
      JcompType atyp = createMethodType(typ,prms,false);
   
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

   @Override protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,JcompType basetype) {
      JcompSymbol js = super.lookupMethod(typer,id,atyps,basetype);
      if (js != null) return js;
      if (super_type != null && super_type != this) {
         if (id == null || !id.equals("<init>"))
            js = super_type.lookupMethod(typer,id,atyps,basetype);
       }
      else if (!getName().equals("java.lang.Object")) {
         JcompType ot = typer.findType("java.lang.Object");
         js = ot.lookupMethod(typer,id,atyps,basetype);
       }
      if (js != null) return js;
      if (interface_types != null) {
         if (!isAbstract()) {
            for (JcompType it : interface_types) {
               js = it.lookupMethod(typer,id,atyps,basetype);
               if (js != null) return js;
             }
          }
       }
      if (outer_type != null && (id == null || !id.equals("<init>"))) {
         js = outer_type.lookupMethod(typer,id,atyps,basetype);
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
      if (isKnownType()) {
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
	    if (aty.isKnownType() && !js.getClassType().isKnownType()) score += 3;
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
      s1 = s1.substring(s.length()).replace(".","$");
      return "L" + s + s1 + ";";
    }

}	// end of innerclass ClassInterfaceType




private static abstract class KnownClassInterfaceType extends ClassInterfaceType {

   KnownClassInterfaceType(String nm) {
      super(nm);
      setScope(new JcompScopeFixed());
    }

   @Override JcompSymbol lookupField(JcompTyper typs,String id,int lvl) {
      return lookupKnownField(typs,id);
    }

   @Override protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,JcompType basetype) {
      return lookupKnownMethod(typer,id,atyps,basetype);
    }

   @Override protected List<JcompSymbol> lookupStatics(JcompTyper typer,String id,JcompType basetype) {
      return lookupKnownStatics(typer,id,basetype);
    }

   @Override protected Set<JcompSymbol> lookupAbstracts(JcompTyper typer) {
      return lookupKnownAbstracts(typer);
    }
   @Override JcompType getKnownType(JcompTyper typer)	{ return this; }
   @Override public boolean isKnownType()		{ return true; }

   @Override public void defineAll(JcompTyper typer) {
      typer.defineAll(getName(),getScope());
    }

}	// end of innerclass KnownClassInterfaceType




private static abstract class UnknownClassInterfaceType extends ClassInterfaceType {

   private Set<String> field_names;
   private Map<String,Set<JcompType>> method_names;

   UnknownClassInterfaceType(String nm) {
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
      if (js == null) {
	 field_names.add(id);
       }
      return js;
    }

    @Override protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,JcompType basetype) {
      JcompSymbol js = super.lookupMethod(typer,id,atyps,basetype);
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


   @Override public boolean isUnknown() 		{ return true; }

}	// end of innerclass UnknownClassInterfaceType



/********************************************************************************/
/*										*/
/*	Class types								*/
/*										*/
/********************************************************************************/

private static class UnknownType extends UnknownClassInterfaceType {

   boolean is_undefined;

   UnknownType(String nm) {
      super(nm);
    }

   @Override public boolean isUndefined()		{ return is_undefined; }
   @Override void setUndefined(boolean fg)		{ is_undefined = fg; }

}	// end of subclass UnknownType



private static class KnownType extends KnownClassInterfaceType {

   KnownType(String nm) {
      super(nm);
    }

}	// end of subclass KnownType



/********************************************************************************/
/*										*/
/*	Interface types 							*/
/*										*/
/********************************************************************************/

private static class UnknownInterfaceType extends UnknownClassInterfaceType {

   UnknownInterfaceType(String nm) {
      super(nm);
    }

   @Override public boolean isInterfaceType()	{ return true; }
   @Override public boolean isAbstract()	{ return true; }

}	// end of subclass UnknownInterfaceType



private static class KnownInterfaceType extends KnownClassInterfaceType {

   KnownInterfaceType(String nm) {
      super(nm);
    }

   @Override public boolean isInterfaceType()	{ return true; }
   @Override public boolean isAbstract()	{ return true; }

}	// end of subclass KnownInterfaceType



/********************************************************************************/
/*										*/
/*	Enumeration type							*/
/*										*/
/********************************************************************************/

private static class EnumType extends UnknownClassInterfaceType {

   private JcompSymbol	values_method;

   EnumType(String nm) {
      super(nm);
      values_method = null;
    }

   @Override public boolean isEnumType()	{ return true; }

   @Override public JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,JcompType basetype) {
      JcompSymbol js = super.lookupMethod(typer,id,atyps,basetype);
      if (js != null) return js;
      if (id.equals("values") && atyps.getComponents().size() == 0) {
         if (values_method == null) {
            JcompType typ1 = JcompType.createArrayType(this);
            JcompType typ2 = JcompType.createMethodType(typ1,new ArrayList<JcompType>(),false);
            int acc = Modifier.PUBLIC | Modifier.STATIC;
            values_method = JcompSymbol.createKnownMethod("values",typ2,this,acc,null,false);
          }
         return values_method;
       }
      return null;
    }

}	// end of subclass EnumType



/********************************************************************************/
/*										*/
/*	Parameterized type							*/
/*										*/
/********************************************************************************/

private static class ParamType extends JcompType {

   private JcompType base_type;
   private List<JcompType> type_params;

   ParamType(JcompType b,Collection<JcompType> pl) {
      super(buildParamName(b,pl));
      base_type = b;
      type_params = new ArrayList<JcompType>(pl);
      JcompScope js = new JcompScopeFixed();
      setScope(js);
    }

   @Override public boolean isParameterizedType()	{ return true; }
   @Override public JcompType getBaseType()		{ return base_type; }
   @Override boolean isBaseKnown()			{ return base_type.isBaseKnown(); }
   @Override public boolean isAbstract()		{ return base_type.isAbstract(); }
   @Override public boolean isUndefined() {
      if (base_type.isUndefined()) return true;
      for (JcompType jt : type_params) {
	 if (jt.isUndefined()) return true;
       }
      return false;
    }

   @Override public List<JcompType> getComponents()	{ return type_params; }

   private static String buildParamName(JcompType b,Collection<JcompType> pl) {
      StringBuffer buf = new StringBuffer();
      buf.append(b.getName());
      buf.append("<");
      int ct = 0;
      for (JcompType p : pl) {
	 if (ct++ > 0) buf.append(",");
	 buf.append(p.getName());
       }
      buf.append(">");
      return buf.toString();
    }

   @Override public boolean isCompatibleWith(JcompType jt) {
      if (jt == this) return true;
      if (jt.isParameterizedType()) {
         if (!getBaseType().isCompatibleWith(jt.getBaseType())) return false;
         ParamType pt = (ParamType) jt;
         if (type_params.equals(pt.type_params)) return true;
         if (type_params.isEmpty()) return true;
         return false;
       }
      return getBaseType().isCompatibleWith(jt);
    }

   @Override JcompType getKnownType(JcompTyper typer) {
      return base_type.getKnownType(typer);
    }

   @Override JcompSymbol lookupField(JcompTyper typs,String id,int lvl) {
      JcompSymbol js = super.lookupField(typs,id,lvl+1);
      if (js != null) return js;
      return base_type.lookupField(typs,id,lvl+1);
    }

   @Override protected JcompSymbol lookupMethod(JcompTyper typer,String id,JcompType atyps,JcompType basetype) {
      JcompSymbol js = super.lookupMethod(typer,id,atyps,basetype);
      if (js != null) return js;
      return base_type.lookupMethod(typer,id,atyps,basetype);
    }

   @Override protected Set<JcompSymbol> lookupAbstracts(JcompTyper typer) {
      return base_type.lookupAbstracts(typer);
    }

   @Override protected List<JcompSymbol> lookupStatics(JcompTyper typer,String id,JcompType base) {
      return base_type.lookupStatics(typer,id,base);
    }

   @Override @SuppressWarnings("unchecked")
   public Type createAstNode(AST ast) {
      ParameterizedType pt = ast.newParameterizedType(base_type.createAstNode(ast));
      List<ASTNode> l = pt.typeArguments();
      for (JcompType jt : type_params) {
	 l.add(jt.createAstNode(ast));
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
      for (JcompType jt : type_params) {
	 s += jt.getJavaTypeName();
       }
      s += ">;";
      return s;
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

   @Override public Type createAstNode(AST ast) {
      if (getName().equals("?")) return ast.newWildcardType();
      return super.createAstNode(ast);
    }

   @Override public String getJavaTypeName() {
      return "T" + getName() + ";";
    }

}	// end of subclass VarType



/********************************************************************************/
/*										*/
/*	Method types								*/
/*										*/
/********************************************************************************/

private static class MethodType extends JcompType {

   private JcompType return_type;
   private List<JcompType> param_types;
   private boolean is_varargs;

   MethodType(JcompType rt,Collection<JcompType> atyps,boolean varargs) {
      super(buildMethodTypeName(rt,atyps,varargs));
      return_type = rt;
      param_types = new ArrayList<JcompType>();
      if (atyps != null) param_types.addAll(atyps);
      is_varargs = varargs;
    }

   @Override public boolean isMethodType()	       { return true; }
   @Override public JcompType getBaseType()	       { return return_type; }

   @Override public List<JcompType> getComponents()    { return param_types; }

   private static String buildMethodTypeName(JcompType r,Collection<JcompType> pl,boolean varargs) {
      StringBuffer buf = new StringBuffer();
      buf.append("(");
      int ct = 0;
      if (pl != null) {
	 for (JcompType p : pl) {
	    if (ct++ > 0) buf.append(",");
	    buf.append(p.getName());
	  }
       }
      if (varargs) buf.append("...");
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
               isok &= param_types.get(i).isCompatibleWith(mt.param_types.get(i));
             }
          }
         if (!isok && mt.is_varargs && param_types.size() >= mt.param_types.size() -1 &&
        	mt.param_types.size() > 0) {
            isok = true;
            for (int i = 0; i < mt.param_types.size()-1; ++i) {
               isok &= param_types.get(i).isCompatibleWith(mt.param_types.get(i));
             }
            JcompType rt = mt.param_types.get(mt.param_types.size()-1);
            if (rt.isArrayType()) rt = rt.getBaseType();
            for (int i = mt.param_types.size()-1; i < param_types.size(); ++i) {
               isok &= param_types.get(i).isCompatibleWith(rt);
             }
          }
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

   @Override public boolean isCompatibleWith(JcompType jt) {
      if (this == jt) return true;
      if (jt.isClassType() || jt.isInterfaceType() || jt.isEnumType() || jt.isArrayType() || jt.isParameterizedType())
         return true;
      return false;
    }

   @Override public Type createAstNode(AST ast) 	{ return null; }

   @Override public String getJavaTypeName()			{ return "Ljava/lang/Object;"; }


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

   @Override public Type createAstNode(AST ast) 	{ return null; }

   @Override public String getJavaTypeName()			{ return "QError"; }

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
         String id,JcompType atyps,JcompType basetype) {
      JcompType jt = findCommonParent(typer,base_types);
      return jt.lookupMethod(typer,id,atyps,basetype);
    }

}	// end of inner class UnionType




}	// end of class JcompType



/* end of JcompType.java */
