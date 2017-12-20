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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/xml/IvyXml.java,v 1.75 2017/12/20 20:36:57 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyXml.java,v $
 * Revision 1.75  2017/12/20 20:36:57  spr
 * Formatting
 *
 * Revision 1.74  2017/10/24 12:46:54  spr
 * Clean up.
 *
 * Revision 1.73  2017/08/04 12:43:13  spr
 * Add ant capability.
 *
 * Revision 1.72  2017/05/12 20:54:03  spr
 * Unsyncrhonize parser
 *
 * Revision 1.71  2016/05/10 13:43:21  spr
 * Clean up code.
 *
 * Revision 1.70  2016/03/22 13:10:34  spr
 * Handle null document.
 *
 * Revision 1.69  2016/01/14 16:57:21  spr
 * Add clone element, debugging code.
 *
 * Revision 1.68  2015/11/20 15:09:27  spr
 * Reformatting.
 *
 * Revision 1.67  2015/09/23 17:59:08  spr
 * Add minor features for output.
 *
 * Revision 1.66  2015/07/02 19:01:35  spr
 * Minor bug fixes
 *
 * Revision 1.65  2015/04/08 13:51:55  spr
 * Handle control characters
 *
 * Revision 1.64  2015/03/31 02:19:28  spr
 * Add getTextElements
 *
 * Revision 1.63  2015/02/14 18:46:52  spr
 * Fix formatting.
 *
 * Revision 1.62  2014/06/12 01:06:34  spr
 * Minor updates
 *
 * Revision 1.61  2014/01/22 00:31:35  spr
 * Handle escapes for cdata.
 *
 * Revision 1.60  2013/11/15 02:39:15  spr
 * Fix imports
 *
 * Revision 1.59  2013/09/24 01:07:54  spr
 * data format
 *
 * Revision 1.58  2012-08-29 01:40:56  spr
 * Code cleanup for new compiler.
 *
 * Revision 1.57  2012-08-19 01:03:02  spr
 * Cleanuo,
 *
 * Revision 1.56  2012-05-22 00:43:48  spr
 * Handle data input and output as fields.
 *
 * Revision 1.55  2012-02-29 01:54:00  spr
 * Code clean up.
 *
 * Revision 1.54  2012-01-12 01:28:25  spr
 * Minor code fixups.
 *
 * Revision 1.53  2011-09-12 20:50:44  spr
 * Add calls for formatting html.
 *
 * Revision 1.52  2011-08-19 23:10:33  spr
 * Code cleanup.
 *
 * Revision 1.51  2011-07-14 12:49:43  spr
 * Add support for partial byte arrays.
 *
 * Revision 1.50  2011-06-29 20:24:22  spr
 * Check for null stream.
 *
 * Revision 1.49  2011-06-29 01:58:43  spr
 * Handle null input streams.
 *
 * Revision 1.48  2011-05-27 19:32:52  spr
 * Change copyrights.
 *
 * Revision 1.47  2011-02-17 23:16:41  spr
 * Handle old versions of java without failing.
 *
 * Revision 1.46  2010-10-21 22:16:00  spr
 * Try to avoid synchronization problems by normalizing the resultant document.
 *
 * Revision 1.45  2010-10-09 02:22:56  spr
 * Top level call to sanitize non-field
 *
 * Revision 1.44  2010-10-08 19:39:10  spr
 * Don't special case tabs in text output.
 *
 * Revision 1.43  2010-10-07 20:10:19  spr
 * Better handling of special characters.
 *
 * Revision 1.42  2010-09-30 17:58:00  spr
 * Allow sanitizing null string
 *
 * Revision 1.41  2010-09-16 23:37:57  spr
 * Add string sanitizing routine.
 *
 * Revision 1.40  2010-08-14 00:29:16  spr
 * Handle possible exception.
 *
 * Revision 1.39  2010-07-24 02:01:27  spr
 * Fix findbugs issue.
 *
 * Revision 1.38  2010-06-01 02:09:06  spr
 * Force loading for dyvise monitoring of ivy-based apps.
 *
 * Revision 1.37  2010-05-18 22:05:48  spr
 * Update xml formatting.
 *
 * Revision 1.36  2010-04-29 18:46:47  spr
 * Handle newer version of java xml parser.
 *
 * Revision 1.35  2010-03-10 18:42:34  spr
 * Handle null elements in access methods.
 *
 * Revision 1.34  2009-09-17 02:01:29  spr
 * Code cleanup.
 *
 * Revision 1.33  2009-06-04 18:50:50  spr
 * Add string attribute with default method.
 *
 * Revision 1.32  2009-03-20 02:00:28  spr
 * Fix up error handling in reader; allow new parser possibility.
 *
 * Revision 1.31  2009-01-27 00:41:56  spr
 * Handle parser memory release.
 *
 * Revision 1.30  2008-06-11 01:46:53  spr
 * Remove unused code.
 *
 * Revision 1.29  2008-05-07 21:15:46  spr
 * Handle CDATA on input.	Children should iterate only locally.
 *
 * Revision 1.28  2008-03-14 12:28:00  spr
 * Handle namespaces.
 *
 * Revision 1.27  2007-11-06 00:22:43  spr
 * Debug checks.
 *
 * Revision 1.26  2007-08-10 02:11:29  spr
 * Cleanups from eclipse.
 *
 * Revision 1.25  2007-05-04 02:00:45  spr
 * Import fixups.
 *
 * Revision 1.24  2007-02-27 18:54:24  spr
 * Add load from File object.
 *
 * Revision 1.23  2006-12-01 03:22:58  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.22  2006/07/10 14:52:26  spr
 * Code cleanup.
 *
 * Revision 1.21  2006/04/21 23:11:10  spr
 * Add color support for I/O.
 *
 * Revision 1.20  2006/01/11 03:11:56  spr
 * Remove commented out code.
 *
 * Revision 1.19  2005/12/08 16:07:45  spr
 * Minor fixup/cleanups.  Hide close errors.
 *
 * Revision 1.18  2005/11/07 21:09:37  spr
 * Add getTextElement and stream parsing calls.
 *
 * Revision 1.17  2005/10/31 19:21:16  spr
 * Add xml output conversion method (copied from IvyXmlWriter)
 *
 * Revision 1.16  2005/09/02 14:43:03  spr
 * Add iterable collection for elementsByName.
 *
 * Revision 1.15  2005/07/23 01:10:14  spr
 * Change return type of parsing methods.
 *
 * Revision 1.14  2005/06/07 02:18:24  spr
 * Update for java 5.0
 *
 * Revision 1.13  2005/02/14 21:09:48  spr
 * Compute node length once (avoid n**2 loop) for large lists.
 *
 * Revision 1.12  2004/11/09 20:33:06  spr
 * Fix up bugs we introduced into xml scanner.
 *
 * Revision 1.11  2004/06/16 19:43:14  spr
 * Add default case of getAttrBool.
 *
 * Revision 1.10  2004/05/28 20:57:38  spr
 * Handle null cases.
 *
 * Revision 1.9  2004/05/20 16:03:55  spr
 * Add iterator over elements with a given tag and routine to return unique such.
 *
 * Revision 1.8  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.7  2003/12/17 21:24:24  spr
 * Set expansion limit to something very large.
 *
 * Revision 1.6  2003/09/24 13:29:00  spr
 * Update parser for 1.4 with large number of elements; add cdata calls to writer.
 *
 * Revision 1.5  2003/08/14 19:13:59  spr
 * Fix up string parsing a bit.  Ensure parsing is synchronized.  Add methods to
 * build xml structures.
 *
 * Revision 1.4  2003/08/04 13:06:54  spr
 * Add code for creating a document.
 *
 * Revision 1.3  2003/05/16 19:12:20  spr
 * Print stack trace on xml errors.
 *
 * Revision 1.2  2003/04/09 21:46:06  spr
 * Add calls for getting int/long attributes with user defaults.
 *
 * Revision 1.1.1.1  2003/02/14 19:48:25  spr
 * Initial version of the common code for various Brown projects.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.xml;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

