/********************************************************************************/
/*										*/
/*		SwingFreezePane.java						*/
/*										*/
/*	Panel that will cache its contents to avoid redraw			*/
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


package edu.brown.cs.ivy.swing;



import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;



public class SwingFreezePane extends JPanel
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private int		paint_count;
private transient Image draw_image;
private boolean 	image_valid;
private boolean 	is_forced_frozen;
private boolean 	force_repaint;
private int		count_threshold;
private transient ChildManager child_manager;
private transient Map<Object,Object> render_hints;

private static final int	REDRAW_THRESHOLD = 1;
private static final int	MIN_AREA = 10000;

private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingFreezePane(Component c)
{
   super(new GridLayout(1,1));

   draw_image = null;
   paint_count = 0;
   image_valid = false;
   is_forced_frozen = false;
   force_repaint = false;
   count_threshold = REDRAW_THRESHOLD;
   setOpaque(false);
   child_manager = new ChildManager();
   render_hints = null;

   if (c != null) add(c);

   invalidate();
}



public void dispose()
{
   if (getComponentCount() > 0) {
      Component cold = getComponent(0);
      if (cold != null) {
	 cold.removeComponentListener(child_manager);
	 remove(cold);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public void invalidateFreeze()
{
   invalidateImage();
}


public void disableFreeze()
{
   paint_count = -1;
}



public void enableFreeze()
{
   paint_count = 0;
}


public void forceFreeze()
{
   if (!image_valid && !is_forced_frozen) {
      setupImage();
      if (image_valid) is_forced_frozen = true;
      force_repaint = false;
    }
}



public void unfreeze()
{
   if (is_forced_frozen && force_repaint) repaint();

   is_forced_frozen = false;
   force_repaint = false;

   paint_count = 0;
   invalidateImage();
}



public void setThreshold(int ct)
{
   count_threshold = ct;
}



/********************************************************************************/
/*										*/
/*	Handling content							*/
/*										*/
/********************************************************************************/

@Override protected void addImpl(Component c,Object cnst,int idx)
{
   if (getComponentCount() > 0) {
      Component cold = getComponent(0);
      if (cold == c) return;
      if (cold != null) {
	 cold.removeComponentListener(child_manager);
	 remove(cold);
       }
    }

   super.addImpl(c,cnst,idx);
   invalidate();

   c.addComponentListener(child_manager);
}




private final class ChildManager extends ComponentAdapter {

   @Override public void componentShown(ComponentEvent e) {
      setVisible(true);
    }

   @Override public void componentHidden(ComponentEvent e) {
      setVisible(false);
    }

}	// end of inner class ChildManager




/********************************************************************************/
/*										*/
/*	Detection of updates							*/
/*										*/
/********************************************************************************/

@Override public void repaint(long tm,int x,int y,int width,int height)
{
   invalidateImage();
   super.repaint(tm,x,y,width,height);
}



private void invalidateImage()
{
   if (is_forced_frozen) {
      image_valid = false;
      force_repaint = true;
      return;
    }

   // if (image_valid) System.err.println("UNFREEZE " + getComponent(0));

   paint_count = 0;
   image_valid = false;
}



/********************************************************************************/
/*										*/
/*	Drawing methods 							*/
/*										*/
/********************************************************************************/

@Override public void paint(Graphics g)
{
   if (checkImage(g)) return;

   super.paint(g);
}



@Override public void paintAll(Graphics g)
{
   if (checkImage(g)) return;

   super.paintAll(g);
}



private boolean checkImage(Graphics g)
{
   if (is_forced_frozen) {
      g.drawImage(draw_image, 0, 0, null);
      return true;
   }

   if (paint_count < 0) return false;

   if (childrenHaveFocus(this)) return false;

   if (getComponentCount() == 0) return false;

   RepaintManager rpm = RepaintManager.currentManager(getComponent(0));
   if (!(rpm instanceof FreezeRepainter)) {
      FreezeRepainter fp = new FreezeRepainter();
      RepaintManager.setCurrentManager(fp);
    }

   Rectangle r = rpm.getDirtyRegion((JComponent) getComponent(0));
   if (r.width != 0 || r.height != 0)
      paint_count = 0;

   if (image_valid) {
      g.drawImage(draw_image,0,0,null);
     return true;
    }

   if (!image_valid && ++paint_count > count_threshold) {
      if (g instanceof Graphics2D) {
	 Graphics2D g2d = (Graphics2D) g;
	 render_hints = new HashMap<Object,Object>(g2d.getRenderingHints());
       }
      SwingUtilities.invokeLater(new ImageSetter());
      // setupImage();
    }

   if (image_valid)
      g.drawImage(draw_image,0,0,null);

   return image_valid;
}




private boolean setupImage()
{
   if (image_valid) return true;

   if (getComponentCount() == 0) return false;

   Component c = getComponent(0);
   if (c == null) return false;
   int wd = c.getWidth();
   int ht = c.getHeight();

   if (wd * ht < MIN_AREA) return false;
   if (MouseInfo.getPointerInfo() == null) return false;
   Point loc = MouseInfo.getPointerInfo().getLocation();
   if (loc == null) return false;
   SwingUtilities.convertPointFromScreen(loc,this);

   if (loc.x >= 0 && loc.x < getWidth() && loc.y >= 0 && loc.y < getHeight()) {
      return false;
    }

   if (wd < 0 || ht < 0) return false;

   if (draw_image == null || draw_image.getWidth(this) != wd || draw_image.getHeight(this) != ht) {
      draw_image = new BufferedImage(wd,ht,BufferedImage.TYPE_4BYTE_ABGR);
    }

   Graphics2D g2 = (Graphics2D) draw_image.getGraphics();
   if (render_hints != null) g2.setRenderingHints(render_hints);
   else {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
   g2.setColor(c.getBackground());
   g2.fillRect(0,0,wd,ht);
   g2.setColor(c.getForeground());

   c.paintAll(g2);
   g2.dispose();

   // System.err.println("FREEZE " + c);
   image_valid = true;

   return true;
}




private boolean childrenHaveFocus(Component comp)
{
   if (comp == null) return false;
   if (comp.isFocusOwner()) return true;
   if (comp instanceof Container) {
      for (Component c : ((Container) comp).getComponents()) {
	 if (childrenHaveFocus(c)) return true;
       }
    }

   return false;
}



private final class ImageSetter implements Runnable {

   @Override public void run() {
      setupImage();
    }

}	// end of inner class ImageSetter



/********************************************************************************/
/*										*/
/*	Repaint manager 							*/
/*										*/
/********************************************************************************/

private static final class FreezeRepainter extends RepaintManager {

   @Override public void addDirtyRegion(JComponent c,int x,int y,int w,int h) {
      super.addDirtyRegion(c,x,y,w,h);
      for (Component p = c; p != null; p = p.getParent()) {
	 if (p instanceof SwingFreezePane) {
	    SwingFreezePane fp = (SwingFreezePane) p;
	    if (EventQueue.isDispatchThread()) fp.invalidateFreeze();
	    else SwingUtilities.invokeLater(new Unfreezer(fp));
	  }
       }
    }

}	// end of inner class RepaintManager


private static class Unfreezer implements Runnable {

   private SwingFreezePane freeze_pane;

   Unfreezer(SwingFreezePane fp) {
      freeze_pane = fp;
    }

   @Override public void run() {
      freeze_pane.invalidateFreeze();
    }

}

}	// end of class SwingFreezePane




/* end of SwingFreezePane.java */

