/********************************************************************************/
/*										*/
/*		PebbleEventEditor.java						*/
/*										*/
/*	Editor panel for managing events					*/
/*										*/
/********************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2005, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/pebble/PebbleEventEditor.java,v 1.15 2015/11/20 15:09:22 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PebbleEventEditor.java,v $
 * Revision 1.15  2015/11/20 15:09:22  spr
 * Reformatting.
 *
 * Revision 1.14  2013-06-03 13:03:32  spr
 * Minor fixes
 *
 * Revision 1.13  2011-09-12 20:50:24  spr
 * Code cleanup.
 *
 * Revision 1.12  2007-08-10 02:11:18  spr
 * Cleanups from eclipse.
 *
 * Revision 1.11  2006-12-01 03:22:53  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.10  2006/07/23 02:25:14  spr
 * Add support for action editing and creation.
 *
 * Revision 1.9  2006/07/10 14:52:23  spr
 * Code cleanup.
 *
 * Revision 1.8  2006/04/07 20:23:59  spr
 * Formatting fix.
 *
 * Revision 1.7  2006/02/21 17:06:42  spr
 * Changes to Pebble to support external data models.
 *
 * Revision 1.6  2005/07/08 20:57:06  spr
 * Charles' upgrade to Pebble UI.
 *
 * Revision 1.5  2005/06/28 17:20:53  spr
 * UI enhancements (CAR)
 *
 * Revision 1.4  2005/06/07 02:18:21  spr
 * Update for java 5.0
 *
 * Revision 1.3  2005/05/07 22:25:41  spr
 * Updates for java 5.0
 *
 * Revision 1.2  2005/04/29 18:15:26  spr
 * Add normalization and class type; fix minor problems.
 *
 * Revision 1.1  2005/04/28 21:48:16  spr
 * Initial release of the pebble automata editor.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.pebble;


import edu.brown.cs.ivy.swing.SwingGridPanel;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;



class PebbleEventEditor extends SwingGridPanel implements PebbleConstants,
		ListSelectionListener, ActionListener, DragGestureListener, DragSourceListener
{




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private PebbleEditor for_editor;
private Window for_window;
private JList<PebbleEvent> list_area;
private JButton edit_button;
private JButton remove_button;
private CreateEventDialog new_dialog;

private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

PebbleEventEditor(PebbleEditor pe,Window win)
{
   for_editor = pe;
   for_window = win;
   list_area = null;
   edit_button = null;
   remove_button = null;

   setupLayout();
   DragSource ds = DragSource.getDefaultDragSource();
   ds.createDefaultDragGestureRecognizer(list_area,DnDConstants.ACTION_COPY_OR_MOVE,this);

   if (for_editor.getBaseModel().supportsEditEvents()) {
      new_dialog = new CreateEventDialog();
    }
   else new_dialog = null;
}




/********************************************************************************/
/*										*/
/*	Methods to handle layout						*/
/*										*/
/********************************************************************************/

