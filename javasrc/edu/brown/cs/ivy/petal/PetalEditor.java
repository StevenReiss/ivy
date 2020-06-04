/********************************************************************************/
/*										*/
/*		PetalEditor.java						*/
/*										*/
/*	Implementation of the Petal graphics editor				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalEditor.java,v 1.30 2018/08/02 15:10:36 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalEditor.java,v $
 * Revision 1.30  2018/08/02 15:10:36  spr
 * Fix imports.
 *
 * Revision 1.29  2017/08/04 12:42:55  spr
 * Clean up
 *
 * Revision 1.28  2017/04/30 02:54:57  spr
 * Clean up code.
 *
 * Revision 1.27  2017/03/14 14:01:23  spr
 * Expose findArc; formatting
 *
 * Revision 1.26  2015/11/20 15:09:23  spr
 * Reformatting.
 *
 * Revision 1.25  2011-09-12 20:50:27  spr
 * Code cleanup.
 *
 * Revision 1.24  2011-05-27 19:32:48  spr
 * Change copyrights.
 *
 * Revision 1.23  2010-12-08 22:50:39  spr
 * Fix up dipslay and add new layouts
 *
 * Revision 1.22  2010-12-03 21:57:39  spr
 * Fix up sizing.
 *
 * Revision 1.21  2010-12-02 23:46:49  spr
 * Petal bug fixes
 *
 * Revision 1.20  2010-11-20 00:28:39  spr
 * Color arcs; add new features to Petal editor.
 *
 * Revision 1.19  2010-11-18 23:09:02  spr
 * Updates to petal to work with bubbles.
 *
 * Revision 1.18  2010-07-28 01:21:20  spr
 * Bug fixes in petal.
 *
 * Revision 1.17  2010-07-01 21:57:08  spr
 * Don't create extra Point object.
 *
 * Revision 1.16  2007-08-10 02:11:21  spr
 * Cleanups from eclipse.
 *
 * Revision 1.15  2006-12-01 03:22:54  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.14  2006/07/10 14:52:24  spr
 * Code cleanup.
 *
 * Revision 1.13  2006/04/21 23:10:57  spr
 * Update printing.
 *
 * Revision 1.12  2005/10/31 19:20:41  spr
 * Add error check for printing.
 *
 * Revision 1.11  2005/06/28 17:21:05  spr
 * Minor bug fixes
 *
 * Revision 1.10  2005/06/07 02:18:22  spr
 * Update for java 5.0
 *
 * Revision 1.9  2005/05/07 22:25:43  spr
 * Updates for java 5.0
 *
 * Revision 1.8  2005/04/28 21:48:40  spr
 * Fix up petal to support pebble.
 *
 * Revision 1.7  2004/06/04 21:02:48  spr
 * Add and correct printing.
 *
 * Revision 1.6  2004/06/02 01:19:34  spr
 * Update print to use resolution to size image.
 *
 * Revision 1.5  2004/05/28 20:57:31  spr
 * Add printing and minor fixes to level layout.
 *
 * Revision 1.4  2004/05/22 02:37:59  spr
 * Fix bugs in the editor where update is called before things are initialized;
 * minor fixups to level layout (more needed).
 *
 * Revision 1.3  2004/05/20 16:03:37  spr
 * Bug fixes for Petal related to CHIA; add oval helper.
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


import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.event.MouseInputAdapter;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;




public class PetalEditor extends JLayeredPane implements Scrollable,
		PetalConstants, PropertyChangeListener, ComponentListener,
		PetalUndoManager.CommandHandler, DropTargetListener, Printable,
		PetalModelListener
{


public PetalEditor(PetalModel model)
{
   this();

   setPetalModel(model);
}



public void setPetalModel(PetalModel model)
{
   if (graph_model != null) graph_model.removeModelListener(this);

   graph_model = model;
   if (graph_model != null) graph_model.addModelListener(this);

   setToolTipText("");
}



public void setBackgroundColor(Color c,boolean opaq)
{
   background_frame.setBackground(c);
   background_frame.setOpaque(opaq);
}



public PetalModel getPetalModel()			{ return graph_model; }

public void setGridSize(int size)			{ grid_size = size; }
public int getGridSize()				{ return grid_size; }

public boolean getClipboardEmpty()			{ return clip_board.getContents(this) == null; }

public void update()					{ localUpdate(); }

public PetalNode findNode(Point p)			{ return findNodeHelper(p, null); }

public void commandUndo()				{ handleUndo(); }

public void commandRedo()				{ handleRedo(); }

public void commandSelectAll()				{ handleSelectAll(); }

public void commandDeselectAll()			{ handleDeselectAll(); }

public void commandCut()				{ handleCutCopy(true); }

public void commandCopy()				{ handleCutCopy(false); }

public void commandPaste()				{ handlePaste(); }

public void commandClear()				{ handleClear(); }

public void commandSplineArcs(boolean fg)		{ handleSplineArcs(fg); }

public void commandRemovePivots()			{ handleRemovePivots(); }

public void commandPrint(String file)			{ handlePrint(file); }


public boolean getSplineArcs()				{ return spline_arcs; }
public void setSplineArcs(boolean fg)			{ spline_arcs = fg; }



public String getUndoCommand()
{
   return undo_support.getUndoCommand();
}

public String getRedoCommand()
{
   return undo_support.getRedoCommand();
}

public void setActive(boolean fg)
{
   if (fg) undo_support.setManager(command_manager);
   else undo_support.setManager(null);
}



public void addEditorCallback(PetalEditorCallback cb)
{
   if (cb != null) editor_callbacks.add(cb);
}

public void removeEditorCallback(PetalEditorCallback cb)
{
   if (cb != null) editor_callbacks.remove(cb);
}


public Point getGridPoint(Point p)
{
   if (grid_size == 0) return p;

   return new Point(gridCoord(p.x),gridCoord(p.y));
}


public void commandLayout(PetalLayoutMethod plm)		{ handleLayout(plm); }

public void commandMoveNode(PetalNode pn,Point loc)		{ handleMoveNode(pn,loc); }

public void commandReplacePivots(PetalArc pa,Point [] pvts)	{ handleReplacePivots(pa,pvts); }

public double getScaleFactor()					{ return scale_by; }
public void setScaleFactor(double v)
{
   if (v <= 0) v = 1.0;
   scale_by = v;
   fixupWindowSize();
}

public void addZoomWheeler()
{
   JComponent c = this;
   if (c.getParent() != null && c.getParent() instanceof JViewport)
      c = (JComponent) c.getParent();
   if (c.getParent() != null && c.getParent() instanceof JScrollPane)
      c = (JComponent) c.getParent();
   c.addMouseWheelListener(new Wheeler());
}


public Dimension getExtent()					{ return getUsedSize(); }



/********************************************************************************/
/*										*/
/*	Internal constants for the editor					*/
/*										*/
/********************************************************************************/

private static final int	BASE_LAYER = 0; 		// background
private static final int	ARC_LAYER = 100;		// arcs drawn here
private static final int	NODE_LAYER = 110;		// nodes drawn here
private static final int	TAB_LAYER = 300;		// item tabs drawn here
private static final int	PDRAG_LAYER = 310;		// drag outline drawn here

private static final int	HANDLE_SIZE = 6;		// size of handle box

private static final int	SELECTION_BOX = -1;		// count for selection box
private static final int	ARC_CREATE = -2;		// count for creating arc
private static final int	IGNORE = -3;			// count to ignore
private static final int	RESIZE = -4;			// count for resizing
private static final int	PIVOT = -5;			// count for creating/moving pivot

private static final int	MIN_NODE_SIZE = 40;		// size for determining position

private static final int	X_OFFSET = 16;			// horizontal offset for overlapping nodes
private static final int	Y_OFFSET = 16;			// vertical offset for overlapping nodes

private static final double     SCALE_FACTOR = 1.125;

private static final Cursor	DEFAULT_CURSOR = null;
private static final Cursor	ITEM_CURSOR = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
private static final Cursor	MOVE_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
private static final Cursor []	CORNER_CURSOR = {
   Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR)
};


