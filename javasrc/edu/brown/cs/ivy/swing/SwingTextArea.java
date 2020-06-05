/********************************************************************************/
/*                                                                              */
/*              SwingTextArea.java                                              */
/*                                                                              */
/*      JTextArea with proper key bindings for machine                          */
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

import javax.swing.JTextArea;
import javax.swing.text.Document;

public class SwingTextArea extends JTextArea
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static final long serialVersionUID = 1;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public SwingTextArea()
{
   SwingText.fixKeyBindings(this);
}


public SwingTextArea(int r,int c)
{
   super(r,c);
   SwingText.fixKeyBindings(this);
}


public SwingTextArea(String text)
{
   super(text);
   SwingText.fixKeyBindings(this);
}


public SwingTextArea(String text,int r,int c)
{
   super(text,r,c);
   SwingText.fixKeyBindings(this);
}


public SwingTextArea(Document d)
{
   super(d);
   SwingText.fixKeyBindings(this);
}


public SwingTextArea(Document d,String text,int r,int c)
{
   super(d,text,r,c);
   SwingText.fixKeyBindings(this);
}




}       // end of class SwingTextArea




/* end of SwingTextArea.java */

