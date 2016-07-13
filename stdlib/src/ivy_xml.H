/********************************************************************************/
/*										*/
/*		ivy_xml.H							*/
/*										*/
/*	C++ class interface to XML (vis Xerces) 				*/
/*										*/
/********************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2005, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_xml.H,v 1.16 2013/09/24 01:07:55 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_xml.H,v $
 * Revision 1.16  2013/09/24 01:07:55  spr
 * data format
 *
 * Revision 1.15  2009-04-13 23:39:32  spr
 * Fixes to allow dual compilation on the macs; xercesc 3.x
 *
 * Revision 1.14  2009-03-20 02:00:57  spr
 * Handle windows files/sockets/etc.
 *
 * Revision 1.13  2009-01-27 00:42:57  spr
 * Fix up hsh functions, xml output.
 *
 * Revision 1.12  2008-11-12 13:48:01  spr
 * Nicer string hash function, update xercesc
 *
 * Revision 1.11  2008-05-07 21:16:07  spr
 * C++ updates for modified libraries.
 *
 * Revision 1.10  2007-12-21 02:22:58  spr
 * Updates to compile on the mac
 *
 * Revision 1.9  2007-09-22 01:48:53  spr
 * Use newer version of xercesc.
 *
 * Revision 1.8  2007-08-10 02:12:03  spr
 * Cleanups for apple and x86_64./
 *
 * Revision 1.7  2007-05-12 00:35:44  spr
 * fix definitions for mac.
 *
 * Revision 1.6  2007-05-04 02:39:48  spr
 * Add xercesc namespace option for apple.
 *
 * Revision 1.5  2007-05-04 02:28:22  spr
 * Fix up import order and xerces version.
 *
 * Revision 1.4  2006-12-01 03:23:00  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.3  2006-11-09 00:33:59  spr
 * Clean up types and operations.
 *
 * Revision 1.2  2006-08-03 12:36:12  spr
 * Text cleanup.
 *
 * Revision 1.1  2005/06/28 17:22:12  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_XML_ALREADY_INCLUDED
#define IVY_XML_ALREADY_INCLUDED


#include <xercesc/dom/DOM.hpp>
#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/parsers/XercesDOMParser.hpp>
#include <xercesc/parsers/SAXParser.hpp>
#include <xercesc/framework/MemBufInputSource.hpp>
#include <xercesc/sax/HandlerBase.hpp>
#include <xercesc/sax/AttributeList.hpp>
#include <xercesc/util/XMLString.hpp>

#include <ivy_datatypes.H>
#include <ivy_string.H>

#include <iostream>
#include <fstream>
#include <sstream>


namespace Ivy {

#ifdef XERCESNS
using namespace XERCESNS;
#else
XERCES_CPP_NAMESPACE_USE
#endif


/************************************************************************/
/*									*/
/*	Forward Type Definitions					*/
/*									*/
/************************************************************************/


typedef DOMDocument *	  IvyXmlDocument;
typedef DOMNodeList *	  IvyXmlNodeList;
typedef DOMNamedNodeMap * IvyXmlNamedNodeMap;
typedef DOMNode *	  IvyXmlDomNode;


class IvyXmlChildIter;
class IvyXmlAttrIter;
class IvyXmlWriter;

typedef const XMLCh * const	IvyXmlConstText;
typedef XMLCh * 		IvyXmlText;



/************************************************************************/
/*									*/
/*	IvyXercesString -- xml string					*/
/*									*/
/************************************************************************/

class IvyXercesString {

private:
   XMLCh * xml_str;

public:
   IvyXercesString()				{ xml_str = NULL; }
   IvyXercesString(const char * str)		{ xml_str = XMLString::transcode(str); }
   IvyXercesString(const XMLCh * wstr)		{ xml_str = XMLString::replicate(wstr); }
   IvyXercesString(const string& c)		{ xml_str = XMLString::transcode(c.c_str()); }
   IvyXercesString(const IvyXercesString &copy) { xml_str = XMLString::replicate(copy.xml_str); }
   ~IvyXercesString()				{ if (xml_str != NULL) XMLString::release(&xml_str); }

