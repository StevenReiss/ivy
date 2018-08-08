/********************************************************************************/
/*										*/
/*		IvyExecQuery.java						*/
/*										*/
/*	Utility methods for getting informatino about the current process	*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/exec/IvyExecQuery.java,v 1.15 2018/08/08 18:47:24 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyExecQuery.java,v $
 * Revision 1.15  2018/08/08 18:47:24  spr
 * Handle java10 base path.
 *
 * Revision 1.14  2018/08/02 15:09:37  spr
 * Fix imports
 *
 * Revision 1.13  2017/05/17 14:13:36  spr
 * Add call to get java binary name.
 *
 * Revision 1.12  2015/03/31 02:19:04  spr
 * Formatting cleanup
 *
 * Revision 1.11  2015/02/14 18:44:50  spr
 * Handle long command lines correctly
 *
 * Revision 1.10  2011-05-27 19:32:35  spr
 * Change copyrights.
 *
 * Revision 1.9  2010-02-12 00:32:11  spr
 * Fix spacing and error messages.
 *
 * Revision 1.8  2009-10-02 00:18:16  spr
 * Import clean up.
 *
 * Revision 1.7  2009-09-17 01:55:24  spr
 * Use jps or equivalent to find processes; add setup code for windows, etc.
 *
 * Revision 1.6  2009-06-04 18:49:37  spr
 * Add ivyJava call and remove getenv.
 *
 * Revision 1.5  2008-06-11 01:46:14  spr
 * Try to close things better.
 *
 * Revision 1.4  2008-06-02 22:16:52  spr
 * Handle getargs using ps.
 *
 * Revision 1.3  2008-03-14 12:25:31  spr
 * Code cleanup.
 *
 * Revision 1.2  2007-08-10 02:10:20  spr
 * Use conventional methods to find process id, etc.
 *
 * Revision 1.1  2007-05-04 02:01:56  spr
 * Add native call to get command line arguments.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.exec;

import edu.brown.cs.ivy.file.IvyFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class IvyExecQuery
{



/********************************************************************************/
/*										*/
/*	Initializers								*/
/*										*/
/********************************************************************************/



/********************************************************************************/
/*										*/
/*	Native methods								*/
/*										*/
/********************************************************************************/

public static String [] getCommandLine()
{
   List<String> cmds = new ArrayList<String>();

   try {
      File f = new File("/proc/self/cmdline");
      boolean fnd = false;
      if (f.exists()) {
	 FileReader fr = new FileReader(f);
	 char [] buf = new char[100000];
	 int fsz = buf.length;
	 int off = 0;
	 for ( ; ; ) {
	    int rsz = fr.read(buf,off,fsz-off);
	    if (rsz < 0) break;
	    off += rsz;
	  }
	 int sz = off;
	 fr.close();
	 if (sz != 0 && sz != 4096) {
	    int st = 0;
	    for (int i = 1; i < sz; ++i) {
	       if (buf[i] == 0) {
		  String s = new String(buf,st,i-st);
		  cmds.add(s);
		  st = i+1;
		}
	     }
	    if (sz > st) {
	       String s = new String(buf,st,sz-st);
	       cmds.add(s);
	     }
	    fnd = true;
	  }
       }
      if (!fnd) {
	 tryUsingJps(cmds);
	 if (cmds.isEmpty()) tryUsingPs(cmds);
       }
    }
   catch (Throwable t) {
      System.err.println("IVY: EXEC: Problem getting command line: " + t);
    }

   String [] r = new String[cmds.size()];
   r = cmds.toArray(r);

   return r;
}



public static String getProcessId()
{
   String mpid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
   if (mpid != null) {
      int idx = mpid.indexOf("@");
      if (idx >= 0) return mpid.substring(0,idx);
    }

   try {
      File f = new File("/proc/self/stat");
      if (f.exists()) {
	 FileReader fr = new FileReader(f);
	 int pid = 0;
	 for ( ; ; ) {
	    int c = fr.read();
	    if (c < '0' || c > '9') break;
	    pid = pid * 10 + c - '0';
	  }
	 fr.close();
	 return Integer.toString(pid);
       }
    }
   catch (Throwable t) {
      System.err.println("IVY: Problem opening /proc/self: " + t);
    }

   try {
      // TODO: Doesn't work on windows
      IvyExec ex = new IvyExec("perl -e 'print getppid() . \"\n\";'",IvyExec.READ_OUTPUT);
      InputStreamReader isr = new InputStreamReader(ex.getInputStream());
      int pid = 0;
      for ( ; ; ) {
	 int c = isr.read();
	 if (c < '0' || c > '9') break;
	 pid = pid * 10 + c - '0';
       }
      isr.close();
      ex.destroy();
      return Integer.toString(pid);
    }
   catch (Throwable t) {
      System.err.println("IVY: Problem executing perl: " + t);
    }

   return null;
}