private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private PetalModel	graph_model;
private TabFrame	tab_frame;
private DragFrame	drag_frame;
private ArcFrame	arc_frame;
private JPanel		background_frame;
private Dimension	editor_size;
private PetalUndoSupport undo_support;
private PetalUndoManager command_manager;
private Vector<PetalEditorCallback> editor_callbacks;
private int		grid_size;
private Point		next_position;
private boolean 	position_center;

private PetalNode	resize_component;
private int		resize_x;
private int		resize_y;

private int		pivot_index;
private PetalArc	pivot_arc;
private boolean 	new_pivot;

private boolean 	spline_arcs;
private boolean 	spline_without_pivots;

private Cursor		cur_cursor;

private int x_offset;
private int y_offset;

private double		scale_by;
private double          user_scale;
private double          prior_scale;


private static Clipboard clip_board = new Clipboard("Petal Clipboard");




/********************************************************************************/
/*										*/
/*	Private constructors							*/
/*										*/
/********************************************************************************/

private PetalEditor()
{
   graph_model = null;
   tab_frame = null;
   drag_frame = null;
   arc_frame = null;
   undo_support = PetalUndoSupport.getSupport();
   if (undo_support.getManager() == null) {
      command_manager = new PetalUndoManager(this);
      undo_support.setManager(command_manager);
    }
   grid_size = 0;
   next_position = null;
   position_center = false;
   editor_callbacks = new Vector<PetalEditorCallback>();
   resize_component = null;
   resize_x = 0;
   resize_y = 0;
   scale_by = 1.0;
   user_scale = 1.0;
   prior_scale = 1.0;
   pivot_index = -1;
   pivot_arc = null;
   new_pivot = false;
   spline_arcs = false;
   spline_without_pivots = false;

   x_offset = 0;
   y_offset = 0;

   setup();
}



private synchronized void setup()
{
   editor_size = null;

   addComponentListener(this);
   setSize(10,10);

   setDoubleBuffered(true);

   tab_frame = new TabFrame();
   tab_frame.setOpaque(false);
   add(tab_frame,getObjectForLayer(TAB_LAYER));
   drag_frame = new DragFrame();
   drag_frame.setOpaque(false);
   add(drag_frame,getObjectForLayer(PDRAG_LAYER));
   arc_frame = new ArcFrame();
   arc_frame.setOpaque(false);
   arc_frame.setBackground(Color.red);
   add(arc_frame,getObjectForLayer(ARC_LAYER));
   background_frame = new JPanel();
   background_frame.setOpaque(true);
   add(background_frame,getObjectForLayer(BASE_LAYER));

   addPropertyChangeListener(this);
   Mouser mh = new Mouser();
   addMouseListener(mh);
   addMouseMotionListener(mh);

   new DropTarget(drag_frame,DnDConstants.ACTION_COPY_OR_MOVE,this,true);

   setRequestFocusEnabled(true);

   Keyer kh = new Keyer();
   addKeyListener(kh);
   drag_frame.addKeyListener(kh);

   setAutoscrolls(true);

   setPetalCursor(DEFAULT_CURSOR);

   setSizes();
}



/********************************************************************************/
/*										*/
/*	Update methods								*/
/*										*/
/********************************************************************************/

private synchronized void localUpdate()
{
   Component [] nds = getComponentsInLayer(NODE_LAYER);

   for (int i = 0; i < nds.length; ++i) {
      remove(nds[i]);
    }

   if (graph_model == null) return;

   PetalNode [] nodes = graph_model.getNodes();
   PetalArc [] arcs = graph_model.getArcs();

   Integer nlay = getObjectForLayer(NODE_LAYER);

   for (int i = nodes.length - 1; i >= 0; --i) {
     Component c = nodes[i].getComponent();
     add(c,nlay);
   }

   for (int i = 0; i < arcs.length; ++i) {
      arcs[i].layout();
    }

   for (int i = 0; i < nodes.length; ++i) {
      PetalLink pl = nodes[i].getLink();
      if (pl != null) {
	 PetalArc pa = nodes[i].getLinkArc();
	 Point p = pl.getLocation(pa,nodes[i]);
	 nodes[i].getComponent().setLocation(p);
       }
    }

   tab_frame.repaint(tab_frame.getBounds());
}


@Override public void modelUpdated()
{
   localUpdate();
}



/********************************************************************************/
/*										*/
/*	Methods to do correlation						*/
/*										*/
/********************************************************************************/

private PetalNode findNodeHelper(Point p, MouseEvent evt)
{
   PetalNode [] nodes = graph_model.getNodes();
   if ( nodes.length == 0 ) return null;
   Point p1 = null;
   PetalNode topNode = null;
   int topPosition = -2;
   int topIndex = -1;
   int last = nodes.length-1;
   if (last == -1) return topNode;

   p = scalePoint(p);

   for (int i = last; i >= 0; --i) {
      Component c = nodes[i].getComponent();
      if (c != null) {
	 p1 = getComponentPoint(nodes[i],p);
	 if (c.contains(p1.x,p1.y)) {
	    int pos = getPosition(c);
	    if ( topPosition == -2 || pos < topPosition ) {
	       topPosition = pos;
	       topIndex = i;
	     }
	  }
       }
    }

   if (topIndex == -1) return topNode;
   topNode = nodes[topIndex];
   if ( topNode == null ) return topNode;
   PetalNode nextNode;
   PetalNode tempNode;
   if (evt != null && evt.getClickCount() == 2 ) {
      nextNode = nodes[0];
      nodes[0] = topNode;
      for (int k = 1; k <= topIndex; ++k) {
	 tempNode = nodes[k];
	 nodes[k] = nextNode;
	 nextNode = tempNode;
       }
    }
   else {
      nextNode = nodes[last];
      nodes[last] = topNode;
      for (int j = last-1; j >= topIndex; --j) {
	 tempNode = nodes[j];
	 nodes[j] = nextNode;
	 nextNode = tempNode;
       }
    }

   return topNode;
}



public PetalArc findArc(Point p)
{
   return findArcHelper(p,null);
}



private PetalArc findArcHelper(Point p,MouseEvent evt)
{
   PetalArc [] arcs = graph_model.getArcs();

   p = scalePoint(p);

   for (int i = 0; i < arcs.length; ++i) {
      if (arcs[i].contains(p)) return arcs[i];
    }

   return null;
}




private Point scalePoint(Point p)
{
   if (scale_by == 1.0 || p == null) return p;

   Point p1 = new Point((int)(p.x/scale_by),(int)(p.y/scale_by));

   return p1;
}


private Dimension scaleDimension(Dimension d)
{
   if (scale_by == 1.0 || d == null) return d;

   Dimension d1 = new Dimension(((int)(d.width*scale_by)),((int)(d.height*scale_by)));

   return d1;
}



private Rectangle scaleRectangle(Rectangle r)
{
   if (scale_by == 1.0 || r == null) return r;

   Rectangle r1 = new Rectangle(scalePoint(r.getLocation()),scaleDimension(r.getSize()));

   return r1;
}




/********************************************************************************/
/*										*/
/*	Basic command methods							*/
/*										*/
/********************************************************************************/

private void handleSelectAll()
{
   graph_model.selectAll();

   localUpdate();
}



private void handleDeselectAll()
{
   graph_model.deselectAll();

   localUpdate();
}



private void handleClear()
{
   undo_support.beginUpdate();
   handleSelectAll();
   handleCutCopy(true);
   undo_support.endUpdate("Clear");
}




/********************************************************************************/
/*										*/
/*	Positioning methods							*/
/*										*/
/********************************************************************************/

