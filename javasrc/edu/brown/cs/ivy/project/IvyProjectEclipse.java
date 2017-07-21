/********************************************************************************/
/*										*/
/*		IvyProjectEclipse.java						*/
/*										*/
/*	IVY project implementation for using eclipse projects			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/project/IvyProjectEclipse.java,v 1.9 2015/11/20 15:09:25 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyProjectEclipse.java,v $
 * Revision 1.9  2015/11/20 15:09:25  spr
 * Reformatting.
 *
 * Revision 1.8  2013/09/24 01:07:52  spr
 * data format
 *
 * Revision 1.7  2012-01-12 01:27:55  spr
 * Fix exceptions
 *
 * Revision 1.6  2011-01-15 18:45:25  spr
 * Handle indirect links to projects
 *
 * Revision 1.5  2010-09-16 23:37:19  spr
 * Delete one blank line.
 *
 * Revision 1.4  2010-07-01 21:57:59  spr
 * Always check files at startup.
 *
 * Revision 1.3  2010-06-01 15:38:00  spr
 * Updates for eclipse.
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

import java.io.*;
import java.util.*;



class IvyProjectEclipse extends IvyProjectBase {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private File workspace_dir;
private File eclipse_dir;
private String eclipse_name;
private Map<String,String> path_vars;
private Map<String,List<String>> path_excludes;
private Map<String,List<String>> path_includes;
private List<String> output_dirs;
private String	output_dir;
private Set<File> check_files;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

IvyProjectEclipse(IvyProjectManager pm,String name,File workspace,String ename)
	throws IvyProjectException
{
   super(pm,name);

   workspace_dir = workspace;

   initialize(ename);
}



IvyProjectEclipse(IvyProjectManager pm,Element e) throws IvyProjectException
{
   super(pm,e);

   workspace_dir = new File(IvyXml.getTextElement(e,"WORKSPACE"));
   String ename = IvyXml.getTextElement(e,"ECLIPSENAME");

   initialize(ename);
}



private void initialize(String ename) throws IvyProjectException
{
   if (!workspace_dir.exists() || !workspace_dir.isDirectory())
      throw new IvyProjectException("Eclipse workspace " + workspace_dir + " doesn't exist");

   check_files = new HashSet<File>();

   File f = new File(workspace_dir,ename);
   if (!f.exists() || !f.isDirectory())
      throw new IvyProjectException("Project " + ename + " not defined in workspace " + workspace_dir);

   File f1 = new File(f,".classpath");
   File f2 = new File(f,".project");
   if (!f1.exists() || !f2.exists())
      throw new IvyProjectException("Project " + ename + " not valid eclipse workspace");

   check_files.add(f1);
   check_files.add(f2);

   eclipse_dir = f;
   eclipse_name = ename;

   path_vars = new HashMap<String,String>();
   path_includes = new HashMap<String,List<String>>();
   path_excludes = new HashMap<String,List<String>>();
   output_dirs = new ArrayList<String>();
   output_dir = null;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override protected String getProjectType()			{ return "ECLIPSE"; }

@Override public File getWorkspace()				{ return workspace_dir; }



/********************************************************************************/
/*										*/
/*	Update methods								*/
/*										*/
/********************************************************************************/

protected synchronized @Override void clear()
{
   super.clear();

   path_vars = new HashMap<String,String>();
   path_includes = new HashMap<String,List<String>>();
   path_excludes = new HashMap<String,List<String>>();
   output_dirs = new ArrayList<String>();
   output_dir = null;
}



@Override public synchronized void update()
{
   clear();

   loadPathVars();

   loadClassPath(eclipse_dir,true);

   computePackages();
   computeClasses();

   findExecutables();
}




/********************************************************************************/
/*										*/
/*	Static query methods							*/
/*										*/
/********************************************************************************/

