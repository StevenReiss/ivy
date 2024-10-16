/********************************************************************************/
/*										*/
/*		JcompSymbol.java						*/
/*										*/
/*	Representation of a Java definition					*/
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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.objectweb.asm.Opcodes;

import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.file.IvyLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *	This class represents a resolved symbol.  All definitions and uses of the
 *	symbol will refer to the same symbol object.  Methods provide information
 *	about the symbol.
 **/


public abstract class JcompSymbol implements JcompConstants {



/********************************************************************************/
/*										*/
/*	Static Fields								*/
/*										*/
/********************************************************************************/

private static AtomicInteger lambda_var_counter = new AtomicInteger();



/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

static JcompSymbol createSymbol(SingleVariableDeclaration n,JcompTyper typer)
{
   JcompType jt = JcompAst.getJavaType(n.getType());
   if (jt == null) {
      System.err.println("NULL TYPE FOR: " + n);
      jt = typer.findType(JcompTyper.TYPE_ERROR);
    }
   for (int i = 0; i < n.getExtraDimensions(); ++i) jt = typer.findArrayType(jt);
   if (n.isVarargs())
      jt = typer.findArrayType(jt);

   return new VariableSymbol(n,jt,n.getModifiers(),null);
}



static JcompSymbol createSymbol(VariableDeclarationFragment n,JcompTyper typer)
{
   JcompType clstyp = null;
   ASTNode par = n.getParent();
   Type typ = null;
   int mods = 0;
   if (par instanceof FieldDeclaration) {
      FieldDeclaration fd = (FieldDeclaration) par;
      typ = fd.getType();
      mods = fd.getModifiers();
      clstyp = JcompAst.getJavaType(fd.getParent());
      if (JcompAst.isInInterface(par)) {
	 mods |= Modifier.STATIC | Modifier.PUBLIC;
       }
    }
   else if (par instanceof VariableDeclarationExpression) {
      VariableDeclarationExpression ve = (VariableDeclarationExpression) par;
      typ = ve.getType();
      mods = ve.getModifiers();
    }
   else if (par instanceof VariableDeclarationStatement) {
      VariableDeclarationStatement vs = (VariableDeclarationStatement) par;
      typ = vs.getType();
      mods = vs.getModifiers();
    }
   else if (par instanceof LambdaExpression) {
      JcompType jt1 = JcompType.createVariableType("L$" + lambda_var_counter.incrementAndGet());
      // JcompType jt1 = typer.findSystemType("?");
      return new VariableSymbol(n,jt1,0,clstyp);
    }
   else throw new Error("Unknown parent for variable decl: " + par);

   JcompType jt = JcompAst.getJavaType(typ);
   for (int i = 0; i < n.getExtraDimensions(); ++i) {
      if (jt != null) jt = typer.findArrayType(jt);
    }

   if (jt == null)
      IvyLog.logE("JCOMP","NULL TYPE for variable declaration: " + typ);

   return new VariableSymbol(n,jt,mods,clstyp);
}



public static JcompSymbol createDummyField(JcompSymbol src,JcompType cls)
{
   VariableSymbol vs = (VariableSymbol) src;
   
   JcompSymbol rslt = new DummyField(vs,cls);
   
   cls.getScope().defineVar(rslt);
   
   return rslt;
}


static JcompSymbol createNestedThis(JcompType cls,JcompType outer)
{
   return new NestedThisSymbol(outer,cls);
}




static JcompSymbol createSymbol(EnumConstantDeclaration n)
{
   EnumDeclaration ed = (EnumDeclaration) n.getParent();

   return new EnumSymbol(n,JcompAst.getJavaType(ed));
}



static JcompSymbol createSymbol(LabeledStatement n)
{
   return new LabelSymbol(n);
}


static JcompSymbol createSymbol(MethodDeclaration n)
{
   return new MethodSymbol(n);
}


static JcompSymbol createSymbol(AnnotationTypeMemberDeclaration n)
{
   return new MethodSymbol(n);
}


static JcompSymbol createSymbol(LambdaExpression n)
{
   return new LambdaSymbol(n);
}


static JcompSymbol createSymbol(MethodReference n)
{
   return new RefSymbol(n);
}


static JcompSymbol createSymbol(AbstractTypeDeclaration n)
{
   return new TypeSymbol(n);
}


static JcompSymbol createSymbol(JcompType type,int acc)
{
   return new AsmTypeSymbol(type,acc);
}


static JcompSymbol createBinaryField(String id,JcompType typ,JcompType cls,int acc,String sign)
{
   return new BinaryField(id,typ,cls,acc,sign);
}



static JcompSymbol createBinaryMethod(String id,JcompType typ,JcompType cls,int access,
				       List<JcompType> excs,boolean gen)
{
   return new BinaryMethod(id,typ,cls,access,excs,gen);
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected JcompSymbol()
{
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

/**
 *	Return the symbol name.
 **/

public abstract String getName();



/**
 *	Return the name to be used in error messages, etc.  Generally the same as
 *	getName()
 **/

public String getReportName()			{ return getName(); }



/**
 *	Return the type object corresponding to this name's type.
 **/

public abstract JcompType getType();
public void setType(JcompType typ)			{ }
public JcompType getDeclaredType(JcompTyper typer)      { return getType(); }



/**
 *	Return the AST node where this name is declared.
 **/

public ASTNode getDefinitionNode()		{ return null; }


/**
 *      Return the initializer AST node
 **/

public Expression getInitializer()              { return null; }


/**
 *	Return the AST ndoe where the name comes from in its declaration.
 **/

public ASTNode getNameNode()			{ return null; }


/**
 *	Determine if the symbol is known, that is, if it is part of standard
 *	Java or the provided compilation context.
 **/

public boolean isKnown()			{ return isBinarySymbol(); }
public boolean isBinarySymbol() 		{ return false; }



/**
 *	Return true if this symbol corresponds to a method.
 **/

public boolean isMethodSymbol() 		{ return false; }


/**
 *	Return true if this symbol represents a constrctor.
 **/

public boolean isConstructorSymbol()		{ return false; }


/**
 *	Return true if this symbol represents a type (class/interface/enum).
 **/

public boolean isTypeSymbol()			{ return false; }


/**
 *	Return true if this symbol represents an enumeration constant.
 **/

public boolean isEnumSymbol()			{ return false; }


/**
 *	Return true if this symbol represents a field.
 **/

public boolean isFieldSymbol()			{ return false; }



/**
 *	Return true if this symbol is a method with a generic return type.
 **/

public boolean isGenericReturn()		{ return false; }



/**
 *	Return the kind of symbol.  This can be used to differentiate between
 *	the different type kinds (i.e. ENUM/INTERFACE/CLASS).
 **/

public JcompSymbolKind getSymbolKind()
{
   if (isConstructorSymbol()) return JcompSymbolKind.CONSTRUCTOR;
   else if (isMethodSymbol()) return JcompSymbolKind.METHOD;
   else if (isFieldSymbol()) return JcompSymbolKind.FIELD;
   else if (isEnumSymbol()) return JcompSymbolKind.FIELD;
   else if (isTypeSymbol()) {
      JcompType jt = getType();
      if (jt.isEnumType()) return JcompSymbolKind.ENUM;
      else if (jt.isInterfaceType()) return JcompSymbolKind.INTERFACE;
      else if (jt.isClassType()) return JcompSymbolKind.CLASS;
    }

   return JcompSymbolKind.NONE;
}


/**
 *	Return true if the symbol is used beyond its definition.
 **/

public boolean isUsed() 			{ return true; }

public int getUseCount()                        { return 0; }

/**
 *	Return true if the symbol's value is used (the symbol is read).
 **/

public boolean isRead() 			{ return true; }

public int getReadCount()                       { return 0; }


void noteUsed() 				{ }
void noteRead() 				{ }


/**
 *	Return true if the symbol is static.
 **/

public boolean isStatic()			{ return true; }


/**
 *	Return true if the symbol is private
 **/

public boolean isPrivate()			{ return false; }

public boolean isVolatile()			{ return false; }


/**
 *	Return true if the symbol is public
 **/

public boolean isPublic()			{ return false; }


/**
 *	Return true if the symbol is protected.
 **/

public boolean isProtected()			{ return false; }


/**
 *	Return true if the symbol if final.
 **/

public boolean isFinal()			{ return false; }


/**
 *	Return true if the symbol is abstract.
 **/

public boolean isAbstract()			{ return false; }


/**
 *	Return the modifiers using standard Java integer notation (java.lang.reflect.Modifier).
 **/

public int getModifiers()			{ return 0; }



/**
 *	Return the exceptions explicitly thrown by a method or constructor.
 **/

public Iterable<JcompType> getExceptions()	{ return Collections.emptyList(); }


/**
 *	Return the type for the declaring class for a symbol.
 **/

public JcompType getClassType() 	{ return null; }



/**
 *	Get the fully qualified name for a symbol.
 **/

public final String getFullName()
{
   return getFullName(false);
}


protected String getFullName(boolean removegenerics)
{
   ASTNode dn = getDefinitionNode();
   if (isNonLocalDef(dn)) {
      for (ASTNode p = dn.getParent(); p != null; p = p.getParent()) {
	 switch (p.getNodeType()) {
	    case ASTNode.ANONYMOUS_CLASS_DECLARATION :
	    case ASTNode.TYPE_DECLARATION :
	    case ASTNode.ANNOTATION_TYPE_DECLARATION :
	    case ASTNode.ENUM_DECLARATION :
	       JcompType jt = JcompAst.getJavaType(p);
	       if (jt != null) {
                  String tnm = (removegenerics ? jt.getNongenericName() : jt.getName());
                  return tnm + "." + getName();
                }
	       break;
	  }
       }
    }

   return getName();
}



/**
 *	Get the fully qualified reporting name for a symbol
 **/

public String getFullReportName()
{
   ASTNode dn = getDefinitionNode();
   if (isNonLocalDef(dn)) {
      for (ASTNode p = dn.getParent(); p != null; p = p.getParent()) {
	 if (p instanceof AbstractTypeDeclaration) {
	    JcompType jt = JcompAst.getJavaType(p);
	    return jt.getName() + "." + getReportName();
	  }
       }
    }

   return getReportName();
}


/**
 *      Get complete name including method, etc.
 **/

public String getCompleteName()
{
   return getFullName();
}


/**
 *      Get non-generic name
 **/

public final String getNongenericName()
{
   return getFullName(true);
}



protected String getNamePrefix()
{
   ASTNode dn = getDefinitionNode();
   String pfx = null;
   for (ASTNode p = dn; p != null; p = p.getParent()) {
      switch (p.getNodeType()) {
         case ASTNode.ANONYMOUS_CLASS_DECLARATION :
         case ASTNode.TYPE_DECLARATION :
         case ASTNode.ANNOTATION_TYPE_DECLARATION :
         case ASTNode.ENUM_DECLARATION :
            JcompType jt = JcompAst.getJavaType(p);
            if (jt != null) pfx = jt.getName(); 
            break;
         case ASTNode.METHOD_DECLARATION :
            JcompSymbol js = JcompAst.getDefinition(p);
            pfx = js.getFullName();
            break;
       }
      if (pfx != null) break;
    }
   
   if (pfx == null) pfx = "";
   else pfx += ".";
   
   return pfx;
}



/**
 *	Get the handle or key for the symbol within a given project.
 **/

public String getHandle(String proj)
{
   return null;
}


public String getSignature()			{ return null; }

private boolean isNonLocalDef(ASTNode n)
{
   if (n == null) return false;
   switch (n.getNodeType()) {
      case ASTNode.SINGLE_VARIABLE_DECLARATION :
      case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
      case ASTNode.VARIABLE_DECLARATION_STATEMENT :
	 return false;
      default :
	 break;
    }
   return true;
}



/********************************************************************************/
/*										*/
/*	AST creation methods							*/
/*										*/
/********************************************************************************/

/**
 *	Create an AST statement node declaring this symbol in a new AST
 **/

public Statement createDeclaration(AST ast)
{
   VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
   vdf.setName(JcompAst.getSimpleName(ast,getName()));
   vdf.setInitializer(getType().createDefaultValue(ast));
   VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);
   vds.setType(getType().createAstNode(ast));

   return vds;
}


/********************************************************************************/
/*										*/
/*	Generic handling							*/
/*										*/
/********************************************************************************/

JcompSymbol parameterize(JcompTyper typer,JcompType ptype,
	Map<String,JcompType> outers)
{
   Map<String,JcompType> nouters = (outers == null ? null : new HashMap<>(outers));
   JcompSymbol newsym = this;
   if (isMethodSymbol()) {
      JcompType jty = JcompGenerics.deriveMethodType(typer,getType(),ptype,nouters);
      if (jty == null || jty == getType()) return this;
      newsym = createParameterized(ptype,jty);
      if (newsym != null) newsym.setOriginalMethod(this);
    }
   else if (isFieldSymbol()) {
      String sgn = getSignature();
      JcompType ftyp = getType();
      if (sgn == null) sgn = ftyp.getSignature();
      if (sgn == null) return this;
      JcompType jty = JcompGenerics.deriveFieldType(typer,ftyp,sgn,
	    ptype,nouters);
      if (jty == null || jty == getType()) return this;
      newsym = createParameterized(ptype,jty);
    }
   return newsym;
}


public JcompSymbol getOriginalMethod()
{
   return this;
}

void setOriginalMethod(JcompSymbol js)          { }


JcompSymbol createParameterized(JcompType classtype,JcompType symtype)
{
   return this;
}



public JcompSymbol getBaseSymbol(JcompTyper typer)
{
   return this;
}



/********************************************************************************/
/*										*/
/*	Annotation methods							*/
/*										*/
/********************************************************************************/

public List<JcompAnnotation> getAnnotations()
{
   ASTNode def = getDefinitionNode();
   if (def == null) return null;
   ASTNode par = def.getParent();

   List<?> mods = null;
   if (def instanceof SingleVariableDeclaration) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) def;
      mods = svd.modifiers();
    }
   else if (par instanceof VariableDeclarationStatement) {
      VariableDeclarationStatement vdf = (VariableDeclarationStatement) par;
      mods = vdf.modifiers();
    }
   else if (par instanceof VariableDeclarationExpression) {
      VariableDeclarationExpression vdf = (VariableDeclarationExpression) par;
      mods = vdf.modifiers();
    }
   else if (def instanceof MethodDeclaration) {
      MethodDeclaration md = (MethodDeclaration) def;
      mods = md.modifiers();
    }
   else if (def instanceof EnumConstantDeclaration) {
      EnumConstantDeclaration ecd = (EnumConstantDeclaration) def;
      mods = ecd.modifiers();
    }
   else return null;

