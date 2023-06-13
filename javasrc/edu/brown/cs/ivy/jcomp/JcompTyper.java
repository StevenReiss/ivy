/********************************************************************************/
/*										*/
/*		JcompTyper.java 						*/
/*										*/
/*	Class to handle type resolution for Java files				*/
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
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.jcode.JcodeMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;


public class JcompTyper implements JcompConstants {



/********************************************************************************/
/*										*/
/*	Public storage								*/
/*										*/
/********************************************************************************/

public JcompType	BOOLEAN_TYPE;
public JcompType	BYTE_TYPE;
public JcompType	CHAR_TYPE;
public JcompType	SHORT_TYPE;
public JcompType	INT_TYPE;
public JcompType	LONG_TYPE;
public JcompType	FLOAT_TYPE;
public JcompType	DOUBLE_TYPE;
public JcompType	VOID_TYPE;

public JcompType	ANY_TYPE;
public JcompType	ERROR_TYPE;

public JcompType	OBJECT_TYPE;
public JcompType	ENUM_TYPE;
public JcompType	CLASS_TYPE;
public JcompType	STRING_TYPE;



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<String,String> initial_types;
private Map<String,JcompType> type_map;

private JcompContext	 type_context;

private static Map<String,JcompType> system_types = new HashMap<>();

private final String [] BASE_TYPES = { "byte", "short", "char", "int", "long", "float",
					  "double", "boolean", "void" };

private static Set<String> known_prefix;


static {
   known_prefix = new HashSet<String>();
   known_prefix.add("java");
   known_prefix.add("javax");
   known_prefix.add("org");
   known_prefix.add("com");
   known_prefix.add("sun");
}




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompTyper(JcompContext ctx)
{
   type_context = ctx;

   initial_types = new HashMap<>();

   type_map = new ConcurrentHashMap<>();

   BYTE_TYPE = definePrimitive(PrimitiveType.BYTE);
   SHORT_TYPE = definePrimitive(PrimitiveType.SHORT);
   CHAR_TYPE = definePrimitive(PrimitiveType.CHAR);
   INT_TYPE = definePrimitive(PrimitiveType.INT);
   LONG_TYPE = definePrimitive(PrimitiveType.LONG);
   FLOAT_TYPE = definePrimitive(PrimitiveType.FLOAT);
   DOUBLE_TYPE = definePrimitive(PrimitiveType.DOUBLE);
   BOOLEAN_TYPE = definePrimitive(PrimitiveType.BOOLEAN);
   VOID_TYPE = definePrimitive(PrimitiveType.VOID);

   ANY_TYPE = JcompType.createAnyClassType();
   type_map.put(TYPE_ANY_CLASS,ANY_TYPE);
   ERROR_TYPE = JcompType.createErrorType();
   type_map.put(TYPE_ERROR,ERROR_TYPE);

   OBJECT_TYPE = findSystemType("java.lang.Object");
   ENUM_TYPE = findSystemType("java.lang.Enum");
   STRING_TYPE = findSystemType("java.lang.String");
   CLASS_TYPE = findSystemType("java.lang.Class");

   BYTE_TYPE = definePrimitive(PrimitiveType.BYTE,"Byte");
   SHORT_TYPE = definePrimitive(PrimitiveType.SHORT,"Short");
   CHAR_TYPE = definePrimitive(PrimitiveType.CHAR,"Character");
   INT_TYPE = definePrimitive(PrimitiveType.INT,"Integer");
   LONG_TYPE = definePrimitive(PrimitiveType.LONG,"Long");
   FLOAT_TYPE = definePrimitive(PrimitiveType.FLOAT,"Float");
   DOUBLE_TYPE = definePrimitive(PrimitiveType.DOUBLE,"Double");
   BOOLEAN_TYPE = definePrimitive(PrimitiveType.BOOLEAN,"Boolean");

   fixJavaType(JcompType.createWildcardType());

   for (String s : BASE_TYPES) {
      initial_types.put(s,s);
    }
}



private JcompType definePrimitive(PrimitiveType.Code pt)
{
   String nm = pt.toString().toLowerCase();
   JcompType ty = null;

   synchronized (system_types) {
      ty = system_types.get(nm);
      if (ty == null) {
	 ty = JcompType.createPrimitiveType(pt,null);
	 system_types.put(nm,ty);
       }
      type_map.put(nm,ty);
    }
   return ty;
}



private JcompType definePrimitive(PrimitiveType.Code pt,String sys)
{
   String nm = pt.toString().toLowerCase();
   JcompType ty = null;

   synchronized (system_types) {
      JcompType jt = null;
      if (sys != null) {
	 jt = findSystemType("java.lang." + sys);
       }
      ty = system_types.get(nm);
      if (ty == null) {
	 ty = JcompType.createPrimitiveType(pt,jt);
	 system_types.put(nm,ty);
       }
      if (jt != null) {
	 jt.setAssociatedType(ty);
	 ty.setAssociatedType(jt);
       }
      type_map.put(nm,ty);
    }
   return ty;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

JcompContext getContext()			      { return type_context; }



public JcompType findType(String nm)
{
   return type_map.get(nm);
}



public JcompType findArrayType(JcompType base)
{
   String s = base.getName() + "[]";
   JcompType jt = findType(s);
   if (jt == null) {
      jt = JcompType.createArrayType(base);
      jt = fixJavaType(jt);
    }
   return jt;
}



public JcompType findSystemType(String nm)
{
   if (nm == null) return null;

   JcompType jt = type_map.get(nm);
   if (jt != null) return jt;

   if (nm.endsWith("[]")) {
      int idx = nm.lastIndexOf("[]");
      String nm0 = nm.substring(0,idx);
      JcompType bty = findSystemType(nm0);
      if (bty == null) return null;
      return findArrayType(bty);
    }

   int idx = nm.indexOf("<");
   if (idx > 0) {
      String t0 = nm.substring(0,idx);
      int idx1 = nm.lastIndexOf(">");
      String t1 = nm.substring(idx+1,idx1);
      return findParameterizedSystemType(t0,t1);
    }

   if (nm.startsWith("REF$")) {

    }



   jt = type_context.defineKnownType(this,nm);
   if (jt == null) return null;

   jt = fixJavaType(jt);

   return jt;
}



private JcompType findParameterizedSystemType(String t0,String args)
{
   JcompType btyp = findSystemType(t0);
   if (btyp == null) return null;

   List<JcompType> argl = new ArrayList<JcompType>();

   int st = 0;
   int lvl = 0;
   for (int i = 1; i < args.length(); ++i) {
      char ch = args.charAt(i);
      if (ch == ',' && lvl == 0) {
	 String s0 = args.substring(st,i).trim();
	 JcompType aty = findSystemType(s0);
	 if (aty == null) return null;
	 argl.add(aty);
	 st = i+1;
       }
      else if (ch == '<') ++lvl;
      else if (ch == '>') --lvl;
    }
   String s0 = args.substring(st).trim();
   JcompType aty = findSystemType(s0);
   if (aty == null) return null;
   argl.add(aty);

   return JcompType.createParameterizedType(btyp,argl,null,this);
}



public JcompType createMethodType(JcompType rettyp,List<JcompType> argtyps,boolean varargs,String signature)
{
   JcompType jt = JcompType.createMethodType(rettyp,argtyps,varargs,signature);
   JcompType jt1 = fixJavaType(jt);

   return jt1;
}


public Collection<JcompType> getAllTypes()
{
   return type_map.values();
}



public static boolean isSystemType(String nm)
{
   int idx = nm.indexOf(".");
   if (idx >= 0) {
      String s = nm.substring(0,idx);
      if (known_prefix.contains(s)) {
	 return true;
       }
    }

   return false;
}


public JcodeMethod getMethodCode(JcompSymbol js)
{
   if (js == null) return null;
   JcodeMethod jm = type_context.getMethodCode(js);
   if (jm == null) return null;
   if (jm.getNumInstructions() <= 0) return null;
   return jm;
}



/********************************************************************************/
/*										*/
/*	JcompType and type mapping maintenance functions			*/
/*										*/
/********************************************************************************/

private void setJavaType(ASTNode n,JcompType jt)
{
   if (jt == null) return;

   JcompAst.setJavaType(n,fixJavaType(jt));
}


public JcompType fixJavaType(JcompType jt)
{
   if (jt == null) return null;

   String nm = jt.getName();
   if (jt.isMethodType()) {
      if (jt.isVarArgs()) nm += "...";
      if (jt.getSignature() != null) {
	 nm += jt.getSignature();
       }
    }
   JcompType jt1 = type_map.get(nm);
   if (jt1 != null) jt = jt1;
   else type_map.put(nm,jt);
   return jt;
}


void applyParameters(JcompType typ)
{
   typ.applyParametersToType(this);
}

void allMembersDefined()
{ }


public JcompType defineUserType(String nm,boolean iface,boolean etype,boolean force)
{
   return defineUserType(nm,iface,etype,false,force);
}



public JcompType defineUserType(String nm,boolean iface,boolean etype,boolean atype,boolean force)
{
   JcompType jt = type_map.get(nm);
   if (jt != null && jt.isBinaryType() && force)
      jt = null;

   if (jt != null) return jt;

   if (!force && isSystemType(nm)) {
      jt = findSystemType(nm);
      if (jt != null) return jt;
    }

   // if (nm.startsWith("edu.brown.cs.s6.runner.Runner")) {
      // jt = findSystemType(nm);
      // if (jt != null) return jt;
    // }

   if (iface) jt = JcompType.createCompiledInterfaceType(nm);
   else if (etype) {
      jt = JcompType.createEnumType(nm);
    }
   else if (atype) jt = JcompType.createCompiledAnnotationType(nm);
   else jt = JcompType.createCompiledClassType(nm);

   jt = fixJavaType(jt);

   if (etype) {
      JcompType par1 = findParameterizedSystemType("java.lang.Enum",nm);
      jt.setSuperType(par1);
    }
  // type_map.put(nm,jt);

   return jt;
}




/********************************************************************************/
/*										*/
/*	Known class lookup methods						*/
/*										*/
/********************************************************************************/

JcompSymbol lookupKnownField(String cls,String id,JcompType orig)
{
   return type_context.defineKnownField(this,cls,id,orig);
}


JcompSymbol lookupKnownMethod(String cls,String id,JcompType argtyp,JcompType ctyp)
{
   return type_context.defineKnownMethod(this,cls,id,argtyp,ctyp);
}


List<JcompSymbol> lookupKnownStatics(String cls,String id,JcompType ctyp)
{
   return type_context.defineKnownStatics(this,cls,id,ctyp);
}

Set<JcompSymbol> lookupKnownAbstracts(String cls)
{
   return type_context.defineKnownAbstracts(this,cls);
}


public List<JcompSymbol> findKnownMethods(String cls)
{
   return type_context.findKnownMethods(this,cls);
}



void defineAll(String cls,JcompScope scp)
{
   type_context.defineAll(this,cls,scp);
}


/********************************************************************************/
/*										*/
/*	Methods to assign types to a Jcomp AST					*/
/*										*/
/********************************************************************************/

void assignTypes(JcompProjectImpl root)
{
   Map<ASTNode,Map<String,String>> specmap = new HashMap<>();
   Map<ASTNode,List<String>> prefmap = new HashMap<>();

   for (ASTNode cu : root.getTrees()) {
      Map<String,String> specificnames = new HashMap<>();
      specmap.put(cu,specificnames);
      List<String> prefixes = new ArrayList<>();
      prefixes.add("java.lang.");
      prefmap.put(cu,prefixes);

      TypeFinder tf = new TypeFinder(specificnames,prefixes);
      cu.accept(tf);
    }

   for (ASTNode cu : root.getTrees()) {
      Map<String,String> knownnames = new HashMap<>(initial_types);
      Map<String,String> specificnames = specmap.get(cu);
      List<String> prefixes = prefmap.get(cu);

      TypePreSetter ts = new TypePreSetter(knownnames,specificnames,prefixes);
      cu.accept(ts);
    }

   for (ASTNode cu : root.getTrees()) {
      Map<String,String> knownnames = new HashMap<>(initial_types);
      Map<String,String> specificnames = specmap.get(cu);
      List<String> prefixes = prefmap.get(cu);

      TypeSetter ts = new TypeSetter(knownnames,specificnames,prefixes);
      cu.accept(ts);
    }
}


void assignTypes(ASTNode n)
{
   Map<String,String> knownnames = new HashMap<String,String>(initial_types);
   Map<String,String> specificnames = new HashMap<String,String>();
   List<String> prefixes = new ArrayList<String>();

   prefixes.add("java.lang.");

   TypeFinder tf = new TypeFinder(specificnames,prefixes);
   n.accept(tf);
   TypeSetter ts = new TypeSetter(knownnames,specificnames,prefixes);
   n.accept(ts);
}



void assignTypes(List<ASTNode> nds)
{
   Map<String,String> knownnames = new HashMap<String,String>(initial_types);
   Map<String,String> specificnames = new HashMap<String,String>();
   List<String> prefixes = new ArrayList<String>();

   prefixes.add("java.lang.");

   TypeFinder tf = new TypeFinder(specificnames,prefixes);
   for (ASTNode n : nds) {
      n.accept(tf);
    }

   TypePreSetter pts = new TypePreSetter(knownnames,specificnames,prefixes);
   for (ASTNode n : nds) {
      n.accept(pts);
    }

   TypeSetter ts = new TypeSetter(knownnames,specificnames,prefixes);
   for (ASTNode n : nds) {
      n.accept(ts);
    }
}



/********************************************************************************/
/*										*/
/*	Methods for handling parameterized types				*/
/*										*/
/********************************************************************************/

JcompType getParameterizedReturnType(String ms,String cs,JcompType ptyp,JcompType atyp)
{
   int i0 = ms.indexOf(")");
   return getActualType(ms,i0+1,cs,ptyp,atyp);
}


JcompType getParameterizedFieldType(String ms,String cs,JcompType ptyp)
{
   return getActualType(ms,0,cs,ptyp,null);
}



private JcompType getActualType(String ms,int idx,String cs,JcompType ptyp,JcompType atyp)
{
   int dims = 0;

   while (ms.charAt(idx) == '[') {
      ++dims;
      ++idx;
    }

   String rslt = null;
   JcompType rtyp = null;

   switch (ms.charAt(idx)) {
      case 'B':
	 rslt = "byte";
	 break;
      case 'C':
	 rslt = "char";
	 break;
      case 'D':
	 rslt = "double";
	 break;
      case 'F':
	 rslt = "float";
	 break;
      case 'I':
	 rslt = "int";
	 break;
      case 'J':
	 rslt = "long";
	 break;
      case 'V':
	 rslt = "void";
	 break;
      case 'S':
	 rslt = "short";
	 break;
      case 'Z':
	 rslt = "boolean";
	 break;
      case 'L':
	 int i0 = findTypeEnd(ms,idx);
	 rslt = ms.substring(idx+1,i0);
	 rslt = rslt.replace('/','.');
	 break;
      case 'T' :
	 int i1 = findTypeEnd(ms,idx);
	 String var = ms.substring(idx+1,i1);
	 rtyp = getParamType(var,cs,ptyp,ms,atyp);
	 if (rtyp == null) rtyp = JcompType.createVariableType(var);
	 break;
    }

   if (rslt != null) rtyp = findSystemType(rslt);
   if (rtyp == null) return null;

   for (int i = 0; i < dims; ++i) rtyp = findArrayType(rtyp);

   return rtyp;
}



private JcompType getParamType(String var,String cs,JcompType ptyp,String ms,JcompType atyp)
{
   int idx = 0;
   int i0 = 1;
   if (cs != null) {
      while (cs.charAt(i0) != '>') {
	 int i1 = cs.indexOf(":",i0);
	 if (i1 < 0) break;
	 String v = cs.substring(i0,i1);
	 if (v.equals(var) && ptyp != null && ptyp.getComponents() != null) {
	    if (ptyp.getComponents().size() >= idx+1) return ptyp.getComponents().get(idx);
	  }
	 i0 = findTypeEnd(cs,i0) + 1;
	 ++idx;
       }
    }

   int ndim = 0;
   idx = 0;
   i0 = ms.indexOf("(") + 1;
   while (ms.charAt(i0) != ')') {
      ndim = 0;
      while (ms.charAt(i0) == '[') {
	 ++ndim;
	 ++i0;
       }
      if (ms.charAt(i0) == 'L') i0 = findTypeEnd(ms,i0);
      else if (ms.charAt(i0) == 'T') {
	 int i1 = i0+1;
	 i0 = findTypeEnd(ms,i0);
	 String v = ms.substring(i1,i0);
	 if (v.equals(var)) break;
       }
      ++i0;
      ++idx;
    }

   if (atyp == null) return null;

   if (idx >= atyp.getComponents().size()) return null;

   JcompType jt = atyp.getComponents().get(idx);
   while (ndim > 0) {
      if (!jt.isArrayType()) return null;
      jt = jt.getBaseType();
      --ndim;
    }

   return jt;
}



private int findTypeEnd(String s,int idx)
{
   int lvl = 0;
   while (idx < s.length()) {
      char c = s.charAt(idx);
      if (c == '<') ++lvl;
      else if (c == '>') --lvl;
      else if (c == ';' && lvl == 0) break;
      ++idx;
    }

   return idx;
}


/********************************************************************************/
/*										*/
/*	Signature creation methods						*/
/*										*/
/********************************************************************************/

private String getMethodSignature(MethodDeclaration md,JcompType ret,List<JcompType> args)
{
   StringBuffer buf = new StringBuffer();
   addTypeParametersSignature(md.typeParameters(),buf);
   buf.append("(");
   for (JcompType jt : args) {
      addTypeSignature(jt,buf);
    }
   buf.append(")");
   if (ret != null) {
      addTypeSignature(ret,buf);
    }
   else buf.append("V");

   // might want exceptions here

   return buf.toString();
}



private String getClassSignature(TypeDeclaration td,JcompType jt)
{
   StringBuffer buf = new StringBuffer();
   addTypeParametersSignature(td.typeParameters(),buf);
   if (jt.getSuperType() != null) {
      addTypeSignature(jt.getSuperType(),buf);
    }
   else buf.append("Ljava/lang/Object;");

   if (jt.getInterfaces() != null) {
      for (JcompType ity : jt.getInterfaces()) {
	 addTypeSignature(ity,buf);
       }
    }
   return buf.toString();
}



private void addTypeParametersSignature(List<?> tps,StringBuffer buf)
{
   if (tps.size() > 0) {
      buf.append("<");
      for (Object o : tps) {
	 TypeParameter tp = (TypeParameter) o;
	 addTypeParameterSignature(tp,buf);
       }
      buf.append(">");
    }
}



private void addTypeParameterSignature(TypeParameter tp,StringBuffer buf)
{
   buf.append(tp.getName().getIdentifier());
   buf.append(":");
   for (Object o : tp.typeBounds()) {
      Type t = (Type) o;
      JcompType jt = JcompAst.getJavaType(t);
      if (jt == null) continue;
      if (jt.isClassType()) {
	 addTypeSignature(jt,buf);
       }
    }
   for (Object o : tp.typeBounds()) {
      Type t = (Type) o;
      JcompType jt = JcompAst.getJavaType(t);
      if (jt == null) continue;
      if (jt.isInterfaceType()) {
	 buf.append(":");
	 addTypeSignature(jt,buf);
       }
    }
   if (tp.typeBounds().size() == 0) {
      buf.append("Ljava/lang/Object;");
    }
}



private void addTypeSignature(JcompType jt,StringBuffer buf)
{
   buf.append(jt.getJavaTypeName());
}



/********************************************************************************/
/*										*/
/*	Visitor for defining types						*/
/*										*/
/********************************************************************************/

private class TypeFinder extends ASTVisitor {

   private Map<String,String> specific_names;
   private List<String> prefix_set;
   private String type_prefix;
   private JcompType outer_type;
   private int anon_counter;

   TypeFinder(Map<String,String> s,List<String> p) {
      specific_names = s;
      prefix_set = p;
      type_prefix = null;
      anon_counter = 0;
      outer_type = null;
    }

   @Override public boolean visit(PackageDeclaration t) {
      String pnm = t.getName().getFullyQualifiedName();
      type_prefix = pnm;
      addPrefixTypes(pnm);
      return false;
    }

   @Override public boolean visit(ImportDeclaration t) {
      String inm = t.getName().getFullyQualifiedName();
      if (t.isStatic()) ;
      else if (t.isOnDemand()) addPrefixTypes(inm);
      else addSpecificType(inm);
      return false;
    }

   @Override public boolean visit(TypeDeclaration t) {
      JcompType out = outer_type;
      String nm = t.getName().getIdentifier();
      if (type_prefix != null) nm = type_prefix + "." + nm;
      addSpecificType(nm);
      type_prefix = nm;
      JcompType jt = defineUserType(nm,t.isInterface(),false,false,true);
      outer_type = jt;
      setJavaType(t,jt);
      JcompSymbol js = JcompSymbol.createSymbol(t);
      jt.setDefinition(js);
      if (out != null && jt != null) jt.setOuterType(out);
      return true;
    }

   @Override public void endVisit(TypeDeclaration t) {
      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);
      if (outer_type != null) {
	 outer_type = outer_type.getOuterType();
       }
    }

   @Override public boolean visit(EnumDeclaration t) {
      JcompType out = outer_type;
      String nm = t.getName().getIdentifier();
      if (type_prefix != null) nm = type_prefix + "." + nm;
      addSpecificType(nm);
      type_prefix = nm;
      JcompType jt = defineUserType(nm,false,true,false,true);
      JcompType etyp = findSystemType("java.lang.Enum");
      jt.setSuperType(etyp);
      outer_type = jt;
      setJavaType(t,jt);
      JcompSymbol js = JcompSymbol.createSymbol(t);
      jt.setDefinition(js);
      if (out != null && jt != null) jt.setOuterType(out);
      return true;
    }

   @Override public void endVisit(EnumDeclaration t) {
      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);
      if (outer_type != null) {
	 outer_type = outer_type.getOuterType();
       }
    }

