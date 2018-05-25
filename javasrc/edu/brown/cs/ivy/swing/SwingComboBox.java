/********************************************************************************/
/*										*/
/*		SwingComboBox.java						*/
/*										*/
/*	Extension of a ComboBox to support undo 				*/
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

/**
 * <p>A wrapper for the Swing JComboBox class</p>
 * <p>Copyright (c) Formaria Ltd., 2008<br>
 * License:	 see license.txt
 * @version 1.0
 */

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingComboBox.java,v 1.21 2016/12/20 21:31:18 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingComboBox.java,v $
 * Revision 1.21  2016/12/20 21:31:18  spr
 * Formating changes.
 *
 * Revision 1.20  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.19  2015/02/14 18:46:18  spr
 * Clean up; add helper methods.
 *
 * Revision 1.18  2013/11/15 02:38:19  spr
 * Update imports; add features to combo box.
 *
 * Revision 1.17  2013/09/24 01:07:53  spr
 * data format
 *
 * Revision 1.16  2013-06-03 13:03:42  spr
 * *** empty log message ***
 *
 * Revision 1.15  2011-05-27 19:32:50  spr
 * Change copyrights.
 *
 * Revision 1.14  2011-02-17 23:16:12  spr
 * Minor cleanups.
 *
 * Revision 1.13  2010-09-16 23:37:42  spr
 * Avoid eclipse java errors.
 *
 * Revision 1.12  2010-07-24 02:01:02  spr
 * Add permanent option for freeze panes; code clean up; add mac support for text components.
 *
 * Revision 1.11  2009-10-02 00:18:30  spr
 * Import clean up.
 *
 * Revision 1.10  2009-09-18 01:44:15  spr
 * Fix up autocompletion and add Collection constructor.
 *
 * Revision 1.9  2009-09-17 02:00:45  spr
 * Add autocomplete, new grid options, fix up lists, add range scroll bar.
 *
 * Revision 1.8  2009-03-20 01:59:50  spr
 * Add enum-based choice box; add remove/update calls to lists; loosen numeric field checking.
 *
 * Revision 1.7  2007-08-10 02:11:23  spr
 * Cleanups from eclipse.
 *
 * Revision 1.6  2007-05-04 02:00:37  spr
 * Import fixups.
 *
 * Revision 1.5  2006/07/23 02:25:33  spr
 * Move list panel and its support to swing; move range slider to swing.
 *
 * Revision 1.4  2006/07/10 14:52:25  spr
 * Code cleanup.
 *
 * Revision 1.3  2005/05/07 22:25:44  spr
 * Updates for java 5.0
 *
 * Revision 1.2  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1.1.1  2003/03/17 19:37:41  spr
 * Initial version of the common code for various Brown projects.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.swing;


import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import java.awt.event.*;
import java.util.Collection;
import java.util.Vector;



public class SwingComboBox<T extends Object> extends JComboBox<T>
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String btn_label;
private UndoableEditSupport undo_support;
private Object selected_item;
private boolean auto_complete;
private boolean case_sensitive;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingComboBox(T [] data) 			{ this(null,data,null); }
public SwingComboBox(Vector<T> data)			{ this(null,data,null); }
public SwingComboBox(String lbl,T [] data)		{ this(lbl,data,null); }
public SwingComboBox(String lbl,Vector<T> data) 	{ this(lbl,data,null); }

public SwingComboBox(String lbl,T [] data,UndoableEditSupport es)
{
   this(lbl,data,false,es);
}

public SwingComboBox(String lbl,Vector<T> data,UndoableEditSupport es)
{
   this(lbl,data,false,es);
}


public SwingComboBox(String lbl,Collection<T> data,UndoableEditSupport es)
{
   this(lbl,data,false,es);
}


public SwingComboBox(String lbl,Collection<T> data,boolean autocomplete,UndoableEditSupport es)
{
   this(lbl,new Vector<T>(),autocomplete,es);
   if (data != null) {
      for (T t : data) addItem(t);
   }
}


public SwingComboBox(T [] data,boolean autocomplete)
{
   this(null,data,autocomplete,null);
}


