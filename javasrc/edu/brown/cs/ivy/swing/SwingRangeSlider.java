/********************************************************************************/
/*										*/
/*		SwingRangeSlider.java						*/
/*										*/
/*	Extension of a JSlider to support range values				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Redistribution and use in source and binary forms, with or without           *
 *  modification, are permitted provided that the following conditions are met:  *
 *                                                                               *
 *  + Redistributions of source code must retain the above copyright notice,     *
 *      this list of conditions and the following disclaimer.                    *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *      this list of conditions and the following disclaimer in the              *
 *      documentation and/or other materials provided with the distribution.     *
 *  + Neither the name of the Brown University nor the names of its              *
 *      contributors may be used to endorse or promote products derived from     *
 *      this software without specific prior written permission.                 *
 *                                                                               *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   *
 *  POSSIBILITY OF SUCH DAMAGE.                                                  *
 *                                                                               *
 ********************************************************************************/


package edu.brown.cs.ivy.swing;


import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.AWTEventMulticaster;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;





public class SwingRangeSlider extends JSlider implements ChangeListener
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String	action_command;
private int	num_decimals;
private transient ActionListener action_listener;


private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingRangeSlider(int min,int max,int dec,int val)
{
   super(min,max,min);

   num_decimals = dec;
   action_command = null;
   action_listener = null;

   if (val < min) val = min;
   if (val > max) val = max;
   setValue(val);

   int del = max - min + 1;
   int fct = 0;
   int minv = min;
   while (del > 10) {
      ++fct;
      del /= 10;
      minv /= 10;
    }
   int delv = 1;
   for (int i = 0; i < fct; ++i) {
      minv *= 10;
      delv *= 10;
    }
   int ct = (max-min+1)/delv;
   if (ct > 5) delv *= 2;
   else if (ct <= 2) delv /= 2;

   double decv = 1;
   for (int i = 0; i < dec; ++i) decv *= 10;

   Hashtable<Integer,JComponent> lbls = new Hashtable<Integer,JComponent>();
   for (int i = 0; ; ++i) {
      int v0 = minv + i * delv;
      if (v0 < min) v0 = min;
      if (v0 > max) v0 = max;
      double v1 = v0;
      v1 /= decv;
      String nv = (decv == 1 ? Integer.toString((int) v1) : Double.toString(v1));
      JLabel vlbl = new JLabel(nv);
      lbls.put(v0,vlbl);
      if (v0 >= max) break;
    }

   setPaintLabels(true);
   setLabelTable(lbls);
   addChangeListener(this);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public double getScaledValue()
{
   double scl = Math.pow(10,num_decimals);
   double v = getValue();
   return v/scl;
}



public void setScaledValue(double v)
{
   double scl = Math.pow(10,num_decimals);
   int iv = (int) (v * scl);
   setValue(iv);
}



public void setFont(Font ft)
{
   super.setFont(ft);
   
   Dictionary<?,?> lbls = getLabelTable();
   if (lbls == null) return;
   for (Enumeration<?> en = lbls.elements(); en.hasMoreElements(); ) {
      Object o = en.nextElement();
      if (o instanceof JLabel) {
         JLabel lbl = (JLabel) o;
         lbl.setFont(ft);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Event methods								*/
/*										*/
/********************************************************************************/

public void setActionCommand(String s)
{
   action_command = s;
}


public String getActionCommand()		{ return action_command; }



public void addActionListener(ActionListener l)
{
   action_listener = AWTEventMulticaster.add(action_listener,l);
}



public void removeActionListener(ActionListener l)
{
   action_listener = AWTEventMulticaster.remove(action_listener,l);
}



@Override public void stateChanged(ChangeEvent evt)
{
   ActionListener al = action_listener;

   if (al != null) {
      ActionEvent aevt = new ActionEvent(this,0,action_command);
      al.actionPerformed(aevt);
    }
}



}	// end of class SwingRangeSlider




/* end of SwingRangeSlider.java */
