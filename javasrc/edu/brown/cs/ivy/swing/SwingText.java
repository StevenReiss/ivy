/********************************************************************************/
/*										*/
/*		SwingText.java							*/
/*										*/
/*	Text drawing support							*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingText.java,v 1.24 2020/05/03 01:19:39 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingText.java,v $
 * Revision 1.24  2020/05/03 01:19:39  spr
 * Code cleanup.
 *
 * Revision 1.23  2020/01/28 21:14:06  spr
 * Clean up.
 *
 * Revision 1.22  2018/09/20 23:57:40  spr
 * Java 10 changes
 *
 * Revision 1.21  2018/08/02 15:10:54  spr
 * Fix imports.  Prepare for java 10.
 *
 * Revision 1.20  2018/05/25 17:57:16  spr
 * Clean up a little.
 *
 * Revision 1.19  2017/06/20 00:53:17  spr
 * Add DPI getting routine.
 *
 * Revision 1.18  2016/11/17 15:56:22  spr
 * Clean up keyboard mappings.
 *
 * Revision 1.17  2016/10/15 00:29:03  spr
 * Handle parent on mac; fix selection model for tree + table.
 *
 * Revision 1.16  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.15  2015/07/02 19:01:33  spr
 * Minor bug fixes
 *
 * Revision 1.14  2013/11/15 02:38:19  spr
 * Update imports; add features to combo box.
 *
 * Revision 1.13  2011-09-12 20:50:31  spr
 * Code cleanup.
 *
 * Revision 1.12  2011-05-27 19:32:51  spr
 * Change copyrights.
 *
 * Revision 1.11  2011-01-11 00:26:57  spr
 * Handle empty freeze pane; code cleanup.
 *
 * Revision 1.10  2010-09-17 16:25:06  spr
 * Fix text character input.
 *
 * Revision 1.9  2010-09-17 15:46:19  spr
 * Add Keymap converter for mac; clean up and finish tree table.
 *
 * Revision 1.8  2010-07-24 02:01:02  spr
 * Add permanent option for freeze panes; code clean up; add mac support for text components.
 *
 * Revision 1.7  2009-10-02 00:18:30  spr
 * Import clean up.
 *
 * Revision 1.6  2009-04-11 01:43:02  spr
 * Prevent text from getting too big.
 *
 * Revision 1.5  2009-03-20 01:59:50  spr
 * Add enum-based choice box; add remove/update calls to lists; loosen numeric field checking.
 *
 * Revision 1.4  2009-01-27 00:40:33  spr
 * IvyXmlWriter cleanup.
 *
 * Revision 1.3  2008-06-11 01:46:38  spr
 * Clean imports.
 *
 * Revision 1.2  2008-03-14 12:27:44  spr
 * Code cleanup.
 *
 * Revision 1.1  2007-11-06 00:22:17  spr
 * Add methods to draw text in a 2d box with proper scaling.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.swing;



import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.Position;
import javax.swing.text.TextAction;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;




public class SwingText {



/********************************************************************************/
/*                                                                              */
/*      Private stoarge                                                         */
/*                                                                              */
/********************************************************************************/

private static Map<Object,Boolean>      done_keys = new WeakHashMap<>();



/********************************************************************************/
/*										*/
/*	Method to draw a string in a box					*/
/*										*/
/********************************************************************************/

public static void drawText(String lbl,Graphics2D g,Rectangle2D box)
{
   if (lbl == null) return;

   Font f = g.getFont();

   FontRenderContext ctx = g.getFontRenderContext();
   LineMetrics lm = f.getLineMetrics(lbl,ctx);
   Rectangle2D rc = f.getStringBounds(lbl,ctx);

   double s0 = box.getWidth() / rc.getWidth();
   double s1 = box.getHeight() / rc.getHeight();
   if (s0 > s1) s0 = s1;
   if (s0 > 1) s0 = 1;
   if (s0 < 0.01) return;
   float fz = f.getSize2D() * ((float) s0);
   Font f1 = f.deriveFont(fz);

   double xp = box.getX() + (box.getWidth() - rc.getWidth() * s0) / 2;
   double yp = box.getY() + (box.getHeight() - rc.getHeight() * s0) / 2 + lm.getAscent() * s0;

   g.setFont(f1);
   g.drawString(lbl,(float) xp,(float) yp);
   g.setFont(f);
}



