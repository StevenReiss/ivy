/********************************************************************************/
/*										*/
/*		ProtoCollection.java						*/
/*										*/
/*	Collection prototype implementation for flow analysis			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/ProtoCollection.java,v 1.13 2018/08/02 15:10:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ProtoCollection.java,v $
 * Revision 1.13  2018/08/02 15:10:17  spr
 * Fix imports.
 *
 * Revision 1.12  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.11  2011-04-13 21:03:14  spr
 * Fix bugs in flow analysis.
 *
 * Revision 1.10  2010-02-12 00:34:14  spr
 * Add some fixups for eclipse.
 *
 * Revision 1.9  2009-09-17 01:57:20  spr
 * Fix a few minor bugs (poll, interfaces); code cleanup for Eclipse.
 *
 * Revision 1.8  2007-08-10 02:10:39  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.7  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.6  2007-01-03 14:04:59  spr
 * Fix imports
 *
 * Revision 1.5  2007-01-03 03:24:18  spr
 * Modifications to handle incremental update.
 *
 * Revision 1.4  2006-12-01 03:22:47  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.3  2006/07/23 02:25:02  spr
 * Minor bug fixes and speed ups.
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


import edu.brown.cs.ivy.jflow.JflowFlags;
import edu.brown.cs.ivy.jflow.JflowValue;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_ClassVector;
import com.ibm.jikesbt.BT_Method;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProtoCollection extends ProtoBase
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private ValueBase	element_value;
private SourceBase	array_source;
private SourceBase	iter_source;
private SourceBase	listiter_source;
private SourceBase	enum_source;
private ValueBase	comparator_value;

private Set<FlowCallSite> element_change;
private Set<FlowCallSite> first_element;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public ProtoCollection(FlowMaster jm,BT_Class bc)
{
   super(jm,bc);

   element_value = null;
   array_source = null;
   iter_source = null;
   listiter_source = null;
   enum_source = null;
   comparator_value = null;

   first_element = new HashSet<>(4);
   element_change = new HashSet<>();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public void setNative()
{
   ValueBase v0 = jflow_master.mutableValue(BT_Class.findJavaLangObject());
   mergeElementValue(v0);
}




/********************************************************************************/
/*										*/
/*	Set methods								*/
/*										*/
/********************************************************************************/

public ValueBase prototype__constructor(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   if (args.size() == 2) {
      BT_ClassVector cv = bm.getSignature().types;
      BT_Class a1 = cv.elementAt(0);
      if (a1 == BT_Class.getInt()) ;
      else if (a1.getName().equals("java.util.Comparator")) {
	 comparator_value = args.get(1);
       }
      else {
	 prototype_addAll(bm,args,frm,cf);
       }
    }

   return returnAny(bm);
}




public ValueBase prototype_add(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl _cf)
{
   ValueBase nv = args.get(1);

   if (args.size() == 3) {
      if (nv.getDataType() == BT_Class.getInt()) {	// add(int,Object), set(int,Object)
	 nv = args.get(2);
       }
    }

   ValueBase ov = element_value;
   mergeElementValue(nv);

   if (bm.getSignature().returnType != BT_Class.getVoid()) {
      addElementChange(frm);

      if (nv == null) return null;
      else if (bm.getSignature().returnType == BT_Class.findJavaLangObject()) return nv;
      else if (ov == null) return returnTrue();
      // check types for possible returnFalse ?
    }

   return returnAny(bm);
}



public ValueBase prototype_addAll(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl _cf)
{
   ValueBase nv;

   if (args.size() == 2) {			// addAll(Collection)
      nv = args.get(1);
    }
   else {
      nv = args.get(2); 			// addAll(int,Collection)
    }

   boolean canchng = false;

   for (SourceBase cs : nv.getSources()) {
      ProtoBase cp = cs.getPrototype();
      if (cp != null && cp instanceof ProtoCollection) {
	 ProtoCollection pc = (ProtoCollection) cp;
	 pc.addElementChange(frm);
	 if (pc.element_value != null) {
	    mergeElementValue(pc.element_value);
	    canchng = true;
	  }
       }
    }

   if (!canchng) return returnFalse();

   return returnAny(bm);
}



public ValueBase prototype_push(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   if (FlowMaster.doDebug()) System.err.println("PROTO PUSH " + bm.getSignature().returnType);

   ValueBase nv = args.get(1);

   prototype_add(bm,args,frm,cf);

   return nv;
}



public ValueBase prototype_addElement(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_add(bm,args,frm,cf);
}



public ValueBase prototype_addFirst(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_add(bm,args,frm,cf);
}




public ValueBase prototype_addLast(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_add(bm,args,frm,cf);
}



public ValueBase prototype_offer(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_add(bm,args,frm,cf);
}



