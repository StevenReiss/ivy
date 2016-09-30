/********************************************************************************/
/*										*/
/*		IvyFile.java							*/
/*										*/
/*	Utility methods for file names						*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/file/IvyFile.java,v 1.23 2016/09/30 20:45:22 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyFile.java,v $
 * Revision 1.23  2016/09/30 20:45:22  spr
 * Add new utility methods.
 *
 * Revision 1.22  2016/07/13 13:03:28  spr
 * Add other file read/write routines
 *
 * Revision 1.21  2015/07/02 19:01:27  spr
 * Minor bug fixes
 *
 * Revision 1.20  2014/06/12 01:06:25  spr
 * Minor updates
 *
 * Revision 1.19  2014/02/26 14:07:51  spr
 * Add new file copy method.
 *
 * Revision 1.18  2014/01/22 00:31:13  spr
 * Minor fixup.
 *
 * Revision 1.17  2013/09/24 01:06:53  spr
 * Minor fix
 *
 * Revision 1.16  2013-05-09 12:16:03  spr
 * Last checkin at for Java 1.6
 *
 * Revision 1.15  2011-05-27 19:32:38  spr
 * Change copyrights.
 *
 * Revision 1.14  2009-10-02 00:18:22  spr
 * Import clean up.
 *
 * Revision 1.13  2009-09-17 01:55:38  spr
 * Add database and setup support.
 *
 * Revision 1.12  2009-06-04 18:49:46  spr
 * Additional expansion options.
 *
 * Revision 1.11  2009-03-20 01:57:00  spr
 * Add remove call for directories; handle windows file names.
 *
 * Revision 1.10  2008-11-12 13:45:19  spr
 * Add DYVISE.
 *
 * Revision 1.9  2008-03-14 12:25:42  spr
 * Add s6 support.
 *
 * Revision 1.8  2006/07/03 18:14:54  spr
 * Add WADI support.
 *
 * Revision 1.7  2006/05/10 13:42:07  spr
 * Check for null parameters.
 *
 * Revision 1.6  2006/03/09 23:32:18  spr
 * Use System.getenv as well as properties.
 *
 * Revision 1.5  2005/10/31 19:20:18  spr
 * Provide an expand call with user-defined additional definitions.
 *
 * Revision 1.4  2005/06/28 17:20:14  spr
 * Fix up known paths for $(xxx) lookup.
 *
 * Revision 1.3  2004/05/05 02:28:08  spr
 * Update import lists using eclipse.
 *
 * Revision 1.2  2003/08/04 13:06:41  spr
 * Add TAIGA to possible expansions.
 *
 * Revision 1.1  2003/03/18 20:19:25  spr
 * Initial conversion of BloomFile to IvyFile -- provide file name lookup.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.file;

import edu.brown.cs.ivy.exec.IvyExecQuery;
import edu.brown.cs.ivy.exec.IvySetup;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;


public class IvyFile {



/********************************************************************************/
/*										*/
/*	Ensure the environment is set up correctly				*/
/*										*/
/********************************************************************************/

static {
   IvySetup.setup();
}



/********************************************************************************/
/*										*/
/*	Method for expanding filenames						*/
/*										*/
/********************************************************************************/

public static File expandFile(String name)
{
   return new File(expandName(name,null));
}



public static File expandFile(String name,Map<String,String> vals)
{
   return new File(expandName(name,vals));
}


public static String expandName(String name)
{
   return expandName(name,null);
}



