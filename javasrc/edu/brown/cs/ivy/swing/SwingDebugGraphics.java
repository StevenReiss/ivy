/********************************************************************************/
/*										*/
/*		SwingDebugGraphics.java 					*/
/*										*/
/*	DebugGraphics2D implementation						*/
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
 * @(#)DebugGraphics.java	1.27 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */



package edu.brown.cs.ivy.swing;


import javax.swing.JComponent;
import javax.swing.JFrame;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;


/**
 * Graphics subclass supporting graphics debugging. Overrides most methods
 * from Graphics.  SwingDebugGraphics objects are rarely created by hand.  They
 * are most frequently created automatically when a JComponent's
 * debugGraphicsOptions are changed using the setDebugGraphicsOptions()
 * method.
 * <p>
 * NOTE: You must turn off double buffering to use DebugGraphics:
 *	 RepaintManager repaintManager = RepaintManager.currentManager(component);
 *	 repaintManager.setDoubleBufferingEnabled(false);
 *
 * @see JComponent#setDebugGraphicsOptions
 * @see RepaintManager#currentManager
 * @see RepaintManager#setDoubleBufferingEnabled
 *
 * @version 1.27 11/17/05
 * @author Dave Karlton
 */

public class SwingDebugGraphics extends Graphics2D {


private Graphics2D		graphics_2d;
private Image			image_buffer;
private int			debug_options;
private int			graphics_ID	= graphics_count++;
private int			x_offset;
private int                     y_offset;

private static int	graphics_count	= 0;

/** Log graphics operations. */
public static final int	LOG_OPTION	= 1 << 0;
/** Flash graphics operations. */
public static final int	FLASH_OPTION	= 1 << 1;
/** Show buffered operations in a separate <code>Frame</code>. */
public static final int	BUFFERED_OPTION	= 1 << 2;
/** Don't debug graphics operations. */
public static final int	NONE_OPTION	= -1;


/**
 * Constructs a new debug graphics context that supports slowed
 * down drawing.
 */
public SwingDebugGraphics()
{
   super();
   image_buffer = null;
   x_offset = 0;
   y_offset = 0;
}

/**
 * Constructs a debug graphics context from an existing graphics
 * context that supports slowed down drawing.
 *
 * @param graphics	the Graphics context to slow down
 */
public SwingDebugGraphics(Graphics2D graphics)
{
   this();
   this.graphics_2d = graphics;
}

/**
 * Overrides <code>Graphics.create</code> to return a DebugGraphics object.
 */
@Override
public Graphics create()
{
   SwingDebugGraphics debugGraphics;
   
   debugGraphics = new SwingDebugGraphics();
   debugGraphics.graphics_2d = (Graphics2D) graphics_2d.create();
   debugGraphics.debug_options = debug_options;
   debugGraphics.image_buffer = image_buffer;
   
   return debugGraphics;
}

/**
 * Overrides <code>Graphics.create</code> to return a DebugGraphics object.
 */
@Override
public Graphics create(int x,int y,int width,int height)
{
   SwingDebugGraphics debugGraphics;
   
   debugGraphics = new SwingDebugGraphics();
   debugGraphics.graphics_2d = (Graphics2D) graphics_2d.create(x, y, width, height);
   debugGraphics.debug_options = debug_options;
   debugGraphics.image_buffer = image_buffer;
   debugGraphics.x_offset = x_offset + x;
   debugGraphics.y_offset = y_offset + y;
   
   return debugGraphics;
}


//------------------------------------------------
//  NEW METHODS
//------------------------------------------------

/**
 * Sets the Color used to flash drawing operations.
 */
public static void setFlashColor(Color flashColor)
{
   info().flash_color = flashColor;
}

/**
 * Returns the Color used to flash drawing operations.
 * @see #setFlashColor
 */
public static Color flashColor()
{
   return info().flash_color;
}

/**
 * Sets the time delay of drawing operation flashing.
 */
public static void setFlashTime(int flashTime)
{
   info().flash_time = flashTime;
}

/**
 * Returns the time delay of drawing operation flashing.
 * @see #setFlashTime
 */
public static int flashTime()
{
   return info().flash_time;
}

/**
 * Sets the number of times that drawing operations will flash.
 */
public static void setFlashCount(int flashCount)
{
   info().flash_count = flashCount;
}

/** Returns the number of times that drawing operations will flash.
 * @see #setFlashCount
 */
public static int flashCount()
{
   return info().flash_count;
}

/** Sets the stream to which the DebugGraphics logs drawing operations.
 */
public static void setLogStream(java.io.PrintStream stream)
{
   info().debug_stream = stream;
}

/** Returns the stream to which the DebugGraphics logs drawing operations.
 * @see #setLogStream
 */
public static java.io.PrintStream logStream()
{
   return info().debug_stream;
}

/** Sets the Font used for text drawing operations.
 */
@Override
public void setFont(Font aFont)
{
   if (debugLog()) {
      info().log(toShortString() + " Setting font: " + aFont);
    }
   graphics_2d.setFont(aFont);
}

/** Returns the Font used for text drawing operations.
 * @see #setFont
 */
@Override
public Font getFont()
{
   return graphics_2d.getFont();
}

/** Sets the color to be used for drawing and filling lines and shapes.
 */
@Override
public void setColor(Color aColor)
{
   if (debugLog()) {
      info().log(toShortString() + " Setting color: " + aColor);
    }
   graphics_2d.setColor(aColor);
}

/** Returns the Color used for text drawing operations.
 * @see #setColor
 */
@Override
public Color getColor()
{
   return graphics_2d.getColor();
}


//-----------------------------------------------
// OVERRIDDEN METHODS
//------------------------------------------------

/**
 * Overrides <code>Graphics.getFontMetrics</code>.
 */
@Override
public FontMetrics getFontMetrics()
{
   return graphics_2d.getFontMetrics();
}

/**
 * Overrides <code>Graphics.getFontMetrics</code>.
 */
@Override
public FontMetrics getFontMetrics(Font f)
{
   return graphics_2d.getFontMetrics(f);
}

/**
 * Overrides <code>Graphics.translate</code>.
 */
@Override
public void translate(int x,int y)
{
   if (debugLog()) {
      info().log(toShortString() + " Translating by: " + new Point(x,y));
    }
   x_offset += x;
   y_offset += y;
   graphics_2d.translate(x, y);
}

/**
 * Overrides <code>Graphics.setPaintMode</code>.
 */
@Override
public void setPaintMode()
{
   if (debugLog()) {
      info().log(toShortString() + " Setting paint mode");
    }
   graphics_2d.setPaintMode();
}

/**
 * Overrides <code>Graphics.setXORMode</code>.
 */
@Override
public void setXORMode(Color aColor)
{
   if (debugLog()) {
      info().log(toShortString() + " Setting XOR mode: " + aColor);
    }
   graphics_2d.setXORMode(aColor);
}

/**
 * Overrides <code>Graphics.getClipBounds</code>.
 */
@Override
public Rectangle getClipBounds()
{
   return graphics_2d.getClipBounds();
}

/**
 * Overrides <code>Graphics.clipRect</code>.
 */
@Override
public void clipRect(int x,int y,int width,int height)
{
   graphics_2d.clipRect(x, y, width, height);
   if (debugLog()) {
      info().log(toShortString() + " Setting clipRect: " + 
            (new Rectangle(x,y,width,height)) +
            " New clipRect: " + graphics_2d.getClip());
    }
}

/**
 * Overrides <code>Graphics.setClip</code>.
 */
@Override
public void setClip(int x,int y,int width,int height)
{
   graphics_2d.setClip(x, y, width, height);
   if (debugLog()) {
      info().log(toShortString() + " Setting new clipRect: " + graphics_2d.getClip());
    }
}

/**
 * Overrides <code>Graphics.getClip</code>.
 */
@Override
public Shape getClip()
{
   return graphics_2d.getClip();
}

/**
 * Overrides <code>Graphics.setClip</code>.
 */
@Override
public void setClip(Shape clip)
{
   graphics_2d.setClip(clip);
   if (debugLog()) {
      info().log(toShortString() + " Setting new clipRect: " + graphics_2d.getClip());
    }
}

/**
 * Overrides <code>Graphics.drawRect</code>.
 */
@Override
public void drawRect(int x,int y,int width,int height)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing rect: " + new Rectangle(x,y,width,height));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawRect(x, y, width, height);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawRect(x, y, width, height);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawRect(x, y, width, height);
}

/**
 * Overrides <code>Graphics.fillRect</code>.
 */
@Override
public void fillRect(int x,int y,int width,int height)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Filling rect: " + new Rectangle(x,y,width,height));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.fillRect(x, y, width, height);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.fillRect(x, y, width, height);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.fillRect(x, y, width, height);
}

