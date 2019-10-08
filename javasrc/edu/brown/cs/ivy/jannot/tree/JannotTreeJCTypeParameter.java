/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCTypeParameter.java                                  */
/*                                                                              */
/*      Compilation trees for type parameters                                   */
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
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.TypeParameter;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.TypeParameterTree;

import edu.brown.cs.ivy.jannot.JannotName;

public class JannotTreeJCTypeParameter extends JannotTree implements TypeParameterTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCTypeParameter(TypeParameter tp)
{
   super(tp);
}



/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitTypeParameter(this,arg);
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.TYPE_PARAMETER;
}



/********************************************************************************/
/*                                                                              */
/*      Tree Methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public List<JannotTreeJCAnnotation> getAnnotations()
{
   List<JannotTreeJCAnnotation> rslt = new ArrayList<>();
   for (Object o : getParam().modifiers()) {
      if (o instanceof Annotation) {
         rslt.add((JannotTreeJCAnnotation) createTree((ASTNode) o));
       }
    }
   return rslt;
}


@Override public List<JannotTree> getBounds()
{
   List<JannotTree> rslt = new ArrayList<>();
   for (Object o : getParam().typeBounds()) {
      rslt.add(createTree((ASTNode) o));
    }
   return rslt;
}


@Override public JannotName getName()
{
   return new JannotName(getParam().getName().getIdentifier());
}




/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private TypeParameter getParam()
{
   return (TypeParameter) ast_node;
}

}       // end of class JannotTreeJCTypeParameter




/* end of JannotTreeJCTypeParameter.java */

