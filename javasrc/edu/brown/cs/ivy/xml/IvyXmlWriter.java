/********************************************************************************/
/*										*/
/*		IvyXmlWriter.java						*/
/*										*/
/*	Writer for outputing XML						*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/xml/IvyXmlWriter.java,v 1.35 2019/02/23 02:59:15 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyXmlWriter.java,v $
 * Revision 1.35  2019/02/23 02:59:15  spr
 * Code cleanup.
 *
 * Revision 1.34  2018/08/02 15:11:00  spr
 * Fix imports.
 *
 * Revision 1.33  2015/11/20 15:09:27  spr
 * Reformatting.
 *
 * Revision 1.32  2014/01/22 00:31:35  spr
 * Handle escapes for cdata.
 *
 * Revision 1.31  2013/11/15 02:39:15  spr
 * Fix imports
 *
 * Revision 1.30  2013/09/24 01:07:54  spr
 * data format
 *
 * Revision 1.29  2012-10-05 00:46:49  spr
 * Bug fix.
 *
 * Revision 1.28  2012-08-29 01:40:56  spr
 * Code cleanup for new compiler.
 *
 * Revision 1.27  2012-05-22 00:43:49  spr
 * Handle data input and output as fields.
 *
 * Revision 1.26  2011-07-14 12:49:43  spr
 * Add support for partial byte arrays.
 *
 * Revision 1.25  2011-05-27 19:32:52  spr
 * Change copyrights.
 *
 * Revision 1.24  2010-07-24 01:59:45  spr
 * USe UTF-8 to write XML files.
 *
 * Revision 1.23  2010-07-01 21:58:27  spr
 * Add getLength call to IvyXmlWriter for strings.
 *
 * Revision 1.22  2010-05-18 22:05:48  spr
 * Update xml formatting.
 *
 * Revision 1.21  2010-02-12 00:40:22  spr
 * Add code to handle java beans.
 *
 * Revision 1.20  2009-09-17 02:01:29  spr
 * Code cleanup.
 *
 * Revision 1.19  2008-11-12 13:47:07  spr
 * Better CDATA handling.
 *
 * Revision 1.18  2008-03-14 12:28:00  spr
 * Handle namespaces.
 *
 * Revision 1.17  2007-08-10 02:11:29  spr
 * Cleanups from eclipse.
 *
 * Revision 1.16  2007-05-04 16:12:51  spr
 * Add constructor for implicit StringWriter (and toString method).
 *
r* Revision 1.15  2007-02-27 18:54:24  spr
 * Add load from File object.
 *
 * Revision 1.14  2006-11-09 00:33:31  spr
 * Add file name based constructor.
 *
 * Revision 1.13  2006/07/10 14:52:26  spr
 * Code cleanup.
 *
 * Revision 1.12  2006/05/10 15:13:37  spr
 * Better error handling.
 *
 * Revision 1.11  2006/04/21 23:11:10  spr
 * Add color support for I/O.
 *
 * Revision 1.10  2005/10/31 19:21:16  spr
 * Add xml output conversion method (copied from IvyXmlWriter)
 *
 * Revision 1.9  2005/07/08 20:58:15  spr
 * Check for null text.
 *
 * Revision 1.8  2005/05/07 22:25:45  spr
 * Updates for java 5.0
 *
 * Revision 1.7  2005/01/05 15:31:02  spr
 * Fix error message and formatting.
 *
 * Revision 1.6  2004/12/20 15:34:57  spr
 * Better handling of XML formatting of XML messages (omit extra white space)./
 *
 * Revision 1.5  2004/11/09 20:33:06  spr
 * Fix up bugs we introduced into xml scanner.
 *
 * Revision 1.4  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.3  2003/09/24 13:29:01  spr
 * Update parser for 1.4 with large number of elements; add cdata calls to writer.
 *
 * Revision 1.2  2003/05/30 16:11:49  spr
 * Change format on error output.
 *
 * Revision 1.1.1.1  2003/02/14 20:23:15  spr
 * Initial version of the common code for various Brown projects.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Color;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Vector;


public class IvyXmlWriter extends PrintWriter
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Writer		base_writer;
private Vector<String>	element_stack;
private int		open_state;
private String		indent_string;
private boolean 	single_line;
private String		name_space;
private String		namespace_ref;
private boolean 	namespace_field;

private static final int	STATE_DONE = 0;
private static final int	STATE_OPEN = 1;
private static final int	STATE_CLOSED = 2;
private static final int	STATE_TEXT = 3;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public IvyXmlWriter(Writer w)
{
   super(w);

   base_writer = w;

   element_stack = new Vector<String>();
   indent_string = "  ";
   open_state = STATE_DONE;
   single_line = false;
   name_space = null;
   namespace_ref = null;
   namespace_field = false;
}



public IvyXmlWriter(OutputStream os) throws IOException
{
   this(new OutputStreamWriter(os,"UTF-8"));
}



public IvyXmlWriter(String f) throws IOException
{
   this(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
}



public IvyXmlWriter(File f) throws IOException
{
   this(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
}



public IvyXmlWriter()
{
   this(new StringWriter());
}




/********************************************************************************/
/*										*/
/*	Control methods 							*/
/*										*/
/********************************************************************************/

