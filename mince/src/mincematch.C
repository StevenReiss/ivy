/********************************************************************************/
/*										*/
/*		mincematch.C							*/
/*										*/
/*	Matching methods for MINCE message service				*/
/*										*/
/********************************************************************************/
/*	Copyright 1997 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 1997, Brown University, Providence, RI.				 *
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
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,	 *
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS 	 *
 *  SOFTWARE.									 *
 *										 *
 ********************************************************************************/


#ifndef lint
static const char * const rcsid = "$Header: /pro/spr_cvs/pro/ivy/mince/src/mincematch.C,v 1.2 2014/06/12 01:06:35 spr Exp $";
#endif


/*********************************************************************************
 *
 * $Log: mincematch.C,v $
 * Revision 1.2  2014/06/12 01:06:35  spr
 * Minor updates
 *
 * Revision 1.1  2005/06/28 17:22:00  spr
 * Move mince from tea to ivy for use in veld, etc.
 *
 *
 ********************************************************************************/


#include "mince_local.H"



/********************************************************************************/
/*										*/
/*	MinceMatchSelector constructors/destructors				*/
/*										*/
/********************************************************************************/


MinceMatchSelectorInfo::MinceMatchSelectorInfo(CStdString pat)
{
   pattern_root = IvyXml::convertStringToXml(pat);
   pattern_text = pat;
}



MinceMatchSelectorInfo::MinceMatchSelectorInfo(IvyXmlNode n)
{
   pattern_root = n;
}




MinceMatchSelectorInfo::~MinceMatchSelectorInfo()			{ }




/********************************************************************************/
/*										*/
/*	MinceMatchSelector access methods					*/
/*										*/
/********************************************************************************/


CStdString
MinceMatchSelectorInfo::getText()
{
   if (pattern_text.empty() && !pattern_root.isNull()) {
      pattern_text = pattern_root.convertToString();
    }

   return pattern_text;
}



/********************************************************************************/
/*										*/
/*	MinceMatchSelector top-level matching methods				*/
/*										*/
/********************************************************************************/


MinceArguments
MinceMatchSelectorInfo::matchMessage(MinceMessage msg) const
{
   MinceMatchArguments args = new MinceMatchArgumentsInfo();

   if (!doMatch(msg->getXml(),args)) {
      delete args;
      args = NULL;
    }

   return args;
}



Bool
MinceMatchSelectorInfo::testMatchMessage(MinceMessage msg) const
{
   return doMatch(msg->getXml(),NULL);
}



/********************************************************************************/
/*										*/
/*	MinceMatchSelector matching methods					*/
/*										*/
/********************************************************************************/


Bool
MinceMatchSelectorInfo::doMatch(IvyXmlNode msg,MinceMatchArguments args) const
{
   if (pattern_root.isNull()) return false;
   if (msg.isNull()) return false;

   return matchElement(pattern_root,msg,args);
}



Bool
MinceMatchSelectorInfo::matchElement(IvyXmlNode pat,IvyXmlNode msg,MinceMatchArguments args) const
{
   if (pat.isNull() && msg.isNull()) return true;
   if (!pat.isElement()) return false;

   StdString pnm = pat.getNodeName();
   if (pnm == MINCE_XML_ANY) return true;
   else if (strprefix(pnm,MINCE_XML_VAR)) {
      if (args != NULL) {
	 StdString idx = pnm.substr(strlen(MINCE_XML_VAR));
	 int aidx = atoi(idx);
	 args->setArgument(aidx,msg);
       }
      return true;
    }
   else if (msg.isNull() || !msg.isElement()) return false;
   else if (STRCASENEQ(pnm,msg.getNodeName())) return false;
   else if (!matchAttrs(pat,msg,args)) return false;
   else if (!matchChildren(pat,msg,args)) return false;

   return true;
}



Bool
MinceMatchSelectorInfo::matchAttrs(IvyXmlNode pat,IvyXmlNode msg,MinceMatchArguments args) const
{
   if (pat.isNull() || msg.isNull()) return false;

   for (IvyXmlAttrIter pmap = pat.getAttrs(); pmap; ++pmap) {
      IvyXmlNode patt = pmap.current();
      StdString anm = patt.getNodeName();
      StdString vl = patt.getNodeValue();
      StdString mvl;
      Bool fnd = false;

      if (msg.getAttrPresent(anm)) {
	 fnd = true;
	 mvl = msg.getAttrString(anm);
       }
      if (vl.empty()) {
	 if (!fnd) return false;
       }
      else if (vl == MINCE_XML_ANY) {
	 if (!fnd) return false;
       }
      else if (strprefix(vl,MINCE_XML_VAR)) {
	 if (!fnd) return false;
	 if (args != NULL) {
	    StdString idx = vl.substr(strlen(MINCE_XML_VAR));
	    int aidx = atoi(idx);
	    args->setArgument(aidx,mvl);
	  }
       }
      else if (mvl.empty()) return false;
      else if (STRCASENEQ(vl,mvl)) return false;
    }

   return true;
}



