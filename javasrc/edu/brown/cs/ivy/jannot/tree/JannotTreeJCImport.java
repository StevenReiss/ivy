/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCImport.java                                         */
/*                                                                              */
/*      Representation of an import tree                                        */
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

import org.eclipse.jdt.core.dom.ImportDeclaration;

import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

public class JannotTreeJCImport extends JannotTree implements ImportTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCImport(ImportDeclaration n)
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
   v.visitImport(this);
}



@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitImport(this,arg);
}

@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getQualId());
   return this;
}






@Override public Tree.Kind getKind()
{
   return Tree.Kind.IMPORT;
}



/********************************************************************************/
/*                                                                              */
/*      Field methods                                                          */
/*                                                                              */
/********************************************************************************/

public JannotTree getQualId()
{
   return null;
}

public boolean isModule()
{
   return false;
}

public boolean getStaticImport()
{
   return isStatic();
}


/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public Tree getQualifiedIdentifier()
{
   return createTree(getImport().getName());
}


@Override public boolean isStatic()
{
   return getImport().isStatic();
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private ImportDeclaration getImport()
{
   return (ImportDeclaration) ast_node;
}


}       // end of class JannotTreeJCImport




/* end of JannotTreeJCImport.java */

