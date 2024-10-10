/********************************************************************************/
/*                                                                              */
/*              JannotInstrumenter.java                                         */
/*                                                                              */
/*      Instrumentation of user annotation processor classes                    */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.ivy.jannot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureWriter;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import edu.brown.cs.ivy.jcode.JcodeConstants;

public class JannotInstrumenter implements JannotConstants, ClassFileTransformer
{



/********************************************************************************/
/*                                                                              */
/*      Instrumentation entry points                                            */
/*                                                                              */
/********************************************************************************/

public static void agentMain(String args,Instrumentation inst)
{
   if (the_control == null) the_control = new JannotInstrumenter(args,inst);
}




/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Instrumentation         class_inst;
private JannotInstrumenter       our_instrumenter;

private static JannotInstrumenter the_control = null;
private static boolean do_debug = true;
private static int ASM_API = JcodeConstants.ASM_API;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotInstrumenter(String args,Instrumentation inst)
{
   class_inst = inst;
   if (class_inst != null) {
      our_instrumenter = this;
      class_inst.addTransformer(our_instrumenter,true);
    }
}



/********************************************************************************/
/*                                                                              */
/*      Transformation methods                                                  */
/*                                                                              */
/********************************************************************************/

public byte [] transform(ClassLoader ldr,String name,Class<?> cls,
      ProtectionDomain dom,byte [] buf)
{
   if (ldr == null) return null;
   if (!ldr.getClass().getName().endsWith("JannotClassLoader")) return null;
   if (name.startsWith("java/") || name.startsWith("sun/") ||
         name.startsWith("javax/")) return null;
   
   return instrument(cls,name,buf);
}



private byte [] instrument(Class<?> cls,String name,byte [] buf)
{
   byte [] rsltcode;
   try {
      ClassReader reader = new ClassReader(buf);
      ClassWriter writer = new SafeClassWriter(reader,
            ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
      ClassVisitor ins = new JannotTransformer(cls,name,writer);
      reader.accept(ins,ClassReader.SKIP_FRAMES);
      rsltcode = writer.toByteArray();
    }
   catch (Throwable t) {
      System.err.println("JANNOT: Problem doing instrumentation: " + t);
      t.printStackTrace();
      return null;
    }
   
   if (do_debug) {
      try {
         String fnm = "/ws/volfred/jannot";
         File f1 = new File(fnm);
         File f2 = new File(f1,name.replace("/","."));
         FileOutputStream fos = new FileOutputStream(f2);
         System.err.println("JANNOT: SAVE IN " + f2);
         fos.write(rsltcode);
         fos.close();
       }
      catch (Throwable t) { }
    }
   
   return rsltcode;
}




/********************************************************************************/
/*                                                                              */
/*      Class visitor for handling transformations                              */
/*                                                                              */
/********************************************************************************/

private class JannotTransformer extends ClassVisitor {
   
   JannotTransformer(Class<?> cls,String name,ClassVisitor cvx) {
      super(ASM_API,cvx);
    }
   
   @Override public void visit(int v,int acc,String name,String sign,String sup,String [] ifaces) {
      String osup = getOurClass(sup);
      String osign = getOurSignature(sign);
      super.visit(v,acc,name,osign,osup,ifaces);
      if (do_debug) {
         System.err.println("JANNOT: start class " + name + " extends " + osup);
       }
    }
   
   
   @Override public MethodVisitor visitMethod(int acc,String name,String desc,String sgn,
         String [] exc) {
      String ndesc = getOurDescriptor(desc);
      String nsgn = getOurSignature(sgn);
      System.err.println("JANNOT: VISIT " + name + " " + desc + " " + 
            ndesc + " " + sgn + " " + nsgn);
      MethodVisitor mv = super.visitMethod(acc,name,ndesc,nsgn,exc);
      if (do_debug) mv = new MethodTracer(mv,name,ndesc);
      mv = new TreeMapper(mv);
      
      return mv;
    }
   
   @Override public FieldVisitor visitField(int acc,String name,String desc,String sgn,Object val) {
      String ndesc = updateDescription(desc);
      return super.visitField(acc,name,ndesc,sgn,val);
    }
   
}       // end of inner class JannotTransformer




/********************************************************************************/
/*                                                                              */
/*      Mapping methods                                                         */
/*                                                                              */
/********************************************************************************/

private static String updateDescription(String d)
{
   return d;
}




private String getOurClass(String cls) 
{
   if (cls.startsWith("com/sun/tools/javac/tree/JCTree")) {
      int idx = 32;
      if (cls.length() < idx) return "edu/brown/cs/ivy/jannot/tree/JannotTree";
      else return "edu/brown/cs/ivy/jannot/tree/JannotTree" + cls.substring(idx);
    }
   if (cls.equals("com/sun/tools/javac/tree/TreeTranslator")) {
      return "edu/brown/cs/ivy/jannot/tree/JannotTreeTranslator";
    }
   if (cls.equals("com/sun/tools/javac/tree/TreeMaker")) {
      return "edu/brown/cs/ivy/jannot/tree/JannotTreeMaker";
    }
   return cls;
}



/********************************************************************************/
/*                                                                              */
/*      Handle descriptor and signature mapping                                 */
/*                                                                              */
/********************************************************************************/

private String getOurDescriptor(String d)
{
   if (d == null) return null;
   if (!d.contains("javac")) return d;
   
   SignatureReader sr = new SignatureReader(d);
   SignatureMapper sm = new SignatureMapper();
   sr.accept(sm);
   return sm.toString();
}


private String getOurSignature(String s)
{
   return getOurDescriptor(s);
}



private final class SignatureMapper extends SignatureWriter {

   @Override public void visitClassType(String name) {
      super.visitClassType(getOurClass(name));
    }
   
   @Override public void visitFormalTypeParameter(String name) {
      super.visitFormalTypeParameter(getOurClass(name));
    }
   
}       // end of inner class SignatureMapper




private String getFieldName(String pfx,String name) 
{
   String newnm = Character.toUpperCase(name.charAt(0)) + name.substring(1);
   return pfx + newnm;
}



/********************************************************************************/
/*                                                                              */
/*      Actual instruction mapper for most methods                              */
/*                                                                              */
/********************************************************************************/

private class TreeMapper extends MethodVisitor {
   
   TreeMapper(MethodVisitor mvx) {
      super(ASM_API,mvx);
    }
   
   @Override public void visitLocalVariable(String name,String desc,String sign,
         Label start,Label end,int idx) {
      super.visitLocalVariable(name,getOurDescriptor(desc),sign,start,end,idx);
    }
   
   @Override public void visitFieldInsn(int opcode,String owner,String name,String desc) {
      if (owner.startsWith("com/sun/tools/javac/tree/JCTree")) {
         String ndesc = getOurDescriptor(desc);
         String cnm = getOurClass(owner);
         if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
            String nm = getFieldName("getField",name);
            String descl = "()" + ndesc;
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,cnm,nm,descl,false);
          }
         else {
            String nm = getFieldName("setField",name);
            String descl = "(" + ndesc + ")V";
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,cnm,nm,descl,false);
          }
       }
      else {
         super.visitFieldInsn(opcode,owner,name,desc);
       }
    }
   
   @Override public void visitMethodInsn(int opcode,String owner,String name,String desc,
         boolean iface) {
      if (owner.startsWith("com/sun/tools/javac")) {
         super.visitMethodInsn(opcode,getOurClass(owner),name,getOurDescriptor(desc),iface);
       }
      else if (owner.equals("com/sun/source/util/Trees") && name.equals("instance")) {
         super.visitMethodInsn(opcode,
               "edu/brown/cs/ivy/jannot/tree/JannotTrees",
               "jannotInstance",desc,iface);
       }
      else {
         super.visitMethodInsn(opcode,owner,name,getOurDescriptor(desc),iface);
       }
    }
   
   @Override public void visitTypeInsn(int opcode,String type) {
      if (type.startsWith("com/sun/tools/javac/tree/JCTree")) {
         super.visitTypeInsn(opcode,getOurClass(type));
       }
      else {
         super.visitTypeInsn(opcode,type);
       }
    }
   
   @Override public void visitInvokeDynamicInsn(String name,String desc,Handle hdl,Object... obj) {
      Handle nhdl = mapHandle(hdl);
      super.visitInvokeDynamicInsn(name,getOurDescriptor(desc),nhdl,obj);
    }
   
   private Handle mapHandle(Handle h) {
      
      return h;
    }
        
}       // end of inner class TreeMapper




