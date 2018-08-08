/********************************************************************************/
/*										*/
/*		PetalClipSet.java						*/
/*										*/
/*	Class to hold a set of nodes and arcs for the clipboard 		*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalClipSet.java,v 1.8 2018/08/02 15:10:36 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalClipSet.java,v $
 * Revision 1.8  2018/08/02 15:10:36  spr
 * Fix imports.
 *
 * Revision 1.7  2015/11/20 15:09:23  spr
 * Reformatting.
 *
 * Revision 1.6  2011-05-27 19:32:48  spr
 * Change copyrights.
 *
 * Revision 1.5  2007-08-10 02:11:21  spr
 * Cleanups from eclipse.
 *
 * Revision 1.4  2007-05-04 02:00:35  spr
 * Import fixups.
 *
 * Revision 1.3  2005/05/07 22:25:43  spr
 * Updates for java 5.0
 *
 * Revision 1.2  2004/05/05 02:28:08  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1  2003/07/16 19:44:58  spr
 * Move petal from bloom to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.petal;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;




public class PetalClipSet implements Transferable
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Vector<Object>	  set_nodes;
private Vector<Object>	  set_arcs;
private HashMap<Object,Object> node_map;
private HashMap<Object,Object> interim_map;

private static DataFlavor      data_flavor = null;




/********************************************************************************/
/*										*/
/*	Static methods								*/
/*										*/
/********************************************************************************/


public static DataFlavor getFlavor()
{
   if (data_flavor == null) {
      data_flavor = new DataFlavor(PetalClipSet.class,"Petal Editor Clip Set");
    }

   return data_flavor;
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalClipSet()
{
   getFlavor();

   set_nodes = new Vector<Object>();
   set_arcs = new Vector<Object>();
   node_map = new HashMap<Object,Object>();
   interim_map = new HashMap<Object,Object>();
}



/********************************************************************************/
/*										*/
/*	Methods for adding new nodes and arcs					*/
/*										*/
/********************************************************************************/

public void addNode(PetalNode src,Object node)
{
   if (node == null) return;

   set_nodes.add(node);

   interim_map.put(node,src);
}


public void addArc(PetalArc src,Object arc)
{
   if (arc == null) return;

   set_arcs.add(arc);

   interim_map.put(arc,src);
}



public Enumeration<?> getNodeElements()		{ return set_nodes.elements(); }
public Iterator<?> getNodeIterator()		{ return set_nodes.iterator(); }

public Enumeration<?> getArcElements()		{ return set_arcs.elements(); }
public Iterator<?> getArcIterator()		{ return set_arcs.iterator(); }



public void setMapping(Object node,PetalNode to)
{
   PetalNode src = (PetalNode) interim_map.get(node);

   if (src != null) node_map.put(src,to);
}


public void setMapping(Object arc,PetalArc to)
{
   PetalArc src = (PetalArc) interim_map.get(arc);

   if (src != null) node_map.put(src,to);
}




public PetalNode getMapping(PetalNode n)
{
   if (n == null) return n;

   for ( ; ; ) {
      PetalNode pn = (PetalNode) node_map.get(n);
      if (pn == null) break;
      n = pn;
    }

   return n;
}



public PetalArc getMapping(PetalArc a)
{
   if (a == null) return null;

   for ( ; ; ) {
      PetalArc pa = (PetalArc) node_map.get(a);
      if (pa == null) break;
      a = pa;
    }

   return a;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public boolean isEmpty()
{
   return set_nodes.size() + set_arcs.size() == 0;
}



/********************************************************************************/
/*										*/
/*	Transferable interface							*/
/*										*/
/********************************************************************************/

@Override public Object getTransferData(DataFlavor f) throws UnsupportedFlavorException
{
   if (f != data_flavor) throw new UnsupportedFlavorException(f);

   return this;
}


@Override public DataFlavor [] getTransferDataFlavors()
{
   return new DataFlavor [] { data_flavor };
}



@Override public boolean isDataFlavorSupported(DataFlavor f)
{
   return f == data_flavor;
}




}	// end of class PetalClipSet



/* end of PetalClipSet.java */
