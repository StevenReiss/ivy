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

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

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
	    "edu.brown.cs.ivy.file.IvyLog.IvySlf4jProvider");
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



/********************************************************************************/
/*										*/
/*	SLF4j HANDLING								*/
/*										*/
/********************************************************************************/

private static class IvySlf4jAdapter implements Logger {

   private String logger_name;

   private IvySlf4jAdapter(String name) {
      if (name == null) name = "IVYSLF4J";
      logger_name = name;
    }

   @Override public String getName() {
      return logger_name;
    }

   @Override public boolean isEnabledForLevel(Level level) {
      switch (level) {
         case DEBUG :
            return isDoLogging(LogLevel.DEBUG);
         case ERROR :
            return isDoLogging(LogLevel.ERROR);
         case INFO :
            return isDoLogging(LogLevel.INFO);
         case TRACE :
            return trace_execution && isDoLogging(LogLevel.DEBUG);
         case WARN :
            return isDoLogging(LogLevel.WARNING);
       }
      return false;
    }


   @Override public void trace(String msg) {
      if (trace_execution) doLog(LogLevel.DEBUG,msg,null);
    }
   @Override public void trace(String fmt,Object arg) {
      if (trace_execution) doLog(LogLevel.DEBUG,fmt,null);
    }
   @Override public void trace(String fmt,Object arg1,Object arg2) {
      if (trace_execution) doLog(LogLevel.DEBUG,fmt,null,arg1,arg2);
    }
   @Override public void trace(String fmt,Object... args) {
      if (trace_execution) doLog(LogLevel.DEBUG,fmt,null, args);
    }
   @Override public void trace(String msg,Throwable t) {
      if (trace_execution) doLog(LogLevel.DEBUG,msg,t);
    }
   @Override public void trace(Marker mkr,String msg) {
      if (trace_execution) doLog(LogLevel.DEBUG,mkr,msg,null);
    }
   @Override public void trace(Marker mkr,String fmt,Object arg) {
      if (trace_execution) doLog(LogLevel.DEBUG,mkr,fmt,null,arg);
    }
   @Override public void trace(Marker mkr,String fmt,Object arg1,Object arg2) {
      if (trace_execution) doLog(LogLevel.DEBUG,mkr,fmt,null,arg1,arg2);
    }
   @Override public void trace(Marker mkr,String fmt,Object... args) {
      if (trace_execution) doLog(LogLevel.DEBUG,mkr,fmt,null,args);
    }
   @Override public void trace(Marker mkr,String msg,Throwable t) {
      if (trace_execution) doLog(LogLevel.DEBUG,mkr,msg,t);
    }
   @Override public boolean isTraceEnabled() {
      return trace_execution && isDoLogging(LogLevel.DEBUG);
    }
   @Override public boolean isTraceEnabled(Marker mkr) {
      return trace_execution && isDoLogging(LogLevel.DEBUG);
    }



   @Override public void debug(String msg) {
      doLog(LogLevel.DEBUG,msg,null);
    }
   @Override public void debug(String fmt,Object arg) {
      doLog(LogLevel.DEBUG,fmt,null);
    }
   @Override public void debug(String fmt,Object arg1,Object arg2) {
      doLog(LogLevel.DEBUG,fmt,null,arg1,arg2);
    }
   @Override public void debug(String fmt,Object... args) {
      doLog(LogLevel.DEBUG,fmt,null, args);
    }
   @Override public void debug(String msg,Throwable t) {
      doLog(LogLevel.DEBUG,msg,t);
    }
   @Override public void debug(Marker mkr,String msg) {
      doLog(LogLevel.DEBUG,mkr,msg,null);
    }
   @Override public void debug(Marker mkr,String fmt,Object arg) {
      doLog(LogLevel.DEBUG,mkr,fmt,null,arg);
    }
   @Override public void debug(Marker mkr,String fmt,Object arg1,Object arg2) {
      doLog(LogLevel.DEBUG,mkr,fmt,null,arg1,arg2);
    }
   @Override public void debug(Marker mkr,String fmt,Object... args) {
      doLog(LogLevel.DEBUG,mkr,fmt,null,args);
    }
   @Override public void debug(Marker mkr,String msg,Throwable t) {
      doLog(LogLevel.DEBUG,mkr,msg,t);
    }
   @Override public boolean isDebugEnabled() {
      return isDoLogging(LogLevel.DEBUG);
    }
   @Override public boolean isDebugEnabled(Marker mkr) {
      return isDoLogging(LogLevel.DEBUG);
    }