import java.awt.Color;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;


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



public synchronized static Document convertStringToDocument(String s)
{
   return convertStringToDocument(s,false);
}


public static Document convertStringToDocument(String s,boolean nsa)
{
   if (s == null) return null;

   Document xdoc = null;
   XmlParser xp = null;
   try {
      // xp = (nsa ? ns_xml_parser : xml_parser);  // requires rtn to be synchronized
      if (xp == null) xp = new XmlParser(nsa);
    }
   catch (Throwable t) {
      System.err.println("IVY: Problem setting up parser: " + t);
      xp = (nsa ? ns_xml_parser : xml_parser);
    }

   StringReader sr = new StringReader(s);
   InputSource ins = new InputSource(sr);

   try {
      xdoc = xp.parse(ins);
      xp.reset();
    }
   catch (Throwable e) {
      System.err.println("IvyXML: parse error: " + e.getMessage());
      System.err.println("IvyXML: String: " + s);
      e.printStackTrace();
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
      System.err.println("IvyXML: write error: " + e.getMessage());
      return null;
    }

   return sw.toString();
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

   char c,c1,c2,c3,c4,c5;
   StringBuffer n = new StringBuffer(pcdata.length());

   for (int i = 0; i < pcdata.length(); i++) {
      c = pcdata.charAt(i);
      if (c == '&') {
	 c1 = lookAhead(1,i,pcdata);
	 c2 = lookAhead(2,i,pcdata);
	 c3 = lookAhead(3,i,pcdata);
	 c4 = lookAhead(4,i,pcdata);
	 c5 = lookAhead(5,i,pcdata);
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
	 else n.append("&");
       }
      else n.append(c);
    }

   return n.toString();
}



