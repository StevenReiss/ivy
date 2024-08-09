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

class BowerSessionManager implements BowerConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<String,BowerSession> session_set;





/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BowerSessionManager()
{
   session_set = new HashMap<>();
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
   
   //parse for session cookie
   String sessionid = null;
   Map<String,HttpCookie> cookies = parseCookies(cookieHeaders);
   HttpCookie cookie = cookies.get(BowerServer.getSessionCookie());
   String c = (cookie == null ? null : cookie.toString());
   if (c != null) {
      if (c.substring(0, c.indexOf('=')).equals(BowerServer.getSessionCookie())) {
         sessionid = c.substring(c.indexOf('=') + 1, c.length() - 1);
       }
    }
   if (sessionid == null) {
      Map<String,List<String>> params = (Map<String,List<String>>) e.getAttribute("paramMap");
      if (params != null) {
         List<String> sparams = params.get(BowerServer.getSessionParameter());
         if (sparams != null) sessionid = sparams.get(0);
       }
    }
   else {
      BowerRouter.setParameter(e,BowerServer.getSessionParameter(),sessionid);
    }
   
   BowerSession cs = null;
   if (sessionid != null) cs = findSession(sessionid);
   if (cs != null && !cs.isValid()) cs = null;
   if (cs == null) cs = beginSession(e);
   
// if (cs != null) cs.saveSession(catre_control);
   
   return null;
}



private static Map<String, HttpCookie> parseCookies(List<String> cookieHeaders)
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

BowerSession beginSession(HttpExchange e)
{
   BowerSession cs = new BowerSession(); 
   String sid = cs.getSessionId();
   session_set.put(sid,cs);
// CatserveServer.setParameter(e,SESSION_PARAMETER,sid);
   
   int maxAge = 31536000; // Set the cookie to expire in one year
   String cookie = String.format("%s=%s; Path=%s; Max-Age=%d", 
         BowerServer.getSessionCookie(), sid, "/", maxAge);
   e.getResponseHeaders().add("Set-Cookie", cookie);
   
// cs.saveSession(catre_control);
   
   return cs;
}



String validateSession(HttpExchange e,String sid)
{
   return sid;
}



void endSession(String sid)
{
// BowerSession csi = session_set.remove(sid);
// if (csi != null) csi.removeSession(catre_control);
}



BowerSession findSession(HttpExchange e)
{
   String sid = BowerRouter.getParameter(e,BowerServer.getSessionParameter());
   if (sid == null) return null;
   
   return findSession(sid);
}



private BowerSession findSession(String sid)
{
   if (sid == null) return null;
   
   BowerSession csi = session_set.get(sid);
   if (csi != null) return csi;
   
// csi = (CatserveSessionImpl) catre_control.getDatabase().loadObject(sid);
   return csi;
}


}       // end of class BowerSessionManager




/* end of BowerSessionManager.java */

