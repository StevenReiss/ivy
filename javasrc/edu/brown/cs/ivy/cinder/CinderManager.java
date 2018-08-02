/********************************************************************************/
/*										*/
/*		CinderManager.java						*/
/*										*/
/*	External interface for classfile patching				*/
/*										*/
/********************************************************************************/
/*	Copyright 1997 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 1997, Brown University, Providence, RI.				 *
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
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,	 *
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS 	 *
 *  SOFTWARE.									 *
 *										 *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/cinder/CinderManager.java,v 1.22 2015/11/20 15:09:10 spr Exp $ */


/*********************************************************************************
 *
 * $Log: CinderManager.java,v $
 * Revision 1.22  2015/11/20 15:09:10  spr
 * Reformatting.
 *
 * Revision 1.21  2011-09-26 23:07:39  spr
 * Use proper base path from variables.
 *
 * Revision 1.20  2009-10-02 00:18:13  spr
 * Import clean up.
 *
 * Revision 1.19  2009-09-17 01:54:51  spr
 * Enable patching at line number level.
 *
 * Revision 1.18  2009-06-04 18:49:20  spr
 * Remove getenv calls.
 *
 * Revision 1.17  2008-06-02 22:16:39  spr
 * Allow others to set debugging flag.
 *
 * Revision 1.16  2008-03-14 12:25:18  spr
 * Fixes for java 1.6; code cleanup.
 *
 * Revision 1.15  2007-08-10 02:10:02  spr
 * Cleanups from Eclipse
 *
 * Revision 1.14  2007-05-10 01:47:59  spr
 * Formating changes.
 *
 * Revision 1.13  2007-05-05 15:22:18  spr
 * Fix boot path for mac.
 *
 * Revision 1.12  2006-11-09 00:32:07  spr
 * Move base path computation to common file.
 *
 * Revision 1.11  2006/07/03 18:14:37  spr
 * Make checkIfClassExists static.
 *
 * Revision 1.10  2006/06/21 02:18:08  spr
 * Add call to get patch type.
 *
 * Revision 1.9  2006/04/07 20:23:42  spr
 * Add error check.
 *
 * Revision 1.8  2005/07/08 20:56:58  spr
 * Upgrade patching to handle constructors; add call to create local variable.
 *
 * Revision 1.7  2005/06/07 02:18:19  spr
 * Update for java 5.0
 *
 * Revision 1.6  2005/05/07 22:25:39  spr
 * Updates for java 5.0
 *
 * Revision 1.5  2005/04/28 21:48:04  spr
 * Check for loading problems; add special call creation method.
 *
 * Revision 1.4  2004/05/05 02:31:32  spr
 * Fix up import conflicts.
 *
 * Revision 1.3  2004/05/05 02:28:08  spr
 * Update import lists using eclipse.
 *
 * Revision 1.2  2003/05/24 00:28:25  spr
 * Add a class filter for package classes.
 *
 * Revision 1.1  2003/03/29 03:40:25  spr
 * Move CINDER interface to JikesBT from Bloom to Ivy.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.cinder;


import edu.brown.cs.ivy.file.IvyFile;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_CodeAttribute;
import com.ibm.jikesbt.BT_Factory;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Item;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_Repository;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;




public class CinderManager extends BT_Factory
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private CinderPatchType patch_type;
private boolean 	patch_all;
private Map<BT_Class,String>   class_files;
private CinderFilter	patch_filter;

public	static boolean	do_debug = false;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public CinderManager()
{
   patch_type = null;
   patch_all = false;
   class_files = new HashMap<BT_Class,String>();
   patch_filter = null;
   BT_Factory.factory = this;
}



public void setPatchType(CinderPatchType pt)		{ patch_type = pt; }

public CinderPatchType getPatchType()			{ return patch_type; }

public void setPatchAll(boolean fg)			{ patch_all = fg; }

public void setPatchFilter(CinderFilter cf)		{ patch_filter = cf; }




/********************************************************************************/
/*										*/
/*	Class path management methods						*/
/*										*/
/********************************************************************************/

public static void setClassPath(String cp)
{
   setClassPath(null,cp);
}


