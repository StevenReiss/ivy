/********************************************************************************/
/*										*/
/*		IvyXml.java							*/
/*										*/
/*	Utility methods for using XML inside of various Brown projects		*/
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
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.brown.cs.ivy.file.IvyLog;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


public class IvyXml
{


/********************************************************************************/
/*										*/
/*	Local Storage								*/
/*										*/
/********************************************************************************/

private static XmlParser	xml_parser;
private static XmlParser	ns_xml_parser;


static {
   try {
      xml_parser = new XmlParser(false);
      ns_xml_parser = new XmlParser(true);
    }
   catch (Throwable t) {
      System.err.println("IVY: Problem setting up parser: " + t);
      t.printStackTrace();
      System.exit(1);
    }

   new NodeSpecIterator(null,null);			// force load
   new NodeIterator(null,null);
}



/********************************************************************************/
/*										*/
/*	XML <-> String conversion methods					*/
/*										*/
/********************************************************************************/

public static Element convertStringToXml(String s)
{
   return convertStringToXml(s,false);
}


public static Element convertStringToXml(String s,boolean nsa)
{
   Document xdoc = convertStringToDocument(s,nsa);

   if (xdoc == null) return null;

   return xdoc.getDocumentElement();
}



public static synchronized Document convertStringToDocument(String s)
{
   return convertStringToDocument(s,false);
}


public static Document convertStringToDocument(String s,boolean nsa)
{
   if (s == null || s.trim().length() == 0) return null;

   Document xdoc = null;
   XmlParser xp = null;
   try {
      // xp = (nsa ? ns_xml_parser : xml_parser);  // requires rtn to be synchronized
      if (xp == null) xp = new XmlParser(nsa);
    }
   catch (Throwable t) {
      IvyLog.logE("Problem setting up parser: " + t);
      xp = (nsa ? ns_xml_parser : xml_parser);
    }

   StringReader sr = new StringReader(s);
   InputSource ins = new InputSource(sr);

   try {
      xdoc = xp.parse(ins);
      xp.reset();
    }
   catch (Throwable e) {
      IvyLog.logE("IvyXml: parse error: " + e.getMessage(),e);
      IvyLog.logE("IvyXml: String: " + s);
    }

   try {
      if (xdoc != null) xdoc.normalizeDocument();
    }
   catch (Throwable t) { }

   return xdoc;
}



public static String convertXmlToString(Node xml)
{
   if (xml == null) return null;

   StringWriter sw = new StringWriter();

   try {
      addXml(xml,sw);
    }
   catch (IOException e) {
      System.err.println("IvyXml: write error: " + e.getMessage());
      return null;
    }

   return sw.toString();
}

public static String debugConvertXmlToString(Object v)
{
   if (v instanceof Node) {
      return convertXmlToString(((Node) v));
   }
   if (v instanceof Element) {
      return convertXmlToString(((Element) v));
   }
   return v.toString();
}


public static void addXml(Node n,Writer w) throws IOException
{
   if (n instanceof Element) {
      Element e = (Element) n;
      w.write("<" + e.getNodeName());
      NamedNodeMap nnm = e.getAttributes();
      for (int i = 0; ; ++i) {
	 Node na = nnm.item(i);
	 if (na == null) break;
	 Attr a = (Attr) na;
	 if (a.getSpecified()) {
	    w.write(" " + a.getName() + "='");
	    outputXmlString(a.getValue(),w);
	    w.write("'");
	  }
       }
      if (e.getFirstChild() == null) w.write(" />");
      else {
	 w.write(">");
	 for (Node cn = n.getFirstChild(); cn != null; cn = cn.getNextSibling()) {
	    addXml(cn,w);
	  }
	 w.write("</" + e.getNodeName() + ">");
       }
    }
   else if (n instanceof CDATASection) {
      w.write("<![CDATA[");
      w.write(n.getNodeValue());
      w.write("]]>");
    }
   else if (n instanceof Text) {
      String s = n.getNodeValue();
      if (s != null) outputXmlString(s,w);
    }
}



public static String xmlSanitize(String s)
{
   return xmlSanitize(s,true);
}



public static String htmlSanitize(String s)
{
   return xmlSanitize(s,true,true);
}



public static String xmlSanitize(String s,boolean fld)
{
   return xmlSanitize(s,fld,false);
}


public static String xmlSanitize(String s,boolean fld,boolean html)
{
   if (s == null) return null;

   StringWriter sw = new StringWriter();

   outputXmlString(s,fld,html,sw);

   return sw.toString();
}



public static void outputXmlString(String s,Writer pw)
{
   outputXmlString(s,false,false,pw);
}




public static void outputXmlString(String s,boolean field,boolean html,Writer pw)
{
   if (s == null) return;

   try {
      for (int i = 0; i < s.length(); ++i) {
	 char c = s.charAt(i);
	 switch (c) {
	    case '&' :
	       pw.write("&amp;");
	       break;
	    case '<' :
	       pw.write("&lt;");
	       break;
	    case '>' :
	       pw.write("&gt;");
	       break;
	    case '"' :
	       pw.write("&quot;");
	       break;
	    case '\'' :
	       if (html) pw.write("&#39;");
	       else pw.write("&apos;");
	       break;
	    case '\034' :
	       pw.write(";");
	       break;
	    default :
	       if ((c < 32 || c >= 128) && (field || (c != '\n' && c != '\r' && c != '\t'))) {
		  String d = Integer.toString(c,16);
		  if (d.length() == 1) d = "0"+d;
		  pw.write("&#");
		  if (!html) pw.write("x");
		  else {
		     // pw.write("x");
		     // while (d.length() < 4) d = "0" + d;
		  }
		  pw.write(d);
		  pw.write(";");
		}
	       else pw.write(c);
	       break;
	  }
       }
    }
   catch (IOException e) { }
}



/**
 * Make CDATA out of possibly encoded PCDATA. <br>
 * E.g. make '&' out of '&amp;'
 */

public static String decodeXmlString(String pcdata)
{
   if (pcdata == null) return null;

   char c;
   char c1;
   char c2;
   char c3;
   char c4;
   char c5;
   char c6;
   char c7;

   StringBuffer n = new StringBuffer(pcdata.length());

   for (int i = 0; i < pcdata.length(); i++) {
      c = pcdata.charAt(i);
      if (c == '&') {
	 c1 = lookAhead(1,i,pcdata);
	 c2 = lookAhead(2,i,pcdata);
	 c3 = lookAhead(3,i,pcdata);
	 c4 = lookAhead(4,i,pcdata);
	 c5 = lookAhead(5,i,pcdata);
	 c6 = lookAhead(6,i,pcdata);
	 c7 = lookAhead(7,i,pcdata);
	 if (c1 == 'a' && c2 == 'm' && c3 == 'p' && c4 == ';') {
	    n.append("&");
	    i += 4;
	  }
	 else if (c1 == 'l' && c2 == 't' && c3 == ';') {
	    n.append("<");
	    i += 3;
	  }
	 else if (c1 == 'g' && c2 == 't' && c3 == ';') {
	    n.append(">");
	    i += 3;
	  }
	 else if (c1 == 'q' && c2 == 'u' && c3 == 'o' && c4 == 't' && c5 == ';') {
	    n.append("\"");
	    i += 5;
	  }
	 else if (c1 == 'a' && c2 == 'p' && c3 == 'o' && c4 == 's' && c5 == ';') {
	    n.append("'");
	    i += 5;
	  }
	 else if (c1 == '#' && c2 == 'x' && c5 == ';') {
	    char [] chrs = new char[] { c3,c4 };
	    String sc = new String(chrs);
	    char c0 = (char) Integer.parseInt(sc,16);
	    n.append(c0);
	    i += 5;
	  }
	 else if (c1 == '#' && c4 == ';') {
	    char [] chrs = new char[] { c2,c3 };
	    String sc = new String(chrs);
	    char c0 = (char) Integer.parseInt(sc,16);
	    n.append(c0);
	    i += 4;
	  }
	 else if (c1 == '#' && c2 == 'x' && c6 == ';') {
	    char [] chrs = new char[] { c3,c4,c5 };
	    String sc = new String(chrs);
	    char c0 = (char) Integer.parseInt(sc,16);
	    n.append(c0);
	    i += 6;
	  }
	 else if (c1 == '#' && c2 == 'x' && c7 == ';') {
	    char [] chrs = new char[] { c3,c4,c5,c6 };
	    String sc = new String(chrs);
	    char c0 = (char) Integer.parseInt(sc,16);
	    n.append(c0);
	    i += 7;
	  }
	 else n.append("&");
       }
      else n.append(c);
    }

   return n.toString();
}



private static char lookAhead(int la,int offset,String data)
{
   if (offset + la >= data.length()) return 0;

   try {
      return data.charAt(offset + la);
    }
   catch (StringIndexOutOfBoundsException e) {
      return 0x0;
    }
}



public static String cdataSanitize(String s)
{
   if (s == null) return null;

   s = s.replace("]]>","@@@]@@@]@@@>");

   StringBuffer buf = null;

   for (int i = 0; i < s.length(); ++i) {
      char c = s.charAt(i);
      if ((c < 32 || c >= 128 || c < 0) && (c != '\n' && c != '\r' && c != '\t')) {
	 if (buf == null) {
	    buf = new StringBuffer();
	    buf.append(s.substring(0,i));
	  }
	 int cv = (c&0xffff);
	 String d = Integer.toString(cv,16);
	 if (d.length() == 1) d = "0"+d;
	 buf.append("&#x");
	 buf.append(d);
	 buf.append(";");
       }
      else if (buf != null) buf.append(c);
    }

   if (buf == null) return s;

   return buf.toString();
}


public static String cdataExpand(String s)
{
   s = s.replace("@@@]@@@]@@@>","]]>");
   if (s.contains("&#x")) {
      StringBuffer buf = new StringBuffer();
      int idx = -1;
      while (idx+1 < s.length()) {
	 int i = s.indexOf("&#x",idx+1);
	 if (i < 0) {
	    buf.append(s.substring(idx+1));
	    break;
	  }
	 buf.append(s.substring(idx+1,i));
	 int val = 0;
	 int k = -1;
	 for (int j = i+3; j < s.length(); ++j) {
	    char c = s.charAt(j);
	    if (c == ';' && j > i+3) {
	       buf.append((char) val);
	       k = j;
	       break;
	    }
	    else {
	       int v = Character.digit(c,16);
	       if (v < 0) {
		  break;
		}
	       else {
		  val = val*16 + v;
		}
	     }
	  }
	 if (k < 0) {
	    buf.append("&#x");
	    idx = i+2;
	  }
	 else {
	    idx = k;
	  }
       }
//    IvyLog.logD("IVYXML","CDATA EXPAND " + s + " " + buf);

      s = buf.toString();
    }

   return s;
}




/********************************************************************************/
/*										*/
/*	Methods to do XML file I/O						*/
/*										*/
/********************************************************************************/

public static Element loadXmlFromFile(String file)
{
   return loadXmlFromFile(file,false);
}


public static synchronized Element loadXmlFromFile(String file,boolean nsa)
{
   FileReader fr = null;

   if (file == null) return null;

   try {
      fr = new FileReader(file);
    }
   catch (FileNotFoundException e) {
      return null;
    }

   return loadXmlFromReader(fr,nsa,file);
}




public static synchronized Element loadXmlFromFile(File file)
{
   return loadXmlFromFile(file,false);
}


public static synchronized Element loadXmlFromFile(File file,boolean nsa)
{
   FileReader fr = null;

   if (file == null) return null;

   try {
      fr = new FileReader(file);
    }
   catch (FileNotFoundException e) {
      return null;
    }

   return loadXmlFromReader(fr,nsa,file.getPath());
}




public static synchronized Element loadXmlFromURL(String url)
{
   return loadXmlFromURL(url,false);
}


public static synchronized Element loadXmlFromURL(String url,boolean nsa)
{
   Element rslt = null;
   XmlParser xp = (nsa ? ns_xml_parser : xml_parser);

   try {
      Document xdoc = xp.parse(url);
      rslt = xdoc.getDocumentElement();
      xp.reset();
    }
   catch (SAXException e) {
      System.err.println("Ivy XML parse error for " + url + " : " + e.getMessage());
    }
   catch (IOException e) {
      System.err.println("Ivy XML I/O error for " + url + " : " + e.getMessage());
    }
   catch (Exception e) {
      System.err.println("Ivy XML error for " + url + " : " + e + " :: " + e.getMessage());
    }

   return rslt;
}




public static synchronized Element loadXmlFromStream(InputStream inf)
{
   return loadXmlFromStream(inf,false);
}


public static synchronized Element loadXmlFromStream(InputStream inf,boolean nsa)
{
   Element rslt = null;
   XmlParser xp = (nsa ? ns_xml_parser : xml_parser);
   if (inf == null) return null;

   try {
      InputSource ins = new InputSource(inf);
      Document xdoc = xp.parse(ins);
      inf.close();
      rslt = xdoc.getDocumentElement();
      xp.reset();
    }
   catch (SAXException e) {
      System.err.println("Ivy XML parse error for input stream: " + e.getMessage());
    }
   catch (IOException e) {
      System.err.println("Ivy XML I/O error for input stream: " + e.getMessage());
    }
   catch (Exception e) {
      System.err.println("Ivy XML error for input stream: " + e);
      e.printStackTrace();
    }

   return rslt;
}



public static synchronized Element loadXmlFromReader(Reader inf)
{
   return loadXmlFromReader(inf,false,null);
}



public static synchronized Element loadXmlFromReader(Reader inf,boolean nsa)
{
   return loadXmlFromReader(inf,nsa,null);
}



private static synchronized Element loadXmlFromReader(Reader inf,boolean nsa,String src)
{
   Element rslt = null;
   XmlParser xp = (nsa ? ns_xml_parser : xml_parser);

   if (src == null) src = inf.getClass().getName();

   try {
      InputSource ins = new InputSource(inf);
      Document xdoc = xp.parse(ins);
      inf.close();
      rslt = xdoc.getDocumentElement();
      xp.reset();
    }
   catch (SAXException e) {
      System.err.println("Ivy XML parse error for reader " + src + ": " + e.getMessage());
    }
   catch (IOException e) {
      System.err.println("Ivy XML I/O error for reader " + src + ": " + e.getMessage());
    }
   catch (Exception e) {
      System.err.println("Ivy XML error for reader " + src + ": " + e + " :: " + e.getMessage());
    }

   return rslt;
}



/********************************************************************************/
/*										*/
/*	Routines for extracting attributes from XML nodes			*/
/*										*/
/********************************************************************************/

public static String getAttrString(Node frm,String id)
{
   return getAttrString(frm,id,null);
}



public static String getAttrString(Node frm,String id,String dflt)
{
   if (frm == null) return dflt;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return dflt;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return dflt;

   Attr a = (Attr) n;

   return a.getValue();
}



public static boolean getAttrPresent(Node frm,String id)
{
   if (frm == null) return false;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return false;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return false;

   return true;
}



public static Integer getAttrInteger(Node frm,String id)
{
   if (frm == null) return null;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return null;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return null;

   Attr a = (Attr) n;

   try {
      return Integer.valueOf(a.getValue());
    }
   catch (NumberFormatException e) { }

   return null;
}



public static boolean getAttrBool(Node frm,String id)	{ return getAttrBool(frm,id,false); }

public static boolean getAttrBool(Node frm,String id,boolean dv)
{
   if (frm == null) return dv;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return dv;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return dv;

   Attr a = (Attr) n;

   String v = a.getValue();
   if (v.length() == 0) return true;

   char c = v.charAt(0);
   if (c == 'f' || c == 'F' || c == '0' || c == 'n' || c == 'N') return false;

   return true;
}



public static int getAttrInt(Node frm,String id)	{ return getAttrInt(frm,id,-1); }
public static int getAttrInt(Node frm,String id,int v)
{
   if (frm == null) return v;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return v;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return v;

   Attr a = (Attr) n;

   try {
      String s = a.getValue();
      int rdx = 10;
      if (s.startsWith("0x")) {
	 rdx = 16;
	 s = s.substring(2);
	 if (s.length() < 8) s = "0" + s;
       }
      else if (s.startsWith("#")) {
	 rdx = 16;
	 s = s.substring(1);
	 if (s.length() < 8) s = "0" + s;
       }
      else if (s.contains(".") || s.contains("E") || s.contains("e")) {
	 double d = getAttrDouble(frm,id,v);
	 return (int) d;
       }

      return (int) Long.parseLong(s,rdx);
    }
   catch (NumberFormatException e) { }

   return v;
}



public static long getAttrLong(Node frm,String id)	{ return getAttrLong(frm,id,-1); }
public static long getAttrLong(Node frm,String id,long v)
{
   if (frm == null) return v;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return v;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return v;

   Attr a = (Attr) n;

   try {
      return Long.parseLong(a.getValue());
    }
   catch (NumberFormatException e) { }

   return v;
}



public static Float getAttrFloat(Node frm,String id)
{
   if (frm == null) return null;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return null;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return null;

   Attr a = (Attr) n;

   try {
      return Float.valueOf(a.getValue());
    }
   catch (NumberFormatException e) { }

   return null;
}



public static float getAttrFloat(Node frm,String id,float dflt)
{
   Float d = getAttrFloat(frm,id);
   if (d == null) return dflt;
   return d.floatValue();
}




public static Double getAttrDouble(Node frm,String id)
{
   if (frm == null) return null;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return null;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return null;

   Attr a = (Attr) n;

   try {
      return Double.valueOf(a.getValue());
    }
   catch (NumberFormatException e) { }

   return null;
}



public static double getAttrDouble(Node frm,String id,double dflt)
{
   Double d = getAttrDouble(frm,id);
   if (d == null) return dflt;
   return d.doubleValue();
}




public static Color getAttrColor(Node frm,String id)
{
   String s = getAttrString(frm,id);
   if (s == null || s.length() == 0) return null;

   if (s.startsWith("0x") || s.startsWith("#") || Character.isDigit(s.charAt(0))) {
      int rgb = getAttrInt(frm,id);
      if ((rgb & 0xff000000) == 0) rgb |= 0xff000000;
      return new Color(rgb,true);
    }

   try {
      String s1 = s.toUpperCase();
      Field f = Color.class.getField(s1);
      Color c = (Color) f.get(null);
      return c;
    }
   catch (Exception e) { }

   return null;
}



public static Color getAttrColor(Node frm,String id,Color dflt)
{
   Color r = getAttrColor(frm,id);
   if (r == null) r = dflt;
   return r;
}


public static Date getAttrDate(Node frm,String id)
{
   long v = getAttrLong(frm,id);
   if (v < 0) return null;
   return new Date(v);
}



@SuppressWarnings("unchecked")
public static <T extends Enum<T>> T getAttrEnum(Node frm,String id,T dflt)
{
   if (frm == null) return dflt;

   Enum<?> v = dflt;
   String s = getAttrString(frm,id);
   if (s == null || s.length() == 0) return dflt;
   Object [] vals = dflt.getClass().getEnumConstants();
   if (vals == null) return null;
   for (int i = 0; i < vals.length; ++i) {
      Enum<?> e = (Enum<?>) vals[i];
      if (e.name().equals(s)) {
	 v = e;
	 break;
       }
    }

   return (T) v;
}


public static <T extends Enum<T>> EnumSet<T> getAttrEnumSet(Node frm,String id,
      Class<T> clazz,boolean dfltall)
{
   EnumSet<T> rslt = EnumSet.noneOf(clazz);
   if (frm == null || !getAttrPresent(frm,id)) {
      if (dfltall) rslt = EnumSet.allOf(clazz);
    }
   else {
      T [] vals = clazz.getEnumConstants();
      String s = getAttrString(frm,id);
      StringTokenizer tok = new StringTokenizer(s,",;: ");
      while (tok.hasMoreTokens()) {
	 String t = tok.nextToken();
	 boolean fnd = false;
	 for (int i = 0; i < vals.length; ++i) {
	    if (vals[i].name().equalsIgnoreCase(t)) {
	       rslt.add(vals[i]);
	       fnd = true;
	       break;
	     }
	  }
	 if (!fnd) {
	    System.err.println("IVY: XML: Enumeration element " + t + " not found");
	  }
       }
    }

   return rslt;
}


@SafeVarargs
public static <T extends Enum<T>> EnumSet<T> getAttrEnumSet(Node frm,String id,
      Class<T> clazz,T... dflts)
{
   EnumSet<T> rslt = EnumSet.noneOf(clazz);
   if (frm == null || !getAttrPresent(frm,id)) {
      for (T en : dflts) {
	 rslt.add(en);
       }
    }
   else {
      T [] vals = clazz.getEnumConstants();
      String s = getAttrString(frm,id);
      StringTokenizer tok = new StringTokenizer(s,",;: ");
      while (tok.hasMoreTokens()) {
	 String t = tok.nextToken();
	 boolean fnd = false;
	 for (int i = 0; i < vals.length; ++i) {
	    if (vals[i].name().equalsIgnoreCase(t)) {
	       rslt.add(vals[i]);
	       fnd = true;
	       break;
	     }
	  }
	 if (!fnd) {
	    System.err.println("IVY: XML: Enumeration element " + t + " not found");
	  }
       }
    }

   return rslt;
}



/********************************************************************************/
/*										*/
/*	Methods to check node types						*/
/*										*/
/********************************************************************************/

public static boolean isElement(Node xml)
{
   return xml != null && xml.getNodeType() == Node.ELEMENT_NODE;
}



public static boolean isElement(Node xml,String id)
{
   if (xml == null) return false;
   if (xml.getNodeType() != Node.ELEMENT_NODE) return false;
   if (id == null) return true;

   String s;
   try {
      s = xml.getNodeName();
    }
   catch (Throwable t) {
      s = xml.getNodeName();
    }

   if (id.equalsIgnoreCase(s)) return true;
   if (id.equalsIgnoreCase(xml.getLocalName())) return true;
   return false;
}



public static Iterator<Element> getChildren(Node xml)
{
   return new NodeIterator(xml,null);
}



public static Iterable<Element> children(Node xml)
{
   return new NodeIterator(xml,null);
}



public static Iterator<Element> getChildren(Node xml,String e)
{
   return new NodeIterator(xml,e);
}


public static Element getChild(Node xml,String c)
{
   if (xml == null) return null;

   for (Element e : children(xml,c)) {
      return e;
    }
   return null;
}



public static Iterable<Element> children(Node xml,String e)
{
   return new NodeIterator(xml,e);
}



private static class NodeIterator implements Iterable<Element>, Iterator<Element> {

