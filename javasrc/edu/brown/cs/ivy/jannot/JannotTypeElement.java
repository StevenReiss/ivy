/********************************************************************************/
/*                                                                              */
/*              JannotTypeElement.java                                          */
/*                                                                              */
/*      Type element representation for annotation processing                   */
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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompType;

class JannotTypeElement extends JannotElement implements TypeElement
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private JcompType       base_type;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTypeElement(AbstractTypeDeclaration n) 
{
   super(n);
   base_type = JcompAst.getJavaType(n);
   if (base_type == null) {
      base_type = JcompAst.getJavaType(n.getName());
    }
}


JannotTypeElement(JcompType jt) 
{
   super(null);
   base_type = jt;
}


/********************************************************************************/
/*                                                                              */
/*      Jcomp methods                                                           */
/*                                                                              */
/********************************************************************************/

JcompType getBaseType()
{
   return base_type; 
}



/********************************************************************************/
/*                                                                              */
/*      Generic methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public ElementKind getKind() 
{
   if (base_type.isAnnotationType()) return ElementKind.ANNOTATION_TYPE;
   else if (base_type.isInterfaceType()) return ElementKind.INTERFACE;
   else if (base_type.isEnumType()) return ElementKind.ENUM;
   else if (base_type.isClassType()) return ElementKind.CLASS;
   
   return super.getKind();
}


@Override public TypeMirror asType()
{
   return JannotTypeMirror.createTypeMirror(base_type);
}


@Override public Name getSimpleName()
{
   String nm = base_type.getName();
   int idx = nm.lastIndexOf(".");
   if (idx > 0) nm = nm.substring(idx+1);
   return new JannotName(nm);
}


@Override public Name getQualifiedName()
{
   String nm = base_type.getName();
   return new JannotName(nm);
}




@Override public <R,P> R accept(ElementVisitor<R,P> v,P p) {
   return v.visitType(this,p);
}



/********************************************************************************/
/*                                                                              */
/*      Type Specific methods                                                   */
/*                                                                              */
/********************************************************************************/


@Override public List<? extends TypeMirror> getInterfaces()
{
   List<JannotTypeMirror> rslt = new ArrayList<>();
   for (JcompType ityp : base_type.getInterfaces()) {
      rslt.add(JannotTypeMirror.createTypeMirror(ityp));
    }
   
   return rslt;
}

@Override public NestingKind getNestingKind() 
{
   if (ast_node == null) return NestingKind.TOP_LEVEL;
   
   switch (ast_node.getNodeType()) {
      case ASTNode.ANONYMOUS_CLASS_DECLARATION :
         return NestingKind.ANONYMOUS;
    }
   for (ASTNode p = ast_node.getParent(); p != null; p = p.getParent()) {
      switch (p.getNodeType()) {
         case ASTNode.COMPILATION_UNIT : 
            return NestingKind.TOP_LEVEL;
         case ASTNode.TYPE_DECLARATION :
         case ASTNode.ANNOTATION_TYPE_DECLARATION : 
         case ASTNode.ENUM_DECLARATION :
            return NestingKind.MEMBER;
         case ASTNode.TYPE_DECLARATION_STATEMENT :
         case ASTNode.METHOD_DECLARATION :
            return NestingKind.LOCAL;
       }
    }
   
   return null;
}


@Override public TypeMirror getSuperclass() 
{
   JcompType styp = base_type.getSuperType();
   if (styp == null) return null;
   return JannotTypeMirror.createTypeMirror(styp);
}


@Override public boolean equals(Object o)
{
   if (o instanceof JannotTypeElement) {
      JannotTypeElement jei = (JannotTypeElement) o;
      return base_type.equals(jei.base_type);
    }
   
   return super.equals(o);
}



@Override public int hashCode()
{
   return base_type.hashCode();
}




}       // end of class JannotTypeElement




/* end of JannotTypeElement.java */

