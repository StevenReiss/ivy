/********************************************************************************/
/*                                                                              */
/*              JannotExecutableElement.java                                    */
/*                                                                              */
/*      Element definition for methods, etc.                                    */
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

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;

class JannotExecutableElement extends JannotElement implements ExecutableElement
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotExecutableElement(AnnotationTypeMemberDeclaration n) 
{
   super(n);
}

JannotExecutableElement(MethodDeclaration n) 
{
   super(n);
}

JannotExecutableElement(Initializer n) 
{
   super(n);
}


/********************************************************************************/
/*                                                                              */
/*      Generic methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public ElementKind getKind() 
{
   switch (ast_node.getNodeType()) {
      case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
         return ElementKind.METHOD;
      case ASTNode.METHOD_DECLARATION :
         MethodDeclaration mthd = (MethodDeclaration) ast_node;
         if (mthd.isConstructor()) return ElementKind.CONSTRUCTOR;
         return ElementKind.METHOD;
      case ASTNode.INITIALIZER :
         return ElementKind.STATIC_INIT;
    }
   return super.getKind();
}

@Override public <R,P> R accept(ElementVisitor<R,P> v,P p) 
{
   return v.visitExecutable(this,p);
}



/********************************************************************************/
/*                                                                              */
/*      Executable-specific methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public AnnotationValue getDefaultValue() 
{ 
   if (ast_node.getNodeType() == ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION) {
      AnnotationTypeMemberDeclaration atmd = (AnnotationTypeMemberDeclaration) ast_node;
      if (atmd.getDefault() != null) 
         return new JannotAnnotationValue(atmd.getDefault());
    }
   return null;
}

@Override public List<? extends VariableElement> getParameters() 
{
   List<JannotVariableElement> rslt = new ArrayList<>();
   if (ast_node.getNodeType() == ASTNode.METHOD_DECLARATION) {
      MethodDeclaration md = (MethodDeclaration) ast_node;
      for (Object o : md.parameters()) {
         SingleVariableDeclaration def = (SingleVariableDeclaration) o;
         rslt.add(new JannotVariableElement(def));
       }
    }
   return rslt;
}

@Override public TypeMirror getReceiverType() 
{
   JcompSymbol js = JcompAst.getDefinition(ast_node);
   return JannotTypeMirror.createTypeMirror(js.getClassType());
}

@Override public TypeMirror getReturnType() 
{
   if (ast_node.getNodeType() == ASTNode.METHOD_DECLARATION) {
      MethodDeclaration md = (MethodDeclaration) ast_node;
      JcompType jt = JcompAst.getJavaType(md.getReturnType2());
      return JannotTypeMirror.createTypeMirror(jt);
    }
   else return null;
}

@Override public List<? extends TypeMirror> getThrownTypes() 
{ 
   List<JannotTypeMirror> rslt = new ArrayList<>();
   if (ast_node.getNodeType() == ASTNode.METHOD_DECLARATION) {
      MethodDeclaration md = (MethodDeclaration) ast_node;
      for (Object o : md.thrownExceptionTypes()) {
         ASTNode an = (ASTNode) o;
         JcompType jt = JcompAst.getJavaType(an);
         rslt.add(JannotTypeMirror.createTypeMirror(jt));
       }
    }
   return rslt;
}

@Override public List<? extends TypeParameterElement> getTypeParameters()
{
   List<JannotTypeParameterElement> rslt = new ArrayList<>();
   if (ast_node.getNodeType() == ASTNode.METHOD_DECLARATION) {
      MethodDeclaration md = (MethodDeclaration) ast_node;
      for (Object o : md.typeParameters()) {
         TypeParameter an = (TypeParameter) o;
         rslt.add(new JannotTypeParameterElement(an));
       }
    }
   return rslt;
}

@Override public boolean isDefault() 
{
   JcompSymbol js = JcompAst.getDefinition(ast_node);
   int mods = js.getModifiers();
   if (org.eclipse.jdt.core.dom.Modifier.isDefault(mods)) return true;   
   return false;
}


@Override public boolean isVarArgs() 
{
   if (ast_node.getNodeType() == ASTNode.METHOD_DECLARATION) {
      MethodDeclaration md = (MethodDeclaration) ast_node;
      return md.isVarargs();
    }
   return false;
}



}       // end of class JannotExecutableElement




/* end of JannotExecutableElement.java */

