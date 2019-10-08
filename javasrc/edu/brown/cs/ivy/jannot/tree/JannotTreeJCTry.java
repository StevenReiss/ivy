/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCTry.java                                            */
/*                                                                              */
/*      description of class                                                    */
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
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.TryStatement;

import com.sun.source.tree.TryTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;


public class JannotTreeJCTry extends JannotTreeJCStatement implements TryTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCTry(TryStatement s)
{
   super(s);
}



/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitTry(this,arg);
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.TRY;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public JannotTreeJCBlock getBlock()
{
   return createTree(getStmt().getBody());
}


@Override public List<JannotTreeJCCatch> getCatches()
{
   List<JannotTreeJCCatch> rslt = new ArrayList<>();
   for (Object o : getStmt().catchClauses()) {
      rslt.add(createTree((CatchClause) o));
    }
   return rslt;
}


@Override public JannotTreeJCBlock getFinallyBlock()
{
   return createTree(getStmt().getFinally());
}




@Override public List<JannotTree> getResources()
{
   List<JannotTree> rslt = new ArrayList<>();
   for (Object o : getStmt().resources()) {
      rslt.add(createTree((ASTNode) o));
    }
   return rslt;
}




/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private TryStatement getStmt()
{
   return (TryStatement) ast_node;
}






}       // end of class JannotTreeJCTry




/* end of JannotTreeJCTry.java */

