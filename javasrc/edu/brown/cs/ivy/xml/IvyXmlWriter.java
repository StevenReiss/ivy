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

public void clear()
{
   if (base_writer instanceof StringWriter) {
      StringWriter sw = (StringWriter) base_writer;
      StringBuffer sbuf = sw.getBuffer();
      sbuf.delete(0,sbuf.length());
    }
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

public synchronized void outputHeader()
{
   println("<?xml version='1.0' encoding='utf-8' ?>");
}



public synchronized void outputComment(String cmmt)
{
   println();
   println("<!-- " + cmmt + "-->");
   println();
}




public synchronized void begin(String elt)
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



public synchronized void end()
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




public synchronized void end(String telt)
{
   int ln = element_stack.size();
   if (ln == 0) throw new Error("End with no corresponding begin");
   String elt = element_stack.lastElement();
   if (name_space != null) telt = name_space + ":" + telt;
   if (!elt.equals(telt)) throw new Error("End " + telt + " attempted when " + elt + " was open");
   end();
}




public synchronized void field(String elt,String val)
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



public synchronized void field(String elt,char [] data)
{
   if (data == null) return;

   field(elt,new String(data));
}



public synchronized void field(String elt,boolean fg)
{
   field(elt,String.valueOf(fg));
}




public synchronized void field(String elt,short v)
{
   field(elt,String.valueOf(v));
}




public synchronized void field(String elt,int v)
{
   field(elt,String.valueOf(v));
}




public synchronized void field(String elt,long v)
{
   field(elt,Long.toString(v));
}




public synchronized void field(String elt,double v)
{
   field(elt,String.valueOf(v));
}



public synchronized void field(String elt,Enum<?> v)
{
   if (v != null) field(elt,v.toString());
}



public synchronized void field(String elt,Color c)
{
   if (c == null) field(elt,(String) null);
   else {
      int cv = c.getRGB();
      field(elt,"0x" + Integer.toHexString(cv));
    }
}


public synchronized void field(String elt,Date d)
{
   if (d == null) field(elt,(long) -1);
   else {
      long t = d.getTime();
      field(elt,t);
    }
}


public synchronized void field(String elt,File f)
{
   if (f != null) field(elt,f.getPath());
}



public synchronized void field(String elt,Object o)
{
   if (o != null) {
      if (o instanceof File) field(elt,(File) o);
      else if (o instanceof Color) field(elt,(Color) o);
      else if (o instanceof Date) field(elt,(Date) o);
      else if (o instanceof char []) field(elt,(char []) o);
      else field(elt,o.toString());
    }
}



public synchronized void emptyElement(String elt)
{
   begin(elt);
   end(elt);
}



public synchronized void textElement(String elt,String t)
{
   begin(elt);
   if (t != null) text(t);
   end(elt);
}



public synchronized void cdataElement(String elt,String t)
{
   begin(elt);
   if (t != null) {
      cdata(t);
    }
   end(elt);
}



public synchronized void textElement(String elt,Object t)
{
   begin(elt);
   if (t != null) text(t.toString());
   end(elt);
}



public synchronized void cdataElement(String elt,Object t)
{
   if (t != null) cdataElement(elt,t.toString());
   else cdataElement(elt,null);
}



public synchronized void bytesElement(String elt,byte [] data)
{
   begin(elt);
   if (data != null) text(IvyXml.byteArrayToString(data));
   end(elt);
}


public synchronized void bytesElement(String elt,byte [] data,int off,int len)
{
   begin(elt);
   if (data != null) text(IvyXml.byteArrayToString(data,off,len));
   end(elt);
}



public synchronized void text(String t)
{
   if (t == null) return;

   if (open_state == STATE_OPEN) {
      print(">");
      open_state = STATE_TEXT;
    }
   IvyXml.outputXmlString(t,this);
}



public synchronized void cdata(String t)
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



public synchronized void xmlText(String t)
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

public synchronized void writeXml(Node n)
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



































