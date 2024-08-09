/********************************************************************************/
/*										*/
/*		IvySlf4jProvider.java						*/
/*										*/
/*	Provider to direct SLF4J output through IvyLog				*/
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

import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.file.IvyLog.LogLevel;

public class IvySlf4jProvider implements SLF4JServiceProvider
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ILoggerFactory loggerFactory;
private IMarkerFactory markerFactory;
private NOPMDCAdapter mdcAdapter;

public static String REQUESTED_API_VERSION = "2.0.99"; // !final



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public IvySlf4jProvider()
{
   super();
}


@Override
public void initialize()
{
   loggerFactory = new IvySlf4jLoggerFactory();
   markerFactory = new BasicMarkerFactory();
   mdcAdapter = new NOPMDCAdapter();
}


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public ILoggerFactory getLoggerFactory()
{
   return loggerFactory;
}

@Override
public IMarkerFactory getMarkerFactory()
{
   return markerFactory;
}

@Override
public NOPMDCAdapter getMDCAdapter()
{
   return mdcAdapter;
}

@Override
public String getRequestedApiVersion()
{
   return REQUESTED_API_VERSION;
}



/********************************************************************************/
/*										*/
/*	Logger factory for IvySlf4j						*/
/*										*/
/********************************************************************************/

private static class IvySlf4jLoggerFactory implements ILoggerFactory {

   @Override public Logger getLogger(String name) {
      try {
	 return new IvySlf4jAdapter(name);
       }
      catch (Throwable t) {
	 return null;
       }

}	// end of inner class IvySlf4jLoggerFactory


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
	    return IvyLog.isDoLogging(LogLevel.DEBUG);
	 case ERROR :
	    return IvyLog.isDoLogging(LogLevel.ERROR);
	 case INFO :
	    return IvyLog.isDoLogging(LogLevel.INFO);
	 case TRACE :
	    return IvyLog.isTracing() && IvyLog.isDoLogging(LogLevel.DEBUG);
	 case WARN :
	    return IvyLog.isDoLogging(LogLevel.WARNING);
       }
      return false;
    }


   @Override public void trace(String msg) {
      if (IvyLog.isTracing()) doLog(LogLevel.DEBUG,msg,null);
    }
   @Override public void trace(String fmt,Object arg) {
      if (IvyLog.isTracing()) doLog(LogLevel.DEBUG,fmt,null);
    }
   @Override public void trace(String fmt,Object arg1,Object arg2) {
      if (IvyLog.isTracing()) doLog(LogLevel.DEBUG,fmt,null,arg1,arg2);
    }
   @Override public void trace(String fmt,Object... args) {
      if (IvyLog.isTracing()) doLog(LogLevel.DEBUG,fmt,null, args);
    }
   @Override public void trace(String msg,Throwable t) {
      if (IvyLog.isTracing()) doLog(LogLevel.DEBUG,msg,t);
    }
   @Override public void trace(Marker mkr,String msg) {
      if (IvyLog.isTracing()) doLog(LogLevel.DEBUG,mkr,msg,null);
    }
   @Override public void trace(Marker mkr,String fmt,Object arg) {
      if (IvyLog.isTracing()) doLog(LogLevel.DEBUG,mkr,fmt,null,arg);
    }
   @Override public void trace(Marker mkr,String fmt,Object arg1,Object arg2) {
      if (IvyLog.isTracing()) doLog(LogLevel.DEBUG,mkr,fmt,null,arg1,arg2);
    }
   @Override public void trace(Marker mkr,String fmt,Object... args) {
      if (IvyLog.isTracing()) doLog(LogLevel.DEBUG,mkr,fmt,null,args);
    }
   @Override public void trace(Marker mkr,String msg,Throwable t) {
      if (IvyLog.isTracing()) doLog(LogLevel.DEBUG,mkr,msg,t);
    }
   @Override public boolean isTraceEnabled() {
      return IvyLog.isTracing() && IvyLog.isDoLogging(LogLevel.DEBUG);
    }
   @Override public boolean isTraceEnabled(Marker mkr) {
      return IvyLog.isTracing() && IvyLog.isDoLogging(LogLevel.DEBUG);
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
      return IvyLog.isDoLogging(LogLevel.DEBUG);
    }
   @Override public boolean isDebugEnabled(Marker mkr) {
      return IvyLog.isDoLogging(LogLevel.DEBUG);
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
      return IvyLog.isDoLogging(LogLevel.INFO);
    }
   @Override public boolean isInfoEnabled(Marker mkr) {
      return IvyLog.isDoLogging(LogLevel.INFO);
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
      return IvyLog.isDoLogging(LogLevel.WARNING);
    }
   @Override public boolean isWarnEnabled(Marker mkr) {
      return IvyLog.isDoLogging(LogLevel.WARNING);
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
      return IvyLog.isDoLogging(LogLevel.ERROR);
    }
   @Override public boolean isErrorEnabled(Marker mkr) {
      return IvyLog.isDoLogging(LogLevel.ERROR);
    }

   private void doLog(LogLevel lvl,String msg,Throwable t) {
      if (!IvyLog.isDoLogging(lvl)) return;
      IvyLog.outsideLog(lvl,logger_name,0,msg,t);
    }
   private void doLog(LogLevel lvl,String ftm,Throwable t,Object ... args) {
      if (!IvyLog.isDoLogging(lvl)) return;
      String msg = String.format(ftm,args);
      IvyLog.outsideLog(lvl,logger_name,0,msg,null);
    }
   private void doLog(LogLevel lvl,Marker mrk,String msg,Throwable t) {
      if (!IvyLog.isDoLogging(lvl)) return;
      if (mrk == null) doLog(lvl,msg,t);
      else {
	 if (msg == null) msg = mrk.toString();
	 else msg += mrk.toString();
       }
      IvyLog.outsideLog(lvl,logger_name,0,msg,t);
    }
   private void doLog(LogLevel lvl,Marker mrk,String fmt,Throwable t ,Object ... args) {
      if (!IvyLog.isDoLogging(lvl)) return;
      String msg = null;
      msg = String.format(fmt,args);
      if (mrk == null) doLog(lvl,msg,t);
      else {
	 if (msg == null) msg = mrk.toString();
	 else msg += mrk.toString();
       }
      IvyLog.outsideLog(lvl,logger_name,0,msg,t);
    }

}	// end of inner class Slf4jAdapter


}	// end of inner class IvySlf4jLoggerFactory



}	// end of class IvySlf4jProvider




/* end of IvySlf4jProvider.java */

