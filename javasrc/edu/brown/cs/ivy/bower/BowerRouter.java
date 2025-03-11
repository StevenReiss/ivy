/********************************************************************************/
/*										*/
/*		BowerRouter.java						*/
/*										*/
/*	Handle routing within BOWER						*/
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import edu.brown.cs.ivy.file.IvyLog;

public class BowerRouter<UserSession extends BowerSession>
	implements BowerConstants, HttpHandler
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private BowerSessionManager<UserSession> session_manager;
private ArrayList<Route> route_interceptors;
private int preroute_index;
private Route error_handler;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BowerRouter(BowerSessionStore<UserSession> bss)
{
   session_manager = new BowerSessionManager<UserSession>(bss);
   route_interceptors = new ArrayList<>();
   preroute_index = 0;

   error_handler = new Route("ALL","*",BowerRouter::handleError);
}



/********************************************************************************/
/*										*/
/*	Routing methods 							*/
/*										*/
/********************************************************************************/

public void addRoute(String method,String url,IHandler<HttpExchange,String> h)
{
   addHTTPInterceptor(new Route(method,url,h));
}


public void addRoute(String method,IHandler<HttpExchange,String> h)
{
   addHTTPInterceptor(new Route(method,null,h));
}


public void addRoute(String method,String url,
      ISessionHandler<HttpExchange,UserSession,String> h)
{
   addHTTPInterceptor(new Route(method,url,h));
}


public void addPreRoute(String method,String url,
      ISessionHandler<HttpExchange,UserSession,String> h)
{
   route_interceptors.add(preroute_index++,new Route(method,url,h));
}


public void addRoute(String method,ISessionHandler<HttpExchange,UserSession,String> h)
{
   addHTTPInterceptor(new Route(method,null,h));
}

public void addHTTPInterceptor(Route r)
{
   route_interceptors.add(r);
}


public void addErrorHandler(IHandler<HttpExchange,String> h)
{
   error_handler = new Route("ALL","*",h);
}


public void addErrorHandler(ISessionHandler<HttpExchange,UserSession,String> h)
{
   error_handler = new Route("ALL",null,h);
}


/********************************************************************************/
/*										*/
/*	Basic handlers								*/
/*										*/
/********************************************************************************/

@SuppressWarnings("unchecked")
public static String handleLogging(HttpExchange e)
{
   Map<String,List<String>> params = (Map<String,List<String>>) e.getAttribute("paramMap");
   String plist = null;
   if (params != null) {
      synchronized (params) {
	 plist = params.toString();
       }
    }

   IvyLog.logI("BOWER",String.format("REST %s %s %s %s %s %s",
	 e.getRequestMethod(),
	 e.getRequestURI().toString(),
	 plist,
	 e.getProtocol(),
	 e.getRemoteAddress().getAddress().getHostAddress(),
	 new Date().toString()));

   return null;
}




public static String handleParameters(HttpExchange e)
{
   Map<String,List<String>> params = parseQueryParameters(e);
   if (!e.getRequestMethod().equals("GET")) {
      synchronized (params) {
	 try {
	    // Parse the request body and populate the filemap
	    parsePostParameters(e,params);
	  }
	 catch (IOException e_IO) {
	    return "Server Internal Error: Bad parameters: " + e_IO.getMessage();
	  }
       }
    }

   return null;
}


public String handleSessions(HttpExchange e)
{
   return session_manager.setupSession(e);
}


public static String handleBadUrl(HttpExchange e,BowerSession sm)
{													
   return errorResponse(e,sm,404,"Bad Url");
}


public static String handleError(HttpExchange e,BowerSession sm)
{
   return errorResponse(e,sm,500,"Internal Error");
}


public void endSession(String sid)
{
   session_manager.endSession(sid);
}



/********************************************************************************/
/*										*/
/*	Parameter access methods						*/
/*										*/
/********************************************************************************/