static Collection<String> getProjects(File ws)
{
   List<String> rslt = new ArrayList<String>();

   if (ws == null || !ws.exists() || !ws.isDirectory()) return rslt;

   File [] fls = ws.listFiles();
   if (fls != null) {
      for (File f : fls) {
	 if (f.isDirectory()) {
	    File [] pfls = f.listFiles();
	    if (pfls == null) continue;
	    int fnd = 0;
	    for (File pf : pfls) {
	       if (pf.getName().equals(".project") || pf.getName().equals(".classpath")) ++fnd;
	     }
	    if (fnd == 2) rslt.add(f.getName());
	  }
       }
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Methods to go through general eclipse files				*/
/*										*/
/********************************************************************************/

private void loadPathVars()
{
   File d = new File(workspace_dir,IVY_PATH_VAR_FILE);
   if (!d.exists()) return;
   check_files.add(d);

   try {
      BufferedReader fr = new BufferedReader(new FileReader(d));
      for ( ; ; ) {
	 String ln = fr.readLine();
	 if (ln == null) break;
	 ln = ln.trim();
	 if (ln.startsWith("#") || ln.length() == 0) continue;
	 int idx = ln.indexOf("=");
	 if (idx < 0) continue;
	 String key = ln.substring(0,idx);
	 String val = ln.substring(idx+1);
	 if (key.startsWith("org.ecliplse.jdt.core.classpathVariable.")) {
	    int idx1 = key.lastIndexOf(".");
	    String var = key.substring(idx1+1);
	    path_vars.put(var,val);
	  }
       }
      fr.close();
    }
   catch (IOException e) { }
}





/********************************************************************************/
/*										*/
/*	Methods to go through .classpath files					*/
/*										*/
/********************************************************************************/

private void loadClassPath(File dir,boolean primary)
{
   File cpf = new File(dir,".classpath");
   if (!cpf.exists()) return;
   Map<String,String> links = setupLinks(dir);

   Element e = IvyXml.loadXmlFromFile(cpf);
   for (Element ent : IvyXml.children(e,"classpathentry")) {
      String k = IvyXml.getAttrString(ent,"kind");
      if (k == null) ;
      else if (k.equals("src")) {
	 String src = IvyXml.getAttrString(ent,"path");
	 src = getFullPath(src,dir,links);
	 String cmb = IvyXml.getAttrString(ent,"combineaccessrules");
	 if (cmb == null) {
	    String ex = IvyXml.getAttrString(ent,"excluding");
	    addToClassPath(src);
	    addToSourcePath(src);
	    if (ex != null) {
	       List<String> r = new ArrayList<String>();
	       for (StringTokenizer tok = new StringTokenizer(ex,"|"); tok.hasMoreTokens(); ) {
		  String pat = tok.nextToken();
		  pat = makeRegex(pat);
		  r.add(pat);
		}
	       path_excludes.put(src,r);
	     }
	    String in = IvyXml.getAttrString(ent,"including");
	    if (in != null) {
	       List<String> r = new ArrayList<String>();
	       for (StringTokenizer tok = new StringTokenizer(in,"|"); tok.hasMoreTokens(); ) {
		  String pat = tok.nextToken();
		  pat = makeRegex(pat);
		  r.add(pat);
		}
	       path_includes.put(src,r);
	     }
	  }
	 else {
	    File pf = getWorkspaceFile(src,dir);
	    if (pf.exists()) {
	       loadClassPath(pf,false);
	     }
	    else {
	       File f1 = new File(workspace_dir,".metadata");
	       File f2 = new File(f1,".plugins");
	       File f3 = new File(f2,"org.eclipse.core.resources");
	       File f4 = new File(f3,".projects");
	       File f5 = new File(f4,src.substring(1));
	       File f6 = new File(f5,".location");
	       if (f6.exists()) {
		  try {
		     DataInputStream dis = new DataInputStream(new FileInputStream(f6));
		     for (int i = 0; i < 16; ++i) dis.readByte();	// skip header
		     String loc = dis.readUTF();
		     if (loc.length() > 0) {
			if (loc.startsWith("URI//file:")) loc = loc.substring(10);
			File f7 = new File(loc);
			if (f7.exists()) loadClassPath(f7,false);
		     }
		     dis.close();
		  }
		  catch (IOException ex) { }
		}
	     }
	  }
       }
      else if (k.equals("con")) ;
      else if (k.equals("lib")) {
	 String lib = IvyXml.getAttrString(ent,"path");
	 lib = getFullPath(lib,dir,links);
	 addToClassPath(lib);
       }
      else if (k.equals("var")) {
	 String pth = IvyXml.getAttrString(ent,"path");
	 pth = getVarPath(pth);
	 if (pth != null) addToClassPath(pth);
	 String src = IvyXml.getAttrString(ent,"sourcepath");
	 src = getVarPath(src);
	 if (src != null) {
	    addToClassPath(src);
	    addToSourcePath(src);
	  }
       }
      else if (k.equals("output")) {
	 String out = IvyXml.getAttrString(ent,"path");
	 out = getFullPath(out,dir,links);
	 addToClassPath(out);
	 if (primary) output_dir = out;
	 output_dirs.add(out);
       }
    }
}



String makeRegex(String fpat)
{
   StringBuffer buf = new StringBuffer();
   buf.append("^");

   for (int i = 0; i < fpat.length(); ++i) {
      if (fpat.charAt(i) == '*' && i+1 < fpat.length() && fpat.charAt(i+1) == '*') {
	 i += 1;
	 buf.append(".*");
       }
      else if (fpat.charAt(i) == '*') {
	 buf.append("[^/\\\\]");
       }
      else buf.append(fpat.charAt(i));
    }

   buf.append("$");

   return buf.toString();
}



/********************************************************************************/
/*										*/
/*	Methods to process .project file					*/
/*										*/
/********************************************************************************/

private Map<String,String> setupLinks(File dir)
{
   Map<String,String> rslt = new HashMap<String,String>();

   File pf = new File(dir,".project");

   Element e = IvyXml.loadXmlFromFile(pf);

   Element le = IvyXml.getChild(e,"linkedResources");
   if (le == null) return rslt;

   for (Element lnk : IvyXml.children(le,"link")) {
      String nm = IvyXml.getTextElement(lnk,"name");
      String loc = IvyXml.getTextElement(lnk,"location");
      String np = getFullPath(nm,dir,null);
      rslt.put(np,loc);
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Path access methods							*/
/*										*/
/********************************************************************************/

private String getFullPath(String file,File dir,Map<String,String> links)
{
   String rslt;

   if (file.startsWith("/") || file.startsWith("\\")) rslt = file;
   else {
      File f = new File(dir,file);
      rslt = f.getPath();
      if (links != null) {
	 for (Map.Entry<String,String> ent : links.entrySet()) {
	    String k = ent.getKey();
	    if (rslt.startsWith(k)) {
	       int ln = k.length();
	       if (rslt.length() == ln || rslt.charAt(ln) == File.separatorChar) {
		  rslt = ent.getValue() + rslt.substring(ln);
		  break;
		}
	     }
	  }
       }
    }

   return rslt;
}



private File getWorkspaceFile(String file,File dir)
{
   File rslt = null;

   if (file.startsWith("/") || file.startsWith("\\")) {
      rslt = new File(workspace_dir,file.substring(1));
    }
   else {
      rslt = new File(dir,file);
    }

   return rslt;
}


private String getVarPath(String path)
{
   if (path == null) return null;

   int idx = path.indexOf("/");
   int idx1 = path.indexOf("\\");
   if (idx < 0) idx = idx1;
   else if (idx1 > 0 && idx1 < idx) idx = idx1;

   if (idx < 0) {
      return path_vars.get(path);
    }
   else {
      String p = path.substring(0,idx);
      p = path_vars.get(p);
      if (p == null) return null;
      return p + path.substring(idx);
    }
}



/********************************************************************************/
/*										*/
/*	Source file methods							*/
/*										*/
/********************************************************************************/

enum MatchType {
   MATCH_NONE,
   MATCH_YES,
   MATCH_NO
}



protected @Override FileFilter createSourceFilter()
{
   if (path_excludes == null && path_includes == null) return super.createSourceFilter();

   return new EclipseSourceFilter();
}



private MatchType fileMatches(File f,Map<String,List<String>> pats)
{
   String nm = f.getPath();
   if (f.isDirectory()) nm += "/";

   for (Map.Entry<String,List<String>> ent : pats.entrySet()) {
      String fp = ent.getKey() + File.separator;
      if (nm.startsWith(fp)) {
	 for (String p : ent.getValue()) {
	    if (fileNameMatches(nm,fp,p)) return MatchType.MATCH_YES;
	  }
	 return MatchType.MATCH_NO;
       }
    }

   return MatchType.MATCH_NONE;
}



private boolean fileNameMatches(String nm,String pfx,String pat)
{
   int ln = pfx.length();
   String r = nm.substring(ln);

   return r.matches(pat);
}



private class EclipseSourceFilter extends SourceFilter {

   @Override public boolean accept(File p) {
      if (!super.accept(p)) return false;

      if (path_includes != null) {
	 switch (fileMatches(p,path_includes)) {
	    case MATCH_NONE :
	       break;
	    case MATCH_YES :
	       return true;
	    case MATCH_NO :
	       return false;
	  }
       }

      if (path_excludes != null) {
	 switch (fileMatches(p,path_excludes)) {
	    case MATCH_NONE :
	       break;
	    case MATCH_YES :
	       return false;
	    case MATCH_NO :
	       return true;
	  }
       }

      return true;
    }

}	// end of subclass EclipseSourceFilter




/********************************************************************************/
/*										*/
/*	Methods for finding user packages					*/
/*										*/
/********************************************************************************/

private void computePackages()
{
   File df = new File(output_dir);
   addPackages(df,null);
}



private void addPackages(File dir,String pfx)
{
   File [] fls = dir.listFiles();
   if (fls == null) return;

   for (int i = 0; i < fls.length; ++i) {
      if (fls[i].getPath().endsWith(".class")) {
	 if (pfx == null) addUserPackage(IVY_DEFAULT_PACKAGE);
	 else addUserPackage(pfx);
	 break;
       }
    }

   for (int i = 0; i < fls.length; ++i) {
      if (fls[i].isDirectory()) {
	 String sfx = fls[i].getName();
	 if (pfx == null) addPackages(fls[i],sfx);
	 else addPackages(fls[i],pfx + "." + sfx);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Methods for finding user classes					*/
/*										*/
/********************************************************************************/

private void computeClasses()
{
   for (String d : output_dirs) {
      File df = new File(d);
      if (df.exists() && df.isDirectory()) {
	 addClasses(df,null,d.equals(output_dir));
       }
    }
}



private void addClasses(File dir,String pfx,boolean primary)
{
   File [] fls = dir.listFiles();
   if (fls == null) return;

   for (int i = 0; i < fls.length; ++i) {
      if (fls[i].getPath().endsWith(".class")) {
	 String s = fls[i].getName();
	 int idx = s.lastIndexOf(".");
	 s = s.substring(0,idx);
	 if (pfx != null) s = pfx + "." + s;
	 addUserClass(fls[i],s,primary);
       }
    }

   for (int i = 0; i < fls.length; ++i) {
      if (fls[i].isDirectory()) {
	 String sfx = fls[i].getName();
	 if (pfx == null) addClasses(fls[i],sfx,primary);
	 else addClasses(fls[i],pfx + "." + sfx,primary);
       }
    }
}




private void addUserClass(File f,String nm,boolean primary)
{
   if (primary && checkStartClass(f)) {
      addStartClass(nm);
    }
}




private boolean checkStartClass(File f)
{
   byte [] match = new byte[] { '\004','m','a','i','n' };

   BufferedInputStream fis = null;

   try {
      fis = new BufferedInputStream(new FileInputStream(f));
      byte [] buf = new byte[1024];
      int ctr = 0;
      int idx = 0;
      for ( ; ; ) {
	 int len = fis.read(buf,0,buf.length);
	 if (len < 0) break;
	 for (idx = 0; idx < len; ++idx) {
	    if (buf[idx] == match[ctr]) {
	       ++ctr;
	       if (ctr >= match.length) return true;
	     }
	    else if (buf[idx] == match[0]) ctr = 1;
	    else ctr = 0;
	  }
       }
    }
   catch (IOException e) {
      return false;
    }
   finally {
      if (fis != null) {
	 try {
	    fis.close();
	  }
	 catch (IOException e) { }
       }
    }

   return false;
}




/********************************************************************************/
/*										*/
/*	Methods to find executables						*/
/*										*/
/********************************************************************************/

private void findExecutables()
{
   File dir = new File(workspace_dir,IVY_EXEC_DIR);
   if (!dir.exists()) return;

   File [] lchs = dir.listFiles();
   for (int i = 0; i < lchs.length; ++i) {
      if (lchs[i].getPath().endsWith(".launch")) {
	 check_files.add(lchs[i]);
	 Element e = IvyXml.loadXmlFromFile(lchs[i]);
	 if (e != null) addExecutable(e);
       }
    }
}



private void addExecutable(Element xml)
{
   String mcls = null;
   String pargs = null;
   String jargs = null;

   for (Element e : IvyXml.children(xml)) {
      String k = IvyXml.getAttrString(e,"key");
      if (k.equals("org.eclipse.jdt.launching.MAIN_TYPE")) {
	 mcls = IvyXml.getAttrString(e,"value");
       }
      else if (k.equals("org.eclipse.jdt.launching.PROJECT_ATTR")) {
	 String proj = IvyXml.getAttrString(e,"value");
	 if (!proj.equals(eclipse_name)) return;	// not for this project
       }
      else if (k.equals("org.eclipse.jdt.launching.PROGRAM_ARGUMENTS")) {
	 pargs = IvyXml.getAttrString(e,"value");
       }
      else if (k.equals("org.eclipse.jdt.lanunching.VM_ARGUMENTS")) {
	 jargs = IvyXml.getAttrString(e,"value");
       }
    }

   EclipseExec ee = new EclipseExec(mcls,pargs,jargs);

   addExecutable(ee);
}




private class EclipseExec extends IvyExecutableBase {

   EclipseExec(String mcls,String pargs,String jargs) {
      super(IvyProjectEclipse.this);

      String run = "$(JAVA) ";
      if (jargs != null) run += jargs + " ";
      run += mcls;
      if (pargs != null) run += " " + pargs;

      setStartClass(mcls);
      setJavaArgs(jargs);
      setProgramArgs(pargs);
      setRunCommand(run);
    }

}	// end of subclass EclipseExec




/********************************************************************************/
/*										*/
/*	Saving code								*/
/*										*/
/********************************************************************************/

@Override protected void localSaveProject(IvyXmlWriter xw)
{
   xw.textElement("WORKSPACE",workspace_dir.getPath());
   xw.textElement("ECLIPSENAME",eclipse_dir.getName());
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return getName() + " (Eclipse)";
}



/********************************************************************************/
/*										*/
/*	Test code								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   IvyProjectManager pm = IvyProjectManager.getManager();

   try {
      IvyProject ip = pm.findProject("s6");
      if (ip == null) ip = pm.createEclipseProject("s6",new File("/home/spr/Eclipse/workspace2"),"s6");
      ip.dumpProject();
    }
   catch (IvyProjectException e) {
      System.err.println("IVYPROJECT: Problem creating eclipse project: " + e);
    }
}




}	// end of class IvyProjectEclipse




/* end of IvyProjectEclipse.java */