private void setupLayout()
{
   int y = 0;
   JLabel lbl = new JLabel("Event Panel",SwingConstants.CENTER);
   addGBComponent(lbl,0,y++,0,1,1,0);
   addGBComponent(new JSeparator(),0,y++,0,1,1,0);

   list_area = new JList<PebbleEvent>();
   list_area.setValueIsAdjusting(true);
   list_area.setVisibleRowCount(25);
   list_area.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
   list_area.addListSelectionListener(this);
   list_area.addMouseListener(new Mouser());
   list_area.setToolTipText("");
   addGBComponent(new JScrollPane(list_area),0,y++,0,1,1,10);

   if (for_editor.getBaseModel().supportsEditEvents()) {
      addGBComponent(new JSeparator(),0,y++,0,1,1,0);

      Box b = Box.createHorizontalBox();
      b.add(Box.createHorizontalGlue());
      edit_button = new JButton("Edit");
      edit_button.addActionListener(this);
      edit_button.setEnabled(false);
      b.add(edit_button);
      b.add(Box.createHorizontalGlue());
      remove_button = new JButton("Remove");
      remove_button.addActionListener(this);
      remove_button.setEnabled(false);
      b.add(remove_button);
      b.add(Box.createHorizontalGlue());
      addGBComponent(b,0,y++,0,1,1,0);

      b = Box.createHorizontalBox();
      b.add(Box.createHorizontalGlue());
      JButton add = new JButton("New");
      add.addActionListener(this);
      b.add(add);
      b.add(Box.createHorizontalGlue());
      addGBComponent(b,0,y++,0,1,1,0);
    }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

void setEvents(Collection<PebbleEvent> e)
{
   list_area.setListData(e.toArray(new PebbleEvent[e.size()]));
   list_area.invalidate();
   list_area.repaint(list_area.getBounds());
}



/********************************************************************************/
/*										*/
/*	Callback Actions							*/
/*										*/
/********************************************************************************/

@Override public void actionPerformed(ActionEvent e) {
   String btn = e.getActionCommand();
   if (btn.equals("Edit")) {
      PebbleEvent evt = list_area.getSelectedValue();
      editEvent(evt);
    }
   else if (btn.equals("Remove")) {
      List<PebbleEvent> sels = list_area.getSelectedValuesList();
      for_editor.getEditModel().removeEvents(sels);
    }
   else if (btn.equals("New")) {
      /* in case the list was cleared since we were last used */
      updateMatches();

      int sts = JOptionPane.showOptionDialog(for_window,new_dialog,
						"Create New Event",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE,
						null,null,null);
      if (sts != 0) return;
      PebbleEvent evt = new PebbleEvent(new_dialog.getEventName(),
					   new_dialog.getEventType());
      evt.setLabel(new_dialog.getEventLabel());
      if (!for_editor.getEditModel().addEvent(evt)) {
	 JOptionPane.showMessageDialog(for_window,"Events must have unique names");
	 return;
       }
      editEvent(evt);
      list_area.setSelectedValue(evt,true);
    }
}




@Override public void valueChanged(ListSelectionEvent e) {
   List<PebbleEvent> sels = list_area.getSelectedValuesList();
   if (edit_button != null) edit_button.setEnabled(sels.size() == 1);
   if (remove_button != null) remove_button.setEnabled(sels.size() != 0);
}




private class Mouser extends MouseAdapter {

   @Override public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
	 int idx = list_area.locationToIndex(e.getPoint());
	 if (idx >= 0) {
	    Object item = list_area.getModel().getElementAt(idx);
	    if (item != null && item instanceof PebbleEvent) {
	       PebbleEvent pe = (PebbleEvent) item;
	       editEvent(pe);
	       list_area.clearSelection();
	       for_editor.update();
	     }
	  }
       }
    }

}	// end of subclass Mouser




/********************************************************************************/
/*										*/
/*	Drag and drop management						*/
/*										*/
/********************************************************************************/

@Override public void dragGestureRecognized(DragGestureEvent evt) {
   Point p = evt.getDragOrigin();
   int idx = list_area.locationToIndex(p);
   if (idx < 0) return;

   PebbleEvent item = list_area.getModel().getElementAt(idx);
   if (item != null) {
      DragSource ds = DragSource.getDefaultDragSource();
      List<PebbleEvent> vals = list_area.getSelectedValuesList();
      if (vals.size() > 1) {
	 for (int i = 0; i < vals.size(); ++i) {
	    if (vals.get(i) == item) {
	       EventSet eset = new EventSet(vals);
	       ds.startDrag(evt,null,eset,this);
	       return;
	     }
	  }
       }
      ds.startDrag(evt,null,item,this);
    }
}

@Override public void dragDropEnd(DragSourceDropEvent dsde) { }

@Override public void dragEnter(DragSourceDragEvent dsde) { }

@Override public void dragExit(DragSourceEvent dse) { }

@Override public void dragOver(DragSourceDragEvent dsde) { }

@Override public void dropActionChanged(DragSourceDragEvent dsde) { }





private static DataFlavor eventset_flavor = new DataFlavor(EventSet.class,"Pebble Event Set");


private static class EventSet extends Vector<PebbleEvent> implements Transferable {

   private static final long serialVersionUID = 1;

