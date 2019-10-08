/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCMemberReference.java                                */
/*                                                                              */
/*      Compilation trees for member references                                 */
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeMethodReference;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

import edu.brown.cs.ivy.jannot.JannotName;



public class JannotTreeJCMemberReference extends JannotTreeJCFunctionalExpression 
        implements MemberReferenceTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCMemberReference(MethodReference n) 
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
   return visitor.visitMemberReference(this,arg);
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.MEMBER_REFERENCE;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public JannotTreeJCExpression getQualifierExpression()
{
   switch (ast_node.getNodeType()) {
      case ASTNode.EXPRESSION_METHOD_REFERENCE :
         return createTree(((ExpressionMethodReference) ast_node).getExpression());
      case ASTNode.SUPER_METHOD_REFERENCE :
      case ASTNode.TYPE_METHOD_REFERENCE :
      case ASTNode.CREATION_REFERENCE :
         break;
    }
   return null;
}


@Override public List<JannotTreeJCExpression> getTypeArguments()
{
   List<JannotTreeJCExpression> rslt = new ArrayList<>();
   for (Object o : getNode().typeArguments()) {
      rslt.add(createTree((Type) o));
    }
   return rslt;
}


@Override public JannotName getName()
{
   Name nm = null;
   switch (ast_node.getNodeType()) {
      case ASTNode.EXPRESSION_METHOD_REFERENCE :
         nm = ((ExpressionMethodReference) ast_node).getName();
         break;
      case ASTNode.SUPER_METHOD_REFERENCE :
         nm = ((SuperMethodReference) ast_node).getName();
         break;
      case ASTNode.TYPE_METHOD_REFERENCE :
         nm = ((TypeMethodReference) ast_node).getName();
         break;
      case ASTNode.CREATION_REFERENCE :
         break;
    }
    
      return createName(nm);
}


@Override public MemberReferenceTree.ReferenceMode getMode()
{
   switch (ast_node.getNodeType()) {
      case ASTNode.EXPRESSION_METHOD_REFERENCE :
      case ASTNode.SUPER_METHOD_REFERENCE :
      case ASTNode.TYPE_METHOD_REFERENCE :
         break;
      case ASTNode.CREATION_REFERENCE :
         return MemberReferenceTree.ReferenceMode.NEW;
    }
   return MemberReferenceTree.ReferenceMode.INVOKE;
}




/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private MethodReference getNode()
{
   return (MethodReference) ast_node;
}




}       // end of class JannotTreeJCMemberReference




/* end of JannotTreeJCMemberReference.java */

