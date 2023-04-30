/********************************************************************************/
/*										*/
/*		JcompContextAst.java						*/
/*										*/
/*	Class to handle represent a Java user context based on ASM		*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2007, Brown University, Providence, RI.				 *
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


package edu.brown.cs.ivy.jcomp;

import edu.brown.cs.ivy.exec.IvyExecQuery;
import edu.brown.cs.ivy.jcode.JcodeConstants;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


class JcompContextAsm extends JcompContext implements JcompConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/**************************\*****************************************************/

private Map<String,AsmClass>	known_types;
private List<ClassPathEntry>	base_files;
private List<String>		class_path;
private JcompContextAsm 	my_parent;
private Map<AsmClass,Set<JcompScope>> all_defined;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompContextAsm(String javahome)
{
   this((JcompContext) null);

   computeBasePath(javahome);
}


JcompContextAsm(JcompContext par,String jarname)
{
  this(par);

  try {
     addUserClassPathEntry(jarname);
   }
  catch (IOException e) {
     System.err.println("JCOMP: can't open file " + jarname + ": " + e);
     e.printStackTrace();
     return;
   }
}


JcompContextAsm(JcompContext par,Iterable<String> jarnames)
{
   this(par);

   for (String jarname : jarnames) {
      try {
	 addUserClassPathEntry(jarname);
       }
      catch (IOException e) {
	 System.err.println("JCOMP: can't open file " + jarname + ": " + e);
	 e.printStackTrace();
	 return;
       }
    }
}



private JcompContextAsm(JcompContext par)
{
   super(par);
   my_parent = null;
   if (par != null && par instanceof JcompContextAsm) my_parent = (JcompContextAsm) par;

   known_types = new HashMap<>();
   base_files = new ArrayList<>();
   class_path = new ArrayList<>();
   all_defined = new HashMap<>();
}




/********************************************************************************/
/*										*/
/*	Language-specific context methods					*/
/*										*/
/********************************************************************************/

JcompType defineKnownType(JcompTyper typer,String name)
{
   if (name == null) return null;

   AsmClass ac = findKnownType(typer,name);

   if (ac == null) {
      if (parent_context != null) {
	 return parent_context.defineKnownType(typer,name);
       }
      // define undefined type here
      return null;
    }

   JcompType jt = ac.getJcompType(typer);

   return jt;
}




JcompSymbol defineKnownField(JcompTyper typer,String cls,String id,JcompType orig)
{
   AsmClass ac = findKnownType(typer,cls);
   if (ac == null) return null;

   AsmField af = ac.findField(typer,id);
   if (af == null) return null;

   return af.createField(typer);
}



JcompSymbol defineKnownMethod(JcompTyper typer,String cls,String id,JcompType argtype,JcompType ctyp)
{
   AsmClass ac = findKnownType(typer,cls);
   if (ac == null) return null;

   List<AsmMethod> aml = ac.findMethods(typer,id,argtype,ctyp);
   if (aml == null || aml.size() == 0) return null;

   AsmMethod best = null;
   int bestscore = 0;
   for (AsmMethod am1 : aml) {
      int score = am1.isCompatibleWith(typer,argtype);
      if (score < 0) continue;
      if (best == null || score < bestscore) {
	 best = am1;
	 bestscore = score;
       }
    }

   if (best == null) return null;

   String cnm = best.for_class.getJavaName();
   JcompType kty = typer.findType(cnm);

   if (kty != null && !kty.isBinaryType()) {
      JcompSymbol js1 = kty.lookupMethod(typer,id,argtype,ctyp,null);
      if (js1 != null) return js1;
    }

   return best.createMethod(typer,argtype,ctyp);
}


