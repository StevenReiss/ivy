/********************************************************************************/
/*                                                                              */
/*              JannotTreeFactory.java                                          */
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



package edu.brown.cs.ivy.jannot.tree;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;

import edu.brown.cs.ivy.jannot.JannotName;

public class JannotTreeFactory implements JannotTreeConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private AST     build_ast;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeFactory()
{ }


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

void setAST(AST ast)
{
   build_ast = ast;
}



/********************************************************************************/
/*                                                                              */
/*      Build methods                                                           */
/*                                                                              */
/********************************************************************************/

public JannotTreeJCCompilationUnit TopLevel(List<JannotTree> defs)
{
   if (build_ast == null) return null;
   return null;
}



public JannotTreeJCPackageDecl PackageDecl(List<JannotTreeJCAnnotation> annots,
      JannotTreeJCExpression pid)
{ 
   return null;
}


public JannotTreeJCImport Import(JannotTreeJCTry qualid,boolean isstatic)
{ 
   return null;
}

public JannotTreeJCClassDecl ClassDef(JannotTreeJCModifiers mods,
      JannotName name,
      List<JannotTreeJCTypeParameter> typarams,
      JannotTreeJCExpression extending,
      List<JannotTreeJCExpression> implementing,
      List<JannotTree> defs) 
{
   return null;
}



public JannotTreeJCMethodDecl MethodDef(JannotTreeJCModifiers mods,
      JannotName name,
      JannotTreeJCExpression restype,
      List<JannotTreeJCTypeParameter> typarams,
      JannotTreeJCVariableDecl recvparam,
      List<JannotTreeJCVariableDecl> params,
      List<JannotTreeJCExpression> thrown,
      JannotTreeJCBlock body,
      JannotTreeJCExpression defaultValue)
{
   return null;
}



public JannotTreeJCVariableDecl VarDef(JannotTreeJCModifiers mods,
      JannotName name,
      JannotTreeJCExpression vartype,
      JannotTreeJCExpression init)
{
   return null;
}



public JannotTreeJCSkip Skip()
{
   return null;
}



public JannotTreeJCBlock Block(long flags, List<JannotTreeJCStatement> stats)
{
   return null;
}



public JannotTreeJCDoWhileLoop DoLoop(JannotTreeJCStatement body,
      JannotTreeJCExpression cond)
{
   return null;
}



public JannotTreeJCWhileLoop WhileLoop(JannotTreeJCExpression cond, JannotTreeJCStatement body)
{
   return null;
}



public JannotTreeJCForLoop ForLoop(List<JannotTreeJCStatement> init,
      JannotTreeJCExpression cond,
      List<JannotTreeJCExpressionStatement> step,
      JannotTreeJCStatement body)
{
   return null;
}



public JannotTreeJCEnhancedForLoop ForeachLoop(JannotTreeJCVariableDecl var, JannotTreeJCExpression expr, JannotTreeJCStatement body)
{
   return null;
}



public JannotTreeJCLabeledStatement Labelled(JannotName label, JannotTreeJCStatement body)
{
   return null;
}



public JannotTreeJCSwitch Switch(JannotTreeJCExpression selector, List<JannotTreeJCCase> cases)
{
   return null;
}



public JannotTreeJCCase Case(JannotTreeJCExpression pat, List<JannotTreeJCStatement> stats)
{
   return null;
}



public JannotTreeJCSynchronized Synchronized(JannotTreeJCExpression lock, JannotTreeJCBlock body)
{
   return null;
}



public JannotTreeJCTry Try(JannotTreeJCBlock body, List<JannotTreeJCCatch> catchers, JannotTreeJCBlock finalizer)
{
   return null;
}



public JannotTreeJCTry Try(List<JannotTree> resources,
      JannotTreeJCBlock body,
      List<JannotTreeJCCatch> catchers,
      JannotTreeJCBlock finalizer)
{
   return null;
}



public JannotTreeJCCatch Catch(JannotTreeJCVariableDecl param, JannotTreeJCBlock body)
{
   return null;
}



public JannotTreeJCConditional Conditional(JannotTreeJCExpression cond,
      JannotTreeJCExpression thenpart,
      JannotTreeJCExpression elsepart)
{
   return null;
}