   EventSet(List<PebbleEvent> os) {
      for (PebbleEvent pe : os) add(pe);
    }

   @Override public Object getTransferData(DataFlavor df) 		{ return this; }

   @Override public DataFlavor [] getTransferDataFlavors() {
      return new DataFlavor [] { eventset_flavor };
    }

   @Override public boolean isDataFlavorSupported(DataFlavor df) {
      return df == eventset_flavor;
    }

}	// end of subclass EventSet





/********************************************************************************/
/*										*/
/*	Event creation dialog							*/
/*										*/
/********************************************************************************/

private class CreateEventDialog extends SwingGridPanel implements ActionListener {

   JTextField name_field;
   JTextField label_field;
   JComboBox<String> event_box;
   private static final long serialVersionUID = 1;

   CreateEventDialog() {
      beginLayout();
      addBannerLabel("Define New Event");
      addSeparator();
      name_field = addTextField("Event Name",null,this,null);
      label_field = addTextField("Event Label",null,this,null);
      Collection<String> c = for_editor.getBaseModel().getEventTypes();
      event_box = addChoice("Event Type",new ArrayList<String>(c),0,this);
      addSeparator();
    }

   @Override public void actionPerformed(ActionEvent evt) {
    }

   String getEventName()			{ return name_field.getText(); }
   EventType getEventType() {
      String s = (String) event_box.getSelectedItem();
      return for_editor.getBaseModel().getEventType(s);
    }
   String getEventLabel() {
       return label_field.getText();
   }

}	// end of subclass CreateEventDialog




/********************************************************************************/
/*										*/
/*	Event editing dialog							*/
/*										*/
/********************************************************************************/

private void editEvent(PebbleEvent ev) {
   if (ev == null) return;
   EditEventDialog eed = new EditEventDialog(ev);
   JScrollPane scroll = new JScrollPane(eed);
   scroll.setPreferredSize(new Dimension(scroll.getPreferredSize().width
	       + 60, 500));
   scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
   int sts = showResizableOptionDialog(for_window,scroll,
					     "Edit Event",
					     JOptionPane.OK_CANCEL_OPTION,
					     JOptionPane.PLAIN_MESSAGE
					     /*,null,null,null*/);
   if (sts != 0) return;
   eed.setParameters();
}

private int showResizableOptionDialog(Component parent, Object message, String title,
	int optionType, int messageType) {
    JOptionPane p = new JOptionPane(message, messageType, optionType, null,
	    null,  null);
    JDialog d = p.createDialog(parent, title);
    d.setResizable(true);
    d.setVisible(true);
    Object selectedValue = p.getValue();
    if (selectedValue == null || !(selectedValue instanceof Integer)) {
	return JOptionPane.CLOSED_OPTION;
    }
	return ((Integer) selectedValue).intValue();
}




private class EditEventDialog extends SwingGridPanel implements ActionListener {

   PebbleEvent for_event;
   JComboBox<String> default_box;
   JTextField label_field;
   Map<String,Component> prop_fields;
   private static final long serialVersionUID = 1;

