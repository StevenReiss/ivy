/********************************************************************************/
/*										*/
/*		JcompScopeAst.java					*/
/*										*/
/*	Class to represent a scope for an AST node				*/
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



import java.util.*;



class JcompScopeAst extends JcompScope implements JcompConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcompScopeAst	 parent_scope;
private JcompScopeLookup lookup_scope;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompScopeAst(JcompScope parent)
{
   parent_scope = (JcompScopeAst) parent;
   if (parent == null) lookup_scope = new JcompScopeLookup();
   else lookup_scope = parent_scope.getLookupScope();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public JcompScope getParent() 		{ return parent_scope; }

private JcompScopeLookup getLookupScope()		{ return lookup_scope; }




/********************************************************************************/
/*										*/
/*	Definition methods							*/
/*										*/
/********************************************************************************/

@Override void defineVar(JcompSymbol js)		      { lookup_scope.defineVar(js,this); }

@Override JcompSymbol lookupVariable(String nm)       { return lookup_scope.lookupVariable(nm,this); }

@Override public Collection<JcompSymbol> getDefinedFields()
{
   Collection<JcompSymbol> rslt = lookup_scope.getDefinedFields(this);
   
   return rslt;
}

@Override void defineMethod(JcompSymbol js)		      { lookup_scope.defineMethod(js,this); }

@Override JcompSymbol lookupMethod(String id,JcompType aty)
{
   return lookup_scope.lookupMethod(id,aty,this);
}

@Override public Collection<JcompSymbol> getDefinedMethods()
{
   return lookup_scope.getDefinedMethods(this);
}

@Override List<JcompSymbol> lookupStatics(String id)
{
   return lookup_scope.lookupStatics(id,this);
}

@Override void getFields(Map<String,JcompType> flds)
{
   lookup_scope.getFields(flds,this);
}

@Override Set<JcompSymbol> lookupAbstracts(JcompTyper typer)
{
   return lookup_scope.lookupAbstracts(typer);
}


}	// end of class JcompScopeAst



/* end of JcompScopeAst.java */
