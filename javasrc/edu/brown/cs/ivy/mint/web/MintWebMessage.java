/********************************************************************************/
/*										*/
/*		MintWebMessage.java						*/
/*										*/
/*	Class representing a encode message from a mint client			*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/web/MintWebMessage.java,v 1.3 2013/11/15 02:39:13 spr Exp $ */

/*********************************************************************************
 *
 * $Log: MintWebMessage.java,v $
 * Revision 1.3  2013/11/15 02:39:13  spr
 * Fix imports
 *
 * Revision 1.2  2011-05-27 19:32:47  spr
 * Change copyrights.
 *
 * Revision 1.1  2011-05-17 01:05:26  spr
 * Mint servlet for messaging.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.mint.web;



import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;


class MintWebMessage implements MintWebConstants {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private byte [] 	message_cnts;
private int		message_len;
private int		message_flags;
	


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MintWebMessage(HttpServletRequest req,int fgs) throws IOException
{
   message_flags = fgs;

   ServletInputStream sis = req.getInputStream();
   byte [] hdr = new byte[4096];

   int sts = sis.readLine(hdr,0,hdr.length);
   if (sts < 0) return;

   String s = new String(hdr,0,sts);
   StringTokenizer tok = new StringTokenizer(s," \r\n");
   message_len = Integer.parseInt(tok.nextToken());
   message_cnts = new byte[message_len];

   int len = 0;
   while (len < message_len) {
      int ct = sis.read(message_cnts,len,message_len-len);
      if (ct < 0) throw new IOException("Incomplete message");
      len += ct;
    }
}



/********************************************************************************/
/*										*/
/*	I/O methods								*/
/*										*/
/********************************************************************************/

int getLength() 			{ return message_len; }
int getFlags()				{ return message_flags; }

void output(OutputStream ots) throws IOException
{
   ots.write(message_cnts,0,message_len);
}




}	// end of class MintWebMessage




/* end of MintWebMessage.java */
