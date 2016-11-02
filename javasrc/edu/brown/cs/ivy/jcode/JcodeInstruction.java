/********************************************************************************/
/*										*/
/*		JcodeInstruction.java						*/
/*										*/
/*	Internal represntation of an instruction				*/
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

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;


public class JcodeInstruction implements JcodeConstants, Opcodes
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcodeMethod in_method;
private int index_no;
private int line_number;
private AbstractInsnNode for_inst;



/********************************************************************************/
/*										*/
/*	Static tables with instruction information				*/
/*										*/
/********************************************************************************/

private static final short  opcode_stack_height[][] = {
   {0, 0},		// nop
   {0, 1},		// aconst_null
   {0, 1},		// iconst_m1
   {0, 1},		// iconst_0
   {0, 1},		// iconst_1
   {0, 1},		// iconst_2
   {0, 1},		// iconst_3
   {0, 1},		// iconst_4
   {0, 1},		// iconst_5
   {0, 2},		// lconst_0
   {0, 2},		// lconst_1
   {0, 1},		// fconst_0
   {0, 1},		// fconst_1
   {0, 1},		// fconst_2
   {0, 2},		// dconst_0
   {0, 2},		// dconst_1
   {0, 1},		// bipush
   {0, 1},		// sipush
   {0, 1},		// ldc
   {0, 1},		// ldc_w
   {0, 2},		// ldc2_w
   {0, 1},		// iload
   {0, 2},		// lload
   {0, 1},		// fload
   {0, 2},		// dload
   {0, 1},		// aload
   {0, 1},		// iload_0
   {0, 1},		// iload_1
   {0, 1},		// iload_2
   {0, 1},		// iload_3
   {0, 2},		// lload_0
   {0, 2},		// lload_1
   {0, 2},		// lload_2
   {0, 2},		// lload_3
   {0, 1},		// fload_0
   {0, 1},		// fload_1
   {0, 1},		// fload_2
   {0, 1},		// fload_3
   {0, 2},		// dload_0
   {0, 2},		// dload_1
   {0, 2},		// dload_2
   {0, 2},		// dload_3
   {0, 1},		// aload_0
   {0, 1},		// aload_1
   {0, 1},		// aload_2
   {0, 1},		// aload_3
   {2, 1},		// iaload
   {2, 2},		// laload
   {2, 1},		// faload
   {2, 2},		// daload
   {2, 1},		// aaload
   {2, 1},		// baload
   {2, 1},		// caload
   {2, 1},		// saload
   {1, 0},		// istore
   {2, 0},		// lstore
   {1, 0},		// fstore
   {2, 0},		// dstore
   {1, 0},		// astore
   {1, 0},		// istore_0
   {1, 0},		// istore_1
   {1, 0},		// istore_2
   {1, 0},		// istore_3
   {2, 0},		// lstore_0
   {2, 0},		// lstore_1
   {2, 0},		// lstore_2
   {2, 0},		// lstore_3
   {1, 0},		// fstore_0
   {1, 0},		// fstore_1
   {1, 0},		// fstore_2
   {1, 0},		// fstore_3
   {2, 0},		// dstore_0
   {2, 0},		// dstore_1
   {2, 0},		// dstore_2
   {2, 0},		// dstore_3
   {1, 0},		// astore_0
   {1, 0},		// astore_1
   {1, 0},		// astore_2
   {1, 0},		// astore_3
   {3, 0},		// iastore
   {4, 0},		// lastore
   {3, 0},		// fastore
   {4, 0},		// dastore
   {3, 0},		// aastore
   {3, 0},		// bastore
   {3, 0},		// castore
   {3, 0},		// sastore
   {1, 0},		// pop
   {2, 0},		// pop2
   {1, 2},		// dup
   {2, 3},		// dup_x1
   {3, 4},		// dup_x2
   {2, 4},		// dup2
   {3, 5},		// dup2_x1
   {4, 6},		// dup2_x2
   {2, 2},		// swap
   {2, 1},		// iadd
   {4, 2},		// ladd
   {2, 1},		// fadd
   {4, 2},		// dadd
   {2, 1},		// isub
   {4, 2},		// lsub
   {2, 1},		// fsub
   {4, 2},		// dsub
   {2, 1},		// imul
   {4, 2},		// lmul
   {2, 1},		// fmul
   {4, 2},		// dmul
   {2, 1},		// idiv
   {4, 2},		// ldiv
   {2, 1},		// fdiv
   {4, 2},		// ddiv
   {2, 1},		// irem
   {4, 2},		// lrem
   {2, 1},		// frem
   {4, 2},		// drem
   {1, 1},		// ineg
   {2, 2},		// lneg
   {1, 1},		// fneg
   {2, 2},		// dneg
   {2, 1},		// ishl
   {3, 2},		// lshl
   {2, 1},		// ishr
   {3, 2},		// lshr
   {2, 1},		// iushr
   {3, 2},		// lushr
   {2, 1},		// iand
   {4, 2},		// land
   {2, 1},		// ior
   {4, 2},		// lor
   {2, 1},		// ixor
   {4, 2},		// lxor
   {0, 0},		// iinc
   {1, 2},		// i2l
   {1, 1},		// i2f
   {1, 2},		// i2d
   {2, 1},		// l2i
   {2, 1},		// l2f
   {2, 2},		// l2d
   {1, 1},		// f2i
   {1, 2},		// f2l
   {1, 2},		// f2d
   {2, 1},		// d2i
   {2, 2},		// d2l
   {2, 1},		// d2f
   {1, 1},		// i2b
   {1, 1},		// i2c
   {1, 1},		// i2s
   {4, 1},		// lcmp
   {2, 1},		// fcmpl
   {2, 1},		// fcmpg
   {4, 1},		// dcmpl
   {4, 1},		// dcmpg
   {1, 0},		// ifeq
   {1, 0},		// ifne
   {1, 0},		// iflt
   {1, 0},		// ifge
   {1, 0},		// ifgt
   {1, 0},		// ifle
   {2, 0},		// if_icmpeq
   {2, 0},		// if_icmpne
   {2, 0},		// if_icmplt
   {2, 0},		// if_icmpge
   {2, 0},		// if_icmpgt
   {2, 0},		// if_icmple
   {2, 0},		// if_acmpeq
   {2, 0},		// if_acmpne
   {0, 0},		// goto
   {0, 1},		// jsr
   {0, 0},		// ret
   {1, 0},		// tableswitch
   {1, 0},		// lookupswitch
   {1, 0},		// ireturn
   {2, 0},		// lreturn
   {1, 0},		// freturn
   {2, 0},		// dreturn
   {1, 0},		// areturn
   {0, 0},		// return
   {0, 1},		// getstatic
   {1, 0},		// putstatic
   {1, 1},		// getfield
   {2, 0},		// putfield
   {1, 1},		// invokevirtual
   {1, 1},		// invokespecial
   {1, 1},		// invokestatic
   {1, 1},		// invokeinterface
   {1, 1},		// xxxunusedxxx
   {0, 1},		// new
   {1, 1},		// newarray
   {1, 1},		// anewarray
   {1, 1},		// arraylength
   {1, 0},		// athrow
   {1, 1},		// checkcast
   {1, 1},		// instanceof
   {1, 0},		// monitorenter
   {1, 0},		// monitorexit
   {0, 0},		// wide
   {2, 1},		// multianewarray
   {1, 0},		// ifnull
   {1, 0},		// ifnonnull
   {0, 0},		// goto_w
   {0, 1},		// jsr_w
};



