/********************************************************************************/
/*										*/
/*		CinderInstrumenter.java 					*/
/*										*/
/*	Interface for different types of instrumentation			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/cinder/CinderInstrumenter.java,v 1.24 2018/08/02 15:09:29 spr Exp $ */


/*********************************************************************************
 *
 * $Log: CinderInstrumenter.java,v $
 * Revision 1.24  2018/08/02 15:09:29  spr
 * Fix imports
 *
 * Revision 1.23  2015/11/20 15:09:10  spr
 * Reformatting.
 *
 * Revision 1.22  2010-07-01 21:54:24  spr
 * Catch errors in writing xml.
 *
 * Revision 1.21  2009-09-17 01:54:51  spr
 * Enable patching at line number level.
 *
 * Revision 1.20  2008-12-04 17:43:45  spr
 * Remove extra prints.
 *
 * Revision 1.19  2008-12-04 17:42:28  spr
 * Add debugging for mac
 *
 * Revision 1.18  2008-05-29 23:24:18  spr
 * Add call to build field load instruction.
 *
 * Revision 1.17  2007-08-10 02:10:02  spr
 * Cleanups from Eclipse
 *
 * Revision 1.16  2007-05-04 01:59:07  spr
 * Fix up line number methods.
 *
 * Revision 1.15  2006-12-01 03:22:37  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.14  2006/07/10 14:52:09  spr
 * Code cleanup.
 *
 * Revision 1.13  2005/09/19 23:32:00  spr
 * Fixups based on findbugs.
 *
 * Revision 1.12  2005/09/02 14:42:17  spr
 * Add file to source location.
 *
 * Revision 1.11  2005/07/08 20:56:58  spr
 * Upgrade patching to handle constructors; add call to create local variable.
 *
 * Revision 1.10  2005/06/07 02:18:19  spr
 * Update for java 5.0
 *
 * Revision 1.9  2005/05/07 22:25:39  spr
 * Updates for java 5.0
 *
 * Revision 1.8  2005/04/28 21:48:04  spr
 * Check for loading problems; add special call creation method.
 *
 * Revision 1.7  2004/09/20 22:21:49  spr
 * Augment block output with counts and allocation types.
 *
 * Revision 1.6  2004/07/08 19:49:06  spr
 * Add utility method to generate a string load instruction.
 *
 * Revision 1.5  2004/05/05 02:28:08  spr
 * Update import lists using eclipse.
 *
 * Revision 1.4  2004/02/26 02:57:03  spr
 * Make methods final that shouldn't be overridden.
 *
 * Revision 1.3  2003/12/17 21:23:42  spr
 * Add start class callback at the start of a class.
 *
 * Revision 1.2  2003/06/07 02:59:22  spr
 * Cleanups prompted by eclipse
 *
 * Revision 1.1  2003/03/29 03:40:25  spr
 * Move CINDER interface to JikesBT from Bloom to Ivy.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.cinder;


import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_ANewArrayIns;
import com.ibm.jikesbt.BT_AttributeVector;
import com.ibm.jikesbt.BT_BasicBlockMarkerIns;
import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_ClassVector;
import com.ibm.jikesbt.BT_CodeAttribute;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_Item;
import com.ibm.jikesbt.BT_LineNumberAttribute;
import com.ibm.jikesbt.BT_Local;
import com.ibm.jikesbt.BT_LocalVariableAttribute;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_MultiANewArrayIns;
import com.ibm.jikesbt.BT_NewArrayIns;
import com.ibm.jikesbt.BT_NewIns;
import com.ibm.jikesbt.BT_Opcodes;

import org.w3c.dom.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;




public abstract class CinderInstrumenter implements CinderConstants
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private CinderPatcher		for_patcher;
private BT_Class		current_class;
private BT_Method		current_method;
private Map<BT_Ins,Integer>	current_lines;

private String			counter_file;
private int			current_counter;
private int			max_counter;

private int			min_id;
private int			max_id;

private Map<BT_Method,Integer>	method_table;
private Map<BT_Class,Integer>	class_table;
private Map<BT_Ins,Integer>	block_table;
private Map<BT_Ins,Vector<BT_Ins>>  branch_table;

private String			source_path;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/


protected CinderInstrumenter()
{
   for_patcher = null;
   current_class = null;
   current_method = null;
   current_lines = null;

   method_table = null;
   class_table = null;
   block_table = null;
   branch_table = null;

   counter_file = null;
   current_counter = 1;
   max_counter = 0;
   min_id = 0;
   max_id = 0;

   source_path = System.getProperty("edu.brown.cs.ivy.SOURCEPATH");
   if (source_path == null) {
      source_path = System.getProperty("java.class.path");
      source_path = source_path + File.pathSeparator + ".";
    }
}




/********************************************************************************/
/*										*/
/*	Handle command line arguments for this patcher				*/
/*										*/
/********************************************************************************/

