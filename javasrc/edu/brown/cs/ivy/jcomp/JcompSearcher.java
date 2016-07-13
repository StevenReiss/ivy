/********************************************************************************/
/*                                                                              */
/*              JcompSearcher.java                                              */
/*                                                                              */
/*      Search retrieval interface for Jcomp                                    */
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

import java.util.List;
import java.util.Set;


/**
 *      This class holds the result of a search.  It can be used either to obtain information
 *      about the search results or to refine or modify those results.
 **/

public interface JcompSearcher extends JcompConstants
{

/**
 *      Return the set of source locations corresponding to the search results.  This is
 *      only defined for searches that yield locations, not those that yield symbols.
 **/
   
List<SearchResult> getMatches();



/**
 *      Return the set of symbols associated with the given search.  This is only defined
 *      for searches that yield symbols.
 **/

Set<JcompSymbol> getSymbols();



/**
 *      This interface represents a file location for a search result.  The offset, length
 *      and source indicate the location.  Where symbol information is available for that
 *      location, it is included.
 **/

interface SearchResult {
   
   int getOffset();
   int getLength();
   JcompSource getFile();
   JcompSymbol getSymbol();
   JcompSymbol getContainer();
   
}  

}       // end of interface JcompSearcher




/* end of JcompSearcher.java */