   if (mods == null || mods.size() == 0) return null;

   List<JcompAnnotation> rslt = JcompAnnotation.getAnnotations(mods);

   return rslt;
}



static Object getValue(Expression e)
{
   switch (e.getNodeType()) {
      case ASTNode.NULL_LITERAL :
	 return null;
      case ASTNode.NUMBER_LITERAL :
	 NumberLiteral nlit = (NumberLiteral) e;
	 return nlit.getToken();
      case ASTNode.BOOLEAN_LITERAL :
	 BooleanLiteral blit = (BooleanLiteral) e;
	 return blit.booleanValue();
      case ASTNode.STRING_LITERAL :
	 StringLiteral slit = (StringLiteral) e;
	 String s0 = slit.getEscapedValue();
	 return IvyFormat.getLiteralValue(s0);
      case ASTNode.TEXT_BLOCK :
         TextBlock tblk = (TextBlock) e;
         String s1 = tblk.getEscapedValue();
         return IvyFormat.getLiteralValue(s1);
      case ASTNode.QUALIFIED_NAME :
      case ASTNode.SIMPLE_NAME :
	 Name snam = (Name) e;
	 return snam.getFullyQualifiedName();
    }
   return e;
}


/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return getName();
}


/********************************************************************************/
/*										*/
/*	KnownField -- field from known class					*/
/*										*/
/********************************************************************************/

