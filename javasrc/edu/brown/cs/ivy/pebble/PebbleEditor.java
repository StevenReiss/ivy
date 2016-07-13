/********************************************************************************/
/*										*/
/*		PebbleEditor.java						*/
/*										*/
/*	Editor window for editing event-based automata				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/pebble/PebbleEditor.java,v 1.15 2015/11/20 15:09:22 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PebbleEditor.java,v $
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
 * Revision 1.11  2007-05-04 02:00:28  spr
 * Fix bugs related to polling.
 *
 * Revision 1.10  2006-08-03 12:35:39  spr
 * Text cleanup.
 *
 * Revision 1.9  2006/07/23 02:25:14  spr
 * Add support for action editing and creation.
 *
 * Revision 1.8  2006/07/10 14:52:23  spr
 * Code cleanup.
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
 * Revision 1.4  2005/06/07 02:18:20  spr
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


import edu.brown.cs.ivy.petal.*;
import edu.brown.cs.ivy.swing.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;


public class PebbleEditor implements PebbleConstants, SwingMonitoredWindow {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		editor_title;
private PetalEditor	editor_pane;
private PebbleModel	base_model;
private PebbleEventEditor event_panel;
private PebbleAutomata	edit_model;
private boolean 	show_self;
private boolean 	show_default;
private AutomataDialog	automata_dialog;
private PetalLayoutMethod layout_method;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PebbleEditor(String ttl,PebbleModel mdl)
{
   editor_title = ttl;

   show_self = false;
   show_default = false;

   base_model = mdl;
   edit_model = new PebbleAutomata(this);
   layout_method = null;

   automata_dialog = new AutomataDialog();
}



/********************************************************************************/
/*										*/
/*	Window methods								*/
/*										*/
/********************************************************************************/

public JFrame getFrame()
{
   JFrame frm = new JFrame();
   frm.setTitle(editor_title);
   frm.setSize(700,500);
   frm.setJMenuBar(new MenuBar(this,frm));
   frm.setContentPane(getInterior(frm));

   frm.addWindowListener(new SwingRootMonitor());

   return frm;
}



public JDialog getDialog(boolean modal)
{
   JDialog dlg = new JDialog();
   dlg.setTitle(editor_title);
   dlg.setModal(modal);
   dlg.setSize(700,500);
   dlg.setJMenuBar(new MenuBar(this,dlg));
   dlg.setContentPane(getInterior(dlg));

   dlg.addWindowListener(new SwingRootMonitor());

   return dlg;
}



private JPanel getInterior(Window win)
{
   JPanel pnl = new JPanel();
   pnl.setLayout(new GridLayout());
   JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true);
   pnl.add(sp);

   event_panel = new PebbleEventEditor(this,win);
   sp.setLeftComponent(event_panel);

   editor_pane = new PetalEditor(edit_model);
   editor_pane.setGridSize(10);
   editor_pane.setMinimumSize(new Dimension(100,100));
   edit_model.setup();
   if (!base_model.supportsOpen()) {
      if (base_model.handleOpen(this,null)) fixLayout();
    }

   JPanel epnl = new JPanel(new GridLayout(1,1));
   epnl.add(editor_pane);
   sp.setRightComponent(new JScrollPane(epnl));

   return pnl;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

PebbleAutomata getEditModel()			{ return edit_model; }
PebbleModel getBaseModel()			{ return base_model; }
public Component getComponent() 		{ return editor_pane; }

Collection<PebbleEvent> getAllEvents()		{ return edit_model.getAllEvents(); }

public Collection<PebbleConstants.State> getAllStates()
{
   Collection<PebbleConstants.State> rslt = new ArrayList<PebbleConstants.State>();

   rslt.addAll(edit_model.getAllStates());

   return rslt;
}
public PebbleConstants.State getStartState()	       { return edit_model.getStartState(); }

public Collection<PebbleConstants.Transition> getAllTransitions(PebbleConstants.State frm)
{
   Collection<PebbleConstants.Transition> rslt = new ArrayList<PebbleConstants.Transition>();
   for (PebbleTransition pt : edit_model.getAllTransitions()) {
       if (frm == null || frm == pt.getFromState()) rslt.add(pt);
     }
   return rslt;
}



