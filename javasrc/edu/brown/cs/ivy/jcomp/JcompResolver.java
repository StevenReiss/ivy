/********************************************************************************/
/*										*/
/*		JcompResolver.java						*/
/*										*/
/*	Class to handle name resolution for Java files				*/
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



import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;


class JcompResolver implements JcompConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcompTyper	 type_data;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompResolver(JcompTyper typer)
{
   type_data = typer;
   JcompType ot = typer.findSystemType("java.lang.Object");
   ot.defineAll(typer);
}



/********************************************************************************/
/*										*/
/*	Top level methods							*/
/*										*/
/********************************************************************************/

void resolveNames(ASTNode n)
{
   DefPass dp = new DefPass();

   n.accept(dp);

   RefPass rp = new RefPass();

   n.accept(rp);

   if (rp.getNeedRescan()) n.accept(rp);

   JcompAst.setTyper(n,type_data);
}




void resolveNames(JcompProjectImpl root)
{
   for (ASTNode cu : root.getTrees()) {
      DefPass dp = new DefPass();
      cu.accept(dp);
    }

   for (ASTNode cu : root.getTrees()) {
      RefPass rp = new RefPass();
      cu.accept(rp);
      if (rp.getNeedRescan()) cu.accept(rp);
      JcompAst.setTyper(cu,type_data);
    }
}