private static class BinaryField extends JcompSymbol {

   private JcompType class_type;
   private String field_name;
   private JcompType field_type;
   private int access_info;
   private String field_signature;

   BinaryField(String id,JcompType fty,JcompType cls,int access,String sign) {
      field_name = id;
      field_type = fty;
      class_type = cls;
      access_info = access;
      field_signature = sign;
    }

   @Override JcompSymbol createParameterized(JcompType ctype,JcompType ftype) {
      return new BinaryField(field_name,ftype,ctype,access_info,field_signature);
    }

   @Override public String getName()		{ return field_name; }
   @Override public JcompType getType() 	{ return field_type; }
   @Override public boolean isBinarySymbol()	{ return true; }
   @Override public boolean isFieldSymbol()	{ return true; }
   @Override public boolean isStatic()		{ return Modifier.isStatic(access_info); }
   @Override public boolean isFinal()		{ return Modifier.isFinal(access_info); }
   @Override public boolean isVolatile()	{ return Modifier.isVolatile(access_info); }
   @Override public JcompType getClassType()	{ return class_type; }
   @Override public boolean isEnumSymbol()	{ return (access_info & Opcodes.ACC_ENUM) != 0; }
   @Override public int getModifiers()		{ return access_info; }
   @Override public String getSignature()		{ return field_signature; }

