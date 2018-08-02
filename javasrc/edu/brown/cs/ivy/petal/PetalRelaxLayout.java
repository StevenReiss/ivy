/********************************************************************************/
/*										*/
/*		PetalRelaxLayout.java						*/
/*										*/
/*	Relaxation-based layout method for Petal Graphs 			*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Redistribution and use in source and binary forms, with or without           *
 *  modification, are permitted provided that the following conditions are met:  *
 *                                                                               *
 *  + Redistributions of source code must retain the above copyright notice,     *
 *      this list of conditions and the following disclaimer.                    *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *      this list of conditions and the following disclaimer in the              *
 *      documentation and/or other materials provided with the distribution.     *
 *  + Neither the name of the Brown University nor the names of its              *
 *      contributors may be used to endorse or promote products derived from     *
 *      this software without specific prior written permission.                 *
 *                                                                               *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   *
 *  POSSIBILITY OF SUCH DAMAGE.                                                  *
 *                                                                               *
 ********************************************************************************/

/*
 * Copyright (c) 1994-1996 Sun Microsystems, Inc. All Rights Reserved.
 * @modified 96/04/24 Jim Hagen : changed stressColor
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
 * without fee is hereby granted.
 * Please refer to the file http://java.sun.com/copy_trademarks.html
 * for further important copyright and trademark information and to
 * http://java.sun.com/licensing.html for further important licensing
 * information for the Java (tm) Technology.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
 * CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
 * PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
 * NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
 * SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
 * SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
 * PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES").  SUN
 * SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
 * HIGH RISK ACTIVITIES.
 */


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalRelaxLayout.java,v 1.13 2017/07/21 14:42:11 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalRelaxLayout.java,v $
 * Revision 1.13  2017/07/21 14:42:11  spr
 * Fix relaxation layout so it works better.
 *
 * Revision 1.12  2015/11/20 15:09:23  spr
 * Reformatting.
 *
 * Revision 1.11  2011-05-27 19:32:49  spr
 * Change copyrights.
 *
 * Revision 1.10  2010-12-08 22:50:39  spr
 * Fix up dipslay and add new layouts
 *
 * Revision 1.9  2010-07-28 01:21:20  spr
 * Bug fixes in petal.
 *
 * Revision 1.8  2007-05-04 02:00:35  spr
 * Import fixups.
 *
 * Revision 1.7  2006/07/10 14:52:24  spr
 * Code cleanup.
 *
 * Revision 1.6  2005/07/08 20:57:47  spr
 * Change imports.
 *
 * Revision 1.5  2005/05/07 22:25:43  spr
 * Updates for java 5.0
 *
 * Revision 1.4  2005/04/28 21:48:40  spr
 * Fix up petal to support pebble.
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



import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;




