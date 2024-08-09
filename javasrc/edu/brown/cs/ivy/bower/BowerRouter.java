/********************************************************************************/
/*                                                                              */
/*              BowerRouter.java                                                */
/*                                                                              */
/*      Handle routing within BOWER                                             */
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import edu.brown.cs.ivy.file.IvyLog;

public class BowerRouter implements BowerConstants, HttpHandler
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private BowerSessionManager session_manager;
private ArrayList<Route> route_interceptors;
private int preroute_index;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BowerRouter()
{
   session_manager = new BowerSessionManager();
   route_interceptors = new ArrayList<>();
   preroute_index = 0;
}



/********************************************************************************/
/*                                                                              */
/*      Routing methods                                                         */
/*                                                                              */
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
      BiFunction<HttpExchange,BowerSession,String> h)
{
   addHTTPInterceptor(new Route(method,url,h));
}


public void addPreRoute(String method,String url,
      BiFunction<HttpExchange,BowerSession,String> h)
{
   route_interceptors.add(preroute_index++,new Route(method,url,h));
}


public void addRoute(String method,
      BiFunction<HttpExchange,BowerSession,String> h)
{
   addHTTPInterceptor(new Route(method,null,h));
}

public void addHTTPInterceptor(Route r)
{
   route_interceptors.add(r);
}



/********************************************************************************/
/*                                                                              */
/*      Basic handlers                                                          */
/*                                                                              */
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
   
   IvyLog.logI("BOWER",String.format("REST %s %s %s %s %s %s %s",
         e.getRequestMethod(),
         e.getRequestURI().toString(),
         plist,
         e.getLocalAddress(),e.getPrincipal(),e.getProtocol(),
         e.getRemoteAddress().getAddress().getHostAddress()));
   
   return null;
}




public static String handleParameters(HttpExchange e)
{
   Map<String,List<String>> params = parseQueryParameters(e);
   if (!e.getRequestMethod().equals("GET")) {
      synchronized(params) {
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


/********************************************************************************/
/*                                                                              */
/*      Parameter access methods                                                */
/*                                                                              */
/********************************************************************************/

@SuppressWarnings("unchecked")
public static String getParameter(HttpExchange e,String name)
{
   try {
      Map<String, List<String>> map = (Map<String, List<String>>) e.getAttribute("paramMap");
      return (map).get(name).get(0);
    }
   catch (Exception err){
      return null;
    }
}


@SuppressWarnings("unchecked")
public static void setParameter(HttpExchange exchange,String name,String val)
{
   Map<String,List<String>> parameters = (Map<String,List<String>>) exchange.getAttribute("paramMap");
   synchronized(parameters){
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



/********************************************************************************/
/*                                                                              */
/*      Parameter management                                                    */
/*                                                                              */
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
      char buf[] = new char[512];
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
	       lparam.add(BowerUtil.unescape(arr.getString(i)));
	     }
	  }
	 else if (val instanceof Map<?,?>) {
	    Map<?,?> data = (Map<?,?>) val;
	    JSONObject mobj = new JSONObject(data);
	    String txt = mobj.toString();
            txt = BowerUtil.unescape(txt);
	    lparam.add(txt);
	  }
	 else if (val instanceof Iterable<?>) {
	    Iterable<?> ival = (Iterable<?>) val;
	    try {
	       JSONArray arr = new JSONArray(ival);
	       for (int i = 0; i < arr.length(); ++i) {
                  String v = arr.getString(i);
                  v = BowerUtil.unescape(v);
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
            txt = BowerUtil.unescape(txt);
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
/*                                                                              */
/*      Routing methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public void handle(HttpExchange e) throws IOException 
{
   for (Route interceptor : route_interceptors) {
      String resp = interceptor.handle(e);
      if (resp != null) {
         BowerServer.sendResponse(e,resp);
         return;
       }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Route representation                                                    */
/*                                                                              */
/********************************************************************************/

public interface IHandler<I,O> {
   O handle(I input);
}

private class Route {
   
   private int check_method;
   private String check_url;
   private Pattern check_pattern;
   private List<String> check_names;
   private IHandler<HttpExchange,String> route_handle;
   private BiFunction<HttpExchange,BowerSession,String> route_function;
   
   Route(String method,String url,IHandler<HttpExchange,String> handler) {
      this(method,url);
      route_handle = handler;
    }
   
   Route(String method,String url,
         BiFunction<HttpExchange,BowerSession,String> handler) {
      this(method,url);
      route_function = handler;
    }
   
   private Route(String method,String url) {
      if (method == null || method.equals("ALL")) check_method = -1;
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
         if (route_handle != null) {
            return route_handle.handle(exchange);
          }
         else if (route_function != null) {
            BowerSession cs = session_manager.findSession(exchange);
            return route_function.apply(exchange,cs);
          }
       }
      catch (Throwable t) {
         IvyLog.logE("BOWER","Problem handling input",t);
         return BowerServer.errorResponse(exchange,null,500,"Problem handling input: " + t);
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
         pat += "([A-Za-z_]+)";
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





}       // end of class BowerRouter




/* end of BowerRouter.java */

