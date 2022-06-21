/********************************************************************************/
/*										*/
/*		JcodeFactory.java						*/
/*										*/
/*	Byte code definitions factory						*/
/*										*/
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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import edu.brown.cs.ivy.file.IvyLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class JcodeFactory implements JcodeConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<String,JcodeFileInfo> class_map;
private Map<String,JcodeClass>	known_classes;
private Map<String,JcodeMethod> known_methods;
private Map<String,JcodeField>	known_fields;
private Queue<String>		work_list;
private LoadExecutor		work_holder;
private Set<String>		path_set;
private List<String>		user_paths;
private Map<Type,JcodeDataType> static_map;
private Map<String,JcodeDataType> name_map;
private Map<String,JcodeDataType> jname_map;

private int			num_threads;

private static int		thread_counter = 0;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public JcodeFactory()
{
   this(Math.max(1,Runtime.getRuntime().availableProcessors()/2));
}


public JcodeFactory(int nth)
{
   class_map = new HashMap<>();
   known_classes = new HashMap<>();
   known_methods = new HashMap<>();
   known_fields = new HashMap<>();
   static_map = new ConcurrentHashMap<>();
   name_map = new ConcurrentHashMap<>();
   jname_map = new HashMap<>();
   path_set = new HashSet<>();
   user_paths = new ArrayList<>();

   work_list = new LinkedList<>();
   num_threads = nth;

   setupClassPath();

   definePrimitive("Z","boolean",Type.BOOLEAN_TYPE);
   definePrimitive("B","byte",Type.BYTE_TYPE);
   definePrimitive("C","char",Type.CHAR_TYPE);
   definePrimitive("D","double",Type.DOUBLE_TYPE);
   definePrimitive("F","float",Type.FLOAT_TYPE);
   definePrimitive("I","int",Type.INT_TYPE);
   definePrimitive("L","long",Type.LONG_TYPE);
   definePrimitive("S","short",Type.SHORT_TYPE);
   definePrimitive("V","void",Type.VOID_TYPE);
}


public void shutDown()
{
   if (work_holder != null) {
      work_holder.shutdownNow();
      work_holder.reset();
      work_holder = null;
    }
   class_map = null;
   known_classes = null;
   known_methods = null;
   known_fields = null;
   work_list = null;
   path_set = null;
   static_map = null;
   name_map = null;
   jname_map = null;
}



private void definePrimitive(String j,String n,Type t)
{
   JcodeDataType jdt = new JcodeDataType(t,this);
   if (j != null) jname_map.put(j,jdt);
   if (n != null) name_map.put(n,jdt);
}




/********************************************************************************/
/*										*/
/*	Class setup methods							*/
/*										*/
/********************************************************************************/

public void addToClassPath(String classpath)
{
   addUserClassPath(classpath);
}


public void load()
{
   loadClasses();
}




/********************************************************************************/
/*										*/
/*	External type access methods						*/
/*										*/
/********************************************************************************/

public JcodeClass findClass(String name)
{
   if (name == null) return null;

   JcodeClass jdt = known_classes.get(name);
   if (jdt != null) return jdt;
   for (String anm = name; anm.contains("."); ) {
      int idx = anm.lastIndexOf(".");
      anm = anm.substring(0,idx) + "$" +  anm.substring(idx+1);
      jdt = known_classes.get(anm);
      if (jdt != null) return jdt;
    }

   synchronized (this) {
      work_list.add(name);
    }
   loadClasses();

   return known_classes.get(name);
}




public JcodeClass findKnownClass(String name)
{
   if (name == null) return null;
   String ldname = null;

   JcodeClass jdt = known_classes.get(name);
   if (jdt != null) return jdt;
   if (class_map.containsKey(name)) ldname = name;

   for (String anm = name; anm.contains("."); ) {
      int idx = anm.lastIndexOf(".");
      anm = anm.substring(0,idx) + "$" +  anm.substring(idx+1);
      jdt = known_classes.get(anm);
      if (jdt != null) return jdt;
      if (ldname == null && class_map.containsKey(anm)) ldname = anm;
    }

   if (ldname == null) return null;

   synchronized (this) {
      work_list.add(ldname);
    }
   loadClasses();

   jdt = known_classes.get(name);
   if (jdt != null && jdt.getName() == null) {
      synchronized (this) {
         int ct = 0;
         while (jdt.getName() == null) {
            IvyLog.logD("JCODE","Waiting for class " + name);
            try {
               wait(50);
             }    
            catch (InterruptedException e) { }
            if (++ct > 100) {
               // waited too long == must be some other problem
               IvyLog.logX("JCODE","Waiting too long for class");
               return jdt;
             }
          }
       }
    }
   
   return jdt;
}




