/********************************************************************************/
/*										*/
/*		SwingGridPanel.java						*/
/*										*/
/*	JPanel with support for GridBagLayout					*/
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


package edu.brown.cs.ivy.swing;



import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;

import edu.brown.cs.ivy.file.IvyI18N;

import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;


public class SwingGridPanel extends JPanel
{



/********************************************************************************/
/*										*/
/*	Local Storage								*/
/*										*/
/********************************************************************************/

protected int	  y_count;
protected HashMap<String,Object> value_map;
protected HashMap<Object,String> tag_map;
protected HashMap<Object,JLabel> label_map;
protected Insets  inset_values;
protected transient IvyI18N i18n_map;

private transient Map<Component,List<Component>> dependent_items;
private transient DepListener dep_listener;

private Box			bottom_box;
private transient UndoableEditSupport undo_support;
private transient ActionListener action_listener;
private String			action_command;
private transient Map<Object,JFileChooser> chooser_map;

protected JLabel		banner_prototype;
protected JLabel		section_prototype;
protected JLabel		label_prototype;


private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingGridPanel()
{
   this(null,null);
}


public SwingGridPanel(UndoableEditSupport ued)
{
   this(ued,null);
}

public SwingGridPanel(UndoableEditSupport ued,IvyI18N i18n)
{
   super(new GridBagLayout(),true);

   undo_support = ued;
   inset_values = null;
   i18n_map = i18n;
   setInsets(2);
   action_listener = null;
   action_command = "PANEL";
   banner_prototype = null;
   section_prototype = null;
   label_prototype = null;
   chooser_map = new HashMap<>();
   label_map = new HashMap<>();
   tag_map = new HashMap<>();
   dependent_items = new WeakHashMap<>();
   dep_listener = new DepListener();

   beginLayout();
}



/********************************************************************************/
/*										*/
/*	Access methods to get button information				*/
/*										*/
/********************************************************************************/

public final String getLabelForComponent(Object c)
{
   return tag_map.get(c);
}


public final JLabel getJLabelForComponent(Object c)
{
   return label_map.get(c);
}


public final Object getComponentForLabel(String c)
{
   if (c == null) return null;

   for (Map.Entry<Object,String> ent : tag_map.entrySet()) {
      if (c.equals(ent.getValue())) {
	 Object rslt = ent.getKey();
	 if (rslt instanceof Component) return rslt;
      }
    }

   return null;
}



public final void saveValues()
{
   value_map = new HashMap<String,Object>();

   for (Iterator<Map.Entry<Object,String>> it = tag_map.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<Object,String> me = it.next();
      String lbl = me.getValue();
      Object cmp = me.getKey();
      Object nval = null;
      if (cmp instanceof JComboBox) {
	 JComboBox<?> cbx = (JComboBox<?>) cmp;
	 nval = Integer.valueOf(cbx.getSelectedIndex());
       }
      else if (cmp instanceof JTextComponent) {
	 JTextComponent tc = (JTextComponent) cmp;
	 nval = tc.getText();
       }
      if (nval != null) {
	 if (undo_support != null) {
	    ValueSetCommand cmd = new ValueSetCommand(lbl,nval);
	    undo_support.postEdit(cmd);
	  }
	 value_map.put(lbl,nval);
       }
    }
}



public final void restoreValues()
{
   if (value_map == null) return;

   for (Iterator<Map.Entry<Object,String>> it = tag_map.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<Object,String> me = it.next();
      String lbl = me.getValue();
      Object cmp = me.getKey();
      if (cmp instanceof JComboBox) {
	 JComboBox<?> cbx = (JComboBox<?>) cmp;
	 Integer ivl = (Integer) value_map.get(lbl);
	 cbx.setSelectedIndex(ivl.intValue());
       }
      else if (cmp instanceof JTextComponent) {
	 JTextComponent tc = (JTextComponent) cmp;
	 String svl = (String) value_map.get(lbl);
	 tc.setText(svl);
       }
    }
}



private class ValueSetCommand extends AbstractUndoableEdit {