   @Override public boolean visit(AnnotationTypeDeclaration t) {
      String nm = t.getName().getIdentifier();
      if (type_prefix != null) nm = type_prefix + "." + nm;
      addSpecificType(nm);
      type_prefix = nm;
      JcompType jt = defineUserType(nm,false,false,true,true);
      setJavaType(t,jt);
      JcompSymbol js = JcompSymbol.createSymbol(t);
      jt.setDefinition(js);
      return true;
    }

   @Override public void endVisit(AnnotationTypeDeclaration t) {
      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);
    }

   @Override public boolean visit(AnonymousClassDeclaration t) {
      String anm = "$00" + (++anon_counter);
      String nm = type_prefix + "." + anm;
      addSpecificType(nm);
      type_prefix = nm;
      JcompType jt = defineUserType(nm,false,false,false,true);
      setJavaType(t,jt);
      return true;
    }

   @Override public void endVisit(AnonymousClassDeclaration t) {
      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);
    }

   @Override public boolean visit(TypeParameter t) {
      String nm = type_prefix + "." + t.getName().getIdentifier();
      JcompType jt = JcompType.createVariableType(nm);
      jt = fixJavaType(jt);
      setJavaType(t.getName(),jt);
      setJavaType(t,jt);
      addSpecificType(nm);
      return true;
    }

   private void addPrefixTypes(String pnm) {
      pnm += ".";
      prefix_set.add(0,pnm);
    }

   private void addSpecificType(String nm) {
      String sfx = nm;
      int idx = nm.lastIndexOf('.');
      if (idx >= 0) sfx = nm.substring(idx+1);
      specific_names.put(sfx,nm);
    }

}	// end of subclass TypeFinder