public boolean getShowSelfArcs()		{ return show_self; }
public void setShowSelfArcs(boolean fg)
{
   if (fg == show_self) return;

   show_self = fg;
   fixLayout();
   if (editor_pane != null) editor_pane.update();
}




boolean getShowDefaultArcs()			{ return show_default; }




/********************************************************************************/
/*										*/
/*	Display methods 							*/
/*										*/
/********************************************************************************/

void showStatus(String sts)				{ }



void assignNextPosition(Component c)
{
   editor_pane.assignNextPosition(c);
}



void setEvents(Collection<PebbleEvent> s)
{
   event_panel.setEvents(s);
}



void update()
{
   editor_pane.update();
}



/********************************************************************************/
/*										*/
/*	Input methods								*/
/*										*/
/********************************************************************************/

private boolean loadFile(Component cmp)
{
   edit_model.clear();

   boolean fg = base_model.handleOpen(this,cmp);

   if (fg) fixLayout();

   return fg;
}


/*
 *
 * Resetting method for "New"
 *
 */

void reset() {
   edit_model.clearValues();
   edit_model.clear();
   List<PebbleEvent> evl = Collections.emptyList();
   setEvents(evl);
   editor_pane.commandClear();
}



private void fixLayout()
{
   if (editor_pane == null) return;
   if (layout_method == null) layout_method = new PetalLevelLayout(editor_pane);
   editor_pane.commandLayout(layout_method);
   Dimension d = editor_pane.getPreferredSize();
   editor_pane.setSize(d);
}



/********************************************************************************/
/*										*/
/*	Window listener 							*/
/*										*/
/********************************************************************************/

@Override public void closeWindow()
{
   if (!base_model.supportsSave()) base_model.handleSave(this,null);
}



/********************************************************************************/
/*										*/
/*	Menu Bar								*/
/*										*/
/********************************************************************************/

private static class MenuBar extends SwingMenuBar implements ActionListener {

   private PebbleEditor for_editor;
   private Window for_window;
   private static final long serialVersionUID = 1;

   MenuBar(PebbleEditor pe,Window win) {
      for_editor = pe;
      for_window = win;
      JMenu filemenu = new JMenu("File");
      JMenu editmenu = new JMenu("Edit");
      JMenu viewmenu = new JMenu("View");
      JMenu selectmenu = new JMenu("Select");
      JMenu layoutmenu = new JMenu("Layout");
      JMenu statemenu = new JMenu("State");
      JMenuItem btn = null;

      if (for_editor.base_model.supportsOpen()) {
	 btn = addButton(filemenu,"New", "Create a new file");
	 btn = addButton(filemenu,"Open","Load an existing file");
       }
      if (for_editor.base_model.supportsSave()) {
	 btn = addButton(filemenu,"Save","Save current automaton");
       }
      if (btn != null) filemenu.addSeparator();

      if (for_editor.base_model.supportsSave()) {
	 btn = addButton(filemenu,"Quit","Quit the automata editor");
       }
      else {
	 btn = addButton(filemenu,"Abort","Close and ignore changes");
	 btn = addButton(filemenu,"Close","Close the automata editor");
       }

      super.add(filemenu);

      btn = addButton(editmenu,"Cut","Cut current selection");
      btn = addButton(editmenu,"Copy","Copy current selection");
      btn = addButton(editmenu,"Paste","Paste from cut buffer");
      btn = addButton(editmenu,"Clear","Clear all objects");
      editmenu.addSeparator();
      btn = addButton(editmenu,"Select All","Select all objects");
      btn = addButton(editmenu,"Clear Selection","Clear any selections");
      editmenu.addSeparator();
      btn = addButton(editmenu,"Normalize Automata","Handle default transitions");
      btn = addButton(editmenu,"Automata Properties","Set automata properties");


//    editmenu.addSeparator();
//    btn = addButton(editmenu,"Undo","Undo previous command(s)");
//    btn = addButton(editmenu,"Redo","Redo undone command(s)");
      super.add(editmenu);

      btn = addButton(selectmenu,"Select All","Select all nodes and arcs");
      btn = addButton(selectmenu,"Clear Selections","Clear all selections");
      super.add(selectmenu);

      btn = addButton(statemenu,"New State","Create a new state");
      super.add(statemenu);

      btn = addButton(layoutmenu,"Level Layout","Redraw using level-based layout");
      btn = addButton(layoutmenu,"Relaxation Layout","Redraw using relaxation-based layout");
      super.add(layoutmenu);

      btn = addCheckButton(viewmenu,"Show Self Loops",for_editor.show_self,"Draw self loop arcs");
      btn = addCheckButton(viewmenu,"Show Default Arcs",for_editor.show_default,"Draw default transitions");
      super.add(viewmenu);
    }


