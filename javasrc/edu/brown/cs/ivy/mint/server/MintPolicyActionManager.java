/********************************************************************************/
/*										*/
/*		MintPolicyActionManager.java					*/
/*										*/
/*	Manager for Policy Actions						*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/server/MintPolicyActionManager.java,v 1.8 2015/11/20 15:09:20 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintPolicyActionManager.java,v $
 * Revision 1.8  2015/11/20 15:09:20  spr
 * Reformatting.
 *
 * Revision 1.7  2012-01-12 01:26:28  spr
 * Formatting
 *
 * Revision 1.6  2011-05-27 19:32:45  spr
 * Change copyrights.
 *
 * Revision 1.5  2007-08-10 02:11:14  spr
 * Cleanups from eclipse; bug fixes to avoid deadlock.
 *
 * Revision 1.4  2007-05-04 02:00:24  spr
 * Fix bugs related to polling.
 *
 * Revision 1.3  2006/07/10 14:52:22  spr
 * Code cleanup.
 *
 * Revision 1.2  2006/02/21 17:06:28  spr
 * Upgrade interface to use Element instead of Node for XML.
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
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;



abstract class MintPolicyActionManager implements MintConstants {


/********************************************************************************/
/*										*/
/*	Methods to create actions from XML description				*/
/*										*/
/********************************************************************************/

static MintPolicyAction createAction(Element xml) throws MintPolicyException
{
   String typ = xml.getNodeName();
   MintPolicyAction act = null;

   if (typ.equalsIgnoreCase(MINT_XML_SEND)) {
      act = new ActionSend(xml);
    }
   else if (typ.equalsIgnoreCase(MINT_XML_SET)) {
      act = new ActionSet(xml);
    }
   else if (typ.equalsIgnoreCase(MINT_XML_YIELD)) {
      act = new ActionSend(xml);
    }
   else if (typ.equalsIgnoreCase(MINT_XML_REPLY)) {
      act = new ActionSend(xml);
    }
   else if (typ.equalsIgnoreCase(MINT_XML_ACTION_COND)) {
      act = new ActionCond(xml);
    }

   if (act == null) throw new MintPolicyException("Illegal action type: " + typ);

   return act;
}



static List<MintPolicyAction> createActionList(Element xml) throws MintPolicyException
{
   List<MintPolicyAction> acts = new ArrayList<MintPolicyAction>();

   for (Element nc : IvyXml.children(xml)) {
      MintPolicyAction act = createAction(nc);
      if (act != null) acts.add(act);
    }

   return acts;
}




/********************************************************************************/
/*										*/
/*	Common methods for performing actions					*/
/*										*/
/********************************************************************************/

private static Node cloneXml(Node xml,MintPolicyContext ctx)
{
   if (xml.getNodeType() != Node.ELEMENT_NODE) return xml;

   String typ = xml.getNodeName();
   Element nx = null;

   if (typ.startsWith(MINT_XML_VAR)) {
      String var = typ.substring(5);
      int j  = Integer.parseInt(var);
      return ctx.getXmlArgument(j);
    }

   nx = (Element) xml.cloneNode(false);

   NamedNodeMap nnm = nx.getAttributes();
   for (int i = 0; ; ++i) {
      Attr atr = (Attr) nnm.item(i);
      if (atr == null) break;
      String v = atr.getValue();
      if (v.startsWith(MINT_XML_VAR)) {
	 String var = v.substring(5);
	 if (Character.isDigit(var.charAt(0))) {
	    int j = Integer.parseInt(var);
	    atr.setValue(ctx.getArgument(j));
	  }
	 else {
	    atr.setValue(ctx.getVariable(var));
	  }
       }
    }

   NodeList nl = xml.getChildNodes();
   for (int i = 0; ; ++i) {
      Node nc = nl.item(i);
      if (nc == null) break;
      nx.appendChild(cloneXml(nc,ctx));
    }

   return nx;
}



/********************************************************************************/
/*										*/
/*	Action subclass for actions that send messages				*/
/*										*/
/********************************************************************************/

private static class ActionSend implements MintPolicyAction, MintConstants {

   private Element xml_message;
   private boolean is_reply;
   private boolean is_yield;

   ActionSend(Element xml) {
      xml_message = null;
      is_reply = false;
      is_yield = false;
   
      String typ = xml.getNodeName();
      if (typ.equalsIgnoreCase(MINT_XML_REPLY)) is_reply = true;
      else if (typ.equalsIgnoreCase(MINT_XML_YIELD)) is_yield = true;
   
      for (Element nc : IvyXml.children(xml)) {
         xml_message = nc;
         break;
       }
    }

   @Override public void perform(MintPolicyContext ctx,MintServerMessage msg) {
      Element nxml = null;
      if (xml_message != null) nxml = (Element) cloneXml(xml_message,ctx);
      if (is_reply) msg.replyTo(nxml);
      else {
	 MintServerMessage nmsg = new MintServerMessage(msg,nxml,is_yield);
	 if (is_yield) ctx.setUsed();
	 msg.getServer().handleMessage(nmsg);
       }
    }

}	// end of subclass ActionSend



/********************************************************************************/
/*										*/
/*	Action subclass for actions that set variables				*/
/*										*/
/********************************************************************************/

private static class ActionSet implements MintPolicyAction, MintConstants {

   private String var_name;
   private String var_value;

   ActionSet(Element xml) throws MintPolicyException {
      var_name = IvyXml.getAttrString(xml,MINT_XML_COND_VAR);
      var_value = IvyXml.getAttrString(xml,MINT_XML_COND_VALUE);
      if (var_name == null)
	 throw new MintPolicyException("SET must have VAR specified");
    }

   @Override public void perform(MintPolicyContext ctx,MintServerMessage msg) {
      if (var_name == null) return;
      ctx.setVariable(var_name,var_value);
    }

}	// end of subclass ActionSet




/********************************************************************************/
/*										*/
/*	Action subclass for actions that are conditional			*/
/*										*/
/********************************************************************************/

private static class ActionCond implements MintPolicyAction, MintConstants {

   private String var_name;
   private String var_value;
   private List<MintPolicyAction> when_actions;

   ActionCond(Element xml) throws MintPolicyException {
      var_name = IvyXml.getAttrString(xml,MINT_XML_COND_VAR);
      var_value = IvyXml.getAttrString(xml,MINT_XML_COND_VALUE);
      if (var_name == null || var_value == null)
	 throw new MintPolicyException("COND must have both VAR and VALUE specified");

      when_actions = MintPolicyActionManager.createActionList(xml);
    }

   @Override public void perform(MintPolicyContext ctx,MintServerMessage msg) {
      if (var_name == null) return;
      String val = ctx.getVariable(var_name);
      if (val == null && var_value != null) return;
      if (val != null && var_value == null) return;
      if (val != null && var_value != null && !val.equals(var_value)) return;
      if (when_actions != null) {
	 for (MintPolicyAction a : when_actions) {
	    a.perform(ctx,msg);
	  }
       }
    }

}	// end of subclass ActionCond





}	// end of class MintPolicyActionManager



/* end of MintPolicyActionManager.java */

