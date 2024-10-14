/********************************************************************************/
/*										*/
/*		IvyProject.java 						*/
/*										*/
/*	IVY generic project definition for use with other packages		*/
/*										*/
/********************************************************************************/
/*	Copyright 2009 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2009, Brown University, Providence, RI.				 *
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


package edu.brown.cs.ivy.project;



import java.io.File;
import java.io.InputStream;



public interface IvyProject {



String getName();
String getOwner();
String getDescription();
String getDirectory();

Iterable<String> getClassPath();

Iterable<String> getStartClasses();

Iterable<String> getUserPackages();

Iterable<String> getSourcePath();

Iterable<IvyExecutable> getExecutables();

Iterable<File> getSourceFiles();

boolean containsFile(String file);
boolean isUserClass(String cls);

File findSourceFile(String cls);
InputStream findClassFile(String cls);
void cleanUp();

String getProperty(String id);
Iterable<String> getPropertyNames();

void update();

void saveProject();

void dumpProject();

File getWorkspace();


}      // end of interface IvyProject




/* end of IvyProject.java */