   private String value_label;
   private transient Object value_object;
   private transient Object old_object;
   private static final long serialVersionUID = 1;

   ValueSetCommand(String lbl,Object nval) {
      value_label = lbl;
      value_object = nval;
      old_object = value_map.get(lbl);
    }

   @Override public void redo() throws CannotRedoException {
      super.redo();
      value_map.put(value_label,value_object);
    }

   @Override public void undo() throws CannotUndoException {
      super.undo();
      value_map.put(value_label,old_object);
    }

}	// end of subclass ValueSetCommand




/********************************************************************************/
/*										*/
/*	Access methods for layout						*/
/*										*/
/********************************************************************************/

public final void setInsets(Insets i)
{
   inset_values = i;
}



public final void setInsets(int v)
{
   inset_values = new Insets(v,v,v,v);
}



@Override public final Insets getInsets()		{ return inset_values; }



public final void setI18N(IvyI18N i18n)
{
   i18n_map = i18n;
}



/********************************************************************************/
/*										*/
/*	Support methods for setting up a panel					*/
/*										*/
/********************************************************************************/

public final void beginLayout()
{
   y_count = 0;
   value_map = null;
   tag_map = new HashMap<>();
   label_map = new HashMap<>();
   bottom_box = null;
   removeAll();
}



public final JLabel addBannerLabel(String txt)
{
   JLabel lbl = createLabel(txt,SwingConstants.CENTER,banner_prototype);
   addGBComponent(lbl,0,y_count++,0,1,1,0);

   return lbl;
}



public final JLabel addSectionLabel(String txt)
{
   JLabel lbl = createLabel(txt,SwingConstants.LEADING,section_prototype);
   addGBComponent(lbl,0,y_count++,0,1,1,0);

   return lbl;
}



public final void addSeparator()
{
   addGBComponent(new JSeparator(),0,y_count++,0,1,1,0);
}



public final void addExpander()
{
   addGBComponent(new JSeparator(),0,y_count++,0,1,10,10);
}



public final JLabel addDescription(String lbl,String val)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   JLabel desc = new JLabel(" " + mapText(val));
   Font fnt = desc.getFont();
   fnt = fnt.deriveFont(Font.PLAIN);
   desc.setFont(fnt);

   addGBComponent(desc,1,y_count++,0,1,10,0);

   addDependency(desc,tag);

   return desc;
}



/********************************************************************************/
/*										*/
/*	Raw components								*/
/*										*/
/********************************************************************************/

public final Component addRawComponent(String lbl, Component c)
{
   JLabel tag = null;
   if (lbl != null) {
      tag = createLabel(lbl);
      addGBComponent(tag,0,y_count,1,1,0,0);
      tag_map.put(c,lbl);
      label_map.put(c,tag);
      addDependency(c,tag);
    }

   addGBComponent(c,1,y_count++,0,1,10,0);

   addDependency(c,tag);

   return c;
}



public final Component addLabellessRawComponent(String lbl, Component c)
{
    addGBComponent(c,0,y_count++,0,1,10,10);

    if (lbl != null) tag_map.put(c, lbl);

    return c;
}


public final Component addLabellessRawComponent(String lbl, Component c,boolean expx,boolean expy)
{
    addGBComponent(c,0,y_count++,0,1,(expx ? 10 : 0),(expy ? 10 : 0));

    tag_map.put(c, lbl);

    return c;
}



/********************************************************************************/
/*										*/
/*	Choice/option components						*/
/*										*/
/********************************************************************************/

public final <T> SwingComboBox<T> addChoice(String lbl,Collection<T> data,int idx,ActionListener cb)
{
   return addChoice(lbl,data,idx,false,cb);
}



public final <T> SwingComboBox<T> addChoice(String lbl,Collection<T> data,int idx,boolean compl,ActionListener cb)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   SwingComboBox<T> cbx = new SwingComboBox<T>(lbl,data,compl,undo_support,i18n_map);

   if (data != null && data.size() > 0 && idx >= 0) cbx.setSelectedIndex(idx);
   addGBComponent(cbx,1,y_count++,1,1,10,0);

   tag_map.put(cbx,lbl);
   addDependency(cbx,tag);

   cbx.setActionCommand(lbl);
   if (cb != null) cbx.addActionListener(cb);

   return cbx;
}