List<JcompSymbol> defineKnownStatics(JcompTyper typer,String cls,String id,JcompType ctyp)
{
   AsmClass ac = findKnownType(typer,cls);
   if (ac == null) return null;

   List<JcompSymbol> rslt = new ArrayList<>();

   List<AsmMethod> aml = ac.findMethods(typer,id,null,ctyp);
   if (aml != null) {
      for (AsmMethod am : aml) {
	 JcompSymbol js = am.createMethod(typer,null,ctyp);
	 if (js.isStatic())
	    rslt.add(js);
       }
    }

   List<AsmField> afl = ac.findFields(typer,id);
   if (afl != null) {
      for (AsmField af : afl) {
	 JcompSymbol js = af.createField(typer);
	 if (js.isStatic()) rslt.add(js);
       }
    }

   if (rslt.isEmpty()) return null;

   return rslt;
}



Set<JcompSymbol> defineKnownAbstracts(JcompTyper typer,String cls)
{
   AsmClass ac = findKnownType(typer,cls);
   if (ac == null) return null;

   List<AsmMethod> aml = ac.findMethods(typer,null,null,null);

   Set<JcompSymbol> rslt = new HashSet<JcompSymbol>();
   if (aml != null) {
      for (AsmMethod am : aml) {
	 JcompSymbol js = am.createMethod(typer,null,null);
	 if (js.isAbstract()) rslt.add(js);
       }
    }

   return rslt;
}


List<JcompSymbol> findKnownMethods(JcompTyper typer,String cls)
{
   AsmClass ac = findKnownType(typer,cls);
   if (ac == null) return null;
   List<JcompSymbol> rslt = new ArrayList<JcompSymbol>();
   for (AsmMethod am : ac.getMethods()) {
      JcompSymbol js = am.createMethod(typer,null,null);
      if (js == null) return null;
      rslt.add(js);
    }
   return rslt;
}



void defineAll(JcompTyper typer,String cls,JcompScope scp)
{
   if (scp == null) return;
   AsmClass ac = findKnownType(typer,cls);
   if (ac == null) return;
   Set<JcompScope> defd = all_defined.get(ac);
   if (defd == null) {
      defd = new HashSet<>();
      all_defined.put(ac,defd);
    }
   if (!defd.add(scp)) return;

   ac.defineAll(typer,scp);
}


List<String> getClassPath()
{
   List<String> rslt;
   if (parent_context != null) {
      rslt = parent_context.getClassPath();
    }
   else rslt = new ArrayList<>();

   rslt.addAll(class_path);
   return rslt;
}



/********************************************************************************/
/*										*/
/*	Type definition code							*/
/*										*/
/********************************************************************************/

private AsmClass findKnownType(JcompTyper typer,String name)
{
   AsmClass ac = known_types.get(name);
   if (ac != null) return ac;
   ac = checkKnownType(name);
   if (ac != null) {
      known_types.put(ac.getInternalName(),ac);
      known_types.put(ac.getJavaName(),ac);
      known_types.put(ac.getAccessName(),ac);
      return ac;
    }

   synchronized (this) {
      if (known_types.containsKey(name)) ac = known_types.get(name);
      else {
	 ac = findKnownClassType(name);
	 if (ac == null) {
	    int idx1 = name.lastIndexOf('$');
	    int idx = name.lastIndexOf('.');
	    if (idx1 < 0 && idx >= 0) {
	       String newnm = name.substring(0,idx) + "$" + name.substring(idx+1);
	       ac = findKnownType(typer,newnm);
	     }
	  }
	 if (ac == null && my_parent != null) {
	    ac = my_parent.findKnownType(typer,name);
	  }
	 known_types.put(name,ac);
	 if (ac != null) {
	    known_types.put(ac.getInternalName(),ac);
	    known_types.put(ac.getJavaName(),ac);
	    known_types.put(ac.getAccessName(),ac);
	  }
       }
    }

   return ac;
}


private AsmClass checkKnownType(String name)
{
   AsmClass ac = known_types.get(name);
   if (ac != null) return ac;
   if (my_parent == null) return null;
   return my_parent.checkKnownType(name);
}





