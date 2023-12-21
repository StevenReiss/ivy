/********************************************************************************/
/*										*/
/*		SwingTree.java							*/
/*										*/
/*	Swing generic tree support code 					*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingTree.java,v 1.13 2018/08/02 15:10:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingTree.java,v $
 * Revision 1.13  2018/08/02 15:10:54  spr
 * Fix imports.  Prepare for java 10.
 *
 * Revision 1.12  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.11  2011-05-27 19:32:51  spr
 * Change copyrights.
 *
 * Revision 1.10  2007-08-10 02:11:24  spr
 * Cleanups from eclipse.
 *
 * Revision 1.9  2007-05-04 02:00:37  spr
 * Import fixups.
 *
 * Revision 1.8  2005/06/07 02:18:23  spr
 * Update for java 5.0
 *
 * Revision 1.7  2005/05/07 22:25:44  spr
 * Updates for java 5.0
 *
 * Revision 1.6  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.5  2004/02/26 02:57:10  spr
 * Make methods final that shouldn't be overridden.
 *
 * Revision 1.4  2003/04/03 19:43:14  spr
 * Add colors to tree nodes as an option.
 *
 * Revision 1.3  2003/04/01 18:19:55  spr
 * Fix up file fields; fix selection color in trees.
 *
 * Revision 1.2  2003/03/29 03:41:30  spr
 * Add new button types to grid panel and menu bars; extend trees to support icons.
 *
 * Revision 1.1.1.1  2003/03/17 19:38:24  spr
 * Initial version of the common code for various Brown projects.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;



public class SwingTree extends JTree implements SwingColors
{



/********************************************************************************/
/*										*/
/*	Interface for components						*/
/*										*/
/********************************************************************************/

public interface Node {

   public boolean isUsable();

   public String getToolTipText();

   public Icon getIcon();

   public Color getTextColor(boolean select);
   public Color getBackgroundColor(boolean select);

}	// end of interface Node




/********************************************************************************/
/*										*/
/*	Interface for handling mouse events					*/
/*										*/
/********************************************************************************/

public interface TreeEventListener extends EventListener {

   public void mouseClicked(MouseEvent evt,int row,TreePath path);

}	// end of interface TreeEventListener



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Vector<TreeEventListener> action_listeners;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingTree()
{
   initialize();
}


public SwingTree(Object [] v)
{
   super(v);
   initialize();
}



public SwingTree(Vector<?> v)
{
   super(v);
   initialize();
}



public SwingTree(Hashtable<?, ?> v)
{
   super(v);
   initialize();
}



public SwingTree(TreeModel v)
{
   super(v);
   initialize();
}




private void initialize()
{
   CellRenderer cr = new CellRenderer();
   setCellRenderer(cr);
   setOpaque(true);
// setBackground(SWING_BACKGROUND_COLOR);
   setBackground(MetalLookAndFeel.getMenuBackground());
   ToolTipManager.sharedInstance().registerComponent(this);
   action_listeners = new Vector<TreeEventListener>();
   addMouseListener(new TreeMouser());
}



/********************************************************************************/
/*										*/
/*	Cell renderer subclass							*/
/*										*/
/********************************************************************************/

private static class CellRenderer extends DefaultTreeCellRenderer {

   private static final long serialVersionUID = 1;

   CellRenderer()				{ }


   @Override public Component getTreeCellRendererComponent(JTree t,Object v,boolean sel,
        					    boolean exp,boolean leaf,
        					    int row,boolean foc) {
      super.getTreeCellRendererComponent(t,v,sel,exp,leaf,row,foc);
   
      if (v != null && v instanceof Node) {
         Node n = (Node) v;
         Color bkg = n.getBackgroundColor(sel);
         if (bkg == null) {
            if (sel) bkg = SWING_SELECT_COLOR;
            else bkg = MetalLookAndFeel.getMenuBackground();
          }
         setOpaque(true);
         super.setBackground(bkg);
   
         Color fg = n.getTextColor(sel);
         if (fg == null) {
            if (!n.isUsable()) fg = MetalLookAndFeel.getInactiveControlTextColor();
            else fg = Color.black;
          }
         super.setForeground(fg);
         setToolTipText(n.getToolTipText());
   
         Icon icn = n.getIcon();
         super.setIcon(icn);
       }
   
      return this;
    }

}	// end of subclass CellRenderer




