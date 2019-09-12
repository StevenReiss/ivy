/********************************************************************************/
/*                                                                              */
/*              JannotTypeMirror.java                                           */
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;

abstract class JannotTypeMirror implements TypeMirror 
{



/********************************************************************************/
/*                                                                              */
/*      Static methods                                                          */
/*                                                                              */
/********************************************************************************/

static JannotTypeMirror createTypeMirror(JcompType jt)
{
   if (jt == null || jt.isVoidType()) return new JannotNoType(null);
   
   if (jt.isPrimitiveType()) return new JannotPrimitiveType(jt);
   else if (jt.isArrayType()) return new JannotArrayType(jt);
   else if (jt.isClassType() || jt.isInterfaceType()) return new JannotClassType(jt);
   else if (jt.isErrorType()) return new JannotErrorType(jt);
   else if (jt.isMethodType()) return new JannotMethodType(jt,null);
   else if (jt.isIntersectionType()) return new JannotIntersectionType(jt);
   else if (jt.isAnyType()) return new JannotNullType(jt);
   else if (jt.isTypeVariable()) new JannotTypeVariable(jt); 
   else if (jt.isUnionType()) return new JannotUnionType(jt);
   else if (jt.isWildcardType()) return new JannotWildcardType(jt);
   
   return new JannotNoType(jt);
}



static JannotTypeMirror createTypeMirror(JcompSymbol sym) 
{
   if (sym.isMethodSymbol()) {
      return new JannotMethodType(sym.getType(),sym);
    }
   else return createTypeMirror(sym.getType());
}


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

protected JcompType       base_type;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTypeMirror(JcompType t) 
{
   base_type = t;
}




/********************************************************************************/
/*                                                                              */
/*      Local methods                                                           */
/*                                                                              */
/********************************************************************************/

JcompType getJcompType()
{
   return base_type;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/


@Override public String toString()
{
   return base_type.toString();
}


@Override public boolean equals(Object o)
{
   if (o instanceof JannotTypeMirror) {
      JannotTypeMirror ot = (JannotTypeMirror) o;
      return base_type.equals(ot.base_type);
    }
   return false;
}


@Override public int hashCode()
{
   return base_type.hashCode();
}



/********************************************************************************/
/*                                                                              */
/*      AnnotatedConstruct methods                                              */
/*                                                                              */
/********************************************************************************/

@Override public <A extends Annotation> A getAnnotation(Class<A> type) 
{
   ASTNode n = getAstNode();
   if (n == null) return null;
   return null;
}


@Override public List<? extends AnnotationMirror> getAnnotationMirrors()
{
   ASTNode n = getAstNode();
   if (n == null) return null;
   return null;
}


@Override public <A extends Annotation> A[] getAnnotationsByType(Class<A> type)
{
   ASTNode n = getAstNode();
   if (n == null) return null;
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      General methods                                                         */
/*                                                                              */
/********************************************************************************/

public Element asElement() 
{
   ASTNode n = getAstNode();
   if (n == null) return null;
   // return new JannotElementImpl(n);
   return null;
}


public TypeMirror getEnclosingType() 
{
   JcompType jt = base_type.getOuterType();
   if (jt == null) return null;
   return createTypeMirror(jt);
}



public List<? extends TypeMirror> getTypeArguments() 
{
   List<JannotTypeMirror> rslt = new ArrayList<>();
   for (JcompType jt : base_type.getComponents()) {
      JannotTypeMirror jtm = createTypeMirror(jt);
      rslt.add(jtm);
    }
   return rslt;
}



/********************************************************************************/
/*                                                                              */
/*      Utility methods                                                         */
/*                                                                              */
/********************************************************************************/

protected ASTNode getAstNode()
{
   if (base_type.getDefinition() == null) return null;
   ASTNode n = base_type.getDefinition().getDefinitionNode();
   return n;
}



/********************************************************************************/
/*                                                                              */
/*      Primitive type mirror                                                   */
/*                                                                              */
/********************************************************************************/

private static class JannotPrimitiveType extends JannotTypeMirror implements PrimitiveType 
{ 
   
   JannotPrimitiveType(JcompType jt) {
      super(jt);
    }
   
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      return v.visitPrimitive(this,p);
    }
   
   @Override public TypeKind getKind()  {
      if (base_type.isBooleanType()) return TypeKind.BOOLEAN;
      else if (base_type.isByteType()) return TypeKind.BYTE;
      else if (base_type.isCharType()) return TypeKind.CHAR;
      else if (base_type.isDoubleType()) return TypeKind.DOUBLE;
      else if (base_type.isFloatType()) return TypeKind.FLOAT;
      else if (base_type.isIntType()) return TypeKind.INT;
      else if (base_type.isLongType()) return TypeKind.LONG;
      else if (base_type.isShortType()) return TypeKind.SHORT;
      else if (base_type.isVoidType()) return TypeKind.VOID;
      return TypeKind.NONE;
    }
   
}       // end of inner class JannotPrimitiveType




/********************************************************************************/
/*                                                                              */
/*      Array Type Mirror                                                       */
/*                                                                              */
/********************************************************************************/

private static class JannotArrayType extends JannotTypeMirror implements ArrayType
{

   JannotArrayType(JcompType jt) {
      super(jt);
    }
   
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      return v.visitArray(this,p);
    }
   
   @Override public TypeKind getKind() {
      return TypeKind.ARRAY;
    }
   
   @Override public TypeMirror getComponentType() {
      return createTypeMirror(base_type.getBaseType());
    }
   
}       // end of inner class JannotArrayType



/********************************************************************************/
/*                                                                              */
/*      Error Type Mirror                                                       */
/*                                                                              */
/********************************************************************************/

private static class JannotErrorType extends JannotTypeMirror implements ErrorType {

