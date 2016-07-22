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



import org.eclipse.jdt.core.dom.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class JcompTyper implements JcompConstants {



/********************************************************************************/
/*										*/
/*	Instances of this class are only used by a single thread at one time;	*/
/*										*/
/********************************************************************************/



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<String,String> initial_types;
private Map<String,JcompType> type_map;

private JcompContext	 type_context;

private static Map<String,JcompType> system_types = new HashMap<String,JcompType>();

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

   initial_types = new HashMap<String,String>();

   type_map = new ConcurrentHashMap<String,JcompType>();

   findSystemType("java.lang.Object");
   findSystemType("java.lang.Enum");
   findSystemType("java.lang.String");
   findSystemType("java.lang.Class");

   definePrimitive(PrimitiveType.BYTE,"Byte");
   definePrimitive(PrimitiveType.SHORT,"Short");
   definePrimitive(PrimitiveType.CHAR,"Character");
   definePrimitive(PrimitiveType.INT,"Integer");
   definePrimitive(PrimitiveType.LONG,"Long");
   definePrimitive(PrimitiveType.FLOAT,"Float");
   definePrimitive(PrimitiveType.DOUBLE,"Double");
   definePrimitive(PrimitiveType.BOOLEAN,"Boolean");
   definePrimitive(PrimitiveType.VOID,null);

   type_map.put(TYPE_ANY_CLASS,JcompType.createAnyClassType());
   type_map.put(TYPE_ERROR,JcompType.createErrorType());

   fixJavaType(JcompType.createVariableType("?"));

   for (String s : BASE_TYPES) {
      initial_types.put(s,s);
    }
}



