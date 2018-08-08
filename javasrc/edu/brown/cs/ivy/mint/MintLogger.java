/********************************************************************************/
/*										*/
/*		MintLogger.java 						*/
/*										*/
/*	Class for logging information from mint 				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Redistribution and use in source and binary forms, with or without           *
 *  modification, are permitted provided that the following conditions are met:  *
 *                                                                               *
 *  + Redistributions of source code must retain the above copyright notice,     *
 *      this list of conditions and the following disclaimer.                    *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *      this list of conditions and the following disclaimer in the              *
 *      documentation and/or other materials provided with the distribution.     *
 *  + Neither the name of the Brown University nor the names of its              *
 *      contributors may be used to endorse or promote products derived from     *
 *      this software without specific prior written permission.                 *
 *                                                                               *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   *
 *  POSSIBILITY OF SUCH DAMAGE.                                                  *
 *                                                                               *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/MintLogger.java,v 1.5 2018/08/02 15:10:26 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintLogger.java,v $
 * Revision 1.5  2018/08/02 15:10:26  spr
 * Fix imports.
 *
 * Revision 1.4  2013/11/15 02:38:56  spr
 * Fix imports.
 *
 * Revision 1.3  2011-05-27 19:32:42  spr
 * Change copyrights.
 *
 * Revision 1.2  2010-10-19 22:00:58  spr
 * Add trace back on xml failures.
 *
 * Revision 1.1  2010-08-20 20:58:24  spr
 * Add logging and options for port numbers
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint;


import edu.brown.cs.ivy.exec.IvyExecQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;



public class MintLogger implements MintConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private PrintStream	log_output;
private String		user_pid;

private static MintLogger the_logger = null;



/********************************************************************************/
/*										*/
/*	Static methods								*/
/*										*/
/********************************************************************************/

public static void log(String msg)
{
   if (the_logger == null) {
      the_logger = new MintLogger();
    }

   the_logger.logMessage(msg);
}



public static void log(String msg,Throwable t)
{
   if (the_logger == null) {
      the_logger = new MintLogger();
    }

   the_logger.logMessage(msg,t);
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private MintLogger()
{
   log_output = null;
   user_pid = IvyExecQuery.getProcessId();
   if (user_pid == null) user_pid = "???";

   File f = new File(System.getProperty("user.home"));
   File logf = new File(f,"mint_log.log");
   try {
      FileOutputStream fos = new FileOutputStream(logf,true);
      log_output = new PrintStream(fos,true);
    }
   catch (IOException e) { }
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

private void logMessage(String msg)
{
   if (log_output != null) log_output.println(user_pid + ": " + msg);

   System.err.println("MINT: " + msg);
}




private void logMessage(String msg,Throwable t)
{
   if (log_output != null) log_output.println(user_pid + ": " + msg);

   System.err.println("MINT: " + msg);

   if (t != null) {
      if (log_output != null) t.printStackTrace(log_output);
      t.printStackTrace();
    }
}




}	// end of class MintLogger




/* end of MintLogger.java */
