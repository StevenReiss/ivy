/********************************************************************************/
/*										*/
/*		SwingListPanel.java						*/
/*										*/
/*	Generic panel for managing a list of items				*/
/*										*/
/********************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss		      */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingListPanel.java,v 1.9 2019/09/12 12:48:51 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingListPanel.java,v $
 * Revision 1.9  2019/09/12 12:48:51  spr
 * Minor extensions.  Code cleanup
 *
 * Revision 1.8  2018/08/02 15:10:54  spr
 * Fix imports.  Prepare for java 10.
 *
 * Revision 1.7  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.6  2015/02/14 18:46:18  spr
 * Clean up; add helper methods.
 *
 * Revision 1.5  2013-06-03 13:03:42  spr
 * *** empty log message ***
 *
 * Revision 1.4  2011-05-27 19:32:51  spr
 * Change copyrights.
 *
 * Revision 1.3  2009-09-17 02:00:45  spr
 * Add autocomplete, new grid options, fix up lists, add range scroll bar.
 *
 * Revision 1.2  2007-05-04 02:00:37  spr
 * Import fixups.
 *
 * Revision 1.1  2006/07/23 02:25:33  spr
 * Move list panel and its support to swing; move range slider to swing.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;


import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;



public abstract class SwingListPanel<T extends Object> extends SwingGridPanel
			implements ActionListener, ListSelectionListener
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JList<T> item_list;
private JButton new_button;
private JButton edit_button;
private JButton delete_button;
private SwingListSet<T> item_set;
private SwingEventListenerList<ActionListener>	action_listeners;

private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected SwingListPanel(SwingListSet<T> itemset)
{
   int y = 0;
   item_set = itemset;

   action_listeners = new SwingEventListenerList<>(ActionListener.class);

   item_list = new JList<T>(item_set);
   item_list.setVisibleRowCount(6);
   addGBComponent(new JScrollPane(item_list),0,y,1,0,1,1);

   new_button = new JButton("New");
   addGBComponent(new_button,1,y++,1,1,0,0);

   edit_button = new JButton("Edit");
   addGBComponent(edit_button,1,y++,1,1,0,0);

   delete_button = new JButton("Delete");
   addGBComponent(delete_button,1,y++,1,1,0,0);

   JLabel dmy = new JLabel();
   addGBComponent(dmy,1,y++,1,1,0,0);

   item_list.addListSelectionListener(this);
   new_button.addActionListener(this);
   edit_button.addActionListener(this);
   delete_button.addActionListener(this);

   fixButtons();
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public void setVisibleRowCount(int ct)
{
   item_list.setVisibleRowCount(ct);
   invalidate();
}


public void setBackground(Color c) 
{
   super.setBackground(c);
   if (item_list != null) {
      item_list.setBackground(c);
      item_list.setOpaque(false);
    }
}


@Override public void addActionListener(ActionListener al)
{
   action_listeners.add(al);
}


@Override public void removeActionListener(ActionListener al)
{
   action_listeners.remove(al);
}




/********************************************************************************/
/*										*/
/*	Update methods								*/
/*										*/
/********************************************************************************/

private void fixButtons()
{
   List<T> sels = item_list.getSelectedValuesList();
   if (sels == null || sels.size() == 0) {
      edit_button.setEnabled(false);
      delete_button.setEnabled(false);
    }
   else {
      delete_button.setEnabled(true);
      if (sels.size() == 1) edit_button.setEnabled(true);
      else edit_button.setEnabled(false);
    }
}




/********************************************************************************/
/*										*/
/*	Event Callbacks 							*/
/*										*/
/********************************************************************************/

@Override public void actionPerformed(ActionEvent evt)
{
   List<T> sels = item_list.getSelectedValuesList();
   String cmd = evt.getActionCommand().toUpperCase();
   if (cmd.equals("NEW")) {
      int osz = item_set.getSize();
      T itm = createNewItem();
      if (itm != null) {
	 itm = editItem(itm);
	 if (itm != null) {
	    item_set.addElement(itm);
	    item_list.setSelectedValue(itm,true);
	    triggerActionEvent("ListUpdated");
	  }
       }
      else if (osz != item_set.getSize()) {
         triggerActionEvent("ListUpdated");
       }
    }
   else if (cmd.equals("EDIT")) {
      if (sels != null && sels.size() == 1) {
	 T itm = editItem(sels.get(0));
	 if (itm != null) {
	    if (itm == sels.get(0)) item_set.editElement(itm);
	    else {
	       T oitm = deleteItem(sels.get(0));
	       item_set.removeElement(oitm);
	       item_set.addElement(itm);
	       item_list.setSelectedValue(itm,true);
	       triggerActionEvent("ListUpdated");
	     }
	  }
       }
    }
   else if (cmd.equals("DELETE")) {
      for (T itm : sels) {
	 item_set.removeElement(itm);
       }
      if (sels.size() > 0) triggerActionEvent("ListUpdated");
    }
   else {
      System.err.println("IVY: Unknown Data command: " + cmd);
    }
}

@Override public void valueChanged(ListSelectionEvent evt) {
   fixButtons();
}



public void triggerActionEvent(String cmd)
{
   ActionEvent evt = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,cmd);
   for (ActionListener al : action_listeners) {
      al.actionPerformed(evt);
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public SwingListSet<T> getItemSet()		{ return item_set; }



/********************************************************************************/
/*										*/
/*	Abstract methods to handle editing					*/
/*										*/
/********************************************************************************/

protected abstract T createNewItem();
protected abstract T editItem(Object itm);
protected abstract T deleteItem(Object itm);



}	// end of class SwingListPanel




/* end of SwingListPanel.java */

















