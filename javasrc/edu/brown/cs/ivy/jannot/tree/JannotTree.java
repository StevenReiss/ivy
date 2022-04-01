/********************************************************************************/
/*										*/
/*		JannotTree.java 						*/
/*										*/
/*	Generic mapping from Eclipse to JavaC tree\\				*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
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



package edu.brown.cs.ivy.jannot.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
// import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
// import org.eclipse.`jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.core.dom.LambdaExpression;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;

import edu.brown.cs.ivy.jannot.JannotName;


public abstract class JannotTree implements Tree,
	JannotTreeConstants
{



/********************************************************************************/
/*										*/
/*	Static creation methods 						*/
/*										*/
/********************************************************************************/

static JannotTreeJCExpression createTree(Expression n)
{
   return (JannotTreeJCExpression) createTree((ASTNode) n);
}


static JannotTreeJCStatement createTree(Statement n)
{
   return (JannotTreeJCStatement) createTree((ASTNode) n);
}


static JannotTreeJCBlock createTree(Block n)
{
   return (JannotTreeJCBlock) createTree((ASTNode) n);
}

static JannotTreeJCCatch createTree(CatchClause n)
{
   return (JannotTreeJCCatch) createTree((ASTNode) n);
}


static JannotTreeJCAnnotation createTree(Annotation n)
{
   return (JannotTreeJCAnnotation) createTree((ASTNode) n);
}

static JannotTreeJCExpression createTree(Type n)
{
   return (JannotTreeJCExpression) createTree((ASTNode) n);
}


static JannotTreeJCVariableDecl createTree(VariableDeclaration n)
{
   return (JannotTreeJCVariableDecl) createTree((ASTNode) n);
}


static List<JannotTreeJCVariableDecl> createTrees(FieldDeclaration fd)
{
   List<JannotTreeJCVariableDecl> rslt = new ArrayList<>();
   for (Object o : fd.fragments()) {
      VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
      rslt.add(createTree(vdf));
    }
   return rslt;     
}


static List<JannotTreeJCVariableDecl> createTrees(VariableDeclarationExpression vde)
{
   List<JannotTreeJCVariableDecl> rslt = new ArrayList<>();
   for (Object o : vde.fragments()) {
      VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
      rslt.add(createTree(vdf));
    }
   return rslt;     
}



static List<JannotTreeJCVariableDecl> createTrees(VariableDeclarationStatement vds)
{
   List<JannotTreeJCVariableDecl> rslt = new ArrayList<>();
   for (Object o : vds.fragments()) {
      VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
      rslt.add(createTree(vdf));
    }
   return rslt;     
}



static JannotTreeJCExpression createTypeTree(Type n)
{
   switch (n.getNodeType()) {
      case ASTNode.ARRAY_TYPE :
	 return new JannotTreeJCArrayTypeTree((ArrayType) n);
      case ASTNode.PRIMITIVE_TYPE :
	 return new JannotTreeJCPrimitiveTypeTree((PrimitiveType) n);
      case ASTNode.PARAMETERIZED_TYPE :
	 return new JannotTreeJCTypeApply((ParameterizedType) n);
      case ASTNode.INTERSECTION_TYPE :
	 return new JannotTreeJCTypeIntersection((IntersectionType) n);
      case ASTNode.UNION_TYPE :
	 return new JannotTreeJCTypeUnion((UnionType) n);
      case ASTNode.WILDCARD_TYPE :
	 return new JannotTreeJCWildcard((WildcardType) n);
      case ASTNode.QUALIFIED_TYPE :
      case ASTNode.SIMPLE_TYPE :
	 return new JannotTreeJCIdent(n);
    }
   return null;
}

