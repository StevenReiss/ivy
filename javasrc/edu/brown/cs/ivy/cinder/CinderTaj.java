/********************************************************************************/
/*										*/
/*		CinderTaj.java							*/
/*										*/
/*	Instrumentation for using the TAJ interface routines			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/cinder/CinderTaj.java,v 1.4 2015/11/20 15:09:11 spr Exp $ */


/*********************************************************************************
 *
 * $Log: CinderTaj.java,v $
 * Revision 1.4  2015/11/20 15:09:11  spr
 * Reformatting.
 *
 * Revision 1.3  2006-12-01 03:22:37  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.2  2004/05/05 02:28:08  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1  2003/03/29 03:40:25  spr
 * Move CINDER interface to JikesBT from Bloom to Ivy.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.cinder;


import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.*;

import java.io.*;
import java.util.StringTokenizer;



public class CinderTaj extends CinderInstrumenter
{


/********************************************************************************/
/*										*/
/*	Constants								*/
/*										*/
/********************************************************************************/

private static final String	CINDER_TAJ_PATH = "$(BLOOM)/java";
private static final String	CINDER_TAJ_CLASS = "edu.brown.bloom.taj.TajControl";

private static final String	CINDER_DATA_FILE = "tajdata.xml";



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private BT_Class	taj_class;

private BT_Method	entry_method;
private BT_Method	entry1_method;
private BT_Method	exit_method;
private BT_Method	exit1_method;
private BT_Method	moninside_method;
private BT_Method	monexit_method;
private BT_Method	access_method;
private BT_Method	write_method;
private BT_Method	block_method;
private BT_Method	alloc_method;
private BT_Method	array_method;

private String		data_directory;
private IvyXmlWriter  data_file;





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CinderTaj()
{
   String p = IvyFile.expandName(CINDER_TAJ_PATH);
   BT_Class.addClassPath(p);
   taj_class = BT_Class.forName(CINDER_TAJ_CLASS);

   entry_method = taj_class.findMethod("handleEntry","(int,java.lang.Object)");
   entry1_method = taj_class.findMethod("handleEntry1","(int,java.lang.Object,java.lang.Object)");
   exit_method = taj_class.findMethod("handleExit","(int)");
   exit1_method = taj_class.findMethod("handleExit1","(java.lang.Object,int)");
   moninside_method = taj_class.findMethod("handleMonitorInside","(java.lang.Object)");
   monexit_method = taj_class.findMethod("handleMonitorExit","(java.lang.Object)");
   access_method = taj_class.findMethod("handleObjectAccess","(java.lang.Object)");
   write_method = taj_class.findMethod("handleObjectWrite","(java.lang.Object)");
   block_method = taj_class.findMethod("handleBlockEntry","(int)");
   alloc_method = taj_class.findMethod("handleObjectAlloc","(java.lang.Object)");
   array_method = taj_class.findMethod("handleBaseArrayAlloc","(java.lang.Object,int)");

   data_directory = null;
}




/************************************************************************/
/*									*/
/*	Setup methods							*/
/*									*/
/************************************************************************/

@Override public void setArguments(String args)
{
   if (args == null) return;

   for (StringTokenizer tok = new StringTokenizer(args); tok.hasMoreTokens(); ) {
      tok.nextToken();
    }
}



@Override public boolean localSetupFiles(BT_Class cls,String infn,String outfn)
{
   File otf = new File(outfn);
   try {
      otf = otf.getCanonicalFile();
    }
   catch (IOException e) { }
   File dir = otf.getParentFile();

   if (dir == null) return false;

   if (data_directory == null || !data_directory.equals(dir.getPath())) {
      data_directory = dir.getPath();
      setCounterFile(data_directory + File.separator + CINDER_DATA_FILE);
    }

   String cnm = cls.className();
   StringBuffer buf = new StringBuffer();
   buf.append(dir.getPath());
   buf.append(File.separator);
   for (int i = 0; i < cnm.length(); ++i) {
      char c = cnm.charAt(i);
      if (c == '/' || c == '\\') buf.append("@");
      else buf.append(c);
    }
   buf.append(".taj");

   data_file = null;
   try {
      data_file = new IvyXmlWriter(new FileWriter(buf.toString()));
      data_file.begin("TAJ");
    }
   catch (IOException e) {
      System.err.println("CINDER: Can't create information file " + buf.toString());
    }

   return true;
}




@Override public void finishMethod()
{
   dumpMethod(data_file);
}



@Override public void finish()
{
   if (data_file != null) {
      data_file.end();
      data_file.close();
      data_file = null;
    }
}




/********************************************************************************/
/*										*/
/*	Patching methods for entry						*/
/*										*/
/********************************************************************************/