/********************************************************************************/
/*										*/
/*	Output definitions							*/
/*										*/
/********************************************************************************/

private static String [] OPCODE_NAMES = new String [] {
   "NOP",
   "ACONST_NULL",
   "ICONST_M1",
   "ICONST_0",
   "ICONST_1",
   "ICONST_2",
   "ICONST_3",
   "ICONST_4",
   "ICONST_5",
   "LCONST_0",
   "LCONST_1",
   "FCONST_0",
   "FCONST_1",
   "FCONST_2",
   "DCONST_0",
   "DCONST_1",
   "BIPUSH",
   "SIPUSH",
   "LDC",
   "LDC_W",
   "LDC2_W",
   "ILOAD",
   "LLOAD",
   "FLOAD",
   "DLOAD",
   "ALOAD",
   "ILOAD_0",
   "ILOAD_1",
   "ILOAD_2",
   "ILOAD_3",
   "LLOAD_0",
   "LLOAD_1",
   "LLOAD_2",
   "LLOAD_3",
   "FLOAD_0",
   "FLOAD_1",
   "FLOAD_2",
   "FLOAD_3",
   "DLOAD_0",
   "DLOAD_1",
   "DLOAD_2",
   "DLOAD_3",
   "ALOAD_0",
   "ALOAD_1",
   "ALOAD_2",
   "ALOAD_3",
   "IALOAD",
   "LALOAD",
   "FALOAD",
   "DALOAD",
   "AALOAD",
   "BALOAD",
   "CALOAD",
   "SALOAD",
   "ISTORE",
   "LSTORE",
   "FSTORE",
   "DSTORE",
   "ASTORE",
   "ISTORE_0",
   "ISTORE_1",
   "ISTORE_2",
   "ISTORE_3",
   "LSTORE_0",
   "LSTORE_1",
   "LSTORE_2",
   "LSTORE_3",
   "FSTORE_0",
   "FSTORE_1",
   "FSTORE_2",
   "FSTORE_3",
   "DSTORE_0",
   "DSTORE_1",
   "DSTORE_2",
   "DSTORE_3",
   "ASTORE_0",
   "ASTORE_1",
   "ASTORE_2",
   "ASTORE_3",
   "IASTORE",
   "LASTORE",
   "FASTORE",
   "DASTORE",
   "AASTORE",
   "BASTORE",
   "CASTORE",
   "SASTORE",
   "POP",
   "POP2",
   "DUP",
   "DUP_X1",
   "DUP_X2",
   "DUP2",
   "DUP2_X1",
   "DUP2_X2",
   "SWAP",
   "IADD",
   "LADD",
   "FADD",
   "DADD",
   "ISUB",
   "LSUB",
   "FSUB",
   "DSUB",
   "IMUL",
   "LMUL",
   "FMUL",
   "DMUL",
   "IDIV",
   "LDIV",
   "FDIV",
   "DDIV",
   "IREM",
   "LREM",
   "FREM",
   "DREM",
   "INEG",
   "LNEG",
   "FNEG",
   "DNEG",
   "ISHL",
   "LSHL",
   "ISHR",
   "LSHR",
   "IUSHR",
   "LUSHR",
   "IAND",
   "LAND",
   "IOR",
   "LOR",
   "IXOR",
   "LXOR",
   "IINC",
   "I2L",
   "I2F",
   "I2D",
   "L2I",
   "L2F",
   "L2D",
   "F2I",
   "F2L",
   "F2D",
   "D2I",
   "D2L",
   "D2F",
   "I2B",
   "I2C",
   "I2S",
   "LCMP",
   "FCMPL",
   "FCMPG",
   "DCMPL",
   "DCMPG",
   "IFEQ",
   "IFNE",
   "IFLT",
   "IFGE",
   "IFGT",
   "IFLE",
   "IF_ICMPEQ",
   "IF_ICMPNE",
   "IF_ICMPLT",
   "IF_ICMPGE",
   "IF_ICMPGT",
   "IF_ICMPLE",
   "IF_ACMPEQ",
   "IF_ACMPNE",
   "GOTO",
   "JSR",
   "RET",
   "TABLESWITCH",
   "LOOKUPSWITCH",
   "IRETURN",
   "LRETURN",
   "FRETURN",
   "DRETURN",
   "ARETURN",
   "RETURN",
   "GETSTATIC",
   "PUTSTATIC",
   "GETFIELD",
   "PUTFIELD",
   "INVOKEVIRTUAL",
   "INVOKESPECIAL",
   "INVOKESTATIC",
   "INVOKEINTERFACE",
   "INVOKEDYNAMIC",
   "NEW",
   "NEWARRAY",
   "ANEWARRAY",
   "ARRAYLENGTH",
   "ATHROW",
   "CHECKCAST",
   "INSTANCEOF",
   "MONITORENTER",
   "MONITOREXIT",
   "WIDE",
   "MULTIANEWARRAY",
   "IFNULL",
   "IFNONNULL",
   "GOTO_W",
   "JSR_W",
};


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcodeInstruction(JcodeMethod bm,int ino,int ln,AbstractInsnNode ins)
{
   in_method = bm;
   index_no = ino;
   line_number = ln;
   for_inst = ins;
}