public SwingComboBox(Vector<T> data,boolean autocomplete)
{
   this(null,data,autocomplete,null);
}


public SwingComboBox(Collection<T> data,boolean autocomplete)
{
   this(null,new Vector<T>(data),autocomplete,null);
}


public SwingComboBox(String lbl,T [] data,boolean auto,UndoableEditSupport es)
{
   super(data);
   btn_label = lbl;
   undo_support = es;
   selected_item = null;
   case_sensitive = true;
   setAutoComplete(auto);
}

public SwingComboBox(String lbl,Vector<T> data,boolean auto,UndoableEditSupport es)
{
   super(data);
   btn_label = lbl;
   undo_support = es;
   selected_item = null;
   case_sensitive = true;
   setAutoComplete(auto);
}




/********************************************************************************/
/*										*/
/*	Callback handlers							*/
/*										*/
/********************************************************************************/

@Override protected void fireActionEvent()
{
   super.fireActionEvent();
   if (undo_support != null) {
      ComboBoxCommand cmd = new ComboBoxCommand();
      undo_support.postEdit(cmd);
      selected_item = getSelectedItem();
    }
}



/********************************************************************************/
/*										*/
/*	Methods for handling autocompletion					*/
/*										*/
/********************************************************************************/

public void setCaseSensitive(boolean fg)
{
   case_sensitive = fg;
}


public void setAutoComplete(boolean fg)
{
   if (auto_complete == fg) return;

   auto_complete = fg;

   if (auto_complete) {
      setEditable(true);
      JTextComponent tc = (JTextComponent) getEditor().getEditorComponent();
      tc.setDocument(new AutoCompleteDocument<T>());
    }
}


public void clear()
{
   if (!auto_complete) return;
   JTextComponent tc = (JTextComponent) getEditor().getEditorComponent();
   tc.setText("");
}




/********************************************************************************/
/*										*/
/*	Auxilliary Methods							*/
/*										*/
/********************************************************************************/

public void setContents(Collection<T> cnts)
{
   removeAllItems();
   for (T x : cnts) {
      addItem(x);
    }
}


@Override public void paint(java.awt.Graphics g)
{
   super.paint(g);
}




/********************************************************************************/
/*										*/
/*	Command class for handling undo 					*/
/*										*/
/********************************************************************************/

private class ComboBoxCommand extends AbstractUndoableEdit {

   private Object old_item;
   private Object new_item;
   private static final long serialVersionUID = 1;

   public ComboBoxCommand() {
      old_item = selected_item;
      new_item = getSelectedItem();
    }

   @Override public String getPresentationName() {
      if (btn_label == null) {
	 if (new_item == null) return "ComboBox";
	 return new_item.toString();
       }
      if (new_item == null) return btn_label;
      return btn_label + "=" + new_item.toString();
    }

   @Override public void redo() throws CannotRedoException {
      super.redo();
      setSelectedItem(new_item);
    }

   @Override public void undo() throws CannotUndoException {
      super.undo();
      setSelectedItem(old_item);
    }

}   // end of subclass ComboBoxCommand



/********************************************************************************/
/*										*/
/*	Document for handling autocompletion					*/
/*										*/
/********************************************************************************/

private class AutoCompleteDocument<S extends T> extends PlainDocument {

    private ComboBoxModel<T> model;
    private JTextComponent text_comp;
    private boolean do_select = false;
    private boolean do_navigate = false;
    private int cur_index = -1;
    private static final long serialVersionUID = 1;


    public AutoCompleteDocument() {
      model = getModel();
      text_comp = (JTextComponent)getEditor().getEditorComponent();
      text_comp.addKeyListener(new AutoKeyer(this));
      text_comp.addFocusListener(new AutoFocus(this));
    }

    void handleKeyPressed(KeyEvent e) {
       if (!isPopupVisible()) setPopupVisible(true);
       updateSelection(e);
     }

