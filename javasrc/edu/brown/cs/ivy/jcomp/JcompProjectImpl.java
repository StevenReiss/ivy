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

/* SVN: $Id: JcompProjectImpl.java,v 1.9 2017/10/24 12:47:04 spr Exp $ */



package edu.brown.cs.ivy.jcomp;

import org.eclipse.jdt.core.dom.*;

import java.util.*;


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
}



void clear()
{
   file_nodes.clear();
   is_resolved = false;
   all_types = null;
   resolve_typer = null;
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
}



@Override public Collection<JcompSemantics> getSources()
{
   return new ArrayList<JcompSemantics>(file_nodes);
}



Collection<ASTNode> getTrees()
{
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




/********************************************************************************/
/*										*/
/*	Compilation methods							*/
/*										*/
/********************************************************************************/

@Override synchronized public void resolve()
{
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
   ClearVisitor cv = new ClearVisitor();

   for (JcompFile jf : file_nodes) {
      ASTNode cu = jf.getAstNode();
      if (cu != null) cu.accept(cv);
    }
}



private static class ClearVisitor extends ASTVisitor {

   @Override public void postVisit(ASTNode n) {
      JcompAst.clearAll(n);
   }

}	// end of inner class ClearVisitor





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
	 if (!jt.isUndefined() && jt.isUnknown()) typs.add(jt);
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
}	// end of class JcompRoot




/* end of JcompRoot.java */

