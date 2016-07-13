/********************************************************************************/
/*										*/
/*		MintSelector.java						*/
/*										*/
/*	Pattern interface for Mint message interface				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/MintSelector.java,v 1.4 2011-05-27 19:32:42 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintSelector.java,v $
 * Revision 1.4  2011-05-27 19:32:42  spr
 * Change copyrights.
 *
 * Revision 1.3  2010-02-12 00:37:05  spr
 * Move int constants to enum.  Handle nodes going down and bad read returns.
 *
 * Revision 1.2  2006/02/21 17:06:23  spr
 * Upgrade interface to use Element instead of Node for XML.
 *
 * Revision 1.1  2005/07/08 23:32:53  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint;


import org.w3c.dom.Element;




public interface MintSelector
{


/********************************************************************************/
/*										*/
/*	Matching methods							*/
/*										*/
/********************************************************************************/

/**
 *	Match this selector against a message.	If there is no match, then null
 *	is returned.  If there is a match, then a MintArguments structure is
 *	returned containing the matched arguments.
 **/

public MintArguments matchMessage(MintMessage msg);



/**
 *	Match the selector without the overhead of producing the match
 *	arguments.
 **/

public boolean testMatchMessage(MintMessage msg);



/********************************************************************************/
/*										*/
/*	Methods for accessing the pattern					*/
/*										*/
/********************************************************************************/

/**
 *	Return the text string representation of the XML pattern.
 **/

public String getText();




/**
 *	Return the XML document representation of the pattern.
 **/

public Element getXml();



}	// end of interface MintSelector



/* end of MintSelector.java */