/********************************************************************************/
/*										*/
/*	Visitor for defining and looking up types				*/
/*										*/
/********************************************************************************/

private abstract class AbstractTypeSetter extends ASTVisitor {

   private Map<String,String> known_names;
   private Map<String,String> specific_names;
   private List<String> prefix_set;
   protected JcompType outer_type;
   protected String type_prefix;
   protected boolean canbe_type;
   protected boolean set_types;

   AbstractTypeSetter(Map<String,String> k,Map<String,String> s,List<String> p) {
      known_names = k;
      specific_names = s;
      prefix_set = p;
      type_prefix = null;
      outer_type = null;
      canbe_type = false;
      set_types = false;
    }

   @Override public void endVisit(PrimitiveType t) {
      String nm = t.getPrimitiveTypeCode().toString().toLowerCase();
      JcompType jt = type_map.get(nm);
      if (jt == null)
	 System.err.println("PRIMITIVE TYPE " + nm + " NOT FOUND");
      setJavaType(t,jt);
    }

   @Override public boolean visit(ParameterizedType t) {
      if (!set_types) return false;
      boolean fg = canbe_type;
      canbe_type = set_types;
      visitItem(t.getType());
      visitList(t.typeArguments());
      canbe_type = fg;

      JcompType jt0 = JcompAst.getJavaType(t.getType());
      List<JcompType> ljt = new ArrayList<JcompType>();
      for (Iterator<?> it = t.typeArguments().iterator(); it.hasNext(); ) {
	 Type t1 = (Type) it.next();
	 JcompType jt2 = JcompAst.getJavaType(t1);
	 if (jt2 == null) jt2 = findType(TYPE_ERROR);
	 ljt.add(jt2);
       }

      JcompType jt1 = jt0;
      if (ljt.size() > 0) {
	 jt1 = JcompType.createParameterizedType(jt0,ljt,null,JcompTyper.this);
       }
      setJavaType(t,jt1);
      return false;
    }

