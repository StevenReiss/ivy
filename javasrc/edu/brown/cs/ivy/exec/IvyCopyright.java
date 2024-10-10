/********************************************************************************/
/*										*/
/*		IvyCopyright.java						*/
/*										*/
/*	Insert or replace copyright statement in files				*/
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



package edu.brown.cs.ivy.exec;

import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public final class IvyCopyright
{



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   IvyCopyright ic = new IvyCopyright(args);
   ic.process();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		copyright_statement;
private List<File>	work_files;
private boolean 	force_replace;
private boolean 	save_backup;

enum LineType { BLANK, COMMENT, COPYRIGHT, OTHER };


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private IvyCopyright(String [] args)
{
   copyright_statement = null;
   work_files = new ArrayList<>();
   force_replace = false;
   save_backup = false;

   scanArgs(args);
}


/********************************************************************************/
/*										*/
/*	Argument scanning							*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-c") && i+1 < args.length) {           // -c <copyright file>
	    copyright_statement = loadFile(args[++i]);
	  }
	 else if (args[i].startsWith("-F")) {                           // -Force
	    force_replace = true;
	  }
	 else if (args[i].startsWith("-B")) {
	    save_backup = true;
	  }
	 else badArgs();
       }
      else {
	 File f2 = new File(args[i]);
	 if (f2.exists() && f2.canRead() && f2.canWrite()) {
	    work_files.add(f2);
	  }
	 else {
	    System.err.println("COPYRIGHT: Skipping file " + f2);
	  }
       }
    }
}



private String loadFile(String f)
{
   try (FileReader fr = new FileReader(f)) {
      StringBuffer buf = new StringBuffer();
      char [] cbuf = new char[16384];
      for ( ; ; ) {
	 int ln = fr.read(cbuf);
	 if (ln <= 0) break;
	 buf.append(cbuf,0,ln);
       }
      return buf.toString();
    }
   catch (IOException e) {
      System.err.println("COPYRIGHT: Copyright file " + f + " not accessible");
      System.exit(1);
    }

   return null;
}


private void badArgs()
{
   System.err.println("COPYRIGHT: copyright [-c <file>] [-Force] file...");
   System.exit(1);
}


/********************************************************************************/
/*										*/
/*	Processing								*/
/*										*/
/********************************************************************************/

private void process()
{
   if (copyright_statement == null) {
      copyright_statement = "/*\tCopyright 2023 Brown University -- Steven P. Reiss\t\t*/\n\n";
    }

   for (File f : work_files) {
      copyInsert(f);
    }
}


private void copyInsert(File inf)
{
   File outf = new File(inf.getPath() + ".copyright");
   String input = loadSourceFile(inf);
   if (input == null) return;
   Point p0 = findInsertionPoint(input);
   if (p0 == null) return;

   try (PrintWriter pw = new PrintWriter(outf)) {
      int start = p0.x;
      int end = p0.y;
      if (start > 0) {
	 pw.write(input.substring(0,start));
       }
      pw.write(copyright_statement);
      pw.write(input.substring(end));
    }
   catch (IOException e) {
     System.err.println("COPYRIGHT: Problem writing copyright for " + inf);
    }

   if (save_backup) {
      File bckf = new File(inf.getPath() + ".backup");
      inf.renameTo(bckf);
   }
   outf.renameTo(inf);

   System.err.println("COPYRIGHT: Copyright inserted into " + inf);
}


private Point findInsertionPoint(String input)
{
   Point fnd = null;
   int ptr = 0;
   while (ptr >= 0 && ptr < input.length()) {
      int nidx = scanComment(input,ptr);
      if (nidx < 0) {
	 if (fnd == null) fnd = new Point(ptr,ptr);
	 return fnd;
       }
      else {
	 String cmmt = input.substring(ptr,nidx);
	 if (!cmmt.toLowerCase().contains("copyright")) {
	    if (fnd != null) return fnd;
	  }
	 else {
	    if (!force_replace) return null;
	    if (fnd == null) fnd = new Point(ptr,nidx);
	    else fnd.y = nidx;
	  }
	 ptr = nidx;
       }
    }
   return fnd;
}


private int scanComment(String input,int idx)
{
   int i;
   String incmmt = null;

   for (i = idx; i < input.length(); ++i) {
      char c = input.charAt(i);
      if (c == ' ' || c == '\t') continue;
      break;
    }

   if (i+1 >= input.length()) return -1;
   char c = input.charAt(i);
   char c1 = input.charAt(i+1);
   if (c == '/' && c1 == '/') incmmt = "\n";
   else if (c == '/' && c1 == '*') incmmt = "*/";
   else return -1;

   int cln = incmmt.length();
   int ecmt = -1;
   for (i = i+2; i < input.length(); ++i) {
      if (input.regionMatches(i,incmmt,0,cln)) {
	 ecmt = i+cln;
	 break;
       }
    }
   if (ecmt < 0) return -1;

   for (i = ecmt; i < input.length(); ++i) {
      char c2 = input.charAt(i);
      if (c2 == ' ' || c2 == '\t') continue;
      else {
	 if (c2 == '\n') ++i;
	 break;
       }
    }

   return i;
}



private String loadSourceFile(File f)
{
   try (FileReader fr = new FileReader(f)) {
      StringBuffer buf = new StringBuffer();
      char [] cbuf = new char[16384];
      for ( ; ; ) {
	 int ln = fr.read(cbuf);
	 if (ln <= 0) break;
	 buf.append(cbuf,0,ln);
       }
      return buf.toString();
    }
   catch (IOException e) {
      System.err.println("COPYRIGHT: Source file " + f + " not accessible");
    }

   return null;
}




}	// end of class IvyCopyright




/* end of IvyCopyright.java */

