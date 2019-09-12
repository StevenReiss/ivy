/********************************************************************************/
/*                                                                              */
/*              JannotAnnotationValue.java                                      */
/*                                                                              */
/*      Annotation Value implementation                                         */
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
import javax.lang.model.element.AnnotationValueVisitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;

import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;

class JannotAnnotationValue implements AnnotationValue
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ASTNode ast_node;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotAnnotationValue(ASTNode n)
{
   ast_node = n;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/


@Override public <R,P> R accept(AnnotationValueVisitor<R,P> v,P p) 
{
   switch (ast_node.getNodeType()) {
      case ASTNode.MARKER_ANNOTATION :
      case ASTNode.NORMAL_ANNOTATION :
      case ASTNode.SINGLE_MEMBER_ANNOTATION :
         return v.visitAnnotation(new JannotAnnotation((Annotation) ast_node),p);
      case ASTNode.ARRAY_INITIALIZER :
         List<JannotAnnotationValue> lrs = new ArrayList<>();
         ArrayInitializer ainit = (ArrayInitializer) ast_node;
         for (Object o : ainit.expressions()) {
            Expression ex = (Expression) o;
            lrs.add(new JannotAnnotationValue(ex));
          }
         return v.visitArray(lrs,p);
      case ASTNode.BOOLEAN_LITERAL :
         BooleanLiteral boollit = (BooleanLiteral) ast_node;
         return v.visitBoolean(boollit.booleanValue(),p);
      case ASTNode.NUMBER_LITERAL :
         JcompType jt = JcompAst.getExprType(ast_node);
         String sv = ((NumberLiteral) ast_node).getToken();
         switch (jt.getName()) {
            case "float" :
               return v.visitFloat((float) JannotElement.getFloatValue(sv),p);
            case "double" :
               return v.visitDouble(JannotElement.getFloatValue(sv),p);
            case "byte" :
               return v.visitByte((byte) JannotElement.getNumericValue(sv),p);
            case "char" :
               return v.visitChar((char) JannotElement.getNumericValue(sv),p);
            case "int" :
               return v.visitInt((int) JannotElement.getNumericValue(sv),p);
            case "long" :
               return v.visitLong(JannotElement.getNumericValue(sv),p);
            case "short" :
               return v.visitShort((short) JannotElement.getNumericValue(sv),p);
          }
         break;
      case ASTNode.CHARACTER_LITERAL :
         String charv = ((CharacterLiteral) ast_node).getEscapedValue();
         charv = IvyFormat.getLiteralValue(charv);
         if (charv.length() == 0) return v.visitChar((char) 0,p);
         else return v.visitChar(charv.charAt(0),p);
      case ASTNode.SIMPLE_NAME :
      case ASTNode.QUALIFIED_NAME :
         JcompSymbol js = JcompAst.getReference(ast_node);
         if (js != null && js.isEnumSymbol()) {
            return v.visitEnumConstant(
                  new JannotVariableElement((EnumConstantDeclaration) ast_node),p);
          }
         else if (js != null && js.isTypeSymbol()) {
            JcompType jtyp = JcompAst.getJavaType(ast_node);
            if (jtyp == null) jtyp = JcompAst.getExprType(ast_node);
            return v.visitType(JannotTypeMirror.createTypeMirror(jtyp),p);
          }
         break;
      case ASTNode.STRING_LITERAL :
         String strv = ((StringLiteral) ast_node).getEscapedValue();
         strv = IvyFormat.getLiteralValue(strv);
         return v.visitString(strv,p);
    }
   return v.visitUnknown(this,p);
}

@Override public Object getValue() {
   return JannotElement.getConstantValue(ast_node);
}

@Override public String toString() {
   return ast_node.toString();
}




}       // end of class JannotAnnotationValue




/* end of JannotAnnotationValue.java */

