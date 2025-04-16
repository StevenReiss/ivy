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



package edu.brown.cs.ivy.file;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;


public class IvyFile {



/********************************************************************************/
/*										*/
/*	Ensure the environment is set up correctly				*/
/*										*/
/********************************************************************************/

private static Map<File,File> canonical_map = new HashMap<>();

private static final int     APPROX_TIME = 15*60*1000;
private static final DateFormat TIME_FORMAT = new SimpleDateFormat("h:mm");


static {
   try {
      Class<?> setup = Class.forName("edu.brown.cs.ivy.exec.IvySetup");
      Method m = setup.getMethod("setup");
      m.invoke(null);
    }
   catch (Throwable e) {
      IvyLog.logE("Problem invoking setup",e);
    }
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
	    if (erslt == null && what.equals("HOST")) erslt = computeHostName();
	  }
	 if (erslt != null) buf.append(erslt);
       }
      else if (c == '/' && (vals == null || !vals.containsKey("NOSLASH"))) {
	 buf.append(File.separatorChar);
       }
      else buf.append(c);
    }

   return buf.toString();
}



public static String expandText(String name,Map<String,String> vals)
{
   return expandText(name,vals,true);
}


public static String expandText(String name,Map<String,String> vals,boolean sys)
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
	    dflt = fixTimeValue(dflt);
	  }
	 if (vals != null && vals.containsKey(what)) {
	    erslt = vals.get(what);
	  }
	 else if (sys) {
	    if (what.equals("USER")) what = "user.name";
	    else if (what.equals("HOME")) what = "user.home";
	    else if (what.equals("CWD")) what = "user.dir";
	    if (erslt == null) erslt = System.getProperty(what);
	    if (erslt == null) erslt = System.getenv(what);
	    if (erslt == null && what.equals("HOST")) erslt = computeHostName();
	  }
	 if (erslt == null) erslt = dflt;
	 if (erslt != null) buf.append(erslt);
       }
      else buf.append(c);
    }

   return buf.toString();
}



