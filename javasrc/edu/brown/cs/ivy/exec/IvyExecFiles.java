/********************************************************************************/
/*										*/
/*		IvyExecFiles.java						*/
/*										*/
/*	Methods for managing file redirection for execution			*/
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


class IvyExecFiles
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Monitor 	monitor_thread;
private List<FileCopy>	active_copies;

private Object		exec_lock;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

IvyExecFiles()
{
   active_copies = new LinkedList<>();
   exec_lock = new Object();

   new FileCopy(null,null);		// force load
   monitor_thread = new Monitor();
}




/********************************************************************************/
/*										*/
/*	Interaction methods							*/
/*										*/
/********************************************************************************/

void createCopyist(InputStream ist,PrintStream ost)
{
   FileCopy fc = new FileCopy(ist,ost);

   synchronized (exec_lock) {
      if (!monitor_thread.isAlive()) {
	 try {
	    monitor_thread.start();
	  }
	 catch (IllegalThreadStateException e) {
	    System.err.println("IVYEXEC: Monitor thread disappeared");
	    monitor_thread = new Monitor();
	    monitor_thread.start();
	  }
       }
      active_copies.add(fc);
      exec_lock.notifyAll();
    }
}




/********************************************************************************/
/*										*/
/*	Class to hold copy information						*/
/*										*/
/********************************************************************************/

private static class FileCopy {

   private InputStream input_reader;
   private OutputStream output_writer;
   private byte [] copy_buffer;

   FileCopy(InputStream ist,OutputStream ost) {
      input_reader = ist;
      output_writer = ost;
      copy_buffer = new byte [1024];
    }

   int process() {
      int tot = 0;
      try {
	 for (int i = 0; i < 10 && input_reader.available() > 0; ++i) {
	    int ln = input_reader.read(copy_buffer);
	    if (ln < 0) {
	       close();
	       return -1;
	     }
	    tot += ln;
	    if (ln > 0 && output_writer != null) output_writer.write(copy_buffer,0,ln);
	  }
       }
      catch (InterruptedIOException e) { }
      catch (IOException e) {
	 close();
	 return -1;
       }

      return tot;
    }

   private void close() {
      try {
	 input_reader.close();
       }
      catch (IOException e) { }
    }

}	// end of subclass FileCopy




/********************************************************************************/
/*										*/
/*	Class to hold monitoring thread 					*/
/*										*/
/********************************************************************************/

private class Monitor extends Thread {

   private long time_out;

   Monitor() {
      super("IvyExecMonitor");
      setDaemon(true);
      time_out = 0;
    }

   @Override public void run() {
      for ( ; ; ) {
	 int tot = 0;
	 synchronized (exec_lock) {
	    for (Iterator<FileCopy> it = active_copies.iterator(); it.hasNext(); ) {
	       FileCopy fc = it.next();
	       int ct = fc.process();
	       if (ct < 0) it.remove();
	       else tot += ct;
	     }

	    long delay;
	    if (active_copies.isEmpty()) {
	       delay = 0;
	     }
	    else if (tot > 0)  {
	       delay = -1;
	       time_out = 0;
	     }
	    else {
	       time_out += 100;
	       if (time_out > 1000) time_out = 1000;
	       delay = time_out;
	     }
	    if (delay >= 0) {
	       try {
		  exec_lock.wait(delay);
		}
	       catch (InterruptedException e) { }
	     }
	  }
       }
    }

}	// end of subclass Monitor




}	// end of class IvyExecFiles




/* end of IvyExecFiles.java */
