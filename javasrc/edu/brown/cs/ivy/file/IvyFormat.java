/********************************************************************************/
/*										*/
/*		IvyFormat.java							*/
/*										*/
/*	Utility methods for formatting						*/
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


package edu.brown.cs.ivy.file;

import java.text.DecimalFormat;
import java.util.Formatter;



public final class IvyFormat {




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private static final DecimalFormat MEMORY_FORMAT = new DecimalFormat("0.0");
private static final DecimalFormat COUNT_FORMAT = new DecimalFormat("0.0");
private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.0");
private static final DecimalFormat TIME_FORMAT;
private static final DecimalFormat INTERVAL_FORMAT = new DecimalFormat("0.0000");
private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.00");


static {
   TIME_FORMAT = new DecimalFormat("0.00");
   TIME_FORMAT.setMinimumFractionDigits(3);
   TIME_FORMAT.setMaximumFractionDigits(3);
}




/********************************************************************************/
/*										*/
/*	Formatting routines for memory size					*/
/*										*/
/********************************************************************************/

public static String formatMemory(double v0)
{
   return formatMemory(v0,0);
}



public static String formatMemory(double v0,double max)
{
   String tail = "";

   double v = v0;

   if (Math.abs(v) > 1024*1024*1024) {
      v /= 1024*1024*1024;
      tail = "G";
    }
   else if (Math.abs(v) > 1024*1024) {
      v /= 1024*1024;
      tail = "M";
    }
   else if (Math.abs(v) > 1024) {
      v /= 1024;
      tail = "K";
    }

   String s = MEMORY_FORMAT.format(v) + tail;

   if (max != 0) {
      s += " (" + formatPercent(v0/max) + "%)";
    }

   return s;
}



/********************************************************************************/
/*										*/
/*	Routines for formatting counts						*/
/*										*/
/********************************************************************************/

public static String formatCount(double v0)
{
   return formatCount(v0,0);
}



public static String formatCount(double v0,double max)
{
   String tail = "";

   double v = v0;

   if (Math.abs(v) > 1000*1000*1000) {
      v /= 1000*1000*1000;
      tail = "G";
    }
   else if (Math.abs(v) > 1000*1000) {
      v /= 1000*1000;
      tail = "M";
    }
   else if (Math.abs(v) > 1000) {
      v /= 1000;
      tail = "K";
    }
   else {
      v = (long) (v + 0.5);
    }

   String s = COUNT_FORMAT.format(v) + tail;

   if (max != 0) {
      s += " (" + formatPercent(v0/max) + "%)";
    }

   return s;
}



/********************************************************************************/
/*										*/
/*	Methods for formatting percentages					*/
/*										*/
/********************************************************************************/

public static String formatPercent(double v)
{
   v *= 100.0;

   return PERCENT_FORMAT.format(v);
}



/********************************************************************************/
/*										*/
/*	Methods for formatting times						*/
/*										*/
/********************************************************************************/

public static String formatTime(double v)
{
   v /= 1000.0; 		// convert to seconds

   return TIME_FORMAT.format(v);
}


public static String formatInterval(double v)
{
   return INTERVAL_FORMAT.format(v);		// in milliseconds
}



/********************************************************************************/
/*										*/
/*	Numeric formatting methods						*/
/*										*/
/********************************************************************************/

public static String formatNumber(double v)
{
   return NUMBER_FORMAT.format(v);
}



/********************************************************************************/
/*                                                                              */
/*      Format String                                                           */
/*                                                                              */
/********************************************************************************/

public static String formatString(String s) 
{
   return formatString(s,false);
}


public static String formatChar(String s)
{
   return formatString(s,true);
}


public static String formatString(String s,boolean ischar)
{
   if (s == null) return null;
   
   StringBuffer buf = new StringBuffer();
   try (Formatter f = new Formatter(buf)) {
      for (int i = 0; i < s.length(); ++i) {
         char c = s.charAt(i);
         switch (c) {
            case '"' :
               if (!ischar) buf.append("\\\"");
               else buf.append(c);
               break;
            case '\'' :
               if (ischar) buf.append("\\\'");
               else buf.append(c);
               break;
            case '\\' :
               buf.append("\\\\");
               break;
            case '\t' :
               buf.append("\\t");
               break;
            case '\n' :
               buf.append("\\n");
               break;
            case '\b' :
               buf.append("\\b");
               break;
            case '\r' :
               buf.append("\\r");
               break;
            case '\f' :
               buf.append("\\f");
               break;
            default :
               if (c < 32 || c >= 128) {
                  f.format("\\u%04x",(int) c);
                }
               else buf.append(c);
               break;
          }
       }
    }
   
   return buf.toString();
}


/********************************************************************************/
/*										*/
/*	Java Type formatting methods						*/
/*										*/
/********************************************************************************/

public static String formatTypeName(String javatype)
{
   return formatTypeName(javatype,false);
}



/********************************************************************************/
/*                                                                              */
/*      String formatting methods                                               */
/*                                                                              */
/********************************************************************************/

public static String getLiteralValue(String s) {
   return getLiteralValue(s,true);
}

public static String getRawLiteralValue(String s) {
   return getLiteralValue(s,false);
}


private static String getLiteralValue(String s,boolean quoted)
{
   StringBuffer buf = new StringBuffer();
   int start = 0;
   int last = s.length();
   if (quoted) {
      ++start;
      --last;
    }
  
   for (int i = start; i < last; i++) {
      char c = s.charAt(i);
      if (c == '\\') {
	 i++;
	 c = s.charAt(i);
	 if (Character.toUpperCase(c) == 'U') {
	    i++;
            int eidx = Math.min(s.length()-1,i+4);
	    String unicodeChars = s.substring(i, eidx);
	    int val = Integer.parseInt(unicodeChars, 16);
            char cval = (char) val;
	    i = eidx-1;
	    buf.append(cval);
          }
	 else if (Character.isDigit(c)) {
            int eidx = Math.min(s.length()-1,i+3);
            String octchars = s.substring(i,eidx);
            int val = Integer.parseInt(octchars,8);
	    i = eidx;
            buf.append((char) val);
          }
	 else {
            switch (c) {
               case '"' :
               case '\'' :
               case '\\' :
               default :
                  buf.append(c);
                  break;
               case 'n' :
                  buf.append('\n');
                  break;
               case 'r' :
                  buf.append('\r');
                  break;
               case 'f' :
                  buf.append('\f');
                  break;
               case 't' :
                  buf.append('\t');
                  break;      
               case 'b' :
                  buf.append('\b');
                  break;               
             }
          }
       }
      else {
	 buf.append(c);
       }
    }
   return buf.toString();
}



public static String getConstantExpression(Object o)
{
   if (o == null) return "null";
   
   if (o instanceof String) {
      String s = o.toString();
      StringBuffer buf = new StringBuffer();
      buf.append("\"");
      for (int i = 0; i < s.length(); ++i) {
         char c = s.charAt(i);
         if (c == '\\' || c <= '"') {
            buf.append("\\");
            buf.append(c);
          }
         else if (c == '\n') buf.append("\\n");
         else if (c == '\r') buf.append("\\r");
         else if (c == '\t') buf.append("\\t");
         else if (c < 32 || c >= 0177) {
            buf.append("\\u");
            String d = Integer.toString(c,16);
            while (d.length() < 4) d = "0" + d;
            buf.append(d);
          }
         else buf.append(c);
       }
      buf.append("\"");
      return buf.toString();
    }
   else if (o instanceof Number) {
      Number v = (Number) o;
      if (o instanceof Double) {
         return v.toString();
       }
      else if (o instanceof Float) {
         return v.toString() + "f";
       }
      else if (o instanceof Integer) {
         return v.toString();
       }
      else if (o instanceof Long) {
         return v.toString() + "l";
       }
      else if (o instanceof Short) {
         return "((short)" + v.toString() + ")";
       }
      else if (o instanceof Byte) {
         return "((byte)" + v.toString() + ")";
       }
      else return v.toString();
    }
   else if (o instanceof Character) {
      StringBuffer buf = new StringBuffer();
      buf.append("'");
      char c = ((Character) o).charValue();
      if (c == '\\' || c <= '\'') {
         buf.append("\\");
         buf.append(c);
       }
      else if (c == '\n') buf.append("\\n");
      else if (c == '\r') buf.append("\\r");
      else if (c == '\t') buf.append("\\t");
      else if (c < 32 || c >= 0177) {
         buf.append("\\u");
         String d = Integer.toString(c,16);
         while (d.length() < 4) d = "0" + d;
         buf.append(d);
       }
      else buf.append(c);
      buf.append("'");
      return buf.toString();
    }
   else {
      return o.toString();
    }
}




/********************************************************************************/
/*                                                                              */
/*      Type format methods                                                     */
/*                                                                              */
/********************************************************************************/

public static String formatTypeNames(String javatype,String sep)
{
   if (javatype == null) return null;

   StringBuffer buf = new StringBuffer();

   int last = 0;
   for ( ; ; ) {
      int next = internalFormatTypeName(javatype,last,buf,false);
      if (next >= javatype.length()) break;
      buf.append(sep);
      last = next;
    }

   return buf.toString();
}




public static String formatTypeName(String javatype,boolean internal)
{
   if (javatype == null) return null;

   StringBuffer buf = new StringBuffer();

   int last = internalFormatTypeName(javatype,0,buf,internal);

   for (int i = last; i < javatype.length(); ++i) {
      buf.append(javatype.charAt(i));
    }

   return buf.toString();
}