public final <T> SwingComboBox<T> addChoice(String lbl,T [] data,int idx,ActionListener cb)
{
   return addChoice(lbl,data,idx,false,cb);
}



public final <T> SwingComboBox<T> addChoice(String lbl,T [] data,int idx,boolean compl,ActionListener cb)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   SwingComboBox<T> cbx = new SwingComboBox<T>(lbl,data,compl,undo_support,i18n_map);
   cbx.setActionCommand(lbl);
   if (cb != null) cbx.addActionListener(cb);
   if (data != null && data.length > 0 && idx > 0) cbx.setSelectedIndex(idx);
   addGBComponent(cbx,1,y_count++,1,1,10,0);

   tag_map.put(cbx,lbl);
   addDependency(cbx,tag);

   return cbx;
}



public final <T> SwingComboBox<T> addChoice(String lbl,Collection<T> data,Object sel,ActionListener cb)
{
   return addChoice(lbl,data,sel,false,cb);
}



public final <T> SwingComboBox<T> addChoice(String lbl,Collection<T> data,Object sel,boolean compl,ActionListener cb)
{
   int idx = -1;
   int i = 0;
   for (T d : data) {
      if (d.equals(sel)) {
	 idx = i;
	 break;
      }
      ++i;
   }

   return addChoice(lbl,data,idx,compl,cb);
}



public final <T> SwingComboBox<T> addChoice(String lbl,T [] data,T sel,ActionListener cb)
{
   return addChoice(lbl,data,sel,false,cb);
}



public final <T> SwingComboBox<T> addChoice(String lbl,T [] data,T sel,boolean compl,ActionListener cb)
{
   int idx = -1;
   for (int i = 0; i < data.length; ++i) {
      if (data[i].equals(sel)) idx = i;
    }

   return addChoice(lbl,data,idx,compl,cb);
}



public final <T extends Enum<?>> SwingComboBox<T> addChoice(String lbl,T v,ActionListener cb)
{
   return addChoice(lbl,v,false,cb);
}



public final <T extends Enum<?>> SwingComboBox<T> addChoice(String lbl,T v,boolean compl,ActionListener cb)
{
   Vector<T> vs = null;

   try {
      Class<?> c = v.getClass();
      Object[] vals = c.getEnumConstants();
      vs = new Vector<T>();
      for (Object o : vals) {
	 @SuppressWarnings("unchecked") T x = (T) o;
	 vs.add(x);
      }
    }
   catch (Exception e) {
      System.err.println("SWING: Problem determining enumeration choices: " + e);
    }

   if (vs == null) return null;

   return addChoice(lbl,vs,v,compl,cb);
}



/********************************************************************************/
/*										*/
/*	Boolean buttons 							*/
/*										*/
/********************************************************************************/

public final JCheckBox addBoolean(String lbl,boolean val,ActionListener cb)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   JCheckBox cbx = new SwingCheckBox(i18n_map);
   cbx.setSelected(val);
   cbx.setActionCommand(lbl);
   cbx.setOpaque(false);

   if (cb != null) cbx.addActionListener(cb);
   addGBComponent(cbx,1,y_count++,1,1,0,0);

   tag_map.put(cbx,lbl);
   addDependency(cbx,tag);

   return cbx;
}




/********************************************************************************/
/*										*/
/*	Button set buttons							*/
/*										*/
/********************************************************************************/

public final JList<String> addButtonSet(String lbl,Map<String,Boolean> values,ListSelectionListener cb)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   String [] valarr = values.keySet().toArray(new String[values.size()]);
   JList<String> lst = new JList<>(valarr);
   lst.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
   for (int i = 0; i < valarr.length; ++i) {
      if (values.get(valarr[i]) == Boolean.TRUE) {
	 lst.addSelectionInterval(i,i);
       }
    }
   if (cb != null) lst.addListSelectionListener(cb);
   lst.setOpaque(false);
   int rows = Math.min(valarr.length,3);
   lst.setVisibleRowCount(rows);
   lst.setLayoutOrientation(JList.HORIZONTAL_WRAP);

   addGBComponent(lst,1,y_count++,1,1,0,0);

   tag_map.put(lst,lbl);
   addDependency(lst,tag);

   return lst;
}