public Collection<JcodeClass> getAllClasses()
{
   HashSet<JcodeClass> rslt = new HashSet<>(known_classes.values());
   return rslt;
}


public Collection<JcodeClass> getAllPossibleClasses(String filter)
{
   HashSet<JcodeClass> rslt = new HashSet<>();
   for (String s : class_map.keySet()) {
      if (filter != null && !s.startsWith(filter)) continue;
      JcodeClass jc = findKnownClass(s);
      if (jc != null) rslt.add(jc);
    }
   return rslt;
}



public Collection<JcodeClass> getAllPossibleClasses(Predicate<String> filter)
{
   HashSet<JcodeClass> rslt = new HashSet<>();
   for (String s : class_map.keySet()) {
      if (!filter.test(s)) continue;
      JcodeClass jc = findKnownClass(s);
      if (jc != null) rslt.add(jc);
    }
   return rslt;
}



/********************************************************************************/
/*										*/
/*	Class path methods							*/
/*										*/
/********************************************************************************/

public List<String> getUserClassPath()
{
   return user_paths;
}



private void addUserClassPath(String cp)
{
   if (cp == null) return;

   StringTokenizer tok = new StringTokenizer(cp,File.pathSeparator);
   while (tok.hasMoreTokens()) {
      String cpe = tok.nextToken();
      if (addClassPathEntry(cpe)) user_paths.add(cpe);
    }
}



private void setupClassPath()
{
   String jh = System.getProperty("edu.brown.cs.ivy.jcode.home");
   if (jh == null) jh = System.getProperty("java.home");

   File jf = new File(jh);
   File jf1 = new File(jf,"lib");
   File jf3 = new File(jf,"jre");
   File jf4 = new File(jf,"jmods");
   File jf5 = new File(jf3,"lib");
   File jf6 = new File(jf3,"jmods");

   if (jf3.exists()) {
      if (jf5.exists()) jf1 = jf5;
    }
   if (!jf4.exists()) {
      if (jf6.exists()) jf4 = jf6;
    }

   File jf7 = new File(jf1,"rt.jar");

   addJavaJars(jf1);

   if (jf4.exists()) {
      addJavaJars(jf4);
    }
   else if (!jf7.exists()) {
      addSystemModules();
    }
}



private void addJavaJars(File f)
{
   if (!f.isDirectory()) return;
   if (f.getName().equals("missioncontrol")) return;
   for (File f1 : f.listFiles()) {
      if (f1.isDirectory()) addJavaJars(f1);
      else if (f1.getPath().endsWith(".jar")) addClassPathEntry(f1.getPath());
      else if (f1.getPath().endsWith(".jmod")) addClassPathModule(f1.getPath());
   }
}



private boolean addClassPathEntry(String cpe)
{
   if (!path_set.add(cpe)) return false;

   File f = new File(cpe);
   if (!f.exists()) return false;
   if (f.isDirectory()) {
      addClassFiles(f,null);
    }
   else {
      try {
	 FileInputStream fis = new FileInputStream(f);
	 JarInputStream jis = new JarInputStream(fis);
	 try {
	    for ( ; ; ) {
	       JarEntry je = jis.getNextJarEntry();
	       if (je == null) break;
	       String cn = je.getName();
	       String en = cn;
	       if (cn.endsWith(".class")) en = cn.substring(0,cn.length()-6);
	       else continue;
	       en = en.replace("/",".");
	       if (!class_map.containsKey(en)) {
		  int sz = (int) je.getSize();
		  byte [] buf = null;
		  if (sz > 0) {
		     buf = new byte[sz];
		     int ln = 0;
		     while (ln < sz) {
			int ct = jis.read(buf,ln,sz-ln);
			ln += ct;
		      }
		   }
		  JcodeFileInfo fi = new JcodeFileInfo(f,cn,buf);
		  class_map.put(en,fi);
		}
	     }
	  }
	 finally {
	    jis.close();
	  }
       }
      catch (IOException e) { }
    }

   return true;
}



