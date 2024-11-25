/********************************************************************************/
/*										*/
/*		FlowIncremental.java						*/
/*										*/
/*	Implementation of incremental flow analysis on top of jflow		*/
/*										*/
/********************************************************************************/
/*	Copyright 2006 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2006, Brown University, Providence, RI.				 *
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


package edu.brown.cs.ivy.jflow.flow;


import edu.brown.cs.ivy.jflow.JflowException;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_CodeAttribute;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_Opcodes;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;




class FlowIncremental implements BT_Opcodes
{



/********************************************************************************/
/*										*/
/*	Local Storage								*/
/*										*/
/********************************************************************************/

private FlowMaster     jflow_master;
private Set<BT_Class> class_set;
private Map<BT_Class,File> changed_classes;
private Collection<BT_Method> changed_methods;
private Map<BT_Method,MethodData> method_map;
private Map<BT_Field,FieldData> field_map;
private Collection<SourceBase> remove_sources;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

FlowIncremental(FlowMaster jm)
{
   jflow_master = jm;
   class_set = new HashSet<>();
   changed_classes = null;
   changed_methods = null;
   method_map = new HashMap<>();
   field_map = new HashMap<>();
   remove_sources = null;
}



/********************************************************************************/
/*										*/
/*	Top-level entries							*/
/*										*/
/********************************************************************************/

void noteChanged(File f)
{
   String cnm = null;
   String snm = null;

   if (!f.exists()) {
      System.err.println("JFLOW: Changed file " + f + " not found");
    }

   long dlm = f.lastModified();

   String fnm = f.getName();
   int idx = fnm.lastIndexOf('.');
   if (fnm.endsWith(".class")) {
      cnm = fnm.substring(0,idx);
    }
   else if (fnm.endsWith(".java")) {
      snm = fnm.substring(0,idx);
    }

   for (BT_Class bc : class_set) {
      String cs = bc.getName();
      String cs1 = cs;
      idx = cs.lastIndexOf('.');
      if (idx >= 0) cs1 = cs.substring(idx+1);
      boolean chk = false;
      if (cnm != null && cnm.equals(cs1)) chk = true;
      if (snm != null && snm.equals(cs1)) chk = true;
      String cf = bc.getSourceFile();
      if (cf != null) {
	 idx = cf.lastIndexOf(".");
	 if (idx > 0) cf = cf.substring(0,idx);
	 if (cf != null && cf.endsWith(fnm)) chk = true;
       }
      if (chk) {
	 System.err.println("Check " + bc + " " + dlm + " " + bc.lastModificationTime);
	 if (dlm > bc.lastModificationTime) {
	    if (changed_classes == null) changed_classes = new HashMap<>();
	    changed_classes.put(bc,f);
	    System.err.println("ADD CHANGED " + bc);
	  }
       }
    }
}



void noteChanged(BT_Method bm)
{
   if (changed_methods == null) changed_methods = new HashSet<BT_Method>();
   changed_methods.add(bm);
   System.err.println("ADD CHANGED METHOD " + bm);
}




/********************************************************************************/
/*										*/
/*	Methods to record current state 					*/
/*										*/
/********************************************************************************/

void noteMethodUsed(BT_Method bm)
{
   BT_Class bc = bm.getDeclaringClass();
   if (!class_set.contains(bc)) {
      class_set.add(bc);
      for (Enumeration<?> e = bc.getFields().elements(); e.hasMoreElements(); ) {
	 BT_Field bf = (BT_Field) e.nextElement();
	 field_map.put(bf,new FieldData(bf));
       }
    }

   MethodData md = method_map.get(bm);
   if (md == null) {
      md = new MethodData(bm);
      method_map.put(bm,md);
    }
}




/********************************************************************************/
/*										*/
/*	Updating Methods							*/
/*										*/
/********************************************************************************/

void updateChanged() throws JflowException
{
   if (changed_classes == null && changed_methods == null) return;

   remove_sources = new ArrayList<SourceBase>();

   if (changed_classes != null) {
      for (Map.Entry<BT_Class,File> ent : changed_classes.entrySet()) {
	 updateClass(ent.getKey(),ent.getValue());
       }
    }

   if (changed_methods != null) {
      for (BT_Method bm : changed_methods) {
	 invalidateMethod(bm);
       }
    }

   jflow_master.analyzeUpdate(remove_sources);

   changed_classes = null;
   changed_methods = null;
   remove_sources = null;
}