static JannotTree createTree(ASTNode n)
{
   if (n == null) return null;

   switch (n.getNodeType()) {
      case ASTNode.CATCH_CLAUSE :
	 return new JannotTreeJCCatch((CatchClause) n);
      case ASTNode.COMPILATION_UNIT :
	 return new JannotTreeJCCompilationUnit((CompilationUnit) n);
      case ASTNode.IMPORT_DECLARATION :
	 return new JannotTreeJCImport((ImportDeclaration) n);
      case ASTNode.PACKAGE_DECLARATION :
	 return new JannotTreeJCPackageDecl((PackageDeclaration) n);
	
      case ASTNode.MARKER_ANNOTATION :
      case ASTNode.NORMAL_ANNOTATION :
      case ASTNode.SINGLE_MEMBER_ANNOTATION :
	 return new JannotTreeJCAnnotation((Annotation) n);
      case ASTNode.METHOD_DECLARATION :
      case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
	 return new JannotTreeJCMethodDecl((MethodDeclaration) n);
      case ASTNode.SINGLE_VARIABLE_DECLARATION :
      case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
	 return new JannotTreeJCVariableDecl((VariableDeclaration) n);
      case ASTNode.TYPE_PARAMETER :
	 return new JannotTreeJCTypeParameter((TypeParameter) n);
      case ASTNode.TYPE_DECLARATION :
      case ASTNode.TYPE_DECLARATION_STATEMENT :
      case ASTNode.ENUM_DECLARATION :
      case ASTNode.ANNOTATION_TYPE_DECLARATION :
      case ASTNode.ANONYMOUS_CLASS_DECLARATION :
	 return new JannotTreeJCClassDecl(n);
      case ASTNode.ASSERT_STATEMENT :
	 return new JannotTreeJCAssert((AssertStatement) n);
      case ASTNode.INITIALIZER :
	 return createTree(((Initializer) n).getBody());
      case ASTNode.BLOCK :
	 return new JannotTreeJCBlock((Block) n);
      case ASTNode.BREAK_STATEMENT :
	 return new JannotTreeJCBreak((BreakStatement) n);
      case ASTNode.CONTINUE_STATEMENT :
	 return new JannotTreeJCContinue((ContinueStatement) n);
      case ASTNode.DO_STATEMENT :
	 return new JannotTreeJCDoWhileLoop((DoStatement) n);
      case ASTNode.EMPTY_STATEMENT :
	 return new JannotTreeJCSkip((EmptyStatement) n);
      case ASTNode.ENHANCED_FOR_STATEMENT :
	 return new JannotTreeJCEnhancedForLoop((EnhancedForStatement) n);
      case ASTNode.FOR_STATEMENT :
	 return new JannotTreeJCForLoop((ForStatement) n);
      case ASTNode.IF_STATEMENT :
	 return new JannotTreeJCIf((IfStatement) n);
      case ASTNode.LABELED_STATEMENT :
	 return new JannotTreeJCLabeledStatement((LabeledStatement) n);
      case ASTNode.RETURN_STATEMENT :
	 return new JannotTreeJCReturn((ReturnStatement) n);
      case ASTNode.SWITCH_STATEMENT :
	 return new JannotTreeJCSwitch((SwitchStatement) n);
      case ASTNode.SWITCH_CASE :
// 	 return new JannotTreeJCCase((SwitchCase) n);
         return null;
      case ASTNode.SYNCHRONIZED_STATEMENT :
	 return new JannotTreeJCSynchronized((SynchronizedStatement) n);
      case ASTNode.THROW_STATEMENT :
	 return new JannotTreeJCThrow((ThrowStatement) n);
      case ASTNode.TRY_STATEMENT :
	 return new JannotTreeJCTry((TryStatement) n);
      case ASTNode.WHILE_STATEMENT :
	 return new JannotTreeJCWhileLoop((WhileStatement) n);
	
      case ASTNode.NAME_QUALIFIED_TYPE :
      case ASTNode.PRIMITIVE_TYPE :
      case ASTNode.QUALIFIED_TYPE :
      case ASTNode.SIMPLE_TYPE :
      case ASTNode.WILDCARD_TYPE :
	 AnnotatableType at = (AnnotatableType) n;
	 if (at.annotations().size() > 0) {
	    return new JannotTreeJCAnnotatedType(at);
	  }
	 return createTypeTree(at);
      case ASTNode.ARRAY_TYPE :
      case ASTNode.PARAMETERIZED_TYPE :
      case ASTNode.INTERSECTION_TYPE :
      case ASTNode.UNION_TYPE :
	 return createTypeTree((Type) n);
	
      case ASTNode.ARRAY_ACCESS :
	 return new JannotTreeJCArrayAccess((ArrayAccess) n);
      case ASTNode.ASSIGNMENT :
	 Assignment asg = (Assignment) n;
	 if (asg.getOperator() == Assignment.Operator.ASSIGN)
	    return new JannotTreeJCAssign(asg);
	 return new JannotTreeJCAssignOp(asg);
      case ASTNode.INFIX_EXPRESSION :
	 return new JannotTreeJCBinary((InfixExpression) n);
      case ASTNode.FIELD_ACCESS :
	 return new JannotTreeJCFieldAccess((FieldAccess) n);
      case ASTNode.QUALIFIED_NAME :
      case ASTNode.SIMPLE_NAME :
	 return new JannotTreeJCIdent(n);
      case ASTNode.INSTANCEOF_EXPRESSION :
// 	 return new JannotTreeJCInstanceOf((InstanceofExpression) n);
         return null;
      case ASTNode.NUMBER_LITERAL :
      case ASTNode.NULL_LITERAL :
      case ASTNode.BOOLEAN_LITERAL :
      case ASTNode.STRING_LITERAL :
      case ASTNode.CHARACTER_LITERAL :
	 return new JannotTreeJCLiteral((Expression) n);
      case ASTNode.ARRAY_CREATION :
	 return new JannotTreeJCNewArray((ArrayCreation) n);
      case ASTNode.PARENTHESIZED_EXPRESSION :
	 return new JannotTreeJCParens((ParenthesizedExpression) n);
      case ASTNode.CONDITIONAL_EXPRESSION :
	 return new JannotTreeJCConditional((ConditionalExpression) n);
      case ASTNode.LAMBDA_EXPRESSION :
	 return new JannotTreeJCLambda((LambdaExpression) n);
      case ASTNode.CREATION_REFERENCE :
      case ASTNode.EXPRESSION_METHOD_REFERENCE :
      case ASTNode.SUPER_METHOD_REFERENCE :
      case ASTNode.TYPE_METHOD_REFERENCE :
	 return new JannotTreeJCMemberReference((MethodReference) n);
      case ASTNode.METHOD_INVOCATION :
	 return new JannotTreeJCMethodInvocation((MethodInvocation) n);
      case ASTNode.CLASS_INSTANCE_CREATION :
	 return new JannotTreeJCNewClass((ClassInstanceCreation) n);
      case ASTNode.CAST_EXPRESSION :
	 return new JannotTreeJCTypeCast((CastExpression) n);
      case ASTNode.POSTFIX_EXPRESSION :
      case ASTNode.PREFIX_EXPRESSION :
	 return new JannotTreeJCUnary((Expression) n);
      case ASTNode.EXPRESSION_STATEMENT :
	 return new JannotTreeJCExpressionStatement(((ExpressionStatement) n).getExpression());
	
      case ASTNode.ARRAY_INITIALIZER :
      case ASTNode.CONSTRUCTOR_INVOCATION :
      case ASTNode.DIMENSION :
      case ASTNode.ENUM_CONSTANT_DECLARATION :
      case ASTNode.EXPORTS_DIRECTIVE :
      case ASTNode.FIELD_DECLARATION :
      case ASTNode.MEMBER_VALUE_PAIR :
      case ASTNode.MODIFIER :
      case ASTNode.MODULE_DECLARATION :
      case ASTNode.MODULE_MODIFIER :
      case ASTNode.OPENS_DIRECTIVE :
      case ASTNode.PROVIDES_DIRECTIVE :
      case ASTNode.REQUIRES_DIRECTIVE :
      case ASTNode.SUPER_CONSTRUCTOR_INVOCATION :
      case ASTNode.SUPER_FIELD_ACCESS :
      case ASTNode.SUPER_METHOD_INVOCATION :
      case ASTNode.THIS_EXPRESSION :
      case ASTNode.TYPE_LITERAL :
      case ASTNode.USES_DIRECTIVE :
      case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
      case ASTNode.VARIABLE_DECLARATION_STATEMENT :
	 // need to determine how each of these is handled
	 // provided they are relevant
	 break;
    }

   // how is x.class represented?
   // is qualified type done correctly as an Ident
   // handle operators with extended operands
   // handling error trees
   // dim annotations for new array from eclipse
   // let statement from eclipse
   // nested array initializers

   // missing: DirectiveTree ( ExportsTree, OpensTree, ProvidesTree, RequiresTree, UsesTree )
   //	ModuleTree

   // Missing; JJCModuleDef, JCUses, JCProvides,. JCRequires, JCOpens, JCExports

   // Possibly add JCTree.Factory


   return null;
}



static JannotName createName(Name n)
{
   if (n == null) return null;
   if (n.isSimpleName()) return new JannotName(((SimpleName) n).getIdentifier());
   return new JannotName(n.getFullyQualifiedName());
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected ASTNode ast_node;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected JannotTree(ASTNode n)
{
   ast_node = n;
}



/********************************************************************************/
/*										*/
/*	Abstract methods							*/
/*										*/
/********************************************************************************/

@Override public abstract Tree.Kind getKind();

@Override public abstract <R,D> R accept(TreeVisitor<R,D> visitor,D data);
public abstract void accept(JannotTreeVisitor v); 
public JannotTree translate(JannotTreeTranslator tt)            { return this; }




/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

public ASTNode getAstNode()
{
   return ast_node;
}



}	// end of class JannotTree




/* end of JannotTree.java */

