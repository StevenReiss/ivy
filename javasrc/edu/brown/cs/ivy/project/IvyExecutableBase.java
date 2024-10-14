/********************************************************************************/
/*										*/
/*		IvyExecutableBase.java						*/
/*										*/
/*	IVY implementation of an executable for a given project 		*/
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


import org.w3c.dom.Element;



class IvyExecutableBase implements IvyExecutable {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private IvyProject for_project;
private String start_class;
private String run_command;
private String java_args;
private String program_args;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

IvyExecutableBase(IvyProject proj,Element e)
{
   this(proj);

   // loadFromXml(e);
}




protected IvyExecutableBase(IvyProject proj)
{
   for_project = proj;
   start_class = null;
   run_command = null;
   java_args = null;
   program_args = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public IvyProject getProject()			{ return for_project; }

@Override public String getRunCommand()			{ return run_command; }

@Override public String getStartClass()			{ return start_class; }

@Override public String getJavaArgs()			{ return java_args; }

@Override public String getProgramArgs()			{ return program_args; }



/********************************************************************************/
/*										*/
/*	Update methods								*/
/*										*/
/********************************************************************************/

protected void setStartClass(String cls)	{ start_class = cls; }
protected void setJavaArgs(String args) 	{ java_args = args; }
protected void setProgramArgs(String args)	{ program_args = args; }
protected void setRunCommand(String cmd)	{ run_command = cmd; }



}      // end of interface IvyExecutableBase




/* end of IvyExecutableBase.java */




