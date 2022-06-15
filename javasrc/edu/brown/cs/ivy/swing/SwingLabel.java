/********************************************************************************/
/*                                                                              */
/*              SwingLabel.java                                                 */
/*                                                                              */
/*      JLabel with internationalization support                                */
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

import javax.swing.Icon;
import javax.swing.JLabel;

import edu.brown.cs.ivy.file.IvyI18N;

public class SwingLabel extends JLabel
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

public SwingLabel()        
{
   this(null,null,JLabel.LEADING,null);
}

public SwingLabel(IvyI18N intl)
{
   this(null,null,JLabel.LEADING,null);
}

public SwingLabel(String text) 
{
   this(text,null,JLabel.LEADING,null);
}

public SwingLabel(String text,IvyI18N intl)
{
   this (text,null,JLabel.LEADING,intl);
}


public SwingLabel(String text,int align) 
{
   this(text,null,align,null);
}

public SwingLabel(String text,int align,IvyI18N intl)
{
   this (text,null,align,intl);
}


public SwingLabel(String text,Icon icon,int align)
{
   this(text,icon,align,null);
}

public SwingLabel(Icon image)
{
   this (null,image,JLabel.LEADING,null);
}

public SwingLabel(Icon image,IvyI18N intl)
{
   this (null,image,JLabel.LEADING,intl);
}

public SwingLabel(Icon image,int align)
{
   this (null,image,align,null);
}

public SwingLabel(Icon image,int align,IvyI18N intl)
{
   this (null,image,align,intl);
}

public SwingLabel(String text,Icon icn,int align,IvyI18N intl)
{
   super((intl == null ? text : intl.getString(text)),icn,align);
    
   i18n_map = intl;
}



/********************************************************************************/
/*                                                                              */
/*      Methods that need to internationalize                                   */
/*                                                                              */
/********************************************************************************/

public void setText(String text)
{
   String text1 = text;
   if (i18n_map != null) text1 = i18n_map.getString(text);
   super.setText(text1);
}



}       // end of class SwingLabel




/* end of SwingLabel.java */

