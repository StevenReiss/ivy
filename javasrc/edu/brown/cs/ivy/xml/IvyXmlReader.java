/********************************************************************************/
/*										*/
/*		IvyXmlReader.java						*/
/*										*/
/*	Class to read one XML message from a Reader				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/xml/IvyXmlReader.java,v 1.19 2018/08/02 15:11:00 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyXmlReader.java,v $
 * Revision 1.19  2018/08/02 15:11:00  spr
 * Fix imports.
 *
 * Revision 1.18  2016/01/14 16:57:22  spr
 * Add clone element, debugging code.
 *
 * Revision 1.17  2013/09/24 01:07:54  spr
 * data format
 *
 * Revision 1.16  2011-05-27 19:32:52  spr
 * Change copyrights.
 *
 * Revision 1.15  2011-03-19 20:32:20  spr
 * Clean up code.
 *
 * Revision 1.14  2009-03-20 02:00:29  spr
 * Fix up error handling in reader; allow new parser possibility.
 *
 * Revision 1.13  2008-05-07 21:15:46  spr
 * Handle CDATA on input.	Children should iterate only locally.
 *
 * Revision 1.12  2007-05-04 02:00:45  spr
 * Import fixups.
 *
 * Revision 1.11  2006/05/17 02:35:31  spr
 * Increase timeout count.
 *
 * Revision 1.10  2006/04/07 20:24:20  spr
 * Connection reset should be eof.
 *
 * Revision 1.9  2006/02/21 17:07:56  spr
 * Check for input length.
 *
 * Revision 1.8  2005/12/22 20:40:30  spr
 * Better error handling.
 *
 * Revision 1.7  2005/06/28 17:21:26  spr
 * Better error detection and messages.
 *
 * Revision 1.6  2005/03/17 13:38:49  spr
 * Handle socket timeouts on read.
 *
 * Revision 1.5  2005/02/24 15:18:18  spr
 * Another various of stream closed to ignore.
 *
 * Revision 1.4  2005/01/15 02:08:32  spr
 * Add some debugging and error checking to XML reader code.
 *
 * Revision 1.3  2004/11/24 22:53:23  spr
 * Better error handling in the XML reader.
 *
 * Revision 1.2  2004/11/09 20:33:06  spr
 * Fix up bugs we introduced into xml scanner.
 *
 * Revision 1.1  2004/11/06 04:28:13  spr
 * Create plain XML reader and modify the reader thread to use it.
 *
 * Revision 1.6  2004/08/24 20:36:21  spr
 * Allow external naming; support different names for different readers.
 *
 * Revision 1.5  2004/08/13 23:54:38  spr
 * Formatting changes.
 *
 * Revision 1.4  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.3  2003/09/02 16:47:24  spr
 * Allow subclasses to set the thread name.
 *
 * Revision 1.2  2003/04/24 20:06:20  spr
 * Shorten thread names and add command to them.
 *
 * Revision 1.1  2003/04/02 00:39:29  spr
 * Thread to handle reading xml with callbacks on complete node.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.xml;


import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketTimeoutException;



public class IvyXmlReader extends FilterReader {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private int		cur_state;
private int		element_depth;
private IOException	last_error;

private final int	MAX_LENGTH = 128*1024*1024;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public IvyXmlReader(Reader r)
{
   super(r);
   cur_state = 0;
   element_depth = 0;
   last_error = null;
}



public IvyXmlReader(InputStream ins)
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

public String readXml() throws IOException
{
   return readXml(false);
}



public String readXml(boolean handletimeout) throws IOException
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
	    System.err.println("IVY: reader exception: " + e);
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
	    System.err.println("IVY: Xml read length > " + MAX_LENGTH);
	  }
       }

      if (buf.length() > 0) {
	 String s = buf.toString();
	 if (s.trim().length() > 0 && last_error != null) {
	    IOException ex = new IOException("Incomplete XML message: `" + buf.toString() + "'");
	    if (last_error != null) ex.initCause(last_error);
	    throw ex;
	  }
       }
    }
   catch (IOException e) {
      throw e;
    }
   catch (Throwable t) {
      System.err.println("Unexpected error building xml: " + t);
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

   buf.append((char) ch);

   switch (cur_state) {
      case 0 :						     // start state
	 if (ch == '<') cur_state = 1;
	 break;
      case 1 :						     // '<' seen
	 if (ch == '?') cur_state = 10;
	 else if (ch == '!') cur_state = 20;
	 else if (ch == '/') cur_state = 30;
	 else cur_state = 40;
	 break;
      case 2 :						     // neutral state
	 if (ch == '<') cur_state = 1;
	 break;
      case 10 : 					     // <? seen
	 if (ch == '?') cur_state = 11;
	 break;
      case 11 : 					     // <? ... ? seen
	 if (ch == '>') cur_state = 2;
	 break;
      case 20 : 					     // <! seen
	 if (ch == '-') cur_state = 21;
	 else if (ch == '[') cur_state = 50;
	 else if (ch == '>') cur_state = 2;
	 else cur_state = 25;
	 break;
      case 21 : 					     // <!- seen
	 if (ch == '-') cur_state = 22;
	 else if (ch == '>') cur_state = 2;
	 else cur_state = 22;
	 break;
      case 22 : 					     // <!-- ... seen
	 if (ch == '-') cur_state = 23;
	 break;
      case 23 : 					     // <!-- ... -
	 if (ch == '-') cur_state = 24;
	 else cur_state = 22;
	 break;
      case 24 : 					     // <!-- ... --
	 if (ch == '>') cur_state = 2;
	 else cur_state = 23;
	 break;
      case 25 : 					     // <! ...
	 if (ch == '>') cur_state = 2;
	 break;
      case 30 : 					     // </
	 if (ch == '>') {
	    if (--element_depth <= 0) rslt = true;
	    cur_state = 2;
	  }
	 break;
      case 40 : 					     // < ...
	 if (ch == '/') cur_state = 41;
	 else if (ch == '>') {
	    ++element_depth;
	    cur_state = 2;
	  }
	 break;
      case 41 : 					     // < ... /
	 if (ch == '>') {
	    if (element_depth == 0) rslt = true;
	    cur_state = 2;
	  }
	 else cur_state = 40;
	 break;
      case 50 : 						// <![ seen
	 if (ch == 'C') cur_state = 51;
	 else if (ch == '>') cur_state = 2;
	 else cur_state = 25;
	 break;
      case 51 : 						// <![C
	 if (ch == 'D') cur_state = 52;
	 else if (ch == '>') cur_state = 2;
	 else cur_state = 25;
	 break;
      case 52 : 						// <![CD
	 if (ch == 'A') cur_state = 53;
	 else if (ch == '>') cur_state = 2;
	 else cur_state = 25;
	 break;
      case 53 : 						// <![CDA
	 if (ch == 'T') cur_state = 54;
	 else if (ch == '>') cur_state = 2;
	 else cur_state = 25;
	 break;
      case 54 : 						// <![CDAT
	 if (ch == 'A') cur_state = 55;
	 else if (ch == '>') cur_state = 2;
	 else cur_state = 25;
	 break;
      case 55 : 						// <![CDATA
	 if (ch == '[') cur_state = 56;
	 else if (ch == '>') cur_state = 2;
	 else cur_state = 25;
	 break;
      case 56 : 						// <![CDATA[...
	 if (ch == ']') cur_state = 57;
	 break;
      case 57 : 						// <![CDATA[...]
	 if (ch == ']') cur_state = 58;
	 else cur_state = 56;
	 break;
      case 58 : 						// <![CDATA[...]]
	 if (ch == '>') cur_state = 2;
	 else if (ch == ']') cur_state = 58;
	 else cur_state = 56;
	 break;
    }

   if (rslt) cur_state = 0;

   return rslt;
}



}	// end of class IvyXmlReader




/* end of IvyXmlReader.java */