private void updateClass(BT_Class bc,File f)
{
   System.err.println("WORK ON UPDATING " + bc + " " + f);
   if (!f.exists()) {
      System.err.println("Update file " + f + " doesn't exist");
      return;
    }
   try {
      bc.becomeStub();
      bc = BT_Class.loadFromFile(f);
    }
   catch (Exception e) {
      System.err.println("Problem loading updated class " + bc + ": " + e);
    }

   for (Enumeration<?> e = bc.getFields().elements(); e.hasMoreElements(); ) {
      BT_Field bf = (BT_Field) e.nextElement();
      FieldData fd = new FieldData(bf);
      FieldData ofd = field_map.get(bf);
      if (ofd != null && !ofd.sameHash(fd)) {
	 System.err.println("FIELD CHANGED: " + bf.fullName());
       }
    }

   for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
      BT_Method bm = (BT_Method) e.nextElement();
      MethodData md = new MethodData(bm);
      MethodData omd = method_map.get(bm);
      if (omd != null && !omd.sameHash(md)) {
	 System.err.println("METHOD CHANGED: " + bm.fullName());
	 invalidateMethod(bm);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Methods to invalidate a method						*/
/*										*/
/********************************************************************************/

private void invalidateMethod(BT_Method bm)
{
   for (MethodBase mb : jflow_master.getAllMethods(bm)) {
      mb.clearForUpdate(remove_sources);
    }

   jflow_master.handleChangedMethod(bm);
}



/********************************************************************************/
/*										*/
/*	MethodData -- information for updating a method 			*/
/*										*/
/********************************************************************************/

private static class MethodData {

   private byte [] method_hash;

   MethodData(BT_Method bm) {
      MemberDigest md = new MemberDigest(bm);
      method_hash = md.getSummary();
    }

   public boolean sameHash(MethodData md) {
      return Arrays.equals(method_hash,md.method_hash);
    }

}	// end of subclass MethodData




/********************************************************************************/
/*										*/
/*	FieldData -- information for updating a field				*/
/*										*/
/********************************************************************************/

private static class FieldData {

   private byte [] field_hash;

   FieldData(BT_Field bf) {
      MemberDigest md = new MemberDigest(bf);
      field_hash = md.getSummary();
    }

   public boolean sameHash(FieldData fd) {
      return Arrays.equals(field_hash,fd.field_hash);
    }

}	// end of subclass FieldData




/********************************************************************************/
/*										*/
/*	MemberDigest -- message digest for a method or field			*/
/*										*/
/********************************************************************************/

private static class MemberDigest {

   private byte [] byte_value;
   private MessageDigest message_digest;

   MemberDigest(BT_Method bm) {
      try {
         message_digest = MessageDigest.getInstance("MD5");
       }
      catch (NoSuchAlgorithmException e) {
         System.err.println("JFLOW: Can't find md5 digest algorithm: " + e);
         System.exit(1);
       }
      addToDigest(bm.getSignature());
      addToDigest(bm.flags);
      addToDigest(bm.getCode());
      byte_value = message_digest.digest();
      message_digest = null;
    }

   MemberDigest(BT_Field bf) {
      try {
         message_digest = MessageDigest.getInstance("MD5");
       }
      catch (NoSuchAlgorithmException e) {
         System.err.println("JFLOW: Can't find md5 digest algorithm: " + e);
         System.exit(1);
       }
      addToDigest(bf.type);
      byte_value = message_digest.digest();
      message_digest = null;
    }

   byte [] getSummary() 		{ return byte_value; }

   private void addToDigest(BT_MethodSignature sgn) {
      if (sgn != null) {
	 String s = sgn.toString();
	 message_digest.update(s.getBytes());
       }
    }

   private void addToDigest(short fgs) {
      message_digest.update((byte) ((fgs>>8)&0xff));
      message_digest.update((byte) (fgs&0xff));
    }

   private void addToDigest(BT_CodeAttribute cod) {
      if (cod != null) {
	 for (Enumeration<?> e = cod.ins.elements(); e.hasMoreElements(); ) {
	    BT_Ins ins = (BT_Ins) e.nextElement();
	    String s = ins.toString();
	    message_digest.update(s.getBytes());
	  }
       }
    }

   private void addToDigest(BT_Class bc) {
      if (bc != null) {
	 String nm = bc.getName();
	 message_digest.update(nm.getBytes());
       }
    }

}	// end of subclass MemberDigest



}	// end of class FlowIncremental




/* end of FlowIncremental.java */
