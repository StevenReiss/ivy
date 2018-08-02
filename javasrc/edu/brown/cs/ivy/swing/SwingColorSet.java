/********************************************************************************/
/*										*/
/*		SwingColorSet.java						*/
/*										*/
/*	Access to colors by name						*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingColorSet.java,v 1.6 2017/07/14 19:36:58 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingColorSet.java,v $
 * Revision 1.6  2017/07/14 19:36:58  spr
 * Update colors.	Make inner classes private.
 *
 * Revision 1.5  2017/06/23 20:55:07  spr
 * Output erroreous lines for debugging.
 *
 * Revision 1.4  2017/06/07 01:59:00  spr
 * Provide a general String to color routine.
 *
 * Revision 1.3  2016/03/22 13:10:16  spr
 * Handle Color[...] as string.
 *
 * Revision 1.2  2013/11/15 02:38:18  spr
 * Update imports; add features to combo box.
 *
 * Revision 1.1  2012-10-25 01:27:17  spr
 * Code clean up.	Add color lookup by name.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;


import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class SwingColorSet {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static Map<String,Color> color_map;




/********************************************************************************/
/*										*/
/*	Methods to get color by name						*/
/*										*/
/********************************************************************************/

public static Color getColorByName(String v)
{
   if (v == null || v.length() == 0) return null;

   Color c = color_map.get(v.toLowerCase());
   if (c != null) return c;
   if (v.contains("_")) {
      c = color_map.get(v.replace("_"," ").toLowerCase());
      if (c != null) return c;
    }

   if (v.startsWith("java.awt.Color[r=")) {
      int idx = v.indexOf("r=")+2;
      int idx1 = v.indexOf(",",idx);
      int r = Integer.parseInt(v.substring(idx,idx1));
      idx = v.indexOf("g=")+2;
      idx1 = v.indexOf(",",idx);
      int g = Integer.parseInt(v.substring(idx,idx1));
      idx = v.indexOf("b=")+2;
      idx1= v.indexOf("]",idx);
      int b = Integer.parseInt(v.substring(idx,idx1));
      c = new Color(r,g,b);
      color_map.put(v.toLowerCase(),c);
      return c;
    }

   if (v.contains(",")) {
      StringTokenizer tok = new StringTokenizer(v,", ");
      if (tok.countTokens() == 3) {
	 try {
	    int rc = Integer.parseInt(tok.nextToken());
	    int gc = Integer.parseInt(tok.nextToken());
	    int bc = Integer.parseInt(tok.nextToken());
	    c = new Color(rc,gc,bc);
	    color_map.put(v.toLowerCase(),c);
	    return c;
	  }
	 catch (NumberFormatException e) { }
       }
      else if (tok.countTokens() == 4) {
	 try {
	    int rc = Integer.parseInt(tok.nextToken());
	    int gc = Integer.parseInt(tok.nextToken());
	    int bc = Integer.parseInt(tok.nextToken());
	    int ac = Integer.parseInt(tok.nextToken());
	    c = new Color(rc,gc,bc,ac);
	    color_map.put(v.toLowerCase(),c);
	    return c;
	  }
	 catch (Exception e) {
	    System.err.println("SWING: Bad color: " + v + ": " + e);
	  }
       }
    }
   String nv = v;
   if (nv.startsWith("#")) nv = nv.substring(1);
   if (nv.startsWith("0x")) nv = nv.substring(2);

   try {
      long lv = Long.parseLong(nv,16);
      int cv = (int)(lv & 0xffffffff);
      if ((cv & 0xff000000) == 0 && nv.length() <= 6) c = new Color(cv);
      else c = new Color(cv,true);
      color_map.put(v.toLowerCase(),c);
      return c;
    }
   catch (NumberFormatException e) {
      System.err.println("IVY: Bad color value: " + v);
    }

   return null;
}



public static String getColorName(Color c)
{
   StringWriter sw = new StringWriter();
   PrintWriter pw = new PrintWriter(sw);
   pw.format("#%02x%02x%02x",c.getRed(),c.getGreen(),c.getBlue());
   return sw.toString();
}



/********************************************************************************/
/*										*/
/*	Static definitions for attributes and their properies			*/
/*										*/
/********************************************************************************/

