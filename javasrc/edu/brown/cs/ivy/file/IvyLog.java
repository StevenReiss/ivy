/********************************************************************************/
/*										*/
/*		IvyLog.java							*/
/*										*/
/*	Generic logging package 						*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
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



package edu.brown.cs.ivy.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class IvyLog
{



/********************************************************************************/
/*										*/
/*	Internal types								*/
/*										*/
/********************************************************************************/

public enum LogLevel {
   ERROR, STATISTICS, WARNING, INFO, DEBUG
}

public interface LoggerThread {
   int getLogId();
}


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static LogLevel log_level;
private static boolean	use_stderr;
private static PrintWriter log_writer;
private static String	default_package;
private static boolean	trace_execution;
private static boolean	trace_thread;


static {
   use_stderr = false;
   log_level = LogLevel.INFO;
   log_writer = null;
   trace_execution = false;
   default_package = "IVY";
   trace_thread = false;

   String id = System.getProperty("slf4j.provider");
   if (id == null) {
      System.setProperty("slf4j.provider",
	    "edu.brown.cs.ivy.file.IvySlf4jProvider");
    }
}


/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

public static void setupLogging(String pkg,boolean thrd)
{
   default_package = pkg;
   trace_thread = thrd;
}



public static void setLogLevel(LogLevel lvl)
{
   log_level = lvl;
}


public static void setLogFile(String f)
{
   setLogFile(new File(f));
}



public static void setLogFile(File f)
{
   if (log_writer != null) {
      log_writer.close();
      log_writer = null;
    }

   f = f.getAbsoluteFile();
   try {
      log_writer = new PrintWriter(new FileWriter(f));
    }
   catch (IOException e) {
      IvyLog.logE("Can't open log file " + f);
      log_writer = null;
    }
}


public static void useStdErr(boolean fg)
{
   use_stderr = fg;
}

public static boolean isTracing()		{ return trace_execution; }
public static boolean isDebug()
{
   return log_level == LogLevel.DEBUG;
}

public static boolean isDoLogging(LogLevel lvl)
{
   return lvl.ordinal() > log_level.ordinal();
}

public static void setTracing(boolean fg)	{ trace_execution = fg; }

public static LogLevel getLogLevel()		{ return log_level; }



/********************************************************************************/
/*										*/
/*	Basic logging methods							*/
/*										*/
/********************************************************************************/

public static void logE(String msg,Throwable t)
{
   log(LogLevel.ERROR,default_package,0,msg,t);
}


public static void logE(String msg)
{
   log(LogLevel.ERROR,default_package,0,msg,null);
}


public static void logX(String msg)
{
   Throwable t = new Throwable(msg);
   log(LogLevel.ERROR,default_package,0,msg,t);
}


public static void logW(String msg)
{
   log(LogLevel.WARNING,default_package,0,msg,null);
}


public static void logI(String msg)
{
   log(LogLevel.INFO,default_package,0,msg,null);
}

public static void logI1(String msg)
{
   log(LogLevel.INFO,default_package,1,msg,null);
}


public static void logD(String msg,Throwable t)
{
   log(LogLevel.DEBUG,default_package,0,msg,t);
}


public static void logD(String msg)
{
   log(LogLevel.DEBUG,default_package,0,msg,null);
}


public static void logD1(String msg)
{
   log(LogLevel.DEBUG,default_package,1,msg,null);
}


public static void logS(String msg)
{
   log(LogLevel.STATISTICS,default_package,0,msg,null);
}


public static void logT(Object msg)
{
   if (trace_execution) {
      logD("EXEC: " + msg);
    }
}



/********************************************************************************/
/*										*/
/*	Package-specific logging methods					*/
/*										*/
/********************************************************************************/

public static void logE(String pkg,String msg,Throwable t)
{
   log(LogLevel.ERROR,pkg,0,msg,t);
}


public static void logE(String pkg,String msg)
{
   log(LogLevel.ERROR,pkg,0,msg,null);
}


public static void logX(String pkg,String msg)
{
   Throwable t = new Throwable(msg);
   log(LogLevel.ERROR,pkg,0,msg,t);
}


public static void logW(String pkg,String msg)
{
   log(LogLevel.WARNING,pkg,0,msg,null);
}


public static void logI(String pkg,String msg)
{
   log(LogLevel.INFO,pkg,0,msg,null);
}

public static void logI1(String pkg,String msg)
{
   log(LogLevel.INFO,pkg,1,msg,null);
}


public static void logD(String pkg,String msg,Throwable t)
{
   log(LogLevel.DEBUG,pkg,0,msg,t);
}


public static void logD(String pkg,String msg)
{
   log(LogLevel.DEBUG,pkg,0,msg,null);
}


public static void logD1(String pkg,String msg)
{
   log(LogLevel.DEBUG,pkg,1,msg,null);
}


public static void logS(String pkg,String msg)
{
   log(LogLevel.STATISTICS,pkg,0,msg,null);
}


public static void logT(String pkg,String msg)
{
   if (trace_execution) {
      log(LogLevel.DEBUG,pkg,0,msg,null);
    }
}


public static void outsideLog(LogLevel lvl,String pkg,int indent,String msg,Throwable t)
{
   log(lvl,pkg,indent,msg,t);
}

/********************************************************************************/
/*										*/
/*	Actual logging routines 						*/
/*										*/
/********************************************************************************/

private static void log(LogLevel lvl,String pkg,int indent,String msg,Throwable t)
{
   if (lvl.ordinal() > log_level.ordinal()) return;

   String s = lvl.toString().substring(0,1);
   String pfx = null;
   if (trace_thread) {
      String sth = "*";
      Thread th = Thread.currentThread();
      if (th instanceof LoggerThread) {
	 LoggerThread lt = (LoggerThread) th;
	 sth = Integer.toString(lt.getLogId());
       }
      pfx = pkg + ":" + sth + ":" + s + ": ";
    }
   else {
      pfx = pkg + ":" + s + ": ";
    }

   for (int i = 0; i < indent; ++i) pfx += "   ";
   String tail = "";
   String tpfx = "";
   while (t != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println();
      t.printStackTrace(pw);
      tail += tpfx + sw.toString();
      t = t.getCause();
      tpfx = "\nCaused By: ";
    }

   if (log_writer != null) {
      log_writer.println(pfx + msg + tail);
      log_writer.flush();
    }
   if (use_stderr || log_writer == null) {
      System.err.println(pfx + msg + tail);
      System.err.flush();
    }
}



}	// end of class IvyLog




/* end of IvyLog.java */