   private Node cur_child;
   private String element_type;

   NodeIterator(Node xml,String et) {
      if (isElement(xml)) cur_child = xml.getFirstChild();
      else cur_child = null;
      element_type = et;

      while (cur_child != null && !isElement(cur_child,element_type)) {
	 cur_child = cur_child.getNextSibling();
       }
    }

   @Override public Iterator<Element> iterator()	{ return this; }

   @Override public boolean hasNext()		{ return cur_child != null; }

   @Override public Element next() throws NoSuchElementException {
      if (cur_child == null) throw new NoSuchElementException();
      Element rslt = (Element) cur_child;

      cur_child = cur_child.getNextSibling();
      while (cur_child != null && !isElement(cur_child,element_type)) {
	 cur_child = cur_child.getNextSibling();
       }

      return rslt;
    }

   @Override public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

}	// end of subclass NodeIterator





/********************************************************************************/
/*										*/
/*	Methods to iterator over subelements by tag				*/
/*										*/
/********************************************************************************/

public static Iterator<Element> getElementsByTag(Node n,String nm)
{
   return new NodeSpecIterator(n,nm);
}


public static Iterable<Element> elementsByTag(Node n,String nm)
{
   return new NodeSpecIterator(n,nm);
}



public static Element getElementByTag(Node n,String nm)
{
   for (Iterator<Element> it = getElementsByTag(n,nm); it.hasNext(); ) {
      Element e = it.next();
      return e;
    }

   return null;
}



private static class NodeSpecIterator implements Iterable<Element>, Iterator<Element> {