private AsmClass findKnownClassType(String name)
{
   String fnm = name.replace('.','/') + ".class";
   InputStream ins = getInputStream(fnm);
   if (ins == null) return null;

   KnownClassVisitor kcv = new KnownClassVisitor();

   try {
      ClassReader cr = new ClassReader(ins);
      cr.accept(kcv,ClassReader.SKIP_CODE);
    }
   catch (IOException e) {
      System.err.println("JCOMP: CONTEXT: Problem reading class file: " + e);
    }
   catch (IllegalArgumentException e) {
      System.err.println("JCOMP: CONTEXT: Problem loading class file: " + e);
    }
   finally {
      try {
	 ins.close();
       }
      catch (IOException e) { }
    }

   return kcv.getAsmClass();
}



private class KnownClassVisitor extends ClassVisitor {

   private AsmClass asm_data;

   KnownClassVisitor() {
      super(JcodeConstants.ASM_API);
      asm_data = null;
    }

   AsmClass getAsmClass()		{ return asm_data; }

   @Override public void visit(int version,int access,String name,String sign,String sup,String [] ifcs) {
      asm_data = new AsmClass(name,access,sign,sup,ifcs);
    }

   @Override public AnnotationVisitor visitAnnotation(String dsc,boolean vis)	{ return null; }
   @Override public void visitAttribute(Attribute attr) 			{ }
   @Override public void visitEnd()						{ }
   @Override public void visitInnerClass(String n,String o,String i,int acc)	{ }
   @Override public void visitOuterClass(String own,String nam,String d)	{ }
   @Override public void visitSource(String src,String dbg)			{ }

   @Override public FieldVisitor visitField(int access,String name,String desc,String sign,Object val) {
      asm_data.addField(name,access,sign,desc);
      return null;
    }

   @Override public MethodVisitor visitMethod(int access,String name,String desc,String sign,String [] excs) {
      asm_data.addMethod(name,access,sign,desc,excs);
      return null;
    }


}	// end of class KnownClassVisitor





/********************************************************************************/
/*										*/
/*	Information about a system class					*/
/*										*/
/********************************************************************************/

private class AsmClass {

   private String class_name;
   private int access_info;
   private String generic_signature;
   private String super_name;
   private String [] iface_names;
   private List<AsmField> field_data;
   private List<AsmMethod> method_data;
   private Map<JcompTyper,JcompType> base_types;
   private Set<JcompScope> local_defined;
   private boolean nested_this;

   AsmClass(String nm,int acc,String sgn,String sup,String [] ifc) {
      class_name = nm;
      access_info = acc & ~Opcodes.ACC_SUPER;
      if (nm.contains("$")) access_info |= Opcodes.ACC_STATIC;
      generic_signature = sgn;
      super_name = sup;
      iface_names = ifc;
      base_types = new WeakHashMap<>();
      field_data = new ArrayList<AsmField>();
      method_data = new ArrayList<AsmMethod>();
      local_defined = null;
      nested_this = false;
    }

   synchronized JcompType getJcompType(JcompTyper typer) {
      JcompType btyp = base_types.get(typer);
      if (btyp == null) {
	 String jnm = class_name.replace('/','.');
	 jnm = jnm.replace('$','.');
	 if ((access_info & Opcodes.ACC_INTERFACE) != 0) {
	    btyp = JcompType.createBinaryInterfaceType(jnm,generic_signature);
	  }
	 else if ((access_info & Opcodes.ACC_ANNOTATION) != 0) {
	    btyp = JcompType.createBinaryAnnotationType(jnm,generic_signature);
	    if ((access_info & Opcodes.ACC_ABSTRACT) != 0) {
	       btyp.setAbstract(true);
	     }
	  }
	 else if ((access_info & Opcodes.ACC_ENUM) != 0) {
	    btyp = JcompType.createBinaryEnumType(jnm,generic_signature);
	    if ((access_info & Opcodes.ACC_ABSTRACT) != 0) {
	       btyp.setAbstract(true);
	     }
	  }
	 else {
	    btyp = JcompType.createBinaryClassType(jnm,generic_signature);
	    if ((access_info & Opcodes.ACC_ABSTRACT) != 0) {
	       btyp.setAbstract(true);
	     }
	  }
	 int idx = class_name.lastIndexOf("/");
	 if (idx < 0) idx = 0;
	 int idx1 = class_name.indexOf("$",idx);
	 if (idx1 > 0) {
	    String ojtnm = class_name.substring(0,idx1);
	    JcompType oty = getAsmTypeName(typer,ojtnm);
	    if (oty != null) btyp.setOuterType(oty);
	    if (idx1 > 0 && nested_this &&
		  oty != null && !oty.isInterfaceType()) {
	       btyp.setInnerNonStatic(true);
	     }
	  }

	 btyp.setContextType(false);
	 if (super_name != null) {
	    JcompType sjt = getAsmTypeName(typer,super_name);
	    if (sjt == null) {
	       System.err.println("SUPER TYPE IS UNKNOWN: " + super_name);
	     }
	    if (sjt != null) btyp.setSuperType(sjt);
	  }
	 if (iface_names != null) {
	    for (String inm : iface_names) {
	       JcompType ijt = getAsmTypeName(typer,inm);
	       if (ijt != null) btyp.addInterface(ijt);
	     }
	  }
	 btyp.setDefinition(JcompSymbol.createSymbol(btyp,access_info));
       }
      btyp = typer.fixJavaType(btyp);
      base_types.put(typer,btyp);

      return btyp;
    }