public static String getParameter(HttpExchange e,String name)
{
   List<String> vals = getParameterList(e,name);
   if (vals == null || vals.isEmpty()) return null;
   return vals.get(0);
}


public static int getIntParameter(HttpExchange he,String name)
{
   return getIntParameter(he,name,0);
}


public static int getIntParameter(HttpExchange he,String name,int dflt)
{
   String s = getParameter(he,name);
   if (s == null) return dflt;
   try {
      return Integer.parseInt(s);
    }
   catch (NumberFormatException e) { }
   return dflt;
}


public static Boolean getBooleanParameter(HttpExchange he,String name,Boolean dflt)
{
   String s = getParameter(he,name);
   if (s == null || s.isEmpty()) return dflt;
   if ("tT1yY".indexOf(s.charAt(0)) >= 0) return true;
   if ("fF0nN".indexOf(s.charAt(0)) >= 0) return false;
   return dflt;
}



@SuppressWarnings("unchecked")
public static <T extends Enum<T>> T getEnumParameter(HttpExchange he,String param,T dflt)
{
   String val = BowerRouter.getParameter(he,param);
   if (val == null || val.isEmpty()) return dflt;
   Object [] vals = dflt.getClass().getEnumConstants();
   if (vals == null) return null;
   Enum<?> v = dflt;
   for (int i = 0; i < vals.length; ++i) {
      Enum<?> e = (Enum<?>) vals[i];
      if (e.name().equalsIgnoreCase(val)) {
         v = e;
         break;
       }
    }
   
   return (T) v;
}


@SuppressWarnings("unchecked")
public static List<String> getParameterList(HttpExchange e,String name)
{
   try {
      Map<String, List<String>> map = (Map<String, List<String>>) e.getAttribute("paramMap");
      return (map).get(name);
    }
   catch (Exception err){
      return null;
    }
}


@SuppressWarnings("unchecked")
public static void setParameter(HttpExchange exchange,String name,String val)
{
   if (name == null) return;

   Map<String,List<String>> parameters = (Map<String,List<String>>) exchange.getAttribute("paramMap");
   synchronized (parameters){
      if (val == null) {
	 parameters.remove(name);
       }
      else {
	 parameters.put(name, Collections.singletonList(val));
       }
    }

   exchange.setAttribute("paramMap", parameters);
}


public static JSONObject getJson(HttpExchange exchange)
{
   String jsonstr = getParameter(exchange,"postData");
   if (jsonstr == null) return null;
   return new JSONObject(jsonstr);
}


public static JSONObject getJson(HttpExchange exchange,String fld)
{
   String jsonstr = getParameter(exchange,fld);
   if (jsonstr == null) return null;
   return new JSONObject(jsonstr);
}



public static String getAccessToken(HttpExchange exchange)
{
   String tok = getParameter(exchange,"access_token");
   if (tok == null) {
      String ahdr = exchange.getRequestHeaders().getFirst("Authorization");
      if (ahdr != null) {
	 if (ahdr.startsWith("Bearer ")) {
	    int idx = ahdr.indexOf(" ");
	    tok = ahdr.substring(idx+1).trim();
	  }
       }
    }
   return tok;
}


/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public static String deferredResponse()
{
   return BOWER_DEFERRED_RESPONSE;
}


public static String buildResponse(BowerSession session,String sts,Object... val)
{
   JSONObject jo;

   if (val.length == 1 && val[0] instanceof JSONObject) {
      jo = (JSONObject) val[0];
    }
   else {
      Map<String,Object> map = new HashMap<>();

      for (int i = 0; i+1 < val.length; i += 2) {
	 String key = val[i].toString();
	 Object v = val[i+1];
	 if (key != null) map.put(key,v);
       }

      jo = new JSONObject(map);
    }

   BowerSessionStore<?> bss = session.getSessionStore();
   String stskey = bss.getStatusKey();
   if (sts != null && stskey != null) {
      if (jo.optString(stskey,null) == null) {
	 jo.put(stskey,sts);
       }
    }

   String spar = bss.getSessionKey();
   String sid = session.getSessionId();
   if (spar != null && sid != null) {
      if (jo.optString(spar,null) == null) {
	 jo.put(spar,sid);
       }
    }

   return jo.toString(2);
}





