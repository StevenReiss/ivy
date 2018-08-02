/********************************************************************************/
/*										*/
/*		MintConnect.java						*/
/*										*/
/*	Class for managing connection to server master for Mint 		*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/MintConnect.java,v 1.7 2011-05-27 19:32:42 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintConnect.java,v $
 * Revision 1.7  2011-05-27 19:32:42  spr
 * Change copyrights.
 *
 * Revision 1.6  2010-08-20 20:58:24  spr
 * Add logging and options for port numbers
 *
 * Revision 1.5  2010-08-04 22:01:53  spr
 * Master server not ready for localhost.
 *
 * Revision 1.4  2009-06-04 18:50:41  spr
 * Use ivyJava call when necessary.
 *
 * Revision 1.3  2007-05-04 02:00:11  spr
 * Fix bugs related to polling.
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

import edu.brown.cs.ivy.mint.match.MintMatchSelector;

import org.w3c.dom.Element;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;



public class MintConnect implements MintConstants {


/********************************************************************************/
/*										*/
/*	Methods to register a server with the master socket			*/
/*										*/
/********************************************************************************/

/**
 *	Register the given server socket with the given name.  If the name
 *	is not specified, the default local name is used.
 **/

public static boolean registerSocket(String id,ServerSocket ss)
{
   if (id == null) {
      String h = ss.getInetAddress().getHostAddress();
      if (h.equals("0.0.0.0")) {
	 try {
	    h = InetAddress.getLocalHost().getHostAddress();
	  }
	 catch (UnknownHostException e) { }
       }

      id = MINT_DEFAULT_SERVICE_NAME + "_" + System.getProperty("user.name") +
	 "_" + h;
    }

   return MintMaster.registerSocket(id,ss);
}



/********************************************************************************/
/*										*/
/*	Methods to create a socket to a server					*/
/*										*/
/********************************************************************************/

/**
 *	Find the mint server corresponding to the given id.  If the name is
 *	not specified, the default local name is used.
 **/

public static Socket findServer(String id)
{
   if (id == null) {
      try {
	 String host = InetAddress.getLocalHost().getHostName();
	 id = MINT_DEFAULT_SERVICE_NAME + "_" + System.getProperty("user.name") +
	    "_" + host;
	 id = id.replace(" ","_");
       }
      catch (IOException e) { return null; }
    }

   String dbg = "";
   String flag = System.getProperty("edu.brown.cs.ivy.mint.debug");
   if (flag != null) {
      if (flag.startsWith("t") || flag.startsWith("T") || flag.startsWith("1") ||
	     flag.startsWith("y") || flag.startsWith("Y"))
	 dbg = "-Debug ";
    }


   String args = dbg + id;

   String pn = System.getProperty("edu.brown.cs.ivy.mint.server.port");
   if (pn != null) {
      args += " -port " + pn;
    }

   return MintMaster.findServer(id,args);
}



/********************************************************************************/
/*										*/
/*	Methods to create a pattern-based selector				*/
/*										*/
/********************************************************************************/

/**
 *	Create a MintSelector for pattern matching using either an XML string
 *	or an XML structure.
 **/

public static MintSelector createSelector(String pat)
{
   return new MintMatchSelector(pat);
}


public static MintSelector createSelector(Element pat)
{
   return new MintMatchSelector(pat);
}




}	// end of class MintConnect




/* end of MintConnect.java */
