/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCCompilationUnit.java                                */
/*                                                                              */
/*      Jannot version of JCCompilationUnit                                     */
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

import javax.tools.JavaFileObject;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

import edu.brown.cs.ivy.jannot.JannotFileObject;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSource;

public class JannotTreeJCCompilationUnit extends JannotTree implements CompilationUnitTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCCompilationUnit(CompilationUnit cu)
{
   super(cu);
}



/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public void accept(JannotTreeVisitor v)
{
   v.visitTopLevel(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitCompilationUnit(this,arg);
}






@Override public Tree.Kind getKind()
{
   return Tree.Kind.COMPILATION_UNIT;
}



/********************************************************************************/
/*                                                                              */
/*      Field accesss methods                                                   */
/*                                                                              */
/********************************************************************************/

public List<JannotTree> getDefs()
{
   List<JannotTree> rslt = new ArrayList<>();
   for (Object o : getCU().types()) {
      rslt.add(createTree((ASTNode) o));
    }
   for (Object o : getCU().imports()) {
      rslt.add(createTree((ASTNode) o));
    }
   return rslt;
}



public JannotTreeJCExpression getPid()
{
   return null;
}




/********************************************************************************/
/*                                                                              */
/*      Compilation Unit methods                                                */
/*                                                                              */
/********************************************************************************/

@Override public List<? extends ImportTree> getImports()
{
   List<JannotTreeJCImport> rslt = new ArrayList<>();
   for (Object o : getCU().imports()) {
      rslt.add((JannotTreeJCImport) createTree((ASTNode) o));
    }
   return rslt; 
}


@Override public LineMap getLineMap()
{
   return null;
}


@Override public PackageTree getPackage()
{
   return (PackageTree) createTree(getCU().getPackage()); 
}



@Override public List<JannotTreeJCAnnotation> getPackageAnnotations()
{
   return null;
}


@Override public ExpressionTree getPackageName()
{
   return createTree(getCU().getPackage().getName());
}



@Override public List<JannotTree> getTypeDecls()
{
   List<JannotTree> rslt = new ArrayList<>();
   for (Object o : getCU().types()) {
      rslt.add(createTree((ASTNode) o));
    }
   return rslt;
}


@Override public JavaFileObject getSourceFile()
{
   CompilationUnit cu = getCU();
        
   JcompSource src = JcompAst.getSource(cu);
   return JannotFileObject.createFileObject(src);
}


public JavaFileObject getFieldSourcefile()
{
   return getSourceFile();
}


/********************************************************************************/
/*                                                                              */
/*      Translation methods                                                     */
/*                                                                              */
/********************************************************************************/

@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getDefs());
   // translate each def and generate the result tree and insert into defs
   return this;
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private CompilationUnit getCU()
{
   return (CompilationUnit) ast_node;
}



}       // end of class JannotTreeJCCompilationUnit




/* end of JannotTreeJCCompilationUnit.java */