   private Node root_node;
   private Node cur_child;
   private String element_type;

   NodeSpecIterator(Node xml,String et) {
      root_node = xml;
      cur_child = xml;
      element_type = et;

      findNext();
      while (cur_child != null && !isElement(cur_child,element_type)) {
	 cur_child = cur_child.getNextSibling();
       }
    }

   @Override public Iterator<Element> iterator()	{ return this; }

   @Override public boolean hasNext()		{ return cur_child != null; }

   @Override public Element next() throws NoSuchElementException {
      if (cur_child == null) throw new NoSuchElementException();
      Element rslt = (Element) cur_child;

      findNext();

      return rslt;
    }

   @Override public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

   private void findNext() {
      while (cur_child != null) {
	 Node n = cur_child.getFirstChild();
	 if (n != null) cur_child = n;
	 else {
	    if (cur_child == root_node) {
	       cur_child = null;
	       return;
	     }
	    n = cur_child.getNextSibling();
	    if (n != null) cur_child = n;
	    else {
	       while (n == null) {
		  n = cur_child.getParentNode();
		  if (n == null || n == root_node) {
		     n = null;
		     break;
		   }
	     cur_child = n;
	     n = cur_child.getNextSibling();
		}
	       cur_child = n;
	     }
	  }
	 if (cur_child != null && isElement(cur_child,element_type)) return;
       }
    }

}	// end of subclass NodeSpecIterator





/********************************************************************************/
/*										*/
/*	Methods to get the string contents of an element			*/
/*										*/
/********************************************************************************/

public static String getText(Node xml)
{
   return getText(xml,true);
}



public static String getText(Node xml,boolean trim)
{
   if (xml == null) return null;
   if (xml.getNodeType() == Node.TEXT_NODE) {
      String r = xml.getNodeValue();
      if (trim && r != null) r = r.trim();
      return r;
    }
   else if (xml.getNodeType() == Node.CDATA_SECTION_NODE) {
      String r = xml.getNodeValue();
      r = cdataExpand(r);
      // DON'T TRIM CDATA if (trim && r != null) r = r.trim();
      return r;
    }
   else if (xml.getNodeType() == Node.ELEMENT_NODE) {
      NodeList nl = xml.getChildNodes();

      if (nl == null) return null;

      StringBuffer buf = new StringBuffer();

      for (int i = 0; ; ++i) {
	 try {
	    Node nc = nl.item(i);
	    if (nc == null) break;
	    String s = getText(nc);
	    if (s != null) buf.append(s);
	  }
	 catch (Throwable t) {
	    System.err.println("Some problem");
	  }
       }

      if (buf.length() == 0) return null;

      String rslt = buf.toString();
      if (trim) rslt = rslt.trim();

      return rslt;
    }

   return null;
}



public static String getTextElement(Node xml,String elt)
{
   if (xml == null) return null;

   String r = getAttrString(xml,elt);

   if (r == null) {
      Element e = getChild(xml,elt);
      if (e != null) r = getText(e);
    }

   return r;
}



public static List<String> getTextElements(Node xml,String elt)
{
   if (xml == null) return null;

   List<String> rslt = new ArrayList<String>();

   for (Element e : children(xml,elt)) {
      String txt = getText(e);
      if (txt != null) rslt.add(txt);
    }

   return rslt;
}




public static byte [] getBytesElement(Node xml,String elt)
{
   String s = getTextElement(xml,elt);

   if (s == null) return null;

   return stringToByteArray(s);
}




/********************************************************************************/
/*										*/
/*	Change element name							*/
/*										*/
/********************************************************************************/

public static Element cloneElement(String name,Element orig)
{
   Document doc = null;
   for (Node n = orig; n != null; n = n.getParentNode()) {
      if (n.getNodeType() == Node.DOCUMENT_NODE) {
	 doc = (Document) n;
	 break;
       }
    }
   if (name == null) name = orig.getNodeName();
   if (doc == null) return null;

   Element nelt = doc.createElement(name);
   NamedNodeMap nnm = orig.getAttributes();
   for (int i = 0; ; ++i) {
      Node na = nnm.item(i);
      if (na == null) break;
      Attr a = (Attr) na;
      nelt.setAttribute(a.getName(),a.getValue());
    }
   for (Node n = orig.getFirstChild(); n != null; n = n.getNextSibling()) {
      Node newsub = n.cloneNode(true);
      nelt.appendChild(newsub);
    }
   return nelt;
}




/********************************************************************************/
/*										*/
/*	Methods for manipulating XML elements					*/
/*										*/
/********************************************************************************/

public static Document newDocument()
{
   return xml_parser.newDocument();
}


public static Document newNSDocument()
{
   return ns_xml_parser.newDocument();
}



/********************************************************************************/
/*										*/
/*	Methods to handle arbitrary byte arrays using string			*/
/*										*/
/********************************************************************************/

public static String byteArrayToString(byte [] byt)
{
   return byteArrayToString(byt,0,byt.length);
}



public static String byteArrayToString(byte [] byt,int off,int len)
{
   StringBuffer sb = new StringBuffer();
   for (int i = off; i < len; ++i) {
      int v0 = byt[i];
      v0 &= 0xff;
      String s1 = Integer.toHexString(v0);
      if (s1.length() == 1) s1 = "0" + s1;
      sb.append(s1);
    }
   return sb.toString();
}





public static byte [] stringToByteArray(String s)
{
   if (s == null) return null;

   int ln = s.length();
   byte [] byt = new byte[ln/2];
   for (int i = 0; i < byt.length; ++i) {
      int c0 = Character.digit(s.charAt(2*i),16);
      int c1 = Character.digit(s.charAt(2*i+1),16);
      byt[i] = (byte) (((c0 & 0xf) << 4) + (c1 & 0xf));
    }
   return byt;
}


public static String decodeCharacters(String val)
{
   StringTokenizer tok = new StringTokenizer(val," ,;");
   return decodeCharacters(val,tok.countTokens());
}




public static String decodeCharacters(String val,int len)
{
   try {
      char[] buf = new char[len];
      StringTokenizer tok = new StringTokenizer(val," ,;");
      int i = 0;
      while (tok.hasMoreTokens()) {
	 String v = tok.nextToken();
	 buf[i++] = (char) Integer.parseInt(v);
       }
      val = new String(buf);
    }
   catch (NumberFormatException e) {
      IvyLog.logE("XML","Problem decoding characters: " + val,e);
    }

   return val;
}


public static String encodeCharacters(String txt)
{
   StringBuffer buf = new StringBuffer();
   for (int i = 0; i < txt.length(); ++i) {
      int ch = txt.charAt(i);
      if (i > 0) buf.append(",");
      buf.append(ch);
    }

   return buf.toString();
}


/********************************************************************************/
/*										*/
/*	Create a parser 							*/
/*										*/
/********************************************************************************/

private static class XmlParser {

