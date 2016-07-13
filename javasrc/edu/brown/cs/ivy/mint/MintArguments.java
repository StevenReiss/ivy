/********************************************************************************/
/*										*/
/*		MintArguments.java						*/
/*										*/
/*	Pattern matching resultant arguments interface				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/MintArguments.java,v 1.4 2011-05-27 19:32:42 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintArguments.java,v $
 * Revision 1.4  2011-05-27 19:32:42  spr
 * Change copyrights.
 *
 * Revision 1.3  2007-08-10 02:11:04  spr
 * Cleanups from eclipse; bug fixes to avoid deadlock.
 *
 * Revision 1.2  2006/02/21 17:06:23  spr
 * Upgrade interface to use Element instead of Node for XML.
 *
 * Revision 1.1  2005/07/08 23:32:52  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint;


import org.w3c.dom.Element;



public interface MintArguments
{


/********************************************************************************/
/*										*/
/*	Methods to access the argument values					*/
/*										*/
/********************************************************************************/

/**
 *	Return the number of matched arguments
 **/

public int getNumArguments();



/**
 *	Return the ith argument as a string
 **/

public String getArgument(int idx);



/**
 *	Return the ith argument as an integer
 **/

public int getIntArgument(int idx);



/**
 *	Return the ith argument as an integer
 **/

public long getLongArgument(int idx);



/**
 *	Return the ith argument as a floating point value
 **/

public double getRealArgument(int idx);



/**
 *	Return the ith argument as an XML element
 **/

public Element getXmlArgument(int idx);




}	// end of interface MintArguments




/* end of MintArguments.java */
