/********************************************************************************/
/*										*/
/*		SwingSetup.java 						*/
/*										*/
/*	Swing common user interface methods					*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingSetup.java,v 1.11 2015/11/20 15:09:26 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingSetup.java,v $
 * Revision 1.11  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.10  2011-05-27 19:32:51  spr
 * Change copyrights.
 *
 * Revision 1.9  2009-04-11 01:43:02  spr
 * Prevent text from getting too big.
 *
 * Revision 1.8  2008-03-14 12:27:44  spr
 * Code cleanup.
 *
 * Revision 1.7  2007-12-13 20:22:13  spr
 * Add call to set the fill when adding a component.
 *
 * Revision 1.6  2007-05-04 02:00:37  spr
 * Import fixups.
 *
 * Revision 1.5  2006/07/10 14:52:25  spr
 * Code cleanup.
 *
 * Revision 1.4  2005/12/22 20:40:17  spr
 * Add action button to grid; try to make file choosers uneditable; fix source viewer.
 *
 * Revision 1.3  2005/05/07 22:25:44  spr
 * Updates for java 5.0
 *
 * Revision 1.2  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1.1.1  2003/03/17 19:38:12  spr
 * Initial version of the common code for various Brown projects.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.swing;



import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.awt.Color;




public class SwingSetup implements SwingColors
{

/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static boolean done_setup = false;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingSetup()
{
   if (!done_setup) {
      done_setup = true;
      setTheme();
      setLocalDefaults();
      ToolTipManager ttm = ToolTipManager.sharedInstance();
      ttm.setDismissDelay(3600*1000);
    }
}



/****************************************************************************************/
/*											*/
/*	Metal Theme-based changes							*/
/*											*/
/****************************************************************************************/


private static class BrownTheme extends DefaultMetalTheme {

   @Override public String getName()		{ return "Brown"; }

   private final ColorUIResource primary1 = new ColorUIResource(SWING_DARK_COLOR);
   private final ColorUIResource primary2 = new ColorUIResource(SWING_SELECT_COLOR);
   private final ColorUIResource primary3 = new ColorUIResource(SWING_BACKGROUND_COLOR);

   private final ColorUIResource secondary1 = new ColorUIResource(SWING_DARK_COLOR);
   private final ColorUIResource secondary3 = new ColorUIResource(SWING_BACKGROUND_COLOR);
   private final ColorUIResource secondary2 = new ColorUIResource(SWING_DISABLE_COLOR);

   @Override protected ColorUIResource getPrimary1()	{ return primary1; }
   @Override protected ColorUIResource getPrimary2()	{ return primary2; }
   @Override protected ColorUIResource getPrimary3()	{ return primary3; }

   @Override protected ColorUIResource getSecondary1()	{ return secondary1; }
   @Override protected ColorUIResource getSecondary2()	{ return secondary2; }
   @Override protected ColorUIResource getSecondary3()	{ return secondary3; }

   @Override public ColorUIResource getTextHighlightColor()	{ return primary2; }
}


private void setTheme()
{
   MetalLookAndFeel.setCurrentTheme(new BrownTheme());
}


/****************************************************************************************/
/*											*/
/*	Defaults -- variations on the theme						*/
/*											*/
/****************************************************************************************/

private void setLocalDefaults()
{
   UIDefaults dflts = UIManager.getDefaults();
   dflts.put("Label.foreground",Color.black);

   dflts.put("TextField.background",SWING_LIGHT_COLOR);
   dflts.put("TextArea.background",SWING_LIGHT_COLOR);
   dflts.put("List.background",SWING_LIGHT_COLOR);
   dflts.put("EditorPane.background",SWING_LIGHT_COLOR);
   dflts.put("TextPane.background",SWING_LIGHT_COLOR);

   dflts.put("FileChooser.readOnly",Boolean.TRUE);
}



/************************************************************************/
/*									*/
/*	Implementation of basic local colors				*/
/*									*/
/************************************************************************/

static class BgColor extends Color {

   private static final long serialVersionUID = 1;

   BgColor(Color base,double v1,double v2) {
      super(SwingSetup.midColorI(base,v1,v2));
    }

   BgColor(Color base) {
      super(SwingSetup.midColorI(base,0.05,0.95));
    }

   BgColor(int r,int g,int b)		{ super(r,g,b); }

   @Override public Color brighter()		{ return SWING_DISABLE_COLOR; }
}





static class SwColor extends Color {

   private static final long serialVersionUID = 1;

   SwColor(Color base,double v1,double v2) {
      super(SwingSetup.midColorI(base,v1,v2));
    }

   SwColor(int r,int g,int b)		{ super(r,g,b); }
}



static class BaseColor extends Color {

   private static final long serialVersionUID = 1;

   BaseColor(int r,int g,int b) {
      super(SwingSetup.baseColorI(r,g,b));
    }

}



/********************************************************************************/
/*										*/
/*	Color manipulation							*/
/*										*/
/********************************************************************************/

private static int midColorI(Color c1,double v1,double v2)
{
   int r,g,b;

   if (v1 >= 0) {
      r = (int)(c1.getRed() * v1 + 255 * v2);
      g = (int)(c1.getGreen() * v1 + 255 * v2);
      b = (int)(c1.getBlue() * v1 + 255 * v2);
    }
   else {
      r = (int)((255-c1.getRed()) * v1 + 255 * v2);
      g = (int)((255-c1.getGreen()) * v1 + 255 * v2);
      b = (int)((255-c1.getBlue()) * v1 + 255 * v2);
    }

   return (b&0xff) | ((g&0xff)<<8) | ((r&0xff)<<16);
}



private static int baseColorI(int r,int g,int b)
{
   double v1 = Math.sqrt(127*127 + 255*255);

   int mx = r;
   if (g > mx) mx = g;
   if (b > mx) mx = b;

   for (int v2 = 0; v2 <= 255; ++v2) {
      double ra = r+v2;
      double ga = g+v2;
      double ba = b+v2;
      double va = Math.sqrt(ra*ra+ga*ga+ba*ba);
      if (va == 0) continue;
      double a = v1/va;
      if ((int)(mx*a) <= 255) {
	 r = (int)(ra*a);
	 g = (int)(ga*a);
	 b = (int)(ba*a);
	 return (b&0xff) | ((g&0xff)<<8) | ((r&0xff)<<16);
       }
    }

   return 0;
}



}	// end of class SwingSetup



/* end of SwingSetup.java */