public synchronized void assignNextPosition(Component c)
{
  Dimension sz = getSize();

  if (c == null) return;
  if (next_position == null) {
    next_position = new Point(PETAL_LEFT_POSITION,PETAL_TOP_POSITION);
    position_center = false;
  }
  else
    {
      PetalNode [] nodes = graph_model.getNodes();
      if (nodes.length == 1) {
	next_position = new Point(PETAL_LEFT_POSITION,PETAL_TOP_POSITION);
	x_offset = 0;
	y_offset = 0;
      }
    }

  Dimension csz = c.getSize();

  if (position_center) {
    next_position.x -= csz.width/2;
    next_position.y -= csz.height/2;
    if (next_position.x < 0) next_position.x = 0;
    if (next_position.y < 0) next_position.y = 0;
    position_center = false;
  }

  next_position.x = gridCoord(next_position.x);
  next_position.y = gridCoord(next_position.y);
  c.setLocation(next_position);

  // the next node will fit on the same line
  if (next_position.x + csz.width*2 + MIN_NODE_SIZE < sz.width) {
    next_position.x += csz.width + MIN_NODE_SIZE;
  }
  // the next node will fit on a new line
  else if (next_position.y + csz.height*2 + MIN_NODE_SIZE < sz.height) {
    next_position.x = PETAL_LEFT_POSITION + x_offset;
    next_position.y += csz.height + MIN_NODE_SIZE;
  }
  // there should be a max of 8 overlapping nodes
  else if (y_offset >= Y_OFFSET*7) {
    next_position.x = PETAL_LEFT_POSITION;
    next_position.y = PETAL_TOP_POSITION;
    x_offset = 0;
    y_offset = 0;
  }
  else {
    x_offset += X_OFFSET;
    y_offset += Y_OFFSET;
    next_position.x = PETAL_LEFT_POSITION + x_offset;
    next_position.y = PETAL_TOP_POSITION + y_offset;
  }
}


/********************************************************************************/
/*										*/
/*	Action methods for mouse movement					*/
/*										*/
/********************************************************************************/

synchronized void handleMove(MouseEvent evt)
{
   Point p = evt.getPoint();
   PetalNode pn = findNodeHelper(p,evt);
   Point p1 = null;
   PetalArc pa = null;
   if (pn == null) pa = findArcHelper(p,evt);
   else p1 = getComponentPoint(pn,p);

   boolean pvt = false;
   int corner = -1;
   if (pa != null)
      pvt = pa.overPivot(scalePoint(p));
   if (pn != null) {
      PetalNode orsz = resize_component;
      if (resize_component == null) checkResize(p);
      if (resize_component != null) corner = resize_x + 3 * resize_y;
      resize_component = orsz;
    }

   if (corner >= 0) setPetalCursor(CORNER_CURSOR[corner]);
   else if (pn != null) setPetalCursor(ITEM_CURSOR);
   else if (pvt) setPetalCursor(MOVE_CURSOR);
   else if (pa != null) setPetalCursor(ITEM_CURSOR);
   else setPetalCursor(DEFAULT_CURSOR);

   graph_model.handleMouseOver(pn,pa,p1);
}




synchronized private void setPetalCursor(Cursor c)
{
   if (c == cur_cursor) return;
   cur_cursor = c;

   setCursor(c);
}



/********************************************************************************/
/*										*/
/*	Action methods for selection						*/
/*										*/
/********************************************************************************/

synchronized void handleClick(MouseEvent evt)
{
   Point p = evt.getPoint();
   PetalNode pn = findNodeHelper(p,evt);
   PetalArc pa = null;
   if (pn == null) pa = findArcHelper(p,evt);

   if (pn != null) {
     if (pn.handleMouseClick(evt)) ;
     else if (graph_model.isSelected(pn)) {
       graph_model.deselect(pn);
     }
     else {
       if (!evt.isShiftDown() && !evt.isControlDown()) graph_model.deselectAll();
       graph_model.select(pn);
     }
   }
   else if (pa != null) {
      pa.handleMouseClick(evt);
      if (graph_model.isSelected(pa)) graph_model.deselect(pa);
      else {
	 if (!evt.isShiftDown() && !evt.isControlDown()) graph_model.deselectAll();
	 graph_model.select(pa);
       }
    }
   else {
      if (!evt.isShiftDown() && !evt.isControlDown()) graph_model.deselectAll();
    }

   commandEndNotify();
}



/********************************************************************************/
/*										*/
/*	Methods for handleing mouse drag					*/
/*										*/
/********************************************************************************/

synchronized private int handleDrag(MouseEvent evt,Point down,int ctr,boolean release)
{
   Rectangle r = new Rectangle(evt.getX(),evt.getY(),1,1);
   scrollRectToVisible(r);

   if (ctr == SELECTION_BOX) {
      handleDragSelect(evt,down,release);
      return ctr;
    }
   if (ctr == ARC_CREATE) {
      handleArcCreate(evt,down,release);
      return ctr;
    }
   if (ctr == RESIZE) {
      handleResize(evt,down,release);
      return ctr;
    }
   if (ctr == PIVOT) {
      handlePivot(evt,down,release);
      return ctr;
    }
   if (ctr == IGNORE) return ctr;
   if (ctr == 0) {
      PetalNode pn = findNode(down);
      PetalArc pa = null;
      if (pn == null) pa = findArc(down);
      Point p = getComponentPoint(pn,down);
      if (checkResize(down)) {
	 handleResize(evt,down,false);
	 return RESIZE;
       }
      if (graph_model.handleArcEndPoint(pn,p,PetalModel.ARC_MODE_START,evt)) {
	 handleArcCreate(evt,down,false);
	 return ARC_CREATE;
       }
      if (pn == null && pa != null) {
	 Point pdown = scalePoint(down);
	 new_pivot = !pa.overPivot(pdown);
	 pivot_index = pa.createPivot(pdown);
	 if (pivot_index >= 0) {
	    undo_support.beginUpdate();
	    if (!graph_model.isSelected(pa)) {
	       graph_model.deselectAll();
	       graph_model.select(pa);
	     }
	    pivot_arc = pa;
	    handlePivot(evt,down,false);
	    return PIVOT;
	  }
       }
      if (pn == null) {
	 handleDragSelect(evt,down,false);
	 return SELECTION_BOX;
       }

      if (!graph_model.isSelected(pn)) {
	 graph_model.deselectAll();
	 graph_model.select(pn);
       }
      ctr = 0;
    }

   handleDragMove(evt,down);

   if (release) {
      fixupWindowSize();
      fixupWindowPosition();
      setPetalCursor(DEFAULT_CURSOR);
      issueCommandDone();
    }

   return ctr+1;
}



private void handleDragMove(MouseEvent evt,Point down)
{
   Point ept = evt.getPoint();
   int dx = ept.x - down.x;
   int dy = ept.y - down.y;

   dx = gridCoord(dx);
   dy = gridCoord(dy);

   if (dx == 0 && dy == 0) return;

   PetalNode [] nds = graph_model.getSelectedNodes();
   if (nds == null || nds.length == 0) return;

   for (int i = 0; i < nds.length; ++i) {
      PetalLink lnk = nds[i].getLink();
      if (lnk == null) {
	 Component c = nds[i].getComponent();
	 if (c != null) {
	    Point pt = c.getLocation(); 	      // normal node
	    if (pt.x + dx < 0) dx = -pt.x;
//	    else if (editor_size != null && pt.x + dx + d.width > editor_size.width)
//	       dx = editor_size.width - d.width - 1 - pt.x;
	    if (pt.y + dy < 0) dy = -pt.y;
//	    else if (editor_size != null && pt.y + dy + d.height > editor_size.height)
//	       dy = editor_size.height - d.height - 1 - pt.y;
	  }
       }
    }

   if (dx == 0 && dy == 0) return;

   setPetalCursor(MOVE_CURSOR);

   CommandMove cmd = new CommandMove();

   for (int i = 0; i < nds.length; ++i) {
      PetalLink lnk = nds[i].getLink();
      if (lnk == null) {
	 Component c = nds[i].getComponent();
	 if (c != null) {
	    Point pt = c.getLocation(); 	      // normal node
	    pt.x += dx;
	    pt.y += dy;
	    c.setLocation(pt);
	    cmd.moveNode(nds[i],dx,dy);
	  }
       }
      else if (nds.length == 1) {
	 lnk.moveLocation(dx,dy,nds[i].getLinkArc(),nds[i]);
	 cmd.moveNode(nds[i],dx,dy);
       }
    }

   undo_support.postEdit(cmd);

   down.x += dx;
   down.y += dy;

   update();
}




