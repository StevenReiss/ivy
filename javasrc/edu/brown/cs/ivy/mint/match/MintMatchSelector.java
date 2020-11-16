/********************************************************************************/
/*										*/
/*		MintMatchSelector.java						*/
/*										*/
/*	Implementation of a XML-based pattern matcher				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/match/MintMatchSelector.java,v 1.15 2018/08/02 15:10:29 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintMatchSelector.java,v $
 * Revision 1.15  2018/08/02 15:10:29  spr
 * Fix imports.
 *
 * Revision 1.14  2015/11/20 15:09:19  spr
 * Reformatting.
 *
 * Revision 1.13  2011-05-27 19:32:44  spr
 * Change copyrights.
 *
 * Revision 1.12  2010-10-19 22:01:02  spr
 * Add trace back on xml failures.
 *
 * Revision 1.11  2010-08-20 20:58:31  spr
 * Add logging and options for port numbers
 *
 * Revision 1.10  2010-08-14 00:28:59  spr
 * Add debugging check.
 *
 * Revision 1.9  2010-08-12 01:12:09  spr
 * Add debugging hook.
 *
 * Revision 1.8  2010-06-01 02:08:43  spr
 * Force load to handle dyvise monitoring of ivy-based apps.
 *
 * Revision 1.7  2010-04-29 18:46:25  spr
 * Handle null fields without aborting.
 *
 * Revision 1.6  2009-09-17 01:59:40  spr
 * Match XML nodes correctly.
 *
 * Revision 1.5  2009-03-20 01:58:23  spr
 * Catch unexpected xml returns.
 *
 * Revision 1.4  2007-08-10 02:11:12  spr
 * Cleanups from eclipse; bug fixes to avoid deadlock.
 *
 * Revision 1.3  2007-05-04 02:00:22  spr
 * Fix bugs related to polling.
 *
 * Revision 1.2  2006/02/21 17:06:27  spr
 * Upgrade interface to use Element instead of Node for XML.
 *
 * Revision 1.1  2005/07/08 23:33:00  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint.match;


import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintLogger;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.mint.MintSelector;
import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;


public class MintMatchSelector implements MintSelector, MintConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Element pattern_root;
private String	pattern_text;


static {
   new MintMatchArguments();		// force load
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public MintMatchSelector(String pat)
{
   pattern_text = pat;
   pattern_root = IvyXml.convertStringToXml(pat);
}



public MintMatchSelector(Element pat)
{
   pattern_root = pat;
   pattern_text = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getText()
{
   if (pattern_text == null && pattern_root != null) {
      pattern_text = IvyXml.convertXmlToString(pattern_root);
    }

   return pattern_text;
}



@Override public Element getXml()
{
   return pattern_root;
}



/********************************************************************************/
/*										*/
/*	Top level matching methods						*/
/*										*/
/********************************************************************************/

@Override public MintArguments matchMessage(MintMessage msg)
{
   MintMatchArguments args = new MintMatchArguments();

   if (!doMatch(msg.getXml(),args)) args = null;

   return args;
}



@Override public boolean testMatchMessage(MintMessage msg)
{
   boolean rslt = doMatch(msg.getXml(),null);

   return rslt;
}



/********************************************************************************/
/*										*/
/*	Actual matching routines						*/
/*										*/
/********************************************************************************/

private boolean doMatch(Element msg,MintMatchArguments args)
{
   if (pattern_root == null) return false;
   if (msg == null) return false;

   boolean fg = matchElement(pattern_root,msg,args);
   if (fg) return true;
   
   if (getText().contains("''")) {
      MintLogger.log("Possible bad pattern: " + getText() + " :: " +
            IvyXml.convertXmlToString(getXml()));
    }
   
   return false;
}