public void setArguments(String args)					{ }




/********************************************************************************/
/*										*/
/*	Top level setup methods 						*/
/*										*/
/********************************************************************************/

public final boolean setupFiles(BT_Class cls,String inf,String outf)
{
   method_table = null;
   class_table = null;
   block_table = null;
   branch_table = null;

   return localSetupFiles(cls,inf,outf);
}


protected boolean localSetupFiles(BT_Class cls,String inf,String outf)	{ return true; }




public final boolean setupMethod(BT_Class bc,BT_Method bm)
{
   current_class = bc;
   current_method = bm;
   current_lines = null;
   if (block_table == null) block_table = new HashMap<BT_Ins,Integer>();
   if (branch_table == null) branch_table = new HashMap<BT_Ins,Vector<BT_Ins>>();

   return useMethod(bc,bm);
}


protected boolean useMethod(BT_Class bc,BT_Method bm)		{ return true; }



public void finishMethod()					{ }

public void startClass(BT_Class bc)				{ }

public void finishClass(BT_Class bc)				{ }

public void finish()						{ }



public int getMinimumId()					{ return min_id; }

public int getMaximumId()					{ return max_id; }




/********************************************************************************/
/*										*/
/*	Actual patching methods 						*/
/*										*/
/********************************************************************************/

public int patchMethodEntry(int nargs,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}


public int patchStartBlock(int ct,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}


public int patchMethodExit(boolean v,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}


public int patchBaseArrayAlloc(int typ,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}


public int patchAlloc(BT_Class bc,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}


public int patchSync(boolean enter,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}


public int patchStaticAccess(BT_Field fld,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}


public int patchFieldAccess(BT_Field fld,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}


public int patchStaticWrite(BT_Field fld,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}


public int patchFieldWrite(BT_Field fld,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}


public int patchCall(BT_Method called,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}



public int patchAny(BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}




/********************************************************************************/
/*										*/
/*	Help methods								*/
/*										*/
/********************************************************************************/

protected int getCurrentOffset()
{
   return for_patcher.getCurrentOffset();
}



/********************************************************************************/
/*										*/
/*	Local access methods							*/
/*										*/
/********************************************************************************/

void setPatcher(CinderPatcher cp)		{ for_patcher = cp; }




/********************************************************************************/
/*										*/
/*	Instruction creation methods						*/
/*										*/
/********************************************************************************/

public BT_Ins createIntegerLoad(int val)
{
   BT_Ins rslt = null;

   if (val == -1) rslt = BT_Ins.make(BT_Opcodes.opc_iconst_m1);
   else if (val >= 0 && val <= 5) rslt = BT_Ins.make(BT_Opcodes.opc_iconst_0 + val);
   else if (val >= 0 || val <= 127) rslt = BT_Ins.make(BT_Opcodes.opc_bipush,val);
   else if (val >= -127 && val < 0) rslt = BT_Ins.make(BT_Opcodes.opc_bipush,(val&0xff));
   else {
      int cidx = current_class.pool.indexOfInteger(val);
      rslt = BT_Ins.make(BT_Opcodes.opc_ldc,cidx);
    }

   return rslt;
}



