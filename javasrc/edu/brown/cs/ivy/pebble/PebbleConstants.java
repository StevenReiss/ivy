/********************************************************************************/
/*										*/
/*		PebbleConstants.java						*/
/*										*/
/*	Constants for use in PEBBLE program event based basic lang editor	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/pebble/PebbleConstants.java,v 1.10 2015/11/20 15:09:22 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PebbleConstants.java,v $
 * Revision 1.10  2015/11/20 15:09:22  spr
 * Reformatting.
 *
 * Revision 1.9  2007-05-04 02:00:28  spr
 * Fix bugs related to polling.
 *
 * Revision 1.8  2006/07/23 02:25:14  spr
 * Add support for action editing and creation.
 *
 * Revision 1.7  2006/07/10 14:52:23  spr
 * Code cleanup.
 *
 * Revision 1.6  2006/02/21 17:06:42  spr
 * Changes to Pebble to support external data models.
 *
 * Revision 1.5  2005/07/08 20:57:06  spr
 * Charles' upgrade to Pebble UI.
 *
 * Revision 1.4  2005/06/28 17:20:53  spr
 * UI enhancements (CAR)
 *
 * Revision 1.3  2005/06/07 02:18:20  spr
 * Update for java 5.0
 *
 * Revision 1.2  2005/04/29 18:15:26  spr
 * Add normalization and class type; fix minor problems.
 *
 * Revision 1.1  2005/04/28 21:48:16  spr
 * Initial release of the pebble automata editor.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.pebble;


import edu.brown.cs.ivy.petal.PetalConstants;

import java.util.Collection;



public interface PebbleConstants extends PetalConstants
{



/********************************************************************************/
/*										*/
/*	State Types								*/
/*										*/
/********************************************************************************/

enum StateType {
   NORMAL("Normal"),
      ACCEPT("Accept"),
      ERROR("Error"),
      IGNORE("Ignore");

   private String type_label;

   private StateType(String s)			{ type_label = s; }

   @Override public String toString()		       { return type_label; }
}



interface State {

   String getName();
   String getLabel();
   StateType getStateType();
   Iterable<Action> getActions();
   void addAction(Action a);
   void removeAction(Action a);

}	// end of interface State



/********************************************************************************/
/*										*/
/*	Transition types							*/
/*										*/
/********************************************************************************/

interface Transition {

   Collection<Event> getTransitionEvents();
   State getSourceState();
   State getTargetState();
   Iterable<Action> getActions();
   void addAction(Action a);
   void removeAction(Action a);

}	// end of interface Tranisition





/********************************************************************************/
/*										*/
/*	Action types								*/
/*										*/
/********************************************************************************/

interface Action {

   String getLabel();

}	// end of interface Action



/********************************************************************************/
/*										*/
/*	Event Types								*/
/*										*/
/********************************************************************************/

int PEBBLE_TYPE_NONE = 0;
int PEBBLE_TYPE_BOOLEAN = 1;
int PEBBLE_TYPE_STRING = 2;
int PEBBLE_TYPE_MATCH = 3;
int PEBBLE_TYPE_CLASS = 4;
int PEBBLE_TYPE_MULTI_MATCH = 5;



interface EventType {

   String getName();
   Iterable<EventProperty> getProperties();

}	// end of interface EventType


interface EventProperty {

   String getName();
   String getLabel();
   int getType();
   String getMatch();

}	// end of interface EventProperty




interface Event {
   String getName();
   String getLabel();
}	// end of interface Event



/********************************************************************************/
/*										*/
/*	Automata Properties							*/
/*										*/
/********************************************************************************/

interface AutomataProperty {

   String getName();
   String getLabel();
   int getType();
   boolean isMultiple();

}	// end of interface AutomataProperty




/********************************************************************************/
/*										*/
/*	Match types								*/
/*										*/
/********************************************************************************/

String [] PEBBLE_MATCH_TYPES = new String [] {
   "CHECK", "NEW", "UNIQUE", "NEWDEF", "UNIDEF"
};



}	// end of interface PebbleConstants




/* end of PebbleConstants.java */
