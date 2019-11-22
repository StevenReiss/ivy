/********************************************************************************/
/*                                                                              */
/*              JannotElementImpl.java                                          */
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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;

public class JannotElement implements Element, Parameterizable, QualifiedNameable, JannotConstants 
{




/********************************************************************************/
/*                                                                              */
/*      Creation methods                                                        */
/*                                                                              */
/********************************************************************************/

static JannotElement createElement(ASTNode n)
{
   switch (n.getNodeType()) {
      case ASTNode.ANNOTATION_TYPE_DECLARATION :
      case ASTNode.TYPE_DECLARATION :
      case ASTNode.ENUM_DECLARATION :
         return new JannotTypeElement((AbstractTypeDeclaration) n);
         
      case ASTNode.METHOD_DECLARATION :
         return new JannotExecutableElement((MethodDeclaration) n);
      case ASTNode.INITIALIZER :
         return new JannotExecutableElement((Initializer) n);
      case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
         return new JannotExecutableElement((AnnotationTypeMemberDeclaration) n);
         
      case ASTNode.TYPE_PARAMETER :
         return new JannotTypeParameterElement((TypeParameter) n);
         
      case ASTNode.ENUM_CONSTANT_DECLARATION :
         return new JannotVariableElement((EnumConstantDeclaration) n);
      case ASTNode.SINGLE_VARIABLE_DECLARATION :
         return new JannotVariableElement((SingleVariableDeclaration) n);
      case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
         return new JannotVariableElement((VariableDeclarationFragment) n);
         
      case ASTNode.COMPILATION_UNIT :
         return new JannotPackageElement((CompilationUnit) n);
      case ASTNode.PACKAGE_DECLARATION :
         return new JannotPackageElement((PackageDeclaration) n);
         
      case ASTNode.SIMPLE_NAME :
      case ASTNode.QUALIFIED_NAME :
         JcompSymbol js = JcompAst.getDefinition(n);
         if (js == null) js = JcompAst.getReference(n);
         if (js != null) {
            ASTNode an = js.getDefinitionNode();
            if (an != null) return createElement(an);
          }
         break;
         
      default :
         if (n.getParent() != null) return createElement(n.getParent());
         break;
    }
   
   return new JannotElement(n);
}


static JannotTypeElement createElement(JcompType jt)
{
   JcompSymbol js = jt.getDefinition();
   if (js != null) {
      ASTNode an = js.getDefinitionNode();
      if (an != null) return (JannotTypeElement) createElement(an);
    }
   
   return new JannotTypeElement(jt);
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

protected ASTNode         ast_node;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotElement(ASTNode n)
{
   ast_node = n;
}


/********************************************************************************/
/*                                                                              */
/*      Type parameter methods                                                  */
/*                                                                              */
/********************************************************************************/

@Override public List<? extends TypeParameterElement> getTypeParameters()
{
   List<JannotTypeParameterElement> rslt = new ArrayList<>();
   
   if (ast_node != null) {
      MethodDeclaration md = (MethodDeclaration) ast_node;
      for (Object o : md.typeParameters()) {
         TypeParameter an = (TypeParameter) o;
         rslt.add(new JannotTypeParameterElement(an));
       }
    }
   
   return rslt;
}

/********************************************************************************/
/*                                                                              */
/*      Local access methods                                                    */
/*                                                                              */
/********************************************************************************/

public ASTNode getAstNode()             { return ast_node; }



/********************************************************************************/
/*                                                                              */
/*      Element methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public ElementKind getKind()
{
   return ElementKind.OTHER;
}



@Override public <R,P> R accept(ElementVisitor<R,P> v,P p)
{
   return v.visitUnknown(this,p);
}



@Override public TypeMirror asType()
{
   JcompType t = JcompAst.getJavaType(ast_node);
   return JannotTypeMirror.createTypeMirror(t);
}


@Override public Set<Modifier> getModifiers()
{
   Set<Modifier> rslt = EnumSet.noneOf(Modifier.class);
   if (ast_node != null) {
      JcompSymbol js = JcompAst.getDefinition(ast_node);
      int mods = js.getModifiers();
      if (org.eclipse.jdt.core.dom.Modifier.isAbstract(mods)) rslt.add(Modifier.ABSTRACT);
      if (org.eclipse.jdt.core.dom.Modifier.isDefault(mods)) rslt.add(Modifier.DEFAULT);   
      if (org.eclipse.jdt.core.dom.Modifier.isFinal(mods)) rslt.add(Modifier.FINAL);
      if (org.eclipse.jdt.core.dom.Modifier.isNative(mods)) rslt.add(Modifier.NATIVE);
      if (org.eclipse.jdt.core.dom.Modifier.isPrivate(mods)) rslt.add(Modifier.PRIVATE);
      if (org.eclipse.jdt.core.dom.Modifier.isProtected(mods)) rslt.add(Modifier.PROTECTED);
      if (org.eclipse.jdt.core.dom.Modifier.isPublic(mods)) rslt.add(Modifier.PUBLIC);
      if (org.eclipse.jdt.core.dom.Modifier.isStatic(mods)) rslt.add(Modifier.STATIC);
      if (org.eclipse.jdt.core.dom.Modifier.isStrictfp(mods)) rslt.add(Modifier.STRICTFP);
      if (org.eclipse.jdt.core.dom.Modifier.isSynchronized(mods)) rslt.add(Modifier.SYNCHRONIZED);
      if (org.eclipse.jdt.core.dom.Modifier.isTransient(mods)) rslt.add(Modifier.TRANSIENT);
      if (org.eclipse.jdt.core.dom.Modifier.isVolatile(mods)) rslt.add(Modifier.VOLATILE);
    }
   
   return rslt;
}


@Override public Name getSimpleName()
{
   JcompSymbol js = JcompAst.getDefinition(ast_node);
   return new JannotName(js.getName());
}


@Override public Name getQualifiedName()
{
   JcompSymbol js = JcompAst.getDefinition(ast_node);
   return new JannotName(js.getFullName());
}



/********************************************************************************/
/*                                                                              */
/*      Element hierarchy methods                                               */
/*                                                                              */
/********************************************************************************/

@Override public List<? extends Element> getEnclosedElements()
{
   List<JannotElement> rslt = new ArrayList<>();
   
   if (ast_node != null) {
      EnclosedVisitor ev = new EnclosedVisitor(ast_node);
      ast_node.accept(ev);
      for (ASTNode n : ev.getAstNodes()) {
         rslt.add(new JannotElement(n));
       }
    }
   
   return rslt;
}


@Override public Element getEnclosingElement()
{
   if (ast_node == null) return null;
   
   for (ASTNode p = ast_node.getParent(); p != null; p = p.getParent()) {
      switch (p.getNodeType()) {
         case ASTNode.ANNOTATION_TYPE_DECLARATION :
         case ASTNode.TYPE_DECLARATION :
         case ASTNode.ENUM_DECLARATION :
         case ASTNode.METHOD_DECLARATION :
         case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :       case ASTNode.ENUM_CONSTANT_DECLARATION :
         case ASTNode.SINGLE_VARIABLE_DECLARATION :
         case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
         case ASTNode.TYPE_PARAMETER :
         case ASTNode.COMPILATION_UNIT :
            return createElement(p);
       }
    }
   return null;
}


private static class EnclosedVisitor extends ASTVisitor {
   
   private List<ASTNode> enclosed_items;
   private ASTNode root_item;
   
   EnclosedVisitor(ASTNode root) {
      enclosed_items = new ArrayList<>();
      root_item = root;
    }
   
   List<ASTNode> getAstNodes()                          { return enclosed_items; }
   
   @Override public boolean preVisit2(ASTNode n) {
      switch (n.getNodeType()) {
         case ASTNode.ANNOTATION_TYPE_DECLARATION :
         case ASTNode.TYPE_DECLARATION :
         case ASTNode.ENUM_DECLARATION :
         case ASTNode.METHOD_DECLARATION :
         case ASTNode.ENUM_CONSTANT_DECLARATION :
         case ASTNode.SINGLE_VARIABLE_DECLARATION :
         case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
         case ASTNode.PACKAGE_DECLARATION :
         case ASTNode.TYPE_PARAMETER :
            return addChild(n);
       }
      
      return true;
    }
   
   private boolean addChild(ASTNode n) {
      if (n == root_item) return true;
      enclosed_items.add(n);
      return false;
    }
   
}       // end of inner class EnclosedVisitor



/********************************************************************************/
/*                                                                              */
/*      Annotation-related methods                                              */
/*                                                                              */
/********************************************************************************/

@Override public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annottype)
{
   if (ast_node instanceof AnnotatableType) {
      AnnotatableType atyp = (AnnotatableType) ast_node;
      for (Object o : atyp.annotations()) {
         Annotation an = (Annotation) o;
         JcompType jt = JcompAst.getJavaType(an);
         if (jt.getName().equals(annottype.getName())) {
            @SuppressWarnings("unchecked") A jan = (A)  new JannotAnnotation(an);
            return jan;
          }
       }
    }
   return null;
}


@Override public List<? extends AnnotationMirror> getAnnotationMirrors()
{
   List<JannotAnnotation> rslt = new ArrayList<>();
   if (ast_node instanceof AnnotatableType) {
      AnnotatableType atyp = (AnnotatableType) ast_node;
      for (Object o : atyp.annotations()) {
         Annotation an = (Annotation) o;
         JannotAnnotation jan = new JannotAnnotation(an);
         rslt.add(jan);
       }
    }
   return rslt;
}


@SuppressWarnings("unchecked") 
@Override public <A extends java.lang.annotation.Annotation> A[] getAnnotationsByType(Class<A> annottype)
{
   List<JannotAnnotation> rslt = new ArrayList<>();
   if (ast_node instanceof AnnotatableType) {
      AnnotatableType atyp = (AnnotatableType) ast_node;
      for (Object o : atyp.annotations()) {
         Annotation an = (Annotation) o;
         JcompType jt = JcompAst.getJavaType(an);
         if (jt.getName().equals(annottype.getName())) {
            JannotAnnotation jan = new JannotAnnotation(an);
            rslt.add(jan);
          }
       }
    }
   JannotAnnotation [] arslt = rslt.toArray(new JannotAnnotation[rslt.size()]);
   return (A []) arslt;
}



/********************************************************************************/
/*                                                                              */
/*      Comparison methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override public boolean equals(Object o)
{
   if (o instanceof JannotElement) {
      JannotElement jei = (JannotElement) o;
      return ast_node.equals(jei.ast_node);
    }
   return false;
}



@Override public int hashCode()
{
   return ast_node.hashCode();
}



/********************************************************************************/
/*                                                                              */
/*      helper methods                                                          */
/*                                                                              */
/********************************************************************************/

static Object getConstantValue(ASTNode an) 
{
   switch (an.getNodeType()) {
      case ASTNode.MARKER_ANNOTATION :
      case ASTNode.NORMAL_ANNOTATION :
      case ASTNode.SINGLE_MEMBER_ANNOTATION :
         return new JannotAnnotation((Annotation) an);
      case ASTNode.ARRAY_INITIALIZER :
         List<JannotAnnotationValue> lrs = new ArrayList<>();
         ArrayInitializer ainit = (ArrayInitializer) an;
         for (Object o : ainit.expressions()) {
            Expression ex = (Expression) o;
            lrs.add(new JannotAnnotationValue(ex));
          }
         return lrs;
      case ASTNode.BOOLEAN_LITERAL :
         BooleanLiteral boollit = (BooleanLiteral) an;
         return boollit.booleanValue();
      case ASTNode.NUMBER_LITERAL :
         JcompType jt = JcompAst.getExprType(an);
         String sv = ((NumberLiteral) an).getToken();
         switch (jt.getName()) {
            case "float" :
               return (float) getFloatValue(sv);
            case "double" :
               return getFloatValue(sv);
            case "byte" :
               return (byte) getNumericValue(sv);
            case "char" :
               return (char) getNumericValue(sv);
            case "int" :
               return (int) getNumericValue(sv);
            case "long" :
               return getNumericValue(sv);
            case "short" :
               return (short) getNumericValue(sv);
          }
         break;
      case ASTNode.CHARACTER_LITERAL :
         String charv = ((CharacterLiteral) an).getEscapedValue();
         charv = IvyFormat.getLiteralValue(charv);
         if (charv.length() == 0) return (char) 0;
         else return charv.charAt(0);
      case ASTNode.SIMPLE_NAME :
      case ASTNode.QUALIFIED_NAME :
         JcompSymbol js = JcompAst.getReference(an);
         if (js != null && js.isEnumSymbol()) {
            return new JannotElement(an);
          }
         else if (js != null && js.isTypeSymbol()) {
            JcompType jtyp = JcompAst.getJavaType(an);
            if (jtyp == null) jtyp = JcompAst.getExprType(an);
            return JannotTypeMirror.createTypeMirror(jtyp);
          }
         break;
      case ASTNode.STRING_LITERAL :
         String strv = ((StringLiteral) an).getEscapedValue();
         strv = IvyFormat.getLiteralValue(strv);
         return strv;
    }
   return null;
}



static double getFloatValue(String sv)
{
   double dv = 0;
   if (sv.endsWith("F") || sv.endsWith("f")) {
      sv = sv.substring(0,sv.length()-1);
    }
   dv = Double.parseDouble(sv);
   return dv;
}



static long getNumericValue(String sv) 
{
   long lv = 0;
   if (sv.endsWith("L") || sv.endsWith("l")) {
      sv = sv.substring(0,sv.length()-1);
    }
   if (sv.startsWith("0x") && sv.length() > 2) {
      sv = sv.substring(2);
      lv = Long.parseLong(sv,16);
    }
   else if (sv.startsWith("0X") && sv.length() > 2) {
      sv = sv.substring(2);
      lv = Long.parseLong(sv,16);
    }
   else if (sv.startsWith("0b") && sv.length() > 2) {
      sv = sv.substring(2);
      lv = Long.parseLong(sv,2);
    }
   else if (sv.startsWith("0B") && sv.length() > 2) {
      sv = sv.substring(2);
      lv = Long.parseLong(sv,2);
    }
   else if (sv.startsWith("0") && sv.length() > 1) {
      sv = sv.substring(1);
      lv = Long.parseLong(sv,8);
    }
   else lv = Long.parseLong(sv);
   
   return lv;
}



}       // end of class JannotElementImpl




/* end of JannotElementImpl.java */