public static void setClassPath(String bp,String cp)
{
   BT_Class.resetClassPath();

   if (bp != null) {
      StringTokenizer tok = new StringTokenizer(bp,File.pathSeparator);
      while (tok.hasMoreTokens()) {
	 String path = tok.nextToken();
	 BT_Class.addClassPath(path);
       }
    }

   if (cp != null) {
      StringTokenizer tok = new StringTokenizer(cp,File.pathSeparator);
      while (tok.hasMoreTokens()) {
	 String path = tok.nextToken();
	 BT_Class.addClassPath(path);
       }
    }
}




public void clear()
{
   class_files = new HashMap<>();
   BT_Repository.empty();
}




/********************************************************************************/
/*										*/
/*	Base path methods							*/
/*										*/
/********************************************************************************/

public static String computeBasePath()
{
   return computeBasePath(null);
}


public static String computeBasePath(String javahome)
{
   if (javahome == null) javahome = System.getProperty("java.home");

   String dir = javahome + File.separator + "lib";

   String bp = addToBasePath(null,new File(dir));

   String bas = IvyFile.expandName("$(JROOT)");
   if (bas != null) bp = addToBasePath(bp,new File(bas));
   bas = IvyFile.expandName("$(JAVA_BOOT)");
   if (bas != null) {
      StringTokenizer tok = new StringTokenizer(bas,File.pathSeparator);
      while (tok.hasMoreTokens()) {
	 String t = tok.nextToken();
	 bp = addToBasePath(bp,new File(t));
       }
    }
   return bp;
}


