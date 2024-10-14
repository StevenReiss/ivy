/********************************************************************************/
/*										*/
/*		SwingTreeTable.java						*/
/*										*/
/*	Implementation of tree-table combination				*/
/*										*/
/********************************************************************************/
/*	Copyright 2010 Brown University -- Steven P. Reiss		      */
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

/*
 * The contents of this file are subject to the Sapient Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://carbon.sf.net/License.html.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is The Carbon Component Framework.
 *
 * The Initial Developer of the Original Code is Sapient Corporation
 *
 * Copyright (C) 2003 Sapient Corporation. All Rights Reserved.
 */


/*
 * @(#)JTreeTable.java	  1.2 98/10/27
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */
/**
 * This example shows how to create a simple SwingTreeTable component,
 * by using a JTree as a renderer (and editor) for the cells in a
 * particular column in the JTable.
 *
 * @version 1.2 10/27/98
 *
 * @author Philip Milne
 * @author Scott Violet
 */


package edu.brown.cs.ivy.swing;



import javax.swing.CellEditor;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
// import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;



public class SwingTreeTable extends JTable {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private TreeTableCellRenderer tree_renderer;			// a subclass of JTree as well

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingTreeTable(TreeTableModel treeTableModel) 
{
   super();

   // Create the tree. It will be used as a renderer and editor.
   tree_renderer = new TreeTableCellRenderer(treeTableModel);
   tree_renderer.setOpaque(isOpaque());

   // Install a tableModel representing the visible rows in the tree.
   super.setModel(new TreeTableModelAdapter(treeTableModel, tree_renderer));

   // Force the JTable and JTree to share their row selection models.
   ListToTreeSelectionModelWrapper selectionWrapper = new
      ListToTreeSelectionModelWrapper();
   tree_renderer.setSelectionModel(selectionWrapper);
   setSelectionModel(selectionWrapper.getListSelectionModel());

   // Install the tree editor renderer and editor.
   setDefaultRenderer(TreeTableModel.class, tree_renderer);
   setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());

   // No grid.
   setShowGrid(false);

   // No intercell spacing
   setIntercellSpacing(new Dimension(0, 0));

   // And update the height of the trees row to match that of
   // the table.
   if (tree_renderer.getRowHeight() < 1) {
      // Metal looks better like this.
      setRowHeight(18);
    }
}



/********************************************************************************/
/*										*/
/*	Overridden methods to get proper behavior				*/
/*										*/
/********************************************************************************/

/**
 * Overridden to message super and forward the method to the tree.
 * Since the tree is not actually in the component hieachy it will
 * never receive this unless we forward it in this manner.
 */

@Override public void updateUI()
{
   super.updateUI();
   if (tree_renderer != null) {
      tree_renderer.updateUI();
    }
   // Use the tree's default foreground and background colors in the table
   LookAndFeel.installColorsAndFont(this, "Tree.background",
				       "Tree.foreground", "Tree.font");
}



/* Workaround for BasicTableUI anomaly. Make sure the UI never tries to
 * paint the editor. The UI currently uses different techniques to
 * paint the renderers and editors and overriding setBounds() below
 * is not the right thing to do for an editor. Returning -1 for the
 * editing row in this case, ensures the editor is never painted.
 */

@Override public int getEditingRow()
{
   return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;
}



/**
 * Overridden to pass the new rowHeight to the tree.
 */

