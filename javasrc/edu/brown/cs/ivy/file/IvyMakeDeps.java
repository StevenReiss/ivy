/********************************************************************************/
/*                                                                              */
/*              IvyMakeDeps.java                                                */
/*                                                                              */
/*      Generate a makefile of java dependencies                                */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.ivy.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import edu.brown.cs.ivy.exec.IvyExec;

public class IvyMakeDeps
{



/********************************************************************************/
/*                                                                              */
/*      Main program                                                            */
/*                                                                              */
/********************************************************************************/

public static void main(String [] args)
{
   IvyMakeDeps md = new IvyMakeDeps(args);
   md.process();
   System.exit(0);
}

/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private File    bin_directory;
private String  package_name;
private String  class_path;
private Map<String,Set<String>> file_depends;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private IvyMakeDeps(String [] args)
{
   bin_directory = null;
   class_path = null;
   package_name = null;
   file_depends = new HashMap<>();
   
   scanArgs(args);
   
   if (bin_directory == null) {
      bin_directory = new File(System.getProperty("user.dir"));
    }
   
   if (!bin_directory.isDirectory()) {
      badArgs();
    }
   
   if (class_path == null) class_path = ".";
   if (package_name == null) {
      findPackageName();
      if (package_name == null) badArgs();
    }
}



/********************************************************************************/
/*                                                                              */
/*      Argument scanning                                                       */
/*                                                                              */
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
         if (args[i].startsWith("-c") && i+1 < args.length) {           // -cp <classpath>
            String cp = args[++i];
            if (class_path == null) class_path = cp;
            else class_path = class_path + File.pathSeparator + cp;
          }
         else if (args[i].startsWith("-d") && i+1 < args.length) {      // -d Directory
            bin_directory = new File(args[++i]);
          }
         else if (args[i].startsWith("-p") && i+i < args.length) {      // -p package
            package_name = args[++i];
          }
         else badArgs();
       }
      else { 
         badArgs();
       }
    }
}


private void badArgs() 
{
   System.err.println("makedeps -cp <classpath> -d <directory> -p <package");
   System.exit(1);
}


private void findPackageName()
{
   File usecf = null;
   for (File cf : bin_directory.listFiles()) {
      if (cf.getName().endsWith(".class") &&
            !cf.getName().contains("$")) {
         usecf = cf;
       }
    }
   if (usecf == null) badArgs();
   
   try {
      String cmd = "javap " + usecf.getPath();
      IvyExec ex = new IvyExec(cmd,IvyExec.READ_OUTPUT);
      InputStream ins = ex.getInputStream();
      String rslt = IvyFile.loadFile(ins);
      StringTokenizer tok = new StringTokenizer(rslt,"\n");
      while (tok.hasMoreTokens()) {
         String ln = tok.nextToken();
         if (ln.contains("Compiled from")) continue;
         StringTokenizer ntok = new StringTokenizer(ln);
         while (ntok.hasMoreTokens()) {
            String pt = ntok.nextToken();
            switch (pt) {
               case "public" :
               case "private" :
               case "protected" :
               case "class" :
               case "interface" :
               case "enum" :
                  break;
               default :
                  int idx1 = pt.lastIndexOf(".");
                  package_name = pt.substring(0,idx1);
                  return;
             }
          }
       }
    }
   catch (IOException e) { }
}



/********************************************************************************/
/*                                                                              */
/*      Processingh methods                                                     */
/*                                                                              */
/********************************************************************************/

private void process()
{
   
   String cmd = "jdeps -verbose:class -filter:none";
   cmd += " -package " + package_name;
   cmd += " -cp " + class_path;
   cmd += " " + bin_directory.getPath();
   
   String rslt;
   try {
      IvyExec ex = new IvyExec(cmd,IvyExec.READ_OUTPUT);
      InputStream ins = ex.getInputStream();
      rslt = IvyFile.loadFile(ins);
    }
   catch (IOException e) {
      badArgs();
      return;
    }
   
   StringTokenizer ltok = new StringTokenizer(rslt,"\n");
   while (ltok.hasMoreTokens()) {
      String ln = ltok.nextToken();
      scanLine(ln);
    }
   
   outputDependencies();
}


private void scanLine(String ln)
{
   StringTokenizer tok = new StringTokenizer(ln);
   if (tok.countTokens() < 3) return;
   String lhs = tok.nextToken();
   tok.nextToken();
   String rhs = tok.nextToken();
   if (!lhs.contains(package_name)) return;
   if (!rhs.contains(package_name)) return;
   String lhcls = lhs;
   int idx = lhcls.indexOf("$");
   if (idx > 0) lhcls = lhcls.substring(0,idx);
   String rhcls = rhs;
   idx = rhcls.indexOf("$");
   if (idx > 0) rhcls = rhcls.substring(0,idx);
   if (lhcls.equals(rhcls)) return;
   Set<String> deps = file_depends.get(lhcls);
   if (deps == null) {
      deps = new HashSet<>();
      file_depends.put(lhcls,deps);
    }
   deps.add(rhcls);
}



private void outputDependencies()
{
   File f = new File(bin_directory,"javadep");
   try (PrintWriter pw = new PrintWriter(f)) {
      for (String key : file_depends.keySet()) {
         String keyname = key;
         int idx = keyname.lastIndexOf(".");
         if (idx > 0) keyname = keyname.substring(idx+1);
         String lhs = keyname + ".class";
         StringBuffer buf = new StringBuffer();
         buf.append(keyname + ".java");
         for (String rhs : file_depends.get(key)) {
            int idx1 = rhs.lastIndexOf(".");
            if (idx1 > 0) rhs = rhs.substring(idx+1);
            buf.append(" ");
            buf.append(rhs + ".java");
          }
         pw.println(lhs + ":\t" + buf.toString());
       }
    }
   catch (IOException e) {
      badArgs();
    }
}



}       // end of class IvyMakeDeps




/* end of IvyMakeDeps.java */