/**
 * Overrides <code>Graphics.clearRect</code>.
 */
@Override
public void clearRect(int x,int y,int width,int height)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Clearing rect: " + new Rectangle(x,y,width,height));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.clearRect(x, y, width, height);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.clearRect(x, y, width, height);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.clearRect(x, y, width, height);
}

/**
 * Overrides <code>Graphics.drawRoundRect</code>.
 */
@Override
public void drawRoundRect(int x,int y,int width,int height,int arcWidth,int arcHeight)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing round rect: " + 
            new Rectangle(x,y,width,height) +
            " arcWidth: " + arcWidth + " archHeight: " + arcHeight);
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
}

/**
 * Overrides <code>Graphics.fillRoundRect</code>.
 */
@Override
public void fillRoundRect(int x,int y,int width,int height,int arcWidth,int arcHeight)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Filling round rect: " + 
            new Rectangle(x,y,width,height) +
            " arcWidth: " + arcWidth + " archHeight: " + arcHeight);
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
}

/**
 * Overrides <code>Graphics.drawLine</code>.
 */
@Override
public void drawLine(int x1,int y1,int x2,int y2)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing line: from " + 
            pointToString(x1, y1) + " to " +
            pointToString(x2, y2));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawLine(x1, y1, x2, y2);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawLine(x1, y1, x2, y2);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawLine(x1, y1, x2, y2);
}

