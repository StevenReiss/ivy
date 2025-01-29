/********************************************************************************/
/*                                                                              */
/*              BowerSession.java                                               */
/*                                                                              */
/*      Implementation of a generic session                                     */
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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public abstract class BowerSession implements BowerConstants
{



/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private long            last_used;
private long            expires_at;
private Map<String,Object> value_map;
private long            expire_delta;
private String          session_uid;

private static final long EXPIRE_DELTA = 1000*60*60*24*4;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BowerSession()
{
   last_used = System.currentTimeMillis();
   expires_at = 0;
   value_map = new HashMap<>();
   expire_delta = EXPIRE_DELTA;
   session_uid = "SESS_" + BowerUtil.randomString(24);  
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public void setExpireDelta(long d)
{
   expire_delta = d;
}


public boolean isValid()
{
   if (expires_at == 0) return true;
   long now = System.currentTimeMillis();
   if (now > expires_at) return false;
   return true;
}


public String getSessionId()
{
   return session_uid;
}


public void setupSession()
{
   last_used = System.currentTimeMillis();
   expires_at = last_used + expire_delta;
}



/********************************************************************************/
/*										*/
/*	Value maintenance							*/
/*										*/
/********************************************************************************/

public void setValue(String key,String val)
{
   value_map.put(key,val);
}



public Object getValue(String key)
{
   return value_map.get(key);
}


public String getStringValue(String key)
{
   Object v = value_map.get(key);
   if (v == null) return null;
   return v.toString();
}


public JSONObject getJsonValue(String key)
{
   Object v = value_map.get(key);
   if (v == null) return null;
   if (v instanceof JSONObject) {
      return (JSONObject) v;
    }
   else if (v instanceof String) {
      return new JSONObject((String) v);
    }
   
   return null;
}


public abstract BowerSessionStore<?> getSessionStore();



}       // end of class BowerSession




/* end of BowerSession.java */

