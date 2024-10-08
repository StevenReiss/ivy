/********************************************************************************/
/*										*/
/*		JcompFile.java							*/
/*										*/
/*	Representation of a Java file for semantic resolution			*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2007, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/


package edu.brown.cs.ivy.jcomp;



import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;

import edu.brown.cs.ivy.file.IvyLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;


class JcompFile implements JcompSemantics, JcompConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcompSource		for_file;
private ASTNode 		ast_root;
private JcompProjectImpl	for_project;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompFile(JcompSource rf)
{
   for_file = rf;
   ast_root = null;
   for_project = null;											
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public ASTNode getAstNode()
{
   if (ast_root == null) {
      IvyLog.logD("JCOMP","Start AST for " + getFile().getFileName());
      if (for_file instanceof JcompExtendedSource1 && for_project != null) {
	 JcompExtendedSource1 efile = (JcompExtendedSource1) for_file;
	 ast_root = efile.getAstRootNode(for_project.getProjectKey());
	 if (ast_root != null) return ast_root;
       }
      if (for_file instanceof JcompExtendedSource) {
	 JcompExtendedSource efile = (JcompExtendedSource) for_file;
	 ast_root = efile.getAstRootNode();
	 if (ast_root != null) return ast_root;
       }
      String txt = for_file.getFileContents();
      if (txt != null) {
	 ast_root = JcompAst.parseSourceFile(txt.toCharArray());
	 if (for_file instanceof JcompAstCleaner) {
	    JcompAstCleaner updr = (JcompAstCleaner) for_file;
	    ast_root = updr.cleanupAst(ast_root);
	  }
	 JcompAst.setSource(ast_root,for_file);
       }
    }
   return ast_root;
}

@Override public CompilationUnit getRootNode()
{
   ASTNode node = getAstNode();
   if (node == null) return null;
   if (node instanceof CompilationUnit) {
      return (CompilationUnit) node;
    }
   return null;
}


@Override public JcompSource getFile()		{ return for_file; }

@Override public List<JcompMessage> getMessages()
{
   List<JcompMessage> rslt = new ArrayList<JcompMessage>();

   ASTNode root = getAstNode();

   if (root != null && root instanceof CompilationUnit) {
      CompilationUnit cu = (CompilationUnit) root;
      for (IProblem p : cu.getProblems()) {
	 JcompMessageSeverity sev = JcompMessageSeverity.NOTICE;
	 if (p.isError()) sev = JcompMessageSeverity.ERROR;
	 else if (p.isWarning()) sev = JcompMessageSeverity.WARNING;
	 JcompMessage rm = new JcompMessage(getFile(),sev,
	       p.getID(),p.getMessage(),
	       p.getSourceLineNumber(),
	       p.getSourceStart(),p.getSourceEnd());
	 rslt.add(rm);
       }
    }

   ErrorVisitor ev = new ErrorVisitor(rslt);
   try {
      if (root != null) root.accept(ev);
    }
   catch (Throwable t) {
    }

   return rslt;
}



private class ErrorVisitor extends ASTVisitor {

   private List<JcompMessage> message_list;
   private boolean have_error;
   private Stack<Boolean> error_stack;

   ErrorVisitor(List<JcompMessage> msgs) {
      message_list = msgs;
      have_error = false;
      error_stack = new Stack<>();
    }

   @Override public void preVisit(ASTNode n) {
      error_stack.push(have_error);
      have_error = false;
    }

   @Override public void postVisit(ASTNode n) {
      boolean fg = error_stack.pop();
      have_error |= fg;
      if (!have_error) {
         JcompType jt = JcompAst.getExprType(n);
         if (jt != null && jt.isErrorType()) {
            if (n instanceof MethodInvocation) {
               MethodInvocation mi = (MethodInvocation) n;
               String mnm = "";
               if (mi.getExpression() != null) {
                  mnm = JcompAst.getExprType(mi.getExpression()).getName() + ".";
                }
               mnm += mi.getName().getIdentifier();
               addError("Undefined method " + mnm,IProblem.UndefinedMethod,n);
             }
            else {
               addError("Expression error",IProblem.InvalidOperator,n);
             }
            have_error = true;
          }
       }
    }

   @Override public boolean visit(SimpleName n) {
      JcompType jt = JcompAst.getExprType(n);
      if (jt != null && jt.isErrorType()) {
	 addError("Undefined name: " + n.getIdentifier(),IProblem.UndefinedName,n);
	 have_error = true;
       }
      return true;
    }
   
   @Override public void endVisit(ReturnStatement n) {
      checkNextReachable(n);
      if (have_error) return;
      ASTNode mthd = null;
      for (ASTNode n1 = n; n1 != null; n1 = n1.getParent()) {
         if (n1 instanceof MethodDeclaration) {
            mthd = n1;
            break;
          }
         else if (n1 instanceof LambdaExpression) {
            break;
          }
       }
      if (mthd == null) return;
      JcompSymbol msym = JcompAst.getDefinition(mthd);
      if (msym == null) return;
      JcompType mtyp = msym.getType();
      if (mtyp == null) return;
      JcompType rtyp = mtyp.getBaseType();
      
      Expression ex = n.getExpression();
      if (ex == null) {
         if (rtyp != null && !rtyp.isVoidType()) {
            IvyLog.logD("JCOMP","NOTE RETURN ERROR");
            addError("Must return value for method",IProblem.ReturnTypeMismatch,n);
          }
         return; 
       }
      
      JcompType rt = JcompAst.getExprType(ex);
      if (rt.isErrorType()) return;
      if (rtyp == null || rtyp.isVoidType()) {
         IvyLog.logD("JCOMP","NOTE RETURN ERROR");  
         addError("Can't return value for void method/constructor",
               IProblem.ReturnTypeMismatch,n);
       }
      // second clause is probably all that is needed
      else if (!rtyp.isAssignCompatibleWith(rt) && !rt.isAssignCompatibleWith(rtyp)) {
         IvyLog.logD("JCOMP","NOTE RETURN ERROR");    
         addError("Return type mismatch",IProblem.ReturnTypeMismatch,n);
       }
    }
   
   
   private void checkNextReachable(ASTNode n)
   {
      ASTNode par = n.getParent();
      if (par instanceof Block) {
         Block blk = (Block) par;
         int idx = blk.statements().indexOf(n);
         if (idx >= 0 && idx+1 < blk.statements().size()) {
            ASTNode next = (ASTNode) blk.statements().get(idx+1);
            if (next instanceof SwitchCase) return;
            else if (next instanceof LabeledStatement) return;
            else if (next instanceof Statement) {
               IvyLog.logD("JCOMP","NOTE REACHABLE ERROR");   
               addError("Unreachable statement",IProblem.UnreachableCatch,next);
             }
          }
       }
      return;
   }

   private void addError(String msg,int id,ASTNode n) {
      int start = n.getStartPosition();
      int end = start + n.getLength();
      int line = 0;
      if (ast_root instanceof CompilationUnit) {
         CompilationUnit cu = (CompilationUnit) ast_root;
         line = cu.getLineNumber(start);
       }
      JcompMessage rm = new JcompMessage(getFile(),JcompMessageSeverity.ERROR,
            id,msg,line,start,end);
      message_list.add(rm);
    }

}	// end of inner class ErrorVisitor



void setRoot(JcompProjectImpl root)
{
   for_project = root;
}

@Override public JcompProject getProject()
{
   return for_project;
}


@Override public void reparse()
{
   IvyLog.logD("JCOMP","Reparse " + getFile().getFileName());
   ast_root = null;
   for_project.setResolved(false,null);
}



private AbstractTypeDeclaration findTypeDecl(String cls,List<?> typs)
{
   AbstractTypeDeclaration atd = null;
   for (int i = 0; atd == null && i < typs.size(); ++i) {
      if (!(typs.get(i) instanceof AbstractTypeDeclaration)) continue;
      AbstractTypeDeclaration d = (AbstractTypeDeclaration) typs.get(i);
      if (cls != null) {
	 JcompType jt = JcompAst.getJavaType(d);
	 if (jt != null && !jt.getName().equals(cls)) {
	    if (cls.startsWith(jt.getName() + ".")) {
	       atd = findTypeDecl(cls,d.bodyDeclarations());
	     }
	    continue;
	  }
       }
      atd = d;
    }

   return atd;
}



/********************************************************************************/
/*										*/
/*	Determine if a class is defined in this file				*/
/*										*/
/********************************************************************************/

