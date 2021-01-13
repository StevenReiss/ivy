/********************************************************************************/
/*										*/
/*		JcompRoot.java							*/
/*										*/
/*	AST node for a set of project files					*/
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

/* SVN: $Id: JcompProjectImpl.java,v 1.17 2019/09/12 12:49:09 spr Exp $ */



package edu.brown.cs.ivy.jcomp;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


class JcompProjectImpl implements JcompProject, JcompConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private List<JcompFile>   file_nodes;
private JcompContext	  base_context;
private boolean 	  is_resolved;
private Set<JcompType>	  all_types;
private JcompTyper        resolve_typer;
private boolean           is_sorted;
private Object            project_key;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompProjectImpl(JcompContext ctx)
{
   file_nodes = new ArrayList<JcompFile>();
   base_context = ctx;
   is_resolved = false;
   resolve_typer = null;
   all_types = null;
   is_sorted = false;
   project_key = null;
}



void clear()
{
   file_nodes = null;
   is_resolved = false;
   all_types = null;
   resolve_typer = null;
   is_sorted = false;
   base_context = null;
   project_key = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

void addFile(JcompFile jf)
{
   if (file_nodes.contains(jf)) return;

   file_nodes.add(jf);
   jf.setRoot(this);
   is_sorted = false;
}


@Override public void addSourceFile(JcompSource src)
{
   JcompFile jf = new JcompFile(src);
   addFile(jf);
   jf.reparse();
}



@Override public Collection<JcompSemantics> getSources()
{
   sortFiles();
   return new ArrayList<JcompSemantics>(file_nodes);
}



Collection<ASTNode> getTrees()
{
   sortFiles();
   List<ASTNode> rslt= new ArrayList<ASTNode>();
   for (JcompFile rf : file_nodes) {
      ASTNode cu = rf.getAstNode();
      if (cu != null) rslt.add(cu);
    }
   return rslt;
}


Set<JcompType> getAllTypes()
{
   return all_types;
}



synchronized void setResolved(boolean fg,JcompTyper typer)
{
   is_resolved = fg;

   if (!fg) all_types = null;
   else {
      for (JcompFile jf : file_nodes) {
	 ASTNode cu = jf.getAstNode();
	 JcompAst.setResolved(cu,typer);
       }
    }
}



synchronized public boolean isResolved()
{
   if (!is_resolved) {
      boolean done = true;
      for (JcompFile jf : file_nodes) {
	 ASTNode cu = jf.getAstNode();
	 done &= JcompAst.isResolved(cu);
       }
      if (done) is_resolved = true;
    }

   return is_resolved;
}



public List<String> getClassPath()
{
   return base_context.getClassPath();
}



@Override public void setProjectKey(Object key)
{
   project_key = key;
}


Object getProjectKey()
{
   if (project_key != null) return project_key;
   return this;
}


/********************************************************************************/
/*										*/
/*	Compilation methods							*/
/*										*/
/********************************************************************************/

@Override synchronized public void resolve()
{
   sortFiles();
   
   if (isResolved()) {
      if (resolve_typer == null) resolve_typer = new JcompTyper(base_context);
      return;
    }

   clearResolve();

   resolve_typer = new JcompTyper(base_context);
   JcompResolver jr = new JcompResolver(resolve_typer);
   resolve_typer.assignTypes(this);
   jr.resolveNames(this);

   all_types = new HashSet<JcompType>(resolve_typer.getAllTypes());

   setResolved(true,resolve_typer);
}


public JcompTyper getResolveTyper()             { return resolve_typer; }



@Override public List<JcompMessage> getMessages()
{
   List<JcompMessage> rslt = new ArrayList<JcompMessage>();
   for (JcompFile jf : file_nodes) {
      List<JcompMessage> nmsg = jf.getMessages();
      if (nmsg != null) rslt.addAll(nmsg);
    }
   return rslt;
}



private void clearResolve()
{
   for (JcompFile jf : file_nodes) {
      ASTNode cu = jf.getAstNode();
      JcompAst.clearSubtree(cu,false);
    }
}









/********************************************************************************/
/*										*/
/*	Symbol Location methods 						*/
/*										*/
/********************************************************************************/

@Override public JcompSearcher findSymbols(String pattern,String kind)
{
   JcompSearch search = new JcompSearch(this);

   findSymbols(search,pattern,kind);

   return search;
}


private void findSymbols(JcompSearch search,String pattern,String kind)
{
   resolve();

   ASTVisitor av = search.getFindSymbolsVisitor(pattern,kind);

   for (JcompFile jf : file_nodes) {
      search.setFile(jf);
      jf.getAstNode().accept(av);
    }
}


@Override public JcompSearcher findSymbolAt(String file,int soff,int eoff)
{
   resolve();

   JcompSearch search = new JcompSearch(this);
   ASTVisitor av = search.getFindLocationVisitor(soff,eoff);

   for (JcompFile jf : file_nodes) {
      if (file != null && !jf.getFile().getFileName().equals(file))
	 continue;
      search.setFile(jf);
      jf.getAstNode().accept(av);
      break;
    }

   return search;
}





@Override public JcompSearcher findTypes(JcompSearcher rs)
{
   JcompSearch rjs = (JcompSearch) rs;
   Set<JcompSymbol> syms = rjs.getSymbols();
   if (syms == null || syms.isEmpty()) return rs;
   List<JcompType> typs = new ArrayList<JcompType>();
   boolean isok = false;
   for (JcompSymbol sym : syms) {
      if (!sym.isTypeSymbol()) {
	 isok = false;
	 JcompType jt = sym.getType();
	 if (!jt.isUndefined() && jt.isCompiledType()) typs.add(jt);
       }
    }
   if (isok) return rs;

   JcompSearch nrjs = new JcompSearch(this);
   for (JcompType jt : typs) {
      findSymbols(nrjs,jt.getName(),"TYPE");
    }

   return nrjs;
}



@Override public JcompSearcher findLocations(JcompSearcher rsr,boolean def,boolean ref,
      boolean impl,boolean ronly,boolean wonly)
{
   JcompSearch rs = (JcompSearch) rsr;
   ASTVisitor av = rs.getLocationsVisitor(def,ref,impl,ronly,wonly);

   for (JcompFile jf : file_nodes) {
      rs.setFile(jf);
      jf.getAstNode().accept(av);
    }

   return rs;
}



@Override public JcompSearcher findSymbolByKey(String proj,String file,String key)
{
   resolve();

   JcompSearch search = new JcompSearch(this);
   ASTVisitor av = search.getFindByKeyVisitor(proj,key);

   for (JcompFile jf : file_nodes) {
      if (!jf.getFile().getFileName().equals(file)) continue;
      search.setFile(jf);
      jf.getAstNode().accept(av);
    }

   return search;
}



/********************************************************************************/
/*										*/
/*	Methods to find appropriate container					*/
/*										*/
/********************************************************************************/

@Override public JcompSymbol getContainer(JcompSemantics js,int soff,int eoff)
{
   CompilationUnit cu = js.getRootNode();
   if (cu == null) return null;

   for (Object o : cu.types()) {
      AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
      int spos = cu.getExtendedStartPosition(atd);
      if (spos <= soff && spos + cu.getExtendedLength(atd) >= eoff) {
	 return getContainer(cu,atd,soff,eoff);
       }
    }

   return null;
}



private JcompSymbol getContainer(CompilationUnit cu,AbstractTypeDeclaration atd,
      int soff,int eoff)
{
   JcompSymbol sym = JcompAst.getDefinition(atd);

   for (Object o : atd.bodyDeclarations()) {
      BodyDeclaration bd = (BodyDeclaration) o;
      int spos = cu.getExtendedStartPosition(bd);
      if (spos <= soff && spos + cu.getExtendedLength(bd) >= eoff) {
	 switch (bd.getNodeType()) {
	    case ASTNode.TYPE_DECLARATION :
	    case ASTNode.ANNOTATION_TYPE_DECLARATION :
	    case ASTNode.ENUM_DECLARATION :
	       return getContainer(cu,(AbstractTypeDeclaration) bd,soff,eoff);
	    case ASTNode.ENUM_CONSTANT_DECLARATION :
	    case ASTNode.FIELD_DECLARATION :
	    case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
	    case ASTNode.INITIALIZER :
	    case ASTNode.METHOD_DECLARATION :
	       return sym;
	  }
       }
    }

   return sym;
}



/********************************************************************************/
/*                                                                              */
/*      Methods to sort files                                                   */
/*                                                                              */
/********************************************************************************/

private synchronized void sortFiles()
{
   if (is_sorted) return;
   
   FileSorter fs = new FileSorter(file_nodes);
   file_nodes = fs.sort();
   is_sorted = true;
}




private class FileSorter {
   
   private List<JcompFile> base_list;
   private Map<JcompFile,List<JcompFile>> depend_names;
   private Map<String,JcompFile> class_names;
   
   FileSorter(List<JcompFile> files) {
      base_list = files;
      depend_names = new HashMap<>();
      class_names = new HashMap<>();
    }
   
   List<JcompFile> sort() {
      computeDepends();
      if (depend_names.isEmpty()) return base_list;
      List<JcompFile> rslt = new ArrayList<>();
      Set<JcompFile> done = new HashSet<>();
      while (rslt.size() < base_list.size()) {
         boolean chng = false;
         JcompFile fst = null;
         for (JcompFile ff : base_list) {
            if (done.contains(ff)) continue;
            if (fst == null) fst = ff;
            boolean allok = true;
            List<JcompFile> rqs = depend_names.get(ff);
            if (rqs != null) {
               for (JcompFile xf : rqs) {
                  if (!done.contains(xf)) allok = false;
                }
             }
            if (allok) {
               rslt.add(ff);
               done.add(ff);
               chng = true;
             }
          }
         if (!chng) {
            rslt.add(fst);
            done.add(fst);
          }
       }
      return rslt;
    }
   
   private void computeDepends() {
      setupNames();
      for (JcompFile ff : base_list) {
         CompilationUnit cu = (CompilationUnit) ff.getAstNode();
         addDepends(ff,cu);
       }
    }
   
   private void setupNames() {
      String pfx = null;
      for (JcompFile ff : base_list) {
         CompilationUnit cu = (CompilationUnit) ff.getAstNode();
         if (cu.getPackage() != null) {
            pfx = cu.getPackage().getName().getFullyQualifiedName();
          }
         for (Object o : cu.types()) {
            setupNames(ff,pfx,(AbstractTypeDeclaration) o);
          }
       }
    }
   
   private void setupNames(JcompFile ff,String pfx,AbstractTypeDeclaration atd) {
      String nm = atd.getName().getIdentifier();
      if (pfx != null) nm = pfx + "." + nm;
      class_names.put(nm,ff);
      for (Object o : atd.bodyDeclarations()) {
         if (o instanceof AbstractTypeDeclaration) {
            setupNames(ff,nm,(AbstractTypeDeclaration) o);
          }
       }
    }
   
   private void addDepends(JcompFile ff,CompilationUnit cu) {
      List<String> pfxs = new ArrayList<String>();
      List<String> known = new ArrayList<String>();
      if (cu.getPackage() != null) {
         pfxs.add(cu.getPackage().getName().getFullyQualifiedName());
       }
      for (Object o : cu.imports()) {
         ImportDeclaration id = (ImportDeclaration) o;
         if (id.isStatic()) {
            String cnm = null;
            if (id.isOnDemand()) {
               cnm = id.getName().getFullyQualifiedName();
             }
            else if (id.getName() instanceof QualifiedName) {
               QualifiedName qn = (QualifiedName) id.getName();
               cnm = qn.getQualifier().getFullyQualifiedName();
             }
            else continue; 
            addDepend(cnm,ff,pfxs,known);
          }
         if (id.isOnDemand()) {
            pfxs.add(id.getName().getFullyQualifiedName());
          }
         else known.add(id.getName().getFullyQualifiedName());
       }
      
      for (Object o : cu.types()) {
         AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
         addDepends(ff,atd,pfxs,known);
       }
    }
   
   private void addDepends(JcompFile ff,AbstractTypeDeclaration atd,
         List<String> pfxs,List<String> known) {
      if (atd instanceof TypeDeclaration) {
         TypeDeclaration td = (TypeDeclaration) atd;
         addTypeDepends(ff,td,pfxs,known);
       }
      else if (atd instanceof EnumDeclaration) {
         EnumDeclaration ed = (EnumDeclaration) atd;
         addEnumDepends(ff,ed,pfxs,known);
       }
      for (Object o1 : atd.bodyDeclarations()) {
         if (o1 instanceof AbstractTypeDeclaration) {
            addDepends(ff,(AbstractTypeDeclaration) o1,pfxs,known);
          }
       }
    }
   
   private void addTypeDepends(JcompFile to,TypeDeclaration td,List<String> pfxs,List<String> known) {
      addDepend(td.getSuperclassType(),to,pfxs,known);
      for (Object o : td.superInterfaceTypes()) {
         Type it = (Type) o;
         addDepend(it,to,pfxs,known);
       }
    }
   
   private void addEnumDepends(JcompFile to,EnumDeclaration td,List<String> pfxs,List<String> known) {
      for (Object o : td.superInterfaceTypes()) {
         Type it = (Type) o;
         addDepend(it,to,pfxs,known);
       }
    }
   
   private void addDepend(Type t,JcompFile to,List<String> pfxs,List<String> known) {
      if (t == null) return;
      if (t.isSimpleType()) {
         SimpleType st = (SimpleType) t;
         String tnm = st.getName().getFullyQualifiedName();
         JcompFile frm = class_names.get(tnm);
         while (frm == null) {
            String xnm = "." + tnm;
            for (String s : known) {
               if (s.endsWith(xnm)) {
                  frm = class_names.get(s);
                  if (frm != null) break;
                }
             }
            if (frm == null) {
               for (String s : pfxs) {
                  frm = class_names.get(s + xnm);
                  if (frm != null) break;
                }
             }
            if (frm == null) {
               int idx = tnm.lastIndexOf(".");
               if (idx < 0) break;
               tnm = tnm.substring(0,idx);
             }
          }
         if (frm != null && frm != to) {
            List<JcompFile> dps = depend_names.get(to);
            if (dps == null) {
               dps = new ArrayList<>();
               depend_names.put(to,dps);
             }
            dps.add(frm);
          }
       }
      else if (t.isParameterizedType()) {
         ParameterizedType pt = (ParameterizedType) t;
         addDepend(pt.getType(),to,pfxs,known);
       }
    }
   
   private void addDepend(String tnm,JcompFile to,List<String> pfxs,List<String> known) {
      JcompFile frm = class_names.get(tnm);
      while (frm == null) {
         String xnm = "." + tnm;
         for (String s : known) {
            if (s.endsWith(xnm)) {
               frm = class_names.get(s);
               if (frm != null) break;
             }
          }
         if (frm == null) {
            for (String s : pfxs) {
               frm = class_names.get(s + xnm);
               if (frm != null) break;
             }
          }
         if (frm == null) {
            int idx = tnm.lastIndexOf(".");
            if (idx < 0) break;
            tnm = tnm.substring(0,idx);
          }
       }
      if (frm != null && frm != to) {
         List<JcompFile> dps = depend_names.get(to);
         if (dps == null) {
            dps = new ArrayList<>();
            depend_names.put(to,dps);
          }
         dps.add(frm);
       }
    }
         
}	// end of inner class FileSorter





}	// end of class JcompProjectImpl




/* end of JcompRoot.java */