   @Override public void endVisit(TypeParameter t) {
      JcompType vart = JcompAst.getJavaType(t);
      for (Object o : t.typeBounds()) {
	 Type tt = (Type) o;
	 JcompType jt = JcompAst.getJavaType(tt);
	 if (jt != null) {
	    if (jt.isInterfaceType()) vart.addInterface(jt);
	    else vart.setSuperType(jt);
	  }
       }
    }

   @Override public boolean visit(ArrayType t) {
      if (!set_types) return false;
      canbe_type = true;
      return true;
    }

   @Override public void endVisit(ArrayType t) {
      if (set_types) {
	 JcompType jt = JcompAst.getJavaType(t.getElementType());
	 for (int i = 0; i < t.getDimensions(); ++i) {
	    jt = JcompType.createArrayType(jt);
	    jt = fixJavaType(jt);
	  }
	 setJavaType(t,jt);
       }
    }

   @Override public boolean visit(UnionType t) {
      if (!set_types) return false;
      canbe_type = true;
      return true;
    }

   @Override public void endVisit(UnionType t) {
      List<JcompType> tps = new ArrayList<JcompType>();
      for (Object o : t.types()) {
	 JcompType jt = JcompAst.getJavaType((ASTNode) o);
	 if (jt != null) tps.add(jt);
       }
      JcompType ut = JcompType.createUnionType(tps);
      ut = fixJavaType(ut);
      setJavaType(t,ut);
    }

