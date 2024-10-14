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
