/********************************************************************************/
/*										*/
/*		JannotTreeJCMethodDecl.java					*/
/*										*/
/*	Compilation trees for method declarations				*/
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

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Name;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

import edu.brown.cs.ivy.jannot.JannotName;

public class JannotTreeJCMethodDecl extends JannotTree implements MethodTree
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JannotTreeJCMethodDecl(MethodDeclaration md)
{
   super(md);
}


/********************************************************************************/
/*										*/
/*	Abstract Method Implementations 					*/
/*										*/
/********************************************************************************/

@Override public void accept(JannotTreeVisitor v)
{
   v.visitMethodDef(this);
}

@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitMethod(this,arg);
}


@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getModifiers());
   tt.translate(getReturnType());
   tt.translate(getTypeParameters());
   tt.translate(getReceiverParameter());
   tt.translate(getParameters());
   tt.translate(getThrows());
   tt.translate(getBody());
   return this;
}





@Override public Tree.Kind getKind()
{
   return Tree.Kind.METHOD;
}


/********************************************************************************/
/*										*/
/*	Methods 								*/
/*										*/
/********************************************************************************/

@Override public JannotTreeJCBlock getBody()
{
   return createTree(getDecl().getBody());
}


@Override public JannotTree getDefaultValue()
{
   return null;
}


@Override public JannotTreeJCModifiers getModifiers()
{
   return new JannotTreeJCModifiers(ast_node,getDecl().modifiers());
}


@Override public Name getName()
{
   return new JannotName(getDecl().getName().getIdentifier());
}


@Override public List<JannotTreeJCVariableDecl> getParameters()
{
   List<JannotTreeJCVariableDecl> rslt = new ArrayList<>();
   for (Object o : getDecl().parameters()) {
      rslt.add((JannotTreeJCVariableDecl) createTree((ASTNode) o));
    }
   return rslt;
}



@Override public JannotTreeJCVariableDecl getReceiverParameter()
{
   return null;
}


@Override public JannotTree getReturnType()
{
   return createTree(getDecl().getReturnType2());
}


@Override public List<JannotTreeJCExpression> getThrows()
{
   List<JannotTreeJCExpression> rslt = new ArrayList<>();
   for (Object o : getDecl().thrownExceptionTypes()) {
      rslt.add((JannotTreeJCExpression) createTree((ASTNode) o));
    }
   return rslt;
}



@Override public List<JannotTreeJCTypeParameter> getTypeParameters()
{
   List<JannotTreeJCTypeParameter> rslt = new ArrayList<>();
   for (Object o : getDecl().typeParameters()) {
      rslt.add((JannotTreeJCTypeParameter) createTree((ASTNode) o));
    }
   return rslt;
}



/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

private MethodDeclaration getDecl()
{
   return (MethodDeclaration) ast_node;
}


}	// end of class JannotTreeJCMethodDecl




/* end of JannotTreeJCMethodDecl.java */