   operator const XMLCh * () const		{ return xml_str; };

};	// end of class IvyXercesString





/************************************************************************/
/*									*/
/*	IvyXmlNode -- holder for a IvyXmlDomNode			*/
/*									*/
/************************************************************************/


class IvyXmlNode {

private:
   IvyXmlDomNode xml_node;

public:
   IvyXmlNode() 			{ xml_node = NULL; }
   IvyXmlNode(IvyXmlDomNode nd) 	{ xml_node = nd; }
   IvyXmlNode(DOMNode& nd)		{ xml_node = &nd; }
   ~IvyXmlNode()			{ }

   operator IvyXmlDomNode()		     { return xml_node; }
   operator const IvyXmlDomNode() const      { return xml_node; }

   inline Bool getAttrPresent(CStdString attr) const;
   inline StdString getAttrString(CStdString attr) const;
   inline Integer getAttrInt(CStdString attr,Integer dflt = -1) const;
   inline Float getAttrFloat(CStdString attr,Float dflt = -1) const;
   inline Bool getAttrBool(CStdString attr) const;

   inline StdString getText() const;

   inline Bool isElement() const;
   inline Bool isCData() const;
   inline Bool isText() const;
   inline Bool isAttr() const;

   inline StdString getNodeName() const;
   inline StdString getNodeValue() const;
   inline IvyXmlNode getNextSibling() const	{ return xml_node->getNextSibling(); }
   inline IvyXmlNode getParent() const	{ return xml_node->getParentNode(); }

   inline StdString convertToString() const;
   inline void writeXml(IvyXmlWriter&) const;

   int operator ==(const IvyXmlNode& n) const { return xml_node == n.xml_node; }
   int operator !=(const IvyXmlNode& n) const { return xml_node != n.xml_node; }

   int operator ==(CStdString s) const		{ return isElement() && getNodeName() == s; }
   int operator ==(ConstText s) const		{ return isElement() && getNodeName() == s; }

public:
   Bool isNull() const				{ return xml_node == NULL; }
   IvyXmlNodeList getChildNodes() const 	{ return xml_node->getChildNodes(); }
   int getNumChildren() const			{ return xml_node->getChildNodes()->getLength(); }
   IvyXmlNamedNodeMap getAttributes() const	{ return xml_node->getAttributes(); }

   inline IvyXmlChildIter getChildren(ConstText elt = NULL) const;
   inline IvyXmlAttrIter getAttrs() const;

   void clear() 				{ xml_node = IvyXmlDomNode(); }
};



/************************************************************************/
/*									*/
/*	IvyXmlChildIter -- iterator for children of a node		*/
/*									*/
/************************************************************************/


class IvyXmlChildIter {

private:
   IvyXmlNode current_node;
   ConstText restrict_to;

public:
   IvyXmlChildIter(IvyXmlDomNode nd,ConstText elt = NULL) {
      current_node = nd->getFirstChild();
      restrict_to = elt;
      checkNode();
    }
   IvyXmlChildIter(const IvyXmlChildIter& ci)	{ current_node = ci.current_node; }
   ~IvyXmlChildIter()				{ }

   Bool more() const					{ return !current_node.isNull(); }
   void next() {
      if (more()) {
	 current_node = current_node.getNextSibling();
	 checkNode();
       }
    }
   IvyXmlNode current() const			{ return current_node; }

   IvyXmlChildIter& operator =(const IvyXmlChildIter& ci) {
      current_node = ci.current_node;
      restrict_to = ci.restrict_to;
      return *this;
    }