   @Override public void actionPerformed(ActionEvent e) {
      String btn = e.getActionCommand();
      if (btn.equals("Quit")) {
	 System.exit(0);
       }
      else if (btn.equals("Close")) {
	 if (!for_editor.base_model.supportsSave()) for_editor.base_model.handleSave(for_editor,for_window);
	 for_window.setVisible(false);
       }
      else if (btn.equals("Abort")) {
	 for_window.setVisible(false);
       }
      else if (btn.equals("Cut")) {
	 for_editor.editor_pane.commandCut();
       }
      else if (btn.equals("Copy")) {
	 for_editor.editor_pane.commandCopy();
       }
      else if (btn.equals("Paste")) {
	 for_editor.editor_pane.commandPaste();
       }
      else if (btn.equals("Clear")) {
	 for_editor.editor_pane.commandClear();
	 for_editor.edit_model.clear();
       }
      else if (btn.equals("Select All")) {
	 for_editor.editor_pane.commandSelectAll();
       }
      else if (btn.equals("Clear Selection")) {
	 for_editor.editor_pane.commandDeselectAll();
       }
      else if (btn.equals("Undo")) {
	 for_editor.editor_pane.commandUndo();
       }
      else if (btn.equals("Redo")) {
	 for_editor.editor_pane.commandRedo();
       }
      else if (btn.equals("Select All")) {
	 for_editor.editor_pane.commandSelectAll();
       }
      else if (btn.equals("Clear Selections")) {
	 for_editor.editor_pane.commandDeselectAll();
       }
      else if (btn.equals("Level Layout")) {
	 for_editor.layout_method = new PetalLevelLayout(for_editor.editor_pane);
	 for_editor.editor_pane.commandLayout(for_editor.layout_method);
       }
      else if (btn.equals("Relaxation Layout")) {
	 for_editor.layout_method = new PetalRelaxLayout(for_editor.editor_pane);
	 for_editor.editor_pane.commandLayout(for_editor.layout_method);
       }
      else if (btn.equals("Show Self Loops")) {
	 JCheckBoxMenuItem jcb = (JCheckBoxMenuItem) e.getSource();
	 for_editor.setShowSelfArcs(jcb.getState());
       }
      else if (btn.equals("Show Default Arcs")) {
	 JCheckBoxMenuItem jcb = (JCheckBoxMenuItem) e.getSource();
	 for_editor.show_default = jcb.getState();
	 for_editor.fixLayout();
	 for_editor.editor_pane.update();
       }
      else if (btn.equals("Automata Properties")) {
	 for_editor.automata_dialog.process(for_editor.editor_pane);
       }
      else if (btn.equals("Normalize Automata")) {
	 for_editor.edit_model.normalizeAutomata();
	 for_editor.fixLayout();
	 for_editor.editor_pane.update();
       }
      else if (btn.equals("New")) {
	 for_editor.reset();
	 for_editor.automata_dialog.process(for_editor.editor_pane);
      }
      else if (btn.equals("Open")) {
	 for_editor.loadFile(for_window);
       }
      else if (btn.equals("Save")) {
	 for_editor.base_model.handleSave(for_editor,for_window);
	 for_editor.editor_pane.update();
       }
      else if (btn.equals("New State")) {
	  for_editor.edit_model.createState(null);
	  for_editor.editor_pane.update();
      }
    }

}	// end of subclass MenuBar




/********************************************************************************/
/*										*/
/*	Automata properties dialog						*/
/*										*/
/********************************************************************************/

private class AutomataDialog {