/********************************************************************************/
/*										*/
/*	Access Methods								*/
/*										*/
/********************************************************************************/

public JcodeMethod getMethod()	      { return in_method; }
public int getLineNumber()	      { return line_number; }
public int getOpcode()		      { return for_inst.getOpcode(); }




/********************************************************************************/
/*										*/
/*	Methods for finding related instructions				*/
/*										*/
/********************************************************************************/

public int getIndex()		      { return index_no; }

public JcodeInstruction getNext()
{
   return in_method.getInstruction(index_no + 1);
}

public JcodeInstruction getPrevious()
{
   return in_method.getInstruction(index_no - 1);
}


/********************************************************************************/
/*										*/
/*	Access methods for instruction data					*/
/*										*/
/********************************************************************************/

public JcodeField getFieldReference()
{
   if (for_inst instanceof FieldInsnNode) {
      FieldInsnNode fn = (FieldInsnNode) for_inst;
      JcodeField bf = in_method.getFactory().findField(null,fn.owner,fn.name);
      if (bf == null) {
	 bf = in_method.getFactory().findInheritedField(fn.owner,fn.name);
       }
      return bf;
    }
   return null;
}

public JcodeMethod getMethodReference()
{
   if (for_inst instanceof MethodInsnNode) {
      MethodInsnNode mn = (MethodInsnNode) for_inst;
      JcodeMethod fm = in_method.getFactory().findMethod(null,mn.owner,mn.name,mn.desc);
      if (fm == null) {
	 fm = in_method.getFactory().findInheritedMethod(mn.owner,mn.name,mn.desc);
       }
      if (fm == null) {
	 // arrays might have methods from Object
	 fm = in_method.getFactory().findMethod(null,"Ljava/lang/Object;",mn.name,mn.desc);
       }
      return fm;
    }
   return null;
}


