/********************************************************************************/
/*                                                                              */
/*              JannotTypeUtils.java                                            */
/*                                                                              */
/*      description of class                                                    */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.ivy.jannot;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompScope;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;

class JannotTypeUtils implements JannotConstants, Types
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private JannotProcessingEnvironment proc_env;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTypeUtils(JannotProcessingEnvironment pe)
{
   proc_env = pe; 
   
}


/********************************************************************************/
/*                                                                              */
/*     Element methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public Element asElement(TypeMirror t)
{
   JcompType jt = getJcompType(t);
   JcompSymbol js = jt.getDefinition();
   if (js != null) {
      ASTNode an = js.getDefinitionNode();
      return JannotElement.createElement(an);
    }
   return null;
}



@Override public TypeElement boxedClass(PrimitiveType p)
{
   JcompType jt = getJcompType(p);
   return JannotElement.createElement(jt);
}


@Override public PrimitiveType unboxedType(TypeMirror t)
{
   JcompType jt = getJcompType(t);
   JcompType bjt = jt.getAssociatedType();
   if (bjt != null && bjt.isPrimitiveType()) 
      return (PrimitiveType) JannotTypeMirror.createTypeMirror(bjt);
   return null;
}


/********************************************************************************/
/*                                                                              */
/*      Type management methods                                                 */
/*                                                                              */
/********************************************************************************/

@Override public TypeMirror asMemberOf(DeclaredType containing,Element elt)
{
   JcompType jt = getJcompType(containing);
   JannotElement jelt = (JannotElement) elt;
   ASTNode eltnode = jelt.getAstNode();
   JcompSymbol def = JcompAst.getDefinition(eltnode);
   if (def == null) def = JcompAst.getReference(eltnode);
   if (def == null) return null;
   
   // Assume that containing is a partially or fully resolved parameterized type
   // find the member of jt that corresponds to element and return its type
   switch (elt.getKind()) {
      case FIELD :
         JcompScope scp = jt.getScope();
         for (JcompSymbol js : scp.getDefinedFields()) {
            if (js.getDefinitionNode() ==  def.getDefinitionNode()) {
               def = js;
               break;
             }
          }
         break;
      case METHOD :
      case CONSTRUCTOR :
         scp = jt.getScope();
         for (JcompSymbol js : scp.getDefinedMethods()) {
            if (js.getDefinitionNode() ==  def.getDefinitionNode()) {       
               def = js;
               break;
             }
          }
         break;
      case ANNOTATION_TYPE :
      case ENUM :
      case CLASS :
      case INTERFACE :
         // need to find inner resolved type
         break;
      default :
         break;
    }
   
   return JannotTypeMirror.createTypeMirror(def.getType());
}


@Override public TypeMirror capture(TypeMirror t)
{ 
   // return the generic type for t?
   return null;
}


@Override public TypeMirror erasure(TypeMirror t)
{
   // assume t is a parameterized type with type variable --
   // return its parameterized type with t changed to its supertype
   return null;
}

@Override public boolean contains(TypeMirror t1,TypeMirror t2)
{
   JcompType jt1 = getJcompType(t1);
   JcompType jt2 = getJcompType(t2);
   
   for (JcompType jout = jt2; jout != null; jout = jout.getOuterType()) {
      if (jout == jt1) return true;
    }
   
   return false;
}


@Override public List<? extends TypeMirror> directSupertypes(TypeMirror t)
{
   List<JannotTypeMirror> rslt = new ArrayList<>();
   JcompType jt = getJcompType(t);
   if (jt.getSuperType() != null) {
      rslt.add(JannotTypeMirror.createTypeMirror(jt.getSuperType()));
    }
   else if (jt.isInterfaceType()) {
      JcompTyper typer = getJcompTyper();
      JcompType otyp = typer.OBJECT_TYPE;
      rslt.add(JannotTypeMirror.createTypeMirror(otyp));
    }
   for (JcompType ityp : jt.getInterfaces()) {
      rslt.add(JannotTypeMirror.createTypeMirror(ityp));
    }
  
   return rslt;
}