   @Override public void endVisit(IntersectionType t) {
      List<JcompType> tps = new ArrayList<JcompType>();
      for (Object o : t.types()) {
	 JcompType jt = JcompAst.getJavaType((ASTNode) o);
	 if (jt != null) tps.add(jt);
       }
      JcompType ut = JcompType.createIntersectionType(tps);
      ut = fixJavaType(ut);
      setJavaType(t,ut);
    }

   @Override public boolean visit(QualifiedType t) {
      if (!set_types) return false;
      canbe_type = true;
      return true;
    }

   @Override public void endVisit(QualifiedType t) {
      JcompType t0 = JcompAst.getJavaType(t.getQualifier());
      JcompType qualt = t0;
      if (qualt.isParameterizedType())
	 qualt = qualt.getBaseType();
      String t1 = qualt.getName() + "." + t.getName().getIdentifier();
      JcompType jt = lookupType(t1);
      SortedMap<String,JcompType> outermap = t0.getOuterComponents();
      if (outermap != null && outermap.size() > 0) {
	 JcompType njt = JcompType.createParameterizedType(jt,new ArrayList<>(),outermap,JcompTyper.this);
	 njt = fixJavaType(njt);
	 jt = njt;
       }
      setJavaType(t,jt);
      setJavaType(t.getName(),jt);
    }

   @Override public boolean visit(SimpleType t) {
      if (!set_types) return false;
      canbe_type = true;
      visitItem(t.getName());
      canbe_type = false;
      JcompType jt = forceType(t.getName());
      setJavaType(t,jt);
      visitList(t.annotations());
      return false;
    }

   @Override public void endVisit(WildcardType t) {
      setJavaType(t,type_map.get("?"));
    }

   @Override public boolean visit(QualifiedName t) {
      if (!set_types) return false;
      boolean ocbt = canbe_type;
      canbe_type = true;
      visitItem(t.getQualifier());
      canbe_type = ocbt;
      String s = t.getFullyQualifiedName();
      JcompType qt = JcompAst.getJavaType(t.getQualifier());
      if (qt != null) {
	 s = qt.getName() + "." + t.getName().getIdentifier();
       }
      JcompType jt = lookupPossibleType(s);
      if (jt != null) {
	 setJavaType(t,jt);
	 // if (JcompAst.getJavaType(t.getName()) == null)
	    // setJavaType(t.getName(),jt);
       }
      return false;
    }

   @Override public boolean visit(NameQualifiedType t) {
      if (!set_types) return false;
      boolean ocbt = canbe_type;
      canbe_type = set_types;
      visitItem(t.getQualifier());
      canbe_type = ocbt;
      String s = t.getName().getFullyQualifiedName();
      JcompType jt = lookupType(s);
      if (jt != null) {
	 setJavaType(t,jt);
	 setJavaType(t.getName(),jt);
       }

      visitList(t.annotations());

      return false;
    }

   @Override public void endVisit(SimpleName t) {
      if (set_types) {
	 String s = t.getFullyQualifiedName();
	 JcompType jt = lookupPossibleType(s);
	 if (jt != null) setJavaType(t,jt);
       }
    }

   protected void visitItem(ASTNode n) {
      if (n != null) {
	 boolean cbt = canbe_type;
	 n.accept(this);
	 canbe_type = cbt;
       }
    }

   protected void visitList(List<?> l) {
      if (l == null) return;
      boolean cbt = canbe_type;
      for (Iterator<?> it = l.iterator(); it.hasNext(); ) {
	 ASTNode n = (ASTNode) it.next();
	 n.accept(this);
	 canbe_type = cbt;
       }
    }

   protected JcompType lookupType(String nm) {
      String s = findTypeName(nm,true);
      JcompType jt = type_map.get(s);
      if (jt != null) return jt;

      jt = findSystemType(s);
      if (jt != null && !canbe_type) {
	 IvyLog.logW("JCOMP","FOUND UNEXPECTED TYPE " + s);
       }

      if (jt == null) {
	 jt = fixJavaType(JcompType.createCompiledClassType(s));
	 jt.setUndefined(true);
	 jt.setSuperType(type_map.get("java.lang.Object"));
       }

      return jt;
    }

   protected JcompType lookupPossibleType(String nm) {
      String s = findTypeName(nm,false);
      if (s == null) return null;
      JcompType jt = type_map.get(s);
      if (jt != null) return jt;

      JcompType vjt = findTypeVariable(nm);
      if (vjt != null) return vjt;

      if (jt == null && canbe_type) {
	 jt = findSystemType(s);
       }

      return jt;
    }

   protected JcompType forceType(Name n) {
      JcompType jt = JcompAst.getJavaType(n);
      if (jt == null) {
	 String qnm = n.getFullyQualifiedName();
	 boolean fg = canbe_type;
	 canbe_type = true;
	 jt = lookupType(qnm);
	 canbe_type = fg;
	 setJavaType(n,jt);
       }
      return jt;
    }

