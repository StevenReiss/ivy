/********************************************************************************/
/*                                                                              */
/*              JannotAnnotation.java                                           */
/*                                                                              */
/*     Annotation representaiton                                                */
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

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompType;

class JannotAnnotation implements java.lang.annotation.Annotation, AnnotationMirror,
        JannotConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Annotation annot_node;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotAnnotation(Annotation ann) 
{
   annot_node = ann;
}



/********************************************************************************/
/*                                                                              */
/*      Local Access methods                                                    */
/*                                                                              */
/********************************************************************************/

Annotation getAstNode()
{
   return annot_node;
}



/********************************************************************************/
/*                                                                              */
/*      Annotation methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override public Class<? extends java.lang.annotation.Annotation> annotationType() 
{
   ASTNode root = annot_node.getRoot();
   JannotProject jp = (JannotProject) root.getProperty(JANNOT_PROJECT_PROP);
   JcompType jt = JcompAst.getJavaType(annot_node);
   return jp.findAnnotationClass(jt.getName());
}


@Override public DeclaredType getAnnotationType() 
{
   JcompType jt = JcompAst.getJavaType(annot_node);
   return (DeclaredType) JannotTypeMirror.createTypeMirror(jt);
}



@Override public Map<? extends ExecutableElement,? extends AnnotationValue> getElementValues() 
{
   Map<JannotExecutableElement,JannotAnnotationValue> rslt = new HashMap<>();
   switch (annot_node.getNodeType()) {
      case ASTNode.MARKER_ANNOTATION :
         break;
      case ASTNode.NORMAL_ANNOTATION :
         NormalAnnotation na = (NormalAnnotation) annot_node;
         for (Object o : na.values()) {
            MemberValuePair mvp = (MemberValuePair) o;
            // this is wrong -- name doesn't give executable element
            JannotExecutableElement key = (JannotExecutableElement) JannotElement.createElement(mvp.getName());
            JannotAnnotationValue av = new JannotAnnotationValue(mvp.getValue());
            rslt.put(key,av);
          }
         break;
      case ASTNode.SINGLE_MEMBER_ANNOTATION :
         SingleMemberAnnotation sma = (SingleMemberAnnotation) annot_node;
         SimpleName sn = JcompAst.getSimpleName(annot_node.getAST(),"value");
         // this is wrong -- name doesn't give executable element
         JannotExecutableElement key = (JannotExecutableElement) JannotElement.createElement(sn);
         rslt.put(key,new JannotAnnotationValue(sma.getValue()));
         break;
    }
   return rslt;
}



/********************************************************************************/
/*                                                                              */
/*      Equality methods                                                        */
/*                                                                              */
/********************************************************************************/

@Override public boolean equals(Object o) 
{
   if (o instanceof JannotAnnotation) {
      JannotAnnotation ja = (JannotAnnotation) o;
      return annot_node.equals(ja.annot_node);
    }
   return false;
}

@Override public int hashCode()
{
   return annot_node.hashCode();
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String toString() 
{
   return annot_node.toString();
}



}       // end of class JannotAnnotation




/* end of JannotAnnotation.java */

