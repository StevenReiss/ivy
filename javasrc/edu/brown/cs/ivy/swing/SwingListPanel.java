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


package edu.brown.cs.ivy.swing;


import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.brown.cs.ivy.file.IvyLog;

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
private JButton up_button;
private JButton down_button;
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
   this(itemset,false);
}



protected SwingListPanel(SwingListSet<T> itemset,boolean orderable)
{
   int y = 0;
   item_set = itemset;
   
   if (!itemset.isOrderable()) orderable = false; 

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
   
   if (orderable) {
      up_button = new JButton("Move Up");
      addGBComponent(up_button,1,y++,1,1,0,0);
      down_button = new JButton("Move Down");
      addGBComponent(down_button,1,y++,1,1,0,0);
    }
   else {
      up_button = null;
      down_button = null;
    }

   JLabel dmy = new JLabel();
   addGBComponent(dmy,1,y++,1,1,0,0);

   item_list.addListSelectionListener(this);
   new_button.addActionListener(this);
   edit_button.addActionListener(this);
   delete_button.addActionListener(this);
   if (up_button != null) up_button.addActionListener(this);
   if (down_button != null) down_button.addActionListener(this);

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


public SwingListSet<T> getListModel()
{
   return item_set;
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
      if (up_button != null) up_button.setEnabled(false);
      if (down_button != null) down_button.setEnabled(false);
    }
   else {
      delete_button.setEnabled(true);
      if (sels.size() == 1) {
         int idx = item_list.getSelectedIndex();
         if (idx == 0 && up_button != null) up_button.setEnabled(false);
         else if (up_button != null) up_button.setEnabled(true);
         if (idx >= item_list.getModel().getSize() -1 && down_button != null) {
            down_button.setEnabled(false);
          }
         else if (down_button != null) down_button.setEnabled(true);
         edit_button.setEnabled(true);
       }
      else {
         edit_button.setEnabled(false);
         if (up_button != null) up_button.setEnabled(false);
         if (down_button != null) down_button.setEnabled(false);
       }
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
	       if (oitm != null) item_set.removeElement(oitm);
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
   else if (cmd.equals("MOVE UP")) {
      if (sels != null && sels.size() == 1) {
	 int idx = item_list.getSelectedIndex();
         if (idx > 0) item_set.moveElement(idx,idx-1); 
       }
    }
   else if (cmd.equals("MOVE DOWN")) {
      if (sels != null && sels.size() == 1) {
	 int idx = item_list.getSelectedIndex();
         if (idx+1 < item_set.getSize()) {
            item_set.moveElement(idx,idx+1);
          }
       }
    }
   else {
      IvyLog.logE("SWING","Unknown SwingList Data command: " + cmd);
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

















