/********************************************************************************/
/*										*/
/*		FlowDetails.java						*/
/*										*/
/*	Holder for information about where to detail the flow analysis		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/FlowDetails.java,v 1.7 2017/02/15 02:09:13 spr Exp $ */


/*********************************************************************************
 *
 * $Log: FlowDetails.java,v $
 * Revision 1.7  2017/02/15 02:09:13  spr
 * Formatting
 *
 * Revision 1.6  2007-08-10 02:10:39  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.5  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.4  2006-12-01 03:22:46  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.3  2006/07/10 14:52:17  spr
 * Code cleanup.
 *
 * Revision 1.2  2006/07/03 18:15:23  spr
 * Efficiency improvements; inlining options.
 *
 * Revision 1.1  2006/06/21 02:18:34  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.flow;

import edu.brown.cs.ivy.cinder.CinderManager;
import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowException;
import edu.brown.cs.ivy.xml.IvyXml;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_MethodSignature;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;



class FlowDetails implements JflowConstants
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster	  chet_main;

private boolean 	proj_field_detail;
private boolean 	lib_field_detail;
private InlineType	proj_method_detail;
private InlineType	lib_method_detail;
private boolean 	proj_source_detail;
private boolean 	lib_source_detail;

private Map<BT_Field,Boolean>	   field_map;
private Map<BT_Method,InlineType>  method_map;
private Map<BT_Class,SourceDetail> source_map;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

FlowDetails(FlowMaster ch)
{
   chet_main = ch;

   proj_field_detail = true;
   lib_field_detail = false;
   field_map = new HashMap<BT_Field,Boolean>();

   proj_method_detail = InlineType.THIS;
   lib_method_detail = InlineType.DEFAULT;
   method_map = new HashMap<BT_Method,InlineType>();

   proj_source_detail = true;
   lib_source_detail = true;
   source_map = new HashMap<BT_Class,SourceDetail>();
}



/********************************************************************************/
/*										*/
/*	Checking methods							*/
/*										*/
/********************************************************************************/

public boolean isFieldDetailed(BT_Field bf)
{
   Boolean bv = field_map.get(bf);

   if (bv != null) return bv.booleanValue();

   BT_Class bc = bf.getDeclaringClass();
   boolean proj = chet_main.isProjectClass(bc);

   if (proj) return proj_field_detail;

   return lib_field_detail;
}



public InlineType isMethodInlined(BT_Method bm)
{
   InlineType bv = method_map.get(bm);

   if (bv != null) return bv;

   BT_Class bc = bm.getDeclaringClass();
   boolean proj = chet_main.isProjectClass(bc);

   if (proj) return proj_method_detail;

   return lib_method_detail;
}



