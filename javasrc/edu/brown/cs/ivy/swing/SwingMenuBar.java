/********************************************************************************/
/*										*/
/*		SwingMenuBar.java						*/
/*										*/
/*	Swing generic menu bar support code					*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingMenuBar.java,v 1.8 2011-05-27 19:32:51 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingMenuBar.java,v $
 * Revision 1.8  2011-05-27 19:32:51  spr
 * Change copyrights.
 *
 * Revision 1.7  2008-11-12 13:46:51  spr
 * No change.
 *
 * Revision 1.6  2007-05-04 02:00:37  spr
 * Import fixups.
 *
 * Revision 1.5  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.4  2004/02/26 02:57:10  spr
 * Make methods final that shouldn't be overridden.
 *
 * Revision 1.3  2003/06/06 19:59:12  spr
 * Change from hashtable/enumeration to hashmap/iterator.
 *
 * Revision 1.2  2003/03/29 03:41:30  spr
 * Add new button types to grid panel and menu bars; extend trees to support icons.
 *
 * Revision 1.1.1.1  2003/03/18 19:27:51  spr
 * Initial version of the common code for various Brown projects.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;

import javax.swing.*;

import java.awt.event.ActionListener;



public abstract class SwingMenuBar extends JMenuBar implements ActionListener, SwingColors
{




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private static final long serialVersionUID = 1L;



/****************************************************************************************/
/*											*/
/*	Constructors									*/
/*											*/
/****************************************************************************************/

public SwingMenuBar()				 { }




/****************************************************************************************/
/*											*/
/*	Button Creation Methods 							*/
/*											*/
/****************************************************************************************/

protected final JMenuItem addButton(JMenu m,String id,String tt)
{
   JMenuItem itm = new JMenuItem(id);
   itm.addActionListener(this);
   itm.setToolTipText(tt);
   if (tt != null) ToolTipManager.sharedInstance().registerComponent(itm);
   m.add(itm);

   return itm;
}


protected final JCheckBoxMenuItem addCheckButton(JMenu m,String id,boolean fg,String tt)
{
   JCheckBoxMenuItem itm = new JCheckBoxMenuItem(id,fg);
   itm.addActionListener(this);
   itm.setToolTipText(tt);
   if (tt != null) ToolTipManager.sharedInstance().registerComponent(itm);
   m.add(itm);

   return itm;
}



protected final JRadioButtonMenuItem addRadioButton(JMenu m,ButtonGroup grp,String id,boolean fg,String tt)
{
   JRadioButtonMenuItem itm = new JRadioButtonMenuItem(id,fg);
   itm.addActionListener(this);
   itm.setToolTipText(tt);
   if (tt != null) ToolTipManager.sharedInstance().registerComponent(itm);
   m.add(itm);
   grp.add(itm);

   return itm;
}




}	// end of abstract class SwingMenuBar



/* end of SwingMenuBar.java */
