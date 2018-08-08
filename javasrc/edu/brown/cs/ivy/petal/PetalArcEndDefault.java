/********************************************************************************/
/*										*/
/*		PetalArcEndDefault.java 					*/
/*										*/
/*	Simple default implementation of a Petal Arc end point			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalArcEndDefault.java,v 1.9 2018/08/02 15:10:36 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalArcEndDefault.java,v $
 * Revision 1.9  2018/08/02 15:10:36  spr
 * Fix imports.
 *
 * Revision 1.8  2017/09/08 18:25:33  spr
 * Fix arc end drawing.
 *
 * Revision 1.7  2015/11/20 15:09:23  spr
 * Reformatting.
 *
 * Revision 1.6  2011-05-27 19:32:48  spr
 * Change copyrights.
 *
 * Revision 1.5  2010-11-20 00:28:39  spr
 * Color arcs; add new features to Petal editor.
 *
 * Revision 1.4  2007-05-04 02:00:35  spr
 * Import fixups.
 *
 * Revision 1.3  2005/05/07 22:25:43  spr
 * Updates for java 5.0
 *
 * Revision 1.2  2004/05/05 02:28:08  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1  2003/07/16 19:44:58  spr
 * Move petal from bloom to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.petal;



import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.geom.Point2D;




public class PetalArcEndDefault implements PetalArcEnd, java.io.Serializable
{


/********************************************************************************/
/*										*/
/*	Constants								*/
/*										*/
/********************************************************************************/

static private final double	DEFAULT_SIZE = 6.0;



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private int	end_type;
private Color	end_color;
private double	end_size;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalArcEndDefault(int type)
{
   this(type,DEFAULT_SIZE,Color.black);
}



public PetalArcEndDefault(int type,double size)
{
   this(type,size,Color.black);
}




public PetalArcEndDefault(int type,double size,Color c)
{
   end_type = type;
   end_color = c;
   end_size = size;
}




/********************************************************************************/
/*										*/
/*	Drawing methods 							*/
/*										*/
/********************************************************************************/

@Override public void draw(Graphics g,Point2D tp,Point2D fp)
{
   if (fp == null || tp == null) return;

   int dtyp = end_type & PETAL_ARC_END_TYPE_MASK;

   if (end_color != null) g.setColor(end_color);

   switch (dtyp) {
      case PETAL_ARC_END_CIRCLE :
	 drawCircle(g,tp,fp);
	 break;
      case PETAL_ARC_END_TRIANGLE :
	 drawTriangle(g,tp,fp);
	 break;
      case PETAL_ARC_END_ARROW :
	 if ((end_type & PETAL_ARC_END_CLOSED) != 0) drawTriangle(g,tp,fp);
	 drawArrow(g,tp,fp);
	 break;
      case PETAL_ARC_END_SQUARE :
	 drawSquare(g,tp,fp);
	 break;
      default :
	 break;
    }
}




private void drawCircle(Graphics g,Point2D tp,Point2D fp)
{
   double d = fp.distance(tp);

   if (d == 0) return;

   double t = end_size/d;
   double cx0 = tp.getX() + t*(fp.getX() - tp.getX());
   double cy0 = tp.getY() + t*(fp.getY() - tp.getY());

   int x0 = (int)(cx0 - end_size+1);
   int y0 = (int)(cy0 - end_size+1);
   int wd = (int)(2 * end_size - 1);
   int ht = (int)(2 * end_size - 1);

   if ((end_type & PETAL_ARC_END_CLOSED) != 0) g.fillOval(x0,y0,wd,ht);
   else g.drawOval(x0,y0,wd,ht);
}





private void drawTriangle(Graphics g,Point2D tp,Point2D fp)
{
   double d = fp.distance(tp);

   if (d == 0) return;

   double t = end_size/d;
   double cx0 = tp.getX() + 2*t*(fp.getX() - tp.getX());
   double cy0 = tp.getY() + 2*t*(fp.getY() - tp.getY());
   double cx1 = cx0 + t*(fp.getY() - tp.getY());
   double cy1 = cy0 - t*(fp.getX() - tp.getX());
   double cx2 = cx0 - t*(fp.getY() - tp.getY());
   double cy2 = cy0 + t*(fp.getX() - tp.getX());

   Polygon p = new Polygon();
   p.addPoint((int) tp.getX(),(int) tp.getY());
   p.addPoint((int) cx1,(int) cy1);
   p.addPoint((int) cx2,(int) cy2);

   if ((end_type & PETAL_ARC_END_CLOSED) != 0) g.fillPolygon(p);
   else g.drawPolygon(p);
}




private void drawArrow(Graphics g,Point2D tp,Point2D fp)
{
   if (fp == null) return;

   double d = fp.distance(tp);

   if (d == 0) return;

   double t = end_size/d;
   double cx0 = tp.getX() + 2*t*(fp.getX() - tp.getX());
   double cy0 = tp.getY() + 2*t*(fp.getY() - tp.getY());
   double cx1 = cx0 + t*(fp.getY() - tp.getY());
   double cy1 = cy0 - t*(fp.getX() - tp.getX());
   double cx2 = cx0 - t*(fp.getY() - tp.getY());
   double cy2 = cy0 + t*(fp.getX() - tp.getX());

   g.drawLine((int) cx1,(int) cy1,(int) tp.getX(),(int) tp.getY());
   g.drawLine((int) cx2,(int) cy2,(int) tp.getX(),(int) tp.getY());
}



private void drawSquare(Graphics g,Point2D tp,Point2D fp)
{
   double d = fp.distance(tp);

   if (d == 0) return;

   double dx = (tp.getX() - fp.getX())/d*end_size*0.5;
   double dy = (fp.getY() - tp.getY())/d*end_size*0.5;

   Polygon p = new Polygon();
   p.addPoint((int)(tp.getX()-dy),(int)(tp.getY()-dx));
   p.addPoint((int)(tp.getX()+dy),(int)(tp.getY()+dx));
   p.addPoint((int)(tp.getX()-2*dx-dy),(int)(tp.getY()-2*dy-dx));
   p.addPoint((int)(tp.getX()-2*dx+dy),(int)(tp.getY()-2*dy+dx));

   if ((end_type & PETAL_ARC_END_CLOSED) != 0) g.fillPolygon(p);
   else g.drawPolygon(p);
}





}	// end of class PetalArcEndDefault




/* end of PetalArcEndDefault.java */
