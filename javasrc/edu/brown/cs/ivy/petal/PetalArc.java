/********************************************************************************/
/*										*/
/*		PetalArc.java							*/
/*										*/
/*	Interface defining an arc in a PetalModel graph 			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalArc.java,v 1.7 2011-05-27 19:32:48 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalArc.java,v $
 * Revision 1.7  2011-05-27 19:32:48  spr
 * Change copyrights.
 *
 * Revision 1.6  2010-12-08 22:50:39  spr
 * Fix up dipslay and add new layouts
 *
 * Revision 1.5  2010-02-12 00:38:50  spr
 * No change.
 *
 * Revision 1.4  2005/07/08 20:57:47  spr
 * Change imports.
 *
 * Revision 1.3  2005/04/28 21:48:40  spr
 * Fix up petal to support pebble.
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



import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;



public interface PetalArc extends PetalConstants, Cloneable
{

PetalNode getSource();
PetalNode getTarget();

Point[] getPoints();
Point2D getRelativePoint(double pos);
double getClosestPoint(Point2D loc,Point2D rslt);

boolean getSplineArc();
void setSplineArc(boolean fg);

void layout();

void draw(Graphics g);

boolean contains(Point p);

boolean overPivot(Point p);
int createPivot(Point p);
boolean movePivot(int idx,Point p);
void removePivot(int idx);

boolean handleMouseClick(MouseEvent me);

String getToolTip();

double getLayoutPriority();




}	// end of interface PetalArc




/* end of PetalArc.java */
