/********************************************************************************/
/*										*/
/*		SwingColorRangeChooser.java					*/
/*										*/
/*	JPanel with controls for choosing a color range 			*/
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



import javax.swing.JComboBox;

import edu.brown.cs.ivy.file.IvyI18N;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SwingColorRangeChooser extends SwingGridPanel implements ActionListener
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private SwingEventListenerList<ActionListener>	action_listeners;
private String		action_name;

private SwingColorButton first_color;
private SwingColorButton second_color;
private JComboBox<String> gradient_choice;

private static final long serialVersionUID = 1;

private String []	gradient_options = {
   "SINGLE", "GRADIENT"
};



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingColorRangeChooser(String nm,Color c1,Color c2)
{
   this(nm,c1,c2,null);
}
   
public SwingColorRangeChooser(String nm,Color c1,Color c2,IvyI18N intl)
{
   action_name = nm;
   action_listeners = new SwingEventListenerList<>(ActionListener.class);

   if (c1 == null) c1 = Color.BLACK;
   if (c2 == null) c2 = c1;

   first_color = null;
   second_color = null;
   gradient_choice = null;

   setupPanel(c1,c2);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public Color getFirstColor()			{ return first_color.getColor(); }
public Color getSecondColor()
{
   if (gradient_choice.getSelectedIndex() == 0) return first_color.getColor();
   return second_color.getColor();
}


public void setColors(Color c1,Color c2)
{
   if (c1 == null) c1 = Color.BLACK;
   if (c2 == null) c2 = c1;
   first_color.setColor(c1);
   second_color.setColor(c2);
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

private void setupPanel(Color c1,Color c2)
{
   first_color = new SwingColorButton("FIRST",c1);
   first_color.addActionListener(this);
   addGBComponent(first_color,0,0,1,1,10,0);

   gradient_choice = new JComboBox<String>(gradient_options);
   if (c1 == c2) gradient_choice.setSelectedIndex(0);
   else gradient_choice.setSelectedIndex(1);
   gradient_choice.addActionListener(this);
   addGBComponent(gradient_choice,1,0,1,1,0,0);

   second_color = new SwingColorButton("SECOND",c2);
   second_color.addActionListener(this);
   addGBComponent(second_color,2,0,1,1,10,0);

   setupButton();
}


private void setupButton()
{
   if (gradient_choice.getSelectedIndex() == 0) {
      second_color.setVisible(false);
    }
   else {
      second_color.setVisible(true);
    }
}



/********************************************************************************/
/*										*/
/*	Action handlers 							*/
/*										*/
/********************************************************************************/

@Override public void actionPerformed(ActionEvent evt)
{
   if (evt.getSource() == gradient_choice) setupButton();

   ActionEvent ne = new ActionEvent(this,evt.getID(),action_name,evt.getWhen(),
					evt.getModifiers());
   for (ActionListener al : action_listeners) {
      al.actionPerformed(ne);
    }
}




}	// end of class SwingColorRangeChooser



/* end of SwingColorRangeChooser.java */

