/********************************************************************************/
/*										*/
/*		SourceSet.java							*/
/*										*/
/*	Representation of a set of sources for static checking			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/SourceSet.java,v 1.11 2018/08/02 15:10:18 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SourceSet.java,v $
 * Revision 1.11  2018/08/02 15:10:18  spr
 * Fix imports.
 *
 * Revision 1.10  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.9  2013/09/24 01:06:56  spr
 * Minor fix
 *
 * Revision 1.8  2011-04-23 00:43:31  spr
 * Code cleanpu
 *
 * Revision 1.7  2011-04-16 01:02:50  spr
 * Fixes to jflow for casting.
 *
 * Revision 1.6  2011-04-13 21:03:15  spr
 * Fix bugs in flow analysis.
 *
 * Revision 1.5  2009-09-17 01:57:20  spr
 * Fix a few minor bugs (poll, interfaces); code cleanup for Eclipse.
 *
 * Revision 1.4  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.3  2006-08-03 12:34:51  spr
 * Ensure fields of unprocessed classes handled correctly.
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

import com.ibm.jikesbt.BT_Class;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;




class SourceSet implements JflowConstants
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private BitSet		set_contents;
private Map<Object,SourceSet> next_map;
private boolean 	has_field;
private RestrictInfo	rest_info;
private BT_Class	enum_class;

private static SourceSetFactory set_factory;

private static final Object	MODEL_SET = new Object();




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SourceSet(FlowMaster jm,SourceSetFactory ssf,BitSet s)
{
   jflow_master = jm;
   set_factory = ssf;
   set_contents = (BitSet) s.clone();
   next_map = new HashMap<Object,SourceSet>();
   has_field = false;
   rest_info = new RestrictInfo();
   enum_class = BT_Class.forName("java.lang.Enum");
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public boolean contains(SourceBase s)
{
   return set_contents.get(s.getId());
}



public boolean isEmpty()
{
   return set_contents.isEmpty();
}



public int size()
{
   return set_contents.cardinality();
}



public boolean hasFieldSource()
{
   return has_field;
}


void setHasFieldSource(boolean fg)			{ has_field = fg; }




public boolean overlaps(SourceSet cs)
{
   if (cs == null) return false;
   if (set_contents.intersects(cs.set_contents)) return true;
   return false;
}



/********************************************************************************/
/*										*/
/*	Iterator methods							*/
/*										*/
/********************************************************************************/

public Iterable<SourceBase> getSources()
{
   return new SourceSetIterable(this);
}


private Iterator<SourceBase> getSourceIterator()
{
   return new SourceSetIterator(set_contents);
}



private static class SourceSetIterable implements Iterable<SourceBase> {

   SourceSet for_set;

   SourceSetIterable(SourceSet ss) {
      for_set = ss;
    }

   @Override public Iterator<SourceBase> iterator() {
      return for_set.getSourceIterator();
    }

}	// end of subclass SourceSetIterable



private class SourceSetIterator implements Iterator<SourceBase> {

   private BitSet bit_set;
   private int	  current_bit;

   SourceSetIterator(BitSet bs) {
      bit_set = bs;
      current_bit = bs.nextSetBit(0);
    }

   @Override public boolean hasNext()			{ return current_bit >= 0; }
   @Override public SourceBase next() {
      SourceBase cs = jflow_master.getSource(current_bit);
      current_bit = bit_set.nextSetBit(current_bit+1);
      return cs;
    }

   @Override public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

}	// end of subclass SourceSetIterator




/********************************************************************************/
/*										*/
/*	Set operation methods							*/
/*										*/
/********************************************************************************/

public SourceSet addToSet(SourceSet st)
{
   if (st == null || st == set_factory.createEmptySet()) return this;
   if (this == set_factory.createEmptySet()) return st;

   SourceSet rslt = this;
   SourceSet nxt = next_map.get(st);
   if (nxt != null) return nxt;

   BitSet bs = (BitSet) st.set_contents.clone();
   bs.andNot(set_contents);
   if (!bs.isEmpty()) {
      Vector<SourceBase> v = new Vector<SourceBase>();
      for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
	 v.add(jflow_master.getSource(i));
       }
      bs.or(set_contents);
      rslt = set_factory.findSet(bs);
      rslt.has_field = has_field | st.has_field;
      if (rslt.next_map.size() == 0) {
	 for (Map.Entry<Object,SourceSet> ent : next_map.entrySet()) {
	    Object o = ent.getKey();
	    if (o instanceof BT_Class) {
	       BT_Class bc = (BT_Class) o;
	       boolean okfg = true;
	       for (SourceBase cs : v) {
		  if (cs.getDataType() == null || SourceFactory.compatibleTypes(cs.getDataType(),bc)) {
		     okfg = false;
		     break;
		   }
		}
	       if (okfg) rslt.next_map.put(bc,ent.getValue());
	     }
	  }
       }
    }

   next_map.put(st,rslt);

   return rslt;
}