/********************************************************************************/
/*										*/
/*	Numeric fields								*/
/*										*/
/********************************************************************************/

public final SwingNumericField addNumericField(String lbl,double min,double max,double val,
						  ActionListener cb1)
{												
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   SwingNumericField tfld = new SwingNumericField(val,10,min,max);
   tfld.setActionCommand(lbl);

   if (cb1 != null) tfld.addActionListener(cb1);

   addGBComponent(tfld,1,y_count++,0,1,10,0);

   tag_map.put(tfld,lbl);
   addDependency(tfld,tag);

   return tfld;
}




public final SwingNumericField addNumericField(String lbl,int min,int max,int val,
						  ActionListener cb1)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   SwingNumericField tfld = new SwingNumericField(val,10,min,max);
   tfld.setActionCommand(lbl);

   if (cb1 != null) tfld.addActionListener(cb1);

   addGBComponent(tfld,1,y_count++,0,1,10,0);

   tag_map.put(tfld,lbl);
   addDependency(tfld,tag);

   return tfld;
}




public final SwingRangeSlider addRange(String lbl,int min,int max,int dec,int val,ChangeListener cb)

{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   SwingRangeSlider sldr = new SwingRangeSlider(min,max,dec,val);

   if (cb != null) sldr.addChangeListener(cb);

   addGBComponent(sldr,1,y_count++,0,1,10,0);

   tag_map.put(sldr,lbl);
   addDependency(sldr,tag);

   return sldr;
}



public final SwingDimensionChooser addDimensionField(String lbl,int w,int h,ActionListener cb1)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   SwingDimensionChooser tfld = new SwingDimensionChooser(lbl,w,h);

   if (cb1 != null) tfld.addActionListener(cb1);

   addGBComponent(tfld,1,y_count++,0,1,10,0);

   tag_map.put(tfld,lbl);
   addDependency(tfld,tag);

   return tfld;
}




/********************************************************************************/
/*										*/
/*	Text fields								*/
/*										*/
/********************************************************************************/

public final JTextField addTextField(String lbl,String val,ActionListener cb1,
		UndoableEditListener cb2)
{
   return addTextField(lbl,val,24,cb1,cb2);
}



public final JTextField addTextField(String lbl,String val,int wid,ActionListener cb1,
		UndoableEditListener cb2)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   String val1 = mapText(val);
   JTextField tfld = new SwingTextField(val1,wid,i18n_map);
   Document doc = tfld.getDocument();
   tfld.setActionCommand(lbl);

   if (cb1 != null) {
      tfld.addActionListener(cb1);
      tfld.addFocusListener(new TextUnfocus());
    }
   if (cb2 != null) doc.addUndoableEditListener(cb2);

   addGBComponent(tfld,1,y_count++,0,1,10,0);

   tag_map.put(tfld,lbl);
   tag_map.put(doc,lbl);
   addDependency(tfld,tag);
   label_map.put(doc,tag);

   return tfld;
}



public final JTextArea addTextArea(String lbl,String val,UndoableEditListener cb)
{
   return addTextArea(lbl,val,10,60,cb);
}



public final JTextArea addTextArea(String lbl,String val,int row,int col,UndoableEditListener cb)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   JTextArea tarea = new SwingTextArea(val,row,col);
   Document doc = tarea.getDocument();

   if (cb != null) doc.addUndoableEditListener(cb);

   addGBComponent(new JScrollPane(tarea),1,y_count++,0,1,10,10);

   tarea.setBorder(new LineBorder(SwingColors.SWING_DARK_COLOR));

   tag_map.put(tarea,lbl);
   tag_map.put(doc,lbl);
   addDependency(tarea,tag);
   label_map.put(doc,tag);

   return tarea;
}




