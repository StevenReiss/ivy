/********************************************************************************/
/*										*/
/*		JcompControl.java						*/
/*										*/
/*	Main access point for java-based semantics				*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 * This program and the accompanying materials are made available under the	 *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at								 *
 *	http://www.eclipse.org/legal/epl-v10.html				 *
 *										 *
 ********************************************************************************/

/* SVN: $Id: JcompControl.java,v 1.9 2017/10/24 12:47:04 spr Exp $ */



package edu.brown.cs.ivy.jcomp;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.jcode.JcodeDataType;
import edu.brown.cs.ivy.jcode.JcodeFactory;

import java.io.File;
import java.util.*;



/**
 *	This is the main class for accessing the Java compilation framework.  It
 *	provides entry points for compiling a single file or a set of files or a
 *	set of files with respect to an existing context.
 **/

public class JcompControl implements JcompConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<JcompSource,JcompFile>  semantic_map;
private JcompContext base_context;
private JcompTyper   system_typer;
private JcompContext android_context;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

/**
 *	Instantiate a new instance of the Java compilation framework.
 **/

public JcompControl()
{
   semantic_map = new WeakHashMap<JcompSource,JcompFile>();
   base_context = new JcompContextAsm(null);
   system_typer = new JcompTyper(base_context);
   android_context = null;
}




/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

public synchronized void addBaseJars(Iterable<String> jars)
{
   if (jars == null) return;
   int ct = 0;
   for (String s : jars) if (s != null) ++ct;
   if (ct == 0) return;

   base_context = new JcompContextAsm(base_context,jars);
}


