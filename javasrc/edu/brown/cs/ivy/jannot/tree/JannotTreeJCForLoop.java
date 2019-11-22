/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCForLoop.java                                        */
/*                                                                              */
/*      Compilation trees for for loops                                         */
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

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.Statement;

import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;


public class JannotTreeJCForLoop extends JannotTreeJCStatement implements ForLoopTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCForLoop(ForStatement s)
{
   super(s);
}



/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public void accept(JannotTreeVisitor v)
{
   v.visitForLoop(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitForLoop(this,arg);
}


@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getInitializer());
   tt.translate(getCondition());
   tt.translate(getUpdate());
   tt.translate(getStatement());
   return this;
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.FOR_LOOP;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public JannotTreeJCExpression getCondition()
{
   return createTree(getStmt().getExpression());
}



@Override public List<JannotTreeJCStatement> getInitializer()
{
   List<JannotTreeJCStatement> rslt = new ArrayList<>();
   for (Object o : getStmt().initializers()) {
      rslt.add(createTree((Statement) o));
    }
   return rslt;
}


@Override public JannotTreeJCStatement getStatement()
{
   return createTree(getStmt().getBody());
}


@Override public List<JannotTreeJCExpressionStatement> getUpdate()
{
   List<JannotTreeJCExpressionStatement> rslt = new ArrayList<>();
   for (Object o : getStmt().updaters()) {
      JannotTreeJCExpressionStatement ex = new JannotTreeJCExpressionStatement((Expression) o);
      rslt.add(ex);
    }
   return rslt;
}




/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private ForStatement getStmt()
{
   return (ForStatement) ast_node;
}





}       // end of class JannotTreeJCForLoop




/* end of JannotTreeJCForLoop.java */