/********************************************************************************/
/*										*/
/*	File requests								*/
/*										*/
/********************************************************************************/

public final JTextField addFileField(String lbl,File val,int md,
					Iterable<FileFilter> filters,
					ActionListener cb1,
					CaretListener cb3,
					UndoableEditListener cb2)
{
   String fnm = null;
   if (val != null) fnm = val.getPath();
   return localAddFileField(lbl,fnm,md,filters,null,cb1,cb3,cb2);
}



public final JTextField addFileField(String lbl,File val,int md,
					Iterable<FileFilter> filters,
					ActionListener cb1,
					UndoableEditListener cb2)
{
   String fnm = null;
   if (val != null) fnm = val.getPath();
   return localAddFileField(lbl,fnm,md,filters,null,cb1,null,cb2);
}



public final JTextField addFileField(String lbl,File val,int md,
					FileFilter filter,
					ActionListener cb1,
					UndoableEditListener cb2)
{
   String fnm = null;
   if (val != null) fnm = val.getPath();
   return addFileField(lbl,fnm,md,filter,cb1,cb2);
}



public final JTextField addFileField(String lbl,File val,int md,ActionListener cb1,
					UndoableEditListener cb2)
{
   String fnm = null;
   if (val != null) fnm = val.getPath();
   return localAddFileField(lbl,fnm,md,null,null,cb1,null,cb2);
}



public final JTextField addFileField(String lbl,String val,int md,ActionListener cb1,
					UndoableEditListener cb2)
{
   return localAddFileField(lbl,val,md,(Iterable<FileFilter>) null,null,cb1,null,cb2);
}




public final JTextField addFileField(String lbl,String val,int md,
					Iterable<FileFilter> filters,
					ActionListener cb1,
					UndoableEditListener cb2)
{
   return localAddFileField(lbl,val,md,filters,null,cb1,null,cb2);
}



public final JTextField addFileField(String lbl,String val,int md,
					Iterable<FileFilter> filters,
					ActionListener cb1,
					CaretListener cb3,
					UndoableEditListener cb2)
{
   return localAddFileField(lbl,val,md,filters,null,cb1,cb3,cb2);
}



public final JTextField addFileField(String lbl,String val,int md,
      Iterable<FileFilter> filters,
      FileSystemView fsv,
      ActionListener cb1,
      CaretListener cb3,
      UndoableEditListener cb2)
{
   return localAddFileField(lbl,val,md,filters,fsv,cb1,cb3,cb2);
}



public final JTextField addFileField(String lbl,String val,int md,
					FileFilter filter,
					ActionListener cb1,
					UndoableEditListener cb2)
{
   List<FileFilter> fl = null;
   if (filter != null) {
      fl = new ArrayList<FileFilter>();
      fl.add(filter);
    }

   return localAddFileField(lbl,val,md,fl,null,cb1,null,cb2);
}



public final JTextField addFileField(String lbl,String val,int md,
					FileFilter filter,
					ActionListener cb1,
					CaretListener cb3,
					UndoableEditListener cb2)
{
   List<FileFilter> fl = null;
   if (filter != null) {
      fl = new ArrayList<FileFilter>();
      fl.add(filter);
    }

   return localAddFileField(lbl,val,md,fl,null,cb1,cb3,cb2);
}



private JTextField localAddFileField(String lbl,String val,int md,
					Iterable<FileFilter> filters,
					FileSystemView fsv,
					ActionListener cb1,
					CaretListener cb3,
					UndoableEditListener cb2)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   JTextField tfld = new SwingTextField(val,32,i18n_map);
   Document doc = tfld.getDocument();
   tfld.setActionCommand(lbl);

   if (cb1 != null) {
      tfld.addActionListener(cb1);
      tfld.addFocusListener(new TextUnfocus());
    }

   if (cb2 != null) doc.addUndoableEditListener(cb2);

   if (cb3 != null) tfld.addCaretListener(cb3);

   addGBComponent(tfld,1,y_count,1,1,10,0);

   JButton browser = new SwingButton("Browse",i18n_map);
   BrowseListener bl = new BrowseListener(tfld,md,filters,fsv);
   browser.addActionListener(bl);
   addGBComponent(browser,2,y_count++,1,1,0,0);

   chooser_map.put(tfld,bl.getFileChooser());
   chooser_map.put(lbl,bl.getFileChooser());

   tag_map.put(tfld,lbl);
   tag_map.put(doc,lbl);
   label_map.put(doc,tag);
   addDependency(tfld,tag);
   addDependency(tfld,browser);

   return tfld;
}



