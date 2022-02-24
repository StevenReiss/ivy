/********************************************************************************/
/*										*/
/*		JannotTreeJCCase.java						*/
/*										*/
/*	Compilation tree for switch cases					*/
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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

public abstract class JannotTreeJCCase extends JannotTreeJCStatement implements CaseTree
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JannotTreeJCCase(SwitchCase n)
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
   v.visitCase(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitCase(this,arg);
}



@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getExpression());
   tt.translate(getStatements());
   return this;
}




@Override public Tree.Kind getKind()
{
   return Tree.Kind.CASE;
}



/********************************************************************************/
/*										*/
/*	Tree methods								*/
/*										*/
/********************************************************************************/

// @SuppressWarnings("deprecation")
@Override public JannotTreeJCExpression getExpression()
{
   for (Object o : getCase().expressions()) {
      return (JannotTreeJCExpression) createTree((ASTNode) o);
    }
   return null;
}

public List<JannotTreeJCExpression> getExpressions()
{
   List<JannotTreeJCExpression> rslt = new ArrayList<>();
   for (Object o : getCase().expressions()) {
      rslt.add((JannotTreeJCExpression) createTree((ASTNode) o));
    }
   return rslt;
}


@Override public List<JannotTreeJCStatement> getStatements()
{
   List<JannotTreeJCStatement> rslt = new ArrayList<>();

   SwitchCase c = getCase();
   if (c.getParent() instanceof SwitchStatement) {
      SwitchStatement sw = (SwitchStatement) c.getParent();
      List<?> stmts = sw.statements();
      int idx = stmts.indexOf(c);
      for (int i = idx+1; i < stmts.size(); ++i) {
	 ASTNode n = (ASTNode) stmts.get(i);
	 if (n instanceof SwitchCase) break;
	 rslt.add((JannotTreeJCStatement) createTree(n));
       }
    }

   return rslt;
}


/****
public List<JannotTreeJCCaseLabel> getLabels()
{
   return null;
}
*****/



/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

private SwitchCase getCase()
{
   return (SwitchCase) ast_node;
}


}	// end of class JannotTreeJCCase




/* end of JannotTreeJCCase.java */