@Override public void setRowHeight(int rowheight)
{
   super.setRowHeight(rowheight);
   if (tree_renderer != null && tree_renderer.getRowHeight() != rowheight) {
      tree_renderer.setRowHeight(getRowHeight());
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

/**
 * Returns the tree that is being shared between the model.
 */

public SwingTree getTree()
{
   return tree_renderer;
}



@Override public void setOpaque(boolean fg)
{
   super.setOpaque(fg);
   if (tree_renderer != null) tree_renderer.setOpaque(fg);
}



public void addTreeExpansionListener(TreeExpansionListener tel)
{
   getTree().addTreeExpansionListener(tel);
}


public void removeTreeExpansionListener(TreeExpansionListener tel)
{
   getTree().removeTreeExpansionListener(tel);
}


@Override public void setSelectionMode(int type) {
   super.setSelectionMode(type);
   int tmode = 0;
   switch (type) {
      case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
	 tmode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;
	 break;
      case ListSelectionModel.SINGLE_INTERVAL_SELECTION :
	 tmode = TreeSelectionModel.SINGLE_TREE_SELECTION;
	 break;
      case ListSelectionModel.SINGLE_SELECTION :
	 tmode = TreeSelectionModel.SINGLE_TREE_SELECTION;
	 break;
    }
   tree_renderer.getSelectionModel().setSelectionMode(tmode);
}



/********************************************************************************/
/*										*/
/*	Tree cell renderer for Tree Tables					*/
/*										*/
/********************************************************************************/

private class TreeTableCellRenderer extends SwingTree implements TableCellRenderer {

   /** Last table/tree row asked to renderer. */
   protected int visible_row;
   private static final long serialVersionUID = 1;

   TreeTableCellRenderer(TreeModel model) {
      super(model);
    }

   /**
    * updateUI is overridden to set the colors of the Tree's renderer
    * to match that of the table.
    */
   @Override public void updateUI() {
      super.updateUI();
      // Make the tree's cell renderer use the table's cell selection colors
      TreeCellRenderer tcr = getCellRenderer();
      if (tcr instanceof DefaultTreeCellRenderer) {
	 DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
	 // For 1.1 uncomment this, 1.2 has a bug that will cause an
	 // exception to be thrown if the border selection color is
	 // null.
	 // dtcr.setBorderSelectionColor(null);
	 dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
	 dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
       }
    }

   /**
    * Sets the row height of the tree, and forwards the row height to
    * the table.
    */
   @Override public void setRowHeight(int rowheight) {
      if (rowheight > 0) {
	 super.setRowHeight(rowheight);
	 if (SwingTreeTable.this != null &&
		SwingTreeTable.this.getRowHeight() != rowheight) {
	    SwingTreeTable.this.setRowHeight(getRowHeight());
	  }
       }
    }

   /**
    * This is overridden to set the height to match that of the JTable.
    */
   @Override public void setBounds(int x, int y, int w, int h) {
      super.setBounds(x, 0, w, SwingTreeTable.this.getHeight());
    }

   /**
    * Sublcassed to translate the graphics such that the last visible
    * row will be drawn at 0,0.
    */
   @Override public void paint(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      double ht = -visible_row * getRowHeight();
      g2.translate(0,ht);
//    System.err.println("PAINT TREE " + visibleRow + " " + g2.getTransform());
//    System.err.println("   " +  g2.getClip() + " " + ((Graphics2D) g).getTransform());
      super.paint(g2);
    }

   /**
    * TreeCellRenderer method. Overridden to update the visible row.
    */
   @Override public Component getTableCellRendererComponent(JTable table,
        					     Object value,
        					     boolean isSelected,
        					     boolean hasFocus,
        					     int row, int column) {
      if (isSelected) setBackground(table.getSelectionBackground());
      else setBackground(table.getBackground());
      visible_row = row;
//    System.err.println("PERF TABLE CELL " + row + " " + value);
      return this;
    }

}	// end of inner class TreeTableCellRenderer




/********************************************************************************/
/*										*/
/*	Cell editor Implementation						*/
/*										*/
/********************************************************************************/

/**
 * TreeTableCellEditor implementation. Component returned is the
 * JTree.
 */

private final class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {

   @Override public Component getTableCellEditorComponent(JTable table,
						   Object value,
						   boolean isSelected,
						   int r, int c) {
      return tree_renderer;
    }

   /**
    * Overridden to return false, and if the event is a mouse event
    * it is forwarded to the tree.<p>
    * The behavior for this is debatable, and should really be offered
    * as a property. By returning false, all keyboard actions are
    * implemented in terms of the table. By returning true, the
    * tree would get a chance to do something with the keyboard
    * events. For the most part this is ok. But for certain keys,
    * such as left/right, the tree will expand/collapse where as
    * the table focus should really move to a different column. Page
    * up/down should also be implemented in terms of the table.
    * By returning false this also has the added benefit that clicking
    * outside of the bounds of the tree node, but still in the tree
    * column will select the row, whereas if this returned true
    * that wouldn't be the case.
    * <p>By returning false we are also enforcing the policy that
    * the tree will never be editable (at least by a key sequence).
    */
   @Override public boolean isCellEditable(EventObject e) {
      if (e instanceof MouseEvent) {
         for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
            if (getColumnClass(counter) == TreeTableModel.class) {
               MouseEvent me = (MouseEvent) e;
               MouseEvent newME = new MouseEvent(tree_renderer, me.getID(),
        					    me.getWhen(), me.getModifiersEx(),
        					    me.getX() - getCellRect(0, counter, true).x,
        					    me.getY(), me.getClickCount(),
        					    me.isPopupTrigger(),me.getButton());
               tree_renderer.dispatchEvent(newME);
               break;
             }
          }
       }
      return false;
    }

   @Override public Object getCellEditorValue() {
      return null;
    }

}	// end of inner class TreeTableCellEditor




/********************************************************************************/
/*										*/
/*	Selection model implementation						*/
/*										*/
/********************************************************************************/

/**
 * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
 * to listen for changes in the ListSelectionModel it maintains. Once
 * a change in the ListSelectionModel happens, the paths are updated
 * in the DefaultTreeSelectionModel.
 */

private class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel {

   /** Set to true when we are updating the ListSelectionModel. */
   protected boolean	     updating_list_selection_model;
   private static final long serialVersionUID = 1;

   ListToTreeSelectionModelWrapper() {
      super();
      getListSelectionModel().addListSelectionListener(createListSelectionListener());
    }

   /**
    * Returns the list selection model. ListToTreeSelectionModelWrapper
    * listens for changes to this model and updates the selected paths
    * accordingly.
    */
   ListSelectionModel getListSelectionModel() {
      return listSelectionModel;
    }

   /**
    * This is overridden to set <code>updatingListSelectionModel</code>
    * and message super. This is the only place DefaultTreeSelectionModel
    * alters the ListSelectionModel.
    */
   @Override public void resetRowSelection() {
      if (!updating_list_selection_model) {
	 updating_list_selection_model = true;
	 try {
	    super.resetRowSelection();
	  }
	 finally {
	    updating_list_selection_model = false;
	  }
       }
      // Notice how we don't message super if
      // updatingListSelectionModel is true. If
      // updatingListSelectionModel is true, it implies the
      // ListSelectionModel has already been updated and the
      // paths are the only thing that needs to be updated.
    }

   /**
    * Creates and returns an instance of ListSelectionHandler.
    */
   protected ListSelectionListener createListSelectionListener() {
      return new ListSelectionHandler();
    }

   /**
    * If <code>updatingListSelectionModel</code> is false, this will
    * reset the selected paths from the selected rows in the list
    * selection model.
    */
   protected void updateSelectedPathsFromSelectedRows() {
      if (!updating_list_selection_model) {
	 updating_list_selection_model = true;
	 try {
	    // This is way expensive, ListSelectionModel needs an
	    // enumerator for iterating.
	    int        min = listSelectionModel.getMinSelectionIndex();
	    int        max = listSelectionModel.getMaxSelectionIndex();

	    clearSelection();
	    if (min != -1 && max != -1) {
	       for (int counter = min; counter <= max; counter++) {
		  if (listSelectionModel.isSelectedIndex(counter)) {
		     TreePath	  selPath = tree_renderer.getPathForRow(counter);

		     if (selPath != null) {
			addSelectionPath(selPath);
		      }
		   }
		}
	     }
	  }
	 finally {
	    updating_list_selection_model = false;
	  }
       }
    }

   /**
    * Class responsible for calling updateSelectedPathsFromSelectedRows
    * when the selection of the list changes.
    */
   private final class ListSelectionHandler implements ListSelectionListener {
      @Override public void valueChanged(ListSelectionEvent e) {
	 updateSelectedPathsFromSelectedRows();
       }
    }	// end of inner inner class ListSelecitonHandler

}	// end of inner class ListToTreeSelectionModelWrapper




/********************************************************************************/
/*										*/
/*	Interface for our tree model						*/
/*										*/
/********************************************************************************/

public interface TreeTableModel extends TreeModel
{
    /**
     * Returns the number of available columns.
     */
    int getColumnCount();

    /**
     * Returns the name for column number <code>column</code>.
     */
    String getColumnName(int column);

    /**
     * Returns the type for column number <code>column</code>.
     */
    Class<?> getColumnClass(int column);

    /**
     * Returns the value to be displayed for node <code>node</code>,
     * at column number <code>column</code>.
     */
    Object getValueAt(Object node, int column);

    /**
     * Indicates whether the the value for node <code>node</code>,
     * at column number <code>column</code> is editable.
     */
    boolean isCellEditable(Object node, int column);

    /**
     * Sets the value for node <code>node</code>,
     * at column number <code>column</code>.
     */
    void setValueAt(Object aValue, Object node, int column);

}	// end of interface SwingTreeTableModel




/********************************************************************************/
/*										*/
/*	Abstract model for easier usage 					*/
/*										*/
/********************************************************************************/

public abstract static class AbstractTreeTableModel implements TreeTableModel
{
   protected Object model_root;
   protected EventListenerList listener_list = new EventListenerList();

   public AbstractTreeTableModel(Object root) {
      this.model_root = root;
    }


   @Override public Object getRoot()			{ return model_root; }
   @Override public boolean isLeaf(Object node) 	{ return getChildCount(node) == 0; }

   @Override public void valueForPathChanged(TreePath path, Object newValue) {}


   // This is not called in the JTree's default mode: use a naive implementation.
   @Override public int getIndexOfChild(Object parent, Object child) {
      for (int i = 0; i < getChildCount(parent); i++) {
	 if (getChild(parent, i).equals(child)) {
	    return i;
	  }
       }
      return -1;
    }


   @Override public void addTreeModelListener(TreeModelListener l) {
      listener_list.add(TreeModelListener.class, l);
    }

   @Override public void removeTreeModelListener(TreeModelListener l) {
      listener_list.remove(TreeModelListener.class, l);
    }

   protected void fireTreeNodesChanged(Object source, Object[] path,
					  int[] childIndices,
					  Object[] children) {
      // Guaranteed to return a non-null array
      Object[] listeners = listener_list.getListenerList();
      TreeModelEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
	 if (listeners[i]==TreeModelListener.class) {
	    // Lazily create the event:
	    if (e == null)
	       e = new TreeModelEvent(source, path, childIndices, children);
	    ((TreeModelListener) listeners[i+1]).treeNodesChanged(e);
	  }
       }
    }


   protected void fireTreeNodesInserted(Object source, Object[] path,
					   int[] childIndices,
					   Object[] children) {
      // Guaranteed to return a non-null array
      Object[] listeners = listener_list.getListenerList();
      TreeModelEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
	 if (listeners[i]==TreeModelListener.class) {
	    // Lazily create the event:
	    if (e == null)
	       e = new TreeModelEvent(source, path, childIndices, children);
	    ((TreeModelListener) listeners[i+1]).treeNodesInserted(e);
	  }
       }
    }

   protected void fireTreeNodesRemoved(Object source, Object[] path,
					  int[] childIndices,
					  Object[] children) {
      // Guaranteed to return a non-null array
      Object[] listeners = listener_list.getListenerList();
      TreeModelEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
	 if (listeners[i]==TreeModelListener.class) {
	    // Lazily create the event:
	    if (e == null)
	       e = new TreeModelEvent(source, path, childIndices, children);
	    ((TreeModelListener) listeners[i+1]).treeNodesRemoved(e);
	  }
       }
    }

   protected void fireTreeStructureChanged(Object source, Object[] path,
        					      int[] childIndices,
        				      Object[] children) {
      // Guaranteed to return a non-null array
      Object[] listeners = listener_list.getListenerList();
      TreeModelEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
         if (listeners[i]==TreeModelListener.class) {
            // Lazily create the event:
            if (e == null)
               e = new TreeModelEvent(source, path, childIndices, children);
            try {
               ((TreeModelListener) listeners[i+1]).treeStructureChanged(e);
             } 
            catch (Throwable t) { }
          }
       }
    }

   // TreeTableModel interace implementations
   
   @Override public Class<?> getColumnClass(int column) {
      if (column == 0) return SwingTreeTable.TreeTableModel.class;
      return Object.class;
    }

   @Override public boolean isCellEditable(Object node, int column) {
      return getColumnClass(column) == TreeTableModel.class;
    }

   @Override public void setValueAt(Object aValue, Object node, int column) { }


   @Override public abstract Object getChild(Object par,int index);
   @Override public abstract int getChildCount(Object par);
   @Override public abstract int getColumnCount();
   @Override public abstract String getColumnName(int col);
   @Override public abstract Object getValueAt(Object node,int col);
   
}	// end of abstract inner class AbstractTreeTableModel