public JFileChooser getFileChooser(JTextField tfld)	{ return chooser_map.get(tfld); }
public JFileChooser getFileChooser(String label)	{ return chooser_map.get(label); }



/********************************************************************************/
/*										*/
/*	Color requests								*/
/*										*/
/********************************************************************************/

public final SwingColorButton addColorField(String lbl,Color val,ActionListener cb1)
{
   return addColorField(lbl,val,false,cb1);
}


public final SwingColorButton addColorField(String lbl,Color val,boolean alpha,ActionListener cb1)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   SwingColorButton btn = new SwingColorButton(lbl,alpha,val,i18n_map);
   if (cb1 != null) btn.addActionListener(cb1);
   addGBComponent(btn,1,y_count++,0,1,10,0);

   tag_map.put(btn,lbl);
   addDependency(btn,tag);

   return btn;
}



public final SwingColorRangeChooser addColorRangeField(String lbl,Color c1,Color c2,ActionListener cb1)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   SwingColorRangeChooser btn = new SwingColorRangeChooser(lbl,c1,c2,i18n_map);
   if (cb1 != null) btn.addActionListener(cb1);
   addGBComponent(btn,1,y_count++,0,1,10,0);

   tag_map.put(btn,lbl);
   addDependency(btn,tag);

   return btn;
}



/********************************************************************************/
/*										*/
/*	Font requests								*/
/*										*/
/********************************************************************************/

public final SwingFontChooser addFontField(String lbl,Font font,Color c,int opts,ActionListener cb1)
{
   JLabel tag = createLabel(lbl);
   addGBComponent(tag,0,y_count,1,1,0,0);

   SwingFontChooser fc = new SwingFontChooser(lbl,font);
   if (cb1 != null) fc.addActionListener(cb1);

   addGBComponent(fc,1,y_count++,0,1,10,0);

   tag_map.put(fc,lbl);
   addDependency(fc,tag);

   return fc;
}




/********************************************************************************/
/*										*/
/*	Bottom buttons								*/
/*										*/
/********************************************************************************/

public final void addBottomButtons()
{
   addGBComponent(bottom_box,0,y_count++,0,1,10,0);
   bottom_box = null;
}



public final void addBottomButtons(int y)
{
   addGBComponent(bottom_box,0,y,0,1,10,0);
   bottom_box = null;
}



public final JButton addBottomButton(String nm,String tag,ActionListener cb)
{
   return addBottomButton(nm,tag,true,cb);
}



public final JButton addBottomButton(String nm,String tag,boolean enabled,ActionListener cb)
{
   if (bottom_box == null) {
      bottom_box = Box.createHorizontalBox();
      bottom_box.add(Box.createHorizontalGlue());
    }

   JButton btn = new SwingButton(nm,i18n_map);
   if (cb != null) btn.addActionListener(cb);
   btn.setActionCommand(tag);
   btn.setEnabled(enabled);
   bottom_box.add(btn);
   bottom_box.add(Box.createHorizontalGlue());
   tag_map.put(btn,tag);

   return btn;
}



public final JToggleButton addBottomToggle(String nm,String tag,boolean st,ChangeListener cb)
{
   if (bottom_box == null) {
      bottom_box = Box.createHorizontalBox();
      bottom_box.add(Box.createHorizontalGlue());
    }

   JToggleButton btn = new SwingToggleButton(nm,i18n_map);
   if (cb != null) btn.addChangeListener(cb);
   btn.setActionCommand(tag);
   btn.setSelected(st);
   btn.setEnabled(true);
   bottom_box.add(btn);
   bottom_box.add(Box.createHorizontalGlue());
   tag_map.put(btn,tag);

   return btn;
}



