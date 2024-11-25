/********************************************************************************/
/*										*/
/*		MethodSpecial.java						*/
/*										*/
/*	Holder for information about methods that need special handling 	*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 1998, Brown University, Providence, RI.				 *
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

import edu.brown.cs.ivy.cinder.CinderManager;
import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowFlags;
import edu.brown.cs.ivy.xml.IvyXml;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodSignature;
import com.ibm.jikesbt.BT_Repository;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;



final class MethodSpecial implements JflowConstants
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private String		method_name;
private String		class_name;
private BT_Class	result_type;
private boolean 	canbe_null;
private boolean 	is_mutable;
private boolean 	return_arg0;
private List<BT_Class>	load_items;
private String		replace_name;
private boolean 	dont_scan;
private boolean 	async_call;
private boolean 	array_copy;
private List<String>	call_backs;
private String		callback_id;
private List<Integer>	call_args;
private int		max_threads;
private boolean 	user_replace;
private boolean 	use_reflection;
private List<When>	when_cases;
private boolean 	does_exit;


private static Map<BT_Method,MethodSpecial> special_methods = new HashMap<>();
private static Set<String>		    callback_names = new HashSet<>();
private static Set<BT_Class>		    full_classes = new HashSet<>();



/********************************************************************************/
/*										*/
/*	Static Access methods							*/
/*										*/
/********************************************************************************/

public static void addSpecialFile(FlowMaster chet,CinderManager cm,Node xml)
{
   addMethodSpecials(chet,cm,xml);
}



static MethodSpecial getSpecial(BT_Method bm)
{
   return special_methods.get(bm);
}


static MethodSpecial getSpecial(MethodBase cfm)
{
   return getSpecial(cfm.getMethod());
}



static boolean canBeCallback(BT_Method bm)
{
   if (callback_names.contains(bm.fullName()) ||
	  callback_names.contains(bm.getName())) return true;

   return false;
}



static boolean isClassSpecial(BT_Class bc)
{
   return full_classes.contains(bc);
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private MethodSpecial(FlowMaster jm,CinderManager cm,Node xml)
{
   jflow_master = jm;
   method_name = IvyXml.getAttrString(xml,"NAME");

   if (method_name == null) {
      System.err.println("JFLOW: Missing method name in special definition");
      method_name = "*";
    }

   int idx = method_name.lastIndexOf('.');
   class_name = method_name.substring(0,idx);
   String tail = method_name.substring(idx+1);

   if (!CinderManager.checkIfClassExists(class_name)) {
      System.err.println("JFLOW: Class " + class_name + " for special methods not found for " +
			    method_name);
      method_name = null;
    }

   load_items = new Vector<BT_Class>();

   replace_name = null;
   result_type = null;
   return_arg0 = false;
   canbe_null = false;
   is_mutable = false;
   user_replace = false;
   dont_scan = true;
   async_call = false;
   array_copy = false;
   call_backs = null;
   callback_id = null;
   call_args = null;
   when_cases = null;
   use_reflection = false;
   does_exit = false;

   setupFields(cm,xml);

   if (method_name != null) {
      int nfnd = 0;
      int narg = IvyXml.getAttrInt(xml,"NARG");
      String sgn = IvyXml.getAttrString(xml,"SIGNATURE");
      if (sgn != null && sgn.length() == 0) sgn = null;
      BT_Class bc = BT_Class.forName(class_name);
      for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
	 BT_Method bm = (BT_Method) e.nextElement();
	 if (bm.getName().equals(tail)) {
	    BT_MethodSignature ms = bm.getSignature();
	    if (narg > 0 && ms.types.size() != narg) continue;
	    if (sgn != null && !ms.toString().equals(sgn)) continue;
	    if (special_methods.get(bm) == null) special_methods.put(bm,this);
	    ++nfnd;
	  }
       }
      if (nfnd == 0) System.err.println("JFLOW: Special method " + method_name + " not found " + tail);
    }

   if (call_backs == null) {
      callback_id = IvyXml.getAttrString(xml,"STARTCB");
    }

   max_threads = IvyXml.getAttrInt(xml,"THREADS");
   if (max_threads <= 0) max_threads = 0;
}



