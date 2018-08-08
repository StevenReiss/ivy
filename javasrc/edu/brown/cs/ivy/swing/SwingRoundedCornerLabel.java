/********************************************************************************/
/*										*/
/*		SwingRoundedCornerLabel.java					*/
/*										*/
/*	Label with rounded corners						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingRoundedCornerLabel.java,v 1.6 2018/08/02 15:10:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingRoundedCornerLabel.java,v $
 * Revision 1.6  2018/08/02 15:10:54  spr
 * Fix imports.  Prepare for java 10.
 *
 * Revision 1.5  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.4  2013/11/15 02:38:19  spr
 * Update imports; add features to combo box.
 *
 * Revision 1.3  2011-05-27 19:32:51  spr
 * Change copyrights.
 *
 * Revision 1.2  2010-07-24 02:01:02  spr
 * Add permanent option for freeze panes; code clean up; add mac support for text components.
 *
 * Revision 1.1  2010-05-18 22:05:40  spr
 * New rounded corner version of JLabel
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.swing;



import javax.swing.JLabel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.RoundRectangle2D;




public class SwingRoundedCornerLabel extends JLabel
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final long serialVersionUID = 1;

private Color start_color, end_color;
private int[] ary_separate_loc;
private int[] ary_separate_height;

private int border_width;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingRoundedCornerLabel(Color scolor, Color ecolor, int[] locs,
			     int[] hts, int bwidth)
{
   start_color = scolor;
   end_color = ecolor;

   ary_separate_loc = locs;
   ary_separate_height = hts;

   border_width = bwidth;

   setLayout(null);
}



public SwingRoundedCornerLabel(int[] locs, int[] hts, int bwidth)
{
   this(Color.WHITE, Color.WHITE, locs, hts, bwidth);
}




/********************************************************************************/
/*										*/
/*	Paint methods								*/
/*										*/
/********************************************************************************/

@Override protected void paintComponent(Graphics g)
{
   Graphics2D graphics2 = (Graphics2D) g;

   graphics2.setColor(Color.LIGHT_GRAY);
   graphics2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
   RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(border_width, border_width, getWidth() - border_width * 2, getHeight() - border_width * 2, 10, 10);

   graphics2.draw(roundedRectangle);

   Paint p = new GradientPaint(0f, 0f, start_color, 0f, getHeight() - 2 * border_width, end_color);
   graphics2.setPaint(p);
   graphics2.fill(roundedRectangle);

   super.paintComponent(g);

   graphics2.setColor(Color.LIGHT_GRAY);

   for (int i = 0; i < ary_separate_loc.length; i++) {
      g.drawLine(ary_separate_loc[i], border_width, ary_separate_loc[i], getHeight() - 2 * border_width);
    }
   for (int i = 0; i < ary_separate_height.length; i++){
      g.drawLine(ary_separate_loc[0], ary_separate_height[i], getWidth() - border_width, ary_separate_height[i]);
    }
}




}	// end of class SwingRoundedCornerLabel




/* end of SwingRoundedCornerLabel.java */