   JannotErrorType(JcompType jt) {
      super(jt);
    }
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      return v.visitError(this,p);
    }
   
   @Override public TypeKind getKind() {
      return TypeKind.ERROR;
    }
   
}       // end of inner class JannotErrorType




/********************************************************************************/
/*                                                                              */
/*      Null Type Mirror                                                        */
/*                                                                              */
/********************************************************************************/

private static class JannotNullType extends JannotTypeMirror implements NullType {

   JannotNullType(JcompType jt) {
      super(jt);
    }
   
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      return v.visitNull(this,p);
    }
   
   @Override public TypeKind getKind() {
      return TypeKind.NULL;
    }
   
}       // end of inner class JannotNullType


/********************************************************************************/
/*                                                                              */
/*      Method type mirror                                                      */
/*                                                                              */
/********************************************************************************/

private static class JannotMethodType extends JannotTypeMirror implements ExecutableType {

   private JcompSymbol for_method;
   
   JannotMethodType(JcompType jt,JcompSymbol method) {
      super(jt);
      for_method = method;
    }
   
   @Override public TypeKind getKind() {
      return TypeKind.EXECUTABLE;
    }
   
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      return v.visitExecutable(this,p);
    }
   
   
   @Override public List<? extends TypeMirror> getParameterTypes() {
      List<JannotTypeMirror> rslt = new ArrayList<>();
      for (JcompType jt : base_type.getComponents()) {
         JannotTypeMirror jtm = createTypeMirror(jt);
         rslt.add(jtm);
       }
      return rslt;
    }
   
   
   @Override public TypeMirror getReceiverType() {
      if (for_method == null) return null;
      return createTypeMirror(for_method.getClassType());
    }
   
   @Override public TypeMirror getReturnType() {
      return createTypeMirror(base_type.getBaseType());
    }
   
   @Override public List<? extends TypeMirror> getThrownTypes() {
      List<JannotTypeMirror> rslt = new ArrayList<>();
      if (for_method != null) {
         for (JcompType jt : for_method.getExceptions()) {
            rslt.add(createTypeMirror(jt));
          }
       }
      return rslt;
    }
   
   @Override public List<? extends TypeVariable> getTypeVariables() {
      List<JannotTypeVariable> rslt = new ArrayList<>();
      MethodDeclaration md = (MethodDeclaration) for_method.getDefinitionNode();
      if (md != null) {
         // need to get a list of type variables
       }
      return rslt;
    }
   
   
}       // end of inner class JannotMethodType



/********************************************************************************/
/*                                                                              */
/*      Type Variable mirror                                                    */
/*                                                                              */
/********************************************************************************/

