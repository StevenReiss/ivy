/********************************************************************************/
/*                                                                              */
/*              BowerSession.java                                               */
/*                                                                              */
/*      Interface defining a session                                            */
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

import org.json.JSONObject;

public interface BowerSession extends BowerConstants
{

boolean isValid();
String getSessionId();
void setupSession();
void setValue(String key,Object val);
Object getValue(String key);
BowerSessionStore<?> getSessionStore();



default String getStringValue(String key) 
{
   Object v = getValue(key);
   if (v == null) return null;
   return v.toString();
}
   
   
   
   
default JSONObject getJsonValue(String key)
{
   Object v = getValue(key);
   if (v == null) return null;
   if (v instanceof JSONObject) {
      return (JSONObject) v;
    }
   else if (v instanceof String) {
      return new JSONObject((String) v);
    }
   
   return null;
}
   
   
   
}       // end of interface BowerSession




/* end of BowerSession.java */

