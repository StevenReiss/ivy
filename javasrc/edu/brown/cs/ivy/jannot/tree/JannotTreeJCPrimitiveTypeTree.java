/********************************************************************************/
/*                                                                              */
/*              JannotTreeJCPrimitiveTypeTree.java                              */
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

import javax.lang.model.type.TypeKind;

import org.eclipse.jdt.core.dom.PrimitiveType;

import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;


public class JannotTreeJCPrimitiveTypeTree extends JannotTreeJCExpression implements PrimitiveTypeTree
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotTreeJCPrimitiveTypeTree(PrimitiveType n) 
{
   super(n);
}


/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public void accept(JannotTreeVisitor v)
{
   v.visitTypeIdent(this);
}


@Override public <R,D> R accept(TreeVisitor<R,D> visitor,D arg)
{
   return visitor.visitPrimitiveType(this,arg);
}



@Override public Tree.Kind getKind()
{
   return Tree.Kind.PRIMITIVE_TYPE;
}



/********************************************************************************/
/*                                                                              */
/*      Tree methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public TypeKind getPrimitiveTypeKind()
{
   PrimitiveType.Code cd = getAstType().getPrimitiveTypeCode();
   TypeKind tk = null;
   if (cd == PrimitiveType.BYTE) tk = TypeKind.BYTE;
   else if (cd == PrimitiveType.BOOLEAN) tk = TypeKind.BOOLEAN;
   else if (cd == PrimitiveType.CHAR) tk = TypeKind.CHAR;
   else if (cd == PrimitiveType.DOUBLE) tk = TypeKind.DOUBLE;
   else if (cd == PrimitiveType.FLOAT) tk = TypeKind.FLOAT;
   else if (cd == PrimitiveType.INT) tk = TypeKind.INT;
   else if (cd == PrimitiveType.LONG) tk = TypeKind.LONG;
   else if (cd == PrimitiveType.SHORT) tk = TypeKind.SHORT;
   else if (cd == PrimitiveType.VOID) tk = TypeKind.VOID;
   return tk;
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private PrimitiveType getAstType()
{
   return (PrimitiveType) ast_node;
}



}       // end of class JannotTreeJCPrimitiveTypeTree




/* end of JannotTreeJCPrimitiveTypeTree.java */

