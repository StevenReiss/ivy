/********************************************************************************/
/*										*/
/*		PetalHelper.java						*/
/*										*/
/*	Helper functions for using Petal					*/
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


package edu.brown.cs.ivy.petal;



import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;



public class PetalHelper
{


/********************************************************************************/
/*										*/
/*	Port location methods							*/
/*										*/
/********************************************************************************/

public static Point findPortPoint(Rectangle b,Point at,Point from)
{
   double v0 = getIntersect(-1,from,at,b.x,b.y,b.x+b.width-1,b.y);
   v0 = getIntersect(v0,from,at,b.x+b.width-1,b.y,b.x+b.width-1,b.y+b.height-1);
   v0 = getIntersect(v0,from,at,b.x,b.y+b.height-1,b.x+b.width-1,b.y+b.height-1);
   v0 = getIntersect(v0,from,at,b.x,b.y,b.x,b.y+b.height-1);

   if (v0 == 1) return at;

   return new Point((int) (from.x + (at.x-from.x)*v0),(int) (from.y + (at.y - from.y)*v0));
}



public static double getIntersect(double v0,Point fp,Point tp,double x0,double y0,
				      double x1,double y1)
{
   double xlk = tp.x - fp.x;
   double ylk = tp.y - fp.y;
   double xnm = x1 - x0;
   double ynm = y1 - y0;
   double xmk = x0 - fp.x;
   double ymk = y0 - fp.y;
   double det = xnm * ylk - ynm * xlk;

   if (Math.abs(det) < 0.00001) return v0;

   double s = (xnm * ymk - ynm * xmk)/det;
   double t = (xlk * ymk - ylk * xmk)/det;

   if (t >= 0 && t <= 1 && s >= 0 && s <= 1 && (v0 < 0 || s < v0)) v0 = s;

   return v0;
}




public static Point findOvalPortPoint(Rectangle b,Point at,Point from)
{
   Point p0 = findPortPoint(b,at,from);
   Point2D.Double p1 = new Point2D.Double(b.x + b.width/2.0,b.y + b.height/2.0);
   double dx = p0.x - p1.x;
   double dy = p0.y - p1.y;
   double th = Math.atan2(dy,dx);
   int x0 = (int) (p1.x + b.width/2.0 * Math.cos(th));
   int y0 = (int) (p1.y + b.height/2.0 * Math.sin(th));
   return new Point(x0,y0);
}



public static Point findShapePortPoint(Component c,Shape shape,Point at,Point from)
{
   double v0 = -1;
   double [] prior = new double[6];
   double [] cur = new double[6];
   double mx = -1;
   double my = -1;
   
   Rectangle bnds = c.getBounds();
   
   for (PathIterator pi = shape.getPathIterator(null,1); !pi.isDone(); pi.next()) {
      int type = pi.currentSegment(cur);
      cur[0] += bnds.x;
      cur[1] += bnds.y;
      switch (type) {
	 case PathIterator.SEG_MOVETO :
	    mx = cur[0];
	    my = cur[1];
	    break;
	 case PathIterator.SEG_CLOSE :
	    v0 = getIntersect(v0,from,at,prior[0],prior[1],mx,my);
	    break;
	 case PathIterator.SEG_LINETO :
	    v0 = getIntersect(v0,from,at,prior[0],prior[1],cur[0],cur[1]);
	    break;
       }
      prior[0] = cur[0];
      prior[1] = cur[1];
    }

   if (v0 == 1 || v0 < 0) return at;

   return new Point((int) (from.x + (at.x-from.x)*v0),(int) (from.y + (at.y - from.y)*v0));
}





}	// end of interface PetalHelper




/* end of PetalHelper.java */



