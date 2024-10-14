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


package edu.brown.cs.ivy.petal;







public interface PetalConstants
{



/********************************************************************************/
/*                                                                              */
/*      Node Shapes                                                             */
/*                                                                              */
/********************************************************************************/

enum PetalNodeShape {
   SQUARE,
   RECTANGLE,
   TRIANGLE,
   TRIANGLE_DOWN,
   CIRCLE,
   DIAMOND,
   PENTAGON
}


   
/********************************************************************************/
/*										*/
/*	Petal Arc End constants 						*/
/*										*/
/********************************************************************************/

int PETAL_ARC_END_CLOSED = 0x1;		// closed (filled) end point

int PETAL_ARC_END_TYPE_MASK = 0xff000;	// end types
int PETAL_ARC_END_ARROW =     0x00000;	// normal arrow head
int PETAL_ARC_END_CIRCLE =    0x01000;	// circle end point
int PETAL_ARC_END_DIAMOND =   0x02000;	// diamond end point
int PETAL_ARC_END_TRIANGLE =  0x04000;	// triangle end point
int PETAL_ARC_END_SQUARE =    0x08000;	// square end point

int PETAL_ARC_END_CLOSED_ARROW = 0x04001;
int PETAL_ARC_END_CLOSED_CIRCLE = 0x01001;
int PETAL_ARC_END_CLOSED_DIAMOND = 0x02001;
int PETAL_ARC_END_CLOSED_TRIANGLE = 0x04001;
int PETAL_ARC_END_CLOSED_SQUARE = 0x08001;



/********************************************************************************/
/*										*/
/*	Keyboard events 							*/
/*										*/
/********************************************************************************/

String PETAL_KEYCMD_CUT = "CUT";


/********************************************************************************/
/*										*/
/*	Positioning constants							*/
/*										*/
/********************************************************************************/

int	PETAL_LEFT_POSITION = 16;
int	PETAL_TOP_POSITION = 16;



}	// end of interface PetalConstants




/* end of PetalConstants.java */

