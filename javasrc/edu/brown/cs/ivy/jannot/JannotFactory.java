/********************************************************************************/
/*                                                                              */
/*              JannotFactory.java                                              */
/*                                                                              */
/*      Factory method for doing annotation processing                          */
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
import java.util.Map;

import edu.brown.cs.ivy.jcomp.JcompProject;

public final class JannotFactory implements JannotConstants
{



/********************************************************************************/
/*                                                                              */
/*      Static Methods                                                          */
/*                                                                              */
/********************************************************************************/

public static synchronized JannotFactory getFactory()
{
   if (the_factory == null) {
      the_factory = new JannotFactory();
    }
   return the_factory;
}




/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<JcompProject,JannotProject> project_map;

private static JannotFactory the_factory = null;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private JannotFactory()
{ 
   project_map = new HashMap<>();
}



/********************************************************************************/
/*                                                                              */
/*      Project access methods                                                  */
/*                                                                              */
/********************************************************************************/

public JannotProject getProject(JcompProject jp)
{
   synchronized (project_map) {
      JannotProject ap = project_map.get(jp);
      if (ap == null) {
         ap = new JannotProjectImpl(jp);
         project_map.put(jp,ap);
       }
      return ap;
    }
}




}       // end of class JannotFactory




/* end of JannotFactory.java */

