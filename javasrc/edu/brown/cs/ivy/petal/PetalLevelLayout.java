/********************************************************************************/
/*										*/
/*		PetalLevelLayout.java						*/
/*										*/
/*	Level-based layout method for Petal graphs				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Redistribution and use in source and binary forms, with or without		 *
 *  modification, are permitted provided that the following conditions are met:  *
 *										 *
 *  + Redistributions of source code must retain the above copyright notice,	 *
 *	this list of conditions and the following disclaimer.			 *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *	this list of conditions and the following disclaimer in the		 *
 *	documentation and/or other materials provided with the distribution.	 *
 *  + Neither the name of the Brown University nor the names of its		 *
 *	contributors may be used to endorse or promote products derived from	 *
 *	this software without specific prior written permission.		 *
 *										 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE	 *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE	 *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE	 *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 	 *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF	 *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS	 *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN	 *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)	 *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE	 *
 *  POSSIBILITY OF SUCH DAMAGE. 						 *
 *										 *
 ********************************************************************************/



/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalLevelLayout.java,v 1.22 2017/11/17 14:24:34 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalLevelLayout.java,v $
 * Revision 1.22  2017/11/17 14:24:34  spr
 * Formatting change.
 *
 * Revision 1.21  2017/08/04 12:42:56  spr
 * Clean up
 *
 * Revision 1.20  2017/07/07 20:56:18  spr
 * Fix problem with possible index out of bounds
 *
 * Revision 1.19  2016/10/28 18:31:56  spr
 * Clean up possible concurrent modification exception.
 *
 * Revision 1.18  2015/11/20 15:09:23  spr
 * Reformatting.
 *
 * Revision 1.17  2011-09-12 20:50:27  spr
 * Code cleanup.
 *
 * Revision 1.16  2011-05-27 19:32:49  spr
 * Change copyrights.
 *
 * Revision 1.15  2011-05-03 01:16:38  spr
 * Add options to set white space.
 *
 * Revision 1.14  2010-12-08 22:50:39  spr
 * Fix up dipslay and add new layouts
 *
 * Revision 1.13  2010-11-18 23:09:02  spr
 * Updates to petal to work with bubbles.
 *
 * Revision 1.12  2009-09-17 02:00:14  spr
 * Eclipse cleanup.
 *
 * Revision 1.11  2006-12-01 03:22:54  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.10  2006/07/10 14:52:24  spr
 * Code cleanup.
 *
 * Revision 1.9  2005/06/07 02:18:22  spr
 * Update for java 5.0
 *
 * Revision 1.8  2005/05/07 22:25:43  spr
 * Updates for java 5.0
 *
 * Revision 1.7  2005/02/14 21:07:30  spr
 * Use simpler algorithms for large graphs.
 *
 * Revision 1.6  2004/05/28 20:57:31  spr
 * Add printing and minor fixes to level layout.
 *
 * Revision 1.5  2004/05/26 13:43:26  spr
 * Fix problems with level ranking heuristics.
 *
 * Revision 1.4  2004/05/22 02:37:59  spr
 * Fix bugs in the editor where update is called before things are initialized;
 * minor fixups to level layout (more needed).
 *
 * Revision 1.3  2004/05/20 16:03:37  spr
 * Bug fixes for Petal related to CHIA; add oval helper.
 *
 * Revision 1.2  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1  2003/07/16 19:44:59  spr
 * Move petal from bloom to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.petal;



import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;




