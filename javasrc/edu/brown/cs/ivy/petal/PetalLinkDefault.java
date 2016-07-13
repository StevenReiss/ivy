/********************************************************************************/
/*										*/
/*		PetalLinkDefault.java						*/
/*										*/
/*	Basic implementation of a link from an arc to a node			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalLinkDefault.java,v 1.6 2015/11/20 15:09:23 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalLinkDefault.java,v $
 * Revision 1.6  2015/11/20 15:09:23  spr
 * Reformatting.
 *
 * Revision 1.5  2011-05-27 19:32:49  spr
 * Change copyrights.
 *
 * Revision 1.4  2006-12-01 03:22:54  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.3  2005/05/07 22:25:43  spr
 * Updates for java 5.0
 *
 * Revision 1.2  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1  2003/07/16 19:44:59  spr
 * Move petal from bloom to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.petal;



import java.awt.*;
import java.awt.geom.Point2D;




public class PetalLinkDefault implements PetalLink, Cloneable, java.io.Serializable
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private double	arc_position;

private double	node_x_position;
private double	node_y_position;

private double	node_x_offset;
private double	node_y_offset;

private boolean is_movable;
private boolean on_path;

private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalLinkDefault(double apos,double xpos,double ypos,double xoff,double yoff)
{
   arc_position = apos;
   node_x_position = xpos;
   node_y_position = ypos;
   node_x_offset = xoff;
   node_y_offset = yoff;
   is_movable = false;
   on_path = false;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public PetalLink getCopy()
{
   try {
      return (PetalLink) clone();
    }
   catch (CloneNotSupportedException e) { }

   return null;
}



public void setMovable(boolean fg)			{ is_movable = fg; }

public void setOnPath(boolean fg)			{ on_path = fg; }



@Override public void moveLocation(int dx,int dy,PetalArc a,PetalNode n)
{
   if (!is_movable) return;

   node_x_offset += dx;
   node_y_offset += dy;

   Point p = getLocation(a,n);

   if (on_path) {
      node_x_offset = node_y_offset = 0;
    }

   Component c = n.getComponent();
   Rectangle b = c.getBounds();
   double nx = b.width * (node_x_position + 1.0)/2.0;
   double ny = b.height * (node_y_position + 1.0)/2.0;
   double x0 = p.x + nx;
   double y0 = p.y + ny;

   Point2D loc = new Point2D.Double(x0,y0);
   Point2D rslt = new Point2D.Double();

   arc_position = a.getClosestPoint(loc,rslt);

   if (!on_path) {
      node_x_offset = x0 - rslt.getX();
      node_y_offset = y0 - rslt.getY();
    }
}



/********************************************************************************/
/*										*/
/*	Methods to compute the location 					*/
/*										*/
/********************************************************************************/

@Override public Point getLocation(PetalArc a,PetalNode n)
{
   Point2D apt = a.getRelativePoint(arc_position);
   double ax = apt.getX() + node_x_offset;
   double ay = apt.getY() + node_y_offset;

   Component c = n.getComponent();
   Rectangle b = c.getBounds();

   double nx = b.width * (node_x_position + 1.0)/2.0;
   double ny = b.height * (node_y_position + 1.0)/2.0;

   ax -= nx;
   ay -= ny;

   return new Point((int) ax,(int) ay);
}




}	// end of class PetalLinkDefault



/* end of PetalLinkDefault.java */