   EditEventDialog(PebbleEvent evt) {
      for_event = evt;
      prop_fields = new HashMap<String,Component>();
   
      EventType et = evt.getEventType();
      beginLayout();
      addBannerLabel("Setup " + et.getName() + " Event " + evt.getName());
      addSeparator();
      addDescription("Event Name",evt.getName());
      label_field = addTextField("Event Label",evt.getLabel(),this,null);
   
      Object [] nds = for_editor.getEditModel().getNodes();
      TreeSet<String> vset = new TreeSet<String>();
      for (int i = 0; i < nds.length; ++i) {
         if (nds[i] instanceof PebbleState) {
            PebbleState ps = (PebbleState) nds[i];
            vset.add(ps.getName());
          }
       }
      String [] sts = new String[vset.size() + 1];
      int i = 0;
      int st0 = 0;
      sts[i++] = "-- SELF LOOP --";
      for (Iterator<String> it = vset.iterator(); it.hasNext(); ) {
         String s = it.next();
         if (s.equals(for_event.getDefaultState())) st0 = i;
         sts[i++] = s;
       }
      if (st0 == 0 && for_event.getDefaultState() != null) for_event.setDefaultState(null);
      default_box = addChoice("Default Target",sts,st0,this);
      addSeparator();
   
      Collection<String> c = for_editor.getEditModel().getAllMatches();
      String [] opts = new String[c.size() + 2];
      i = 0;
      opts[i++] = "NONE";
      opts[i++] = "NEW";
      for (Iterator<String> it = c.iterator(); it.hasNext(); ) opts[i++] = it.next();
   
      boolean sep = false;
      for (EventProperty ep : et.getProperties()) {
         int pt = ep.getType();
         if (pt == PEBBLE_TYPE_BOOLEAN) {
            JCheckBox cb = addBoolean(ep.getLabel(),evt.getBooleanParam(ep.getName()),this);
            prop_fields.put(ep.getName(),cb);
            sep = false;
          }
         else if (pt == PEBBLE_TYPE_STRING) {
            JTextField tf = addTextField(ep.getLabel(),evt.getStringParam(ep.getName()),this,null);
            prop_fields.put(ep.getName(),tf);
            sep = false;
          }
         else if (pt == PEBBLE_TYPE_CLASS) {
            if (!sep) addSeparator();
            /*
            String lbl = ep.getLabel();
            JTextField tf = addTextField(lbl + " Class",evt.getStringParam(ep.getName()),this,null);
            String v = evt.getMatchValue(ep.getName());
            int idx = 0;
            if (v != null) {
               for (int j = 2; j < opts.length; ++j) {
        	  if (v.equals(opts[j])) {
        	     idx = j;
        	     String tv = for_editor.getEditModel().getEventMatch(v);
        	     tf.setText(tv);
        	   }
        	}
             }
            if (idx == 0) {
        	tf.setEnabled(false);
            } else {
        	tf.setEnabled(true);
            }
            JComboBox cb = addChoice("Match",opts,idx,this);
            prop_fields.put(ep.getName(),tf);
            prop_fields.put(ep.getName() + "_VALUE",cb);
            */
            ClassEditor ce = new ClassEditor();
            ce.init(ep.getName(), evt);
            addRawComponent(ep.getLabel() + " Class", ce);
            prop_fields.put(ep.getName(), ce);
            addSeparator();
            sep = true;
          }
         else if (pt == PEBBLE_TYPE_MATCH) {
            if (!sep) addSeparator();
            MatchEditor me = new MatchEditor();
            me.init(ep.getName(), evt);
            addRawComponent(ep.getLabel() + " Class", me);
            prop_fields.put(ep.getName(), me);
            /*
            String lbl = ep.getLabel();
            JTextField tf = addTextField(lbl + " Class",evt.getStringParam(ep.getName()),this,null);
            String v = evt.getMatchValue(ep.getName());
            int idx = 0;
            tf.setEnabled(false);
            if (v != null) {
               for (int j = 2; j < opts.length; ++j) {
        	  if (v.equals(opts[j])) {
        	     idx = j;
        	     String tv = for_editor.getEditModel().getEventMatch(v);
        	     tf.setText(tv);
        	     tf.setEnabled(true);
        	   }
        	}
             }
            JComboBox cb = addChoice("Match",opts,idx,this);
            cb.setModel(getMatchModel(v));
            prop_fields.put(ep.getName(),tf);
            prop_fields.put(ep.getName() + "_VALUE",cb);
            idx = 0;
            v = evt.getMatchMode(ep.getName());
            for (int j = 1; j < PEBBLE_MATCH_TYPES.length; ++j) {
            if (v != null && v.equals(PEBBLE_MATCH_TYPES[j])) {
        	  idx = j;
        	}
             }
            JComboBox mcb = addChoice("Match Mode",PEBBLE_MATCH_TYPES,idx,this);
            prop_fields.put(ep.getName() + "_MODE",mcb);
            */
            addSeparator();
            sep = true;
          }
         else if (pt == PEBBLE_TYPE_MULTI_MATCH) {
             if (!sep) addSeparator();
             MultiMatchEditor mme = new MultiMatchEditor();
             mme.init(ep.getName(), evt, ep.getLabel());
             addLabellessRawComponent(ep.getLabel(), mme);
             addSeparator();
             prop_fields.put(ep.getName(), mme);
             sep = true;
             /** FIXME add, etc. */
         }
       }
      if (!sep) addSeparator();
    }