private MethodSpecial(FlowMaster jm,CinderManager cm,BT_Method bm)
{
   jflow_master = jm;
   method_name = bm.fullName();
   class_name = bm.getDeclaringClass().getName();
   result_type = null;
   return_arg0 = false;
   is_mutable = true;
   canbe_null = true;
   load_items = new Vector<BT_Class>();
   replace_name = null;
   dont_scan = true;
   call_backs = null;
   callback_id = null;
   call_args = null;
   async_call = false;
   user_replace = false;
   when_cases = null;
   does_exit = false;

   if (bm.fullName().equals("java.lang.Thread.start")) async_call = true;

   if (special_methods.get(bm) == null) {
      special_methods.put(bm,this);
    }
}




/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

private void setupFields(CinderManager cm,Node xml)
{
   if (IvyXml.getAttrPresent(xml,"REPLACE")) {
      replace_name = IvyXml.getAttrString(xml,"REPLACE");
      if (replace_name != null && replace_name.length() == 0) replace_name = null;
      if (replace_name != null && replace_name.endsWith(".<init>")) {
	 int xidx = replace_name.lastIndexOf('.');
	 String rnam = replace_name.substring(0,xidx);
	 if (CinderManager.checkIfClassExists(rnam)) {
	    // result_type = BT_Class.forName(rnam);
	    load_items.add(BT_Class.forName(rnam));
	  }
       }
    }

   if (IvyXml.getAttrPresent(xml,"RETURN")) {
      String rnam = IvyXml.getAttrString(xml,"RETURN");
      if (rnam == null || rnam.length() == 0) {
	 if (result_type != null) rnam = result_type.getName();
	 else rnam = "*";
       }
      if (rnam.equals("*")) ;
      else if (rnam.equals("0")) {
	 return_arg0 = true;
       }
      else {
	 if (!CinderManager.checkIfClassExists(rnam)) {
	    String arnam = IvyXml.getAttrString(xml,"ARETURN");
	    if (arnam != null && CinderManager.checkIfClassExists(arnam)) rnam = arnam;
	    else {
	       System.err.println("JFLOW: Return class " + rnam + " for special methods not found");
	       method_name = null;
	       rnam = null;
	     }
	  }

	 if (rnam != null) {
	    result_type = BT_Class.forName(rnam);
	    load_items.add(result_type);
	  }
       }
    }

   canbe_null = IvyXml.getAttrBool(xml,"NULL",canbe_null);
   is_mutable = IvyXml.getAttrBool(xml,"MUTABLE",is_mutable);
   user_replace = IvyXml.getAttrBool(xml,"USER",user_replace);
   use_reflection = IvyXml.getAttrBool(xml,"REFLECT",use_reflection);
   does_exit = IvyXml.getAttrBool(xml,"EXIT",does_exit);

   String lnam = IvyXml.getAttrString(xml,"LOAD");
   if (lnam != null) {
      StringTokenizer tok = new StringTokenizer(lnam);
      while (tok.hasMoreTokens()) {
	 String id = tok.nextToken();
	 for (Enumeration<?> e = BT_Repository.getClasses().elements(); e.hasMoreElements(); ) {
	    BT_Class bc = (BT_Class) e.nextElement();
	    if (bc.getName().startsWith(id)) {
	       load_items.add(bc);
	     }
	  }
       }
    }

   if (IvyXml.getAttrPresent(xml,"SCAN")) {
      dont_scan = true;
      if (IvyXml.getAttrBool(xml,"SCAN")) dont_scan = false;
    }

   async_call = IvyXml.getAttrBool(xml,"ASYNC",async_call);
   array_copy = IvyXml.getAttrBool(xml,"ARRAYCOPY",array_copy);

   String cbnm = IvyXml.getAttrString(xml,"CALLBACK");
   if (cbnm != null && cbnm.length() > 0) {
      call_backs = new Vector<String>();
      call_args = new Vector<Integer>();
      callback_id = IvyXml.getAttrString(xml,"CBID");
      for (StringTokenizer tok = new StringTokenizer(cbnm); tok.hasMoreTokens(); ) {
	 String cn = tok.nextToken();
	 call_backs.add(cn);
	 callback_names.add(cn);
       }
      String ags = IvyXml.getAttrString(xml,"ARGS");
      if (ags == null || ags.length() == 0) {
	 System.err.println("JFLOW: Args missing for callback for " + method_name);
	 ags = "1";
       }
      for (StringTokenizer tok = new StringTokenizer(ags); tok.hasMoreTokens(); ) {
	 try {
	    int i = Integer.parseInt(tok.nextToken());
	    call_args.add(Integer.valueOf(i));
	  }
	 catch (NumberFormatException _e) {
	    System.err.println("JFLOW: Args contains bad value for " + method_name);
	  }
       }
    }

   for (Iterator<Element> it = IvyXml.getElementsByTag(xml,"WHEN"); it.hasNext(); ) {
      Node w = it.next();
      When wn = new When(cm,w);
      if (when_cases == null) when_cases = new Vector<When>();
      when_cases.add(wn);
    }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getMethodName()			{ return method_name; }


ValueBase getReturnValue(BT_Method bm,MethodBase caller,int ino)
{
   BT_Class rslt = getResultType(caller,ino);

   if (rslt == null) {
      if (bm.isConstructor()) rslt = bm.getDeclaringClass();
      else rslt = bm.getSignature().returnType;
    }

   ValueBase cv = null;
   if (user_replace) {
      SourceBase src = jflow_master.createLocalSource(null,-1,rslt,false);
      SourceSet cset = jflow_master.createSingletonSet(src);
      cv = jflow_master.objectValue(rslt,cset,JflowFlags.NON_NULL);
    }
   else if (is_mutable) cv = jflow_master.mutableValue(rslt);
   else cv = jflow_master.nativeValue(rslt);

   if (!canbe_null || bm.isConstructor()) cv = cv.forceNonNull();

   return cv;
}



boolean returnsArg0()			{ return return_arg0; }


Iterator<BT_Class> getLoadClasses()
{
   return load_items.iterator();
}


String getReplaceName() 		{ return replace_name; }


boolean getDontScan()			{ return dont_scan; }


Iterator<String> getCallbacks()
{
   if (call_backs == null || call_backs.size() == 0) return null;
   return call_backs.iterator();
}


String getCallbackId()			{ return callback_id; }


List<Integer> getArgList()		{ return call_args; }

boolean getIsAsync()			{ return async_call; }

boolean getIsArrayCopy()		{ return array_copy; }

boolean getIsReflective()		{ return use_reflection; }

boolean getExits()			{ return does_exit; }

int getMaxThreads()			{ return max_threads; }





/********************************************************************************/
/*										*/
/*	Methods to read the xml files						*/
/*										*/
/********************************************************************************/

private static void addMethodSpecials(FlowMaster jm,CinderManager cm,Node xml)
{
   if (xml == null) return;

   for (Iterator<Element> it = IvyXml.getChildren(xml); it.hasNext(); ) {
      Node n = it.next();
      if (IvyXml.isElement(n,"METHOD")) {
	 createSpecial(jm,cm,n);
       }
      else if (IvyXml.isElement(n,"CLASS")) {
	 String cnam = IvyXml.getAttrString(n,"NAME");
	 boolean ufg = IvyXml.getAttrBool(n,"USE");
	 boolean vdfg = IvyXml.getAttrBool(n,"VOID");
	 boolean spcfg = IvyXml.getAttrBool(n,"FIELDS",vdfg);
	 if (CinderManager.checkIfClassExists(cnam)) {
	    BT_Class bc = BT_Class.forName(cnam);
	    if (ufg) jm.noteClassUsed(bc);
	    if (spcfg) full_classes.add(bc);
	    addClassMethods(jm,cm,bc,vdfg);
	  }
       }
      else if (IvyXml.isElement(n,"PACKAGE")) {
	 String pnam = IvyXml.getAttrString(n,"NAME");
	 boolean ufg = IvyXml.getAttrBool(n,"USE");
	 boolean vdfg = IvyXml.getAttrBool(n,"VOID");
	 boolean spcfg = IvyXml.getAttrBool(n,"FIELDS",vdfg);
	 if (!pnam.endsWith(".")) pnam += ".";
	 for (Iterator<BT_Class> it1 = cm.getAllClasses(); it1.hasNext(); ) {
	    BT_Class bc = it1.next();
	    if (bc.getName().startsWith(pnam)) {
	       if (spcfg) full_classes.add(bc);
	       addClassMethods(jm,cm,bc,vdfg);
	       if (ufg) jm.noteClassUsed(bc);
	     }
	  }
       }
      else if (IvyXml.isElement(n,"USE")) {
	 String cnam = IvyXml.getAttrString(n,"CLASS");
	 if (CinderManager.checkIfClassExists(cnam)) {
	    BT_Class bc = BT_Class.forName(cnam);
	    jm.noteClassUsed(bc);
	  }
       }
    }
}




private static void addClassMethods(FlowMaster jm,CinderManager cm,BT_Class bc,boolean vdfg)
{
   for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
      BT_Method bm = (BT_Method) e.nextElement();
      if (!bm.isConstructor() && !bm.isStaticInitializer()) {
	 if (vdfg || !bm.isVoidMethod()) {
	    new MethodSpecial(jm,cm,bm);
	  }
       }
    }
}



