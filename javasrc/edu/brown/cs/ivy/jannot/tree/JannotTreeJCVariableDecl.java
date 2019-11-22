/********************************************************************************/
/*										*/
/*		JannotTreeJCVariableDecl.java					*/
/*										*/
/*	Compilation tree for variable decarations				*/
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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.VariableTree;

import edu.brown.cs.ivy.jannot.JannotName;

public class JannotTreeJCVariableDecl extends JannotTreeJCStatement implements VariableTree
{



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JannotTreeJCVariableDecl(VariableDeclaration vd)
{
   super(vd);
}


/********************************************************************************/
/*										*/
/*	Abstract Method Implementations 					*/
/*										*/
/********************************************************************************/

@Override public void accept(JannotTreeVisitor v)
{
   v.visitVarDef(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitVariable(this,arg);
}


@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getModifiers());
   tt.translate(getNameExpression());
   tt.translate(getType());
   tt.translate(getInitializer());
   return this;
}


@Override public Tree.Kind getKind()
{
   return Tree.Kind.VARIABLE;
}


/********************************************************************************/
/*										*/
/*	Tree methods								*/
/*										*/
/********************************************************************************/

@Override public JannotTreeJCExpression getInitializer()
{
   return createTree(getDecl().getInitializer());
}


@Override public JannotTreeJCModifiers getModifiers()
{
   if (ast_node instanceof SingleVariableDeclaration) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) ast_node;
      return new JannotTreeJCModifiers(svd,svd.modifiers());
    }
   else {
      ASTNode par = getDecl().getParent();
      if (par instanceof VariableDeclarationStatement) {
	 VariableDeclarationStatement vds = (VariableDeclarationStatement) par;
	 return new JannotTreeJCModifiers(vds,vds.modifiers());
       }
      else if (par instanceof VariableDeclarationExpression) {
	 VariableDeclarationExpression vds = (VariableDeclarationExpression) par;
	 return new JannotTreeJCModifiers(vds,vds.modifiers());
       }
      else if (par instanceof FieldDeclaration) {
	 FieldDeclaration fd = (FieldDeclaration) par;
	 return new JannotTreeJCModifiers(fd,fd.modifiers());
       }
    }
   return null;
}


@Override public JannotName getName()
{
   return new JannotName(getDecl().getName().getIdentifier());
}

@Override public JannotTreeJCExpression getNameExpression()
{
   return createTree(getDecl().getName());
}


@Override public JannotTree getType()
{
   if (ast_node instanceof SingleVariableDeclaration) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) ast_node;
      return createTree(svd.getType());
    }
   else {
      ASTNode par = getDecl().getParent();
      if (par instanceof VariableDeclarationStatement) {
	 VariableDeclarationStatement vds = (VariableDeclarationStatement) par;
	 return createTree(vds.getType());
       }
      else if (par instanceof VariableDeclarationExpression) {
	 VariableDeclarationExpression vds = (VariableDeclarationExpression) par;
	 return createTree(vds.getType());
       }
      else if (par instanceof FieldDeclaration) {
	 FieldDeclaration fd = (FieldDeclaration) par;
	 return createTree(fd.getType());
       }
    }
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      Field methods                                                           */
/*                                                                              */
/********************************************************************************/

public JannotTreeJCModifiers getFieldMods()
{
   return getModifiers();
}


/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

private VariableDeclaration getDecl()
{
   return (VariableDeclaration) ast_node;
}




}	// end of class JannotTreeJCVariableDecl




/* end of JannotTreeJCVariableDecl.java */