public JcodeDataType getTypeReference()
{
   if (for_inst instanceof MultiANewArrayInsnNode) {
      MultiANewArrayInsnNode mn = (MultiANewArrayInsnNode) for_inst;
      return in_method.getFactory().findJavaType(mn.desc);
    }
   else if (for_inst instanceof TypeInsnNode) {
      TypeInsnNode tn = (TypeInsnNode) for_inst;
      return in_method.getFactory().findClassType(tn.desc);
    }
   else if (for_inst instanceof IntInsnNode) {
      IntInsnNode in = (IntInsnNode) for_inst;
      String t = null;
      switch (in.operand) {
	 case T_BOOLEAN :
	    t = "Z";
	    break;
	 case T_BYTE :
	    t = "B";
	    break;
	 case T_CHAR :
	    t = "C";
	    break;
	 case T_DOUBLE :
	    t = "D";
	    break;
	 case T_FLOAT :
	    t = "F";
	    break;
	 case T_INT :
	    t = "I";
	    break;
	 case T_LONG :
	    t = "L";
	    break;
	 case T_SHORT :
	    t = "S";
	    break;
       }
      if (t != null) return in_method.getFactory().findJavaType(t);
    }

   return null;
}


public int getLocalVariable()
{
   if (for_inst instanceof IincInsnNode) {
      IincInsnNode in = (IincInsnNode) for_inst;
      return in.var;
    }
   else if (for_inst instanceof VarInsnNode) {
      VarInsnNode vn = (VarInsnNode) for_inst;
      return vn.var;
    }
   return -1;
}