public static String errorResponse(HttpExchange e,BowerSession cs,int status,String msg)
{
   Headers hdrs = e.getRequestHeaders();
   BowerSessionStore<?> bss = null;
   if (cs != null) {
      bss = cs.getSessionStore();
    }
   
   boolean dojson = false;
   List<String> acc =  hdrs.get("Accept");
   if (acc != null) {
      for (String s : acc) {
         if (s.toLowerCase().contains("json")) dojson = true;
         IvyLog.logD("BOWER","Check error header " + s + " " + dojson + " " + bss);
       }
    }
   else {
      for (String s : hdrs.keySet()) {
         IvyLog.logD("BOWER","Request header " + s);
       }
      IvyLog.logD("BOWER","No accept header in the request");
    }

   String text = status + ": " + msg;

   e.setAttribute(BOWER_RETURN_CODE,status);

   if (bss != null && dojson) {
      text = buildResponse(cs,"ERROR",bss.getReturnCodeKey(),status,
	    bss.getErrorMessageKey(),text);
    }

   return text;
}


public static String jsonOKResponse(BowerSession cs,Object... data)
{
  return buildResponse(cs,"OK",data);
}


public static String sendFileResponse(HttpExchange he,File f)
{
   BowerServer.sendFileResponse(he,f);

   return deferredResponse();
}


public static void finishResponse(HttpExchange he,BowerSession cs,String status,Object... data)
{
   finishResponse(he,cs,0,status,data);
}


public static void finishResponse(HttpExchange he,BowerSession cs,int rcode,String status,Object... data)
{
   BowerSessionStore<?> bss = cs.getSessionStore();

   String resp = buildResponse(cs,status,data);
   if (rcode == 0) {
      if (resp.startsWith("{")){
	 try {
	    JSONObject jo = new JSONObject(resp);
	    rcode = jo.getInt(bss.getReturnCodeKey());
	  }
	 catch (Throwable t) { }
       }
      if (rcode == 0) rcode = 200;
    }

   BowerServer.sendResponse(he,resp,rcode);
}



/********************************************************************************/
/*										*/
/*	Parameter management							*/
/*										*/
/********************************************************************************/

public static Map<String,List<String>> parseQueryParameters(HttpExchange exchange) {

   Map<String, List<String>> parameters = new HashMap<>();

   String query = exchange.getRequestURI().getQuery();
   if (query != null) {
      String[] pairs = query.split("&");
      for (String pair : pairs) {
	 String[] keyvalue = pair.split("=");
	 if (keyvalue.length != 2) continue;
	 String key = keyvalue[0];
	 String value = keyvalue[1];
	 value = BowerUtil.unescape(value);

	 // Check if the key already exists in the parameters map
	 List<String> values = parameters.getOrDefault(key, new ArrayList<String>());
	 values.add(value);

	 parameters.put(key, values);
       }
    }

   exchange.setAttribute("paramMap", parameters);

   return parameters;
}