/********************************************************************************/
/*										*/
/*	Low level add methods for panels					*/
/*										*/
/********************************************************************************/

public final GridBagConstraints addGBComponent(Component c,int x,int y,int wd,int ht,int dx,int dy)
{
   GridBagConstraints gbc = new GridBagConstraints();

   gbc.gridx = x;
   gbc.gridy = y;
   gbc.gridwidth = wd;
   gbc.gridheight = ht;
   gbc.weightx = dx;
   gbc.weighty = dy;
   gbc.fill = GridBagConstraints.BOTH;
   if (inset_values != null) gbc.insets = inset_values;

   add(c,gbc);

   return gbc;
}



public final GridBagConstraints addGBComponent(Component c,int x,int y,int wd,int ht,int dx,int dy,
						  int fill)
{
   GridBagConstraints gbc = new GridBagConstraints();

   gbc.gridx = x;
   gbc.gridy = y;
   gbc.gridwidth = wd;
   gbc.gridheight = ht;
   gbc.weightx = dx;
   gbc.weighty = dy;
   gbc.fill = fill;
   if (inset_values != null) gbc.insets = inset_values;

   add(c,gbc);

   return gbc;
}



public final GridBagConstraints addGBComponent(Component c,int x,int y,int wd,int ht,int dx,int dy,
						  int fill,int anchor)
{
   GridBagConstraints gbc = new GridBagConstraints();

   gbc.gridx = x;
   gbc.gridy = y;
   gbc.gridwidth = wd;
   gbc.gridheight = ht;
   gbc.weightx = dx;
   gbc.weighty = dy;
   gbc.fill = fill;
   gbc.anchor = anchor;
   if (inset_values != null) gbc.insets = inset_values;

   add(c,gbc);

   return gbc;
}



public final GridBagConstraints addGBComponent(Component c,int x,int y,int wd,int ht,int dx,int dy,
						  Insets ins)
{
   GridBagConstraints gbc = new GridBagConstraints();

   gbc.gridx = x;
   gbc.gridy = y;
   gbc.gridwidth = wd;
   gbc.gridheight = ht;
   gbc.weightx = dx;
   gbc.weighty = dy;
   gbc.fill = GridBagConstraints.BOTH;
   gbc.insets = ins;

   add(c,gbc);

   return gbc;
}




/********************************************************************************/
/*										*/
/*	Label prototype methods 						*/
/*										*/
/********************************************************************************/

public void setBannerPrototype(JLabel lbl)		{ banner_prototype = lbl; }
public JLabel getBannerPrototype()			{ return banner_prototype; }
public void setSectionPrototype(JLabel lbl)		{ section_prototype = lbl; }
public void setLabelPrototype(JLabel lbl)		{ label_prototype = lbl; }



public JLabel createLabel(String txt,int halign,JLabel proto)
{
   JLabel lbl = new SwingLabel(txt,halign,i18n_map);

   if (proto != null) {
      lbl.setVerticalAlignment(proto.getVerticalAlignment());
      lbl.setVerticalTextPosition(proto.getVerticalTextPosition());
      lbl.setBorder(proto.getBorder());
      lbl.setBackground(proto.getBackground());
      lbl.setFont(proto.getFont());
      lbl.setForeground(proto.getForeground());
    }

   return lbl;
}



private JLabel createLabel(String txt)
{
   String txt1 = mapText(txt);

   return createLabel(" " + txt1 + " : ",SwingConstants.RIGHT,label_prototype);
}



private String mapText(String txt)
{
   if (i18n_map == null) return txt;
   return i18n_map.getString(txt);
}



/********************************************************************************/
/*										*/
/*	Action listener methods 						*/
/*										*/
/********************************************************************************/

public void addActionListener(ActionListener al)
{
   if (al != null) {
      action_listener = AWTEventMulticaster.add(action_listener,al);
    }
}


public void removeActionListener(ActionListener al)
{
   if (al != null) {
      action_listener = AWTEventMulticaster.remove(action_listener,al);
    }
}