static {
   color_map = new HashMap<String,Color>();

   color_map.put("snow".toLowerCase(),new Color(255,250,250));
   color_map.put("ghost white".toLowerCase(),new Color(248,248,255));
   color_map.put("GhostWhite".toLowerCase(),new Color(248,248,255));
   color_map.put("white smoke".toLowerCase(),new Color(245,245,245));
   color_map.put("WhiteSmoke".toLowerCase(),new Color(245,245,245));
   color_map.put("gainsboro".toLowerCase(),new Color(220,220,220));
   color_map.put("floral white".toLowerCase(),new Color(255,250,240));
   color_map.put("FloralWhite".toLowerCase(),new Color(255,250,240));
   color_map.put("old lace".toLowerCase(),new Color(253,245,230));
   color_map.put("OldLace".toLowerCase(),new Color(253,245,230));
   color_map.put("linen".toLowerCase(),new Color(250,240,230));
   color_map.put("antique white".toLowerCase(),new Color(250,235,215));
   color_map.put("AntiqueWhite".toLowerCase(),new Color(250,235,215));
   color_map.put("papaya whip".toLowerCase(),new Color(255,239,213));
   color_map.put("PapayaWhip".toLowerCase(),new Color(255,239,213));
   color_map.put("blanched almond".toLowerCase(),new Color(255,235,205));
   color_map.put("BlanchedAlmond".toLowerCase(),new Color(255,235,205));
   color_map.put("bisque".toLowerCase(),new Color(255,228,196));
   color_map.put("peach puff".toLowerCase(),new Color(255,218,185));
   color_map.put("PeachPuff".toLowerCase(),new Color(255,218,185));
   color_map.put("navajo white".toLowerCase(),new Color(255,222,173));
   color_map.put("NavajoWhite".toLowerCase(),new Color(255,222,173));
   color_map.put("moccasin".toLowerCase(),new Color(255,228,181));
   color_map.put("cornsilk".toLowerCase(),new Color(255,248,220));
   color_map.put("ivory".toLowerCase(),new Color(255,255,240));
   color_map.put("lemon chiffon".toLowerCase(),new Color(255,250,205));
   color_map.put("LemonChiffon".toLowerCase(),new Color(255,250,205));
   color_map.put("seashell".toLowerCase(),new Color(255,245,238));
   color_map.put("honeydew".toLowerCase(),new Color(240,255,240));
   color_map.put("mint cream".toLowerCase(),new Color(245,255,250));
   color_map.put("MintCream".toLowerCase(),new Color(245,255,250));
   color_map.put("azure".toLowerCase(),new Color(240,255,255));
   color_map.put("alice blue".toLowerCase(),new Color(240,248,255));
   color_map.put("AliceBlue".toLowerCase(),new Color(240,248,255));
   color_map.put("lavender".toLowerCase(),new Color(230,230,250));
   color_map.put("lavender blush".toLowerCase(),new Color(255,240,245));
   color_map.put("LavenderBlush".toLowerCase(),new Color(255,240,245));
   color_map.put("misty rose".toLowerCase(),new Color(255,228,225));
   color_map.put("MistyRose".toLowerCase(),new Color(255,228,225));
   color_map.put("white".toLowerCase(),new Color(255,255,255));
   color_map.put("black".toLowerCase(),new Color(0,0,0));
   color_map.put("dark slate gray".toLowerCase(),new Color(47, 79, 79));
   color_map.put("DarkSlateGray".toLowerCase(),new Color(47, 79, 79));
   color_map.put("dark slate grey".toLowerCase(),new Color(47, 79, 79));
   color_map.put("DarkSlateGrey".toLowerCase(),new Color(47, 79, 79));
   color_map.put("dim gray".toLowerCase(),new Color(105,105,105));
   color_map.put("DimGray".toLowerCase(),new Color(105,105,105));
   color_map.put("dim grey".toLowerCase(),new Color(105,105,105));
   color_map.put("DimGrey".toLowerCase(),new Color(105,105,105));
   color_map.put("slate gray".toLowerCase(),new Color(112,128,144));
   color_map.put("SlateGray".toLowerCase(),new Color(112,128,144));
   color_map.put("slate grey".toLowerCase(),new Color(112,128,144));
   color_map.put("SlateGrey".toLowerCase(),new Color(112,128,144));
   color_map.put("light slate gray".toLowerCase(),new Color(119,136,153));
   color_map.put("LightSlateGray".toLowerCase(),new Color(119,136,153));
   color_map.put("light slate grey".toLowerCase(),new Color(119,136,153));
   color_map.put("LightSlateGrey".toLowerCase(),new Color(119,136,153));
   color_map.put("gray".toLowerCase(),new Color(190,190,190));
   color_map.put("grey".toLowerCase(),new Color(190,190,190));
   color_map.put("light grey".toLowerCase(),new Color(211,211,211));
   color_map.put("LightGrey".toLowerCase(),new Color(211,211,211));
   color_map.put("light gray".toLowerCase(),new Color(211,211,211));
   color_map.put("LightGray".toLowerCase(),new Color(211,211,211));
   color_map.put("midnight blue".toLowerCase(),new Color(25, 25,112));
   color_map.put("MidnightBlue".toLowerCase(),new Color(25, 25,112));
   color_map.put("navy".toLowerCase(),new Color(0,0,12));
   color_map.put("navy blue".toLowerCase(),new Color(0,0,128));
   color_map.put("NavyBlue".toLowerCase(),new Color(0,0,128));
   color_map.put("cornflower blue".toLowerCase(),new Color(100,149,237));
   color_map.put("CornflowerBlue".toLowerCase(),new Color(100,149,237));
   color_map.put("dark slate blue".toLowerCase(),new Color(72, 61,139));
   color_map.put("DarkSlateBlue".toLowerCase(),new Color(72, 61,139));
   color_map.put("slate blue".toLowerCase(),new Color(106, 90,205));
   color_map.put("SlateBlue".toLowerCase(),new Color(106, 90,205));
   color_map.put("medium slate blue".toLowerCase(),new Color(123,104,238));
   color_map.put("MediumSlateBlue".toLowerCase(),new Color(123,104,238));
   color_map.put("light slate blue".toLowerCase(),new Color(132,112,255));
   color_map.put("LightSlateBlue".toLowerCase(),new Color(132,112,255));
   color_map.put("medium blue".toLowerCase(),new Color(0,0,205));
   color_map.put("MediumBlue".toLowerCase(),new Color(0,0,205));
   color_map.put("royal blue".toLowerCase(),new Color(65,105,225));
   color_map.put("RoyalBlue".toLowerCase(),new Color(65,105,225));
   color_map.put("blue".toLowerCase(),new Color(0,0,255));
   color_map.put("dodger blue".toLowerCase(),new Color(30,144,255));
   color_map.put("DodgerBlue".toLowerCase(),new Color(30,144,255));
   color_map.put("deep sky blue".toLowerCase(),new Color(0,191,255));
   color_map.put("DeepSkyBlue".toLowerCase(),new Color(0,191,255));
   color_map.put("sky blue".toLowerCase(),new Color(135,206,235));
   color_map.put("SkyBlue".toLowerCase(),new Color(135,206,235));
   color_map.put("light sky blue".toLowerCase(),new Color(135,206,250));
   color_map.put("LightSkyBlue".toLowerCase(),new Color(135,206,250));
   color_map.put("steel blue".toLowerCase(),new Color(70,130,180));
   color_map.put("SteelBlue".toLowerCase(),new Color(70,130,180));
   color_map.put("light steel blue".toLowerCase(),new Color(176,196,222));
   color_map.put("LightSteelBlue".toLowerCase(),new Color(176,196,222));
   color_map.put("light blue".toLowerCase(),new Color(173,216,230));
   color_map.put("LightBlue".toLowerCase(),new Color(173,216,230));
   color_map.put("powder blue".toLowerCase(),new Color(176,224,230));
   color_map.put("PowderBlue".toLowerCase(),new Color(176,224,230));
   color_map.put("pale turquoise".toLowerCase(),new Color(175,238,238));
   color_map.put("PaleTurquoise".toLowerCase(),new Color(175,238,238));
   color_map.put("dark turquoise".toLowerCase(),new Color(0,206,209));
   color_map.put("DarkTurquoise".toLowerCase(),new Color(0,206,209));
   color_map.put("medium turquoise".toLowerCase(),new Color(72,209,204));
   color_map.put("MediumTurquoise".toLowerCase(),new Color(72,209,204));
   color_map.put("turquoise".toLowerCase(),new Color(64,224,208));
   color_map.put("cyan".toLowerCase(),new Color(0,255,255));
   color_map.put("light cyan".toLowerCase(),new Color(224,255,255));
   color_map.put("LightCyan".toLowerCase(),new Color(224,255,255));
   color_map.put("cadet blue".toLowerCase(),new Color(95,158,160));
   color_map.put("CadetBlue".toLowerCase(),new Color(95,158,160));
   color_map.put("medium aquamarine".toLowerCase(),new Color(102,205,170));
   color_map.put("MediumAquamarine".toLowerCase(),new Color(102,205,170));
   color_map.put("aquamarine".toLowerCase(),new Color(127,255,212));
   color_map.put("dark green".toLowerCase(),new Color(0,100,0));
   color_map.put("DarkGreen".toLowerCase(),new Color(0,100,0));
   color_map.put("dark olive green".toLowerCase(),new Color(85,107, 47));
   color_map.put("DarkOliveGreen".toLowerCase(),new Color(85,107, 47));
   color_map.put("dark sea green".toLowerCase(),new Color(143,188,143));
   color_map.put("DarkSeaGreen".toLowerCase(),new Color(143,188,143));
   color_map.put("sea green".toLowerCase(),new Color(46,139, 87));
   color_map.put("SeaGreen".toLowerCase(),new Color(46,139, 87));
   color_map.put("medium sea green".toLowerCase(),new Color(60,179,113));
   color_map.put("MediumSeaGreen".toLowerCase(),new Color(60,179,113));
   color_map.put("light sea green".toLowerCase(),new Color(32,178,170));
   color_map.put("LightSeaGreen".toLowerCase(),new Color(32,178,170));
   color_map.put("pale green".toLowerCase(),new Color(152,251,152));
   color_map.put("PaleGreen".toLowerCase(),new Color(152,251,152));
   color_map.put("spring green".toLowerCase(),new Color(0,255,127));
   color_map.put("SpringGreen".toLowerCase(),new Color(0,255,127));
   color_map.put("lawn green".toLowerCase(),new Color(124,252,0));
   color_map.put("LawnGreen".toLowerCase(),new Color(124,252,0));
   color_map.put("green".toLowerCase(),new Color(0,255,0));
   color_map.put("chartreuse".toLowerCase(),new Color(127,255,0));
   color_map.put("medium spring green".toLowerCase(),new Color(0,250,154));
   color_map.put("MediumSpringGreen".toLowerCase(),new Color(0,250,154));
   color_map.put("green yellow".toLowerCase(),new Color(173,255, 47));
   color_map.put("GreenYellow".toLowerCase(),new Color(173,255, 47));
   color_map.put("lime green".toLowerCase(),new Color(50,205, 50));
   color_map.put("LimeGreen".toLowerCase(),new Color(50,205, 50));
   color_map.put("yellow green".toLowerCase(),new Color(154,205, 50));
   color_map.put("YellowGreen".toLowerCase(),new Color(154,205, 50));
   color_map.put("forest green".toLowerCase(),new Color(34,139, 34));
   color_map.put("ForestGreen".toLowerCase(),new Color(34,139, 34));
   color_map.put("olive drab".toLowerCase(),new Color(107,142, 35));
   color_map.put("OliveDrab".toLowerCase(),new Color(107,142, 35));
   color_map.put("dark khaki".toLowerCase(),new Color(189,183,107));
   color_map.put("DarkKhaki".toLowerCase(),new Color(189,183,107));
   color_map.put("khaki".toLowerCase(),new Color(240,230,140));
   color_map.put("pale goldenrod".toLowerCase(),new Color(238,232,170));
   color_map.put("PaleGoldenrod".toLowerCase(),new Color(238,232,170));
   color_map.put("light goldenrod yellow".toLowerCase(),new Color(250,250,210));
   color_map.put("LightGoldenrodYellow".toLowerCase(),new Color(250,250,210));
   color_map.put("light yellow".toLowerCase(),new Color(255,255,224));
   color_map.put("LightYellow".toLowerCase(),new Color(255,255,224));
   color_map.put("yellow".toLowerCase(),new Color(255,255,0));
   color_map.put("gold".toLowerCase(),new Color(255,215,0));
   color_map.put("light goldenrod".toLowerCase(),new Color(238,221,130));
   color_map.put("LightGoldenrod".toLowerCase(),new Color(238,221,130));
   color_map.put("goldenrod".toLowerCase(),new Color(218,165, 32));
   color_map.put("dark goldenrod".toLowerCase(),new Color(184,134, 11));
   color_map.put("DarkGoldenrod".toLowerCase(),new Color(184,134, 11));
   color_map.put("rosy brown".toLowerCase(),new Color(188,143,143));
   color_map.put("RosyBrown".toLowerCase(),new Color(188,143,143));
   color_map.put("indian red".toLowerCase(),new Color(205, 92, 92));
   color_map.put("IndianRed".toLowerCase(),new Color(205, 92, 92));
   color_map.put("saddle brown".toLowerCase(),new Color(139, 69, 19));
   color_map.put("SaddleBrown".toLowerCase(),new Color(139, 69, 19));
   color_map.put("sienna".toLowerCase(),new Color(160, 82, 45));
   color_map.put("peru".toLowerCase(),new Color(205,133, 63));
   color_map.put("burlywood".toLowerCase(),new Color(222,184,135));
   color_map.put("beige".toLowerCase(),new Color(245,245,220));
   color_map.put("wheat".toLowerCase(),new Color(245,222,179));
   color_map.put("sandy brown".toLowerCase(),new Color(244,164, 96));
   color_map.put("SandyBrown".toLowerCase(),new Color(244,164, 96));
   color_map.put("tan".toLowerCase(),new Color(210,180,140));
   color_map.put("chocolate".toLowerCase(),new Color(210,105, 30));
   color_map.put("firebrick".toLowerCase(),new Color(178, 34, 34));
   color_map.put("brown".toLowerCase(),new Color(165, 42, 42));
   color_map.put("dark salmon".toLowerCase(),new Color(233,150,122));
   color_map.put("DarkSalmon".toLowerCase(),new Color(233,150,122));
   color_map.put("salmon".toLowerCase(),new Color(250,128,114));
   color_map.put("light salmon".toLowerCase(),new Color(255,160,122));
   color_map.put("LightSalmon".toLowerCase(),new Color(255,160,122));
   color_map.put("orange".toLowerCase(),new Color(255,165,0));
   color_map.put("dark orange".toLowerCase(),new Color(255,140,0));
   color_map.put("DarkOrange".toLowerCase(),new Color(255,140,0));
   color_map.put("coral".toLowerCase(),new Color(255,127, 80));
   color_map.put("light coral".toLowerCase(),new Color(240,128,128));
   color_map.put("LightCoral".toLowerCase(),new Color(240,128,128));
   color_map.put("tomato".toLowerCase(),new Color(255, 99, 71));
   color_map.put("orange red".toLowerCase(),new Color(255, 69,0));
   color_map.put("OrangeRed".toLowerCase(),new Color(255, 69,0));
   color_map.put("red".toLowerCase(),new Color(255,0,0));
   color_map.put("hot pink".toLowerCase(),new Color(255,105,180));
   color_map.put("HotPink".toLowerCase(),new Color(255,105,180));
   color_map.put("deep pink".toLowerCase(),new Color(255, 20,147));
   color_map.put("DeepPink".toLowerCase(),new Color(255, 20,147));
   color_map.put("pink".toLowerCase(),new Color(255,192,203));
   color_map.put("light pink".toLowerCase(),new Color(255,182,193));
   color_map.put("LightPink".toLowerCase(),new Color(255,182,193));
   color_map.put("pale violet red".toLowerCase(),new Color(219,112,147));
   color_map.put("PaleVioletRed".toLowerCase(),new Color(219,112,147));
   color_map.put("maroon".toLowerCase(),new Color(176, 48, 96));
   color_map.put("medium violet red".toLowerCase(),new Color(199, 21,133));
   color_map.put("MediumVioletRed".toLowerCase(),new Color(199, 21,133));
   color_map.put("violet red".toLowerCase(),new Color(208, 32,144));
   color_map.put("VioletRed".toLowerCase(),new Color(208, 32,144));
   color_map.put("magenta".toLowerCase(),new Color(255,0,255));
   color_map.put("violet".toLowerCase(),new Color(238,130,238));
   color_map.put("plum".toLowerCase(),new Color(221,160,221));
   color_map.put("orchid".toLowerCase(),new Color(218,112,214));
   color_map.put("medium orchid".toLowerCase(),new Color(186, 85,211));
   color_map.put("MediumOrchid".toLowerCase(),new Color(186, 85,211));
   color_map.put("dark orchid".toLowerCase(),new Color(153, 50,204));
   color_map.put("DarkOrchid".toLowerCase(),new Color(153, 50,204));
   color_map.put("dark violet".toLowerCase(),new Color(148,0,211));
   color_map.put("DarkViolet".toLowerCase(),new Color(148,0,211));
   color_map.put("blue violet".toLowerCase(),new Color(138, 43,226));
   color_map.put("BlueViolet".toLowerCase(),new Color(138, 43,226));
   color_map.put("purple".toLowerCase(),new Color(160, 32,240));
   color_map.put("medium purple".toLowerCase(),new Color(147,112,219));
   color_map.put("MediumPurple".toLowerCase(),new Color(147,112,219));
   color_map.put("thistle".toLowerCase(),new Color(216,191,216));
   color_map.put("snow1".toLowerCase(),new Color(255,250,250));
   color_map.put("snow2".toLowerCase(),new Color(238,233,233));
   color_map.put("snow3".toLowerCase(),new Color(205,201,201));
   color_map.put("snow4".toLowerCase(),new Color(139,137,137));
   color_map.put("seashell1".toLowerCase(),new Color(255,245,238));
   color_map.put("seashell2".toLowerCase(),new Color(238,229,222));
   color_map.put("seashell3".toLowerCase(),new Color(205,197,191));
   color_map.put("seashell4".toLowerCase(),new Color(139,134,130));
   color_map.put("AntiqueWhite1".toLowerCase(),new Color(255,239,219));
   color_map.put("AntiqueWhite2".toLowerCase(),new Color(238,223,204));
   color_map.put("AntiqueWhite3".toLowerCase(),new Color(205,192,176));
   color_map.put("AntiqueWhite4".toLowerCase(),new Color(139,131,120));
   color_map.put("bisque1".toLowerCase(),new Color(255,228,196));
   color_map.put("bisque2".toLowerCase(),new Color(238,213,183));
   color_map.put("bisque3".toLowerCase(),new Color(205,183,158));
   color_map.put("bisque4".toLowerCase(),new Color(139,125,107));
   color_map.put("PeachPuff1".toLowerCase(),new Color(255,218,185));
   color_map.put("PeachPuff2".toLowerCase(),new Color(238,203,173));
   color_map.put("PeachPuff3".toLowerCase(),new Color(205,175,149));
   color_map.put("PeachPuff4".toLowerCase(),new Color(139,119,101));
   color_map.put("NavajoWhite1".toLowerCase(),new Color(255,222,173));
   color_map.put("NavajoWhite2".toLowerCase(),new Color(238,207,161));
   color_map.put("NavajoWhite3".toLowerCase(),new Color(205,179,139));
   color_map.put("NavajoWhite4".toLowerCase(),new Color(139,121, 94));
   color_map.put("LemonChiffon1".toLowerCase(),new Color(255,250,205));
   color_map.put("LemonChiffon2".toLowerCase(),new Color(238,233,191));
   color_map.put("LemonChiffon3".toLowerCase(),new Color(205,201,165));
   color_map.put("LemonChiffon4".toLowerCase(),new Color(139,137,112));
   color_map.put("cornsilk1".toLowerCase(),new Color(255,248,220));
   color_map.put("cornsilk2".toLowerCase(),new Color(238,232,205));
   color_map.put("cornsilk3".toLowerCase(),new Color(205,200,177));
   color_map.put("cornsilk4".toLowerCase(),new Color(139,136,120));
   color_map.put("ivory1".toLowerCase(),new Color(255,255,240));
   color_map.put("ivory2".toLowerCase(),new Color(238,238,224));
   color_map.put("ivory3".toLowerCase(),new Color(205,205,193));
   color_map.put("ivory4".toLowerCase(),new Color(139,139,131));
   color_map.put("honeydew1".toLowerCase(),new Color(240,255,240));
   color_map.put("honeydew2".toLowerCase(),new Color(224,238,224));
   color_map.put("honeydew3".toLowerCase(),new Color(193,205,193));
   color_map.put("honeydew4".toLowerCase(),new Color(131,139,131));
   color_map.put("LavenderBlush1".toLowerCase(),new Color(255,240,245));
   color_map.put("LavenderBlush2".toLowerCase(),new Color(238,224,229));
   color_map.put("LavenderBlush3".toLowerCase(),new Color(205,193,197));
   color_map.put("LavenderBlush4".toLowerCase(),new Color(139,131,134));
   color_map.put("MistyRose1".toLowerCase(),new Color(255,228,225));
   color_map.put("MistyRose2".toLowerCase(),new Color(238,213,210));
   color_map.put("MistyRose3".toLowerCase(),new Color(205,183,181));
   color_map.put("MistyRose4".toLowerCase(),new Color(139,125,123));
   color_map.put("azure1".toLowerCase(),new Color(240,255,255));
   color_map.put("azure2".toLowerCase(),new Color(224,238,238));
   color_map.put("azure3".toLowerCase(),new Color(193,205,205));
   color_map.put("azure4".toLowerCase(),new Color(131,139,139));
   color_map.put("SlateBlue1".toLowerCase(),new Color(131,111,255));
   color_map.put("SlateBlue2".toLowerCase(),new Color(122,103,238));
   color_map.put("SlateBlue3".toLowerCase(),new Color(105, 89,205));
   color_map.put("SlateBlue4".toLowerCase(),new Color(71, 60,139));
   color_map.put("RoyalBlue1".toLowerCase(),new Color(72,118,255));
   color_map.put("RoyalBlue2".toLowerCase(),new Color(67,110,238));
   color_map.put("RoyalBlue3".toLowerCase(),new Color(58, 95,205));
   color_map.put("RoyalBlue4".toLowerCase(),new Color(39, 64,139));
   color_map.put("blue1".toLowerCase(),new Color(0,0,255));
   color_map.put("blue2".toLowerCase(),new Color(0,0,238));
   color_map.put("blue3".toLowerCase(),new Color(0,0,205));
   color_map.put("blue4".toLowerCase(),new Color(0,0,139));
   color_map.put("DodgerBlue1".toLowerCase(),new Color(30,144,255));
   color_map.put("DodgerBlue2".toLowerCase(),new Color(28,134,238));
   color_map.put("DodgerBlue3".toLowerCase(),new Color(24,116,205));
   color_map.put("DodgerBlue4".toLowerCase(),new Color(16, 78,139));
   color_map.put("SteelBlue1".toLowerCase(),new Color(99,184,255));
   color_map.put("SteelBlue2".toLowerCase(),new Color(92,172,238));
   color_map.put("SteelBlue3".toLowerCase(),new Color(79,148,205));
   color_map.put("SteelBlue4".toLowerCase(),new Color(54,100,139));
   color_map.put("DeepSkyBlue1".toLowerCase(),new Color(0,191,255));
   color_map.put("DeepSkyBlue2".toLowerCase(),new Color(0,178,238));
   color_map.put("DeepSkyBlue3".toLowerCase(),new Color(0,154,205));
   color_map.put("DeepSkyBlue4".toLowerCase(),new Color(0,104,139));
   color_map.put("SkyBlue1".toLowerCase(),new Color(135,206,255));
   color_map.put("SkyBlue2".toLowerCase(),new Color(126,192,238));
   color_map.put("SkyBlue3".toLowerCase(),new Color(108,166,205));
   color_map.put("SkyBlue4".toLowerCase(),new Color(74,112,139));
   color_map.put("LightSkyBlue1".toLowerCase(),new Color(176,226,255));
   color_map.put("LightSkyBlue2".toLowerCase(),new Color(164,211,238));
   color_map.put("LightSkyBlue3".toLowerCase(),new Color(141,182,205));
   color_map.put("LightSkyBlue4".toLowerCase(),new Color(96,123,139));
   color_map.put("SlateGray1".toLowerCase(),new Color(198,226,255));
   color_map.put("SlateGray2".toLowerCase(),new Color(185,211,238));
   color_map.put("SlateGray3".toLowerCase(),new Color(159,182,205));
   color_map.put("SlateGray4".toLowerCase(),new Color(108,123,139));
   color_map.put("LightSteelBlue1".toLowerCase(),new Color(202,225,255));
   color_map.put("LightSteelBlue2".toLowerCase(),new Color(188,210,238));
   color_map.put("LightSteelBlue3".toLowerCase(),new Color(162,181,205));
   color_map.put("LightSteelBlue4".toLowerCase(),new Color(110,123,139));
   color_map.put("LightBlue1".toLowerCase(),new Color(191,239,255));
   color_map.put("LightBlue2".toLowerCase(),new Color(178,223,238));
   color_map.put("LightBlue3".toLowerCase(),new Color(154,192,205));
   color_map.put("LightBlue4".toLowerCase(),new Color(104,131,139));
   color_map.put("LightCyan1".toLowerCase(),new Color(224,255,255));
   color_map.put("LightCyan2".toLowerCase(),new Color(209,238,238));
   color_map.put("LightCyan3".toLowerCase(),new Color(180,205,205));
   color_map.put("LightCyan4".toLowerCase(),new Color(122,139,139));
   color_map.put("PaleTurquoise1".toLowerCase(),new Color(187,255,255));
   color_map.put("PaleTurquoise2".toLowerCase(),new Color(174,238,238));
   color_map.put("PaleTurquoise3".toLowerCase(),new Color(150,205,205));
   color_map.put("PaleTurquoise4".toLowerCase(),new Color(102,139,139));
   color_map.put("CadetBlue1".toLowerCase(),new Color(152,245,255));
   color_map.put("CadetBlue2".toLowerCase(),new Color(142,229,238));
   color_map.put("CadetBlue3".toLowerCase(),new Color(122,197,205));
   color_map.put("CadetBlue4".toLowerCase(),new Color(83,134,139));
   color_map.put("turquoise1".toLowerCase(),new Color(0,245,255));
   color_map.put("turquoise2".toLowerCase(),new Color(0,229,238));
   color_map.put("turquoise3".toLowerCase(),new Color(0,197,205));
   color_map.put("turquoise4".toLowerCase(),new Color(0,134,139));
   color_map.put("cyan1".toLowerCase(),new Color(0,255,255));
   color_map.put("cyan2".toLowerCase(),new Color(0,238,238));
   color_map.put("cyan3".toLowerCase(),new Color(0,205,205));
   color_map.put("cyan4".toLowerCase(),new Color(0,139,139));
   color_map.put("DarkSlateGray1".toLowerCase(),new Color(151,255,255));
   color_map.put("DarkSlateGray2".toLowerCase(),new Color(141,238,238));
   color_map.put("DarkSlateGray3".toLowerCase(),new Color(121,205,205));
   color_map.put("DarkSlateGray4".toLowerCase(),new Color(82,139,139));
   color_map.put("aquamarine1".toLowerCase(),new Color(127,255,212));
   color_map.put("aquamarine2".toLowerCase(),new Color(118,238,198));
   color_map.put("aquamarine3".toLowerCase(),new Color(102,205,170));
   color_map.put("aquamarine4".toLowerCase(),new Color(69,139,116));
   color_map.put("DarkSeaGreen1".toLowerCase(),new Color(193,255,193));
   color_map.put("DarkSeaGreen2".toLowerCase(),new Color(180,238,180));
   color_map.put("DarkSeaGreen3".toLowerCase(),new Color(155,205,155));
   color_map.put("DarkSeaGreen4".toLowerCase(),new Color(105,139,105));
   color_map.put("SeaGreen1".toLowerCase(),new Color(84,255,159));
   color_map.put("SeaGreen2".toLowerCase(),new Color(78,238,148));
   color_map.put("SeaGreen3".toLowerCase(),new Color(67,205,128));
   color_map.put("SeaGreen4".toLowerCase(),new Color(46,139, 87));
   color_map.put("PaleGreen1".toLowerCase(),new Color(154,255,154));
   color_map.put("PaleGreen2".toLowerCase(),new Color(144,238,144));
   color_map.put("PaleGreen3".toLowerCase(),new Color(124,205,124));
   color_map.put("PaleGreen4".toLowerCase(),new Color(84,139, 84));
   color_map.put("SpringGreen1".toLowerCase(),new Color(0,255,127));
   color_map.put("SpringGreen2".toLowerCase(),new Color(0,238,118));
   color_map.put("SpringGreen3".toLowerCase(),new Color(0,205,102));
   color_map.put("SpringGreen4".toLowerCase(),new Color(0,139, 69));
   color_map.put("green1".toLowerCase(),new Color(0,255,0));
   color_map.put("green2".toLowerCase(),new Color(0,238,0));
   color_map.put("green3".toLowerCase(),new Color(0,205,0));
   color_map.put("green4".toLowerCase(),new Color(0,139,0));
   color_map.put("chartreuse1".toLowerCase(),new Color(127,255,0));
   color_map.put("chartreuse2".toLowerCase(),new Color(118,238,0));
   color_map.put("chartreuse3".toLowerCase(),new Color(102,205,0));
   color_map.put("chartreuse4".toLowerCase(),new Color(69,139,0));
   color_map.put("OliveDrab1".toLowerCase(),new Color(192,255, 62));
   color_map.put("OliveDrab2".toLowerCase(),new Color(179,238, 58));
   color_map.put("OliveDrab3".toLowerCase(),new Color(154,205, 50));
   color_map.put("OliveDrab4".toLowerCase(),new Color(105,139, 34));
   color_map.put("DarkOliveGreen1".toLowerCase(),new Color(202,255,112));
   color_map.put("DarkOliveGreen2".toLowerCase(),new Color(188,238,104));
   color_map.put("DarkOliveGreen3".toLowerCase(),new Color(162,205, 90));
   color_map.put("DarkOliveGreen4".toLowerCase(),new Color(110,139, 61));
   color_map.put("khaki1".toLowerCase(),new Color(255,246,143));
   color_map.put("khaki2".toLowerCase(),new Color(238,230,133));
   color_map.put("khaki3".toLowerCase(),new Color(205,198,115));
   color_map.put("khaki4".toLowerCase(),new Color(139,134, 78));
   color_map.put("LightGoldenrod1".toLowerCase(),new Color(255,236,139));
   color_map.put("LightGoldenrod2".toLowerCase(),new Color(238,220,130));
   color_map.put("LightGoldenrod3".toLowerCase(),new Color(205,190,112));
   color_map.put("LightGoldenrod4".toLowerCase(),new Color(139,129, 76));
   color_map.put("LightYellow1".toLowerCase(),new Color(255,255,224));
   color_map.put("LightYellow2".toLowerCase(),new Color(238,238,209));
   color_map.put("LightYellow3".toLowerCase(),new Color(205,205,180));
   color_map.put("LightYellow4".toLowerCase(),new Color(139,139,122));
   color_map.put("yellow1".toLowerCase(),new Color(255,255,0));
   color_map.put("yellow2".toLowerCase(),new Color(238,238,0));
   color_map.put("yellow3".toLowerCase(),new Color(205,205,0));
   color_map.put("yellow4".toLowerCase(),new Color(139,139,0));
   color_map.put("gold1".toLowerCase(),new Color(255,215,0));
   color_map.put("gold2".toLowerCase(),new Color(238,201,0));
   color_map.put("gold3".toLowerCase(),new Color(205,173,0));
   color_map.put("gold4".toLowerCase(),new Color(139,117,0));
   color_map.put("goldenrod1".toLowerCase(),new Color(255,193, 37));
   color_map.put("goldenrod2".toLowerCase(),new Color(238,180, 34));
   color_map.put("goldenrod3".toLowerCase(),new Color(205,155, 29));
   color_map.put("goldenrod4".toLowerCase(),new Color(139,105, 20));
   color_map.put("DarkGoldenrod1".toLowerCase(),new Color(255,185, 15));
   color_map.put("DarkGoldenrod2".toLowerCase(),new Color(238,173, 14));
   color_map.put("DarkGoldenrod3".toLowerCase(),new Color(205,149, 12));
   color_map.put("DarkGoldenrod4".toLowerCase(),new Color(139,101,8));
   color_map.put("RosyBrown1".toLowerCase(),new Color(255,193,193));
   color_map.put("RosyBrown2".toLowerCase(),new Color(238,180,180));
   color_map.put("RosyBrown3".toLowerCase(),new Color(205,155,155));
   color_map.put("RosyBrown4".toLowerCase(),new Color(139,105,105));
   color_map.put("IndianRed1".toLowerCase(),new Color(255,106,106));
   color_map.put("IndianRed2".toLowerCase(),new Color(238, 99, 99));
   color_map.put("IndianRed3".toLowerCase(),new Color(205, 85, 85));
   color_map.put("IndianRed4".toLowerCase(),new Color(139, 58, 58));
   color_map.put("sienna1".toLowerCase(),new Color(255,130, 71));
   color_map.put("sienna2".toLowerCase(),new Color(238,121, 66));
   color_map.put("sienna3".toLowerCase(),new Color(205,104, 57));
   color_map.put("sienna4".toLowerCase(),new Color(139, 71, 38));
   color_map.put("burlywood1".toLowerCase(),new Color(255,211,155));
   color_map.put("burlywood2".toLowerCase(),new Color(238,197,145));
   color_map.put("burlywood3".toLowerCase(),new Color(205,170,125));
   color_map.put("burlywood4".toLowerCase(),new Color(139,115, 85));
   color_map.put("wheat1".toLowerCase(),new Color(255,231,186));
   color_map.put("wheat2".toLowerCase(),new Color(238,216,174));
   color_map.put("wheat3".toLowerCase(),new Color(205,186,150));
   color_map.put("wheat4".toLowerCase(),new Color(139,126,102));
   color_map.put("tan1".toLowerCase(),new Color(255,165, 79));
   color_map.put("tan2".toLowerCase(),new Color(238,154, 73));
   color_map.put("tan3".toLowerCase(),new Color(205,133, 63));
   color_map.put("tan4".toLowerCase(),new Color(139, 90, 43));
   color_map.put("chocolate1".toLowerCase(),new Color(255,127, 36));
   color_map.put("chocolate2".toLowerCase(),new Color(238,118, 33));
   color_map.put("chocolate3".toLowerCase(),new Color(205,102, 29));
   color_map.put("chocolate4".toLowerCase(),new Color(139, 69, 19));
   color_map.put("firebrick1".toLowerCase(),new Color(255, 48, 48));
   color_map.put("firebrick2".toLowerCase(),new Color(238, 44, 44));
   color_map.put("firebrick3".toLowerCase(),new Color(205, 38, 38));
   color_map.put("firebrick4".toLowerCase(),new Color(139, 26, 26));
   color_map.put("brown1".toLowerCase(),new Color(255, 64, 64));
   color_map.put("brown2".toLowerCase(),new Color(238, 59, 59));
   color_map.put("brown3".toLowerCase(),new Color(205, 51, 51));
   color_map.put("brown4".toLowerCase(),new Color(139, 35, 35));
   color_map.put("salmon1".toLowerCase(),new Color(255,140,105));
   color_map.put("salmon2".toLowerCase(),new Color(238,130, 98));
   color_map.put("salmon3".toLowerCase(),new Color(205,112, 84));
   color_map.put("salmon4".toLowerCase(),new Color(139, 76, 57));
   color_map.put("LightSalmon1".toLowerCase(),new Color(255,160,122));
   color_map.put("LightSalmon2".toLowerCase(),new Color(238,149,114));
   color_map.put("LightSalmon3".toLowerCase(),new Color(205,129, 98));
   color_map.put("LightSalmon4".toLowerCase(),new Color(139, 87, 66));
   color_map.put("orange1".toLowerCase(),new Color(255,165,0));
   color_map.put("orange2".toLowerCase(),new Color(238,154,0));
   color_map.put("orange3".toLowerCase(),new Color(205,133,0));
   color_map.put("orange4".toLowerCase(),new Color(139, 90,0));
   color_map.put("DarkOrange1".toLowerCase(),new Color(255,127,0));
   color_map.put("DarkOrange2".toLowerCase(),new Color(238,118,0));
   color_map.put("DarkOrange3".toLowerCase(),new Color(205,102,0));
   color_map.put("DarkOrange4".toLowerCase(),new Color(139, 69,0));
   color_map.put("coral1".toLowerCase(),new Color(255,114, 86));
   color_map.put("coral2".toLowerCase(),new Color(238,106, 80));
   color_map.put("coral3".toLowerCase(),new Color(205, 91, 69));
   color_map.put("coral4".toLowerCase(),new Color(139, 62, 47));
   color_map.put("tomato1".toLowerCase(),new Color(255, 99, 71));
   color_map.put("tomato2".toLowerCase(),new Color(238, 92, 66));
   color_map.put("tomato3".toLowerCase(),new Color(205, 79, 57));
   color_map.put("tomato4".toLowerCase(),new Color(139, 54, 38));
   color_map.put("OrangeRed1".toLowerCase(),new Color(255, 69,0));
   color_map.put("OrangeRed2".toLowerCase(),new Color(238, 64,0));
   color_map.put("OrangeRed3".toLowerCase(),new Color(205, 55,0));
   color_map.put("OrangeRed4".toLowerCase(),new Color(139, 37,0));
   color_map.put("red1".toLowerCase(),new Color(255,0,0));
   color_map.put("red2".toLowerCase(),new Color(238,0,0));
   color_map.put("red3".toLowerCase(),new Color(205,0,0));
   color_map.put("red4".toLowerCase(),new Color(139,0,0));
   color_map.put("DebianRed".toLowerCase(),new Color(215,7,81));
   color_map.put("DeepPink1".toLowerCase(),new Color(255, 20,147));
   color_map.put("DeepPink2".toLowerCase(),new Color(238, 18,137));
   color_map.put("DeepPink3".toLowerCase(),new Color(205, 16,118));
   color_map.put("DeepPink4".toLowerCase(),new Color(139, 10, 80));
   color_map.put("HotPink1".toLowerCase(),new Color(255,110,180));
   color_map.put("HotPink2".toLowerCase(),new Color(238,106,167));
   color_map.put("HotPink3".toLowerCase(),new Color(205, 96,144));
   color_map.put("HotPink4".toLowerCase(),new Color(139, 58, 98));
   color_map.put("pink1".toLowerCase(),new Color(255,181,197));
   color_map.put("pink2".toLowerCase(),new Color(238,169,184));
   color_map.put("pink3".toLowerCase(),new Color(205,145,158));
   color_map.put("pink4".toLowerCase(),new Color(139, 99,108));
   color_map.put("LightPink1".toLowerCase(),new Color(255,174,185));
   color_map.put("LightPink2".toLowerCase(),new Color(238,162,173));
   color_map.put("LightPink3".toLowerCase(),new Color(205,140,149));
   color_map.put("LightPink4".toLowerCase(),new Color(139, 95,101));
   color_map.put("PaleVioletRed1".toLowerCase(),new Color(255,130,171));
   color_map.put("PaleVioletRed2".toLowerCase(),new Color(238,121,159));
   color_map.put("PaleVioletRed3".toLowerCase(),new Color(205,104,137));
   color_map.put("PaleVioletRed4".toLowerCase(),new Color(139, 71, 93));
   color_map.put("maroon1".toLowerCase(),new Color(255, 52,179));
   color_map.put("maroon2".toLowerCase(),new Color(238, 48,167));
   color_map.put("maroon3".toLowerCase(),new Color(205, 41,144));
   color_map.put("maroon4".toLowerCase(),new Color(139, 28, 98));
   color_map.put("VioletRed1".toLowerCase(),new Color(255, 62,150));
   color_map.put("VioletRed2".toLowerCase(),new Color(238, 58,140));
   color_map.put("VioletRed3".toLowerCase(),new Color(205, 50,120));
   color_map.put("VioletRed4".toLowerCase(),new Color(139, 34, 82));
   color_map.put("magenta1".toLowerCase(),new Color(255,0,255));
   color_map.put("magenta2".toLowerCase(),new Color(238,0,238));
   color_map.put("magenta3".toLowerCase(),new Color(205,0,205));
   color_map.put("magenta4".toLowerCase(),new Color(139,0,139));
   color_map.put("orchid1".toLowerCase(),new Color(255,131,250));
   color_map.put("orchid2".toLowerCase(),new Color(238,122,233));
   color_map.put("orchid3".toLowerCase(),new Color(205,105,201));
   color_map.put("orchid4".toLowerCase(),new Color(139, 71,137));
   color_map.put("plum1".toLowerCase(),new Color(255,187,255));
   color_map.put("plum2".toLowerCase(),new Color(238,174,238));
   color_map.put("plum3".toLowerCase(),new Color(205,150,205));
   color_map.put("plum4".toLowerCase(),new Color(139,102,139));
   color_map.put("MediumOrchid1".toLowerCase(),new Color(224,102,255));
   color_map.put("MediumOrchid2".toLowerCase(),new Color(209, 95,238));
   color_map.put("MediumOrchid3".toLowerCase(),new Color(180, 82,205));
   color_map.put("MediumOrchid4".toLowerCase(),new Color(122, 55,139));
   color_map.put("DarkOrchid1".toLowerCase(),new Color(191, 62,255));
   color_map.put("DarkOrchid2".toLowerCase(),new Color(178, 58,238));
   color_map.put("DarkOrchid3".toLowerCase(),new Color(154, 50,205));
   color_map.put("DarkOrchid4".toLowerCase(),new Color(104, 34,139));
   color_map.put("purple1".toLowerCase(),new Color(155, 48,255));
   color_map.put("purple2".toLowerCase(),new Color(145, 44,238));
   color_map.put("purple3".toLowerCase(),new Color(125, 38,205));
   color_map.put("purple4".toLowerCase(),new Color(85, 26,139));
   color_map.put("MediumPurple1".toLowerCase(),new Color(171,130,255));
   color_map.put("MediumPurple2".toLowerCase(),new Color(159,121,238));
   color_map.put("MediumPurple3".toLowerCase(),new Color(137,104,205));
   color_map.put("MediumPurple4".toLowerCase(),new Color(93, 71,139));
   color_map.put("thistle1".toLowerCase(),new Color(255,225,255));
   color_map.put("thistle2".toLowerCase(),new Color(238,210,238));
   color_map.put("thistle3".toLowerCase(),new Color(205,181,205));
   color_map.put("thistle4".toLowerCase(),new Color(139,123,139));
   color_map.put("gray0".toLowerCase(),new Color(0,0,0));
   color_map.put("grey0".toLowerCase(),new Color(0,0,0));
   color_map.put("gray1".toLowerCase(),new Color(3,3,3));
   color_map.put("grey1".toLowerCase(),new Color(3,3,3));
   color_map.put("gray2".toLowerCase(),new Color(5,5,5));
   color_map.put("grey2".toLowerCase(),new Color(5,5,5));
   color_map.put("gray3".toLowerCase(),new Color(8,8,8));
   color_map.put("grey3".toLowerCase(),new Color(8,8,8));
   color_map.put("gray4".toLowerCase(),new Color(10, 10, 10));
   color_map.put("grey4".toLowerCase(),new Color(10, 10, 10));
   color_map.put("gray5".toLowerCase(),new Color(13, 13, 13));
   color_map.put("grey5".toLowerCase(),new Color(13, 13, 13));
   color_map.put("gray6".toLowerCase(),new Color(15, 15, 15));
   color_map.put("grey6".toLowerCase(),new Color(15, 15, 15));
   color_map.put("gray7".toLowerCase(),new Color(18, 18, 18));
   color_map.put("grey7".toLowerCase(),new Color(18, 18, 18));
   color_map.put("gray8".toLowerCase(),new Color(20, 20, 20));
   color_map.put("grey8".toLowerCase(),new Color(20, 20, 20));
   color_map.put("gray9".toLowerCase(),new Color(23, 23, 23));
   color_map.put("grey9".toLowerCase(),new Color(23, 23, 23));
   color_map.put("gray10".toLowerCase(),new Color(26, 26, 26));
   color_map.put("grey10".toLowerCase(),new Color(26, 26, 26));
   color_map.put("gray11".toLowerCase(),new Color(28, 28, 28));
   color_map.put("grey11".toLowerCase(),new Color(28, 28, 28));
   color_map.put("gray12".toLowerCase(),new Color(31, 31, 31));
   color_map.put("grey12".toLowerCase(),new Color(31, 31, 31));
   color_map.put("gray13".toLowerCase(),new Color(33, 33, 33));
   color_map.put("grey13".toLowerCase(),new Color(33, 33, 33));
   color_map.put("gray14".toLowerCase(),new Color(36, 36, 36));
   color_map.put("grey14".toLowerCase(),new Color(36, 36, 36));
   color_map.put("gray15".toLowerCase(),new Color(38, 38, 38));
   color_map.put("grey15".toLowerCase(),new Color(38, 38, 38));
   color_map.put("gray16".toLowerCase(),new Color(41, 41, 41));
   color_map.put("grey16".toLowerCase(),new Color(41, 41, 41));
   color_map.put("gray17".toLowerCase(),new Color(43, 43, 43));
   color_map.put("grey17".toLowerCase(),new Color(43, 43, 43));
   color_map.put("gray18".toLowerCase(),new Color(46, 46, 46));
   color_map.put("grey18".toLowerCase(),new Color(46, 46, 46));
   color_map.put("gray19".toLowerCase(),new Color(48, 48, 48));
   color_map.put("grey19".toLowerCase(),new Color(48, 48, 48));
   color_map.put("gray20".toLowerCase(),new Color(51, 51, 51));
   color_map.put("grey20".toLowerCase(),new Color(51, 51, 51));
   color_map.put("gray21".toLowerCase(),new Color(54, 54, 54));
   color_map.put("grey21".toLowerCase(),new Color(54, 54, 54));
   color_map.put("gray22".toLowerCase(),new Color(56, 56, 56));
   color_map.put("grey22".toLowerCase(),new Color(56, 56, 56));
   color_map.put("gray23".toLowerCase(),new Color(59, 59, 59));
   color_map.put("grey23".toLowerCase(),new Color(59, 59, 59));
   color_map.put("gray24".toLowerCase(),new Color(61, 61, 61));
   color_map.put("grey24".toLowerCase(),new Color(61, 61, 61));
   color_map.put("gray25".toLowerCase(),new Color(64, 64, 64));
   color_map.put("grey25".toLowerCase(),new Color(64, 64, 64));
   color_map.put("gray26".toLowerCase(),new Color(66, 66, 66));
   color_map.put("grey26".toLowerCase(),new Color(66, 66, 66));
   color_map.put("gray27".toLowerCase(),new Color(69, 69, 69));
   color_map.put("grey27".toLowerCase(),new Color(69, 69, 69));
   color_map.put("gray28".toLowerCase(),new Color(71, 71, 71));
   color_map.put("grey28".toLowerCase(),new Color(71, 71, 71));
   color_map.put("gray29".toLowerCase(),new Color(74, 74, 74));
   color_map.put("grey29".toLowerCase(),new Color(74, 74, 74));
   color_map.put("gray30".toLowerCase(),new Color(77, 77, 77));
   color_map.put("grey30".toLowerCase(),new Color(77, 77, 77));
   color_map.put("gray31".toLowerCase(),new Color(79, 79, 79));
   color_map.put("grey31".toLowerCase(),new Color(79, 79, 79));
   color_map.put("gray32".toLowerCase(),new Color(82, 82, 82));
   color_map.put("grey32".toLowerCase(),new Color(82, 82, 82));
   color_map.put("gray33".toLowerCase(),new Color(84, 84, 84));
   color_map.put("grey33".toLowerCase(),new Color(84, 84, 84));
   color_map.put("gray34".toLowerCase(),new Color(87, 87, 87));
   color_map.put("grey34".toLowerCase(),new Color(87, 87, 87));
   color_map.put("gray35".toLowerCase(),new Color(89, 89, 89));
   color_map.put("grey35".toLowerCase(),new Color(89, 89, 89));
   color_map.put("gray36".toLowerCase(),new Color(92, 92, 92));
   color_map.put("grey36".toLowerCase(),new Color(92, 92, 92));
   color_map.put("gray37".toLowerCase(),new Color(94, 94, 94));
   color_map.put("grey37".toLowerCase(),new Color(94, 94, 94));
   color_map.put("gray38".toLowerCase(),new Color(97, 97, 97));
   color_map.put("grey38".toLowerCase(),new Color(97, 97, 97));
   color_map.put("gray39".toLowerCase(),new Color(99, 99, 99));
   color_map.put("grey39".toLowerCase(),new Color(99, 99, 99));
   color_map.put("gray40".toLowerCase(),new Color(102,102,102));
   color_map.put("grey40".toLowerCase(),new Color(102,102,102));
   color_map.put("gray41".toLowerCase(),new Color(105,105,105));
   color_map.put("grey41".toLowerCase(),new Color(105,105,105));
   color_map.put("gray42".toLowerCase(),new Color(107,107,107));
   color_map.put("grey42".toLowerCase(),new Color(107,107,107));
   color_map.put("gray43".toLowerCase(),new Color(110,110,110));
   color_map.put("grey43".toLowerCase(),new Color(110,110,110));
   color_map.put("gray44".toLowerCase(),new Color(112,112,112));
   color_map.put("grey44".toLowerCase(),new Color(112,112,112));
   color_map.put("gray45".toLowerCase(),new Color(115,115,115));
   color_map.put("grey45".toLowerCase(),new Color(115,115,115));
   color_map.put("gray46".toLowerCase(),new Color(117,117,117));
   color_map.put("grey46".toLowerCase(),new Color(117,117,117));
   color_map.put("gray47".toLowerCase(),new Color(120,120,120));
   color_map.put("grey47".toLowerCase(),new Color(120,120,120));
   color_map.put("gray48".toLowerCase(),new Color(122,122,122));
   color_map.put("grey48".toLowerCase(),new Color(122,122,122));
   color_map.put("gray49".toLowerCase(),new Color(125,125,125));
   color_map.put("grey49".toLowerCase(),new Color(125,125,125));
   color_map.put("gray50".toLowerCase(),new Color(127,127,127));
   color_map.put("grey50".toLowerCase(),new Color(127,127,127));
   color_map.put("gray51".toLowerCase(),new Color(130,130,130));
   color_map.put("grey51".toLowerCase(),new Color(130,130,130));
   color_map.put("gray52".toLowerCase(),new Color(133,133,133));
   color_map.put("grey52".toLowerCase(),new Color(133,133,133));
   color_map.put("gray53".toLowerCase(),new Color(135,135,135));
   color_map.put("grey53".toLowerCase(),new Color(135,135,135));
   color_map.put("gray54".toLowerCase(),new Color(138,138,138));
   color_map.put("grey54".toLowerCase(),new Color(138,138,138));
   color_map.put("gray55".toLowerCase(),new Color(140,140,140));
   color_map.put("grey55".toLowerCase(),new Color(140,140,140));
   color_map.put("gray56".toLowerCase(),new Color(143,143,143));
   color_map.put("grey56".toLowerCase(),new Color(143,143,143));
   color_map.put("gray57".toLowerCase(),new Color(145,145,145));
   color_map.put("grey57".toLowerCase(),new Color(145,145,145));
   color_map.put("gray58".toLowerCase(),new Color(148,148,148));
   color_map.put("grey58".toLowerCase(),new Color(148,148,148));
   color_map.put("gray59".toLowerCase(),new Color(150,150,150));
   color_map.put("grey59".toLowerCase(),new Color(150,150,150));
   color_map.put("gray60".toLowerCase(),new Color(153,153,153));
   color_map.put("grey60".toLowerCase(),new Color(153,153,153));
   color_map.put("gray61".toLowerCase(),new Color(156,156,156));
   color_map.put("grey61".toLowerCase(),new Color(156,156,156));
   color_map.put("gray62".toLowerCase(),new Color(158,158,158));
   color_map.put("grey62".toLowerCase(),new Color(158,158,158));
   color_map.put("gray63".toLowerCase(),new Color(161,161,161));
   color_map.put("grey63".toLowerCase(),new Color(161,161,161));
   color_map.put("gray64".toLowerCase(),new Color(163,163,163));
   color_map.put("grey64".toLowerCase(),new Color(163,163,163));
   color_map.put("gray65".toLowerCase(),new Color(166,166,166));
   color_map.put("grey65".toLowerCase(),new Color(166,166,166));
   color_map.put("gray66".toLowerCase(),new Color(168,168,168));
   color_map.put("grey66".toLowerCase(),new Color(168,168,168));
   color_map.put("gray67".toLowerCase(),new Color(171,171,171));
   color_map.put("grey67".toLowerCase(),new Color(171,171,171));
   color_map.put("gray68".toLowerCase(),new Color(173,173,173));
   color_map.put("grey68".toLowerCase(),new Color(173,173,173));
   color_map.put("gray69".toLowerCase(),new Color(176,176,176));
   color_map.put("grey69".toLowerCase(),new Color(176,176,176));
   color_map.put("gray70".toLowerCase(),new Color(179,179,179));
   color_map.put("grey70".toLowerCase(),new Color(179,179,179));
   color_map.put("gray71".toLowerCase(),new Color(181,181,181));
   color_map.put("grey71".toLowerCase(),new Color(181,181,181));
   color_map.put("gray72".toLowerCase(),new Color(184,184,184));
   color_map.put("grey72".toLowerCase(),new Color(184,184,184));
   color_map.put("gray73".toLowerCase(),new Color(186,186,186));
   color_map.put("grey73".toLowerCase(),new Color(186,186,186));
   color_map.put("gray74".toLowerCase(),new Color(189,189,189));
   color_map.put("grey74".toLowerCase(),new Color(189,189,189));
   color_map.put("gray75".toLowerCase(),new Color(191,191,191));
   color_map.put("grey75".toLowerCase(),new Color(191,191,191));
   color_map.put("gray76".toLowerCase(),new Color(194,194,194));
   color_map.put("grey76".toLowerCase(),new Color(194,194,194));
   color_map.put("gray77".toLowerCase(),new Color(196,196,196));
   color_map.put("grey77".toLowerCase(),new Color(196,196,196));
   color_map.put("gray78".toLowerCase(),new Color(199,199,199));
   color_map.put("grey78".toLowerCase(),new Color(199,199,199));
   color_map.put("gray79".toLowerCase(),new Color(201,201,201));
   color_map.put("grey79".toLowerCase(),new Color(201,201,201));
   color_map.put("gray80".toLowerCase(),new Color(204,204,204));
   color_map.put("grey80".toLowerCase(),new Color(204,204,204));
   color_map.put("gray81".toLowerCase(),new Color(207,207,207));
   color_map.put("grey81".toLowerCase(),new Color(207,207,207));
   color_map.put("gray82".toLowerCase(),new Color(209,209,209));
   color_map.put("grey82".toLowerCase(),new Color(209,209,209));
   color_map.put("gray83".toLowerCase(),new Color(212,212,212));
   color_map.put("grey83".toLowerCase(),new Color(212,212,212));
   color_map.put("gray84".toLowerCase(),new Color(214,214,214));
   color_map.put("grey84".toLowerCase(),new Color(214,214,214));
   color_map.put("gray85".toLowerCase(),new Color(217,217,217));
   color_map.put("grey85".toLowerCase(),new Color(217,217,217));
   color_map.put("gray86".toLowerCase(),new Color(219,219,219));
   color_map.put("grey86".toLowerCase(),new Color(219,219,219));
   color_map.put("gray87".toLowerCase(),new Color(222,222,222));
   color_map.put("grey87".toLowerCase(),new Color(222,222,222));
   color_map.put("gray88".toLowerCase(),new Color(224,224,224));
   color_map.put("grey88".toLowerCase(),new Color(224,224,224));
   color_map.put("gray89".toLowerCase(),new Color(227,227,227));
   color_map.put("grey89".toLowerCase(),new Color(227,227,227));
   color_map.put("gray90".toLowerCase(),new Color(229,229,229));
   color_map.put("grey90".toLowerCase(),new Color(229,229,229));
   color_map.put("gray91".toLowerCase(),new Color(232,232,232));
   color_map.put("grey91".toLowerCase(),new Color(232,232,232));
   color_map.put("gray92".toLowerCase(),new Color(235,235,235));
   color_map.put("grey92".toLowerCase(),new Color(235,235,235));
   color_map.put("gray93".toLowerCase(),new Color(237,237,237));
   color_map.put("grey93".toLowerCase(),new Color(237,237,237));
   color_map.put("gray94".toLowerCase(),new Color(240,240,240));
   color_map.put("grey94".toLowerCase(),new Color(240,240,240));
   color_map.put("gray95".toLowerCase(),new Color(242,242,242));
   color_map.put("grey95".toLowerCase(),new Color(242,242,242));
   color_map.put("gray96".toLowerCase(),new Color(245,245,245));
   color_map.put("grey96".toLowerCase(),new Color(245,245,245));
   color_map.put("gray97".toLowerCase(),new Color(247,247,247));
   color_map.put("grey97".toLowerCase(),new Color(247,247,247));
   color_map.put("gray98".toLowerCase(),new Color(250,250,250));
   color_map.put("grey98".toLowerCase(),new Color(250,250,250));
   color_map.put("gray99".toLowerCase(),new Color(252,252,252));
   color_map.put("grey99".toLowerCase(),new Color(252,252,252));
   color_map.put("gray100".toLowerCase(),new Color(255,255,255));
   color_map.put("grey100".toLowerCase(),new Color(255,255,255));
   color_map.put("dark grey".toLowerCase(),new Color(169,169,169));
   color_map.put("DarkGrey".toLowerCase(),new Color(169,169,169));
   color_map.put("dark gray".toLowerCase(),new Color(169,169,169));
   color_map.put("DarkGray".toLowerCase(),new Color(169,169,169));
   color_map.put("dark blue".toLowerCase(),new Color(0,0,139));
   color_map.put("DarkBlue".toLowerCase(),new Color(0,0,139));
   color_map.put("dark cyan".toLowerCase(),new Color(0,139,139));
   color_map.put("DarkCyan".toLowerCase(),new Color(0,139,139));
   color_map.put("dark magenta".toLowerCase(),new Color(139,0,139));
   color_map.put("DarkMagenta".toLowerCase(),new Color(139,0,139));
   color_map.put("dark red".toLowerCase(),new Color(139,0,0));
   color_map.put("DarkRed".toLowerCase(),new Color(139,0,0));
   color_map.put("light green".toLowerCase(),new Color(144,238,144));
   color_map.put("LightGreen".toLowerCase(),new Color(144,238,144));
   color_map.put("invisible",new Color(0,true));
   color_map.put("transparent",new Color(0,true));
}




}	// end of class SwingColorSet




/* end of SwingColorSet.java */
