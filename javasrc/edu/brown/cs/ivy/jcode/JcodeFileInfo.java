/********************************************************************************/
/*                                                                              */
/*              JcodeFileInfo.java                                              */
/*                                                                              */
/*      Information about a source for a class                                  */
/*                                                                              */
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
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




package edu.brown.cs.ivy.jcode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;



class JcodeFileInfo implements JcodeConstants
{

   
/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private File base_file;
private String jar_item;
private byte [] file_data;   



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JcodeFileInfo(File f) 
{
   base_file = f;
   jar_item = null;
   file_data = null;
}


JcodeFileInfo(File jar,String itm,byte [] data)
 {
   base_file = jar;
   jar_item = itm;
   file_data = data;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

File getJarFile()
{
    if (jar_item != null) return base_file;
    return null;
}


File getPath()
{
   return base_file;
}



/********************************************************************************/
/*                                                                              */
/*      Data methods                                                            */
/*                                                                              */
/********************************************************************************/

InputStream getInputStream() 
{
   if (jar_item == null) {
      try {
         FileInputStream ins = new FileInputStream(base_file);
         return ins;
       }
      catch (IOException e) { }
    }
   else if (file_data != null) {
      ByteStream bs = new ByteStream(this,file_data);
      return bs;
    }
   else {
      try {
         FileInputStream ins = new FileInputStream(base_file);
         JarInputStream jins = new JarInputStream(ins);
         for ( ; ; ) {
            JarEntry je = jins.getNextJarEntry();
            if (je == null) break;
            if (je.getName().equals(jar_item)) {
               return jins;
             }
          }
         ins.close();
       }
      catch (IOException e) {
         System.err.println("FAIT: Problem reading jar file: " + e);
         e.printStackTrace();
       }
    }
   return null;
}



void clear() 
{
   file_data = null;
}



/********************************************************************************/
/*                                                                              */
/*      Stream from stored data                                                 */
/*                                                                              */
/********************************************************************************/

private static class ByteStream extends ByteArrayInputStream {
   
   private JcodeFileInfo file_info;
   
   ByteStream(JcodeFileInfo fi,byte [] data) {
      super(data);
      file_info = fi;
    }
   
   @Override public void close() {
      file_info.clear();
    }
   
}	// end of inner class ByteStream



}       // end of class JcodeFileInfo




/* end of JcodeFileInfo.java */
