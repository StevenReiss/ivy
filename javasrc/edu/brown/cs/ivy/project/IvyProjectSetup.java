/********************************************************************************/
/*										*/
/*		IvyProjectSetup.java						*/
/*										*/
/*	IVY project command line and graphical interface for projects		*/
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



public final class IvyProjectSetup implements IvyProjectConstants {


/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   IvyProjectSetup ps = new IvyProjectSetup(args);

   ps.process();
}




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private IvyProjectManager project_manager;
private File eclipse_workspace;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private IvyProjectSetup(String [] args)
{
   eclipse_workspace = null;
   project_manager = null;

   scanArgs(args);
}



/********************************************************************************/
/*										*/
/*	Argument scanning methods						*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   String ws = null;
   String pdir = null;

   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-w") && i+1 < args.length) {           // -workspace <dir>
	    ws = args[++i];
	  }
	 else if (args[i].startsWith("-d") && i+1 < args.length) {      // -dir <project dir>
	    pdir = args[++i];
	  }
	 else badArgs();
       }
      else badArgs();
    }

   project_manager = IvyProjectManager.getManager(pdir);

   if (ws != null) {
      eclipse_workspace = new File(ws);
      if (!eclipse_workspace.isDirectory()) badArgs();
    }
}



private void badArgs()
{
   System.err.println("IVYPROJECTSETUP: projectsetup -workspace <eclipse workspace dir>");
   System.exit(1);
}




/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

private void process()
{
   if (eclipse_workspace != null) {
      for (String s : project_manager.getEclipseProjects(eclipse_workspace)) {
	 IvyProject ep = project_manager.findProject(s);
	 if (ep == null) {
	    try {
	       ep = project_manager.createEclipseProject(s,eclipse_workspace,s);
	     }
	    catch (IvyProjectException e) {
	       System.err.println("IVYPROJECTSETUP: Problem creating eclipse project " + s + ": " + e);
	     }
	  }
	 if (ep != null) ep.saveProject();
       }
    }
}



}	// end of class IvyProjectSetup




/* end of IvyProjectSetup.java */