   @Override protected String getFullName(boolean nogen) {
      String tnm = (nogen ? class_type.getNongenericName() : class_type.getName());
      return tnm + "." + field_name;
    }



}	// end of subtype KnownField




/********************************************************************************/
/*										*/
/*	VariableSymbol -- local variable					*/
/*										*/
/********************************************************************************/

private static class VariableSymbol extends JcompSymbol {

   private VariableDeclaration ast_node;
   private JcompType java_type;
   private JcompType class_type;
   private int is_used;
   private int is_read;

   VariableSymbol(VariableDeclaration n,JcompType t,int mods,JcompType clstyp) {
      ast_node = n;
      java_type = t;
      class_type = null;
      is_used = 0;
      is_read = 0;
    }

   VariableSymbol(VariableSymbol frm,JcompType ctyp,JcompType typ) {
      ast_node = frm.ast_node;
      java_type = typ;
      class_type = ctyp;
      is_used = frm.is_used;
      is_read = frm.is_read;
    }

   @Override JcompSymbol createParameterized(JcompType ctype,JcompType vtype) {
      return new VariableSymbol(this,ctype,vtype);
    }

   @Override public String getName() {
      if (ast_node == null) return "???";
      return ast_node.getName().getIdentifier();
   }
   @Override public String getCompleteName() {
      if (isFieldSymbol()) return getFullName();
      return getNamePrefix() + getName();
    }
   
   @Override public JcompType getType() { return java_type; }
   @Override public boolean isUsed()	{ return is_used > 0; }
   @Override public boolean isRead()	{ return is_read > 0; }
   @Override void noteUsed()			{ is_used++; }
   @Override void noteRead()			{ is_read++; }
   @Override public int getReadCount()  { return is_read; }
   @Override public int getUseCount()   { return is_used; }

   @Override public void setType(JcompType typ) {
      java_type = typ;
    }

   @Override public ASTNode getDefinitionNode() {
      for (ASTNode p = ast_node; p != null; p = p.getParent()) {
         switch (p.getNodeType()) {
            case ASTNode.FIELD_DECLARATION :
               return p;
            case ASTNode.SINGLE_VARIABLE_DECLARATION :
            case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
            case ASTNode.VARIABLE_DECLARATION_STATEMENT :
            case ASTNode.LAMBDA_EXPRESSION :
               return p;
          }
       }
      return null;
    }
   