public void setSingleLine(boolean fg)		{ single_line = fg; }

public void setIndentString(String s)		{ indent_string = s; }

@Override public String toString()
{
   return base_writer.toString();
}

public String closeResult()
{
   String rslt = base_writer.toString();
   close();
   return rslt;
}


public void setNameSpace(String ns,String ref)
{
   name_space = ns;
   namespace_ref = ref;
   namespace_field = false;
}


public int getLength()
{
   if (base_writer instanceof StringWriter) {
      StringWriter sw = (StringWriter) base_writer;
      StringBuffer sb = sw.getBuffer();
      return sb.length();
    }

   return -1;
}




/********************************************************************************/
/*										*/
/*	Xml Element methods							*/
/*										*/
/********************************************************************************/

synchronized public void outputHeader()
{
   println("<?xml version='1.0' encoding='utf-8' ?>");
}



synchronized public void outputComment(String cmmt)
{
   println();
   println("<!-- " + cmmt + "-->");
   println();
}




synchronized public void begin(String elt)
{
   if (name_space != null) elt = name_space + ":" + elt;

   switch (open_state) {
      case STATE_DONE :
	 break;
      case STATE_OPEN :
	 print(">");
	 if (!single_line) println();
	 break;
      case STATE_CLOSED :
	 break;
    }

   indent();
   print("<");
   print(elt);
   open_state = STATE_OPEN;
   element_stack.addElement(elt);

   if (name_space != null && !namespace_field) {
      field("xmlns:" + name_space,namespace_ref);
      namespace_field = true;
    }
}



synchronized public void end()
{
   int ln = element_stack.size();
   if (ln == 0) throw new Error("End with no corresponding begin");
   String elt = element_stack.lastElement();
   element_stack.setSize(ln-1);

   if (open_state == STATE_DONE) return;
   else if (open_state == STATE_OPEN) {
      print(" />");
    }
   else if (elt != null) {
      if (open_state != STATE_TEXT) indent();
      print("</");
      print(elt);
      print(">");
    }
   if (!single_line || ln == 1) println();
   if (ln == 1) open_state = STATE_DONE;
   else open_state = STATE_CLOSED;
}




synchronized public void end(String telt)
{
   int ln = element_stack.size();
   if (ln == 0) throw new Error("End with no corresponding begin");
   String elt = element_stack.lastElement();
   if (name_space != null) telt = name_space + ":" + telt;
   if (!elt.equals(telt)) throw new Error("End " + telt + " attempted when " + elt + " was open");
   end();
}




synchronized public void field(String elt,String val)
{
   if (open_state != STATE_OPEN) {
      throw new Error("Field must be specified inside an element");
    }

   print(" ");
   print(elt);
   print("='");
   IvyXml.outputXmlString(val,this);
   print("'");
}



synchronized public void field(String elt,char [] data)
{
   if (data == null) return;

   field(elt,new String(data));
}



synchronized public void field(String elt,boolean fg)
{
   field(elt,String.valueOf(fg));
}




synchronized public void field(String elt,short v)
{
   field(elt,String.valueOf(v));
}




synchronized public void field(String elt,int v)
{
   field(elt,String.valueOf(v));
}




synchronized public void field(String elt,long v)
{
   field(elt,Long.toString(v));
}




synchronized public void field(String elt,double v)
{
   field(elt,String.valueOf(v));
}



synchronized public void field(String elt,Enum<?> v)
{
   if (v != null) field(elt,v.toString());
}