public SourceSet restrictByType(BT_Class dt,boolean pfg)
{
   return handleTypeRestricts(dt,pfg,false);
}


public SourceSet removeByType(BT_Class dt)
{
   return handleTypeRestricts(dt,false,true);
}



private SourceSet handleTypeRestricts(BT_Class dt,boolean pfg,boolean comp)
{
   if (dt == null) {
      System.err.println("RESTRICT USING NULL CLASS");
      return this;
    }

   rest_info.set(dt,pfg,comp);
   SourceSet nxt = next_map.get(rest_info);
   if (nxt != null) return nxt;

   boolean hasfld = false;
   BitSet bs = null;
   boolean havedt = false;
   for (SourceBase cs : getSources()) {
      boolean fg;
      if (cs.getDataType() == null) fg = true;
      else {
	 fg = SourceFactory.compatibleTypes(cs.getDataType(),dt);
	 if (comp) fg = !fg;
       }
      if (!fg) {
	 if (bs == null) bs = (BitSet) set_contents.clone();
	 bs.clear(cs.getId());
	 if (!pfg || dt.isDescendentOf(enum_class) ) {
	    List<SourceBase> bl = jflow_master.getLocalSources(dt);
	    // TODO: want to revisit this instruction if new sources are added
	    if (bl.isEmpty()) {
	       SourceBase mt = cs.mutateTo(dt);
	       if (mt != null) bl.add(mt);
	     }
	    for (SourceBase sb : bl) {
	       bs.set(sb.getId());
	       if (sb.getFieldSource() != null) hasfld = true;
	       havedt = true;
	     }
	  }
	 else if (FlowMaster.doDebug()) {
	    System.err.println("\tIGNORE CAST to "+ dt.getName() + "FROM " + cs);
	  }
       }
      else {
	 if (!havedt && cs.getDataType() != null) havedt = true;
	 if (cs.getFieldSource() != null) hasfld = true;
       }
    }

   if (bs != null && !havedt) {
      bs.clear();
      hasfld = false;
    }

   SourceSet rslt = (bs == null ? this : set_factory.findSet(bs));
   rslt.has_field = hasfld;

   RestrictInfo ri = new RestrictInfo(dt,pfg,comp);
   next_map.put(ri,rslt);

   return rslt;
}



private static class RestrictInfo {

   private BT_Class rest_class;
   private boolean rest_project;
   private boolean rest_comp;

   RestrictInfo() {
      this(null,false,false);
    }

   RestrictInfo(BT_Class bc,boolean pro,boolean cmp) {
      rest_class = bc;
      rest_project = pro;
      rest_comp = cmp;
    }

   void set(BT_Class bc,boolean pro,boolean cmp) {
      rest_class = bc;
      rest_project = pro;
      rest_comp = cmp;
    }

   @Override public boolean equals(Object o) {
      if (o == null || !(o instanceof RestrictInfo)) return false;
      RestrictInfo ri = (RestrictInfo) o;
      return rest_class == ri.rest_class && rest_project == ri.rest_project &&
	 rest_comp == ri.rest_comp;
    }

   @Override public int hashCode() {
      int hc = (rest_class == null ? 0 : rest_class.hashCode());
      if (rest_project) hc += 1024;
      if (rest_comp) hc += 2048;
      return hc;
    }

}	// end of subclass RestrictInfo




/********************************************************************************/
/*										*/
/*	Model set methods							*/
/*										*/
/********************************************************************************/

public SourceSet getModelSet()
{
   SourceSet ss = next_map.get(MODEL_SET);
   if (ss == null) {
      BitSet bs = null;
      for (SourceBase cs : getSources()) {
	 if (cs.isModel()) {
	    if (bs == null) bs = new BitSet();
	    bs.set(cs.getId());
	  }
       }
      if (bs == null) ss = set_factory.createEmptySet();
      else ss = set_factory.findSet(bs);
      next_map.put(MODEL_SET,ss);
    }

   return ss;
}




/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   StringBuffer buf = new StringBuffer();
   buf.append("{");
   int ctr = 0;
   for (SourceBase cs : getSources()) {
      if (ctr++ != 0) buf.append(",");
      buf.append(cs.toString());
    }
   buf.append("}");
   return buf.toString();
}



}	// end of class SourceSet




/* end of SourceSet.java */