private boolean matchElement(Element pat,Element msg,MintMatchArguments args)
{
   if (pat == null && msg == null) return true;
   if (pat == null) return false;

   String pnm;
   try {
      pnm = pat.getNodeName();
    }
   catch (Throwable t) {
      MintLogger.log("Unexpected xml failure: " + t,t);
      pnm = pat.getNodeName();
      if (pnm == null) return false;
    }

   if (pnm == null || pnm.equals(MINT_XML_ANY)) return true;
   if (pnm.startsWith(MINT_XML_VAR)) {
      if (args != null) {
	 String idx = pnm.substring(MINT_XML_VAR.length());
	 int aidx = Integer.parseInt(idx);
	 args.setArgument(aidx,msg);
       }
      return true;
    }

   if (msg == null) return false;

   if (!pnm.equalsIgnoreCase(msg.getNodeName())) return false;

   if (!matchAttrs(pat,msg,args)) return false;

   if (!matchChildren(pat,msg,args)) return false;

   return true;
}



private boolean matchAttrs(Element pat,Element msg,MintMatchArguments args)
{
   if (pat == null || msg == null) return false;

   NamedNodeMap pmap = pat.getAttributes();
   NamedNodeMap mmap = msg.getAttributes();

   if (pmap != null) {
      for (int i = 0; ; ++i) {
	 Node patt = pmap.item(i);
	 if (patt == null) break;
	 String anm = patt.getNodeName();
	 String vl = patt.getNodeValue();
	 String mvl = null;
	 boolean fnd = false;
	 if (mmap != null) {
	    Node matt = mmap.getNamedItem(anm);
	    if (matt != null) {
	       fnd = true;
	       mvl = matt.getNodeValue();
	     }
	  }
	 if (vl == null) {
	    if (!fnd) return false;
	  }
	 else if (vl.equals(MINT_XML_ANY)) {
	    if (!fnd) return false;
	  }
	 else if (vl.startsWith(MINT_XML_VAR)) {
	    if (!fnd) return false;
	    if (args != null) {
	       String idx = vl.substring(MINT_XML_VAR.length());
	       int aidx = Integer.parseInt(idx);
	       args.setArgument(aidx,mvl);
	     }
	  }
	 else if (mvl == null) return false;
	 else if (!vl.equalsIgnoreCase(mvl)) return false;
       }
    }

   return true;
}



private boolean matchChildren(Element pat,Element msg,MintMatchArguments args)
{
   if (pat == null || msg == null) return false;

   HashSet<Element> done = new HashSet<Element>();

   NodeList plist = pat.getChildNodes();
   NodeList mlist = msg.getChildNodes();
   StringBuffer ptxt = new StringBuffer();

   if (plist == null) return true;

   for (int i = 0; ; ++i) {
      Node pch = plist.item(i);
      if (pch == null) break;
      if (pch.getNodeType() == Node.TEXT_NODE) {
	 ptxt.append(pch.getNodeValue().trim());
       }
      else if (pch.getNodeType() == Node.ELEMENT_NODE) {
	 if (mlist == null) {
	    if (!matchElement((Element) pch,null,args)) return false;
	  }
	 else {
	    boolean fnd = false;
	    for (int j = 0; ; ++j) {
	       Node mch = mlist.item(j);
	       if (mch == null) break;
	       if (mch.getNodeType() == Node.ELEMENT_NODE && !done.contains(mch)) {
		  if (matchElement((Element) pch,(Element) mch,args)) {
		     done.add((Element) mch);
		     fnd = true;
		     break;
		   }
		}
	     }
	    if (!fnd) {
	       if (!matchElement((Element) pch,null,args)) return false;
	     }
	  }
       }
    }

   String pstr = ptxt.toString().trim();
   if (pstr.length() > 0) {
      StringBuffer mtxt = new StringBuffer();
      if (mlist != null) {
	 for (int j = 0; ; ++j) {
	    Node mch = mlist.item(j);
	    if (mch == null) break;
	    if (mch.getNodeType() == Node.TEXT_NODE) {
	       mtxt.append(mch.getNodeValue());
	     }
	  }
       }

      if (pstr.startsWith(MINT_XML_VAR)) {
	 String idx = pstr.substring(MINT_XML_VAR.length());
	 int aidx = Integer.parseInt(idx);
	 if (args != null) args.setArgument(aidx,mtxt.toString());
       }
      else if (!pstr.equalsIgnoreCase(mtxt.toString())) return false;
    }

   return true;
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return getText();
}



}	// end of class MintMatchSelector



/* end of MintMatchSelector.java */

