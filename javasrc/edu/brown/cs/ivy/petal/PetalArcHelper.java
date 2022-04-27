/********************************************************************************/
/*										*/
/*		PetalArcHelper.java						*/
/*										*/
/*	Helper class for user implementations of PetalArc			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalArcHelper.java,v 1.18 2018/08/02 15:10:36 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalArcHelper.java,v $
 * Revision 1.18  2018/08/02 15:10:36  spr
 * Fix imports.
 *
 * Revision 1.17  2016/10/28 18:31:56  spr
 * Clean up possible concurrent modification exception.
 *
 * Revision 1.16  2015/11/20 15:09:23  spr
 * Reformatting.
 *
 * Revision 1.15  2011-05-27 19:32:48  spr
 * Change copyrights.
 *
 * Revision 1.14  2010-12-08 22:50:39  spr
 * Fix up dipslay and add new layouts
 *
 * Revision 1.13  2010-12-02 23:46:49  spr
 * Petal bug fixes
 *
 * Revision 1.12  2010-11-20 00:28:39  spr
 * Color arcs; add new features to Petal editor.
 *
 * Revision 1.11  2010-11-18 23:09:02  spr
 * Updates to petal to work with bubbles.
 *
 * Revision 1.10  2009-09-17 02:00:14  spr
 * Eclipse cleanup.
 *
 * Revision 1.9  2006-12-01 03:22:54  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.8  2005/07/08 20:57:47  spr
 * Change imports.
 *
 * Revision 1.7  2005/06/07 02:18:22  spr
 * Update for java 5.0
 *
 * Revision 1.6  2005/05/07 22:25:43  spr
 * Updates for java 5.0
 *
 * Revision 1.5  2005/04/28 21:48:40  spr
 * Fix up petal to support pebble.
 *
 * Revision 1.4  2004/05/20 19:09:38  spr
 * Fix problem doing layout before drawing.
 *
 * Revision 1.3  2004/05/20 16:03:37  spr
 * Bug fixes for Petal related to CHIA; add oval helper.
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



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Vector;




public class PetalArcHelper implements PetalArc, java.io.Serializable
{


/********************************************************************************/
/*										*/
/*	Constants								*/
/*										*/
/********************************************************************************/

private static final int	CORRELATE_SIZE = 4;
private static final int	PIVOT_CORRELATE_SIZE = 16;

private static final long	serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private transient PetalNode source_node;
private PetalPort	source_port;
private transient PetalNode target_node;
private PetalPort	target_port;
private transient PetalArcEnd source_end;
private transient PetalArcEnd target_end;

private Vector<Point>	arc_pivots;
private Point []	arc_points;

private transient Stroke	  arc_stroke;
private transient GeneralPath	  arc_path;
private transient Color 	  arc_color;

private boolean 	spline_arc;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalArcHelper(PetalNode src,PetalNode tgt)
{
   this(src,null,tgt,null);
}