   @Override public void info(String msg) {
      doLog(LogLevel.INFO,msg,null);
    }
   @Override public void info(String fmt,Object arg) {
      doLog(LogLevel.INFO,fmt,null);
    }
   @Override public void info(String fmt,Object arg1,Object arg2) {
      doLog(LogLevel.INFO,fmt,null,arg1,arg2);
    }
   @Override public void info(String fmt,Object... args) {
      doLog(LogLevel.INFO,fmt,null, args);
    }
   @Override public void info(String msg,Throwable t) {
      doLog(LogLevel.INFO,msg,t);
    }
   @Override public void info(Marker mkr,String msg) {
      doLog(LogLevel.INFO,mkr,msg,null);
    }
   @Override public void info(Marker mkr,String fmt,Object arg) {
      doLog(LogLevel.INFO,mkr,fmt,null,arg);
    }
   @Override public void info(Marker mkr,String fmt,Object arg1,Object arg2) {
      doLog(LogLevel.INFO,mkr,fmt,null,arg1,arg2);
    }
   @Override public void info(Marker mkr,String fmt,Object... args) {
      doLog(LogLevel.INFO,mkr,fmt,null,args);
    }
   @Override public void info(Marker mkr,String msg,Throwable t) {
      doLog(LogLevel.INFO,mkr,msg,t);
    }
   @Override public boolean isInfoEnabled() {
      return isDoLogging(LogLevel.INFO);
    }
   @Override public boolean isInfoEnabled(Marker mkr) {
      return isDoLogging(LogLevel.INFO);
    }


   @Override public void warn(String msg) {
      doLog(LogLevel.WARNING,msg,null);
    }
   @Override public void warn(String fmt,Object arg) {
      doLog(LogLevel.WARNING,fmt,null);
    }
   @Override public void warn(String fmt,Object arg1,Object arg2) {
      doLog(LogLevel.WARNING,fmt,null,arg1,arg2);
    }
   @Override public void warn(String fmt,Object... args) {
      doLog(LogLevel.WARNING,fmt,null, args);
    }
   @Override public void warn(String msg,Throwable t) {
      doLog(LogLevel.WARNING,msg,t);
    }
   @Override public void warn(Marker mkr,String msg) {
      doLog(LogLevel.WARNING,mkr,msg,null);
    }
   @Override public void warn(Marker mkr,String fmt,Object arg) {
      doLog(LogLevel.WARNING,mkr,fmt,null,arg);
    }
   @Override public void warn(Marker mkr,String fmt,Object arg1,Object arg2) {
      doLog(LogLevel.WARNING,mkr,fmt,null,arg1,arg2);
    }
   @Override public void warn(Marker mkr,String fmt,Object... args) {
      doLog(LogLevel.WARNING,mkr,fmt,null,args);
    }
   @Override public void warn(Marker mkr,String msg,Throwable t) {
      doLog(LogLevel.WARNING,mkr,msg,t);
    }
   @Override public boolean isWarnEnabled() {
      return isDoLogging(LogLevel.WARNING);
    }
   @Override public boolean isWarnEnabled(Marker mkr) {
      return isDoLogging(LogLevel.WARNING);
    }


