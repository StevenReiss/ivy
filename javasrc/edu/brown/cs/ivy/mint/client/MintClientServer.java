/********************************************************************************/
/*										*/
/*		MintClientServer.java						*/
/*										*/
/*	Connection between client and server					*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/client/MintClientServer.java,v 1.5 2018/08/02 15:10:28 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintClientServer.java,v $
 * Revision 1.5  2018/08/02 15:10:28  spr
 * Fix imports.
 *
 * Revision 1.4  2011-05-27 19:32:43  spr
 * Change copyrights.
 *
 * Revision 1.3  2007-08-10 02:11:09  spr
 * Cleanups from eclipse; bug fixes to avoid deadlock.
 *
 * Revision 1.2  2007-05-04 02:00:13  spr
 * Fix bugs related to polling.
 *
 * Revision 1.1  2005/07/08 23:32:57  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint.client;


import edu.brown.cs.ivy.mint.MintConnect;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;



class MintClientServer {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Socket server_socket;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MintClientServer(String id)
{
   server_socket = MintConnect.findServer(id);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

BufferedReader getReader()
{
   BufferedReader lnr = null;

   if (server_socket != null) {
      try {
	 InputStream ins = server_socket.getInputStream();
	 BufferedInputStream bins = new BufferedInputStream(ins);
	 InputStreamReader insr = new InputStreamReader(bins);
	 lnr = new BufferedReader(insr);
       }
      catch (IOException e) {
	 return null;
       }
    }

   return lnr;
}




PrintWriter getWriter()
{
   PrintWriter pw = null;

   if (server_socket != null) {
      try {
	 OutputStream ots = server_socket.getOutputStream();
	 OutputStreamWriter osw = new OutputStreamWriter(ots);
	 pw = new PrintWriter(osw,true);
       }
      catch (IOException e) { pw = null; }
    }

   return pw;
}




}	// end of class MintClientServer



/* end of MintClientServer.java */
