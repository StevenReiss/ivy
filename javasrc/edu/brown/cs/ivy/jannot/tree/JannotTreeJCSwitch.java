/********************************************************************************/
/*										*/
/*		JannotTreeJCSwitch.java 					*/
/*										*/
/*	Compilation trees for switch statements 				*/
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

import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;

import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;


public class JannotTreeJCSwitch extends JannotTreeJCStatement implements SwitchTree
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JannotTreeJCSwitch(SwitchStatement s)
{
   super(s);
}



/********************************************************************************/
/*										*/
/*	Abstract Method Implementations 					*/
/*										*/
/********************************************************************************/

@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitSwitch(this,arg);
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.SWITCH;
}



/********************************************************************************/
/*										*/
/*	Tree methods								*/
/*										*/
/********************************************************************************/

@Override public List<JannotTreeJCCase> getCases()
{
   List<JannotTreeJCCase> rslt = new ArrayList<>();
   for (Object o : getStmt().statements()) {
      if (o instanceof SwitchCase) {
	 rslt.add(new JannotTreeJCCase((SwitchCase) o));
       }
    }
   return rslt;
}



@Override public JannotTreeJCExpression getExpression()
{
   return createTree(getStmt().getExpression());
}



/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

private SwitchStatement getStmt()
{
   return (SwitchStatement) ast_node;
}





}	// end of class JannotTreeJCSwitch




/* end of JannotTreeJCSwitch.java */

