/********************************************************************************/
/*										*/
/*		PebbleState.java						*/
/*										*/
/*	Implementation of a State in the automata				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/pebble/PebbleState.java,v 1.11 2015/11/20 15:09:22 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PebbleState.java,v $
 * Revision 1.11  2015/11/20 15:09:22  spr
 * Reformatting.
 *
 * Revision 1.10  2013-06-03 13:03:32  spr
 * Minor fixes
 *
 * Revision 1.9  2007-08-10 02:11:18  spr
 * Cleanups from eclipse.
 *
 * Revision 1.8  2006-12-01 03:22:53  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.7  2006/07/23 02:25:14  spr
 * Add support for action editing and creation.
 *
 * Revision 1.6  2006/07/10 14:52:23  spr
 * Code cleanup.
 *
 * Revision 1.5  2006/02/21 17:06:42  spr
 * Changes to Pebble to support external data models.
 *
 * Revision 1.4  2005/07/08 20:57:06  spr
 * Charles' upgrade to Pebble UI.
 *
 * Revision 1.3  2005/06/28 17:20:53  spr
 * UI enhancements (CAR)
 *
 * Revision 1.2  2005/05/07 22:25:41  spr
 * Updates for java 5.0
 *
 * Revision 1.1  2005/04/28 21:48:16  spr
 * Initial release of the pebble automata editor.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.pebble;


import edu.brown.cs.ivy.petal.PetalHelper;
import edu.brown.cs.ivy.petal.PetalNodeDefault;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.swing.SwingListPanel;
import edu.brown.cs.ivy.swing.SwingListSet;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;



class PebbleState extends PetalNodeDefault implements PebbleConstants.State, PebbleConstants
{




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private PebbleEditor	base_editor;
private String		state_name;
private String		state_label;
private boolean 	is_start;
private boolean 	is_accept;
private boolean 	is_error;
private boolean 	is_ignore;
private Collection<Action> state_actions;
private PebbleState	default_next;

private StateIcon	state_icon;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

PebbleState(PebbleEditor pe,String name)
{
   this(pe,name,false);
}


PebbleState(PebbleEditor pe,String name,boolean start)
{
   base_editor = pe;
   state_name = name;
   is_start = start;
   is_accept = false;
   is_error = false;
   is_ignore = false;
   state_actions = new ArrayList<Action>();
   default_next = this;
   state_label = null;
   state_icon = new StateIcon();
   state_icon.reset();

   JLabel lbl = new JLabel(name,state_icon,SwingConstants.CENTER);
   lbl.setHorizontalTextPosition(SwingConstants.CENTER);
   Dimension d = lbl.getPreferredSize();
   lbl.setSize(d);
   lbl.setMinimumSize(d);
   lbl.setBackground(Color.YELLOW);
   setComponent(lbl);
}





/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public Point findPortPoint(Point at,Point from)
{
   return PetalHelper.findOvalPortPoint(getComponent().getBounds(),at,from);
}



@Override public String getToolTip(Point _at)
{
   return "State " + state_name;
}



@Override public String getName() 			{ return state_name; }
@Override public String getLabel()			{ return state_label; }
boolean isStart()				{ return is_start; }
boolean isAccept()				{ return is_accept; }
boolean isError()				{ return is_error; }
boolean isIgnore()				{ return is_ignore; }

@Override public StateType getStateType()
{
   StateType state = StateType.NORMAL;
   if (isAccept()) state = StateType.ACCEPT;
   else if (isError()) state = StateType.ERROR;
   else if (isIgnore()) state = StateType.IGNORE;
   return state;
}


PebbleState getDefaultNextState()
{
   if (default_next == null || !getAllStates().contains(default_next)) {
      default_next = this;
    }

   return default_next;
}



void setName(String name)
{
   state_name = name;
   reset();
}



void setStateType(StateType t)
{
   is_accept = (t == StateType.ACCEPT);
   is_error = (t == StateType.ERROR);
   is_ignore = (t == StateType.IGNORE);
   reset();
}


void setDefaultNextState(String s)
{
   default_next = base_editor.getEditModel().findState(s);
}



boolean getUsesActions()
{
   return base_editor.getBaseModel().getUsesStateActions();
}

@Override public Iterable<Action> getActions()
{
   return new ArrayList<Action>(state_actions);
}



@Override public void addAction(Action a)
{
   if (a != null && !state_actions.contains(a)) state_actions.add(a);
}

@Override public void removeAction(Action a)
{
   state_actions.remove(a);
}



SwingListSet<Action> getActionSet()
{
   return new SwingListSet<Action>(new ArrayList<Action>(state_actions));
}


Collection<?> getAllStates()
{
   return base_editor.getAllStates();
}



String getStateLabel()			{ return state_label; }
void setStateLabel(String s)		{
    state_label = s;
    reset();
}

public String getGUIText() {
    if (state_label != null) {
	return state_label;
    }
	return getName();
}




/********************************************************************************/
/*										*/
/*	Drawing related methods 						*/
/*										*/
/********************************************************************************/

