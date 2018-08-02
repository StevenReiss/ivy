/********************************************************************************/
/*										*/
/*		PetalSelectionSet.java						*/
/*										*/
/*	Interface defining the graph model used by Petal			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalSelectionSet.java,v 1.9 2015/11/20 15:09:24 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalSelectionSet.java,v $
 * Revision 1.9  2015/11/20 15:09:24  spr
 * Reformatting.
 *
 * Revision 1.8  2011-10-12 21:47:30  spr
 * Code cleanup.
 *
 * Revision 1.7  2011-05-27 19:32:49  spr
 * Change copyrights.
 *
 * Revision 1.6  2010-11-18 23:09:02  spr
 * Updates to petal to work with bubbles.
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
 * Revision 1.2  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1  2003/07/16 19:44:59  spr
 * Move petal from bloom to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.petal;


import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;




public class PetalSelectionSet implements PetalConstants
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private PetalModel for_model;
private HashSet<Object>  select_set;
private PetalUndoSupport undo_support;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalSelectionSet(PetalModel model)
{
   for_model = model;
   select_set = new HashSet<Object>();
   undo_support = PetalUndoSupport.getSupport();
}



/********************************************************************************/
/*										*/
/*	External methods for handling various types of selection		*/
/*										*/
/********************************************************************************/

public synchronized void select(Object o)		{ addSelection(o); }



public synchronized void selectInBox(Rectangle box)
{
   Command sel = new Command(select_set.iterator());

   PetalNode [] nodes = for_model.getNodes();
   for (int i = 0; i < nodes.length; ++i) {
      Component c = nodes[i].getComponent();
      Rectangle r = c.getBounds();
      Point p = r.getLocation();
      Dimension s = r.getSize();
      if (box.contains(p) && box.contains(p.x+s.width,p.y+s.height))
	 selectInternal(nodes[i]);
    }

   PetalArc [] arcs = for_model.getArcs();
   for (int i = 0; i < arcs.length; ++i) {
      Point [] pts = arcs[i].getPoints();
      boolean fg = true;
      for (int j = 0; fg && j < pts.length; ++j) {
	 if (!box.contains(pts[j])) fg = false;
       }
      if (fg) selectInternal(arcs[i]);
    }

   sel.setResultSelection(select_set.iterator());
   undo_support.postEdit(sel);
}



public synchronized void selectAll()
{
   Command sel = new Command(select_set.iterator());

   deselectInternal();

   PetalNode [] nodes = for_model.getNodes();
   for (int i = 0; i < nodes.length; ++i) {
      selectInternal(nodes[i]);
    }

   PetalArc [] arcs = for_model.getArcs();
   for (int i = 0; i < arcs.length; ++i) {
      selectInternal(arcs[i]);
    }

   sel.setResultSelection(select_set.iterator());
   undo_support.postEdit(sel);
}




/********************************************************************************/
/*										*/
/*	External methods for deselection					*/
/*										*/
/********************************************************************************/

public synchronized void deselectAll()			{ clearSelection(); }



public synchronized void deselect(Object o)		{ removeSelection(o); }



/********************************************************************************/
/*										*/
/*	External methods for doing selection within a command (not undone)	*/
/*										*/
/********************************************************************************/

public synchronized void deselectLocal(Object o)	{ deselectInternal(o); }

public synchronized void deselectAllLocal()		{ deselectInternal(); }

public synchronized void selectLocal(Object o)		{ selectInternal(o); }




/********************************************************************************/
/*										*/
/*	External methods for accessing the selections				*/
/*										*/
/********************************************************************************/

public synchronized int getNumSelections()
{
   return select_set.size();
}



