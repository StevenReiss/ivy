/********************************************************************************/
/*                                                                              */
/*              JannotElementUtils.java                                         */
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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSemantics;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;

class JannotElementUtils implements JannotConstants, Elements
{


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

JannotElementUtils(JannotProcessingEnvironment proc)
{
   proc_env = proc;
}



/********************************************************************************/
/*                                                                              */
/*      Annotation related methods                                              */
/*                                                                              */
/********************************************************************************/

@Override public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e)
{
   JannotElement je = (JannotElement) e;
   List<JannotAnnotation> rslt = getAnnotations(je.getAstNode());
   
   return rslt;
}



List<JannotAnnotation> getAnnotations(ASTNode n)
{
   List<JannotAnnotation> rslt = new ArrayList<>();
   
   if (n instanceof AbstractTypeDeclaration) {
      for (JcompType jt = JcompAst.getJavaType(n); jt != null; jt = jt.getSuperType()) {
         AbstractTypeDeclaration atd = getTypeNode(jt);
         if (atd != null) addAnnotations(atd.modifiers(),rslt);
       }
    }
   else {
      if (n instanceof VariableDeclarationFragment) {
         n = n.getParent();
       }
      if (n instanceof FieldDeclaration) {
         FieldDeclaration fdecl = (FieldDeclaration) n;
         addAnnotations(fdecl.modifiers(),rslt);
       }
      else if (n instanceof VariableDeclarationStatement) {
         VariableDeclarationStatement vds = (VariableDeclarationStatement) n;
         addAnnotations(vds.modifiers(),rslt);
       }
      else if (n instanceof VariableDeclarationExpression) {
         VariableDeclarationExpression vde = (VariableDeclarationExpression) n;
         addAnnotations(vde.modifiers(),rslt);
       }
      else if (n instanceof SingleVariableDeclaration) {
         SingleVariableDeclaration svd = (SingleVariableDeclaration) n;
         addAnnotations(svd.modifiers(),rslt);
       }
    }
   return rslt;
}



private void addAnnotations(List<?> mods,List<JannotAnnotation> rslt)
{
   for (Object o : mods) {
      if (o instanceof Annotation) {
         Annotation an = (Annotation) o;
         JannotAnnotation ja = new JannotAnnotation(an);
         rslt.add(ja);
       }
    }
}



@Override public Map<? extends ExecutableElement,? extends AnnotationValue>
getElementValuesWithDefaults(AnnotationMirror a)
{
   Map<JannotExecutableElement,JannotAnnotationValue> rslt = new HashMap<>();
   
   Map<? extends ExecutableElement,? extends AnnotationValue> given = a.getElementValues();
   for (Map.Entry<? extends ExecutableElement,? extends AnnotationValue> ent :
      given.entrySet()) {
      JannotExecutableElement key = (JannotExecutableElement) ent.getKey();
      JannotAnnotationValue val = (JannotAnnotationValue) ent.getValue();
      rslt.put(key,val);
    }
   
   JannotAnnotation ja = (JannotAnnotation) a;
   JcompType jt = JcompAst.getJavaType(ja.getAstNode());
   AnnotationTypeDeclaration atd = (AnnotationTypeDeclaration) getTypeNode(jt);
   if (atd == null) return rslt;
   for (Object o : atd.bodyDeclarations()) {
      if (o instanceof AnnotationTypeMemberDeclaration) {
         AnnotationTypeMemberDeclaration atmd = (AnnotationTypeMemberDeclaration) o;
         JannotExecutableElement key = new JannotExecutableElement(atmd);
         Expression ex = atmd.getDefault();
         if (ex != null) {
            if (rslt.get(key) == null) {
               rslt.put(key,new JannotAnnotationValue(ex));
             }
          }
       }
    }
   
   return rslt;
}


