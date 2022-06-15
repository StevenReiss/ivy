/********************************************************************************/
/*                                                                              */
/*              IvyI18N.java                                                    */
/*                                                                              */
/*      Generic support for internationalization                                */
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



package edu.brown.cs.ivy.file;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class IvyI18N
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/


protected Locale use_locale = Locale.getDefault();
protected Map<String,ResourceBundle> known_bundles;

private static final String   DEFAULT_BUNDLE = "Labels";



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

IvyI18N()
{
   use_locale = Locale.getDefault();
   known_bundles = new HashMap<>();
   addBundle(DEFAULT_BUNDLE);
}


   
/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

public void setLocale(Locale lcl) 
{
   if (lcl == null) lcl = Locale.getDefault();
   use_locale = lcl;
   for (Map.Entry<String,ResourceBundle> ent : known_bundles.entrySet()) {
      String nm = ent.getKey();
      ent.setValue(ResourceBundle.getBundle(nm,lcl));
    }
}


public void addBundle(String nm)
{
   known_bundles.put(nm,ResourceBundle.getBundle(nm,use_locale));
}


public IvyI18N setDefaultBundle(String dflt)
{
   return new SubI18N(this,dflt);
}



/********************************************************************************/
/*                                                                              */
/*      Handle User Interface Text Mappings                                     */
/*                                                                              */
/********************************************************************************/

public String getString(String key)
{
   if (key == null) return null;
   
   return getString(DEFAULT_BUNDLE,key);
}



public String getString(String bndl,String key)
{
   if (key == null) return null;
   
   ResourceBundle bdl = known_bundles.get(bndl);
   if (bdl == null) {
      IvyLog.logX("BOARD","Bundle " + bndl + " unknown");
      return key;      
    }
   
   String s = bdl.getString(key);
   if (s != null) return s;
   
   IvyLog.logX("BOARD","Unknown i18n string for " + bndl + "." + key);
   
   return key;
}


/********************************************************************************/
/*                                                                              */
/*      Implementation that uses original, but with different default           */
/*                                                                              */
/********************************************************************************/

private static class SubI18N extends IvyI18N {

   private String default_bundle;
   private IvyI18N parent_i18n;
   
   SubI18N(IvyI18N par,String dflt) {
      default_bundle = dflt;
      parent_i18n = par;
    }
   
   @Override public void setLocale(Locale lcl) {
      throw new IllegalStateException("Can't set locale on for inner I18N");
    }
   
   @Override public String getString(String key) {
      return getString(default_bundle,key);
    }
   
   @Override public String getString(String bndl,String key) {
      ResourceBundle bdl = known_bundles.get(bndl);
      if (bdl != null && key != null) {
         String s = bdl.getString(key);
         if (s != null) return s;
       }
      return parent_i18n.getString(bndl,key);
    }
   
}       // end of innerclass SubI18N



}       // end of class IvyI18N




/* end of IvyI18N.java */

