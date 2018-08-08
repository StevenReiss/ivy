/********************************************************************************/
/*										*/
/*		IvyExec.java							*/
/*										*/
/*	Utility methods for executing proecesses				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/exec/IvyExec.java,v 1.31 2018/08/02 15:09:36 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyExec.java,v $
 * Revision 1.31  2018/08/02 15:09:36  spr
 * Fix imports
 *
 * Revision 1.30  2017/09/08 18:23:24  spr
 * Handle running the correct version of java.
 *
 * Revision 1.29  2015/11/20 15:09:12  spr
 * Reformatting.
 *
 * Revision 1.28  2013/09/24 01:06:51  spr
 * Minor fix
 *
 * Revision 1.27  2013-06-11 23:14:28  spr
 * Code cleanup.
 *
 * Revision 1.26  2012-11-04 02:11:18  spr
 * Remove extra print.
 *
 * Revision 1.25  2012-11-04 02:10:39  spr
 * Add debugging
 *
 * Revision 1.24  2012-10-05 00:46:33  spr
 * Make line splitter public.
 *
 * Revision 1.23  2012-02-29 01:53:47  spr
 * Minor changes.
 *
 * Revision 1.22  2011-06-29 20:24:17  spr
 * no change
 *
 * Revision 1.21  2011-06-24 20:15:44  spr
 * Add working directory option to exec.
 *
 * Revision 1.20  2011-05-27 19:32:35  spr
 * Change copyrights.
 *
 * Revision 1.19  2011-04-01 23:07:38  spr
 * Add method taking list.
 *
 * Revision 1.18  2010-06-01 02:07:58  spr
 * Let java run windows commands directly.
 *
 * Revision 1.17  2010-03-30 16:25:27  spr
 * Minor fixes to setup code.
 *
 * Revision 1.16  2010-02-26 21:04:49  spr
 * Update setup and exec to work better with windows and bubbles.
 *
 * Revision 1.15  2009-09-17 01:55:24  spr
 * Use jps or equivalent to find processes; add setup code for windows, etc.
 *
 * Revision 1.14  2009-06-04 18:49:37  spr
 * Add ivyJava call and remove getenv.
 *
 * Revision 1.13  2008-11-12 13:44:58  spr
 * Add single threaded file management facility.
 *
 * Revision 1.12  2008-06-11 01:46:14  spr
 * Try to close things better.
 *
 * Revision 1.11  2008-03-14 12:25:30  spr
 * Code cleanup.
 *
 * Revision 1.10  2006-12-01 03:22:42  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.9  2006-11-09 00:32:51  spr
 * Fix up get command and args list.
 *
 * Revision 1.8  2006/07/10 14:52:13  spr
 * Code cleanup.
 *
 * Revision 1.7  2005/09/19 23:32:00  spr
 * Fixups based on findbugs.
 *
 * Revision 1.6  2005/05/07 22:25:40  spr
 * Updates for java 5.0
 *
 * Revision 1.5  2004/09/20 22:22:05  spr
 * Minor changes to handle windows better.
 *
 * Revision 1.4  2004/05/05 02:28:08  spr
 * Update import lists using eclipse.
 *
 * Revision 1.3  2003/04/24 20:06:04  spr
 * Shorten thread names and add command to them.
 *
 * Revision 1.2  2003/03/29 03:40:52  spr
 * Provide a dialog-based wait mechanism.
 *
 * Revision 1.1.1.1  2003/03/17 17:12:50  spr
 * Initial version of the common code for various Brown projects.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.exec;

import edu.brown.cs.ivy.file.IvyFile;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class IvyExec
{


/********************************************************************************/
/*										*/
/*	Flags									*/
/*										*/
/********************************************************************************/

public final static int ERROR_OUTPUT = 0x1;	// redirect all output to stderr
public final static int IGNORE_OUTPUT = 0x2;	// ignore all output
public final static int PROVIDE_INPUT = 0x10;	// app will provide input
public final static int READ_OUTPUT = 0x20;	// app will read the output
public final static int READ_ERROR = 0x40;	// app will read std error
public final static int USER_PROCESS = 0x80;	// explicitly a user process



/********************************************************************************/
/*										*/
/*	Storage 								*/
/*										*/
/********************************************************************************/