void reset()
{
   state_icon.reset();

   JLabel lbl = (JLabel) getComponent();
   lbl.setText(getGUIText());
   Dimension d = lbl.getPreferredSize();
   lbl.setSize(d);
   lbl.setMinimumSize(d);
   lbl.setBackground(Color.YELLOW);
}




/********************************************************************************/
/*										*/
/*	Popup menu for states							*/
/*										*/
/********************************************************************************/

@Override public boolean handleMouseClick(MouseEvent evt)
{
   if ((evt.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == 0) return false;

   StateDialog sd = new StateDialog(this);
   sd.process((Component) evt.getSource());

   return true;
}




/********************************************************************************/
/*										*/
/*	Dialog class								*/
/*										*/
/********************************************************************************/

private class StateDialog implements ActionListener
{
   private PebbleState	  for_state;
   private SwingGridPanel message_box;
   private StateActionPanel action_panel;
   private String	  next_state;
   private StateType	  state_type;
   private JTextField	  dstate_name;
   private JTextField	  dstate_label;

   StateDialog(PebbleState ps) {
      for_state = ps;
      message_box = null;
    }
   /** Utility method to trim whitespace and turn blank strings
    * into nulls for use with for_state.set*
    */
   private String normalizeInput(String input) {
       String res = input.trim();
       return res.length() == 0 ? null : res;
   }

   void process(Component c) {
      layoutPanel();
      int sts = JOptionPane.showOptionDialog(c,message_box,null,
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE,
						null,null,null);
      if (sts == 0) {
	 for_state.setStateType(state_type);
	 String newName = normalizeInput(dstate_name.getText());
	 if (!newName.equals(for_state.getName())) {
	    if (for_state.base_editor.getEditModel().findState(newName) != null) {
		JOptionPane.showMessageDialog(c, "State name must be unique, ignored.");
	    } else {
		for_state.setName(normalizeInput(dstate_name.getText()));
	    }
	 }
	 if (next_state != null) for_state.setDefaultNextState(next_state);
	 for_state.setStateLabel(normalizeInput(dstate_label.getText()));
	 if (action_panel != null) {
	    state_actions.clear();
	    for (Action a : action_panel.getItemSet()) state_actions.add(a);
	  }
       }

      message_box = null;
    }

   private void layoutPanel() {
      action_panel = null;
      message_box = new SwingGridPanel();
      message_box.beginLayout();
      message_box.addBannerLabel("State Dialog");
      message_box.addSeparator();

      dstate_name = message_box.addTextField("State Name",for_state.getName(),this,null);
      dstate_label = message_box.addTextField("State Label",for_state.getStateLabel(),this,null);
      message_box.addSeparator();

      state_type = for_state.getStateType();
      message_box.addChoice("State Type",StateType.values(),state_type,this);
      message_box.addSeparator();

      if (for_state.getUsesActions()) {
	 action_panel = new StateActionPanel();
	 message_box.addRawComponent("Actions",action_panel);
	 action_panel.addActionListener(this);
       }

      next_state = for_state.getDefaultNextState().getName();
      Object [] sts = for_state.getAllStates().toArray();
      int idx = 0;
      for (int i = 0; i < sts.length; ++i) {
	 PebbleState ps = (PebbleState) sts[i];
	 if (ps.getName().equals(next_state)) idx = i;
	 if (ps.getStateLabel() == null) {
	     sts[i] = ps.getName();
	 } else {
	     sts[i] = ps.getStateLabel() + " (" + ps.getName() + ")";
	 }
       }
      message_box.addChoice("Default Next State",sts,idx,this);
    }

   @Override public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("State Type")) {
         JComboBox<?> cbx = (JComboBox<?>) e.getSource();
         state_type = (StateType) cbx.getSelectedItem();
       }
      /*
      else if (e.getActionCommand().equals("State Name")) {
         JTextField tf = (JTextField) e.getSource();
         if (tf.getText().trim().length() > 0) dstate_name = tf.getText().trim();
       }
      else if (e.getActionCommand().equals("State Label")) {
         JTextField tf = (JTextField) e.getSource();
         String s = tf.getText().trim();
         if (s.length() == 0) dstate_label = null;
         else dstate_label = s;
       } */
      else if (e.getActionCommand().equals("Default Next State")) {
         JComboBox<?> cbx = (JComboBox<?>) e.getSource();
         next_state = (String) cbx.getSelectedItem();
       }
    }

}	// end of subclass StateDialog




