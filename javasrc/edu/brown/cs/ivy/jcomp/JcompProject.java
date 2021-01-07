/********************************************************************************/
/*                                                                              */
/*              JcompProject.java                                               */
/*                                                                              */
/*      Interface representing a project to compile                             */
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

import java.util.Collection;
import java.util.List;


/**
 *      This interface represents a set of files compiled together as a single project.  It provides
 *      methods to search over the files in various ways.  It also provides hooks to get the semantic
 *      objects for the individual files and the full set of error messages.
 **/

public interface JcompProject extends JcompConstants
{

/**
 *      This forces the system to actually compile the files in the project.  It is often called implicitly,
 *      but can be called explicitly at any time.  Calling it twice will have no effect unless a file has
 *      been changed (by calling reparse() on its semantic object).
 **/
   
void resolve();

boolean isResolved();

void setProjectKey(Object key);

JcompTyper getResolveTyper();



/**
 *      Search for all symbols of a given kind (TYPE, CLAS, ENUM, INTERFACE, CLASS&ENUM, CLASS&INTERFACE,
 *      METHOD, CONSTRUCTOR, FIELD, PACKAGE) matching the given pattern.  Patterns are the same as those
 *      used for an Eclipse search.
 **/

JcompSearcher findSymbols(String pat,String kind);



/**
 *      Return the symbol defined or referenced at the given source offset.
 **/

JcompSearcher findSymbolAt(String file,int soff,int eoff);



/**
 *      Map the symbol results of the given searcher to their corresponding type results.
 **/

JcompSearcher findTypes(JcompSearcher js);



/**
 *      Find the set of locations corresponding to the symbols returned by a given searcher restricted
 *      to references, definitions, implementations, or read/write.
 **/

JcompSearcher findLocations(JcompSearcher js,boolean def,boolean ref,boolean impl,boolean ronly,boolean wonly);



/**
 *      Find the symbol corresponding to a given key or handle.
 **/

JcompSearcher findSymbolByKey(String proj,String file,String key);



/**
 *      Return the set of all error messages for all files in the project.
 **/ 

List<JcompMessage> getMessages();



/**
 *      Return the set of all semantic information for files in the project.
 **/

Collection<JcompSemantics> getSources();

List<String> getClassPath();

void addSourceFile(JcompSource src);

/**
 *      Find the containing symbol (i.e. method) for a given source offset.
 **/

JcompSymbol getContainer(JcompSemantics js,int soff,int eoff);




}       // end of interface JcompProject




/* end of JcompProject.java */