public BT_Ins createStringLoad(String s)
{
   BT_Ins rslt = null;

   if (s == null) rslt = BT_Ins.make(BT_Opcodes.opc_aconst_null);
   else rslt = BT_Ins.make(BT_Opcodes.opc_ldc,s);

   return rslt;
}



public BT_Ins createNullLoad()
{
   return BT_Ins.make(BT_Opcodes.opc_aconst_null);
}



public BT_Ins createLocalLoad(int idx,BT_Class typ)
{
   BT_Ins rslt = null;
   int opc;

   if (typ == null) opc = BT_Opcodes.opc_aload;
   else opc = typ.getOpcodeForLoadLocal();

   if (idx <= 3) {
      if (opc == BT_Opcodes.opc_aload) opc = BT_Opcodes.opc_aload_0;
      else if (opc == BT_Opcodes.opc_iload) opc = BT_Opcodes.opc_iload_0;
      else if (opc == BT_Opcodes.opc_dload) opc = BT_Opcodes.opc_dload_0;
      else if (opc == BT_Opcodes.opc_fload) opc = BT_Opcodes.opc_fload_0;
      else if (opc == BT_Opcodes.opc_lload) opc = BT_Opcodes.opc_lload_0;
      opc += idx;
      rslt = BT_Ins.make(opc);
    }
   else {
      rslt = BT_Ins.make(opc,idx);
    }

   return rslt;
}



public BT_Ins createLocalStore(int idx,BT_Class typ)
{
   BT_Ins rslt = null;
   int opc;

   if (typ == null) opc = BT_Opcodes.opc_astore;
   else opc = typ.getOpcodeForStoreLocal();

   if (idx <= 3) {
      if (opc == BT_Opcodes.opc_astore) opc = BT_Opcodes.opc_astore_0;
      else if (opc == BT_Opcodes.opc_istore) opc = BT_Opcodes.opc_istore_0;
      else if (opc == BT_Opcodes.opc_dstore) opc = BT_Opcodes.opc_dstore_0;
      else if (opc == BT_Opcodes.opc_fstore) opc = BT_Opcodes.opc_fstore_0;
      else if (opc == BT_Opcodes.opc_lstore) opc = BT_Opcodes.opc_lstore_0;
      opc += idx;
      rslt = BT_Ins.make(opc);
    }
   else {
      rslt = BT_Ins.make(opc,idx);
    }

   return rslt;
}



public BT_Ins createFieldLoad(BT_Field fld)
{
   BT_Ins rslt = null;
   int opc;

   if (fld.isStatic()) opc = BT_Opcodes.opc_getstatic;
   else opc = BT_Opcodes.opc_getfield;

   rslt = BT_Ins.make(opc,fld);

   return rslt;
}


public BT_Ins createCall(BT_Method bm)
{
   return BT_Ins.make(bm.getOpcodeForInvoke(),bm);
}



public BT_Ins createSpecialCall(BT_Method bm)
{
   int opc = bm.getOpcodeForInvoke();
   if (opc == BT_Opcodes.opc_invokevirtual) opc = BT_Opcodes.opc_invokespecial;

   return BT_Ins.make(opc,bm);
}



public BT_Local createLocalVariable(BT_Method bm,String nm,BT_Class typ,BT_Ins si,BT_Ins ei)
{
   BT_CodeAttribute cod = bm.getCode();
   BT_LocalVariableAttribute lva = (BT_LocalVariableAttribute) cod.attributes.getAttribute("LocalVariableTable");
   int mxl = 0;

   if (lva == null) {
      lva = new BT_LocalVariableAttribute(0, cod);
      cod.attributes.addElement(lva);
    }
   else {
      for (int i = 0; i < lva.localVariables.length; ++i) {
	 if (lva.localVariables[i].localIndex >= mxl) mxl = lva.localVariables[i].localIndex+1;
       }
    }

   BT_LocalVariableAttribute.LV lv = new BT_LocalVariableAttribute.LV(si,ei,nm,typ,mxl);
   int sz = lva.localVariables.length;
   BT_LocalVariableAttribute.LV [] narr = new BT_LocalVariableAttribute.LV[sz+1];
   for (int i = 0; i < sz; ++i) narr[i] = lva.localVariables[i];
   narr[sz] = lv;
   lva.localVariables = narr;

   return new BT_Local(mxl);
}




