/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCContinue.java                                       */
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

import org.eclipse.jdt.core.dom.ContinueStatement;

import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

import edu.brown.cs.ivy.jannot.JannotName;

public class JannotTreeJCContinue extends JannotTreeJCStatement implements ContinueTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCContinue(ContinueStatement bs)
{
   super(bs);
}



/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitContinue(this,arg);
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.CONTINUE;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public JannotName getLabel()
{
   if (getContinue().getLabel() == null) return null;
   
   return new JannotName(getContinue().getLabel().getIdentifier());
}




/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private ContinueStatement getContinue()
{
   return (ContinueStatement) ast_node;
}





}       // end of class JannotTreeJCContinue




/* end of JannotTreeJCContinue.java */

