/********************************************************************************/
/*										*/
/*		SwingEditorPane.java						*/
/*										*/
/*	JEditorPane with additional support facilities				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingEditorPane.java,v 1.2 2013/11/15 02:38:19 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingEditorPane.java,v $
 * Revision 1.2  2013/11/15 02:38:19  spr
 * Update imports; add features to combo box.
 *
 * Revision 1.1  2011-11-22 12:07:34  spr
 * Add editor pane extensions.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.swing;



import javax.swing.JEditorPane;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.net.URL;


public class SwingEditorPane extends JEditorPane
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private static SwingEditorPane source_component = null;
private static Clipboard system_clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
private static Transferable last_content = null;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingEditorPane()				{ }

public SwingEditorPane(String url) throws IOException	{ super(url); }

public SwingEditorPane(String type,String txt)		{ super(type,txt); }

public SwingEditorPane(URL u) throws IOException	{ super(u); }




/********************************************************************************/
/*										*/
/*	Methods for tracking current clip owner 				*/
/*										*/
/********************************************************************************/

@Override public void copy()
{
   source_component = this;
   super.copy();
   last_content = system_clipboard.getContents(null);
}



@Override public void cut()
{
   source_component = this;
   super.cut();
   last_content = system_clipboard.getContents(null);
}



public SwingEditorPane getClipboardSource()
{
   if (source_component != null && system_clipboard.getContents(null) != last_content) {
      source_component = null;
      last_content = null;
    }

   return source_component;
}



}	// end of class SwingEditorPane



/* end of SwingEditorPane.java */
