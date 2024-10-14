/********************************************************************************/
/*										*/
/*		MintMatchArguments.java 					*/
/*										*/
/*	Holder for argument list that results from a match			*/
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


package edu.brown.cs.ivy.mint.match;


import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Element;

import java.util.Vector;



public class MintMatchArguments implements MintArguments
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Vector<Object> arg_list;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MintMatchArguments()
{
   arg_list = new Vector<Object>();
}



/********************************************************************************/
/*										*/
/*	Methods to set arguments						*/
/*										*/
/********************************************************************************/

void setArgument(int idx,int v) 		{ setArgument(idx,Integer.valueOf(v)); }

void setArgument(int idx,long v)		{ setArgument(idx,Long.valueOf(v)); }

void setArgument(int idx,double v)		{ setArgument(idx,Double.valueOf(v)); }

void setArgument(int idx,Object v)
{
   if (idx >= arg_list.size()) {
      arg_list.setSize(idx+1);
    }

   arg_list.setElementAt(v,idx);
}



/********************************************************************************/
/*										*/
/*	Methods to access the arguments 					*/
/*										*/
/********************************************************************************/

@Override public int getNumArguments()			{ return arg_list.size(); }



@Override public String getArgument(int idx)
{
   if (idx < 0 || idx >= arg_list.size()) return null;

   Object v = arg_list.elementAt(idx);

   if (v == null) return null;

   if (v instanceof Element) {
      return IvyXml.convertXmlToString((Element) v);
    }

   return v.toString();
}



@Override public int getIntArgument(int idx)
{
   if (idx < 0 || idx >= arg_list.size()) return 0;

   Object v = arg_list.elementAt(idx);

   if (v == null) return 0;

   if (v instanceof Number) return ((Number) v).intValue();

   try {
      return Integer.parseInt(v.toString());
    }
   catch (NumberFormatException e) { }

   return 0;
}




@Override public long getLongArgument(int idx)
{
   if (idx < 0 || idx >= arg_list.size()) return 0;

   Object v = arg_list.elementAt(idx);

   if (v == null) return 0;

   if (v instanceof Number) return ((Number) v).longValue();

   try {
      return Long.parseLong(v.toString());
    }
   catch (NumberFormatException e) { }

   return 0;
}




@Override public double getRealArgument(int idx)
{
   if (idx < 0 || idx >= arg_list.size()) return 0;

   Object v = arg_list.elementAt(idx);

   if (v == null) return 0;

   if (v instanceof Number) return ((Number) v).doubleValue();

   try {
      Double d = Double.valueOf(v.toString());
      return d.doubleValue();
    }
   catch (NumberFormatException e) { }

   return 0;
}



@Override public Element getXmlArgument(int idx)
{
   if (idx < 0 || idx >= arg_list.size()) return null;

   Object v = arg_list.elementAt(idx);

   if (v == null) return null;

   if (v instanceof Element) return (Element) v;

   return null;
}




}	// end of class MintMatchArguments




/* end of MintMatchArguments.java */

