/********************************************************************************/
/*										*/
/*		MethodFactory.java						*/
/*										*/
/*	Class to hold create and manage method instances			*/
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

import com.ibm.jikesbt.BT_Method;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



class MethodFactory implements JflowConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;

private Map<BT_Method,Map<Object,MethodBase>> method_map;
private Map<BT_Method,MethodBase> proto_map;


private static final Object DEFAULT_OBJECT = new Object();




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MethodFactory(FlowMaster jm)
{
   jflow_master = jm;
   method_map = new HashMap<BT_Method,Map<Object,MethodBase>>();
   proto_map = new HashMap<BT_Method,MethodBase>();
}




/********************************************************************************/
/*										*/
/*	Special file methods							*/
/*										*/
/********************************************************************************/

void addSpecialFile(FlowMaster cm,CinderManager cdr,Node xml)
{
   MethodSpecial.addSpecialFile(cm,cdr,xml);
}




/********************************************************************************/
/*										*/
/*	Creation methods							*/
/*										*/
/********************************************************************************/

MethodBase findMethod(BT_Method bm,List<ValueBase> args,InlineType inline)
{
   Object key = null;

   if (args == null || args.size() == 0) key = DEFAULT_OBJECT;
   else if (bm.isStatic()) key = DEFAULT_OBJECT;
   else {
      ValueBase cv = args.get(0);
      switch (inline) {
	 case NONE :
	    key = DEFAULT_OBJECT;
	    break;
	 case DEFAULT :
	    key = getSourceKey(cv);
	    break;
	 case THIS :
	    key = cv;
	    break;
	 case SOURCES :
	    if (args.size() == 1) key = getSourceKey(cv);
	    else {
	       List<Object> kl = new ArrayList<Object>();
	       for (ValueBase vb : args) {
		  kl.add(getSourceKey(vb));
		}
	       key = kl;
	     }
	    break;
	 case VALUES :
	    if (args.size() == 1) key = getSourceKey(cv);
	    else {
	       List<Object> kl = new ArrayList<Object>();
	       for (ValueBase vb : args) {
		  kl.add(vb);
		}
	       key = kl;
	     }
	    break;
       }
    }

   Map<Object,MethodBase> mm = method_map.get(bm);
   if (mm == null) {
      mm = new HashMap<Object,MethodBase>(4);
      method_map.put(bm,mm);
    }

   MethodBase cm = mm.get(key);

   // handle recursive calls that would otherwise be infinite
   if (cm == null) {
      if (inline == InlineType.THIS && key instanceof ValueBase) {
	 for (Object o : mm.keySet()) {
	    if (o instanceof ValueBase) {
	       if (matchInlineValues((ValueBase) key,(ValueBase) o)) {
		  cm = mm.get(o);
		  mm.put(key,cm);
		  break;
		}
	     }
	  }
       }
    }

   if (cm == null) {
      cm = new MethodBase(jflow_master,bm,mm.size());
      mm.put(key,cm);
      if (FlowMaster.doDebug() && key != DEFAULT_OBJECT) {
	 System.err.println("\tCreate INLINE method for " + bm.getName() + " " + inline + " " + key);
       }
    }

    return cm;
}



private Object getSourceKey(ValueBase jv)
{
   SourceSet set = jv.getSourceSet();
   if (set != null && !set.isEmpty()) {
      set = set.getModelSet();
      if (!set.isEmpty()) return set;
    }

   return DEFAULT_OBJECT;
}




MethodBase findPrototypeMethod(BT_Method bm)
{
   MethodBase cm = proto_map.get(bm);
   if (cm == null) {
      cm = new MethodBase(jflow_master,bm,0);
      cm.setPrototype();
      proto_map.put(bm,cm);
    }
   return cm;
}




Iterable<MethodBase> getAllMethods(BT_Method bm)
{
   Map<Object,MethodBase> mm = method_map.get(bm);

   if (mm != null) return new ArrayList<MethodBase>(mm.values());

   List<MethodBase> lcm = Collections.emptyList();
   return lcm;
}




MethodBase createMetaMethod(BT_Method bm)
{
   MethodBase cm = new MethodBase(jflow_master,bm);

   return cm;
}



private boolean matchInlineValues(ValueBase v1,ValueBase v2)
{
   if (v1.getDataType() != v2.getDataType()) return false;

   ValueBase v3 = v1.mergeValue(v2);
   if (v3 == v1 || v3 == v2) return true;

   if (v1.getSourceSet().size() == 1 && v2.getSourceSet().size() == 1) {
      for (SourceBase s1 : v1.getSources()) {
	 for (SourceBase s2 : v2.getSources()) {
	    MethodBase m1 = s1.getSourceMethod();
	    MethodBase m2 = s2.getSourceMethod();
	    if (m1 != null && m2 != null && m1.getMethod() == m2.getMethod() &&
		   s1.getSourceInstruction() == s2.getSourceInstruction()) {
	       // System.err.println("MATCH PREVIOUS");
	       return true;
	     }
	  }
       }
    }

   return false;
}




/********************************************************************************/
/*										*/
/*	Special access methods							*/
/*										*/
/********************************************************************************/

boolean canBeCallback(BT_Method bm)
{
   return MethodSpecial.canBeCallback(bm);
}



boolean getIsArrayCopy(BT_Method bm)
{
   MethodSpecial ms = MethodSpecial.getSpecial(bm);
   if (ms != null) return ms.getIsArrayCopy();

   return false;
}



boolean canBeReplaced(BT_Method bm)
{
   MethodSpecial ms = MethodSpecial.getSpecial(bm);
   if (ms != null) return ms.getReplaceName() != null;

   return false;
}


String getCallbackStart(BT_Method bm)
{
   MethodSpecial ms = MethodSpecial.getSpecial(bm);
   if (ms != null && ms.getCallbacks() == null) {
      return ms.getCallbackId();
    }

   return null;
}




}	// end of class MethodFactory



/* end of MethodFactory.java */