public ValueBase prototype_put(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_add(bm,args,frm,cf);
}



public ValueBase prototype_clear(BT_Method bm,List<ValueBase> _args,FlowCallSite _frm,FlowControl _cf)
{
   return returnAny(bm);
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
   if (comparator_value != null) return comparator_value;

   return returnNull(bm);
}



public ValueBase prototype_contains(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl _cf)
{
   ValueBase v = args.get(1);

   addElementChange(frm);

   if (element_value == null) return returnFalse();
   else if (v.mustBeNull() && !element_value.canBeNull()) return returnFalse();
   // else check data type compatability

   return returnAny(bm);
}




public ValueBase prototype_containsAll(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl _cf)
{
   ValueBase nv = args.get(1);
   boolean canbetrue = true;
   boolean canbefalse = true;

   addElementChange(frm);

   for (SourceBase cs : nv.getSources()) {
      ProtoBase cp = cs.getPrototype();
      if (cp != null && cp instanceof ProtoCollection) {
	 ProtoCollection pc = (ProtoCollection) cp;
	 pc.addElementChange(frm);
	 // check if must be true or false
       }
    }

   if (!canbetrue && canbefalse) return returnFalse();
   if (canbetrue && !canbefalse) return returnTrue();

   return returnAny(bm);
}




public ValueBase prototype_copyInto(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   prototype_toArray(bm,args,frm,cf);

   return returnVoid();
}