/**
 * Overrides <code>Graphics.draw3DRect</code>.
 */
@Override
public void draw3DRect(int x,int y,int width,int height,boolean raised)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing 3D rect: " +
            new Rectangle(x,y,width,height) +
            " Raised bezel: " + raised);
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.draw3DRect(x, y, width, height, raised);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.draw3DRect(x, y, width, height, raised);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.draw3DRect(x, y, width, height, raised);
}

/**
 * Overrides <code>Graphics.fill3DRect</code>.
 */
@Override
public void fill3DRect(int x,int y,int width,int height,boolean raised)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Filling 3D rect: " + 
            new Rectangle(x,y,width,height) +
            " Raised bezel: " + raised);
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.fill3DRect(x, y, width, height, raised);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.fill3DRect(x, y, width, height, raised);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.fill3DRect(x, y, width, height, raised);
}

/**
 * Overrides <code>Graphics.drawOval</code>.
 */
@Override
public void drawOval(int x,int y,int width,int height)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing oval: " + new Rectangle(x,y,width,height));
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawOval(x, y, width, height);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawOval(x, y, width, height);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawOval(x, y, width, height);
}

/**
 * Overrides <code>Graphics.fillOval</code>.
 */
@Override
public void fillOval(int x,int y,int width,int height)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Filling oval: " + new Rectangle(x,y,width,height));
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.fillOval(x, y, width, height);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.fillOval(x, y, width, height);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.fillOval(x, y, width, height);
}

/**
 * Overrides <code>Graphics.drawArc</code>.
 */
@Override
public void drawArc(int x,int y,int width,int height,int startAngle,int arcAngle)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing arc: " +
            new Rectangle(x,y,width,height) +
            " startAngle: " + startAngle + " arcAngle: " + arcAngle);
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawArc(x, y, width, height, startAngle, arcAngle);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawArc(x, y, width, height, startAngle, arcAngle);
}

/**
 * Overrides <code>Graphics.fillArc</code>.
 */
@Override
public void fillArc(int x,int y,int width,int height,int startAngle,int arcAngle)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Filling arc: " + 
            new Rectangle(x,y,width,height) +
            " startAngle: " + startAngle + " arcAngle: " + arcAngle);
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.fillArc(x, y, width, height, startAngle, arcAngle);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.fillArc(x, y, width, height, startAngle, arcAngle);
}

/**
 * Overrides <code>Graphics.drawPolyline</code>.
 */
@Override
public void drawPolyline(int [] xPoints,int [] yPoints,int nPoints)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing polyline: " + " nPoints: " + 
            nPoints + " X's: " +
            Arrays.toString(xPoints) + " Y's: " + Arrays.toString(yPoints));
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawPolyline(xPoints, yPoints, nPoints);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawPolyline(xPoints, yPoints, nPoints);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawPolyline(xPoints, yPoints, nPoints);
}

/**
 * Overrides <code>Graphics.drawPolygon</code>.
 */
@Override
public void drawPolygon(int [] xPoints,int [] yPoints,int nPoints)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing polygon: " + " nPoints: " + 
            nPoints + " X's: " +
            Arrays.toString(xPoints) + " Y's: " + Arrays.toString(yPoints));
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawPolygon(xPoints, yPoints, nPoints);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawPolygon(xPoints, yPoints, nPoints);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawPolygon(xPoints, yPoints, nPoints);
}

