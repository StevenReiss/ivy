/********************************************************************************/
/*                                                                              */
/*              BowerConstants.java                                             */
/*                                                                              */
/*      Constants for Brown Organized Web Environment Resource                  */
/*      This is a generic, simple, Java web server modeled on Express           */
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

import com.sun.net.httpserver.HttpExchange;

import edu.brown.cs.ivy.bower.BowerRouter.IHandler;
import edu.brown.cs.ivy.bower.BowerRouter.ISessionHandler;

public interface BowerConstants
{

/**
 *      String to return for deferred response
 **/

String BOWER_DEFERRED_RESPONSE = "*DEFER*";


/**
 *      Attributes for handling errors
 **/
String BOWER_RETURN_CODE = "*BOWER_RETURN_CODE*";
String BOWER_EXCEPTION = "*BOWER_EXCEPTION*";







/**
 *      Interface for managine load/store of sessions
 **/

interface BowerSessionStore<S extends BowerSession> {
   
   default String getSessionKey() { return "SESSION"; }
   String getSessionCookie();
   default String getStatusKey()        { return "STATUS"; }
   default String getErrorMessageKey()  { return "MESSAGE"; }
   default String getReturnCodeKey()    { return "RETURNCODE"; }

   S createNewSession();
   void saveSession(S  bs);
   S loadSession(String id);
   void removeSession(String id);
   default boolean validateSession(HttpExchange e,String sid) {
      return true;
    }
   
}


/**
 *      Functional event handler with Session and input data
 **/

@FunctionalInterface
interface BowerSessionHandler<S extends BowerSession> 
        extends ISessionHandler<HttpExchange,S,String> { 
   
   String handle(HttpExchange e,S bs);
   
}       // end of inner class BowerSessionHandler


/**
 *      Functional event handler without session data
 **/ 

interface BowerHandler extends IHandler<HttpExchange,String> {
   
   String handle(HttpExchange e);
   
}       // end of inner class BowerHandler



/**
 *      Mime Types
 **/

String	XML_MIME = "application/xml";
String	JSON_MIME = "application/json";
String	TEXT_MIME = "text/plain";
String	HTML_MIME = "text/html";
String	CSS_MIME = "text/css";
String	JS_MIME = "application/javascript";
String	SVG_MIME = "image/svg+xml";
String	PNG_MIME = "image/png";



}       // end of interface BowerConstants




/* end of BowerConstants.java */

