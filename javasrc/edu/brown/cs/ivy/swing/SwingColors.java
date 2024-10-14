/********************************************************************************/
/*										*/
/*		SwingColors.java						*/
/*										*/
/*	Colors applicable to all Swing interfaces for brown packages		*/
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


package edu.brown.cs.ivy.swing;


import java.awt.Color;



public interface SwingColors {


/****************************************************************************************/
/*											*/
/*	Colors										*/
/*											*/
/****************************************************************************************/

Color  SWING_BASE_COLOR = new SwingSetup.BaseColor(255,255,000);


Color SWING_BACKGROUND_COLOR = new SwingSetup.BgColor(SWING_BASE_COLOR,0.05,0.95);

Color SWING_SELECT_COLOR = new SwingSetup.SwColor(SWING_BASE_COLOR,0.5,0.5);

Color SWING_DISABLE_COLOR = new SwingSetup.SwColor(SWING_BASE_COLOR,0.40,0.40);

Color SWING_FOCUS_COLOR = new SwingSetup.SwColor(SWING_BASE_COLOR,-0.1,0.9);

Color SWING_LIGHT_COLOR = new SwingSetup.SwColor(SWING_BASE_COLOR,0.15,0.85);

Color SWING_DARK_COLOR = new SwingSetup.SwColor(SWING_BASE_COLOR,0.6,0.1);

Color SWING_FOREGROUND_COLOR = new SwingSetup.SwColor(SWING_BASE_COLOR,0,0);

Color SWING_BORDER_COLOR = SWING_DARK_COLOR;


Color SWING_TRANSPARENT = new Color(0,true);



}	// end of interface SwingColors



/* end of SwingColors.java */