private void handleDragSelect(MouseEvent evt,Point dp,boolean release)
{
   Point ep = evt.getPoint();
   Rectangle r = new Rectangle();
   if (ep == null || dp == null) return;

   ep = scalePoint(ep);
   dp = scalePoint(dp);

   r.setSize(Math.abs(ep.x - dp.x), Math.abs(ep.y - dp.y));
   r.setLocation(Math.min(ep.x,dp.x),Math.min(ep.y,dp.y));

   if (!release) {
      drag_frame.setDragBounds(r);
    }
   else {
      drag_frame.clear();
      if (!evt.isShiftDown() && !evt.isControlDown()) graph_model.deselectAll();
      graph_model.selectInBox(r);
      issueCommandDone();
    }

   localUpdate();
}




private void handleArcCreate(MouseEvent evt,Point dp,boolean release)
{
   Point ep = evt.getPoint();
   PetalNode to = findNode(ep);
   Point p1 = (to != null ? getComponentPoint(to,ep) : null);

   if (!release) {
      graph_model.handleMouseOver(to,null,p1);
      drag_frame.setArcCreate(dp,ep);
      graph_model.handleArcEndPoint(to,p1,PetalModel.ARC_MODE_MIDDLE,evt);
    }
   else {
      Point p = next_position;
      boolean ok = true;

      if (to != null) {
	 if (!graph_model.handleArcEndPoint(to,p1,PetalModel.ARC_MODE_END,evt)) ok = false;
       }

      PetalNode frm = findNode(dp);
      drag_frame.clear();
      if (to != frm) {
	 if (to == null) {
	    next_position = ep.getLocation();
	    next_position = scalePoint(next_position);
	    position_center = true;
	  }
	 else if (frm == null) {
	    next_position = dp.getLocation();
	    next_position = scalePoint(next_position);
	    position_center = true;
	  }
       }
      if (ok) graph_model.createArc(frm,to);

      next_position = p;
      position_center = false;
      issueCommandDone();
    }

   localUpdate();
}



private int gridCoord(int x)
{
   if (grid_size <= 0) return x;

   x = ((x + grid_size/2)/grid_size)*grid_size;

   return x;
}




/********************************************************************************/
/*                                                                              */
/*      Methods for handling ZOOM                                               */
/*                                                                              */
/********************************************************************************/

private void zoom(int amt) 
{
   for (int i = 0; i < Math.abs(amt); ++i) {
      if (amt < 0) user_scale /= SCALE_FACTOR;
      else user_scale *= SCALE_FACTOR;
    }
   if (Math.abs(user_scale - 1.0) < 0.001) user_scale = 1;
   if (user_scale < 1/128.0) user_scale = 1/128.0;
   // if (user_scale > 2048) user_scale = 2048;
   
   double sf = getScaleFactor();
   if (sf * user_scale / prior_scale > 2) {
      user_scale = 2 * prior_scale / sf;
    }
   sf = sf * user_scale / prior_scale;
   setScaleFactor(sf);
   prior_scale = user_scale;
   repaint();
}



private class Wheeler implements MouseWheelListener {
   
   
   @Override public void mouseWheelMoved(MouseWheelEvent e) {
      int mods = e.getModifiersEx();
      if ((mods & (InputEvent.CTRL_DOWN_MASK|InputEvent.META_DOWN_MASK)) == 0) 
         return;
      
      int ct = e.getWheelRotation();
      zoom(ct);
      e.consume();
    }
   
}	// end of inner class Wheeler




/********************************************************************************/
/*										*/
/*	Property change event methods						*/
/*										*/
/********************************************************************************/

@Override public void propertyChange(PropertyChangeEvent evt)
{
}



/********************************************************************************/
/*										*/
/*	Component change event methods						*/
/*										*/
/********************************************************************************/

@Override public void componentHidden(ComponentEvent evt)		{ }


@Override public void componentMoved(ComponentEvent evt)			{ }


@Override public void componentShown(ComponentEvent evt)			{ }


@Override public void componentResized(ComponentEvent evt)
{
  setSizes();
}



private synchronized void setSizes()
{
   Dimension size = getSize();

   if (editor_size == null) {
      editor_size = size.getSize();
    }

   Dimension psz = getPreferredSize();
   Dimension esz = editor_size;

   if (!psz.equals(size)) {
      setSize(psz);
      invalidate();
      repaint();
      // Component rt = this;
      // while (rt.getParent() != null) rt = rt.getParent();
      // rt.validate();
    }

   if (tab_frame != null && !tab_frame.getSize().equals(esz)) tab_frame.setSize(esz);
   if (drag_frame != null && !drag_frame.getSize().equals(esz)) drag_frame.setSize(esz);
   if (arc_frame != null && !arc_frame.getSize().equals(esz)) arc_frame.setSize(esz);
   if (background_frame != null && !background_frame.getSize().equals(esz))
      background_frame.setSize(esz);

   if (next_position != null) {
      if (next_position.x > size.width - MIN_NODE_SIZE ||
	     next_position.y > size.height - MIN_NODE_SIZE) {
	next_position = null;
       }
    }
}



/********************************************************************************/
/*										*/
/*	Scrollable interface methods						*/
/*										*/
/********************************************************************************/

@Override public Dimension getPreferredSize()
{
   if (editor_size != null) {
      if (scale_by == 1) return editor_size;
      return new Dimension((int)(editor_size.width * scale_by),
			      (int)(editor_size.height * scale_by));
    }

   return new Dimension(10,10);
}




@Override public Dimension getPreferredScrollableViewportSize()
{
   return getPreferredSize();
}



@Override public int getScrollableUnitIncrement(Rectangle vr,int o,int d)
{
   return 1;
}


@Override public int getScrollableBlockIncrement(Rectangle vr,int o,int d)
{
   return 20;
}



@Override public boolean getScrollableTracksViewportWidth()
{
   return false;
}



@Override public boolean getScrollableTracksViewportHeight()
{
   return false;
}




/********************************************************************************/
/*										*/
/*	Painting methods							*/
/*										*/
/********************************************************************************/

@Override public void paint(Graphics g)
{
   if (scale_by != 1.0 && g instanceof Graphics2D) {
      Graphics2D g1 = (Graphics2D) g.create();
      g1.scale(scale_by,scale_by);
      super.paint(g1);
    }
   else {
      super.paint(g);
    }
}




/********************************************************************************/
/*										*/
/*	Component command event methods 					*/
/*										*/
/********************************************************************************/

@Override public void commandEndNotify()
{
   issueCommandDone();

   localUpdate();
}



private void handleUndo()
{
   undo_support.undo();
}



private void handleRedo()
{
   undo_support.redo();
}



private void issueCommandDone()
{
   for (PetalEditorCallback cb : editor_callbacks) {
      cb.commandDoneCallback();
    }
}



/********************************************************************************/
/*										*/
/*	Component mouse event methods						*/
/*										*/
/********************************************************************************/

private class Mouser extends MouseInputAdapter {

   private Point down_point;
   private int move_count;
   private boolean doing_popup;

   Mouser() {
      down_point = null;
      move_count = -1;
      doing_popup = false;
    }

   @Override public void mouseClicked(MouseEvent evt) {
      if (!doing_popup && move_count == 0) handleClick(evt);
      doing_popup = false;
    }

   @Override public void mousePressed(MouseEvent evt) {
      grabFocus();
      down_point = evt.getPoint();
      move_count = 0;
      if (evt.isPopupTrigger()) {
	 PetalNode pn = findNode(down_point);
	 PetalArc pa = null;
	 if (pn == null) pa = findArc(down_point);
	 if (graph_model.handlePopupRequest(pn,pa,evt)) doing_popup = true;
       }
    }