   protected String findTypeName(String nm,boolean force) {
      if (outer_type != null) {
	 String onm = findOuterTypeName(nm);
	 if (onm != null) return onm;
       }

      if (specific_names != null) {
	 String spn1 = specific_names.get(nm);
	 if (spn1 != null) return spn1;
       }
      String spn = checkTypeName(nm,nm);
      if (spn != null) return spn;

      int idx = nm.lastIndexOf('.');            // look for subtypes
      if (idx >= 0) {
	 String pfx = nm.substring(0,idx);
	 String s0 = findTypeName(pfx,false);
	 if (s0 != null) {
	    String s = s0 + nm.substring(idx);
	    String s1 = checkTypeName(nm,s);
	    if (s1 != null) {
	       known_names.put(nm,s1);
	       return s1;
	     }
	    JcompType jt = findSystemType(s0);
	    if (jt != null) {
	       String s2 = checkParentType(nm.substring(idx+1),jt,false,new HashSet<>());
	       if (s2 != null) {
		  known_names.put(nm,s2);
		  return s2;
		}
	     }
	  }
       }

      for (String p : prefix_set) {		// look for on-demand types
	 String t = p + nm;
	 spn = checkTypeName(nm,t);
	 if (spn != null) return spn;
       }

      if (!force) return null;

      if (type_prefix != null && idx < 0) {
	 spn = type_prefix + "." + nm;
       }
      else spn = nm;

      // known_names.put(nm,spn);

      return spn;
    }


   protected JcompType findTypeVariable(String nm)
   {
      for (JcompType ot = outer_type; ot != null; ot = ot.getOuterType()) {
	 String sgn = ot.getSignature();
	 if (sgn == null) continue;
	 // should create a new type variable for nm from the signature and return it
	 // need to have super/interface types set appropriately
	 List<String> vnms = JcompGenerics.getTypeVariableNames(sgn,false);
	 if (vnms != null && vnms.contains(nm)) {
	    return JcompType.createVariableType(nm);
	  }
       }

      return null;
   }



   protected String findOuterTypeName(String nm)
   {
      if (!canbe_type) return null;

      String s1 = checkParentType(nm,outer_type,true,new HashSet<JcompType>());
      if (s1 != null) return s1;

      return null;
   }


   protected String checkParentType(String nm,JcompType ty,boolean samefile,Set<JcompType> done)
   {
      while (ty.isParameterizedType()) ty = ty.getBaseType();
      if (!done.add(ty)) return null;

      String basenm = ty.getJavaTypeName();
      basenm = basenm.substring(1,basenm.length()-1).replace("/",".");
      String nm1 = basenm.replace("$",".");
      String nm2 = nm1 + "." + nm;
      String ns4 = checkTypeName(null,nm2);
      if (ns4 != null) return ns4;

      String s1 = basenm + "$" + nm;
      String s2 = checkTypeName(null,s1);
      if (s2 != null) return s2;

      JcompType stype = ty.getSuperType();
      if (stype != null) {
	 String s3 = checkParentType(nm,stype,false,done);
	 if (s3 != null) return s3;
       }

      if (ty.getInterfaces() != null) {
	 for (JcompType jti : ty.getInterfaces()) {
	    String s4 = checkParentType(nm,jti,false,done);
	    if (s4 != null) return s4;
	  }
       }

      if (samefile) {
	 JcompType outty = ty.getOuterType();
	 if (outty != null) {
	    String s5 = checkParentType(nm,outty,true,done);
	    if (s5 != null) return s5;
	  }
       }

      return null;
   }


   protected String checkTypeName(String nm,String tnm)
   {
      String spn = known_names.get(tnm);
      if (spn != null) {
	 if (nm != null) known_names.put(nm,spn);
	 return spn;
       }

      if (canbe_type) {
	 JcompType t = findSystemType(tnm);
	 if (t != null) {
	    tnm = t.getName();
	    if (nm != null && !t.isUndefined()) known_names.put(nm,tnm);
	    return tnm;
	  }
       }

      return null;
   }

}	// end of subclass AbstractTypeSetter



/********************************************************************************/
/*										*/
/*	Type Pre-Setter -- handle type information				*/
/*										*/
/********************************************************************************/

private class TypePreSetter extends AbstractTypeSetter {

   TypePreSetter(Map<String,String> k,Map<String,String> s,List<String> p) {
      super(k,s,p);
    }

   @Override public boolean visit(PackageDeclaration t) {
      String pnm = t.getName().getFullyQualifiedName();
      type_prefix = pnm;
      return false;
    }

   @Override public boolean visit(TypeDeclaration t) {
      String nm = t.getName().getIdentifier();
      if (type_prefix != null) nm = type_prefix + "." + nm;
      type_prefix = nm;
      JcompType out = outer_type;
      JcompType jt = JcompAst.getJavaType(t);
      outer_type = jt;

      set_types = true;
      canbe_type = true;
      visitItem(t.getSuperclassType());
      visitList(t.typeParameters());
      visitList(t.superInterfaceTypes());
      visitItem(t.getName());
      canbe_type = false;
      set_types = false;

      if (jt != null) {
	 if ((t.getModifiers() & Modifier.ABSTRACT) != 0) {
	    jt.setAbstract(true);
	  }
	 if (out != null && (t.getModifiers() & Modifier.STATIC) == 0 && !out.isInterfaceType()) {
	    jt.setInnerNonStatic(true);
	  }
	
	 if (type_prefix != null) nm = type_prefix + "." + nm;
	 Type sty = t.getSuperclassType();
	 if (sty != null) {
	    JcompType suptyp = JcompAst.getJavaType(sty);
	    jt.setSuperType(suptyp);
	  }
	 for (Iterator<?> it = t.superInterfaceTypes().iterator(); it.hasNext(); ) {
	    Type ity = (Type) it.next();
	    if (ity != null) {
	       JcompType inttyp = JcompAst.getJavaType(ity);
	       if (!inttyp.isUndefined()) jt.addInterface(inttyp);
	       // else System.err.println("Undefined pretype " + ity);
	     }
	  }
	
	 String sgn = getClassSignature(t,jt);
	 jt.setSignature(sgn);
       }

      visitList(t.bodyDeclarations());
      // visitList(t.modifiers());

      outer_type = out;

      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);