/********************************************************************************/
/*										*/
/*	Stub creation methods							*/
/*										*/
/********************************************************************************/

protected void createThrowStub(BT_Class bc,BT_Method bm)
{
   String mnam = bm.getName();
   if (mnam.startsWith("CINDER_PRIVATE_THROW_")) return;
   if (bm.isNative()) return;

   BT_MethodSignature msgn = bm.getSignature();
   int mid = findMethodId(bm);

   String nnm = "CINDER_PRIVATE_THROW_" + mnam + "_" + mid;

   short fgs = BT_Item.PRIVATE;
   if (bm.isStatic()) fgs |= BT_Item.STATIC;
   boolean synfg = bm.isSynchronized() && !bm.isStatic();	//***** handle static sync methods
   if (synfg) bm.flags &= ~ BT_Item.SYNCHRONIZED;

   BT_Method nbm = BT_Method.createMethod(bc,fgs,msgn,nnm,null);

   BT_CodeAttribute cod = createThrowStubCode(nbm,synfg,mid);

   nbm.setCode(bm.getCode());
   bm.setCode(cod);
}



private BT_CodeAttribute createThrowStubCode(BT_Method bm,boolean synfg,int mid)
{
   BT_InsVector iv = new BT_InsVector();
   BT_MethodSignature msgn = bm.getSignature();

   BT_Ins ins;
   int sidx,eidx,cidx;
   int aidx = 0;

   if (synfg) {
      ins = BT_Ins.make(BT_Opcodes.opc_aload_0);
      iv.addElement(ins);
      ins = BT_Ins.make(BT_Opcodes.opc_monitorenter);
      iv.addElement(ins);
    }

   sidx = iv.size();
   BT_ClassVector atyps = msgn.types;
   if (!bm.isStatic()) {
      ins = BT_Ins.make(BT_Opcodes.opc_aload_0);
      iv.addElement(ins);
      ++aidx;
    }

   for (int i = 0; i < atyps.size(); ++i ) {
      BT_Class abc = atyps.elementAt(i);
      ins = createLocalLoad(aidx++,abc);
      if (isCategory2(abc)) ++aidx;
      iv.addElement(ins);
    }

   ins = createCall(bm);
   iv.addElement(ins);
   eidx = iv.size();

   if (synfg) {
      ins = BT_Ins.make(BT_Opcodes.opc_aload_0);
      iv.addElement(ins);
      ins = BT_Ins.make(BT_Opcodes.opc_monitorexit);
      iv.addElement(ins);
    }

   ins = BT_Ins.make(msgn.returnType.getOpcodeForReturn());
   iv.addElement(ins);

   cidx = iv.size();

   if (synfg) {
      ins = BT_Ins.make(BT_Opcodes.opc_aload_0);
      iv.addElement(ins);
      ins = BT_Ins.make(BT_Opcodes.opc_monitorexit);
      iv.addElement(ins);
    }

   ins = BT_Ins.make(BT_Opcodes.opc_athrow);
   iv.addElement(ins);

   BT_CodeAttribute cod = new BT_CodeAttribute(iv.toArray());
   BT_Class throwcls = BT_Class.forName("java.lang.Throwable");
   cod.setExceptionHandler(sidx,eidx,cidx,throwcls);

   return cod;
}





/********************************************************************************/
/*										*/
/*	utility methods 							*/
/*										*/
/********************************************************************************/


protected int getBlockEndIndex(int sidx)
{
   BT_CodeAttribute bc = current_method.getCode();
   BT_InsVector iv = bc.ins;

   int lidx = sidx;

   for (int i = sidx+1; i < iv.size(); ++i) {
      BT_Ins bi = iv.elementAt(i);
      if (bi instanceof BT_BasicBlockMarkerIns) break;
      lidx = i;
    }

   return lidx;
}



