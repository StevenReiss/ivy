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



import org.eclipse.jdt.core.dom.*;

import java.util.*;


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
	    v = Math.abs(v);
	    if (v < 256) type = "byte";
	    else if (v < 65536) type = "short";
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
	    List<JcompSymbol> defs = jt.lookupStatics(type_data,inm);
	    if (defs != null) {
	       for (JcompSymbol js : defs) {
		  if (js.isMethodSymbol()) cur_scope.defineMethod(js);
		  else cur_scope.defineVar(js);
		}
	     }
	  }
	 else if (n.isOnDemand() && n.isStatic() && jt1 != null) {
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

   RefPass() {
      cur_scope = null;
      cur_type = null;
      outer_types = new Stack<JcompType>();
    }

   public @Override boolean visit(AnnotationTypeMemberDeclaration n)	{ return false; }
   public @Override boolean visit(AnnotationTypeDeclaration n)		{ return false; }
   public @Override boolean visit(PackageDeclaration n) 		{ return false; }
   public @Override boolean visit(ImportDeclaration n)			{ return false; }
   public @Override boolean visit(ArrayType n)				{ return true; }
   public @Override boolean visit(ParameterizedType n)			{ return false; }
   public @Override boolean visit(PrimitiveType n)			{ return false; }
   public @Override boolean visit(QualifiedType n)			{ return true; }
   public @Override boolean visit(SimpleType n) 			{ return true; }
   public @Override boolean visit(WildcardType n)			{ return false; }
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
      JcompAst.setExprType(n,findType("java.lang.Class"));
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
      List<JcompType> atyp = buildArgumentList(n.arguments(),true);
      lookupMethod(bt,atyp,n,n.getName(),null,isstatic);
      // might want to use outer types if this failed
   
      return false;
    }

   public @Override boolean visit(SuperMethodInvocation n) {
      JcompType bt = null;
      if (cur_type != null) bt = cur_type.getSuperType();
      Name nn = n.getQualifier();
      if (nn != null) {
	 nn.accept(this);
	 bt = JcompAst.getJavaType(nn);
	 if (bt == null) bt = JcompAst.getExprType(nn);
	 if (bt != null) bt = bt.getSuperType();
       }

      List<JcompType> atyp = buildArgumentList(n.arguments(),true);

      lookupMethod(bt,atyp,n,n.getName(),null,false);

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
      if (bt.getName().contains("PhysicsVector") && bt.isKnownType()) {
         System.err.println("CHECK PHYSICS");
       }
      List<JcompType> atys = buildArgumentList(n.arguments(),false);
      if (bt.needsOuterClass()) {
         JcompType oty = bt.getOuterType();
         if (oty != null) atys.add(0,oty);
       }
      JcompAst.setExprType(n,bt);		      // set default type
      lookupMethod(bt,atys,n,null,"<init>",false);    // this can reset the type
      JcompType rt = JcompAst.getExprType(n);
      if (rt.isErrorType() && atys.size() == 0) {
         boolean havecnst = false;
         if (bt.getScope() != null) {
            for (JcompSymbol xjs : bt.getScope().getDefinedMethods()) {
               if (xjs.getName().equals("<init>")) havecnst = true;
             }
          }
         if (!havecnst) {
                                                               JcompAst.setExprType(n,bt);  // handle default constructor
          }
       }
   }

   public @Override void endVisit(ConstructorInvocation n) {
      List<JcompType> atys = buildArgumentList(n.arguments(),false);
      JcompAst.setExprType(n,cur_type);  // set type, will be reset on error
      lookupMethod(cur_type,atys,n,null,"<init>",false);
    }

   public @Override void endVisit(SuperConstructorInvocation n) {
      List<JcompType> atys = buildArgumentList(n.arguments(),false);
      JcompType  bt = null;
      if (cur_type != null) bt = cur_type.getSuperType();
      if (bt == null) bt = findType("java.lang.Object");
      lookupMethod(bt,atys,n,null,"<init>",false);
      if (JcompAst.getReference(n) != null)
	 JcompAst.setExprType(n,cur_type);
      else
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
      if (b != null && b.isCompatibleWith(b1)) ;
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
      JcompType b2 = findType(TYPE_ERROR);
      JcompAst.setExprType(n.getInitializer(),b2);
   }

   public @Override void endVisit(CastExpression n) {
      JcompType jt = JcompAst.getJavaType(n.getType());
      if (jt != null && jt.isUnknown()) jt = null;
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

      if ((n.getOperator() == InfixExpression.Operator.PLUS ||
	    n.getOperator() == InfixExpression.Operator.MINUS ||
	    n.getOperator() == InfixExpression.Operator.TIMES ||
	    n.getOperator() == InfixExpression.Operator.DIVIDE) &&
	    t1a != null && t2a != null && t1 != null && t2 != null &&
	    !t1.isNumericType() && !t2.isNumericType() &&
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
	     n.getOperator() == InfixExpression.Operator.EQUALS)
	 t1 = findType("boolean");
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
	       if (t2 == null) ;
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

   @Override public boolean visit(SwitchCase n) {
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

   private void lookupMethod(JcompType bt,List<JcompType> atyp,ASTNode n,SimpleName nm,String id,boolean isstatic) {
      JcompType mtyp = null;
      try {
         mtyp = JcompType.createMethodType(null,atyp,false);
       }
      catch (Throwable t) {
         System.err.println("PROBLEM CREATING METHOD TYPE: " + t);
         System.err.println("CASE: " + bt + " " + nm + " " + n);
         t.printStackTrace();
         mtyp = findType(TYPE_ERROR);
       }
   
      if (id == null && nm != null) id = nm.getIdentifier();
   
      JcompSymbol js = null;
      if (bt != null) js = callLookupMethod(bt,id,mtyp);
      if (js != null) {
         js = checkProtections(js,bt,n);
       }
      if (js != null && bt != null && isstatic && !js.isStatic()) {
         if (!bt.isCompatibleWith(js.getClassType())) {
            System.err.println("JCOMP: Attempt to call non-static method statically");
            // js = null;
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
               rt = rt.getBaseType();
             }
          }
         else if (rt.isTypeVariable()) {
            // handle variable type
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


   private JcompSymbol checkProtections(JcompSymbol js,JcompType basetype,ASTNode n) {
      if (js == null || js.isPublic()) return js;
      JcompType fromtype = null;
      for (ASTNode p = n; p != null; p = p.getParent()) {
	 switch (p.getNodeType()) {
	    case ASTNode.TYPE_DECLARATION :
	    case ASTNode.ENUM_DECLARATION :
	    case ASTNode.ANNOTATION_TYPE_DECLARATION :
	       fromtype = JcompAst.getJavaType(p);
	       break;
	  }
	 if (fromtype != null) break;
       }
      if (fromtype == null) return js;

      JcompType totype = js.getClassType();
      if (totype == null) return js;
      if (fromtype == totype) return js;
      if (fromtype.getName().startsWith(totype.getName() + ".")) return js;
      if (totype.getName().startsWith(fromtype.getName() + ".")) return js;
      if (js.isPrivate()) {
	 String nm1 = totype.getName();
	 String nm2 = fromtype.getName();
	 if (nm2.startsWith(nm1)) return js;
       }
      else if (js.isProtected()) {
	 if (fromtype.isCompatibleWith(basetype)) {
	    if (fromtype.isCompatibleWith(totype))
	       return js;
	  }
	 if (js.getName().equals("clone") && basetype.isArrayType())
	    return js;
       }
      else {
	 String pkg1 = getPackageName(fromtype);
	 String pkg2 = getPackageName(totype);
	 if (pkg1.equals(pkg2)) return js;
       }

      // System.err.println("ATTEMPT TO ACCESS SYMBOL OUT OF CONTEXT:" + n + " " + js);

      return null;
   }


   private String getPackageName(JcompType typ)  {
      String nm = typ.getName();
      int idx = nm.lastIndexOf(".");
      if (idx < 0) return "<DEFAULT>";
      for ( ; ; ) {
	 String pkg = nm.substring(0,idx);
	 idx = pkg.lastIndexOf(".");
	 if (idx < 0) return pkg;
	 if (idx == pkg.length()-1) {
	    pkg = pkg.substring(0,idx);
	  }
	 else if (Character.isUpperCase(pkg.charAt(idx+1))) {
	    pkg = pkg.substring(0,idx);
	  }
	 else return pkg;
       }
   }

   private JcompSymbol callLookupMethod(JcompType bt,String id,JcompType mtyp) {
      try {
         return bt.lookupMethod(type_data,id,mtyp);
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



}	// end of class JcompResolver




/* end of JcompResolver.java */