public static String expandName(String name,Map<String,String> vals)
{
   StringBuffer buf = new StringBuffer();

   if (name == null) return null;

   for (int i = 0; i < name.length(); ++i) {
      char c = name.charAt(i);
      if (c == '$' && i+1 < name.length() && name.charAt(i+1) == '(') {
	 StringBuffer tok = new StringBuffer();
	 for (i = i+2; i < name.length() && name.charAt(i) != ')'; ++i) {
	    tok.append(name.charAt(i));
	  }
	 if (i >= name.length()) break;
	 String erslt = null;
	 String what = tok.toString();
	 if (vals != null && vals.containsKey(what)) {
	    erslt = vals.get(what);
	  }
	 else {
	    if (what.equals("PRO")) what = "ROOT";
	    else if (what.equals("USER")) what = "user.name";
	    else if (what.equals("HOME")) what = "user.home";
	    else if (what.equals("CWD")) what = "user.dir";
	    erslt = System.getProperty(what);
	    if (erslt == null) erslt = System.getProperty("edu.brown.cs.override." + what);
	    if (erslt == null) erslt = System.getProperty("edu.brown.cs." + what);
	    if (erslt == null) erslt = System.getProperty("edu.brown.cs.ivy." + what);
	    if (erslt == null) erslt = System.getProperty("edu.brown.bloom." + what);
	    if (erslt == null) erslt = System.getProperty("edu.brown.cs.taiga." + what);
	    if (erslt == null) erslt = System.getProperty("edu.brown.cs.veld." + what);
	    if (erslt == null) erslt = System.getProperty("edu.brown.cs.wadi." + what);
	    if (erslt == null) erslt = System.getProperty("edu.brown.cs.dyvise." + what);
	    if (erslt == null) erslt = System.getProperty("edu.brown.cs.s6." + what);
	    if (erslt == null) erslt = System.getProperty("edu.brown.clime." + what);
	    if (erslt == null) erslt = System.getenv(what);
	    if (erslt == null) erslt = System.getenv("BROWN_IVY_" + what);
	    if (erslt == null) erslt = System.getenv("BROWN_BLOOM_" + what);
	    if (erslt == null) erslt = System.getenv("BROWN_TAIGA_" + what);
	    if (erslt == null) erslt = System.getenv("BROWN_CLIME_" + what);
	    if (erslt == null) erslt = System.getenv("BROWN_VELD_" + what);
	    if (erslt == null) erslt = System.getenv("BROWN_WADI_" + what);
	    if (erslt == null) erslt = System.getenv("BROWN_DYVISE_" + what);
	    if (erslt == null) erslt = System.getenv("BROWN_S6_" + what);
	    if (erslt == null) erslt = System.getenv("BROWN_" + what);
	    if (erslt == null && what.equals("HOST")) erslt = IvyExecQuery.computeHostName();
	  }
	 if (erslt != null) buf.append(erslt);
       }
      else if (c == '/' && (vals == null || !vals.containsKey("NOSLASH"))) buf.append(File.separatorChar);
      else buf.append(c);
    }

   return buf.toString();
}



public static String expandText(String name,Map<String,String> vals)
{
   StringBuffer buf = new StringBuffer();

   if (name == null) return null;

   for (int i = 0; i < name.length(); ++i) {
      char c = name.charAt(i);
      if (c == '$' && i+1 < name.length() && name.charAt(i+1) == '(') {
	 StringBuffer tok = new StringBuffer();
	 for (i = i+2; i < name.length() && name.charAt(i) != ')'; ++i) {
	    tok.append(name.charAt(i));
	  }
	 if (i >= name.length()) break;
	 String erslt = null;
	 String what = tok.toString();
	 String dflt = null;
	 int idx = what.indexOf("=");
	 if (idx > 0) {
	    dflt = what.substring(idx+1).trim();
	    what = what.substring(0,idx).trim();
	  }
	 if (vals != null && vals.containsKey(what)) {
	    erslt = vals.get(what);
	  }
	 else if (what.equals("USER")) what = "user.name";
	 else if (what.equals("HOME")) what = "user.home";
	 else if (what.equals("CWD")) what = "user.dir";
	 if (erslt == null) erslt = System.getProperty(what);
	 if (erslt == null) erslt = System.getenv(what);
	 if (erslt == null && what.equals("HOST")) erslt = IvyExecQuery.computeHostName();
	 if (erslt == null) erslt = dflt;
	 if (erslt != null) buf.append(erslt);
       }
      else buf.append(c);
    }

   return buf.toString();
}



/********************************************************************************/
/*										*/
/*	Methods for removing a directory and all its files			*/
/*										*/
/********************************************************************************/

public static void remove(String dir) throws IOException
{
   if (dir == null) return;

   remove(new File(dir));
}




public static void remove(File dir) throws IOException
{
   if (dir == null || !dir.exists()) return;
   if (!dir.isDirectory()) {
      if (!dir.delete()) {
	 throw new IOException("Problem removing " + dir);
       }
      return;
    }

   File [] fls = dir.listFiles();
   if (fls != null) {
      for (int i = 0; i < fls.length; ++i) {
	 remove(fls[i]);
       }
    }
   if (!dir.delete()) {
      throw new IOException("Problem removing " + dir);
    }
}



/********************************************************************************/
/*										*/
/*	Update all permissions in a file/directory				*/
/*										*/
/********************************************************************************/

public static void updatePermissions(File dir,int permissions)
{
   Path p = Paths.get(dir.getAbsolutePath());

   updatePermissions(p,permissions);
}



