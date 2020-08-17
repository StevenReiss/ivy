/********************************************************************************/
/*										*/
/*		PebbleAutomata.java						*/
/*										*/
/*	Graph model for the pebble editor					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/pebble/PebbleAutomata.java,v 1.14 2018/08/02 15:10:34 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PebbleAutomata.java,v $
 * Revision 1.14  2018/08/02 15:10:34  spr
 * Fix imports.
 *
 * Revision 1.13  2015/11/20 15:09:22  spr
 * Reformatting.
 *
 * Revision 1.12  2013-06-03 13:03:32  spr
 * Minor fixes
 *
 * Revision 1.11  2009-09-17 02:00:09  spr
 * Eclipse cleanup.
 *
 * Revision 1.10  2007-08-10 02:11:18  spr
 * Cleanups from eclipse.
 *
 * Revision 1.9  2006-12-01 03:22:53  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.8  2006/07/10 14:52:23  spr
 * Code cleanup.
 *
 * Revision 1.7  2006/02/21 17:06:42  spr
 * Changes to Pebble to support external data models.
 *
 * Revision 1.6  2005/07/08 20:57:06  spr
 * Charles' upgrade to Pebble UI.
 *
 * Revision 1.5  2005/06/28 17:20:53  spr
 * UI enhancements (CAR)
 *
 * Revision 1.4  2005/06/07 02:18:20  spr
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


import edu.brown.cs.ivy.petal.PetalArc;
import edu.brown.cs.ivy.petal.PetalClipSet;
import edu.brown.cs.ivy.petal.PetalModelBase;
import edu.brown.cs.ivy.petal.PetalNode;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;



class PebbleAutomata extends PetalModelBase implements PebbleConstants
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private PebbleEditor		for_editor;
private Set<PebbleState>	state_set;
private int			state_counter;
private PebbleState		start_state;
private Set<PebbleTransition>	transition_set;
private Map<String,PebbleEvent> event_set;
private Map<String,String>	match_set;
private Map<AutomataProperty,String> attribute_values;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

PebbleAutomata(PebbleEditor pe)
{
   for_editor = pe;
   state_counter = 1;
   start_state = null;
   state_set = new HashSet<>();
   transition_set = new HashSet<>();
   event_set = new TreeMap<>();
   match_set = new TreeMap<>();
   attribute_values = new HashMap<>();
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

void setup()
{
   if (start_state == null) {
      start_state = createState("Start",true);
    }
}




void clear()
{
   state_set = new HashSet<PebbleState>();
   state_set.add(start_state);
   transition_set = new HashSet<PebbleTransition>();

   if (for_editor.getBaseModel().supportsEditEvents()) {
      event_set.clear();
      match_set.clear();
    }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

PebbleState getStartState()			{ return start_state; }




/********************************************************************************/
/*										*/
/*	Normalization methods							*/
/*										*/
/********************************************************************************/

void normalizeAutomata()
{
   boolean foundTrigger = false;
   for (PebbleEvent pe : event_set.values()) {
       if (pe.getBooleanParam("TRIGGER")) {
	   foundTrigger = true;
	   break;
       }
   }

   for (Iterator<PebbleState> it = state_set.iterator(); it.hasNext(); ) {
      PebbleState ps = it.next();
      for (Iterator<PebbleEvent> it1 = event_set.values().iterator(); it1.hasNext(); ) {
	 PebbleEvent pe = it1.next();
	 Vector<PebbleTransition> fnd = new Vector<PebbleTransition>();
	 int ncnd = 0;
	 boolean orderr = false;
	 for (Iterator<PebbleTransition> it2 = transition_set.iterator(); it2.hasNext(); ) {
	    PebbleTransition pt = it2.next();
	    if (pt.getSource() == ps && pt.getEvents().contains(pe)) {
	       if (!foundTrigger && ps == start_state) {
		   pe.setBooleanParam("TRIGGER", true);
	       }
	       fnd.add(pt);
	       if (pt.getCondition() == null) ++ncnd;
	       else if (ncnd > 0) orderr = true;
	     }
	  }

	 PebbleState ns = ps.getDefaultNextState();
	 if (ns == null) ns = findState(pe.getDefaultState());
	 if (ns == null) ns = ps;

	 if (ncnd > 1 || orderr) {
	    for (Iterator<PebbleTransition> it2 = transition_set.iterator(); it2.hasNext(); ) {
	       PebbleTransition pt = it2.next();
	       if (pt.getSource() == ps && pt.getEvents().contains(pe)) {
		  if (pt.getCondition() == null && pt.getTarget() == ns) {
		     it2.remove();
		     fnd.remove(pt);
		     --ncnd;
		   }
		}
	     }
	  }
	 if (fnd.isEmpty() || ncnd == 0) {
	    PebbleTransition pt = createTransition(ps,ns);
	    pt.addEvent(pe);
	  }

       }
    }
}