private static class JannotTypeVariable extends JannotTypeMirror implements TypeVariable {

   
   JannotTypeVariable(JcompType jt) {
      super(jt);
    }
   
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      return v.visitTypeVariable(this,p);
    }
   
   @Override public TypeKind getKind() {
      return TypeKind.TYPEVAR;
    }
   
   
   // one of these is wrong
   
   @Override public TypeMirror getLowerBound() {
      JcompType jt = base_type.getSuperType();
      if (jt == null) return null;
      
      return createTypeMirror(jt);
    }
   
   
   @Override public TypeMirror getUpperBound() {
      JcompType jt = base_type.getSuperType();
      if (jt == null) return null;
      
      return createTypeMirror(jt);
    }
   
}       // end of inner class JannotTypeVariable


/********************************************************************************/
/*                                                                              */
/*      Intersection type mirror                                                */
/*                                                                              */
/********************************************************************************/

private static class JannotIntersectionType extends JannotTypeMirror implements IntersectionType
{

   JannotIntersectionType(JcompType jt) {
      super(jt);
    }
   
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      return v.visitIntersection(this,p);
    }
   
   @Override public TypeKind getKind() {
      return TypeKind.INTERSECTION;
    }
   
   @Override public List<? extends TypeMirror> getBounds() {
      List<JannotTypeMirror> rslt = new ArrayList<>();
      for (JcompType jt : base_type.getComponents()) {
         JannotTypeMirror jtm = createTypeMirror(jt);
         rslt.add(jtm);
       }
      return rslt;
    }
   
}       // end of inner class JannotIntersectionType





/********************************************************************************/
/*                                                                              */
/*      Union type mirror                                                       */
/*                                                                              */
/********************************************************************************/

private static class JannotUnionType extends JannotTypeMirror implements UnionType
{
   
   JannotUnionType(JcompType jt) {
      super(jt);
    }
   
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      return v.visitUnion(this,p);
    }
   
   @Override public TypeKind getKind() {
      return TypeKind.UNION;
    }
   
   @Override public List<? extends TypeMirror> getAlternatives() {
      List<JannotTypeMirror> rslt = new ArrayList<>();
      for (JcompType jt : base_type.getComponents()) {
         JannotTypeMirror jtm = createTypeMirror(jt);
         rslt.add(jtm);
       }
      return rslt;
    }

}       // end of inner class JannotUnionType



/********************************************************************************/
/*                                                                              */
/*      Wildcard type mirror                                                    */
/*                                                                              */
/********************************************************************************/

private static class JannotWildcardType extends JannotTypeMirror implements WildcardType 
{

   JannotWildcardType(JcompType jt) {
      super(jt);
    }
   
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      return v.visitWildcard(this,p);
    }
   
   @Override public TypeKind getKind() {
      return TypeKind.WILDCARD;
    }
   
   // one of these is wrong
   
   @Override public TypeMirror getExtendsBound() {
      JcompType jt = base_type.getSuperType();
      if (jt == null) return null;
      return createTypeMirror(jt);
    }
   
   @Override public TypeMirror getSuperBound() {
      JcompType jt = base_type.getSuperType();
      if (jt == null) return null;
      return createTypeMirror(jt);
    }
   
}       // end of inner class JanotWildcardType




/********************************************************************************/
/*                                                                              */
/*      No type mirror                                                          */
/*                                                                              */
/********************************************************************************/

private static class JannotNoType extends JannotTypeMirror implements NoType 
{
   JannotNoType(JcompType jt) {
      super(jt);
    }
   
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      return v.visitNoType(this,p);
    }
   
   @Override public TypeKind getKind() {
      return TypeKind.NONE;
    }
   
   @Override public String toString() {
      return "*NONE";
    }
   
}       // end of inner class JannotNoType




/********************************************************************************/
/*                                                                              */
/*      Class type mirror                                                       */
/*                                                                              */
/********************************************************************************/

private static class JannotClassType extends JannotTypeMirror implements DeclaredType
{
   JannotClassType(JcompType jt) {
      super(jt);
    }
   
   @Override public <R,P> R accept(TypeVisitor<R,P> v,P p) {
      if (base_type.isUndefined() || base_type.isUnknown()) return v.visitUnknown(this,p);
      return v.visitDeclared(this,p);
    }
   
   @Override public TypeKind getKind() {
      return TypeKind.DECLARED;
    }
   
}       // end of inner class JannotClassType





}       // end of class JannotTypeMirror




/* end of JannotTypeMirror.java */