public PetalArcHelper(PetalNode src,PetalPort sport,PetalNode tgt,PetalPort tport)
{
   source_node = src;
   if (sport == null) source_port = new PetalPort();
   else source_port = sport;

   target_node = tgt;
   if (tport == null) target_port = new PetalPort();
   else target_port = tport;

   arc_pivots = null;

   arc_stroke = null;
   arc_path = null;				// not drawn
   arc_color = null;
   arc_points = null;
   source_end = null;
   target_end = null;

   spline_arc = true;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public PetalNode getSource()		{ return source_node; }

@Override public PetalNode getTarget()		{ return target_node; }

public PetalPort getSourcePort()	{ return source_port; }

public PetalPort getTargetPort()	{ return target_port; }



public void setSourceEnd(PetalArcEnd e) 	{ source_end = e; }

public void setTargetEnd(PetalArcEnd e) 	{ target_end = e; }



public void setSource(PetalNode pn)
{
   if (source_node == pn) return;

   source_node = pn;
   clearPivots();
   arc_path = null;
}



public void setTarget(PetalNode pn)
{
   if (target_node == pn) return;

   target_node = pn;
   clearPivots();
   arc_path = null;
}



public PetalArcHelper cloneArcHelper(PetalClipSet pcs)
{
   PetalArcHelper pa = null;

   try {
      pa = (PetalArcHelper) clone();
    }
   catch (CloneNotSupportedException e) { }

   if (pa != null && pcs != null) {
      pa.source_node = pcs.getMapping(source_node);
      pa.target_node = pcs.getMapping(target_node);
    }

   return pa;
}



@Override public void setSplineArc(boolean fg)			{ spline_arc = fg; }
@Override public boolean getSplineArc()				{ return spline_arc; }




public void getPropertiesFrom(PetalArcHelper h)
{
   source_end = h.source_end;
   target_end = h.target_end;

   if (h.arc_pivots != null) arc_pivots = new Vector<Point>(h.arc_pivots);

   spline_arc = h.spline_arc;
}




public void setStroke(Stroke s)
{
   arc_stroke = s;
}


public void setColor(Color c)
{
   arc_color = c;
}


@Override public double getLayoutPriority()			{ return 1.0; }



/********************************************************************************/
/*										*/
/*	Interaction methods							*/
/*										*/
/********************************************************************************/

@Override public boolean handleMouseClick(MouseEvent evt) 	{ return false; }

@Override public String getToolTip()				{ return null; }




/********************************************************************************/
/*										*/
/*	Methods to get the points for the arc					*/
/*										*/
/********************************************************************************/

@Override public Point [] getPoints()
{
   return arc_points;
}



/********************************************************************************/
/*										*/
/*	Method to draw the arc							*/
/*										*/
/********************************************************************************/

@Override public void layout()
{
   arc_path = new GeneralPath();

   int ct = 2;
   if (arc_pivots != null) ct += arc_pivots.size();
   arc_points = new Point[ct];
   ct = 0;

   if (source_node == null || target_node == null) return;
   Component sc = source_node.getComponent();
   Component tc = target_node.getComponent();

   // getConnectionPoint should take a bias parameter, indicating which edge is preferred if any
   arc_points[ct++] = source_port.getConnectionPoint(sc.getBounds());

   if (arc_pivots != null) {
      for (Iterator<Point> it = arc_pivots.iterator(); it.hasNext(); ) {
	 arc_points[ct++] = it.next();
       }
    }

   arc_points[ct] = target_port.getConnectionPoint(tc.getBounds());

   arc_points[0] = source_node.findPortPoint(arc_points[0],arc_points[1]);
   arc_points[ct] = target_node.findPortPoint(arc_points[ct],arc_points[ct-1]);

   if (!spline_arc || arc_points.length == 2) {
      for (int i = 1; i < arc_points.length; ++i) {
	 Line2D line = new Line2D.Double(arc_points[i-1],arc_points[i]);
	 arc_path.append(line,true);
       }
    }
   else {
      if (arc_points.length == 2) {
	 Point p = new Point();
	 arc_path.moveTo(arc_points[0].x,arc_points[0].y);
	 for (int i = 1; i < arc_points.length; ++i) {
	    if (Math.abs(arc_points[i-1].x - arc_points[i].x) <
		   Math.abs(arc_points[i-1].y - arc_points[i].y)) {
	       p.setLocation(arc_points[i-1].x,arc_points[i].y);
	     }
	    else {
	       p.setLocation(arc_points[i].x,arc_points[i-1].y);
	     }
	    arc_path.quadTo(p.x,p.y,arc_points[i].x,arc_points[i].y);
	  }
       }
      else {
	 /*********
	 System.err.println("BEGIN SPLINE");
	 for (int i = 0; i < arc_points.length; ++i) {
	    System.err.println("POINT " + arc_points[i].x + " " + arc_points[i].y);
	 }
	 ************/
	 generateSpline();
       }
    }
}



private void generateSpline()
{
   int ptct = arc_points.length;
   Point2D [] npts = new Point2D[ptct];
   double [] g = new double[ptct];

   // first find control points that put the original points on the curve

   for (int i = 0; i < ptct; ++i) {
      npts[i] = new Point2D.Double(6.0*arc_points[i].x,6.0*arc_points[i].y);
    }
   npts[0].setLocation(arc_points[0].x,arc_points[0].y);
   g[0] = 0;

   for (int i = 1; i < ptct-1; ++i) {
      double c = 4.0 - g[i-1];
      npts[i].setLocation((npts[i].getX() - npts[i-1].getX())/c,
			     (npts[i].getY() - npts[i-1].getY())/c);
      g[i] = 1.0/c;
    }

   npts[ptct-1].setLocation(arc_points[ptct-1].x,arc_points[ptct-1].y);

   for (int i = ptct-2; i > 0; --i) {
      npts[i].setLocation(npts[i].getX() - g[i]*npts[i+1].getX(),
			     npts[i].getY() - g[i]*npts[i+1].getY());
    }

   // then we can draw the spline

   arc_path.moveTo(arc_points[0].x,arc_points[0].y);
   calcBSpline(npts[0],npts[0],npts[0],npts[1]);
   calcBSpline(npts[0],npts[0],npts[1],npts[2]);
   for (int i = 1; i < ptct-2; ++i) {
      calcBSpline(npts[i-1],npts[i],npts[i+1],npts[i+2]);
    }
   calcBSpline(npts[ptct-3],npts[ptct-2],npts[ptct-1],npts[ptct-1]);
   calcBSpline(npts[ptct-2],npts[ptct-1],npts[ptct-1],npts[ptct-1]);
}



private void calcBSpline(Point2D cm1,Point2D c,Point2D cp1,Point2D cp2)
{
   Point2D p1 = new Point2D.Double();
   Point2D p2 = new Point2D.Double();
   Point2D t = new Point2D.Double();
   Point2D p3 = new Point2D.Double();

   thirdPoint(c,cp1,p1);
   thirdPoint(cp1,c,p2);
   thirdPoint(cp1,cp2,t);
   midPoint(t,p2,p3);

   arc_path.curveTo((float) p1.getX(),(float) p1.getY(),
		       (float) p2.getX(),(float) p2.getY(),
		       (float) p3.getX(),(float) p3.getY());
}



private void thirdPoint(Point2D p,Point2D q,Point2D r)
{
   r.setLocation((2*p.getX()+q.getX())/3.0 , (2*p.getY()+q.getY())/3.0);
}


private void midPoint(Point2D p,Point2D q,Point2D r)
{
   r.setLocation((p.getX()+q.getX())/2.0, (p.getY()+q.getY())/2.0);
}



@Override public void draw(Graphics g)
{
   if (arc_path == null) layout();
   if (arc_stroke == null) {
      arc_stroke = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND);
    }

   Graphics2D g2 = (Graphics2D) g;

   g2.setStroke(arc_stroke);
   if (arc_color != null) {
      g2.setColor(arc_color);
   }
   g2.draw(arc_path);

   if (source_end != null) source_end.draw(g,arc_points[0],getArrowPoint(true));
   if (target_end != null) {
      int ct = arc_points.length-1;
      target_end.draw(g,arc_points[ct],getArrowPoint(false));
    }
}



