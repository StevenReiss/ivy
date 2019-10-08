/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCBinary.java                                         */
/*                                                                              */
/*      Compilation trees for binary operators                                  */
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.InfixExpression;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;


public class JannotTreeJCBinary extends JannotTreeJCExpression implements BinaryTree
{


/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private static Map<InfixExpression.Operator,Tree.Kind> op_map;

static {
   op_map = new HashMap<>();
   op_map.put(InfixExpression.Operator.AND,Tree.Kind.AND);
   op_map.put(InfixExpression.Operator.CONDITIONAL_AND,Tree.Kind.CONDITIONAL_AND); 
   op_map.put(InfixExpression.Operator.CONDITIONAL_OR,Tree.Kind.CONDITIONAL_OR); 
   op_map.put(InfixExpression.Operator.DIVIDE,Tree.Kind.DIVIDE); 
   op_map.put(InfixExpression.Operator.EQUALS,Tree.Kind.EQUAL_TO); 
   op_map.put(InfixExpression.Operator.GREATER,Tree.Kind.GREATER_THAN); 
   op_map.put(InfixExpression.Operator.GREATER_EQUALS,Tree.Kind.GREATER_THAN_EQUAL); 
   op_map.put(InfixExpression.Operator.LEFT_SHIFT,Tree.Kind.LEFT_SHIFT); 
   op_map.put(InfixExpression.Operator.LESS,Tree.Kind.LESS_THAN); 
   op_map.put(InfixExpression.Operator.LESS_EQUALS,Tree.Kind.LESS_THAN_EQUAL);  
   op_map.put(InfixExpression.Operator.MINUS,Tree.Kind.MINUS);  
   op_map.put(InfixExpression.Operator.NOT_EQUALS,Tree.Kind.NOT_EQUAL_TO); 
   op_map.put(InfixExpression.Operator.OR,Tree.Kind.OR);  
   op_map.put(InfixExpression.Operator.PLUS,Tree.Kind.PLUS); 
   op_map.put(InfixExpression.Operator.REMAINDER,Tree.Kind.REMAINDER);  
   op_map.put(InfixExpression.Operator.RIGHT_SHIFT_SIGNED,Tree.Kind.RIGHT_SHIFT); 
   op_map.put(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED,Tree.Kind.UNSIGNED_RIGHT_SHIFT); 
   op_map.put(InfixExpression.Operator.TIMES,Tree.Kind.MULTIPLY); 
   op_map.put(InfixExpression.Operator.XOR,Tree.Kind.XOR); 
}



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCBinary(InfixExpression n) 
{
   super(n);
}


/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitBinary(this,arg);
}



@Override public Tree.Kind getKind()
{
   return op_map.get(getNode().getOperator());
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public JannotTreeJCExpression getLeftOperand()
{
   return createTree(getNode().getLeftOperand()); 
}


@Override public JannotTreeJCExpression getRightOperand()
{
   return createTree(getNode().getRightOperand()); 
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private InfixExpression getNode()
{
   return (InfixExpression) ast_node;
}



}       // end of class JannotTreeJCBinary




/* end of JannotTreeJCBinary.java */