   String getInternalName()			{ return class_name; }
   String getJavaName() {
      String jnm = class_name.replace('/','.');
      jnm = jnm.replace('$','.');
      return jnm;
    }
   String getAccessName() {
      return class_name.replace('/','.');
    }
   boolean isStatic()				{ return (access_info&Opcodes.ACC_STATIC) != 0; }
   String getGenericSignature() 		{ return generic_signature; }
   List<AsmMethod> getMethods() 		{ return method_data; }

   void addField(String nm,int acc,String sgn,String desc) {
      AsmField af = new AsmField(this,nm,acc,sgn,desc,sgn);
      field_data.add(af);
      if (nm.startsWith("thi$")) {
	 access_info &= ~Opcodes.ACC_STATIC;
	 nested_this = true;
       }
    }

   void addMethod(String nm,int acc,String sgn,String desc,String [] exc) {
      AsmMethod am = new AsmMethod(this,nm,acc,sgn,desc,exc);
      method_data.add(am);
    }

   AsmField findField(JcompTyper typer,String id) {
      for (AsmField af : field_data) {
	 if (af.getName().equals(id)) return af;
       }
      if (super_name != null) {
	 AsmClass scl = findKnownType(typer,super_name);
	 if (scl != null) {
	    AsmField af = scl.findField(typer,id);
	    if (af != null) return af;
	  }
       }
      if (iface_names != null) {
	 for (String inm : iface_names) {
	    AsmClass icl = findKnownType(typer,inm);
	    if (icl != null) {
	       AsmField af = icl.findField(typer,id);
	       if (af != null) return af;
	     }
	  }
       }
      return null;
    }

   List<AsmField> findFields(JcompTyper typer,String id) {
      List<AsmField> rslt = new ArrayList<AsmField>();
      for (AsmField af : field_data) {
	 if (id == null || af.getName().equals(id)) rslt.add(af);
       }
      if (super_name != null) {
	 AsmClass scl = findKnownType(typer,super_name);
	 if (scl != null) {
	    List<AsmField> afl = scl.findFields(typer,id);
	    if (afl != null) rslt.addAll(afl);
	  }
       }
      if (iface_names != null) {
	 for (String inm : iface_names) {
	    AsmClass icl = findKnownType(typer,inm);
	    if (icl != null) {
	       List<AsmField> afl = icl.findFields(typer,id);
	       if (afl != null) rslt.addAll(afl);
	     }
	  }
       }

      if (rslt.isEmpty()) return null;

      return rslt;
    }

