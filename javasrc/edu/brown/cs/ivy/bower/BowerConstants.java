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

import java.util.function.BiFunction;

import com.sun.net.httpserver.HttpExchange;

import edu.brown.cs.ivy.bower.BowerRouter.IHandler;

public interface BowerConstants
{

/**
 *      Interface for managine load/store of sessions
 **/

interface BowerSessionStore {
   
   void saveSession(BowerSession  bs);
   BowerSession loadSession(String id);
   void removeSession(String id);
   
}


/**
 *      Functional event handler with Session and input data
 **/

interface BowerSessionHandler extends BiFunction<HttpExchange,BowerSession,String> {
   
   String apply(HttpExchange e,BowerSession bs);
   
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

