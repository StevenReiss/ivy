/********************************************************************************/
/*										*/
/*		IvyImplBuilder.java						*/
/*										*/
/*	Program to handle building an implementation jar file			*/
/*										*/
/********************************************************************************/
/*	Copyright 2003 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2003, Brown University, Providence, RI.				 *
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

import edu.brown.cs.ivy.jcode.JcodeClass;
import edu.brown.cs.ivy.jcode.JcodeFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipException;


public class IvyImplBuilder
{



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   IvyImplBuilder tt = new IvyImplBuilder(args);
   tt.process();
}




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private JcodeFactory    code_manager;
private String		class_path;
private List<String>	type_names;
private String		out_file;
private Set<JcodeClass> output_classes;
private String		start_class;
private String		manifest_file;
private boolean 	for_applet;
private Collection<Resource> resource_set;
private Set<String>	have_props;




/********************************************************************************/
/*										*/
/*	Constructor								*/
/*										*/
/********************************************************************************/

private IvyImplBuilder(String [] args)
{
   class_path = null;
   type_names = new ArrayList<>();
   out_file = null;
   output_classes = new HashSet<>();
   resource_set = new ArrayList<>();
   start_class = null;
   manifest_file = null;
   have_props = new HashSet<>();
   for_applet = false;

   code_manager = new JcodeFactory();

   scanArgs(args);
}





/********************************************************************************/
/*										*/
/*	Argument processing							*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-cp") && i+1 < args.length) {          // -cp <classpath>
	    class_path = args[++i];
	  }
	 else if (args[i].startsWith("-j") && i+1 < args.length) {      // -j <outfile>
	    out_file = args[++i];
	  }
	 else if (args[i].startsWith("-x") && i+1 < args.length) {      // -x <start class>
	    start_class = args[++i];
	    type_names.add(args[i]);
	  }
	 else if (args[i].startsWith("-m") && i+1 < args.length) {      // -m <manifest file>
	    manifest_file = args[++i];
	  }
	 else if (args[i].startsWith("-R") && i+1 < args.length) {      // -R <dir:path>
	    resource_set.add(new Resource(args[++i]));
	  }
	 else if (args[i].startsWith("nop")) {                          // -noplugin
	    have_props.add("plugin.properties");
	  }
	 else if (args[i].startsWith("-a")) {                           // -applet
	    for_applet = true;
	  }
	 else {
	    badArgs();
	  }
       }
      else if (args[i].startsWith("-")) {
	 badArgs();
       }
      else type_names.add(args[i]);
    }

   if (out_file == null) out_file = "impl.jar";
}



private void badArgs()
{
   System.err.println("IvyImplBuilder [-cp <classpath>] [-S <system pfx>] [-x <startclass>] [-j <outfile>] {<type>}");
   System.exit(1);
}



/********************************************************************************/
/*										*/
/*	Type resolution processing						*/
/*										*/
/********************************************************************************/

private void process()
{
   code_manager.addToClassPath(class_path);

   for (String nm: type_names) {
      loadType(nm);
    }

   Set<String> srcs = new HashSet<>();

   boolean chng = true;
   while (chng) {
      chng = false;
      for (JcodeClass bc : code_manager.getAllClasses()) {
	 String path = bc.getFilePath();
	 if (path == null) continue;
	 if (isSystemJar(path)) continue;
	 if (!output_classes.contains(bc)) {
	    srcs.add(path);
	    output_classes.add(bc);
	    chng = true;
	  }
       }
    }

   Manifest man = createManifest(output_classes);

   try (JarOutputStream jarout = new JarOutputStream(new FileOutputStream(out_file),man)) {
      for (String src : srcs) {
	 installSource(jarout,src);
       }

      for (Resource r : resource_set) {
	 installResource(jarout,r);
       }
    }
   catch (IOException e) {
      System.err.println("TOBI: Problem creating output jar file " + out_file + ": " + e);
      System.exit(1);
    }
}





/********************************************************************************/
/*										*/
/*	Type loading code							*/
/*										*/
/********************************************************************************/

private void loadType(String nm)
{
   String nm0 = nm;

   if (classExists(nm)) return;

   for ( ; ; ) {
      int idx = nm.lastIndexOf(".");
      if (idx < 0) break;
      String nm1 = nm.substring(0,idx) + "$" + nm.substring(idx+1);
      if (classExists(nm1)) return;
      String nm2 = nm.substring(0,idx) + "_" + nm.substring(idx+1);
      if (classExists(nm2)) return;
      nm = nm1;
    }

   System.err.println("TOBI: Can't locate type " + nm0);

   System.exit(1);
}



private boolean classExists(String nm)
{
   try {
      JcodeClass btc = code_manager.findClass(nm);
      if (btc != null) return true;
    }
   catch (Throwable t) { }

   return false;
}



