/********************************************************************************/
/*										*/
/*		PetalModelBase.java						*/
/*										*/
/*	Basic model that doesn't store nodes and arcs                           */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalModelBase.java,v 1.9 2018/08/02 15:10:36 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalModelBase.java,v $
 * Revision 1.9  2018/08/02 15:10:36  spr
 * Fix imports.
 *
 * Revision 1.8  2015/11/20 15:09:23  spr
 * Reformatting.
 *
 * Revision 1.7  2013/11/15 02:39:14  spr
 * Fix imports
 *
 * Revision 1.6  2011-05-27 19:32:49  spr
 * Change copyrights.
 *
 * Revision 1.5  2010-11-18 23:09:02  spr
 * Updates to petal to work with bubbles.
 *
 * Revision 1.4  2009-01-27 00:40:02  spr
 * IvyXmlWriter cleanup.
 *
 * Revision 1.3  2007-05-04 02:00:35  spr
 * Import fixups.
 *
 * Revision 1.2  2005/07/08 20:57:47  spr
 * Change imports.
 *
 * Revision 1.1  2005/04/28 21:48:40  spr
 * Fix up petal to support pebble.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.petal;


import edu.brown.cs.ivy.swing.SwingEventListenerList;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;




public abstract class PetalModelBase implements PetalModel
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private PetalSelectionSet select_set;
private SwingEventListenerList<PetalModelListener> model_listeners;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalModelBase()
{
   model_listeners = new SwingEventListenerList<PetalModelListener>(PetalModelListener.class);

   select_set = new PetalSelectionSet(this);
}



/********************************************************************************/
/*										*/
/*	Methods for handling cut and paste					*/
/*										*/
/********************************************************************************/

@Override public Object getCopyObject(PetalArc pa)		{ return pa; }
@Override public Object getCopyObject(PetalNode pn)		{ return pn; }

@Override public PetalNode addPasteNode(Object o,boolean dofg)			{ return null; }
@Override public PetalNode addPasteNode(Object o,PetalClipSet pcs,boolean dofg)	{ return null; }
@Override public PetalArc addPasteArc(Object o,PetalClipSet pcs,boolean dofg)	{ return null; }




/********************************************************************************/
/*										*/
/*	Listener methods							*/
/*										*/
/********************************************************************************/

@Override public void addModelListener(PetalModelListener l)
{
   model_listeners.add(l);
}


@Override public void removeModelListener(PetalModelListener l)
{
   model_listeners.remove(l);
}



@Override public void fireModelUpdated()
{
   for (PetalModelListener l : model_listeners) {
      l.modelUpdated();
    }
}




/********************************************************************************/
/*										*/
/*	Methods for handling selection						*/
/*										*/
/********************************************************************************/

@Override public synchronized void select(Object o)		{ select_set.select(o); }


@Override public synchronized void selectInBox(Rectangle box)	{ select_set.selectInBox(box); }


@Override public synchronized void selectAll()			{ select_set.selectAll(); }


@Override public synchronized void deselectAll()			{ select_set.deselectAll(); }


@Override public synchronized void deselect(Object o)		{ select_set.deselect(o); }


@Override public synchronized PetalNode [] getSelectedNodes()	{ return select_set.getSelectedNodes(); }


@Override public synchronized PetalArc [] getSelectedArcs()	{ return select_set.getSelectedArcs(); }


@Override public synchronized boolean isSelected(PetalNode n)	{ return select_set.isSelected(n); }


@Override public synchronized boolean isSelected(PetalArc a)	{ return select_set.isSelected(a); }

@Override public int getNumSelections()				{ return select_set.getNumSelections(); }




/********************************************************************************/
/*										*/
/*	Methods for handling correlation					*/
/*										*/
/********************************************************************************/

public PetalNode findNodeAtLocation(Point p)
{
   PetalNode [] nds = getNodes();

   for (int i = 0; i < nds.length; ++i) {
      Component c = nds[i].getComponent();
      Rectangle r = c.getBounds();
      if (r != null && r.contains(p)) return nds[i];
    }

   return null;
}



public PetalArc findArcAtLocation(Point p)
{
   PetalArc [] acs = getArcs();

   for (int i = 0; i < acs.length; ++i) {
      if (acs[i].contains(p)) return acs[i];
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Methods for handling arc creation					*/
/*										*/
/********************************************************************************/

@Override public boolean handleArcEndPoint(PetalNode pn,Point p,int mode,MouseEvent evt)
{
   if (mode == ARC_MODE_START && pn == null) return false;

   return false;
}




/********************************************************************************/
/*										*/
/*	Methods for handling mouse over 					*/
/*										*/
/********************************************************************************/

@Override public void handleMouseOver(PetalNode pn,PetalArc pa,Point p)		{ }

@Override public boolean handlePopupRequest(PetalNode pn,PetalArc pa,MouseEvent evt)
{
   return false;
}



/********************************************************************************/
/*										*/
/*	Map arcs to arcs for node						*/
/*										*/
/********************************************************************************/

@Override public synchronized PetalArc [] getArcsFromNode(PetalNode n)
{
   PetalArc [] allarcs = getArcs();

   int ct = 0;
   for (PetalArc a : allarcs) {
      if (a.getSource() == n) ++ct;
    }

   PetalArc [] arcs = new PetalArc[ct];
   ct = 0;
   for (PetalArc a : allarcs) {
      if (a.getSource() == n) arcs[ct++] = a;
    }

   return arcs;
}




@Override public synchronized PetalArc [] getArcsToNode(PetalNode n)
{
   PetalArc [] allarcs = getArcs();

   int ct = 0;
   for (PetalArc a : allarcs) {
      if (a.getTarget() == n) ++ct;
    }

   PetalArc [] arcs = new PetalArc[ct];
   ct = 0;
   for (PetalArc a : allarcs) {
      if (a.getTarget() == n) arcs[ct++] = a;
    }

   return arcs;
}




}	// end of class PetalModelDefault




/* end of PetalModelBase.java */
