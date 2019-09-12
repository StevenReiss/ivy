/********************************************************************************/
/*                                                                              */
/*              JannotFiler.java                                                */
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

class JannotFiler implements JannotConstants, Filer
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<String,JannotFileObject> open_files;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotFiler()
{
   open_files = new HashMap<>();
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

List<JannotFileObject> getWrittenFiles()
{
   List<JannotFileObject> objs = new ArrayList<>();
   for (Iterator<JannotFileObject> it = open_files.values().iterator(); it.hasNext(); ) {
      JannotFileObject jfo = it.next();
      // might want to check if jfo is closed
      objs.add(jfo);
      it.remove();
    }
   return objs;
}





/********************************************************************************/
/*                                                                              */
/*      Processing methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override public JavaFileObject createClassFile(CharSequence name,Element ... elts)
        throws IOException
{
   throw new IOException("Class file creation not supported");
}


@Override public FileObject createResource(JavaFileManager.Location loc,
      CharSequence module,CharSequence name,Element ... elts) throws IOException
{
   throw new IOException("Resource file creation not supported");
}


@Override public FileObject getResource(JavaFileManager.Location loc,
      CharSequence module,CharSequence name) throws IOException
{
   throw new IOException("Resource file creation not supported");
}

@Override public JavaFileObject createSourceFile(CharSequence name,Element ... elts)
        throws IOException
{
   try {
      URI uri = new URI(name.toString());
      JannotFileObject fobj = new JannotFileObject(uri,JavaFileObject.Kind.SOURCE,elts);
      if (open_files.get(name) != null) throw new FilerException("File altready exists");
      open_files.put(name.toString(),fobj);
      return fobj;
    }
   catch (URISyntaxException e) { }
   
   return null;
}




}       // end of class JannotFiler




/* end of JannotFiler.java */