private boolean isSystemJar(String path)
{
   String pfx = System.getProperty("java.home");
   if (path.startsWith(pfx)) return true;

   return false;
}



/********************************************************************************/
/*										*/
/*	Code to support setting up the output jar file				*/
/*										*/
/********************************************************************************/

private Manifest createManifest(Set<JcodeClass> clss)
{
   Manifest man = new Manifest();

   if (start_class != null) {
      Attributes attrs = man.getMainAttributes();
      attrs.put(Attributes.Name.MANIFEST_VERSION,"1.0");
      start_class.replace(".","/");
      attrs.put(Attributes.Name.MAIN_CLASS,start_class);
    }

   for (JcodeClass bc : clss) {
      Attributes attr = new Attributes();
      Date d = new Date(bc.getLastModified());
      attr.put(new Attributes.Name("Date"),d.toString());
      attr.put(new Attributes.Name("Source"),bc.getFilePath());
      man.getEntries().put(entryName(bc),attr);
    }

   if (for_applet) {
      Attributes attrs = man.getMainAttributes();
      attrs.put(new Attributes.Name("Permissions"),"all-permissions");
    }

   if (manifest_file != null) {
      Attributes attrs = man.getMainAttributes();
      try (BufferedReader br = new BufferedReader(new FileReader(manifest_file))) {
	 StringBuffer buf = new StringBuffer();
	 String key = null;
	 for ( ; ; ) {
	    String ln = br.readLine();
	    if (ln == null || !Character.isWhitespace(ln.charAt(0))) {
	       if (key != null) attrs.put(new Attributes.Name(key),buf.toString());
	       key = null;
	       buf = new StringBuffer();
	     }
	    if (ln == null) break;
	    if (Character.isWhitespace(ln.charAt(0))) buf.append(ln);
	    else {
	       int idx = ln.indexOf(":");
	       key = ln.substring(0,idx).trim();
	       buf.append(ln.substring(idx+1).trim());
	     }
	  }
       }
      catch (IOException e) {
	 System.err.println("TOBI: Problem reading manifest file: " + e);
       }
     }

   return man;
}




private String entryName(JcodeClass bc)
{
   String s = bc.getName().replace('.','/');
   s += ".class";

   return s;
}



private void installSource(JarOutputStream jarout,String src) throws IOException
{
   try {
      JarFile jf = new JarFile(src);
      installJar(jarout,src,jf);
    }
   catch (ZipException e) {
      installFile(jarout,src,new FileInputStream(src));
    }
}



private void installJar(JarOutputStream jarout,String src,JarFile jf) throws IOException
{
   for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements(); ) {
      JarEntry je = e.nextElement();
      if (je.getName().endsWith("pack.properties")) ;
      else if (je.getName().endsWith(".properties")) {
	 if (!have_props.contains(je.getName())) {
	    have_props.add(je.getName());
	    copyFile(jarout,je.getName(),jf.getInputStream(je));
	  }
       }
      else {
	 for (JcodeClass bc : output_classes) {
	    if (bc.getFilePath().equals(src)) {
	       String enm = entryName(bc);
	       if (je.getName().equals(enm)) {
		  copyFile(jarout,enm,jf.getInputStream(je));
		  break;
		}
	     }
	  }
       }
    }
}



private void installFile(JarOutputStream jarout,String src,FileInputStream fis) throws IOException
{
   for (JcodeClass bc : output_classes) {
      if (bc.getFilePath().equals(src)) copyFile(jarout,entryName(bc),fis);
    }
}



private void installResource(JarOutputStream jarout,Resource r) throws IOException
{
   FileInputStream fis = new FileInputStream(r.getFileName());
   copyFile(jarout,r.getJarName(),fis);
}



private void copyFile(JarOutputStream jarout,String nm,InputStream fis) throws IOException
{
   jarout.putNextEntry(new JarEntry(nm));

   byte [] buf = new byte[8192];
   for ( ; ; ) {
      int len = fis.read(buf);
      if (len < 0) break;
      jarout.write(buf,0,len);
    }

   fis.close();
}



/********************************************************************************/
/*										*/
/*	Resource subclass							*/
/*										*/
/********************************************************************************/

private static class Resource {

   private String file_path;
   private String use_path;

   Resource(String r) {
      int idx = r.indexOf(":");
      String dir = r.substring(0,idx);
      use_path = r.substring(idx+1);
      file_path = dir + File.separator + use_path;
      use_path = use_path.replace(File.separator,"/");
    }

   String getFileName() 			{ return file_path; }
   String getJarName()				{ return use_path; }

}	// end of subclass Resource




}	// end of class IvyImplBuilder




/* end of IvyImplBuilder.java */