/********************************************************************************/
/*										*/
/*	Icon class								*/
/*										*/
/********************************************************************************/

private class StateIcon implements Icon {

   private Color fill_color;
   private int border_width;
   private int icon_width;
   private int icon_height;

   StateIcon() {
      fill_color = Color.white;
      border_width = 1;
      icon_width = 20;
      icon_height = 20;
    }

   @Override public int getIconWidth()				{ return icon_width; }
   @Override public int getIconHeight()				{ return icon_height; }

   @Override public void paintIcon(Component _c,Graphics g,int x,int y) {
      Color oc = g.getColor();

      g.setColor(fill_color);
      g.fillOval(x,y,icon_width,icon_height);

      g.setColor(Color.BLACK);
      for (int i = 0; i < border_width; ++i) {
	 g.drawOval(x+i,y+i,icon_width-2*i-1,icon_height-2*i-1);
       }

      g.setColor(oc);
    }

   void reset() {
      if (isIgnore()) fill_color = Color.LIGHT_GRAY;
      else if (isError()) fill_color = Color.RED;
      else if (isAccept()) fill_color = Color.GREEN;
      else fill_color = Color.WHITE;

      if (isStart()) border_width = 4;
      else border_width = 1;

      String nm = getGUIText();
      JLabel tlbl = new JLabel(nm);
      Dimension sz = tlbl.getPreferredSize();
      int delta = (border_width - 1)*2;
      icon_width = sz.width + 6 + delta;
      icon_height = sz.height*2 + 5;

      if (icon_width > 1000) {
	 System.err.println("SIZE " + icon_width + " " + icon_height);
	 System.err.println(tlbl.getPreferredSize() + " " + sz + " " + border_width + " " + delta);
       }
    }

}	// end of subclass StateIcon




/********************************************************************************/
/*										*/
/*	Action Panel								*/
/*										*/
/********************************************************************************/

private class StateActionPanel extends SwingListPanel<Action> {

   private static final long serialVersionUID = 1;

   StateActionPanel() {
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

}	// end of subclass StateActionPanel



}	// end of class PebbleState




/* end of PebbleState.java */