private final static char lookAhead(int la,int offset,String data)
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


public synchronized static Element loadXmlFromFile(String file,boolean nsa)
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




public synchronized static Element loadXmlFromFile(File file)
{
   return loadXmlFromFile(file,false);
}


public synchronized static Element loadXmlFromFile(File file,boolean nsa)
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




public synchronized static Element loadXmlFromURL(String url)
{
   return loadXmlFromURL(url,false);
}


public synchronized static Element loadXmlFromURL(String url,boolean nsa)
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
      e.printStackTrace();
    }

   return rslt;
}




public synchronized static Element loadXmlFromStream(InputStream inf)
{
   return loadXmlFromStream(inf,false);
}


public synchronized static Element loadXmlFromStream(InputStream inf,boolean nsa)
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



public synchronized static Element loadXmlFromReader(Reader inf)
{
   return loadXmlFromReader(inf,false,null);
}



public synchronized static Element loadXmlFromReader(Reader inf,boolean nsa)
{
   return loadXmlFromReader(inf,nsa,null);
}



private synchronized static Element loadXmlFromReader(Reader inf,boolean nsa,String src)
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
      e.printStackTrace();
    }

   return rslt;
}



/********************************************************************************/
/*										*/
/*	Routines for extracting attributes from XML nodes			*/
/*										*/
/********************************************************************************/

static public String getAttrString(Node frm,String id)
{
   return getAttrString(frm,id,null);
}



static public String getAttrString(Node frm,String id,String dflt)
{
   if (frm == null) return dflt;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return dflt;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return dflt;

   Attr a = (Attr) n;

   return a.getValue();
}



static public boolean getAttrPresent(Node frm,String id)
{
   if (frm == null) return false;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return false;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return false;

   return true;
}



static public Integer getAttrInteger(Node frm,String id)
{
   if (frm == null) return null;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return null;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return null;

   Attr a = (Attr) n;

   try {
      return new Integer(a.getValue());
    }
   catch (NumberFormatException e) { }

   return null;
}



static public boolean getAttrBool(Node frm,String id)	{ return getAttrBool(frm,id,false); }

static public boolean getAttrBool(Node frm,String id,boolean dv)
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



static public int getAttrInt(Node frm,String id)	{ return getAttrInt(frm,id,-1); }
static public int getAttrInt(Node frm,String id,int v)
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



static public long getAttrLong(Node frm,String id)	{ return getAttrLong(frm,id,-1); }
static public long getAttrLong(Node frm,String id,long v)
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



static public Float getAttrFloat(Node frm,String id)
{
   if (frm == null) return null;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return null;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return null;

   Attr a = (Attr) n;

   try {
      return new Float(a.getValue());
    }
   catch (NumberFormatException e) { }

   return null;
}



static public float getAttrFloat(Node frm,String id,float dflt)
{
   Float d = getAttrFloat(frm,id);
   if (d == null) return dflt;
   return d.floatValue();
}




