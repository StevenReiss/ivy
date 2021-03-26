/********************************************************************************/
/*										*/
/*		JannotTreeJCIdent.java						*/
/*										*/
/*	description of class							*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.ivy.jannot.tree;

import edu.brown.cs.ivy.jannot.JannotName;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;


public class JannotTreeJCIdent extends JannotTreeJCExpression implements IdentifierTree
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JannotTreeJCIdent(ASTNode n)
{
   super(n);
}


/********************************************************************************/
/*										*/
/*	Abstract Method Implementations 					*/
/*										*/
/********************************************************************************/

@Override public void accept(JannotTreeVisitor v)
{
   v.visitIdent(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitIdentifier(this,arg);
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.IDENTIFIER;
}



/********************************************************************************/
/*										*/
/*	Tree methods								*/
/*										*/
/********************************************************************************/

@Override public JannotName getName()
{
   if (ast_node instanceof Name) return createName((Name) ast_node);
   if (ast_node instanceof SimpleType) {
      SimpleType st = (SimpleType) ast_node;
      return createName(st.getName());
    }
   else if (ast_node instanceof QualifiedType) {
      QualifiedType qt = (QualifiedType) ast_node;
      String sn = qt.getQualifier().toString() + "." + qt.getName().getIdentifier();
      return new JannotName(sn);
    }
  return null; 
}




}	// end of class JannotTreeJCIdent




/* end of JannotTreeJCIdent.java */

