/********************************************************************************/
/*										*/
/*		PebbleEvent.java						*/
/*										*/
/*	Implementation of an Event to drive the automata			*/
/*										*/
/********************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2005, Brown University, Providence, RI.				 *
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


package edu.brown.cs.ivy.pebble;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;



class PebbleEvent implements PebbleConstants.Event, Transferable, Serializable, PebbleConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		event_name;
private String		event_label;
private transient EventType event_type;
private transient Map<String,Object> event_params;
private String		next_state;
private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

PebbleEvent(String name,EventType type)
{
   event_name = name;
   event_type = type;
   event_params = new HashMap<String,Object>();
   event_label = null;

   next_state = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName() 		{ return event_name; }
void setName(String nm) 		{ event_name = nm; }

EventType getEventType()		{ return event_type; }

String getDefaultState()		{ return next_state; }
void setDefaultState(String s)		{ next_state = s; }

@Override public String getLabel()		{ return event_label; }
void setLabel(String s)
{
   if (s != null && s.length() == 0) event_label = null;
   else event_label = s;
}




/********************************************************************************/
/*										*/
/*	Parameter methods							*/
/*										*/
/********************************************************************************/

boolean getBooleanParam(String name)
{
   Object o = event_params.get(name);
   if (o != null && o instanceof Boolean) return ((Boolean) o).booleanValue();
   return false;
}


void setBooleanParam(String name,boolean v)
{
   event_params.put(name,(v ? Boolean.TRUE : Boolean.FALSE));
}



String getStringParam(String name)
{
   return (String) event_params.get(name);
}



void setStringParam(String name,String v)
{
   if (v == null) event_params.remove(name);
   else event_params.put(name,v);
}



String getMatchValue(String name)
{
   return (String) event_params.get(name);
}


void setMatchValue(String name,String v)
{
   if (v == null) event_params.remove(name);
   else event_params.put(name,v);
}



String getMatchMode(String name)
{
   return (String) event_params.get(name + "_MODE");
}



void setMatchMode(String name,String v)
{
   name = name + "_MODE";

   if (v == null) event_params.remove(name);
   else event_params.put(name,v);
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return event_name + ": " + event_label;
}


String getToolTipText()
{
   return "Event " + event_name + ": " + event_label;
}



/********************************************************************************/
/*										*/
/*	Drag and drop operations						*/
/*										*/
/********************************************************************************/

private static DataFlavor event_flavor = new DataFlavor(PebbleEvent.class,"Pebble Event");

@Override public Object getTransferData(DataFlavor df)		{ return this; }

@Override public DataFlavor [] getTransferDataFlavors()
{
   return new DataFlavor [] { event_flavor };
}

@Override public boolean isDataFlavorSupported(DataFlavor df)
{
   return df == event_flavor;
}



}	// end of class PebbleEvent




/* end of PebbleEvent.java */