    void handleFocusGained() {
       String contents = text_comp.getText();
       int n = model.getSize();
       for ( int i = 0; i < n; i++ ){
	  String currentItem = model.getElementAt( i ).toString();
	  if (currentItem.toLowerCase().equals( contents.toLowerCase())) {
	     do_navigate = true;
	     if ( i > 0 ){
		cur_index = i - 1;
		setSelectedItem(model.getElementAt(cur_index).toString());
	      }
	     else{
		cur_index = -1;
		setSelectedItem(null);
	      }
	     do_navigate = false;
	     break;
	   }
	}
     }

    private void updateSelection(KeyEvent e) {
      int key = e.getKeyCode();
      int count = getItemCount() - 1;

      if (( key == KeyEvent.VK_DOWN ) && ( cur_index < count )){
	 cur_index += 1;
	 do_navigate = true;
       }
      else if(( key == KeyEvent.VK_UP ) && ( cur_index > 0 )){
	 cur_index -= 1;
	 do_navigate = true;
       }

      if ( do_navigate ) {
	 String selection = (String)getItemAt( cur_index );
	 try {
	    insertString( 0, selection, null );
	    highLightText( 0 );
	  }
	 catch(Throwable ex) { }
	 do_navigate = false;
       }
     }

    @Override public void insertString(int offs,String str,AttributeSet a ) throws BadLocationException {
       if ( do_select ) return;
       if ( do_navigate ) super.remove( 0, getLength() );

       super.insertString( offs, str, a );
       Object item = lookupItem( getText( 0, getLength() ));
       if ( item != null ){
	  if (!do_navigate) setSelectedItem(item);
	  super.remove( 0, getLength() );
	  super.insertString( 0, item.toString(), a );
	  highLightText( offs + str.length() );
	}
       else{
	  if(isPopupVisible()) setPopupVisible( false );
	  text_comp.setSelectionEnd(0);
	  text_comp.setCaretPosition(text_comp.getText().length());
	}
     }

    @Override public void remove(int offs,int len) throws BadLocationException {
       if ( do_select ) return;
       super.remove( offs, len );
     }

    private void setSelectedItem(Object item) {
       do_select = true;
       model.setSelectedItem( item );
       do_select = false;
     }

    private Object lookupItem(String pattern) {
       int n = model.getSize();
       for ( int i = 0; i < n; i++ ){
	  String currentItem = model.getElementAt( i ).toString();
	  if (case_sensitive) {
	     if (currentItem.contains(pattern)) {
		cur_index = i;
		return currentItem;
	      }
	   }
	  else {
	     if ( currentItem.toLowerCase().contains( pattern.toLowerCase() )) {
		cur_index = i;
		return currentItem;
	      }
	   }
	}
       return null;
     }

    private void highLightText(int start) {
       text_comp.setSelectionStart(start);
       text_comp.setSelectionEnd(getLength());
     }

}	// end of innerclass AutoCompleteDocument



private class AutoKeyer extends KeyAdapter {

   private AutoCompleteDocument<?> auto_doc;

   AutoKeyer(AutoCompleteDocument<?> acd) {
      auto_doc = acd;
    }

   @Override public void keyPressed(KeyEvent e) {
      auto_doc.handleKeyPressed(e);
    }

}	// end of inner class AutoKeyer



private class AutoFocus implements FocusListener {

   private AutoCompleteDocument<?> auto_doc;

   AutoFocus(AutoCompleteDocument<?> acd) {
      auto_doc = acd;
    }

   @Override public void focusGained(FocusEvent e) {
      auto_doc.handleFocusGained();
    }

   @Override public void focusLost(FocusEvent e)		{ }

}	// end of innerclass AutoFocus



/********************************************************************************/
/*										*/
/*	Test program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   new SwingSetup();

   JFrame frm = new JFrame();
   SwingComboBox<String> cbx = new SwingComboBox<String>(new String [] {
					    "red", "green", "yellow", "blue", "orange",
					    "black", "brown", "white", "cyan", "magenta",
					    "gray" }, true);
   frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   frm.getContentPane().add(cbx);
   frm.pack();
   frm.setVisible(true);
}


}	// end of class SwingComboBox




/* end of SwingComboBox.java */
