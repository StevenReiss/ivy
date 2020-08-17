/********************************************************************************/
/*										*/
/*		SwingFontChooser.java						*/
/*										*/
/*	JPanel with controls for choosing fonts, compact layout 		*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingFontChooser.java,v 1.8 2018/08/02 15:10:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingFontChooser.java,v $
 * Revision 1.8  2018/08/02 15:10:54  spr
 * Fix imports.  Prepare for java 10.
 *
 * Revision 1.7  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.6  2013/11/15 02:38:19  spr
 * Update imports; add features to combo box.
 *
 * Revision 1.5  2013-06-03 13:03:42  spr
 * *** empty log message ***
 *
 * Revision 1.4  2012-03-21 23:54:43  spr
 * Clean up the code.
 *
 * Revision 1.3  2011-06-29 01:58:26  spr
 * Fix up new grid options.
 *
 * Revision 1.2  2011-06-28 00:12:31  spr
 * Add new swing dialog options.
 *
 * Revision 1.1  2011-06-24 20:16:25  spr
 * Add new types of fields to grid panel.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.swing;



import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SwingFontChooser extends SwingGridPanel implements ActionListener
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

public static final int 	     FONT_FIXED_FAMILY = 1;
public static final int 	     FONT_FIXED_SIZE = 2;
public static final int 	     FONT_FIXED_STYLE = 4;
public static final int 	     FONT_FIXED_COLOR = 8;


private SwingEventListenerList<ActionListener>	action_listeners;
private String		action_name;

private int		choice_options;

private JComboBox<String> family_list;
private JComboBox<String> font_options;
private JComboBox<Integer> font_sizes;
private SwingColorButton color_button;
private JLabel		sample_text;
private Font		current_font;
private Color		current_color;

private final static long serialVersionUID = 1;

private Integer []	default_sizes = new Integer [] { 4,6,8,9,10,11,12,13,14,16,18 };
private String []	default_options = {
   "PLAIN", "BOLD", "ITALIC", "BLD-IT"
};



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingFontChooser(String nm,Font font)
{
   this(nm,font,null,0);
}



public SwingFontChooser(String nm,Font font,Color c,int opts)
{
   action_name = nm;
   action_listeners = new SwingEventListenerList<>(ActionListener.class);
   if (c == null) opts |= FONT_FIXED_COLOR;
   choice_options = opts;

   if (font == null) {
      JLabel lbl = new JLabel();
      font = lbl.getFont();
    }

   family_list = null;
   font_options = null;
   font_sizes = null;
   color_button = null;
   sample_text = null;
   current_font = font;
   current_color = c;

   setupPanel();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public Font getFont()				{ return current_font; }
public Color getFontColor()			{ return current_color; }


public void setFont(Font f,Color c)
{
   if (f != null) {
      current_font = f;
      if (family_list != null) family_list.setSelectedItem(f.getFamily());
      if (font_sizes != null) font_sizes.setSelectedItem(Integer.valueOf(f.getSize()));
      if (font_options != null) font_options.setSelectedIndex(f.getStyle());
    }
   if (c != null) {
      current_color = c;
      if (color_button != null) color_button.setColor(c);
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
/*	Panel setup methods							*/
/*										*/
/********************************************************************************/

private void setupPanel()
{
   GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
   String [] fam = ge.getAvailableFontFamilyNames();

   if ((choice_options & FONT_FIXED_FAMILY) == 0) {
      family_list = new JComboBox<String>(fam);
      family_list.setSelectedItem(current_font.getFamily());
      family_list.addActionListener(this);
      Dimension d = family_list.getPreferredSize();
      d.width = Math.min(d.width,150);
      family_list.setSize(d);
      family_list.setMaximumSize(d);
      family_list.setPreferredSize(d);
      addGBComponent(family_list,0,0,1,1,10,0);
    }
   else {
      JLabel lbl = new JLabel(current_font.getFamily());
      addGBComponent(lbl,0,0,1,1,10,0);
    }

   if ((choice_options & FONT_FIXED_SIZE) == 0) {
      font_sizes = new JComboBox<Integer>(default_sizes);
      // font_sizes.setEditable(true);
      font_sizes.setSelectedItem(Integer.valueOf(current_font.getSize()));
      font_sizes.addActionListener(this);
      addGBComponent(font_sizes,1,0,1,1,2,0);
    }
   else {
      JLabel lbl = new JLabel(Integer.toString(current_font.getSize()));
      addGBComponent(lbl,1,0,1,1,2,0);
    }

   if ((choice_options & FONT_FIXED_STYLE) == 0) {
      font_options = new JComboBox<String>(default_options);
      font_options.setSelectedIndex(current_font.getStyle());
      font_options.addActionListener(this);
      addGBComponent(font_options,2,0,1,1,2,0);
    }
   else {
      JLabel lbl = new JLabel(default_options[current_font.getStyle()]);
      addGBComponent(lbl,2,0,1,1,2,0);
    }

   if (current_color != null) {
      color_button = new SwingColorButton("FONTCOLOR",current_color);
      color_button.setPreferredSize(new Dimension(16,16));
      color_button.addActionListener(this);
      addGBComponent(color_button,3,0,1,1,2,0);
    }

   sample_text = new JLabel("This is what the font looks like");
   sample_text.setBorder(new EmptyBorder(0,10,0,10));
   sample_text.setFont(current_font);
   addGBComponent(sample_text,0,1,0,1,10,0);
}



/********************************************************************************/
/*										*/
/*	Action handlers 							*/
/*										*/
/********************************************************************************/

@Override public void actionPerformed(ActionEvent evt)
{
   if (evt.getSource() == color_button && color_button != null) {
      if (current_color.equals(color_button.getColor())) return;
      current_color = color_button.getColor();
    }
   else {
      Font ft = null;
      if (evt.getSource() == family_list) {
	 String fam = family_list.getSelectedItem().toString();
	 if (fam.equals(current_font.getFamily())) return;
	 ft = new Font(fam,current_font.getStyle(),current_font.getSize());
       }
      else if (evt.getSource() == font_sizes) {
	 String sz = font_sizes.getSelectedItem().toString();
	 try {
	    float n = Float.parseFloat(sz);
	    if (n != current_font.getSize2D()) ft = current_font.deriveFont(n);
	  }
	 catch (NumberFormatException e) { }
       }
      else if (evt.getSource() == font_options) {
	 int sty = font_options.getSelectedIndex();
	 if (sty != current_font.getStyle()) ft = current_font.deriveFont(sty);
       }
      if (ft == null || ft.equals(current_font)) return;
      current_font = ft;
    }

   sample_text.setForeground(current_color);
   sample_text.setFont(current_font);
   sample_text.repaint();

   ActionEvent ne = new ActionEvent(this,evt.getID(),action_name,evt.getWhen(),
					evt.getModifiers());
   for (ActionListener al : action_listeners) {
      al.actionPerformed(ne);
    }
}




}	// end of class SwingFontChooser



/* end of SwingFontChooser.java */
