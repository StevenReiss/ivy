/********************************************************************************/
/*                                                                              */
/*              SwingTextField.java                                             */
/*                                                                              */
/*      JTextField with proper key bindings                                     */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2011 Brown University -- Steven P. Reiss                    */
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

/* SVN: $Id$ */



package edu.brown.cs.ivy.swing;

import javax.swing.JTextField;
import javax.swing.text.Document;

import edu.brown.cs.ivy.file.IvyI18N;

public class SwingTextField extends JTextField
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

public SwingTextField()
{
   i18n_map = null;
   SwingText.fixKeyBindings(this);
}

public SwingTextField(int col)
{
   super(col);
   i18n_map = null;
   SwingText.fixKeyBindings(this);
}


public SwingTextField(String text)
{
   super(text);
   i18n_map = null;
   SwingText.fixKeyBindings(this);
}


public SwingTextField(String text,int cols)
{
   super(text,cols);
   i18n_map = null;
   SwingText.fixKeyBindings(this);
}


public SwingTextField(Document doc,String text,int cols)
{
   super(doc,text,cols);
   i18n_map = null;
   SwingText.fixKeyBindings(this);
}



public SwingTextField(IvyI18N intl)
{
   i18n_map = intl;
   SwingText.fixKeyBindings(this);
}

public SwingTextField(int col,IvyI18N intl)
{
   super(col);
   i18n_map = intl;
   SwingText.fixKeyBindings(this);
}


public SwingTextField(String text,IvyI18N intl)
{
   super((intl == null ? text : intl.getString(text)));
   i18n_map = intl;
   SwingText.fixKeyBindings(this);
}


public SwingTextField(String text,int cols,IvyI18N intl)
{
   super((intl == null ? text : intl.getString(text)),cols);
   i18n_map = intl;
   SwingText.fixKeyBindings(this);
}


public SwingTextField(Document doc,String text,int cols,IvyI18N intl)
{
   super(doc,(intl == null ? text: intl.getString(text)),cols);
   i18n_map = null;
   SwingText.fixKeyBindings(this);
}




/********************************************************************************/
/*                                                                              */
/*      Internationalization methods                                            */
/*                                                                              */
/********************************************************************************/

@Override public void setText(String text)
{
   String text1 = text;
   if (i18n_map != null) text1 = i18n_map.getString(text);
   super.setText(text1);
}


}       // end of class SwingTextField




/* end of SwingTextField.java */