   operator int() const 				{ return more(); }
   IvyXmlChildIter& operator ++()			{ next(); return *this; }
   IvyXmlNode operator * () const			{ return current(); }

private:
   void checkNode() {
      if (restrict_to == NULL) return;
      while (!current_node.isNull()) {
	 if (current_node.isElement() && current_node.getNodeName() == restrict_to) break;
	 current_node = current_node.getNextSibling();
       }
    }

};




/************************************************************************/
/*									*/
/*	IvyXmlAttrIter -- iterator over attributes of a node		*/
/*									*/
/************************************************************************/


class IvyXmlAttrIter {

private:
   IvyXmlNamedNodeMap attr_map;
   int cur_index;
   int map_length;

public:
   IvyXmlAttrIter(IvyXmlNode n) {
      attr_map = n.getAttributes();
      map_length = attr_map->getLength();
      cur_index = 0;
    }
   IvyXmlAttrIter(const IvyXmlAttrIter& ai) {
      attr_map = ai.attr_map;
      map_length = ai.map_length;
      cur_index = ai.cur_index;
    }
   ~IvyXmlAttrIter()					{ }

   Bool more() const					{ return cur_index < map_length; }
   void next()						{ ++cur_index; }
   IvyXmlNode current() 				{ return attr_map->item(cur_index); }

   IvyXmlNode getNamedItem(CStdString id) {
      return attr_map->getNamedItem(IvyXercesString(id));
    }

   IvyXmlAttrIter& operator =(const IvyXmlAttrIter& ai) {
      attr_map = ai.attr_map;
      map_length = ai.map_length;
      cur_index = ai.cur_index;
      return *this;
    }
   operator int() const 				{ return more(); }
   IvyXmlAttrIter& operator++ ()			{ next(); return *this; }
   IvyXmlNode operator * ()				{ return current(); }

   StdString currentName() {
      IvyXmlDomNode dn = attr_map->item(cur_index);
      DOMAttr * a = (DOMAttr *) dn;
      Text t = XMLString::transcode(a->getName());
      StdString s = t;
      ::free(t);
      return s;
    }
   bool currentSpecified() {
      IvyXmlDomNode dn = attr_map->item(cur_index);
      DOMAttr * a = (DOMAttr *) dn;
      return a->getSpecified();
    }
};




/************************************************************************/
/*									*/
/*	IvyXmlSaxHandler -- general purpose callback handler		*/
/*									*/
/************************************************************************/


class IvyXmlSaxHandler : public HandlerBase {

public:
   static StdString convertToString(IvyXmlConstText t) {
      if (t == NULL) return string();

      IvyXmlText src = (IvyXmlText) t;
      char buf[10240];
      for (int i = 0; i < 10230; ++i) {
	 buf[i] = *src++;
	 if (buf[i] == 0) break;
       }
      return buf;
    }

   static StdString convertToString(IvyXmlConstText t,int ln) {
      if (t == NULL) return string();

      IvyXmlText src = (IvyXmlText) t;
      char buf[10240];
      if (ln >= 10239) ln = 10239;
      for (int i = 0; i < ln; ++i) {
	 buf[i] = *src++;
       }
      buf[ln] = 0;
      return buf;
    }

   virtual bool getDoValidation() const 	{ return false; }

   inline virtual void parse(CStdString file);
};




/************************************************************************/
/*									*/
/*	IvyXmlAttributeList -- holder for an attribute list		*/
/*									*/
/************************************************************************/


class IvyXmlAttributeList {

private:
   const AttributeList& attr_list;

public:
   IvyXmlAttributeList(const AttributeList& al) : attr_list(al) { }
   ~IvyXmlAttributeList()						{ }

   int getLength() const			{ return attr_list.getLength(); }

   StdString getName(int idx) const {
      return IvyXmlSaxHandler::convertToString(attr_list.getName(idx));
    }

   StdString getType(int idx) const {
      return IvyXmlSaxHandler::convertToString(attr_list.getType(idx));
    }

   StdString getValue(int idx) const {
      return IvyXmlSaxHandler::convertToString(attr_list.getValue(idx));
    }