private String exec_command;
private int exec_flags;
private Process exec_process;
private ReaderThread output_thread;
private ReaderThread error_thread;

private static IvyExecFiles exec_poller = new IvyExecFiles();
private static boolean	use_polling = false;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public IvyExec(String cmd) throws IOException
{
   this(cmd,null,null,ERROR_OUTPUT);
}


public IvyExec(String cmd,int flags) throws IOException
{
   this(cmd,null,null,flags);
}



public IvyExec(String cmd,File cwd) throws IOException
{
   this(cmd,null,cwd,ERROR_OUTPUT);
}


public IvyExec(String cmd,File cwd,int flags) throws IOException
{
   this(cmd,null,cwd,flags);
}



public IvyExec(String cmd,String [] env,int flags) throws IOException
{
   this(cmd,env,null,flags);
}



public IvyExec(String [] args,String [] env,int flags) throws IOException
{
   this(args,env,null,flags);
}



public IvyExec(List<String> argl,String [] env,int flags) throws IOException
{
   this(argl,env,null,flags);
}



public IvyExec(String cmd,String [] env,File cwd,int flags) throws IOException
{
   String osnm = System.getProperty("os.name");
   if (osnm.startsWith("Windows")) {
      // cmd = "cmd /c " + cmd;
    }

   exec_command = cmd;

   List<String> argv = tokenize(cmd);
   String [] args = new String [argv.size()];
   args = argv.toArray(args);

   doExec(args,env,cwd,flags);
}



public IvyExec(String [] args,String [] env,File cwd,int flags) throws IOException
{
   StringBuffer buf = new StringBuffer();
   buf.append(args[0]);
   for (int i = 1; i < args.length; ++i) buf.append(" " + args[i]);
   exec_command = buf.toString();

   doExec(args,env,cwd,flags);
}



public IvyExec(List<String> argl,String [] env,File cwd,int flags) throws IOException
{
   String [] args = new String[argl.size()];
   args = argl.toArray(args);

   StringBuffer buf = new StringBuffer();
   buf.append(args[0]);
   for (int i = 1; i < args.length; ++i) buf.append(" " + args[i]);
   exec_command = buf.toString();

   doExec(args,env,cwd,flags);
}






/********************************************************************************/
/*										*/
/*	Execution methods							*/
/*										*/
/********************************************************************************/

private void doExec(String [] args,String [] env,File cwd,int flags) throws IOException
{
   String cnm = "*";
   if (args.length > 0) {
      cnm = args[0];
      int idx = cnm.lastIndexOf(File.separator);
      if (idx > 0) cnm = cnm.substring(idx+1);
    }

   String osnm = System.getProperty("os.name");
   if (osnm.startsWith("Windows")) {
      System.err.println("IVY: EXEC: ");
      for (String s : args) System.err.println("\tARG: " + s);
    }

   exec_flags = flags;
   exec_process = null;
   output_thread = null;
   error_thread = null;
   exec_process = Runtime.getRuntime().exec(args,env,cwd);
   boolean daemon = ((flags & USER_PROCESS) == 0);

   if ((flags & PROVIDE_INPUT) == 0) {
      OutputStream ost = exec_process.getOutputStream();
      ost.close();
    }

   if ((flags & READ_OUTPUT) == 0) {
      InputStream ist = exec_process.getInputStream();
      PrintStream pst;
      if ((flags & IGNORE_OUTPUT) != 0) pst = null;
      else if ((flags & ERROR_OUTPUT) != 0) pst = System.err;
      else pst = System.out;
      if (use_polling) exec_poller.createCopyist(ist,pst);
      else {
	 output_thread = new ReaderThread(ist,pst,cnm);
	 output_thread.setDaemon(daemon);
	 output_thread.start();
       }
    }

   if ((flags & READ_ERROR) == 0) {
      InputStream ist = exec_process.getErrorStream();
      PrintStream pst;
      if ((flags & ERROR_OUTPUT) != 0) pst = System.err;
      else if ((flags & IGNORE_OUTPUT) != 0) pst = null;
      else pst = System.err;
      if (use_polling) exec_poller.createCopyist(ist,pst);
      else {
	 error_thread = new ReaderThread(ist,pst,cnm);
	 error_thread.setDaemon(daemon);
	 error_thread.start();
       }
    }
}





/********************************************************************************/
/*										*/
/*	Process methods 							*/
/*										*/
/********************************************************************************/

