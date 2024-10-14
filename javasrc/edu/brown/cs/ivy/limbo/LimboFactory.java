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



package edu.brown.cs.ivy.limbo;



import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.xml.IvyXml;

import org.w3c.dom.Element;

import java.io.File;
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

private static Map<File,LimboFile> file_map = new HashMap<>();



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
   file = IvyFile.getCanonical(file);
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
   f = IvyFile.getCanonical(f);

   file_map.remove(f);
}



/********************************************************************************/
/*										*/
/*	Methods for managing LimboFiles 					*/
/*										*/
/********************************************************************************/

static LimboFile findFile(File f)
{
   f = IvyFile.getCanonical(f);

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

