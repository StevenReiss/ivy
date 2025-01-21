/********************************************************************************/
/*                                                                              */
/*              BowerServer.java                                                */
/*                                                                              */
/*      Main server and controller for BOWER web server                         */
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import edu.brown.cs.ivy.file.IvyLog;

import com.sun.net.httpserver.HttpsParameters;

public class BowerServer implements BowerConstants
{

// TODO: need to add an interface for loading, storing and removing sessions
// Probably also a default implementation of that interface
// Register this with the server and use in session manager.
// Session manager should be here and requested by the router
// Also, remove static fields so that one can have multiple servers


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private HttpServer      http_server;
private int             port_number;
private File            keystore_file;
private String          keystore_password;
private BowerRouter     http_router;
private HttpContext     router_context;
private Executor        task_executor;
private BowerSessionStore session_store;

private static String   session_parameter = "BOWERSESSION";
private static String   session_cookie = "Bower.Session";



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BowerServer(int port)
{
   port_number = port;
   http_server = null;
   keystore_file = null;
   keystore_password = null;
   http_router = null;
   router_context = null;
   task_executor = null;
   session_store = null;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public void setupHttps(File jksfile,String jkspwd)
{
   keystore_file = jksfile;
   keystore_password = jkspwd;
}


public void setPort(int port)
{
   port_number = port;
}


public BowerRouter getRouter()
{
   if (http_router == null) {
      setRouter(new BowerRouter(session_store));
    }
   return http_router;
}


public void setRouter(BowerRouter r)
{
   if (http_router != null) {
      if (http_server != null && router_context != null) {
         http_server.removeContext(router_context); 
       }
      http_router = null;
    } 
     
   http_router = r;
   if (r != null) {
      if (http_server != null) {
         router_context = http_server.createContext("/",r); 
       }
    }
}


public void setExecutor(Executor e) 
{
   task_executor = e;
   if (http_server != null && e != null) {
      http_server.setExecutor(e);
    }
}


public static void setSessionParameter(String nm)
{
   session_parameter = nm;
   if (session_cookie.equals("Bower.session")) {
      session_cookie = nm;
    }
}

public void setSessionStore(BowerSessionStore bss)
{
   session_store = bss;
}

static String getSessionParameter()
{
   return session_parameter;
}


public static void setSessionCookie(String nm)
{
   session_cookie = nm;
   if (session_parameter.equals("BOWERSESSION")) {
      String s = nm.toUpperCase();
      s = s.replace(".","");
      session_parameter = s;
    }
}

static String getSessionCookie()
{
   return session_cookie;
}


/********************************************************************************/
/*                                                                              */
/*      Run the http server                                                     */
/*                                                                              */
/********************************************************************************/

public boolean start()
{
   if (http_server == null) {
      if (!setup()) return false;
    }
   http_server.start();
   
   String what = "HTTPS";
   if (keystore_password == null) what = "HTTP";
   IvyLog.logD("BOWER",what + " server setup on port " + port_number);
   
   return true;
}



public boolean setup()
{
   InetSocketAddress iad = new InetSocketAddress(port_number);
   
   try {
      if (keystore_password != null && keystore_file != null) {
         HttpsServer sserver = HttpsServer.create(iad,0);
         char [] kpass = keystore_password.toCharArray();
         KeyStore keystore = KeyStore.getInstance("JKS");
         FileInputStream kis = new FileInputStream(keystore_file);
         keystore.load(kis,kpass);
         KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
         kmf.init(keystore,kpass);
         SSLContext sslcontext = SSLContext.getInstance("TLS");
         sslcontext.init(kmf.getKeyManagers(),null,null);
         sserver.setHttpsConfigurator(new Configurator(sslcontext));
         http_server = sserver;
       }
      else if (keystore_password == null) {
         http_server = HttpServer.create(iad,0);
       }
    }
   catch (Exception e) {
      http_server = null;
    }
   
   if (http_server == null) return false;
   
   if (http_router != null) {
      router_context = http_server.createContext("/",http_router); 
    }
   if (task_executor == null) {
      task_executor = new ThreadPoolExecutor(10,10,
            10,TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());
    }
   http_server.setExecutor(task_executor);
   
   return true;
}



private class Configurator extends HttpsConfigurator {
   
   Configurator(SSLContext ctx) {
      super(ctx);
    }
   
   @Override public void configure(HttpsParameters params) {
      params.setSSLParameters(getSSLContext().getDefaultSSLParameters());
    }
   
}       // end of inner class Configurator


/********************************************************************************/
/*                                                                              */
/*      Response handling                                                       */
/*                                                                              */
/********************************************************************************/

static void sendResponse(HttpExchange exchange,String response)
{
   int rcode = 200;
   if (response.startsWith("{") && response.contains("ERROR")) {
      try {
         JSONObject jresp = new JSONObject(response);
         rcode = jresp.optInt("RETURNCODE",500);
       }
      catch (Throwable t) { }
    }
   sendResponse(exchange,response,rcode);
}



static void sendResponse(HttpExchange exchange, String response,int rcode)
{
   IvyLog.logD("BOWER","Sending response: " + response);
   
   try {
      exchange.sendResponseHeaders(rcode, response.getBytes().length);
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
   catch (IOException e){
      System.err.println("(2) Error sending response to server, message: " + e.getMessage());
    }
}


public static String jsonResponse(JSONObject jo)
{
   if (jo.optString("STATUS",null) == null) {
      jo.put("STATUS","OK");
    }
   
   return jo.toString(2);
}


public static String jsonResponse(BowerSession cs,JSONObject jo)
{
   if (jo.optString("STATUS",null) == null) {
      jo.put("STATUS","OK");
    }
   if (jo.optString(session_parameter,null) == null) {
      jo.put(session_parameter,cs.getSessionId());
    }
   
   return jo.toString(2);
}


public static String jsonResponse(BowerSession cs, Object... val)
{
   Map<String,Object> map = new HashMap<>();
   if (cs != null) map.put(session_parameter, cs.getSessionId());
   
   for (int i = 0; i+1 < val.length; i += 2) {
      String key = val[i].toString();
      Object v = val[i+1];
      map.put(key,v);
    }
   
   if (map.get("STATUS") == null) map.put("STATUS","OK");
   
   JSONObject jo = new JSONObject(map);
   return jo.toString(2);
}


static String jsonError(BowerSession cs,String msg)
{
   IvyLog.logD("BOWER","JSONERROR " + msg);
   
   return jsonResponse(cs,"STATUS","FAIL","MESSAGE",msg);
}


static String jsonError(BowerSession cs, int status, String msg)
{
   IvyLog.logD("BOWER","JSONERROR " + status + " " + msg);
   
   return jsonResponse(cs,"STATUS","ERROR",
         "RETURNCODE",status,
         "MESSAGE",errorResponse(status, msg));
}


static String errorResponse(int status,String msg)
{
   IvyLog.logD("BOWER","ERROR " + status + " " + msg);
   
   return status + ": " + msg;
}



static String errorResponse(HttpExchange e,BowerSession cs,int status,String msg)
{
   Headers hdrs = e.getRequestHeaders();
   String acc =  hdrs.getFirst("Accept");
   if (acc != null && acc.toLowerCase().contains("json")) {
      return jsonError(cs,status,msg);
    }
   return errorResponse(status,msg);
}





}       // end of class BowerServer




/* end of BowerServer.java */