   @Override public Expression getInitializer() {
      if (ast_node == null) return null;
      if (ast_node instanceof VariableDeclarationFragment) {
         VariableDeclarationFragment vdf = (VariableDeclarationFragment) ast_node;
         return vdf.getInitializer();
       }
      else if (ast_node instanceof SingleVariableDeclaration) {
         SingleVariableDeclaration svd = (SingleVariableDeclaration) ast_node;
         return svd.getInitializer();
       }
      return null;
   }
   @Override public ASTNode getNameNode()		{ return ast_node; }

   @Override public boolean isFieldSymbol() {
      for (ASTNode p = ast_node; p != null; p = p.getParent()) {
         switch (p.getNodeType()) {
            case ASTNode.FIELD_DECLARATION :
               return true;
            case ASTNode.METHOD_DECLARATION :
               return false;
            case ASTNode.CATCH_CLAUSE :
               return false;
            case ASTNode.LAMBDA_EXPRESSION :
               return false;
          }
       }
      return false;
    }

   @Override public JcompType getClassType() {
      if (class_type != null) return class_type;
      boolean isfld = false;
      for (ASTNode p = ast_node; p != null; p = p.getParent()) {
	 switch (p.getNodeType()) {
	    case ASTNode.FIELD_DECLARATION :
	       isfld = true;
	       break;
	    case ASTNode.METHOD_DECLARATION :
	       if (!isfld) return null;
	       break;
	    case ASTNode.CATCH_CLAUSE :
	       return null;
	    case ASTNode.LAMBDA_EXPRESSION :
	       return null;
	    case ASTNode.TYPE_DECLARATION :
	    case ASTNode.ENUM_DECLARATION :
	       if (!isfld) return null;
	       return JcompAst.getJavaType(p);
	  }
       }
      return null;
    }

   @Override public JcompSymbolKind getSymbolKind() {
      if (isFieldSymbol()) return JcompSymbolKind.FIELD;
      return JcompSymbolKind.LOCAL;
    }

   @Override public int getModifiers() {
      int mods = 0;
      boolean done = false;
      for (ASTNode p = ast_node; p != null; p = p.getParent()) {
         done = true;
         switch (p.getNodeType()) {
            case ASTNode.SINGLE_VARIABLE_DECLARATION :
               SingleVariableDeclaration svd = (SingleVariableDeclaration) p;
               mods = svd.getModifiers();
               break;
            case ASTNode.VARIABLE_DECLARATION_STATEMENT :
               VariableDeclarationStatement vds = (VariableDeclarationStatement) p;
               mods = vds.getModifiers();
               break;
            case ASTNode.FOR_STATEMENT :
               break;
            case ASTNode.FIELD_DECLARATION :
               FieldDeclaration fd = (FieldDeclaration) p;
               mods = fd.getModifiers();
               JcompType jt = getClassType();
               if (jt.isInterfaceType()) mods |= Modifier.STATIC | Modifier.FINAL;
                  break;
            default :
               done = false;
               break;
          }
         if (done) break;
       }
   
      return mods;
    }

   @Override public boolean isStatic() {
      return Modifier.isStatic(getModifiers());
    }
   @Override public boolean isPrivate() {
      return Modifier.isPrivate(getModifiers());
    }
   @Override public boolean isPublic() {
      return Modifier.isPublic(getModifiers());
    }
   @Override public boolean isProtected() {
      return Modifier.isProtected(getModifiers());
    }
   @Override public boolean isFinal() {
      return Modifier.isFinal(getModifiers());
    }

   @Override public String getHandle(String proj) {
      String pfx = proj + "#";
      JcompType jt = getType();
      if (jt == null) return pfx + getName();
      return pfx + jt.getJavaTypeName() + "." + getName();
    }

}	// end of subclass VariableSymbol


private static class DummyField extends VariableSymbol {
   
   DummyField(VariableSymbol src,JcompType cls) {
      super(src,cls,src.getType());
    }
   
   @Override public boolean isFieldSymbol()             { return true; }
   
   @Override public int getModifiers()                  { return Modifier.PUBLIC; }
   
}




private static class NestedThisSymbol extends JcompSymbol {

   private JcompType java_type;
   private JcompType class_type;

   NestedThisSymbol(JcompType t,JcompType clstyp) {
      java_type = t;
      class_type = clstyp;
    }

   @Override public String getName()		{ return "this$0"; }
   @Override protected String getFullName(boolean nogen) {
      String tnm = (nogen ? class_type.getNongenericName() : class_type.getName());   
      return tnm + ".this$0";
    }
   @Override public JcompType getType() 	{ return java_type; }

   @Override public boolean isFieldSymbol()	{ return true; }

   @Override public JcompSymbolKind getSymbolKind() {
      return JcompSymbolKind.FIELD;
    }

