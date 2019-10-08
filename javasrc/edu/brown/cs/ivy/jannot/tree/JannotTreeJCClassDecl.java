/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCClassDecl.java                                      */
/*                                                                              */
/*      Compilation trees for class declarations                                */
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
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

import edu.brown.cs.ivy.jannot.JannotName;

public class JannotTreeJCClassDecl extends JannotTreeJCStatement implements ClassTree
{



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCClassDecl(ASTNode n)
{
   super(n);
}


/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitClass(this,arg);
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.CLASS;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public JannotTree getExtendsClause()
{
   if (ast_node instanceof AnonymousClassDeclaration) return null; 
   
   AbstractTypeDeclaration atd = getTypeDecl();
   if (atd instanceof TypeDeclaration) {
      TypeDeclaration td = (TypeDeclaration) atd;
      return createTree(td.getSuperclassType());
    }
   return null;
}


@Override public List<JannotTree> getImplementsClause()
{
   List<JannotTree> rslt = new ArrayList<>();
   if (ast_node instanceof AnonymousClassDeclaration) return rslt;
   
   AbstractTypeDeclaration atd = getTypeDecl();
   if (atd instanceof TypeDeclaration) {
      TypeDeclaration td = (TypeDeclaration) atd;
      for (Object o : td.superInterfaceTypes()) {
         rslt.add(createTree((ASTNode) o));
       }
    }
   return rslt;
}



@Override public List<JannotTree> getMembers()
{
   List<JannotTree> rslt = new ArrayList<>();
   if (ast_node instanceof AbstractTypeDeclaration) {
      AbstractTypeDeclaration atd = getTypeDecl();
      for (Object o : atd.bodyDeclarations()) {
         rslt.add(createTree((ASTNode) o));
       }
    }
   else if (ast_node instanceof AnonymousClassDeclaration) {
      AnonymousClassDeclaration acd = (AnonymousClassDeclaration) ast_node;
      for (Object o : acd.bodyDeclarations()) {
         rslt.add(createTree((ASTNode) o));
       }
    }
   return rslt;
}



@Override public JannotTreeJCModifiers getModifiers()
{
   if (ast_node instanceof AnonymousClassDeclaration) return null;
   
   return new JannotTreeJCModifiers(ast_node,getTypeDecl().modifiers());
}



@Override public JannotName getSimpleName()
{
   if (ast_node instanceof AnonymousClassDeclaration) return null;
   
   return new JannotName(getTypeDecl().getName().getIdentifier());
}


@Override public List<JannotTreeJCTypeParameter> getTypeParameters()
{
   List<JannotTreeJCTypeParameter> rslt = new ArrayList<>();
   if (ast_node instanceof AbstractTypeDeclaration) {
      AbstractTypeDeclaration atd = getTypeDecl();
      List<?> tps = null;
      if (atd instanceof TypeDeclaration) {
         TypeDeclaration td = (TypeDeclaration) atd;
         tps = td.typeParameters();
       }
      if (tps != null) {
         for (Object o : tps) {
            rslt.add((JannotTreeJCTypeParameter) createTree((ASTNode) o));
          }
       }
    }
   return rslt;
}




/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private AbstractTypeDeclaration getTypeDecl()
{
   if (ast_node instanceof AbstractTypeDeclaration) 
      return (AbstractTypeDeclaration) ast_node;
   else if (ast_node instanceof TypeDeclarationStatement) {
      TypeDeclarationStatement tds = (TypeDeclarationStatement) ast_node;
      return tds.getDeclaration();
    }
   return null;
}



}       // end of class JannotTreeJCClassDecl




/* end of JannotTreeJCClassDecl.java */

