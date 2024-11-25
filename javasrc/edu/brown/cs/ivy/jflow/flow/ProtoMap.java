/********************************************************************************/
/*										*/
/*		ProtoMap.java							*/
/*										*/
/*	Map prototype implementation for flow analysis				*/
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

import edu.brown.cs.ivy.jflow.JflowFlags;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Method;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class ProtoMap extends ProtoBase
{


private ProtoCollection key_set;
private ProtoCollection value_set;
private ProtoCollection entry_set;
private SourceBase	key_source;
private SourceBase	value_source;
private SourceBase	entry_source;

private SourceBase	map_source;
private ValueBase	map_value;

private boolean 	is_empty;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public ProtoMap(FlowMaster jm,BT_Class bc)
{
   super(jm,bc);

   is_empty = true;
   key_set = new ProtoCollection(jflow_master,BT_Class.forName("java.util.Set"));
   value_set = new ProtoCollection(jflow_master,BT_Class.forName("java.util.Set"));
   entry_set = new ProtoCollection(jflow_master,BT_Class.forName("java.util.Set"));
   key_source = null;
   value_source = null;
   entry_source = null;

   MapEntry ent = new MapEntry(jflow_master);
   map_source = jflow_master.createPrototypeSource(BT_Class.forName("java.util.Map$Entry"),ent);
   SourceSet cset = jflow_master.createSingletonSet(map_source);
   map_value = jflow_master.objectValue(map_source.getDataType(),cset,JflowFlags.NON_NULL);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public void setNative()
{
   key_set.setNative();
   value_set.setNative();
   entry_set.setNative();
}



// CHECKSTYLE:OFF
/********************************************************************************/
/*										*/
/*	Map methods								*/
/*										*/
/********************************************************************************/

public ValueBase prototype__constructor(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   if (args.size() == 2) {
      ValueBase cv = args.get(1);
      if (cv.getDataType() != BT_Class.getInt()) prototype_putAll(bm,args,frm,cf);
    }

   return returnAny(bm);
}



public ValueBase prototype_clear(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   return returnAny(bm);
}




public ValueBase prototype_containsKey(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return key_set.prototype_contains(bm,args,frm,cf);
}




public ValueBase prototype_containsValue(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return value_set.prototype_contains(bm,args,frm,cf);
}




public ValueBase prototype_contains(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return value_set.prototype_contains(bm,args,frm,cf);
}




public ValueBase prototype_elements(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return value_set.prototype_elements(bm,args,frm,cf);
}



public ValueBase prototype_entrySet(BT_Method _bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   if (entry_source == null) {
      entry_source = jflow_master.createPrototypeSource(entry_set.getDataType(),entry_set);
    }

   SourceSet cset = jflow_master.createSingletonSet(entry_source);
   return jflow_master.objectValue(entry_source.getDataType(),cset,JflowFlags.NON_NULL);
}




public ValueBase prototype_equals(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   return returnAny(bm);
}




public ValueBase prototype_get(BT_Method bm,List<?> _args,FlowCallSite frm,FlowControl _cf)
{
   value_set.addElementChange(frm);

   ValueBase cv = value_set.getElementValue();
   if (cv == null) return returnNull(bm);

   cv = cv.allowNull();

   return cv;
}



public ValueBase prototype_hashCode(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   return returnAny(bm);
}




public ValueBase prototype_isEmpty(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return key_set.prototype_isEmpty(bm,args,frm,cf);
}




public ValueBase prototype_keySet(BT_Method _bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   if (key_source == null) {
      key_source = jflow_master.createPrototypeSource(key_set.getDataType(),key_set);
    }

   SourceSet cset = jflow_master.createSingletonSet(key_source);
   return jflow_master.objectValue(key_source.getDataType(),cset,JflowFlags.NON_NULL);
}




public ValueBase prototype_keys(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return key_set.prototype_elements(bm,args,frm,cf);
}



public ValueBase prototype_put(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   setNonEmpty();

   ValueBase cv = value_set.getElementValue();

   ValueBase kv = args.get(1);
   ValueBase ov = args.get(2);

   List<ValueBase> nargs = new Vector<ValueBase>();
   nargs.add(args.get(0));
   nargs.add(kv);

   key_set.prototype_add(bm,nargs,frm,cf);

   nargs.set(1,ov);
   value_set.prototype_add(bm,nargs,frm,cf);

   if (cv == null) return returnNull(bm);
   cv = cv.allowNull();

   return cv;
}




public ValueBase prototype_putIfAbsent(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_put(bm,args,frm,cf);
}



public ValueBase prototype_putAll(BT_Method bm,List<ValueBase> args,FlowCallSite _frm,FlowControl _cf)
{
   setNonEmpty();

   ValueBase nv = args.get(1);
   boolean addany = false;
   for (SourceBase cs : nv.getSources()) {
      ProtoBase cp = cs.getPrototype();
      if (cp != null && cp instanceof ProtoMap) {
	 ProtoMap pm = (ProtoMap) cp;
	 key_set.mergeElementValue(pm.key_set.getElementValue());
	 value_set.mergeElementValue(pm.value_set.getElementValue());
       }
      else addany = true;
    }

   if (addany) {
      ValueBase cv = jflow_master.mutableValue(BT_Class.findJavaLangObject());
      key_set.mergeElementValue(cv);
      value_set.mergeElementValue(cv);
    }

   return returnAny(bm);
}




public ValueBase prototype_remove(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   key_set.prototype_remove(bm,args,frm,cf);

   ValueBase cv = value_set.getElementValue();
   if (cv == null) return returnNull(bm);
   cv = cv.allowNull();

   return cv;
}



public ValueBase prototype_size(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return key_set.prototype_size(bm,args,frm,cf);
}



public ValueBase prototype_values(BT_Method _bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   if (value_source == null) {
      value_source = jflow_master.createPrototypeSource(value_set.getDataType(),value_set);
    }

   SourceSet cset = jflow_master.createSingletonSet(value_source);
   return jflow_master.objectValue(value_source.getDataType(),cset,JflowFlags.NON_NULL);
}



public ValueBase prototype_clone(BT_Method _bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   SourceBase subs = jflow_master.createPrototypeSource(getDataType(),this);

   SourceSet cset = jflow_master.createSingletonSet(subs);
   ValueBase cv = jflow_master.objectValue(getDataType(),cset,JflowFlags.NON_NULL);

   return cv;
}



public ValueBase prototype_comparator(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   return returnAny(bm);
}




public ValueBase prototype_firstKey(BT_Method bm,List<?> _args,FlowCallSite frm,FlowControl _cf)
{
   key_set.addElementChange(frm);

   ValueBase cv = key_set.getElementValue();
   if (cv == null) return returnNull(bm);

   return cv;
}




public ValueBase prototype_lastKey(BT_Method bm,List<?> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_firstKey(bm,args,frm,cf);
}



public ValueBase prototype_subMap(BT_Method _bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   BT_Class cls = BT_Class.forName("java.util.Map");
   SourceBase subs = jflow_master.createPrototypeSource(cls,this);
   SourceSet cset = jflow_master.createSingletonSet(subs);
   ValueBase cv = jflow_master.objectValue(cls,cset,JflowFlags.NON_NULL);

   return cv;
}



public ValueBase prototype_headMap(BT_Method bm,List<?> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_subMap(bm,args,frm,cf);
}



public ValueBase prototype_tailMap(BT_Method bm,List<?> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_subMap(bm,args,frm,cf);
}




/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

private void setNonEmpty()
{
   if (is_empty) {
      entry_set.setElementValue(map_value);
      is_empty = false;
    }
}




/********************************************************************************/
/*										*/
/*	Methods for incremental update						*/
/*										*/
/********************************************************************************/

@Override void handleUpdates(Collection<SourceBase> oldsrcs,
		      Map<SourceSet,SourceSet> srcupdates,
		      Map<JflowValue,JflowValue> valupdates)
{
   if (map_value != null) {
      JflowValue jv = valupdates.get(map_value);
      if (jv != null) map_value = (ValueBase) jv;
    }
}



/********************************************************************************/
/*										*/
/*	Prototype for Map.Entry 						*/
/*										*/
/********************************************************************************/

@SuppressWarnings("unused")
private class MapEntry extends ProtoBase {

   MapEntry(FlowMaster jm) {
      super(jm,BT_Class.forName("java.util.Map$Entry"));
    }

   public ValueBase prototype_getKey(BT_Method _bm,List<?> _args,FlowCallSite frm,FlowControl _cf) {
      key_set.addElementChange(frm);
      return key_set.getElementValue();
    }

   public ValueBase prototype_getValue(BT_Method _bm,List<?> _args,FlowCallSite frm,FlowControl _cf) {
      value_set.addElementChange(frm);
      return value_set.getElementValue();
    }

   public ValueBase prototype_setValue(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf) {
      return value_set.prototype_add(bm,args,frm,cf);
    }

}	// end of class Map.Entry




}	// end of class ProtoMap




/* end of ProtoMap.java */
