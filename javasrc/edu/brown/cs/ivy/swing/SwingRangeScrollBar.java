/********************************************************************************/
/*										*/
/*		SwingRangeScrollBar.java					*/
/*										*/
/*	Scroll bar that allows a range setting					*/
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

/*
****************************************************************************
*      Copyright (C) 1999 by the University of Maryland, College Park
*
*		  Human Computer Interaction Laboratory
*		      email: hcil-info@cs.umd.edu
*		      http://www.cs.umd.edu/hcil/
*
*	       Laboratory for Language and Media Processing
*			email: lamp@cfar.umd.edu
*			http://lamp.cfar.umd.edu
*
*		  Institute for Advanced Computer Studies
*	      University of Maryland, College Park, MD 20742
*
*
* Permission to use, copy, modify and distribute this software and its
* documentation for any purpose other than its incorporation into a
* commercial product is hereby granted without fee, provided that the
* above copyright notice and this permission notice appear in all copies of
* the software and related documentation.
***************************************************************************
*/

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingRangeScrollBar.java,v 1.9 2018/08/02 15:10:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingRangeScrollBar.java,v $
 * Revision 1.9  2018/08/02 15:10:54  spr
 * Fix imports.  Prepare for java 10.
 *
 * Revision 1.8  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.7  2012-10-25 01:27:17  spr
 * Code clean up.	Add color lookup by name.
 *
 * Revision 1.6  2011-05-27 19:32:51  spr
 * Change copyrights.
 *
 * Revision 1.5  2011-03-19 20:32:16  spr
 * Clean up code.
 *
 * Revision 1.4  2010-02-12 00:40:02  spr
 * Fix file-based options.  Fix spacing.
 *
 * Revision 1.3  2009-10-07 22:38:17  spr
 * Code cleanup; fix problem with null args to choices.
 *
 * Revision 1.2  2009-10-02 00:18:30  spr
 * Import clean up.
 *
 * Revision 1.1  2009-09-17 02:00:45  spr
 * Add autocomplete, new grid options, fix up lists, add range scroll bar.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;




