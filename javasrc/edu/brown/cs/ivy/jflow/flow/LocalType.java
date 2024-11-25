/********************************************************************************/
/*										*/
/*		LocalType.java							*/
/*										*/
/*	Class for holding information about a local (user-specialized) type	*/
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
import edu.brown.cs.ivy.xml.IvyXml;

import com.ibm.jikesbt.BT_Class;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



class LocalType implements JflowConstants
{


/********************************************************************************/
/*										*/
/*	LocalType Factory methods						*/
/*										*/
/********************************************************************************/

private static Map<String,LocalType>	local_types = new HashMap<>();



static LocalType getLocalType(String name)
{
   if (name == null) return null;

   if (name.startsWith("*")) return ANY_TYPE;

   return local_types.get(name);
}



static void createLocalTypes(String file)
{
   Element e = IvyXml.loadXmlFromFile(file);
   if (e == null) return;

   loadLocalTypes(e);
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String type_name;
private String type_description;
private BT_Class base_type;

private static Map<String,List<LocalOp>> operator_set = new HashMap<>();
private static LocalType	ANY_TYPE = new LocalType();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

LocalType(Element e)
{
   type_name = IvyXml.getAttrString(e,"NAME");
   type_description = null;
   base_type = null;

   String btnm = IvyXml.getAttrString(e,"BASE");
   if (btnm != null && CinderManager.checkIfClassExists(btnm))
      base_type = BT_Class.forName(btnm);

   String d = IvyXml.getTextElement(e,"DESCRIPTION");
   if (d != null) type_description = d;
}



private LocalType()
{
   type_name = "*ANY*";
   type_description = "Any element of the base type";
   base_type = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getName()				{ return type_name; }
String getDescription() 			{ return type_description; }
BT_Class getBaseType()				{ return base_type; }




/********************************************************************************/
/*										*/
/*	Loading methods 							*/
/*										*/
/********************************************************************************/

private static void loadLocalTypes(Element xml)
{
   for (Element e : IvyXml.elementsByTag(xml,"USERTYPE")) {
      LocalType lt = new LocalType(e);
      String nm = lt.getName();
      if (nm == null) continue;
      if (local_types.containsKey(nm)) {
	 System.err.println("JFLOW: Duplicate definition of local type " + nm);
	 continue;
       }
      local_types.put(nm,lt);
    }

   for (Element e : IvyXml.elementsByTag(xml,"OP")) {
      String code = IvyXml.getAttrString(e,"ID");
      if (code == null) continue;
      LocalOp lop = new LocalOp(e);
      if (lop.getResultType() == null) continue;
      List<LocalOp> ll = operator_set.get(code);
      if (ll == null) {
	 ll = new ArrayList<LocalOp>();
	 operator_set.put(code,ll);
       }
      ll.add(lop);
    }
}




/********************************************************************************/
/*										*/
/*	Operator representation 						*/
/*										*/
/********************************************************************************/

private static class LocalOp {

   private LocalType lhs_type;
   private LocalType rhs_type;
   private LocalType result_type;

   LocalOp(Element e) {
      String lnm = IvyXml.getTextElement(e,"LHS");
      String rnm = IvyXml.getTextElement(e,"RHS");
      String xnm = IvyXml.getTextElement(e,"RESULT");
      result_type = getLocalType(xnm);
      lhs_type = getLocalType(lnm);
      if (lhs_type == null) result_type = null;
      rhs_type = null;
      if (rnm != null) {
	 rhs_type = getLocalType(rnm);
	 if (rhs_type == null) result_type = null;
       }
    }

   LocalType getResultType()			{ return result_type; }

}	// end of subclass LocalOp




}	// end of class LocalType




/* end of LocalType.java */