   StdString getValue(CStdString name) const {
      return IvyXmlSaxHandler::convertToString(attr_list.getValue(name.c_str()));
    }

   Bool getValueBool(CStdString name) const {
      StdString v = getValue(name);
      if (v.empty()) return false;
      if (v[0] == 'f' || v[0] == 'F' || v[0] == '0' || v[0] == 'n' || v[0] == 'N') return false;
      return true;
    }

   bool checkValue(CStdString name) const {
      return attr_list.getValue(name.c_str()) != NULL;
    }
};




/************************************************************************/
/*									*/
/*	IvyXml -- class holder for XML methods				*/
/*									*/
/************************************************************************/


class IvyXml {

private:

public:
   static IvyXmlDocument loadXmlDocumentFromFile(CStdString name) {
      initXml();

      XercesDOMParser * parser = new XercesDOMParser();
#if XERCES_VERSION_MAJOR >= 3
      parser->setValidationScheme(XercesDOMParser::Val_Never);
      parser->setCreateEntityReferenceNodes(true);
#else
      parser->setDoValidation(false);
      parser->setExpandEntityReferences(true);
#endif
      parser->setExitOnFirstFatalError(false);
      parser->setDoNamespaces(false);

      if (access(name,R_OK)) {
	 cerr << "IVY: can't open XML file " << name << endl;
       }
      else {
	 try {
	    parser->parse(name.c_str());
	  }
	 catch (const XMLException& exc) {
	    cerr << "IVY: error during XML parsing: " << exc.getMessage() << endl;
	  }
       }

      DOMDocument * doc = parser->getDocument();

      // delete parser;

      return doc;
    }

   static IvyXmlDomNode loadXmlFromFile(CStdString file) {
      IvyXmlDocument doc = loadXmlDocumentFromFile(file);

      if (doc == NULL) return NULL;

      return doc->getDocumentElement();
    }

   static IvyXmlDomNode convertStringToXml(CStdString txt) {
      initXml();

      XercesDOMParser * parser = new XercesDOMParser();
#if XERCES_VERSION_MAJOR >= 3
      parser->setValidationScheme(XercesDOMParser::Val_Never);
      parser->setCreateEntityReferenceNodes(true);
#else
      parser->setDoValidation(false);
      parser->setExpandEntityReferences(true);
#endif
      parser->setExitOnFirstFatalError(false);
      parser->setDoNamespaces(false);

      MemBufInputSource ins((XMLByte *) txt.c_str(),txt.length(),"IvyStringBuffer",false);

      try {
	 parser->parse(ins);
       }
      catch (const XMLException& exc) {
	 cerr << "IVY: error during XML parsing: " << exc.getMessage() << endl;
       }

      DOMDocument * doc = parser->getDocument();

      // delete parser;

      return doc->getDocumentElement();
    }

   static StdString getAttrString(const IvyXmlDomNode& frm,CStdString id) {
      StdString rslt;
      Text v = getAttrValue(frm,id);
      if (v != NULL) {
	 rslt = v;
	 delete v;
       }
      return rslt;
    }

   static bool getAttrPresent(const IvyXmlDomNode& frm,CStdString id) {
      Text v = getAttrValue(frm,id);
      if (v == NULL) return false;
      delete v;
      return true;
    }

   static Integer getAttrInt(const IvyXmlDomNode& frm,CStdString id,Integer dflt = -1) {
      Text v = getAttrValue(frm,id);
      if (v == NULL) return dflt;
      Integer iv = atoi(v);
      delete v;
      return iv;
    }

   static Float getAttrFloat(const IvyXmlDomNode& frm,CStdString id,Float dflt = -1) {
      Text v = getAttrValue(frm,id);
      if (v == NULL) return dflt;
      Float fv = atof(v);
      delete v;
      return fv;
    }

