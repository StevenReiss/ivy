/********************************************************************************/
/*										*/
/*		JcodeMethod.java						*/
/*										*/
/*	Byte code definitions method representation				*/
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
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH  HE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.ivy.jcode;


import org.objectweb.asm.tree.*;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;

import java.security.*;

import java.util.*;
import java.lang.reflect.*;


public class JcodeMethod extends MethodNode
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcodeFactory		bcode_factory;
private JcodeClass		in_class;
private List<JcodeInstruction>	ins_list;
private Map<Label,Integer>	goto_map;
private String			match_name;
private Collection<JcodeMethod>  parent_methods;
private Collection<JcodeMethod>  child_methods;
private Collection<JcodeTryCatchBlock> try_blocks;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcodeMethod(JcodeFactory bf,JcodeClass cls,int a,String n,String d,String s,String [] ex)
{
   super(Opcodes.ASM5,a,n,d,s,ex);
   bcode_factory = bf;
   in_class = cls;
   match_name = null;
   goto_map = null;
   ins_list = null;
   parent_methods = null;
   child_methods = null;
   try_blocks = new ArrayList<JcodeTryCatchBlock>();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public JcodeDataType getDeclaringClass()
{
   return in_class.getDataType();
}



public String getName()
{
   return name;
}

public String getFullName()
{
   return in_class.getName() + "." + name;
}



public String getDescription()
{
   return desc;
}


String getMatchName()
{
   if (match_name == null) {
      match_name = name + "." + desc;
    }
   return match_name;
}


public boolean isStaticInitializer()
{
   return getName().equals("<clinit>");
}

public boolean isStatic()
{
   return Modifier.isStatic(access);
}

public boolean isAbstract()
{
   return Modifier.isAbstract(access);
}


public boolean isNative()
{
   return Modifier.isNative(access);
}


public boolean isPrivate()
{
   return Modifier.isPrivate(access);
}


public boolean isSynchronized()
{
   return Modifier.isSynchronized(access);
}

public boolean isConstructor()
{
   return getName().equals("<init>");
}


public JcodeDataType getReturnType()
{
   Type rt = Type.getReturnType(desc);
   return in_class.getDataType(rt);
}


public List<JcodeDataType> getExceptionTypes()
{
   List<JcodeDataType> rslt = new ArrayList<JcodeDataType>();
   for (int i = 0; i < exceptions.size(); ++i) {
      String enm = exceptions.get(i);
      JcodeDataType fdt = bcode_factory.findClassType(enm);
      if (fdt != null) rslt.add(fdt);
    }
   return rslt;
}


public List<JcodeClass> getExceptionClasses()
{
   List<JcodeClass> rslt = new ArrayList<JcodeClass>();
   for (int i = 0; i < exceptions.size(); ++i) {
      String enm = exceptions.get(i);
      JcodeClass fdt = bcode_factory.findClass(enm);
      if (fdt != null) rslt.add(fdt);
    }
   return rslt;
}


public JcodeDataType getArgType(int idx)
{
   Type [] atyps = Type.getArgumentTypes(desc);
   if (idx < 0 || idx >= atyps.length) return null;
   return in_class.getDataType(atyps[idx]);
}

public int getNumArguments()
{
   Type [] atyps = Type.getArgumentTypes(desc);
   return atyps.length;
}



public int getLocalSize()
{
   return maxLocals;
}


public int getNumInstructions()
{
   if (ins_list == null) return 0;
   return ins_list.size();
}

public JcodeInstruction getInstruction(int idx)
{
   if (ins_list == null) return null;
   if (idx < 0 || idx >= ins_list.size()) return null;
   return ins_list.get(idx);
}


public int getIndexOf(JcodeInstruction ins)
{
   return ins_list.indexOf(ins);
}



/********************************************************************************/
/*										*/
/*	Visitation methods							*/
/*										*/
/********************************************************************************/

@Override public void visitEnd()
{
   super.visitEnd();

   int lno = 0;
   ins_list = new ArrayList<JcodeInstruction>();
   goto_map = new HashMap<Label,Integer>();
   int sz = 0;

   if (instructions.size() == 0) {
      if (!isStatic()) ++maxLocals;
      Type [] atyps = Type.getArgumentTypes(desc);
      for (int i = 0; i < atyps.length; ++i) {
	 maxLocals += 1;
	 switch (atyps[i].getSort()) {
	    case Type.LONG :
	    case Type.DOUBLE :
	       maxLocals += 1;
	       break;
	  }
       }
    }

   InsnList inl = instructions;
   for (int i = 0; i < inl.size(); ++i) {
      AbstractInsnNode ain = inl.get(i);
      switch (ain.getType()) {
	 case AbstractInsnNode.LABEL :
	    LabelNode lnode = (LabelNode) ain;
	    goto_map.put(lnode.getLabel(),sz);
	    break;
	 case AbstractInsnNode.LINE :
	    LineNumberNode lnnode = (LineNumberNode) ain;
	    lno = lnnode.line;
	    break;
	 case AbstractInsnNode.FRAME :
	    // these can be ignored
	    break;
	 default :
	    JcodeInstruction bi = new JcodeInstruction(this,sz,lno,ain);
	    ins_list.add(bi);
	    ++sz;
	    break;
       }
    }

   computeDigest();
}


@Override public void visitTypeInsn(int opc,String typ)
{
   in_class.getFactory().noteClass(typ);
   super.visitTypeInsn(opc,typ);
}


@Override public void visitFieldInsn(int opc,String o,String n,String d)
{
   in_class.getFactory().noteClass(o);
   in_class.getFactory().noteType(d);
   super.visitFieldInsn(opc,o,n,d);
}


@Override public void visitMethodInsn(int opc,String o,String n,String d,boolean itf)
{
   in_class.getFactory().noteClass(o);
   in_class.getFactory().noteType(d);
   super.visitMethodInsn(opc,o,n,d,itf);
}



@Override public void visitTryCatchBlock(Label start,Label end,Label hdlr,String typ)
{
   super.visitTryCatchBlock(start,end,hdlr,typ);

   JcodeTryCatchBlock tcd = new JcodeTryCatchBlock(this,start,end,hdlr,typ);
   try_blocks.add(tcd);
}



/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

JcodeFactory getFactory()		{ return bcode_factory; }

JcodeInstruction findInstruction(Label l)
{
   if (goto_map == null) return null;
   Integer idx = goto_map.get(l);
   if (idx == null) return null;
   return ins_list.get(idx);
}





/********************************************************************************/
/*										*/
/*	Digest methods							       */
/*										*/
/********************************************************************************/

private void computeDigest()
{
   MessageDigest md = null;
   try {
      md = MessageDigest.getInstance("MD5");
    }
   catch (NoSuchAlgorithmException e) {
      System.err.println("FAIT: Can't find MD5 digest");
      System.exit(1);
    }

   addToDigest(md,name);
   addToDigest(md,desc);
   addToDigest(md,signature);
   for (int i = 0; i < instructions.size(); ++i) {
      AbstractInsnNode ain = instructions.get(i);
      String ins = JcodeInstruction.getString(ain,this);
      addToDigest(md,ins);
    }

   // message_digest = md.digest();
}


private void addToDigest(MessageDigest md,String s)
{
   if (s != null) md.update(s.getBytes());
}



/********************************************************************************/
/*										*/
/*	Methods for maintaining method hierarchy				*/
/*										*/
/********************************************************************************/

public synchronized Collection<JcodeMethod> getParentMethods()
{
   if (parent_methods != null) return parent_methods;

   parent_methods = in_class.findParentMethods(name,desc,false,false,null);

   return parent_methods;
}



public synchronized Collection<JcodeMethod> getChildMethods()
{
   if (child_methods != null) return child_methods;

   if (isPrivate()) {
      child_methods = Collections.emptyList();
    }
   else {
      child_methods = in_class.findChildMethods(name,desc,false,null);
    }

   return child_methods;
}




/********************************************************************************/
/*										*/
/*	Exception handling							*/
/*										*/
/********************************************************************************/

public Collection<JcodeTryCatchBlock> getTryCatchBlocks()
{
   return try_blocks;
}



/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return getDeclaringClass().toString() + "." + getName() + getDescription();
}




}	// end of class JcodeMethod




/* end of JcodeMethod.java */