/********************************************************************************/
/*										*/
/*	Correlation methods							*/
/*										*/
/********************************************************************************/

@Override public boolean contains(Point p)
{
   if (arc_path == null) return false;

   Point2D cpt = new Point2D.Double();

   getClosestPoint(p,cpt);

   return p.distance(cpt) <= CORRELATE_SIZE;
}



/********************************************************************************/
/*										*/
/*	Relative point location methods 					*/
/*										*/
/********************************************************************************/

@Override public Point2D getRelativePoint(double pos)
{
   if (arc_path == null) layout();

   AffineTransform at = new AffineTransform();
   double [] pts = new double[6];
   Point2D.Double cpt = new Point2D.Double();
   Point2D.Double npt = new Point2D.Double();

   double len = 0;
   for (PathIterator pi = arc_path.getPathIterator(at,1.0); !pi.isDone(); pi.next()) {
      int pt = pi.currentSegment(pts);
      int j = -1;
      switch (pt) {
	 case PathIterator.SEG_MOVETO :
	    cpt.setLocation(pts[0],pts[1]);
	    break;
	 case PathIterator.SEG_LINETO :
	    j = 0;
	    break;
	 case PathIterator.SEG_QUADTO :
	    j = 2;
	    break;
	 case PathIterator.SEG_CUBICTO :
	    j = 4;
	    break;
	 default :
	    break;
       }
      if (j >= 0) {
	 len += cpt.distance(pts[j],pts[j+1]);
	 cpt.setLocation(pts[j],pts[j+1]);
       }
    }

   double apos = len * pos;
   double nlen = 0;
   for (PathIterator pi = arc_path.getPathIterator(at,1.0); !pi.isDone(); pi.next()) {
      int pt = pi.currentSegment(pts);
      int j = -1;
      nlen = 0;
      switch (pt) {
	 case PathIterator.SEG_MOVETO :
	    cpt.setLocation(pts[0],pts[1]);
	    break;
	 case PathIterator.SEG_LINETO :
	    j = 0;
	    break;
	 case PathIterator.SEG_QUADTO :
	    j = 2;
	    break;
	 case PathIterator.SEG_CUBICTO :
	    j = 4;
	    break;
	 default :
	    break;
       }
      if (j >= 0) {
	 npt.setLocation(pts[j],pts[j+1]);
	 nlen = cpt.distance(npt);
	 if (nlen >= apos) break;
	 apos -= nlen;
	 cpt.setLocation(npt);
       }
    }

   if (nlen > 0) {
      double ax = cpt.x + apos/nlen * (npt.x - cpt.x);
      double ay = cpt.y + apos/nlen * (npt.y - cpt.y);
      cpt.setLocation(ax,ay);
    }

   return cpt;
}




