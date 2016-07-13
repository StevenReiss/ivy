/********************************************************************************/
/*										*/
/*		IvyJSonReader.java						*/
/*										*/
/*	Class to read one JSon message from a Reader				*/
/*										*/
/********************************************************************************/
/*	Copyright 2003 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Redistribution and use in source and binary forms, with or without		 *
 *  modification, are permitted provided that the following conditions are met:  *
 *										 *
 *  + Redistributions of source code must retain the above copyright notice,	 *
 *	this list of conditions and the following disclaimer.			 *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *	this list of conditions and the following disclaimer in the		 *
 *	documentation and/or other materials provided with the distribution.	 *
 *  + Neither the name of the Brown University nor the names of its		 *
 *	contributors may be used to endorse or promote products derived from	 *
 *	this software without specific prior written permission.		 *
 *										 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE	 *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE	 *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE	 *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 	 *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF	 *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS	 *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN	 *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)	 *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE	 *
 *  POSSIBILITY OF SUCH DAMAGE. 						 *
 *										 *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/xml/IvyJsonReader.java,v 1.1 2015/07/02 19:01:35 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyJsonReader.java,v $
 * Revision 1.1  2015/07/02 19:01:35  spr
 * Minor bug fixes
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.xml;

import java.io.*;
import java.net.SocketTimeoutException;


public class IvyJsonReader extends FilterReader
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private int		cur_state;
private int		element_depth;
private int		string_char;
private IOException	last_error;

private final int	MAX_LENGTH = 128*1024*1024;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public IvyJsonReader(Reader r)
{
   super(r);
   cur_state = 0;
   element_depth = 0;
   string_char = 0;
   last_error = null;
}



public IvyJsonReader(InputStream ins)
{
   this(new InputStreamReader(ins));
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public IOException getLastError()
{
   return last_error;
}




/********************************************************************************/
/*										*/
/*	Method to read an XML message						*/
/*										*/
/********************************************************************************/

public String readJson() throws IOException
{
   return readJson(false);
}



public String readJson(boolean handletimeout) throws IOException
{
   boolean eof = false;

   StringBuffer buf = new StringBuffer();

   init();

   int errct = 0;

   try {
      while (!eof) {
	 int ch = 0;
	 try {
	    ch = read();
	  }
	 catch (SocketTimeoutException e) {
	    if (handletimeout) throw e;
	    continue;
	  }
	 catch (IOException e) {
	    // System.err.println("IVY: reader exception: " + e);
	    String msg = e.getMessage();
	    last_error = e;
	    if (msg.equals("Socket closed")) ch = -1;
	    else if (msg.equals("Stream closed")) ch = -1;
	    else if (errct++ > 1000) ch = -1;
	    else if (msg.equals("Operation timed out") && !handletimeout) continue;
	    else if (msg.equals("Read timed out") && !handletimeout) continue;
	    else if (msg.equals("Connection reset")) ch = -1;
	    else throw e;
	  }
	 if (ch == -1) eof = true;
	 else {
	    errct = 0;
	    last_error = null;
	    if (addChar(buf,ch)) {
	       return buf.toString();
	     }
	  }
	 if (buf.length() > MAX_LENGTH) {
	    System.err.println("IVY: Json read length > " + MAX_LENGTH);
	  }
       }

      if (buf.length() > 0) {
	 String s = buf.toString();
	 if (s.trim().length() > 0 && last_error != null) {
	    IOException ex = new IOException("Incomplete JSON message: `" + buf.toString() + "'");
	    if (last_error != null) ex.initCause(last_error);
	    throw ex;
	  }
       }
    }
   catch (IOException e) {
      throw e;
    }
   catch (Throwable t) {
      System.err.println("Unexpected error building json: " + t);
      System.err.println("\tBuffer size = " + buf.length());
    }

   return null;
}




private void init()
{
   cur_state = 0;
   element_depth = 0;
}



private boolean addChar(StringBuffer buf,int ch)
{
   boolean rslt = false;

   if (cur_state == 0 && ch != '{') {
      System.err.println("JSON: Skip " + ch + " (" + ((char) ch) + ")");
      return false;
    }

   buf.append((char) ch);

   switch (cur_state) {
      case 0 :							// start state
	 if (ch == '{') {
	    cur_state = 1;
	    element_depth = 1;
	  }
	 break;
      case 1 :							// normal state
	 if (ch == '"' || ch == '\'') {
	    cur_state = 2;
	    string_char = ch;
	  }	
	 else if (ch == '}' || ch == ']') {
	    if (--element_depth == 0) rslt = true;
	  }
	 else if (ch == '{' || ch == '[') {
	    ++element_depth;
	  }
	 break;
      case 2 :							// inside string
	 if (ch == string_char) cur_state = 1;
	 else if (ch == '\\') cur_state = 3;
	 break;
      case '3' :
	 cur_state = 2;
	 break;
    }

   if (rslt) cur_state = 0;

   return rslt;
}



}	// end of class IvyJsonReader




/* end of IvyJsonReader.java */