public static String getHostName()
{
   String h = IvyFile.expandName("$(HOST)");
   if (h != null) return h;

   try {
      InetAddress lh = InetAddress.getLocalHost();
      h = lh.getCanonicalHostName();
      if (h != null) return h;
    }
   catch (IOException e ) {
      System.err.println("IVY: Problem getting host name: " + e);
    }

   return "localhost";
}



public static String computeHostName()
{
   try {
      InetAddress lh = InetAddress.getLocalHost();
      String h = lh.getCanonicalHostName();
      if (h != null) return h;
    }
   catch (IOException e ) {
      System.err.println("IVY: Problem getting host name: " + e);
    }

   return "localhost";
}



public static String getJavaPath()
{
   String jpath = System.getProperty("java.home");
   File jhome = new File(jpath);
   File f1 = new File(jhome,"bin");
   File f2 = new File(f1,"java");
   if (f2.exists() && f2.canExecute()) return f2.getPath();
   
   File f3 = new File(jpath,"jre");
   File f4 = new File(f3,"bin");
   File f5 = new File(f4,"java");
   if (f5.exists() && f5.canExecute()) return f5.getPath();
      
   return "java";
}



/********************************************************************************/
/*										*/
/*	Java queries								*/
/*										*/
/********************************************************************************/

public static List<File> computeBasePath()
{
   return computeBasePath(null);
}


public static List<File> computeBasePath(String javahome)
{
   List<File> rslt = new ArrayList<File>();

   if (javahome == null) javahome = System.getProperty("java.home");

   File f1 = new File(javahome);
   File f2 = new File(f1,"lib");
   File f3 = new File(f1,"jmods");
   
   if (f3.exists()) addToBasePath(f3,rslt);
   else addToBasePath(f2,rslt);
   
   return rslt;
}


private static void addToBasePath(File dir,List<File> rslt)
{
   if (!dir.exists()) ;
   else if (dir.isDirectory()) {
      File [] cnts = dir.listFiles();
      for (int i = 0; i < cnts.length; ++i) addToBasePath(cnts[i],rslt);
    }
   else if (dir.getName().endsWith(".jar")) {
      try {
	 JarFile jf = new JarFile(dir);
	 rslt.add(dir);
	 jf.close();
       }
      catch (IOException e) {
	 System.err.println("S6: CONTEXT: Can't open system jar file " + dir);
       }
    }
   else if (dir.getName().endsWith(".jmod")) {
      try {
         ZipFile zf = new ZipFile(dir);
         rslt.add(dir);
         zf.close();
       }
      catch (IOException e) {
	 System.err.println("S6: CONTEXT: Can't open system jmod file " + dir);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Local routine to use the ps command when direct access fails		*/
/*										*/
/********************************************************************************/

private static void tryUsingJps(List<String> args) throws IOException
{
   String pid = getProcessId();
   if (pid == null) return;

   IvyExec ex = new IvyExec("jps -ml",IvyExec.READ_OUTPUT);
   InputStreamReader isr = new InputStreamReader(ex.getInputStream());
   BufferedReader br = new BufferedReader(isr);
   for ( ; ; ) {
      String ln = br.readLine();
      if (ln == null) return;
      StringTokenizer tok = new StringTokenizer(ln);
      if (!tok.hasMoreTokens()) continue;
      String tpid = tok.nextToken();
      if (tpid.equals(pid)) {
	 args.add("java");              // implicit 'java' command
	 while (tok.hasMoreTokens()) args.add(tok.nextToken());
	 break;
       }
    }
}



private static void tryUsingPs(List<String> args) throws IOException
{
   String pid = getProcessId();
   if (pid == null) return;

   IvyExec ex = new IvyExec("ps -o command -www " + pid,IvyExec.READ_OUTPUT);
   InputStreamReader isr = new InputStreamReader(ex.getInputStream());
   BufferedReader br = new BufferedReader(isr);
   String ln = br.readLine();
   if (ln == null) return;
   ln = br.readLine();
   if (ln == null) return;

   StringTokenizer tok = new StringTokenizer(ln);
   while (tok.hasMoreTokens()) args.add(tok.nextToken());
}



}	// end of class IvyExecQuery




/* end of IvyExecQuery.java */
