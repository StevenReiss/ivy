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

private static final double THRESHOLD = 0.1;




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
	 node_set[i].node_x = (graph_size.width-csz.width) * Math.random();
	 node_set[i].node_y = (graph_size.height-csz.height) * Math.random();
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

   for (int i = 0; i < num_edges; i++) {
      Edge e = edge_set[i];
      double p0 = e.edge_priority;
      double vx = node_set[e.to_node].node_x - node_set[e.from_node].node_x;
      double vy = node_set[e.to_node].node_y - node_set[e.from_node].node_y;
      double len = Math.sqrt(vx * vx + vy * vy);

      if (len < e.edge_length) continue;	// ignore if too close

      double f = (e.edge_length - len) / (len * 3) / num_edges;
      double dx = f * vx * p0;
      double dy = f * vy * p0;

      node_set[e.to_node].node_dx += dx;
      node_set[e.to_node].node_dy += dy;
      node_set[e.from_node].node_dx += -dx;
      node_set[e.from_node].node_dy += -dy;
    }

   for (int i = 0; i < num_nodes; i++) {
      Node n1 = node_set[i];
      double dx = 0;
      double dy = 0;

      for (int j = 0; j < num_nodes; j++) {
	 if (i == j) continue;
	 Node n2 = node_set[j];
	 double vx = n1.node_x - n2.node_x;
	 double vy = n1.node_y - n2.node_y;
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
	 n1.node_dx += dx;
	 n1.node_dy += dy;
       }
    }

   double minx = 0;
   double miny = 0;
   double szx = Math.max(graph_size.width / 100.0,5);
   double szy = Math.max(graph_size.height / 100.0,5);

   for (int i = 0; i < num_nodes; i++) {
      Node n = node_set[i];
      // Dimension csz = n.getSize();
      if (!n.is_fixed) {
	 n.node_x += Math.max(-szx, Math.min(szx, n.node_dx));
	 n.node_y += Math.max(-szy, Math.min(szy, n.node_dy));
	 if (n.node_dx >= THRESHOLD || n.node_dy >= THRESHOLD) chng = true;
	 // System.out.println("v= " + n.dx + "," + n.dy);
	 if (i == 0 || n.node_x < minx) minx = n.node_x;
	 if (i == 0 || n.node_y < miny) miny = n.node_y;
       }
      n.node_dx /= 2;
      n.node_dy /= 2;
    }

   if (minx != 0 || miny != 0) {
      for (int i = 0; i < num_nodes; ++i) {
	 Node n = node_set[i];
	 if (!n.is_fixed) {
	    n.node_x -= minx;
	    n.node_y -= miny;
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
      if (n.node_x < minx) minx = n.node_x;
      if (n.node_y < miny) miny = n.node_y;
    }

   for (int i = 0; i < num_nodes; ++i) {
      node_set[i].node_x -= minx + border_size;
      node_set[i].node_y -= miny + border_size;
    }
}




/********************************************************************************/
/*										*/
/*	Subclasses for nodes and edges						*/
/*										*/
/********************************************************************************/

private class Node {

    private double node_x;
    private double node_y;
    private double node_dx;
    private double node_dy;
    private boolean is_fixed;
    private PetalNode for_node;

    Node(PetalNode pn) {
       for_node = pn;
       Component cmp = pn.getComponent();
       Point p = cmp.getLocation();
       node_x = p.x;
       node_y = p.y;
       node_dx = 0;
       node_dy = 0;
       is_fixed = false;
     }

    Dimension getSize() 		{ return for_node.getComponent().getSize(); }

    void setPosition() {
       int xpos = (int) node_x;
       int ypos = (int) node_y;
       for_editor.commandMoveNode(for_node,new Point(xpos,ypos));
     }

}	// end of inner class Node



private class Edge {

   private int from_node;
   private int to_node;
   private double edge_length;
   private PetalArc for_arc;
   private double edge_priority;

   Edge(PetalArc pa) {
      for_arc = pa;
      Integer vl = node_map.get(pa.getSource());
      from_node = vl.intValue();
      vl = node_map.get(pa.getTarget());
      to_node = vl.intValue();
      edge_length = arc_length;
      edge_priority = pa.getLayoutPriority();
    }

   void setPosition() {
      for_editor.commandReplacePivots(for_arc,null);
    }

}       // end of inneer class Edge





}	// end of class PetalRelaxLayout




/* end of PetalRelaxLayout.java */
