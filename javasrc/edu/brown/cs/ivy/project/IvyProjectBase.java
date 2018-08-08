/********************************************************************************/
/*										*/
/*		IvyProjectBase.java						*/
/*										*/
/*	IVY project basic implementation class					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/project/IvyProjectBase.java,v 1.5 2018/08/02 15:10:38 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyProjectBase.java,v $
 * Revision 1.5  2018/08/02 15:10:38  spr
 * Fix imports.
 *
 * Revision 1.4  2015/11/20 15:09:25  spr
 * Reformatting.
 *
 * Revision 1.3  2012-01-12 01:27:55  spr
 * Fix exceptions
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
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import org.w3c.dom.Element;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;



abstract class IvyProjectBase implements IvyProject, IvyProjectConstants {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private IvyProjectManager for_manager;
private String	project_name;
private String	project_owner;
private String	project_description;
private String	project_directory;
private List<String> class_paths;
private List<String> source_paths;
private List<String> start_classes;
private Set<String> user_packages;
private Set<File> source_files;
private Map<String,String> project_properties;
private List<IvyExecutable> project_execs;
private Map<File,JarFile> current_jars;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

IvyProjectBase(IvyProjectManager pm,Element e) 
{
   this(pm,IvyXml.getTextElement(e,"NAME"));

   loadProjectFromXml(e);
}




protected IvyProjectBase(IvyProjectManager pm,String name)
{
   for_manager = pm;
   project_name = name;
   project_owner = System.getProperty("user.name");
   project_description = null;
   project_directory = null;
   class_paths = new ArrayList<String>();
   source_paths = new ArrayList<String>();
   start_classes = new ArrayList<String>();
   user_packages = new LinkedHashSet<String>();
   project_properties = new HashMap<String,String>();
   current_jars = new HashMap<File,JarFile>();

   source_files = null;

   project_execs = new ArrayList<IvyExecutable>();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName() 			{ return project_name; }

@Override public String getOwner()			{ return project_owner; }

@Override public String getDescription()			{ return project_description; }

@Override public String getDirectory()			{ return project_directory; }


@Override public Iterable<String> getClassPath()
{
   return new ArrayList<String>(class_paths);
}


@Override public Iterable<String> getSourcePath()
{
   return new ArrayList<String>(source_paths);
}


@Override public Iterable<String> getStartClasses()
{
   return new ArrayList<String>(start_classes);
}


@Override public Iterable<String> getUserPackages()
{
   return new ArrayList<String>(user_packages);
}


@Override public Iterable<IvyExecutable> getExecutables()
{
   return new ArrayList<IvyExecutable>(project_execs);
}



@Override public synchronized Iterable<File> getSourceFiles()
{
   if (source_files == null) {
      findSourceFiles();
    }

   return new ArrayList<File>(source_files);
}



@Override public String getProperty(String id)
{
   return project_properties.get(id);
}



@Override public Iterable<String> getPropertyNames()
{
   return new ArrayList<String>(project_properties.keySet());
}


protected String getProjectType()			{ return "BASE"; }

@Override public File getWorkspace()				{ return null; }



/********************************************************************************/
/*										*/
/*	Containment methods							*/
/*										*/
/********************************************************************************/

@Override public synchronized boolean containsFile(String file)
{
   if (file.endsWith(".java")) {
      if (source_files == null) findSourceFiles();
      File f = new File(file);
      return source_files.contains(f);
    }

   return false;
}



@Override public boolean isUserClass(String cls)
{
   if (cls == null) return false;

   if (cls.startsWith("L") && cls.endsWith(";")) {
      cls = cls.substring(1,cls.length()-1);
      cls = cls.replace("/",".");
    }

   int idx = cls.lastIndexOf(".");
   if (idx < 0) return user_packages.contains(IVY_DEFAULT_PACKAGE);

   String pkg = cls.substring(0,idx);

   return user_packages.contains(pkg);
}



@Override public File findSourceFile(String cls)
{
   int idx = cls.indexOf("$");
   if (idx >= 0) cls = cls.substring(0,idx);
   cls = cls.replace(".",File.separator);
   cls += ".java";

   for (File f : getSourceFiles()) {
      if (f.getPath().endsWith(cls)) return f;
    }

   return null;
}