@Override public double getClosestPoint(Point2D loc,Point2D rslt)
{
   AffineTransform at = new AffineTransform();
   double [] pts = new double[6];
   Point2D.Double cpt = new Point2D.Double();
   Point2D.Double npt = new Point2D.Double();
   Point2D.Double pt0 = null;
   Point2D.Double pt1 = null;
   double dmin = 0;
   double lmin = 0;

   if (arc_path == null) layout();

   double len = 0;
   for (PathIterator pi = arc_path.getPathIterator(at,1.0); !pi.isDone(); pi.next()) {
      int pt = pi.currentSegment(pts);
      int j = -1;
      switch (pt) {
	 case PathIterator.SEG_MOVETO :
	    cpt.setLocation(pts[0],pts[1]);
	    break;
	 case PathIterator.SEG_LINETO :
	    j = 0;
	    break;
	 case PathIterator.SEG_QUADTO :
	    j = 2;
	    break;
	 case PathIterator.SEG_CUBICTO :
	    j = 4;
	    break;
	 default :
	    break;
       }
      if (j >= 0) {
	 npt.setLocation(pts[j],pts[j+1]);
	 double d = Line2D.ptSegDistSq(cpt.x,cpt.y,npt.x,npt.y,loc.getX(),loc.getY());
	 if (pt0 == null || d < dmin) {
	    dmin = d;
	    lmin = len;
	    pt0 = new Point2D.Double(cpt.x,cpt.y);
	    pt1 = new Point2D.Double(npt.x,npt.y);
	  }
	 len += cpt.distance(npt);
	 cpt.setLocation(npt);
       }
    }

   if (pt0 == null || pt1 == null) {
      if (rslt != null) rslt.setLocation(cpt.x,cpt.y);
      return 0;
    }

   double x1,y1;

   double f = pt1.x - pt0.x;
   double g = pt1.y - pt0.y;
   double fsq = f*f;
   double gsq = g*g;
   double fgsq = fsq+gsq;
   if (fgsq < 0.0000001) {
      x1 = pt1.x;
      y1 = pt1.y;
    }
   else {
      double xj0 = loc.getX() - pt0.x;
      double yj0 = loc.getY() - pt0.y;
      double tj = (f * xj0 + g * yj0)/fgsq;
      if (tj < 0) tj = 0;
      if (tj > 1) tj = 1;
      x1 = pt0.x + tj*(pt1.x - pt0.x);
      y1 = pt0.y + tj*(pt1.y - pt0.y);
    }

   double apos = 0;

   if (len > 0) {
      double len1 = Point2D.distance(pt0.x,pt0.y,x1,y1);
      apos = (lmin + len1)/len;
    }

   if (rslt != null) rslt.setLocation(x1,y1);

   return apos;
}



public Point2D getArrowPoint(boolean start)
{
   AffineTransform at = new AffineTransform();
   double [] pts = new double[6];
   Point2D.Double cpt = new Point2D.Double();
   Point2D.Double npt = new Point2D.Double();

   if (arc_path == null) layout();

   for (PathIterator pi = arc_path.getPathIterator(at,1.0); !pi.isDone(); pi.next()) {
      int pt = pi.currentSegment(pts);
      int j = -1;
      switch (pt) {
	 case PathIterator.SEG_MOVETO :
	    npt.setLocation(pts[0],pts[1]);
	    break;
	 case PathIterator.SEG_LINETO :
	    j = 0;
	    break;
	 case PathIterator.SEG_QUADTO :
	    j = 2;
	    break;
	 case PathIterator.SEG_CUBICTO :
	    j = 4;
	    break;
	 default :
	    break;
       }
      if (j >= 0) {
	 cpt.setLocation(npt);
	 npt.setLocation(pts[j],pts[j+1]);
	 if (start) return npt;
       }
    }

   return cpt;
}