public JannotTreeJCIf If(JannotTreeJCExpression cond, JannotTreeJCStatement thenpart, JannotTreeJCStatement elsepart)
{
   return null;
}



public JannotTreeJCExpressionStatement Exec(JannotTreeJCExpression expr)
{
   return null;
}



public JannotTreeJCBreak Break(JannotName label)
{
   return null;
}



public JannotTreeJCContinue Continue(JannotName label)
{
   return null;
}



public JannotTreeJCReturn Return(JannotTreeJCExpression expr)
{
   return null;
}



public JannotTreeJCThrow Throw(JannotTreeJCExpression expr)
{
   return null;
}



public JannotTreeJCAssert Assert(JannotTreeJCExpression cond, JannotTreeJCExpression detail)
{
   return null;
}



public JannotTreeJCMethodInvocation Apply(List<JannotTreeJCExpression> typeargs,
      JannotTreeJCExpression fn,
      List<JannotTreeJCExpression> args)
{
   return null;
}



public JannotTreeJCNewClass NewClass(JannotTreeJCExpression encl,
      List<JannotTreeJCExpression> typeargs,
      JannotTreeJCExpression clazz,
      List<JannotTreeJCExpression> args,
      JannotTreeJCClassDecl def)
{
   return null;
}



public JannotTreeJCNewArray NewArray(JannotTreeJCExpression elemtype,
      List<JannotTreeJCExpression> dims,
      List<JannotTreeJCExpression> elems)
{
   return null;
}



public JannotTreeJCParens Parens(JannotTreeJCExpression expr)
{
   return null;
}



public JannotTreeJCAssign Assign(JannotTreeJCExpression lhs, JannotTreeJCExpression rhs)
{
   return null;
}



public JannotTreeJCAssignOp Assignop(JannotTreeTag opcode, JannotTree lhs, JannotTree rhs)
{
   return null;
}



public JannotTreeJCUnary Unary(JannotTreeTag opcode, JannotTreeJCExpression arg)
{
   return null;
}



public JannotTreeJCBinary Binary(JannotTreeTag opcode, JannotTreeJCExpression lhs, JannotTreeJCExpression rhs)
{
   return null;
}



public JannotTreeJCTypeCast TypeCast(JannotTree expr, JannotTreeJCExpression type)
{
   return null;
}



public JannotTreeJCInstanceOf TypeTest(JannotTreeJCExpression expr, JannotTree clazz)
{
   return null;
}



public JannotTreeJCArrayAccess Indexed(JannotTreeJCExpression indexed, JannotTreeJCExpression index)
{
   return null;
}



public JannotTreeJCFieldAccess Select(JannotTreeJCExpression selected, JannotName selector)
{
   return null;
}



public JannotTreeJCIdent Ident(JannotName idname)
{
   return null;
}



// public JannotTreeJCLiteral Literal(TypeTag tag, Object value)
// {
   // return null;
// }



// public JannotTreeJCPrimitiveTypeTree TypeIdent(TypeTag typetag)
// {
   // return null;
// }


public JannotTreeJCArrayTypeTree TypeArray(JannotTreeJCExpression elemtype)
{
   return null;
}



public JannotTreeJCTypeApply TypeApply(JannotTreeJCExpression clazz, List<JannotTreeJCExpression> arguments)
{
   return null;
}



public JannotTreeJCTypeParameter TypeParameter(JannotName name, List<JannotTreeJCExpression> bounds)
{
   return null;
}



// public JannotTreeJCWildcard Wildcard(TypeBoundKind kind, JannotTree type)
// {
   // return null;
// }



// public TypeBoundKind TypeBoundKind(BoundKind kind)
// {
   // return null;
// }



public JannotTreeJCAnnotation Annotation(JannotTree annotationType, List<JannotTreeJCExpression> args)
{
   return null;
}



public JannotTreeJCModifiers Modifiers(long flags, List<JannotTreeJCAnnotation> annotations)
{
   return null;
}




}       // end of class JannotTreeFactory




/* end of JannotTreeFactory.java */