   @Override public JcompType getClassType()	{ return class_type; }

   @Override public int getModifiers() {
      return Modifier.PROTECTED;
    }

   @Override public boolean isStatic() {
      return false;
    }
   @Override public boolean isPrivate() {
      return false;
    }
   @Override public boolean isPublic() {
      return false;
    }
   @Override public boolean isProtected() {
      return true;
    }
   @Override public boolean isFinal() {
      return false;
    }

   @Override public String getHandle(String proj) {
      String pfx = proj + "#";
      JcompType jt = getType();
      if (jt == null) return pfx + getName();
      return pfx + jt.getJavaTypeName() + "." + getName();
    }

}	// end of subclass NestedThisSymbol





/********************************************************************************/
/*										*/
/*	EnumSymbol -- enumeration constant					*/
/*										*/
/********************************************************************************/

private static class EnumSymbol extends JcompSymbol {

   private EnumConstantDeclaration ast_node;
   private JcompType java_type;

   EnumSymbol(EnumConstantDeclaration n,JcompType t) {
      ast_node = n;
      java_type = t;
    }

   @Override public String getName() {
      if (ast_node == null) return "???";
      return ast_node.getName().getIdentifier();
   }
   @Override public JcompType getType() 		{ return java_type; }
   @Override public boolean isEnumSymbol()		{ return true; }
   @Override public ASTNode getNameNode()		{ return ast_node; }
   @Override public JcompSymbolKind getSymbolKind()	{ return JcompSymbolKind.FIELD; }
   @Override public int getModifiers() { 
      return ast_node.getModifiers() | Modifier.STATIC | Modifier.PUBLIC;
    }
   @Override public JcompType getClassType()		{ return java_type; }

   @Override public String getHandle(String proj) {
      return proj + "#" + getFullName() + getType().getJavaTypeName();

    }

}	// end of subclass EnumSymbol





/********************************************************************************/
/*										*/
/*	LabelSymbol -- statement label						*/
/*										*/
/********************************************************************************/

private static class LabelSymbol extends JcompSymbol {

   private LabeledStatement ast_node;

   LabelSymbol(LabeledStatement n) {
      ast_node = n;
    }

   @Override public String getName() {
      if (ast_node == null) return "???";
      return ast_node.getLabel().getIdentifier();
   }
   @Override public String getCompleteName() {
      return getNamePrefix() + getName();
    }
   @Override public JcompType getType() { return null; }

}	// end of subclass LabelSymbol



/********************************************************************************/
/*										*/
/*	MethodSymbol -- method							*/
/*										*/
/********************************************************************************/

private static class MethodSymbol extends JcompSymbol {

   private ASTNode ast_node;
   private int is_used;
   private int symbol_mods;
   private boolean in_annotation;
   private JcompType method_type;
   private JcompType class_type;
   private JcompSymbol original_method;

   MethodSymbol(MethodDeclaration n) {
      ast_node = n;
      is_used = 0;
      symbol_mods = n.getModifiers();
      in_annotation = false;
      method_type = null;
      class_type = null;
      original_method = null;
      if (JcompAst.isInInterface(n)) {
         symbol_mods |= Modifier.PUBLIC;
         if (n.getBody() == null) symbol_mods |= Modifier.ABSTRACT;
       }
    }

   MethodSymbol(AnnotationTypeMemberDeclaration n) {
      ast_node = n;
      is_used = 0;
      symbol_mods = n.getModifiers();
      symbol_mods |= Modifier.PUBLIC;
      in_annotation = true;
      original_method = null;
   }

   MethodSymbol(MethodSymbol frm,JcompType ctyp,JcompType mtyp) {
      ast_node = frm.ast_node;
      is_used = frm.is_used;
      symbol_mods = frm.symbol_mods;
      in_annotation = frm.in_annotation;
      method_type = mtyp;
      class_type = ctyp;
      original_method = null;
    }

   @Override JcompSymbol createParameterized(JcompType ctype,JcompType mtype) {
      return new MethodSymbol(this,ctype,mtype);
    }

   @Override public String getName() {
      if (ast_node == null) return "???";
      if (in_annotation) return ((AnnotationTypeMemberDeclaration) ast_node).getName().getIdentifier();
      MethodDeclaration md = (MethodDeclaration) ast_node;
      if (md.isConstructor()) return "<init>";
      return md.getName().getIdentifier();
    }

   @Override public String getReportName() {
      return getName();
    }

   @Override public JcompType getType() {
      if (method_type != null) return method_type;
      return JcompAst.getJavaType(ast_node);
    }

   @Override public ASTNode getDefinitionNode() 	{ return ast_node; }
   @Override public ASTNode getNameNode()		{ return ast_node; }

   @Override public boolean isMethodSymbol()		{ return true; }
   @Override public boolean isConstructorSymbol() {
      if (in_annotation) return false;
      return ((MethodDeclaration) ast_node).isConstructor();
    }