   List<AsmMethod> findMethods(JcompTyper typer,String id,JcompType argtyp,JcompType ctyp) {
      List<AsmMethod> rslt = new ArrayList<AsmMethod>();
      for (AsmMethod am : method_data) {
	 if ((id == null || am.getName().equals(id)) && am.isCompatibleWith(typer,argtyp) >= 0)
	    rslt.add(am);
       }
      if (super_name != null && rslt.isEmpty()) {
	 AsmClass scl = findKnownType(typer,super_name);
	 if (scl != null) {
	    List<AsmMethod> rl = scl.findMethods(typer,id,argtyp,ctyp);
	    if (rl != null) rslt.addAll(rl);
	  }
       }
      if (iface_names != null && rslt.isEmpty()) {
	 for (String inm : iface_names) {
	    AsmClass icl = findKnownType(typer,inm);
	    if (icl != null) {
	       List<AsmMethod> rl = icl.findMethods(typer,id,argtyp,ctyp);
	       if (rl != null) rslt.addAll(rl);
	     }
	  }
       }
      if (rslt.isEmpty() && (access_info & Opcodes.ACC_INTERFACE) != 0) {
	 AsmClass jlo = findKnownType(typer,"java.lang.Object");
	 List<AsmMethod> rl = jlo.findMethods(typer,id,argtyp,ctyp)   ;
	 if (rl != null) rslt.addAll(rl);
       }

      if (rslt.isEmpty()) return null;

      return rslt;
    }

   synchronized void defineAll(JcompTyper typer,JcompScope scp) {
      if (local_defined == null) local_defined = new HashSet<>();
      else if (!local_defined.add(scp)) return;

      for (AsmField af : field_data) {
	 if (scp.lookupVariable(af.getName()) == null) {
	    JcompSymbol js = af.createField(typer);
	    scp.defineVar(js);
	  }
	 else {
	    JcompSymbol js = af.createField(typer);
	    scp.defineDupVar(js);
	  }
       }
      for (AsmMethod am : method_data) {
	 JcompType atyp = am.getMethodType(typer,null,null);
	 JcompSymbol fjs = scp.lookupExactMethod(am.getName(),atyp);
	 if (fjs != null) {
	    JcompType btyp = fjs.getType();
	    if (!btyp.equals(atyp)) {
	       List<JcompType> bcomp = btyp.getComponents();
	       List<JcompType> acomp = atyp.getComponents();
	       if (bcomp.size() != acomp.size()) fjs = null;
	       // else if (atyp.isVarArgs() != btyp.isVarArgs()) fjs = null;
	       else {
		  for (int i = 0; i < bcomp.size(); ++i) {
		     if (!acomp.get(i).equals(bcomp.get(i)))
			fjs = null;
		   }
		}
	     }
	  }
	 if (fjs == null) {
	    JcompSymbol js = am.createMethod(typer,null,getJcompType(typer));
	    scp.defineMethod(js);
	  }
       }
      if (super_name != null) {
	 JcompType styp = findExistingType(typer,super_name);
	 if (styp == null || styp.isBinaryType()) {
	    AsmClass scl = findKnownType(typer,super_name);
	    if (scl != null) scl.defineAll(typer,scp);
	  }
       }
      if (iface_names != null) {
	 for (String inm : iface_names) {
	    JcompType styp =  findExistingType(typer,inm);
	    if (styp == null || styp.isBinaryType()) {
	       AsmClass icl = findKnownType(typer,inm);
	       if (icl != null) {
		  icl.defineAll(typer,scp);
		}
	     }
	  }
       }
   }


   private JcompType findExistingType(JcompTyper typer,String nm) {
      String nm1 = nm.replace("/",".");
      JcompType jty = typer.findType(nm1);
      if (jty != null) return jty;
      String nm2 = nm1.replace("$",".");
      jty = typer.findType(nm2);
      if (jty != null) return jty;
      return null;
    }

}	// end of innerclass AsmClass



/********************************************************************************/
/*										*/
/*	Storage for field information from ASM					*/
/*										*/
/********************************************************************************/

private class AsmField {

   private AsmClass for_class;
   private String field_name;
   private int access_info;
   private String field_description;
   private String field_signature;

   AsmField(AsmClass cls,String nm,int acc,String sgn,String desc,String sign) {
      for_class = cls;
      field_name = nm;
      access_info = acc;
      field_description = desc;
      field_signature = sign;
    }

   String getName()			{ return field_name; }