/********************************************************************************/
/*										*/
/*	Abstract cell editor implementation					*/
/*										*/
/********************************************************************************/

public static class AbstractCellEditor implements CellEditor {

    protected EventListenerList listener_list = new EventListenerList();

    @Override public Object getCellEditorValue()				{ return null; }
    @Override public boolean isCellEditable(EventObject e)		{ return true; }
    @Override public boolean shouldSelectCell(EventObject anEvent)	{ return false; }
    @Override public boolean stopCellEditing()				{ return true; }
    @Override public void cancelCellEditing()				{ }

    @Override public void addCellEditorListener(CellEditorListener l) {
       listener_list.add(CellEditorListener.class, l);
     }

    @Override public void removeCellEditorListener(CellEditorListener l) {
       listener_list.remove(CellEditorListener.class, l);
     }

    protected void fireEditingStopped() {
       // Guaranteed to return a non-null array
       Object[] listeners = listener_list.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length-2; i>=0; i-=2) {
	  if (listeners[i]==CellEditorListener.class) {
	     ((CellEditorListener) listeners[i+1]).editingStopped(new ChangeEvent(this));
	   }
	}
     }

    protected void fireEditingCanceled() {
       // Guaranteed to return a non-null array
       Object[] listeners = listener_list.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length-2; i>=0; i-=2) {
	  if (listeners[i]==CellEditorListener.class) {
	     ((CellEditorListener) listeners[i+1]).editingCanceled(new ChangeEvent(this));
	   }
	}
     }

}	// end of inner class AbstractCellEditor


	