/**
 * Overrides <code>Graphics.fillPolygon</code>.
 */
@Override
public void fillPolygon(int [] xPoints,int [] yPoints,int nPoints)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Filling polygon: " +
            " nPoints: " + nPoints + " X's: " +
            Arrays.toString(xPoints) + " Y'`: " + Arrays.toString(yPoints));
    }
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.fillPolygon(xPoints, yPoints, nPoints);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.fillPolygon(xPoints, yPoints, nPoints);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.fillPolygon(xPoints, yPoints, nPoints);
}

/**
 * Overrides <code>Graphics.drawString</code>.
 */
@Override
public void drawString(String aString,int x,int y)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing string: \"" + aString + "\" at: " +
            new Point(x,y));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawString(aString, x, y);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawString(aString, x, y);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawString(aString, x, y);
}

/**
 * Overrides <code>Graphics.drawString</code>.
 */
@Override
public void drawString(AttributedCharacterIterator iterator,int x,int y)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing text: \"" + iterator + "\" at: " +
            new Point(x,y));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawString(iterator, x, y);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawString(iterator, x, y);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawString(iterator, x, y);
}

/**
 * Overrides <code>Graphics.drawBytes</code>.
 */
@Override
public void drawBytes(byte [] data,int offset,int length,int x,int y)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing bytes at: " + new Point(x,y));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawBytes(data, offset, length, x, y);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawBytes(data, offset, length, x, y);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawBytes(data, offset, length, x, y);
}

/**
 * Overrides <code>Graphics.drawChars</code>.
 */
@Override
public void drawChars(char [] data,int offset,int length,int x,int y)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info().log(toShortString() + " Drawing chars at " + new Point(x,y));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawChars(data, offset, length, x, y);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      Color oldColor = getColor();
      int count = (info.flash_count * 2) - 1;
      
      for (int i = 0; i < count; i++) {
	 graphics_2d.setColor((i % 2) == 0 ? info.flash_color : oldColor);
	 graphics_2d.drawChars(data, offset, length, x, y);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
      graphics_2d.setColor(oldColor);
    }
   graphics_2d.drawChars(data, offset, length, x, y);
}

/**
 * Overrides <code>Graphics.drawImage</code>.
 */
@Override
public boolean drawImage(Image img,int x,int y,ImageObserver observer)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info.log(toShortString() + " Drawing image: " + img + " at: " + new Point(x,y));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawImage(img, x, y, observer);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      int count = (info.flash_count * 2) - 1;
      ImageProducer oldProducer = img.getSource();
      ImageProducer newProducer = new FilteredImageSource(oldProducer,
            new DebugGraphicsFilter(info.flash_color));
      Image newImage = Toolkit.getDefaultToolkit().createImage(newProducer);
      DebugGraphicsObserver imageObserver = new DebugGraphicsObserver();
      
      Image imageToDraw;
      for (int i = 0; i < count; i++) {
	 imageToDraw = (i % 2) == 0 ? newImage : img;
	 loadImage(imageToDraw);
	 graphics_2d.drawImage(imageToDraw, x, y, imageObserver);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
    }
   return graphics_2d.drawImage(img, x, y, observer);
}

/**
 * Overrides <code>Graphics.drawImage</code>.
 */
@Override
public boolean drawImage(Image img,int x,int y,int width,int height,
      ImageObserver observer)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info.log(toShortString() + " Drawing image: " + img + " at: " +
            new Rectangle(x,y,width,height));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawImage(img, x, y, width, height, observer);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      int count = (info.flash_count * 2) - 1;
      ImageProducer oldProducer = img.getSource();
      ImageProducer newProducer = new FilteredImageSource(oldProducer,
            new DebugGraphicsFilter(info.flash_color));
      Image newImage = Toolkit.getDefaultToolkit().createImage(newProducer);
      DebugGraphicsObserver imageObserver = new DebugGraphicsObserver();
      
      Image imageToDraw;
      for (int i = 0; i < count; i++) {
	 imageToDraw = (i % 2) == 0 ? newImage : img;
	 loadImage(imageToDraw);
	 graphics_2d.drawImage(imageToDraw, x, y, width, height, imageObserver);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
    }
   return graphics_2d.drawImage(img, x, y, width, height, observer);
}

/**
 * Overrides <code>Graphics.drawImage</code>.
 */