public static void drawTextRight(String lbl,Graphics2D g,Rectangle2D box)
{
   if (lbl == null) return;

   Font f = g.getFont();

   FontRenderContext ctx = g.getFontRenderContext();
   LineMetrics lm = f.getLineMetrics(lbl,ctx);
   Rectangle2D rc = f.getStringBounds(lbl,ctx);

   double s0 = box.getWidth() / rc.getWidth();
   double s1 = box.getHeight() / rc.getHeight();
   if (s0 > s1) s0 = s1;
   if (s0 > 1) s0 = 1;
   if (s0 < 0.01) return;
   float fz = f.getSize2D() * ((float) s0);
   Font f1 = f.deriveFont(fz);

   double xp = box.getX() + (box.getWidth() - rc.getWidth() * s0);
   double yp = box.getY() + (box.getHeight() - rc.getHeight() * s0) / 2 + lm.getAscent() * s0;

   g.setFont(f1);
   g.drawString(lbl,(float) xp,(float) yp);
   g.setFont(f);
}



public static void drawText(String lbl,Graphics2D g,double x,double y,double w,double h)
{
   drawText(lbl,g,new Rectangle2D.Double(x,y,w,h));
}




public static void drawVerticalText(String lbl,Graphics2D g,Rectangle2D box)
{
   Font f0 = g.getFont();

   AffineTransform nat = AffineTransform.getRotateInstance(-Math.PI/2.0);
   Font f1 = f0.deriveFont(nat);

   FontRenderContext ctx = g.getFontRenderContext();
   LineMetrics lm = f0.getLineMetrics(lbl,ctx);
   Rectangle2D rc = f0.getStringBounds(lbl,ctx);

   double s0 = box.getWidth() / rc.getHeight();
   double s1 = box.getHeight() / rc.getWidth();
   if (s0 > s1) s0 = s1;
   if (s0 > 1) s0 = 1;
   if (s0 < 0.01) return;
   float fz = f1.getSize2D() * ((float) s0);
   Font f2 = f1.deriveFont(fz);

   g.setFont(f2);

   double yp = box.getY() + box.getHeight() - (box.getHeight() - rc.getWidth() * s0) / 2;
   double xp = box.getX() + (box.getWidth() - rc.getHeight() * s0) / 2 + lm.getAscent() * s0;

   // System.err.println("TEXT " + box + " (" + xp + " , " + yp + ") @ " + fz);

   Rectangle2D r2 = g.getClipBounds();
   g.setClip(box);

   g.drawString(lbl,(float) xp,(float) yp);

   g.setClip(r2);

   g.setFont(f0);
}



/********************************************************************************/
/*                                                                              */
/*      Font methods                                                            */
/*                                                                              */
/********************************************************************************/

public static Font deriveLarger(Font f)
{
   float sz = f.getSize2D();
   return f.deriveFont(sz+2f);
}




/********************************************************************************/
/*										*/
/*	Methods to fix JTextComponent for the mac				*/
/*										*/
/********************************************************************************/

public static void fixKeyBindings(JTextComponent tc)
{
   fixKeyBindings(tc,true);
}



public static void fixKeyBindings(JTextComponent tc,boolean doedit)
{
   int mask = getMenuShortcutKeyMaskEx();
   if (mask != InputEvent.META_DOWN_MASK) return;

   Keymap k = tc.getKeymap(); 
   fixKeyBindings(k);
   tc.setKeymap(k);
   
   if (doedit) defineEditBindings(tc);
}