      return false;
    }



   @Override public boolean visit(EnumDeclaration t) {
      String nm = t.getName().getIdentifier();
      if (type_prefix != null) nm = type_prefix + "." + nm;
      type_prefix = nm;

      set_types = true;
      canbe_type = true;
      visitList(t.superInterfaceTypes());
      visitItem(t.getName());
      canbe_type = false;
      set_types = false;
      visitList(t.enumConstants());
      visitList(t.bodyDeclarations());

      // visitList(t.modifiers());

      JcompType jt = JcompAst.getJavaType(t);
      for (Iterator<?> it = t.superInterfaceTypes().iterator(); it.hasNext(); ) {
	 Type ity = (Type) it.next();
	 jt.addInterface(JcompAst.getJavaType(ity));
       }

      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);

      return false;
    }

   @Override public boolean visit(AnonymousClassDeclaration t) {
      JcompType jt = JcompAst.getJavaType(t);
      type_prefix = jt.getName();

      JcompType out = outer_type;
      outer_type = jt;

      visitList(t.bodyDeclarations());

      if (out != null && jt != null) {
	 jt.setOuterType(out);
       }

      outer_type = out;

      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);

      if (t.getParent() instanceof ClassInstanceCreation) {
	 ClassInstanceCreation cic = (ClassInstanceCreation) t.getParent();
	 Type sty = cic.getType();
	 JcompType xjt = JcompAst.getJavaType(sty);
	 if (xjt != null) {
	    if (xjt.isInterfaceType()) {
	       jt.setSuperType(findType("java.lang.Object"));
	       jt.addInterface(xjt);
	     }
	    else jt.setSuperType(xjt);
	  }
       }
      else if (t.getParent() instanceof EnumConstantDeclaration) {
	 EnumConstantDeclaration ecd = (EnumConstantDeclaration) t.getParent();
	 JcompType sty = JcompAst.getJavaType(ecd);
	 if (sty == null) {
	    EnumDeclaration ed = (EnumDeclaration) ecd.getParent();
	    sty = JcompAst.getJavaType(ed);
	  }
	 jt.setSuperType(sty);
       }

      return false;
    }

   @Override public boolean visit(MethodDeclaration md) {
      return false;
    }

   @Override public boolean visit(FieldDeclaration md) {
      return false;
    }

}	// end of inner class TypePreSetter




/********************************************************************************/
/*										*/
/*	Type Setter -- handle setting types throughout				*/
/*										*/
/********************************************************************************/

private class TypeSetter extends AbstractTypeSetter {

   TypeSetter(Map<String,String> k,Map<String,String> s,List<String> p) {
      super(k,s,p);
      set_types = true;
    }

   @Override public boolean visit(PackageDeclaration t) {
      String pnm = t.getName().getFullyQualifiedName();
      type_prefix = pnm;
      return false;
    }

   @Override public boolean visit(ImportDeclaration t) {
      canbe_type = true;
      visitItem(t.getName());
      canbe_type = false;
      return false;
    }

   @Override public boolean visit(TypeDeclaration t) {
      String nm = t.getName().getIdentifier();
      if (type_prefix != null) nm = type_prefix + "." + nm;
      type_prefix = nm;
      JcompType out = outer_type;
      JcompType jt = JcompAst.getJavaType(t);
      outer_type = jt;

      canbe_type = true;
      visitList(t.modifiers());
      visitItem(t.getSuperclassType());
      visitList(t.typeParameters());
      visitList(t.superInterfaceTypes());
      visitItem(t.getName());
      canbe_type = false;

      if (jt != null) {
	 if ((t.getModifiers() & Modifier.ABSTRACT) != 0) {
	    jt.setAbstract(true);
	  }
	 if (out != null && (t.getModifiers() & Modifier.STATIC) == 0 && !out.isInterfaceType()) {
	    jt.setInnerNonStatic(true);
	  }
	
	 if (type_prefix != null) nm = type_prefix + "." + nm;
	 Type sty = t.getSuperclassType();
	 if (sty != null) {
	    JcompType suptyp = JcompAst.getJavaType(sty);
	    if (suptyp.isUndefined()) {
	       // System.err.println("Super type is undefined");
	     }
	    jt.setSuperType(suptyp);
	  }
	 for (Iterator<?> it = t.superInterfaceTypes().iterator(); it.hasNext(); ) {
	    Type ity = (Type) it.next();
	    if (ity != null) {
	       JcompType inttyp = JcompAst.getJavaType(ity);
	       if (inttyp.isUndefined()) {
		  // System.err.println("Interface type is undefined");
		}
	       jt.addInterface(inttyp);
	     }
	  }
	
	 String sgn = getClassSignature(t,jt);
	 jt.setSignature(sgn);
       }

      visitList(t.bodyDeclarations());
      // visitList(t.modifiers());

      outer_type = out;

      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);