/********************************************************************************/
/*                                                                              */
/*      Member methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public List<? extends Element> getAllMembers(TypeElement type) 
{
   List<JannotElement> rslt = new ArrayList<>();
   
   for (JcompType jt = getJcompType(type); jt != null; jt = jt.getSuperType()) {
      AbstractTypeDeclaration atd = getTypeNode(jt);
      for (Object o : atd.bodyDeclarations()) {
          BodyDeclaration bd = (BodyDeclaration) o;
          JannotElement je = JannotElement.createElement(bd);
          if (je != null) rslt.add(je);
       }
    }
   
   return rslt;
}


@Override public PackageElement getPackageElement(CharSequence name)
{
   JcompProject jp = proc_env.getJcompProject();
   for (JcompSemantics js : jp.getSources()) {
      CompilationUnit cu = (CompilationUnit) js.getAstNode();
      if (cu == null) continue;
      PackageDeclaration pd = cu.getPackage();
      if (pd == null) continue;
      String pnm = pd.getName().getFullyQualifiedName();
      if (pnm.equals(name)) return (PackageElement) JannotElement.createElement(pd);
    }
   return null;
}



@Override public PackageElement getPackageOf(Element type)
{
   JannotElement je = (JannotElement) type;
   ASTNode an = je.getAstNode();
   CompilationUnit root = (CompilationUnit) an.getRoot();
   return (PackageElement) JannotElement.createElement(root);
}



/********************************************************************************/
/*                                                                              */
/*      Type Methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public TypeElement getTypeElement(CharSequence name)
{
   // find the system type for name
   // get the definition node for that type
   // return the type element 
   
   return null;
}


@Override public boolean hides(Element hider,Element hidden)
{
   // need to figure out what this means
   
   return false;
}




/********************************************************************************/
/*                                                                              */
/*      Naming methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public Name getBinaryName(TypeElement type) 
{
   JcompType jt = getJcompType(type);
   if (jt == null) return null;
   return new JannotName(jt.getJavaTypeName());
}



@Override public Name getName(CharSequence s)
{
   return new JannotName(s.toString());
}



/********************************************************************************/
/*                                                                              */
/*      Expression methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override public String getConstantExpression(Object v)
{
   return IvyFormat.getConstantExpression(v);
}


/********************************************************************************/
/*                                                                              */
/*      Property methods                                                        */
/*                                                                              */
/********************************************************************************/

@Override public boolean isDeprecated(Element e)
{ 
   JannotElement je = (JannotElement) e;
   ASTNode an = je.getAstNode();
   if (an instanceof BodyDeclaration) {
      BodyDeclaration bd = (BodyDeclaration) an;
      for (Object o : bd.modifiers()) {
         if (o instanceof Annotation) {
            Annotation oa = (Annotation) o;
            JcompType jt = JcompAst.getJavaType(oa.getTypeName());
            if (jt.getName().equals("java.lang.Deprecated")) return true;
          }
       }
    }
   return false;
}



@Override public boolean isFunctionalInterface(TypeElement type)
{
   JcompType jt = getJcompType(type);
   if (jt == null) return false;
   
   return jt.isFunctionRef();
}



@SuppressWarnings("unused")
@Override public boolean overrides(ExecutableElement overrides,
      ExecutableElement overridden,TypeElement type)
{
   JcompSymbol js1 = getJcompSymbol(overrides);
   JcompSymbol js2 = getJcompSymbol(overridden);
   JcompType jt = getJcompType(type);
   
   // need to do something here
   
   return false;
}




/********************************************************************************/
/*                                                                              */
/*      JavaDoc methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public String getDocComment(Element e) 
{
   BodyDeclaration bd = getBodyDeclaration(e);
   if (bd == null) return null;
   Javadoc jd = bd.getJavadoc();
   if (jd == null) return null;
   return jd.toString();
}




/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void printElements(Writer w,Element... elements)
{
   for (Element elt : elements) {
      JannotElement je = (JannotElement) elt;
      ASTNode an = je.getAstNode();
      try {
         w.write(an.toString());
         w.write("\n");
       }
      catch (IOException e) { }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Utility methods                                                         */
/*                                                                              */
/********************************************************************************/

private JcompType getJcompType(Element e) 
{
   if (e instanceof JannotElement) {
      JannotElement je = (JannotElement) e;
      JcompType jt = JcompAst.getJavaType(je.getAstNode());
      if (jt == null) jt = JcompAst.getExprType(je.getAstNode());
      return jt;
    }
   return null;
}



private JcompSymbol getJcompSymbol(Element e) 
{
   if (e instanceof JannotElement) {
      JannotElement je = (JannotElement) e;
      JcompSymbol js = JcompAst.getDefinition(je.getAstNode());
      if (js == null) js = JcompAst.getReference(je.getAstNode());
      return js;
    }
   return null;
}



private AbstractTypeDeclaration getTypeNode(JcompType jt)
{
   if (jt == null) return null;
   JcompSymbol js = jt.getDefinition();
   if (js == null) return null;
   return (AbstractTypeDeclaration) js.getDefinitionNode();
}


private BodyDeclaration getBodyDeclaration(Element e)
{
   if (e instanceof JannotElement) {
      JannotElement je = (JannotElement) e;
      ASTNode an = je.getAstNode();
      if (an instanceof BodyDeclaration) return (BodyDeclaration) an;
      if (an instanceof VariableDeclarationFragment) {
         ASTNode pdef = an.getParent();
         if (pdef instanceof BodyDeclaration) return (BodyDeclaration) pdef;
       }
    }
   
   return null;
}


}       // end of class JannotElementUtils




/* end of JannotElementUtils.java */

