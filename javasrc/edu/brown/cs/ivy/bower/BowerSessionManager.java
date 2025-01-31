/********************************************************************************/
/*                                                                              */
/*              BowerSessionManager.java                                        */
/*                                                                              */
/*      Session manager for BOWER web server                                    */
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

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import edu.brown.cs.ivy.file.IvyLog;

class BowerSessionManager<UserSession extends BowerSession> implements BowerConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<String,UserSession> session_set;
private BowerSessionStore<UserSession> session_store;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BowerSessionManager(BowerSessionStore<UserSession> bss)
{
   session_set = new HashMap<>();
   session_store = bss;
}



/********************************************************************************/
/*                                                                              */
/*      Setup a session                                                         */
/*                                                                              */
/********************************************************************************/

@SuppressWarnings("unchecked")
String setupSession(HttpExchange e)
{
   Headers requestHeaders = e.getRequestHeaders();
   List<String> cookieHeaders = requestHeaders.get("Cookie"); 
   
   String cookiename = session_store.getSessionCookie();
   String paramname = session_store.getSessionKey();
   //parse for session cookie
   String sessionid = null;
   Map<String,HttpCookie> cookies = parseCookies(cookieHeaders);
   HttpCookie cookie = cookies.get(cookiename);
   String c = (cookie == null ? null : cookie.toString());
   if (c != null) {
      if (c.substring(0, c.indexOf('=')).equals(cookiename)) {
         sessionid = c.substring(c.indexOf('=') + 1, c.length() - 1);
       }
    }
   if (sessionid == null) {
      sessionid = BowerRouter.getParameter(e,paramname);
    }
   if (sessionid == null) {
      // get value from JSON paramemters
      Map<String,List<String>> params = (Map<String,List<String>>) e.getAttribute("paramMap");
      if (params != null) {
         List<String> sparams = params.get(paramname);
         if (sparams != null) sessionid = sparams.get(0);
       }
    }
   if (sessionid == null) {
      // get value from URL query
      String q = e.getRequestURI().getQuery();
      if (q != null) {
         String [] pairs = q.split("&");
         for (String pair : pairs) {
            String[] keyvalue = pair.split("=");
            if (keyvalue.length != 2) continue;
            String key = keyvalue[0];
            if (key.equals(paramname)) {
               sessionid = BowerUtil.unescape(keyvalue[1]);
               break;
             }
          }
       }
    }
   
   BowerRouter.setParameter(e,paramname,sessionid);
   
   UserSession cs = null;
   if (sessionid != null) cs = findSession(sessionid);
   if (cs != null && !cs.isValid()) cs = null;
   if (cs == null) cs = beginSession(e);
   
   return null;
}



private static Map<String,HttpCookie> parseCookies(List<String> cookieHeaders)
{
   Map<String, HttpCookie> returnMap = new HashMap<>();
   if (cookieHeaders != null) {
      for (String h : cookieHeaders) {
         String[] headers = h.split(";\\s");
         for (String header : headers) {
            try {
               List<HttpCookie> cookies = HttpCookie.parse(header);
               for (HttpCookie cookie : cookies) {
                  returnMap.put(cookie.getName(), cookie);
                }
             }
            catch (Throwable t) {
               IvyLog.logD("BOWER","Problem parsing cookies");
             }
          }
       }
    }
   return returnMap;
}




/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

UserSession beginSession(HttpExchange e)
{
   UserSession cs = session_store.createNewSession(); 
   String sid = cs.getSessionId();
   session_set.put(sid,cs);
   BowerRouter.setParameter(e,session_store.getSessionKey(),sid);
   
   int maxAge = 31536000; // Set the cookie to expire in one year
   String cookie = String.format("%s=%s; Path=%s; Max-Age=%d", 
         session_store.getSessionCookie(), sid, "/", maxAge);
   e.getResponseHeaders().add("Set-Cookie", cookie);
   
   session_store.saveSession(cs);
   
   return cs;
}



String validateSession(HttpExchange e,String sid)
{
   return sid;
}



void endSession(String sid)
{
   BowerSession csi = session_set.remove(sid);
   if (csi != null && session_store != null) {
      session_store.removeSession(sid);
    }
}



UserSession findSession(HttpExchange e)
{
   String sid = BowerRouter.getParameter(e,session_store.getSessionKey());
   if (sid == null) return null;
   
   return findSession(sid);
}



private UserSession findSession(String sid)
{
   if (sid == null) return null;
   
   UserSession csi = session_set.get(sid);
   if (csi != null) return csi;
   
   csi = session_store.loadSession(sid);
   
   return csi;
}


}       // end of class BowerSessionManager




/* end of BowerSessionManager.java */

