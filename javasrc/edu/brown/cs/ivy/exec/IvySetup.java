/********************************************************************************/
/*										*/
/*		IvySetup.java							*/
/*										*/
/*	Class for setting up a remote IVY distribution				*/
/*										*/
/********************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Redistribution and use in source and binary forms, with or without		 *
 *  modification, are permitted provided that the following conditions are met:  *
 *										 *
 *  + Redistributions of source code must retain the above copyright notice,	 *
 *	this list of conditions and the following disclaimer.			 *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *	this list of conditions and the following disclaimer in the		 *
 *	documentation and/or other materials provided with the distribution.	 *
 *  + Neither the name of the Brown University nor the names of its		 *
 *	contributors may be used to endorse or promote products derived from	 *
 *	this software without specific prior written permission.		 *
 *										 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE	 *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE	 *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE	 *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 	 *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF	 *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS	 *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN	 *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)	 *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE	 *
 *  POSSIBILITY OF SUCH DAMAGE. 						 *
 *										 *
 ********************************************************************************/





package edu.brown.cs.ivy.exec;


import edu.brown.cs.ivy.file.IvyFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;



public final class IvySetup {



/********************************************************************************/
/*										*/
/*	Main program for creating setup 					*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   String dir = null;
   boolean nohost = false;

   if (args != null) {
      for (int i = 0; i < args.length; ++i) {
	 if (args[i].startsWith("-d") && i+1 < args.length) {      // -d <directory>
	    dir = args[++i];
	  }
	 else if (args[i].startsWith("-l")) {                      // -local
	    nohost = true;
	  }
	 else if (args[i].startsWith("-c")) {                      // -cwd
	    dir = System.getProperty("user.dir");
	  }
	 else {
	    System.err.println("IVYSETUP: ivysetup [-d directory]");
	    System.exit(1);
	  }
       }
    }

   if (dir == null) dir = findIvyDirectory();

   File libdir = new File(dir,"lib");
   File jarf = new File(libdir,"ivyfull.jar");
   boolean installok = jarf.exists();
   if (!installok) {
      jarf = new File(libdir,"ivy.jar");
      if (jarf.exists()) installok = true;
    }
   if (!installok) {
      File f1 = new File(dir,"javasrc");
      File f2 = new File(f1,"edu");
      File f3 = new File(f2,"brown");
      File f4 = new File(f3,"cs");
      File f5 = new File(f4,"ivy");
      File f6 = new File(f5,"exec");
      File f7 = new File(f6,"IvySetup.java");
      if (f7.exists()) installok = true;
    }

   if (!installok) {
      System.err.println("IVYSETUP: Please run in the top level ivy directory");
      System.exit(1);
    }

   File ivv = new File(System.getProperty("user.home"),".ivy");
   if (!ivv.exists()) {
      if (!ivv.mkdir()) {
	 System.err.println("IVYSETUP: Can't create ivy directory " + ivv);
	 System.exit(1);
       }
    }
   File pf = new File(ivv,"Props");

   Properties p = new Properties();

   if (pf.exists()) {
      try {
	 FileInputStream fis = new FileInputStream(pf);
	 p.loadFromXML(fis);
	 fis.close();
       }
      catch (IOException e) {
	 System.err.println("IVYSETUP: Problem loading old properties: " + e);
       }
    }

   if (!nohost) {
      try {
	 Registry rmireg = LocateRegistry.getRegistry("valerie");
	 Object o = rmireg.lookup("edu.brown.cs.ivy.mint.registry");
	 if (o != null && !nohost) {
	    p.setProperty("edu.brown.cs.ivy.mint.registryhost","valerie.cs.brown.edu");
	  }
       }
      catch (java.rmi.UnknownHostException e) {
	 System.err.println("IVYSETUP: Mint registry host not used");
	 nohost = true;
       }
      catch (Exception e) {
	 System.err.println("IVYSETUP: Mint registry host not used");
	 System.err.println("ERROR: " + e);
	 e.printStackTrace();
	 nohost = true;
       }
    }

   if (nohost) {
      p.setProperty("edu.brown.cs.ivy.mint.registryhost","localhost");
    }

   p.setProperty("BROWN_IVY_IVY",dir);
   p.setProperty("edu.brown.cs.ivy.IVY",dir);

   try {
      FileOutputStream os = new FileOutputStream(pf);
      p.storeToXML(os,"SETUP on " + new Date().toString());
      os.close();
    }
   catch (IOException e) {
      System.err.println("IVYSETUP: Problem writing property file: " + e);
      System.exit(1);
    }

   if (nohost) {
      File fv = new File(ivv,"Force");
      try {
	 FileWriter os = new FileWriter(fv);
	 os.write("Local only\n");
	 os.close();
       }
      catch (IOException e) { }

      System.err.println("IVYSETUP: Setup working for local environment only");
    }

   System.err.println("IVYSETUP: Setup complete");
}



/********************************************************************************/
/*										*/
/*	Routine for setting up ivy environment internally			*/
/*										*/
/********************************************************************************/

public static void setup()
{
   File ivv = new File(System.getProperty("user.home"),".ivy");

   if (!setup(ivv)) {
      if (findIvyDirectory() == null) return;

      main(new String [] { "-local" });
      if (setup(ivv)) return;

      System.err.println("IVY: setup file ~/.ivy/Props missing, unreadable or incomplete");
      System.err.println("IVY: try running IvySetup");
      System.exit(1);
    }
}




public static boolean setup(File ivv)
{
   File force = new File(ivv,"Force");
   String check = IvyFile.expandName("$(IVY)");
   if (!force.exists() && check.length() > 0) {
      return true;
    }
   File df = new File(ivv,"Props");
   if (!df.exists()) return false;

   try {
      Properties p = new Properties();
      FileInputStream fis = new FileInputStream(df);
      p.loadFromXML(fis);
      for (Object opn : p.keySet()) {
	 String pn = (String) opn;
	 String pv = p.getProperty(pn);
	 System.setProperty(pn,pv);
       }
      fis.close();
    }
   catch (IOException e) {
      return false;
    }

   check = IvyFile.expandName("$(IVY)");
   if (check.length() == 0) {
      return false;
    }

   return true;
}



/********************************************************************************/
/*										*/
/*	Find Ivy Directory							*/
/*										*/
/********************************************************************************/

private static String findIvyDirectory()
{
   String s = System.getProperty("java.class.path");
   if (s == null) return null;
   StringTokenizer tok = new StringTokenizer(s,File.pathSeparator);
   while (tok.hasMoreTokens()) {
      String pe = tok.nextToken().trim();
      if (pe.endsWith("ivy.jar") || pe.endsWith("ivyfull.jar")) {
	 File f = new File(pe);
	 File f1 = f.getParentFile();
	 if (f1 == null) f1 = new File(System.getProperty("user.dir"));
	 if (f1.getName().equals("lib")) f1 = f1.getParentFile();
	 return f1.getPath();
       }
      else if (pe.endsWith(".jar")) continue;
      else if (pe.endsWith("java")) {
	 File f1 = new File(pe);
	 File f2 = new File(f1,"edu");
	 File f3 = new File(f2,"brown");
	 File f4 = new File(f3,"cs");
	 File f5 = new File(f4,"ivy");
	 File f6 = new File(f5,"exec");
	 File f7 = new File(f6,"IvySetup.class");
	 if (f7.exists()) {
	    File f8 = f1.getParentFile();
	    return f8.getPath();
	  }
       }
    }
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      Hide the constructor                                                    */
/*                                                                              */
/********************************************************************************/

private IvySetup() { }


}	// end of class IvySetup



/* end of IvySetup.java */

									















































