   @Override public void mouseDragged(MouseEvent evt) {
      if (doing_popup) return;
      move_count = handleDrag(evt,down_point,move_count,false);
    }

   @Override public void mouseReleased(MouseEvent evt) {
      if (doing_popup) doing_popup = false;
      else if (move_count != 0) handleDrag(evt,down_point,move_count,true);
      else if (evt.isPopupTrigger()) {
	 PetalNode pn = findNode(down_point);
	 PetalArc pa = null;
	 if (pn == null) pa = findArc(down_point);
	 graph_model.handlePopupRequest(pn,pa,evt);
       }
    }

   @Override public void mouseMoved(MouseEvent evt) {
      handleMove(evt);
    }
}	// end of subclass Mouser



/********************************************************************************/
/*										*/
/*	Class for handling key events						*/
/*										*/
/********************************************************************************/

@Override public boolean isFocusable()			{ return true; }


private class Keyer extends KeyAdapter {

   @Override public void keyPressed(KeyEvent e) {
      switch (e.getKeyCode()) {
	 case KeyEvent.VK_CUT :
	 case KeyEvent.VK_DELETE :
	 case KeyEvent.VK_BACK_SPACE :
	    commandCut();
	    break;
	 case KeyEvent.VK_UNDO :
	    commandUndo();
	    break;
	 case KeyEvent.VK_AGAIN :
	    commandRedo();
	    break;
	 case KeyEvent.VK_COPY :
	    commandCopy();
	    break;
	 case KeyEvent.VK_PASTE :
	    commandPaste();
	    break;
	 default :
	    switch (e.getKeyChar()) {
	       case 21 :				// ^U :: undo
	       case 26 :				// ^Z :: undo
		  commandUndo();
		  break;
	       case 18 :				// ^R :: redo
		  commandRedo();
		  break;
	       case 24 :				// ^X :: cut
		  commandCut();
		  break;
	       case 3 : 				// ^C :: copy
		  commandCopy();
		  break;
	       case 22 :				// ^V :: paste
		  commandPaste();
		  break;
	     }
	    break;
       }
    }

   @Override public void keyTyped(KeyEvent e) {
    }

   @Override public void keyReleased(KeyEvent e) {
    }


}	// end of subclass Keyer



/********************************************************************************/
/*										*/
/*	Classes for locally drawn frames					*/
/*										*/
/********************************************************************************/

private class TabFrame extends JPanel {

   private static final long	   serialVersionUID = 1;

   @Override public void paint(Graphics g) {
      PetalNode [] nodes = graph_model.getSelectedNodes();

      for (int i = 0; i < nodes.length; ++i) {
	 drawNodeHandles(g,nodes[i]);
       }

      PetalArc [] arcs = graph_model.getSelectedArcs();

      for (int i = 0; i < arcs.length; ++i) {
	 drawArcHandles(g,arcs[i]);
       }
    }

   private void drawNodeHandles(Graphics g,PetalNode n) {
      Component c = n.getComponent();
      if (c == null) return;

      Rectangle bnds = c.getBounds();
      g.setColor(Color.red);

      for (int i = 0; i <= 2; ++i) {
	 for (int j = 0; j <= 2; ++j) {
	    if (i == 1 && j == 1) continue;
	    int x = bnds.x + bnds.width * i/2 - HANDLE_SIZE * i/2;
	    int y = bnds.y + bnds.height * j/2 - HANDLE_SIZE * j/2;
	    g.fillRect(x,y,HANDLE_SIZE,HANDLE_SIZE);
	  }
       }
    }

   private void drawArcHandles(Graphics g,PetalArc a) {
      Point [] pts = a.getPoints();
      g.setColor(Color.red);
      for (int i = 0; i < pts.length; ++i) {
	 int x = pts[i].x - HANDLE_SIZE/2;
	 int y = pts[i].y - HANDLE_SIZE/2;
	 g.fillRect(x,y,HANDLE_SIZE,HANDLE_SIZE);
       }
    }


}	// end of subclass TabFrame




private class DragFrame extends JPanel {

   private Rectangle drag_bounds;
   private Point arc_source;
   private Point arc_destination;

   private static final long	   serialVersionUID = 1;

   DragFrame() {
      drag_bounds = null;
      arc_source = null;
      arc_destination = null;
    }

   void setDragBounds(Rectangle bnds) {
      drag_bounds = scaleRectangle(bnds.getBounds());
      super.invalidate();
    }

   void setArcCreate(Point sp,Point dp) {
      arc_source = scalePoint(sp.getLocation());
      arc_destination = scalePoint(dp.getLocation());
      super.invalidate();
    }

   void clear() {
      drag_bounds = null;
      arc_source = null;
      arc_destination = null;
      super.invalidate();
    }

   @Override public void paint(Graphics g) {
      if (drag_bounds != null) {
	 g.setColor(Color.red);
	 g.drawRect(drag_bounds.x,drag_bounds.y,drag_bounds.width,drag_bounds.height);
       }
      else if (arc_source != null) {
	 g.setColor(Color.red);
	 g.drawLine(arc_source.x,arc_source.y,arc_destination.x,arc_destination.y);
       }
    }

}	// end of subclass DragFrame




private class ArcFrame extends JComponent {

   private static final long serialVersionUID = 1;

   ArcFrame() {
      super.setDoubleBuffered(true);
    }

   @Override protected void paintComponent(Graphics g) {
      PetalArc [] arcs = graph_model.getArcs();

      for (int i = 0; i < arcs.length; ++i) {
	 arcs[i].draw(g);
       }
    }

}	// end of subclass ArcFrame




/********************************************************************************/
/*										*/
/*	Methods and subclases for movement commands				*/
/*										*/
/********************************************************************************/

private void handleMoveNode(PetalNode pn,Point loc)
{
   Point oloc = pn.getComponent().getLocation();
   CommandMove cm = new CommandMove();
   cm.moveNode(pn,loc.x-oloc.x,loc.y-oloc.y);
   pn.getComponent().setLocation(loc);
   undo_support.postEdit(cm);
}



private class CommandMove extends AbstractUndoableEdit {

   private HashMap<PetalNode,Point> nodes_moved;
   private static final long serialVersionUID = 1;

   CommandMove()			{ nodes_moved = new HashMap<PetalNode,Point>(); }

   @Override public String getPresentationName() { return "Move Nodes"; }

   @Override public boolean addEdit(UndoableEdit ed) {
      if (ed instanceof CommandMove) {
	 CommandMove cmv = (CommandMove) ed;
	 if (cmv.getModel() == getModel()) {
	    for (PetalNode n : cmv.nodes_moved.keySet()) {
	       Point p = cmv.nodes_moved.get(n);
	       if (p != null) moveNode(n,p.x,p.y);
	     }

	    return true;
	  }
       }

      return false;
    }


   @Override public void redo() throws CannotRedoException {
      super.redo();

      Point nloc = new Point();

      for (PetalNode n : nodes_moved.keySet()) {
	 Point p = nodes_moved.get(n);
	 PetalLink lnk = n.getLink();
	 if (lnk == null) {
	    Component c = n.getComponent();
	    Point loc = c.getLocation();
	    nloc.setLocation(loc.x + p.x, loc.y + p.y);
	    c.setLocation(nloc);
	  }
	 else {
	    lnk.moveLocation(p.x,p.y,n.getLinkArc(),n);
	  }
       }

      localUpdate();
    }



   @Override public void undo() throws CannotUndoException {
      super.undo();

      Point nloc = new Point();

      for (PetalNode n : nodes_moved.keySet()) {
	 Point p = nodes_moved.get(n);
	 PetalLink lnk = n.getLink();
	 if (lnk == null) {
	    Component c = n.getComponent();
	    Point loc = c.getLocation();
	    nloc.setLocation(loc.x - p.x, loc.y - p.y);
	    c.setLocation(nloc);
	  }
	 else {
	    lnk.moveLocation(-p.x,-p.y,n.getLinkArc(),n);
	  }
       }

      localUpdate();
    }



