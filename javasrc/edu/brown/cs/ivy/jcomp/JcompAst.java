/********************************************************************************/
/*										*/
/*		JcompAst.java							*/
/*										*/
/*	Auxilliary methods for using ASTs in Jcomp				*/
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

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *	This class provides a set of static routines for accessing information from
 *	the abstract syntax tree.
 **/

public abstract class JcompAst implements JcompConstants {



/********************************************************************************/
/*										*/
/*	Parsing methods 							*/
/*										*/
/********************************************************************************/

public static CompilationUnit parseSourceFile(String text)
{
   return parseSourceFile(text.toCharArray());
}


public static CompilationUnit parseSourceFile(char [] buf)
{
   ASTParser parser = ASTParser.newParser(AST.JLS12);
   parser.setKind(ASTParser.K_COMPILATION_UNIT);
   parser.setSource(buf);

   Map<String,String> options = JavaCore.getOptions();
   JavaCore.setComplianceOptions(JavaCore.VERSION_12,options);
   options.put("org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures","enabled");
   options.put("org.eclipse.jdt.core.compiler.problem.assertIdentifier","ignore");
   options.put("org.eclipse.jdt.core.compiler.problem.enumIdentifier","ignore");
   parser.setCompilerOptions(options);
   parser.setResolveBindings(false);
   parser.setStatementsRecovery(true);
   CompilationUnit cu = (CompilationUnit) parser.createAST(null);

   return cu;
}



public static ASTNode parseStatement(String text)
{
   ASTParser parser = ASTParser.newParser(AST.JLS12);
   parser.setKind(ASTParser.K_COMPILATION_UNIT);
   Map<String,String> optsion = JavaCore.getOptions();
   // JavaCore.setComplianceOptions(JavaCore.VERSION_1_8,optsion);
   parser.setCompilerOptions(optsion);
   parser.setSource(text.toCharArray());
   CompilationUnit cu = (CompilationUnit) parser.createAST(null);

   return cu;
}



public static AST createNewAst()
{
   AST ast = AST.newAST(AST.JLS12,true);

   return ast;
}



/********************************************************************************/
/*										*/
/*	Resolving methods							*/
/*										*/
/********************************************************************************/


public static JcompProject getResolvedAst(JcompControl ctrl,ASTNode an)
{
   return getResolvedAst(ctrl,an,null);
}

public static JcompProject getResolvedAst(JcompControl ctrl,ASTNode an,List<String> jarnames)
{
   if (an == null) return null;

   return getResolvedAst(ctrl,Collections.singletonList(an),jarnames);
}


public static JcompProject getResolvedAst(JcompControl ctrl,List<ASTNode> srcasts,List<String> jarnames)
{
   if (srcasts == null || srcasts.isEmpty()) return null;

   List<JcompSource> srcs = new ArrayList<>();
   ASTNode sync = null;
   for (ASTNode an : srcasts) {
      JcompSource src = new LocalSource(an);
      srcs.add(src);
      if (sync == null) sync = an;
    }
   List<String> jars = jarnames;
   if (jars == null) jars = new ArrayList<>();

   JcompProject proj = ctrl.getProject(jars,srcs,false);
   try {
      synchronized (sync) {
	 proj.resolve();
       }
    }
   catch (Throwable t) {
      t.printStackTrace();
      ctrl.freeProject(proj);
      return null;
    }

   return proj;
}





private static class LocalSource implements JcompExtendedSource {

   private ASTNode root_result;

   LocalSource(ASTNode nd) {
      root_result = nd.getRoot();
    }

   @Override public String getFileContents() {
      return root_result.toString();
    }

   @Override public String getFileName() {
      return "*SCRAP*";
    }