   @Override public void error(String msg) {
      doLog(LogLevel.ERROR,msg,null);
    }
   @Override public void error(String fmt,Object arg) {
      doLog(LogLevel.ERROR,fmt,null);
    }
   @Override public void error(String fmt,Object arg1,Object arg2) {
      doLog(LogLevel.ERROR,fmt,null,arg1,arg2);
    }
   @Override public void error(String fmt,Object... args) {
      doLog(LogLevel.ERROR,fmt,null, args);
    }
   @Override public void error(String msg,Throwable t) {
      doLog(LogLevel.ERROR,msg,t);
    }
   @Override public void error(Marker mkr,String msg) {
      doLog(LogLevel.ERROR,mkr,msg,null);
    }
   @Override public void error(Marker mkr,String fmt,Object arg) {
      doLog(LogLevel.ERROR,mkr,fmt,null,arg);
    }
   @Override public void error(Marker mkr,String fmt,Object arg1,Object arg2) {
      doLog(LogLevel.ERROR,mkr,fmt,null,arg1,arg2);
    }
   @Override public void error(Marker mkr,String fmt,Object... args) {
      doLog(LogLevel.ERROR,mkr,fmt,null,args);
    }
   @Override public void error(Marker mkr,String msg,Throwable t) {
      doLog(LogLevel.ERROR,mkr,msg,t);
    }
   @Override public boolean isErrorEnabled() {
      return isDoLogging(LogLevel.ERROR);
    }
   @Override public boolean isErrorEnabled(Marker mkr) {
      return isDoLogging(LogLevel.ERROR);
    }

   private void doLog(LogLevel lvl,String msg,Throwable t) {
      if (!isDoLogging(lvl)) return;
      IvyLog.log(lvl,logger_name,0,msg,t);
    }
   private void doLog(LogLevel lvl,String ftm,Throwable t,Object ... args) {
      if (!isDoLogging(lvl)) return;
      String msg = String.format(ftm,args);
      IvyLog.log(lvl,logger_name,0,msg,null);
    }
   private void doLog(LogLevel lvl,Marker mrk,String msg,Throwable t) {
      if (!isDoLogging(lvl)) return;
      if (mrk == null) doLog(lvl,msg,t);
      else {
	 if (msg == null) msg = mrk.toString();
	 else msg += mrk.toString();
       }
      IvyLog.log(lvl,logger_name,0,msg,t);
    }
   private void doLog(LogLevel lvl,Marker mrk,String fmt,Throwable t ,Object ... args) {
      if (!isDoLogging(lvl)) return;
      String msg = null;
      msg = String.format(fmt,args);
      if (mrk == null) doLog(lvl,msg,t);
      else {
	 if (msg == null) msg = mrk.toString();
	 else msg += mrk.toString();
       }
      IvyLog.log(lvl,logger_name,0,msg,t);
    }

}	// end of inner class Slf4jAdapter

public static class IvySlf4jProvider implements SLF4JServiceProvider {
   private ILoggerFactory loggerFactory;
   private IMarkerFactory markerFactory;
   private NOPMDCAdapter mdcAdapter;

   public static String REQUESTED_API_VERSION = "2.0.99"; // !final

   public ILoggerFactory getLoggerFactory() {
      return loggerFactory;
    }

   @Override
   public IMarkerFactory getMarkerFactory() {
      return markerFactory;
    }

   @Override
   public NOPMDCAdapter getMDCAdapter() {
      return mdcAdapter;
    }

   @Override
   public String getRequestedApiVersion() {
      return REQUESTED_API_VERSION;
    }

   @Override
   public void initialize() {
      loggerFactory = new IvySlf4jLoggerFactory();
      markerFactory = new BasicMarkerFactory();
      mdcAdapter = new NOPMDCAdapter();
    }

}	// end of inner class IvySlf4jProvider


private static class IvySlf4jLoggerFactory implements ILoggerFactory {

   @Override public Logger getLogger(String name) {
      try {
         return new IvySlf4jAdapter(name);
       }
      catch (Throwable t) {
         return null;
       }
    }

}	// end of inner class IvySlf4jLoggerFactory



}	// end of class IvyLog




/* end of IvyLog.java */
