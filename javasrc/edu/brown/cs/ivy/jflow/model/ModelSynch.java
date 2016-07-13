/********************************************************************************/
/*										*/
/*		ModelSynch.java 						*/
/*										*/
/*	Routines to handling model syncrhonization information			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/model/ModelSynch.java,v 1.3 2007-05-04 02:00:03 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ModelSynch.java,v $
 * Revision 1.3  2007-05-04 02:00:03  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.2  2006/07/10 14:52:19  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:37  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.model;

import edu.brown.cs.ivy.jflow.*;

import java.util.*;



class ModelSynch implements JflowConstants
{




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Collection<ModelMethod> model_methods;
private Collection<ModelSourceSet> synch_sets;
private JflowSource []	source_set;
private List<Partition> partition_list;
private Map<Collection<JflowSource>,ModelSourceSet> partition_map;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelSynch(Collection<ModelMethod> methods)
{
   model_methods = methods;
   synch_sets = null;
   partition_list = null;
   partition_map = null;
   source_set = null;
}



/********************************************************************************/
/*										*/
/*	Methods to do partition refinement on the synchronization sets		*/
/*										*/
/********************************************************************************/

void establishPartitions()
{
   Set<JflowSource> srcset = getSourceSet();

   if (srcset.isEmpty() || srcset.size() <= 1) return;

   JflowSource [] srcs = new JflowSource[srcset.size()];
   source_set = srcset.toArray(srcs);

   partition_list = new Vector<Partition>();
   Partition p0 = new Partition(0,srcs.length);
   partition_list.add(p0);

   for (ModelSourceSet cs : synch_sets) {
      refinePartitions(cs);
    }

   setupPartitionMap();

   replaceSourceSets();
}



/********************************************************************************/
/*										*/
/*	Methods to get the overall set of sources				*/
/*										*/
/********************************************************************************/

private Set<JflowSource> getSourceSet()
{
   Set<JflowSource> sources = new HashSet<JflowSource>();

   for (ModelMethod mm : model_methods) {
      Set<JflowModel.Node> sts = getStateSet(mm);
      for (JflowModel.Node mn : sts) {
	 Collection<JflowSource> css = mn.getWaitSet();
	 if (css != null) {
	    ModelSourceSet cs = new ModelSourceSet(css);
	    if (synch_sets == null) synch_sets = new HashSet<ModelSourceSet>();
	    synch_sets.add(cs);
	    for (JflowSource src : cs) {
	       sources.add(src);
	     }
	  }
       }
    }

   return sources;
}




private Set<JflowModel.Node> getStateSet(JflowModel.Method mm)
{
   Set<JflowModel.Node> s = new HashSet<JflowModel.Node>();
   addStateToSet(mm.getStartNode(),s);
   return s;
}



private void addStateToSet(JflowModel.Node n,Set<JflowModel.Node> s)
{
   if (s.contains(n)) return;
   s.add(n);
   for (JflowModel.Node nn : n.getTransitions()) {
      addStateToSet(nn,s);
    }
}




/********************************************************************************/
/*										*/
/*	Methods to refine the partition based on a set				*/
/*										*/
/********************************************************************************/

private void refinePartitions(ModelSourceSet cs)
{
   for (ListIterator<Partition> it = partition_list.listIterator(); it.hasNext(); ) {
      Partition p = it.next();
      Partition np = refinePartition(p,cs);
      if (np != null) it.add(np);
    }
}



private Partition refinePartition(Partition p,ModelSourceSet cs)
{
   Partition pnew = null;
   int cont = 0;

   for (int i = p.getStartIndex(); i < p.getEndIndex(); ) {
      JflowSource src = source_set[i];
      int ncont = (cs.contains(src) ? 1 : -1);
      if (cont == 0) {
	 cont = ncont;
	 ++i;
       }
      else if (cont != ncont) {
	 int j = p.getEndIndex()-1;
	 swap(i,j);
	 if (pnew == null) {
	    pnew = new Partition(j,j+1);
	    p.removeRight();
	  }
	 else {
	    pnew.addLeft();
	    p.removeRight();
	  }
       }
      else ++i;
    }

   return pnew;
}



private void swap(int i,int j)
{
   JflowSource s = source_set[i];
   source_set[i] = source_set[j];
   source_set[j] = s;
}




/********************************************************************************/
/*										*/
/*	Methods to build sample sets based on partitions			*/
/*										*/
/********************************************************************************/

private void setupPartitionMap()
{
   partition_map = new HashMap<Collection<JflowSource>,ModelSourceSet>();

   for (ModelSourceSet cs : synch_sets) {
      ModelSourceSet rslt = null;
      for (Partition p : partition_list) {
	 JflowSource src = source_set[p.getStartIndex()];
	 if (cs.contains(src)) {
	    ModelSourceSet sset = new ModelSourceSet(src);
	    if (rslt == null) rslt = sset;
	    else rslt = rslt.combine(sset);
	  }
       }
      partition_map.put(cs,rslt);
    }
}




/********************************************************************************/
/*										*/
/*	Methods to get update the programs using simplified sets		*/
/*										*/
/********************************************************************************/

private void replaceSourceSets()
{
   for (ModelMethod mm : model_methods) {
      fixStateSet(mm);
    }
}




private void fixStateSet(JflowModel.Method mm)
{
   Set<ModelState> s = new HashSet<ModelState>();
   fixStateNodes((ModelState) mm.getStartNode(),s);
}



private void fixStateNodes(ModelState n,Set<ModelState> s)
{
   if (s.contains(n)) return;

   Collection<JflowSource> cs = n.getWaitSet();
   if (cs != null) {
      ModelSourceSet ns = partition_map.get(cs);
      if (ns != cs && ns != null) n.setWaitSet(ns);
    }

   s.add(n);
   for (JflowModel.Node jmn : n.getTransitions()) {
      ModelState nn = (ModelState) jmn;
      fixStateNodes(nn,s);
    }
}




/********************************************************************************/
/*										*/
/*	Class representing a source set partition				*/
/*										*/
/********************************************************************************/

private static class Partition {

   private int start_index;
   private int end_index;

   Partition(int st,int en) {
      start_index = st;
      end_index = en;
    }

   int getStartIndex()				{ return start_index; }
   int getEndIndex()				{ return end_index; }

   void removeRight()				{ end_index -= 1; }
   void addLeft()				{ start_index -= 1; }

}	// end of subclass Partition




}	// end of class ModelSynch




/* end of ModelSynch.java */
