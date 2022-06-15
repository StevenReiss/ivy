/********************************************************************************/
/*                                                                              */
/*              SwingToggleButton.java                                          */
/*                                                                              */
/*      Internationalized JToggleButton                                         */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;

import edu.brown.cs.ivy.file.IvyI18N;

public class SwingToggleButton extends JToggleButton
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private transient IvyI18N i18n_map;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public SwingToggleButton()
{
   this(null,null,null);
}

public SwingToggleButton(IvyI18N intl)
{
   this(null,null,intl);
}

public SwingToggleButton(String text)
{
   this (text,null,null);
}

public SwingToggleButton(String text,IvyI18N intl)
{
   this (text,null,intl);
}

public SwingToggleButton(String text,boolean sel)
{
   this (text,null,sel,null);
}

public SwingToggleButton(String text,boolean sel,IvyI18N intl)
{
   this (text,null,sel,intl);
}


public SwingToggleButton(String text,Icon icon)
{
   this(text,icon,null);
}

public SwingToggleButton(String text,Icon icon,IvyI18N intl)
{
   super((intl == null ? text : intl.getString(text)),icon);
   i18n_map = intl;
}

public SwingToggleButton(String text,Icon icon,boolean  sel)
{
   this(text,icon,sel,null);
}

public SwingToggleButton(String text,Icon icon,boolean sel,IvyI18N intl)
{
   super((intl == null ? text : intl.getString(text)),icon,sel);
   i18n_map = intl;
}



public SwingToggleButton(Action a)
{
   super(a);
   i18n_map = null;
}


public SwingToggleButton(Action a,IvyI18N intl)
{
   super(a);
   i18n_map = intl;
   if (intl != null) {
      String text = a.getValue(Action.NAME).toString();
      setText(text);
    }
}


public SwingToggleButton(Icon icn)
{
   super(icn);
   i18n_map = null;
}

public SwingToggleButton(Icon icn,IvyI18N intl)
{
   super(icn);
   i18n_map = intl;
}

public SwingToggleButton(Icon icn,boolean sel)
{
   super(icn,sel);
   i18n_map = null;
}

public SwingToggleButton(Icon icn,boolean sel,IvyI18N intl)
{
   super(icn,sel);
   i18n_map = intl;
}



/********************************************************************************/
/*                                                                              */
/*      Internationalized routines                                              */
/*                                                                              */
/********************************************************************************/

@Override public void setText(String text)
{
   String text1 = text;
   if (i18n_map != null) text1 = i18n_map.getString(text);
   super.setText(text1);
}





}       // end of class SwingToggleButton




/* end of SwingToggleButton.java */