@Override
public boolean drawImage(Image img,int x,int y,Color bgcolor,ImageObserver observer)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info.log(toShortString() + " Drawing image: " + img + " at: " + new Point(x,y) +
            ", bgcolor: " + bgcolor);
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawImage(img, x, y, bgcolor, observer);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      int count = (info.flash_count * 2) - 1;
      ImageProducer oldProducer = img.getSource();
      ImageProducer newProducer = new FilteredImageSource(oldProducer,
            new DebugGraphicsFilter(info.flash_color));
      Image newImage = Toolkit.getDefaultToolkit().createImage(newProducer);
      DebugGraphicsObserver imageObserver = new DebugGraphicsObserver();
      
      Image imageToDraw;
      for (int i = 0; i < count; i++) {
	 imageToDraw = (i % 2) == 0 ? newImage : img;
	 loadImage(imageToDraw);
	 graphics_2d.drawImage(imageToDraw, x, y, bgcolor, imageObserver);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
    }
   return graphics_2d.drawImage(img, x, y, bgcolor, observer);
}

/**
 * Overrides <code>Graphics.drawImage</code>.
 */
@Override
public boolean drawImage(Image img,int x,int y,int width,int height,Color bgcolor,
      ImageObserver observer)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info.log(toShortString() + " Drawing image: " + img + " at: " +
            new Rectangle(x,y,width,height) + ", bgcolor: " + bgcolor);
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawImage(img, x, y, width, height, bgcolor, observer);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      int count = (info.flash_count * 2) - 1;
      ImageProducer oldProducer = img.getSource();
      ImageProducer newProducer = new FilteredImageSource(oldProducer,
            new DebugGraphicsFilter(info.flash_color));
      Image newImage = Toolkit.getDefaultToolkit().createImage(newProducer);
      DebugGraphicsObserver imageObserver = new DebugGraphicsObserver();
      
      Image imageToDraw;
      for (int i = 0; i < count; i++) {
	 imageToDraw = (i % 2) == 0 ? newImage : img;
	 loadImage(imageToDraw);
	 graphics_2d.drawImage(imageToDraw, x, y, width, height, bgcolor, imageObserver);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
    }
   return graphics_2d.drawImage(img, x, y, width, height, bgcolor, observer);
}

/**
 * Overrides <code>Graphics.drawImage</code>.
 */
@Override
public boolean drawImage(Image img,int dx1,int dy1,int dx2,int dy2,int sx1,int sy1,
      int sx2,int sy2,ImageObserver observer)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info.log(toShortString() + " Drawing image: " + img + " destination: " +
            new Rectangle(dx1,dy1,dx2,dy2) + " source: " +
            new Rectangle(sx1,sy1,sx2,sy2));
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      int i = 0;
      int count = (info.flash_count * 2) - 1;
      ImageProducer oldProducer = img.getSource();
      ImageProducer newProducer = new FilteredImageSource(oldProducer,
            new DebugGraphicsFilter(info.flash_color));
      Image newImage = Toolkit.getDefaultToolkit().createImage(newProducer);
      DebugGraphicsObserver imageObserver = new DebugGraphicsObserver();
      
      Image imageToDraw;
      for (i = 0; i < count; i++) {
	 imageToDraw = (i % 2) == 0 ? newImage : img;
	 loadImage(imageToDraw);
	 graphics_2d.drawImage(imageToDraw, dx1, dy1, dx2, dy2,
               sx1, sy1, sx2, sy2,
               imageObserver);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
    }
   return graphics_2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
}

/**
 * Overrides <code>Graphics.drawImage</code>.
 */
@Override
public boolean drawImage(Image img,int dx1,int dy1,int dx2,int dy2,int sx1,int sy1,
      int sx2,int sy2,Color bgcolor,ImageObserver observer)
{
   DebugInfo info = info();
   
   if (debugLog()) {
      info.log(toShortString() + " Drawing image: " + img + " destination: " +
            new Rectangle(dx1,dy1,dx2,dy2) + " source: " +
            new Rectangle(sx1,sy1,sx2,sy2) + ", bgcolor: " + bgcolor);
    }
   
   if (isDrawingBuffer()) {
      if (debugBuffered()) {
	 Graphics debugGraphics = debugGraphics();
         
	 debugGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor,
               observer);
	 debugGraphics.dispose();
       }
    }
   else if (debugFlash()) {
      int i = 0;
      int count = (info.flash_count * 2) - 1;
      ImageProducer oldProducer = img.getSource();
      ImageProducer newProducer = new FilteredImageSource(oldProducer,
            new DebugGraphicsFilter(info.flash_color));
      Image newImage = Toolkit.getDefaultToolkit().createImage(newProducer);
      DebugGraphicsObserver imageObserver = new DebugGraphicsObserver();
      
      Image imageToDraw;
      for (i = 0; i < count; i++) {
	 imageToDraw = (i % 2) == 0 ? newImage : img;
	 loadImage(imageToDraw);
	 graphics_2d.drawImage(imageToDraw, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
               bgcolor, imageObserver);
	 Toolkit.getDefaultToolkit().sync();
	 sleep(info.flash_time);
       }
    }
   return graphics_2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor,
         observer);
}

