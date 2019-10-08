/********************************************************************************/
/*                                                                              */
/*              JannotTrees.java                                                */
/*                                                                              */
/*      Implementation of tree utilities for java compiler using eclipse trees  */
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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;

import edu.brown.cs.ivy.jannot.JannotProcessingEnvironment;

public class JannotTrees extends com.sun.source.util.Trees implements JannotTreeConstants
{



/********************************************************************************/
/*                                                                              */
/*      Factory methods                                                         */
/*                                                                              */
/********************************************************************************/

public JannotTrees jannotInstance(ProcessingEnvironment env)
{
   return new JannotTrees(env);
}


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private JannotProcessingEnvironment proc_env;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private JannotTrees(ProcessingEnvironment pe) 
{
   proc_env = (JannotProcessingEnvironment) pe;
}


/********************************************************************************/
/*                                                                              */
/*      Abstract access methods                                                 */
/*                                                                              */
/********************************************************************************/

@Override public String getDocComment(TreePath path)
{
   return null;
}


@Override public Element getElement(TreePath path)
{
   return null;
}


@Override public TypeMirror getLub(CatchTree tree)
{
   return null;
}

@Override public TypeMirror getOriginalType(ErrorType err)
{
   return null;
}


@Override public TreePath getPath(Element e)
{
   return null;
}


@Override public TreePath getPath(CompilationUnitTree unit,Tree node)
{
   return null;
}


@Override public TreePath getPath(Element e,AnnotationMirror a)
{
   return null;
}


@Override public TreePath getPath(Element e,AnnotationMirror a,AnnotationValue v)
{
   return null;
}

@Override public Scope getScope(TreePath path)
{
   return null;
}


@Override public SourcePositions getSourcePositions()
{
   return null;
}


@Override public Tree getTree(Element e)
{
   return null;
}


@Override public Tree getTree(Element e,AnnotationMirror a)
{
   return null;
}


@Override public Tree getTree(Element e,AnnotationMirror a,AnnotationValue v)
{
   return null;
}


@Override public MethodTree getTree(ExecutableElement method)
{
   return null;
}


@Override public ClassTree getTree(TypeElement element)
{
   return null;
}


@Override public TypeMirror getTypeMirror(TreePath path)
{
   return null;
}


@Override public boolean isAccessible(Scope scope,Element member,DeclaredType type)
{
   return false;
}


@Override public boolean isAccessible(Scope scope,TypeElement type)
{
   return false;
}


@Override public void printMessage(Diagnostic.Kind kind,CharSequence msg,
      Tree t,CompilationUnitTree root)
{
   proc_env.getMessager().printMessage(kind,msg);
}


}       // end of class JannotTrees




/* end of JannotTrees.java */