private void addClassPathModule(String cpe)
{
// IvyLog.logD("JCODE","Add class path entry " + cpe);

   try {
      File f = new File(cpe);
      ZipFile zf = new ZipFile(f);
      for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements(); ) {
	 ZipEntry ze = e.nextElement();
	 String cn = ze.getName();
	 String en = cn;
	 if (cn.endsWith(".class")) en = cn.substring(0,cn.length()-6);
	 else continue;
	 if (cn.contains("module-info")) continue;
	 if (!en.startsWith("classes/")) continue;
	 en = en.substring(8);
	 en = en.replace("/",".");
	 if (!class_map.containsKey(en)) {
	    InputStream jis = zf.getInputStream(ze);
	    int sz = (int) ze.getSize();
	    byte [] buf = null;
	    if (sz > 0) {
	       buf = new byte[sz];
	       int ln = 0;
	       while (ln < sz) {
		  int ct = jis.read(buf,ln,sz-ln);
		  ln += ct;
		}
	     }
	    JcodeFileInfo fi = new JcodeFileInfo(f,cn,buf);
	    class_map.put(en,fi);
	  }
       }
      zf.close();
    }
   catch (IOException e) { }
}


private void addSystemModules()
{
   ModuleFinder mfndr = ModuleFinder.ofSystem();
   for (ModuleReference mr : mfndr.findAll()) {
      try {
	 ModuleReader mrdr = mr.open();
	 mrdr.list().forEach(s -> addSystemClass(mr,mrdr,s));
       }
      catch (IOException e) {
	 System.err.println("Can open " + mr);
       }
    }
}


private void addSystemClass(ModuleReference mr,ModuleReader mrdr,String id)
{
   try {
      String en = id;
      if (id.endsWith(".class")) en = id.substring(0,id.length()-6);
      en = en.replace("/",".");
      if (!class_map.containsKey(en)) {
	 ByteBuffer bb = mrdr.read(id).get();
	 int len = bb.limit() - bb.position();
	 byte [] buf = new byte[len];
	 bb.get(buf);
	 JcodeFileInfo fi = new JcodeFileInfo(null,id,buf);
	 class_map.put(en,fi);
       }
    }
   catch (Exception e) {
      System.err.println("JCODE: problem processing system module " + mr + " " + id +
	    ": " + e);
    }
}


private void addClassFiles(File dir,String pfx)
{
   if (dir == null || !dir.isDirectory() || dir.listFiles() == null) {
      return;
    }

   for (File f : dir.listFiles()) {
      if (f.isDirectory()) {
	 String sfx = f.getName();
	 if (pfx != null) sfx = pfx + "." + sfx;
	 addClassFiles(f,sfx);
       }
      else if (f.getName().endsWith(".class")) {
	 String sfx = f.getName();
	 int idx = sfx.lastIndexOf(".");
	 sfx = sfx.substring(0,idx);
	 if (pfx != null) sfx = pfx + "." + sfx;
	 if (!class_map.containsKey(sfx)) {
	    JcodeFileInfo fi = new JcodeFileInfo(f);
	    class_map.put(sfx,fi);
	  }
       }
    }
}



/********************************************************************************/
/*										*/
/*	Type access methods							*/
/*										*/
/********************************************************************************/

public JcodeDataType findNamedType(String s)
{
   JcodeDataType bdt = null;

   bdt = name_map.get(s);
   if (bdt != null) return bdt;
   if (s.endsWith("[]")) {
      s = s.substring(0,s.length()-2);
      bdt = findNamedType(s);
      bdt = bdt.getArrayType();
    }
   else {
      Type t = Type.getObjectType(s);
      if (t != null) bdt = createDataType(t);
    }

   return bdt;
}


public JcodeDataType findJavaType(String s)
{
   JcodeDataType bdt = null;

   bdt = jname_map.get(s);
   if (bdt != null) return bdt;
   Type t = Type.getType(s);
   if (t != null) bdt = createDataType(t);

   return bdt;
}



JcodeDataType findClassType(String s)
{
   if (!s.endsWith(";") && !s.startsWith("[")) {
      s = "L" + s.replace('.','/') + ";";
    }

   return findJavaType(s);
}


JcodeDataType findDataType(Type t)
{
   JcodeDataType bdt = null;

   if (t == null) return null;

   bdt = static_map.get(t);
   if (bdt == null) {
      bdt = createDataType(t);
    }

   return bdt;
}