/********************************************************************************/
/*										*/
/*	Edit override methods							*/
/*										*/
/********************************************************************************/

@Override public boolean handleArcEndPoint(PetalNode pn,Point p,int mode,MouseEvent evt)
{
   if (mode == ARC_MODE_START && pn == null) return false;

   if ((evt.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == 0) return false;

   return true;
}



/********************************************************************************/
/*										*/
/*	Methods for handling Cut and Paste					*/
/*										*/
/********************************************************************************/

@Override public PetalNode addPasteNode(Object o,boolean dofg)
{
   PebbleState nst = null;

   if (o instanceof PebbleState) {
      PebbleState ost = (PebbleState) o;
      nst = createState(ost.getName());
    }

   return nst;
}



@Override public PetalNode addPasteNode(Object o,PetalClipSet pcs,boolean dofg)
{
   return null;
}



@Override public PetalArc addPasteArc(Object o,PetalClipSet pcs,boolean dofg)
{
   PetalArc narc = null;

   if (o instanceof PebbleTransition) {
      PebbleTransition ot = (PebbleTransition) o;
      PetalNode ops = ot.getSource();
      PetalNode opt = ot.getTarget();
      PebbleState ps = (PebbleState) pcs.getMapping(ops);
      PebbleState pt = (PebbleState) pcs.getMapping(opt);
      if (ps != null && pt != null) narc = createTransition(ps,pt);
    }

   return narc;
}



/********************************************************************************/
/*										*/
/*	Methods for managing states						*/
/*										*/
/********************************************************************************/

PebbleState createState(String nm)
{
   return createState(nm,false);
}



private PebbleState createState(String nm,boolean start)
{
   while (nm == null || findState(nm) != null) {
      nm = "S" + (state_counter++);
    }

   PebbleState ps = new PebbleState(for_editor,nm,start);
   state_set.add(ps);

   for_editor.assignNextPosition(ps.getComponent());

   return ps;
}



PebbleState findState(String nm)
{
   if (nm == null) return null;

   for (Iterator<PebbleState> it = state_set.iterator(); it.hasNext(); ) {
      PebbleState ps = it.next();
      if (ps.getName().equals(nm)) return ps;
    }

   return null;
}



@Override public void removeNode(PetalNode n)
{
   if (n instanceof PebbleState) {
      PebbleState ps = (PebbleState) n;
      if (ps == start_state) return;
      state_set.remove(ps);
      removeArcsForState(ps);
    }
}




@Override public synchronized boolean dropNode(Object o,Point p,PetalNode pn,PetalArc pa)
{
   if (pa == null && pn != null && !(pn instanceof PebbleState)) {
      for (Iterator<PebbleTransition> it = transition_set.iterator(); it.hasNext(); ) {
	 PebbleTransition pt = it.next();
	 if (pt.getLabel() == pn) {
	    pa = pt;
	    break;
	  }
       }
    }

   if (o instanceof PebbleEvent && pa != null) {
      PebbleTransition pt = (PebbleTransition) pa;
      PebbleEvent pe = (PebbleEvent) o;
      pe = findEvent(pe.getName());
      pt.addEvent(pe);
      return true;
    }
   else if (o instanceof Vector<?> && pa != null) {
      Vector<?> pes = (Vector<?>) o;
      PebbleTransition pt = (PebbleTransition) pa;
      for (Iterator<?> it = pes.iterator(); it.hasNext(); ) {
	 PebbleEvent pe = (PebbleEvent) it.next();
	 pe = findEvent(pe.getName());
	 pt.addEvent(pe);
       }
      return true;
    }

   return false;
}




Collection<PebbleState> getAllStates()
{
   return state_set;
}



@Override public PetalNode [] getNodes()
{
   Vector<PetalNode> v = new Vector<PetalNode>();

   v.addAll(state_set);

   for (Iterator<PebbleTransition> it = transition_set.iterator(); it.hasNext(); ) {
      PebbleTransition pt = it.next();
      if (showArc(pt)) {
	 PetalNode lbl = pt.getLabel();
	 if (lbl != null) v.add(lbl);
       }
    }

   PetalNode [] nds = new PetalNode[v.size()];
   nds = v.toArray(nds);
   return nds;
}




/********************************************************************************/
/*										*/
/*	Methods to create an arc						*/
/*										*/
/********************************************************************************/

@Override public void createArc(PetalNode f,PetalNode t)
{
   if (f == null || !(f instanceof PebbleState)) {
      for_editor.showStatus("Transition must start at an existing state");
      return;
    }
   if (t != null && !(t instanceof PebbleState)) {
      for_editor.showStatus("Can only have transitions between states");
      return;
    }

   PebbleState fs = (PebbleState) f;
   PebbleState ts = (PebbleState) t;

   if (ts == null) {
      ts = createState(null);
    }

   createTransition(fs,ts);
}



@Override public void removeArc(PetalArc pa)
{
   if (pa instanceof PebbleTransition) {
      PebbleTransition pt = (PebbleTransition) pa;
      transition_set.remove(pt);
    }
}



@Override public PetalArc [] getArcs()
{
   Vector<PebbleTransition> v = new Vector<PebbleTransition>();

   for (Iterator<PebbleTransition> it = transition_set.iterator(); it.hasNext(); ) {
      PebbleTransition pt = it.next();
      if (showArc(pt)) v.add(pt);
    }

   PetalArc [] arcs = new PetalArc[v.size()];
   arcs = v.toArray(arcs);
   return arcs;
}




Collection<PebbleTransition> getAllTransitions()
{
   return transition_set;
}



PebbleTransition createTransition(PebbleState fs,PebbleState ts)
{
   for (Iterator<PebbleTransition> it = transition_set.iterator(); it.hasNext(); ) {
      PebbleTransition pt = it.next();
      if (pt.getSource() == fs && pt.getTarget() == ts) return pt;
    }

   PebbleTransition pt = new PebbleTransition(for_editor,fs,ts);

   transition_set.add(pt);

   return pt;
}



private void removeArcsForState(PebbleState ps)
{
   for (Iterator<PebbleTransition> it = transition_set.iterator(); it.hasNext(); ) {
      PebbleTransition pt = it.next();
      if (pt.getSource() == ps || pt.getTarget() == ps) {
	 it.remove();
       }
    }
}



private boolean showArc(PebbleTransition pt)
{
   if (!for_editor.getShowSelfArcs()) {
      if (pt.getSource() == pt.getTarget()) return false;
    }

   if (!for_editor.getShowDefaultArcs()) {
    }

   return true;
}



/********************************************************************************/
/*										*/
/*	Event management methods						*/
/*										*/
/********************************************************************************/

boolean addEvent(PebbleEvent ev)
{
   String nm = ev.getName();
   if (nm == null || nm.length() == 0) {
      for (int i = 1; ; ++i) {
	 String nnm = "E" + i;
	 if (event_set.get(nnm) == null) {
	    ev.setName(nnm);
	  }
       }
    }

   if (event_set.get(ev.getName()) != null) return false;

   event_set.put(ev.getName(),ev);

   for_editor.setEvents(event_set.values());

   return true;
}




void removeEvent(PebbleEvent ev)
{
   localRemoveEvent(ev);

   for_editor.setEvents(event_set.values());
}



void removeEvents(List<PebbleEvent> evts)
{
   for (PebbleEvent ev : evts) localRemoveEvent(ev);

   for_editor.setEvents(event_set.values());
}


void removeEvents(Object [] evts)
{
   for (int i = 0; i < evts.length; ++i) {
      PebbleEvent ev = (PebbleEvent) evts[i];
      localRemoveEvent(ev);
    }

   for_editor.setEvents(event_set.values());
}



private void localRemoveEvent(PebbleEvent ev)
{
   event_set.remove(ev.getName());
   // remove event from any arc
}



Collection<PebbleEvent> getAllEvents()
{
   return event_set.values();
}



PebbleEvent findEvent(String name)
{
   return event_set.get(name);
}



/********************************************************************************/
/*										*/
/*	Match management methods						*/
/*										*/
/********************************************************************************/

void setEventMatch(String val,String cls)
{
   if (cls == null) match_set.remove(val);
   else if (val != null) match_set.put(val,cls);
}


String getEventMatch(String val)
{
   if (val == null) return null;

   return match_set.get(val);
}



Collection<String> getAllMatches()
{
   return match_set.keySet();
}


String getNextMatch()
{
   int i = 1;
   while (match_set.containsKey("C" + i)) ++i;
   String next = "C" + i;
   setEventMatch(next, ""); // so we don't duplicate
   return next;
}



/********************************************************************************/
/*										*/
/*	Attribute values methods						*/
/*										*/
/********************************************************************************/

void clearValues()
{
   attribute_values.clear();
}


String getValue(AutomataProperty attr)
{
   return attribute_values.get(attr);
}


void setValue(AutomataProperty attr,String val)
{
   attribute_values.put(attr,val);
}




}	// end of class PebbleAutomata




/* end of PebbleAutomata.java */