/********************************************************************************/
/*										*/
/*	Class to map a tree table model to a table model			*/
/*										*/
/********************************************************************************/

private static class TreeTableModelAdapter extends AbstractTableModel
{
   private JTree for_tree;
   private transient TreeTableModel tree_table_model;
   private transient Map<Integer,Object> row_map;
   private int row_count;

   private static final long serialVersionUID = 1;

   TreeTableModelAdapter(TreeTableModel treetablemodel, JTree tree) {
      for_tree = tree;
      tree_table_model = treetablemodel;
      TreeTableModelListener listener = new TreeTableModelListener(this);
   
      // Install a tree and tree model listeners that can update the table when
      // tree changes. We use delayedFireTableDataChanged as we can
      // not be guaranteed the tree will have finished processing
      // the event before us.
      tree.addTreeExpansionListener(listener);
      treetablemodel.addTreeModelListener(listener);
    }

   // Wrappers, implementing TableModel interface.

   @Override public int getColumnCount() {
      return tree_table_model.getColumnCount();
    }

   @Override public String getColumnName(int column) {
      return tree_table_model.getColumnName(column);
    }

   @Override public Class<?> getColumnClass(int column) {
      return tree_table_model.getColumnClass(column);
    }

   @Override public int getRowCount() {
      if (row_map == null) nodeForRow(0);
      if (for_tree.getRowCount() != row_count) {
         boolean fg = for_tree.isLargeModel();
         for_tree.setLargeModel(!fg);
         for_tree.setLargeModel(fg);
//       BasicTreeUI tui = (BasicTreeUI) for_tree.getUI();
//       System.err.println("DIRVERGENT ROW COUNT " + for_tree.getRowCount() + " " + row_count);
         return Math.min(for_tree.getRowCount(),row_count);
       }
      return row_count;
//    return for_tree.getRowCount();
    }