   static bool getAttrBool(const IvyXmlDomNode& frm,CStdString id) {
      Text v = getAttrValue(frm,id);
      if (v == NULL) return false;
      bool fg = true;
      if (v[0] == 'f' || v[0] == 'F' || v[0] == '0' || v[0] == 'n' || v[0] == 'N') fg = false;
      delete v;
      return fg;
    }

   static StdString getText(const IvyXmlDomNode& xml) {
      StdString rslt;

      if (xml->getNodeType() == DOMNode::TEXT_NODE ||
	     xml->getNodeType() == DOMNode::CDATA_SECTION_NODE) {
	 Text v = XMLString::transcode(xml->getNodeValue());
	 Text p = v;
	 while (isspace(*p)) ++p;
	 int i = strlen(p);
	 if (i > 0) {
	    Text q = &p[i-1];
	    while (isspace(*q)) *q-- = 0;
	  }
	 rslt = p;
	 delete v;
       }
      else if (xml->getNodeType() == DOMNode::ELEMENT_NODE) {
	 IvyXmlNodeList nl = xml->getChildNodes();
	 for (int i = 0; ; ++i) {
	    IvyXmlDomNode nc = nl->item(i);
	    if (nc == NULL) break;
	    StdString s = getText(nc);
	    rslt += s;
	  }
       }

      return rslt;
    }

   static bool isElement(const IvyXmlDomNode& xml) {
      return xml != NULL && xml->getNodeType() == DOMNode::ELEMENT_NODE;
    }

   static bool isCData(const IvyXmlDomNode& xml) {
      return xml != NULL && xml->getNodeType() == DOMNode::CDATA_SECTION_NODE;
    }

   static bool isText(const IvyXmlDomNode& xml) {
      return xml != NULL && (xml->getNodeType() == DOMNode::CDATA_SECTION_NODE ||
				  xml->getNodeType() == DOMNode::TEXT_NODE);
    }

    static bool isAttr(const IvyXmlDomNode& xml) {
      return xml != NULL && xml->getNodeType() == DOMNode::ATTRIBUTE_NODE;
    }

  static StdString getNodeName(const IvyXmlDomNode& xml) {
      const XMLCh * xch = xml->getNodeName();
      Text t = XMLString::transcode(xch);
      StdString rslt = t;
      ::free(t);
      return rslt;
    }

   static StdString getNodeValue(const IvyXmlDomNode& xml) {
      Text t = XMLString::transcode(xml->getNodeValue());
      StdString rslt = t;
      ::free(t);
      return rslt;
    }

   static bool parseXmlFromFile(CStdString name,IvyXmlSaxHandler * hdlr,bool validate = true) {
      bool fg = true;

      initXml();

      SAXParser * parser = new SAXParser();
      parser->setEntityResolver(hdlr);
      parser->setDTDHandler(hdlr);
      parser->setDocumentHandler(hdlr);
      parser->setErrorHandler(hdlr);
#if XERCES_VERSION_MAJOR >= 3
      parser->setValidationScheme((validate ? SAXParser::Val_Auto : SAXParser::Val_Never));
#else
      parser->setDoValidation(validate);
#endif

      try {
	 parser->parse(name.c_str());
       }
      catch (const SAXException& exc) {
	 cerr << "IVY: error during XML parsing: " << hdlr->convertToString(exc.getMessage()) << endl;
	 fg = false;
       }
      catch (const XMLException& exc) {
	 cerr << "IVY: error during XML parsing: " << exc.getMessage() << endl;
	 fg = false;
       }

      // delete parser;

      return fg;
    }

