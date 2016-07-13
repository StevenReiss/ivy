/********************************************************************************/
/*										*/
/*		PetalCircleLayout.java						*/
/*										*/
/*	Circular layout method for Petal Graphs 				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalCircleLayout.java,v 1.4 2015/11/20 15:09:23 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalCircleLayout.java,v $
 * Revision 1.4  2015/11/20 15:09:23  spr
 * Reformatting.
 *
 * Revision 1.3  2013/11/15 02:39:14  spr
 * Fix imports
 *
 * Revision 1.2  2011-05-27 19:32:48  spr
 * Change copyrights.
 *
 * Revision 1.1  2010-12-08 22:50:39  spr
 * Fix up dipslay and add new layouts
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.petal;



import java.awt.Component;
import java.awt.Point;
import java.util.*;



public class PetalCircleLayout implements PetalLayoutMethod {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private PetalEditor	for_editor;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalCircleLayout(PetalEditor pe)
{
   for_editor = pe;
}



/********************************************************************************/
/*										*/
/*	Layout methods								*/
/*										*/
/********************************************************************************/

@Override public void doLayout(PetalModel m)
{
   Map<PetalNode,Node> nodemap = new HashMap<PetalNode,Node>();
   List<Node> nodes = new ArrayList<Node>();
   Set<Node> done = new HashSet<Node>();
   double max = 0;

   PetalNode [] pnds = m.getNodes();
   for (int i = 0; i < pnds.length; ++i) {
      PetalNode pn = pnds[i];
      Component c = pn.getComponent();
      if (c == null) continue;
      if (pn.getLink() != null) continue;
      Node n = new Node(pn,i);
      nodemap.put(pn,n);
      if (c.getWidth() > max) max = c.getWidth();
      if (c.getHeight() > max) max = c.getHeight();
    }

   PetalArc [] parcs = m.getArcs();
   for (PetalArc pa : parcs) {
      Node n1 = nodemap.get(pa.getSource());
      Node n2 = nodemap.get(pa.getTarget());
      if (n1 == null || n2 == null) continue;
      n1.addOutEdge();
      n2.addInEdge();
      for_editor.commandReplacePivots(pa,null);
    }

   if (pnds.length > 100) {
      for (PetalNode pn : pnds) {
	 Node n = nodemap.get(pn);
	 if (n != null) nodes.add(n);
       }
    }
   else {
      for ( ; ; ) {
	 Node first = null;
	 for (Node n : nodemap.values()) {
	    if (done.contains(n)) continue;
	    if (nodes.size() == 0) {
	       if (first == null || n.earlierThan(first)) first = n;
	     }
	    else {
	       if (first == null || n.comesBefore(first)) first = n;
	     }
	  }
	 if (first == null) break;
	 done.add(first);
	 nodes.add(first);
	 for (PetalArc pa : parcs) {
	    if (pa.getSource() == first.getNode()) {
	       Node n1 = nodemap.get(pa.getTarget());
	       if (n1 != null) n1.addConnection();
	     }
	    if (pa.getTarget() == first.getNode()) {
	       Node n1 = nodemap.get(pa.getSource());
	       if (n1 != null) n1.addConnection();
	     }
	  }
       }
    }

   int ct = nodes.size();
   double r = Math.max(ct * max / Math.PI,100);
   double phi = 2 * Math.PI / ct;
   double minx = 0;
   double miny = 0;
   for (int i = 0; i < nodes.size(); ++i) {
      Node n = nodes.get(i);
      PetalNode pn = n.getNode();
      double ang = Math.PI / 2.0 - i * phi;
      double x0 = r * Math.cos(ang);
      double y0 = r * Math.sin(ang);
      Component c = pn.getComponent();
      x0 -= c.getWidth() / 2;
      y0 -= c.getHeight() / 2;
      if (minx > x0) minx = x0;
      if (miny > y0) miny = y0;
    }
   for (int i = 0; i < nodes.size(); ++i) {
      Node n = nodes.get(i);
      PetalNode pn = n.getNode();
      double ang = Math.PI / 2.0 - i * phi;
      double x0 = r * Math.cos(ang);
      double y0 = r * Math.sin(ang);
      Component c = pn.getComponent();
      x0 -= c.getWidth() / 2 + minx - 5;
      y0 -= c.getHeight() / 2 + miny - 5;
      int xpos = (int) x0;
      int ypos = (int) y0;
      for_editor.commandMoveNode(pn,new Point(xpos,ypos));
    }
}





/********************************************************************************/
/*										*/
/*	Node representation							*/
/*										*/
/********************************************************************************/

private class Node {

   private PetalNode petal_node;
   private int num_inedge;
   private int num_outedge;
   private int num_connect;
   private int node_number;

   Node(PetalNode pn,int ct) {
      petal_node = pn;
      node_number = ct;
      num_inedge = 0;
      num_outedge = 0;
      num_connect = 0;
    }

   PetalNode getNode()			{ return petal_node; }

   void addInEdge()			{ num_inedge++; }
   void addOutEdge()			{ num_outedge++; }
   void addConnection() 		{ num_connect++; }

   boolean earlierThan(Node n1) {
      if (n1.num_inedge < num_inedge) return false;
      if (n1.num_inedge > num_inedge) return true;
      if (n1.num_outedge > num_outedge) return false;
      if (n1.num_outedge < num_outedge) return true;
      if (n1.node_number < node_number) return false;
      return true;
    }

   boolean comesBefore(Node n1) {
      if (num_connect > n1.num_connect) return true;
      if (num_connect < n1.num_connect) return false;
      int dn = num_inedge + num_outedge - num_connect;
      int dn1 = n1.num_inedge + n1.num_outedge - n1.num_connect;
      if (dn < dn1) return true;
      if (dn > dn1) return false;
      if (n1.node_number < node_number) return false;
      return true;
    }

}	// end of inner class Node



}	// end of class PetalCircleLayout




/* end of PetalCircleLayout.java */
