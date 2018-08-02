/********************************************************************************/
/*										*/
/*		SwingColorButton.java						*/
/*										*/
/*	JPanel for showing and selecting color					*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingColorButton.java,v 1.6 2015/11/20 15:09:25 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingColorButton.java,v $
 * Revision 1.6  2015/11/20 15:09:25  spr
 * Reformatting.
 *
 * Revision 1.5  2013/11/15 02:38:18  spr
 * Update imports; add features to combo box.
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
 * Revision 1.1  2011-06-24 20:17:24  spr
 * Add new field handlers.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.swing;



import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;



public class SwingColorButton extends JButton implements ActionListener {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/


private String button_name;
private Color current_color;
private boolean use_alpha;

private SwingEventListenerList<ActionListener>	action_listeners;



private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingColorButton(String nm,Color c)
{
   this(nm,false,c);
}



public SwingColorButton(String nm,boolean alpha,Color c)
{
   action_listeners = new SwingEventListenerList<ActionListener>(ActionListener.class);

   if (c == null) c = Color.BLACK;
   button_name = nm;
   current_color = c;
   use_alpha = alpha;

   setActionCommand("COLOR");
   setPreferredSize(new Dimension(80,16));
   setBorder(new LineBorder(Color.BLACK,1));
   super.addActionListener(this);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public Color getColor() 			{ return current_color; }
public void setColor(Color c)
{
   current_color = c;
   repaint();
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
/*	Painting methods							*/
/*										*/
/********************************************************************************/

@Override protected void paintComponent(Graphics g0)
{
   Graphics2D g = (Graphics2D) g0.create();
   g.setColor(current_color);
   Dimension sz = getSize();
   g.fillRect(0,0,sz.width,sz.height);
}




/********************************************************************************/
/*										*/
/*	Action handling methods 						*/
/*										*/
/********************************************************************************/

@Override public void actionPerformed(ActionEvent e)
{
   String cmd = e.getActionCommand();
   if (cmd.equals("COLOR")) {
      Component c = getParent();
      Color ncol = null;
      try {
	 // use advanced color picker if available
	 Window w = null;
	 for (Component xc = c; xc != null; xc = xc.getParent()) {
	    if (xc instanceof Window) {
	       w = (Window) xc;
	       break;
	     }
	  }
	 Class<?> cls = Class.forName("com.bric.swing.ColorPicker");
	 Method m = cls.getMethod("showDialog",Window.class,String.class,Color.class,boolean.class);
	 ncol = (Color) m.invoke(null,w,"Select Color for " + button_name,current_color,use_alpha);
       }
      catch (Throwable t) {
	 ncol = JColorChooser.showDialog(c,"Select Color for " + button_name,current_color);
       }

      if (ncol != null) {
	 current_color = ncol;
	 repaint();
	 ActionEvent ne = new ActionEvent(this,e.getID(),button_name,e.getWhen(),e.getModifiers());
	 for (ActionListener al : action_listeners) {
	    al.actionPerformed(ne);
	  }
       }
    }
}




}	// end of class SwingColorButton




/* end of SwingColorButton.java */