public static void updatePermissions(Path dir,int permissions)
{
   Set<PosixFilePermission> perms = EnumSet.noneOf(PosixFilePermission.class);
   if ((permissions & 01) != 0) perms.add(PosixFilePermission.OTHERS_EXECUTE);
   if ((permissions & 02) != 0) perms.add(PosixFilePermission.OTHERS_WRITE);
   if ((permissions & 04) != 0) perms.add(PosixFilePermission.OTHERS_READ);
   if ((permissions & 010) != 0) perms.add(PosixFilePermission.GROUP_EXECUTE);
   if ((permissions & 020) != 0) perms.add(PosixFilePermission.GROUP_WRITE);
   if ((permissions & 040) != 0) perms.add(PosixFilePermission.GROUP_READ);
   if ((permissions & 0100) != 0) perms.add(PosixFilePermission.OWNER_EXECUTE);
   if ((permissions & 0200) != 0) perms.add(PosixFilePermission.OWNER_WRITE);
   if ((permissions & 0400) != 0) perms.add(PosixFilePermission.OWNER_READ);

   updatePermissions(dir,perms);
}



public static void updatePermissions(Path dir,Set<PosixFilePermission> perms)
{
   try {
      Files.setPosixFilePermissions(dir,perms);
    }
   catch (IOException e) { }

   File [] fls = dir.toFile().listFiles();
   if (fls != null) {
      for (int i = 0; i < fls.length; ++i) {
	  Path p = Paths.get(fls[i].getAbsolutePath());
	  updatePermissions(p,perms);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Methods for loading a file into a string				*/
/*										*/
/********************************************************************************/

public static String loadFile(File f) throws IOException
{
   FileReader fr = new FileReader(f);

   String txt = loadFile(fr);

   fr.close();

   return txt;
}




public static String loadFile(Reader fr) throws IOException
{
   StringBuffer buf = new StringBuffer();

   char [] cbuf = new char[16384];
   for ( ; ; ) {
      int ln = fr.read(cbuf);
      if (ln <= 0) break;
      buf.append(cbuf,0,ln);
    }

   return buf.toString();
}




public static void copyFile(File sf,File df) throws IOException
{
   FileInputStream r = new FileInputStream(sf);
   FileOutputStream w = new FileOutputStream(df);
   byte [] buf = new byte[8192];
   for ( ; ; ) {
      int ln = r.read(buf);
      if (ln <= 0) break;
      w.write(buf,0,ln);
    }
   w.close();
   r.close();
}




public static void copyFile(File sf,OutputStream w) throws IOException
{
   FileInputStream r = new FileInputStream(sf);
   byte [] buf = new byte[8192];
   for ( ; ; ) {
      int ln = r.read(buf);
      if (ln <= 0) break;
      w.write(buf,0,ln);
    }
   r.close();
}




public static void copyFile(File sf,Writer w) throws IOException
{
   FileReader r = new FileReader(sf);
   char [] buf = new char[8192];
   for ( ; ; ) {
      int ln = r.read(buf);
      if (ln <= 0) break;
      w.write(buf,0,ln);
    }
   r.close();
}




public static void copyFile(File sf,File df,Map<String,String> vals) throws IOException
{
   BufferedReader r = new BufferedReader(new FileReader(sf));
   FileWriter w = new FileWriter(df);
   String eol = System.getProperty("line.separator");
   if (eol == null) eol = "\n";
   for ( ; ; ) {
      String ln = r.readLine();
      if (ln == null) break;
      ln = expandText(ln,vals);
      w.write(ln);
      w.write(eol);
    }
   w.close();
   r.close();
}



/********************************************************************************/
/*                                                                              */
/*      Path management methods                                                 */
/*                                                                              */
/********************************************************************************/

public static String getRelativePath(File f,File dir)
{
   String p1 = f.getAbsolutePath();
   String p2 = dir.getAbsolutePath();
   if (p1.equals(p2)) return "";
   
   if (p2.length() > p1.length()) {
      String p3 = p1;
      p1 = p2;
      p2 = p3;
    }
   else if (p1.length() == p2.length()) return null;
   
   if (!p1.startsWith(p2)) return null;
   char ch = p1.charAt(p2.length());
   if (ch != File.separatorChar) return null;
   
   return p1.substring(p2.length()+1);
}



public static File getCommonParent(File f1,File f2)
{
   List<File> path1 = new ArrayList<File>();
   List<File> path2 = new ArrayList<File>();
   
   path1.add(f1);
   while (f1.getParentFile() != null) {
      f1 = f1.getParentFile();
      path1.add(f1);
    }
   
   path2.add(f2);
   while (f2.getParentFile() != null) {
      f2 = f2.getParentFile();
      path2.add(f2);
    }
   
   File par = null;
   for (int i = 0; ; ++i) {
      if (i >= path1.size() || i >= path2.size()) break;
      File p1 = path1.get(path1.size()-i-1);
      File p2 = path2.get(path2.size()-i-1);
      if (!p1.equals(p2)) break;
      par = p1;
    }
   return par;
}



}	// end of class IvyFile
