/********************************************************************************/
/*										*/
/*		MintPolicyWhenManager.java					*/
/*										*/
/*	Manager for Policy Whens						*/
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


package edu.brown.cs.ivy.mint.server;


import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintConnect;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintSelector;
import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Element;



abstract class MintPolicyWhenManager implements MintConstants {


/********************************************************************************/
/*										*/
/*	Methods to create conditions from XML description			*/
/*										*/
/********************************************************************************/

static MintPolicyWhen createWhen(Element xml) throws MintPolicyException
{
   String typ = xml.getNodeName();
   MintPolicyWhen w = null;

   if (typ.equalsIgnoreCase(MINT_XML_MATCH)) {
      w  = new WhenMsg(xml);
    }
   else if (typ.equalsIgnoreCase(MINT_XML_FILTER)) {
      w = new WhenMsg(xml);
    }
   else if (typ.equalsIgnoreCase(MINT_XML_WHEN_COND)) {
      w = new WhenCond(xml);
    }
   else throw new MintPolicyException("Illegal When Clause");

   return w;
}




/********************************************************************************/
/*										*/
/*	Subclass for conditional WHEN clause					*/
/*										*/
/********************************************************************************/

private static class WhenCond implements MintPolicyWhen, MintConstants {

   private String var_name;
   private String var_value;
   private MintPolicyWhen when_clause;

   WhenCond(Element xml) throws MintPolicyException {
      var_name = IvyXml.getAttrString(xml,MINT_XML_COND_VAR);
      var_value = IvyXml.getAttrString(xml,MINT_XML_COND_VALUE);
      if (var_name == null)
	 throw new MintPolicyException("COND must have VAR specified");
      for (Element nc : IvyXml.children(xml)) {
	 when_clause = MintPolicyWhenManager.createWhen(nc);
	 if (when_clause != null) break;
       }
    }

   @Override public boolean isFilter() {
      if (when_clause == null) return false;
      return when_clause.isFilter();
    }

   @Override public boolean isMessage() {
      if (when_clause == null) return false;
      return when_clause.isMessage();
    }

   @Override public boolean match(MintPolicyContext ctx,MintServerMessage msg) {
      if (var_name == null) return false;
      String val = ctx.getVariable(var_name);
      if (val == null && var_value != null) return false;
      if (val != null && var_value == null) return false;
      if (val != null && var_value != null && !val.equals(var_value)) return false;
      if (when_clause != null) return when_clause.match(ctx,msg);
      return true;
    }

}	// end of subclass WhenCond



/********************************************************************************/
/*										*/
/*	Subclass for message-based WHEN clause					*/
/*										*/
/********************************************************************************/

private static class WhenMsg implements MintPolicyWhen, MintConstants {

   private MintSelector the_pattern;
   private boolean is_filter;

   WhenMsg(Element xml) {
      is_filter = false;
      the_pattern = null;

      Element xmlpat = null;
      String typ = xml.getNodeName();
      if (typ.equalsIgnoreCase(MINT_XML_FILTER)) is_filter = true;

      for (Element nc : IvyXml.children(xml)) {
	 xmlpat = nc;
	 break;
       }

      if (xmlpat != null) the_pattern = MintConnect.createSelector(xmlpat);
    }

   @Override public boolean isFilter()		{ return is_filter; }
   @Override public boolean isMessage()		{ return the_pattern != null; }

   @Override public boolean match(MintPolicyContext ctx,MintServerMessage msg) {
      if (the_pattern == null) return true;
      MintArguments args = the_pattern.matchMessage(msg);
      if (args == null) return false;
      ctx.setArguments(args);
      return true;
    }

}	// end of subclass WhenMsg






}	// end of class MintPolicyWhenManager



/* end of MintPolicyWhenManager.java */