private JcodeDataType createDataType(Type t)
{
   JcodeDataType bdt = new JcodeDataType(t,this);
   JcodeDataType obdt = static_map.putIfAbsent(t,bdt);
   if (obdt != null) return obdt;
   if (t.getSort() != Type.METHOD && t.getClassName() != null) {
      name_map.put(t.getClassName(),bdt);
    }
   return bdt;
}



JcodeClass findClassNode(String nm)
{
   if (nm == null) return null;

   return known_classes.get(nm);
}



/********************************************************************************/
/*										*/
/*	Member access methods							*/
/*										*/
/********************************************************************************/

public JcodeMethod findMethod(String nm,String cls,String mnm,String desc)
{
   if (nm == null) {
      if (desc != null) nm = cls + "." + mnm + desc;
      else nm = cls + "." + mnm;
    }

   JcodeMethod bm = known_methods.get(nm);
   if (bm != null) return bm;

   synchronized (known_methods) {
      bm = known_methods.get(nm);
      if (bm == null) {
	 if (cls == null) {
	    int idx0 = nm.indexOf("(");
	    int idx1 = 0;
	    if (idx0 < 0) idx1 = nm.lastIndexOf(".");
	    else idx1 = nm.lastIndexOf(".",idx0);
	    cls = nm.substring(0,idx1);
	    if (idx0 >= 0) {
	       mnm = nm.substring(idx1+1,idx0);
	       desc = nm.substring(idx0);
	     }
	    else {
	       mnm = nm.substring(idx1+1);
	       desc = null;
	     }
	  }
	 JcodeClass bc = known_classes.get(cls);
	 if (bc == null) return null;
	 bm = bc.findMethod(mnm,desc);
	 known_methods.put(nm,bm);
       }
      return bm;
    }
}


public Collection<JcodeMethod> findAllMethods(JcodeDataType cls,String mnm,String desc)
{
   JcodeClass fc = known_classes.get(cls.getName());
   if (fc == null) return null;
   return fc.findAllMethods(mnm,desc);
}




public JcodeMethod findInheritedMethod(String cls,String nm,String desc)
{
   JcodeClass bc = known_classes.get(cls);
   if (bc == null) return null;

   List<JcodeMethod> rslt = new ArrayList<>();
   bc.findParentMethods(nm,desc,true,true,rslt);

   if (rslt.isEmpty()) return null;

   return rslt.get(0);
}





public List<JcodeMethod> findStaticInitializers(String cls)
{
   synchronized (known_methods) {
      JcodeClass bc = known_classes.get(cls);
      if (bc == null) return null;
      return bc.findStaticInitializers();
    }
}

public JcodeField findField(String nm,String cls,String fnm)
{
   if (nm == null) nm = cls + "." + fnm;

   synchronized (known_fields) {
      JcodeField bf = known_fields.get(nm);
      if (bf == null) {
	 if (cls == null) {
	    int idx = nm.lastIndexOf(".");
	    cls = nm.substring(0,idx);
	    fnm = nm.substring(idx+1);
	  }
	 JcodeClass bc = findClass(cls);
	 if (bc != null) bf = bc.findField(fnm);
	 if (bf != null) known_fields.put(nm,bf);
       }
      return bf;
    }
}



public JcodeField findInheritedField(String cls,String fnm)
{
   JcodeClass bc = known_classes.get(cls);
   if (bc == null) return null;

   return bc.findInheritedField(fnm);
}


public List<JcodeField> findAllFields(JcodeDataType cls,String fnm)
{
   JcodeClass fc = known_classes.get(cls.getName());
   if (fc == null) return null;
   return fc.findAllFields(fnm);
}




/********************************************************************************/
/*										*/
/*	Class loading methods							*/
/*										*/
/********************************************************************************/

private synchronized void loadClasses()
{
   if (num_threads == 0) {
      while (!work_list.isEmpty()) {
	 String name = work_list.remove();
	 LoadTask lt = new LoadTask(name);
	 lt.run();
       }
    }
   else if (work_list.isEmpty()) return;
   else {
      if (work_holder == null) {
	 work_holder = new LoadExecutor(num_threads);
       }
      for (String s : work_list) work_holder.workOnClass(s);
      work_list.clear();
      synchronized (work_holder) {
	 while (!work_holder.isDone()) {
	    try {
	       work_holder.wait(10000);
	     }
	    catch (InterruptedException e) { }
	  }
	 work_holder.reset();
       }
      // work_holder.shutdown();
      // work_holder = null;
    }
}