static void loadImage(Image img)
{
   // imageLoadingIcon.loadImage(img);
}


/**
 * Overrides <code>Graphics.copyArea</code>.
 */
@Override
public void copyArea(int x,int y,int width,int height,int destX,int destY)
{
   if (debugLog()) {
      info().log(toShortString() + " Copying area from: " +
            new Rectangle(x,y,width,height) + " to: " + new Point(destX,destY));
    }
   graphics_2d.copyArea(x, y, width, height, destX, destY);
}

final void sleep(int mSecs)
{
   try {
      Thread.sleep(mSecs);
    }
   catch (Exception e) {
    }
}

/**
 * Overrides <code>Graphics.dispose</code>.
 */
@Override
public void dispose()
{
   graphics_2d.dispose();
   graphics_2d = null;
}

// ALERT!
/**
 * Returns the drawingBuffer value.
 *
 * @return true if this object is drawing from a Buffer
 */
public boolean isDrawingBuffer()
{
   return image_buffer != null;
}

String toShortString()
{
   StringBuffer buffer = new StringBuffer("Graphics" + (isDrawingBuffer() ? "<B>" : "") +
         "(" + graphics_ID + "-" + debug_options + ")");
   return buffer.toString();
}

String pointToString(int x,int y)
{
   StringBuffer buffer = new StringBuffer("(" + x + ", " + y + ")");
   return buffer.toString();
}

/** Enables/disables diagnostic information about every graphics
 * operation. The value of <b>options</b> indicates how this information
 * should be displayed. LOG_OPTION causes a text message to be printed.
 * FLASH_OPTION causes the drawing to flash several times. BUFFERED_OPTION
 * creates a new Frame that shows each operation on an
 * offscreen buffer. The value of <b>options</b> is bitwise OR'd into
 * the current value. To disable debugging use NONE_OPTION.
 */
public void setDebugOptions(int options)
{
   if (options != 0) {
      if (options == NONE_OPTION) {
	 if (debug_options != 0) {
	    System.err.println(toShortString() + " Disabling debug");
	    debug_options = 0;
          }
       }
      else {
	 if (debug_options != options) {
	    debug_options |= options;
	    if (debugLog()) {
	       System.err.println(toShortString() + " Enabling debug");
             }
          }
       }
    }
}

/** Returns the current debugging options for this DebugGraphics.
 * @see #setDebugOptions
 */
public int getDebugOptions()
{
   return debug_options;
}

/** Static wrapper method for DebugInfo.setDebugOptions(). Stores
 * options on a per component basis.
 */
static void setDebugOptions(JComponent component,int options)
{
   info().setDebugOptions(component, options);
}

/** Static wrapper method for DebugInfo.getDebugOptions().
 */
static int getDebugOptions(JComponent component)
{
   DebugInfo dgi = info();
   if (dgi == null) {
      return 0;
    }
   return dgi.getDebugOptions(component);
}

/** Returns non-zero if <b>component</b> should display with DebugGraphics,
 * zero otherwise. Walks the JComponent's parent tree to determine if
 * any debugging options have been set.
 */
static int shouldComponentDebug(JComponent component)
{
   DebugInfo info = info();
   if (info == null) {
      return 0;
    }
   else {
      Container container = component;
      int debugOptions = 0;
      
      while (container != null && (container instanceof JComponent)) {
	 debugOptions |= info.getDebugOptions((JComponent) container);
	 container = container.getParent();
       }
      
      return debugOptions;
    }
}

/** Returns the number of JComponents that have debugging options turned
 * on.
 */
static int debugComponentCount()
{
   return 1;
}


boolean debugLog()
{
   return (debug_options & LOG_OPTION) == LOG_OPTION;
}

boolean debugFlash()
{
   return (debug_options & FLASH_OPTION) == FLASH_OPTION;
}

boolean debugBuffered()
{
   return (debug_options & BUFFERED_OPTION) == BUFFERED_OPTION;
}

/** Returns a DebugGraphics for use in buffering window.
 */
