/********************************************************************************/
/*                                                                              */
/*              SwingMenuItem.java                                              */
/*                                                                              */
/*      Internationalized JMenuItem                                             */
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
import javax.swing.JMenuItem;

import edu.brown.cs.ivy.file.IvyI18N;

public class SwingMenuItem extends JMenuItem
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

public SwingMenuItem()
{
   this(null,null,null);
}

public SwingMenuItem(IvyI18N intl)
{
   this(null,null,intl);
}

public SwingMenuItem(String text)
{
   this (text,null,null);
}

public SwingMenuItem(String text,IvyI18N intl)
{
   this (text,null,intl);
}

public SwingMenuItem(String text,int mnem)
{
   this (text,mnem,null);
}

public SwingMenuItem(String text,int mnem,IvyI18N intl)
{
   super((intl == null ? text : intl.getString(text)),mnem);
   i18n_map = intl;
}


public SwingMenuItem(String text,Icon icon)
{
   this(text,icon,null);
}

public SwingMenuItem(String text,Icon icon,IvyI18N intl)
{
   super((intl == null ? text : intl.getString(text)),icon);
   i18n_map = intl;
}


public SwingMenuItem(Action a)
{
   super(a);
   i18n_map = null;
}


public SwingMenuItem(Action a,IvyI18N intl)
{
   super(a);
   i18n_map = intl;
   if (intl != null) {
      String text = a.getValue(Action.NAME).toString();
      setText(text);
    }
}


public SwingMenuItem(Icon icn)
{
   super(icn);
   i18n_map = null;
}

public SwingMenuItem(Icon icn,IvyI18N intl)
{
   super(icn);
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




}       // end of class SwingMenuItem




/* end of SwingMenuItem.java */

