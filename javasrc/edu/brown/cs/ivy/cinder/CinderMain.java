/********************************************************************************/
/*										*/
/*		CinderMain.java 						*/
/*										*/
/*	Main program for class file patching					*/
/*										*/
/********************************************************************************/
/*	Copyright 1997 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 1997, Brown University, Providence, RI.				 *
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
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,	 *
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS 	 *
 *  SOFTWARE.									 *
 *										 *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/cinder/CinderMain.java,v 1.9 2013/09/24 01:06:48 spr Exp $ */


/*********************************************************************************
 *
 * $Log: CinderMain.java,v $
 * Revision 1.9  2013/09/24 01:06:48  spr
 * Minor fix
 *
 * Revision 1.8  2009-09-17 01:54:51  spr
 * Enable patching at line number level.
 *
 * Revision 1.7  2008-03-14 12:25:18  spr
 * Fixes for java 1.6; code cleanup.
 *
 * Revision 1.6  2007-08-10 02:10:02  spr
 * Cleanups from Eclipse
 *
 * Revision 1.5  2006-12-01 03:22:37  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.4  2006/07/10 14:52:09  spr
 * Code cleanup.
 *
 * Revision 1.3  2005/09/19 23:32:00  spr
 * Fixups based on findbugs.
 *
 * Revision 1.2  2004/05/05 02:28:08  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1  2003/03/29 03:40:25  spr
 * Move CINDER interface to JikesBT from Bloom to Ivy.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.cinder;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;



public class CinderMain
{



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

static public void main(String [] args)
{
   CinderMain cm = new CinderMain(args);

   cm.process();

   System.exit(0);
}




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private String	input_file;
private String	output_file;
private String	patch_class;
private CinderPatchType patch_type;
private String	class_path;
private String	base_path;
private String	patch_args;
private String	server_data;

private CinderManager patch_worker;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private CinderMain(String [] args)
{
   input_file = null;
   output_file = null;
   class_path = null;
   base_path = null;
   patch_type = new CinderPatchType();
   patch_args = null;
   server_data = null;

   scanArgs(args);
}




/********************************************************************************/
/*										*/
/*	Argument proicessing methods						*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-i") && i+1 < args.length) {
	    input_file = args[++i];
	  }
	 else if (args[i].startsWith("-o") && i+1 < args.length) {
	    output_file = args[++i];
	  }
	 else if (args[i].startsWith("-c") && i+1 < args.length) {
	    class_path = args[++i];
	  }
	 else if (args[i].startsWith("-b") && i+1 < args.length) {
	    base_path = args[++i];
	  }
	 else if (args[i].startsWith("-t") && i+1 < args.length) {
	    patch_class = args[++i];
	  }
	 else if (args[i].startsWith("-a") && i+1 < args.length) {
	    if (patch_args == null) patch_args = args[++i];
	    else patch_args = patch_args + " " + args[++i];
	  }
	 else if (args[i].startsWith("-s") && i+1 < args.length) {
	    server_data = args[++i];
	  }
	 else if (args[i].startsWith("-B")) {                   // -BLOCKS
	    patch_type.setPatchBasicBlocks(true);
	  }
	 else if (args[i].startsWith("-E")) {                   // -ENTRY [#]
	    int ct = 1;
	    if (i+1 < args.length && Character.isDigit(args[i+1].charAt(0))) {
	       try {
		  ct = 1+Integer.parseInt(args[++i]);
		}
	       catch (NumberFormatException e) {
		  badArgs();
		}
	     }
	    patch_type.setPatchEntry(true,ct);
	  }
	 else if (args[i].startsWith("-XV")) {                  // -XV (exit value)
	    patch_type.setPatchExit(true,true);
	  }
	 else if (args[i].startsWith("-X")) {                   // -X (exit, no value)
	    patch_type.setPatchExit(true,false);
	  }
	 else if (args[i].startsWith("-S")) {                   // -SYNC
	    patch_type.setPatchSynchronization(true);
	  }
	 else if (args[i].startsWith("-L")) {                   // -LOCAL
	    patch_type.setPatchLocalAccess(true);
	  }
	 else if (args[i].startsWith("-R")) {                   // -REMOTE
	    patch_type.setPatchRemoteAccess(true);
	  }
	 else if (args[i].startsWith("-A")) {                   // -ACCESS
	    patch_type.setPatchLocalAccess(true);
	    patch_type.setPatchRemoteAccess(true);
	  }
	 else if (args[i].startsWith("-W")) {                   // -WRITE
	    patch_type.setPatchWrites(true);
	  }
	 else if (args[i].startsWith("-N")) {                   // -NEW
	    patch_type.setPatchAllocs(true);
	  }
	 else badArgs();
       }
      else if (input_file == null) input_file = args[i];
      else if (output_file == null) output_file = args[i];
      else badArgs();
    }

   if (server_data == null) {
      if (input_file == null || output_file == null || patch_class == null) badArgs();
    }
   else if (input_file != null) badArgs();
}



private void badArgs()
{
   System.err.println("CINDER: CinderMain infile outfile [-BLOCKS] [-ENTRY [#]] ");
   System.err.println("\t[-X[V]] [-SYNC] [-LOCAL] [-REMOTE] [-ACCESS] [-WRITE] [-NEW]");
   System.err.println("\t[-cp <classpath>] [-bp <basepath>]");
   System.err.println("\t-t <classinstrumenter>");
   System.err.println();
   System.err.println("CINDER: CinderMain -server <port> outfile [-BLOCKS] [-ENTRY [#]] ");
   System.err.println("\t[-X[V]] [-SYNC] [-LOCAL] [-REMOTE] [-ACCESS] [-WRITE] [-NEW]");
   System.err.println("\t[-cp <classpath>] [-bp <basepath>]");
   System.err.println("\t-t <classinstrumenter>");

   System.exit(1);
}




/********************************************************************************/
/*										*/
/*	Cinder processing methods						*/
/*										*/
/********************************************************************************/