   void setParameters() {
      String nxt = (String) default_box.getSelectedItem();
      if (nxt.startsWith("--")) for_event.setDefaultState(null);
      else for_event.setDefaultState(nxt);
      String lbl = label_field.getText().trim();
      for_event.setLabel(lbl);
   
      EventType et = for_event.getEventType();
      for (EventProperty ep : et.getProperties()) {
         int pt = ep.getType();
         if (pt == PEBBLE_TYPE_BOOLEAN) {
            JCheckBox cb = (JCheckBox) prop_fields.get(ep.getName());
            for_event.setBooleanParam(ep.getName(),cb.isSelected());
          }
         else if (pt == PEBBLE_TYPE_STRING) {
            JTextField tf = (JTextField) prop_fields.get(ep.getName());
            for_event.setStringParam(ep.getName(),tf.getText());
          }
         else if (pt == PEBBLE_TYPE_MATCH) {
             MatchEditor me = (MatchEditor) prop_fields.get(ep.getName());
             me.commit(ep.getName());
         }
         else if (pt == PEBBLE_TYPE_CLASS) {
             ClassEditor ce = (ClassEditor) prop_fields.get(ep.getName());
             ce.commit(ep.getName());
            /*
            JTextField tf = (JTextField) prop_fields.get(ep.getName());
            JComboBox cb = (JComboBox) prop_fields.get(ep.getName() + "_VALUE");
            JComboBox mcb = (JComboBox) prop_fields.get(ep.getName() + "_MODE");
            String tv = tf.getText().trim();
            if (tv.length() == 0) tv = null;
            String v = cb.getSelectedItem().toString(); // FIX ME
            if (v.equals("NONE")) {
               for_event.setMatchValue(ep.getName(),null);
               tf.setEnabled(false);
             }
            else {
               tf.setEnabled(true);
               if (v.equals("NEW")) {
        	  if (tv == null) {
        	     for_event.setMatchValue(ep.getName(),null);
        	     continue;
        	   }
        	  v = for_editor.getEditModel().getNextMatch();
        	}
               for_event.setMatchValue(ep.getName(),v);
               String ov = for_editor.getEditModel().getEventMatch(v);
               if (ov == null || !ov.equals(tv)) {
        	  for_editor.getEditModel().setEventMatch(v,tv);
        	}
             }
            if (mcb != null) {
               for_event.setMatchMode(ep.getName(),(String) mcb.getSelectedItem());
             }
             */
          }
          else if (pt == PEBBLE_TYPE_MULTI_MATCH) {
              MultiMatchEditor mme = (MultiMatchEditor) prop_fields.get(ep.getName());
              mme.commit(ep.getName());
          }
       }
    }

   @Override public void actionPerformed(ActionEvent evt) {
      EventType et = for_event.getEventType();
      if (prop_fields == null) return;

      for (EventProperty ep : et.getProperties()) {
	 int pt = ep.getType();
	 if (pt == PEBBLE_TYPE_MATCH) {
	    JComboBox<?> cb = (JComboBox<?>) prop_fields.get(ep.getName() + "_VALUE");
	    JTextField tf = (JTextField) prop_fields.get(ep.getName());
	    if (evt.getSource() == tf) {
	       String s = (String) cb.getSelectedItem();
	       if (s.equals("NONE")) cb.setSelectedIndex(1);
	     }
	    else if (evt.getSource() == cb) {
	       Object o = cb.getSelectedItem();
	       if (o == MATCH_NONE) {
		  tf.setEnabled(false);
		  tf.setText("");
	       } else {
		  tf.setEnabled(true);
		  String s = (String) cb.getSelectedItem();
		  String nv = for_editor.getEditModel().getEventMatch(s);
		  tf.setText(nv);
		}
	     }
	  }
       }
    }

}	// end of subclass EditEventDialog

private Set<Reference<MatchComboModel> > models = new HashSet<
    Reference<MatchComboModel> >();
