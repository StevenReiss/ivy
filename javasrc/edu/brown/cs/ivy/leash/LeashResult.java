/********************************************************************************/
/*                                                                              */
/*              LeashResult.java                                                */
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



package edu.brown.cs.ivy.leash;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class LeashResult implements LeashConstants, Comparable<LeashResult>
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private LeashConnection for_connection;
private File            result_file;
private List<Integer>   result_lines;
private List<Integer>   result_columns;
private double          result_score;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

LeashResult(LeashConnection conn,String file,String loc,double score)
{
   for_connection = conn;
   result_file = new File(file);
   result_score = score;
   result_lines = new ArrayList<>();
   result_columns = new ArrayList<>();
   
   for (StringTokenizer tok = new StringTokenizer(loc,";"); tok.hasMoreTokens(); ) {
      String loc1 = tok.nextToken();
      if (loc1.startsWith("slc:")) {
         loc1 = loc1.substring(4);
         int idx = loc1.indexOf(",");
         if (idx > 0) {
            result_lines.add(Integer.parseInt(loc1.substring(0,idx)));
            result_columns.add(Integer.parseInt(loc1.substring(idx+1)));
          }
         else {
            result_lines.add(Integer.parseInt(loc1));
            result_columns.add(0);
          }
       }
    }
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public File getFilePath()
{
   if (for_connection.isLocal()) return result_file;
   
   return null;
}


public List<Integer> getLines()
{
   return result_lines;
}


public List<Integer> getColumns()
{
   return result_columns;
}


public double getScore()
{
   return result_score;
}


public String getFileContents()
{
   if (for_connection.isLocal()) return null;
   
   return for_connection.getFileContents(result_file);
}

/********************************************************************************/
/*                                                                              */
/*      Comparator                                                              */
/*                                                                              */
/********************************************************************************/

@Override public int compareTo(LeashResult r)
{
   return Double.compare(r.result_score,result_score);
}



}       // end of class LeashResult




/* end of LeashResult.java */