private static String addToBasePath(String bp,File dir)
{
   if (!dir.exists()) return bp;

   if (dir.isDirectory()) {
      File [] cnts = dir.listFiles();
      for (int i = 0; i < cnts.length; ++i) {
	 bp = addToBasePath(bp,cnts[i]);
       }
    }
   else if (dir.getName().endsWith(".jar")) {
      if (bp == null) bp = dir.getAbsolutePath();
      else bp = bp + File.pathSeparator + dir.getAbsolutePath();
    }

   return bp;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public Iterator<BT_Class> getAllClasses()	{ return class_files.keySet().iterator(); }


public Set<BT_Class> getAllClassSet()		{ return new HashSet<BT_Class>(class_files.keySet()); }


public int getAllClassSize()			{ return class_files.size(); }


public String getClassFile(BT_Class bc) 	{ return class_files.get(bc); }



/********************************************************************************/
/*										*/
/*	Methods to handle name conversion					*/
/*										*/
/********************************************************************************/

public static String getMethodName(BT_Method m)
{
   String s = m.fullName();

   if (m.isConstructor()) s += ".<init>";
// else if (m.isStaticInitializer()) s += ".<clinit>";

   return s;
}



public static String getMethodSignature(BT_Method m)
{
   BT_MethodSignature sgn = m.getSignature();

   StringBuffer buf = new StringBuffer();
   buf.append(sgn.returnType.getName());
   buf.append("(");
   int act = 0;
   for (Enumeration<?> e = sgn.types.elements(); e.hasMoreElements(); ) {
      BT_Class bc = (BT_Class) e.nextElement();
      if (act++ != 0) buf.append(",");
      buf.append(bc.getName());
    }
   buf.append(")");

   return buf.toString();
}



public static String getMethodUniqueName(BT_Method m)
{
   BT_MethodSignature sgn = m.getSignature();

   StringBuffer buf = new StringBuffer();
   buf.append(m.fullName());
   buf.append("@(");
   int act = 0;
   for (Enumeration<?> e = sgn.types.elements(); e.hasMoreElements(); ) {
      BT_Class bc = (BT_Class) e.nextElement();
      if (act++ != 0) buf.append(",");
      buf.append(bc.getName());
    }
   buf.append(")");

   return buf.toString();
}



/********************************************************************************/
/*										*/
/*	Methods to actually to patching 					*/
/*										*/
/********************************************************************************/

public void processFile(String file,String outf)
{
   File f = new File(file);

   CinderPatcher cp = new CinderPatcher(patch_type);

   if (!f.canRead()) {
      System.err.println("CINDER: Can't read input file " + file);
      System.exit(1);
    }

   try {
      BT_Class btc = BT_Class.loadFromFile(f);
      if (!cp.setup(btc,file,outf)) return;
      cp.patch(btc);
      btc.write(outf);
      cp.finish();
    }
   catch (Exception e) {
      System.err.println("CINDER: Problem loading class " + file + ": " + e.getMessage());
    }
}




/********************************************************************************/
/*										*/
/*	Method to patch a class to an output stream				*/
/*										*/
/********************************************************************************/

public BT_Class processClass(String cnm,OutputStream os)
{
   DataOutputStream dos = (os == null ? null : new DataOutputStream(os));
   BT_Class btc = BT_Class.forName(cnm);

   CinderPatcher cp = new CinderPatcher(patch_type);

   try {
      cp.patch(btc);
      if (dos != null) {
	 btc.write(dos);
	 dos.flush();
       }
    }
   catch (Exception e) {
      System.err.println("CINDER: Problem loading class " + cnm + ": " + e.getMessage());
      e.printStackTrace();
    }

   return btc;
}



public static boolean checkIfClassExists(String nm)
{
   if (nm == null) return false;

   try {
      BT_Class btc = BT_Class.forName(nm);
      if (btc != null && !btc.isStub()) return true;
    }
   catch (Throwable t) {
      System.err.println("CINDER: Problem looking up class: " + t);
      t.printStackTrace();
    }

   return false;
}




/********************************************************************************/
/*										*/
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

public static String convertClassNameToJarName(String nm)
{
   StringBuffer buf = new StringBuffer();

   for (int i = 0; i < nm.length(); ++i) {
      char c = nm.charAt(i);
      if (c == '.' || c == File.separatorChar) c = '/';
      buf.append(c);
    }

   buf.append(".class");

   return buf.toString();
}




/********************************************************************************/
/*										*/
/*	BT interface methods							*/
/*										*/
/********************************************************************************/

@Override public void noteAnomalyInClass(String msg)
{
   if (do_debug) super.noteAnomalyInClass(msg);
}



@Override public void noteClassLoaded(BT_Class c,String from,DataInputStream dis)
{
   class_files.put(c,from);

   if (do_debug) super.noteClassLoaded(c,from,dis);
}


@Override public BT_Class noteClassNotFound(String cls)
{
   if (do_debug) System.err.println("CINDER: Class not found: " + cls);

   return BT_Class.createStub(cls);
}



@Override public void noteClassPathProblem(String ent,String msg)
{
   if (do_debug) super.noteClassPathProblem(ent,msg);
}



@Override public void noteClassReadIOException(String cls,String fil,IOException ex)
{
   if (do_debug) super.noteClassReadIOException(cls,fil,ex);
}



@Override public void noteClassSaved(BT_Class c)
{
   if (do_debug) super.noteClassSaved(c);
}



public void noteClassVerifyFailure(String cls,String fil,String hint,Exception ex)
{
   if (do_debug) super.noteClassVerifyFailure(cls,fil,hint,ex);
}


@Override public void noteClassWriteIOException(String cls,String fil,IOException ex)
{
   if (do_debug) super.noteClassWriteIOException(cls,fil,ex);
}


@Override public void noteIncompatibleClassChangeError(BT_Item u,BT_Item used,String msg)
{
   if (do_debug) super.noteIncompatibleClassChangeError(u,used,msg);
}



@Override public void noteExecutionFallsOffEndOfMethod(int dep,BT_CodeAttribute cd)
{
   if (do_debug) super.noteExecutionFallsOffEndOfMethod(dep,cd);
}



@Override public void noteStackNotEmptyAtReturn(int dep,int n,BT_CodeAttribute cd)
{
   if (do_debug) super.noteStackNotEmptyAtReturn(dep,n,cd);
}



@Override public void noteStackUnderflow(int dep,int ix,BT_CodeAttribute cd)
{
   if (do_debug) super.noteStackUnderflow(dep,ix,cd);
}



@Override public void noteStackDepthInconsistent(int ix,BT_CodeAttribute cd)
{
   if (do_debug) super.noteStackDepthInconsistent(ix,cd);
}



@Override public void noteToJbtUser(String msg,Exception ex)
{
   if (do_debug) super.noteToJbtUser(msg,ex);
}



@Override public void noteUndeclaredField(BT_Field f,String ref)
{
   if (do_debug) super.noteUndeclaredField(f,ref);
}



@Override public void noteUndeclaredMethod(BT_Method m,String ref)
{
   if (do_debug)  {
      System.err.println("CINDER: undeclared method: " + m.fullName() + " called from " + ref);
    }
}



@Override public boolean isProjectClass(String cls,Object fil)
{
   boolean fg = super.isProjectClass(cls,fil);

   if (patch_all) return true;
   if (patch_filter != null) fg = patch_filter.isProjectClass(cls,fil,fg);

   return fg;
}



}	// end of class CinderManager




/* end of CinderManager.java */