   @Override public ASTNode getAstRootNode() {
      return root_result;
    }

}	// end of inner class ResultSource




/********************************************************************************/
/*										*/
/*	Scope Properties							*/
/*										*/
/********************************************************************************/

static public JcompScope getJavaScope(ASTNode n)
{
   return (JcompScope) n.getProperty(PROP_JAVA_SCOPE);
}


static void setJavaScope(ASTNode n,JcompScope s)
{
   n.setProperty(PROP_JAVA_SCOPE,s);
}



/********************************************************************************/
/*										*/
/*	Type properties 							*/
/*										*/
/********************************************************************************/

/**
 *	Return the type associated with a node.  This is the type for declarations
 *	and type references, not for expressions.
 **/

public static JcompType getJavaType(ASTNode n)
{
   if (n == null) return null;

   return (JcompType) n.getProperty(PROP_JAVA_TYPE);
}


static void setJavaType(ASTNode n,JcompType t)
{
   n.setProperty(PROP_JAVA_TYPE,t);
}


/**
 *	Return the name of the type associated with a node.  This is a short cut
 *	that uses getJavaType(n) and then, if the type is not null, calls getName()
 *	on the type.
 **/

public static String getJavaTypeName(ASTNode n)
{
   JcompType jt = getJavaType(n);
   if (jt == null) return null;
   return jt.getName();
}




/********************************************************************************/
/*										*/
/*	Symbol reference properties						*/
/*										*/
/********************************************************************************/

/**
 *	Get the symbol referenced by this node.
 **/

public static JcompSymbol getReference(ASTNode n)
{
   return (JcompSymbol) n.getProperty(PROP_JAVA_REF);
}


public static void setReference(ASTNode n,JcompSymbol js)
{
   n.setProperty(PROP_JAVA_REF,js);
   js.noteUsed();
   if (!js.isRead()) {
      if (canBeRead(n))
	 js.noteRead();
    }}



private static boolean canBeRead(ASTNode ref)
{
   ASTNode prev = null;
   for (ASTNode n = ref; n != null; n = n.getParent()) {
      if (n instanceof Assignment) {
	 if (prev == null) return false;
	 if (prev.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) return false;
	 return true;
       }
      else if (n instanceof ArrayAccess) {
	 if (prev == null) return true;
	 if (prev.getLocationInParent() != ArrayAccess.ARRAY_PROPERTY) return true;
       }
      else if (n instanceof FieldAccess) {
	 if (prev == null) return true;
	 if (prev.getLocationInParent() != FieldAccess.NAME_PROPERTY) return true;
       }
      else if (n instanceof SuperFieldAccess) {
	 if (prev == null) return true;
	 if (prev.getLocationInParent() != SuperFieldAccess.NAME_PROPERTY) return true;
       }
      else if (n instanceof ThisExpression) ;
      else if (n instanceof SimpleName) ;
      else if (n instanceof QualifiedName) {
	 if (prev != null) {
	    if (prev.getLocationInParent() != QualifiedName.NAME_PROPERTY) return true;
	  }
       }
      else if (n instanceof Expression) return true;
      else if (n instanceof Statement) return true;
      else if (n instanceof SingleVariableDeclaration) return false;
      else if (n instanceof VariableDeclarationFragment) return false;

      prev = n;
    }
   return true;
}



/********************************************************************************/
/*										*/
/*	Symbol definition properties						*/
/*										*/
/********************************************************************************/

/**
 *	Get the symbol defined by the corresponding AST node.
 **/

public static JcompSymbol getDefinition(ASTNode n)
{
   return (JcompSymbol) n.getProperty(PROP_JAVA_DEF);
}



public static void setDefinition(ASTNode n,JcompSymbol t)
{
   n.setProperty(PROP_JAVA_DEF,t);
}



/********************************************************************************/
/*										*/
/*	Expression type properties						*/
/*										*/
/********************************************************************************/

/**
 *	Get the evaluation type of the expression at the given AST node.  This
 *	is the computed type during evaluation.
 **/

public static JcompType getExprType(ASTNode n)
{
   return (JcompType) n.getProperty(PROP_JAVA_ETYPE);
}


static void setExprType(ASTNode n,JcompType t)
{
   if (t == null) {
      // System.err.println("ASSIGN NULL to " + n);
    }
   // if (t != null && t.isErrorType())
      // System.err.println("ASSIGN ERROR");
   n.setProperty(PROP_JAVA_ETYPE,t);
}



/********************************************************************************/
/*										*/
/*	Source methods								*/
/*										*/
/********************************************************************************/

/**
 *	Return the source file.  This should only work for the AST root.
 **/

public static JcompSource getSource(ASTNode n)
{
   n = n.getRoot();
   return (JcompSource) n.getProperty(PROP_JAVA_SOURCE);
}



static public void setSource(ASTNode n,JcompSource s)
{
   n = n.getRoot();
   n.setProperty(PROP_JAVA_SOURCE,s);
}




/********************************************************************************/
/*										*/
/*	Root methods								*/
/*										*/
/********************************************************************************/

static void setResolved(ASTNode n,JcompTyper jt)
{
   n = n.getRoot();
   n.setProperty(PROP_JAVA_TYPER,jt);
   n.setProperty(PROP_JAVA_RESOLVED,Boolean.TRUE);
}



static public boolean isResolved(ASTNode n)
{
   n = n.getRoot();
   return n.getProperty(PROP_JAVA_RESOLVED) == Boolean.TRUE;
}


public static void setKeep(ASTNode n)
{
   setKeep(n,true);
}



public static void setKeep(ASTNode n,boolean fg)
{
   n = n.getRoot();
   n.setProperty(PROP_JAVA_KEEP,fg);
}



static public boolean isKeep(ASTNode n)
{
   n = n.getRoot();
   return n.getProperty(PROP_JAVA_KEEP) == Boolean.TRUE;
}



public static JcompTyper getTyper(ASTNode n)
{
   n = n.getRoot();
   return (JcompTyper) n.getProperty(PROP_JAVA_TYPER);
}

static void setTyper(ASTNode n,JcompTyper typer)
{
   n = n.getRoot();
   n.setProperty(PROP_JAVA_TYPER,typer);
}


public static JcompProject getProject(ASTNode n)
{
   n = n.getRoot();
   return (JcompProject) n.getProperty(PROP_JAVA_PROJECT);
}

public static void setProject(ASTNode n,JcompProject p)
{
   n = n.getRoot();
   n.setProperty(PROP_JAVA_PROJECT,p);
}



/********************************************************************************/
/*										*/
/*	General property methods						*/
/*										*/
/********************************************************************************/

static public void clearSubtree(ASTNode n,boolean refsonly)
{
   if (n == null) return;

   ClearVisitor cv = new ClearVisitor(refsonly);
   n.accept(cv);
}


static public void clearAll(ASTNode n)
{
   if (n == null) return;

   n.setProperty(PROP_JAVA_TYPE,null);
   n.setProperty(PROP_JAVA_SCOPE,null);
   n.setProperty(PROP_JAVA_REF,null);
   n.setProperty(PROP_JAVA_ETYPE,null);
   n.setProperty(PROP_JAVA_DEF,null);
   n.setProperty(PROP_JAVA_RESOLVED,null);
   n.setProperty(PROP_JAVA_TYPER,null);
}


static public void clearRefs(ASTNode n)
{
   if (n == null) return;

   n.setProperty(PROP_JAVA_REF,null);
   n.setProperty(PROP_JAVA_ETYPE,null);
}




private static class ClearVisitor extends ASTVisitor {

