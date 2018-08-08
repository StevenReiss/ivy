/********************************************************************************/
/*										*/
/*		IvyProjectManager.java						*/
/*										*/
/*	IVY project manager for accessing and setting up projects		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/project/IvyProjectManager.java,v 1.4 2018/08/02 15:10:38 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyProjectManager.java,v $
 * Revision 1.4  2018/08/02 15:10:38  spr
 * Fix imports.
 *
 * Revision 1.3  2017/12/20 20:36:53  spr
 * Formatting
 *
 * Revision 1.2  2009-10-02 00:18:29  spr
 * Import clean up.
 *
 * Revision 1.1  2009-09-19 00:22:01  spr
 * Add IvyProject implementaton.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.project;


import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;



public class IvyProjectManager implements IvyProjectConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private File			base_directory;
private Map<String,IvyProjectBase> project_map;



private static Map<File,IvyProjectManager> manager_set;


static {
   manager_set = new HashMap<File,IvyProjectManager>();
}



/********************************************************************************/
/*										*/
/*	Static creation methods 						*/
/*										*/
/********************************************************************************/

public static IvyProjectManager getManager()
{
   return getManager(null);
}


public static synchronized IvyProjectManager getManager(String dir)
{
   if (dir == null) dir = IVY_PROJECT_DIRECTORY;

   File f = new File(dir);
   if (!f.exists() && !f.mkdirs()) return null;
   if (!f.isDirectory()) return null;

   IvyProjectManager pm = manager_set.get(f);
   if (pm == null) {
      pm = new IvyProjectManager(f);
      manager_set.put(f,pm);
    }

   return pm;
}




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private IvyProjectManager(File dir)
{
   base_directory = dir;
   project_map = new HashMap<String,IvyProjectBase>();

   loadProjects();
}



/********************************************************************************/
/*										*/
/*	Interface methods for project access					*/
/*										*/
/********************************************************************************/

public IvyProject findProject(String name)
{
   IvyProjectBase ip = project_map.get(name);

   if (ip != null) ip.update();

   return ip;
}




/********************************************************************************/
/*										*/
/*	Interface methods for getting project set				*/
/*										*/
/********************************************************************************/

public Collection<IvyProject> getAllProjects()
{
   Collection<IvyProject> rslt = new ArrayList<IvyProject>(project_map.values());

   return rslt;
}



public void defineEclipseProjects(String workspace)
{
   defineEclipseProjects(new File(workspace));
}



public void defineEclipseProjects(File workspace)
{
   for (String s : getEclipseProjects(workspace)) {
      IvyProject ep = findProject(s);
      if (ep == null) {
	 try {
	    ep = createEclipseProject(s,workspace,s);
	  }
	 catch (IvyProjectException e) {
	    System.err.println("IVYPROJECT: Problem creating eclipse project " + s + ": " + e);
	  }
       }
      if (ep != null) ep.saveProject();
    }
}




/********************************************************************************/
/*										*/
/*	Methods for creating							*/
/*										*/
/********************************************************************************/

IvyProject createEditableProject(String name)
{
   if (project_map.containsKey(name)) return null;

   return null;
}




IvyProject createEclipseProject(String name,File workspace,String ename) throws IvyProjectException
{
   if (project_map.containsKey(name)) return null;

   IvyProjectBase ip = new IvyProjectEclipse(this,name,workspace,ename);
   ip.update();
   project_map.put(ip.getName(),ip);

   return ip;
}




IvyProject createBinaryProject(String name,String pid)
{
   if (project_map.containsKey(name)) return null;

   return null;
}




IvyProject createAntProject(String name,String dir,String antfile)
{
   if (project_map.containsKey(name)) return null;

   return null;
}



/********************************************************************************/
/*										*/
/*	Other access methods							*/
/*										*/
/********************************************************************************/

Collection<String> getEclipseProjects(File workspace)
{
   return IvyProjectEclipse.getProjects(workspace);
}


public File getProjectDirectory()
{
   if (base_directory != null) return base_directory;

   return new File(IVY_PROJECT_DIRECTORY);
}




/********************************************************************************/
/*										*/
/*	Methods to load existing projects					*/
/*										*/
/********************************************************************************/

private void loadProjects()
{
   for (String fn : base_directory.list()) {
      if (fn.endsWith(IVY_PROJECT_EXTENSION)) {
	 File f = new File(base_directory,fn);
	 Element e = IvyXml.loadXmlFromFile(f);
	 if (e != null && IvyXml.isElement(e,"PROJECT")) {
	    IvyProjectBase ip = null;
	    String typ = IvyXml.getAttrString(e,"TYPE");
	    try {
	       if (typ == null || typ.equals("XML")) {
		  // TODO: load editable project
		}
	       else if (typ.equals("ECLIPSE")) {
		  ip = new IvyProjectEclipse(this,e);
		}
	     }
	    catch (IvyProjectException t) {
	       System.err.println("IVYPROJECT: Problem loading project: " + t);
	     }
	    if (ip != null) {
	       project_map.put(ip.getName(),ip);
	       ip.setDirectory(base_directory.getPath());
	     }
	  }
       }
    }
}




}      // end of abstract class IvyProjectManager




/* end of IvyProjectManager.java */




