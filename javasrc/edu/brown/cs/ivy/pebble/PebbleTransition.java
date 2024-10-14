/********************************************************************************/
/*										*/
/*		PebbleTransition.java						*/
/*										*/
/*	Local definition of arc information for Pebble graphs			*/
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


package edu.brown.cs.ivy.pebble;


import edu.brown.cs.ivy.petal.PetalArcDefault;
import edu.brown.cs.ivy.petal.PetalArcEndDefault;
import edu.brown.cs.ivy.petal.PetalLinkDefault;
import edu.brown.cs.ivy.petal.PetalNode;
import edu.brown.cs.ivy.petal.PetalNodeDefault;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.swing.SwingListPanel;
import edu.brown.cs.ivy.swing.SwingListSet;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;



class PebbleTransition extends PetalArcDefault implements PebbleConstants.Transition, PebbleConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ArcLabel arc_label;
private transient  List<PebbleEvent> arc_events;
private transient PebbleEditor base_editor;
private String	arc_condition;
private transient Collection<Action> arc_actions;

private static final long serialVersionUID = 1;

private static final float [] DASHES = { 10.0f, 5.0f };




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

PebbleTransition(PebbleEditor pe,PebbleState s1,PebbleState s2)
{
   super(s1,s2);

   base_editor = pe;

   arc_events = new Vector<PebbleEvent>();
   arc_condition = null;
   arc_actions = new ArrayList<Action>();

   setTargetEnd(new PetalArcEndDefault(PETAL_ARC_END_ARROW));

   PetalLinkDefault lnk = new PetalLinkDefault(0.3,0,0,0,0);
   lnk.setMovable(true);
   arc_label = new ArcLabel();
   arc_label.setLink(lnk,this);
   setupLabel();
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

PetalNode getLabel()			{ return arc_label; }

Collection<PebbleEvent> getAllEvents()	{ return base_editor.getAllEvents(); }

boolean usesEvent(PebbleEvent pe)	{ return arc_events.contains(pe); }

boolean getUsesConditions()		{ return base_editor.getBaseModel().getUsesConditions(); }

Collection<PebbleEvent> getEvents()	{ return arc_events; }

PebbleState getFromState()		{ return (PebbleState) getSource(); }
PebbleState getToState()		{ return (PebbleState) getTarget(); }

@Override public PebbleConstants.State getSourceState()	{ return getFromState(); }
@Override public PebbleConstants.State getTargetState()	{ return getToState(); }
@Override public Collection<PebbleConstants.Event> getTransitionEvents()
{
   return new ArrayList<PebbleConstants.Event>(arc_events);
}


boolean getUsesActions()
{
   return base_editor.getBaseModel().getUsesTransitionActions();
}

String getCondition()			{ return arc_condition; }
boolean setCondition(String c)
{
   if (c != null && !base_editor.getBaseModel().checkCondition(c)) {
      return false;
    }

   if (c == null && arc_condition != null) setSolid();
   else if (c != null && arc_condition == null) setDashed();

   arc_condition = c;
   return true;
}


@Override public Iterable<Action> getActions()
{
   return new ArrayList<Action>(arc_actions);
}


@Override public void addAction(Action a)
{
   if (a != null && !arc_actions.contains(a)) arc_actions.add(a);
}


@Override public void removeAction(Action a)
{
   arc_actions.remove(a);
}


SwingListSet<Action> getActionSet()
{
   return new SwingListSet<Action>(new ArrayList<Action>(arc_actions));
}



/********************************************************************************/
/*										*/
/*	Interaction methods							*/
/*										*/
/********************************************************************************/

@Override public boolean handleMouseClick(MouseEvent evt)
{
   return checkTransitionDialog(evt);
}


private boolean checkTransitionDialog(MouseEvent evt)
{
   if ((evt.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == 0) return false;

   TransitionDialog td = new TransitionDialog(this);
   td.process((Component) evt.getSource());

   return true;
}



/********************************************************************************/
/*										*/
/*	Events management methods						*/
/*										*/
/********************************************************************************/

void addEvent(PebbleEvent pe)
{
   if (pe == null) return;

   arc_events.add(pe);
   setupLabel();
}



void addEvent(String name)
{
   PebbleEvent pe = base_editor.getEditModel().findEvent(name);
   addEvent(pe);
}



void setupLabel()
{
   if (arc_label == null) return;

   StringBuffer buf = new StringBuffer();

   if (arc_events.size() == 0) buf.append("No Events");
   else {
      for (Iterator<PebbleEvent> it = arc_events.iterator(); it.hasNext(); ) {
	 PebbleEvent pe = it.next();
	 if (buf.length() > 0) buf.append(",");
	 buf.append(pe.getName());
       }
    }

   arc_label.setLabel(buf.toString());
}



private void removeEvent(int idx)
{
   PebbleEvent pe = arc_events.get(idx);

   arc_events.remove(pe);
   setupLabel();
}



private void moveUp(int idx)
{
   if (idx == 0) return;

   PebbleEvent pe = arc_events.get(idx);
   arc_events.remove(pe);
   arc_events.add(idx-1,pe);
   setupLabel();
}




private void moveDown(int idx)
{
   if (idx >= arc_events.size()-1) return;

   PebbleEvent pe = arc_events.get(idx);
   arc_events.remove(pe);
   arc_events.add(idx+1,pe);
   setupLabel();
}




/********************************************************************************/
/*										*/
/*	Drawing methods 							*/
/*										*/
/********************************************************************************/

private void setSolid()
{
   Stroke s = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND);

   setStroke(s);
}




private void setDashed()
{
   Stroke s = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,1.0f,DASHES,0);

   setStroke(s);
}