   private boolean refs_only;

   ClearVisitor(boolean refsonly) {
      refs_only = refsonly;
    }

   @Override public void postVisit(ASTNode n) {
      if (refs_only) JcompAst.clearRefs(n);
      else JcompAst.clearAll(n);
    }

}	// end of inner class ClearVisitor



/********************************************************************************/
/*										*/
/*	Methods for handling names						*/
/*										*/
/********************************************************************************/

/**
 *	Return a AST node (or tree) for a possibly qualified name.  The nodes are
 *	constructed in the given AST.
 **/

public static Name getQualifiedName(AST ast,String s)
{
   synchronized (ast) {
      int idx = s.lastIndexOf(".");
      if (s.endsWith(".")) {
	 s = s.substring(0,idx);
	 idx = s.lastIndexOf(".");
       }
      if (idx < 0) {
	 try {
	    return ast.newSimpleName(s);
	  }
	 catch (IllegalArgumentException e) {
	    return ast.newSimpleName("JCOMP_ILLEGAL_NAME");
	  }
       }
      else {
	 try {
	    return ast.newQualifiedName(getQualifiedName(ast,s.substring(0,idx)),
					   ast.newSimpleName(s.substring(idx+1)));
	  }
	 catch (IllegalArgumentException e) {
	    System.err.println("PROBLEM CREATING NEW NAME FOR " + s + ": " + e);
	    throw e;
	  }
       }
    }
}


/**
 *	Return a new AST node for a simple (unqualified) name in the given AST.
 **/

public static SimpleName getSimpleName(AST ast,String s)
{
   synchronized (ast) {
      try {
	 return ast.newSimpleName(s);
       }
      catch (IllegalArgumentException e) {
	 System.err.println("PROBLEM CREATING NEW SIMPLE NAME FOR " + s + ": " + e);
	 throw e;
       }
    }
}




/********************************************************************************/
/*										*/
/*	Methods for creating special AST nodes					*/
/*										*/
/*	These are needed because some AST operations are not thread safe	*/
/*										*/
/********************************************************************************/

/**
 *	Construct an AST node for an integer constant.
 **/

public static NumberLiteral newNumberLiteral(AST ast,int v)
{
   return newNumberLiteral(ast,Integer.toString(v));
}

/**
 *	Construct an AST node for a long constant.
 **/

public static NumberLiteral newNumberLiteral(AST ast,long v)
{
   return newNumberLiteral(ast,Long.toString(v));
}

/**
 *	Construct an AST node for a number represented by a string.
 **/

public static NumberLiteral newNumberLiteral(AST ast,String v)
{
   synchronized (ast) {
      return ast.newNumberLiteral(v);
    }
}


static AtomicInteger unique_counter = new AtomicInteger();

/**
 *	Return a new string with the given prefix
 **/

public static String getUniqueString(String pfx)
{
   return pfx + unique_counter.incrementAndGet();
}



/********************************************************************************/
/*										*/
/*	Check if an AST contains a return statement				*/
/*										*/
/********************************************************************************/

/**
 *	Determine if the tree rooted in the input node contains a return statement.
 **/

public static boolean checkHasReturn(ASTNode n)
{
   if (n == null) return false;
   ReturnCheck rc = new ReturnCheck();
   n.accept(rc);
   return rc.hasReturn();
}


/**
 *	Determine if the tree rooted in the input node can return or whether it
 *	will not return.
 **/

public static boolean checkCanReturn(ASTNode n)
{
   if (n == null) return false;
   ReturnCheck rc = new ReturnCheck();
   n.accept(rc);
   return rc.canReturn();
}



private static class ReturnCheck extends ASTVisitor {

