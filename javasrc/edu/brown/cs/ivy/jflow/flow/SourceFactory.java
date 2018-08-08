/********************************************************************************/
/*										*/
/*		jflow_master.java						*/
/*										*/
/*	Factory for creating sources of various types				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/SourceFactory.java,v 1.9 2018/08/02 15:10:18 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SourceFactory.java,v $
 * Revision 1.9  2018/08/02 15:10:18  spr
 * Fix imports.
 *
 * Revision 1.8  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.7  2011-05-11 01:10:46  spr
 * Prototype entries had bad names.  Code cleansing.
 *
 * Revision 1.6  2011-04-16 01:02:50  spr
 * Fixes to jflow for casting.
 *
 * Revision 1.5  2007-02-27 18:53:29  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.4  2007-01-03 14:04:59  spr
 * Fix imports
 *
 * Revision 1.3  2007-01-03 03:24:18  spr
 * Modifications to handle incremental update.
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


import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowValue;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SourceFactory implements JflowConstants
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private SourceBase	program_return;
private Map<String,SourceBase> thread_map;
private Map<ProtoBase,SourceProto> proto_map;
private Map<String,SourceBase> string_map;
private Map<BT_Class,SourceBase> fixed_map;
private Map<BT_Class,SourceBase> mutable_map;
private Map<BT_Class,List<SourceLocal>> local_map;


private static Map<BT_Class,Map<BT_Class,Boolean>> compat_map = new HashMap<>();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SourceFactory(FlowMaster jm)
{
   jflow_master = jm;
   program_return = null;
   thread_map = new HashMap<>();
   proto_map = new HashMap<>();
   string_map = new HashMap<>();
   fixed_map = new HashMap<>();
   mutable_map = new HashMap<>();
   local_map = new HashMap<>();
}




/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

SourceBase createArraySource(BT_Class acls,ValueBase size)
{
   return new SourceArray(jflow_master,acls,size);
}



SourceBase createFieldSource(BT_Field fld,ValueBase obj)
{
   return SourceField.getFieldSourceObject(jflow_master,fld,obj);
}



SourceBase createFixedSource(BT_Class bc)
{
   SourceBase fs = fixed_map.get(bc);
   if (fs == null) {
      ProtoBase cp = jflow_master.createPrototype(bc);
      if (cp == null) {
	 if (bc.isAbstract() || bc.isInterface()) fs = createMutableSource(bc);
	 else fs = new SourceFixed(jflow_master,bc,false);
       }
      else {
	 fs = new SourceProto(jflow_master,bc,cp,false);
	 cp.setNative();
       }
      fixed_map.put(bc,fs);
    }

   return fs;
}



SourceBase createMutableSource(BT_Class bc)
{
   SourceBase fs = mutable_map.get(bc);
   if (fs == null) {
      ProtoBase cp = jflow_master.createPrototype(bc);
      if (cp == null) fs = new SourceFixed(jflow_master,bc,true);
      else {
	 fs = new SourceProto(jflow_master,bc,cp,true);
	 cp.setNative();
       }
      mutable_map.put(bc,fs);
    }

   return fs;
}



SourceBase createLocalSource(MethodBase cm,int ino,BT_Class bc,boolean uniq)
{
   SourceLocal sl = new SourceLocal(jflow_master,cm,ino,bc,uniq);

   if (jflow_master.isProjectClass(bc)) {
      List<SourceLocal> lcls = local_map.get(bc);
      if (lcls == null) {
	 lcls = new ArrayList<SourceLocal>();
	 local_map.put(bc,lcls);
       }
      lcls.add(sl);
    }

   return sl;
}



SourceBase createPrototypeSource(MethodBase cm,int ino,BT_Class bc,ProtoBase cp)
{
   SourceProto cs = proto_map.get(cp);
   if (cs == null) {
      cs = new SourceProto(jflow_master,cm,ino,bc,cp);
      proto_map.put(cp,cs);
    }

   return cs;
}



SourceBase createStringSource(String v)
{
   SourceBase ss = string_map.get(v);
   if (ss == null) {
      ss = new SourceString(jflow_master,v);
      string_map.put(v,ss);
    }
   return ss;
}


public SourceBase getSource(int id)
{
   return SourceBase.getSource(id);
}



/********************************************************************************/
/*										*/
/*	Static source for use with return fields in abstract programs		*/
/*										*/
/********************************************************************************/