/********************************************************************************/
/*										*/
/*	Update methods								*/
/*										*/
/********************************************************************************/

@Override public synchronized void update()
{
   source_files = null; 	// force recomputation
}



/********************************************************************************/
/*										*/
/*	Local editing methods							*/
/*										*/
/********************************************************************************/

protected synchronized void clear()
{
   class_paths = new ArrayList<String>();
   source_paths = new ArrayList<String>();
   start_classes = new ArrayList<String>();
   user_packages = new LinkedHashSet<String>();
   project_properties = new HashMap<String,String>();

   source_files = null;

   project_execs = new ArrayList<IvyExecutable>();
}




protected void addToClassPath(String cp)
{
   if (class_paths.contains(cp)) return;

   class_paths.add(cp);
}


protected void addToSourcePath(String sp)
{
   if (source_paths.contains(sp)) return;

   source_paths.add(sp);
}



protected void addUserPackage(String p)
{
   if (user_packages.contains(p)) return;

   user_packages.add(p);
}



protected void addStartClass(String c)
{
   if (start_classes.contains(c)) return;

   start_classes.add(c);
}




protected void setDescription(String d)
{
   project_description = d;
}



protected void addExecutable(IvyExecutable ie)
{
   project_execs.add(ie);
}



void setDirectory(String d)
{
   project_directory = d;
}



/********************************************************************************/
/*										*/
/*	Methods for loading project from xml description			*/
/*										*/
/********************************************************************************/

