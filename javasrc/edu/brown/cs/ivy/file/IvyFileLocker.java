/********************************************************************************/
/*                                                                              */
/*              IvyFileLocker.java                                              */
/*                                                                              */
/*      Provide file locks for cross-process locking                            */
/*                                                                              */
/********************************************************************************/
/*	Copyright 2015 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2015 Brown University, Providence, RI.				 *
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;

public final class IvyFileLocker
{


/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private FileOutputStream lock_file;
private FileLock file_lock;
private File lock_name;
   


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public IvyFileLocker(String base)
{
   this(new File(base));
}



public IvyFileLocker(File f)
{
   lock_file = null;
   file_lock = null;
   
   if (f.isDirectory()) f = new File(f,".lock");
   else if (!f.getName().endsWith(".lock")) f = new File(f.getPath() + ".lock");
   
   lock_name = f;
   f.setWritable(true,false);
   
   try {
      lock_file = new FileOutputStream(f);
      f.setWritable(true,false);
    }
   catch (IOException e) {
      IvyLog.logE("FILE","Lock file " + f + " couldn't be opened");
    }
}



/********************************************************************************/
/*                                                                              */
/*      Locking methods                                                         */
/*                                                                              */
/********************************************************************************/

public boolean lock()
{
   if (lock_file == null) return false;
   if (file_lock != null) return true;		// assumes only one lock per process
   
   for (int i = 0; i < 256; ++i) {
      try {
         file_lock = lock_file.getChannel().lock();
         return true;
       }
      catch (FileLockInterruptionException e) { }
      catch (ClosedChannelException e) {
         IvyLog.logE("FILE","Lock file closed: " + lock_name);  
         lock_file = null;
         return false;
       }
      catch (IOException e) {
         IvyLog.logE("FILE","File lock failed for " + lock_name,e);
       }
    }
   
   return false;
}


public boolean tryLock()
{
   if (lock_file == null) return false;
   if (file_lock != null) return true;		// assumes only one lock per process
   
   try {
      file_lock = lock_file.getChannel().tryLock();
      if (file_lock != null) return true;
    }
   catch (IOException e) {
      IvyLog.logE("FILE","File try lock failed for " + lock_name,e);
    }
   
   return false;
}




public void unlock()
{
   if (file_lock == null) return;
   
   try {
      file_lock.release();
    }
   catch (IOException e) {
      IvyLog.logE("FILE","File unlock failed",e);
    }
   
   file_lock = null;
}



}       // end of class IvyFileLocker




/* end of IvyFileLocker.java */