static public Double getAttrDouble(Node frm,String id)
{
   if (frm == null) return null;

   NamedNodeMap map = frm.getAttributes();
   Node n = map.getNamedItem(id);

   if (n == null) return null;
   if (n.getNodeType() != Node.ATTRIBUTE_NODE) return null;

   Attr a = (Attr) n;

   try {
      return new Double(a.getValue());
    }
   catch (NumberFormatException e) { }

   return null;
}



static public double getAttrDouble(Node frm,String id,double dflt)
{
   Double d = getAttrDouble(frm,id);
   if (d == null) return dflt;
   return d.doubleValue();
}




static public Color getAttrColor(Node frm,String id)
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



static public Color getAttrColor(Node frm,String id,Color dflt)
{
   Color r = getAttrColor(frm,id);
   if (r == null) r = dflt;
   return r;
}


static public Date getAttrDate(Node frm,String id)
{
   long v = getAttrLong(frm,id);
   if (v < 0) return null;
   return new Date(v);
}



@SuppressWarnings("unchecked")
static public <T extends Enum<T>> T getAttrEnum(Node frm,String id,T dflt)
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


static public <T extends Enum<T>> EnumSet<T> getAttrEnumSet(Node frm,String id,
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



/********************************************************************************/
/*										*/
/*	Methods to check node types						*/
/*										*/
/********************************************************************************/

static public boolean isElement(Node xml)
{
   return xml != null && xml.getNodeType() == Node.ELEMENT_NODE;
}



static public boolean isElement(Node xml,String id)
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



static public Iterator<Element> getChildren(Node xml)
{
   return new NodeIterator(xml,null);
}



static public Iterable<Element> children(Node xml)
{
   return new NodeIterator(xml,null);
}



static public Iterator<Element> getChildren(Node xml,String e)
{
   return new NodeIterator(xml,e);
}


static public Element getChild(Node xml,String c)
{
   if (xml == null) return null;

   for (Element e : children(xml,c)) {
      return e;
    }
   return null;
}



static public Iterable<Element> children(Node xml,String e)
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

static public Iterator<Element> getElementsByTag(Node n,String nm)
{
   return new NodeSpecIterator(n,nm);
}


static public Iterable<Element> elementsByTag(Node n,String nm)
{
   return new NodeSpecIterator(n,nm);
}



static public Element getElementByTag(Node n,String nm)
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

static public String getText(Node xml)
{
   return getText(xml,true);
}



static public String getText(Node xml,boolean trim)
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
      return r;
    }
   else if (xml.getNodeType() == Node.ELEMENT_NODE) {
      NodeList nl = xml.getChildNodes();

      if (nl == null) return null;

      StringBuffer buf = new StringBuffer();

      for (int i = 0; ; ++i) {
	 Node nc = nl.item(i);
	 if (nc == null) break;
	 String s = getText(nc);
	 if (s != null) buf.append(s);
       }

      if (buf.length() == 0) return null;

      return buf.toString();
    }

   return null;
}



static public String getTextElement(Node xml,String elt)
{
   if (xml == null) return null;

   String r = getAttrString(xml,elt);

   if (r == null) {
      Element e = getChild(xml,elt);
      if (e != null) r = getText(e);
    }

   return r;
}



static public List<String> getTextElements(Node xml,String elt)
{
   if (xml == null) return null;

   List<String> rslt = new ArrayList<String>();

   for (Element e : children(xml,elt)) {
      String txt = getText(e);
      if (txt != null) rslt.add(txt);
    }

   return rslt;
}




static public byte [] getBytesElement(Node xml,String elt)
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

static public Element cloneElement(String name,Element orig)
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

static public String byteArrayToString(byte [] byt)
{
   return byteArrayToString(byt,0,byt.length);
}



static public String byteArrayToString(byte [] byt,int off,int len)
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





static public byte [] stringToByteArray(String s)
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




/********************************************************************************/
/*										*/
/*	Create a parser 							*/
/*										*/
/********************************************************************************/

private static class XmlParser {

   DocumentBuilder parser_object;

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
	 parser_object = dbf.newDocumentBuilder();
       }
      catch (ParserConfigurationException e) {
	 System.err.println("IvyXML: Problem creating java xml parser: " + e.getMessage());
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
