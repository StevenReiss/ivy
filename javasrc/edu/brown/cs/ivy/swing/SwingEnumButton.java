/********************************************************************************/
/*										*/
/*		SwingEnumButton.java						*/
/*										*/
/*	Multiple choise button for choosing from a set of items 		*/
/*										*/
/********************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss		      */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingEnumButton.java,v 1.6 2018/08/02 15:10:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingEnumButton.java,v $
 * Revision 1.6  2018/08/02 15:10:54  spr
 * Fix imports.  Prepare for java 10.
 *
 * Revision 1.5  2013/11/15 02:38:19  spr
 * Update imports; add features to combo box.
 *
 * Revision 1.4  2011-05-27 19:32:50  spr
 * Change copyrights.
 *
 * Revision 1.3  2010-10-05 23:54:49  spr
 * Add expansion listener support to tree tables.
 *
 * Revision 1.2  2010-10-01 20:59:28  spr
 * Use swing/awt event lists.
 *
 * Revision 1.1  2010-06-30 23:24:58  spr
 * Add enumeration button.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;


import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import edu.brown.cs.ivy.file.IvyI18N;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;



public class SwingEnumButton<E extends Enum<?>> extends JButton implements ActionListener
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private int	cur_index;
private E []	value_set;
private transient Icon [] icon_set;
private EventListenerList value_listeners;
private transient IvyI18N i18n_map;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/


@SuppressWarnings("unchecked")
public SwingEnumButton(E dflt)
{
   this(dflt,(Class<E>) dflt.getClass(),null,null);
}

@SuppressWarnings("unchecked")
public SwingEnumButton(E dflt,IvyI18N intl)
{
   this(dflt,(Class<E>) dflt.getClass(),null,intl);
}


@SuppressWarnings("unchecked")
public SwingEnumButton(E dflt,Icon [] icons)
{
   this(dflt,(Class<E>) dflt.getClass(),icons,null);
}


@SuppressWarnings("unchecked")
public SwingEnumButton(E dflt,Icon [] icons,IvyI18N intl)
{
   this(dflt,(Class<E>) dflt.getClass(),icons,intl);
}


public SwingEnumButton(E dflt,Class<E> cls,Icon [] icons)
{
  this(dflt,cls,icons,null);
}


public SwingEnumButton(E dflt,Class<E> cls,Icon [] icons,IvyI18N intl)
{
   value_set = cls.getEnumConstants();
   i18n_map = intl;

   icon_set = new Icon[value_set.length];
   if (icons != null) {
      if (icons.length != value_set.length)
	 throw new IllegalArgumentException("Icons length must be same as values length");
      System.arraycopy(icons,0,icon_set,0,icons.length);
    }

   if (dflt == null) cur_index = 0;
   else {
      cur_index = Arrays.binarySearch(value_set,dflt);
      if (cur_index < 0) cur_index = 0;
    }

   value_listeners = new EventListenerList();

   setDisplay();

   addActionListener(this);
}





/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public E getValue()
{
   return value_set[cur_index];
}



public void addValueListener(ChangeListener cl)
{
   value_listeners.add(ChangeListener.class,cl);
}


public void removeValueListener(ChangeListener cl)
{
   value_listeners.remove(ChangeListener.class,cl);
}




/********************************************************************************/
/*										*/
/*	Methods to update the button						*/
/*										*/
/********************************************************************************/

private void setDisplay()
{
   if (icon_set[cur_index] == null) {
      setIcon(null);
      String text = value_set[cur_index].toString();
      if (i18n_map != null) text = i18n_map.getString(text);
      setText(text);
    }
   else {
      setIcon(icon_set[cur_index]);
      setText(null);
    }
}



/********************************************************************************/
/*										*/
/*	Action handling methods 						*/
/*										*/
/********************************************************************************/

@Override public void actionPerformed(ActionEvent evt)
{
   cur_index = (cur_index + 1) % value_set.length;
   setDisplay();

   ChangeEvent ce = new ChangeEvent(this);
   for (ChangeListener cl : value_listeners.getListeners(ChangeListener.class)) {
      cl.stateChanged(ce);
    }
}



}	// end of class SwingEnumButton




/* end of SwingEnumButton.java */