public static boolean parsePostParameters(HttpExchange exchange,Map<String,List<String>> params) throws IOException
{
   Headers rqhdr = exchange.getRequestHeaders();
   String cnttype = rqhdr.getFirst("Content-Type");
   int cntlen = getBodySize(rqhdr);
   if (cnttype == null) return false;
   if (cntlen > 2*1024*1024) return false;
   String boundary = null;
   boolean json = false;
   if (cnttype.toLowerCase().startsWith("multipart/form-data")) {
      boundary = cnttype.split("boundary=")[1];
    }
   else if (cnttype.toLowerCase().contains("/json")) {
      json = true;
    }
   InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
   BufferedReader br = new BufferedReader(isr);
   if (boundary != null) {		   // multipart
      // TODO: handle multipart form data
    }
   else if (json) {
      if (cntlen == 0) cntlen = 2*1024*1024;
      String cnts = "";
      char [] buf = new char[512];
      while (cntlen > 0) {
	 int rln = Math.min(cntlen,512);
	 int aln = br.read(buf,0,rln);
	 if (aln <= 0) break;
	 cntlen -= aln;
	 cnts += new String(buf,0,aln);
       }
      cnts = cnts.trim();
      if (!cnts.startsWith("{")) return false;
      JSONObject obj = null;
      try {
	 obj = new JSONObject(cnts);
       }
      catch (JSONException e) {
	 IvyLog.logD("BOWER","Problem parsing json: " + e + ":\n" + cnts);
	 return false;
       }
      for (Map.Entry<String,Object> ent : obj.toMap().entrySet()) {
	 List<String> lparam = params.get(ent.getKey());
	 Object val = ent.getValue();
	 if (val == null) continue;
	 if (lparam == null) {
	    lparam = new ArrayList<>();
	    params.put(ent.getKey(),lparam);
	  }
	 if (val instanceof JSONArray) {
	    JSONArray arr = (JSONArray) val;
	    for (int i = 0; i < arr.length(); ++i) {
	       lparam.add(arr.getString(i));
	     }
	  }
	 else if (val instanceof Map<?,?>) {
	    Map<?,?> data = (Map<?,?>) val;
	    JSONObject mobj = new JSONObject(data);
	    String txt = mobj.toString();
	    lparam.add(txt);
	  }
	 else if (val instanceof Iterable<?>) {
	    Iterable<?> ival = (Iterable<?>) val;
	    try {
	       JSONArray arr = new JSONArray(ival);
	       for (int i = 0; i < arr.length(); ++i) {
		  String v = arr.getString(i);
		  lparam.add(v);
		}
	     }
	    catch (JSONException e) {
	       String txt = val.toString();
	       lparam.add(txt);
	     }
	  }
	 else {
	    String txt = val.toString();
	    lparam.add(txt);
	  }
       }
    }
   else {
      // handle application/x-www-form-urlencoded
      String query = br.readLine();
      if (query == null) return true;
      String[] keyValuePairs = query.split("&");
      for (String keyValue : keyValuePairs) {
	 String[] parts = keyValue.split("=");
	 if (parts.length == 2) {
	    String key = parts[0];
	    String value = parts[1];
	    value = BowerUtil.unescape(value);
	    List<String> lparam = params.get(key);
	    if (lparam == null) {
	       lparam = new ArrayList<>();
	       params.put(key,lparam);
	     }
	    lparam.add(value);
	  }
       }
    }

   return true;
}


private static int getBodySize(Headers hdrs)
{
   if (hdrs.containsKey("content-length")) {
      return Integer.parseInt(hdrs.getFirst("content-length"));
    }
   return 0;
}


/********************************************************************************/
/*										*/
/*	Routing methods 							*/
/*										*/
/********************************************************************************/

@Override public void handle(HttpExchange he)
{
   try {
      for (Route interceptor : route_interceptors) {
	 String resp = interceptor.handle(he);
	 if (resp != null) {
	    if (resp.equals(BOWER_DEFERRED_RESPONSE)) return;
	    BowerServer.sendResponse(he,resp);
	    return;
	  }
       }
      UserSession sess = session_manager.findSession(he);
      String resp1 = handleBadUrl(he,sess);
      BowerServer.sendResponse(he,resp1);
    }
   catch (Throwable t) {
      IvyLog.logE("BOWER","Problem handling input",t);
      he.setAttribute(BOWER_EXCEPTION,t);
      String resp = null;
      try {
	 resp = error_handler.handle(he);
       }
      catch (Throwable t1) {
	 IvyLog.logE("Problem with error handler",t1);
       }
      if (resp == null) resp = handleError(he,null);
      BowerServer.sendResponse(he,resp);
    }

}



/********************************************************************************/
/*										*/
/*	Route representation							*/
/*										*/
/********************************************************************************/