public synchronized void useAndroid()
{
   List<String> androidjar = new ArrayList<String>();
   File f = IvyFile.expandFile("$(IVY)/lib/androidjar");
   if (f.list() != null) {
      for (String pn : f.list()) {
	 if (pn.endsWith(".jar")) {
	    File f1 = new File(f,pn);
	    androidjar.add(f1.getAbsolutePath());
	  }
       }
    }
   addBaseJars(androidjar);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

/**
 *	Return a handle to the semantic data for a given source file.  This call should
 *	be used to get the data for a single file after creating a JcompProject with
 *	that file and resolving the project.
 **/

public synchronized JcompSemantics getSemanticData(JcompSource rf)
{
   JcompFile rjf = semantic_map.get(rf);
   if (rjf == null) {
      rjf = new JcompFile(rf);
      semantic_map.put(rf,rjf);
    }
   return rjf;
}


public JcompTyper getSystemTyper()
{
   return system_typer;
}


public JcompType getSystemType(String name)
{
   return system_typer.findSystemType(name);
}


public static JcompType convertType(JcompTyper typer,JcodeDataType cty)
{
   if (cty == null) return null;
   String tnm = cty.getName();
   JcompType rslt = typer.findType(tnm);
   if (rslt != null) return rslt;
   rslt = typer.findSystemType(tnm);
   return rslt;
}




/********************************************************************************/
/*										*/
/*	Project management methods						*/
/*										*/
/********************************************************************************/

/**
 *	Return a handle to the semantic data for a set of files.  These files are
 *	compiled together so that they can refer to each other.
 **/

public JcompProject getProject(Collection<JcompSource> rfs)
{
   return getProject((Collection<String>) null,rfs,false);
}

public JcompProject getProject(Collection<JcompSource> rfs, boolean android)
{
   return getProject((Collection<String>) null,rfs,android);
}



/**
 *	Return a handle to the semantic data for a set of files.  These files are
 *	compiled together so they can refer to each other.  In addition, the name
 *	of a jar file can be provided.	All the classes in the corresonding jar
 *	file will be accessible when compiling the set of files.
 **/

public JcompProject getProject(String jar,Collection<JcompSource> rfs)
{
   return getProject(jar,rfs,false);
}


public JcompProject getProject(String jar,Collection<JcompSource> rfs,boolean android)
{
   List<String> jars = null;
   if (jar != null) {
      jars = Collections.singletonList(jar);
    }
   return getProject(jars,rfs,android);
}



public synchronized JcompProject getProject(Collection<String> jars,Collection<JcompSource> rfs)
{
   return getProject(jars,rfs,false);
}



public synchronized JcompProject getProject(Collection<String> jars,
	 Collection<JcompSource> rfs,boolean android)
{
   JcompContext ctx = base_context;

   if (android) {
      if (android_context == null) {
	 List<String> androidjar = new ArrayList<String>();
	 File f = IvyFile.expandFile("$(IVY)/lib/androidjar");
	 if (f.list() != null) {
	    for (String pn : f.list()) {
	       if (pn.endsWith(".jar")) {
		  File f1 = new File(f,pn);
		  androidjar.add(f1.getAbsolutePath());
		}
	     }
	  }
	 android_context = new JcompContextAsm(ctx,androidjar);
       }
      ctx = android_context;
    }

   if (jars != null) {
      for (String jarnm : jars) {
	 if (jarnm != null) ctx = new JcompContextAsm(ctx,jarnm);
       }
    }

   JcompProjectImpl root = new JcompProjectImpl(ctx);
   for (JcompSource rf : rfs) {
      JcompFile rjf = semantic_map.get(rf);
      if (rjf == null) {
	 rjf = new JcompFile(rf);
	 semantic_map.put(rf,rjf);
       }
      root.addFile(rjf);
    }

   return root;
}



public synchronized JcompProject getProject(JcodeFactory jf,Collection<JcompSource> rfs)
{
   JcompContext ctx = new JcompContextCode(jf);
   JcompProjectImpl root = new JcompProjectImpl(ctx);
   for (JcompSource rf : rfs) {
      JcompFile rjf = semantic_map.get(rf);
      if (rjf == null) {
	 rjf = new JcompFile(rf);
	 semantic_map.put(rf,rjf);
       }
      root.addFile(rjf);
    }
   
   return root; 
}




public void freeProject(JcompProject proj)
{
   if (proj == null) return;
   
   for (JcompSemantics sem : proj.getSources()) {
      JcompSource src = sem.getFile();
      semantic_map.remove(src);
    }
   JcompProjectImpl impl = (JcompProjectImpl) proj;
   impl.clear();
}



/**
 *	Return a handle to the semantic data for a set of files.  These files are
 *	compiled together so they can refer to each other.  In addition, the name
 *	of a jar file can be provided.	All the classes in the corresonding jar
 *	file will be accessible when compiling the set of files.
 *	Use getProject instead.
 **/

@Deprecated
public synchronized JcompProject getSemanticData(String jar,Collection<JcompSource> rfs)
{
   return getProject(jar,rfs);
}


/**
 *	Return a handle to the semantic data for a set of files.  These files are
 *	compiled together so that they can refer to each other.
 *	(Use getProject instead).
 **/

@Deprecated
public synchronized JcompProject getSemanticData(Collection<JcompSource> rfs)
{
   return getProject(rfs);
}



/********************************************************************************/
/*										*/
/*	Typer related methods							*/
/*										*/
/********************************************************************************/

/**
 *	Return a JavaTyper with the appropriate context for looking up system types.
 **/

public synchronized JcompTyper getTyper(Collection<String> jars,boolean android)
{
   JcompContext ctx = base_context;

   if (android) {
      List<String> androidjar = new ArrayList<String>();
      File f = IvyFile.expandFile("$(IVY)/lib/androidjar");
      if (f.list() != null) {
	 for (String pn : f.list()) {
	    if (pn.endsWith(".jar")) {
	       File f1 = new File(f,pn);
	       androidjar.add(f1.getAbsolutePath());
	     }
	  }
       }
      ctx = new JcompContextAsm(ctx,androidjar);
    }

   if (jars != null) {
      for (String s : jars) {
	 if (s != null) ctx = new JcompContextAsm(ctx,s);
       }
    }

   return new JcompTyper(ctx);
}



}	// end of class JcompControl




/* end of JcompLanguage.java */