   static bool parseXmlFromString(CStdString txt,IvyXmlSaxHandler * hdlr) {
      bool fg = true;

      initXml();

      SAXParser * parser = new SAXParser();
      parser->setEntityResolver(hdlr);
      parser->setDTDHandler(hdlr);
      parser->setDocumentHandler(hdlr);
      parser->setErrorHandler(hdlr);

      MemBufInputSource ins((XMLByte *) txt.c_str(),txt.length(),"IvyStringBuffer",false);

      try {
	 parser->parse(ins);
       }
      catch (const SAXException& exc) {
	 cerr << "IVY: error during XML parsing: " << hdlr->convertToString(exc.getMessage()) << endl;
	 fg = false;
       }
      catch (const XMLException& exc) {
	 cerr << "IVY: error during XML parsing: " << exc.getMessage() << endl;
	 fg = false;
       }

      return fg;
    }

private:
   static void initXml() {
      static bool xml_inited = false;

      if (xml_inited) return;
      xml_inited = true;

      try {
	 XMLPlatformUtils::Initialize();
       }
      catch (const XMLException& exc) {
	 cerr << "IVY: error during XML initialization: " << exc.getMessage() << endl;
	 ::exit(1);
       }
    }

   static Text getAttrValue(const IvyXmlDomNode& frm,CStdString id) {
      IvyXmlNamedNodeMap map = frm->getAttributes();
      IvyXmlDomNode n = map->getNamedItem(IvyXercesString(id));

      if (n == NULL) return NULL;
      if (n->getNodeType() != DOMNode::ATTRIBUTE_NODE) return NULL;

      DOMAttr * a = (DOMAttr *)(n);

      return XMLString::transcode(a->getValue());
    }

};	// end of class IvyXml





/************************************************************************/
/*									*/
/*	IvyXmlWriter -- output stream for generating XML		*/
/*									*/
/************************************************************************/


class IvyXmlWriter {

private:
   enum State { DONE, OPEN, CLOSED, TEXT };

   List<string> element_stack;
   State open_state;
   string indent_string;

protected:
   IvyXmlWriter()					{ setup(); }
   virtual ~IvyXmlWriter()				{ }

public:
   void begin(CStdString id) {
      if (open_state == OPEN) output(">\n");
      indent();
      output("<");
      output(id);
      open_state = OPEN;
      element_stack.pushBack(id);
    }

   void end() {
      int ln = element_stack.size();
      if (ln == 0) return;
      string elt = element_stack.back();
      element_stack.popBack();
      if (open_state == DONE) return;
      else if (open_state == OPEN) output(" />\n");
      else if (!elt.empty()) {
	 if (open_state != TEXT) indent();
	 output("</");
	 output(elt);
	 output(">\n");
       }
      if (ln == 1) open_state = DONE;
      else open_state = CLOSED;
    }

   void end(CStdString id) {
      for ( ; ; ) {
	 if (element_stack.size() == 0) break;
	 string elt = element_stack.back();
	 end();
	 if (id.empty() || elt == id) break;
       }
    }

   void field(CStdString elt,CStdString val) {
      if (open_state != OPEN) return;
      output(" ");
      output(elt);
      output("='");
      outputXmlString(val);
      output("'");
    }

   void field(CStdString elt,ConstText val) {
      if (val == NULL) return;
      if (open_state != OPEN) return;
      output(" ");
      output(elt);
      output("='");
      outputXmlString(val);
      output("'");
    }

   void field(CStdString elt,bool fg) {
      if (open_state != OPEN) return;
      output(" ");
      output(elt);
      output("='");
      output((fg ? "T" : "F"));
      output("'");
    }

   void field(CStdString elt,int v) {
      if (open_state != OPEN) return;
      char buf[1024];
      sprintf(buf," %s='%d'",elt.c_str(),v);
      output(buf);
    }

   void field(CStdString elt,long v) {
      if (open_state != OPEN) return;
      char buf[1024];
      sprintf(buf," %s='%ld'",elt.c_str(),v);
      output(buf);
    }

   void field(CStdString elt,unsigned int v) {
      if (open_state != OPEN) return;
      char buf[1024];
      sprintf(buf," %s='%u'",elt.c_str(),v);
      output(buf);
    }

   void field(CStdString elt,unsigned long v) {
      if (open_state != OPEN) return;
      char buf[1024];
      sprintf(buf," %s='%lu'",elt.c_str(),v);
      output(buf);
    }

