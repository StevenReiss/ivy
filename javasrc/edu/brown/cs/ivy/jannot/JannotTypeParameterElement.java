/********************************************************************************/
/*                                                                              */
/*              JannotTypeParameterElement.java                                 */
/*                                                                              */
/*      Type Parameter element for annotation processing                        */
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompType;

class JannotTypeParameterElement extends JannotElement implements TypeParameterElement
{



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTypeParameterElement(TypeParameter n) 
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
   return ElementKind.TYPE_PARAMETER;
}

@Override public <R,P> R accept(ElementVisitor<R,P> v,P p) 
{
   return v.visitTypeParameter(this,p);
}



/********************************************************************************/
/*                                                                              */
/*      Type Parameter methods                                                  */
/*                                                                              */
/********************************************************************************/

@Override public List<? extends TypeMirror> getBounds() 
{
   List<JannotTypeMirror> rslt = new ArrayList<>();
   TypeParameter tp = (TypeParameter) ast_node;
   for (Object o1 : tp.typeBounds()) {
      Type tt = (Type) o1;
      JcompType jt = JcompAst.getJavaType(tt);
      if (jt != null) rslt.add(JannotTypeMirror.createTypeMirror(jt));
    }
   return rslt;
}



@Override public Element getGenericElement() 
{
   for (ASTNode p = ast_node.getParent(); p != null; p = p.getParent()) {
      switch (p.getNodeType()) {
         case ASTNode.TYPE_DECLARATION :
         case ASTNode.ENUM_DECLARATION :
         case ASTNode.ANNOTATION_TYPE_DECLARATION :
         case ASTNode.METHOD_DECLARATION :
            return new JannotElement(p);
       }
    }
   return null;
}



}       // end of class JannotTypeParameterElement




/* end of JannotTypeParameterElement.java */

