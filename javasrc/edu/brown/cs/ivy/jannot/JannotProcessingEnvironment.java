/********************************************************************************/
/*                                                                              */
/*              JannotProcessingEnvironment.java                                */
/*                                                                              */
/*      description of class                                                    */
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



package edu.brown.cs.ivy.jannot;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import edu.brown.cs.ivy.jcomp.JcompProject;


public class JannotProcessingEnvironment implements JannotConstants, ProcessingEnvironment
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private JannotMessager our_messager;
private JannotFiler our_filer;
private JannotElementUtils element_utils;
private JannotTypeUtils type_utils;
private Map<String,String> option_map;
private JcompProject for_project;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotProcessingEnvironment(JcompProject jp)
{
   our_messager = new JannotMessager();
   our_filer = new JannotFiler();
   option_map = new HashMap<>();
   element_utils = new JannotElementUtils(this);
   type_utils = new JannotTypeUtils(this);
   for_project = jp;
}


/********************************************************************************/
/*                                                                              */
/*      Local access methods                                                    */
/*                                                                              */
/********************************************************************************/

JcompProject getJcompProject()
{
   return for_project;
}



/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

void defineOption(String name,String value)
{
   option_map.put(name,value);
}



/********************************************************************************/
/*                                                                              */
/*      Local methods                                                           */
/*                                                                              */
/********************************************************************************/

boolean haveErrors()
{
   return our_messager.haveErrors();
}



/********************************************************************************/
/*                                                                              */
/*      Processing Environment Methods                                          */
/*                                                                              */
/********************************************************************************/

@Override public Elements getElementUtils()
{
   return element_utils;
}


@Override public Types getTypeUtils()
{
   return type_utils;
}


@Override public Filer getFiler()
{
   return our_filer;
}


@Override public Locale getLocale()
{
   return Locale.getDefault();
}


@Override public Messager getMessager()
{
   return our_messager;
}


@Override public Map<String,String> getOptions()
{
   return option_map;
}


@Override public SourceVersion getSourceVersion()
{
   return SourceVersion.RELEASE_10;
}



/********************************************************************************/
/*                                                                              */
/*      Messager implementation                                                 */
/*                                                                              */
/********************************************************************************/

private static class JannotMessager implements Messager {
   
   private boolean have_errors;
   
   JannotMessager() {
      have_errors = false;
    }
   
   boolean haveErrors()                         { return have_errors; }
   
   @Override public void printMessage(Diagnostic.Kind kind,CharSequence msg) {
      if (kind == Diagnostic.Kind.ERROR) have_errors = true;
    }
   
   @Override public void printMessage(Diagnostic.Kind kind,CharSequence msg,Element e) {
      if (kind == Diagnostic.Kind.ERROR) have_errors = true;
    }
   
   @Override public void printMessage(Diagnostic.Kind kind,CharSequence msg,Element e,
         AnnotationMirror a) {
      if (kind == Diagnostic.Kind.ERROR) have_errors = true;
    }
   
   @Override public void printMessage(Diagnostic.Kind kind,CharSequence msg,Element e,
         AnnotationMirror a,AnnotationValue v) {
      if (kind == Diagnostic.Kind.ERROR) have_errors = true;
    }
   
}       // end of inner class JannotMessager




}       // end of class JannotProcessingEnvironment




/* end of JannotProcessingEnvironment.java */

