/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCNewArray.java                                       */
/*                                                                              */
/*      Compilation trees for array creation                                    */
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

import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;


public class JannotTreeJCNewArray extends JannotTreeJCExpression implements NewArrayTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCNewArray(ArrayCreation n) 
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
   v.visitNewArray(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitNewArray(this,arg);
}



@Override public JannotTree translate(JannotTreeTranslator tt)
{
   tt.translate(getAnnotations());
   for (List<JannotTreeJCAnnotation> oda : getDimAnnotations()) {
      tt.translate(oda);
    }
   tt.translate(getType());
   tt.translate(getDimensions());
   tt.translate(getInitializers());
   return this;
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.NEW_ARRAY;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public List<JannotTreeJCAnnotation> getAnnotations()
{
   List<JannotTreeJCAnnotation> rslt = new ArrayList<>();
   Type t = getNode().getType();
   if (t instanceof AnnotatableType) {
      AnnotatableType at = (AnnotatableType) t;
      for (Object o : at.annotations()) {
         rslt.add(createTree((Annotation) o));
       }
    }
   return rslt;
}



@Override public List<List<JannotTreeJCAnnotation>> getDimAnnotations()
{
   List<List<JannotTreeJCAnnotation>> rslt = new ArrayList<>();
   return rslt;
}


@Override public List<JannotTreeJCExpression> getDimensions()
{
   List<JannotTreeJCExpression> rslt = new ArrayList<>();
   for (Object o : getNode().dimensions()) {
      rslt.add(createTree((Expression) o));
    }
   return rslt;
}


@Override public List<JannotTreeJCExpression> getInitializers()
{
   List<JannotTreeJCExpression> rslt = new ArrayList<>();
   ArrayInitializer ai = getNode().getInitializer();
   if (ai != null) {
      for (Object o : ai.expressions()) {
         rslt.add(createTree((Expression) o));
       }
    }
   return rslt;
}




@Override public JannotTreeJCExpression getType()
{
   return createTypeTree(getNode().getType()); 
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private ArrayCreation getNode()
{
   return (ArrayCreation) ast_node;
}





}       // end of class JannotTreeJCNewArray




/* end of JannotTreeJCNewArray.java */