   private DocumentBuilder parser_object;

   XmlParser(boolean ns) throws Exception {
      DocumentBuilderFactory dbf = null;
      try {
	 dbf = DocumentBuilderFactory.newInstance();
       }
      catch (Exception e) {
	 System.err.println("IVY XML: Problem creating document builder factory: " + e);
	 throw e;
       }
      dbf.setValidating(false);
      // dbf.setXIncludeAware(false);
      dbf.setNamespaceAware(ns);
      dbf.setIgnoringElementContentWhitespace(true);

      try {
	 System.setProperty("entityExpansionLimit","100000000");
       }
      catch (Throwable e) { }
      try {
	 dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
       }
      catch (Throwable e) { }
      try {
	 parser_object = dbf.newDocumentBuilder();
       }
      catch (ParserConfigurationException e) {
	 System.err.println("IvyXml: Problem creating java xml parser: " + e.getMessage());
	 System.exit(1);
       }
    }

   Document parse(InputSource is) throws Exception {
      return parser_object.parse(is);
    }

   Document parse(String uri) throws Exception {
      return parser_object.parse(uri);
    }

   Document newDocument() {
      return parser_object.newDocument();
    }

   void reset() {
      try {
	 parser_object.reset();
	 parse("<END/>");
       }
      catch (Throwable e) { }
    }

}	// end of subclass XmlParser




}	// end of class IvyXml



/* end of IvyXml.java */