private static void createSpecial(FlowMaster jm,CinderManager cm,Node xml)
{
   String name = IvyXml.getAttrString(xml,"NAME");

   if (name == null) return;

   int idx = name.lastIndexOf('.');
   String cname = name.substring(0,idx);
   String tail = name.substring(idx+1);

   if (!CinderManager.checkIfClassExists(cname)) return;

   int ntoset = 0;
   int narg = IvyXml.getAttrInt(xml,"NARG");
   String sgn = IvyXml.getAttrString(xml,"SIGNATURE");
   if (sgn != null && sgn.length() == 0) sgn = null;
   BT_Class bc = BT_Class.forName(cname);
   Set<MethodSpecial> done = new HashSet<MethodSpecial>();

   for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
      BT_Method bm = (BT_Method) e.nextElement();
      if (bm.getName().equals(tail)) {
	 BT_MethodSignature ms = bm.getSignature();
	 if (narg > 0 && ms.types.size() != narg) continue;
	 if (sgn != null && !ms.toString().equals(sgn)) continue;
	 MethodSpecial ms1 = special_methods.get(bm);
	 if (ms1 == null) ntoset++;
	 else if (done.contains(ms1)) ;
	 else {
	    done.add(ms1);
	    ms1.setupFields(cm,xml);
	  }
       }
    }

   if (ntoset > 0) {
      new MethodSpecial(jm,cm,xml);
    }
}