   void moveNode(PetalNode n,int dx,int dy) {
      Point pt = nodes_moved.get(n);
      if (pt == null) nodes_moved.put(n,new Point(dx,dy));
      else pt.translate(dx,dy);
    }

   private PetalModel getModel()			{ return graph_model; }



}	// end of subclass CommandMove




/********************************************************************************/
/*										*/
/*	Command classes for clipboard commands					*/
/*										*/
/********************************************************************************/

private void handleCutCopy(boolean cutfg)
{
   undo_support.beginUpdate();

   PetalClipSet cs = (PetalClipSet) getClipboardData(cutfg);

   if (cs == null) {
      System.err.println("PETAL: Noting to cut/copy");
      undo_support.endUpdate("Empty cut/copy");
      return;
    }

   CutCopyCommand cmd = new CutCopyCommand(true);
   clip_board.setContents(cs,null);

   undo_support.postEdit(cmd);
   if (cutfg) graph_model.deselectAll();

   undo_support.endUpdate(cmd.getPresentationName());

   localUpdate();
}



private Transferable getClipboardData(boolean cutfg)
{
   PetalClipSet cs = new PetalClipSet();
   PetalArc [] arcs = graph_model.getArcs();
   PetalNode [] nds = graph_model.getNodes();

   for (int i = 0; i < arcs.length; ++i) {
      PetalArc pa = arcs[i];
      if (graph_model.isSelected(pa) || graph_model.isSelected(pa.getSource()) ||
	     graph_model.isSelected(pa.getTarget())) {
	 cs.addArc(pa,graph_model.getCopyObject(pa));
	 if (cutfg) graph_model.removeArc(pa);
       }
    }

   for (int i = 0; i < nds.length; ++i) {
      PetalNode pn = nds[i];
      if (graph_model.isSelected(pn)) {
	 cs.addNode(pn,graph_model.getCopyObject(pn));
	 if (cutfg) graph_model.removeNode(pn);
       }
    }

   if (cs.isEmpty()) return null;

   return cs;
}




private class CutCopyCommand extends AbstractUndoableEdit {

   private Transferable old_clipboard;
   private boolean is_cut;
   private static final long serialVersionUID = 1;

   CutCopyCommand(boolean cut) {
      old_clipboard = clip_board.getContents(PetalEditor.this);
      is_cut = cut;
    }

   @Override public String getPresentationName()		{ return (is_cut ? "Cut" : "Copy"); }

   @Override public void redo() throws CannotRedoException {
      super.redo();
      old_clipboard = clip_board.getContents(PetalEditor.this);
      Transferable clipset = getClipboardData(is_cut);
      clip_board.setContents(clipset,null);
      localUpdate();
    }

   @Override public void undo() throws CannotUndoException {
      super.undo();
      if (is_cut) {
	 Transferable clipset = clip_board.getContents(this);
	 if (clipset != null) pasteClipboardData(clipset,true);
       }
      clip_board.setContents(old_clipboard,null);
      localUpdate();
    }

}	// end of subclass CutCopyCommand




/********************************************************************************/
/*										*/
/*	Paste Command methods and classes					*/
/*										*/
/********************************************************************************/

private void handlePaste()
{
  PasteCommand cmd = new PasteCommand();

  Transferable clipset = clip_board.getContents(this);

  undo_support.beginUpdate();

  graph_model.deselectAll();

  pasteClipboardData(clipset,false);

  undo_support.postEdit(cmd);
  undo_support.endUpdate("Paste");

  localUpdate();
}



private void pasteClipboardData(Transferable data,boolean dofg)
{
   if (data == null) return;

   PetalClipSet pcs = null;
   try {
      pcs = (PetalClipSet) data.getTransferData(PetalClipSet.getFlavor());
    }
   catch (UnsupportedFlavorException e) {
      System.err.println("PETAL: Problem with pasting: " + e);
    }
   catch (java.io.IOException e) {
      System.err.println("PETAL: Problem with paste data: " + e);
    }

   if (pcs == null) {
      return;
    }

   for (Iterator<?> it = pcs.getNodeIterator(); it.hasNext(); ) {
      Object cent = it.next();
      PetalNode pn = graph_model.addPasteNode(cent,dofg);
      if (pn != null) {
	 graph_model.select(pn);
	 pcs.setMapping(cent,pn);
       }
    }

   for (Iterator<?> it = pcs.getArcIterator(); it.hasNext(); ) {
      Object crel = it.next();
      PetalArc pa = graph_model.addPasteArc(crel,pcs,dofg);
      if (pa != null) {
	 graph_model.select(pa);
	 pcs.setMapping(crel,pa);
       }
   }

   for (Iterator<?> it = pcs.getNodeIterator(); it.hasNext(); ) {
      Object cent = it.next();
      PetalNode pn = graph_model.addPasteNode(cent,pcs,dofg);
      if (pn != null) {
	 graph_model.select(pn);
	 pcs.setMapping(cent,pn);
       }
    }

}



private class PasteCommand extends AbstractUndoableEdit {

   private Transferable old_clipboard;
   private static final long	   serialVersionUID = 1;

   PasteCommand() {
      old_clipboard = clip_board.getContents(PetalEditor.this);
    }

   @Override public String getPresentationName()		{ return "Paste"; }

   @Override public void redo() throws CannotRedoException {
      super.redo();

      old_clipboard = clip_board.getContents(PetalEditor.this);

      Transferable clipset = clip_board.getContents(this);
      if (clipset != null) pasteClipboardData(clipset,true);
      localUpdate();
    }

   @Override public void undo() throws CannotUndoException {
      super.undo();

      // Transferable clipset = getClipboardData(true);

      clip_board.setContents(old_clipboard,null);
      localUpdate();
    }

}	// end of subclass PasteCommand






/********************************************************************************/
/*										*/
/*	Callbacks for drag and drop						*/
/*										*/
/********************************************************************************/

@Override public void dragEnter(DropTargetDragEvent dtde)
{
}



@Override public void dragExit(DropTargetEvent dte)
{
}


@Override public void dragOver(DropTargetDragEvent dtde)
{
}


@Override public void drop(DropTargetDropEvent dtde)
{
   dtde.getDropAction();
   Transferable t = dtde.getTransferable();
   DataFlavor [] flvrs = t.getTransferDataFlavors();
   Point p = dtde.getLocation();
   PetalNode pn = findNode(p);
   PetalArc pa = findArc(p);
   Point pscale = scalePoint(p);

   for (int i = 0; i < flvrs.length; ++i) {
      try {
	 Object o = t.getTransferData(flvrs[i]);
	 if (graph_model.dropNode(o,pscale,pn,pa)) {
	    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
	    dtde.dropComplete(true);
//	    requestFocusInWindow();		THIS ONLY WORKS in 1.4
	    requestFocus();
	    break;
	  }
       }
      catch (UnsupportedFlavorException e) {
	 System.err.println("PETAL: Problem handling drop: " + e);
       }
      catch (IOException e) {
	 System.err.println("PETAL: Problem handling drop: " + e);
       }
    }
}


@Override public void dropActionChanged(DropTargetDragEvent dtde)
{
}



/********************************************************************************/
/*										*/
/*	Methods for tool tips							*/
/*										*/
/********************************************************************************/

@Override public String getToolTipText(MouseEvent evt)
{
   Point p = evt.getPoint();

   PetalNode pn = findNode(p);
   if (pn != null) {
      Point p1 = getComponentPoint(pn,p);
      return pn.getToolTip(p1);
    }

   PetalArc pa = findArc(p);
   if (pa != null) {
      return pa.getToolTip();
    }

   return null;
}



private Point getComponentPoint(PetalNode pn,Point p)
{
   if (pn == null) return p;

   Component c = pn.getComponent();
   if (c == null) return p;

   Point loc = c.getLocation();

   return new Point(p.x - loc.x, p.y - loc.y);
}



/********************************************************************************/
/*										*/
/*	Methods for resizing							*/
/*										*/
/********************************************************************************/

