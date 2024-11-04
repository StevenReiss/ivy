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


package edu.brown.cs.ivy.exec;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.file.IvyLog;

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
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private static final int COMMAND_LINE_SIZE = 1000000;
private static final int BUFFER_SIZE = 4096;


/********************************************************************************/
/*										*/
/*	Native methods								*/
/*										*/
/********************************************************************************/

public static String [] getCommandLine()
{
   List<String> cmds = new ArrayList<>();

   try {
      File f = new File("/proc/self/cmdline");
      boolean fnd = false;
      if (f.exists()) {
	 FileReader fr = new FileReader(f);
	 try {
	    char [] buf = new char[COMMAND_LINE_SIZE];
	    int fsz = buf.length;
	    int off = 0;
	    for ( ; ; ) {
	       int rsz = fr.read(buf,off,fsz-off);
	       if (rsz < 0) break;
	       off += rsz;
	     }
	    int sz = off;
	    if (sz != 0 && sz != BUFFER_SIZE) {
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
	 finally {
	    fr.close();
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
	 int pid = 0;
	 FileReader fr = new FileReader(f);
	 try {
	    for ( ; ; ) {
	       int c = fr.read();
	       if (c < '0' || c > '9') break;
	       pid = pid * 10 + c - '0';
	     }
	  }
	 finally {
	    fr.close();
	  }
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



public static Integer getProcessNumber()
{
   String s = getProcessId();
   if (s == null) return null;
   int i = Integer.parseInt(s);
   return i;
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
   catch (IOException e) {
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
   catch (IOException e) {
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
   if (!dir.exists()) { }
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
	 IvyLog.logE("IVY","Can't open system jar file " + dir);
       }
    }
   else if (dir.getName().endsWith(".jmod")) {
      try {
	 ZipFile zf = new ZipFile(dir);
	 rslt.add(dir);
	 zf.close();
       }
      catch (IOException e) {
	 IvyLog.logE("IVY","Can't open system jmod file " + dir);
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












