private static String fixTimeValue(String val)
{
   if (val.startsWith("@")) {
      val = val.substring(1);
    }
   else return val;

   int delta = 60*60*1000;
   if (val.startsWith("+")) {
      val = val.substring(1);
      delta = Integer.parseInt(val);
    }
   long t0 = System.currentTimeMillis() + delta;
   t0 = (t0 + APPROX_TIME-1) / APPROX_TIME;
   t0 = t0 * APPROX_TIME;

   Date d = new Date(t0);
   String rslt = TIME_FORMAT.format(d);

   return rslt;
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


public static String loadFile(InputStream ins) throws IOException
{
   if (ins == null) return null;
   InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
   return loadFile(isr);
}


public static byte [] loadBinaryFile(InputStream r) throws IOException
{
   ByteArrayOutputStream w = new ByteArrayOutputStream();
   byte [] buf = new byte[8192];
   for ( ; ; ) {
      int ln = r.read(buf);
      if (ln <= 0) break;
      w.write(buf,0,ln);
    }
   byte [] rslt = w.toByteArray();
   r.close();
   w.close();
   return rslt;
}




public static void copyFile(File sf,File df) throws IOException
{
   FileInputStream r = new FileInputStream(sf);
   FileOutputStream w = new FileOutputStream(df);
   try {
      byte [] buf = new byte[8192];
      for ( ; ; ) {
	 int ln = r.read(buf);
	 if (ln <= 0) break;
	 w.write(buf,0,ln);
       }
    }
   finally {
      w.close();
      r.close();
    }
}



public static void copyFile(InputStream r,File df) throws IOException
{
   FileOutputStream w = new FileOutputStream(df);
   try {
      byte [] buf = new byte[8192];
      for ( ; ; ) {
	 int ln = r.read(buf);
	 if (ln <= 0) break;
	 w.write(buf,0,ln);
       }
    }
   finally {
      w.close();
      r.close();
    }
}



public static void copyFileNoClose(InputStream r,File df) throws IOException
{
   FileOutputStream w = new FileOutputStream(df);
   try {
      byte [] buf = new byte[8192];
      for ( ; ; ) {
	 int ln = r.read(buf);
	 if (ln <= 0) break;
	 w.write(buf,0,ln);
       }
    }
   finally {
      w.close();
    }
}



public static void copyHierarchy(File sd,File dd) throws IOException
{
   if (sd.isDirectory()) {
      if (!dd.exists()) dd.mkdirs();
      for (File f : sd.listFiles()) {
	 File df = new File(dd,f.getName());
	 copyHierarchy(f,df);
       }
    }
   else {
      copyFile(sd,dd);
    }
}




public static void copyFile(File sf,OutputStream w) throws IOException
{
   FileInputStream r = new FileInputStream(sf);
   try {
      byte [] buf = new byte[8192];
      for ( ; ; ) {
	 int ln = r.read(buf);
	 if (ln <= 0) break;
	 w.write(buf,0,ln);
       }
    }
   finally {
      r.close();
    }
}


public static void copyFile(InputStream ins,OutputStream ots) throws IOException
{
   byte [] buf = new byte[8192];
   for ( ; ; ) {
      int ln = ins.read(buf);
      if (ln < 0) break;
      ots.write(buf,0,ln);
    }
   ins.close();
   ots.close();
}




public static void copyFile(File sf,Writer w) throws IOException
{
   FileReader r = new FileReader(sf);
   try {
      char [] buf = new char[8192];
      for ( ; ; ) {
	 int ln = r.read(buf);
	 if (ln <= 0) break;
	 w.write(buf,0,ln);
       }
    }
   finally {
      r.close();
    }
}




public static void copyFile(File sf,File df,Map<String,String> vals) throws IOException
{
   try (
	 BufferedReader r = new BufferedReader(new FileReader(sf));
	 FileWriter w = new FileWriter(df)) {
      String eol = System.getProperty("line.separator");
      if (eol == null) eol = "\n";
      for ( ; ; ) {
	 String ln = r.readLine();
	 if (ln == null) break;
	 ln = expandText(ln,vals);
	 w.write(ln);
	 w.write(eol);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Path management methods 						*/
/*										*/
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


public static File getCanonical(File f0)
{
   if (f0 == null) return null;
   File f2 = canonical_map.get(f0);
   if (f2 != null) return f2;

   File f1 = f0;
   if (!f0.isAbsolute()) f1 = f0.getAbsoluteFile();
   f2 = canonical_map.get(f1);
   if (f2 != null) return f2;

   try {
      f2 = f1.getCanonicalFile();
    }
   catch (IOException e) {
      f2 = f1;
    }

   canonical_map.put(f0,f2);
   canonical_map.put(f1,f2);

   return f2;
}

public static String getCanonicalPath(File f1)
{
   if (f1 == null) return null;

   File f2 = getCanonical(f1);

   return f2.getPath();
}



/********************************************************************************/
/*										*/
/*	Encoding methods							*/
/*										*/
/********************************************************************************/

public static String digestString(String text)
{
   String rslt = text;
   try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte [] dig = md5.digest(rslt.getBytes());
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < dig.length; ++i) {
	 int v = dig[i] & 0xff;
	 String s0 = Integer.toString(v,16);
	 if (s0.length() == 1) buf.append("0");
	 buf.append(s0);
       }
      rslt = buf.toString();
    }
   catch (NoSuchAlgorithmException e) { }

   return rslt;
}


/********************************************************************************/
/*										*/
/*	JAR utilities								*/
/*										*/
/********************************************************************************/

public static File getJarFile(Class<?> c)
{
   String s = c.getName();
   s = s.replace(".","/") + ".class";
   ClassLoader cl = c.getClassLoader();
   URL url = cl.getResource(s);
   if (url == null) return null;
   String file = url.toString();
   if (file.startsWith("jar:file:/")) file = file.substring(9);
   if (file.length() >= 3 && file.charAt(0) == '/' &&
	 Character.isLetter(file.charAt(1)) && file.charAt(2) == ':' &&
	 File.separatorChar == '\\') file = file.substring(1);
   int idx = file.lastIndexOf("!");
   if (idx > 0) file = file.substring(0,idx);
   if (File.separatorChar != '/') file = file.replace('/',File.separatorChar);
   file = file.replace("%20"," ");
   if (!file.endsWith(".jar")) return null;
   File f = new File(file);
   return f;
}




/********************************************************************************/
/*										*/
/*	Host name utilities							*/
/*										*/
/********************************************************************************/

public static String computeHostName()
{
   try {
      InetAddress lh = InetAddress.getLocalHost();
      String h = lh.getCanonicalHostName();
      if (h != null) return h;
    }
   catch (IOException e) {
      System.err.println("IVY: Problem getting host name: " + e);
    }

   return "localhost";
}


/********************************************************************************/
/*										*/
/*	Markdown methods							*/
/*										*/
/********************************************************************************/

public static String parseMarkdown(String md)
{
   Parser p = Parser.builder().build();
   Node document = p.parse(md);
   HtmlRenderer renderer = HtmlRenderer.builder().build();
   String text = renderer.render(document);

   if (text != null && text.startsWith("<p>")) text = text.substring(3);

   return text;
}

}	// end of class IvyFile