private Graphics debugGraphics()
{
   SwingDebugGraphics debugGraphics;
   DebugInfo info = info();
   JFrame debugFrame;
   
   if (info.debug_frame == null) {
      info.debug_frame = new JFrame();
      info.debug_frame.setSize(500, 500);
    }
   debugFrame = info.debug_frame;
   debugFrame.setVisible(true);
   debugGraphics = new SwingDebugGraphics((Graphics2D) debugFrame.getGraphics());
   debugGraphics.setFont(getFont());
   debugGraphics.setColor(getColor());
   debugGraphics.translate(x_offset, y_offset);
   debugGraphics.setClip(getClipBounds());
   if (debugFlash()) {
      debugGraphics.setDebugOptions(FLASH_OPTION);
    }
   return debugGraphics;
}

/** Returns DebugInfo, or creates one if none exists.
 */
private static DebugInfo debug_graphics_info = new DebugInfo();

static DebugInfo info()
{
   return debug_graphics_info;
}


/********************************************************************************/
/*										*/
/*	Graphics2D methods							*/
/*										*/
/********************************************************************************/

@Override
public void addRenderingHints(Map<?, ?> hints)
{
   if (debugLog()) {
      info().log(toShortString() + " addRenderingHints " + hints);
    }
   graphics_2d.addRenderingHints(hints);
}


@Override
public void clip(Shape s)
{
   if (debugLog()) {
      info().log(toShortString() + " clip shape " + s);
    }
   graphics_2d.clip(s);
}


@Override
public void draw(Shape s)
{
   if (debugLog()) {
      info().log(toShortString() + " draw shape " + s);
    }
   graphics_2d.draw(s);
}


@Override
public void drawGlyphVector(GlyphVector g,float x,float y)
{
   if (debugLog()) {
      info().log(toShortString() + " drawGlyphVector " + g + " " + x + " " + y);
    }
   graphics_2d.drawGlyphVector(g, x, y);
}


@Override
public void drawImage(BufferedImage img,BufferedImageOp op,int x,int y)
{
   if (debugLog()) {
      info().log(toShortString() + " drawImage " + img + " " + op + " " + x + " " + y);
    }
   graphics_2d.drawImage(img, op, x, y);
}


@Override
public boolean drawImage(Image img,AffineTransform t,ImageObserver obs)
{
   if (debugLog()) {
      info().log(toShortString() + " drawImage " + img + " " + t + " " + obs);
    }
   return graphics_2d.drawImage(img, t, obs);
}


@Override
public void drawRenderableImage(RenderableImage img,AffineTransform t)
{
   if (debugLog()) {
      info().log(toShortString() + " drawRenderableImage " + t);
    }
   graphics_2d.drawRenderableImage(img, t);
}


@Override
public void drawRenderedImage(RenderedImage img,AffineTransform t)
{
   if (debugLog()) {
      info().log(toShortString() + " drawRenderedImage " + t);
    }
   graphics_2d.drawRenderedImage(img, t);
}


@Override
public void drawString(AttributedCharacterIterator it,float x,float y)
{
   if (debugLog()) {
      info().log(toShortString() + " drawString " + x + " " + y);
    }
   graphics_2d.drawString(it, x, y);
}


@Override
public void drawString(String s,float x,float y)
{
   if (debugLog()) {
      info().log(toShortString() + " drawString " + x + " " + y + " : " + s);
    }
   graphics_2d.drawString(s, x, y);
}


@Override
public void fill(Shape s)
{
   if (debugLog()) {
      info().log(toShortString() + " fill " + s);
    }
   graphics_2d.fill(s);
}


@Override
public Color getBackground()
{
   return graphics_2d.getBackground();
}

@Override
public Composite getComposite()
{
   return graphics_2d.getComposite();
}

@Override
public GraphicsConfiguration getDeviceConfiguration()
{
   return graphics_2d.getDeviceConfiguration();
}

@Override
public FontRenderContext getFontRenderContext()
{
   return graphics_2d.getFontRenderContext();
}

@Override
public Paint getPaint()
{
   return graphics_2d.getPaint();
}

@Override
public Object getRenderingHint(RenderingHints.Key k)
{
   return graphics_2d.getRenderingHint(k);
}

@Override
public RenderingHints getRenderingHints()
{
   return graphics_2d.getRenderingHints();
}

@Override
public Stroke getStroke()
{
   return graphics_2d.getStroke();
}

@Override
public AffineTransform getTransform()
{
   return graphics_2d.getTransform();
}

@Override
public boolean hit(Rectangle r,Shape s,boolean on)
{
   return graphics_2d.hit(r, s, on);
}