/********************************************************************************/
/*										*/
/*	Label class								*/
/*										*/
/********************************************************************************/

private class ArcLabel extends PetalNodeDefault {

   private static final long serialVersionUID = 1;

   ArcLabel() {
      JLabel lbl = new JLabel("NEW ARC");
      Font fn = lbl.getFont();
      fn = fn.deriveFont(9.0f);
      lbl.setFont(fn);
      lbl.setBorder(new LineBorder(Color.black,1));
      lbl.setSize(lbl.getPreferredSize());
      lbl.setOpaque(true);
      setComponent(lbl);
    }

   @Override public String getToolTip(Point at) {
      JLabel jl = (JLabel) getComponent();
      return jl.getText();
    }

   void setLabel(String s) {
      JLabel jl = (JLabel) getComponent();
      jl.setText(s);
      jl.setSize(jl.getPreferredSize());
    }

   @Override public boolean handleMouseClick(MouseEvent evt) {
      return checkTransitionDialog(evt);
    }

}	// end of subclass ArcLabel





/********************************************************************************/
/*										*/
/*	Dialog class								*/
/*										*/
/********************************************************************************/

private class TransitionDialog implements ActionListener
{
   private PebbleTransition for_transition;
   private SwingGridPanel message_box;
   private JDialog current_dialog;
   private JOptionPane option_pane;
   private String new_event;
   private JTextField when_field;
   private TransitionActionPanel action_panel;

   TransitionDialog(PebbleTransition pt) {
      for_transition = pt;
      message_box = null;
      current_dialog = null;
    }

   void process(Component c) {
      boolean done = false;
      while (!done) {
         layoutPanel();
         String [] opts = new String[] { "OK", "Cancel" };
         option_pane = new JOptionPane(message_box,JOptionPane.PLAIN_MESSAGE,
        				  JOptionPane.DEFAULT_OPTION,
        				  null,opts,null);
         option_pane.setValue(null);
         new_event = null;
         current_dialog = option_pane.createDialog(c,"Transition Editor");
         current_dialog.setVisible(true);
         current_dialog = null;
         String sts = (String) option_pane.getValue();
         option_pane = null;
         if (sts == null || sts.equals("Cancel")) done = true;
         else if (sts.equals("OK")) {
            if (when_field != null) {
               String s = when_field.getText().trim();
               if (s.length() == 0) for_transition.setCondition(null);
               else if (!for_transition.setCondition(s)) {
        	  JOptionPane.showMessageDialog(c,"Condition is invalid");
        	}
             }
            if (action_panel != null) {
               arc_actions.clear();
               for (Action a : action_panel.getItemSet()) arc_actions.add(a);
             }
            if (new_event == null) done = true;
            else {
               for_transition.addEvent(new_event);
             }
          }
         else if (sts.startsWith("REMOVE-")) {
            int idx = Integer.parseInt(sts.substring(7));
            for_transition.removeEvent(idx);
          }
         else if (sts.startsWith("UP-")) {
            int idx = Integer.parseInt(sts.substring(3));
            for_transition.moveUp(idx);
          }
         else if (sts.startsWith("DOWN-")) {
            int idx = Integer.parseInt(sts.substring(5));
            for_transition.moveDown(idx);
          }
   
         message_box = null;
       }
    }