public class PetalLevelLayout implements PetalLayoutMethod {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private PetalEditor	for_editor;
private double		white_fraction;
private double		white_space;
private boolean 	center_nodes;
private boolean 	use_border;

private boolean 	multi_pivot;
private boolean 	two_way;
private boolean 	bottom_up;
private boolean 	breadth_first;
private boolean 	dummy_nodes;
private boolean 	opt_levels;
private boolean 	even_out;
private boolean 	level_x;
private boolean 	spline_arcs;
private boolean 	use_crossings;



private int		num_pass;

private Graph		graph_id;

private int		first_level;
private int		last_level;
private int		max_level;
private int		cur_pass;
private int []		max_rank;
private Item [][]	rank_data;

private static Random	random_gen = new Random();


//private static final double	  DEFAULT_WHITE_FRACTION = 2.00;
private static final double	DEFAULT_WHITE_FRACTION = 0.75;
private static final double	DEFAULT_WHITE_SPACE = 0.0;
private static final int	MIN_WHITE_SPACE = 1;
private static final int	DEFAULT_NUM_PASS = 5;
private static final int	DEFAULT_BORDER_SIZE = 10;

enum NodeState {
   PRESET,
   OPEN,
   WORKING,
   SET,
   DONE
}

enum PivotType {
   MIDDLE,
   FRONT,
   BACK,
   FORWARD,
   REVERSE,
   SELF
}

private static final double	INFINITY = 10000000.0;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalLevelLayout(PetalEditor pe)
{
   for_editor = pe;

   white_fraction = DEFAULT_WHITE_FRACTION;
   white_space = DEFAULT_WHITE_SPACE;
   center_nodes = true;
   use_border = true;

   two_way = false;
   bottom_up = false;
   breadth_first = false;
   dummy_nodes = true;
   opt_levels = false;
   even_out = true;
   level_x = true;
   multi_pivot = false;
   spline_arcs = true;
   use_crossings = true;

   num_pass = DEFAULT_NUM_PASS;

   graph_id = null;
   first_level = -1;
   last_level = -1;
   cur_pass = -1;
   max_level = -1;
   max_rank = null;
   rank_data = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public void setTwoWay(boolean fg)			{ two_way = fg; }
public void setBottomUp(boolean fg)			{ bottom_up = fg; }
public void setBreadthFirst(boolean fg) 		{ breadth_first = fg; }
public void setOptimizeLevels(boolean fg)		{ opt_levels = fg; }
public void setLevelX(boolean fg)			{ level_x = fg; }
public void setMultiPivot(boolean fg)			{ multi_pivot = fg; }
public void setSplineArcs(boolean fg)			{ spline_arcs = fg; }
public void setWhiteFraction(double v)			{ white_fraction = v; }
public void setWhiteSpace(double v)			{ white_space = v; }




/********************************************************************************/
/*										*/
/*	Layout methods								*/
/*										*/
/********************************************************************************/

@Override public synchronized void doLayout(PetalModel m)
{
   graph_id = new Graph(m);

   if (graph_id.getNumNodes() > 500) {		// handle large graphs efficiently
      use_crossings = false;
    }

   setLevels();
   if (opt_levels) optimize();
   if (dummy_nodes) insertNodes();
   setupRanks();
   computeRanks();
   if (dummy_nodes) removeNodes();

   convertToAbsPositions();

   graph_id = null;
   rank_data = null;
   max_rank = null;
}




/********************************************************************************/
/*										*/
/*	Leveling methods							*/
/*										*/
/********************************************************************************/

private void setLevels()
{
   for (Node n : graph_id.getNodes()) {
      if (n.getState() == NodeState.OPEN) setLevel(n,1);
    }

   if (bottom_up) {
      int mx = 0;
      for (Node n : graph_id.getNodes()) {
	 int j = n.getLevelPos();
	 if (j > mx) mx = j;
       }
      for (Node n : graph_id.getNodes()) {
	 if (n.getState() == NodeState.DONE) {
	    int p = mx + 1 - n.getLevelPos();
	    n.setLevelPos(p);
	  }
       }
    }
}



private int setLevel(Node n,int lvl)
{
   if (n.getState() == NodeState.PRESET || n.getState() == NodeState.WORKING) return -2;

   int olvl = n.getLevelPos();
   if (n.getState() == NodeState.DONE) return olvl;
   if (n.getState() == NodeState.SET && olvl > lvl) return olvl;

   if (bottom_up) {
      n.setState(NodeState.WORKING);
      lvl = 0;
    }
   else {
      if (breadth_first) n.setState(NodeState.DONE);
      else n.setState(NodeState.WORKING);
      n.setLevelPos(lvl);
    }

   for (Arc a : n.getArcs()) {
      if (a.getLayoutPriority() == 0) continue;
      Node n1 = a.getToNode();
      if (two_way && n1 == n) n1 = a.getFromNode();
      if (n1 != null) {
	 if (bottom_up) {
	    int j = setLevel(n1,1);
	    if (j > lvl) lvl = j;
	  }
	 else setLevel(n1,lvl+1);
       }
    }

   if (bottom_up) {
      n.setState(NodeState.DONE);
      n.setLevelPos(lvl+1);
    }
   else if (!breadth_first) {
      if (n.getState() == NodeState.WORKING) n.setState(NodeState.SET);
    }

   return n.getLevelPos();
}




/********************************************************************************/
/*										*/
/*	Level optimization methods						*/
/*										*/
/********************************************************************************/

private void optimize()
{
   int nnod = graph_id.getNumNodes();

   if (nnod < 4) return;

   Node [] nodes = new Node[nnod];
   int [] ctrs = new int[nnod];
   int mxpass = 2*nnod;
   if (mxpass > 40) mxpass = 40;
   boolean chngfg = true;

   for (int pass = 0; chngfg && pass < mxpass; ++pass) {
      chngfg = false;
      for (int i = 0; i < nnod; ++i) nodes[i] = null;
      for (int i = 0; i < nnod; ++i) ctrs[i] = 0;
      for (Node sn : graph_id.getNodes()) {
	 int j = random_gen.nextInt(nnod);
	 while (nodes[j] != null) j = (j+1) % nnod;
	 nodes[j] = sn;
	 int p = sn.getLevelPos();
	 if (p >= 0 && p < nnod) ctrs[p]++;
       }
      for (int i = 0; i < nnod; ++i) {
	 Node sn = nodes[i];
	 if (sn != null && sn.getState() != NodeState.PRESET && sn.getLevelPos() > 1) {
	    if (optLevel(nnod,sn,ctrs)) chngfg = true;
	  }
       }
    }
}



private boolean optLevel(int nnod,Node sn,int [] ctrs)
{
   int olvl = sn.getLevelPos();
   boolean chng = false;
   int blvl = -1;
   int bcost = 1000000;

   for (int i = 0; i < 3; ++i) {
      int lvl = olvl + i - 1;
      if (lvl < 1 || lvl >= ctrs.length) continue;

      int t = 0;
      for (Arc sa : sn.getAllArcs()) {
	 Node na = sa.getFromNode();
	 if (na == sn) na = sa.getToNode();
	 if (na == sn) continue;
	 int j = na.getLevelPos();
	 if (j < 0) continue;
	 if (j == lvl) t += 4;
	 else t += (lvl-j)*(lvl-j);
       }

      int mtch = -1;
      if (blvl < 0 || blvl >= ctrs.length) mtch = 1;
      else if (t > bcost) mtch = 0;
      else if (t < bcost) mtch = 1;
      else {
	 if (even_out) {
	    if (lvl >= nnod) mtch = 0;
	    if (blvl < nnod) {
	       if (ctrs[lvl] > ctrs[blvl]) mtch = 0;
	       else if (ctrs[lvl] < ctrs[blvl]) mtch = 1;
	     }
	  }
	 if (mtch < 0) {
	    if (lvl != olvl) mtch = 0;
	    else mtch = 1;
	  }
       }

      if (mtch != 0) {
	 blvl = lvl;
	 bcost = t;
       }
    }

   if (blvl != olvl) {
      sn.setLevelPos(blvl);
      chng = true;
    }

   return chng;
}



/********************************************************************************/
/*										*/
/*	Method to insert dummy nodes						*/
/*										*/
/********************************************************************************/

private void insertNodes()
{
   for (Arc ha : graph_id.getArcs()) {
      while (ha != null) {
	 Node fn = ha.getFromNode();
	 Node tn = ha.getToNode();
	 int fl = fn.getLevelPos();
	 int tl = tn.getLevelPos();
	 if (Math.abs(fl-tl) != 1) {
	    int xl;
	    if (fl == tl) xl = fl+1;
	    else if (fl < tl) xl = fl+1;
	    else xl = fl-1;
	    Node xn = graph_id.splitArc(ha);
	    xn.setLevelPos(xl);
	    Iterator<Arc> it1 = xn.getFromArcs().iterator();
	    if (it1.hasNext()) ha = it1.next();
	    else ha = null;
	  }
	 else ha = null;
       }
    }
}




/********************************************************************************/
/*										*/
/*	Methods to remove dummy nodes						*/
/*										*/
/********************************************************************************/

private void removeNodes()
{
   Vector<Arc> dels = new Vector<Arc>();

   for (Arc ha : graph_id.getArcs()) {
      Node fn = ha.getFromNode();
      Node tn = ha.getToNode();
      if (fn.isDummy()) dels.add(ha);
      else if (tn.isDummy()) {
	 Node sn = fn;
	 while (tn.isDummy()) {
	    Node nn = tn.getFromArcs().iterator().next().getToNode();
	    Pivot pv = new Pivot(tn.getPos());
	    for (int i = 0; i < 2; ++i) {
	       int fl = sn.getPos(i);
	       int tl = tn.getPos(i);
	       int nl = nn.getPos(i);
	       if (i == 0 && !level_x) pv.setRelCoordType(i,PivotType.MIDDLE);
	       else if (i == 1 && level_x) pv.setRelCoordType(i,PivotType.MIDDLE);
	       else if (!multi_pivot) pv.setRelCoordType(i,PivotType.MIDDLE);
	       else if (fl < tl) {
		  if (tl < nl) pv.setRelCoordType(i,PivotType.FORWARD);
		  else if (tl == nl) pv.setRelCoordType(i,PivotType.MIDDLE);
		  else pv.setRelCoordType(i,PivotType.FRONT);
		}
	       else if (fl > tl) {
		  if (tl > nl) pv.setRelCoordType(i,PivotType.REVERSE);
		  else if (tl == nl) pv.setRelCoordType(i,PivotType.MIDDLE);
		  else pv.setRelCoordType(i,PivotType.BACK);
		}
	       else pv.setRelCoordType(i,PivotType.MIDDLE);
	     }
	    if (sn == nn) pv.setRelCoordType(1,PivotType.SELF);  // handle self arcs
	    ha.addPivot(pv);
	    sn = tn;
	    tn = nn;
	  }
	 ha.setTo(tn);
       }
    }

   for (Arc ha : dels) {
      graph_id.removeArc(ha);
    }
}





/********************************************************************************/
/*										*/
/*	Methods to initialize for ranking					*/
/*										*/
/********************************************************************************/

private void setupRanks()
{
   cur_pass = 0;

   int mxl = 0;
   for (Node hn : graph_id.getNodes()) {
      int j = hn.getLevelPos();
      if (j > mxl) mxl = j;
    }

   max_level = mxl;
   max_rank = new int [mxl+1];
   rank_data = new Item [mxl+1][];
   first_level = -1;
   last_level = -1;

   int [] lvlcts = new int[mxl+1];
   for (int i = 0; i <= mxl; ++i) {
      lvlcts[i] = 0;
      max_rank[i] = 0;
    }

   for (Node hn : graph_id.getNodes()) {
      int i = hn.getLevelPos();
      lvlcts[i] += 1;
      hn.setData(-1);
      if (first_level < 0 || i < first_level) first_level = i;
      if (last_level < 0 || i > last_level) last_level = i;
    }

   for (int i = 0; i <= mxl; ++i) {
      max_rank[i] = Math.max(max_rank[i],lvlcts[i]);
      rank_data[i] = new Item [max_rank[i]+1];
      lvlcts[i] = 0;
    }

   for (Node hn : graph_id.getNodes()) {
      int i = hn.getLevelPos();
      rank_data[i][lvlcts[i]] = new Item(hn);
      ++lvlcts[i];
    }
}



private class Item {

   Node item_node;
   double item_value;
   int item_rank;

   Item(Node n) {
      item_node = n;
      item_value = 0;
      item_rank = 0;
    }

}	// end of subclass Item




/********************************************************************************/
/*										*/
/*	Methods to compute ranking						*/
/*										*/
/********************************************************************************/

private void computeRanks()
{
   if (rank_data == null) return;

   cur_pass = 0;
   while (cur_pass++ < num_pass && nextPass()) ;

   for (int i = 0; i <= max_level; ++i) {
      for (int j = 0; j <= max_rank[i]; ++j) {
	 if (rank_data[i][j] != null) {
	    Node n = rank_data[i][j].item_node;
	    if (n != null) {
	       int k = n.getData();
	       if (k < 0) k = j+1;
	       n.setRankPos(k);
	     }
	  }
       }
    }
}



private boolean nextPass()
{
   int slvl,elvl,dlvl;
   ItemCompare ic = new ItemCompare();

   if (cur_pass == 1) {
      slvl = first_level;
      elvl = last_level+1;
      dlvl = 1;
    }
   else if ((cur_pass&1) == 0) {
//    slvl = first_level + 1;
      slvl = first_level;
      elvl = last_level + 1;
      dlvl = 1;
    }
   else {
      slvl = last_level - 1;
      elvl = first_level - 1;
      dlvl = -1;
    }

   if (first_level == last_level) return false;

   boolean fg = false;
   if (cur_pass <= 4) fg = true;

   for (int lvl = slvl; lvl != elvl; lvl += dlvl) {
      for (int i = 0; i < max_rank[lvl]; ++i) {
	 Node n = rank_data[lvl][i].item_node;
	 rank_data[lvl][i].item_value = getValue(n,lvl,dlvl);
       }
      if (max_rank[lvl] > 1) {
	 int nitm = 0;
	 for (int i = 0; i <= max_rank[lvl]; ++i) if (rank_data[lvl][i] != null) ++nitm;
	 Item [] ritms = new Item[nitm];
	 for (int i = 0; i < nitm; ++i) ritms[i] = rank_data[lvl][i];
	 Arrays.sort(ritms,ic);
	 for (int i = 0; i < nitm; ++i) rank_data[lvl][i] = ritms[i];
       }
      if (max_rank[lvl] < 100) adjustRanks(lvl);
      assignRanks(max_rank[lvl],rank_data[lvl]);

      for (int i = 0; i < max_rank[lvl]; ++i) {
	 Node n = rank_data[lvl][i].item_node;
	 if (n.getData() != rank_data[lvl][i].item_rank) {
	    fg = true;
	    n.setData(rank_data[lvl][i].item_rank);
	  }
       }
    }

   return fg;
}



private void adjustRanks(int lvl)
{
   double cost = computeCost(lvl,0,0);
   if (cost < 1) return;

   for (int i = 1; i < max_rank[lvl]; ++i) {
      double xc = computeCost(lvl,i-1,i);
      if (xc < cost) {
	 Item xitm = rank_data[lvl][i-1];
	 rank_data[lvl][i-1] = rank_data[lvl][i];
	 rank_data[lvl][i] = xitm;
	 cost = xc;
	 if (i > 1) i -= 2;
       }
    }
}



private double computeCost(int lvl,int itm0,int itm1)
{
   double c = 0;

   if (itm0 != itm1) {
      Item xitm = rank_data[lvl][itm0];
      rank_data[lvl][itm0] = rank_data[lvl][itm1];
      rank_data[lvl][itm1] = xitm;
    }

   assignRanks(max_rank[lvl],rank_data[lvl]);

   if (use_crossings && max_rank[lvl] < 32) c = crossingCost(lvl);
   else {
      for (int i = 0; i < max_rank[lvl]; ++i) {
	 Node n = rank_data[lvl][i].item_node;
	 c += nodeCost(n,lvl,rank_data[lvl][i].item_rank);
       }
    }

   if (itm0 != itm1) {
      Item xitm = rank_data[lvl][itm0];
      rank_data[lvl][itm0] = rank_data[lvl][itm1];
      rank_data[lvl][itm1] = xitm;
    }

   return c;
}



private double crossingCost(int lvl)
{
   double c = 0;

   for (int i = 0; i < max_rank[lvl]; ++i) {
      Node n1 = rank_data[lvl][i].item_node;
      int r1 = rank_data[lvl][i].item_rank;
      for (Arc a1 : n1.getAllArcs()) {
	 Node ht1 = a1.getFromNode();
	 if (ht1 == n1) ht1 = a1.getToNode();
	 int nrnk1 = ht1.getData();
	 if (nrnk1 < 0) continue;
	 if (nrnk1 != r1) c += 0.1;
	 int nlvl1 = ht1.getLevelPos();
	 for (int j = i+1; j < max_rank[lvl]; ++j) {
	    Node n2 = rank_data[lvl][j].item_node;
	    for (Arc a2 : n2.getAllArcs()) {
	       Node ht2 = a2.getFromNode();
	       int nrnk2 = ht2.getData();
	       if (nrnk2 < 0) continue;
	       if (ht2 == n1) ht2 = a2.getToNode();
	       if (nlvl1 != ht2.getLevelPos()) continue;
	       if (nrnk2 < nrnk1)
		  ++c;
	     }
	  }
       }
    }

   return c;
}




private double nodeCost(Node hn,int lvl,int rnk)
{
   double c = 0;

   if (rnk < 0) return 0;

   for (Arc ha : hn.getAllArcs()) {
      Node ht = null;
      if (ha.getFromNode() == hn) ht = ha.getToNode();
      else ht = ha.getFromNode();
      if (ht.getData() < 0) continue;
      double x = rnk - ht.getData();
      x *= x;
      c += x;
    }

   return c;
}



private double getValue(Node hn,int lvl,int dlvl)
{
   double ct = 0;
   double  pos = 0;
   int bothpass = 3;

   for (Arc ha : hn.getAllArcs()) {
      Node ht = null;
      int j = ha.getFromNode().getLevelPos();
      if (lvl - j == dlvl || (cur_pass > bothpass && j-lvl == dlvl)) ht = ha.getFromNode();
      j = ha.getToNode().getLevelPos();
      if (lvl - j == dlvl || (cur_pass > bothpass && j-lvl == dlvl)) ht = ha.getToNode();
      if (ht != null) {
	 double npos = ht.getData();
	 if (npos >= 0) {
	    pos += npos;
	    ct += 1;
	  }
       }
    }

   if (ct == 0) return INFINITY;

   return pos/ct;
}




private void assignRanks(int ct,Item [] itms)
{
   int last = 0;

   for (int i = 0; i < ct; ++i) {
      int j;
      if (itms[i].item_value >= INFINITY) j = last+1;
      else j = (int) itms[i].item_value;
      if (j > last || i == 0) {
	 itms[i].item_rank = j;
	 last = j;
       }
      else {
	 itms[i].item_rank = last+1;
	 last = last+1;
       }
    }
}






private static class ItemCompare implements Comparator<Item>
{
   @Override public int compare(Item i1,Item i2) {
      if (i1.item_value < i2.item_value) return -1;
      else if (i1.item_value == i2.item_value) return 0;
      else return 1;
    }

}	// end of subclass ItemCompare





/********************************************************************************/
/*										*/
/*	Positioning methods							*/
/*										*/
/********************************************************************************/

private void convertToAbsPositions()
{
   Point mincol = new Point(-1,-1);
   Point maxcol = new Point(-1,-1);

   for (Node gn : graph_id.getNodes()) {
      updateMinMax(gn.getPos(),mincol,maxcol);
    }

   for (Arc ga : graph_id.getArcs()) {
      for (Pivot gp : ga.getPivots()) {
	 updateMinMax(gp.getPos(),mincol,maxcol);
       }
    }

   Point numcol = new Point();
   numcol.x = maxcol.x - mincol.x + 1;
   numcol.y = maxcol.y - mincol.y + 1;
   int mxcol = Math.max(numcol.x,numcol.y);

   double [] xsiz = new double[mxcol+4];
   double [] ysiz = new double[mxcol+4];
   double [] xpos = new double[mxcol+4];
   double [] ypos = new double[mxcol+4];
   for (int i = 0; i < mxcol+4; ++i) xsiz[i] = ysiz[i] = xpos[i] = ypos[i] = 0;

   for (Node gn : graph_id.getNodes()) {
      Dimension sz = gn.getSize();
      int xr = gn.getPos(0) - mincol.x;
      int yr = gn.getPos(1) - mincol.y;
      xsiz[xr] = Math.max(xsiz[xr],sz.width);
      ysiz[yr] = Math.max(ysiz[yr],sz.height);
    }

   Point mxsize = new Point(0,0);
   for (int i = 0; i < numcol.x; ++i) {
      mxsize.x = (int) Math.max(mxsize.x,xsiz[i]);
    }
   for (int i = 0; i < numcol.y; ++i) {
      mxsize.y = (int) Math.max(mxsize.y,ysiz[i]);
    }

   Point wsiz = computeWhiteSpace(mxsize);

   double totx = 0;
   if (use_border) {
      totx = DEFAULT_BORDER_SIZE;
      if (totx == 0) totx = wsiz.x/2;
    }
   for (int i = 0; i < numcol.x; ++i) {
      xpos[i] = totx;
      totx += xsiz[i] + wsiz.x;
    }
   xpos[numcol.x] = totx;

   double toty = 0;
   if (use_border) {
      toty = DEFAULT_BORDER_SIZE;
      if (toty == 0) toty = wsiz.y/2;
    }
   for (int i = 0; i < numcol.y; ++i) {
      ypos[i] = toty;
      toty += ysiz[i] + wsiz.y;
    }
   ypos[numcol.y] = toty;

   for (Node gn : graph_id.getNodes()) {
      Dimension sz = gn.getSize();
      int ix = gn.getPos(0) - mincol.x;
      int iy = gn.getPos(1) - mincol.y;
      double vx = xpos[ix];
      double vy = ypos[iy];
      if (center_nodes) {
	 vx += xsiz[ix]/2 - sz.width/2;
	 vy += ysiz[iy]/2 - sz.height/2;
       }
      gn.setAbsPosition((int) vx,(int) vy);
    }

   for (Arc ga : graph_id.getArcs()) {
      Vector<Point> rslt = new Vector<Point>();
      // System.err.println("BEGIN ARC");
      for (Pivot gp : ga.getPivots()) {
	 gp.insertPivots(rslt,xpos,ypos,mincol,wsiz);
       }
      if (rslt.size() == 0 && spline_arcs) {
	 Node n1 = ga.getFromNode();
	 Node n2 = ga.getToNode();
	 if (n1.getPos(0) == n2.getPos(0) || n1.getPos(1) == n2.getPos(1)) {
	    Point px = new Point();
	    int ix1 = n1.getPos(0) - mincol.x;
	    int ix2 = n2.getPos(0) - mincol.x;
	    int iy1 = n1.getPos(1) - mincol.y;
	    int iy2 = n2.getPos(1) - mincol.y;
	    double vx1 = xpos[ix1];
	    double vx2 = xpos[ix2];
	    double vy1 = ypos[iy1];
	    double vy2 = ypos[iy2];
	    double xp,yp;
	    if (vx1 == vx2) {
	       if (iy1 < iy2) xp = vx1 + xsiz[ix1];
	       else xp = vx1;
	     }
	    else if (ix1 < ix2) xp = (vx1 + xsiz[ix1] + vx2)/2.0;
	    else xp = (vx1 + vx2 + xsiz[ix2])/2.0;
	    if (vy1 == vy2) {
	       if (ix1 < ix2) yp = vy1;
	       else yp = vy1 + ysiz[iy1];
	     }
	    else if (iy1 < iy2) yp = (vy1 + ysiz[iy1] + vy2)/2.0;
	    else yp = (vy1 + vy2 + ysiz[iy2])/2.0;
	    px.setLocation(xp,yp);
	    rslt.add(px);
	  }
       }
      ga.setAbsPosition(rslt);
    }
}



private void updateMinMax(Point pos,Point min,Point max)
{
   if (min.x < 0 || min.x > pos.x) min.x = pos.x;
   if (min.y < 0 || min.y > pos.y) min.y = pos.y;
   if (max.x < 0 || max.x < pos.x) max.x = pos.x;
   if (max.y < 0 || max.y < pos.y) max.y = pos.y;
}



private Point computeWhiteSpace(Point mx)
{
   Point wsp = new Point(0,0);

   if (mx.x == 0) wsp.x = MIN_WHITE_SPACE;
   else wsp.x = (int)(mx.x * white_fraction + white_space);
   if (mx.y == 0) wsp.y = MIN_WHITE_SPACE;
   else wsp.y = (int)(mx.y * white_fraction + white_space);

   return wsp;
}




/********************************************************************************/
/*										*/
/*	Subclass for graphs							*/
/*										*/
/********************************************************************************/


private class Graph {

   Vector<Node> graph_nodes;
   Vector<Arc> graph_arcs;
   Map<PetalNode,Node> node_map;

   Graph(PetalModel pm) {
      node_map = new HashMap<PetalNode,Node>();
      graph_nodes = new Vector<Node>();
      graph_arcs = new Vector<Arc>();

      PetalNode [] pnds = pm.getNodes();
      for (int i = 0; i < pnds.length; ++i) {
	 if (pnds[i].getLink() == null) {
	    Node n = new Node(pnds[i]);
	    graph_nodes.add(n);
	    node_map.put(pnds[i],n);
	  }
       }

      PetalArc [] parc = pm.getArcs();
      for (int i = 0; i < parc.length; ++i) {
	 Node n1 = node_map.get(parc[i].getSource());
	 Node n2 = node_map.get(parc[i].getTarget());
	 if (n1 != null && n2 != null) {
	    Arc a = new Arc(parc[i],n1,n2);
	    graph_arcs.add(a);
	  }
       }
    }

   Iterable<Node> getNodes()		{ return graph_nodes; }
   Iterable<Arc> getArcs()		{ return graph_arcs; }

   int getNumNodes()			{ return graph_nodes.size(); }

   Node dummyNode() {
      Node n = new Node(null);
      graph_nodes.add(n);
      return n;
    }

   Node splitArc(Arc a) {
      Node n = dummyNode();
      new Arc(null,n,a.getToNode());
      a.setTo(n);
      return n;
    }

   void removeArc(Arc a) {
      graph_arcs.remove(a);
      a.getFromNode().removeSource(a);
      a.getToNode().removeTarget(a);
    }

}	// end of subclass Graph





/********************************************************************************/
/*										*/
/*	Subclasses for nodes							*/
/*										*/
/********************************************************************************/

private class Node {

   private PetalNode for_node;

   private Vector<Arc> from_arcs;
   private Vector<Arc> to_arcs;
   private Vector<Arc> all_arcs;

   private Dimension node_size;
   private Point rel_pos;

   private int arb_data;
   private NodeState node_state;

   Node(PetalNode pn) {
      for_node = pn;
      from_arcs = new Vector<Arc>();
      to_arcs = new Vector<Arc>();
      all_arcs = new Vector<Arc>();
      if (pn == null) node_size = new Dimension(0,0);
      else node_size = pn.getComponent().getSize();
      arb_data = 0;
      node_state = NodeState.OPEN;
      rel_pos = new Point(-1,-1);
    }

   Iterable<Arc> getFromArcs()		{ return from_arcs; }
   Iterable<Arc> getAllArcs()		{ return all_arcs; }
   Iterable<Arc> getArcs()		{ return (two_way ? getAllArcs() : getFromArcs()); }

   boolean isDummy()			{ return for_node == null; }

   int getData()			{ return arb_data; }
   void setData(int d)			{ arb_data = d; }
   NodeState getState() 		{ return node_state; }
   void setState(NodeState s)		{ node_state = s; }

   Dimension getSize()			{ return node_size; }

   void addSource(Arc a) {
      from_arcs.add(a);
      if (!all_arcs.contains(a)) all_arcs.add(a);
    }

   void addTarget(Arc a) {
      to_arcs.add(a);
      if (!all_arcs.contains(a)) all_arcs.add(a);
    }

   void removeTarget(Arc a) {
      to_arcs.remove(a);
      if (a.getFromNode() != a.getToNode()) all_arcs.remove(a);
    }

   void removeSource(Arc a) {
      from_arcs.remove(a);
      if (a.getFromNode() != a.getToNode()) all_arcs.remove(a);
    }

   int getLevelPos() {
      return (level_x ? rel_pos.x : rel_pos.y);
    }
   void setLevelPos(int v) {
      if (level_x) rel_pos.x = v;
      else rel_pos.y = v;
    }
   void setRankPos(int v) {
      if (level_x) rel_pos.y = v;
      else rel_pos.x = v;
    }
   Point getPos()			{ return rel_pos; }
   int getPos(int idx)			{ return (idx == 0 ? rel_pos.x : rel_pos.y); }

   void setAbsPosition(int x,int y) {
      if (for_node != null) {
	 for_editor.commandMoveNode(for_node,new Point(x,y));
       }
    }

}	// end of subclass Node



/********************************************************************************/
/*										*/
/*	Subclasses for edges							*/
/*										*/
/********************************************************************************/

private class Arc {

   private PetalArc for_arc;
   private Node from_node;
   private Node to_node;
   private Vector<Pivot> pivot_list;

   Arc(PetalArc pa,Node s,Node t) {
      for_arc = pa;
      from_node = s;
      to_node = t;
      pivot_list = new Vector<Pivot>();
      s.addSource(this);
      t.addTarget(this);
    }

   Node getFromNode()				{ return from_node; }
   Node getToNode()				{ return to_node; }
   synchronized Iterable<Pivot> getPivots()	{ return new ArrayList<Pivot>(pivot_list); }

   void setTo(Node n) {
      to_node.removeTarget(this);
      to_node = n;
      n.addTarget(this);
    }

   synchronized void addPivot(Pivot p)		{ pivot_list.add(p); }

   void setAbsPosition(Vector<Point> plst) {
      if (plst == null || plst.size() == 0) for_editor.commandReplacePivots(for_arc,null);
      else {
	 Point [] pvts = new Point[plst.size()];
	 plst.copyInto(pvts);
	 for_editor.commandReplacePivots(for_arc,pvts);
       }
    }

   double getLayoutPriority() {
      if (for_arc == null) return 1.0;
      return for_arc.getLayoutPriority();
    }

}	// end of subclass Arc



private class Pivot {

   private Point pivot_point;
   private PivotType x_type;
   private PivotType y_type;

   Pivot(Point p) {
      pivot_point = p;
      x_type = PivotType.MIDDLE;
      y_type = PivotType.MIDDLE;
    }

   void setRelCoordType(int i,PivotType t) {
      if (i == 0) x_type = t;
      else y_type = t;
    }

   Point getPos()			{ return pivot_point; }

   void insertPivots(Vector<Point> rslt,double [] xpos,double [] ypos,Point  mincol,Point wsiz) {
      int midx = 1;
      if (x_type == PivotType.FORWARD || x_type == PivotType.REVERSE || x_type == PivotType.SELF ||
	     y_type == PivotType.FORWARD || y_type == PivotType.REVERSE || y_type == PivotType.SELF)
	 midx = 3;

      for (int idx = 0; idx < midx; ++idx) {
	 if (idx == 1) continue;
	 int jx = pivot_point.x - mincol.x;
	 int jy = pivot_point.y - mincol.y;
	 double sx = xpos[jx];
	 double sy = ypos[jy];
	 double ex = xpos[jx+1] - wsiz.x;
	 double ey = ypos[jy+1] - wsiz.y;
	 if (sy == ey && (y_type == PivotType.SELF || x_type == PivotType.SELF)) {
	    sy -= wsiz.y/2;
	    ey += wsiz.y/2;
	  }
	 int xr = getPoint(x_type,idx,sx,ex);
	 int yr = getPoint(y_type,idx,sy,ey);
	 if (rslt.size() > 0) {
	    Point lp = rslt.lastElement();
	    if (lp.x == xr && lp.y == yr)
	       continue;
	 }
	 // System.err.println("ADD " + xr + " " + yr);
	 rslt.add(new Point(xr,yr));
       }
    }

   private int getPoint(PivotType typ,int idx,double s,double e) {
      double r = 0;
      switch (typ) {
	 default :
	 case MIDDLE :
	    r = (s+e)/2;
	    break;
	 case FRONT :
	    r = s;
	    break;
	 case BACK :
	    r = e;
	    break;
	 case SELF :
	 case FORWARD :
	    if (idx == 0) r = s;
	    else if (idx == 1) r = (s+e)/2;
	    else if (idx == 2) r = e;
	    break;
	 case REVERSE :
	    if (idx == 0) r = e;
	    else if (idx == 1) r = (s+e)/2;
	    else if (idx == 2) r = s;
	    break;
       }
      return (int) r;
    }

}	// end of subclass Pivot




}	// end of class PetalLevelLayout




/* end of PetalLevelLayout.java */
