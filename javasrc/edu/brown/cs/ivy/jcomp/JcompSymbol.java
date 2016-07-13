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

import org.eclipse.jdt.core.dom.*;

import java.util.Collections;
import java.util.List;


/**
 *	This class represents a resolved symbol.  All definitions and uses of the
 *	symbol will refer to the same symbol object.  Methods provide information
 *	about the symbol.
 **/


abstract public class JcompSymbol implements JcompConstants {



/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

static JcompSymbol createSymbol(SingleVariableDeclaration n,JcompTyper typer)
{
   JcompType jt = JcompAst.getJavaType(n.getType());
   if (jt == null) System.err.println("NULL TYPE FOR: " + n);
   for (int i = 0; i < n.getExtraDimensions(); ++i) jt = typer.findArrayType(jt);
   if (n.isVarargs()) jt = typer.findArrayType(jt);

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
   else throw new Error("Unknown parent for variable decl: " + par);

   JcompType jt = JcompAst.getJavaType(typ);
   for (int i = 0; i < n.getExtraDimensions(); ++i) {
      if (jt != null) jt = typer.findArrayType(jt);
    }

   if (jt == null)
      System.err.println("NULL TYPE for variable declaration: " + typ);

   return new VariableSymbol(n,jt,mods,clstyp);
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


static JcompSymbol createSymbol(AbstractTypeDeclaration n)
{
   return new TypeSymbol(n);
}


static JcompSymbol createSymbol(JcompType type)
{
   return new AsmTypeSymbol(type);
}


static JcompSymbol createKnownField(String id,JcompType typ,JcompType cls,boolean stat,boolean fnl)
{
   return new KnownField(id,typ,cls,stat,fnl);
}



static JcompSymbol createKnownMethod(String id,JcompType typ,JcompType cls,int access,
				       List<JcompType> excs,boolean gen)
{
   return new KnownMethod(id,typ,cls,access,excs,gen);
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

public String getReportName()		{ return getName(); }



/**
 *	Return the type object corresponding to this name's type.
 **/

public abstract JcompType getType();



/**
 *	Return the AST node where this name is declared.
 **/

public ASTNode getDefinitionNode()		{ return null; }


/**
 *	Return the AST ndoe where the name comes from in its declaration.
 **/

public ASTNode getNameNode()			{ return null; }


/**
 *	Determine if the symbol is known, that is, if it is part of standard
 *	Java or the provided compilation context.
 **/

public boolean isKnown()			{ return false; }



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



/**
 *	Return true if the symbol's value is used (the symbol is read).
 **/

public boolean isRead() 			{ return true; }


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

public String getFullName()
{
   ASTNode dn = getDefinitionNode();
   if (isNonLocalDef(dn)) {
      for (ASTNode p = dn.getParent(); p != null; p = p.getParent()) {
	 if (p instanceof AbstractTypeDeclaration) {
	    JcompType jt = JcompAst.getJavaType(p);
	    return jt.getName() + "." + getName();
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
 *	Get the handle or key for the symbol within a given project.
 **/

public String getHandle(String proj)
{
   return null;
}


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

private static class KnownField extends JcompSymbol {

   private JcompType class_type;
   private String field_name;
   private JcompType field_type;
   private boolean is_static;
   private boolean is_final;

   KnownField(String id,JcompType fty,JcompType cls,boolean stat,boolean fnl) {
      field_name = id;
      field_type = fty;
      class_type = cls;
      is_static = stat;
      is_final = fnl;
    }

   @Override public String getName()		{ return field_name; }
   @Override public JcompType getType() 	{ return field_type; }
   @Override public boolean isKnown()		{ return true; }
   @Override public boolean isFieldSymbol()	{ return true; }
   @Override public boolean isStatic()		{ return is_static; }
   @Override public boolean isFinal()		{ return is_final; }
   @Override public JcompType getClassType()	{ return class_type; }

}	// end of subtype KnownField




/********************************************************************************/
/*										*/
/*	VariableSymbol -- local variable					*/
/*										*/
/********************************************************************************/

private static class VariableSymbol extends JcompSymbol {

   private VariableDeclaration ast_node;
   private JcompType java_type;
   private boolean is_used;
   private boolean is_read;

   VariableSymbol(VariableDeclaration n,JcompType t,int mods,JcompType clstyp) {
      ast_node = n;
      java_type = t;
      is_used = false;
      is_read = false;
    }

   @Override public String getName()	{ return ast_node.getName().getIdentifier(); }
   @Override public JcompType getType() { return java_type; }
   @Override public boolean isUsed()	{ return is_used; }
   @Override public boolean isRead()	{ return is_read; }
   @Override void noteUsed()			{ is_used = true; }
   @Override void noteRead()			{ is_read = true; }

   @Override public ASTNode getDefinitionNode() {
      for (ASTNode p = ast_node; p != null; p = p.getParent()) {
	 switch (p.getNodeType()) {
	    case ASTNode.FIELD_DECLARATION :
	       return p;
	    case ASTNode.SINGLE_VARIABLE_DECLARATION :
	    case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
	    case ASTNode.VARIABLE_DECLARATION_STATEMENT :
	       return p;
	  }
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
	  }
       }
      return false;
    }

   @Override public JcompSymbolKind getSymbolKind() {
      if (isFieldSymbol()) return JcompSymbolKind.FIELD;
      return JcompSymbolKind.LOCAL;
    }

   @Override public int getModifiers() {
      for (ASTNode p = ast_node; p != null; p = p.getParent()) {
	 switch (p.getNodeType()) {
	    case ASTNode.SINGLE_VARIABLE_DECLARATION :
	       SingleVariableDeclaration svd = (SingleVariableDeclaration) p;
	       return svd.getModifiers();
	    case ASTNode.VARIABLE_DECLARATION_STATEMENT :
	       VariableDeclarationStatement vds = (VariableDeclarationStatement) p;
	       return vds.getModifiers();
	    case ASTNode.FOR_STATEMENT :
	       return 0;
	    case ASTNode.FIELD_DECLARATION :
	       FieldDeclaration fd = (FieldDeclaration) p;
	       return fd.getModifiers();
	  }
       }
      return 0;
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

   @Override public String getName()			{ return ast_node.getName().getIdentifier(); }
   @Override public JcompType getType() 		{ return java_type; }
   @Override public boolean isEnumSymbol()		{ return true; }
   @Override public ASTNode getNameNode()		{ return ast_node; }
   @Override public JcompSymbolKind getSymbolKind()	{ return JcompSymbolKind.FIELD; }
   @Override public int getModifiers()			{ return ast_node.getModifiers(); }

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

   @Override public String getName()		{ return ast_node.getLabel().getIdentifier(); }
   @Override public JcompType getType() { return null; }

}	// end of subclass LabelSymbol



/********************************************************************************/
/*										*/
/*	MethodSymbol -- method							*/
/*										*/
/********************************************************************************/

private static class MethodSymbol extends JcompSymbol {

   private MethodDeclaration ast_node;
   private boolean is_used;
   private int symbol_mods;

   MethodSymbol(MethodDeclaration n) {
      ast_node = n;
      is_used = false;
      symbol_mods = ast_node.getModifiers();
      if (JcompAst.isInInterface(n)) {
	 symbol_mods |= Modifier.PUBLIC | Modifier.ABSTRACT;
       }
    }

   @Override public String getName() {
      if (ast_node.isConstructor()) return "<init>";
      return ast_node.getName().getIdentifier();
    }

   @Override public String getReportName() {
      return ast_node.getName().getIdentifier();
    }

   @Override public JcompType getType() 	{ return JcompAst.getJavaType(ast_node); }

   @Override public ASTNode getDefinitionNode() { return ast_node; }
   @Override public ASTNode getNameNode()		{ return ast_node; }

   @Override public boolean isMethodSymbol()		{ return true; }
   @Override public boolean isConstructorSymbol()	{ return ast_node.isConstructor(); }

   @Override public boolean isStatic()			{ return Modifier.isStatic(symbol_mods); }
   @Override public boolean isPrivate() 		{ return Modifier.isPrivate(symbol_mods); }
   @Override public boolean isFinal()			{ return Modifier.isFinal(symbol_mods); }
   @Override public boolean isAbstract()		{ return Modifier.isAbstract(symbol_mods); }
   @Override public boolean isPublic()			{ return Modifier.isPublic(symbol_mods); }
   @Override public boolean isProtected()		{ return Modifier.isProtected(symbol_mods); }

   @Override public boolean isUsed()			{ return is_used; }
   @Override public void noteUsed()			{ is_used = true; }
   @Override public int getModifiers()			{ return symbol_mods; }

   @Override public JcompType getClassType() {
      return JcompAst.getJavaType(ast_node.getParent());
    }

   @Override public String getHandle(String proj) {
      return proj + "#" + getFullName() + getType().getJavaTypeName();
    }

}	// end of subclass MethodSymbol



private static class KnownMethod extends JcompSymbol {

   private String method_name;
   private JcompType method_type;
   private int access_flags;
   private List<JcompType> declared_exceptions;
   private JcompType class_type;
   private boolean is_generic;

   KnownMethod(String nm,JcompType typ,JcompType cls,int acc,List<JcompType> excs,boolean gen) {
      method_name = nm;
      method_type = typ;
      access_flags = acc;
      if (excs == null) excs = Collections.emptyList();
      declared_exceptions = excs;
      class_type = cls;
      is_generic = gen;
    }

   @Override public String getName()			{ return method_name; }
   @Override public String getFullName() {
      return class_type.getName() + "." + method_name;
   }
   @Override public JcompType getType() 		{ return method_type; }
   @Override public boolean isKnown()			{ return true; }
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

   @Override public String getName()			{ return ast_node.getName().getIdentifier(); }
   @Override public JcompType getType() 		{ return JcompAst.getJavaType(ast_node); }

   @Override public ASTNode getDefinitionNode() 	{ return ast_node; }
   @Override public ASTNode getNameNode()		{ return ast_node; }
   @Override public boolean isTypeSymbol()		{ return true; }

   @Override public String getFullName() {
      JcompType t = getType();
      if (t != null) return t.getName();
      return ast_node.getName().getIdentifier();
    }

   @Override public String getFullReportName()		{ return getFullName(); }

   @Override public String getHandle(String proj) {
      JcompType jt = getType();
      if (jt == null) jt = JcompType.createErrorType();
      return proj + "#" + jt.getJavaTypeName();
    }

}	// end of subclass TypeSymbol


private static class AsmTypeSymbol extends JcompSymbol {

   private JcompType for_type;

   AsmTypeSymbol(JcompType type) {
      for_type = type;
    }

   @Override public String getName() {
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

   @Override public String getFullName() {
      return for_type.getName();
    }

   @Override public String getFullReportName()		{ return getFullName(); }

   @Override public String getHandle(String proj) {
      JcompType jt = getType();
      if (jt == null) jt = JcompType.createErrorType();
      return proj + "#" + jt.getJavaTypeName();
    }

}	// end of subclass TypeSymbol



}	// end of class JcompSymbol