public ValueBase prototype_elementAt(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_elements(BT_Method _bm,List<ValueBase> _args,FlowCallSite _frm,FlowControl _cf)
{
   if (enum_source == null) {
      ProtoBase cp = new CollectionEnum(jflow_master);
      BT_Class cls = BT_Class.forName("java.util.Enumeration");
      enum_source = jflow_master.createPrototypeSource(cls,cp);
    }

   SourceSet cset = jflow_master.createSingletonSet(enum_source);
   ValueBase cv = jflow_master.objectValue(BT_Class.forName("java.util.Enumeration"),cset,JflowFlags.NON_NULL);

   return cv;
}



public ValueBase prototype_equals(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl _cf)
{
   ValueBase nv = args.get(1);
   boolean canbetrue = true;
   boolean canbefalse = true;

   addElementChange(frm);

   for (SourceBase cs : nv.getSources()) {
      ProtoBase cp = cs.getPrototype();
      if (cp != null && cp instanceof ProtoCollection) {
	 ProtoCollection pc = (ProtoCollection) cp;
	 pc.addElementChange(frm);
	 // check if must be true or false
       }
    }

   if (!canbetrue && canbefalse) return returnFalse();
   if (canbetrue && !canbefalse) return returnTrue();

   return returnAny(bm);
}




public ValueBase prototype_first(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_firstElement(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_get(BT_Method bm,List<ValueBase> _args,FlowCallSite frm,FlowControl _cf)
{
   if (bm.getSignature().returnType != BT_Class.getVoid()) addElementChange(frm);

   return element_value;
}



public ValueBase prototype_getFirst(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_getLast(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_peek(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_pop(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_poll(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_take(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_hashCode(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   return returnAny(bm);
}



public ValueBase prototype_headSet(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_subList(bm,args,frm,cf);
}



public ValueBase prototype_indexOf(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   return returnAny(bm);
}



public ValueBase prototype_insertElementAt(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_add(bm,args,frm,cf);
}



public ValueBase prototype_isEmpty(BT_Method bm,List<ValueBase> _args,FlowCallSite frm,FlowControl _cf)
{
   first_element.add(frm);

   if (element_value == null) return returnTrue();

   return returnAny(bm);
}



public ValueBase prototype_iterator(BT_Method _bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   if (iter_source == null) {
      ProtoBase cp = new CollectionIter(jflow_master);
      BT_Class cls = BT_Class.forName("java.util.Iterator");
      iter_source = jflow_master.createPrototypeSource(cls,cp);
    }

   SourceSet cset = jflow_master.createSingletonSet(iter_source);
   ValueBase cv = jflow_master.objectValue(BT_Class.forName("java.util.Iterator"),cset,JflowFlags.NON_NULL);

   return cv;
}




public ValueBase prototype_last(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_lastElement(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_get(bm,args,frm,cf);
}



public ValueBase prototype_lastIndexOf(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   return returnAny(bm);
}



public ValueBase prototype_listIterator(BT_Method _bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   if (listiter_source == null) {
      ProtoBase cp = new CollectionListIter(jflow_master);
      BT_Class cls = BT_Class.forName("java.util.ListIterator");
      listiter_source = jflow_master.createPrototypeSource(cls,cp);
    }

   SourceSet cset = jflow_master.createSingletonSet(listiter_source);
   ValueBase cv = jflow_master.objectValue(BT_Class.forName("java.util.ListIterator"),cset,JflowFlags.NON_NULL);

   return cv;
}




public ValueBase prototype_remove(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   if (bm.getSignature().returnType != BT_Class.getVoid()) addElementChange(frm);

   if (args.size() >= 2) {
      ValueBase v = args.get(1);

      if (v.getDataType() == BT_Class.getInt()) {	   // remove(int index)
	 return prototype_get(bm,args,frm,cf);
       }

      if (element_value == null) return returnFalse();
      else if (v.mustBeNull() && !element_value.canBeNull()) return returnFalse();
      // else check data type compatability
    }

   if (bm.getSignature().returnType == BT_Class.findJavaLangObject()) {
      ValueBase cv = element_value;
      if (cv != null) {
	 cv = cv.allowNull();
	 return cv;
       }
      returnNull(bm);
    }

   return returnAny(bm);
}



public ValueBase prototype_removeAll(BT_Method bm,List<ValueBase> _args,FlowCallSite _frm,FlowControl _cf)
{
   return returnAny(bm);
}



public ValueBase prototype_removeAllElements(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_clear(bm,args,frm,cf);
}



public ValueBase prototype_removeElement(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_remove(bm,args,frm,cf);
}



public ValueBase prototype_removeElementAt(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_remove(bm,args,frm,cf);
}



public ValueBase prototype_removeFirst(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   ValueBase cv = prototype_remove(bm,args,frm,cf);

   return cv;
}



public ValueBase prototype_removeLast(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_remove(bm,args,frm,cf);
}



public ValueBase prototype_removeRange(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_remove(bm,args,frm,cf);
}



public ValueBase prototype_retainAll(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf)
{
   return returnAny(bm);
}



public ValueBase prototype_set(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_add(bm,args,frm,cf);
}



public ValueBase prototype_setElementAt(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_add(bm,args,frm,cf);
}



public ValueBase prototype_setSize(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   ValueBase cv = args.get(1);
   Object pv = cv.getProgramValue();
   if (pv != null && pv instanceof Long) {
      Long lv = (Long) pv;
      if (lv.intValue() == 0) return prototype_removeAll(bm,args,frm,cf);
    }

   // first simulate add of null
   ValueBase nullv = jflow_master.nullValue();
   mergeElementValue(nullv);

   // then simulate a remove
   return prototype_remove(bm,args,frm,cf);
}



public ValueBase prototype_size(BT_Method bm,List<ValueBase> _args,FlowCallSite frm,FlowControl _cf)
{
   first_element.add(frm);

   if (element_value == null) {
      return jflow_master.rangeValue(BT_Class.getInt(),0,0);
    }

   return returnAny(bm);
}



public ValueBase prototype_subList(BT_Method _bm,List<ValueBase> _args,FlowCallSite _frm,FlowControl _cf)
{
   BT_Class cls = BT_Class.forName("java.util.List");
   SourceBase subs = jflow_master.createPrototypeSource(cls,this);

   SourceSet cset = jflow_master.createSingletonSet(subs);
   ValueBase cv = jflow_master.objectValue(BT_Class.forName("java.util.List"),cset,JflowFlags.NON_NULL);

   return cv;
}




public ValueBase prototype_subSet(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_subList(bm,args,frm,cf);
}



public ValueBase prototype_tailSet(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   return prototype_subList(bm,args,frm,cf);
}



public ValueBase prototype_toArray(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf)
{
   ValueBase cv;

   if (bm.getSignature().returnType != BT_Class.getVoid()) addElementChange(frm);

   if (args.size() == 2) {
      cv = args.get(1);
      for (SourceBase cs : cv.getSources()) {
	 if (cs.addToArrayContents(element_value,null)) {
	    cf.noteArrayChange(cs);
	  }
       }
    }
   else {
      if (array_source == null) {
	 array_source = jflow_master.createArraySource(BT_Class.findJavaLangObject(),
								prototype_size(bm,null,frm,cf));
       }
      if (array_source.addToArrayContents(element_value,null)) {
	 cf.noteArrayChange(array_source);
       }
      SourceSet cset = jflow_master.createSingletonSet(array_source);
      cv = jflow_master.objectValue(array_source.getDataType(),cset,JflowFlags.NON_NULL);
    }

   return cv;
}



/********************************************************************************/
/*										*/
/*	Methods for handling the value and changes				*/
/*										*/
/********************************************************************************/

void mergeElementValue(ValueBase cv)
{
   if (element_value == null) setElementValue(cv);
   else if (cv != null) setElementValue(element_value.mergeValue(cv));
}




void setElementValue(ValueBase cv)
{
   if (cv == element_value || cv == null) return;

   if (element_value == null) {
      for (FlowCallSite cs : first_element) {
	 cs.requeue();
       }
      first_element.clear();
    }

   element_value = cv;

   for (FlowCallSite cs : element_change) {
      cs.requeue();
    }
}



void addElementChange(FlowCallSite frm)
{
   if (frm != null) element_change.add(frm);
}



void addFirstElement(FlowCallSite frm)
{
   if (frm != null) first_element.add(frm);
}



ValueBase getElementValue()				{ return element_value; }




/********************************************************************************/
/*										*/
/*	Methods for incremental update						*/
/*										*/
/********************************************************************************/

@Override void handleUpdates(Collection<SourceBase> oldsrcs,
		      Map<SourceSet,SourceSet> srcupdates,
		      Map<JflowValue,JflowValue> valupdates)
{
   if (element_value != null) {
      JflowValue jv = valupdates.get(element_value);
      if (jv != null) element_value = (ValueBase) jv;
    }
   if (comparator_value != null) {
      JflowValue jv = valupdates.get(comparator_value);
      if (jv != null) comparator_value = (ValueBase) jv;
    }

   if (array_source != null) {
      array_source.handleUpdates(oldsrcs,srcupdates,valupdates);
    }
}



/********************************************************************************/
/*										*/
/*	Prototype iterator for collections					*/
/*										*/
/********************************************************************************/

@SuppressWarnings("unused")
private class CollectionIter extends ProtoBase {

   CollectionIter(FlowMaster jm) {
      super(jm,BT_Class.forName("java.util.Iterator"));
    }

   public ValueBase prototype_hasNext(BT_Method bm,List<?> _args,FlowCallSite frm,FlowControl _cf) {
      first_element.add(frm);
      if (element_value == null) return returnFalse();
      return returnAny(bm);
    }

   public ValueBase prototype_next(BT_Method _bm,List<?> _args,FlowCallSite frm,FlowControl _cf) {
      addElementChange(frm);
      return element_value;
    }

   public ValueBase prototype_remove(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf) {
      return returnAny(bm);
    }

}	// end of subclass CollectionIter




/********************************************************************************/
/*										*/
/*	Prototype iterator for collections -- not currently used		*/
/*										*/
/********************************************************************************/

@SuppressWarnings("unused")
private class CollectionListIter extends ProtoBase {

   CollectionListIter(FlowMaster jm) {
      super(jm,BT_Class.forName("java.util.ListIterator"));
    }

   public ValueBase prototype_add(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf) {
      return ProtoCollection.this.prototype_add(bm,args,frm,cf);
    }

   public ValueBase prototype_hasNext(BT_Method bm,List<?> _args,FlowCallSite frm,FlowControl _cf) {
      first_element.add(frm);
      if (element_value == null) return returnFalse();
      return returnAny(bm);
    }

   public ValueBase prototype_hasPrevious(BT_Method bm,List<?> _args,FlowCallSite frm,FlowControl _cf) {
      first_element.add(frm);
      if (element_value == null) return returnFalse();
      return returnAny(bm);
    }

   public ValueBase prototype_next(BT_Method _bm,List<?> _args,FlowCallSite frm,FlowControl _cf) {
      addElementChange(frm);
      return element_value;
    }

   public ValueBase prototype_nextIndex(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf) {
      return returnAny(bm);
    }

   public ValueBase prototype_previous(BT_Method _bm,List<?> _args,FlowCallSite frm,FlowControl _cf) {
      addElementChange(frm);
      return element_value;
    }

   public ValueBase prototype_previousIndex(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf) {
      return returnAny(bm);
    }

   public ValueBase prototype_remove(BT_Method bm,List<?> _args,FlowCallSite _frm,FlowControl _cf) {
      return returnAny(bm);
    }

   public ValueBase prototype_set(BT_Method bm,List<ValueBase> args,FlowCallSite frm,FlowControl cf) {
      return ProtoCollection.this.prototype_set(bm,args,frm,cf);
    }

}	// end of subclass CollectionListIter




/********************************************************************************/
/*										*/
/*	Prototype enumerator for collections					*/
/*										*/
/********************************************************************************/

@SuppressWarnings("unused")
private class CollectionEnum extends ProtoBase {

   CollectionEnum(FlowMaster jm) {
      super(jm,BT_Class.forName("java.util.Enumeration"));
    }

   public ValueBase prototype_hasMoreElements(BT_Method bm,List<?> _args,FlowCallSite frm,FlowControl _cf) {
      first_element.add(frm);
      if (element_value == null) return returnFalse();
      return returnAny(bm);
    }

   public ValueBase prototype_nextElement(BT_Method _bm,List<?> _args,FlowCallSite frm,FlowControl _cf) {
      addElementChange(frm);
      return element_value;
    }

}	// end of subclass CollectionEnum




}	// end of class ProtoCollection




/* end of ProtoCollection.java */