private class LoadExecutor extends ThreadPoolExecutor implements ThreadFactory {

   private int num_active;
   private boolean work_pending;
   private ConcurrentMap<String,Object> work_items;


   LoadExecutor(int nth) {
      super(nth,nth,10,TimeUnit.SECONDS,new LinkedBlockingQueue<>());
      num_active = 0;
      work_pending = true;
      work_items = new ConcurrentHashMap<>();
      setThreadFactory(this);
    }

   void workOnClass(String c) {
      if (work_items.putIfAbsent(c,Boolean.TRUE) != null) return;
   
      LoadTask task = new LoadTask(c);
      execute(task);
    }


   @Override synchronized protected void beforeExecute(Thread t,Runnable r) {
      ++num_active;
      work_pending = false;
    }

   @Override synchronized protected void afterExecute(Runnable r,Throwable t) {
      --num_active;
      if (num_active == 0 && getQueue().size() == 0) {
	 notifyAll();
       }
    }

   void reset() {
      work_items.clear();
      work_pending = true;
    }

   synchronized boolean isDone() {
      return !work_pending && num_active == 0 && getQueue().size() == 0;
    }

   @Override public Thread newThread(Runnable r) {
      Thread t= new Thread(r,"JCODE_" + (++thread_counter));
      t.setDaemon(true);
      return t;
    }

}	// end of class LoadExecutor




private class LoadTask implements Runnable {

   private String load_class;

   LoadTask(String c) {
      load_class = c;
    }

   @Override public void run() {
      JcodeFileInfo fi = class_map.get(load_class);
      String altname = load_class;
      if (fi == null) {
         while (altname.contains(".")) {
            int idx = altname.lastIndexOf(".");
            altname = altname.substring(0,idx) + "$" + altname.substring(idx+1);
            fi = class_map.get(altname);
            if (fi != null) break;
          }
       }
   
      if (fi == null) {
         // System.err.println("JCODE: Can't find class " + load_class);
         return;
       }
      try {
         JcodeClass bc = null;
         synchronized (known_classes) {
            if (known_classes.get(load_class) == null) {
               bc = new JcodeClass(JcodeFactory.this,fi,true);
               known_classes.put(load_class,bc);
               String c1 = load_class.replace('.','/');
               known_classes.put(c1,bc);
               String c2 = "L" + c1 + ";";
               known_classes.put(c2,bc);
               if (c1.contains("$")) {
                  String c3 = c1.replace('$','.');
                  known_classes.put(c3,bc);
                  String c4 = load_class.replace('$','.');
                  known_classes.put(c4,bc);
                }
             }
          }
         
         if (bc != null) {
            // System.err.println("JCODE: Load class " + load_class);
            InputStream ins = fi.getInputStream();
            if (ins == null) {
               System.err.println("JCODE: Can't open file for class " + load_class);
             }
            else {
               ClassReader cr = new ClassReader(ins)	  ;
               cr.accept(bc,0);
               ins.close();
             }
          }
       }
      catch (IOException e) {
         System.err.println("JCODE: Problem reading class " + load_class);
       }
   }

}	// end of inner class LoadTask




void noteType(String desc)
{
   if (desc.startsWith("L") && desc.endsWith(";")) {
      String nm = desc.substring(1,desc.length()-1);
      nm = nm.replace('/','.');
      noteClass(nm);
    }
   else if (desc.startsWith("[")) {
      noteType(desc.substring(1));
    }
   else if (desc.startsWith("(")) {
      for (Type t : Type.getArgumentTypes(desc)) {
	 switch (t.getSort()) {
	    case Type.ARRAY :
	    case Type.OBJECT :
	       noteType(t.getDescriptor());
	       break;
	  }
       }
    }
   else if (desc.length() > 1) {
      System.err.println("JCODE: Type for load not found: '" + desc + "'");
    }
}




void noteClass(String nm)
{
   nm = nm.replace("/",".");
   if (nm.startsWith("[")) {
      noteType(nm);
      return;
    }

   if (work_holder != null) {
      work_holder.workOnClass(nm);
    }
   else {
      if (known_classes.containsKey(nm)) return;
      work_list.add(nm);
      known_classes.put(nm,null);
    }
}








}	// end of class JcodeFactory




/* end of JcodeFactory.java */
