/********************************************************************************/
/*										*/
/*		SwingToolBar.java						*/
/*										*/
/*	Swing generic tool bar support code					*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingToolBar.java,v 1.7 2018/08/02 15:10:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingToolBar.java,v $
 * Revision 1.7  2018/08/02 15:10:54  spr
 * Fix imports.  Prepare for java 10.
 *
 * Revision 1.6  2011-05-27 19:32:51  spr
 * Change copyrights.
 *
 * Revision 1.5  2008-11-12 13:46:51  spr
 * No change.
 *
 * Revision 1.4  2006-12-01 03:22:55  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.3  2005/05/07 22:25:44  spr
 * Updates for java 5.0
 *
 * Revision 1.2  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1.1.1  2003/03/17 19:38:17  spr
 * Initial version of the common code for various Brown projects.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;

import edu.brown.cs.ivy.file.IvyI18N;

import java.awt.event.ActionListener;
import java.util.HashMap;



public abstract class SwingToolBar extends JToolBar implements ActionListener, SwingColors
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private HashMap<String,AbstractButton> tool_items;
private transient IvyI18N i18n_map;

private static final long serialVersionUID = 1L;



/****************************************************************************************/
/*											*/
/*	Constructors									*/
/*											*/
/****************************************************************************************/

public SwingToolBar()
{
   this(null);
}


public SwingToolBar(IvyI18N intl)
{
   tool_items = new HashMap<String,AbstractButton>();
   i18n_map = intl;
}



/****************************************************************************************/
/*											*/
/*	Button Creation Methods 							*/
/*											*/
/****************************************************************************************/

protected JButton addButton(String id,String tt)
{
   return addButton(id,null,tt);
}


protected JButton addButton(String id,String iconf,String tt)
{
   Icon icon = (iconf == null ? null : new ImageIcon(iconf));
   String xid = (icon == null ? id : null);
   JButton btn = new SwingButton(xid,icon,i18n_map);
   addSomeButton(btn,id,tt);

   return btn;
}


protected JCheckBox addCheckButton(String id,boolean fg,String tt)
{
   return addCheckButton(id,null,fg,tt);
}



protected JCheckBox addCheckButton(String id,String iconf,boolean fg,String tt)
{
   Icon icon = (iconf == null ? null : new ImageIcon(iconf));
   JCheckBox btn = new SwingCheckBox(id,icon,fg,i18n_map);
   addSomeButton(btn,id,tt);

   return btn;
}



protected JRadioButton addRadioButton(ButtonGroup grp,String id,boolean fg,String tt)
{
   return addRadioButton(grp,id,null,fg,tt);
}



protected JRadioButton addRadioButton(ButtonGroup grp,String id,String iconf,
					 boolean fg,String tt)
{
   Icon icon = (iconf == null ? null : new ImageIcon(iconf));
   JRadioButton btn = new SwingRadioButton(id,icon,fg,i18n_map);
   addSomeButton(btn,id,tt);
   grp.add(btn);

   return btn;
}



private void addSomeButton(AbstractButton btn,String id,String tt)
{
   btn.setActionCommand(id);
   btn.addActionListener(this);
   btn.setToolTipText(tt);
   if (tt != null) ToolTipManager.sharedInstance().registerComponent(btn);
   add(btn);
   tool_items.put(id,btn);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public AbstractButton findToolItem(String id)
{
   if (id == null) return null;

   return tool_items.get(id);
}




}	// end of abstract class SwingToolBar



/* end of SwingToolBar.java */

