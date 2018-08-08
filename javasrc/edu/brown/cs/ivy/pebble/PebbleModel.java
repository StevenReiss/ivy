/********************************************************************************/
/*										*/
/*		PebbleModel.java						*/
/*										*/
/*	Model describing the events, conditions, & actions allowed on automata	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/pebble/PebbleModel.java,v 1.12 2018/08/02 15:10:34 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PebbleModel.java,v $
 * Revision 1.12  2018/08/02 15:10:34  spr
 * Fix imports.
 *
 * Revision 1.11  2010-02-12 00:38:28  spr
 * No change.
 *
 * Revision 1.10  2006/07/23 02:25:14  spr
 * Add support for action editing and creation.
 *
 * Revision 1.9  2006/07/10 14:52:23  spr
 * Code cleanup.
 *
 * Revision 1.8  2006/02/21 17:06:42  spr
 * Changes to Pebble to support external data models.
 *
 * Revision 1.7  2006/01/30 19:06:16  spr
 * Formatting cleanup.
 *
 * Revision 1.6  2005/07/08 20:57:06  spr
 * Charles' upgrade to Pebble UI.
 *
 * Revision 1.5  2005/06/28 17:20:53  spr
 * UI enhancements (CAR)
 *
 * Revision 1.4  2005/06/07 02:18:21  spr
 * Update for java 5.0
 *
 * Revision 1.3  2005/05/07 22:25:41  spr
 * Updates for java 5.0
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


import java.awt.Component;
import java.util.Collection;




public interface PebbleModel extends PebbleConstants {



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

boolean supportsEditEvents();

Collection<String> getEventTypes();
EventType getEventType(String nm);


boolean getUsesConditions();
boolean getUsesTransitionActions();
boolean getUsesStateActions();

Collection<AutomataProperty> getAttributeSet();



/********************************************************************************/
/*										*/
/*	Checking methods							*/
/*										*/
/********************************************************************************/

boolean checkCondition(String v);
Action createNewAction();
Action editAction(Action a);



/********************************************************************************/
/*										*/
/*	Input methods								*/
/*										*/
/********************************************************************************/

boolean supportsOpen();
boolean handleOpen(PebbleEditor pe,Component comp);



/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

boolean supportsSave();
void handleSave(PebbleEditor pe,Component comp);



}	// end of class PebbleModel




/* end of PebbleModel.java */