   void field(CStdString elt,double v) {
      if (open_state != OPEN) return;
      char buf[1024];
      sprintf(buf," %s='%g'",elt.c_str(),v);
      output(buf);
    }

   void field(CStdString elt,char key,long id) {
      if (open_state != OPEN) return;
      char buf[1024];
      sprintf(buf," %s='%c%ld'",elt.c_str(),key,id);
      output(buf);
    }

   void field(CStdString elt,LongLong v) {
      if (open_state != OPEN) return;
      char buf[1024];
      sprintf(buf," %s='%lld'",elt.c_str(),v);
      output(buf);
    }

   void field(CStdString elt,ULongLong v) {
      if (open_state != OPEN) return;
      char buf[1024];
      sprintf(buf," %s='%llu'",elt.c_str(),v);
      output(buf);
    }

   void setIndentString(CStdString s)				{ indent_string = s; }

   void text(CStdString t) {
      if (open_state == OPEN) {
	 output(">");
	 open_state = TEXT;
       }
      outputXmlString(t);
    }

   void write(IvyXmlNode xn) {
      xn.writeXml(*this);
    }

protected:
   virtual void output(CStdString s) = 0;
   virtual void output(const char *) = 0;
   virtual void output(char) = 0;

private:
   void setup() {
      indent_string = "  ";
      open_state = DONE;
    }

   void indent() {
      int ln = element_stack.size();
      for (int i = 0; i < ln; ++i) output(indent_string);
    }

   void outputXmlString(CStdString v) {
      char c;
      for (int i = 0; (c = v[i]) != 0; ++i) {
	 switch (c) {
	    case '&' :
	       output("&amp;");
	       break;
	    case '<' :
	       output("&lt;");
	       break;
	    case '>' :
	       output("&gt;");
	       break;
	    case '"' :
	       output("&quot;");
	       break;
	    case '\'' :
	       output("&apos;");
	       break;
	    default :
	       output(c);
	       break;
	  }
       }
    }

};	// end of class IvyXmlWriter




/************************************************************************/
/*									*/
/*	IvyXmlWriter -- output stream for generating XML		*/
/*									*/
/************************************************************************/


class IvyXmlFileWriter : public ofstream, public IvyXmlWriter {

public:
   IvyXmlFileWriter()					    { }
   IvyXmlFileWriter(const char * f) : ofstream(f)	    { }
   IvyXmlFileWriter(const string& s) : ofstream(s.c_str())  { }

   using IvyXmlWriter::end;

protected:
   void output(CStdString v)			{ *this << v; }
   void output(const char * v)			{ *this << v; }
   void output(char v)				{ *this << v; }
};	// end of class IvyXmlFileWriter




/************************************************************************/
/*									*/
/*	IvyXmlStringWriter -- string stream for generating XML		*/
/*									*/
/************************************************************************/


class IvyXmlStringWriter : public ostringstream, public IvyXmlWriter {

public:
   IvyXmlStringWriter() : ostringstream(), IvyXmlWriter()	{ }

protected:
   void output(CStdString v)			{ *this << v; }
   void output(const char * v)			{ *this << v; }
   void output(char v)				{ *this << v; }

};	// end of class IvyXmlStringWriter




/************************************************************************/
/*									*/
/*	Inline definitions for IvyXmlNode				*/
/*									*/
/************************************************************************/

inline Bool
IvyXmlNode::getAttrPresent(CStdString attr) const
{
   return IvyXml::getAttrPresent(xml_node,attr);
}



inline StdString
IvyXmlNode::getAttrString(CStdString attr) const
{
   return IvyXml::getAttrString(xml_node,attr);
}



inline Integer
IvyXmlNode::getAttrInt(CStdString attr,Integer dflt) const
{
   return IvyXml::getAttrInt(xml_node,attr,dflt);
}



inline Float
IvyXmlNode::getAttrFloat(CStdString attr,Float dflt) const
{
   return IvyXml::getAttrFloat(xml_node,attr,dflt);
}



