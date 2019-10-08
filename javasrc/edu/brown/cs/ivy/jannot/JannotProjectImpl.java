/********************************************************************************/
/*                                                                              */
/*              JannotProjectImpl.java                                          */
/*                                                                              */
/*      Implementation of a project                                             *//*                                                                              */
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSemantics;
import edu.brown.cs.ivy.jcomp.JcompType;

class JannotProjectImpl extends JannotProject
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/


private JannotClassLoader class_loader;
private Map<Processor,Set<String>> annot_processors; 
private JannotProcessingEnvironment process_env;
private Set<String> annot_types;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotProjectImpl(JcompProject jp)
{
   super(jp);
   
   List<String> cp = jcomp_project.getClassPath();
   URL [] urls = new URL[cp.size()];
   for (int i = 0; i < urls.length; ++i) {
      String ent = cp.get(i);
      String nm = ent;
      try {
         if (ent.endsWith(".jar")) {
            nm = "jar:" + ent;
          }
         else {
            File fent = new File(ent);
            nm = "file:" + fent.getAbsolutePath();
            if (fent.isDirectory() && !nm.endsWith("/")) nm += "/";
          }
         URL u = new URL(nm);
         urls[i] = u;
       }
      catch (MalformedURLException e) {
         System.err.println("JANNOT: bad class path entry: " + ent);
       }
    }
   class_loader = new JannotClassLoader(urls);
   annot_processors = new LinkedHashMap<>();
   process_env = new JannotProcessingEnvironment(jp);
   annot_types = new HashSet<>();
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@SuppressWarnings("unchecked") @Override public Class<? extends java.lang.annotation.Annotation> findAnnotationClass(String name)
{
   try {
      Class<?> c = class_loader.loadClass(name);    
      return (Class<? extends java.lang.annotation.Annotation>) c;
    }
   catch (Exception e) { }
   
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      Methods to maintain the set of processors                               */
/*                                                                              */
/********************************************************************************/

@Override public boolean addAnnotationProcessor(String clazz)
{
   try {
      Class<?> cz = class_loader.loadClass(clazz);
      Processor p = (Processor) cz.getConstructor().newInstance();
      p.init(process_env);
      Set<String> annots = p.getSupportedAnnotationTypes();
      annot_processors.put(p,annots);
      annot_types.addAll(annots);
      p.getSupportedOptions();
      p.getSupportedSourceVersion();
      return true;
    }
   catch (ClassNotFoundException e) {
      System.err.println("JANNOT: annotation processor not found: " + clazz);
    }
   catch (ClassCastException e) {
      System.err.println("JANNOT: bad annotation processor: " + clazz);
    }
   catch (Exception e) {
      System.err.println("JANNOT: bad annotation processor constructor: " + clazz);
    }
   
   return false;
}



/********************************************************************************/
/*                                                                              */
/*      Processing methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override public boolean processProject()
{
   if (annot_processors.isEmpty()) return true;
   
   List<ASTNode> startfiles = new ArrayList<>();
   for (JcompSemantics js : jcomp_project.getSources()) {
      ASTNode as = js.getAstNode();
      if (as != null) startfiles.add(as);
    }
   
   AnnotationPresentFinder af = new AnnotationPresentFinder();
   for (ASTNode root : startfiles) {
      ASTNode rt = root.getRoot();
      rt.setProperty(JANNOT_PROJECT_PROP,this);
      root.accept(af);
    }
   
   Set<JannotTypeElement> annotelts = new HashSet<>();
   for (JannotAnnotation an : af.getAnnotations()) {
      JannotTypeMirror dt = (JannotTypeMirror) an.getAnnotationType();
      JcompType jt = dt.getJcompType();
      if (jt != null) {
         JannotTypeElement elt = JannotElement.createElement(jt);
         annotelts.add(elt);
       }
    }
   
   for ( ; ; ) {
      JannotRoundEnvironment renv = processRound(startfiles,annotelts);
      if (renv.processingOver()) break;
      List<ASTNode> newstarts = new ArrayList<>();
      for (JannotFileObject jfo : renv.getWrittenFiles()) {
         for (JcompSemantics js : jcomp_project.getSources()) {
            if (js.getFile() == jfo) {
               ASTNode as = js.getAstNode();
               if (as != null) newstarts.add(as);
             }
          }
       }
      startfiles = newstarts;
    }
   
   if (process_env.haveErrors()) return false;
   
   return true;
}



private JannotRoundEnvironment processRound(List<ASTNode> asts,Set<JannotTypeElement> annotelts) 
{
   JannotRoundEnvironment renv = new JannotRoundEnvironment(process_env,asts,annotelts);
   Set<JannotTypeElement> ourelts = new HashSet<JannotTypeElement>(annotelts);
   
   for (Processor p : annot_processors.keySet()) {
      Set<String> serves = annot_processors.get(p);
      Set<JannotTypeElement> uses = new HashSet<>();
      for (JannotTypeElement annot : ourelts) {
         JcompType jt = annot.getBaseType();
         if (doesServe(serves,jt)) uses.add(annot);
       }
      if (uses.isEmpty() && !doesServe(serves,null)) continue;
      try {
         if (p.process(uses,renv)) {
            ourelts.removeAll(uses);
          }
       }
      catch (Exception e) { }
    }
   List<JannotFileObject> newfiles = renv.getWrittenFiles();
   for (JannotFileObject jfo : newfiles) {
      jcomp_project.addSourceFile(jfo);
    }
   
   jcomp_project.resolve();
   
   return renv;
}



private boolean doesServe(Set<String> pats,JcompType typ)
{
   if (typ == null) {
      return pats.contains("*");
    }
   
   String tname = typ.getName();
   if (pats.contains("*")) return true;
   if (pats.contains(tname)) return true;
   for (String s : pats) {
      int idx = s.indexOf("/");
      if (idx > 0) {
         String mod = s.substring(0,idx);
         s = s.substring(idx+1);
         System.err.println("Attempt to check module " + mod);
       }
      if (s.endsWith(".*")) {
         String s1 = s.substring(0,s.length()-1);
         if (tname.startsWith(s1)) return true;
       }
      else if (s.equals(tname)) return true;
    }
   
   return false;
}



/********************************************************************************/
/*                                                                              */
/*      Annotation finder class                                                 */
/*                                                                              */
/********************************************************************************/

private class AnnotationPresentFinder extends ASTVisitor {
   
   private Set<JannotAnnotation> annotation_asts;
   
   AnnotationPresentFinder() {
      annotation_asts = new HashSet<>();
    }
   
   Set<JannotAnnotation> getAnnotations()                { return annotation_asts; }
   
   @Override public boolean visit(TypeDeclaration td) {
      addAnnotations(td);
      return true;
    }
   
   @Override public boolean visit(EnumDeclaration td) {
      addAnnotations(td);
      return true;
    }
   
   @Override public boolean visit(AnnotationTypeDeclaration td) {
      addAnnotations(td);
      return true;
    }
   
   @Override public boolean visit(FieldDeclaration td) {
      addAnnotations(td);
      return true;
    }
   
   @Override public boolean visit(VariableDeclarationStatement td) {
      addAnnotations(td);
      return true;
    }
   
   @Override public boolean visit(VariableDeclarationExpression td) {
      addAnnotations(td);
      return true;
    }
   
   @Override public boolean visit(SingleVariableDeclaration td) {
      addAnnotations(td);
      return true;
    }
   
   
   @Override public boolean visit(NormalAnnotation n) {
      return false;
    }
   
   @Override public boolean visit(MarkerAnnotation n) {
      return false;
    }
   
   @Override public boolean visit(SingleMemberAnnotation n) {
      return false;
    }
   
   private void addAnnotations(ASTNode n) {
      JannotElementUtils  jeu = (JannotElementUtils) process_env.getElementUtils();
      annotation_asts.addAll(jeu.getAnnotations(n));
    }
   
}



/********************************************************************************/
/*                                                                              */
/*      Our class loader                                                        */
/*                                                                              */
/********************************************************************************/

private static class JannotClassLoader extends URLClassLoader {
   
   JannotClassLoader(URL [] urls) {
      super(urls);
    }

}       // end of inner class JannotClassLoader



}       // end of class JannotProjectImpl




/* end of JannotProjectImpl.java */