   private SwingGridPanel message_box;
   private Map<AutomataProperty,Component> attr_map;

   AutomataDialog() {
      message_box = null;
      attr_map = null;
    }

   void process(Component c) {
      layoutPanel();
      int sts = JOptionPane.showOptionDialog(c,message_box,null,
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE,
						null,null,null);
      if (sts == 0) setValues();
      message_box = null;
      attr_map = null;
    }

   private void layoutPanel() {
      message_box = new SwingGridPanel();
      message_box.beginLayout();
      message_box.addBannerLabel("Automata Properties");
      message_box.addSeparator();
      attr_map = new HashMap<AutomataProperty,Component>();

      for (Iterator<AutomataProperty> it = base_model.getAttributeSet().iterator(); it.hasNext(); ) {
	 AutomataProperty ap = it.next();
	 Component cx = null;
	 String v = getEditModel().getValue(ap);
	 switch (ap.getType()) {
	    case PEBBLE_TYPE_BOOLEAN :
	       boolean bvl;
	       if (v == null) bvl = false;
	       else if (v.startsWith("T") || v.startsWith("t")) bvl = true;
	       else bvl = false;
	       cx = message_box.addBoolean(ap.getLabel(),bvl,null);
	       break;
	    case PEBBLE_TYPE_STRING :
	       if (ap.isMultiple()) {
		  cx = message_box.addTextArea(ap.getLabel(),v,5,30,null);
		}
	       else {
		  cx = message_box.addTextField(ap.getLabel(),v,null,null);
		}
	       break;
	  }
	 if (cx != null) attr_map.put(ap,cx);
       }
    }

   private void setValues() {
      for (Iterator<Map.Entry<AutomataProperty,Component>> it = attr_map.entrySet().iterator(); it.hasNext(); ) {
	 Map.Entry<AutomataProperty,Component> ent = it.next();
	 AutomataProperty ap = ent.getKey();
	 String v = null;
	 if (ap.getType() == PEBBLE_TYPE_BOOLEAN) {
	    JCheckBox cbx = (JCheckBox) ent.getValue();
	    v = (cbx.isSelected() ? "TRUE" : "FALSE");
	  }
	 else if (ap.getType() == PEBBLE_TYPE_STRING) {
	    JTextComponent jtc = (JTextComponent) ent.getValue();
	    v = jtc.getText();
	    //if (v.length() == 0) v = null; // ???
	  }
	 getEditModel().setValue(ap,v);
       }
    }

}	// end of subclass AutomataDialog



/********************************************************************************/
/*										*/
/*	Factory methods for models						*/
/*										*/
/********************************************************************************/

public void clearAutomata()
{
   edit_model.clearValues();
   edit_model.clear();
}


public PebbleConstants.Event createEvent(String name,EventType ety,String label)
{
   PebbleEvent pe = new PebbleEvent(name,ety);
   pe.setLabel(label);
   edit_model.addEvent(pe);

   return pe;
}



public PebbleConstants.State createState(String name,String lbl,boolean start)
{
   PebbleState ps = null;
   if (start) {
      ps = edit_model.getStartState();
      ps.setName(name);
    }
   else {
      ps = edit_model.createState(name);
    }

   if (lbl != null) ps.setStateLabel(lbl);

   return ps;
}



public PebbleConstants.Transition createTransition(PebbleConstants.State fs,
						      PebbleConstants.State ts,
						      PebbleConstants.Event ev)
{
   PebbleState f = (PebbleState) fs;
   PebbleState t = (PebbleState) ts;
   PebbleEvent e = (PebbleEvent) ev;

   PebbleTransition pt = edit_model.createTransition(f,t);
   pt.addEvent(e);

   return pt;
}




/********************************************************************************/
/*										*/
/*	Test program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   new SwingSetup();

   PebbleModel mdl = new PebbleXmlModel("/pro/ivy/pebble/src/testmodel.xml");
   PebbleEditor pe = new PebbleEditor("Pebble Editor Test",mdl);
   JFrame frm = pe.getFrame();
   frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   frm.setVisible(true);

   pe.loadFile(frm);
}






}	// end of class PebbleEditor




/* end of PebbleEditor.java */