      return false;
   }



   @Override public boolean visit(EnumDeclaration t) {
      String nm = t.getName().getIdentifier();
      if (type_prefix != null) nm = type_prefix + "." + nm;
      type_prefix = nm;

      canbe_type = true;
      visitList(t.modifiers());
      visitList(t.superInterfaceTypes());
      visitItem(t.getName());
      canbe_type = false;
      visitList(t.enumConstants());
      visitList(t.bodyDeclarations());
      // visitList(t.modifiers());

      JcompType jt = JcompAst.getJavaType(t);
      for (Iterator<?> it = t.superInterfaceTypes().iterator(); it.hasNext(); ) {
	 Type ity = (Type) it.next();
	 jt.addInterface(JcompAst.getJavaType(ity));
       }

      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);

      return false;
    }

   @Override public boolean visit(AnonymousClassDeclaration t) {
      JcompType jt = JcompAst.getJavaType(t);
      type_prefix = jt.getName();

      JcompType out = outer_type;
      outer_type = jt;

      visitList(t.bodyDeclarations());

      if (out != null && jt != null) {
	 jt.setOuterType(out);
       }
      outer_type = out;

      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);

      if (t.getParent() instanceof ClassInstanceCreation) {
	 ClassInstanceCreation cic = (ClassInstanceCreation) t.getParent();
	 Type sty = cic.getType();
	 JcompType xjt = JcompAst.getJavaType(sty);
	 if (xjt != null) {
	    if (xjt.isInterfaceType()) {
	       jt.setSuperType(findType("java.lang.Object"));
	       jt.addInterface(xjt);
	     }
	    else jt.setSuperType(xjt);
	  }
       }
      else if (t.getParent() instanceof EnumConstantDeclaration) {
	 EnumConstantDeclaration ecd = (EnumConstantDeclaration) t.getParent();
	 JcompType sty = JcompAst.getJavaType(ecd);
	 if (sty == null) {
	    EnumDeclaration ed = (EnumDeclaration) ecd.getParent();
	    sty = JcompAst.getJavaType(ed);
	  }
	 jt.setSuperType(sty);
       }

      return false;
    }



   @Override public boolean visit(ExpressionMethodReference t) {
      boolean ocbt = canbe_type;
      canbe_type = set_types;
      visitItem(t.getExpression());
      canbe_type = ocbt;
      return false;
    }


   @Override public boolean visit(MethodDeclaration t) {
      canbe_type = set_types;
      visitItem(t.getReturnType2());
      visitList(t.thrownExceptionTypes());
      visitList(t.typeParameters());
      canbe_type = false;
      visitList(t.parameters());
      if (t.isConstructor()) canbe_type = set_types;
      visitItem(t.getName());
      canbe_type = false;
      visitItem(t.getBody());
      visitList(t.modifiers());

      JcompType jt = JcompAst.getJavaType(t.getReturnType2());
      if (t.isConstructor()) jt = VOID_TYPE;
      List<JcompType> ljt = new ArrayList<>();
      for (Iterator<?> it = t.parameters().iterator(); it.hasNext(); ) {
	 SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
	 JcompType pjt = JcompAst.getJavaType(svd);
	 if (pjt == null) pjt = type_map.get(TYPE_ERROR);
	 ljt.add(pjt);
       }
      JcompType clstyp = JcompAst.getJavaType(t.getParent());
      if (clstyp != null && clstyp.needsOuterClass() && t.isConstructor()) {
	 JcompType outer = clstyp.getOuterType();
	 if (outer != null) ljt.add(0,outer);
       }

      String sgn = getMethodSignature(t,jt,ljt);
      JcompType mt = createMethodType(jt,ljt,t.isVarargs(),sgn);
      setJavaType(t,mt);

      return false;
    }


   @Override public boolean visit(AnnotationTypeMemberDeclaration t) {
      canbe_type = set_types;
      visitItem(t.getType());
      canbe_type = false;
      visitItem(t.getName());
      visitItem(t.getDefault());
      JcompType jt = JcompAst.getJavaType(t.getType());
      List<JcompType> ljt = new ArrayList<>();
      JcompType mt = createMethodType(jt,ljt,false,null);
      setJavaType(t,mt);
      return false;
   }

   @Override public boolean visit(FieldDeclaration t) {
      canbe_type = set_types;
      visitItem(t.getType());
      canbe_type = false;
      visitList(t.fragments());
      visitList(t.modifiers());
      return false;
    }

   @Override public boolean visit(VariableDeclarationStatement t) {
      canbe_type = set_types;
      visitItem(t.getType());
      canbe_type = false;
      visitList(t.fragments());
      visitList(t.modifiers());
      return false;
    }

   @Override public boolean visit(VariableDeclarationExpression t) {
      canbe_type = set_types;
      visitItem(t.getType());
      canbe_type = false;
      visitList(t.fragments());
      // visitList(t.modifiers());
      return false;
    }

   @Override public boolean visit(SingleVariableDeclaration t) {
      canbe_type = set_types;
      visitItem(t.getType());
      canbe_type = false;
      visitItem(t.getName());
      visitItem(t.getInitializer());
      JcompType jt = JcompAst.getJavaType(t.getType());
      for (int i = 0; i < t.getExtraDimensions(); ++i) jt = findArrayType(jt);
      if (t.isVarargs()) jt = findArrayType(jt);
      JcompAst.setJavaType(t,jt);
      visitList(t.modifiers());
      return false;
    }

   @Override public boolean visit(CastExpression t) {
      canbe_type = set_types;
      visitItem(t.getType());
      canbe_type = false;
      visitItem(t.getExpression());
      return false;
    }

   @Override public boolean visit(ClassInstanceCreation t) {
      canbe_type = set_types;
      visitItem(t.getType());
      visitList(t.typeArguments());
      canbe_type = false;
      visitItem(t.getExpression());
      visitList(t.arguments());
      visitItem(t.getAnonymousClassDeclaration());
      return false;
    }

   @Override public boolean visit(FieldAccess t) {
      canbe_type = set_types;
      visitItem(t.getExpression());
      canbe_type = false;
      visitItem(t.getName());
      return false;
    }

   @Override public boolean visit(InstanceofExpression t) {
      visitItem(t.getLeftOperand());
      canbe_type = set_types;
      visitItem(t.getRightOperand());
      canbe_type = false;
      return false;
    }

   @Override public boolean visit(MethodInvocation t) {
      canbe_type = set_types;
      visitItem(t.getExpression());
      visitList(t.typeArguments());
      canbe_type = false;
      visitItem(t.getName());
      visitList(t.arguments());
      return false;
    }

   @Override public boolean visit(SuperFieldAccess t) {
      canbe_type = set_types;
      visitItem(t.getQualifier());
      canbe_type = false;
      visitItem(t.getName());
      return false;
    }

   @Override public boolean visit(SuperMethodInvocation t) {
      canbe_type = set_types;
      visitItem(t.getQualifier());
      visitList(t.typeArguments());
      canbe_type = false;
      visitItem(t.getName());
      visitList(t.arguments());
      return false;
    }

   @Override public boolean visit(ThisExpression t) {
      canbe_type = set_types;
      visitItem(t.getQualifier());
      canbe_type = false;
      return false;
    }

   @Override public boolean visit(TypeLiteral t) {
      canbe_type = set_types;
      visitItem(t.getType());
      canbe_type = false;
      return false;
    }

   @Override public boolean visit(MarkerAnnotation t) {
      canbe_type = set_types;
      visitItem(t.getTypeName());
      forceType(t.getTypeName());

      canbe_type = false;
      return false;
    }

   @Override public boolean visit(NormalAnnotation t) {
      canbe_type = set_types;
      visitItem(t.getTypeName());
      forceType(t.getTypeName());
      canbe_type = false;
      visitList(t.values());
      return false;
    }

   @Override public boolean visit(SingleMemberAnnotation t) {
      canbe_type = set_types;
      visitItem(t.getTypeName());
      forceType(t.getTypeName());
      canbe_type = false;
      visitItem(t.getValue());
      return false;
    }

}	// end of inner class TypeSetter









}	// end of class JcompTyper





/* end of JcompTyper.java */