   private void layoutPanel() {
      message_box = new SwingGridPanel();
      PebbleState s0 = (PebbleState) for_transition.getSource();
      PebbleState s1 = (PebbleState) for_transition.getTarget();
      when_field = null;
      action_panel = null;

      message_box.addBannerLabel("Transition from " + s0.getName() + " to " + s1.getName());
      message_box.addSeparator();
      int idx = 0;
      for (Iterator<PebbleEvent> it = for_transition.arc_events.iterator(); it.hasNext(); ) {
	 PebbleEvent pe = it.next();
	 message_box.addSectionLabel("Event " + pe.getName());
	 if (for_transition.getUsesConditions()) {
	    when_field = message_box.addTextField("When",for_transition.getCondition(),null,null);
	  }
	 if (for_transition.getUsesActions()) {
	    action_panel = new TransitionActionPanel();
	    message_box.addRawComponent("Actions",action_panel);
	    action_panel.addActionListener(this);
	  }
	 message_box.addBottomButton("Remove Event","REMOVE-" + idx,this);
	 message_box.addBottomButton("Move Event Up","UP-" + idx,this);
	 message_box.addBottomButton("Move Event Down","DOWN-" + idx,this);
	 message_box.addBottomButtons();
	 message_box.addSeparator();
	 ++idx;
       }

      new_event = null;
      Collection<PebbleEvent> c = for_transition.getAllEvents();
      Vector<PebbleEvent> v = new Vector<PebbleEvent>();
      for (Iterator<PebbleEvent> it = c.iterator(); it.hasNext(); ) {
	 PebbleEvent pe = it.next();
	 if (!for_transition.usesEvent(pe) && for_transition.getCondition() == null) v.add(pe);
       }
      if (v.size() > 0) {
	 String [] evts = new String[v.size() + 1];
	 int i = 0;
	 evts[i++] = "-- None --";
	 for (Iterator<PebbleEvent> it = v.iterator(); it.hasNext(); ) {
	    PebbleEvent pe = it.next();
	    evts[i++] = pe.getName();
	  }
	 message_box.addChoice("Use Event",evts,0,this);
	 message_box.addSeparator();
       }
    }

   @Override public void actionPerformed(ActionEvent evt) {
      String c = evt.getActionCommand();
      if (c.startsWith("REMOVE-") || c.startsWith("UP-") || c.startsWith("DOWN-")) {
         option_pane.setValue(c);
         current_dialog.setVisible(false);
       }
      else if (c.equals("Use Event")) {
         JComboBox<?> cb = (JComboBox<?>) evt.getSource();
         String nevt = (String) cb.getSelectedItem();
         if (nevt.startsWith("--")) new_event = null;
         else new_event = nevt;
       }
    }

}	// end of subclass TransitionDialog




/********************************************************************************/
/*										*/
/*	Action Panel								*/
/*										*/
/********************************************************************************/

private class TransitionActionPanel extends SwingListPanel<Action> {

   private static final long serialVersionUID = 1;

   TransitionActionPanel() {
      super(getActionSet());
    }

   @Override protected Action createNewItem() {
      return base_editor.getBaseModel().createNewAction();
    }

   @Override protected Action editItem(Object itm) {
      Action a = (Action) itm;
      return base_editor.getBaseModel().editAction(a);
    }

   @Override protected Action deleteItem(Object itm) {
      Action a = (Action) itm;
      return a;
    }

}	// end of subclass TransitionActionPanel




}	// end of class PebbleTransition




/* end of PebbleTransition.java */