public int getIntValue()
{
   if (for_inst instanceof IincInsnNode) {
      IincInsnNode in = (IincInsnNode) for_inst;
      return in.incr;
    }
   else if (for_inst instanceof IntInsnNode) {
      IntInsnNode in = (IntInsnNode) for_inst;
      return in.operand;
    }
   else if (for_inst instanceof MultiANewArrayInsnNode) {
      MultiANewArrayInsnNode mn = (MultiANewArrayInsnNode) for_inst;
      return mn.dims;
    }

   switch (getOpcode()) {
      case ICONST_0 :
      case LCONST_0 :
	 return 0;
      case ICONST_1 :
      case LCONST_1 :
	 return 1;
      case ICONST_2 :
	 return 2;
      case ICONST_3 :
	 return 3;
      case ICONST_4 :
	 return 4;
      case ICONST_5 :
	 return 5;
      case ICONST_M1 :
	 return -1;
    }

   return 0;
}


public JcodeInstruction getTargetInstruction()
{
   if (for_inst instanceof JumpInsnNode) {
      JumpInsnNode jn = (JumpInsnNode) for_inst;
      return in_method.findInstruction(jn.label.getLabel());
    }
   return null;
}



public Collection<JcodeInstruction> getTargetInstructions()
{
   if (for_inst instanceof LookupSwitchInsnNode) {
      LookupSwitchInsnNode ln = (LookupSwitchInsnNode) for_inst;
      List<JcodeInstruction> rslt = new ArrayList<JcodeInstruction>();
      for (Object o : ln.labels) {
	 LabelNode lbl = (LabelNode) o;
	 JcodeInstruction fi = in_method.findInstruction(lbl.getLabel());
	 if (fi != null) rslt.add(fi);
       }
      if (ln.dflt != null) {
	 JcodeInstruction fi = in_method.findInstruction(ln.dflt.getLabel());
	 if (fi != null) rslt.add(fi);
       }
      return rslt;
    }
   else if (for_inst instanceof TableSwitchInsnNode) {
      TableSwitchInsnNode tn = (TableSwitchInsnNode) for_inst;
      List<JcodeInstruction> rslt = new ArrayList<JcodeInstruction>();
      for (Object o : tn.labels) {
	 LabelNode lbl = (LabelNode) o;
	 JcodeInstruction fi = in_method.findInstruction(lbl.getLabel());
	 if (fi != null) rslt.add(fi);
       }
      if (tn.dflt != null) {
	 JcodeInstruction fi = in_method.findInstruction(tn.dflt.getLabel());
	 if (fi != null) rslt.add(fi);
       }
      return rslt;
    }
   return null;
}


public Object getObjectValue()
{
   if (for_inst instanceof LdcInsnNode) {
      LdcInsnNode ld = (LdcInsnNode) for_inst;
      return ld.cst;
    }
   return null;
}



public int getStackDiff()
{
   int opc = getOpcode();
   switch (opc) {
      case INVOKESTATIC :
      case INVOKEDYNAMIC :
      case INVOKEVIRTUAL :
      case INVOKESPECIAL :
	 JcodeMethod fm = getMethodReference();
	 int ct = fm.getNumArguments();
	 if (!fm.isStatic()) ++ct;
	 if (!fm.getReturnType().isVoid()) --ct;
	 return -ct;
    }

   return opcode_stack_height[opc][1] - opcode_stack_height[opc][0];
}


public int getPoppedStackDiff()
{
   int opc = getOpcode();
   return -opcode_stack_height[opc][0];
}




/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

public String toString()
{
   String s = getIndex() + ": " + getString(for_inst,in_method);
   if (line_number > 0) s += " (line " + line_number +")";

   return s;
}