   private boolean has_return;
   private boolean has_throw;

   ReturnCheck() {
      has_return = false;
      has_throw = false;
    }

   boolean hasReturn()				{ return has_return; }
   boolean canReturn()				{ return has_return || has_throw; }

   @Override public void endVisit(ReturnStatement n) {
      if (n.getExpression() != null) has_return = true;
    }

   @Override public void endVisit(ThrowStatement n) {
      has_throw = true;
    }

   @Override public boolean visit(MethodDeclaration n)		{ return false; }
   @Override public boolean visit(FieldDeclaration n)		{ return false; }
   @Override public boolean visit(AnonymousClassDeclaration n)	{ return false; }

}



/********************************************************************************/
/*										*/
/*	Find set of exceptions thrown in a block				*/
/*										*/
/********************************************************************************/

/**
 *	Return the set of types corresponding to the exceptions that can be
 *	thrown by the tree rooted in the input node.
 **/

public static Set<JcompType> findExceptions(ASTNode n)
{
   if (n == null) return new HashSet<>();

   ExceptionFinder ef = new ExceptionFinder();
   n.accept(ef);
   return ef.getExceptions();
}


private static class ExceptionFinder extends ASTVisitor
{
   private Set<JcompType> found_exceptions;
   private Stack<Set<JcompType>> try_stack;