inline Bool
IvyXmlNode::getAttrBool(CStdString attr) const
{
   return IvyXml::getAttrBool(xml_node,attr);
}




inline Bool
IvyXmlNode::isElement() const
{
   return IvyXml::isElement(xml_node);
}


inline Bool
IvyXmlNode::isCData() const
{
   return IvyXml::isCData(xml_node);
}


inline Bool
IvyXmlNode::isText() const
{
   return IvyXml::isText(xml_node);
}


inline Bool
IvyXmlNode::isAttr() const
{
   return IvyXml::isAttr(xml_node);
}


inline StdString
IvyXmlNode::getNodeName() const
{
   return IvyXml::getNodeName(xml_node);
}



inline StdString
IvyXmlNode::getNodeValue() const
{
   return IvyXml::getNodeValue(xml_node);
}



inline StdString
IvyXmlNode::getText() const
{
   return IvyXml::getText(xml_node);
}



inline IvyXmlChildIter
IvyXmlNode::getChildren(ConstText elt) const
{
   return IvyXmlChildIter(xml_node,elt);
}



inline IvyXmlAttrIter
IvyXmlNode::getAttrs() const
{
   return IvyXmlAttrIter(xml_node);
}



inline StdString
IvyXmlNode::convertToString() const
{
   StringBuffer sbuf;

   if (isElement()) {
      sbuf.append("<");
      sbuf.append(getNodeName());
      for (IvyXmlAttrIter ai = getAttrs(); ai; ++ai) {
	 IvyXmlNode n = ai.current();
	 if (!n.isAttr()) return NULL;
	 if (ai.currentSpecified()) {
	    sbuf.append(" ");
	    sbuf.append(ai.currentName());
	    sbuf.append("='");
	    sbuf.append(n.getNodeValue());
	    sbuf.append("'");
	  }
       }
      if (getNumChildren() == 0) sbuf.append(" />");
      else {
	 sbuf.append(">");
	 for (IvyXmlChildIter ci = getChildren(); ci; ++ci) {
	    IvyXmlNode chld = *ci;
	    sbuf.append(chld.convertToString());
	  }
	 sbuf.append("</");
	 sbuf.append(getNodeName());
	 sbuf.append(">");
       }
    }
   else if (isText()) {
      sbuf.append(getNodeValue());
    }

   return sbuf.getString();
}



inline void
IvyXmlNode::writeXml(IvyXmlWriter& xw) const
{
   if (isElement()) {
      xw.begin(getNodeName());
      for (IvyXmlAttrIter ai = getAttrs(); ai; ++ai) {
	 IvyXmlNode n = ai.current();
	 if (n.isAttr() && ai.currentSpecified()) {
	    xw.field(ai.currentName(),n.getNodeValue());
	  }
       }
      for (IvyXmlChildIter ci = getChildren(); ci; ++ci) {
	 IvyXmlNode chld = *ci;
	 chld.writeXml(xw);
       }
      xw.end();
    }
   else if (isText()) {
      xw.text(getNodeValue());
    }
}



inline void
IvyXmlSaxHandler::parse(CStdString file)
{
   IvyXml::parseXmlFromFile(file,this,getDoValidation());
}




inline StdString
safeXmlOutput(CStdString v)
{
   StringBuffer buf;

   char c;
   for (int i = 0; (c = v[i]) != 0; ++i) {
      switch (c) {
	 case '&' :
	    buf << "&amp;";
	    break;
	 case '<' :
	    buf << "&lt;";
	    break;
	 case '>' :
	    buf << "&gt;";
	    break;
	 case '"' :
	    buf << "&quot;";
	    break;
	 case '\'' :
	    buf << "&apos;";
	    break;
	 default :
	    buf << c;
	    break;
       }
    }

   return buf.getString();
}




}	// end of namespace Ivy


#endif


/* end of ivy_xml.H */