public static void fixKeyBindings(JComponent c)
{
   int mask = getMenuShortcutKeyMaskEx();
   if (mask != InputEvent.META_DOWN_MASK) return;
   
   InputMap m = c.getInputMap();
   fixKeyBindings(m);
}



public static void fixKeyBindings(Keymap k)
{
   int mask = getMenuShortcutKeyMaskEx();
   if (mask != InputEvent.META_DOWN_MASK) return;
   if (done_keys.put(k,Boolean.TRUE) != null) return;

   for (KeyStroke ks : k.getBoundKeyStrokes()) {
      if (ks.getModifiers() == InputEvent.CTRL_DOWN_MASK) {
	 KeyStroke nks = KeyStroke.getKeyStroke(ks.getKeyCode(),mask);
	 Action act = k.getAction(ks);
	 k.removeKeyStrokeBinding(ks);
	 k.addActionForKeyStroke(nks,act);
       }
    }

   if (k.getDefaultAction() != null)
      k.setDefaultAction(new MacKeyTypedAction());

   Keymap par = k.getResolveParent();
   if (par != null && par != k) fixKeyBindings(par);
}


public static void fixKeyBindings(InputMap m)
{
   if (m == null) return;
   int mask = getMenuShortcutKeyMaskEx();
   if (mask != InputEvent.META_DOWN_MASK) return;
   if (done_keys.put(m,Boolean.TRUE) != null) return;
   
   if (m.keys() != null) {
      for (KeyStroke ks : m.keys()) {
         if (ks.getModifiers() == InputEvent.CTRL_DOWN_MASK) {
            KeyStroke nks = KeyStroke.getKeyStroke(ks.getKeyCode(),mask);
            Object act = m.get(ks);
            if (act instanceof Action) {
               m.remove(ks);
               m.put(nks,act);
             }
          }
       }
    }
   
   InputMap pm = m.getParent();
   if (pm != null && pm != m) fixKeyBindings(pm);
}




public static void defineEditBindings(JTextComponent tc)
{
   int mask = getMenuShortcutKeyMaskEx();
   KeyStroke cutkey = KeyStroke.getKeyStroke(KeyEvent.VK_X,mask);
   KeyStroke pastekey = KeyStroke.getKeyStroke(KeyEvent.VK_V,mask);
   KeyStroke copykey = KeyStroke.getKeyStroke(KeyEvent.VK_C,mask);
   Keymap km = tc.getKeymap();
   if (km == null) return;
   if (km.getAction(cutkey) == null) {
      km.addActionForKeyStroke(cutkey,new DefaultEditorKit.CutAction());
    }
   if (km.getAction(pastekey) == null) {
      km.addActionForKeyStroke(pastekey,new DefaultEditorKit.PasteAction());
    }
   if (km.getAction(copykey) == null) {
      km.addActionForKeyStroke(copykey,new DefaultEditorKit.CopyAction());
    }
   tc.setKeymap(km);
}




private static class MacKeyTypedAction extends TextAction {

   private static final long serialVersionUID = 1;

   MacKeyTypedAction() {
      super(DefaultEditorKit.defaultKeyTypedAction);
    }

   @Override public void actionPerformed(ActionEvent e) {
      JTextComponent target = getTextComponent(e);

      if (target != null && e != null) {
	 if (!target.isEditable() || !target.isEnabled()) {
	    target.getToolkit().beep();
	    return;
	  }
       }

      String content = e.getActionCommand();
      int mod = e.getModifiers();

      if ((content != null) && (content.length() > 0) &&
	     (((mod & ActionEvent.META_MASK) == 0) &&
		 ! (((mod & ActionEvent.CTRL_MASK) != 0) &&
		       ((mod & ActionEvent.ALT_MASK) == 0) ))) {
	 char c = content.charAt(0);
	 if (c >= 0x20 && (c != 0x7F)) { // Filter out CTRL chars and delete
	    if (target != null)
	       target.replaceSelection(content);
	  }
       }
    }

}	// end of inner class MacKeyTypedAction