   JcompSymbol createField(JcompTyper typer) {
      JcompType fty = getAsmType(typer,field_description);
      return JcompSymbol.createBinaryField(field_name,fty,for_class.getJcompType(typer),access_info,field_signature);
    }

}	// end of innerclass AsmField




/********************************************************************************/
/*										*/
/*	Storage for method information from ASM 				*/
/*										*/
/********************************************************************************/

private class AsmMethod {

   private AsmClass for_class;
   private String method_name;
   private int access_info;
   private String method_desc;
   private String generic_signature;
   private String [] exception_types;

   AsmMethod(AsmClass cls,String nm,int acc,String sgn,String desc,String [] excs) {
      for_class = cls;
      method_name = nm;
      access_info = acc;
      generic_signature = sgn;
      method_desc = desc;
      exception_types = excs;
    }

   String getName()			{ return method_name; }

   int isCompatibleWith(JcompTyper typer,JcompType argtyp) {
      if (argtyp == null) return 0;

      Type [] margs = Type.getArgumentTypes(method_desc);
      JcompType [] jmargs = new JcompType[margs.length];
      for (int i = 0; i < margs.length; ++i) {
	 jmargs[i] = getAsmType(typer,margs[i]);
       }
      boolean vargs = (access_info & Opcodes.ACC_VARARGS) != 0;
      boolean init = method_name.equals("<init>") && for_class.isStatic();
      if (init) {
	 String cn = for_class.getAccessName();
	 int idx = cn.lastIndexOf('$');
	 int idx1 = cn.lastIndexOf('.');
	 init = idx > 0 && idx > idx1;
       }

      int score = compatiblityScore(argtyp,jmargs,vargs,init);

      return score;
    }

   JcompSymbol createMethod(JcompTyper typer,JcompType argtyp,JcompType ctyp) {
      Type mret = Type.getReturnType(method_desc);
      JcompType rt = getAsmType(typer,mret);

      boolean gen = false;
      String csgn = for_class.getGenericSignature();
      if (generic_signature != null && csgn != null && ctyp != null && ctyp.isParameterizedType()) {
	 JcompType nrt = typer.getParameterizedReturnType(generic_signature,csgn,ctyp,argtyp);
	 if (nrt != null) {
	    rt = nrt;
	    gen = true;
	  }
       }
      else if (generic_signature != null && csgn != null && ctyp != null && argtyp != null) {
	 JcompType nrt = typer.getParameterizedReturnType(generic_signature,csgn,null,argtyp);
	 if (nrt != null) {
	    rt = nrt;
	    gen = true;
	  }
       }
      else if (generic_signature != null) {
	 JcompType nrt = typer.getParameterizedReturnType(generic_signature,csgn,ctyp,argtyp);
	 if (nrt != null) {
	    rt = nrt;
	    gen = true;
	  }
       }

      JcompType mt = getMethodType(typer,rt,generic_signature);

      List<JcompType> excs = new ArrayList<JcompType>();
      if (exception_types != null) {
	 for (String s : exception_types) {
	    JcompType jt = getAsmTypeName(typer,s);
	    excs.add(jt);
	  }
       }

      return JcompSymbol.createBinaryMethod(method_name,mt,for_class.getJcompType(typer),access_info,excs,gen);
    }

   JcompType getMethodType(JcompTyper typer,JcompType rt,String signature) {
      List<JcompType> atys = new ArrayList<JcompType>();
      for (Type t : Type.getArgumentTypes(method_desc)) {
	 atys.add(getAsmType(typer,t));
       }
      boolean var = (access_info & Opcodes.ACC_VARARGS) != 0;
      JcompType mt = typer.createMethodType(rt,atys,var,signature);
      return mt;
    }


}	// end of innerclass AsmMethod




/********************************************************************************/
/*										*/
/*	Routines to setup base path						*/
/*										*/
/********************************************************************************/

private void computeBasePath(String javahome)
{
   List<File> base = IvyExecQuery.computeBasePath(javahome);

   for (File f : base) {
      try {
	 addClassPathEntry(f);
       }
      catch (IOException e) { }
    }
}


/********************************************************************************/
/*										*/
/*	Routines to lookup a file						*/
/*										*/
/********************************************************************************/

