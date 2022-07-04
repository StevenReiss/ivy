/********************************************************************************/
/*										*/
/*		LimboLine.java							*/
/*										*/
/*	Representative for a line location					*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/limbo/LimboLine.java,v 1.7 2017/04/14 23:39:02 spr Exp $ */


/*********************************************************************************
 *
 * $Log: LimboLine.java,v $
 * Revision 1.7  2017/04/14 23:39:02  spr
 * Fix ups to support limbo from bedrock.
 *
 * Revision 1.6  2011-05-27 19:32:41  spr
 * Change copyrights.
 *
 * Revision 1.5  2011-03-23 23:24:24  spr
 * Add force validate; clean up.
 *
 * Revision 1.4  2010-07-01 21:55:10  spr
 * Ensure we compare lines to lines
 *
 * Revision 1.3  2009-09-17 01:58:24  spr
 * Fixup limbo so its actually usable (by dyvise).
 *
 * Revision 1.2  2008-06-11 01:46:32  spr
 * Clean imports.
 *
 * Revision 1.1  2008-03-14 12:26:31  spr
 * Add limbo implementation (not testing of it).
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.limbo;


import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import org.w3c.dom.Element;

import java.io.File;



public abstract class LimboLine
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private   File	 file_name;
private   int	 line_number;
private   long	 file_dlm;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected LimboLine()
{
   file_name = null;
   line_number = -1;
   file_dlm = 0;
}



protected LimboLine(String file,int line)
{
   this(new File(file),line);
}


protected LimboLine(File file,int line)
{
   file = IvyFile.getCanonical(file);

   file_name = file;
   line_number = line;
   file_dlm = file.lastModified();
}



protected LimboLine(Element e)
{
   file_dlm = IvyXml.getAttrLong(e,"DLM");
   file_name = new File(IvyXml.getTextElement(e,"FILE"));
   line_number = IvyXml.getAttrInt(e,"LINE");
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public File getSourceFile()
{
   return file_name;
}

public String getSourceName()
{
   return file_name.getAbsolutePath();
}


public int getLine()
{
   return line_number;
}



protected void setFile(File f)
{
   file_name = f;
   file_dlm = f.lastModified();
}



protected void clearLine()
{
   line_number = -1;
}


protected void setLine(int ln)
{
   line_number = ln;
}


/********************************************************************************/
/*										*/
/*	Validation methods							*/
/*										*/
/********************************************************************************/

public void forceValidate()
{
   file_dlm = 0;
   revalidate();
}



public void revalidate()
{
   long ndlm = file_name.lastModified();

   if (file_dlm == ndlm) return;

   LimboFile lf = LimboFactory.findFile(file_name);

   localValidate(lf);

   file_dlm = ndlm;
}


protected abstract void localValidate(LimboFile lf);



/********************************************************************************/
/*										*/
/*	Comparison methods							*/
/*										*/
/********************************************************************************/

public int compareTo(LimboLine l)
{
   int fg = getSourceFile().compareTo(l.getSourceFile());
   if (fg != 0) return fg;
   return getLine() - l.getLine();
}




@Override public boolean equals(Object o)
{
   if (o instanceof LimboLine) {
      LimboLine l = (LimboLine) o;
      if (!getSourceFile().equals(l.getSourceFile())) return false;
      return getLine() == l.getLine();
    }
   return false;
}



@Override public int hashCode()
{
   int v = getSourceFile().hashCode();
   v += getLine();

   return v;
}




/********************************************************************************/
/*										*/
/*	I/O methods								*/
/*										*/
/********************************************************************************/


public String getXml()
{
   IvyXmlWriter xw = new IvyXmlWriter();
   writeXml(xw);
   return xw.toString();
}



public void writeXml(IvyXmlWriter xw)
{
   xw.begin("LIMBO");
   xw.field("LINE",line_number);
   xw.field("DLM",file_dlm);
   xw.textElement("FILE",getSourceName());
   localWriteXml(xw);
   xw.end("LIMBO");
}


protected abstract void localWriteXml(IvyXmlWriter xw);




/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

protected static int stringDiff(CharSequence s,CharSequence t)
{
   int n = s.length();
   int m = t.length();
   if (n == 0) return m;
   if (m == 0) return n;

   int [][] d = new int[n+1][m+1];
   for (int i = 0; i <= n; i++) d[i][0] = i;
   for (int j = 0; j <= m; j++) d[0][j] = j;

   for (int i = 1; i <= n; ++i) {
      char s_i = s.charAt(i-1);
      for (int j = 1; j <= m; ++j) {
	 char t_j = t.charAt (j - 1);
	 int cost = (s_i == t_j ? 0 : 1);
	 d[i][j] = min3(d[i-1][j]+1,d[i][j-1]+1,d[i-1][j-1]+cost);
       }
    }

   return d[n][m];
}



private static int min3(int a, int b, int c)
{
   if (b < a) a = b;
   if (c < a) a = c;
   return a;
}




}	// end of abstrace class LimboLine



/* end of LimboLine.java */
