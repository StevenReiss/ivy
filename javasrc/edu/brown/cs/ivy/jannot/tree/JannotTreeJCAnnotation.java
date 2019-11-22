/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCAnnotation.java                                     */
/*                                                                              */
/*      Compiler tree for annotations                                           */
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

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

public class JannotTreeJCAnnotation extends JannotTreeJCExpression implements AnnotationTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCAnnotation(Annotation n)
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
   v.visitAnnotation(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitAnnotation(this,arg);
}


@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getAnnotationType());
   tt.translate(getArguments());
   return this;
}




@Override public Tree.Kind getKind()
{
   return Tree.Kind.ANNOTATION;
}



/********************************************************************************/
/*                                                                              */
/*      Field methods                                                           */
/*                                                                              */
/********************************************************************************/

public List<JannotTreeJCExpression> getArgs()
{
   return getArguments();
}


/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public JannotTree getAnnotationType()
{
   return createTree(getAnnotation().getTypeName()); 
}



@Override public List<JannotTreeJCExpression> getArguments()
{
   List<JannotTreeJCExpression> rslt = new ArrayList<>();
   Annotation an = getAnnotation();
   if (an.isSingleMemberAnnotation()) {
      SingleMemberAnnotation sma = (SingleMemberAnnotation) an;
      rslt.add(createTree(sma.getValue()));
    }
   else if (an.isNormalAnnotation()) {
      NormalAnnotation na = (NormalAnnotation) an;
      for (Object o : na.values()) {
         rslt.add(createTree((Expression) o));
       }
    }
   return rslt;
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private Annotation getAnnotation()
{
   return (Annotation) ast_node;
}


}       // end of class JannotTreeJCAnnotation




/* end of JannotTreeJCAnnotation.java */