   @Override public boolean isStatic()			{ return Modifier.isStatic(symbol_mods); }
   @Override public boolean isPrivate() 		{ return Modifier.isPrivate(symbol_mods); }
   @Override public boolean isFinal()			{ return Modifier.isFinal(symbol_mods); }
   @Override public boolean isAbstract()		{ return Modifier.isAbstract(symbol_mods); }
   @Override public boolean isPublic()			{ return Modifier.isPublic(symbol_mods); }
   @Override public boolean isProtected()		{ return Modifier.isProtected(symbol_mods); }

   @Override public boolean isUsed()			{ return is_used > 0; }
   @Override public void noteUsed()			{ is_used++; }
   @Override public int getUseCount()                   { return is_used; }
   @Override public int getModifiers()			{ return symbol_mods; }

   @Override public JcompType getClassType() {
      if (class_type != null) return class_type;
      return JcompAst.getJavaType(ast_node.getParent());
    }

   @Override public String getHandle(String proj) {
      return proj + "#" + getFullName() + getType().getJavaTypeName();
    }

   @Override public JcompType getDeclaredType(JcompTyper typer) {
      JcompType jt = getType();
      if (isConstructorSymbol()) {
         JcompType ct = getClassType();
         if (ct != null && ct.needsOuterClass()) {
            List<JcompType> atys = new ArrayList<>(jt.getComponents());
            atys.remove(0);
            JcompType njt = typer.createMethodType(jt.getBaseType(),atys,jt.isVarArgs(),jt.getSignature());
            return njt;
          }
       }
      return jt;
    }

   @Override public JcompSymbol getOriginalMethod() { 
      if (original_method == null) return this;
      return original_method.getOriginalMethod();
    }
   @Override void setOriginalMethod(JcompSymbol js)     { original_method = js.getOriginalMethod(); }

}	// end of subclass MethodSymbol



private static class BinaryMethod extends JcompSymbol {

   private String method_name;
   private JcompType method_type;
   private int access_flags;
   private List<JcompType> declared_exceptions;
   private JcompType class_type;
   private boolean is_generic;
   private JcompSymbol original_method;

   BinaryMethod(String nm,JcompType typ,JcompType cls,int acc,List<JcompType> excs,boolean gen) {
      method_name = nm;
      method_type = typ;
      access_flags = acc;
      if (excs == null) excs = Collections.emptyList();
      declared_exceptions = excs;
      class_type = cls;
      is_generic = gen;
      original_method = null;
    }

   @Override JcompSymbol createParameterized(JcompType ctype,JcompType mtype) {
      return new BinaryMethod(method_name,mtype,ctype,access_flags,declared_exceptions,is_generic);
    }

   @Override public String getName()			{ return method_name; }
   @Override protected String getFullName(boolean nogen) {
      String tnm = (nogen ? class_type.getNongenericName() : class_type.getName());
      return tnm + "." + method_name;
    }
   @Override public JcompType getType() 		{ return method_type; }
   @Override public boolean isBinarySymbol()		{ return true; }
   @Override public boolean isMethodSymbol()		{ return true; }
   @Override public boolean isStatic()			{ return (access_flags & Modifier.STATIC) != 0; }
   @Override public boolean isAbstract()		{ return (access_flags & Modifier.ABSTRACT) != 0; }
   @Override public boolean isPublic()			{ return (access_flags & Modifier.PUBLIC) != 0; }
   @Override public boolean isProtected()		{ return (access_flags & Modifier.PROTECTED) != 0; }
   @Override public boolean isPrivate() 		{ return (access_flags & Modifier.PRIVATE) != 0; }
   @Override public boolean isFinal()			{ return (access_flags & Modifier.FINAL) != 0; }
   @Override public Iterable<JcompType> getExceptions() { return declared_exceptions; }
   @Override public JcompType getClassType()		{ return class_type; }
   @Override public boolean isGenericReturn()		{ return is_generic; }
   @Override public boolean isConstructorSymbol()	{ return method_name.equals("<init>"); }
   @Override public int getModifiers()			{ return access_flags; }



   @Override public JcompSymbol getBaseSymbol(JcompTyper typer) {
      if (class_type.isParameterizedType()) {
         JcompType cty = class_type.getBaseType();
         while (cty.isParameterizedType()) cty = cty.getBaseType();
         JcompSymbol js = cty.lookupMethod(typer,method_name,method_type);
         if (js != null) return js;
       }
      return this;
    }

   @Override public JcompSymbol getOriginalMethod() { 
      if (original_method == null) return this;
      return original_method.getOriginalMethod();
    }
   @Override void setOriginalMethod(JcompSymbol js)     { original_method = js.getOriginalMethod(); }
   
}	// end of subclass KnownMethod




/********************************************************************************/
/*										*/
/*	TypeSymbol -- type reference						*/
/*										*/
/********************************************************************************/

private static class TypeSymbol extends JcompSymbol {

   private AbstractTypeDeclaration ast_node;

   TypeSymbol(AbstractTypeDeclaration n) {
      ast_node = n;
    }