public SourceBase getProgramReturnSource()
{
   if (program_return == null) {
      program_return = new ProgramReturnSource(jflow_master);
    }
   return program_return;
}



private static class ProgramReturnSource extends SourceBase {

   ProgramReturnSource(FlowMaster jm)	{ super(jm); }

   @Override public void outputXml(IvyXmlWriter xw,String id) {
      xw.begin("SOURCE");
      xw.field("TYPE","PROGRAMRETURN");
      if (id != null) xw.field("CID",id);
      xw.end();
    }

}	// end of subclass ProgramReturnSource



/********************************************************************************/
/*										*/
/*	Sources for use as thread holders for events				*/
/*										*/
/********************************************************************************/

public SourceBase getProgramThreadSource(String id)
{
   SourceBase cs = thread_map.get(id);

   if (cs == null) {
      cs = new ProgramThreadSource(jflow_master,id);
      thread_map.put(id,cs);
    }

   return cs;
}



private static class ProgramThreadSource extends SourceBase {

   private String var_id;

   ProgramThreadSource(FlowMaster jm,String id) {
      super(jm);
      var_id = id;
    }

   @Override public void outputXml(IvyXmlWriter xw,String id) {
      xw.begin("SOURCE");
      xw.field("TYPE","PROGRAMTHREAD");
      if (id != null) xw.field("CID",id);
      xw.field("VAR",var_id);
      xw.end();
    }

}	// end of subclass ProgramReturnSource



/********************************************************************************/
/*										*/
/*	Methods for handling incremental update 				*/
/*										*/
/********************************************************************************/

void handleUpdates(Collection<SourceBase> oldsrcs,
		      Map<SourceSet,SourceSet> srcupdates,
		      Map<JflowValue,JflowValue> valupdates)
{
   for (SourceProto sp : proto_map.values()) {
      sp.handleUpdates(oldsrcs,srcupdates,valupdates);
    }

   for (SourceBase sb : fixed_map.values()) {
      sb.handleUpdates(oldsrcs,srcupdates,valupdates);
    }

   for (SourceBase sb : mutable_map.values()) {
      sb.handleUpdates(oldsrcs,srcupdates,valupdates);
    }
}



/********************************************************************************/
/*										*/
/*	Methods to find local instances compatible with cast			*/
/*										*/
/********************************************************************************/

List<SourceBase> getLocalSources(BT_Class c)
{
   List<SourceBase> rslt = new ArrayList<SourceBase>();

   for (Map.Entry<BT_Class,List<SourceLocal>> ent : local_map.entrySet()) {
      BT_Class c1 = ent.getKey();
      if (compatibleTypes(c1,c)) {
	 rslt.addAll(ent.getValue());
       }
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Compatible type data							*/
/*										*/
/********************************************************************************/

static boolean compatibleTypes(BT_Class c1,BT_Class c2)
{
   Map<BT_Class,Boolean> m1 = compat_map.get(c1);
   if (m1 == null) {
      m1 = new HashMap<BT_Class,Boolean>(4);
      compat_map.put(c1,m1);
    }
   Boolean vl = m1.get(c2);
   if (vl == null) {
      if (c1.isDerivedFrom(c2)) vl = Boolean.TRUE;
      else if (!c1.isPrimitive() && c2 == BT_Class.findJavaLangObject()) vl = Boolean.TRUE;
      else if (c1.isArray() && c2.isArray()) {
	 return compatibleArrayTypes(c1.arrayType,c2.arrayType);
       }
      else vl = Boolean.FALSE;
      m1.put(c2,vl);
    }

   return vl.booleanValue();
}



private static boolean compatibleArrayTypes(BT_Class c1,BT_Class c2)
{
   if (c1.isPrimitive() || c2.isPrimitive()) return c1 == c2;

   return compatibleTypes(c1,c2);
}




}	// end of class SourceFactory




/* end of SourceFactory.java */

