/********************************************************************************/
/*										*/
/*		PetalNodeDefault.java						*/
/*										*/
/*	Simple default implementation of a Petal Node				*/
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


package edu.brown.cs.ivy.petal;



import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;




public class PetalNodeDefault implements PetalNode, java.io.Serializable
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Component	node_component;
private transient PetalLink node_link;
private transient PetalArc link_arc;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalNodeDefault(Component c)
{
   node_component = c;
   node_link = null;
   link_arc = null;
}



public PetalNodeDefault(String s)
{
   JLabel lbl = new JLabel(s,SwingConstants.CENTER);

   node_component = lbl;

   lbl.setBackground(Color.yellow);
   lbl.setText(s);
   lbl.setBorder(new LineBorder(Color.green,2));
   lbl.setMinimumSize(lbl.getPreferredSize());
   lbl.setSize(lbl.getPreferredSize());
   lbl.setMaximumSize(new Dimension(400,400));
   lbl.setOpaque(true);

   node_link = null;
   link_arc = null;
}


public PetalNodeDefault(PetalNodeShape ns,String s)
{
   this(new PetalNodeComponent(ns,s));
}



protected PetalNodeDefault()
{
   node_component = null;
   node_link = null;
   link_arc = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public Component getComponent()	{ return node_component; }

protected void setComponent(Component c)	{ node_component = c; }



/********************************************************************************/
/*										*/
/*	Port location methods							*/
/*										*/
/********************************************************************************/

@Override public Point findPortPoint(Point at,Point from)
{
   if (getComponent() instanceof PetalNodeComponent) {
      PetalNodeComponent pnc = (PetalNodeComponent) getComponent();
      return pnc.findPortPoint(at,from);
    }
   
   return PetalHelper.findPortPoint(node_component.getBounds(),at,from);
}



/********************************************************************************/
/*										*/
/*	Link methods								*/
/*										*/
/********************************************************************************/

@Override public PetalLink getLink()				{ return node_link; }

@Override public PetalArc getLinkArc()				{ return link_arc; }

public void setLink(PetalLink lnk,PetalArc arc)
{
   node_link = lnk;
   link_arc = arc;
}



/********************************************************************************/
/*										*/
/*	Input handlers								*/
/*										*/
/********************************************************************************/

@Override public boolean handleMouseClick(MouseEvent evt)	{ return false; }

@Override public boolean handleKeyInput(KeyEvent evt)		{ return false; }



/********************************************************************************/
/*										*/
/*	Tool tip methods							*/
/*										*/
/********************************************************************************/

@Override public String getToolTip(Point at)			{ return null; }




}	// end of interface PetalNodeDefault




/* end of PetalNodeDefault.java */