private java.util.List<String> matchClasses;

private String newEventMatch(MatchComboModel model) {
    String value = for_editor.getEditModel().getNextMatch();
    matchClasses = new ArrayList<String>(for_editor.getEditModel().getAllMatches());
    updateMatches();
    return value;
}

private void updateMatches() {
    for (Iterator<Reference<MatchComboModel> > it = models.iterator();
	    it.hasNext(); ) {
	Reference<MatchComboModel> rm = it.next();
	MatchComboModel m = rm.get();
	if (m == null) {
	    it.remove();
	} else {
	    m.update();
	}
    }
}


private MatchComboModel getMatchModel(String selected) {
    if (matchClasses == null) {
	matchClasses = new ArrayList<String>(for_editor.getEditModel().getAllMatches());
    }
    if (selected != null && selected.trim().equals("")) {
	selected = null;
    }
    MatchComboModel model = new MatchComboModel(selected);
    models.add(new WeakReference<MatchComboModel>(model));
    return model;
}

private Object MATCH_NONE = new Object() {
    @Override public String toString() { return "NONE"; }
};
private Object MATCH_NEW = new Object() {
    @Override public String toString() { return "NEW"; }
};

private class MatchComboModel extends AbstractListModel<Object> implements ComboBoxModel<Object> {

    private static final long serialVersionUID = 1;

    private String selected;

    public MatchComboModel(String sel) {
	if (sel == null || sel.equals("NONE")) {
	    selected = null;
	} else {
	    selected = sel;
	}
    }

    public void update() {
	fireContentsChanged(this, 0, getSize());
    }

    public void update(int i) {
	fireContentsChanged(this, i, i);
    }

    @Override public Object getSelectedItem() {
	return selected == null ? MATCH_NONE : selected;
    }

    @Override public void setSelectedItem(Object o) {
	if (o == MATCH_NONE || o == null) {
	    selected = null;
	    update(0);
	} else if (o == MATCH_NEW) {
	    selected = newEventMatch(this);
	    update();
	} else {
	    selected = (String) o;
	    update(matchClasses.indexOf(selected) + 2);
	}
    }

    @Override public Object getElementAt(int index) {
	if (index == 0) {
	    return MATCH_NONE;
	} else if (index == 1) {
	    return MATCH_NEW;
	} else {
	    return matchClasses.get(index - 2);
	}
    }

    @Override public int getSize() {
	return matchClasses.size() + 2;
    }

}

private class ClassEditor extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1;
    private JLabel classLabel;
    private JTextField classname;
    private JComboBox<Object> matchClass;

    protected PebbleEvent for_event;
    public void setup() {
	setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
	//mainLabel = new JLabel(label + " Class: ");
	//add(mainLabel);
	matchClass = new JComboBox<Object>();
	add(matchClass);
	matchClass.addActionListener(this);
	classLabel = new JLabel(" Classname: ");
	add(classLabel);
	classname = new JTextField(15); 
	add(classname);
    }

    public void sync(String name) {
	matchClass.setModel(getMatchModel(for_event.getMatchValue(name)));
	if (getMatchValue() != null) {
	    classname.setText(
		    for_editor.getEditModel().getEventMatch(getMatchValue()));
	} else {
	    classname.setText("(N/A)");
	}
	fixEnabled();
	invalidate();
    }

    public String getMatchValue() {
	Object o = matchClass.getSelectedItem();
	if (o == MATCH_NONE) {
	    return null;
	}
	return (String) o;
    }

    public void init(String name, PebbleEvent new_evt) {
	for_event = new_evt;
	setup();
	sync(name);
    }

    public void commit(String name) {
	String val = getMatchValue();
	for_event.setMatchValue(name, val);
	if (val != null) {
	   //for_event.setStringParam(name, classname.getText());
	   String t = classname.getText().trim();
	   for_editor.getEditModel().setEventMatch(val, t);
	}
    }

    @Override public void actionPerformed(ActionEvent evt) {
	if (evt.getSource() == matchClass) {
	    fixEnabled();
	    Object o = matchClass.getSelectedItem();
	    if (o != MATCH_NONE) {
		classname.setText(
			for_editor.getEditModel().getEventMatch((String) o));
		classname.requestFocusInWindow();
	    } else {
		classname.setText("(N/A)");
	    }
	}
    }

    public void fixEnabled() {
	fixEnabled(matchClass.getSelectedItem() != MATCH_NONE);
    }

    protected void fixEnabled(boolean enabled) {
	classLabel.setEnabled(enabled);
	classname.setEnabled(enabled);
    }


}
private class MatchEditor extends ClassEditor {
    private static final long serialVersionUID = 1;
    /* Layout:
     *	[Label] Class:
     *	 Match: [NONE]
     *
     *	[Label] Class:
     *	 Match: [C1] (type [...................]) Mode: [....]
     */
    private JLabel modeLabel;
    private JComboBox<String> matchMode;