private boolean checkResize(Point p)
{
   PetalNode [] nds = graph_model.getSelectedNodes();
   if (nds.length != 1) return false;

   Component c = nds[0].getComponent();
   if (c == null) return false;
   Rectangle bnds = c.getBounds();

   p = scalePoint(p);

   for (int i = 0; i <= 2; ++i) {
      for (int j = 0; j <= 2; ++j) {
	 if (i == 1 && j == 1) continue;
	 int x = bnds.x + bnds.width * i/2 - HANDLE_SIZE * i/2;
	 int y = bnds.y + bnds.height * j/2 - HANDLE_SIZE * j/2;
	 int x0 = p.x - x;
	 int y0 = p.y - y;
	 if (x0 >= 0 && x0 < HANDLE_SIZE && y0 >= 0 && y0 < HANDLE_SIZE) {
	    resize_component = nds[0];
	    resize_x = i;
	    resize_y = j;
	    return true;
	  }
       }
    }

   return false;
}



private void handleResize(MouseEvent evt,Point dp,boolean release)
{
   Component c = resize_component.getComponent();
   Dimension minsz = c.getMinimumSize();
   Dimension maxsz = c.getMaximumSize();
   Rectangle bnds = c.getBounds();
   Point p = evt.getPoint();

   dp = scalePoint(dp);
   p = scalePoint(p);

   if (minsz == null) minsz = new Dimension(10,10);

   int x0 = bnds.x;
   int y0 = bnds.y;
   int x1 = bnds.x + bnds.width;
   int y1 = bnds.y + bnds.height;

   if (resize_x == 0) {
      int xsz = dp.x - p.x + bnds.width;
      if (maxsz != null && xsz > maxsz.width) xsz = maxsz.width;
      if (xsz < minsz.width) xsz = minsz.width;
      x0 = x1 - xsz;
    }
   else if (resize_x == 2) {
      int xsz = p.x - dp.x + bnds.width;
      if (maxsz != null && xsz > maxsz.width) xsz = maxsz.width;
      if (xsz < minsz.width) xsz = minsz.width;
      x1 = x0 + xsz;
    }

   if (resize_y == 0) {
      int ysz = dp.y - p.y + bnds.height;
      if (maxsz != null && ysz > maxsz.height) ysz = maxsz.height;
      if (ysz < minsz.height) ysz = minsz.height;
      y0 = y1 - ysz;
    }
   else if (resize_y == 2) {
      int ysz = p.y - dp.y + bnds.height;
      if (maxsz != null && ysz > maxsz.height) ysz = maxsz.height;
      if (ysz < minsz.height) ysz = minsz.height;
      y1 = y0 + ysz;
    }

   Rectangle r = new Rectangle(x0,y0,x1-x0,y1-y0);

   if (!release) {
      drag_frame.setDragBounds(r);
    }
   else {
      drag_frame.clear();
      CommandResize cr = new CommandResize(resize_component,r);
      c.setBounds(r);
      undo_support.postEdit(cr);
      resize_component = null;
      fixupWindowSize();
      fixupWindowPosition();
      setPetalCursor(DEFAULT_CURSOR);
      issueCommandDone();
    }

   localUpdate();
}




private class CommandResize extends AbstractUndoableEdit {

   private PetalNode for_node;
   private Rectangle start_size;
   private Rectangle end_size;
   private static final long	   serialVersionUID = 1;

   CommandResize(PetalNode pn,Rectangle r) {
      for_node = pn;
      start_size = pn.getComponent().getBounds();
      end_size = r;
    }

   @Override public String getPresentationName() { return "Resize Node"; }

   @Override public void redo() throws CannotRedoException {
      super.redo();

      Component c = for_node.getComponent();
      c.setBounds(end_size);
      localUpdate();
    }

   @Override public void undo() throws CannotUndoException {
      super.undo();

      Component c = for_node.getComponent();
      c.setBounds(start_size);
      localUpdate();
    }

}	// end of subclass CommandResize




/********************************************************************************/
/*										*/
/*	Methods for handling splines on arcs					*/
/*										*/
/********************************************************************************/

private void handleSplineArcs(boolean fg)
{
   PetalArc [] arcs = graph_model.getSelectedArcs();
   CommandSplineArcs csa = null;

   if (arcs == null || arcs.length == 0) {
      if (spline_arcs == fg) return;
      csa = new CommandSplineArcs(true,fg);
      spline_arcs = fg;
    }
   else {
      for (int i = 0; i < arcs.length; ++i) {
	 if (arcs[i].getSplineArc() != fg) {
	    if (csa == null) csa = new CommandSplineArcs(false,fg);
	    csa.addArc(arcs[i]);
	    arcs[i].setSplineArc(fg);
	  }
       }
    }

   if (csa != null) {
      undo_support.postEdit(csa);
      localUpdate();
    }
}




private class CommandSplineArcs extends AbstractUndoableEdit {

   private boolean flag_value;
   private boolean set_flag;
   private Vector<PetalArc> arc_set;
   private static final long	   serialVersionUID = 1;

   CommandSplineArcs(boolean set,boolean fg) {
      set_flag = set;
      flag_value = fg;
      arc_set = new Vector<PetalArc>();
    }

   void addArc(PetalArc a)		{ arc_set.add(a); }

   @Override public String getPresentationName() {
      return (flag_value ? "Curve arcs" : "Straight arcs");
    }

   @Override public void redo() throws CannotRedoException {
      super.redo();
      setValues(flag_value);
    }

   @Override public void undo() throws CannotUndoException {
      super.undo();
      setValues(!flag_value);
    }

   private void setValues(boolean fg) {
      if (set_flag) spline_arcs = fg;
      for (PetalArc a : arc_set) {
	 a.setSplineArc(fg);
       }
      localUpdate();
   }

}	// end of subclass CommandSplineArcs







/********************************************************************************/
/*										*/
/*	Methods for handling arc pivots 					*/
/*										*/
/********************************************************************************/

private void handlePivot(MouseEvent evt,Point dp,boolean release)
{
   Point ep = evt.getPoint();
   Point sep = scalePoint(ep);

   boolean valid = pivot_arc.movePivot(pivot_index,sep);

   setPetalCursor(MOVE_CURSOR);

   if (release) {
      if (!valid) {
	 pivot_arc.removePivot(pivot_index);
	 if (pivot_arc.getSplineArc() && spline_without_pivots) {
	    int pct = pivot_arc.getPoints().length;
	    if (pct == 2) pivot_arc.setSplineArc(false);
	  }
	 valid = true;
       }
      else {
	 CommandPivot cr = new CommandPivot(pivot_arc,dp,ep,new_pivot);
	 undo_support.postEdit(cr);
       }
      fixupWindowSize();
      fixupWindowPosition();
      undo_support.endUpdate("Pivot");
      issueCommandDone();
      setPetalCursor(DEFAULT_CURSOR);
    }

   if (valid) localUpdate();
}



private void handleReplacePivots(PetalArc pa,Point [] pvts)
{
   CommandReplacePivots cr = new CommandReplacePivots(pa,pvts);

   Point [] pts = pa.getPoints();
   if (pts != null) {
      for (int i = pts.length-3; i >= 0; --i) pa.removePivot(i);
    }

   if (pvts != null) {
      for (int i = 0; i < pvts.length; ++i) pa.movePivot(i,pvts[i]);
    }

   undo_support.postEdit(cr);
}




private void handleRemovePivots()
{
   PetalArc [] arcs = graph_model.getSelectedArcs();

   if (arcs == null || arcs.length == 0) arcs = graph_model.getArcs();

   undo_support.beginUpdate();
   for (int i = 0; i < arcs.length; ++i) {
      handleReplacePivots(arcs[i],null);
    }
   undo_support.endUpdate("Remove Pivots");

   localUpdate();
}




private class CommandPivot extends AbstractUndoableEdit {

   private PetalArc for_arc;
   private Point start_point;
   private Point end_point;
   private boolean created_pivot;
   private static final long serialVersionUID = 1;