@FunctionalInterface
public interface IHandler<I,O> {
   O handle(I input);
}

@FunctionalInterface
public interface ISessionHandler<I,S,O> {
   O handle(I input,S session);
}

private class Route {

   private int check_method;
   private String check_url;
   private Pattern check_pattern;
   private List<String> check_names;
   private IHandler<HttpExchange,String> route_handle;
   private ISessionHandler<HttpExchange,UserSession,String> route_function;

   Route(String method,String url,IHandler<HttpExchange,String> handler) {
      this(method,url);
      route_handle = handler;
    }

   Route(String method,String url,
	 ISessionHandler<HttpExchange,UserSession,String> handler) {
      this(method,url);
      route_function = handler;
    }

   private Route(String method,String url) {
      if (method == null || method.equals("ALL") || method.equals("USE")) check_method = -1;
      else {
	 check_method = 0;
	 String[] ms = method.split(" ,;");
	 for (String mm : ms) {
	    int ordinal = getHttpMethodOrdinal(mm);
	    if (ordinal >= 0) check_method |= (1 << ordinal);
	  }
       }
      check_url = url;
      route_handle = null;
      route_function = null;
      check_pattern = null;
      check_names = null;
      setupPatterns();
    }

   public String handle(HttpExchange exchange) {
      int ordinal = getHttpMethodOrdinal(exchange.getRequestMethod());
      int v = 1 << ordinal;
   
      if ((v & check_method) == 0) return null;
   
      if (check_pattern != null) {
         Matcher m = check_pattern.matcher(exchange.getRequestURI().toString());
         if (!m.matches()) return null;
   
         int idx = 1;
         for (String s : check_names) {
            String p = m.group(idx++);
            setParameter(exchange,s,p);
          }
       }
      else if (check_url != null && !exchange.getRequestURI().toString().startsWith(check_url)) {
         return null;
       }
   
      try {
         if (check_url != null) exchange.setAttribute("BOWER_MATCH",check_url);
         if (check_pattern != null) exchange.setAttribute("BOWER_PATTERN",check_pattern);
         if (route_handle != null) {
            return route_handle.handle(exchange);
          }
         else if (route_function != null) {
            UserSession cs = session_manager.findSession(exchange);
            return route_function.handle(exchange,cs);
          }
       }
      catch (Throwable t) {
         IvyLog.logE("BOWER","Problem handling input",t);
         exchange.setAttribute(BOWER_EXCEPTION,t);
         UserSession cs = session_manager.findSession(exchange);
         return errorResponse(exchange,cs,500,"Problem handling input: " + t);
       }
   
      return null;
    }

   private void setupPatterns() {
      if (check_url == null || !check_url.contains(":")) return;
      check_names = new ArrayList<>();
      String u = check_url;
      String pat = "";
      for (int i = u.indexOf(":"); i >= 0; i = u.indexOf(":")) {
	 int j = u.indexOf("/",i);
	 pat += u.substring(0,i);
	 pat += "([^\\/]+)";
	 String nm = null;
	 if (j < 0) {
	    nm = u.substring(i+1);
	    u = "";
	  }
	 else {
	    nm = u.substring(i+1,j);
	    u = u.substring(j);
	  }
	 check_names.add(nm);
       }
      pat += u;
      check_pattern = Pattern.compile(pat);
    }

//gets the ordinal value for a given HTTP string;
   private int getHttpMethodOrdinal(String method) {
      switch (method) {
	 case "GET":
	    return 0;
	 case "POST":
	    return 1;
	 case "PUT":
	    return 2;
	 case "DELETE":
	    return 3;
	 case "HEAD":
	    return 4;
	 case "OPTIONS":
	    return 5;
	 case "TRACE":
	    return 6;
	 case "CONNECT":
	    return 7;
	 case "PATCH":
	    return 8;
	 default:
	    return -1;
       }
    }

}  // end of inner class Route





}	// end of class BowerRouter




/* end of BowerRouter.java */

