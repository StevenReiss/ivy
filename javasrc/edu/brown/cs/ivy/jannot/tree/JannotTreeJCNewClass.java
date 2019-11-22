/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCNewClass.java                                       */
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

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;



public class JannotTreeJCNewClass extends JannotTreeJCPolyExpression 
        implements NewClassTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCNewClass(ClassInstanceCreation n) 
{
   super(n);
}


/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public void accept(JannotTreeVisitor v)
{
   v.visitNewClass(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitNewClass(this,arg);
}



@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getEnclosingExpression());
   tt.translate(getIdentifier());
   tt.translate(getArguments());
   tt.translate(getClassBody());
   return this;
}




@Override public Tree.Kind getKind()
{
   return Tree.Kind.NEW_CLASS;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public List<JannotTreeJCExpression> getArguments()
{
   List<JannotTreeJCExpression> rslt = new ArrayList<>();
   for (Object o : getNode().arguments()) {
      rslt.add(createTree((Expression) o));
    }
   return rslt; 
}


@Override public JannotTreeJCExpression getEnclosingExpression()
{
   return createTree(getNode().getExpression()); 
}

@Override public JannotTreeJCExpression getIdentifier()
{
   return createTree(getNode().getType()); 
}

@Override public List<JannotTreeJCExpression> getTypeArguments()
{
   List<JannotTreeJCExpression> rslt = new ArrayList<>();
   for (Object o : getNode().typeArguments()) {
      rslt.add(createTree((Type) o));
    }
   return rslt; 
}


@Override public JannotTreeJCClassDecl getClassBody()
{
   return (JannotTreeJCClassDecl) createTree(getNode().getAnonymousClassDeclaration());
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private ClassInstanceCreation getNode()
{
   return (ClassInstanceCreation) ast_node;
}






}       // end of class JannotTreeJCNewClass




/* end of JannotTreeJCNewClass.java */