@Override
public void rotate(double t)
{
   if (debugLog()) {
      info().log(toShortString() + " rotate " + t);
    }
   graphics_2d.rotate(t);
}


@Override
public void rotate(double t,double x,double y)
{
   if (debugLog()) {
      info().log(toShortString() + " rotate " + t + " " + x + " " + y);
    }
   graphics_2d.rotate(t, x, y);
}


@Override
public void scale(double x,double y)
{
   if (debugLog()) {
      info().log(toShortString() + " scale " + x + " " + y);
    }
   graphics_2d.scale(x, y);
}


@Override
public void setBackground(Color c)
{
   if (debugLog()) {
      info().log(toShortString() + " setBackground " + c);
    }
   graphics_2d.setBackground(c);
}


@Override
public void setComposite(Composite c)
{
   if (debugLog()) {
      info().log(toShortString() + " setComposite " + c);
    }
   graphics_2d.setComposite(c);
}


@Override
public void setPaint(Paint p)
{
   if (debugLog()) {
      info().log(toShortString() + " setPaint " + p);
    }
   graphics_2d.setPaint(p);
}


@Override
public void setRenderingHint(RenderingHints.Key k,Object v)
{
   if (debugLog()) {
      info().log(toShortString() + " setRenderingHint " + k + " " + v);
    }
   graphics_2d.setRenderingHint(k, v);
}


@Override
public void setRenderingHints(Map<?, ?> h)
{
   if (debugLog()) {
      info().log(toShortString() + " setRenderingHints " + h);
    }
   graphics_2d.setRenderingHints(h);
}


@Override
public void setStroke(Stroke s)
{
   if (debugLog()) {
      info().log(toShortString() + " setStroke " + s);
    }
   graphics_2d.setStroke(s);
}


@Override
public void setTransform(AffineTransform t)
{
   if (debugLog()) {
      info().log(toShortString() + " setTransform " + t);
    }
   graphics_2d.setTransform(t);
}


@Override
public void shear(double x,double y)
{
   if (debugLog()) {
      info().log(toShortString() + " shear " + x + " " + y);
    }
   graphics_2d.shear(x, y);
}


@Override
public void transform(AffineTransform t)
{
   if (debugLog()) {
      info().log(toShortString() + " transform " + t);
    }
   graphics_2d.transform(t);
}


@Override
public void translate(double x,double y)
{
   if (debugLog()) {
      info().log(toShortString() + " translate " + x + " " + y);
    }
   graphics_2d.translate(x, y);
}


/********************************************************************************/
/*										*/
/*	Debug information class 						*/
/*										*/
/********************************************************************************/

private static final class DebugInfo {
   
   private Color flash_color = Color.red;
   private int   flash_time  = 100;
   private int	 flash_count = 2;
   private Hashtable<JComponent, Integer> component_to_debug;
   private JFrame debug_frame = null;
   private java.io.PrintStream	debug_stream = System.out;
   
   void setDebugOptions(JComponent component,int debug) {
      if (debug == 0) {
         return;
       }
      if (component_to_debug == null) {
         component_to_debug = new Hashtable<JComponent, Integer>();
       }
      if (debug > 0) {
         component_to_debug.put(component, Integer.valueOf(debug));
       }
      else {
         component_to_debug.remove(component);
       }
    }
   
   int getDebugOptions(JComponent component) {
      if (component_to_debug == null) {
         return 0;
       }
      else {
         Integer integer = component_to_debug.get(component);
         return integer == null ? 0 : integer.intValue();
       }
    }
   
   void log(String string) {
      debug_stream.println(string);
    }
   
} // end of inner class DebugInfo


/********************************************************************************/
/*										*/
/*	Graphics filter debug class						*/
/*										*/
/********************************************************************************/

private static final class DebugGraphicsFilter extends RGBImageFilter {
   
   private Color color_filter;
   
   DebugGraphicsFilter(Color c) {
      canFilterIndexColorModel = true;
      color_filter = c;
    }
   
   @Override
   public int filterRGB(int x,int y,int rgb) {
      return color_filter.getRGB() | (rgb & 0xFF000000);
    }
   
}       // end of inner class DebugGrpahicsFilter


/********************************************************************************/
/*										*/
/*	Graphics observer debug class						*/
/*										*/
/********************************************************************************/

private static final class DebugGraphicsObserver implements ImageObserver {
   
   @Override public synchronized boolean imageUpdate(Image img,int infoflags,
         int x,int y,int width,int height) {
      return true;
    }
   
}       // end of innter class DebugGraphicsObserver


} // end of class SwingDebugGraphics


/* end of SwingDebugGraphics.java */
