/********************************************************************************/
/*										*/
/*		JannotTreeVisitor.java						*/
/*										*/
/*	Internal tree visitor							*/
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



public class JannotTreeVisitor implements JannotTreeConstants
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public JannotTreeVisitor()
{ }




/********************************************************************************/
/*										*/
/*	Visitation methods							*/
/*										*/
/********************************************************************************/

public void visitAnnotatedType(JannotTreeJCAnnotatedType t)		{ visitTree(t); }
public void visitAnnotation(JannotTreeJCAnnotation t)			{ visitTree(t); }
public void visitApply(JannotTreeJCMethodInvocation t)			{ visitTree(t); }
public void visitAssert(JannotTreeJCAssert t)				{ visitTree(t); }
public void visitAssign(JannotTreeJCAssign t)                           { visitTree(t); }
public void visitAssignOp(JannotTreeJCAssignOp t)			{ visitTree(t); }
public void visitBinary(JannotTreeJCBinary t)				{ visitTree(t); }
public void visitBlock(JannotTreeJCBlock t)				{ visitTree(t); }
public void visitBreak(JannotTreeJCBreak t)				{ visitTree(t); }
public void visitCase(JannotTreeJCCase t)				{ visitTree(t); }
public void visitCatch(JannotTreeJCCatch t)				{ visitTree(t); }
public void visitClassDef(JannotTreeJCClassDecl t)			{ visitTree(t); }
public void visitConditional(JannotTreeJCConditional t) 		{ visitTree(t); }
public void visitContinue(JannotTreeJCContinue t)			{ visitTree(t); }
public void visitDoLoop(JannotTreeJCDoWhileLoop t)			{ visitTree(t); }
// public void visitErroenous(JannotTreeJCErroneous t)			{ visitTree(t); }
public void visitExec(JannotTreeJCExpressionStatement t)		{ visitTree(t); }
public void visitForeachLoop(JannotTreeJCEnhancedForLoop t)		{ visitTree(t); }
public void visitForLoop(JannotTreeJCForLoop t) 			{ visitTree(t); }
public void visitIdent(JannotTreeJCIdent t)				{ visitTree(t); }
public void visitIf(JannotTreeJCIf t)					{ visitTree(t); }
public void visitImport(JannotTreeJCImport t)				{ visitTree(t); }
public void visitIndexed(JannotTreeJCArrayAccess t)			{ visitTree(t); }
public void visitLabelled(JannotTreeJCLabeledStatement t)		{ visitTree(t); }
public void visitLambda(JannotTreeJCLambda t)				{ visitTree(t); }
// public void visitLetExpr(JannotTreeJCLexExpr t)			{ visitTree(t); }
public void visitLiteral(JannotTreeJCLiteral t) 			{ visitTree(t); }
public void visitMethodDef(JannotTreeJCMethodDecl t)			{ visitTree(t); }
public void visitModifiers(JannotTreeJCModifiers t)			{ visitTree(t); }
public void visitNewArray(JannotTreeJCNewArray t)			{ visitTree(t); }
public void visitNewClass(JannotTreeJCNewClass t)			{ visitTree(t); }
public void visitParens(JannotTreeJCParens t)				{ visitTree(t); }
public void visitPackageDef(JannotTreeJCPackageDecl t)                  { visitTree(t); }
public void visitReference(JannotTreeJCMemberReference t)		{ visitTree(t); }
public void visitReturn(JannotTreeJCReturn t)				{ visitTree(t); }
public void visitSelect(JannotTreeJCFieldAccess t)			{ visitTree(t); }
public void visitSkip(JannotTreeJCSkip t)				{ visitTree(t); }
public void visitSwitch(JannotTreeJCSwitch t)				{ visitTree(t); }
public void visitSynchronized(JannotTreeJCSynchronized t)		{ visitTree(t); }
public void visitThrow(JannotTreeJCThrow t)				{ visitTree(t); }
public void visitTopLevel(JannotTreeJCCompilationUnit t)		{ visitTree(t); }
public void visitTry(JannotTreeJCTry t) 				{ visitTree(t); }
public void visitTypeApply(JannotTreeJCTypeApply t)			{ visitTree(t); }
public void visitTypeArray(JannotTreeJCArrayTypeTree t) 		{ visitTree(t); }
// public void visitTypeBoundKind(JannotTreeJCTypeBoundKind t)		{ visitTree(t); }
public void visitTypeCast(JannotTreeJCTypeCast t)			{ visitTree(t); }
public void visitTypeIdent(JannotTreeJCPrimitiveTypeTree t)		{ visitTree(t); }
public void visitTypeIntersection(JannotTreeJCTypeIntersection t)	{ visitTree(t); }
public void visitTypeParameter(JannotTreeJCTypeParameter t)		{ visitTree(t); }
public void visitTypeTest(JannotTreeJCInstanceOf t)			{ visitTree(t); }
public void visitTypeUnion(JannotTreeJCTypeUnion t)			{ visitTree(t); }
public void visitUnary(JannotTreeJCUnary t)				{ visitTree(t); }
public void visitVarDef(JannotTreeJCVariableDecl t)			{ visitTree(t); }
public void visitWhileLoop(JannotTreeJCWhileLoop t)			{ visitTree(t); }
public void visitWildcard(JannotTreeJCWildcard t)			{ visitTree(t); }



/********************************************************************************/
/*                                                                              */
/*      Generic method                                                          */
/*                                                                              */
/********************************************************************************/

public void visitTree(JannotTree t)		
{ 
   throw new Error("Should be called -- user did not override " + t.getClass());
}




}	// end of class JannotTreeVisitor




/* end of JannotTreeVisitor.java */

