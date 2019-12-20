/********************************************************************************/
/*                                                                              */
/*              IvyStringDiff.java                                              */
/*                                                                              */
/*      String Differencing Algorithms                                          */
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



public abstract class IvyStringDiff
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static double           delete_cost = 1.0;
private static double           insert_cost = 1.0;
private static double           transpose_cost = 1.0;
private static double           caps_cost = 0.5;



/********************************************************************************/
/*                                                                              */
/*      Basic string difference method                                          */
/*                                                                              */
/********************************************************************************/

public static double stringDiff(CharSequence s,CharSequence t)
{
   int n = s.length();
   int m = t.length();
   if (n == 0) return m;
   if (m == 0) return n;
   
   double [][] d = new double[n+1][m+1];
   for (int i = 0; i <= n; i++) d[i][0] = i;
   for (int j = 0; j <= m; j++) d[0][j] = j;
   
   for (int i = 1; i <= n; ++i) {
      char s_i = s.charAt(i-1);
      for (int j = 1; j <= m; ++j) {
	 char t_j = t.charAt (j - 1);
         double cost = subCost(s_i,t_j);
         double del = d[i-1][j] + delete_cost;
         double ins = d[i][j-1] + insert_cost;
         double sub = d[i-1][j-1]+cost;
         d[i][j] = min3(del,ins,sub);
         if (i > 1 && j > 1 && s_i == t.charAt(j-2) && s.charAt(i-2) == t_j) {
            double trns = d[i-2][j-2] + transpose_cost;
            d[i][j] = Math.min(d[i][j],trns);
          }

       }
    }
   
   return d[n][m];
}


/**
 *      return a number from 0 to 1 giving relative difference.  This
 *      is 1 when strings are identical and 0 if totally different.
 *
 **/

 public static double normalizedStringDiff(CharSequence s,CharSequence t)
{
   double v = stringDiff(s,t);
   int n = s.length();
   int m = t.length();
   double mxl = Math.max(n,m);
   if (mxl == 0) return 0;
   return 1 - v/mxl;
}




private static double min3(double a, double b, double c)
{
   if (b < a) a = b;
   if (c < a) a = c;
   return a;
}


private static double subCost(char a,char b)
{
   if (a == b) return 0;
   if (Character.toUpperCase(a) == Character.toUpperCase(b)) return caps_cost;
   if (a == '_' && b == '-') return caps_cost;
   if (b == '_' && a == '-') return caps_cost;
   // add other common typos
   
   return 1;
}



/********************************************************************************/
/*                                                                              */
/*      Tuning methods                                                          */
/*                                                                              */
/********************************************************************************/

public void setCosts(double ins,double del,double trns,double caps)
{
   insert_cost = ins;
   delete_cost = del;
   transpose_cost = trns;
   caps_cost = caps;
}



}       // end of class IvyStringDiff




/* end of IvyStringDiff.java */