/********************************************************************************/
/*										*/
/*	When cases								*/
/*										*/
/********************************************************************************/

private BT_Class getResultType(MethodBase caller,int ino)
{
   if (when_cases != null) {
      for (When w : when_cases) {
	 if (w.match(caller,ino)) {
	    BT_Class rt = w.getResultType();
	    if (rt != null) return rt;
	  }
       }
    }

   return result_type;
}




String getReplaceName(MethodBase caller,int ino)
{
   if (when_cases != null) {
      for (When w : when_cases) {
	 if (w.match(caller,ino)) {
	    String rp = w.getReplaceName();
	    if (rp != null) return rp;
	  }
       }
    }

   return replace_name;
}




boolean getUseReflection(MethodBase caller,int ino)
{
   if (use_reflection) return true;

   if (when_cases != null) {
      for (When w : when_cases) {
	 if (w.match(caller,ino)) {
	    return w.getUseReflection();
	  }
       }
    }

   return false;
}




private static class When {

   private String caller_name;
   private String caller_sign;
   private int instance_number;
   private String replace_name;
   private BT_Class result_type;
   private boolean use_reflection;

   When(CinderManager cm,Node xml) {
      caller_name = IvyXml.getAttrString(xml,"CALLER");
      caller_sign = IvyXml.getAttrString(xml,"SIGNATURE");
      instance_number = IvyXml.getAttrInt(xml,"INSTANCE",0);
      replace_name = IvyXml.getAttrString(xml,"REPLACE");
      use_reflection = IvyXml.getAttrBool(xml,"REFLECT");

      if (caller_name == null) {
	 System.err.println("JFLOW: When clause with no CALLER specified");
       }

      result_type = null;

      String rnam = IvyXml.getAttrString(xml,"RETURN");
      if (replace_name.endsWith(".<init>") && (rnam == null || rnam.length() == 0)) {
	 int xidx = replace_name.lastIndexOf('.');
	 rnam = replace_name.substring(0,xidx);
       }
      if (rnam != null && !rnam.equals("*") && CinderManager.checkIfClassExists(rnam)) {
	 result_type = BT_Class.forName(rnam);
       }
    }

   String getReplaceName()			{ return replace_name; }
   BT_Class getResultType()			{ return result_type; }
   boolean getUseReflection()			{ return use_reflection; }

   boolean match(MethodBase caller,int ino) {
      if (caller_name == null) return false;
      if (caller == null) return false;
      if (!caller_name.equals(caller.getMethodName())) return false;

      if (caller_sign != null) {
	 if (!caller_sign.equals(caller.getMethodSignature())) return false;
       }

      if (instance_number == 0) return true;

      // here we should count calls that match in the instruction vector
      // and only choose the instance_numberth one (it has to match ino)

      return true;
    }

}	// end of subclass When



}	// end of class MethodSpecial





/* end of MethodSpecial.java */
