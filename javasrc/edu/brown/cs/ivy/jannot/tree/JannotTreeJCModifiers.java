/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCModifiers.java                                      */
/*                                                                              */
/*      Tree for a modifier list                                                */
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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;

import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

public class JannotTreeJCModifiers extends JannotTree implements ModifiersTree
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private List<?>         modifier_list;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCModifiers(ASTNode base,List<?> mods)
{
   super(base);
   modifier_list = mods;
}


/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitModifiers(this,arg);
}


@Override public Tree.Kind getKind()
{
   return Tree.Kind.MODIFIERS;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public List<JannotTreeJCAnnotation> getAnnotations()
{
   List<JannotTreeJCAnnotation> rslt = new ArrayList<>();
   for (Object o : modifier_list) {
      if (o instanceof Annotation) {
         rslt.add((JannotTreeJCAnnotation) createTree((ASTNode) o));
       }
    }
   return rslt;
}

@Override public Set<Modifier> getFlags()
{
   EnumSet<Modifier> rslt = EnumSet.noneOf(Modifier.class);
   for (Object o : modifier_list) {
      if (o instanceof org.eclipse.jdt.core.dom.Modifier) {
         org.eclipse.jdt.core.dom.Modifier mod = (org.eclipse.jdt.core.dom.Modifier) o;
         if (mod.isAbstract()) rslt.add(Modifier.ABSTRACT);
         else if (mod.isDefault()) rslt.add(Modifier.DEFAULT);
         else if (mod.isFinal()) rslt.add(Modifier.FINAL);
         else if (mod.isNative()) rslt.add(Modifier.NATIVE);
         else if (mod.isPrivate()) rslt.add(Modifier.PRIVATE);
         else if (mod.isProtected()) rslt.add(Modifier.PROTECTED);
         else if (mod.isStatic()) rslt.add(Modifier.STATIC);
         else if (mod.isStrictfp()) rslt.add(Modifier.STRICTFP);
         else if (mod.isSynchronized()) rslt.add(Modifier.SYNCHRONIZED);
         else if (mod.isTransient()) rslt.add(Modifier.TRANSIENT);
         else if (mod.isVolatile()) rslt.add(Modifier.VOLATILE);
       }
    }
   return rslt;
}



}       // end of class JannotTreeJCModifiers




/* end of JannotTreeJCModifiers.java */

