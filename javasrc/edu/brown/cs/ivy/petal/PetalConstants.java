/********************************************************************************/
/*										*/
/*		PetalConstants.java						*/
/*										*/
/*	Constants for use in the PETAL graphics editor				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalConstants.java,v 1.4 2011-05-27 19:32:48 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalConstants.java,v $
 * Revision 1.4  2011-05-27 19:32:48  spr
 * Change copyrights.
 *
 * Revision 1.3  2008-11-12 13:46:48  spr
 * No change.
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







public interface PetalConstants
{



/********************************************************************************/
/*										*/
/*	Petal Arc End constants 						*/
/*										*/
/********************************************************************************/

static int PETAL_ARC_END_CLOSED = 0x1;		// closed (filled) end point

static int PETAL_ARC_END_TYPE_MASK = 0xff000;	// end types
static int PETAL_ARC_END_ARROW =     0x00000;	// normal arrow head
static int PETAL_ARC_END_CIRCLE =    0x01000;	// circle end point
static int PETAL_ARC_END_DIAMOND =   0x02000;	// diamond end point
static int PETAL_ARC_END_TRIANGLE =  0x04000;	// triangle end point
static int PETAL_ARC_END_SQUARE =    0x08000;	// square end point

static int PETAL_ARC_END_CLOSED_ARROW = 0x04001;
static int PETAL_ARC_END_CLOSED_CIRCLE = 0x01001;
static int PETAL_ARC_END_CLOSED_DIAMOND = 0x02001;
static int PETAL_ARC_END_CLOSED_TRIANGLE = 0x04001;
static int PETAL_ARC_END_CLOSED_SQUARE = 0x08001;



/********************************************************************************/
/*										*/
/*	Keyboard events 							*/
/*										*/
/********************************************************************************/

static	String PETAL_KEYCMD_CUT = "CUT";


/********************************************************************************/
/*										*/
/*	Positioning constants							*/
/*										*/
/********************************************************************************/

static int	PETAL_LEFT_POSITION = 16;
static int	PETAL_TOP_POSITION = 16;



}	// end of interface PetalConstants




/* end of PetalConstants.java */