public InputStream getErrorStream()
{
   if ((exec_flags & READ_ERROR) == 0) {
      System.err.println("IVYEXEC: Attempt to use committed error stream");
      return null;
    }

   return exec_process.getErrorStream();
}





public InputStream getInputStream()
{
   if ((exec_flags & READ_OUTPUT) == 0) {
      System.err.println("IVYEXEC: Attempt to use committed output stream");
      return null;
    }

   return exec_process.getInputStream();
}





public OutputStream getOutputStream()
{
   if ((exec_flags & PROVIDE_INPUT) == 0) {
      System.err.println("IVYEXEC: Attempt to use committed input stream");
      return null;
    }

   return exec_process.getOutputStream();
}



public int waitFor()
{
   int rslt = 0;

   for ( ; ; ) {
      try {
	 rslt = exec_process.waitFor();
	 handleExit();
	 break;
       }
      catch (InterruptedException e) { }
    }

   return rslt;
}



public int exitValue()
{
   return exec_process.exitValue();
}



public void destroy()
{
   if (isRunning()) exec_process.destroy();

   handleExit();

   try {
      exec_process.getInputStream().close();
    }
   catch (IOException e) { }
   try {
      exec_process.getErrorStream().close();
    }
   catch (IOException e) { }
   try {
      exec_process.getOutputStream().close();
    }
   catch (IOException e) { }

   if (output_thread != null) {
      output_thread.interrupt();
      output_thread = null;
    }

   if (error_thread != null) {
      error_thread.interrupt();
      error_thread = null;
    }
}



public boolean isRunning()
{
   try {
      exec_process.exitValue();
    }
   catch (IllegalThreadStateException e) {
      return true;
    }

   handleExit();

   return false;
}



public String getCommand()			{ return exec_command; }



public static void usePolling(boolean fg)	{ use_polling = fg; }



/********************************************************************************/
/*										*/
/*	Java execution methods							*/
/*										*/
/********************************************************************************/


private static final String DEFS = "-DBROWN_IVY_IVY=$(IVY)";
private static final String DEFS1 = "-Dedu.brown.cs.IVY=$(IVY)";


public static IvyExec ivyJava(String cls,String jargs,String args) throws IOException
{
   return ivyJava(cls,jargs,args,ERROR_OUTPUT);
}



public static IvyExec ivyJava(String cls,String jargs,String args,int flags) throws IOException
{
   String check = IvyFile.expandName("$(IVY)");
   if (check.length() == 0) {
      System.err.println("IVY: IVY is not set up correctly. Please define BROWN_IVY_IVY");
      System.exit(1);
    }

   String defs = IvyFile.expandName(DEFS);
   String defs1 = IvyFile.expandName(DEFS1);

   String libpath = System.getProperty("java.library.path");
   String lp2 = IvyFile.expandName("$(IVY)/lib/$(BROWN_IVY_ARCH)");
   if (libpath == null) libpath = lp2;
   else if (!libpath.contains(lp2)) libpath += File.pathSeparator + lp2;

   String cp = null;
   String fjn = IvyFile.expandName("$(IVY)/ivyfull.jar");
   File f = new File(fjn);
   if (f.exists()) cp = fjn;

   if (cp == null || cp.length() == 0) {
      String jdir = IvyFile.expandName("$(IVY)/java");
      String jidir = IvyFile.expandName("$(IVY)/java/edu/brown/cs/ivy/exec/");
      File jf = new File(jdir);
      File jif = new File(jidir);
      String kdir = IvyFile.expandName("$(IVY)/lib/ivy.jar");
      File kf = new File(kdir);
      if (jf.exists() && jf.isDirectory() && jif.exists()) cp = jdir;
      else if (kf.exists() && kf.canRead()) cp = kdir;
      if (cp != null) {
	 String cp3 = IvyFile.expandName("$(IVY)/lib/jikesbt.jar");
	 File lf = new File(cp3);
	 if (lf.exists() && lf.canRead()) cp += File.pathSeparator + cp3;
       }
    }

   if (cp == null || cp.length() == 0) {
      try {
	 Class.forName(cls);
	 cp = System.getProperty("java.class.path");
       }
      catch (ClassNotFoundException e) {
	 System.err.println("Can't find a class path for " + cls);
       }
    }

   StringBuffer cmd = new StringBuffer();
   cmd.append(IvyExecQuery.getJavaPath());
   cmd.append(" -Xmx1024m ");
   cmd.append("'" + defs + "' ");
   cmd.append("'" + defs1 + "' ");
   cmd.append("'-Djava.library.path=" + libpath + "' ");
   cmd.append("-cp '" + cp + "' ");
   if (jargs != null) cmd.append(jargs + " ");
   cmd.append(cls);
   if (args != null) {
      cmd.append(" " + args);
    }
   System.err.println("IVYEXEC: " + cmd.toString());

   IvyExec ex = new IvyExec(cmd.toString(),flags);

   return ex;
}







