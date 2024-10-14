/********************************************************************************/
/*										*/
/*		LimboBestLine.java						*/
/*										*/
/*	Location tracking using W_BESTI_LINE					*/
/*		Best match on current line					*/
/*		Exact match on context lines					*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
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


package edu.brown.cs.ivy.limbo;



import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import org.w3c.dom.Element;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;



class LimboBestLine extends LimboLine {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		line_text;
private int []		pre_context;
private int []		post_context;

private static final int	CONTEXT_SIZE = 4;
private static final boolean	IGNORE_SPACE = false;
private static final double	CONTEXT_THRESHOLD = 0.35;
private static final double	CONTEXT_SCALE = 0.4;

private static final boolean	LINE_IGNORE_SPACE = true;
private static final double	LINE_THRESHOLD = 0.5;
private static final double	LINE_SCALE = (1.0 - CONTEXT_SCALE);

private static final double	THRESHOLD_VALUE = 0.35;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

LimboBestLine(LimboFile f,int line)
{
   super(f.getFile(),line);

   resetData(f,line);
}



LimboBestLine(Element e)
{
   super(e);

   line_text = IvyXml.getTextElement(e,"MATCHLINE");
   pre_context = loadArray(e,"PRECONTEXT");
   post_context = loadArray(e,"POSTCONTEXT");
}



/********************************************************************************/
/*										*/
/*	Methods to find best match						*/
/*										*/
/********************************************************************************/

@Override protected void localValidate(LimboFile f)
{
   if (getSourceFile() == null) setFile(f.getFile());
   clearLine();

   Map<Integer,Double> r = new TreeMap<Integer,Double>();

   int ct = f.getLineCount();
   int pct = pre_context.length;
   int qct = post_context.length;
   for (int i = 1; i <= ct; ++i) {
      double npre = 0;
      for (int j = 0; j < pct; ++j) {
	 String d = f.getLine(i-pct+j);
	 if (d == null) continue;
	 if (IGNORE_SPACE) d = d.trim();
	 if (pre_context[j] == d.hashCode()) ++npre;
       }
      double npost = 0;
      for (int j = 0; j < qct; ++j) {
	 String d = f.getLine(i+j+1);
	 if (d == null) continue;
	 if (IGNORE_SPACE) d = d.trim();
	 if (post_context[j] == d.hashCode()) ++npost;
       }
      if (qct == 0 && pct == 0) continue;
      double v = (npre + npost) / (pct + qct);
      if (v >= CONTEXT_THRESHOLD) r.put(i,v*CONTEXT_SCALE);
    }

   if (line_text == null) line_text = "";
   int dln = line_text.length();
   for (int i = 1; i <= ct; ++i) {
      String d = f.getLine(i);
      if (LINE_IGNORE_SPACE) d = d.trim();
      if (dln > 0) {
	 double delta = stringDiff(d,line_text);
	 double v = (dln - delta)/dln;
	 if (v <= LINE_THRESHOLD) continue;
	 Double v0 = r.get(i);
	 if (v0 == null) r.put(i,v*LINE_SCALE);
	 else r.put(i,v*LINE_SCALE + v0);
       }
      else if (d.length() == 0) {
	 double v = LINE_THRESHOLD;
	 Double v0 = r.get(i);
	 if (v0 == null) r.put(i,v*LINE_SCALE);
	 else r.put(i,v*LINE_SCALE + v0);
      }
    }

   double bestv = -1;
   int bestln = -1;
   for (Map.Entry<Integer,Double> ent : r.entrySet()) {
      if (ent.getValue() > bestv) {
	 bestv = ent.getValue();
	 bestln = ent.getKey();
       }
    }

   if (bestln < 0 || bestv < THRESHOLD_VALUE) return;

   setLine(bestln);

   resetData(f,bestln);
}



/********************************************************************************/
/*										*/
/*	Methods to set up context information					*/
/*										*/
/********************************************************************************/

private void resetData(LimboFile f,int line)
{
   line_text = f.getLine(line);
   if (LINE_IGNORE_SPACE && line_text != null) line_text = line_text.trim();

   int pct = (line - CONTEXT_SIZE > 1 ? CONTEXT_SIZE : line-1);
   if (pre_context == null || pre_context.length != pct) pre_context = new int[pct];
   for (int i = 0; i < pct; ++i) {
      String d = f.getLine(line-pct+i);
      if (d == null) d = "";
      if (IGNORE_SPACE) d = d.trim();
      pre_context[i] = d.hashCode();
    }
   int fct = f.getLineCount();
   pct = (fct - line > CONTEXT_SIZE ? CONTEXT_SIZE : fct-line);
   if (pct < 0) pct = 0;
   if (post_context == null || post_context.length != pct) post_context = new int[pct];
   for (int i = 0; i < pct; ++i) {
      String d = f.getLine(line+i+1);
      if (d == null) d = "";
      if (IGNORE_SPACE) d = d.trim();
      post_context[i] = d.hashCode();
    }
}




/********************************************************************************/
/*										*/
/*	I/O methods								*/
/*										*/
/********************************************************************************/

@Override protected void localWriteXml(IvyXmlWriter xw)
{
   xw.textElement("MATCHLINE",line_text);
   storeArray(xw,"PRECONTEXT",pre_context);
   storeArray(xw,"POSTCONTEXT",post_context);
}




private void storeArray(IvyXmlWriter xw,String tag,int [] r)
{
   xw.begin(tag);
   xw.field("LENGTH",r.length);
   for (int i : r) {
      xw.text(" " + i);
    }
   xw.end(tag);
}



private int [] loadArray(Element e,String tag)
{
   Element ce = IvyXml.getElementByTag(e,tag);
   int [] r = new int[IvyXml.getAttrInt(ce,"LENGTH")];
   if (r.length == 0) return r;

   StringTokenizer tok = new StringTokenizer(IvyXml.getText(ce));
   int ct = 0;
   while (tok.hasMoreTokens()) {
      String s = tok.nextToken();
      r[ct++] = Integer.parseInt(s);
    }
   return r;
}



}	// end of class LimboBestLine



/* end of LimboBestLine.java */