private int getPreviousPivot(Point p)
{
   Rectangle2D r = new Rectangle(p.x-1,p.y-1,3,3);
   AffineTransform at = new AffineTransform();
   double [] pts = new double[6];
   Point2D.Double npt = new Point2D.Double();
   Line2D.Double ln = new Line2D.Double();
   QuadCurve2D.Double qc = new QuadCurve2D.Double();
   CubicCurve2D.Double cc = new CubicCurve2D.Double();

   if (arc_path == null) layout();

   int ct = 0;
   int rslt = -1;
   for (PathIterator pi = arc_path.getPathIterator(at); rslt < 0 && !pi.isDone(); pi.next()) {
      int pt = pi.currentSegment(pts);
      int j = -1;
      switch (pt) {
	 case PathIterator.SEG_MOVETO :
	    npt.setLocation(pts[0],pts[1]);
	    break;
	 case PathIterator.SEG_LINETO :
	    ln.setLine(npt.x,npt.y,pts[0],pts[1]);
	    if (ln.intersects(r)) rslt = ct;
	    j = 0;
	    break;
	 case PathIterator.SEG_QUADTO :
	    qc.setCurve(npt.x,npt.y,pts[0],pts[1],pts[2],pts[3]);
	    if (qc.intersects(r)) rslt = ct;
	    j = 2;
	    break;
	 case PathIterator.SEG_CUBICTO :
	    cc.setCurve(npt.x,npt.y,pts[0],pts[1],pts[2],pts[3],pts[4],pts[5]);
	    if (cc.intersects(r)) rslt = ct;
	    j = 4;
	    break;
	 default :
	    break;
       }
      if (j >= 0) {
	 npt.setLocation(pts[j],pts[j+1]);
	 ++ct;
       }
    }

   if (rslt >= 0) {
      if (rslt > 0) --rslt;			// eliminate dummy first segment
      if (rslt > arc_pivots.size()) --rslt;	// eliminate dummy last segment
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Pivot methods								*/
/*										*/
/********************************************************************************/

public void clearPivots()
{
   if (arc_pivots == null) return;

   arc_pivots = null;
}



public Point [] getPivots()
{
   if (arc_pivots == null) return null;

   int ct = arc_pivots.size();
   if (ct == 0) return null;

   Point [] pvts = new Point[ct];
   arc_pivots.copyInto(pvts);

   return pvts;
}



@Override public boolean overPivot(Point p)
{
   if (arc_pivots == null) return false;

   for (int i = 0; i < arc_pivots.size(); ++i) {
      Point pv = arc_pivots.elementAt(i);
      double d = pv.distance(p);
      if (d <= PIVOT_CORRELATE_SIZE/2) {
	 return true;
       }
    }

   return false;
}




@Override public int createPivot(Point p)
{
   int rslt = -1;

   if (arc_pivots == null) {
      arc_pivots = new Vector<Point>();
      arc_pivots.add(p);
      rslt = 0;
    }
   else {
      for (int i = 0; i < arc_pivots.size(); ++i) {
	 Point pv = arc_pivots.elementAt(i);
	 double d = pv.distance(p);
	 if (d <= PIVOT_CORRELATE_SIZE/2) {
	    rslt = i;
	    break;
	  }
       }
      if (rslt < 0) {
	 rslt = getPreviousPivot(p);
	 if (rslt >= 0) arc_pivots.insertElementAt(p,rslt);
       }
    }

   layout();

   return rslt;
}



@Override public boolean movePivot(int idx,Point p)
{
   if (arc_pivots == null) arc_pivots = new Vector<Point>();
   if (arc_pivots.size() == idx) arc_pivots.add(p);
   else if (arc_pivots.size() < idx) return false;
   else arc_pivots.setElementAt(p,idx);

   Component sc = source_node.getComponent();
   Component tc = target_node.getComponent();
   Rectangle sr = sc.getBounds();
   Rectangle tr = tc.getBounds();
   if (sr.contains(p)) return false;
   if (tr.contains(p)) return false;

   if (!spline_arc && arc_points != null && idx > 0 && idx+2 < arc_points.length) {
      Point p0 = arc_points[idx];
      Point p1 = arc_points[idx+2];
      double d = Line2D.ptSegDist(p0.x,p0.y,p1.x,p1.y,p.x,p.y);
      if (d <= PIVOT_CORRELATE_SIZE/2) return false;
    }
   else if (arc_points != null) {
      for (int i = 0; i < arc_points.length; ++i) {
	 double d = p.distance(arc_points[i]);
	 if (i != idx+1 && d <= PIVOT_CORRELATE_SIZE/2)
	    return false;
       }
    }

   layout();

   return true;
}



@Override public void removePivot(int idx)
{
   if (arc_pivots.size() <= 1) arc_pivots = null;
   else arc_pivots.removeElementAt(idx);

   layout();
}



}	// end of interface PetalArcHelper




/* end of PetalArcHelper.java */




