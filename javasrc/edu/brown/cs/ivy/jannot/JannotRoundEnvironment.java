/********************************************************************************/
/*                                                                              */
/*              JannotRoundEnvironment.java                                     */
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



package edu.brown.cs.ivy.jannot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;

class JannotRoundEnvironment implements RoundEnvironment, JannotConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private JannotProcessingEnvironment proc_env;
private List<JannotTypeElement> base_annots;
private List<ASTNode>   root_nodes;
private Map<String,Set<Annotation>> annot_nodes;
private List<JannotFileObject> new_files;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotRoundEnvironment(JannotProcessingEnvironment penv,List<ASTNode> roots,
      Collection<JannotTypeElement> base)
{ 
   proc_env = penv;
   base_annots = new ArrayList<>(base);
   root_nodes = new ArrayList<>(roots);
   AnnotationFinder af = new AnnotationFinder();
   for (ASTNode n : root_nodes) {
      n.accept(af);
    }
   annot_nodes = af.getAstNodes();
   new_files = null;
}





/********************************************************************************/
/*                                                                              */
/*      RoundEnvironment access methods                                         */
/*                                                                              */
/********************************************************************************/

@Override public boolean errorRaised()
{
   return proc_env.haveErrors();
}


@Override public Set<? extends Element> getRootElements()
{
   Set<JannotElement> rslt = new HashSet<>();
   for (ASTNode an : root_nodes) {
      rslt.add(new JannotElement(an));
    }
   return rslt;
}


@Override public boolean processingOver()
{
   if (new_files == null) getWrittenFiles();
   return new_files.isEmpty();
}


List<JannotFileObject> getWrittenFiles()
{
   JannotFiler filer = (JannotFiler) proc_env.getFiler();
   new_files = filer.getWrittenFiles();
   return new_files;
}



/********************************************************************************/
/*                                                                              */
/*      Element access methods                                                  */
/*                                                                              */
/********************************************************************************/

@Override public Set<? extends Element> getElementsAnnotatedWith(
      Class<? extends java.lang.annotation.Annotation> a)
{
   String aname = a.getName();
   Set<String> keys = Collections.singleton(aname);
   return getElementsAnnotatedWith(keys);
}



@Override public Set<? extends Element> getElementsAnnotatedWith(TypeElement te)
{
   JannotElement jae = (JannotElement) te;
   JcompSymbol js = JcompAst.getDefinition(jae.getAstNode());
   String tname = js.getFullName();
   Set<String> keys = Collections.singleton(tname);
   return getElementsAnnotatedWith(keys);
}


@Override public Set<? extends Element> getElementsAnnotatedWithAny(
      Set<Class<? extends java.lang.annotation.Annotation>> annots)
{
   Set<String> keys = new HashSet<>();
   for (Class<? extends java.lang.annotation.Annotation> a : annots) {
      String aname = a.getName();
      keys.add(aname);
    }
   return getElementsAnnotatedWith(keys);
}



@Override public Set<? extends Element> getElementsAnnotatedWithAny(TypeElement... annots)
{
   Set<String> keys = new HashSet<>();
   for (TypeElement te : annots) {
      JannotElement jae = (JannotElement) te;
      JcompSymbol js = JcompAst.getDefinition(jae.getAstNode());
      String tname = js.getFullName();
      keys.add(tname);
    }
   return getElementsAnnotatedWith(keys);
}




/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private Set<JannotElement> getElementsAnnotatedWith(Set<String> keys)
{
   Set<JannotElement> rslt = new HashSet<>();
   
   for (JannotTypeElement aelt : base_annots) {
      JcompType js = aelt.getBaseType();
      String tname = js.getName();
      if (keys.contains(tname) || keys.contains("*")) {
         Set<Annotation> srcannots = annot_nodes.get(tname);
         if (srcannots != null) {
            for (Annotation an : srcannots) {
               JannotElement parelt = JannotElement.createElement(an);
               if (parelt != null) rslt.add(parelt);
             }
          }
       }
    }

   return rslt;
}




private static class AnnotationFinder extends ASTVisitor {
   
   private Map<String,Set<Annotation>> annotation_asts;
   
   AnnotationFinder() {
      annotation_asts = new HashMap<>();
    }
   
   Map<String,Set<Annotation>> getAstNodes()            { return annotation_asts; }
   
   @Override public boolean visit(NormalAnnotation n) {
      addAnnotation(n);
      return false;
    }
   
   @Override public boolean visit(MarkerAnnotation n) {
      addAnnotation(n);
      return false;
    }
   
   @Override public boolean visit(SingleMemberAnnotation n) {
      addAnnotation(n);
      return false;
    }
   
   private void addAnnotation(Annotation n) {
      JcompType jt = JcompAst.getJavaType(n.getTypeName());
      String nm = jt.getName();
      Set<Annotation> nodes = annotation_asts.get(nm);
      if (nodes == null) {
         nodes = new HashSet<>();
         annotation_asts.put(nm,nodes);
       }
      nodes.add(n);
    }
   
}



}       // end of class JannotRoundEnvironment




/* end of JannotRoundEnvironment.java */