/********************************************************************************/
/*										*/
/*	Methods for handling line numbers					*/
/*										*/
/********************************************************************************/

final public void setLineTable(BT_Method bm)
{
   BT_LineNumberAttribute lns = null;
   BT_CodeAttribute ca = bm.getCode();
   if (ca != null) {
      BT_AttributeVector av = ca.attributes;
      lns = (BT_LineNumberAttribute) av.getAttribute("LineNumberTable");
    }
   setLineTable(lns);
}




final void setLineTable(BT_LineNumberAttribute lns)
{
   if (lns == null) {
      current_lines = null;
    }
   else  {
      current_lines = new HashMap<>();
      for (int i = 0; i < lns.pcRanges.length; ++i) {
	 current_lines.put(lns.pcRanges[i].startIns,Integer.valueOf(lns.pcRanges[i].lineNumber));
       }
    }
}



public int getLineNumber(int idx)
{
   BT_CodeAttribute bc = current_method.getCode();
   if (bc == null) return 0;
   BT_InsVector iv = bc.ins;

   if (current_lines == null) return 0;
   if (idx < 0) idx = 0;

   while (idx >= 0) {
      BT_Ins bi = iv.elementAt(idx);
      Integer ivl = current_lines.get(bi);
      if (ivl != null) {
	 return ivl.intValue();
       }
      --idx;
    }

   return 0;
}



public int getMinLineNumber()
{
   if (current_lines == null) return 0;

   int ln = 0;
   for (Iterator<Integer> it = current_lines.values().iterator(); it.hasNext(); ) {
      Integer iv = it.next();
      if (ln == 0 || iv.intValue() < ln) ln = iv.intValue();
    }

   return ln;
}




public int getMaxLineNumber()
{
   if (current_lines == null) return 0;

   int ln = 0;
   for (Iterator<Integer> it = current_lines.values().iterator(); it.hasNext(); ) {
      Integer iv = it.next();
      if (iv.intValue() > ln) ln = iv.intValue();
    }

   return ln;
}




public int getLineNumber(BT_Method bm,int idx)
{
   setLineTable(bm);

   BT_CodeAttribute bc = bm.getCode();
   if (bc == null) return 0;
   BT_InsVector iv = bc.ins;

   if (current_lines == null) return 0;
   if (idx < 0) idx = 0;

   while (idx >= 0) {
      BT_Ins bi = iv.elementAt(idx);
      Integer ivl = current_lines.get(bi);
      if (ivl != null) {
	 return ivl.intValue();
       }
      --idx;
    }

   return 0;
}




/********************************************************************************/
/*										*/
/*	Methods for handling methods and blocks 				*/
/*										*/
/********************************************************************************/

protected final void setCounterFile(String file)
{
   if (file != null && counter_file != null && counter_file.equals(file)) return;

   counter_file = file;
   readCounter();
}



public int findMethodId()
{
   return findMethodId(current_method);
}



public int findMethodId(BT_Method bm)
{
   if (bm == null) return 0;
   if (method_table == null) method_table = new HashMap<BT_Method,Integer>();

   Integer ivl = method_table.get(bm);
   if (ivl == null) {
      ivl = Integer.valueOf(getNewId());
      method_table.put(bm,ivl);
    }

   return ivl.intValue();
}




public int findClassId()
{
   return findClassId(current_class);
}


public int findClassId(BT_Class bc)
{
   if (bc == null) return 0;
   if (class_table == null) class_table = new HashMap<>();

   Integer ivl = class_table.get(bc);
   if (ivl == null) {
      ivl = Integer.valueOf(getNewId());
      class_table.put(bc,ivl);
    }

   return ivl.intValue();
}




public int findBlockId(BT_Ins ins)
{
   if (current_method == null) return -1;

   Integer ivl = block_table.get(ins);
   if (ivl == null) {
      ivl = Integer.valueOf(getNewId());
      block_table.put(ins,ivl);
    }

   return ivl.intValue();
}