    @Override public void setup() {
	super.setup();
	modeLabel = new JLabel(" Mode: ");
	add(modeLabel);
	matchMode = new JComboBox<String>(PEBBLE_MATCH_TYPES);
	add(matchMode);
    }


    @Override public void sync(String name) {
	String v = for_event.getMatchMode(name);
	if (v == null) {
	    matchMode.setSelectedIndex(0);
	} else {
	    matchMode.setSelectedItem(for_event.getMatchMode(name));
	}
	super.sync(name);
    }


    @Override protected void fixEnabled(boolean enabled) {
	super.fixEnabled(enabled);
	modeLabel.setEnabled(enabled);
	matchMode.setEnabled(enabled);
    }

    @Override public void commit(String name) {
	if (getMatchValue() != null) {
	   for_event.setMatchMode(name, (String) matchMode.getSelectedItem());
	}
	super.commit(name);
    }
}

private class MultiMatchEditor extends JPanel implements ActionListener {

    private java.util.List<MatchEditor> matches = new ArrayList<MatchEditor>();

    private JLabel nameLabel;

    private JPanel top;

    private String name;

    protected PebbleEvent for_event;

    private JButton addButton;
    private JButton removeButton;
    private static final long serialVersionUID = 1;

    /*
     * name:   [...] items:
     * name 1: [C1] type [....] mode [...]
     */

    private void setupLayout() {
	top = new JPanel();
	top.setLayout(new BoxLayout(top, BoxLayout.LINE_AXIS));
	top.add(nameLabel);
	addButton = new JButton("Add Item");
	removeButton = new JButton("Remove Item");
	addButton.addActionListener(this);
	removeButton.addActionListener(this);
	top.add(addButton);
	top.add(removeButton);
    }

    public void init(String name_, PebbleEvent pe, String label) {
	name = name_;
	for_event = pe;
	setup(label);
	sync();
    }

    public void sync() {
	String s = for_event.getStringParam(name);
	if (s != null && s.length() > 0) {
	    setCount(Integer.parseInt(s));
	}
    }

    public void commit(String wName) {
	for_event.setStringParam(wName, "" + matches.size());
	int i = 0;
	for (MatchEditor me : matches) {
	    me.commit(wName + ++i);
	}
    }

    private void redoLayout() {
	removeAll();
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	add(top);
	for (MatchEditor me : matches) {
	    add(me);
	}
	revalidate();
    }

    public void setup(String myName) {
	nameLabel = new JLabel(myName + ":");
	setupLayout();
	redoLayout();
    }

    private void setCount(int newCount) {
	if (matches.size() < newCount) {
	   for (int i = matches.size(); i < newCount; ++i) {
	       MatchEditor me = new MatchEditor();
	       me.init(name + i, for_event);
	       matches.add(me);
	   }
	} else if (matches.size() > newCount) {
	    matches.subList(newCount, matches.size() - 1).clear();
	}

	redoLayout();
    }

    @Override public void actionPerformed(ActionEvent ae) {
	if (ae.getSource() == addButton) {
	    setCount(matches.size() + 1);
	} else if (ae.getSource() == removeButton) {
	    setCount(matches.size() - 1);
	}
	//setCount(Integer.parseInt(count.getText()));
    }
}



}

/* end of PebbleEventEditor.java */

