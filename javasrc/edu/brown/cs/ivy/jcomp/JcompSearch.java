/********************************************************************************/
/*										*/
/*		JcompSearch.java						*/
/*										*/
/*	Various search functions for Java programs				*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 * This program and the accompanying materials are made available under the	 *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at								 *
 *	http://www.eclipse.org/legal/epl-v10.html				 *
 *										 *
 ********************************************************************************/

/* SVN: $Id: JcompSearch.java,v 1.5 2019/04/25 20:11:10 spr Exp $ */



package edu.brown.cs.ivy.jcomp;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;



class JcompSearch implements JcompSearcher, JcompConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcompProjectImpl	jcomp_root;
private Set<JcompSymbol>	match_symbols;
private List<SearchResult>	result_map;
private JcompFile		current_file;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompSearch(JcompProjectImpl root)
{
   jcomp_root = root;
   match_symbols = new HashSet<JcompSymbol>();
   result_map = new ArrayList<SearchResult>();
   current_file = null;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

void setFile(JcompFile jf) 		                { current_file = jf; }

@Override public List<SearchResult> getMatches() 	{ return result_map; }
@Override public Set<JcompSymbol> getSymbols()		{ return match_symbols; }



/********************************************************************************/
/*										*/
/*	Search for symbols							*/
/*										*/
/********************************************************************************/

ASTVisitor getFindSymbolsVisitor(String pat,String kind)
{
   if (pat == null) pat = "*";

   String p1 = pat;
   String p2 = null;
   String p3 = null;
   String p4 = null;
   EnumSet<JcompSymbolKind> kindset = EnumSet.noneOf(JcompSymbolKind.class);

   switch (kind) {
      case "TYPE" :
      case "CLASS" :
      case "ENUM" :
      case "INTERFACE" :
      case "CLASS&ENUM" :
      case "CLASS&INTERFACE" :
	 int idx1 = pat.indexOf("<");
	 int idx2 = findEndOfTypes(pat,idx1);
	 if (idx1 > 0 && idx2 > 0) {
	    p1 = pat.substring(0,idx1);
	    p2 = pat.substring(idx1,idx2+1);
	  }
	 break;
      case "METHOD" :
      case "CONSTRUCTOR" :
	int idx3 = pat.indexOf("<");
	int idx4 = pat.indexOf("(");
	if (idx3 >= 0 && (idx4 < 0 || idx3 < idx4) &&
	      (idx3 == 0 || pat.charAt(idx3-1) == '.')) {
	   int idx6 = findEndOfTypes(pat,idx3);
	   if (idx6 > 0) {
	      p2 = pat.substring(idx3,idx6+1);
	      p1 = p1.substring(idx6+1);
	      idx4 = p1.indexOf("(");
	    }
	 }
	if (idx4 >= 0) {
	   int idx5 = p1.indexOf(")");
	   if (idx5 > 0 && idx5 > idx4) {
	      p3 = p1.substring(idx4,idx5+1);
	      String rest = null;
	      if (idx5+1 < p1.length()) rest = p1.substring(idx5+1).trim();
	      if (rest != null && rest.length() == 0) rest = null;
	      p1 = p1.substring(0,idx4);
	      if (rest != null) p1 += " " + rest;
	    }
	 }
	int idx7 = p1.indexOf(" ");
	if (idx7 > 0) {
	   p4 = p1.substring(idx7+1).trim();
	   p1 = p1.substring(0,idx7);
	 }
	break;
      case "FIELD" :
	 int idx8 = p1.indexOf(" ");
	 if (idx8 > 0) {
	    p4 = p1.substring(idx8+1).trim();
	    p1 = p1.substring(0,idx8);
	  }
	 break;
      case "PACKAGE" :
	 break;
    }

   switch (kind) {
      case "TYPE" :
	 kindset.add(JcompSymbolKind.CLASS);
	 kindset.add(JcompSymbolKind.INTERFACE);
	 kindset.add(JcompSymbolKind.ENUM);
	 break;
      case "CLASS" :
	 kindset.add(JcompSymbolKind.CLASS);
	 break;
      case "ENUM" :
	 kindset.add(JcompSymbolKind.ENUM);
	 break;
      case "INTERFACE" :
	 kindset.add(JcompSymbolKind.INTERFACE);
	 break;
      case "CLASS&ENUM" :
	 kindset.add(JcompSymbolKind.CLASS);
	 kindset.add(JcompSymbolKind.ENUM);
	 break;
      case "CLASS&INTERFACE" :
	 kindset.add(JcompSymbolKind.CLASS);
	 kindset.add(JcompSymbolKind.INTERFACE);
	 break;
      case "METHOD" :
	 kindset.add(JcompSymbolKind.METHOD);
	 break;
      case "CONSTRUCTOR" :
	 kindset.add(JcompSymbolKind.CONSTRUCTOR);
	 break;
      case "FIELD" :
	 kindset.add(JcompSymbolKind.FIELD);
	 break;
      case "PACKAGE" :
	 kindset.add(JcompSymbolKind.PACKAGE);
	 break;
    }

   return new FindSymbolVisitor(p1,p2,p3,p4,kindset);
}



private int findEndOfTypes(String pat,int idx1)
{
   if (idx1 < 0) return -1;

   int lvl = 0;
   for (int i = idx1; i < pat.length(); ++i) {
      char c = pat.charAt(i);
      if (c == '<') ++ lvl;
      else if (c == '>') {
	 if (--lvl == 0) return i;
       }
    }
   return -1;
}


private class FindSymbolVisitor extends ASTVisitor {

   private Pattern name_pattern;
   private String generics_pattern;
   private List<String> parameters_pattern;
   private boolean extra_parameters;
   private Pattern type_pattern;
   private EnumSet<JcompSymbolKind> search_kind;

   FindSymbolVisitor(String np,String gp,String ap,String tp,EnumSet<JcompSymbolKind> kinds) {
      fixNamePattern(np);
      fixGenericsPattern(gp);
      fixParametersPattern(ap);
      fixTypePattern(tp);
      search_kind = kinds;
    }


   @Override public void postVisit(ASTNode n) {
      JcompSymbol js = JcompAst.getDefinition(n);
      if (js == null) return;
      if (match(js)) {
	 match_symbols.add(js);
       }
    }

   private boolean match(JcompSymbol js) {
      if (!search_kind.contains(js.getSymbolKind())) return false;
      if (js.isTypeSymbol()) {
	 JcompType jt = js.getType();
	 String n1 = jt.getName();
	 int idx1 = n1.indexOf("<");
	 if (idx1 > 0) {
	    String n2 = n1.substring(idx1);
	    n1 = n1.substring(0,idx1);
	    if (!matchGenerics(n2)) return false;
	  }
	 if (!matchName(jt.getName()))return false;
       }
      else if (js.isConstructorSymbol() || js.isMethodSymbol()) {
	 String n1 = js.getFullName();
	 if (js.isConstructorSymbol()) {
	    n1 = js.getClassType().getName();
	  }
	 int idx1 = n1.indexOf("<");
	 if (idx1 > 0) {
	    String n2 = n1.substring(idx1);
	    n1 = n1.substring(0,idx1);
	    if (!matchGenerics(n2)) return false;
	  }
	 if (!matchName(n1)) return false;
	 JcompType jt = js.getType();
	 String n3 = jt.getName();
	 int idx3 = n3.lastIndexOf(")");
	 // int idx2 = n3.indexOf("(");
	 // String n4 = n3.substring(idx2+1,idx3);
	 if (!matchParameters(jt)) return false;
	 String n5 = n3.substring(idx3+1);
	 if (!js.isConstructorSymbol() && !matchType(n5)) return false;
       }
      else if (js.isFieldSymbol()) {
	 String n1 = js.getFullName();
	 if (!matchName(n1)) return false;
	 String n2 = js.getType().getName();
	 if (!matchType(n2)) return false;
       }
      else return false;

      return true;
    }


   private void fixNamePattern(String pat) {
      name_pattern = null;
      pat = pat.trim();
      if (pat == null || pat.length() == 0 || pat.equals("*")) return;
      String q1 = "([A-Za-z0-9$_]+\\.)*";
      if (pat.startsWith(".")) pat = pat.substring(1);
      String q2 = pat.replace(".","\\.");
      q2 = q2.replace("*","(.*)");
      name_pattern = Pattern.compile(q1 + q2);
    }

   private boolean matchName(String itm) {
      if (name_pattern == null) return true;
      if (itm == null) return false;
      if (name_pattern.matcher(itm).matches()) return true;
      return false;
    }

   private void fixGenericsPattern(String pat) {
      generics_pattern = null;
      if (pat == null) return;
      pat = pat.trim();
      if (pat.length() == 0 || pat.equals("*")) return;   generics_pattern = pat;
    }

   private boolean matchGenerics(String itm) {
      if (generics_pattern == null) return true;
      if (itm == null) return false;
      // TODO : fix this
      return true;
    }

   private void fixParametersPattern(String pat) {
      parameters_pattern = null;
      if (pat == null) return;
      pat = pat.replace(" ","");
      if (pat.length() == 0) return;
      parameters_pattern = new ArrayList<String>();
      extra_parameters = false;
      int lvl = 0;
      int spos = -1;
      for (int i = 0; i < pat.length(); ++i) {
	 char c = pat.charAt(i);
	 if (c == '(') continue;
	 else if (c == ')') {
	    if (spos >= 0) parameters_pattern.add(pat.substring(spos,i));
	    spos = -1;
	    break;
	  }
	 else if (c == '*') {
	    if (spos >= 0) parameters_pattern.add(pat.substring(spos,i));
	    spos = -1;
	    extra_parameters = true;
	    break;
	  }
	 else if (c == ',') {
	    if (spos >= 0) parameters_pattern.add(pat.substring(spos,i));
	    spos = -1;
	  }
	 else if (c == '<') {
	    if (spos >= 0) parameters_pattern.add(pat.substring(spos,i));
	    ++lvl;
	    spos = -1;
	  }
	 else if (c == '>') {
	    if (lvl > 0) --lvl;
	    spos = -1;
	  }
	 else if (spos < 0 && lvl == 0) {
	    spos = i;
	  }
       }
      if (spos > 0) parameters_pattern.add(pat.substring(spos));
    }

   private boolean matchParameters(JcompType jt) {
      if (parameters_pattern == null) return true;

      List<JcompType>  ptyps = jt.getComponents();
      for (int i = 0; i < parameters_pattern.size(); ++i) {
	 if (i >= ptyps.size()) return false;
	 String patstr = parameters_pattern.get(i);
	 String actstr = ptyps.get(i).getName();
	 String act1 = actstr;
	 int idx1 = actstr.indexOf("<");
	 if (idx1 >= 0) act1 = actstr.substring(0,idx1);
	 if (!patstr.equals(actstr) && !patstr.equals(act1)) {
	    return false;
	  }
       }
      if (ptyps.size() == parameters_pattern.size() || extra_parameters) return true;

      return false;
    }

   private void fixTypePattern(String pat) {
      type_pattern = null;
      if (pat == null) return;
      pat = pat.trim();
      if (pat.length() == 0 || pat.equals("*")) return;
      int idx1 = pat.indexOf("<");
      if (idx1 == 0) return;
      if (idx1 > 0) pat = pat.substring(0,idx1);
      if (pat.startsWith(".")) pat = pat.substring(1);
      String q1 = "([A-Za-z0-9$_]+\\.)*";
      String q2 = pat.replace(".","\\.");
      q2 = q2.replace("*","(.*)");
      type_pattern = Pattern.compile(q1 + q2);
    }

   private boolean matchType(String itm) {
      if (type_pattern == null) return true;
      if (itm == null) return false;
      if (type_pattern.matcher(itm).matches()) return true;
      return false;
    }

}	// end of inner class FindSymbolVisitor




/********************************************************************************/
/*										*/
/*	Visitor to find a location						*/
/*										*/
/********************************************************************************/

ASTVisitor getFindLocationVisitor(int soff,int eoff)
{
   return new FindLocationVisitor(soff,eoff);
}



private class FindLocationVisitor extends ASTVisitor {

   private int start_offset;
   private int end_offset;

   FindLocationVisitor(int soff,int eoff) {
      start_offset = soff;
      end_offset = eoff;
    }

   @Override public boolean preVisit2(ASTNode n) {
      int soff = n.getStartPosition();
      int eoff = soff + n.getLength();
      if (eoff < start_offset) return false;
      if (soff > end_offset) return false;
      return true;
    }

   @Override public boolean visit(SimpleName n) {
      JcompSymbol js = JcompAst.getDefinition(n);
      if (js == null) js = JcompAst.getReference(n);
      if (js == null) {
         JcompType jt = JcompAst.getJavaType(n);
         if (jt != null) js = jt.getDefinition();
      }
      if (js != null) {
         match_symbols.add(js);
       }
      return false;
    }

}	// end of inner class FindLocationVisitor











/********************************************************************************/
/*										*/
/*	Methods to find nodes associated with a definition			*/
/*										*/
/********************************************************************************/

ASTVisitor getLocationsVisitor(boolean defs,boolean refs,boolean impls,
      boolean ronly,boolean wonly)
{
   if (impls) {
      expandMatchesForImplementations();
    }

   return new LocationVisitor(defs,refs,ronly,wonly);
}



private void expandMatchesForImplementations()
{
   Set<JcompSymbol> add = new HashSet<JcompSymbol>();
   for (JcompSymbol js : match_symbols) {
      if (js.isMethodSymbol() && !js.isConstructorSymbol()) {
	 JcompType jt = js.getType();
	 String nm = js.getName();
	 JcompType deft = js.getClassType();
	 Set<JcompType> chld = getChildTypes(deft);
	 for (JcompType ct : chld) {
	    JcompScope scp = ct.getScope();
	    if (scp != null) {
	       JcompSymbol xsym = scp.lookupMethod(null,nm,jt,null,null);
	       if (!match_symbols.contains(xsym)) add.add(xsym);
	    }
	  }
	
       }
    }

   match_symbols.addAll(add);
}


private Set<JcompType> getChildTypes(JcompType jt)
{
   Set<JcompType> rslt = new HashSet<JcompType>();

   List<JcompType> workq = new ArrayList<JcompType>();
   workq.add(jt);
   while (!workq.isEmpty()) {
      JcompType ct = workq.remove(0);
      for (JcompType xt : jcomp_root.getAllTypes()) {
	 if (rslt.contains(xt) || xt == jt) continue;
	 if (xt.isClassType()) {
	    if (xt.isCompatibleWith(ct)) {
	       workq.add(xt);
	       rslt.add(xt);
	     }
	  }
       }
    }

   return rslt;
}



private class LocationVisitor extends ASTVisitor {

   private boolean use_defs;
   private boolean use_refs;
   private boolean read_only;
   private boolean write_only;
   private JcompSymbol cur_symbol;

   LocationVisitor(boolean def,boolean ref,boolean r,boolean w) {
      use_defs = def;
      use_refs = ref;
      read_only = r;
      write_only = w;
      cur_symbol = null;
    }

   @Override public void preVisit(ASTNode n) {
      cur_symbol = getRelevantSymbol(n);
    }

   @Override public void postVisit(ASTNode n) {
      if (cur_symbol != null && cur_symbol == getRelevantSymbol(n)) {
	 if (checkReadWrite(n)) {
	    result_map.add(new Match(current_file,n,cur_symbol,getContainerSymbol(n)));
	  }
	 cur_symbol = null;
       }
    }

   private JcompSymbol getRelevantSymbol(ASTNode n) {
      JcompSymbol js = null;
      if (use_defs) {
	 js = JcompAst.getDefinition(n);
	 if (js != null && match_symbols.contains(js))
	    return js;
       }
      if (use_refs) {
	 js = JcompAst.getReference(n);
	 if (js != null && match_symbols.contains(js)) return js;
       }
      return null;
   }

   private JcompSymbol getContainerSymbol(ASTNode n) {
      while (n != null) {
	 if (n instanceof BodyDeclaration) {
	    JcompSymbol js = JcompAst.getDefinition(n);
	    if (js != null) return js;
	    else System.err.println("BAD BODY DECL");
	  }
	 else if (n instanceof VariableDeclarationFragment && n.getParent() instanceof FieldDeclaration) {
	    JcompSymbol js = JcompAst.getDefinition(n);
	    if (js != null) return js;
	  }
	 n = n.getParent();
      }
      return null;
   }


   private boolean checkReadWrite(ASTNode n) {
      if (read_only == write_only) return true;

      boolean write = false;
      boolean read = true;

      StructuralPropertyDescriptor spd = null;
      for (ASTNode p = n; p != null; p = p.getParent()) {
	 boolean done = true;
	 switch (p.getNodeType()) {
	    case ASTNode.ASSIGNMENT :
	       if (spd == Assignment.LEFT_HAND_SIDE_PROPERTY) {
		  read = false;
		  write = true;
		}
	       break;
	    case ASTNode.POSTFIX_EXPRESSION :
	       read = true;
	       write = true;
	       break;
	    case ASTNode.PREFIX_EXPRESSION :
	       PrefixExpression pfx = (PrefixExpression) p;
	       String op = pfx.getOperator().toString();
	       if (op.equals("++") || op.equals("--")) {
		  read = true;
		  write = true;
		}
	       break;
	    case ASTNode.ARRAY_ACCESS :
	       if (spd == ArrayAccess.ARRAY_PROPERTY) done = false;
	       break;
	    case ASTNode.SIMPLE_NAME :
	    case ASTNode.QUALIFIED_NAME :
	       done = false;
	       break;
	    case ASTNode.METHOD_DECLARATION :
	    case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
	    case ASTNode.FIELD_DECLARATION :
	    case ASTNode.TYPE_DECLARATION :
	    case ASTNode.ENUM_DECLARATION :
	       read = true;
	       write = true;
	       break;
	    default :
	       break;
	  }
	 spd = p.getLocationInParent();
	 if (done) break;
       }

      if (read && read_only) return true;
      if (write && write_only) return true;

      return false;
    }

}	// end of inner class LocationsVisitor




/********************************************************************************/
/*                                                                              */
/*      Visitor to find things by key                                           */
/*                                                                              */
/********************************************************************************/

ASTVisitor getFindByKeyVisitor(String proj,String key)
{
   return new FindByKeyVisitor(proj,key);
}



private class FindByKeyVisitor extends ASTVisitor {
   
   private String using_project;
   private String using_key;
   
   FindByKeyVisitor(String proj,String key) {
      using_project = proj;
      using_key = key;
    }
   
   @Override public void postVisit(ASTNode n) {
      JcompSymbol js = JcompAst.getDefinition(n);
      if (js != null && current_file != null) {
	 String hdl = js.getHandle(using_project);
	 if (hdl != null && hdl.equals(using_key)) {
	    match_symbols.add(js);
	  }
       }
    }
}

/********************************************************************************/
/*										*/
/*	Search Result								*/
/*										*/
/********************************************************************************/

private static class Match implements SearchResult {

   private int match_start;
   private int match_length;
   private JcompSource match_file;
   private JcompSymbol match_symbol;
   private JcompSymbol container_symbol;

   Match(JcompFile jf,ASTNode n,JcompSymbol js,JcompSymbol cntr) {
      match_start = n.getStartPosition();
      match_length = n.getLength();
      match_file = jf.getFile();
      match_symbol = js;
      container_symbol = cntr;
    }

   @Override public int getOffset()			{ return match_start; }
   @Override public int getLength()			{ return match_length; }
   @Override public JcompSymbol getSymbol()	        { return match_symbol; }
   @Override public JcompSymbol getContainer()	        { return container_symbol; }
   @Override public JcompSource getFile()		{ return match_file; }

}	// end of inner class Match


}	// end of class JcompSearch




/* end of JcompSearch.java */