public synchronized boolean contains(String name)
{
   for (ClassPathEntry cpe : base_files) {
      if (cpe.contains(name)) return true;
    }

   return false;
}



public synchronized InputStream getInputStream(String name)
{
   for (ClassPathEntry cpe : base_files) {
      InputStream ins = cpe.getInputStream(name);
      if (ins != null) return ins;
    }

   return null;
}



/********************************************************************************/
/*										*/
/*	Methods for class path entries						*/
/*										*/
/********************************************************************************/

private void addUserClassPathEntry(String fil) throws IOException
{
   class_path.add(fil);
   addClassPathEntry(fil);
}


private void addClassPathEntry(String fil) throws IOException
{
   addClassPathEntry(new File(fil));
}



private void addClassPathEntry(File f) throws IOException
{
   if (!f.exists()) return;
   if (f.isDirectory()) {
      base_files.add(new DirClassPathEntry(f));
    }
   else if (f.getName().endsWith(".jmod")) {
      base_files.add(new JmodClassPathEntry(new ZipFile(f)));
    }
   else {
      base_files.add(new JarClassPathEntry(new JarFile(f)));
    }
}



private abstract static class ClassPathEntry {

   abstract boolean contains(String name);
   abstract InputStream getInputStream(String name);

}	// end of inner class ClassPathEntry


private static class JarClassPathEntry extends ClassPathEntry {

   private JarFile jar_file;

   JarClassPathEntry(JarFile jf) {
      jar_file = jf;
    }

   @Override boolean contains(String name) {
      if (jar_file.getEntry(name) != null) return true;
      return false;
    }

   @Override InputStream getInputStream(String name) {
      ZipEntry ent = jar_file.getEntry(name);
      if (ent != null) {
	 try {
	    return jar_file.getInputStream(ent);
	  }
	 catch (ZipException e) {
	    System.err.println("JCOMP: Problem with system zip file: " + e);
	  }
	 catch (IOException e) {
	    System.err.println("JCOMP: Problem opening system jar entry: " + e);
	  }
       }
      return null;
    }

}	// end of inner class JarClassPathEntry



private static class JmodClassPathEntry extends ClassPathEntry {

   private ZipFile jmod_file;

   JmodClassPathEntry(ZipFile jf) {
      jmod_file = jf;
      // might want to add implied dependencies here
    }

   @Override boolean contains(String name) {
      String usename = "classes/" + name;
      if (jmod_file.getEntry(usename) != null) return true;
      return false;
    }

   @Override InputStream getInputStream(String name) {
      String usename = "classes/" + name;
      ZipEntry ent = jmod_file.getEntry(usename);
      if (ent != null) {
	 try {
	    return jmod_file.getInputStream(ent);
	  }
	 catch (ZipException e) {
	    System.err.println("JCOMP: Problem with system zip file: " + e);
	  }
	 catch (IOException e) {
	    System.err.println("JCOMP: Problem opening system jmod entry: " + e);
	  }
       }
      return null;
    }

}	// end of inner class JarClassPathEntry



private static class DirClassPathEntry extends ClassPathEntry {

   private File root_dir;

   DirClassPathEntry(File dir) {
      root_dir = dir;
    }

   @Override boolean contains(String name) {
      File fil = findFile(name);
      if (fil == null) return false;
      return true;
    }

   @Override InputStream getInputStream(String name) {
      File fil = findFile(name);
      if (fil == null) return null;
      try {
	 return new FileInputStream(fil);
       }
      catch (IOException e) {
	 System.err.println("JCOMP: Probleam reading file " + fil);
       }
      return null;
    }

   private File findFile(String name) {
      StringTokenizer tok = new StringTokenizer(name,"/");
      File rslt = root_dir;
      while (tok.hasMoreTokens()) {
	 String fnm = tok.nextToken();
	 rslt = new File(rslt,fnm);
       }
      if (rslt.exists()) return rslt;
      return null;
    }

}	// end of inner class DirClassPathEntry



}	// end of class JcompContextAsm



/* end of JcompContextAsm.java */