   CommandPivot(PetalArc pa,Point sp,Point ep,boolean newpvt) {
      for_arc = pa;
      start_point = sp;
      end_point = ep;
      created_pivot = newpvt;
    }

   @Override public String getPresentationName() { return "Create/Move Pivot"; }

   @Override public void redo() throws CannotRedoException {
      super.redo();
      fixupPivot(start_point,end_point,false);
    }

   @Override public void undo() throws CannotUndoException {
      super.undo();
      fixupPivot(end_point,start_point,created_pivot);
    }

   private void fixupPivot(Point sp,Point ep,boolean remove) {
      int pidx = for_arc.createPivot(sp);
      if (pidx >= 0) {
	 if (!for_arc.movePivot(pidx,ep) || remove) pivot_arc.removePivot(pidx);
       }
      localUpdate();
    }

}	// end of subclass CommandResize




private class CommandReplacePivots extends AbstractUndoableEdit {

   private PetalArc for_arc;
   private Point [] old_pivots;
   private Point [] new_pivots;
   private static final long serialVersionUID = 1;

   CommandReplacePivots(PetalArc pa,Point [] pvts) {
      for_arc = pa;
      new_pivots = pvts;
      Point [] pts = pa.getPoints();
      if (pts == null || pts.length == 2) old_pivots = null;
      else {
	 old_pivots = new Point [pts.length-2];
	 for (int i = 0; i < old_pivots.length; ++i) old_pivots[i] = pts[i+1];
       }
    }

   @Override public String getPresentationName()	{ return "Fixup Pivots"; }

   @Override public void redo() throws CannotRedoException {
      super.redo();
      fixupPivots(new_pivots);
    }

   @Override public void undo() throws CannotUndoException {
      super.undo();
      fixupPivots(old_pivots);
    }

   private void fixupPivots(Point [] pvts) {
      Point [] pts = for_arc.getPoints();
      for (int i = pts.length-3; i >= 0; --i) for_arc.removePivot(i);
      if (pvts != null) {
	 for (int i = 0; i < pvts.length; ++i) for_arc.movePivot(i,pvts[i]);
       }
      localUpdate();
    }

}	// end of subclass CommandReplacePivots





/********************************************************************************/
/*										*/
/*	Methods to handle layout						*/
/*										*/
/********************************************************************************/

private void handleLayout(PetalLayoutMethod plm)
{
   if (plm != null) {
      undo_support.beginUpdate();
      plm.doLayout(graph_model);
      undo_support.endUpdate("Layout");
    }

   fixupWindowSize();

   invalidate(); // ??? fixupWindowSize does this already

   commandEndNotify();
}



/********************************************************************************/
/*										*/
/*	Methods to handle viewport and size					*/
/*										*/
/********************************************************************************/

private void fixupWindowSize()
{
   Dimension mxsz = getUsedSize();

   mxsz.width += 5;
   mxsz.height += 5;

   if (editor_size == null) editor_size = mxsz.getSize();
   else {
      if (mxsz.width > editor_size.width) editor_size.width = mxsz.width;
      if (mxsz.height > editor_size.height) editor_size.height = mxsz.height;
    }

   if (getParent() instanceof JViewport) {
      JViewport vp = (JViewport) getParent();
      Dimension d1 = vp.getViewSize();
      Dimension d2 = vp.getExtentSize();
      Dimension d3 = getSize();
      if (d1.width < d2.width || d1.height < d2.height) {
	 if (d1.width < d2.width) d1.width = d2.width;
	 if (d1.height < d2.height) d1.height = d2.height;
	 vp.setViewSize(d1);
      }
      if (d3.width < d2.width || d3.height < d2.height) {
	 if (d3.width < d2.width) d3.width = d2.width;
	 if (d3.height < d2.height) d3.height = d2.height;
	 setSize(d3);
      }

      if (editor_size.width < d2.width) editor_size.width = d2.width;
      if (editor_size.height < d2.height) editor_size.height = d2.height;
   }

   setSizes();
}



private void fixupWindowPosition()
{
   if (graph_model.getNumSelections() == 0) return;

   JViewport vp = getViewport();
   if (vp == null) return;

   PetalNode [] nds = graph_model.getSelectedNodes();
   PetalArc [] acs = graph_model.getSelectedArcs();

   Rectangle rb = new Rectangle();
   Rectangle rs = null;

   for (int i = 0; i < nds.length; ++i) {
      Component c = nds[i].getComponent();
      c.getBounds(rb);
      if (rs == null) rs = new Rectangle(rb);
      else rs.add(rb);
    }

   for (int i = 0; i < acs.length; ++i) {
      Point [] pts = acs[i].getPoints();
      for (int j = 0; j < pts.length; ++j) {
	 if (rs == null) rs = new Rectangle(pts[j]);
	 else rs.add(pts[j]);
       }
    }

   if (rs != null) {
      vp.scrollRectToVisible(rs);
    }
}




private JViewport getViewport()
{
   Component c = getParent();
   while (c != null) {
      if (c instanceof JViewport) return (JViewport) c;
      else if (c instanceof JScrollPane) {
	 JScrollPane sp = (JScrollPane) c;
	 return sp.getViewport();
       }
      else if (c instanceof Container) {
	 Container cc = (Container) c;
	 if (cc.getComponentCount() != 1) break;
	 c = cc.getParent();
       }
      else break;
    }

   return null;
}




private Dimension getUsedSize()
{
   Dimension mxsz = new Dimension(0,0);
   PetalNode [] nodes = graph_model.getNodes();
   PetalArc [] arcs = graph_model.getArcs();

   for (int i = 0; i < nodes.length; ++i) {
      Component c = nodes[i].getComponent();
      if (c != null) {
	 Rectangle r = c.getBounds();
	 if (r.x + r.width > mxsz.width) mxsz.width = r.x + r.width;
	 if (r.y + r.height > mxsz.height) mxsz.height = r.y + r.height;
       }
    }
   for (int i = 0; i < arcs.length; ++i) {
      Point [] pts = arcs[i].getPoints();
      if (pts != null) {
	 for (int j = 0; j < pts.length; ++j) {
            if (pts[j] == null) continue;
	    if (pts[j].x > mxsz.width) mxsz.width = pts[j].x;
	    if (pts[j].y > mxsz.height) mxsz.height = pts[j].y;
	  }
       }
    }

   return mxsz;
}



/********************************************************************************/
/*										*/
/*	Methods for printing							*/
/*										*/
/********************************************************************************/

private void handlePrint(String file)
{
   PrinterJob pjob = PrinterJob.getPrinterJob();
   PageFormat fmt = pjob.pageDialog(pjob.defaultPage());
   pjob.setPrintable(this,fmt);
   if (pjob.printDialog()) {
      try {
	 pjob.print();
       }
      catch (PrinterException ex) {
	 System.err.println("PETAL: Printing Problem: " + ex);
       }
    }
}



@Override public int print(Graphics g,PageFormat fmt,int idx)
{
   if (idx > 0) return Printable.NO_SUCH_PAGE;

   Component c = this;
   while (!(c instanceof Frame)) {
      c = c.getParent();
    }

   Dimension d1 = super.getSize();
   d1.width /= scale_by;
   d1.height /= scale_by;

   Dimension d = new Dimension((int) fmt.getWidth(),(int) fmt.getHeight());
   int res = 72;
   Dimension d0 = new Dimension(d);

   int margin = (int)(res * 0.5);
   d.width -= 2*margin;
   d.height -= 2*margin;

   Color cl = g.getColor();
   g.setColor(Color.WHITE);
   g.fillRect(0,0,d0.width,d0.height);
   g.setColor(cl);
   int w = d1.width;
   int h = d1.height;
   if (w > d.width) w = d.width;
   if (h > d.height) h = d.height;
   g.clipRect(margin,margin,w,h);
   g.translate(margin,margin);
   paint(g);

   return Printable.PAGE_EXISTS;
}



}	// end of class PetalEditor




/* end of PetalEditor.java */