synchronized public void field(String elt,Color c)
{
   if (c == null) field(elt,(String) null);
   else {
      int cv = c.getRGB();
      field(elt,"0x" + Integer.toHexString(cv));
    }
}


synchronized public void field(String elt,Date d)
{
   if (d == null) field(elt,(long) -1);
   else {
      long t = d.getTime();
      field(elt,t);
    }
}



synchronized public void field(String elt,Object o)
{
   if (o != null) field(elt,o.toString());
}



synchronized public void emptyElement(String elt)
{
   begin(elt);
   end(elt);
}



synchronized public void textElement(String elt,String t)
{
   begin(elt);
   if (t != null) text(t);
   end(elt);
}



synchronized public void cdataElement(String elt,String t)
{
   begin(elt);
   if (t != null) {
      cdata(t);
    }
   end(elt);
}



synchronized public void textElement(String elt,Object t)
{
   begin(elt);
   if (t != null) text(t.toString());
   end(elt);
}



synchronized public void cdataElement(String elt,Object t)
{
   if (t != null) cdataElement(elt,t.toString());
   else cdataElement(elt,null);
}



synchronized public void bytesElement(String elt,byte [] data)
{
   begin(elt);
   if (data != null) text(IvyXml.byteArrayToString(data));
   end(elt);
}


synchronized public void bytesElement(String elt,byte [] data,int off,int len)
{
   begin(elt);
   if (data != null) text(IvyXml.byteArrayToString(data,off,len));
   end(elt);
}



synchronized public void text(String t)
{
   if (t == null) return;

   if (open_state == STATE_OPEN) {
      print(">");
      open_state = STATE_TEXT;
    }
   IvyXml.outputXmlString(t,this);
}



synchronized public void cdata(String t)
{
   if (t == null) return;

   t = IvyXml.cdataSanitize(t);

   if (open_state == STATE_OPEN) {
      print(">");
      open_state = STATE_TEXT;
    }
   print("<![CDATA[");
   print(t);
   print("]]>");
}



synchronized public void xmlText(String t)
{
   if (t == null) return;

   switch (open_state) {
      case STATE_DONE :
	 break;
      case STATE_OPEN :
	 print(">");
	 if (!single_line) println();
	 break;
      case STATE_CLOSED :
	 break;
    }
   open_state = STATE_TEXT;

   print(t);
}



private void indent()
{
   if (single_line) return;
   int ln = element_stack.size();
   for (int i = 0; i < ln; ++i) print(indent_string);
}







/********************************************************************************/
/*										*/
/*	Methods to dump an XML subtree						*/
/*										*/
/********************************************************************************/

synchronized public void writeXml(Node n)
{
   if (n == null) return;
   if (n.getNodeType() == Node.ELEMENT_NODE) {
      begin(n.getNodeName());
      NamedNodeMap nnm = n.getAttributes();
      for (int i = 0; i < nnm.getLength(); ++i) {
	 Node atr = nnm.item(i);
	 if (atr.getNodeType() == Node.ATTRIBUTE_NODE) {
	    Attr a = (Attr) atr;
	    if (a.getSpecified()) field(a.getName(),a.getValue());
	  }
       }
      NodeList nl = n.getChildNodes();
      for (int i = 0; i < nl.getLength(); ++i) {
	 Node c = nl.item(i);
	 writeXml(c);
       }
      end();
    }
   else if (n.getNodeType() == Node.TEXT_NODE) {
      String s = n.getNodeValue();
      if (s != null && s.trim().length() > 0) text(s);
    }
   else if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
      String s = n.getNodeValue();
      if (s != null && s.trim().length() > 0) cdata(s);
    }
}




/********************************************************************************/
/*										*/
/*	Methods to serialize an object						*/
/*										*/
/********************************************************************************/

public void serializeBean(Object o)
{
   ByteArrayOutputStream bas = new ByteArrayOutputStream();
   XMLEncoder xec = new XMLEncoder(bas);
   xec.writeObject(o);
   xec.close();
   Element e = IvyXml.convertStringToXml(bas.toString());
   writeXml(e);
}




/********************************************************************************/
/*										*/
/*	Other access methods							*/
/*										*/
/********************************************************************************/

public Writer getBaseWriter()			{ return base_writer; }




}	// end of class IvyXmlWriter




/* end of IvyXmlWriter.java */

