/********************************************************************************/
/*										*/
/*		SourceSetFactory.java						*/
/*										*/
/*	Factory for creating source sets					*/
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


import edu.brown.cs.ivy.jflow.JflowConstants;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;



public class SourceSetFactory implements JflowConstants
{




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private Map<BitSet,SourceSet> set_table;
private SourceSet  empty_set;
private Map<SourceBase,SourceSet> single_map;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SourceSetFactory(FlowMaster jm)
{
   jflow_master = jm;
   set_table = new WeakHashMap<BitSet,SourceSet>();
   empty_set = null;
   single_map = new HashMap<SourceBase,SourceSet>();
}




public void cleanup()
{
   set_table = new HashMap<>();
   single_map = new HashMap<>();
}


/********************************************************************************/
/*										*/
/*	Methods for building sets						*/
/*										*/
/********************************************************************************/

public SourceSet createEmptySet()
{
   if (empty_set == null) {
      BitSet hs = new BitSet(1);
      empty_set = findSetInternal(hs);
    }
   return empty_set;
}



public SourceSet createSingletonSet(SourceBase s)
{
   if (s == null) return createEmptySet();

   SourceSet cs = single_map.get(s);
   if (cs == null) {
      int id = s.getId();
      BitSet bs = new BitSet(id+1);
      bs.set(id);
      cs = findSetInternal(bs);
      if (s.getFieldSource() != null) cs.setHasFieldSource(true);
      single_map.put(s,cs);
    }

   return cs;
}



SourceSet findSet(BitSet s)
{
   if (s.isEmpty()) return createEmptySet();

   return findSetInternal(s);
}




private SourceSet findSetInternal(BitSet s)
{
   SourceSet cs = set_table.get(s);
   if (cs == null) {
      s = (BitSet) s.clone();
      cs = new SourceSet(jflow_master,this,s);
      set_table.put(s,cs);
    }
   return cs;
}



/********************************************************************************/
/*										*/
/*	Methods for handling incremental updates				*/
/*										*/
/********************************************************************************/

Map<SourceSet,SourceSet> removeSources(Collection<SourceBase> srcs)
{
   if (srcs == null || srcs.isEmpty()) return null;

   Map<SourceSet,SourceSet> updatemap = new HashMap<SourceSet,SourceSet>();

   int mid = 0;
   for (SourceBase sb : srcs) {
      if (sb != null) {
	 int id = sb.getId();
	 if (id > mid) mid = id;
       }
    }
   BitSet del = new BitSet(mid+1);
   for (SourceBase sb : srcs) {
      if (sb != null) {
	 int id = sb.getId();
	 del.set(id);
       }
    }

   Collection<BitSet> sets = new ArrayList<BitSet>(set_table.keySet());

   for (BitSet src : sets) {
      if (src.intersects(del)) {
	 BitSet rslt = (BitSet) src.clone();
	 rslt.andNot(del);
	 SourceSet nset = findSetInternal(rslt);
	 SourceSet oset = set_table.get(src);
	 updatemap.put(oset,nset);
       }
    }

   return updatemap;
}




}	// end of class SourceSetFactory



/* end of SourceSetFactory.java */
