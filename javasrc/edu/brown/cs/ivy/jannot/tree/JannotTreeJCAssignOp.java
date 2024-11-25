/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCAssignOp.java                                       */
/*                                                                              */
/*      Compilation tree for assignments other that =                           */
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

import org.eclipse.jdt.core.dom.Assignment;

import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;



public class JannotTreeJCAssignOp extends JannotTreeJCExpression implements CompoundAssignmentTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCAssignOp(Assignment n) 
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
   v.visitAssignOp(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitCompoundAssignment(this,arg);
}



@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getVariable());
   tt.translate(getExpression());
   return this;
}



@Override public Tree.Kind getKind()
{
   Assignment.Operator op = getNode().getOperator();
   if (op == Assignment.Operator.BIT_AND_ASSIGN) return Tree.Kind.AND_ASSIGNMENT;
   else if (op == Assignment.Operator.BIT_OR_ASSIGN) return Tree.Kind.OR_ASSIGNMENT;
   else if (op == Assignment.Operator.BIT_XOR_ASSIGN) return Tree.Kind.XOR_ASSIGNMENT;
   else if (op == Assignment.Operator.DIVIDE_ASSIGN) return Tree.Kind.DIVIDE_ASSIGNMENT;
   else if (op == Assignment.Operator.LEFT_SHIFT_ASSIGN) return Tree.Kind.LEFT_SHIFT_ASSIGNMENT;
   else if (op == Assignment.Operator.MINUS_ASSIGN) return Tree.Kind.MINUS_ASSIGNMENT;
   else if (op == Assignment.Operator.PLUS_ASSIGN) return Tree.Kind.PLUS_ASSIGNMENT;
   else if (op == Assignment.Operator.REMAINDER_ASSIGN) return Tree.Kind.REMAINDER_ASSIGNMENT;
   else if (op == Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN) return Tree.Kind.RIGHT_SHIFT_ASSIGNMENT;
   else if (op == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN) 
      return Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT;
   else if (op == Assignment.Operator.TIMES_ASSIGN) return Tree.Kind.MULTIPLY_ASSIGNMENT;
   else return Tree.Kind.ASSIGNMENT;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public JannotTreeJCExpression getExpression()
{
   return createTree(getNode().getRightHandSide()); 
}


@Override public JannotTreeJCExpression getVariable()
{
   return createTree(getNode().getLeftHandSide()); 
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private Assignment getNode()
{
   return (Assignment) ast_node;
}


}       // end of class JannotTreeJCAssignOp




/* end of JannotTreeJCAssignOp.java */

