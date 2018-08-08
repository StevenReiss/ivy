/********************************************************************************/
/*										*/
/*		MintContextManager.java 					*/
/*										*/
/*	Context manager for Mint message server 				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/server/MintContextManager.java,v 1.8 2018/08/02 15:10:30 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintContextManager.java,v $
 * Revision 1.8  2018/08/02 15:10:30  spr
 * Fix imports.
 *
 * Revision 1.7  2013/11/15 02:39:11  spr
 * Fix imports
 *
 * Revision 1.6  2011-05-27 19:32:45  spr
 * Change copyrights.
 *
 * Revision 1.5  2011-05-18 01:02:04  spr
 * Changes to fix up web connection user id.
 *
 * Revision 1.4  2011-05-17 01:05:07  spr
 * Mint server to support web-scale messages.
 *
 * Revision 1.3  2010-08-20 20:58:34  spr
 * Add logging and options for port numbers
 *
 * Revision 1.2  2007-05-04 02:00:24  spr
 * Fix bugs related to polling.
 *
 * Revision 1.1  2005/07/08 23:33:06  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint.server;


import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;



class MintContextManager implements MintConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private MintServer		for_server;
private Map<String,String>	mint_context;
private MintServerWebConnection web_connect;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MintContextManager(MintServer ms)
{
   for_server = ms;
   mint_context = new HashMap<String,String>();
   web_connect = null;
}



/********************************************************************************/
/*										*/
/*	Methods to handle messages						*/
/*										*/
/********************************************************************************/

boolean handleMessage(MintServerMessage msg)
{
   Element root = msg.getXml();
   boolean cont = true;

   if (!IvyXml.isElement(root,MINT_CONTEXT_ELT_MINT)) return cont;

   for (Element nc : IvyXml.children(root)) {
      cont &= handleMintMsg(msg,nc);
    }

   return cont;
}


private boolean handleMintMsg(MintServerMessage msg,Element xml)
{
   boolean cont = true;

   if (IvyXml.isElement(xml,MINT_CONTEXT_ELT_SET)) {
      handleSetContext(xml);
    }
   else if (IvyXml.isElement(xml,MINT_CONTEXT_ELT_GET)) {
      cont = false;
      handleGetContext(msg);
    }
   else if (IvyXml.isElement(xml,MINT_CONTEXT_ELT_WEB)) {
      cont = false;
      if (web_connect == null) {
	 String url = IvyXml.getAttrString(xml,"URL");
	 String key = IvyXml.getAttrString(xml,"KEY");
	 web_connect = new MintServerWebConnection(for_server,url,key);
	 for_server.addSpecialConnection(web_connect);
       }
    }

   return cont;
}



private void handleSetContext(Node xml)
{
   NamedNodeMap atts = xml.getAttributes();
   for (int i = 0; ; ++i) {
      Node nc = atts.item(i);
      if (nc == null) break;
      if (nc.getNodeType() == Node.ATTRIBUTE_NODE) {
	 Attr att = (Attr) nc;
	 String nm = att.getName();
	 String vl = att.getValue();
	 if (vl != null) mint_context.put(nm,vl);
	 else mint_context.remove(nm);
       }
    }
}



private void handleGetContext(MintServerMessage msg)
{
   StringBuffer buf = new StringBuffer();
   buf.append("<" + MINT_CONTEXT_ELT_STATE + " ");

   for (Map.Entry<String,String> ent : mint_context.entrySet()) {
      String s = ent.getKey();
      String v = ent.getValue();
      buf.append(s + "='" + v + "' ");
    }

   buf.append("/>");

   msg.replyTo(buf.toString());
}



}	// end of class MintContextManager



/* end of MintContextManager.java */