@Override public boolean definesClass(String cls)
{
   ASTNode root = getAstNode();
   if (root == null || root.getNodeType() != ASTNode.COMPILATION_UNIT)
      return false;
   CompilationUnit cu = (CompilationUnit) root;

   List<?> typs = cu.types();
   AbstractTypeDeclaration atd = findTypeDecl(cls,typs);

   return atd != null;
}




/********************************************************************************/
/*										*/
/*	Handle Finding related packages 					*/
/*										*/
/********************************************************************************/

@Override public Set<String> getRelatedPackages()
{
   Set<String> rslt = new HashSet<>();
   CompilationUnit cu = getRootNode();
   if (cu == null) return rslt;

   PackageDeclaration pd = cu.getPackage();
   if (pd != null) {
      String nm = pd.getName().getFullyQualifiedName();
      rslt.add(nm);
    }

   for (Object o : cu.imports()) {
      ImportDeclaration id = (ImportDeclaration) o;
      if (id.isStatic()) continue;
      String inm = id.getName().getFullyQualifiedName();
      if (!id.isOnDemand()) {
	 int idx = inm.lastIndexOf(".");
	 if (idx < 0) continue;
	 inm = inm.substring(0,idx);
       }
      rslt.add(inm);
    }

   return rslt;
}



/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return "FILE:" + for_file.getFileName();
}




}	// end of class JcompFile




/* end of JcompFile.java */