private void definePrimitive(PrimitiveType.Code pt,String sys)
{
   String nm = pt.toString().toLowerCase();

   synchronized (system_types) {
      JcompType ty = system_types.get(nm);
      if (ty == null) {
	 JcompType jt = null;
	 if (sys != null) {
	    jt = findSystemType("java.lang." + sys);
	  }
	 ty = JcompType.createPrimitiveType(pt,jt);
	 if (jt != null) jt.setAssociatedType(ty);
	 system_types.put(nm,ty);
       }
      else {
	 JcompType jt = null;
	 if (sys != null) {
	    jt = findSystemType("java.lang." + sys);
	    if (jt != null) jt.setAssociatedType(ty);
	  }
       }
      type_map.put(nm,ty);
    }
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

   int idx = nm.indexOf("<");
   if (idx > 0) {
      String t0 = nm.substring(0,idx);
      int idx1 = nm.lastIndexOf(">");
      String t1 = nm.substring(idx+1,idx1);
      return findParameterizedSystemType(t0,t1);
    }

   idx = nm.lastIndexOf("[]");
   if (idx > 0) {
      String nm0 = nm.substring(0,idx);
      JcompType bty = findSystemType(nm0);
      if (bty == null) return null;
      return findArrayType(bty);
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

   return JcompType.createParameterizedType(btyp,argl);
}



Collection<JcompType> getAllTypes()
{
   return type_map.values();
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

   JcompType jt1 = type_map.get(jt.getName());
   if (jt1 != null) jt = jt1;
   else type_map.put(jt.getName(),jt);
   return jt;
}



public JcompType defineUserType(String nm,boolean iface,boolean etype,boolean force)
{
   JcompType jt = type_map.get(nm);
   if (jt != null && jt.isKnownType() && force)
      jt = null;

   if (jt != null) return jt;

   int idx = nm.indexOf(".");
   if (idx >= 0 && !force) {
      String s = nm.substring(0,idx);
      if (known_prefix.contains(s)) {
	 jt = findSystemType(nm);
	 if (jt != null) return jt;
       }
    }
   if (nm.startsWith("edu.brown.cs.s6.runner.Runner")) {
      jt = findSystemType(nm);
      if (jt != null) return jt;
    }

   if (iface) jt = JcompType.createUnknownInterfaceType(nm);
   else if (etype) jt = JcompType.createEnumType(nm);
   else jt = JcompType.createUnknownType(nm);

   type_map.put(nm,jt);

   return jt;
}




/********************************************************************************/
/*										*/
/*	Known class lookup methods						*/
/*										*/
/********************************************************************************/

JcompSymbol lookupKnownField(String cls,String id)
{
   return type_context.defineKnownField(this,cls,id);
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
/*	Methods to assign types to a Jcomp AST				*/
/*										*/
/********************************************************************************/

void assignTypes(JcompProjectImpl root)
{
   Map<ASTNode,Map<String,String>> specmap = new HashMap<ASTNode,Map<String,String>>();
   Map<ASTNode,List<String>> prefmap = new HashMap<ASTNode,List<String>>();

   for (ASTNode cu : root.getTrees()) {
      Map<String,String> specificnames = new HashMap<String,String>();
      specmap.put(cu,specificnames);
      List<String> prefixes = new ArrayList<String>();
      prefixes.add("java.lang.");
      prefmap.put(cu,prefixes);

      TypeFinder tf = new TypeFinder(specificnames,prefixes);
      cu.accept(tf);
    }

   for (ASTNode cu : root.getTrees()) {
      Map<String,String> knownnames = new HashMap<String,String>(initial_types);
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
   while (cs.charAt(i0) != '>') {
      int i1 = cs.indexOf(":",i0);
      String v = cs.substring(i0,i1);
      if (v.equals(var)) {
	 if (ptyp.getComponents().size() >= idx+1) return ptyp.getComponents().get(idx);
       }
      i0 = findTypeEnd(cs,i0) + 1;
      ++idx;
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
/*	Visitor for defining types						*/
/*										*/
/********************************************************************************/

private class TypeFinder extends ASTVisitor {

   private Map<String,String> specific_names;
   private List<String> prefix_set;
   private String type_prefix;
   private int anon_counter;

   TypeFinder(Map<String,String> s,List<String> p) {
      specific_names = s;
      prefix_set = p;
      type_prefix = null;
      anon_counter = 0;
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
      String nm = t.getName().getIdentifier();
      if (type_prefix != null) nm = type_prefix + "." + nm;
      addSpecificType(nm);
      type_prefix = nm;
      JcompType jt = defineUserType(nm,t.isInterface(),false,true);
      setJavaType(t,jt);
      JcompSymbol js = JcompSymbol.createSymbol(t);
      jt.setDefinition(js);
      return true;
    }

   @Override public void endVisit(TypeDeclaration t) {
      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);
    }

   @Override public boolean visit(EnumDeclaration t) {
      String nm = t.getName().getIdentifier();
      if (type_prefix != null) nm = type_prefix + "." + nm;
      addSpecificType(nm);
      type_prefix = nm;
      JcompType jt = defineUserType(nm,false,true,true);
      JcompType etyp = findSystemType("java.lang.Enum");
      jt.setSuperType(etyp);
      setJavaType(t,jt);
      JcompSymbol js = JcompSymbol.createSymbol(t);
      jt.setDefinition(js);
      return true;
    }

   @Override public void endVisit(EnumDeclaration t) {
      int idx = type_prefix.lastIndexOf('.');
      if (idx < 0) type_prefix = null;
      else type_prefix = type_prefix.substring(0,idx);
    }

   @Override public boolean visit(AnonymousClassDeclaration t) {
      String anm = "$00" + (++anon_counter);
      String nm = type_prefix + "." + anm;
      addSpecificType(nm);
      type_prefix = nm;
      JcompType jt = defineUserType(nm,false,false,true);
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
      fixJavaType(jt);
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

private class TypeSetter extends ASTVisitor {

   private Map<String,String> known_names;
   private Map<String,String> specific_names;
   private List<String> prefix_set;
   private JcompType outer_type;
   private String type_prefix;
   private boolean canbe_type;

   TypeSetter(Map<String,String> k,Map<String,String> s,List<String> p) {
      known_names = k;
      specific_names = s;
      prefix_set = p;
      type_prefix = null;
      outer_type = null;
      canbe_type = false;
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
      visitItem(t.getSuperclassType());
      visitList(t.typeParameters());
      visitList(t.superInterfaceTypes());
      visitItem(t.getName());
      canbe_type = false;
      if (t.modifiers().contains(Modifier.ABSTRACT)) {
         jt.setAbstract(true);
       }
   
      if (type_prefix != null) nm = type_prefix + "." + nm;
      Type sty = t.getSuperclassType();
      if (sty != null && jt != null) jt.setSuperType(JcompAst.getJavaType(sty));
      for (Iterator<?> it = t.superInterfaceTypes().iterator(); it.hasNext(); ) {
         Type ity = (Type) it.next();
         if (jt != null) jt.addInterface(JcompAst.getJavaType(ity));
       }
   
      visitList(t.bodyDeclarations());
      // visitList(t.modifiers());
   
      if (out != null && jt != null) jt.setOuterType(out);
   
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

      if (out != null && jt != null) jt.setOuterType(out);

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

   @Override public void endVisit(PrimitiveType t) {
      String nm = t.getPrimitiveTypeCode().toString().toLowerCase();
      JcompType jt = type_map.get(nm);
      if (jt == null)
	  System.err.println("PRIMITIVE TYPE " + nm + " NOT FOUND");
      setJavaType(t,jt);
    }

   @Override public boolean visit(ParameterizedType t) {
      canbe_type = true;
      visitItem(t.getType());
      visitList(t.typeArguments());
      canbe_type = false;
   
      JcompType jt0 = JcompAst.getJavaType(t.getType());
      List<JcompType> ljt = new ArrayList<JcompType>();
      for (Iterator<?> it = t.typeArguments().iterator(); it.hasNext(); ) {
         Type t1 = (Type) it.next();
         JcompType jt2 = JcompAst.getJavaType(t1);
         if (jt2 == null) jt2 = JcompType.createErrorType();
         ljt.add(jt2);
       }
      JcompType jt1 = JcompType.createParameterizedType(jt0,ljt);
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
      canbe_type = true;
      return true;
    }

   @Override public void endVisit(ArrayType t) {
      JcompType jt = JcompAst.getJavaType(t.getElementType());
      for (int i = 0; i < t.getDimensions(); ++i) {
	 jt = JcompType.createArrayType(jt);
	 jt = fixJavaType(jt);
       }
      setJavaType(t,jt);
    }

   @Override public boolean visit(UnionType t) {
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
      setJavaType(t,ut);
    }


   @Override public boolean visit(QualifiedType t) {
      canbe_type = true;
      return true;
    }

   @Override public void endVisit(QualifiedType t) {
      JcompType t0 = JcompAst.getJavaType(t.getQualifier());
      String t1 = t0.getName() + "." + t.getName().getIdentifier();
      JcompType jt = lookupType(t1);
      setJavaType(t,jt);
    }

   @Override public boolean visit(SimpleType t) {
      canbe_type = true;
      visitItem(t.getName());
      canbe_type = false;
      JcompType jt = forceType(t.getName());
      setJavaType(t,jt);
      return false;
    }

   @Override public void endVisit(WildcardType t) {
      setJavaType(t,type_map.get("?"));
    }

   @Override public boolean visit(QualifiedName t) {
      boolean ocbt = canbe_type;
      canbe_type = true;
      visitItem(t.getQualifier());
      canbe_type = ocbt;
      String s = t.getFullyQualifiedName();
      JcompType jt = lookupPossibleType(s);
      if (jt != null) setJavaType(t,jt);
      return false;
    }

   @Override public void endVisit(SimpleName t) {
      String s = t.getFullyQualifiedName();
      JcompType jt = lookupPossibleType(s);
      if (jt != null) setJavaType(t,jt);
    }

   @Override public boolean visit(MethodDeclaration t) {
      canbe_type = true;
      visitItem(t.getReturnType2());
      visitList(t.thrownExceptions());
      visitList(t.typeParameters());
      canbe_type = false;
      visitList(t.parameters());
      if (t.isConstructor()) canbe_type = true;
      visitItem(t.getName());
      canbe_type = false;
      visitItem(t.getBody());
      visitList(t.modifiers());

      JcompType jt = JcompAst.getJavaType(t.getReturnType2());
      List<JcompType> ljt = new ArrayList<JcompType>();
      for (Iterator<?> it = t.parameters().iterator(); it.hasNext(); ) {
	 SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
	 JcompType pjt = JcompAst.getJavaType(svd);
	 if (pjt == null) pjt = type_map.get(TYPE_ERROR);
	 ljt.add(pjt);
       }
      JcompType mt = JcompType.createMethodType(jt,ljt,t.isVarargs());
      mt = fixJavaType(mt);
      setJavaType(t,mt);

      return false;
    }

   @Override public boolean visit(FieldDeclaration t) {
      canbe_type = true;
      visitItem(t.getType());
      canbe_type = false;
      visitList(t.fragments());
      visitList(t.modifiers());
      return false;
    }

   @Override public boolean visit(VariableDeclarationStatement t) {
      canbe_type = true;
      visitItem(t.getType());
      canbe_type = false;
      visitList(t.fragments());
      visitList(t.modifiers());
      return false;
    }

   @Override public boolean visit(VariableDeclarationExpression t) {
      canbe_type = true;
      visitItem(t.getType());
      canbe_type = false;
      visitList(t.fragments());
      // visitList(t.modifiers());
      return false;
    }

   @Override public boolean visit(SingleVariableDeclaration t) {
      canbe_type = true;
      visitItem(t.getType());
      canbe_type = false;
      visitItem(t.getName());
      visitItem(t.getInitializer());
      JcompType jt = JcompAst.getJavaType(t.getType());
      for (int i = 0; i < t.getExtraDimensions(); ++i) jt = findArrayType(jt);
      JcompAst.setJavaType(t,jt);
      // visitList(t.modifiers());
      return false;
    }

   @Override public boolean visit(CastExpression t) {
      canbe_type = true;
      visitItem(t.getType());
      canbe_type = false;
      visitItem(t.getExpression());
      return false;
    }

   @Override public boolean visit(ClassInstanceCreation t) {
      canbe_type = true;
      visitItem(t.getType());
      visitList(t.typeArguments());
      canbe_type = false;
      visitItem(t.getExpression());
      visitList(t.arguments());
      visitItem(t.getAnonymousClassDeclaration());
      return false;
    }

   @Override public boolean visit(FieldAccess t) {
      canbe_type = true;
      visitItem(t.getExpression());
      canbe_type = false;
      visitItem(t.getName());
      return false;
    }

   @Override public boolean visit(InstanceofExpression t) {
      visitItem(t.getLeftOperand());
      canbe_type = true;
      visitItem(t.getRightOperand());
      canbe_type = false;
      return false;
    }

   @Override public boolean visit(MethodInvocation t) {
      canbe_type = true;
      visitItem(t.getExpression());
      visitList(t.typeArguments());
      canbe_type = false;
      visitItem(t.getName());
      visitList(t.arguments());
      return false;
    }

   @Override public boolean visit(SuperFieldAccess t) {
      canbe_type = true;
      visitItem(t.getQualifier());
      canbe_type = false;
      visitItem(t.getName());
      return false;
    }

   @Override public boolean visit(SuperMethodInvocation t) {
      canbe_type = true;
      visitItem(t.getQualifier());
      visitList(t.typeArguments());
      canbe_type = false;
      visitItem(t.getName());
      visitList(t.arguments());
      return false;
    }

   @Override public boolean visit(ThisExpression t) {
      canbe_type = true;
      visitItem(t.getQualifier());
      canbe_type = false;
      return false;
    }

   @Override public boolean visit(TypeLiteral t) {
      canbe_type = true;
      visitItem(t.getType());
      canbe_type = false;
      return false;
    }

   @Override public boolean visit(MarkerAnnotation t) {
      canbe_type = true;
      visitItem(t.getTypeName());
      forceType(t.getTypeName());
      canbe_type = false;
      return false;
    }

   @Override public boolean visit(NormalAnnotation t) {
      canbe_type = true;
      visitItem(t.getTypeName());
      forceType(t.getTypeName());
      canbe_type = false;
      visitList(t.values());
      return false;
    }

   @Override public boolean visit(SingleMemberAnnotation t) {
      canbe_type = true;
      visitItem(t.getTypeName());
      forceType(t.getTypeName());
      canbe_type = false;
      visitItem(t.getValue());
      return false;
    }

   private void visitItem(ASTNode n) {
      if (n != null) {
	 boolean cbt = canbe_type;
	 n.accept(this);
	 canbe_type = cbt;
       }
    }

   private void visitList(List<?> l) {
      if (l == null) return;
      boolean cbt = canbe_type;
      for (Iterator<?> it = l.iterator(); it.hasNext(); ) {
	 ASTNode n = (ASTNode) it.next();
	 n.accept(this);
	 canbe_type = cbt;
       }
    }

   private JcompType lookupType(String nm) {
      String s = findTypeName(nm,true);
      JcompType jt = type_map.get(s);
      if (jt != null) return jt;

      jt = findSystemType(s);
      jt = findSystemType(s);
      if (jt != null && !canbe_type) {
	 System.err.println("FOUND UNEXPECTED TYPE " + s);
       }

      if (jt == null) {
	 jt = fixJavaType(JcompType.createUnknownType(nm));
	 jt.setUndefined(true);
	 jt.setSuperType(type_map.get("java.lang.Object"));
       }

      return jt;
    }

   private JcompType lookupPossibleType(String nm) {
      String s = findTypeName(nm,false);
      if (s == null) return null;
      JcompType jt = type_map.get(s);
      if (jt == null && canbe_type) {
	 jt = findSystemType(s);
       }
      return jt;
    }

   private JcompType forceType(Name n) {
      JcompType jt = JcompAst.getJavaType(n);
      if (jt == null) {
	 String qnm = n.getFullyQualifiedName();
	 jt = lookupType(qnm);
	 setJavaType(n,jt);
       }
      return jt;
    }

   private String findTypeName(String nm,boolean force) {
      String spn = specific_names.get(nm);
      if (spn != null) return spn;
      spn = known_names.get(nm);
      if (spn != null) return spn;

      if (canbe_type && findSystemType(nm) != null) {
	 known_names.put(nm,nm);
	 return nm;
       }

      int idx = nm.lastIndexOf('.');            // look for subtypes
      if (idx >= 0) {
	 String pfx = nm.substring(0,idx);
	 String s = findTypeName(pfx,false);
	 if (s != null) {
	    s += nm.substring(idx);
	    known_names.put(nm,s);
	    return s;
	  }
       }

      for (String p : prefix_set) {		// look for on-demand types
	 String t = p + nm;
	 spn = known_names.get(t);
	 if (spn != null) {
	    known_names.put(nm,spn);
	    return spn;
	  }
	 if (canbe_type && findType(t) != null) {
	    known_names.put(nm,t);
	    return t;
	  }
	 if (canbe_type && findSystemType(t) != null) {
	    known_names.put(nm,t);
	    return t;
	  }
       }

      if (outer_type != null && canbe_type) {
	 JcompType stype = outer_type.getSuperType();
	 if (stype != null) {
	    while (stype.isParameterizedType()) stype = stype.getBaseType();
	    String t1 = stype.getName() + "." + nm;
	    JcompType jt1 = findType(t1);
	    if (jt1 != null) return jt1.getName();
	    JcompType jt2 = findSystemType(t1);
	    if (jt2 != null) return jt2.getName();
	  }
	 if (outer_type.getInterfaces() != null) {
	    for (JcompType jti : outer_type.getInterfaces()) {
	       while (jti.isParameterizedType()) jti = jti.getBaseType();
	       String t1 = jti.getName() + "." + nm;
	       JcompType jt1 = findType(t1);
	       if (jt1 != null) return jt1.getName();
	       JcompType jt2 = findSystemType(t1);
	       if (jt2 != null) return jt2.getName();
	     }
	  }
       }

      if (!force) return null;

      if (type_prefix != null && idx < 0) {
	 spn = type_prefix + "." + nm;
       }
      else spn = nm;
      known_names.put(nm,spn);

      return spn;
    }

}	// end of subclass TypeSetter




}	// end of class JcompTyper





/* end of JcompTyper.java */
