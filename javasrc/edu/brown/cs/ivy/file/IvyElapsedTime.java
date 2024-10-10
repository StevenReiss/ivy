/********************************************************************************/
/*										*/
/*		IvyElapsedTime.java						*/
/*										*/
/*	Elapsed Time Utility							*/
/*										*/
/********************************************************************************/
/*	Copyright 2010 Brown University -- Jarrell Travis Webb		      */
/*	Copyright 2015 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2015 Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/


package edu.brown.cs.ivy.file;

import java.util.Calendar;
import java.util.Date;

/**
 * This utility implementation helps compensate a sun oversight. Given
 * {@link Calendar} or {@link Date} ranges or elapsed milliseconds ({@link Long}
 * ) this can calculate elapsed days, hours, minutes, and seconds. It can even
 * print them out in a nice format. It converts eplased milliseconds into days,
 * hours, minutes, and seconds.
 *
 * @author jtwebb
 *
 */

public final class IvyElapsedTime {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private int elapsed_days;
private long elapsed_milliseconds;
private int elapsed_hours;
private int elapsed_minutes;
private int elapsed_seconds;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public IvyElapsedTime()
{
   this(0);
}



public IvyElapsedTime(long milliseconds)
{
   setElapsedMilliseconds(milliseconds);
}


public IvyElapsedTime(Calendar startDate, Calendar endDate)
{
   setRange(startDate, endDate);
}



public IvyElapsedTime(Date startDate, Date endDate)
{
   setRange(startDate, endDate);
}



/********************************************************************************/
/*										*/
/*	Computation methods							*/
/*										*/
/********************************************************************************/

public void addElapsedMilliseconds(long milliseconds)
{
   setElapsedMilliseconds(getElapsedMilliseconds() + milliseconds);
}



public void addRange(Calendar startDate, Calendar endDate)
{
   addRange(startDate.getTime(), endDate.getTime());
}



public void addRange(Date startDate, Date endDate)
{
   addElapsedMilliseconds(endDate.getTime() - startDate.getTime());
}


public void subtractElapsedMilliseconds(long milliseconds)
{
   setElapsedMilliseconds(getElapsedMilliseconds() - milliseconds);
}



public void subtractRange(Calendar startDate, Calendar endDate)
{
   subtractRange(startDate.getTime(), endDate.getTime());
}



public void subtractRange(Date startDate, Date endDate)
{
   subtractElapsedMilliseconds(endDate.getTime() - startDate.getTime());
}


/********************************************************************************/
/*										*/
/*	Setup and output methods						*/
/*										*/
/********************************************************************************/

@Override
public String toString() {
   return createString(false);
}


public String toString(boolean showSeconds) {
   return createString(showSeconds);
}




private String createString(boolean showSeconds)
{
   StringBuilder buf = new StringBuilder();

   String seconds = Integer.toString(getSeconds());
   String minutes = Integer.toString(getMinutes());
   String hours = Integer.toString(getHours());
   String days = Integer.toString(getDays());

   boolean needsComma = false;
   if (getDays() > 0) {
      buf.append(days).append(" day");
      if (getDays() != 1)
	 buf.append("s");
      needsComma = true;
    }
   if (getHours() > 0) {
      if (needsComma)
	 buf.append(", ");
      buf.append(hours).append(" hour");
      if (getHours() != 1)
	 buf.append("s");
      needsComma = true;
    }
   if (getMinutes() > 0) {
      if (needsComma)
	 buf.append(", ");
      buf.append(minutes).append(" minute");
      if (getMinutes() != 1)
	 buf.append("s");
      needsComma = true;
    }
   if (showSeconds && getSeconds() > 0) {
      if (needsComma)
	 buf.append(", ");
      buf.append(seconds).append(" second");
      if (getSeconds() != 1)
	 buf.append("s");
      needsComma = true;
    }

   return buf.toString();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public int getDays()
{
   return elapsed_days;
}



public long getElapsedMilliseconds()
{
   return elapsed_milliseconds;
}



public int getHours()
{
   return elapsed_hours;
}



public int getMinutes()
{
   return elapsed_minutes;
}



public int getSeconds()
{
   return elapsed_seconds;
}



private void setDays(int days)
{
   elapsed_days = days;
}




public void setElapsedMilliseconds(long milliseconds)
{
   elapsed_milliseconds = milliseconds;
   convert();
}



private void setHours(int hours)
{
   elapsed_hours = hours;
}



private void setMinutes(int minutes)
{
   elapsed_minutes = minutes;
}



public void setRange(Calendar startDate, Calendar endDate)
{
   setRange(startDate.getTime(), endDate.getTime());
}



public void setRange(Date startDate, Date endDate)
{
   setElapsedMilliseconds(endDate.getTime() - startDate.getTime());
}



private void setSeconds(int seconds) {
   elapsed_seconds = seconds;
}



private void convert()
{
   long time = getElapsedMilliseconds() / 1000;
   setSeconds((int) (time % 60));
   setMinutes((int) ((time % 3600) / 60));
   setHours((int) ((time % 86400) / 3600));
   setDays((int) time / 86400);
}



}	// end of class IvyElapsedTime




/* end of IvyElapsedTime.java */
