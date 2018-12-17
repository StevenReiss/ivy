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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/file/IvyFileLocker.java,v 1.1 2017/06/23 20:54:44 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyFileLocker.java,v $
 * Revision 1.1  2017/06/23 20:54:44  spr
 * Add file locking as a public class.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

public class IvyFileLocker
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
   
   try {
      lock_file = new FileOutputStream(f);
    }
   catch (IOException e) {
      System.err.println("IVY: lock file " + f + " couldn't be opened");
    }
}



/********************************************************************************/
/*                                                                              */
/*      Locking methods                                                         */
/*                                                                              */
/********************************************************************************/

public void lock()
{
   if (lock_file == null) return;
   if (file_lock != null) return;		// assumes only one lock per process
   
   for (int i = 0; i < 250; ++i) {
      try {
         file_lock = lock_file.getChannel().lock();
         return;
       }
      catch (IOException e) {
         System.err.println("IVY: File lock failed for " + lock_name + ": " + e);
         e.printStackTrace();
       }
    }
}




public void unlock()
{
   if (file_lock == null) return;
   
   try {
      file_lock.release();
    }
   catch (IOException e) {
      System.err.println("IVY: file unlock failed: " + e);
    }
   
   file_lock = null;
}



}       // end of class IvyFileLocker




/* end of IvyFileLocker.java */