@Override public int patchMethodEntry(int nargs,BT_Method bm,BT_InsVector iv,int idx)
{
   if (nargs > 1) nargs = 1;

   int ctr = 0;
   int midx = findMethodId();

   BT_Ins i0 = createIntegerLoad(midx);
   iv.insertElementAt(i0,idx+ctr++);

   BT_Ins i1;
   if (bm.isInstanceMethod()) i1 = createLocalLoad(0,null);
   else i1 = BT_Ins.make(BT_Opcodes.opc_aconst_null);
   iv.insertElementAt(i1,idx+ctr++);

   if (nargs > 0) {
      BT_Ins i2;
      if (bm.isInstanceMethod()) i2 = BT_Ins.make(BT_Opcodes.opc_aload_1);
      else i2 = BT_Ins.make(BT_Opcodes.opc_aload_0);
      iv.insertElementAt(i2,idx+ctr++);
    }

   BT_Method pe = (nargs == 0 ? entry_method : entry1_method);
   BT_Ins i3 = createCall(pe);
   iv.insertElementAt(i3,idx+ctr++);

   return ctr;
}




/********************************************************************************/
/*										*/
/*	Patching methods for exit						*/
/*										*/
/********************************************************************************/

@Override public int patchMethodExit(boolean value,BT_Method bm,BT_InsVector iv,int idx)
{
   int midx = findMethodId();
   int ctr = 0;

   if (value) {
      BT_Ins i0 = BT_Ins.make(BT_Opcodes.opc_dup);
      iv.insertElementAt(i0,idx+ctr++);
    }
   BT_Ins i1 = createIntegerLoad(midx);
   iv.insertElementAt(i1,idx+ctr++);

   BT_Method pe = (value ? exit1_method : exit_method);
   BT_Ins i2 = createCall(pe);
   iv.insertElementAt(i2,idx+ctr++);

   return ctr;
}




/********************************************************************************/
/*										*/
/*	Patching methods for basic blocks					*/
/*										*/
/********************************************************************************/

@Override public int patchStartBlock(int ct,BT_Method bm,BT_InsVector iv,int idx)
{
   int bid = findBlockId(iv.elementAt(idx));

   int ctr = 0;

   BT_Ins i0 = createIntegerLoad(bid);
   iv.insertElementAt(i0,idx+1+ctr++);

   BT_Ins i1 = createCall(block_method);
   iv.insertElementAt(i1,idx+1+ctr++);

   return ctr;
}



/************************************************************************/
/*									*/
/*	Patching methods for allocation 				*/
/*									*/
/************************************************************************/

@Override public int patchAlloc(BT_Class bc,BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;

   BT_Ins i0 = BT_Ins.make(BT_Opcodes.opc_dup);
   iv.insertElementAt(i0,idx+1+ctr++);

   BT_Ins i1 = createCall(alloc_method);
   iv.insertElementAt(i1,idx+1+ctr++);

   return ctr;
}



@Override public int patchBaseArrayAlloc(int typ,BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;

   BT_Ins i0 = BT_Ins.make(BT_Opcodes.opc_dup);
   iv.insertElementAt(i0,idx+1+ctr++);

   BT_Ins i1 = createIntegerLoad(typ);
   iv.insertElementAt(i1,idx+1+ctr++);

   BT_Ins i2 = createCall(array_method);
   iv.insertElementAt(i2,idx+1+ctr++);

   return ctr;
}



/************************************************************************/
/*									*/
/*	Patching methods for synchronization				*/
/*									*/
/************************************************************************/

@Override public int patchSync(boolean enter,BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;
   int del = (enter ? 1 : 0);

   BT_Ins i0,i1;

   i0 = BT_Ins.make(BT_Opcodes.opc_dup);
   iv.insertElementAt(i0,idx+del+ctr++);

   if (enter) {
      i1 = createCall(moninside_method);
    }
   else {
      i1 = createCall(monexit_method);
    }

   iv.insertElementAt(i1,idx+del+ctr++);

   return ctr;
}




/************************************************************************/
/*									*/
/*	Patching methods for fields					*/
/*									*/
/************************************************************************/

@Override public int patchStaticAccess(BT_Field fld,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}



@Override public int patchFieldAccess(BT_Field fld,BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;
   int del = 1;

   BT_Ins i0 = BT_Ins.make(BT_Opcodes.opc_dup);
   iv.insertElementAt(i0,idx+del+ctr++);

   BT_Ins i1 = createCall(access_method);
   iv.insertElementAt(i1,idx+del+ctr++);

   return ctr;
}




@Override public int patchStaticWrite(BT_Field fld,BT_Method bm,BT_InsVector iv,int idx)
{
   return 0;
}



@Override public int patchFieldWrite(BT_Field fld,BT_Method bm,BT_InsVector iv,int idx)
{
   int ctr = 0;
   int del = 1;

   BT_Ins i0 = BT_Ins.make(BT_Opcodes.opc_dup);
   iv.insertElementAt(i0,idx+del+ctr++);

   BT_Ins i1 = createCall(write_method);
   iv.insertElementAt(i1,idx+del+ctr++);

   return ctr;
}




}	// end of interface CinderTaj




/* end of CinderTaj.java */