protected final int getNewId()
{
   while (current_counter >= max_counter) readCounter();

   int id = current_counter++;
   if (min_id == 0 || id < min_id) min_id = id;
   if (id > max_id) max_id = id;
   return id;
}



protected void dumpMethod(IvyXmlWriter xw)
{
   dumpMethod(xw,current_method);
}



protected void dumpClass(IvyXmlWriter xw,BT_Class bc)
{
   int idx = findClassId(bc);
   xw.begin("CLASS");
   xw.field("NAME",bc.getName());
   xw.field("INDEX",idx);

   String sfl = getSourceFile(bc);
   if (sfl != null) xw.field("FILE",sfl);

   localClassDump(xw,bc);

   xw.end();
}



protected void localClassDump(IvyXmlWriter xw,BT_Class bc)	      { }





protected void dumpMethod(IvyXmlWriter xw,BT_Method mid)
{
   int idx = findMethodId(mid);
   xw.begin("METHOD");
   xw.field("NAME",CinderManager.getMethodName(mid));
   xw.field("SIGNATURE",mid.getSignature().toString());
   xw.field("INDEX",idx);

   setLineTable(mid);

   String sfl = getSourceFile(mid.getDeclaringClass());
   if (sfl != null) xw.field("FILE",sfl);
   xw.field("START",getMinLineNumber());
   xw.field("END",getMaxLineNumber());

   localMethodDump(xw,mid);

   dumpBlocks(xw,mid);

   xw.end();
}




protected void localMethodDump(IvyXmlWriter xw,BT_Method mid)	      { }




private void dumpBlocks(IvyXmlWriter xw,BT_Method mid)
{
   BT_CodeAttribute bc = mid.getCode();
   if (bc == null) return;

   BT_InsVector iv = bc.ins;
   Integer lblk = null;
   int sidx = 0;

   for (int i = 0; i < iv.size(); ++i) {
      BT_Ins ins = iv.elementAt(i);
      Integer nblk = block_table.get(ins);
      if (nblk != null) {
	 if (lblk != null) outputBlock(xw,iv,lblk.intValue(),sidx,i-1);
	 lblk = nblk;
	 sidx = i;
       }
    }

   if (lblk != null) outputBlock(xw,iv,lblk.intValue(),sidx,iv.size()-1);
}




private void outputBlock(IvyXmlWriter xw,BT_InsVector iv,int bno,int sidx,int eidx)
{
   xw.begin("BLOCK");
   xw.field("METHOD",findMethodId());
   xw.field("INDEX",bno);
   xw.field("STARTINS",sidx);
   xw.field("ENDINS",eidx);

   int slin = getLineNumber(sidx);
   int elin = getLineNumber(eidx);
   if (slin > elin) {
      int tmp = slin;
      slin = elin;
      elin = tmp;
    }
   xw.field("START",slin);
   xw.field("END",elin);

   for (int i = sidx; i <= eidx; ++i) {
      BT_Ins ins = iv.elementAt(i);
      if (ins instanceof BT_MultiANewArrayIns) {
	 BT_MultiANewArrayIns nins = (BT_MultiANewArrayIns) ins;
	 xw.begin("ALLOC");
	 xw.field("ARRAY",nins.dimensions);
	 xw.field("CLASS",nins.getClassTarget().getName());
	 xw.end();
       }
      else if (ins instanceof BT_ANewArrayIns) {
	 BT_ANewArrayIns nins = (BT_ANewArrayIns) ins;
	 xw.begin("ALLOC");
	 xw.field("ARRAY",1);
	 xw.field("CLASS",nins.getClassTarget().getName());
	 xw.end();
       }
      else if (ins instanceof BT_NewIns) {
	 BT_NewIns nins = (BT_NewIns) ins;
	 xw.begin("ALLOC");
	 xw.field("CLASS",nins.getClassTarget().getName());
	 xw.end();
       }
      else if (ins instanceof BT_NewArrayIns) {
	 BT_NewArrayIns nins = (BT_NewArrayIns) ins;
	 xw.begin("ALLOC");
	 xw.field("ARRAY",1);
	 xw.field("CLASS",nins.getClassTarget().getName());
	 xw.end();
       }
    }

   if (branch_table != null) {
      BT_Ins sins = iv.elementAt(sidx);
      Vector<BT_Ins> v = branch_table.get(sins);
      if (v != null) {
	 for (Iterator<BT_Ins> it = v.iterator(); it.hasNext(); ) {
	    BT_Ins tins = it.next();
	    Integer tvl = block_table.get(tins);
	    if (tvl != null) {
	       xw.begin("BRANCH");
	       xw.field("TO",tvl.intValue());
	       xw.end();
	     }
	  }
       }
    }

   xw.end();
}




