/********************************************************************************/
/*										*/
/*		PetalPort.java							*/
/*										*/
/*	Interface defining a port of a node in a PetalModel graph		*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalPort.java,v 1.5 2011-05-27 19:32:49 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalPort.java,v $
 * Revision 1.5  2011-05-27 19:32:49  spr
 * Change copyrights.
 *
 * Revision 1.4  2010-02-12 00:38:50  spr
 * No change.
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



import java.awt.Point;
import java.awt.Rectangle;




public class PetalPort implements PetalConstants, java.io.Serializable
{

/********************************************************************************/
/*										*/
/*	Constants								*/
/*										*/
/********************************************************************************/

public static	final	int	NORTH_WEST = 1;
public static	final	int	NORTH = 2;
public static	final	int	NORTH_EAST = 3;
public static	final	int	WEST = 4;
public static	final	int	CENTER = 5;
public static	final	int	EAST = 6;
public static	final	int	SOUTH_WEST = 7;
public static	final	int	SOUTH = 8;
public static	final	int	SOUTH_EAST = 9;



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private double x_offset;
private double y_offset;

private static final long	serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PetalPort(double x,double y)
{
   setPosition(x,y);
}



public PetalPort(int where)
{
   switch (where) {
      case NORTH_WEST :
	 setPosition(-1,-1);
	 break;
      case NORTH :
	 setPosition(0,-1);
	 break;
      case NORTH_EAST :
	 setPosition(1,-1);
	 break;
      case WEST :
	 setPosition(-1,0);
	 break;
      case CENTER :
	 setPosition(0,0);
	 break;
      case EAST :
	 setPosition(1,0);
	 break;
      case SOUTH_WEST :
	 setPosition(-1,1);
	 break;
      case SOUTH :
	 setPosition(0,1);
	 break;
      case SOUTH_EAST :
	 setPosition(1,1);
	 break;
      default :
	 System.err.println("PETAL: Illegal port specified");
	 setPosition(0,0);
    }
}



public PetalPort()
{
   setPosition(0,0);
}



private void setPosition(double x,double y)
{
   if (x < -1.0) x = -1.0;
   if (x > 1.0) x = 1.0;
   if (y < -1.0) y = -1.0;
   if (y > 1.0) y = 1.0;

   x_offset = x;
   y_offset = y;
}





/********************************************************************************/
/*										*/
/*	Methods to get the actual position for a port				*/
/*										*/
/********************************************************************************/

public Point getConnectionPoint(Rectangle b)
{
   double px = b.x + (x_offset + 1.0)/2.0 * b.width;
   double py = b.y + (y_offset + 1.0)/2.0 * b.height;

   return new Point((int) px,(int) py);
}




}	// end of interface PetalPort




/* end of PetalPort.java */