Bool
MinceMatchSelectorInfo::matchChildren(IvyXmlNode pat,IvyXmlNode msg,MinceMatchArguments args) const
{
   if (pat.isNull() || msg.isNull()) return false;

   HashMap<IvyXmlNode,Bool> done;
   StringBuffer ptxt;

   for (IvyXmlChildIter plist = pat.getChildren(); plist; ++plist) {
      IvyXmlNode pch = *plist;
      if (pch.isText()) {
	 ptxt.append(trim(pch.getNodeValue()));
       }
      else if (pch.isElement()) {
	 if (msg.getNumChildren() == 0) {
	    if (!matchElement(pch,IvyXmlNode(),args)) return false;
	  }
	 else {
	    Bool fnd = false;
	    for (IvyXmlChildIter mlist = msg.getChildren(); mlist; ++mlist) {
	       IvyXmlNode mch = *mlist;
	       if (mch.isElement() && !done[mch]) {
		  if (matchElement(pch,mch,args)) {
		     done[mch] = true;
		     fnd = true;
		     break;
		   }
		}
	     }
	    if (!fnd) return false;
	  }
       }
    }

   StdString pstr = trim(ptxt.getString());
   if (!pstr.empty()) {
      StdString mtxt = msg.getText();
      if (strprefix(pstr,MINCE_XML_VAR)) {
	 StdString idx = pstr.substr(strlen(MINCE_XML_VAR));
	 int aidx = atoi(idx);
	 if (args != NULL) args->setArgument(aidx,mtxt);
       }
      else if (STRCASENEQ(pstr,mtxt)) return false;
    }

   return true;
}



/********************************************************************************/
/*										*/
/*	MinceMatchArguments constructors/destructors				*/
/*										*/
/********************************************************************************/


MinceMatchArgumentsInfo::MinceMatchArgumentsInfo()			{
}



MinceMatchArgumentsInfo::~MinceMatchArgumentsInfo()
{

   for (MinceArgumentListIter ali = arg_list.begin(); ali != arg_list.end(); ++ali) {
      delete *ali;
    }
}



/********************************************************************************/
/*										*/
/*	MinceMatchArguments set methods 					*/
/*										*/
/********************************************************************************/


void
MinceMatchArgumentsInfo::setArgument(int idx,Integer v)
{
   setArgument(idx,new MinceArgDataIntInfo(v));
}



void
MinceMatchArgumentsInfo::setArgument(int idx,Float v)
{
   setArgument(idx,new MinceArgDataRealInfo(v));
}



void
MinceMatchArgumentsInfo::setArgument(int idx,IvyXmlNode v)
{
   setArgument(idx,new MinceArgDataXmlInfo(v));
}



void
MinceMatchArgumentsInfo::setArgument(int idx,CStdString v)
{
   setArgument(idx,new MinceArgDataStringInfo(v));
}




void
MinceMatchArgumentsInfo::setArgument(int idx,MinceArgData d)
{
   if (idx >= (int) arg_list.size()) {
      arg_list.resize(idx+1);
    }

   arg_list[idx] = d;
}



/********************************************************************************/
/*										*/
/*	MinceMatchArguments get methods 					*/
/*										*/
/********************************************************************************/


StdString
MinceMatchArgumentsInfo::getArgument(int idx) const
{
   MinceArgData d = getArgumentData(idx);
   if (d == NULL) return "";
   return d->getStringValue();
}



Integer
MinceMatchArgumentsInfo::getIntArgument(int idx) const
{
   MinceArgData d = getArgumentData(idx);
   if (d == NULL) return 0;
   return d->getIntValue();
}



Float
MinceMatchArgumentsInfo::getRealArgument(int idx) const
{
   MinceArgData d = getArgumentData(idx);
   if (d == NULL) return 0;
   return d->getFloatValue();
}



IvyXmlNode
MinceMatchArgumentsInfo::getXmlArgument(int idx) const
{
   MinceArgData d = getArgumentData(idx);
   if (d == NULL) return IvyXmlNode();
   return d->getXmlValue();
}



MinceArgData
MinceMatchArgumentsInfo::getArgumentData(int idx) const
{
   if (idx >= (int) arg_list.size()) return NULL;
   return arg_list[idx];
}




/* end of mincematch.C */
