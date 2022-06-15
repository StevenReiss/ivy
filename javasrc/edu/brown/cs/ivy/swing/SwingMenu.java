/********************************************************************************/
/*                                                                              */
/*              SwingMenu.java                                                  */
/*                                                                              */
/*      Internationalized version of JMenu                                      */
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
import javax.swing.JMenu;

import edu.brown.cs.ivy.file.IvyI18N;

public class SwingMenu extends JMenu
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

public SwingMenu()
{
   i18n_map = null;
}

public SwingMenu(IvyI18N intl)
{
   i18n_map = intl;
}

public SwingMenu(String text)
{
   this(text,null);
}

public SwingMenu(String text,IvyI18N intl)
{
   super((intl == null ? text : intl.getString(text)));
   i18n_map = intl;   
}

public SwingMenu(String text,boolean b)
{
   this (text,b,null);
}

public SwingMenu(String text,boolean b,IvyI18N intl)
{
   super((intl == null ? text : intl.getString(text)),b);
   i18n_map = intl;
}


public SwingMenu(Action a)
{
   super(a);
   i18n_map = null;
}


public SwingMenu(Action a,IvyI18N intl)
{
   super(a);
   i18n_map = intl;
   if (intl != null) {
      String text = a.getValue(Action.NAME).toString();
      setText(text);
    }
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




}       // end of class SwingMenu




/* end of SwingMenu.java */

