/********************************************************************************/
/*										*/
/*		IvyXmlReaderThread.java 					*/
/*										*/
/*	Thread to read XML messages from an input stream			*/
/*										*/
/********************************************************************************/
/*	Copyright 2003 Brown University -- Steven P. Reiss		      */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/xml/IvyXmlReaderThread.java,v 1.18 2015/11/20 15:09:27 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyXmlReaderThread.java,v $
 * Revision 1.18  2015/11/20 15:09:27  spr
 * Reformatting.
 *
 * Revision 1.17  2011-05-27 19:32:52  spr
 * Change copyrights.
 *
 * Revision 1.16  2006-12-01 03:22:58  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.15  2006/07/10 14:52:26  spr
 * Code cleanup.
 *
 * Revision 1.14  2006/05/10 15:13:37  spr
 * Better error handling.
 *
 * Revision 1.13  2005/12/08 16:07:45  spr
 * Minor fixup/cleanups.  Hide close errors.
 *
 * Revision 1.12  2005/06/28 17:21:26  spr
 * Better error detection and messages.
 *
 * Revision 1.11  2005/01/15 02:08:32  spr
 * Add some debugging and error checking to XML reader code.
 *
 * Revision 1.10  2005/01/05 15:31:02  spr
 * Fix error message and formatting.
 *
 * Revision 1.9  2004/11/24 22:53:23  spr
 * Better error handling in the XML reader.
 *
 * Revision 1.8  2004/11/09 20:33:06  spr
 * Fix up bugs we introduced into xml scanner.
 *
 * Revision 1.7  2004/11/06 04:28:13  spr
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


import java.io.IOException;
import java.io.Reader;



public abstract class IvyXmlReaderThread extends Thread {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private IvyXmlReader xml_reader;

private static int thread_counter = 0;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public IvyXmlReaderThread(Reader rdr)
{
   this("IvyXmlReader_" + (++thread_counter),rdr);
}



public IvyXmlReaderThread(String name,Reader rdr)
{
   super(name);

   if (rdr == null) xml_reader = null;
   else xml_reader = new IvyXmlReader(rdr);
}




/********************************************************************************/
/*										*/
/*	Main reader routines							*/
/*										*/
/********************************************************************************/

@Override public void run()
{
   if (xml_reader == null) return;

   try {
      IOException ex = null;

      try {
	 for ( ; ; ) {
	    String xmlmsg = xml_reader.readXml();
	    if (xmlmsg == null) break;
	    processXmlMessage(xmlmsg);
	  }
       }
      catch (IOException e) {
	 ex = e;
       }

      if (ex == null) ex = xml_reader.getLastError();
      if (ex != null) {
	 processIoError(ex);
       }

      try {
	 xml_reader.close();
       }
      catch (IOException e) { }
    }
   catch (Throwable t) {
      if (t.getMessage() != null &&
	     (t.getMessage().equalsIgnoreCase("Socket closed") ||
		 t.getMessage().equalsIgnoreCase("Connection reset"))) ;
      else {
	 System.err.println("IVY: Unexpected XML reader error: " + t);
	 t.printStackTrace();
       }
    }
   finally {
      processDone();
    }
}



/********************************************************************************/
/*										*/
/*	Callback methods							*/
/*										*/
/********************************************************************************/

protected abstract void processXmlMessage(String msg);

protected abstract void processDone();

protected void processIoError(IOException e)
{
   System.err.println("IVY: XML reader error for " + getName() + ": " + e);
   if (e.getCause() != null) {
      System.err.println("IVY: Caused by: " + e.getCause());
    }
}



}	// end of class IvyXmlReaderThread




/* end of IvyXmlReaderThread.java */
