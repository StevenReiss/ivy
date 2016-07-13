/********************************************************************************/
/*                                                                              */
/*              JcompSource.java                                                */
/*                                                                              */
/*      User implementation of a source file for compilation                    */
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



/**
 *      This class is meant to be implemented by the user of the Jcomp package.
 *      It represents a source file.
 **/

public interface JcompSource
{

/**
 *      Return the contents of the file as a string.  If the file is on disk, you can 
 *      user IvyFile.loadFile to return its contents.
 **/
   
String getFileContents();



/**
 *      Return the name of the file.  This is used for error messages.
 **/

String getFileName();



}       // end of interface JcompSource




/* end of JcompSource.java */