public class SwingRangeScrollBar extends JPanel implements Adjustable {


/********************************************************************************/
/*										*/
/*	Constant definitions							*/
/*										*/
/********************************************************************************/

private final static int RIGHT_THUMB = 1;
private final static int LEFT_THUMB  = -1;

private final static int BASIC_THUMB_WIDTH = 5;


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private AdjustmentListener adj_listener;

private boolean right_moved;
private boolean left_moved;
private boolean right_value_changed;
private boolean left_value_changed;
private boolean restricted_drag;

private int bar_width;
private int bar_height;
private int last_thumb_changed;
private int left_value;
private int right_value;
private int left_position;
private int right_position;
private int thumb_width;
private int thumb_range_min;
private int thumb_range_max;
private int thumb_range;
private int actual_range_min;
private int actual_range_max;
private int actual_range;
private int unit_increment;
private int block_increment;
private int bar_orientation;
private int m_arrow_length;

private int right_box_position;
private int left_box_position;
private int left_box_value;
private int right_box_value;

private Color	thumb_color;
private Color	thumb_shadow_color;
private Color	thumb_highlight_color;
private Color	track_color;
private Color	track_highlight_color;

private RangeSliderMotionAdapter motion_listener;
private RangeSliderMouseAdapter mouse_listener;


private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingRangeScrollBar(int orientation)
{
   this(orientation,0,100,0,10);
}


public SwingRangeScrollBar(int rmin,int rmax)
{
   this(HORIZONTAL,rmin,rmax,rmin,(rmin+rmax)/2);
}


public SwingRangeScrollBar(int rmin,int rmax,int lv,int uv)
{
   this(HORIZONTAL,rmin,rmax,lv,uv);
}


public SwingRangeScrollBar(int orient,int rmin,int rmax,int lv,int uv)
{
   bar_width = 100;
   bar_height = 10;

   right_moved = false;
   left_moved = false;
   restricted_drag = false;
   right_value_changed = true;
   left_value_changed = true;

   actual_range_min = rmin;
   actual_range_max = rmax;
   left_value = lv;
   right_value = uv;
   last_thumb_changed = 0;

   rangeSetup();
   generalSetup();
   bar_orientation = orient;

   if (bar_orientation == VERTICAL) {
      bar_width = 10;
      bar_height = 100;
      this.bar_orientation = VERTICAL;
    }

   UIManager.getColor("ScrollBar.shadow");
   UIManager.getColor("ScrollBar.highlight");
   UIManager.getColor("ScrollBar.darkShadow");
   thumb_color = UIManager.getColor("ScrollBar.thumb");
   thumb_shadow_color = UIManager.getColor("ScrollBar.thumbShadow");
   thumb_highlight_color = UIManager.getColor("ScrollBar.thumbHighlight");
   UIManager.getColor("ScrollBar.thumbShadow");
   UIManager.getColor("ScrollBar.thumbDarkShadow");
   track_color = UIManager.getColor("ScrollBar.track");
   track_highlight_color = UIManager.getColor("ScrollBar.trackHighlight");

   setMinimumSize(new Dimension(bar_width,bar_height));
   setPreferredSize(new Dimension(bar_width,bar_height));
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public int getBlockIncrement()			{ return block_increment; }

public int getChangedThumb()			{ return last_thumb_changed; }

public int getLeftValue()			{ return left_value; }

@Override public int getMaximum() 			{ return actual_range_max; }

@Override public int getMinimum() 			{ return actual_range_min; }

@Override public int getOrientation()			{ return bar_orientation; }

public int getRightValue()			{ return right_value; }

@Override public int getUnitIncrement()			{ return unit_increment; }

@Override public int getValue()				{ return left_value; }

@Override public int getVisibleAmount()			{ return right_value - left_value + 1; }




/********************************************************************************/
/*										*/
/*	Set methods								*/
/*										*/
/********************************************************************************/

@Override public synchronized void addAdjustmentListener(AdjustmentListener l)
{
   adj_listener = l;
}

@Override public synchronized void removeAdjustmentListener(AdjustmentListener l)
{
   adj_listener = null;
}


@Override public void setBlockIncrement(int v)		{ block_increment = v; }

public void setLeftValue(int v)
{
   if (v < actual_range_min)
      left_value = actual_range_min;
   else if (v >= right_value)
      left_value = right_value - 1;
   else if (left_value == v) return;
   else
      left_value = v;
   left_value_changed = true;
   adjustValue();
   repaint();
}


public void setLeftBoxValue(int v)
{
   if (v < actual_range_min)
      left_box_value = actual_range_min;
   else if (v >= right_box_value)
      left_box_value = right_box_value - 1;
   else
      left_box_value = v;
   adjustBoxValue();
   repaint();
}



@Override public void setMaximum(int v)
{
   actual_range_max = v;
   rangeSetup();
   repaint();
}

@Override public void setMinimum(int v)
{
   actual_range_min = v;
   rangeSetup();
   repaint();
}


public void setRightValue(int v)
{
   if (v > actual_range_max)
      right_value = actual_range_max;
   else if (v <= left_value)
      right_value = left_value + 1;
   else if (v == right_value) return;
   else
      right_value = v;

   right_value_changed = true;
   adjustValue();
   repaint();
}


public void setRightBoxValue(int v)
{
   if (v > actual_range_max)
      right_box_value = actual_range_max;
   else if (v<= left_box_value)
      right_box_value = left_box_value + 1;
   else
      right_box_value = v;

   adjustBoxValue();
   repaint();
}


@Override public void setUnitIncrement(int v)
{
   unit_increment = v;
}

@Override public void setValue(int v)
{
   setLeftValue(v);
}


public void setValues( int lv, int rv, int minimum, int maximum)
{
   actual_range_min = minimum;
   actual_range_max = maximum;
   rangeSetup();
   setLeftValue(lv);
   setRightValue(rv);
   repaint();
}


public void setValues(int lv,int rv)
{
   setLeftValue(lv);
   setRightValue(rv);
}



@Override public void setVisibleAmount(int v)
{
   setRightValue(left_value + v);
}




/********************************************************************************/
/*										*/
/*	Painting interface							*/
/*										*/
/********************************************************************************/

/************
public void update(Graphics g)
{
   paint(g);
}
**************/


@Override public void repaint()
{
   left_value_changed = right_value_changed = true;
   super.repaint();
}



@Override public void paint (Graphics g)
{
   super.paint(g);

   Dimension panelSize = getSize();

   graphicsInit(panelSize);

   // Color the background
   if (bar_orientation == HORIZONTAL)
      drawHorizontalArrows(g);
   else
      drawVerticalArrows(g);

   refresh(g);
}



/********************************************************************************/
/*										*/
/*	Adjustment methods							*/
/*										*/
/********************************************************************************/

private void adjustPosition()
{
   if (right_value_changed)
      right_position = (int)(((double)(right_value - actual_range_min)/actual_range)*thumb_range) + thumb_range_min;
   if (left_value_changed)
      left_position = (int)(((double)(left_value - actual_range_min)/actual_range)*thumb_range) + thumb_range_min;
   right_value_changed = left_value_changed = false;
   if (bar_orientation == HORIZONTAL) {
      if (right_position > thumb_range_max)
	 right_position = thumb_range_max;
      if (left_position < thumb_range_min)
	 left_position = thumb_range_min;
    }
   else {
      if (right_position < thumb_range_max)
	 right_position = thumb_range_max;
      if (left_position > thumb_range_min)
	 left_position = thumb_range_min;
    }
}



private void adjustBoxPosition()
{
   right_box_position = (int) (((double) (right_box_value - actual_range_min)/actual_range)*thumb_range) + thumb_range_min;
   left_box_position = (int) (((double) (left_box_value - actual_range_min)/actual_range)*thumb_range) + thumb_range_min;

   if (bar_orientation == HORIZONTAL) {
      if (right_box_position > thumb_range_max)
	 right_box_position = thumb_range_max;
      if (left_box_position < thumb_range_min)
	 left_box_position = thumb_range_min;
    }
   else {
      if (right_box_position < thumb_range_max)
	 right_box_position = thumb_range_max;
      if (left_box_position > thumb_range_min)
	 left_box_position = thumb_range_min;
    }
}



private void adjustValue()
{
   int pRightV = right_value;
   int pLeftV = left_value;
   // The next two variables are in case of a change of value without moving
   // the thumbs.
   boolean rightChange = right_value_changed, leftChange = left_value_changed;

   //AGAIN the following is needed for doing changes if value changes without
   // movement of thumbs (e.g. changing value from inside the aplication)
   if (rightChange) last_thumb_changed = RIGHT_THUMB;
   if (leftChange) last_thumb_changed = LEFT_THUMB;

   if (right_moved) {
      right_value = (int)Math.round(((double)(right_position - thumb_range_min)/thumb_range)*actual_range) + actual_range_min;
      right_value_changed = true;
    }
   if (left_moved) {
      left_value =(int)Math.round(((double)(left_position - thumb_range_min)/thumb_range)*actual_range) +actual_range_min;
      left_value_changed = true;
    }
   right_moved = left_moved = false;
   /* Since sometimes when the range is large and you pull the two thumbs
      together even a difference of one will yield the same position. But
      since we always want to keep at least a difference of one between
      the thumbs, the following will take care of that. */

    adjustPosition();

    if (adj_listener != null) {
       if ((pRightV != right_value) || rightChange)
	  adj_listener.adjustmentValueChanged( new AdjustmentEvent( this, AdjustmentEvent.ADJUSTMENT_FIRST, AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED, right_value));
       if ((pLeftV!=left_value) || leftChange)
	  adj_listener.adjustmentValueChanged( new AdjustmentEvent( this, AdjustmentEvent.ADJUSTMENT_FIRST, AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED, left_value));
     }
}



private void adjustBoxValue()
{
   adjustBoxPosition();
}


private void blockIncrementRight()
{
   last_thumb_changed = RIGHT_THUMB;
   if ((right_value+block_increment)<=actual_range_max) {
      right_value += block_increment;
      if (adj_listener != null)
	 adj_listener.adjustmentValueChanged( new AdjustmentEvent( this, AdjustmentEvent.ADJUSTMENT_FIRST, AdjustmentEvent.BLOCK_INCREMENT, right_value));
    }
   else {
      right_value = actual_range_max;
    }
   right_value_changed = true;
   refresh();
}


private void blockIncrementLeft()
{
   last_thumb_changed = LEFT_THUMB;
   if ((left_value-block_increment)>=actual_range_min) {
      left_value-=block_increment;
    }
   else
      left_value = actual_range_min;
   if (adj_listener != null)
      adj_listener.adjustmentValueChanged( new AdjustmentEvent( this, AdjustmentEvent.ADJUSTMENT_FIRST, AdjustmentEvent.BLOCK_INCREMENT, left_value));
   left_value_changed = true;
   refresh();
}





/********************************************************************************/
/*										*/
/*	Methods for dragging							*/
/*										*/
/********************************************************************************/

private void dragBar(int initialPos,int drag)
{
   int oldValueRight, oldValueLeft;
   oldValueRight = right_value;
   oldValueLeft = left_value;
   if (bar_orientation == HORIZONTAL) {
      if (initialPos >thumb_range_max) initialPos = thumb_range_max;
      else if (initialPos < thumb_range_min) initialPos = thumb_range_min;
      if (drag > initialPos) {
	 int dragAmount = drag - initialPos;
	 if (right_position >= thumb_range_max)
	    restricted_drag = true;
	 if (!restricted_drag) {
	    if ( (right_position + dragAmount)>=thumb_range_max)
	       dragAmount = thumb_range_max - right_position;
	    dragRightThumb(right_position + dragAmount);
	    if (right_value != oldValueRight) {
	       int difference = right_value - oldValueRight;
	       left_value +=difference;
	       left_value_changed = true;
	       motion_listener.setInitialPos(drag);
	       refresh();
	     }
	  }
       }
      else {
	int dragAmount = initialPos - drag;
	if (left_position <= thumb_range_min) restricted_drag = true;
	if (!restricted_drag) {
	   if ( (left_position - dragAmount)<=thumb_range_min)
	      dragAmount = left_position - thumb_range_min;
	   dragLeftThumb(left_position - dragAmount);
	   if (left_value != oldValueLeft) {
	      int difference = oldValueLeft - left_value;
	      right_value -=difference;
	      right_value_changed = true;
	      motion_listener.setInitialPos(drag);
	      refresh();
	    }
	 }
       }
    }
   else {
      if (initialPos <thumb_range_max) initialPos = thumb_range_max;
      else if (initialPos > thumb_range_min) initialPos = thumb_range_min;
      if (drag > initialPos) {
	 int dragAmount = drag - initialPos;
	 if (left_position >= thumb_range_min) restricted_drag = true;
	 if (!restricted_drag) {
	    if ( (left_position + dragAmount)>=thumb_range_min)
	       dragAmount = thumb_range_min - left_position;
	    dragLeftThumb(left_position + dragAmount);
	    if (left_value != oldValueLeft) {
	       int difference = oldValueLeft - left_value;
	       right_value -=difference;
	       right_value_changed = true;
	       motion_listener.setInitialPos(drag);
	       refresh();
	     }
	  }
       }
      else {
	 int dragAmount = initialPos - drag;
	 if (right_position <= thumb_range_max) restricted_drag = true;
	 if (!restricted_drag) {
	    if ( (right_position - dragAmount)<=thumb_range_max)
	       dragAmount = right_position - thumb_range_max;
	    dragRightThumb(right_position - dragAmount);
	    if (right_value != oldValueRight) {
	       int difference = right_value - oldValueRight;
	       left_value +=difference;
	       left_value_changed = true;
	       motion_listener.setInitialPos(drag);
	       refresh();
	     }
	  }
       }
    }
   restricted_drag = false;

   if (adj_listener != null)
      adj_listener.adjustmentValueChanged( new AdjustmentEvent( this, AdjustmentEvent.ADJUSTMENT_FIRST, AdjustmentEvent.TRACK, right_value));
}



private void dragLeftThumb(int drag)
{
   last_thumb_changed = LEFT_THUMB;
   int correctionToDrag = drag;
   if (bar_orientation == HORIZONTAL) {
      if ( (drag>=thumb_range_min) && (correctionToDrag< right_position) ) left_position = drag;
      else if ( correctionToDrag>=right_position) left_position = right_position;
      else left_position = thumb_range_min;
    }
   else {
      if ( (drag <= thumb_range_min) && (drag > right_position) ) left_position = drag;
      else if (drag <= right_position) left_position = right_position;
      else left_position = thumb_range_min;
    }
   left_moved = true;
   refresh();
}


private void dragRightThumb(int drag)
{
   last_thumb_changed = RIGHT_THUMB;
   if (bar_orientation == HORIZONTAL) {
      if ((drag<=thumb_range_max)&&(drag>left_position)) right_position = drag;
      else if (drag<=left_position) right_position = left_position;
      else right_position = thumb_range_max;
    }
   else {
      if ((drag >= thumb_range_max) && (drag < left_position)) right_position = drag;
      else if (drag>= left_position) right_position = left_position;
      else right_position = thumb_range_max;
    }
   right_moved = true;
   refresh();
}



/********************************************************************************/
/*										*/
/*	Arrow drawing methods							*/
/*										*/
/********************************************************************************/

private void drawHorizontalArrows(Graphics g)
{
   int xpoints[] = new int[3], ypoints[] = new int[3];

   // Drawing Background
   g.setColor(track_color);
   g.fillRect(0,0,bar_width,bar_height);

   // Left Arrow Decrement
   // Set up the points
   xpoints[0] = xpoints[1] = m_arrow_length;
   xpoints[2] = 0;
   ypoints[0] = 0;
   ypoints[1] = bar_height;
   ypoints[2] = bar_height/2 ;
   // Drawing it
   g.setColor(thumb_color);
   g.fillPolygon(xpoints, ypoints, 3);
   mouse_listener.setLeftDArrow( new Polygon(xpoints, ypoints, 3) );

   // Giving the arrow a 3D look
   g.setColor(thumb_highlight_color);
   //Top
   g.drawLine(xpoints[2], ypoints[2], xpoints[0], ypoints[0]);
   g.drawLine(xpoints[2]+1, ypoints[2], xpoints[0], ypoints[0]+1);
   g.setColor(thumb_shadow_color);
   //Bottom
   g.drawLine((xpoints[2]+1), (ypoints[2]+1), xpoints[1], ypoints[1]);
   g.drawLine((xpoints[2]+2), (ypoints[2]+1), xpoints[1], (ypoints[1]-1));
   //Side
   g.drawLine(xpoints[0], ypoints[0] + 1, xpoints[1], ypoints[1]-1);
   g.drawLine(xpoints[0]-1, ypoints[0]+2, xpoints[1]-1, ypoints[1]-2);

   //Left Arrow Increment
   //Set up the points
   xpoints[0] = xpoints[1] = m_arrow_length +1;
   xpoints[2] = 2*m_arrow_length + 1;
   ypoints[0] = 0;
   ypoints[1] = bar_height;
   ypoints[2] = bar_height/2 ;
   // Drawing it
   g.setColor(thumb_color);
   g.fillPolygon(xpoints, ypoints, 3);
   mouse_listener.setLeftIArrow( new Polygon(xpoints, ypoints, 3) );
   // Giving the arrow a 3D look
   g.setColor(thumb_highlight_color);
   //Top && Side
   g.drawLine(xpoints[0], ypoints[0], xpoints[2], ypoints[2]);
   g.drawLine(xpoints[0], ypoints[0]+1,xpoints[2]-1,ypoints[2]);

   g.setColor(thumb_shadow_color);
   g.drawLine(xpoints[0], ypoints[0], xpoints[1], ypoints[1]);
   g.drawLine(xpoints[0]+1, ypoints[0] + 1, xpoints[1]+1, ypoints[1]-1);
   //Bottom
   g.drawLine(xpoints[1]+2, ypoints[1]-1, xpoints[2], ypoints[2]);
   g.drawLine(xpoints[1]+2, ypoints[1]-2, xpoints[2]-1, ypoints[2]);

   //RIGHT ARROW Decrement
   // Setting up points
   xpoints[0] = xpoints[1] = bar_width - m_arrow_length -1;
   xpoints[2] = bar_width - 2*m_arrow_length -1;
   ypoints[0] = 0;
   ypoints[1] = bar_height;
   ypoints[2] = bar_height/2 ;
   // Drawing the Arrow
   g.setColor(thumb_color);
   g.fillPolygon(xpoints, ypoints, 3);
   mouse_listener.setRightDArrow( new Polygon(xpoints, ypoints, 3) );
   // Giving the arrow a 3D look
   g.setColor(thumb_highlight_color);
   //Top
   g.drawLine(xpoints[2], ypoints[2], xpoints[0], ypoints[0]);
   g.drawLine(xpoints[2]+1, ypoints[2], xpoints[0], ypoints[0]+1);
   g.setColor(thumb_shadow_color);
   //Bottom
   g.drawLine((xpoints[2]+1), (ypoints[2]+1), xpoints[1], ypoints[1]);
   g.drawLine((xpoints[2]+2), (ypoints[2]+1), xpoints[1], (ypoints[1]-1));
   //Side
   g.drawLine(xpoints[0], ypoints[0] + 1, xpoints[1], ypoints[1]-1);
   g.drawLine(xpoints[0]-1, ypoints[0]+2, xpoints[1]-1, ypoints[1]-2);

   //RIGHT ARROW Increment
   // Setting up points
   xpoints[0] = xpoints[1] = bar_width - m_arrow_length;
   xpoints[2] = bar_width;
   ypoints[0] = 0;
   ypoints[1] = bar_height;
   ypoints[2] = bar_height/2 ;
   // Drawing the Arrow
   g.setColor(thumb_color);
   g.fillPolygon(xpoints, ypoints, 3);
   mouse_listener.setRightIArrow( new Polygon(xpoints, ypoints, 3) );

   // Giving the arrow a 3D look
   g.setColor(thumb_highlight_color);
   //Top && Side
   g.drawLine(xpoints[0], ypoints[0], xpoints[2], ypoints[2]);
   g.drawLine(xpoints[0], ypoints[0]+1,xpoints[2]-1,ypoints[2]);
   g.setColor(thumb_shadow_color);
   g.drawLine(xpoints[0], ypoints[0], xpoints[1], ypoints[1]);
   g.drawLine(xpoints[0]+1, ypoints[0] + 1, xpoints[1]+1, ypoints[1]-1);
   //Bottom
   g.drawLine(xpoints[1]+2, ypoints[1]-1, xpoints[2], ypoints[2]);
   g.drawLine(xpoints[1]+2, ypoints[1]-2, xpoints[2]-1, ypoints[2]);
}



private void drawVerticalArrows(Graphics g)
{
   int height = this.bar_height;
   int xpoints[] = new int[3];
   int ypoints[] = new int[3];

   //Drawing background
   g.setColor(track_color);
   g.fillRect( 0,0,bar_width,bar_height);

   // Bottom arrow decrement
   // set up points
   xpoints[0] = bar_width/2 -1;
   xpoints[1] = 0;
   xpoints[2] = bar_width - 1;
   ypoints[0] = height;
   ypoints[1] = ypoints[2] = height - m_arrow_length;
   // draw arrow
   g.setColor(thumb_color);
   g.fillPolygon(xpoints, ypoints, 3);
   // register with the mouse listener
   mouse_listener.setLeftDArrow( new Polygon(xpoints, ypoints, 3) );
   //GIVE ARROW 3D LOOK
   g.setColor(thumb_highlight_color);
   g.drawLine(xpoints[1], ypoints[1], xpoints[2]-1, ypoints[2]);
   g.setColor(thumb_shadow_color);
   g.drawLine(xpoints[2], ypoints[2], xpoints[0], ypoints[0]);
   g.drawLine(xpoints[1]+1, ypoints[1]+1, xpoints[0], ypoints[0]);

   // Bottom arrow increment
   xpoints[0] = xpoints[1];
   xpoints[1] = xpoints[2];
   xpoints[2] = bar_width/2 -1;
   ypoints[0] = ypoints[1]-=1;
   ypoints[2] = ypoints[0] - m_arrow_length;
   g.setColor(thumb_color);
   g.fillPolygon(xpoints, ypoints, 3);
   mouse_listener.setLeftIArrow( new Polygon(xpoints, ypoints, 3) );
   g.setColor(thumb_highlight_color);
   g.drawLine(xpoints[0], ypoints[0], xpoints[2], ypoints[2]);
   g.setColor(thumb_shadow_color);
   g.drawLine(xpoints[0], ypoints[0], xpoints[1], ypoints[1]);
   g.drawLine(xpoints[1], ypoints[1], xpoints[2]+1, ypoints[2]-1);

   // Upper arrow increment
   xpoints[0] = bar_width/2 - 1;
   xpoints[1] = 0;
   xpoints[2] = bar_width - 1;
   ypoints[0] = 0;
   ypoints[1] = ypoints[2] = m_arrow_length;
   g.setColor(thumb_color);
   g.fillPolygon(xpoints, ypoints, 3);
   mouse_listener.setRightIArrow( new Polygon(xpoints, ypoints, 3) );
   g.setColor(thumb_highlight_color);
   g.drawLine( xpoints[0], ypoints[0], xpoints[1], ypoints[1] );
   g.setColor(thumb_shadow_color);
   g.drawLine( xpoints[0] +1, ypoints[0] +1, xpoints[2], ypoints[2]);
   g.drawLine( xpoints[1] +1, ypoints[1], xpoints[2], ypoints[2]);

   // Upper arrow decrement
   xpoints[0] = xpoints[1];
   xpoints[1] = xpoints[2];
   xpoints[2] = bar_width/2 - 1;
   ypoints[0] = ypoints[1]+=1;
   ypoints[2] = ypoints[2] +1 + m_arrow_length;
   g.setColor(thumb_color);
   g.fillPolygon(xpoints, ypoints, 3);
   mouse_listener.setRightDArrow( new Polygon(xpoints, ypoints, 3) );
   g.setColor(thumb_highlight_color);
   g.drawLine(xpoints[0], ypoints[0], xpoints[1]-1, ypoints[1]);
   g.setColor(thumb_shadow_color);
   g.drawLine(xpoints[1], ypoints[1], xpoints[2], ypoints[2]);
   g.drawLine(xpoints[0]+1, ypoints[0]+1, xpoints[2], ypoints[2]);
}



private void estimateArrowLength()
{
   int od;

   if (bar_orientation == HORIZONTAL) {
      m_arrow_length = (int)Math.round(0.075 * bar_width);
      od = bar_height;
    }
   else {
      m_arrow_length = (int)Math.round(0.075 * bar_height);
      od = bar_width;
    }

   // Provide a maximum size
   m_arrow_length = Math.min(m_arrow_length, 20);

   // Keep in proportion to other dimension
   m_arrow_length = Math.min(m_arrow_length, od);
}




/********************************************************************************/
/*										*/
/*	Thumb drawing methods							*/
/*										*/
/********************************************************************************/

private void drawHorizontalThumbs(Graphics g)
{
   g.setColor(track_color);

   // Instead of clearing the whole range just clean outide the thumbs
   g.fillRect(2*m_arrow_length+1, 0, left_position -thumb_width - 2*m_arrow_length-1, bar_height);
   // g.fillRect(2*m_arrow_length+1, 0, width - 4*m_arrow_length -1, bar_height);

   g.fillRect(right_position + thumb_width, 0, bar_width - 2*m_arrow_length -1-right_position - thumb_width, bar_height);
   g.drawLine(left_position, 1, right_position, 1);
   g.drawLine(left_position, bar_height, right_position, bar_height);
   g.setColor(thumb_color);

   if (left_position == right_position) {
      //Left Thumb
      g.fill3DRect(left_position-thumb_width, 1, thumb_width, bar_height/2, true);
      mouse_listener.setLeftThumb( new Rectangle(left_position-thumb_width, 1, thumb_width, (bar_height-1)/2) );

      //Right thumb
      g.fill3DRect(right_position, 1, thumb_width, bar_height/2, true);
      mouse_listener.setRightThumb( new Rectangle( right_position, 1, thumb_width, (bar_height-1)/2));

      // Tie
      g.fill3DRect(left_position - thumb_width, bar_height/2+1, 2*thumb_width, bar_height/2, true);
      mouse_listener.setMidBlock( new Rectangle(left_position - thumb_width, bar_height/2, 2*thumb_width, bar_height/2) );
    }
   else {
      // Left Thumb
      g.fill3DRect(left_position-thumb_width, 1, thumb_width, bar_height-1, true);
      //g.fill3DRect(leftPosition, 1, thumbWidth, height, true);
      mouse_listener.setLeftThumb( new Rectangle(left_position-thumb_width, 1, thumb_width, bar_height-1) );

      // Right Thumb
      g.fill3DRect(right_position,1, thumb_width, bar_height-1, true);
      mouse_listener.setRightThumb( new Rectangle(right_position, 1, thumb_width, bar_height-1) );

      // Block in between
      // Modifications added by Laurent in order to display the bounding boxes
      // of the different sliders on the filtering panel.
      g.setColor(track_highlight_color);
      g.fillRect(left_position+1, 2, right_position-left_position-1, bar_height-2);
      g.setColor(thumb_color);

      // Setting the Block for the Block Increments
      mouse_listener.setLeftBlock( new Rectangle(thumb_range_min, 0, (left_position - thumb_range_min), bar_height) );
      int pos = right_position + thumb_width + 1;
      mouse_listener.setRightBlock( new Rectangle( pos, 0, (thumb_range_max - pos), bar_height));

      // Setting the Block between the Two Thumbs
      mouse_listener.setMidBlock( new Rectangle( left_position + thumb_width, 0, (right_position - left_position - thumb_width), bar_height) );
    }
}


private void drawVerticalThumbs(Graphics g )
{
   g.setColor(track_color);

   g.fillRect(0, 2*m_arrow_length+3, bar_width, right_position - thumb_width- 2*m_arrow_length - 1);

   g.fillRect(0, left_position+thumb_width, bar_width, bar_height - 2*m_arrow_length - left_position - thumb_width);

   g.drawLine(0, left_position, 0, right_position);

   g.drawLine(bar_width, left_position, bar_width, right_position);

   g.setColor(thumb_color);

   if (left_position == right_position) {
      //Bottom Thumb
      g.fill3DRect(0, left_position, (bar_width)/2, thumb_width, true);
      mouse_listener.setLeftThumb( new Rectangle(0, left_position, (bar_width-1)/2,thumb_width));
      // Upper Thumb
      g.fill3DRect(0, right_position-thumb_width, (bar_width)/2, thumb_width, true);
      mouse_listener.setRightThumb( new Rectangle(0, right_position-thumb_width, (bar_width-1)/2, thumb_width));

      // Tie
      g.fill3DRect(bar_width/2, right_position-thumb_width, bar_width/2, 2*thumb_width, true);
      mouse_listener.setMidBlock( new Rectangle(bar_width/2, right_position-thumb_width, bar_width/2, 2*thumb_width) );
    }
   else {
      //Bottom Thumb
      g.fill3DRect(0, left_position, bar_width, thumb_width, true);
      mouse_listener.setLeftThumb( new Rectangle(0, left_position, bar_width,thumb_width));

      // Upper Thumb
      g.fill3DRect(0, right_position-thumb_width, bar_width, thumb_width, true);
      mouse_listener.setRightThumb( new Rectangle(0, right_position-thumb_width, bar_width, thumb_width));

      // Block in between
      // Modifications added by Laurent in order to display the bounding boxes
      // of the different sliders on the filtering panel.
      g.setColor(track_highlight_color);
      g.fillRect(1, right_position, bar_width-2, left_position-right_position-1);

      // Setting the blocks for block increments and the mid block for dragging
      // Setting the space between the bottom arrow and the bottom thumb.
      mouse_listener.setLeftBlock( new Rectangle(0, left_position+ thumb_width+1, bar_width, left_position - thumb_range_min) );
      // Setting the space between the top arrows and the top thumb
      mouse_listener.setRightBlock( new Rectangle( 0, thumb_range_max-thumb_width, bar_width, thumb_range_max - right_position) );

      // Setting the Block between the Two Thumbs
      mouse_listener.setMidBlock( new Rectangle( 1, right_position + 1, bar_width, left_position - right_position - 1));
    }
}



/********************************************************************************/
/*										*/
/*	Basic top-level drawing methods 					*/
/*										*/
/********************************************************************************/

private void generalSetup()
{
   thumb_range_min = thumb_range_max = thumb_range = 0;
   unit_increment = 1;
   block_increment = 10;
   thumb_width = BASIC_THUMB_WIDTH;

   motion_listener = new RangeSliderMotionAdapter();
   mouse_listener = new RangeSliderMouseAdapter();
   addMouseListener( mouse_listener );
   addMouseMotionListener( motion_listener);
}



private void graphicsInit(Dimension new_size)
{
   if ((new_size.width!= bar_width) ||( new_size.height!=bar_height)){
      bar_height = new_size.height;
      bar_width  = new_size.width;

      estimateArrowLength();

      if (this.bar_orientation == VERTICAL) {
	 thumb_range_min = bar_height - 2*m_arrow_length - thumb_width;
	 thumb_range_max = 2*m_arrow_length + thumb_width + 3;
       }
      else {
	 thumb_range_min =  2*m_arrow_length + thumb_width + 2;
	 thumb_range_max = bar_width - 2*m_arrow_length - thumb_width -2;
       }
      thumb_range = thumb_range_max - thumb_range_min;
    }
}



private void rangeSetup()
{
   if (actual_range_min>=actual_range_max) {
      actual_range_min = 0;
      actual_range_max = 100;
    }
   actual_range = actual_range_max - actual_range_min;
}



private void refresh() {
   Graphics g = getGraphics();
   refresh(g);
}


private void refresh( Graphics g )
{
   if (g!= null) {
      if (left_moved || right_moved)
	 adjustValue();
      if (left_value_changed || right_value_changed)
	 adjustPosition();
      if (bar_orientation == HORIZONTAL)
	 drawHorizontalThumbs(g);
      else
	 drawVerticalThumbs(g);
    }
}



/********************************************************************************/
/*										*/
/*	Movement handling methods						*/
/*										*/
/********************************************************************************/

private void unitIncrementRight()
{
   last_thumb_changed = RIGHT_THUMB;
   if ((right_value+unit_increment)<=actual_range_max) {
      right_value += unit_increment;
      if (adj_listener != null)
	 adj_listener.adjustmentValueChanged( new AdjustmentEvent( this, AdjustmentEvent.ADJUSTMENT_FIRST, AdjustmentEvent.UNIT_INCREMENT, right_value));
    }
   else
      right_value = actual_range_max;
   right_value_changed = true;
   refresh();
}


private void unitDecrementRight()
{
   last_thumb_changed = RIGHT_THUMB;
   if ((right_value-unit_increment)>=left_value) {
      right_value -= unit_increment;
      if (adj_listener != null)
	 adj_listener.adjustmentValueChanged( new AdjustmentEvent( this, AdjustmentEvent.ADJUSTMENT_FIRST, AdjustmentEvent.UNIT_DECREMENT, right_value));
    }
   else right_value = left_value;

   right_value_changed = true;
   refresh();
}


private void unitIncrementLeft()
{
   last_thumb_changed = LEFT_THUMB;
   if ((left_value-unit_increment)>=actual_range_min) {
      left_value-=unit_increment;
      if (adj_listener != null)
	 adj_listener.adjustmentValueChanged( new AdjustmentEvent( this, AdjustmentEvent.ADJUSTMENT_FIRST, AdjustmentEvent.UNIT_INCREMENT, left_value));
    }
   else
      left_value = actual_range_min;
   left_value_changed = true;
   refresh();
}


private void unitDecrementLeft()
{
   last_thumb_changed = LEFT_THUMB;
   if ((left_value+unit_increment)<=right_value) {
      left_value+=unit_increment;
      if (adj_listener != null)
	 adj_listener.adjustmentValueChanged( new AdjustmentEvent( this, AdjustmentEvent.ADJUSTMENT_FIRST, AdjustmentEvent.UNIT_DECREMENT, left_value));
    }
   else
      left_value = right_value;
   left_value_changed = true;
   refresh();
}



/********************************************************************************/
/*										*/
/*	Mouse handler								*/
/*										*/
/********************************************************************************/

private class RangeSliderMouseAdapter extends MouseAdapter {

   private Polygon leftIArrow, rightIArrow,leftDArrow, rightDArrow;
   private Rectangle leftThumb, rightThumb, leftBlock, rightBlock, midBlock;

   RangeSliderMouseAdapter() {
      super();
    }

   void setLeftIArrow( Polygon p) {
      leftIArrow = p;
    }

   void setLeftDArrow( Polygon p) {
      leftDArrow = p;
    }

   void setRightIArrow( Polygon p) {
      rightIArrow = p;
    }

   void setRightDArrow( Polygon p) {
      rightDArrow = p;
    }

   void setLeftThumb(Rectangle r) {
      leftThumb = r;
    }

   void setRightThumb(Rectangle r) {
      rightThumb = r;
    }

   void setLeftBlock( Rectangle r) {
      leftBlock = r;
    }

   void setRightBlock( Rectangle r) {
      rightBlock = r;
    }

   void setMidBlock( Rectangle r) {
      midBlock = r;
    }

   @Override public void mouseClicked(MouseEvent e) {
      Point clickedAt = e.getPoint();
      if (leftDArrow.contains(clickedAt)) unitIncrementLeft();
      else if (rightIArrow.contains(clickedAt)) unitIncrementRight();
      else if (leftIArrow.contains(clickedAt)) unitDecrementLeft();
      else if (rightDArrow.contains(clickedAt)) unitDecrementRight();
      else if (leftBlock.contains(clickedAt)) blockIncrementLeft();
      else if (rightBlock.contains(clickedAt)) blockIncrementRight();
    }

   @Override public void mousePressed(MouseEvent e) {
      Point pressedAt = e.getPoint();

      if (pressedAt == null) return;
      if (leftThumb == null) return;

      if (leftThumb.contains(pressedAt)) motion_listener.setLeftThumbDragMode(true);
      else if (rightThumb.contains(pressedAt)) motion_listener.setRightThumbDragMode(true);
      else if (midBlock.contains(pressedAt)) {
	 if (bar_orientation == HORIZONTAL)
	    motion_listener.setMidDragMode( true, e.getX() );
	 else
	    motion_listener.setMidDragMode( true, e.getY() );
       }
    }

   @Override public void mouseReleased(MouseEvent e) {
      motion_listener.setLeftThumbDragMode( false );
      motion_listener.setRightThumbDragMode( false );
      if ( bar_orientation == HORIZONTAL )
	 motion_listener.setMidDragMode( false, e.getX());
      else
	 motion_listener.setMidDragMode( false, e.getY());
    }

}	// end of innerclass RangeSliderMouseAdapter




/********************************************************************************/
/*										*/
/*	Mouse movement handler							*/
/*										*/
/********************************************************************************/

private class RangeSliderMotionAdapter extends MouseMotionAdapter {

   private boolean leftThumbDragMode, rightThumbDragMode, midDragMode;
   private int initialPos;

   RangeSliderMotionAdapter() {
      super();
    }

   void setLeftThumbDragMode( boolean mode) {
      leftThumbDragMode = mode;
    }

   void setRightThumbDragMode( boolean mode) {
      rightThumbDragMode = mode;
    }

   void setMidDragMode( boolean mode, int pos) {
      midDragMode = mode;
      setInitialPos( pos);
    }

   void setInitialPos( int p ) {
      initialPos = p;
    }

   @Override public void mouseDragged(MouseEvent e) {
      if (leftThumbDragMode) {
         if (bar_orientation == HORIZONTAL) dragLeftThumb( e.getX() );
         else dragLeftThumb( e.getY());
       }
      else if (rightThumbDragMode) {
         if (bar_orientation == HORIZONTAL) dragRightThumb( e.getX() );
         else dragRightThumb( e.getY() );
       }
      else if (midDragMode) {
         int drag;
         if (bar_orientation == HORIZONTAL) drag = e.getX();
         else drag = e.getY();
         dragBar(initialPos, drag);
       }
    }

}	// end of inner class RangeSliderMotionAdapter




/********************************************************************************/
/*										*/
/*	Test program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   new SwingSetup();

   JFrame frm = new JFrame();
   frm.setMinimumSize(new Dimension(5,5));
   SwingRangeScrollBar rs = new SwingRangeScrollBar(HORIZONTAL,0,100,10,20);
   System.err.println("SIZE " + rs.getMinimumSize());
   rs.setSize(400,10);
   frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   frm.getContentPane().add(rs);
   frm.pack();
   frm.setSize(400,10);
   System.err.println("FMIN  " + frm.getMinimumSize());
   System.err.println("FPRE " + frm.getPreferredSize());
   System.err.println("FSIZ " + frm.getSize());
   frm.setVisible(true);
}




}	// end of class SwingRangeScrollBar




/* end of SwingRangeScrollBar.java */