void resolveNames(List<ASTNode> nodes)
{
   DefPass dp = new DefPass();
   for (ASTNode n : nodes) {
      n.accept(dp);
    }
   RefPass rp = new RefPass();
   for (ASTNode n : nodes) {
      n.accept(rp);
      JcompAst.setTyper(n,type_data);
    }
   if (rp.getNeedRescan()) {
      for (ASTNode n : nodes) {
	 n.accept(rp);
	 JcompAst.setTyper(n,type_data);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

private JcompType findType(String nm)
{
   return type_data.findType(nm);
}



private JcompType findArrayType(JcompType jt)
{
   return type_data.findArrayType(jt);
}



private JcompType findNumberType(String n)
{
   boolean ishex = false;
   boolean isoctal = false;
   boolean isreal = false;
   String type = null;

   for (int i = 0; i < n.length(); ++i) {
      switch (n.charAt(i)) {
	 case '.' :
	 case 'E' :
	 case 'e' :
	    if (!ishex) isreal = true;
	    break;
	 case 'l' :
	 case 'L' :
	    type = "long";
	    break;
	 case 'f' :
	 case 'F' :
	    if (!ishex) type = "float";
	    break;
	 case 'd' :
	 case 'D' :
	    if (!ishex) type = "double";
	    break;
	 case 'X' :
	 case 'x' :
	    ishex = true;
	    isoctal = false;
	    break;
	 case '0' :
	    if (i == 0) isoctal = true;
	    break;
       }
    }

   if (type == null) {
      if (isreal) type = "double";
      else {
	 type = "int";
	 try {
	    int base = 10;
	    if (isoctal) base = 8;
	    else if (ishex) {
	       base = 16;
	       if (n.startsWith("0x")) n = n.substring(2);
	     }
	    int v = Integer.parseInt(n,base);
	    if (v >= 0) {
	       if (v <= Byte.MAX_VALUE) type = "byte";
	       else if (v <= Short.MAX_VALUE) type = "short";
	     }
	    else {
	       if (v >= Byte.MIN_VALUE) type = "byte";
	       else if (v >= Short.MIN_VALUE) type = "short";
	     }
	  }
	 catch (NumberFormatException e) { }
       }
    }

   return type_data.findType(type);
}




/********************************************************************************/
/*										*/
/*	DefPass -- definition pass						*/
/*										*/
/********************************************************************************/

private class DefPass extends ASTVisitor {

   private JcompScope cur_scope;

   DefPass() {
      cur_scope = new JcompScopeAst(null);
    }

   public @Override boolean visit(AnnotationTypeMemberDeclaration n)	{ return false; }
   public @Override boolean visit(AnnotationTypeDeclaration n)		{ return false; }
   public @Override boolean visit(PackageDeclaration n) 		{ return false; }

   public @Override void preVisit(ASTNode n) {
      switch (n.getNodeType()) {
	 case ASTNode.TYPE_DECLARATION :
	 case ASTNode.ENUM_DECLARATION :
	 case ASTNode.ANONYMOUS_CLASS_DECLARATION :
	 case ASTNode.INITIALIZER :
	 case ASTNode.BLOCK :
	 case ASTNode.CATCH_CLAUSE :
	 case ASTNode.FOR_STATEMENT :
	 case ASTNode.ENHANCED_FOR_STATEMENT :
	 case ASTNode.SWITCH_STATEMENT :
	 case ASTNode.ANNOTATION_TYPE_DECLARATION :
	    cur_scope = new JcompScopeAst(cur_scope);
	    JcompType jt = JcompAst.getJavaType(n);
	    if (jt != null) jt.setScope(cur_scope);
	    JcompAst.setJavaScope(n,cur_scope);
	    break;
       }
    }

   public @Override void postVisit(ASTNode n) {
      JcompScope s = JcompAst.getJavaScope(n);
      if (s != null) cur_scope = s.getParent();
    }


   public @Override boolean visit(ImportDeclaration n) {
      Name nm = n.getName();
      if (nm.isQualifiedName()) {
         QualifiedName qn = (QualifiedName) nm;
         JcompType jt1 = JcompAst.getJavaType(qn);
         JcompType jt = JcompAst.getJavaType(qn.getQualifier());
         String inm = qn.getName().getIdentifier();
         if (jt != null && jt1 == null) {
            jt.defineAll(type_data);
            List<JcompSymbol> defs = jt.lookupStatics(type_data,inm);
            if (defs != null) {
               for (JcompSymbol js : defs) {
                  if (js.isMethodSymbol()) cur_scope.defineMethod(js);
                  else cur_scope.defineVar(js);
                }
             }
          }
         else if (n.isOnDemand() && n.isStatic() && jt1 != null) {
            jt1.defineAll(type_data);
            List<JcompSymbol> defs = jt1.lookupStatics(type_data,null);
            if (defs != null) {
               for (JcompSymbol js : defs) {
                  if (js.isMethodSymbol()) {
                     if (js.isConstructorSymbol()) continue;
                     cur_scope.defineMethod(js);
                   }
                  else cur_scope.defineVar(js);
                }
             }
          }
       }
      return false;
    }

   public @Override boolean visit(MethodDeclaration n) {
      String nm;
      if (n.isConstructor()) nm = "<init>";
      else nm = n.getName().getIdentifier();
      JcompSymbol jm = cur_scope.defineMethod(nm,n);
      cur_scope = new JcompScopeAst(cur_scope);
      JcompAst.setJavaScope(n,cur_scope);
      JcompAst.setDefinition(n,jm);
      JcompAst.setDefinition(n.getName(),jm);
      return true;
    }

   public @Override void endVisit(MethodDeclaration n) {
      cur_scope = cur_scope.getParent();
    }

   public @Override boolean visit(LambdaExpression n) {
      JcompSymbol js = cur_scope.defineLambda(n);
      JcompAst.setDefinition(n,js);
      cur_scope = new JcompScopeAst(cur_scope);
      JcompAst.setJavaScope(n,cur_scope);
      return true;
    }

   public @Override boolean visit(CreationReference n) {
      JcompSymbol js = cur_scope.defineReference(n);
      JcompAst.setDefinition(n,js);
      return true;
   }

   public @Override boolean visit(TypeMethodReference n) {
      JcompSymbol js = cur_scope.defineReference(n);
      JcompAst.setDefinition(n,js);
      return true;
   }

   public @Override boolean visit(ExpressionMethodReference n) {
      JcompSymbol js = cur_scope.defineReference(n);
      JcompAst.setDefinition(n,js);
      return true;
   }

   public @Override boolean visit(SuperMethodReference n) {
      JcompSymbol js = cur_scope.defineReference(n);
      JcompAst.setDefinition(n,js);
      return true;
   }

   public @Override void endVisit(LambdaExpression n) {
      cur_scope = cur_scope.getParent();
    }

   public @Override void endVisit(CreationReference n) {
      JcompSymbol js = cur_scope.defineReference(n);
      JcompAst.setDefinition(n,js);
    }

   public @Override boolean visit(SingleVariableDeclaration n) {
      JcompSymbol js = JcompSymbol.createSymbol(n,type_data);
      cur_scope.defineVar(js);
      JcompAst.setDefinition(n,js);
      JcompAst.setDefinition(n.getName(),js);
      return true;
    }

   public @Override boolean visit(VariableDeclarationFragment n) {
      JcompSymbol js = JcompSymbol.createSymbol(n,type_data);
      cur_scope.defineVar(js);
      JcompAst.setDefinition(n,js);
      JcompAst.setDefinition(n.getName(),js);
      return true;
    }

   public @Override boolean visit(EnumConstantDeclaration n) {
      JcompSymbol js = JcompSymbol.createSymbol(n);
      cur_scope.defineVar(js);
      JcompAst.setDefinition(n,js);
      return true;
    }

   public @Override boolean visit(LabeledStatement n) {
      JcompSymbol js = JcompSymbol.createSymbol(n);
      cur_scope.defineVar(js);
      JcompAst.setDefinition(n,js);
      return true;
    }

   public @Override void endVisit(TypeDeclaration n) {
      JcompType jt = JcompAst.getJavaType(n);
      if (jt == null) return;
      JcompAst.setJavaType(n.getName(),jt);
      JcompSymbol js = jt.getDefinition();
      if (js == null) js = JcompSymbol.createSymbol(n);
      cur_scope.getParent().defineVar(js);
      JcompAst.setDefinition(n,js);
      JcompAst.setDefinition(n.getName(),js);
      if (jt.needsOuterClass()) {
	 // define field symbol for this$0
	 JcompSymbol thisjs = JcompSymbol.createNestedThis(jt,jt.getOuterType());
	 jt.getScope().defineVar(thisjs);
       }
    }

   public @Override void endVisit(AnnotationTypeDeclaration n) {
      JcompType jt = JcompAst.getJavaType(n);
      if (jt == null) return;
      JcompAst.setJavaType(n.getName(),jt);
      JcompSymbol js = jt.getDefinition();
      if (js == null) js = JcompSymbol.createSymbol(n);
      cur_scope.getParent().defineVar(js);
      JcompAst.setDefinition(n,js);
      JcompAst.setDefinition(n.getName(),js);
    }

   public @Override void endVisit(EnumDeclaration n) {
      JcompType jt = JcompAst.getJavaType(n);
      JcompAst.setJavaType(n.getName(),jt);
      JcompSymbol js = jt.getDefinition();
      if (js == null) js = JcompSymbol.createSymbol(n);
      cur_scope.getParent().defineVar(js);
      JcompAst.setDefinition(n,js);
      JcompAst.setDefinition(n.getName(),js);
    }

   public @Override void endVisit(Initializer n) {
      // TODO: create static initializer name
    }

}	// end of subclass DefPass





/********************************************************************************/
/*										*/
/*	RefPass -- handle references						*/
/*										*/
/********************************************************************************/

private class RefPass extends ASTVisitor {

   private JcompScope cur_scope;
   private JcompType cur_type;
   private Stack<JcompType> outer_types;
   private boolean need_rescan;

   RefPass() {
      cur_scope = null;
      cur_type = null;
      outer_types = new Stack<JcompType>();
      need_rescan = false;
    }

   boolean getNeedRescan()						{ return need_rescan; }

   public @Override boolean visit(AnnotationTypeMemberDeclaration n)	{ return false; }
   public @Override boolean visit(AnnotationTypeDeclaration n)		{ return false; }
   public @Override boolean visit(PackageDeclaration n) 		{ return false; }
   public @Override boolean visit(ImportDeclaration n)			{ return false; }
   public @Override boolean visit(ArrayType n)				{ return true; }
   public @Override boolean visit(ParameterizedType n)			{ return false; }
   public @Override boolean visit(PrimitiveType n)			{ return false; }
   public @Override boolean visit(QualifiedType n)			{ return true; }
   public @Override boolean visit(NameQualifiedType n)			{ return true; }
   public @Override boolean visit(SimpleType n) 			{ return true; }
   public @Override boolean visit(WildcardType n)			{ return false; }
   public @Override boolean visit(UnionType n)				{ return true; }
   public @Override boolean visit(IntersectionType n)			{ return true; }
   public @Override boolean visit(NormalAnnotation n)			{ return false; }
   public @Override boolean visit(SingleMemberAnnotation n)		{ return false; }

   public @Override void preVisit(ASTNode n) {
      JcompScope s = JcompAst.getJavaScope(n);
      if (s != null) cur_scope = s;
    }

   public @Override void postVisit(ASTNode n) {
      JcompScope s = JcompAst.getJavaScope(n);
      if (s != null) cur_scope = s.getParent();
    }

   public @Override void endVisit(BooleanLiteral n) {
      JcompAst.setExprType(n,findType("boolean"));
    }

   public @Override void endVisit(CharacterLiteral n) {
      JcompAst.setExprType(n,findType("char"));
    }

   public @Override void endVisit(NullLiteral n) {
      JcompAst.setExprType(n,findType(TYPE_ANY_CLASS));
    }

   public @Override void endVisit(NumberLiteral n) {
      JcompType t = findNumberType(n.getToken());
      JcompAst.setExprType(n,t);
    }

   public @Override void endVisit(StringLiteral n) {
      JcompAst.setExprType(n,findType("java.lang.String"));
    }

   public @Override void endVisit(TypeLiteral n) {
      JcompType t0 = findType("java.lang.Class");
      JcompType t1 = JcompAst.getJavaType(n.getType());
      if (t1 != null && !t1.isErrorType()) {
	 List<JcompType> argl = new ArrayList<>();
	 argl.add(t1);
	 JcompType t2 = JcompType.createParameterizedType(t0,argl,type_data);
	 if (t2 != null) t0 = t2;
       }
      JcompAst.setExprType(n,t0);
    }

   public @Override boolean visit(FieldAccess n) {
      n.getExpression().accept(this);
      JcompType t = JcompAst.getExprType(n.getExpression());
      if (t == null) {
         t = JcompType.createErrorType();
         JcompAst.setExprType(n.getExpression(),t);
       }
      JcompSymbol js = null;
      if (t != null) js = t.lookupField(type_data,n.getName().getIdentifier());
      if (js == null && t != null && (t.isArrayType() || t.isErrorType()) &&
             n.getName().getIdentifier().equals("length")) {
         JcompAst.setExprType(n,findType("int"));
         return false;
       }
      if (js == null) {
         JcompAst.setExprType(n,findType(TYPE_ERROR));
       }
      else {
         JcompAst.setReference(n.getName(),js);
         JcompType jt = js.getType();
         JcompAst.setExprType(n,jt);
       }
      return false;
    }

   public @Override boolean visit(SuperFieldAccess n) {
      Name qn = n.getQualifier();
      if (qn != null) qn.accept(this);
      JcompSymbol js = null;
      if (cur_type != null) {
	 JcompType jt = cur_type.getSuperType();
	 if (jt != null) js = jt.lookupField(type_data,n.getName().getIdentifier());
       }
      if (js == null) {
	 JcompAst.setExprType(n,findType(TYPE_ERROR));
       }
      else {
	 JcompAst.setReference(n.getName(),js);
	 JcompType t = js.getType();
	 JcompAst.setExprType(n,t);
       }
      return false;
    }

   public @Override boolean visit(QualifiedName n) {
      JcompType nt = JcompAst.getJavaType(n);
      if (nt != null) {
         JcompAst.setExprType(n,nt);
         return false;
       }
      Name qn = n.getQualifier();
      JcompType qt = JcompAst.getJavaType(qn);
      if (qt == null) {
         qn.accept(this);
         qt = JcompAst.getExprType(qn);
       }
      JcompSymbol typesym = qt.getDefinition();
      if (typesym != null && JcompAst.getReference(qn) == null) {
         JcompAst.setReference(qn,typesym);
       }
      JcompSymbol js = null;
      qt.defineAll(type_data);
      if (qt != null) js = qt.lookupField(type_data,n.getName().getIdentifier());
      if (js == null && qt != null && (qt.isArrayType() || qt.isErrorType()) &&
             n.getName().getIdentifier().equals("length")) {
         JcompAst.setExprType(n,findType("int"));
         return false;
       }
      else if (js == null) {
         JcompAst.setExprType(n,findType(TYPE_ERROR));
       }
      else {
         JcompAst.setReference(n.getName(),js);
         JcompType t = js.getType();
         JcompAst.setExprType(n,t);
         JcompAst.setExprType(n.getName(),t);
       }
      return false;
    }

   public @Override void endVisit(SimpleName n) {
      JcompSymbol js = JcompAst.getReference(n);
      if (js != null) {
         JcompAst.setExprType(n,js.getType());
         return;
       }
      js = JcompAst.getDefinition(n);
      if (js != null) {
         JcompAst.setExprType(n,js.getType());
         JcompAst.setReference(n,js);
         return;
       }
      JcompType jt = JcompAst.getJavaType(n);
      if (jt != null) {
         JcompAst.setExprType(n,jt);
         if (js == null && jt.getDefinition() != null) {
            JcompAst.setReference(n,jt.getDefinition());
          }
       }
   
      if (cur_scope != null) {
         String name = n.getIdentifier();
         JcompSymbol d = cur_scope.lookupVariable(name);
         if (d == null && cur_type != null) {
            d = cur_type.lookupField(type_data,name);
          }
         if (d == null) {
            if (jt == null) {
               JcompAst.setExprType(n,findType(TYPE_ERROR));
             }
            else {
               d = jt.getDefinition();
               if (d != null) JcompAst.setReference(n,d);
             }
          }
         else {
            JcompAst.setReference(n,d);
            JcompType t = d.getType();
            JcompAst.setExprType(n,t);
          }
       }
    }


   public @Override void endVisit(SimpleType t) {
      Name n = t.getName();
      JcompType jt = JcompAst.getExprType(n);
      if (jt == null) jt = findType(TYPE_ERROR);
      JcompAst.setExprType(t,jt);
   }

   public @Override boolean visit(MethodInvocation n) {
      JcompType bt = cur_type;
      Expression e = n.getExpression();
      boolean isstatic = false;
      if (e != null) {
         e.accept(this);
         bt = JcompAst.getJavaType(e);
         if (bt == null) bt = JcompAst.getExprType(e);
         else isstatic = true;
       }
      List<JcompType> targs = null;
      if (n.typeArguments().size() > 0) {
         targs = new ArrayList<>();
         for (Object o : n.typeArguments()) {
            Type tat = (Type) o;
            targs.add(JcompAst.getJavaType(tat));
          }
       }
      List<JcompType> atyp = buildArgumentList(n.arguments(),true);
      lookupMethod(bt,atyp,n,n.getName(),null,isstatic,false,targs);
      // might want to use outer types if this failed
   
      return false;
    }

   public @Override boolean visit(SuperMethodInvocation n) {
      JcompType bt = null;
      if (cur_type != null) bt = cur_type.getSuperType();
      if (bt == null) bt = type_data.findSystemType("java.lang.Object");
      Name nn = n.getQualifier();
      if (nn != null) {
	 nn.accept(this);
	 bt = JcompAst.getJavaType(nn);
	 if (bt == null) bt = JcompAst.getExprType(nn);
	 if (bt != null) bt = bt.getSuperType();
       }
      List<JcompType> targs = null;
      if (n.typeArguments().size() > 0) {
	 targs = new ArrayList<>();
	 for (Object o : n.typeArguments()) {
	    Type tat = (Type) o;
	    targs.add(JcompAst.getJavaType(tat));
	  }
       }
      List<JcompType> atyp = buildArgumentList(n.arguments(),true);

      lookupMethod(bt,atyp,n,n.getName(),null,false,atyp.size() == 0,targs);

      return false;
    }

   public @Override void endVisit(ClassInstanceCreation n) {
      JcompType xt = null;
      Expression e = n.getExpression();
      if (e != null) {
         xt = JcompAst.getJavaType(e);
         if (xt == null) xt = JcompAst.getExprType(e);
       }
   
      JcompType bt = JcompAst.getJavaType(n.getType());
      if (bt == null) {
         bt = JcompType.createErrorType();
         JcompAst.setJavaType(n.getType(),bt);
       }
      List<JcompType> targs = null;
      if (n.typeArguments().size() > 0) {
         targs = new ArrayList<>();
         for (Object o : n.typeArguments()) {
            Type tat = (Type) o;
            targs.add(JcompAst.getJavaType(tat));
          }
       }
      List<JcompType> atys = buildArgumentList(n.arguments(),false);
      boolean dfltcnst = atys.size() == 0;
      if (bt.needsOuterClass()) {
         JcompType oty = bt.getOuterType();
         if (oty != null) atys.add(0,oty);
       }
   
      JcompAst.setExprType(n,bt);		      // set default type
      lookupMethod(bt,atys,n,null,"<init>",false,dfltcnst,targs);    // this can reset the type
   }

   public @Override void endVisit(ConstructorInvocation n) {
      List<JcompType> atys = buildArgumentList(n.arguments(),false);
      if (cur_type.needsOuterClass()) {
	 JcompType oty = cur_type.getOuterType();
	 if (oty != null) atys.add(0,oty);
       }
      JcompAst.setExprType(n,cur_type);  // set type, will be reset on error
      List<JcompType> targs = null;
      if (n.typeArguments().size() > 0) {
	 targs = new ArrayList<>();
	 for (Object o : n.typeArguments()) {
	    Type tat = (Type) o;
	    targs.add(JcompAst.getJavaType(tat));
	  }
       }
      lookupMethod(cur_type,atys,n,null,"<init>",false,atys.size() == 0,targs);
    }

   public @Override void endVisit(SuperConstructorInvocation n) {
      List<JcompType> atys = buildArgumentList(n.arguments(),false);
      JcompType  bt = null;
      if (cur_type != null) bt = cur_type.getSuperType();
      if (bt == null) bt = findType("java.lang.Object");
      if (bt.needsOuterClass()) {
         JcompType oty = bt.getOuterType();
         if (oty != null) atys.add(0,oty);
       }
      List<JcompType> targs = null;
      if (n.typeArguments().size() > 0) {
         targs = new ArrayList<>();
         for (Object o : n.typeArguments()) {
            Type tat = (Type) o;
            targs.add(JcompAst.getJavaType(tat));
          }
       }
      lookupMethod(bt,atys,n,null,"<init>",false,atys.size() == 0,targs);
      if (JcompAst.getReference(n) != null)
         JcompAst.setExprType(n,cur_type);
      else if (JcompAst.getExprType(n) == null) 
         JcompAst.setExprType(n,findType(TYPE_ERROR));
    }

   public @Override void endVisit(ArrayAccess n) {
      JcompType t = JcompAst.getExprType(n.getArray());
      if (t != null && !t.isErrorType()) t = t.getBaseType();
      if (t == null) t = findType(TYPE_ERROR);
      JcompAst.setExprType(n,t);
    }

   public @Override void endVisit(ArrayCreation n) {
      JcompType bt = JcompAst.getJavaType(n.getType());

      for (Object o : n.dimensions()) {
	 Expression e = (Expression) o;
	 JcompType jtyp = JcompAst.getExprType(e);
	 if (jtyp.isErrorType()) continue;
	 if (jtyp.isIntType()) continue;
	 if (jtyp.isNumericType()) continue;
	 bt = findType(TYPE_ERROR);
	 break;
       }
      JcompAst.setExprType(n,bt);
    }

   public @Override void endVisit(ArrayInitializer n) {
      JcompType bt = findType(TYPE_ANY_CLASS);
      for (Iterator<?> it = n.expressions().iterator(); it.hasNext(); ) {
         Expression e = (Expression) it.next();
         JcompType xbt = JcompAst.getExprType(e);
         if (xbt != null) {
            bt = xbt;
            break;
          }
       }
      JcompType rslt = findArrayType(bt);
      if (n.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
         ASTNode gp = n.getParent().getParent();
         JcompType tgtty = null;
         switch (gp.getNodeType()) {
            case ASTNode.FIELD_DECLARATION :
               FieldDeclaration fd = (FieldDeclaration) gp;
               tgtty = JcompAst.getJavaType(fd.getType());
               break;
            case ASTNode.VARIABLE_DECLARATION_STATEMENT :
               VariableDeclarationStatement vds = (VariableDeclarationStatement) gp;
               tgtty = JcompAst.getJavaType(vds.getType());
               break;
          }
         if (tgtty != null && tgtty.isArrayType()) {
            JcompType tgtbase = tgtty.getBaseType();
            if (bt.isCompatibleWith(tgtbase)) {
               rslt = tgtty;
             }
          }
       }
      JcompAst.setExprType(n,rslt);
    }

   public @Override void endVisit(Assignment n) {
      JcompType b = JcompAst.getExprType(n.getRightHandSide());
      JcompType b1 = JcompAst.getExprType(n.getLeftHandSide());
      if (b != null && b.isAssignCompatibleWith(b1)) ;
      else if (b1 != null && b1.isErrorType()) ;
      else {
	 JcompType str = findType("java.lang.String");
	 if (b1 == str && n.getOperator() == Assignment.Operator.PLUS_ASSIGN)
	    b = str;
	 else
	    b = findType(TYPE_ERROR);
       }
      JcompAst.setExprType(n,b);
    }

   public @Override void endVisit(VariableDeclarationFragment n) {
      if (n.getInitializer() == null) return;
      JcompType b = JcompAst.getExprType(n.getInitializer());
      if (b != null && b.isErrorType())
         return;
      JcompSymbol def = JcompAst.getDefinition(n);
      if (def != null) {
         JcompType b1 = def.getType();
         if (b1 != null) {
            b1.defineAll(type_data);
            if (b != null && b.isCompatibleWith(b1)) return;
            if (b1.isParameterizedType()) {
               JcompType b1a = b1.getBaseType();
               if (b != null && b.isCompatibleWith(b1a)) return;
             }
            else if (b != null && b.isArrayType() && b1.isArrayType()) {
               JcompType bp = b.getBaseType();
               JcompType b1p = b1.getBaseType();
               if (bp.isCompatibleWith(b1p)) return;
               if (b1p.isCompatibleWith(bp)) return;
             }
          }
       }
      if (b != null && b.getName().equals("java.lang.Object")) return;       // handle weird parameterized cases
   
      JcompType b2 = findType(TYPE_ERROR);
      JcompAst.setExprType(n.getInitializer(),b2);
   }

   public @Override void endVisit(CastExpression n) {
      JcompType jt = JcompAst.getJavaType(n.getType());
      if (jt != null && jt.isCompiledType()) jt = null;
      if (jt == null) jt = JcompAst.getExprType(n.getType());
      if (jt == null) jt = findType(TYPE_ERROR);

      JcompAst.setExprType(n,JcompAst.getJavaType(n.getType()));
    }

   public @Override void endVisit(ConditionalExpression n) {
      JcompType t1 = JcompAst.getExprType(n.getThenExpression());
      JcompType t2 = JcompAst.getExprType(n.getElseExpression());
      t1 = JcompType.mergeTypes(type_data,t1,t2);
      JcompAst.setExprType(n,t1);
    }

   public @Override void endVisit(InfixExpression n) {
      JcompType t1 = JcompAst.getExprType(n.getLeftOperand());
      JcompType t2 = JcompAst.getExprType(n.getRightOperand());
      if (t1 == null) System.err.println("NULL TYPE FOR LEFT " + n);
      if (t2 == null) System.err.println("NULL TYPE FOR RIGHT " + n);
      JcompType t1a = (t1 == null ? null : t1.getAssociatedType());
      JcompType t2a = (t2 == null ? null : t2.getAssociatedType());
      if (t1 != null && t1.isPrimitiveType()) t1a = t1;
      if (t2 != null && t2.isPrimitiveType()) t2a = t2;

      if ((n.getOperator() == InfixExpression.Operator.PLUS ||
	    n.getOperator() == InfixExpression.Operator.MINUS ||
	    n.getOperator() == InfixExpression.Operator.TIMES ||
	    n.getOperator() == InfixExpression.Operator.DIVIDE) &&
	    t1a != null && t2a != null && t1 != null && t2 != null &&
	    (!t1.isNumericType() || !t2.isNumericType()) &&
	    t1a.isNumericType() && t2a.isNumericType()) {
	 t1 = t1a;
	 t2 = t2a;
       }

      if (n.getOperator() == InfixExpression.Operator.CONDITIONAL_AND ||
	     n.getOperator() == InfixExpression.Operator.CONDITIONAL_OR ||
	     n.getOperator() == InfixExpression.Operator.EQUALS ||
	     n.getOperator() == InfixExpression.Operator.GREATER ||
	     n.getOperator() == InfixExpression.Operator.GREATER_EQUALS ||
	     n.getOperator() == InfixExpression.Operator.LESS ||
	     n.getOperator() == InfixExpression.Operator.LESS_EQUALS ||
	     n.getOperator() == InfixExpression.Operator.NOT_EQUALS ||
	     n.getOperator() == InfixExpression.Operator.EQUALS) {
	 // check for valid comparison types and generate error if not
	 t1 = findType("boolean");
       }
      else if (n.getOperator() == InfixExpression.Operator.PLUS &&
	    t1 != null && t2 != null &&
	    (!t1.isNumericType() || !t2.isNumericType())) {
	 if (t1.isErrorType()) t1 = t2;
	 else if (t2.isErrorType()) ;
	 else t1 = findType("java.lang.String");
       }
      else {
	 t1 = JcompType.mergeTypes(type_data,t1,t2);
	 if (n.hasExtendedOperands()) {
	    for (Iterator<?> it = n.extendedOperands().iterator(); it.hasNext(); ) {
	       Expression e = (Expression) it.next();
	       t2 = JcompAst.getExprType(e);
	       if (t2 == null) continue;
	       if (t1.isNumericType() && !t2.isNumericType()) {
		  t2a = t2.getAssociatedType();
		  if (t2a != null && t2a.isNumericType()) t2 = t2a;
		}
	       else if (t2.isNumericType()) t1 = JcompType.mergeTypes(type_data,t1,t2);
	       else if (t2.isErrorType()) ;
	       else if (n.getOperator() == InfixExpression.Operator.PLUS) {
		  t1 = findType("java.lang.String");
		  break;
		}
	     }
	  }
       }
      JcompAst.setExprType(n,t1);
    }

   public @Override void endVisit(InstanceofExpression n) {
      JcompAst.setExprType(n,findType("boolean"));
    }
   public @Override void endVisit(ParenthesizedExpression n) {
      JcompAst.setExprType(n,JcompAst.getExprType(n.getExpression()));
    }

   public @Override void endVisit(PostfixExpression n) {
      JcompAst.setExprType(n,JcompAst.getExprType(n.getOperand()));
    }

   public @Override void endVisit(PrefixExpression n) {
      JcompAst.setExprType(n,JcompAst.getExprType(n.getOperand()));
    }

   public @Override void endVisit(ThisExpression n) {
      Name nm = n.getQualifier();
      JcompType jt = cur_type;
      if (nm != null) {
	 jt = JcompAst.getJavaType(nm);
	 if (jt == null) jt = JcompAst.getExprType(nm);
	 if (jt == null) jt = findType(TYPE_ERROR);
       }
      JcompAst.setExprType(n,jt);
    }

   public @Override void endVisit(VariableDeclarationExpression n) {
      JcompType jt = JcompAst.getJavaType(n.getType());
      if (jt == null) JcompAst.setExprType(n,findType(TYPE_ERROR));
      else JcompAst.setExprType(n,jt);
    }

   public @Override boolean visit(TypeDeclaration n) {
      outer_types.push(cur_type);
      cur_type = JcompAst.getJavaType(n);
      return true;
    }

   public @Override void endVisit(TypeDeclaration n) {
      cur_type = outer_types.pop();
    }

   public @Override boolean visit(EnumDeclaration n) {
      outer_types.push(cur_type);
      cur_type = JcompAst.getJavaType(n);
      return true;
    }

   public @Override void endVisit(EnumDeclaration n) {
      cur_type = outer_types.pop();
    }

   public @Override boolean visit(MemberValuePair n) {
      Expression ex = n.getValue();
      if (ex != null) ex.accept(this);
      return false;
    }

   public @Override boolean visit(AnonymousClassDeclaration n) {
      outer_types.push(cur_type);
      cur_type = JcompAst.getJavaType(n);
      return true;
    }

   public @Override void endVisit(AnonymousClassDeclaration n) {
      cur_type = outer_types.pop();
    }

   public @Override boolean visit(SwitchCase n) {
      SwitchStatement ss = null;
      for (ASTNode p = n; p != null; p = p.getParent()) {
	 if (p instanceof SwitchStatement) {
	    ss = (SwitchStatement) p;
	    break;
	  }
       }
      if (ss != null) {
	 JcompType switchtype = JcompAst.getExprType(ss.getExpression());
	 if (switchtype != null && switchtype.isEnumType()) {
	    if (n.getExpression() instanceof SimpleName) {
	       switchtype.defineAll(type_data);
	       SimpleName sn = (SimpleName) n.getExpression();
	       JcompSymbol js = switchtype.getScope().lookupVariable(sn.getIdentifier());
	       if (js != null && js.isEnumSymbol()) {
		  JcompAst.setReference(sn,js);
		  JcompAst.setExprType(sn,switchtype);
		  return false;
		}
	     }
	  }
       }

      return true;
    }



   public @Override void endVisit(UnionType t) {
      JcompType jt = JcompAst.getJavaType(t);
      JcompAst.setExprType(t,jt);
    }
   public @Override void endVisit(IntersectionType t) {
      JcompType jt = JcompAst.getJavaType(t);
      JcompAst.setExprType(t,jt);
    }


   public @Override boolean visit(LambdaExpression e)
   {
      JcompType typ = getReferenceType(e);
      if (typ == null) {
	 need_rescan = true;
	 return false;
       }
      List<JcompType> argtypes = typ.getComponents();
      int idx = 0;
      for (Object o : e.parameters()) {
	 if (o instanceof VariableDeclarationFragment) {
	    VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	    JcompType aty = argtypes.get(idx);
	    JcompSymbol js = JcompAst.getDefinition(vdf);
	    js.setType(aty);
	  }
       }

      return true;
   }




   public @Override void endVisit(LambdaExpression e) {
      List<JcompType> argtypes = new ArrayList<JcompType>();
      for (Object o : e.parameters()) {
	 JcompType jt = type_data.findSystemType("?");
	 if (o instanceof SingleVariableDeclaration) {
	    SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
	    jt = JcompAst.getJavaType(svd.getType());
	  }
	 else {
	    VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	    JcompSymbol js = JcompAst.getDefinition(vdf);
	    if (js != null) jt = js.getType();
	  }

	 argtypes.add(jt);
       }
      JcompSymbol fsym = JcompAst.getDefinition(e);
      JcompType rettype = JcompAst.getExprType(e.getBody());
      JcompType methodtype = JcompType.createMethodType(rettype,argtypes,false,null);
      JcompType reftype = JcompType.createFunctionRefType(methodtype,null,fsym);
      JcompAst.setExprType(e,reftype);
      JcompAst.setJavaType(e,reftype);
    }

   public @Override void endVisit(CreationReference r) {
      r.getType().accept(this);
      for (Object o : r.typeArguments()) {
	 Type t = (Type) o;
	 t.accept(this);
       }
      JcompType typ = JcompAst.getJavaType(r.getType());
      if (typ == null) typ = JcompAst.getExprType(r.getType());
      handleReference(r,typ,true,"<init>");
    }
   public @Override boolean visit(ExpressionMethodReference r) {
      r.getExpression().accept(this);
      boolean oref = false;
      JcompType qt = JcompAst.getJavaType(r.getExpression());
      if (qt == null) {
	 oref = true;
	 qt = JcompAst.getExprType(r.getExpression());
       }
      handleReference(r,qt,oref,r.getName().getIdentifier());
      return false;
    }
   public @Override void endVisit(SuperMethodReference r) {
      JcompType typ = cur_type;
      if (r.getQualifier() != null) {
	 r.getQualifier().accept(this);
	 typ = JcompAst.getJavaType(r.getQualifier());
	 if (typ == null) typ = JcompAst.getExprType(r.getQualifier());
       }
      if (typ != null) typ = typ.getSuperType();
      handleReference(r,typ,false,r.getName().getIdentifier());
    }
   public @Override boolean visit(TypeMethodReference r) {
      r.getType().accept(this);
      JcompType qt = JcompAst.getJavaType(r.getType());
      handleReference(r,qt,false,r.getName().getIdentifier());

      return false;
    }

   public @Override void endVisit(IfStatement s) {
      JcompType typ = JcompAst.getExprType(s.getExpression());
      if (!typ.isBooleanType()) {
	 if (!typ.isNumericType()) {
	    JcompAst.setExprType(s.getExpression(),findType(TYPE_ERROR));
	  }
       }
    }

   public @Override void endVisit(WhileStatement s) {
      JcompType typ = JcompAst.getExprType(s.getExpression());
      if (!typ.isBooleanType()) {
	 if (!typ.isNumericType()) {
	    JcompAst.setExprType(s.getExpression(),findType(TYPE_ERROR));
	  }
       }
    }

   public @Override void endVisit(DoStatement s) {
      JcompType typ = JcompAst.getExprType(s.getExpression());
      if (!typ.isBooleanType()) {
	 if (!typ.isNumericType()) {
	    JcompAst.setExprType(s.getExpression(),findType(TYPE_ERROR));
	  }
       }
    }

   private boolean handleReference(ASTNode r,JcompType typ,boolean ref,String id)
   {
      JcompType rtyp = getReferenceType(r);
      if (rtyp == null) {
         need_rescan = true;
         JcompAst.setExprType(r,type_data.findSystemType("?"));
         return false;
       }
      JcompType mtyp = null;
      JcompType ctyp = null;
      if (ref) {
         List<JcompType> comps = new ArrayList<>(rtyp.getComponents());
         comps.add(0,typ);
         ctyp = JcompType.createMethodType(rtyp.getBaseType(),comps,rtyp.isVarArgs(),null);
       }
      if (rtyp.getComponents().size() >= 1) {
         List<JcompType> comps = new ArrayList<>(rtyp.getComponents());
         if (comps.get(0).isCompatibleWith(typ)) {
            comps.remove(0);
            mtyp = JcompType.createMethodType(rtyp.getBaseType(),comps,
        	  rtyp.isVarArgs(),null);
          }
       }
      // should handle type arguments
      boolean thisarg = false;
      JcompSymbol js = null;
      Collection<JcompSymbol> mthds = typ.getScope().getDefinedMethods();
      for (JcompSymbol ms : mthds) {
         if (ms.getName().equals(id)) {
            if (ms.isStatic() && ms.getType().isCompatibleWith(rtyp)) {
               js = ms;
               break;
             }
            else if (!ms.isStatic() && mtyp != null &&
        	  ms.getType().isCompatibleWith(mtyp)) {
               thisarg = true;
               js = ms;
               break;
             }
            else if (!ms.isStatic() && ctyp != null &&
        	  ms.getType().isCompatibleWith(ctyp)) {
               js = ms;
               break;
             }
            else if (!ms.isStatic() && ref &&
        	  ms.getType().isCompatibleWith(rtyp)) {
               thisarg = true;
               js = ms;
               break;
             }
         }
       }
   
      if (js != null) {
         JcompAst.setReference(r,js);
         JcompType styp = js.getType();
         JcompType nstype = null;
         if (thisarg) {
            List<JcompType> comps = new ArrayList<>(styp.getComponents());
            comps.add(0,js.getClassType());
            nstype = styp;
            styp = JcompType.createMethodType(styp.getBaseType(),comps,
                  styp.isVarArgs(),null);
          }
         JcompSymbol rsym = JcompAst.getDefinition(r);
         JcompType reftype = JcompType.createFunctionRefType(styp,nstype,rsym,js);
         JcompAst.setExprType(r,reftype);
         JcompAst.setJavaType(r,reftype);
       }
      else {
         JcompAst.setExprType(r,findType(TYPE_ERROR));
       }
   
      return true;
   }


   private void lookupMethod(JcompType bt,List<JcompType> atyp,ASTNode n,SimpleName nm,
         String id,boolean isstatic,boolean dfltcnst,List<JcompType> typargs) {
      JcompType mtyp = null;
      try {
         mtyp = JcompType.createMethodType(null,atyp,false,null);
       }
      catch (Throwable t) {
         System.err.println("PROBLEM CREATING METHOD TYPE: " + t);
         System.err.println("CASE: " + bt + " " + nm + " " + n);
         t.printStackTrace();
         mtyp = findType(TYPE_ERROR);
       }
   
      if (id == null && nm != null) id = nm.getIdentifier();
      
      bt.defineAll(type_data);
   
      JcompSymbol js = null;
      if (bt != null) js = callLookupMethod(bt,id,mtyp,n);
      // if (js != null) {                 // done in callLookupMethod
         // js = checkProtections(js,bt,n);
       // }
      if (js != null && bt != null && isstatic && !js.isStatic()) {
         if (!bt.isCompatibleWith(js.getClassType())) {
            System.err.println("JCOMP: Attempt to call non-static method statically");
            // js = null;
          }
       }
   
      if (js == null && id != null && bt != null && id.equals("<init>") && dfltcnst) {
         boolean havecnst = false;
         if (bt.getScope() != null) {
            for (JcompSymbol xjs : bt.getScope().getDefinedMethods()) {
               if (xjs.getName().equals("<init>")) havecnst = true;
             }
          }
         if (!havecnst) {
            JcompAst.setExprType(n,bt);
            return;
          }
       }
   
      if (js != null) {
         if (nm != null) JcompAst.setReference(nm,js);
         JcompAst.setReference(n,js);
         JcompType rt = js.getType().getBaseType();
         if (rt == null) {
            if (id != null && id.equals("<init>")) rt = findType("void");
            else rt = findType(TYPE_ERROR);
          }
         if (rt.isParameterizedType()) {
            boolean usesvar = false;
            for (JcompType prt : rt.getComponents()) {
               if (prt.isTypeVariable()) usesvar = true;
             }
            if (usesvar) {
               rt = JcompGenerics.deriveReturnType(type_data,js.getType(),bt,atyp,js.getClassType(),typargs);
               // rt = rt.getBaseType();
             }
          }
         else if (rt.isTypeVariable() || js.getType().getSignature() != null) {
            rt = JcompGenerics.deriveReturnType(type_data,js.getType(),bt,atyp,js.getClassType(),typargs);
            // handle variable type
          }
         else if (rt.getSignature() != null && rt.getSignature().startsWith("<")) {
            rt = JcompGenerics.deriveReturnType(type_data,js.getType(),bt,atyp,js.getClassType(),typargs);
          }
         else if (id != null && id.equals("clone") && atyp.size() == 0 && bt != null) {
            rt = bt;
          }
         if (n instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) n;
            JcompAst.setExprType(n,JcompAst.getJavaType(cic.getType()));
          }
         else JcompAst.setExprType(n,rt);
       }
      else {
         JcompAst.setExprType(n,findType(TYPE_ERROR));
       }
    }

   

   private JcompSymbol callLookupMethod(JcompType bt,String id,JcompType mtyp,ASTNode n) {
      try {
         return bt.lookupMethod(type_data,id,mtyp,n);
       }
      catch (Throwable t) {
         System.err.println("JCOMP: PROBLEM LOOKING UP " + bt + " " + id + " " + mtyp);
         t.printStackTrace();
         return null;
       }
    }

   private List<JcompType> buildArgumentList(List<?> args,boolean accept) {
      List<JcompType> atyp = new ArrayList<JcompType>();
      for (Iterator<?> it = args.iterator(); it.hasNext(); ) {
	 Expression e = (Expression) it.next();
	 if (accept) e.accept(this);
	 JcompType ejt = JcompAst.getExprType(e);
	 if (ejt == null) {
	    System.err.println("NO EXPR TYPE FOR: " + e.getClass().getName() + " " + e.getNodeType() + " " + e);
	    ejt = findType(TYPE_ERROR);
	  }
	 atyp.add(ejt);
       }
      return atyp;
    }


}	// end of subclass RefPass





/********************************************************************************/
/*										*/
/*	Lambda and Reference Type Inference					*/
/*										*/
/********************************************************************************/

JcompType getReferenceType(ASTNode lambda)
{
   ASTNode par = lambda.getParent();
   if (par instanceof VariableDeclarationFragment) par = par.getParent();

   JcompType typ = null;

   switch (par.getNodeType()) {
      case ASTNode.METHOD_INVOCATION :
	 typ = getReferenceTypeFrom((MethodInvocation) par,lambda);
	 break;
      case ASTNode.ASSIGNMENT :
	 typ = getReferenceTypeFrom((Assignment) par);
	 break;
      case ASTNode.VARIABLE_DECLARATION_STATEMENT :
	 typ = getReferenceTypeFrom((VariableDeclarationStatement) par);
	 break;
      case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
	 typ = getReferenceTypeFrom((VariableDeclarationExpression) par);
	 break;
      case ASTNode.CAST_EXPRESSION :
	 typ = getReferenceTypeFrom((CastExpression) par);
	 break;
    }

   if (typ != null) {
      typ.defineAll(type_data);
      JcompType mt = typ.getFunctionalType();
      if (mt != null) typ = mt;
    }
   else {
      JcompAst.setExprType(lambda,type_data.findType(TYPE_ANY_CLASS));
    }

   return typ;
}


JcompType getReferenceTypeFrom(MethodInvocation mi,ASTNode arg)
{
   JcompSymbol js = JcompAst.getReference(mi);
   if (js == null) return null;
   int idx = 0;
   for (Object o : mi.arguments()) {
       if (arg == o) break;
       ++idx;
    }
   return js.getType().getComponents().get(idx);
}


JcompType getReferenceTypeFrom(Assignment par)
{
   return JcompAst.getExprType(par.getLeftHandSide());
}


JcompType getReferenceTypeFrom(VariableDeclarationStatement vds)
{
   return JcompAst.getJavaType(vds.getType());
}


JcompType getReferenceTypeFrom(VariableDeclarationExpression vde)
{
   return JcompAst.getJavaType(vde.getType());
}


JcompType getReferenceTypeFrom(CastExpression ce)
{
   return JcompAst.getJavaType(ce.getType());
}




}	// end of class JcompResolver




/* end of JcompResolver.java */