static String getString(AbstractInsnNode ain,JcodeMethod bm)
{
   StringBuffer buf = new StringBuffer();

   switch (ain.getType()) {
      case AbstractInsnNode.FIELD_INSN :
	 FieldInsnNode fin = (FieldInsnNode) ain;
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" ");
	 buf.append(fin.owner);
	 buf.append(".");
	 buf.append(fin.name);
	 buf.append(" (");
	 buf.append(fin.desc);
	 buf.append(")");
	 break;
      case AbstractInsnNode.IINC_INSN :
	 IincInsnNode iin = (IincInsnNode) ain;
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" L");
	 buf.append(iin.var);
	 buf.append(",");
	 buf.append(iin.incr);
	 break;
      case AbstractInsnNode.INT_INSN :
	 IntInsnNode iin1 = (IntInsnNode) ain;
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" ");
	 buf.append(iin1.operand);
	 break;
      case AbstractInsnNode.JUMP_INSN :
	 JumpInsnNode jin = (JumpInsnNode) ain;
	 JcodeInstruction tins = bm.findInstruction(jin.label.getLabel());
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" ");
	 buf.append(tins.getIndex());
	 break;
      case AbstractInsnNode.LDC_INSN :
	 LdcInsnNode lin = (LdcInsnNode) ain;
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" '");
	 if (lin.cst instanceof String) {
	    String xs = (String) lin.cst;
	    for (int i = 0; i < xs.length(); ++i) {
	       char c = xs.charAt(i);
	       if (c == '\n') buf.append("\\n");
	       else if (c == '\t') buf.append("\\t");
	       else if (c == '\r') buf.append("\\r");
	       if (c < 32 || c >= 127) {
		  buf.append("&");
		  buf.append(Integer.toString(c,16));
		  buf.append(";");
		}
	       else buf.append(c);
	     }
	    buf.append("'");
	  }
	 else buf.append(lin.cst.toString());
	 break;
      case AbstractInsnNode.LOOKUPSWITCH_INSN :
	 LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) ain;
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" ");
	 int sz = lsin.keys.size();
	 for (int i = 0; i < sz; ++i) {
	    buf.append(lsin.keys.get(i).toString());
	    buf.append("=>");
	    LabelNode ln = lsin.labels.get(i);
	    buf.append(ln.getLabel().toString());
	    buf.append(",");
	  }
	 if (lsin.dflt != null) {
	    buf.append("?=>");
	    buf.append(lsin.dflt.getLabel().toString());
	  }
	 break;
      case AbstractInsnNode.METHOD_INSN :
	 MethodInsnNode min = (MethodInsnNode) ain;
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" ");
	 buf.append(min.owner);
	 buf.append(".");
	 buf.append(min.name);
	 buf.append(min.desc);
	 break;
      case AbstractInsnNode.MULTIANEWARRAY_INSN :
	 MultiANewArrayInsnNode mnain = (MultiANewArrayInsnNode) ain;
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" ");
	 buf.append(mnain.desc);
	 buf.append(",");
	 buf.append(mnain.dims);
	 break;
      case AbstractInsnNode.TABLESWITCH_INSN :
	 TableSwitchInsnNode tsin = (TableSwitchInsnNode) ain;
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" [");
	 buf.append(tsin.min);
	 buf.append("..");
	 buf.append(tsin.max);
	 buf.append("]=>");
	 for (int i = 0; i < tsin.labels.size(); ++i) {
	    LabelNode ln = tsin.labels.get(i);
	    buf.append(ln.getLabel().toString());
	    buf.append(",");
	  }
	 buf.append("?=>");
	 if (tsin.dflt != null) buf.append(tsin.dflt.getLabel().toString());
	 break;
      case AbstractInsnNode.TYPE_INSN :
	 TypeInsnNode tin = (TypeInsnNode) ain;
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" ");
	 buf.append(tin.desc);
	 break;
      case AbstractInsnNode.VAR_INSN :
	 VarInsnNode vin = (VarInsnNode) ain;
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 buf.append(" L");
	 buf.append(vin.var);
	 break;
      case AbstractInsnNode.INSN :
	 buf.append(OPCODE_NAMES[ain.getOpcode()]);
	 break;
      case AbstractInsnNode.LABEL :
	 LabelNode lbln = (LabelNode) ain;
	 buf.append(lbln.getLabel().toString());
	 buf.append(":");
	 break;
    }

   return buf.toString();
}




}	// end of class JcodeInstruction




/* end of JcodeInstruction.java */

