/********************************************************************************/
/*										*/
/*		BowerCORS.java							*/
/*										*/
/*	Handle CORS policies							*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
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
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.ivy.bower;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import edu.brown.cs.ivy.bower.BowerConstants.BowerHandler;
import edu.brown.cs.ivy.file.IvyLog;

public class BowerCORS implements BowerConstants, BowerHandler
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String	access_origin;
private String	allow_methods;
private String	allow_headers;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BowerCORS(String access)
{
   access_origin = access;
   allow_methods = "GET POST PUT DELETE OPTIONS";
   allow_headers = "Content-Type, Authorization";
}



/********************************************************************************/
/*										*/
/*	Access methnods 							*/
/*										*/
/********************************************************************************/

public void setAllowMethods(String s)
{
   allow_methods = s;
}


public void setAllowHeaders(String s)
{
   allow_headers = s;
}



/********************************************************************************/
/*										*/
/*	Handling routine							*/
/*										*/
/********************************************************************************/

public String handle(HttpExchange exchange)
{
   if (access_origin == null) return null;

   Headers hdrs = exchange.getResponseHeaders();
   hdrs.add("Access-Control-Allow-Origin",access_origin);
   IvyLog.logD("BOWER","Add access response header " + access_origin);

   if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
      if (allow_methods != null) {
	 hdrs.add("Access-Control-Allow-Methods",allow_methods);
       }
      if (allow_headers != null) {
	 hdrs.add("Access-Control.Allow-Headers",allow_headers);
       }
      exchange.setAttribute(BOWER_RETURN_CODE,204);
      return "";
    }

   return null;
}



}	// end of class BowerCORS




/* end of BowerCORS.java */