public class PetalRelaxLayout implements PetalLayoutMethod {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private PetalEditor	for_editor;

private Node [] 	node_set;
private Edge [] 	edge_set;
private int		num_nodes;
private int		num_edges;

private boolean 	is_random;
private int		relax_count;
private double		arc_length;
private double		push_distance;
private int		border_size;

private Dimension	graph_size;
private Map<PetalNode,Integer> node_map;

private final static double THRESHOLD = 0.1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalRelaxLayout(PetalEditor pe)
{
   this(pe,false,1000);
}



public PetalRelaxLayout(PetalEditor pe,boolean rnd)
{
   this(pe,rnd,1000);
}


public PetalRelaxLayout(PetalEditor pe,boolean rnd,int ctr)
{
   for_editor = pe;

   is_random = rnd;
   arc_length = 100;
   push_distance = 100;
   border_size = 4;
   relax_count = ctr;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public void setRandom(boolean fg)		{ is_random = fg; }
public void setCount(int ctr)			{ relax_count = ctr; }

public void setArcLength(double v)		{ arc_length = v; }
public void setDistance(double v)		{ push_distance = v; }



/********************************************************************************/
/*										*/
/*	Layout methods								*/
/*										*/
/********************************************************************************/

@Override public void doLayout(PetalModel m)
{
   node_map = new HashMap<PetalNode,Integer>();

   PetalNode [] pnds = m.getNodes();
   node_set = new Node[pnds.length];
   int ct = 0;
   double xtot = 0;
   double ytot = 0;
   for (int i = 0; i < pnds.length; ++i) {
      if (pnds[i].getLink() == null) {
	 Component c = pnds[i].getComponent();
	 if (c != null) {
	    Dimension d = c.getSize();
	    xtot += d.width;
	    ytot += d.height;
	  }
	 node_set[ct] = new Node(pnds[i]);
	 node_map.put(pnds[i],Integer.valueOf(ct));
	 ++ct;
       }
    }
   num_nodes = ct;

   PetalArc [] parc = m.getArcs();
   edge_set = new Edge[parc.length];
   ct = 0;
   for (int i = 0; i < parc.length; ++i) {
      if (parc[i].getSource() != parc[i].getTarget())
	 edge_set[ct++] = new Edge(parc[i]);
    }
   num_edges = ct;

   graph_size = for_editor.getSize().getSize();
   double asz = Math.sqrt(num_nodes) + 1;
   double xsz = xtot / num_nodes * asz * 2.0;
   double ysz = ytot / num_nodes * asz * 2.0;
   if (graph_size.width < xsz) graph_size.width = (int) xsz;
   if (graph_size.height < ysz) graph_size.height = (int) ysz;

   if (is_random) {
      for (int i = 0; i < num_nodes; ++i) {
	 Dimension csz = node_set[i].getSize();
	 node_set[i].x = (graph_size.width-csz.width) * Math.random();
	 node_set[i].y = (graph_size.height-csz.height) * Math.random();
       }
    }

   ct = 0;
   while (relax()) {
      if (ct++ > relax_count) break;
   }

   reposition();

   for (int i = 0; i < num_nodes; ++i) node_set[i].setPosition();
   for (int i = 0; i < num_edges; ++i) edge_set[i].setPosition();
}





/********************************************************************************/
/*										*/
/*	Relaxation methods							*/
/*										*/
/********************************************************************************/

private boolean relax()
{
   boolean chng = false;

   for (int i = 0 ; i < num_edges ; i++) {
      Edge e = edge_set[i];
      double p0 = e.priority;
      double vx = node_set[e.to].x - node_set[e.from].x;
      double vy = node_set[e.to].y - node_set[e.from].y;
      double len = Math.sqrt(vx * vx + vy * vy);

      if (len < e.len) continue;	// ignore if too close

      double f = (e.len - len) / (len * 3) / num_edges;
      double dx = f * vx * p0;
      double dy = f * vy * p0;

      node_set[e.to].dx += dx;
      node_set[e.to].dy += dy;
      node_set[e.from].dx += -dx;
      node_set[e.from].dy += -dy;
    }

   for (int i = 0 ; i < num_nodes ; i++) {
      Node n1 = node_set[i];
      double dx = 0;
      double dy = 0;

      for (int j = 0 ; j < num_nodes ; j++) {
	 if (i == j) continue;
	 Node n2 = node_set[j];
	 double vx = n1.x - n2.x;
	 double vy = n1.y - n2.y;
	 double len = vx * vx + vy * vy;
	 if (len >= 4*push_distance*push_distance) continue;
	 double d;
	 double l1 = Math.sqrt(len);
	 if (len <= 1) d = 50;
	 else if (len >= push_distance) d = 1;
	 else {
	    double x = l1/push_distance;
	    d = 49 * x * x - 98 *x + 50;
	  }
	 dx += vx / l1 * d;
	 dy += vy / l1 * d;
       }

      if (dx != 0 || dy != 0) {
	 n1.dx += dx;
	 n1.dy += dy;
       }
    }

   double minx = 0;
   double miny = 0;
   double szx = Math.max(graph_size.width / 100.0,5);
   double szy = Math.max(graph_size.height / 100.0,5);

   for (int i = 0 ; i < num_nodes ; i++) {
      Node n = node_set[i];
      // Dimension csz = n.getSize();
      if (!n.fixed) {
	 n.x += Math.max(-szx, Math.min(szx, n.dx));
	 n.y += Math.max(-szy, Math.min(szy, n.dy));
	 if (n.dx >= THRESHOLD || n.dy >= THRESHOLD) chng = true;
	 // System.out.println("v= " + n.dx + "," + n.dy);
	 if (i == 0 || n.x < minx) minx = n.x;
	 if (i == 0 || n.y < miny) miny = n.y;
       }
      n.dx /= 2;
      n.dy /= 2;
    }

   if (minx != 0 || miny != 0) {
      for (int i = 0; i < num_nodes; ++i) {
	 Node n = node_set[i];
	 if (!n.fixed) {
	    n.x -= minx;
	    n.y -= miny;
	 }
      }
   }


   return chng;
}




/********************************************************************************/
/*										*/
/*	Positioning methods							*/
/*										*/
/********************************************************************************/

private void reposition()
{
   double minx = graph_size.width;
   double miny = graph_size.height;

   for (int i = 0; i < num_nodes; ++i) {
      Node n = node_set[i];
      if (n.x < minx) minx = n.x;
      if (n.y < miny) miny = n.y;
    }

   for (int i = 0; i < num_nodes; ++i) {
      node_set[i].x -= minx + border_size;
      node_set[i].y -= miny + border_size;
    }
}




/********************************************************************************/
/*										*/
/*	Subclasses for nodes and edges						*/
/*										*/
/********************************************************************************/

private class Node {

    double x;
    double y;
    double dx;
    double dy;
    boolean fixed;
    private PetalNode for_node;

    Node(PetalNode pn) {
       for_node = pn;
       Component cmp = pn.getComponent();
       Point p = cmp.getLocation();
       x = p.x;
       y = p.y;
       dx = 0;
       dy = 0;
       fixed = false;
     }

    Dimension getSize() 		{ return for_node.getComponent().getSize(); }

    void setPosition() {
       int xpos = (int) x;
       int ypos = (int) y;
       for_editor.commandMoveNode(for_node,new Point(xpos,ypos));
     }

}	// end of subclass Node



private class Edge {

   int from;
   int to;
   double len;
   private PetalArc for_arc;
   double priority;

   Edge(PetalArc pa) {
      for_arc = pa;
      Integer vl = node_map.get(pa.getSource());
      from = vl.intValue();
      vl = node_map.get(pa.getTarget());
      to = vl.intValue();
      len = arc_length;
      priority = pa.getLayoutPriority();
    }

   void setPosition() {
      for_editor.commandReplacePivots(for_arc,null);
    }

}





}	// end of class PetalRelaxLayout




/* end of PetalRelaxLayout.java */
