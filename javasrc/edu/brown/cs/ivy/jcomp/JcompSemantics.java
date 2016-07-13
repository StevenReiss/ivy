/********************************************************************************/
/*                                                                              */
/*              JcompSemantics.java                                             */
/*                                                                              */
/*      Interface describing semantics for a single file                        */
/*                                                                              */
/********************************************************************************/
/*	Copyright 2014 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2014, Brown University, Providence, RI.				 *
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
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.List;
import java.util.Set;


/**
 *      This interface represents the semantic inforamtion available for a particular
 *      file.
 **/

public interface JcompSemantics extends JcompConstants
{

/**
 *      This method forces the file to be reparsed.  It should be called if the
 *      contents of the file change.  The reparse will not occur immediately, but
 *      rather the next time that it is needed.
 **/
   
void reparse();
   


/**
 *      Get the set of error messages for this file.
 **/

List<JcompMessage> getMessages();



/**
 *      Get the source associated with this file.
 **/

JcompSource getFile();


/**
 *      Determine if this file defines the given class (fully qualified name).
 **/

boolean definesClass(String cls);



/**
 *      Return the AST node (with semantic information accessible using JcompAST)
 *      for this file.
 **/

CompilationUnit getRootNode();

ASTNode getAstNode();



/**
 *      If this file is part of a set of files (project) compiled together, return
 *      the corresponding project object.
 **/
JcompProject getProject();



/**
 *      Compute the set of related packages based on the import and package statements
 *      in this file.
 **/

Set<String> getRelatedPackages(); 
   


}       // end of interface JcompSemantics




/* end of JcompSemantics.java */
