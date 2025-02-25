/********************************************************************************/
/*                                                                              */
/*              BowerStatic.java                                                */
/*                                                                              */
/*      Provide access to static files                                          */
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



package edu.brown.cs.ivy.bower;

import java.io.File;
import java.net.URI;

import com.sun.net.httpserver.HttpExchange;

import edu.brown.cs.ivy.bower.BowerConstants.BowerHandler;

public class BowerStatic implements BowerConstants, BowerHandler
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private File base_directory;
private String attr_name;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BowerStatic(String path)
{
   base_directory = new File(path);
   attr_name = null;
}

BowerStatic(String path,String attr)
{
   base_directory = new File(path);
   attr_name = attr;
}



/********************************************************************************/
/*                                                                              */
/*      Handler for files                                                       */
/*                                                                              */
/********************************************************************************/

@Override public String handle(HttpExchange he)
{
   String pfx = (String) he.getAttribute("BOWER_MATCH");
   String filepath = null;
   if (attr_name == null && pfx != null) {
      URI uri = he.getRequestURI();
      String path = uri.getPath();
      if (path.startsWith(pfx)) {
         filepath = path.substring(pfx.length());
       }
    }
   else if (attr_name != null) {
      filepath = BowerRouter.getParameter(he,attr_name);
    }
   
   if (filepath == null) return null;
   
   int idx = filepath.indexOf("?");
   if (idx > 0) filepath = filepath.substring(0,idx);
   
   File f1 = base_directory;
   String [] comps = filepath.split("\\/");
   if (comps.length == 0) return null;
   for (int i = 0; i < comps.length; ++i) {
      String c = comps[i];
      if (c.equals("..")) c = "XXXXXXXX";
      f1 = new File(f1,c);
    }
   if (!f1.exists()) return null;
   
   if (!f1.exists()) {
      return null;
    }
   
   return BowerRouter.sendFileResponse(he,f1);
}




}       // end of class BowerStatic




/* end of BowerStatic.java */