 private static int internalFormatTypeName(String jty,int idx,StringBuffer buf,boolean internal)
{
   if (idx >= jty.length()) return idx;
   
   if (jty.charAt(idx) == '<') {
      int lvl = 0;
      while (idx < jty.length()) {
         ++idx;
         if (jty.charAt(idx) == '<') ++lvl;
         else if (jty.charAt(idx) == '>') {
            if (lvl == 0) {
               ++idx;
               break;
             }
            else --lvl;
          }
       }
    }
   
   if (idx >= jty.length()) return idx;

   switch (jty.charAt(idx)) {
      case '?' :
	 ++idx;
	 idx = internalFormatTypeName(jty,idx,buf,internal);
	 buf.append("?");
	 return idx;

      case '*' :
	 ++idx;
	 buf.append("*");
	 return idx;

      case '[' :
	 ++idx;
	 String ln = null;
	 if (jty.charAt(idx) == '*') {
	    int jidx = ++idx;
	    while (Character.isDigit(jty.charAt(jidx))) ++jidx;
	    ln = jty.substring(idx,jidx);
	    idx = jidx;
	  }
	 idx = internalFormatTypeName(jty,idx,buf,internal);
	 if (ln == null) buf.append("[]");
	 else buf.append("[" + ln + "]");
	 return idx;

      case 'Q' :
      case 'L' :
	 for (int i = idx+1; i < jty.length(); ++i) {
	    if (jty.charAt(i) == '<' || jty.charAt(i) == ';') {
	       String rslt = jty.substring(idx+1,i);
	       if (!internal) rslt = rslt.replace("$",".");
	       rslt = rslt.replace("/",".");
	       buf.append(rslt);
	       if (jty.charAt(i) == '<') {
		  buf.append("<");
		  ++i;
		  while (i < jty.length()) {
                     if (jty.charAt(i) == '+') {
                        buf.append("? extends ");
                        ++i;
                      }
                     else if (jty.charAt(i) == '-') {
                        buf.append("? implements ");
                        ++i;
                      }
		     i = internalFormatTypeName(jty,i,buf,internal);
		     if (i >= jty.length() || jty.charAt(i) == '>') break;
		     buf.append(",");
		   }
		  ++i;
		  buf.append(">");
		}
	       if (i < jty.length() && jty.charAt(i) == ';') ++i;
	       return i;
	     }
	  }
	 break;

      case 'T' :
	 int i1 = jty.indexOf(";",idx);
	 if (i1 < 0) i1 = jty.length();
	 buf.append(jty.substring(idx+1,i1));
         if (i1 < jty.length() && jty.charAt(i1) == ';') ++i1;
	 return i1;

      case '(' :
	 StringBuffer args = new StringBuffer();
	 int act = 0;
	 ++idx;
	 while (idx < jty.length() && jty.charAt(idx) != ')') {
	    if (act++ != 0) args.append(",");
	    idx = internalFormatTypeName(jty,idx,args,internal);
	  }
	 idx = internalFormatTypeName(jty,idx+1,buf,internal);
	 buf.append("(" + args.toString() + ")");
	 return idx;

      case 'B' :
	 buf.append("byte");
	 break;
      case 'C' :
	 buf.append("char");
	 break;
      case 'D' :
	 buf.append("double");
	 break;
      case 'F' :
	 buf.append("float");
	 break;
      case 'I' :
	 buf.append("int");
	 break;
      case 'J' :
	 buf.append("long");
	 break;
      case 'S' :
	 buf.append("short");
	 break;
      case 'V' :
	 buf.append("void");
	 break;
      case 'Z' :
	 buf.append("boolean");
	 break;
    }

   return idx+1;
}



}	// end of class IvyFormat



/* end of IvyFormat.java */