private void readCounter()
{
   if (counter_file == null) max_counter += 256;
   else {
      String lnm = counter_file + ".lock";
      File lf = new File(lnm);

      try {
	 for (int i = 0; i < 20; ++i) {
	    boolean fg = lf.createNewFile();
	    if (fg) break;
	    Thread.sleep(i*10);
	  }

	 Node xml = IvyXml.loadXmlFromFile(counter_file);
	 if (xml == null) current_counter = 1;
	 else current_counter = IvyXml.getAttrInt(xml,"COUNTER");
	 if (current_counter <= 0) current_counter = 1;
       }
      catch (Throwable e) {
	 current_counter = 1;
       }


      max_counter = current_counter + 256;

      try {
	 IvyXmlWriter xw = new IvyXmlWriter(new FileWriter(counter_file));
	 xw.begin("TAJ");
	 xw.field("COUNTER",max_counter);
	 xw.end();
	 xw.close();
       }
      catch (IOException e) {
	 System.err.println("CINDER: Problem updating xml file " + counter_file);
       }

      lf.delete();
    }
}



/********************************************************************************/
/*										*/
/*	Items for maintaining the branch table					*/
/*										*/
/********************************************************************************/

protected void noteBlockBranch(BT_Ins from,BT_Ins to)
{
   Vector<BT_Ins> v = branch_table.get(from);
   if (v == null) {
      v = new Vector<BT_Ins>();
      branch_table.put(from,v);
    }
   v.add(to);
}




/********************************************************************************/
/*										*/
/*	Items for checking for type 2 types					*/
/*										*/
/********************************************************************************/

protected boolean isCategory2(BT_Class c)
{
   if (c == BT_Class.getDouble() || c == BT_Class.getLong()) return true;
   return false;
}




/********************************************************************************/
/*										*/
/*	Method for finding actual source path for a class			*/
/*										*/
/********************************************************************************/

public void setSourcePath(String sourcepath)
{
   source_path = sourcepath;
}



protected String getSourceFile(BT_Class c)
{
   String s = c.getSourceFile();
   if (s.startsWith(File.separator)) return s;
   if (source_path == null) return s;

   String pfx = c.getName();
   int idx = pfx.lastIndexOf('.');
   if (idx >= 0) {
      pfx = pfx.substring(0,idx);
      pfx = pfx.replace('.',File.separatorChar);
      pfx += File.separator;
    }
   else pfx = "";

   StringTokenizer tok = new StringTokenizer(source_path,File.pathSeparator);
   while (tok.hasMoreTokens()) {
      String dir = tok.nextToken();
      File f = new File(dir + File.separator + pfx + s);
      if (f.exists()) return f.getAbsolutePath();
    }

   return s;
}



protected String getSourceLocation()
{
   int idx = for_patcher.getCurrentIndex() - for_patcher.getCurrentOffset();

   StringBuffer buf = new StringBuffer();

   buf.append(current_class.getName());
   buf.append(".");
   buf.append(current_method.getName());

   String fil = getSourceFile(current_class);
   buf.append("@" + fil);
   int ln = getLineNumber(idx);
   if (ln > 0) buf.append(":" + ln);

   return buf.toString();
}



}	// end of abstract class CinderInstrumenter




/* end of CinderInstrumenter.java */



