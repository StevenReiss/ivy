/********************************************************************************/
/*										*/
/*		IvyProjectConstants.java					*/
/*										*/
/*	IVY project associated constants					*/
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


import edu.brown.cs.ivy.file.IvyFile;



interface IvyProjectConstants {


/********************************************************************************/
/*										*/
/*	File locations								*/
/*										*/
/********************************************************************************/

String IVY_PROJECT_DIRECTORY = IvyFile.expandName("$(HOME)/.ivy/Projects");

String IVY_CURRENT_PROJECT = ".currentproject";
String IVY_PROJECT_EXTENSION = ".ivy";


String IVY_PATH_VAR_FILE = IvyFile.expandName(".metadata/.plugins/org.eclipse.core.runtime/" + 
      ".settings/org.eclipse.jdt.core.prefs");
String IVY_EXEC_DIR = IvyFile.expandName(".metadata/.plugins/org.eclipse.debug.core/.launches");

		


/********************************************************************************/
/*										*/
/*	Internal names								*/
/*										*/
/********************************************************************************/

String IVY_DEFAULT_PACKAGE = "<DEFAULT>";



}      // end of interface IvyProjectConstants




/* end of IvyProjectConstants.java */






