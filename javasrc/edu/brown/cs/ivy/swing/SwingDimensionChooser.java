/********************************************************************************/
/*										*/
/*		SwingDimensionChooser.java					*/
/*										*/
/*	JPanel with controls for defining a dimension (w x h)			*/
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



import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SwingDimensionChooser extends SwingGridPanel implements ActionListener
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private SwingEventListenerList<ActionListener>	action_listeners;
private String		action_name;

private SwingNumericField width_field;
private SwingNumericField height_field;

private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingDimensionChooser(String nm,int w,int h)
{
   action_name = nm;
   action_listeners = new SwingEventListenerList<>(ActionListener.class);

   if (w < 0) w = 200;
   if (h < 0) h = 200;

   width_field = null;
   height_field = null;

   setupPanel(w,h);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public int getWidthValue()			{ return (int) width_field.getValue(); }
public int getHeightValue()			{ return (int) height_field.getValue(); }
public Dimension getDimension()
{
   return new Dimension(getWidthValue(),getHeightValue());
}

public void setValue(int w,int h)
{
   if (width_field != null) width_field.setValue(w);
   if (height_field != null) height_field.setValue(h);
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

private void setupPanel(int w,int h)
{
   width_field = new SwingNumericField(w,4,0,4096);
   width_field.addActionListener(this);
   addGBComponent(width_field,0,0,1,1,10,0);

   JLabel l1 = new JLabel(" W x ");
   addGBComponent(l1,1,0,1,1,0,0);

   height_field = new SwingNumericField(h,4,0,4096);
   height_field.addActionListener(this);
   addGBComponent(height_field,2,0,1,1,10,0);

   JLabel l2 = new JLabel(" H");
   addGBComponent(l2,3,0,1,1,0,0);
}




/********************************************************************************/
/*										*/
/*	Action handlers 							*/
/*										*/
/********************************************************************************/

@Override public void actionPerformed(ActionEvent evt)
{
   ActionEvent ne = new ActionEvent(this,evt.getID(),action_name,evt.getWhen(),
					evt.getModifiers());
   for (ActionListener al : action_listeners) {
      al.actionPerformed(ne);
    }
}




}	// end of class SwingDimensionChooser



/* end of SwingDimensionChooser.java */