@Override public ArrayType getArrayType(TypeMirror comptype)
{
   JcompType jt = getJcompType(comptype);
   JcompType atyp = getJcompTyper().findArrayType(jt);
   return (ArrayType) JannotTypeMirror.createTypeMirror(atyp);
}

@Override public DeclaredType getDeclaredType(TypeElement te,TypeMirror... args)
{
   // need to find generic type with type of te and args
   // and return it
   return null;
}


@Override public DeclaredType getDeclaredType(DeclaredType cont,TypeElement te,
      TypeMirror... args)
{
   // need to find type generic type with cont and args
   // then find internal component related to te
   return null;
}


@Override public NoType getNoType(TypeKind kind)
{
   JcompType none = null;
   
   return (NoType) JannotTypeMirror.createTypeMirror(none);
}

@Override public NullType getNullType()
{
   JcompTyper typer = getJcompTyper();
   JcompType nulltype = typer.ANY_TYPE;
   return (NullType) JannotTypeMirror.createTypeMirror(nulltype);
}


@Override public PrimitiveType getPrimitiveType(TypeKind kind)
{
   JcompType jt = null;
   JcompTyper typer = getJcompTyper();
   switch (kind) {
      case BOOLEAN :
         jt = typer.BOOLEAN_TYPE;
         break;
      case BYTE :
         jt = typer.BYTE_TYPE;
         break;
      case CHAR :
         jt = typer.CHAR_TYPE;
         break;
      case DOUBLE :
         jt = typer.DOUBLE_TYPE;
         break;
      case FLOAT :
         jt = typer.FLOAT_TYPE;
         break;
      case INT :
         jt = typer.INT_TYPE;
         break;
      case LONG :
         jt = typer.LONG_TYPE;
         break;
      case SHORT :
         jt = typer.SHORT_TYPE;
         break;
      default :
      case VOID :
         jt = typer.VOID_TYPE;
         break;
    }
   return (PrimitiveType) JannotTypeMirror.createTypeMirror(jt);
}


@Override public WildcardType getWildcardType(TypeMirror ext,TypeMirror sup)
{
   JcompTyper typer = getJcompTyper();
   JcompType wtyp = typer.findSystemType("?");
   return (WildcardType) JannotTypeMirror.createTypeMirror(wtyp);
}



@Override public boolean isAssignable(TypeMirror t1,TypeMirror t2)
{
   JcompType jt1 = getJcompType(t1);
   JcompType jt2 = getJcompType(t2);
   if (jt1.isAssignCompatibleWith(jt2)) return true;
   return false;
}



@Override public boolean isSameType(TypeMirror t1,TypeMirror t2)
{
   JcompType jt1 = getJcompType(t1);
   JcompType jt2 = getJcompType(t2);
   if (jt1.isWildcardType()) return false;
   return jt1.equals(jt2);
}



@Override public boolean isSubsignature(ExecutableType t1,ExecutableType t2)
{
   JcompType jt1 = getJcompType(t1);
   JcompType jt2 = getJcompType(t2);
   if (jt1.isCompatibleWith(jt2)) return true;
   return false;
}


@Override public boolean isSubtype(TypeMirror t1,TypeMirror t2)
{
   JcompType jt1 = getJcompType(t1);
   JcompType jt2 = getJcompType(t2);
   if (jt1.isCompatibleWith(jt2)) return true;
   return false;
}



/********************************************************************************/
/*                                                                              */
/*      Utility methods                                                         */
/*                                                                              */
/********************************************************************************/

private JcompType getJcompType(TypeMirror tm)
{
   JannotTypeMirror jtm = (JannotTypeMirror) tm;
   JcompType jt = jtm.getJcompType();
   return jt;
}


private JcompTyper getJcompTyper() 
{
   JcompProject jp = proc_env.getJcompProject();
   return jp.getResolveTyper();
}

}       // end of class JannotTypeUtils




/* end of JannotTypeUtils.java */

