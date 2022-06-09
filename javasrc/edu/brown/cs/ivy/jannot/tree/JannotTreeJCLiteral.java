/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCLiteral.java                                        */
/*                                                                              */
/*      Compilation trees for various types of literals                         */
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



package edu.brown.cs.ivy.jannot.tree;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompType;

public class JannotTreeJCLiteral extends JannotTreeJCExpression implements LiteralTree
{



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCLiteral(Expression n) 
{
   super(n);
}


/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public void accept(JannotTreeVisitor v)
{
   v.visitLiteral(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitLiteral(this,arg);
}



@Override public Tree.Kind getKind()
{
   switch (ast_node.getNodeType()) {
      case ASTNode.NUMBER_LITERAL :
         JcompType jt = JcompAst.getExprType(ast_node);
         if (jt.isIntType()) return Tree.Kind.INT_LITERAL;
         else if (jt.isFloatType()) return Tree.Kind.FLOAT_LITERAL;
         else if (jt.isDoubleType()) return Tree.Kind.DOUBLE_LITERAL;
         else if (jt.isLongType()) return Tree.Kind.LONG_LITERAL;
         return Tree.Kind.INT_LITERAL;
      case ASTNode.NULL_LITERAL :
         return Tree.Kind.NULL_LITERAL;
      case ASTNode.BOOLEAN_LITERAL :
         return Tree.Kind.BOOLEAN_LITERAL;
      case ASTNode.STRING_LITERAL :
      case ASTNode.TEXT_BLOCK :
         return Tree.Kind.STRING_LITERAL;
      case ASTNode.CHARACTER_LITERAL :
         return Tree.Kind.CHAR_LITERAL;
    }
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public Object getValue()
{
   switch (ast_node.getNodeType()) {
      case ASTNode.NUMBER_LITERAL :
         JcompType jt = JcompAst.getExprType(ast_node);
         String sv = ((NumberLiteral) ast_node).getToken();
         if (jt.isIntType()) return (int) getNumericValue(sv);
         else if (jt.isFloatType()) return (float) getFloatValue(sv);
         else if (jt.isDoubleType()) return getFloatValue(sv);
         else if (jt.isLongType()) return getNumericValue(sv);
         return 0;
      case ASTNode.NULL_LITERAL :
         return null;
      case ASTNode.BOOLEAN_LITERAL :
         return ((BooleanLiteral) ast_node).booleanValue();
      case ASTNode.STRING_LITERAL :
         String strv = ((StringLiteral) ast_node).getEscapedValue();
         return IvyFormat.getLiteralValue(strv);
      case ASTNode.TEXT_BLOCK :
         String tstrv = ((TextBlock) ast_node).getEscapedValue();
         return IvyFormat.getLiteralValue(tstrv);
      case ASTNode.CHARACTER_LITERAL :
         String chrv = ((CharacterLiteral) ast_node).getEscapedValue();
         String chv = IvyFormat.getLiteralValue(chrv);
         char val;
         if (chv.length() == 0) val = 0;
         else val = chv.charAt(0);
         return val;
    }
   return null; 
}


/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/


private double getFloatValue(String sv)
{
   double dv = 0;
   if (sv.endsWith("F") || sv.endsWith("f")) {
      sv = sv.substring(0,sv.length()-1);
    }
   dv = Double.parseDouble(sv);
   return dv;
}



private long getNumericValue(String sv)
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

}       // end of class JannotTreeJCLiteral




/* end of JannotTreeJCLiteral.java */

