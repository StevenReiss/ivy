/********************************************************************************/
/*										*/
/*		JannotTreeJCEnhancedForLoop.java				*/
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

import org.eclipse.jdt.core.dom.EnhancedForStatement;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

public class JannotTreeJCEnhancedForLoop extends JannotTreeJCStatement
//	implements EnhancedForLoopTree
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JannotTreeJCEnhancedForLoop(EnhancedForStatement n)
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
   v.visitForeachLoop(this);
}

@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return null;
// return visitor.visitEnhancedForLoop(this,arg);
}



@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getVariable());
   tt.translate(getExpression());
   tt.translate(getStatement());
   return this;
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.ENHANCED_FOR_LOOP;
}



/********************************************************************************/
/*										*/
/*	Tree methods								*/
/*										*/
/********************************************************************************/

// @Override
public JannotTreeJCExpression getExpression()
{
   return createTree(getFor().getExpression());
}



// @Override
public JannotTreeJCStatement getStatement()
{
   return createTree(getFor().getBody());
}


// @Override
public JannotTreeJCVariableDecl getVariable()
{
   return createTree(getFor().getParameter());
}


// @Override
public JannotTree getVariableOrRecordPattern()
{
   return null;
}


// @Override
// public EnhancedForLoopTree.DeclarationKind getDeclarationKind()
// {
//    return null;
// }





/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

private EnhancedForStatement getFor()
{
   return (EnhancedForStatement) ast_node;
}




}	// end of class JannotTreeJCEnhancedForLoop




/* end of JannotTreeJCEnhancedForLoop.java */

