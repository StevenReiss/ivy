/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCUnary.java                                          */
/*                                                                              */
/*      Compilation trees for unary operators                                   */
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

import com.sun.source.tree.UnaryTree;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.PostfixExpression;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;


public class JannotTreeJCUnary extends JannotTreeJCExpression implements UnaryTree
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static Map<Object,Tree.Kind> op_map;

static {
   op_map = new HashMap<>();
   op_map.put(PrefixExpression.Operator.COMPLEMENT,Tree.Kind.BITWISE_COMPLEMENT);
   op_map.put(PrefixExpression.Operator.DECREMENT,Tree.Kind.PREFIX_DECREMENT); 
   op_map.put(PrefixExpression.Operator.INCREMENT,Tree.Kind.PREFIX_INCREMENT); 
   op_map.put(PrefixExpression.Operator.MINUS,Tree.Kind.UNARY_MINUS); 
   op_map.put(PrefixExpression.Operator.NOT,Tree.Kind.LOGICAL_COMPLEMENT); 
   op_map.put(PrefixExpression.Operator.PLUS,Tree.Kind.UNARY_PLUS); 
   op_map.put(PostfixExpression.Operator.DECREMENT,Tree.Kind.POSTFIX_DECREMENT); 
   op_map.put(PostfixExpression.Operator.INCREMENT,Tree.Kind.POSTFIX_INCREMENT); 
}




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCUnary(Expression n) 
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
   v.visitUnary(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitUnary(this,arg);
}



@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getExpression());
   return this;
}



@Override public Tree.Kind getKind()
{
   Object op = null;
   if (ast_node instanceof PrefixExpression) {
      op = ((PrefixExpression) ast_node).getOperator();
    }
   else {
      op = ((PostfixExpression) ast_node).getOperator();
    }
      
   return op_map.get(op);
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public JannotTreeJCExpression getExpression()
{
   Expression ex = null;
   if (ast_node instanceof PrefixExpression) {
      ex = ((PrefixExpression) ast_node).getOperand();
    }
   else {
      ex = ((PostfixExpression) ast_node).getOperand();
    }   
   return createTree(ex); 
}



}       // end of class JannotTreeJCUnary




/* end of JannotTreeJCUnary.java */

