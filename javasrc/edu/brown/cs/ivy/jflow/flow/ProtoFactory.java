/********************************************************************************/
/*										*/
/*		jflow_master.java						*/
/*										*/
/*	Prototype factory implementation					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/ProtoFactory.java,v 1.7 2017/12/20 20:36:49 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ProtoFactory.java,v $
 * Revision 1.7  2017/12/20 20:36:49  spr
 * Formatting
 *
 * Revision 1.6  2008-11-12 13:45:39  spr
 * Eclipse fixups.
 *
 * Revision 1.5  2007-08-10 02:10:39  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.4  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.3  2006-12-01 03:22:47  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.2  2006/07/10 14:52:17  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:34  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.flow;

import edu.brown.cs.ivy.cinder.CinderManager;
import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.xml.IvyXml;

import com.ibm.jikesbt.BT_Class;

import org.w3c.dom.Node;

import java.lang.reflect.Constructor;
import java.util.*;



public class ProtoFactory implements JflowConstants
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private Map<BT_Class,Class<?>> class_map;

private static Class<?> [] cnst_params = new Class<?> [] { FlowMaster.class, BT_Class.class };


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public ProtoFactory(FlowMaster jm)
{
   jflow_master = jm;
   class_map = new HashMap<BT_Class,Class<?>>();
}



/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

public ProtoBase createPrototype(BT_Class bc)
{
   Class<?> c = class_map.get(bc);

   if (c == null) return null;

   ProtoBase cp = null;

   try {
      Constructor<?> cnst = c.getConstructor(cnst_params);
      cp = (ProtoBase) cnst.newInstance(new Object [] { jflow_master,bc });
    }
   catch (NoSuchMethodException _e) { }
   catch (Exception e) {
      System.err.println("JFLOW: Problem creating class prototype for " + bc.getName() + ": " + e);
    }

   return cp;
}



/********************************************************************************/
/*										*/
/*	Methods to load the prototype information				*/
/*										*/
/********************************************************************************/

public void addPrototypes(Node xml)
{
   if (xml == null) return;

   for (Iterator<?> it = IvyXml.getChildren(xml); it.hasNext(); ) {
      Node n = (Node) it.next();
      if (IvyXml.isElement(n,"PROTOTYPE")) {
	 addPrototype(n);
       }
      else if (IvyXml.isElement(n,"PROTOTYPES")) {
	 for (Iterator<?> it1 = IvyXml.getChildren(n); it1.hasNext(); ) {
	    Node n1 = (Node) it1.next();
	    if (IvyXml.isElement(n1,"PROTOTYPE")) {
	       addPrototype(n1);
	     }
	  }
       }
    }
}



private void addPrototype(Node xml)
{
   String cl = IvyXml.getAttrString(xml,"CLASS");
   if (cl == null) {
      System.err.println("JFLOW: CLASS must be specified for prototype");
      return;
    }
   if (!CinderManager.checkIfClassExists(cl)) {
      System.err.println("JFLOW: Class " + cl + " not found for prototype");
      return;
    }
   BT_Class bc = BT_Class.forName(cl);

   boolean inher = IvyXml.getAttrBool(xml,"INHERIT");
   String us = IvyXml.getAttrString(xml,"USE");
   if (us == null) {
      System.err.println("JFLOW: USE must be specified for prototype");
      return;
    }
   Class<?> uc = null;
   try {
      uc = Class.forName(us);
    }
   catch (ClassNotFoundException _e) {
      System.err.println("JFLOW: USE class " + us + " not found for prototype");
      return;
    }

   addItems(bc,uc,inher);
}



private void addItems(BT_Class bc,Class<?> pc,boolean inher)
{
   boolean nonew = (bc.isAbstract() || bc.isInterface());

   class_map.put(bc,pc);

   if (nonew || inher) {
      for (Enumeration<?> e = bc.getKids().elements(); e.hasMoreElements(); ) {
	 BT_Class cc = (BT_Class) e.nextElement();
	 addItems(cc,pc,inher);
       }
    }
}




}	// end of class ProtoFactory




/* end of jflow_master.java */