/********************************************************************************/
/*										*/
/*	Handle J8-J10 differences						*/
/*										*/
/********************************************************************************/

public static int getMenuShortcutKeyMaskEx()
{
   if (System.getenv("USE_MAC_KEYS") != null) {
      String s = System.getenv("USE_MAC_KEYS").toLowerCase();
      if (!s.startsWith("n") && !s.startsWith("f") && !s.startsWith("0")) {
         return InputEvent.META_DOWN_MASK;
       }
      else {
         return InputEvent.CTRL_DOWN_MASK;
       }
    }  
   
   Toolkit tk = Toolkit.getDefaultToolkit();

   try {
      return tk.getMenuShortcutKeyMaskEx();
    }
   catch (Throwable e) { }

   try	{
      Class<?> ctk = tk.getClass();
      Method m2 = ctk.getMethod("getMenuShortcutKeyMask");
      int v = (Integer) m2.invoke(tk);
      int r = convertEventMask(v);
      return r;
    }
   catch (NoSuchMethodException e) { }
   catch (InvocationTargetException e) { }
   catch (IllegalAccessException e) { }

   return InputEvent.CTRL_DOWN_MASK;
}



@SuppressWarnings("deprecation")
public static int viewToModel2D(JTextComponent c,Point pt)
{
   // return c.viewToModel2D(pt);
   return c.viewToModel(pt);
}


@SuppressWarnings("deprecation")
public static Rectangle modelToView2D(Object c,int pos) throws BadLocationException
{
   if (c instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) c;
      // Rectangle r = tc.modelToView2D(pos);
      // if (r == null) return r;
      // return r.getBounds();
      return tc.modelToView(pos);
    }

   return null;
}


@SuppressWarnings("deprecation")
public static String getToolTipText2D(TextUI tu,JTextComponent tc,Point pt)
{
   // return tu.getToolTipText2D(tc,pt);
   return tu.getToolTipText(tc,pt);
}


@SuppressWarnings("deprecation")
public static Rectangle modelToView2D(TextUI tu,JTextComponent tc,int pos,Position.Bias bias)
	throws BadLocationException
{
   // Rectangle2D r2 = tu.modelToView2D(tc,pos,bias);
   // return r2.getBounds();
   return tu.modelToView(tc,pos,bias);
}


@SuppressWarnings("deprecation")
public static int viewToModel2D(TextUI tu,JTextComponent tc,Point2D pt2,Position.Bias [] bias)
{
   // return tu.viewToModel2D(tc,pt2,bias);
   Point pt = null;
   if (pt2 instanceof Point) pt = (Point) pt2;
   else pt = new Point((int) pt2.getX(),(int) pt2.getY());
   return tu.viewToModel(tc,pt,bias);
}



@SuppressWarnings("deprecation")
private static int convertEventMask(int v)
{
   int r = 0;
   if ((v & InputEvent.CTRL_MASK) != 0) r |= InputEvent.CTRL_DOWN_MASK;
   if ((v & InputEvent.META_MASK) != 0) r |= InputEvent.META_DOWN_MASK;
   if ((v & InputEvent.SHIFT_MASK) != 0) r |= InputEvent.SHIFT_DOWN_MASK;
   if ((v & InputEvent.ALT_MASK) != 0) r |= InputEvent.ALT_DOWN_MASK;
   if ((v & InputEvent.BUTTON1_MASK) != 0) r |= InputEvent.BUTTON1_DOWN_MASK;
   if ((v & InputEvent.BUTTON2_MASK) != 0) r |= InputEvent.BUTTON2_DOWN_MASK;
   if ((v & InputEvent.BUTTON2_MASK) != 0) r |= InputEvent.BUTTON3_DOWN_MASK;

   return r;
}




}	// end of class SwingText




/* end of SwingText.java */