   ExceptionFinder() {
      found_exceptions = new HashSet<JcompType>();
      try_stack = new Stack<Set<JcompType>>();
    }

   Set<JcompType> getExceptions()			{ return found_exceptions; }

   @Override public void endVisit(ThrowStatement n) {
      JcompType jt = JcompAst.getExprType(n.getExpression());
      if (jt != null) found_exceptions.add(jt);
    }

   @Override public void endVisit(MethodInvocation n) {
      JcompSymbol js = JcompAst.getReference(n);
      handleCall(js);
    }

   @Override public void endVisit(ClassInstanceCreation n) {
      JcompSymbol js = JcompAst.getReference(n);
      handleCall(js);
    }

   @Override public boolean visit(TryStatement n) {
      try_stack.push(found_exceptions);
      found_exceptions = new HashSet<JcompType>();
      return true;
    }

   @Override public void endVisit(CatchClause n) {
      SingleVariableDeclaration svd = n.getException();
      Type t = svd.getType();
      JcompType jt = JcompAst.getJavaType(t);
      if (jt == null) return;
      for (Iterator<JcompType> it = found_exceptions.iterator(); it.hasNext(); ) {
	 JcompType et = it.next();
	 if (et.isCompatibleWith(jt)) it.remove();
       }
    }

   @Override public void endVisit(TryStatement n) {
      Set<JcompType> add = found_exceptions;
      found_exceptions = try_stack.pop();
      found_exceptions.addAll(add);
    }

   private void handleCall(JcompSymbol js) {
      if (js == null) return;
      for (JcompType jt : js.getExceptions()) {
	 found_exceptions.add(jt);
       }
      if (js.getDefinitionNode() != null) {
	 ASTNode an = js.getDefinitionNode();
	 if (an.getNodeType() == ASTNode.METHOD_DECLARATION) {
	    MethodDeclaration md = (MethodDeclaration) an;
	    for (Object o : md.thrownExceptionTypes()) {
	       Type n = (Type) o;
	       JcompType jt = JcompAst.getJavaType(n);
	       if (jt != null) found_exceptions.add(jt);
	     }
	  }
       }
    }

}	// end of subclass ExceptionFinder





/********************************************************************************/
/*										*/
/*	Check for recursion							*/
/*										*/
/********************************************************************************/

/**
 *	Determine if the given method declaration is recursive (calls itself
 *	directly).
 **/

public static boolean checkIfRecursive(MethodDeclaration md)
{
   RecursiveCheck rc = new RecursiveCheck(md);
   md.accept(rc);
   return rc.isRecursive();
}


private static class RecursiveCheck extends ASTVisitor {

   private JcompSymbol	 method_sym;
   private boolean	is_recursive;

   RecursiveCheck(MethodDeclaration md) {
      method_sym = getDefinition(md);
      is_recursive = false;
    }

   boolean isRecursive()			{ return is_recursive; }