public boolean useLocalSource(BT_Method frm,BT_Class cls)
{
   BT_Class bc = frm.getDeclaringClass();
   boolean rslt;

   if (chet_main.isProjectClass(bc)) rslt = proj_source_detail;
   else rslt = lib_source_detail;

   SourceDetail sd = source_map.get(cls);

   if (sd != null) {
      rslt = sd.useLocalSource(frm,rslt);
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Methods to load the xml 						*/
/*										*/
/********************************************************************************/

public void addDetails(Node xml) throws JflowException
{
   if (xml == null) return;

   for (Element n : IvyXml.elementsByTag(xml,"DETAIL")) {
      String dflt = IvyXml.getAttrString(n,"FIELD");
      proj_field_detail = isProjectDefault(dflt,proj_field_detail);
      lib_field_detail = isLibraryDefault(dflt,lib_field_detail);
      dflt = IvyXml.getAttrString(n,"METHOD");
      proj_method_detail = isProjectMethodDefault(dflt,proj_method_detail);
      lib_method_detail = isLibraryMethodDefault(dflt,lib_method_detail);
      dflt = IvyXml.getAttrString(n,"SOURCE");
      proj_source_detail = isProjectDefault(dflt,proj_source_detail);
      lib_source_detail = isLibraryDefault(dflt,lib_source_detail);

      for (Iterator<Element> it1 = IvyXml.getChildren(n); it1.hasNext(); ) {
	 Node n1 = it1.next();
	 if (IvyXml.isElement(n1,"FIELD")) {
	    new FieldDetail(n1);
	  }
	 else if (IvyXml.isElement(n1,"METHOD")) {
	    new MethodDetail(n1);
	  }
	 else if (IvyXml.isElement(n1,"SOURCE")) {
	    new SourceDetail(n1);
	  }
       }
    }
}



private boolean isProjectDefault(String s,boolean dflt)
{
   if (s == null || s.length() == 0) return dflt;
   else if (s.equalsIgnoreCase("PROJECT")) return true;
   else if (s.equalsIgnoreCase("LIBRARY")) return false;
   else if (s.equalsIgnoreCase("ALL")) return true;
   else if (s.equalsIgnoreCase("NONE")) return false;
   return dflt;
}




private boolean isLibraryDefault(String s,boolean dflt)
{
   if (s == null || s.length() == 0) return dflt;
   else if (s.equalsIgnoreCase("PROJECT")) return false;
   else if (s.equalsIgnoreCase("LIBRARY")) return true;
   else if (s.equalsIgnoreCase("ALL")) return true;
   else if (s.equalsIgnoreCase("NONE")) return false;
   return dflt;
}




private InlineType isProjectMethodDefault(String s,InlineType dflt)
{
   if (s == null || s.length() == 0) return dflt;

   InlineType ttyp = InlineType.THIS;
   InlineType ftyp = InlineType.DEFAULT;

   String [] args = s.split(",|:");
   if (args.length > 1) {
      s = args[0];
      try {
	 ttyp = InlineType.valueOf(args[1]);
       }
      catch (IllegalArgumentException e) {
	 System.err.println("JFLOW: Illegal inline type for detailing: " + args[1]);
       }
      if (args.length > 2) {
	 try {
	    ftyp = InlineType.valueOf(args[2]);
	  }
	 catch (IllegalArgumentException e) {
	    System.err.println("JFLOW: Illegal inline type for detailing: " + args[2]);
	  }
       }
    }

   if (s.equalsIgnoreCase("PROJECT")) return ttyp;
   else if (s.equalsIgnoreCase("LIBRARY")) return ftyp;
   else if (s.equalsIgnoreCase("ALL")) return ttyp;
   else if (s.equalsIgnoreCase("NONE")) return ftyp;
   return dflt;
}




private InlineType isLibraryMethodDefault(String s,InlineType dflt)
{
   if (s == null || s.length() == 0) return dflt;

   InlineType ttyp = InlineType.THIS;
   InlineType ftyp = InlineType.DEFAULT;

   String [] args = s.split(",|:");
   if (args.length > 1) {
      s = args[0];
      try {
	 ttyp = InlineType.valueOf(args[1]);
       }
      catch (IllegalArgumentException e) {
	 System.err.println("JFLOW: Illegal inline type for detailing: " + args[1]);
       }
      if (args.length > 2) {
	 try {
	    ftyp = InlineType.valueOf(args[2]);
	  }
	 catch (IllegalArgumentException e) {
	    System.err.println("JFLOW: Illegal inline type for detailing: " + args[2]);
	  }
       }
    }

   if (s.equalsIgnoreCase("PROJECT")) return ftyp;
   else if (s.equalsIgnoreCase("LIBRARY")) return ttyp;
   else if (s.equalsIgnoreCase("ALL")) return ttyp;
   else if (s.equalsIgnoreCase("NONE")) return ftyp;
   return dflt;
}




/********************************************************************************/
/*										*/
/*	Common superclass for member (field or method) details			*/
/*										*/
/********************************************************************************/

private abstract class MemberDetail {

   private BT_Class for_class;
   private boolean project_only;
   private boolean library_only;
   private boolean inherit_flag;
   private boolean detail_flag;
   private InlineType detail_type;

   MemberDetail(Node xml) throws JflowException {
      String cls = IvyXml.getAttrString(xml,"CLASS");
      if (cls != null) {
	 if (!CinderManager.checkIfClassExists(cls))
	    throw new JflowException("Class " + cls + " not found for FIELD DETAIL definition");
	 for_class = BT_Class.forName(cls);
       }
      else for_class = null;

      library_only = IvyXml.getAttrBool(xml,"LIBRARY");
      project_only = IvyXml.getAttrBool(xml,"PROJECT");
      inherit_flag = IvyXml.getAttrBool(xml,"INHERIT");

      String s = IvyXml.getAttrString(xml,"MODE");
      detail_type = null;
      try {
	 if (s != null) detail_type = InlineType.valueOf(s);
       }
      catch (IllegalArgumentException _e) { }
      if (detail_type == null) {
	 detail_flag = IvyXml.getAttrBool(xml,"MODE");
	 if (detail_flag) detail_type = InlineType.THIS;
	 else detail_type = InlineType.DEFAULT;
       }
      else {
	 if (detail_type == InlineType.NONE || detail_type == InlineType.DEFAULT) detail_flag = false;
	 else detail_flag = true;
       }
    }

   protected BT_Class getMemberClass()			{ return for_class; }
   protected boolean getInherit()			{ return inherit_flag; }
   protected boolean getDetails()			{ return detail_flag; }
   protected InlineType getDetailType() 		{ return detail_type; }

   protected boolean detailClass(BT_Class bc) {
      boolean proj = chet_main.isProjectClass(bc);
      if (project_only && !proj) return false;
      if (library_only && proj) return false;
      if (for_class == null) return true;
      if (bc == for_class) return detail_flag;
      if (inherit_flag) {
	 if (bc.isDescendentOf(for_class)) return detail_flag;
       }
      return false;
    }

   protected Set<BT_Class> classSet()		{ return classSet(for_class,null); }

   private Set<BT_Class> classSet(BT_Class bc,Set<BT_Class> s) {
      if (s == null) s = new HashSet<BT_Class>();
      s.add(bc);
      if (getInherit()) {
	 for (Enumeration<?> e = bc.getKids().elements(); e.hasMoreElements(); ) {
	    BT_Class sc = (BT_Class) e.nextElement();
	    if (!s.contains(sc)) classSet(sc,s);
	  }
       }

      return s;
    }

}	// end of subclass FieldDetail




/********************************************************************************/
/*										*/
/*	Holder for field information						*/
/*										*/
/********************************************************************************/

private class FieldDetail extends MemberDetail {

   private String field_name;

   FieldDetail(Node xml) throws JflowException {
      super(xml);

      field_name = IvyXml.getAttrString(xml,"NAME");

      BT_Class bc = getMemberClass();

      if (bc == null) {
	 throw new JflowException("Class must be defined in FIELD DETAIL");
       }

      addFields(bc,field_map);
    }

   protected void addFields(BT_Class bc,Map<BT_Field,Boolean> fmap) {
      Boolean fg = Boolean.valueOf(detailClass(bc));

      for (Enumeration<?> e = bc.getFields().elements(); e.hasMoreElements(); ) {
	 BT_Field bf = (BT_Field) e.nextElement();
	 if (field_name == null || bf.getName().equals(field_name)) {
	    fmap.put(bf,fg);
	  }
       }
      if (getInherit()) {
	 for (Enumeration<?> e = bc.getKids().elements(); e.hasMoreElements(); ) {
	    BT_Class sc = (BT_Class) e.nextElement();
	    addFields(sc,fmap);
	  }
       }
    }

}	// end of subclass FieldDetail




/********************************************************************************/
/*										*/
/*	Holder for information on methods to inline				*/
/*										*/
/********************************************************************************/

private class MethodDetail extends MemberDetail {

   private String method_name;

   MethodDetail(Node xml) throws JflowException {
      super(xml);

      method_name = IvyXml.getAttrString(xml,"NAME");

      int narg = IvyXml.getAttrInt(xml,"NARG");
      String sgn = IvyXml.getAttrString(xml,"SIGNATURE");
      if (sgn != null && sgn.length() == 0) sgn = null;
      BT_Class bc = getMemberClass();

      addMethods(bc,narg,sgn,method_map);
    }

   protected void addMethods(BT_Class bc,int narg,String sgn,Map<BT_Method,InlineType> mmap) {
      InlineType ttyp = InlineType.DEFAULT;
      if (detailClass(bc)) ttyp = getDetailType();
      for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
	 BT_Method bm = (BT_Method) e.nextElement();
	 if (method_name == null || bm.getName().equals(method_name)) {
	    BT_MethodSignature ms = bm.getSignature();
	    if (narg > 0 && ms.types.size() != narg) continue;
	    if (sgn != null && !ms.toString().equals(sgn)) continue;
	    mmap.put(bm,ttyp);
	  }
       }

      if (getInherit()) {
	 for (Enumeration<?> e = bc.getKids().elements(); e.hasMoreElements(); ) {
	    BT_Class sc = (BT_Class) e.nextElement();
	    addMethods(sc,narg,sgn,mmap);
	  }
       }
    }

}	// end of subclass MethodDetail




/********************************************************************************/
/*										*/
/*	Holder for information on sources to merge				*/
/*										*/
/********************************************************************************/

private class SourceDetail extends MemberDetail {

   private Collection<SourceFrom> from_set;

   SourceDetail(Node xml) throws JflowException {
      super(xml);

      from_set = new Vector<SourceFrom>();

      for (Iterator<Element> it = IvyXml.getChildren(xml); it.hasNext(); ) {
	 Node n = it.next();
	 if (IvyXml.isElement(n,"FROM")) {
	    SourceFrom sf = new SourceFrom(n);
	    from_set.add(sf);
	  }
       }

      for (BT_Class bc : classSet()) {
	 if (source_map.get(bc) == null) source_map.put(bc,this);
       }
    }

   boolean useLocalSource(BT_Method frm,boolean dflt) {
      for (SourceFrom sf : from_set) {
	 if (sf.appliesTo(frm))
	    return sf.getDetails();
       }
      return dflt;
    }

}	// end of subclass SourceDetail



private class SourceFrom extends MemberDetail {

   private String method_name;
   private int num_arg;
   private String method_sign;

   SourceFrom(Node xml) throws JflowException {
      super(xml);
      method_name = IvyXml.getAttrString(xml,"NAME");

      num_arg = IvyXml.getAttrInt(xml,"NARG");
      method_sign = IvyXml.getAttrString(xml,"SIGNATURE");
      if (method_sign != null && method_sign.length() == 0) method_sign = null;
    }

   boolean appliesTo(BT_Method bm) {
      if (method_name != null && !bm.getName().equals(method_name)) return false;

      BT_MethodSignature ms = bm.getSignature();
      if (num_arg > 0 && ms.types.size() != num_arg) return false;
      if (method_sign != null && !ms.toString().equals(method_sign)) return false;

      return detailClass(bm.getDeclaringClass());
    }

}	// end of subclass SourceFrom





}	// end of class FlowDetails




/* end of FlowDetails.java */