public void setActionCommand(String c)			{ action_command = c; }



public void fireActionPerformed()
{
   ActionListener al = action_listener;

   if (al == null) return;

   ActionEvent evt = new ActionEvent(this,0,action_command);

   al.actionPerformed(evt);
}



/********************************************************************************/
/*										*/
/*	Handle dependencies to allow easy show/hide				*/
/*										*/
/********************************************************************************/

private void addDependency(Component from,Component to)
{
   if (from == null || to == null) return;

   List<Component> deps = dependent_items.get(from);
   if (deps == null) {
      deps = new ArrayList<>();
      dependent_items.put(from,deps);
      from.addComponentListener(dep_listener);
    }
   deps.add(to);
   if (to instanceof JLabel) {
      if (label_map.get(from) == null) label_map.put(from,(JLabel) to);
    }
}



private final class DepListener extends ComponentAdapter {

   @Override public void componentShown(ComponentEvent e) {
      update(e,true);
    }

   @Override public void componentHidden(ComponentEvent e) {
      update(e,false);
    }

   private void update(ComponentEvent e,boolean vis) {
      Component jc = (Component) e.getSource();
      List<Component> deps = dependent_items.get(jc);
      if (deps == null) return;
      for (Component dc : deps) dc.setVisible(vis);
    }

}	// end of inner class DepListener



/********************************************************************************/
/*										*/
/*	Handler for text fields 						*/
/*										*/
/********************************************************************************/

private class TextUnfocus implements FocusListener {

   private String last_text;

   TextUnfocus() {
      last_text = null;
    }

   @Override public void focusGained(FocusEvent e) {
      JTextField tfld = (JTextField) e.getSource();
      last_text = tfld.getText();
    }

   @Override public void focusLost(FocusEvent e) {
      JTextField tfld = (JTextField) e.getSource();
      String t = tfld.getText();
      if (t == null && last_text == null) return;
      else if (t == null || !t.equals(last_text)) {
	 tfld.postActionEvent();
       }
      last_text = null;
    }

}	// end of inner class TextUnfocus




/********************************************************************************/
/*										*/
/*	Handler for file fields 						*/
/*										*/
/********************************************************************************/

private class BrowseListener implements ActionListener {

   private JFileChooser file_chooser;
   private List<FileFilter> user_filters;
   private JTextField text_field;
   private int file_mode;

   BrowseListener(JTextField tfld,int md,Iterable<FileFilter> filters,FileSystemView fsv) {
      text_field = tfld;
      if (fsv != null) file_chooser = new JFileChooser(fsv);
      else file_chooser = new JFileChooser();
      file_mode = md;
      if (filters == null) user_filters = null;
      else {
	 user_filters = new ArrayList<FileFilter>();
	 for (FileFilter ff : filters) user_filters.add(ff);
       }
    }

   JFileChooser getFileChooser()		{ return file_chooser; }

   @Override public void actionPerformed(ActionEvent e) {
      String cur = text_field.getText();
      if (cur != null) {
	 File curf = null;
	 FileSystemView fsv = file_chooser.getFileSystemView();
	 if (fsv == null) curf = new File(cur);
	 else curf = fsv.createFileObject(cur);
	 file_chooser.setSelectedFile(curf);
       }
      file_chooser.setFileSelectionMode(file_mode);
      if (user_filters != null) {
	 file_chooser.setAcceptAllFileFilterUsed(false);
	 int ct = 0;
	 for (FileFilter ff : user_filters) {
	    if (ct++ == 0) file_chooser.setFileFilter(ff);
	    file_chooser.addChoosableFileFilter(ff);
	  }
       }

      int rval = file_chooser.showOpenDialog(SwingGridPanel.this);
      if (rval == JFileChooser.APPROVE_OPTION) {
	 text_field.setText(file_chooser.getSelectedFile().getAbsolutePath());
	 text_field.postActionEvent();
       }
    }

}	// end of subclass BrowseListener




}	// end of class SwingGridPanel




/* end of SwingGridPanel.java */