   @Override public String getName() {
      if (ast_node == null) return "???";
      return ast_node.getName().getIdentifier();
   }
   @Override public JcompType getType() 		{ return JcompAst.getJavaType(ast_node); }

   @Override public ASTNode getDefinitionNode() 	{ return ast_node; }
   @Override public ASTNode getNameNode()		{ return ast_node; }
   @Override public boolean isTypeSymbol()		{ return true; }

   @Override protected String getFullName(boolean nogen) {
      JcompType t = getType();
      if (t != null) {
         return (nogen ? t.getNongenericName() : t.getName());
       }
      return ast_node.getName().getIdentifier();
    }

   @Override public String getFullReportName()		{ return getFullName(); }

   @Override public String getHandle(String proj) {
      JcompType jt = getType();
      if (jt == null) jt = JcompType.createErrorType();
      return proj + "#" + jt.getJavaTypeName();
    }

   @Override public boolean isStatic() {
      return Modifier.isStatic(getModifiers());
    }
   
   @Override public boolean isPrivate() {
      return Modifier.isPrivate(getModifiers());
    }
   
   @Override public int getModifiers() {
      return ast_node.getModifiers();
    }
}	// end of subclass TypeSymbol


private static class AsmTypeSymbol extends JcompSymbol {

   private JcompType for_type;
   private int access_info;

   AsmTypeSymbol(JcompType type,int acc) {
      for_type = type;
      access_info = acc;
    }

   @Override public String getName() {
      if (for_type == null) return "???";
      String nm = for_type.getName();
      int idx = nm.indexOf("<");
      if (idx > 0) nm = nm.substring(0,idx);
      idx = nm.lastIndexOf(".");
      if (idx > 0) nm = nm.substring(idx+1);
      return nm;
    }

   @Override public JcompType getType() 		{ return for_type; }

   @Override public ASTNode getDefinitionNode() 	{ return null; }
   @Override public ASTNode getNameNode()		{ return null; }
   @Override public boolean isTypeSymbol()		{ return true; }
   @Override public int getModifiers()			{ return access_info; }

   @Override protected String getFullName(boolean nogen) {
      if (nogen) return for_type.getNongenericName();
      return for_type.getName();
    }

   @Override public String getFullReportName()		{ return getFullName(); }

   @Override public String getHandle(String proj) {
      JcompType jt = getType();
      if (jt == null) jt = JcompType.createErrorType();
      return proj + "#" + jt.getJavaTypeName();
    }

}	// end of subclass TypeSymbol



/********************************************************************************/
/*										*/
/*	LambdaSymbol -> lambda expression					*/
/*										*/
/********************************************************************************/

private static AtomicInteger lambda_counter = new AtomicInteger();

private static class LambdaSymbol extends JcompSymbol {

   private LambdaExpression ast_node;
   private String lambda_name;

   LambdaSymbol(LambdaExpression n) {
      ast_node = n;
      lambda_name = "LAMBDA$" + lambda_counter.incrementAndGet();
    }

   @Override public String getName() {
      return lambda_name;
    }

   @Override public String getReportName() {
      return lambda_name;
    }

   @Override public JcompType getType() 		{ return JcompAst.getJavaType(ast_node); }

   @Override public ASTNode getDefinitionNode() 	{ return ast_node; }
   @Override public ASTNode getNameNode()		{ return ast_node; }

   @Override public boolean isMethodSymbol()	{ return true; }

   @Override public JcompType getClassType() {
      for (ASTNode n = ast_node; n != null; n = n.getParent()) {
	 if (n instanceof TypeDeclaration) {
	    return JcompAst.getJavaType(n);
	  }
       }
      return null;
    }

   @Override public String getHandle(String proj) {
      return proj + "#" + getFullName() + getType().getJavaTypeName();
    }

}	// end of subclass LambdaSymbol



private static class RefSymbol extends JcompSymbol {

   private MethodReference ast_node;
   private String ref_name;

   RefSymbol(MethodReference n) {
      ast_node = n;
      ref_name = "MREF$" + lambda_counter.incrementAndGet();
    }

   @Override public String getName() {
      return ref_name;
    }

   @Override public String getReportName() {
      return ref_name;
    }

   @Override public JcompType getType() 		{ return JcompAst.getJavaType(ast_node); }

   @Override public ASTNode getDefinitionNode() 	{ return ast_node; }
   @Override public ASTNode getNameNode()		{ return ast_node; }

   @Override public boolean isMethodSymbol()		{ return true; }

   @Override public JcompType getClassType() {
      for (ASTNode n = ast_node; n != null; n = n.getParent()) {
	 if (n instanceof TypeDeclaration) {
	    return JcompAst.getJavaType(n);
	  }
       }
      return null;
    }

   @Override public String getHandle(String proj) {
      return proj + "#" + getFullName() + getType().getJavaTypeName();
    }

}	// end of subclass RefSymbol









}	// end of class JcompSymbol