   protected Object nodeForRow(int row) {
      Object root = tree_table_model.getRoot();
      int start = for_tree.isRootVisible() ? 0 : -1;
      Map<Integer,Object> usemap = row_map;
      if (usemap == null) {
         usemap = new HashMap<>();
         row_count = exploreTree(root,start,usemap);
         row_map = usemap;
       }
      Object rslt = usemap.get(row);
      
      TreePath treePath = for_tree.getPathForRow(row);
      if (treePath == null) return null;
      Object val = treePath.getLastPathComponent();
      if (!val.equals(rslt)) {
//       System.err.println("DIVERGENT NODE FOR ROW " + row + " " + val + " " + rslt);
       }
//    return val;
      
      return rslt;
      
    }
   
   private int exploreTree(Object node,int row,Map<Integer,Object> rslt) {
      boolean expand = for_tree.isExpanded(row);
      if (row >= 0) rslt.put(row,node);
      
//    TreePath tp = for_tree.getPathForRow(row);
//    System.err.println("NODE AT " + row + " = " + node + " " + expand + " " + for_tree.isCollapsed(0) + " " +
//          for_tree.isVisible(tp));
      ++row;
      if (expand) {
         for (int i = 0; i < tree_table_model.getChildCount(node); ++i) {
            Object child = tree_table_model.getChild(node,i);
            row = exploreTree(child,row,rslt);
          }
       }
      
      return row;
    }
   