/********************************************************************************/
/*										*/
/*	Mouse handler methods and classes					*/
/*										*/
/********************************************************************************/

public final void addTreeEventListener(TreeEventListener l)
{
   if (l != null) action_listeners.addElement(l);
}


public final void removeTreeEventListener(TreeEventListener l)
{
   if (l != null) action_listeners.removeElement(l);
}




private class TreeMouser extends MouseAdapter {

   @Override public void mousePressed(MouseEvent e) {
      int selrow = getRowForLocation(e.getX(),e.getY());
      if (selrow != -1) {
	 TreePath selpath = getPathForLocation(e.getX(),e.getY());
	 for (Iterator<TreeEventListener> it = action_listeners.iterator(); it.hasNext(); ) {
	    TreeEventListener el = it.next();
	    el.mouseClicked(e,selrow,selpath);
	  }
       }
    }
}	// end of subclass TreeMouser



/********************************************************************************/
/*                                                                              */
/*     Save and restore expansion paths based on names                          */
/*                                                                              */
/********************************************************************************/

public Object saveExpansions()
{
   TreeExpansion expansions = null;
   Object root = getModel().getRoot();
   TreePath rootpath = new TreePath(root);
   expansions = findExpansions(rootpath,expansions);
   
   return expansions;
}


private TreeExpansion findExpansions(TreePath path,TreeExpansion parent)
{
   TreeModel mdl = getModel();
   Object node = path.getLastPathComponent();
   TreeExpansion expansion = parent;
   if (isExpanded(path)) {
      expansion = new TreeExpansion(node);
      if (parent != null) parent.addChild(expansion); 
      int ct = mdl.getChildCount(node);
      for (int i = 0; i < ct; ++i) {
         TreePath cpath = path.pathByAddingChild(mdl.getChild(node,i));
         findExpansions(cpath,expansion);
       }
    }
   
   return expansion;
}


public void restoreExpansions(Object expandobj)
{
   if (expandobj == null || !(expandobj instanceof TreeExpansion)) return;
   TreeExpansion expansion = (TreeExpansion) expandobj;
   Object root = getModel().getRoot();
   TreePath rootpath = new TreePath(root);
   restoreExpansions(rootpath,expansion);
}


private void restoreExpansions(TreePath path,TreeExpansion expansion)
{
   if (expansion == null) return;
   
   if (!isExpanded(path)) {
      expandPath(path);
    }
   
   Object node = path.getLastPathComponent();
   TreeModel mdl = getModel();
   int ct = mdl.getChildCount(node);
   for (int i = 0; i < ct; ++i) {
      Object child = mdl.getChild(node,i);
      TreeExpansion cexpt = expansion.getChild(child);
      if (cexpt != null) {
         TreePath cpath = path.pathByAddingChild(child);
         restoreExpansions(cpath,cexpt);
       }
    }
}


private class TreeExpansion {
   
   private String tree_node;
   private Map<String,TreeExpansion> child_nodes;
   
   TreeExpansion(Object node) {
      tree_node = node.toString();
      child_nodes = null;
    }
   
   void addChild(TreeExpansion exp) {
      if (child_nodes == null) child_nodes = new HashMap<>();
      child_nodes.put(exp.tree_node,exp);
    }
   
   TreeExpansion getChild(Object n) {
       if (child_nodes == null) return null;
       return child_nodes.get(n.toString());
    }
   
}

}	// end of class SwingTree




/* end of SwingTree.java */
