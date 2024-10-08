/********************************************************************************/
/*										*/
/*		JcodeClass.java 						*/
/*										*/
/*	Internal representation of a Java class 				*/
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

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;



public final class JcodeClass extends ClassNode implements JcodeConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Type	base_type;
private JcodeFactory bcode_factory;
private JcodeFileInfo from_file;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcodeClass(JcodeFactory bf,JcodeFileInfo fi,boolean proj)
{
   super(ASM_API);
   bcode_factory = bf;
   from_file = fi;
   base_type = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getName()
{
   if (base_type == null) return null;
   return base_type.getClassName();
}

public JcodeDataType getDataType()
{
   if (base_type == null) return null;
   return bcode_factory.findDataType(base_type);
}


JcodeDataType getDataType(Type t)
{
   return bcode_factory.findDataType(t);
}


JcodeFactory getFactory()			{ return bcode_factory; }


public String getJarFile()
{
   if (from_file == null) return null;
   File f = from_file.getJarFile();
   if (f == null) return null;
   return f.getPath();
}


public String getFilePath()
{
   if (from_file == null) return null;
   File f = from_file.getPath();
   if (f == null) return null;
   return f.getPath();
}



public long getLastModified()
{
   if (from_file == null) return 0;
   File f = from_file.getPath();
   if (f == null) return 0;
   return f.lastModified();
}


public String getPackageName()
{
   String full = base_type.getClassName();
   int idx = full.lastIndexOf(".");
   if (idx < 0) return "";
   return full.substring(0,idx);
}


public Collection<JcodeClass> getParents()
{
   List<JcodeClass> rslt = new ArrayList<JcodeClass>();
   if (superName != null) {
      JcodeClass jc = bcode_factory.findClass(superName);
      if (jc != null) rslt.add(jc);
    }
   for (Object sv : interfaces) {
      JcodeClass jc = bcode_factory.findClass(sv.toString());
      if (jc != null) rslt.add(jc);
    }
   return rslt;
}



public boolean isInterface()
{
   return Modifier.isInterface(access);
}

public boolean isAnnotation()
{
   return (access & Opcodes.ACC_ANNOTATION) != 0;
}

public boolean isEnum()
{
   return (access & Opcodes.ACC_ENUM) != 0;
}

public boolean isAbstract()
{
   return Modifier.isAbstract(access);
}

public boolean isStatic()
{
   return Modifier.isStatic(access);
}

public boolean isPublic()
{
   return Modifier.isPublic(access);
}

public int getModifiers()
{
   return access; 
}

public Collection<JcodeMethod> getMethods()
{
   List<JcodeMethod> rslt = new ArrayList<JcodeMethod>();
   for (Object md : methods) {
      rslt.add((JcodeMethod) md);
    }
   return rslt;
}




/********************************************************************************/
/*										*/
/*	Member access methods							*/
/*										*/
/********************************************************************************/

public JcodeMethod findMethod(String nm,String desc)
{ 
   String argdesc = desc;
   if (desc != null) {
      int idx = desc.lastIndexOf(")");
      if (idx > 0) argdesc = desc.substring(0,idx+1);
    }
   for (Object o : methods) {
      JcodeMethod bm = (JcodeMethod) o;
      if (bm.getName().equals(nm)) {
         if (desc == null) return bm;
         String bmd = bm.getDescription();
         if (bmd.equals(desc)) return bm;
         if (bmd.startsWith(argdesc)) return bm;
         String sgn = bm.getSignature();
         if (sgn != null) {
            int idx1 = sgn.indexOf("(");
            int idx2 = sgn.lastIndexOf(")");
            sgn = sgn.substring(idx1,idx2+1);
            if (sgn.equals(argdesc)) return bm;
          }
         if (bm.isPolymorphic()) return bm;
         // if (desc.endsWith(")") && bmd.startsWith(desc)) return bm;
       }
    }

   if (desc != null && desc.contains(",")) {
      return findMethod(nm,desc.replace(",",""));
    }

   return null;
}


public List<JcodeMethod> findAllMethods(String nm,String desc)
{
   List<JcodeMethod> rslt = new ArrayList<JcodeMethod>();
   for (Object o : methods) {
      JcodeMethod bm = (JcodeMethod) o;
      if (nm != null && !bm.getName().equals(nm)) continue;
      if (!compatibleWith(bm,desc)) continue;
      rslt.add(bm);
    }

   return rslt;
}



private boolean compatibleWith(JcodeMethod bm,String desc)
{
   if (desc == null) return true;
   if (desc.equals(bm.getDescription())) return true;
   if (bm.isVarArgs()) return true;
   try {
      Type [] typs0 = Type.getArgumentTypes(desc);
      int v0 = typs0.length;
      Type [] typs1 = Type.getArgumentTypes(bm.getDescription());
      int v1 = typs1.length;
      if (v0 == v1) return true;
    }
   catch (Throwable t) {
      return true;
    }
   
   return false;
}




public List<JcodeMethod> findStaticInitializers()
{
   List<JcodeMethod> rslt = new ArrayList<JcodeMethod>();
   for (Object o : methods) {
      JcodeMethod bm = (JcodeMethod) o;
      if (bm.getName().equals("<clinit>")) rslt.add(bm);
    }
   return rslt;
}


public JcodeField findField(String nm)
{
   for (Object o : fields) {
      JcodeField bf = (JcodeField) o;
      if (bf.getName().equals(nm)) return bf;
    }

   return null;
}



public JcodeField findInheritedField(String nm)
{
   JcodeField bf = findField(nm);
   if (bf != null) return bf;
   JcodeClass bc = bcode_factory.findClassNode(superName);
   if (bc != null) bf = bc.findInheritedField(nm);
   if (bf == null) {
      for (Object o : interfaces) {
	 bc = bcode_factory.findClassNode(o.toString());
	 if (bc != null) bf = bc.findInheritedField(nm);
	 if (bf != null) break;
       }
    }

   return bf;
}


public List<JcodeField> findAllFields(String nm)
{
   List<JcodeField> rslt = new ArrayList<>();
   for (Object o : fields) {
      JcodeField jf = (JcodeField) o;
      if (nm != null && !jf.getName().equals(nm)) continue;
      rslt.add(jf);
    }
   JcodeClass bc = bcode_factory.findClassNode(superName);
   if (bc != null) {
      List<JcodeField> pflds = bc.findAllFields(nm);
      if (pflds != null) rslt.addAll(pflds);
    }
   return rslt;
}



public JcodeMethod findInheritedMethod(String nm,String desc)
{
   JcodeMethod jm = findMethod(nm,desc);
   if (jm != null) return jm;
   JcodeClass bc = bcode_factory.findClassNode(superName);
   if (bc != null) jm = bc.findInheritedMethod(nm,desc);
   if (jm != null) return jm;
   for (String ifs : interfaces) {
      bc = bcode_factory.findClassNode(ifs);
      if (bc != null) jm = bc.findInheritedMethod(nm,desc);
      if (jm != null && !jm.isAbstract()) return jm; 
    }
   return null;
}



public Collection<JcodeMethod> findParentMethods(String nm,String desc,
						    boolean check,boolean first,
						    Collection<JcodeMethod> rslt)
{
   if (rslt == null) rslt = new HashSet<JcodeMethod>();

   if (first && !rslt.isEmpty()) return rslt;
   if (nm.equals("<init>")) return rslt;

   if (check) {
      JcodeMethod bm = findMethod(nm,desc);
      if (bm != null) {
	 rslt.add(bm);
	 if (first) return rslt;
       }
    }

   JcodeClass bc = bcode_factory.findClassNode(superName);
   if (bc != null) bc.findParentMethods(nm,desc,true,first,rslt);

   for (Object o : interfaces) {
      bc = bcode_factory.findClassNode(o.toString());
      if (bc != null) bc.findParentMethods(nm,desc,true,first,rslt);
    }

   return rslt;
}



public Collection<JcodeMethod> findChildMethods(String nm,String desc,boolean check,
					    Collection<JcodeMethod> rslt)
{
   if (rslt == null) rslt = new HashSet<>();
   int sz0 = rslt.size();
   
   if (check) {
      JcodeMethod bm = findMethod(nm,desc);
      if (bm != null) rslt.add(bm);
    }

   JcodeDataType xdt = getDataType();
   Collection<JcodeDataType> ctyps = xdt.getChildTypes();
   if (ctyps != null) {
      for (JcodeDataType dt : ctyps) {
         JcodeClass bc = bcode_factory.findClassNode(dt.getDescriptor());
         if (bc != null) bc.findChildMethods(nm,desc,true,rslt);
       }
    }
   
   if (check && rslt.size() == sz0) {
      JcodeClass sc = this;
      while (sc.superName != null) {
         sc = bcode_factory.findClassNode(sc.superName);
         if (sc == null) break;
         JcodeMethod bm = sc.findMethod(nm,desc);
         if (bm != null) {
            rslt.add(bm);
            break;
          }
       }
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Visit methods								*/
/*										*/
/********************************************************************************/

@Override public void visit(int v,int a,String cname,String sgn,
      String sup,String [] ifaces)
{
   super.visit(v,a,cname,sgn,sup,ifaces);
   base_type = Type.getObjectType(cname);
   if (sup != null) bcode_factory.noteClass(sup);
   if (ifaces != null)
      for (String f : ifaces) {
         bcode_factory.noteClass(f);
       }
}


@Override public void visitEnd()
{
   JcodeDataType bdt = bcode_factory.findClassType(name);
   bdt.noteSuperType(superName);
   bdt.noteInterfaces(interfaces);
   bdt.noteModifiers(access);
}


@Override public MethodVisitor visitMethod(int a,String n,String d,String sgn,String [] ex)
{
   if (ex != null) {
      for (String s : ex) bcode_factory.noteClass(s);
    }

   for (Type t : Type.getArgumentTypes(d)) bcode_factory.noteType(t.getDescriptor());
   bcode_factory.noteType(Type.getReturnType(d).getDescriptor());
   JcodeMethod bm = new JcodeMethod(bcode_factory,this,a,n,d,sgn,ex);
   methods.add(bm);
   return bm;
}


@Override public FieldVisitor visitField(int a,String n,String d,String sgn,Object val)
{
   bcode_factory.noteType(d);
   JcodeField bf = new JcodeField(this,a,n,d,sgn,val);
   fields.add(bf);
   return bf;
}




@Override public void visitInnerClass(String cname,String oname,String iname,int acc)
{
   bcode_factory.noteClass(cname);
   super.visitInnerClass(cname,oname,iname,acc);
   if (cname.equals(this.name)) {
      access |= acc;
    }
}

@Override public void visitOuterClass(String owner,String cname,String desc)
{
   super.visitOuterClass(owner,cname,desc);
}




/********************************************************************************/
/*                                                                              */
/*      Debugging methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public String toString()
{
   return getName();
}




}	// end of class JcodeClass



/* end of JcodeClass.java */