private void process()
{
   patch_worker = new CinderManager();
   CinderManager.setClassPath(base_path,class_path);

   try {
      Class<?> c = Class.forName(patch_class);
      CinderInstrumenter ci = (CinderInstrumenter) c.getDeclaredConstructor().newInstance();
      if (patch_args != null) ci.setArguments(patch_args);
      patch_type.setInstrumenter(ci);
    }
   catch (Exception e) {
      System.err.println("CINDER: Problem loading instrumenter: " + e.getMessage());
      System.exit(1);
    }

   patch_worker.setPatchType(patch_type);

   if (server_data == null) patch_worker.processFile(input_file,output_file);
   else runServer(server_data);
}




/************************************************************************/
/*									*/
/*	Methods for running as a server 				*/
/*									*/
/************************************************************************/

private void runServer(String args)
{
   int port = 0;
  
   try {
      port = Integer.parseInt(args);
    }
   catch (NumberFormatException e) {
      badArgs();
    }

   ServerSocket ss = null;

   try {
      ss = new ServerSocket(port);
    }
   catch (IOException e) {
      System.err.println("CINDER: Server already running");
      System.exit(0);
      return;
    }

   for ( ; ; ) {
      try {
	 Socket s = ss.accept();
	 handleConnection(s);
	 s.close();
       }
      catch (IOException e) {
	 break;
       }
    }

   try {
      ss.close();
    }
   catch (IOException e) { }
}



private void handleConnection(Socket s)
{
   try {
      InputStream ins = s.getInputStream();
      BufferedInputStream bins = new BufferedInputStream(ins);
      OutputStream ots = s.getOutputStream();
      OutputStreamWriter osw = new OutputStreamWriter(ots);
      PrintWriter pw = new PrintWriter(osw,true);

      for ( ; ; ) {
	 StringBuffer cmd = new StringBuffer();
	 for ( ; ; ) {
	    int ch = bins.read();
	    if (ch == '\n') break;
	    cmd.append((char) ch);
	  }
System.err.println("CINDER: Server command: " + cmd);
	 StringTokenizer tok = new StringTokenizer(cmd.toString());
	 if (tok.hasMoreTokens()) {
	    String what = tok.nextToken();
	    String ifile,ofile;
	    File tmpf = null;
	    ofile = null;
	    if (what.equals("PATCH") && tok.hasMoreTokens()) {
	       int len = Integer.parseInt(tok.nextToken());
	       if (len > 0) {
		  byte [] buf = new byte[len];
		  int rln = bins.read(buf,0,len);
		  if (rln != len) System.err.println("CINDER: Read error");
		  tmpf = File.createTempFile("Cinder",".class");
		  ifile = tmpf.getAbsolutePath();
		  FileOutputStream fots = new FileOutputStream(tmpf);
		  fots.write(buf);
		  fots.close();
		  if (tok.hasMoreTokens()) {
		     ofile = tok.nextToken();
		   }
		  patch_worker.processFile(ifile,ofile);
		  tmpf.delete();
		}
System.err.println("CINDER: ==> " + ofile);
	       if (ofile == null) pw.println("*");
	       else pw.println(ofile);
	     }
	  }
       }
    }
   catch (IOException e) { }
}



}	// end of class CinderMain




/* end of CinderMain.java */