private void loadProjectFromXml(Element e) 
{
   project_name = IvyXml.getTextElement(e,"NAME");
   project_owner = IvyXml.getTextElement(e,"OWNER");
   project_description = IvyXml.getTextElement(e,"DESCRIPTION");

   Element cpe = IvyXml.getChild(e,"PATHS");
   if (cpe != null) {
      for (Element ce : IvyXml.children(cpe,"CP")) {
	 String cp = IvyXml.getText(ce);
	 class_paths.add(cp);
       }
      for (Element ce : IvyXml.children(cpe,"SP")) {
	 String sp = IvyXml.getText(ce);
	 source_paths.add(sp);
       }
    }

   cpe = IvyXml.getChild(e,"USER");
   if (cpe != null) {
      for (Element ce : IvyXml.children(cpe,"PACKAGE")) {
	 String pn = IvyXml.getText(ce);
	 user_packages.add(pn);
       }
      for (Element ce : IvyXml.children(cpe,"CLASS")) {
	 String cty = IvyXml.getAttrString(ce,"TYPE");
	 if (cty != null) {
	    if (cty.equals("START")) {
	       start_classes.add(IvyXml.getText(ce));
	     }
	  }
       }
    }

   cpe = IvyXml.getChild(e,"EXECS");
   if (cpe != null) {
      for (Element ce : IvyXml.children(cpe,"EXEC")) {
	 IvyExecutable de = new IvyExecutableBase(this,ce);
	 project_execs.add(de);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Methods for saving project to XML					*/
/*										*/
/********************************************************************************/

@Override public void saveProject()
{
   File pfile = new File(for_manager.getProjectDirectory(),project_name + IVY_PROJECT_EXTENSION);

   try {
      IvyXmlWriter xw = new IvyXmlWriter(pfile);
      xw.begin("PROJECT");
      xw.field("TYPE",getProjectType());
      xw.field("NAME",project_name);
      localSaveProject(xw);
      xw.textElement("OWNER",project_owner);
      xw.cdataElement("DESCRIPTION",project_description);
      xw.begin("PATHS");
      for (String s : class_paths) {
	 xw.textElement("CP",s);
       }
      for (String s : source_paths) {
	 xw.textElement("SP",s);
       }
      xw.end("PATHS");
      xw.begin("USER");
      for (String s : user_packages) {
	 xw.textElement("PACKAGE",s);
       }
      for (String s : start_classes) {
	 xw.begin("CLASS");
	 xw.field("TYPE","START");
	 xw.text(s);
	 xw.end("CLASS");
       }
      xw.end("USER");
      xw.begin("EXECS");
      // for (IvyExecutable ie : project_execs) ie.outputXml(xw);
      xw.end("EXECS");
      xw.end("PROJECT");
      xw.close();
    }
   catch (IOException e) {
      System.err.println("IVYPROJECT: Problem saving project: " + e);
    }
}



protected void localSaveProject(IvyXmlWriter xw)	{ }




/********************************************************************************/
/*										*/
/*	Methods to find all source files					*/
/*										*/
/********************************************************************************/

private void findSourceFiles()
{
   source_files = new HashSet<File>();
   FileFilter ff = createSourceFilter();

   for (String s : source_paths) {
      File f = new File(s);
      addFiles(f,ff,source_files);
    }
}



protected FileFilter createSourceFilter()
{
   return new SourceFilter();
}




private void addFiles(File f,FileFilter ff,Collection<File> rslt)
{
   if (!ff.accept(f)) return;

   if (f.isDirectory()) {
      File [] fls = f.listFiles(ff);
      if (fls == null) return;
      for (int i = 0; i < fls.length; ++i) {
	 addFiles(fls[i],ff,rslt);
       }
    }
   else if (!rslt.contains(f)) {
      rslt.add(f);
    }
}



protected static class SourceFilter implements FileFilter {

   @Override public boolean accept(File p) {
      if (p.isDirectory()) return true;
      String m;
      String nm = p.getName();
      int idx = nm.lastIndexOf(".");
      if (idx < 0) return false;
      m = nm.substring(idx);
      return m.equals(".java");
    }

}	// end of subclass SourceFilter




/********************************************************************************/
/*										*/
/*	Methods to find binary files						*/
/*										*/
/********************************************************************************/

@Override public InputStream findClassFile(String cls)
{
   if (!cls.endsWith(".class")) {
      cls = cls.replace('.','/') + ".class";
    }

   for (String cp : getClassPath()) {
      File f = new File(cp);
      if (!f.exists()) continue;
      try {
	 if (f.isDirectory()) {
	    InputStream ins = findInDirectory(f,cls);
	    if (ins != null) return ins;
	  }
	 else {
	    InputStream ins = findInJar(f,cls);
	    if (ins != null) return ins;
	  }
       }
      catch (IOException e) { }
    }

   return null;
}




@Override public void cleanUp()
{
   synchronized (current_jars) {
      for (JarFile jf : current_jars.values()) {
	 try {
	    jf.close();
	  }
	 catch (IOException e) { }
       }
      current_jars.clear();
    }
}



private InputStream findInDirectory(File d,String cls) throws IOException
{
   if (File.separatorChar != '/') cls = cls.replace('/',File.separatorChar);

   File f = new File(d,cls);

   return new FileInputStream(f);
}



private InputStream findInJar(File j,String cls) throws IOException
{
   JarFile jf = null;

   synchronized (current_jars) {
      jf = current_jars.get(j);
      if (jf == null) {
	 jf = new JarFile(j);
	 current_jars.put(j,jf);
       }
    }

   ZipEntry ent = jf.getEntry(cls);
   if (ent == null) return null;

   return jf.getInputStream(ent);
}




/********************************************************************************/
/*										*/
/*	Ouptut methods								*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return getName();
}



/********************************************************************************/
/*										*/
/*	Debugging routines							*/
/*										*/
/********************************************************************************/

@Override public void dumpProject()
{
   System.err.println("PROJECT: " + getName());
   System.err.println("\tOWNER: " + getOwner());
   System.err.println("\tDESCRIPTION: " + getDescription());
   System.err.println("\tCLASSPATH");
   for (String s : getClassPath()) System.err.println("\t\t" + s);
   System.err.println("\tUSER PACKAGES");
   for (String s : getUserPackages()) System.err.println("\t\t" + s);
   System.err.println("\tSOURCEPATH");
   for (String s : getSourcePath()) System.err.println("\t\t" + s);
   System.err.println("\tSOURCE FILES");
   for (File f : getSourceFiles()) System.err.println("\t\t" + f);
   System.err.println("\tSTART CLASSES");
   for (String s : getStartClasses()) System.err.println("\t\t" + s);
}




}	// end of class IvyProjectBase




/* end of IvyProjectBase.java */
