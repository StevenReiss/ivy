/********************************************************************************/
/*										*/
/*		PetalModelDefault.java						*/
/*										*/
/*	Default model storing nodes and arcs					*/
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


package edu.brown.cs.ivy.petal;


import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;




public class PetalModelDefault extends PetalModelBase implements PetalModel
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Vector<PetalNode> node_list;
private Vector<PetalArc>  arc_list;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalModelDefault()
{
   node_list = new Vector<PetalNode>();
   arc_list = new Vector<PetalArc>();
}



/********************************************************************************/
/*										*/
/*	General maintenance methods						*/
/*										*/
/********************************************************************************/


public void clear()
{
   deselectAll();

   Vector<PetalArc> c = new Vector<PetalArc>(arc_list);
   for (PetalArc pa : c) {
      removeArc(pa);
    }

   Vector<PetalNode> d = new Vector<PetalNode>(node_list);
   for (PetalNode pn : d) {
      removeNode(pn);
    }
}



/********************************************************************************/
/*										*/
/*	Methods to manage the set of nodes					*/
/*										*/
/********************************************************************************/

public synchronized void addNode(PetalNode n)
{
   if (node_list.contains(n)) return;

   node_list.add(n);
}



@Override public synchronized void removeNode(PetalNode n)
{
   if (!canRemoveNode(n)) return;

   node_list.removeElement(n);
   removeArcs(n,null);
   removeArcs(null,n);
}



@Override public synchronized PetalNode [] getNodes()
{
   int ct = node_list.size();
   PetalNode [] nds = new PetalNode[ct];
   node_list.copyInto(nds);

   return nds;
}



@Override public synchronized boolean dropNode(Object o,Point p,PetalNode pn,PetalArc pa)
{
   if (o instanceof PetalNode) {
      if (!node_list.contains(o)) {
	 addNode((PetalNode) o);
	 return true;
       }
    }

   return false;
}



protected boolean canRemoveNode(PetalNode pn)
{
   return true;
}



/********************************************************************************/
/*										*/
/*	Methods to manage the set of arcs					*/
/*										*/
/********************************************************************************/

public synchronized void addArc(PetalArc a)
{
   if (arc_list.contains(a)) return;

   arc_list.add(a);
}



@Override public synchronized void removeArc(PetalArc a)
{
   if (!canRemoveArc(a)) return;

   Vector<PetalNode> v = new Vector<PetalNode>(node_list);
   for (PetalNode pn : v) {
      if (pn.getLinkArc() == a) {
	 removeNode(pn);
       }
    }

   arc_list.removeElement(a);
}



public synchronized void removeArcs(PetalNode from,PetalNode to)
{
   int j = 0;
   int ct = arc_list.size();

   for (int i = 0; i < ct; ++i) {
      PetalArc a = arc_list.elementAt(i);
      if ((from == null || a.getSource() == from) && (to == null || a.getTarget() == to)) ++j;
      else if (j > 0) arc_list.setElementAt(a,i-j);
    }

   if (j != 0) arc_list.setSize(ct-j);
}




@Override public synchronized PetalArc [] getArcs()
{
   int ct = arc_list.size();
   PetalArc [] arcs = new PetalArc[ct];
   arc_list.copyInto(arcs);

   return arcs;
}



@Override public synchronized PetalArc [] getArcsFromNode(PetalNode n)
{
   int ct = 0;
   for (PetalArc a : arc_list) {
      if (a.getSource() == n) ++ct;
    }

   PetalArc [] arcs = new PetalArc[ct];
   ct = 0;
   for (PetalArc a : arc_list) {
      if (a.getSource() == n) arcs[ct++] = a;
    }

   return arcs;
}




@Override public synchronized PetalArc [] getArcsToNode(PetalNode n)
{
   int ct = 0;
   for (PetalArc a : arc_list) {
      if (a.getTarget() == n) ++ct;
    }

   PetalArc [] arcs = new PetalArc[ct];
   ct = 0;
   for (PetalArc a : arc_list) {
      if (a.getTarget() == n) arcs[ct++] = a;
    }

   return arcs;
}




@Override public void createArc(PetalNode f,PetalNode t)				{ }



protected boolean canRemoveArc(PetalArc pa)		{ return true; }




/********************************************************************************/
/*										*/
/*	Methods for handling correlation					*/
/*										*/
/*		These are more efficient than the PetalModelBase methods	*/
/*										*/
/********************************************************************************/

@Override public PetalNode findNodeAtLocation(Point p)
{
   for (PetalNode n : node_list) {
      Component c = n.getComponent();
      Rectangle r = c.getBounds();
      if (r != null && r.contains(p)) return n;
    }

   return null;
}



@Override public PetalArc findArcAtLocation(Point p)
{
   for (PetalArc a : arc_list) {
      if (a.contains(p)) return a;
    }

   return null;
}




}	// end of class PetalModelDefault




/* end of PetalModelDefault.java */
