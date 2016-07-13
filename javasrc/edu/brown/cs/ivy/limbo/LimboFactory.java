/********************************************************************************/
/*										*/
/*		LimboFactory.java						*/
/*										*/
/*	Method for creating LimboLines						*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Redistribution and use in source and binary forms, with or without           *
 *  modification, are permitted provided that the following conditions are met:  *
 *                                                                               *
 *  + Redistributions of source code must retain the above copyright notice,     *
 *      this list of conditions and the following disclaimer.                    *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *      this list of conditions and the following disclaimer in the              *
 *      documentation and/or other materials provided with the distribution.     *
 *  + Neither the name of the Brown University nor the names of its              *
 *      contributors may be used to endorse or promote products derived from     *
 *      this software without specific prior written permission.                 *
 *                                                                               *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   *
 *  POSSIBILITY OF SUCH DAMAGE.                                                  *
 *                                                                               *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/limbo/LimboFactory.java,v 1.8 2011-05-27 19:32:41 spr Exp $ */


/*********************************************************************************
 *
 * $Log: LimboFactory.java,v $
 * Revision 1.8  2011-05-27 19:32:41  spr
 * Change copyrights.
 *
 * Revision 1.7  2011-03-23 23:24:24  spr
 * Add force validate; clean up.
 *
 * Revision 1.6  2010-08-06 12:40:54  spr
 * Allow files to be deleted from table.
 *
 * Revision 1.5  2009-09-17 01:58:24  spr
 * Fixup limbo so its actually usable (by dyvise).
 *
 * Revision 1.4  2008-06-11 01:46:32  spr
 * Clean imports.
 *
 * Revision 1.3  2008-03-14 12:26:31  spr
 * Add limbo implementation (not testing of it).
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.limbo;



import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;



public class LimboFactory
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private File	cur_file;

static Map<File,LimboFile> file_map = new HashMap<File,LimboFile>();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public LimboFactory()
{
   cur_file = null;
}



/********************************************************************************/
/*										*/
/*	Creation methods							*/
/*										*/
/********************************************************************************/

public static LimboLine createLine(String file,int line)
{
   return createLine(new File(file),line);
}


public static LimboLine createLine(File file,int line)
{
   return new LimboBestLine(findFile(file),line);
}


public void setFile(String file)
{
   setFile(new File(file));
}



public void setFile(File file)
{
   try {
      file = file.getCanonicalFile();
    }
   catch (IOException e) { }
   cur_file = file;
}




public LimboLine createLine(int line)
{
   if (cur_file == null) return null;

   return createLine(cur_file,line);
}


public static LimboLine createFromXml(String xml)
{
   Element e = IvyXml.convertStringToXml(xml);
   if (e == null) return null;

   return createFromXml(e);
}


public static LimboLine createFromXml(Element e)
{
   return new LimboBestLine(e);
}



public static void removeFile(File f)
{
   try {
      f = f.getCanonicalFile();
    }
   catch (IOException e) { }

   file_map.remove(f);
}



/********************************************************************************/
/*										*/
/*	Methods for managing LimboFiles 					*/
/*										*/
/********************************************************************************/

static LimboFile findFile(File f)
{
   try {
      f = f.getCanonicalFile();
    }
   catch (IOException e) { }

   LimboFile lf = file_map.get(f);
   if (lf == null || f.lastModified() > lf.getLastModified()) {
      // might want to remove files from the map use LRU at this point
      lf = new LimboFile(f);
      file_map.put(f,lf);
    }

   return lf;
}



}	// end of class LimboFactory



/* end of LimboFactory.java */

