/********************************************************************************/
/*                                                                              */
/*              JannotFileObject.java                                           */
/*                                                                              */
/*      Implementation of a Java File Object                                    */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.ivy.jannot;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.lang.model.element.Element;
import javax.tools.SimpleJavaFileObject;

import edu.brown.cs.ivy.jcomp.JcompSource;

import javax.tools.JavaFileObject;



public class JannotFileObject extends SimpleJavaFileObject implements JannotConstants, JcompSource
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Element [] related_elements;
private JannotOutputStream output_buffer;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotFileObject(URI name,JavaFileObject.Kind k,Element [] elts)
{
   super(name,k);
   related_elements = elts;
   output_buffer = null;
}


public static JannotFileObject createFileObject(JcompSource src)
{
   if (src == null) return null;
   
   String pnm = src.getFileName();
   try {
      URI uri = new URI("file:" + pnm);
      return new JannotFileObject(uri,JavaFileObject.Kind.SOURCE,null);
    }
   catch (URISyntaxException e) {}
   
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

/**
 *      Check if file is closed
 **/
boolean isClosed()
{
   if (output_buffer == null) return false;
   return output_buffer.isClosed();
}

/**
 *      Get elements related to the file
 **/
Element [] getRelatedElements()
{
   return related_elements;
}



/********************************************************************************/
/*                                                                              */
/*      File Methods                                                            */
/*                                                                              */
/********************************************************************************/

@Override public OutputStream openOutputStream()
{
   if (output_buffer == null) {
      output_buffer = new JannotOutputStream();
    }
   return output_buffer;
}


@Override public CharSequence getCharContent(boolean ignerr)
{
   return output_buffer.toString();
}


/********************************************************************************/
/*                                                                              */
/*      Jcomp source methods                                                    */
/*                                                                              */
/********************************************************************************/

@Override public String getFileContents()
{
   return output_buffer.toString();
}


@Override public String getFileName()
{
   return getName();
}



/********************************************************************************/
/*                                                                              */
/*      Track whether output stream is closed                                   */
/*                                                                              */
/********************************************************************************/

private static class JannotOutputStream extends ByteArrayOutputStream {
   
   private boolean is_closed;
   
   JannotOutputStream() { 
      is_closed = false;
    }
   
   @Override public void close() {
      is_closed = true;
    }
      
   boolean isClosed()                           { return is_closed; }
   
}       // end of inner class JannotOutputStream

}       // end of class JannotFileObject




/* end of JannotFileObject.java */