   void invalidateCache()                       { row_map = null; }
   
   @Override public void fireTableDataChanged() {
      invalidateCache();
      super.fireTableDataChanged();
    }

   @Override public Object getValueAt(int row, int column) {
      Object rslt =  tree_table_model.getValueAt(nodeForRow(row), column);
      return rslt;
    }

   @Override public boolean isCellEditable(int row, int column) {
      return tree_table_model.isCellEditable(nodeForRow(row), column);
    }

   @Override public void setValueAt(Object value, int row, int column) {
      tree_table_model.setValueAt(value, nodeForRow(row), column);
    }

}	// end of inner class TreeTableModelAdapter


private static class TreeTableModelListener implements TreeExpansionListener, TreeModelListener {

   private TreeTableModelAdapter for_adapter;
   
   TreeTableModelListener(TreeTableModelAdapter mdl) {
      for_adapter = mdl;
    }
   
   @Override public void treeExpanded(TreeExpansionEvent event) {
//    System.err.println("TREE EXPANDED " + event.getPath());
      fireTableDataChanged();
    }
   @Override public void treeCollapsed(TreeExpansionEvent event) {
      fireTableDataChanged();
    }

   @Override public void treeNodesChanged(TreeModelEvent e) {
      delayedFireTableDataChanged();
    }
   
   @Override public void treeNodesInserted(TreeModelEvent e) {
      delayedFireTableDataChanged();
    }
   
   @Override public void treeNodesRemoved(TreeModelEvent e) {
      delayedFireTableDataChanged();
    }
   
   @Override public void treeStructureChanged(TreeModelEvent e) {
      delayedFireTableDataChanged();
    }  
   
   private void fireTableDataChanged() {
      if (SwingUtilities.isEventDispatchThread()) {
         for_adapter.fireTableDataChanged();
       }
      else {
         delayedFireTableDataChanged();
       }
    }
   
   private void delayedFireTableDataChanged() {
      for_adapter.invalidateCache();
      SwingUtilities.invokeLater(
            new Runnable() {
               @Override public void run() {
                  for_adapter.fireTableDataChanged();
                }
             });
    }
}




}	// end of class SwingTreeTable




/* end of SwingTreeTable.java */