public synchronized PetalNode [] getSelectedNodes()
{
   int ct = 0;
   for (Iterator<?> it = select_set.iterator(); it.hasNext(); ) {
      Object o = it.next();
      if (o instanceof PetalNode) ++ct;
    }

   PetalNode [] nds = new PetalNode[ct];
   ct = 0;
   for (Iterator<?> it = select_set.iterator(); it.hasNext(); ) {
      Object o = it.next();
      if (o instanceof PetalNode) nds[ct++] = (PetalNode) o;
    }

   return nds;
}




public synchronized PetalArc [] getSelectedArcs()
{
   int ct = 0;
   for (Iterator<?> it = select_set.iterator(); it.hasNext(); ) {
      Object o = it.next();
      if (o instanceof PetalArc) ++ct;
    }

   PetalArc [] arcs = new PetalArc[ct];
   ct = 0;
   for (Iterator<?> it = select_set.iterator(); it.hasNext(); ) {
      Object o = it.next();
      if (o instanceof PetalArc) arcs[ct++] = (PetalArc) o;
    }

   return arcs;
}



public synchronized boolean isSelected(Object o)
{
   return select_set.contains(o);
}



/********************************************************************************/
/*										*/
/*	Internal methods for managing selections				*/
/*										*/
/********************************************************************************/

private void addSelection(Object o)
{
   if (o == null || select_set.contains(o)) return;

   Command sel = new Command(select_set.iterator());

   selectInternal(o);

   sel.setResultSelection(select_set.iterator());
   undo_support.postEdit(sel);
}



private void removeSelection(Object o)
{
   if (o == null || !select_set.contains(o)) return;

   Command sel = new Command(select_set.iterator());

   deselectInternal(o);

   sel.setResultSelection(select_set.iterator());
   undo_support.postEdit(sel);
}



private void clearSelection()
{
   if (select_set.size() == 0) return;

   Command sel = new Command(select_set.iterator());

   deselectInternal();

   undo_support.postEdit(sel);
}



private void selectInternal(Object o)			{ select_set.add(o); }

private void deselectInternal() 			{ select_set.clear(); }

private void deselectInternal(Object o) 		{ select_set.remove(o); }




/********************************************************************************/
/*										*/
/*	Command subclass for handling selection commands			*/
/*										*/
/********************************************************************************/

private class Command extends AbstractUndoableEdit {

   private Vector<Object> original_selection;
   private Vector<Object> result_selection;
   private static final long serialVersionUID = 1;

   Command(Iterator<?> osel) {
      if (osel == null || !osel.hasNext()) original_selection = null;
      else {
	 original_selection = new Vector<Object>();
	 while (osel.hasNext()) {
	    original_selection.add(osel.next());
	  }
       }

      result_selection = null;
    }

   void setResultSelection(Iterator<?> nsel) {
      if (nsel == null || !nsel.hasNext()) result_selection = null;
      else {
         result_selection = new Vector<Object>();
         while (nsel.hasNext()) {
            result_selection.add(nsel.next());
          }
       }
    }

   @Override public String getPresentationName()		{ return "Select Nodes"; }

   @Override public boolean addEdit(UndoableEdit ed) {
      if (ed instanceof Command) {
	 Command csl = (Command) ed;
	 if (csl.getSelectionSet() == PetalSelectionSet.this) {
	    result_selection = csl.result_selection;
	    return true;
	  }
       }

      return false;
    }

   @Override public void redo() throws CannotRedoException {
      super.redo();
      setSelection(result_selection);
    }

   @Override public void undo() throws CannotUndoException {
      super.undo();
      setSelection(original_selection);
    }

   private PetalSelectionSet getSelectionSet()		{ return PetalSelectionSet.this; }

   private void setSelection(Vector<?> v) {
      deselectInternal();

      if (v != null) {
	 for (Iterator<?> it = v.iterator(); it.hasNext(); ) {
	    Object o = it.next();
	    selectInternal(o);
	  }
       }

      for_model.fireModelUpdated();
    }

}	// end of subclass Command





}	// end of class PetalSelectionSet




/* end of PetalSelectionSet.java */