/********************************************************************************/
/*										*/
/*	Cleanup routines							*/
/*										*/
/********************************************************************************/

private void handleExit()
{
   if (use_polling) {
      try {
	 exec_process.getInputStream().close();
       }
      catch (IOException e) { }
      try {
	 exec_process.getErrorStream().close();
       }
      catch (IOException e) { }
      try {
	 exec_process.getOutputStream().close();
       }
      catch (IOException e) { }
    }
}




/********************************************************************************/
/*										*/
/*	Methods to display a dialog until the process terminates		*/
/*										*/
/********************************************************************************/

public int dialogWait(JFrame f,String ttl,String msg,boolean cancancel)
{
   String [] opts;
   if (cancancel) opts = new String [] { "Cancel" };
   else opts = new String [] {	};

   JOptionPane op = new JOptionPane(msg,JOptionPane.INFORMATION_MESSAGE,
				       JOptionPane.OK_CANCEL_OPTION,null,opts);

   JDialog d = op.createDialog(f,ttl);
   WaitThread wt = new WaitThread(d);
   wt.start();
   d.setVisible(true);
   Object o = op.getValue();
   if (o != null && o instanceof Integer && ((Integer) o).intValue() == 0) o = null;
   if (cancancel && o != null) wt.interrupt();
   for ( ; ; ) {
      try {
	 if (!wt.isAlive()) break;
	 wt.join();
       }
      catch (InterruptedException e) { }
    }

   return wt.getStatus();
}



private class WaitThread extends Thread {

   private JDialog dialog_box;
   private int result_status;

   WaitThread(JDialog jd) {
      super("Wait on " + jd.getTitle());
      dialog_box = jd;
      result_status = -1;
    }

   @Override public void run() {
      result_status = waitFor();
      dialog_box.setVisible(false);
    }

   int getStatus()				{ return result_status; }

}	// end of subclass WaitThread





/********************************************************************************/
/*										*/
/*	Reader thread methods							*/
/*										*/
/********************************************************************************/

private static class ReaderThread extends Thread {

   private BufferedReader input_reader;
   private PrintStream output_writer;

   ReaderThread(InputStream ist,PrintStream ost,String cmd) {
      super("IvyExecRdr-" + cmd);
      input_reader = new BufferedReader(new InputStreamReader(ist));
      output_writer = ost;
    }

   @Override public void run() {
      try {
	 for ( ; ; ) {
	    String l = input_reader.readLine();
	    if (l == null) break;
	    if (output_writer != null) output_writer.println(l);
	  }
       }
      catch    (IOException e) { return; }
    }

}	// end of subclass ReaderThread



/********************************************************************************/
/*										*/
/*	Tokenize: split command arguments from string to list using quotes	*/
/*										*/
/********************************************************************************/

public static List<String> tokenize(String cmd)
{
   List<String> argv = new ArrayList<String>();

   char quote = 0;
   StringBuffer buf = new StringBuffer();
   for (int i = 0; i < cmd.length(); ++i) {
      char c = cmd.charAt(i);
      if (quote != 0 && c == quote) {
	 quote = 0;
	 continue;
       }
      else if (quote == 0 && (c == '"' || c == '\'')) {
	 quote = c;
	 continue;
       }
      else if (quote == 0 && (c == ' ' || c == '\n')) {
	 if (buf.length() > 0) {
	    argv.add(buf.toString());
	    buf = new StringBuffer();
	  }
       }
      else buf.append(c);
    }
   if (buf.length() > 0) {
      argv.add(buf.toString());
    }

   return argv;
}




}	// end of class IvyExec



/* end of IvyExec.java */