   @Override public void endVisit(MethodInvocation mi) {
      if (JcompAst.getReference(mi) == method_sym) is_recursive = true;
    }

}	// end of inner class RecursiveCheck



/********************************************************************************/
/*										*/
/*	Get name node associated with AST node					*/
/*										*/
/********************************************************************************/

public static Name getNameFromNode(ASTNode d)
{
   Name nm = null;

   if (d == null) return null;

   switch (d.getNodeType()) {
      default :
      case ASTNode.ANNOTATION_TYPE_DECLARATION :
      case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
	 break;
      case ASTNode.ENUM_CONSTANT_DECLARATION :
	 nm = ((EnumConstantDeclaration) d).getName();
	 break;
      case ASTNode.ENUM_DECLARATION :
	 nm = ((EnumDeclaration) d).getName();
	 break;
      case ASTNode.METHOD_DECLARATION :
	 nm = ((MethodDeclaration) d).getName();
	 break;
      case ASTNode.PACKAGE_DECLARATION :
	 nm = ((PackageDeclaration) d).getName();
	 break;
      case ASTNode.SINGLE_VARIABLE_DECLARATION :
	 nm = ((SingleVariableDeclaration) d).getName();
	 break;
      case ASTNode.TYPE_DECLARATION :
	 nm = ((TypeDeclaration) d).getName();
	 break;
      case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
	 nm = ((VariableDeclarationFragment) d).getName();
	 break;
      case ASTNode.METHOD_INVOCATION :
	 nm = ((MethodInvocation) d).getName();
	 break;
      case ASTNode.SIMPLE_NAME :
	 nm = (SimpleName) d;
	 break;
      case ASTNode.QUALIFIED_NAME :
	 nm = (QualifiedName) d;
	 break;
      case ASTNode.FIELD_DECLARATION :
	 FieldDeclaration fd = (FieldDeclaration) d;
	 VariableDeclarationFragment fvfd = (VariableDeclarationFragment) fd.fragments().get(0);
	 return getNameFromNode(fvfd);
    }

   return nm;
}



/********************************************************************************/
/*										*/
/*	Check for getter/setter methods 					*/
/*										*/
/********************************************************************************/

public static boolean isGetMethod(MethodDeclaration d)
{
   if (d.parameters().size() > 0) return false;
   Type t = d.getReturnType2();
   if (t == null) return false;
   Block b = d.getBody();
   if (b == null) return true;		// allow abstract get methods
   if (t.isPrimitiveType()) {
      PrimitiveType pt = (PrimitiveType) t;
      if (pt.getPrimitiveTypeCode() == PrimitiveType.VOID) return false;
    }
   if (b.statements().size() != 1) return false;
   ASTNode n = (ASTNode) b.statements().get(0);
   if (n.getNodeType() != ASTNode.RETURN_STATEMENT) return false;

   return true;
}



public static boolean isSetMethod(MethodDeclaration d)
{
   int np = d.parameters().size();
   if (np == 0) return false;
   // check for void ?
   Block b = d.getBody();
   if (b == null) return true;
   if (b.statements().size() > np) return false;
   for (Object o : b.statements()) {
      ASTNode n = (ASTNode) o;
      if (n.getNodeType() != ASTNode.EXPRESSION_STATEMENT) return false;
      ExpressionStatement ex = (ExpressionStatement) n;
      if (ex.getExpression().getNodeType() != ASTNode.ASSIGNMENT) return false;
    }

   return true;
}



/********************************************************************************/
/*										*/
/*	Find AST node for given offset						*/
/*										*/
/********************************************************************************/

public static ASTNode findNodeAtOffset(ASTNode n,int offset)
{
   FindLocationVisitor vis = new FindLocationVisitor(offset);
   n.accept(vis);

   return vis.getMatch();
}


private static class FindLocationVisitor extends ASTVisitor {

   private int start_offset;
   private ASTNode best_match;

   FindLocationVisitor(int soff) {
      start_offset = soff;
      best_match = null;
    }

   ASTNode getMatch() {
      return best_match;
    }

   @Override public boolean preVisit2(ASTNode n) {
      int soff = n.getStartPosition();
      int eoff = soff + n.getLength();
      if (eoff < start_offset) return false;
      if (soff > start_offset) return false;
      if (best_match == null) best_match = n;
      else {
	 switch (n.getNodeType()) {
	    case ASTNode.JAVADOC :
	    case ASTNode.BLOCK_COMMENT :
	    case ASTNode.LINE_COMMENT :
	       break;
	    default :
	       best_match = n;
	       break;
	  }
       }
      return true;
    }

}	// end of inner class FindLocationVisitor



/********************************************************************************/
/*										*/
/*	Check for various circumstances 					*/
/*										*/
/********************************************************************************/

public static boolean isInInterface(ASTNode n)
{
   if (n == null) return false;
   for (ASTNode p = n.getParent(); p != null; p = p.getParent()) {
      if (p instanceof AbstractTypeDeclaration) {
	 if (p instanceof TypeDeclaration) {
	    TypeDeclaration td = (TypeDeclaration) p;
	    return td.isInterface();
	  }
	 else return false;
       }
    }
   return false;
}




}	// end of abstract class JcompAst




/* end of JcompAst.java */
