/********************************************************************************/
/*										*/
/*		LimboFile.java							*/
/*										*/
/*	Internal representation of file data for easy access			*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/limbo/LimboFile.java,v 1.6 2011-05-27 19:32:41 spr Exp $ */


/*********************************************************************************
 *
 * $Log: LimboFile.java,v $
 * Revision 1.6  2011-05-27 19:32:41  spr
 * Change copyrights.
 *
 * Revision 1.5  2010-02-26 21:05:18  spr
 * Fix potential errors in limbo with first/last lines
 *
 * Revision 1.4  2010-02-12 00:35:16  spr
 * Fix spacing; avoid null pointer no missing attribute.
 *
 * Revision 1.3  2008-03-14 12:26:31  spr
 * Add limbo implementation (not testing of it).
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.limbo;


import java.io.*;
import java.util.ArrayList;
import java.util.List;



class LimboFile {




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private File   for_file;
private String [] line_data;
private long last_modified;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

LimboFile(File f)
{
   for_file = f;
   line_data = null;
   last_modified = f.lastModified();

   try {
      BufferedReader br = new BufferedReader(new FileReader(f));
      List<String> ldata = new ArrayList<String>();
      for ( ; ; ) {
	 String ln = br.readLine();
	 if (ln == null) break;
	 ldata.add(ln);

       }
      br.close();
      line_data = ldata.toArray(new String[ldata.size()]);
    }
   catch (IOException e) {
      System.err.println("LIMBO: Problem reading file " + f + ": " + e);
    }
}




LimboFile(Reader r)
{
   for_file = null;
   line_data = null;
   last_modified = 0;

   try {
      BufferedReader br = new BufferedReader(r);
      List<String> ldata = new ArrayList<String>();
      for ( ; ; ) {
	 String ln = br.readLine();
	 if (ln == null) break;
	 ldata.add(ln);

       }
      br.close();
      line_data = ldata.toArray(new String[ldata.size()]);
    }
   catch (IOException e) {
      System.err.println("LIMBO: Problem reading provided Reader: " + e);
    }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

boolean isValid()			{ return line_data != null; }


String getFileName()			{ return for_file.getAbsolutePath(); }
File getFile()				{ return for_file; }
long getLastModified()			{ return last_modified; }


String getLine(int n)
{
   if (n <= 0) return null;
   if (n > line_data.length) return null;

   return line_data[n-1];
}



int getLineCount()
{
   return line_data.length;
}





}	// end of class LimboFile




/* end of LimboFile.java */

