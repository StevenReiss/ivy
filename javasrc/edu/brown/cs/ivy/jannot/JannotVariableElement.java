/********************************************************************************/
/*                                                                              */
/*              JannotVariableElement.java                                      */
/*                                                                              */
/*      Handle fields, constants, and local variable elements                   */
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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.VariableElement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;

class JannotVariableElement extends JannotElement implements VariableElement
{



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotVariableElement(EnumConstantDeclaration n) 
{
   super(n);
}


JannotVariableElement(SingleVariableDeclaration n) 
{
   super(n);
}



JannotVariableElement(VariableDeclarationFragment n) 
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
      case ASTNode.ENUM_CONSTANT_DECLARATION :
         return ElementKind.ENUM_CONSTANT;
      case ASTNode.SINGLE_VARIABLE_DECLARATION :
         if (ast_node.getParent().getNodeType() == ASTNode.CATCH_CLAUSE)
            return ElementKind.EXCEPTION_PARAMETER;
         else return ElementKind.PARAMETER;
      case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
         JcompSymbol def = JcompAst.getDefinition(ast_node);
         if (def.isFieldSymbol()) return ElementKind.FIELD;
         else return ElementKind.LOCAL_VARIABLE;
    }
   return super.getKind();
}



@Override public <R,P> R accept(ElementVisitor<R,P> v,P p)  {
   return v.visitVariable(this,p);
}



/********************************************************************************/
/*                                                                              */
/*      Variable-specific methods                                               */
/*                                                                              */
/********************************************************************************/

@Override public Object getConstantValue()  
{
   JcompSymbol js = JcompAst.getDefinition(ast_node);
   if (js != null && js.isFinal() && js.isStatic()) {
      ASTNode def = js.getDefinitionNode();
      if (def != null) {
         if (def instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) def;
            Expression ex = vdf.getInitializer();
            if (ex != null) return getConstantValue(ex);
          }
       }
    }
   return null;
}



}       // end of class JannotVariableElement




/* end of JannotVariableElement.java */

