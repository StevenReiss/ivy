/********************************************************************************/
/*										*/
/*		IvyExecutable.java						*/
/*										*/
/*	IVY description of an executable for a given project			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/project/IvyExecutable.java,v 1.2 2019/09/12 12:48:33 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyExecutable.java,v $
 * Revision 1.2  2019/09/12 12:48:33  spr
 * Code cleanup.
 *
 * Revision 1.1  2009-09-19 00:22:01  spr
 * Add IvyProject implementaton.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.project;



public interface IvyExecutable {


IvyProject getProject();

String getStartClass();

String getRunCommand();

String getJavaArgs();
String getProgramArgs();




}      // end of interface IvyExecutable




/* end of IvyExecutable.java */



