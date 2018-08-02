/********************************************************************************/
/*										*/
/*		MintPolicyManager.java						*/
/*										*/
/*	Main program implmentation of the Mint message server			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/server/MintPolicyManager.java,v 1.7 2011-05-27 19:32:45 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintPolicyManager.java,v $
 * Revision 1.7  2011-05-27 19:32:45  spr
 * Change copyrights.
 *
 * Revision 1.6  2010-08-20 20:58:34  spr
 * Add logging and options for port numbers
 *
 * Revision 1.5  2007-05-05 01:19:45  spr
 * Formatting changes.
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
 * Revision 1.1  2005/07/08 23:33:07  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint.server;


import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintLogger;
import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MintPolicyManager implements MintConstants
{



private List<Mapping>	filter_checks;
private List<Mapping>	message_checks;
private List<Mapping>	variable_checks;
private Map<String,String> map_variables;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MintPolicyManager(MintServer s)
{
   filter_checks = null;
   message_checks = null;
   variable_checks = null;
   map_variables = new HashMap<String,String>();
}




/********************************************************************************/
/*										*/
/*	Methods for scanning the XML structure					*/
/*										*/
/********************************************************************************/

void addPolicyFile(String fnm)
{
   if (fnm == null) return;

   File file = new File(fnm);

   if (!file.exists()) return;

   Element n = IvyXml.loadXmlFromFile(fnm);
   if (n == null) {
      MintLogger.log("Problem with policy file " + fnm);
    }
   else {
      try {
	 setupPolicy(n);
       }
      catch (MintPolicyException e) {
	 MintLogger.log("Policy setup error: " + e.getMessage());
       }
    }
}







private void setupPolicy(Element xml) throws MintPolicyException
{
   String typ = xml.getNodeName();

   if (typ.equalsIgnoreCase(MINT_XML_MAPPINGS)) {
      setupChildren(xml);
    }
   else if (typ.equalsIgnoreCase(MINT_XML_MAP)) {
      Mapping map = new Mapping(xml);
      if (map.isMessage()) {
	 if (map.isFilter()) {
	    if (filter_checks == null) filter_checks = new ArrayList<Mapping>();
	    filter_checks.add(map);
	  }
	 else {
	    if (message_checks == null) message_checks = new ArrayList<Mapping>();
	    message_checks.add(map);
	  }
       }
      else {
	 if (variable_checks == null) variable_checks = new ArrayList<Mapping>();
	 variable_checks.add(map);
       }
    }
   else {
      throw new MintPolicyException("Invalid xml node: " + typ);
    }
}



private void setupChildren(Element xml) throws	MintPolicyException
{
   for (Element nc : IvyXml.children(xml)) {
      setupPolicy(nc);
    }
}




/********************************************************************************/
/*										*/
/*	Methods to handle message processing					*/
/*										*/
/********************************************************************************/

boolean filterMessage(MintServerMessage msg)
{
   if (filter_checks == null) return true;

   MintPolicyContext ctx = new MintPolicyContext(this);

   for (Mapping map : filter_checks) {
      map.handleMessage(ctx,msg);
    }

   return ctx.isUsed();
}



void handleMessage(MintServerMessage msg)
{
   if (message_checks == null) return;

   MintPolicyContext ctx = new MintPolicyContext(this);

   for (Mapping map : message_checks) {
      map.handleMessage(ctx,msg);
    }
}



/********************************************************************************/
/*										*/
/*	Methods for managing variables						*/
/*										*/
/********************************************************************************/

void setVariable(String v,String value)
{
   if (value == null) map_variables.remove(v);
   else map_variables.put(v,value);
}



String getVariable(String v)
{
   return map_variables.get(v);
}



/********************************************************************************/
/*										*/
/*	Mapping -- subclass to hold a policy mapping				*/
/*										*/
/********************************************************************************/

private static class Mapping {

   private MintPolicyWhen when_clause;
   private List<MintPolicyAction> action_list;
   private boolean is_filter;
   private boolean is_message;

   Mapping(Element xml) throws MintPolicyException {
      when_clause = null;
      action_list = new ArrayList<MintPolicyAction>();
      is_filter = false;
      is_message = false;

      for (Element nc : IvyXml.children(xml)) {
	 setupMappingElement(nc);
       }

      if (when_clause == null) throw new MintPolicyException("Mapping without a condition");
      if (action_list.size() == 0) throw new MintPolicyException("Mapping without any actions");

      if (when_clause != null) {
	 is_filter = when_clause.isFilter();
	 is_message = when_clause.isMessage();
       }
    }

   private void setupMappingElement(Element xml) throws MintPolicyException {
      String typ = xml.getNodeName();
      if (typ.equalsIgnoreCase(MINT_XML_ACTIONS)) {
	 action_list.addAll(MintPolicyActionManager.createActionList(xml));
       }
      else if (when_clause == null) {
	 when_clause = MintPolicyWhenManager.createWhen(xml);
       }
    }

   boolean isMessage()			{ return is_message; }
   boolean isFilter()			{ return is_filter; }

   void handleMessage(MintPolicyContext ctx,MintServerMessage msg) {
      if (when_clause.match(ctx,msg)) {
	 ctx.setUsed();
	 for (MintPolicyAction a : action_list) {
	    a.perform(ctx,msg);
	  }
       }
    }

}	// end of subclass Mapping



}	// end of class MintPolicyManager




/* end of MintPolicyManager.java */