/********************************************************************************/
/*                                                                              */
/*      Debug outputer                                                          */
/*                                                                              */
/********************************************************************************/

private static class MethodTracer extends MethodVisitor {
   
   private String method_name;
   private String method_desc;
   
   MethodTracer(MethodVisitor xmv,String nm,String desc) {
      super(ASM_API,new TraceMethodVisitor(xmv,new Textifier()));
      method_name = nm;
      method_desc = desc;
    }
   
   @Override public void visitEnd() {
      TraceMethodVisitor tmv = (TraceMethodVisitor) this.mv;
      Printer p = tmv.p;
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      super.visitEnd();
      p.print(pw);
      System.err.println("JANNOT: method output: " + method_name + " " + 
            method_desc + ":\n" + sw.toString());
    }
   
}




/********************************************************************************/
/*                                                                              */
/*      Class Writer that handles common super for known types                  */
/*                                                                              */
/********************************************************************************/

private static class SafeClassWriter extends ClassWriter {
   
   SafeClassWriter(ClassReader cr,int fgs) {
      super(cr,fgs);
    }
   
   @Override protected String getCommonSuperClass(String typ1,String typ2) {
      try {
         return super.getCommonSuperClass(typ1,typ2);
       }
      catch (Throwable t) { }
      
      return "java/lang/Object";
    }
   
}       // end of inner class SafeClassWriter




}       // end of class JannotInstrumenter




/* end of JannotInstrumenter.java */

