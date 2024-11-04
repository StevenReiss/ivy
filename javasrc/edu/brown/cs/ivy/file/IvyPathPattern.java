/********************************************************************************/
/*                                                                              */
/*              IvyPathPattern.java                                             */
/*                                                                              */
/*      Handle path patterns (*, **, ? in partial paths)                        */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.ivy.file;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IvyPathPattern
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Pattern match_pattern;


      
/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public IvyPathPattern(String pattern) 
{
   match_pattern = makeRegex(pattern);
}

public IvyPathPattern(File pattern)
{
   match_pattern = makeRegex(pattern.getPath());
}



/********************************************************************************/
/*                                                                              */
/*      Matching methods                                                        */
/*                                                                              */
/********************************************************************************/

public boolean doesMatch(File f)
{
   String p = f.getAbsolutePath();
   return doesMatch(p);
}


public boolean doesMatch(String path)
{
   Matcher m = match_pattern.matcher(path);
   return m.matches();
}


/********************************************************************************/
/*                                                                              */
/*      Convert file pattern to regex                                           */
/*                                                                              */
/********************************************************************************/

private Pattern makeRegex(String pat)
{
   StringBuffer buf = new StringBuffer();
   boolean lastsep = false;
   for (int i = 0; i < pat.length(); ++i) {
      lastsep = false;
      char c = pat.charAt(i);
      switch (c) {
         case '.' :
            buf.append("\\.");
            break;
         case '?' :
            buf.append("[^/\\\\]");
            break;
         case '*' :
            if (i+1 < pat.length() && pat.charAt(i+1) == '*') {
               ++i;
               lastsep = true;
               buf.append("(.*)");
             }
            else {
               buf.append("[^/\\\\]*");
             }
            break;
         case '/' :
         case '\\' :
            buf.append("\\");
            buf.append(File.separator);
            lastsep = true;
            break;
         default :
            buf.append(c);
            break;  
       }
    }
   if (!lastsep) {
      buf.append("(\\");
      buf.append(File.separator);
      buf.append("(.*))?");
    }
   
   return Pattern.compile(buf.toString());
}



/********************************************************************************/
/*                                                                              */
/*      Debugging methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public String toString()
{
   return match_pattern.toString();
}



}       // end of class IvyPathPattern




/* end of IvyPathPattern.java */

