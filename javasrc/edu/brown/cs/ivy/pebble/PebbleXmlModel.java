/********************************************************************************/
/*										*/
/*		PebbleXmlModel.java						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/pebble/PebbleXmlModel.java,v 1.10 2018/08/02 15:10:34 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PebbleXmlModel.java,v $
 * Revision 1.10  2018/08/02 15:10:34  spr
 * Fix imports.
 *
 * Revision 1.9  2018/05/25 17:57:07  spr
 * Formatting.
 *
 * Revision 1.8  2015/11/20 15:09:22  spr
 * Reformatting.
 *
 * Revision 1.7  2013/09/24 01:07:50  spr
 * data format
 *
 * Revision 1.6  2009-09-17 02:00:09  spr
 * Eclipse cleanup.
 *
 * Revision 1.5  2007-08-10 02:11:18  spr
 * Cleanups from eclipse.
 *
 * Revision 1.4  2006-12-01 03:22:53  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.3  2006/07/23 02:25:14  spr
 * Add support for action editing and creation.
 *
 * Revision 1.2  2006/07/10 14:52:23  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/03/10 03:25:52  spr
 * Move the old model to PebbleXmlModel.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.pebble;


import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class PebbleXmlModel implements PebbleModel, PebbleConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<String,Event> event_map;
private List<XmlAction>    action_set;
private List<Condition> condition_set;
private boolean transition_actions;
private boolean state_actions;
private Element format_xml;
private List<AutomataProperty> attribute_set;
private JFileChooser file_chooser;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PebbleXmlModel(String xmlfile)
{
   event_map = new TreeMap<>();
   action_set = new Vector<>();
   condition_set = new Vector<>();
   transition_actions = false;
   state_actions = false;
   format_xml = null;
   attribute_set = new Vector<>();
   String cwd = System.getProperty("user.dir");
   file_chooser = new JFileChooser(new File(cwd));

   Node xml = IvyXml.loadXmlFromFile(xmlfile);

   if (xml == null) {
      System.err.println("PEBBLE: Bad xml model file " + xmlfile);
      System.exit(1);
    }

   for (Iterator<Element> it1 = IvyXml.getChildren(xml); it1.hasNext(); ) {
      Node x1 = it1.next();
      if (IvyXml.isElement(x1,"ACTIONS")) {
	 transition_actions = IvyXml.getAttrBool(x1,"TRANSITIONS",true);
	 state_actions = IvyXml.getAttrBool(x1,"STATES",true);
	 for (Iterator<Element> it = IvyXml.getElementsByTag(x1,"ACTION"); it.hasNext(); ) {
	    Element e = it.next();
	    action_set.add(new XmlAction(e));
	  }
       }
      else if (IvyXml.isElement(x1,"CONDITIONS")) {
	 for (Iterator<Element> it = IvyXml.getElementsByTag(x1,"CONDITION"); it.hasNext(); ) {
	    Element e = it.next();
	    condition_set.add(new Condition(e));
	  }
       }
      else if (IvyXml.isElement(x1,"EVENTS")) {
	 for (Iterator<Element> it = IvyXml.getElementsByTag(x1,"EVENT"); it.hasNext(); ) {
	    Element e = it.next();
	    Event evt = new Event(e);
	    event_map.put(evt.getName(),evt);
	  }
       }
      else if (IvyXml.isElement(x1,"FORMAT")) {
	 for (Iterator<Element> it = IvyXml.getChildren(x1); it.hasNext(); ) {
	    format_xml = it.next();
	    break;
	  }
	 getAttributes(format_xml);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public boolean supportsEditEvents()		{ return true; }


@Override public Collection<String> getEventTypes()
{
   return event_map.keySet();
}


@Override public EventType getEventType(String nm)
{
   return event_map.get(nm);
}



@Override public boolean getUsesConditions()		{ return transition_actions || state_actions; }
@Override public boolean getUsesTransitionActions()	{ return transition_actions; }
@Override public boolean getUsesStateActions()		{ return state_actions; }




/********************************************************************************/
/*										*/
/*	Checking methods							*/
/*										*/
/********************************************************************************/

@Override public boolean checkCondition(String v)
{
   if (v == null) return true;

   for (Iterator<Condition> it = condition_set.iterator(); it.hasNext(); ) {
      Condition c = it.next();
      if (c.check(v)) return true;
    }

   return false;
}



public boolean checkActions(String v)
{
   if (v == null) return true;

   StringTokenizer tok = new StringTokenizer(v,"\n");
   while (tok.hasMoreTokens()) {
      String ln = tok.nextToken().trim();
      if (ln.length() == 0) continue;
      boolean fnd = false;
      for (XmlAction a : action_set) {
	 fnd = a.check(ln);
	 if (fnd) break;
       }
      if (!fnd) return false;
    }

   return true;
}



@Override public Action createNewAction()
{
   return null;
}


@Override public Action editAction(Action a)
{
   return null;
}



/********************************************************************************/
/*										*/
/*	Subclass to hold events 						*/
/*										*/
/********************************************************************************/

private static class Event implements EventType, Serializable {

   private String event_name;
   private transient List<EventProperty> event_props;
   private static final long serialVersionUID = 1;

   Event(Node xml) {
      event_name = IvyXml.getAttrString(xml,"NAME");
      event_props = new Vector<EventProperty>();

      for (Iterator<Element> it = IvyXml.getElementsByTag(xml,"PROP"); it.hasNext(); ) {
	 Element e = it.next();
	 EventProp ep = new EventProp(e);
	 event_props.add(ep);
       }
    }

   @Override public String getName()				{ return event_name; }
   @Override public Iterable<EventProperty> getProperties() {
      return new ArrayList<EventProperty>(event_props);
    }

   @Override public String toString()				{ return event_name; }

}	// end of subclass Event




/********************************************************************************/
/*										*/
/*	Subclass to hold event properties					*/
/*										*/
/********************************************************************************/

private static class EventProp implements EventProperty, Serializable {

   private String prop_name;
   private String prop_label;
   private int prop_type;
   private String match_type;
   private static final long serialVersionUID = 1;

   EventProp(Node xml) {
      prop_name = IvyXml.getAttrString(xml,"NAME");
      prop_label = IvyXml.getAttrString(xml,"LABEL");
      match_type = IvyXml.getAttrString(xml,"MATCH");

      prop_type = PEBBLE_TYPE_NONE;
      String tyn = IvyXml.getAttrString(xml,"TYPE");
      if (tyn == null) {
	 if (match_type != null) prop_type = PEBBLE_TYPE_MATCH;
       }
      else if (tyn.equalsIgnoreCase("Boolean")) prop_type = PEBBLE_TYPE_BOOLEAN;
      else if (tyn.equalsIgnoreCase("String")) prop_type = PEBBLE_TYPE_STRING;
      else if (tyn.equalsIgnoreCase("Match")) prop_type = PEBBLE_TYPE_MATCH;
      else if (tyn.equalsIgnoreCase("Class")) prop_type = PEBBLE_TYPE_CLASS;
      else if (tyn.equalsIgnoreCase("MultiMatch")) prop_type = PEBBLE_TYPE_MULTI_MATCH;

      if (prop_name == null && prop_type == PEBBLE_TYPE_MATCH) prop_name = match_type;
    }

   @Override public String getName()			{ return prop_name; }
   @Override public String getLabel()			{ return prop_label; }
   @Override public int getType() 			{ return prop_type; }
   @Override public String getMatch()			{ return match_type; }

}	// end of subclass EventProp




/********************************************************************************/
/*										*/
/*	Subclass to hold parsable item (action/condition)			*/
/*										*/
/********************************************************************************/

private abstract static class Parsable {

   private String syntax_string;
   private Pattern syntax_regex;
   private Map<String,UseItem> use_items;

   protected Parsable(Element xml) {
      syntax_string = IvyXml.getAttrString(xml,"SYNTAX");
      use_items = new HashMap<String,UseItem>();
   
      for (Iterator<Element> it = IvyXml.getElementsByTag(xml,"USE"); it.hasNext(); ) {
         Element e = it.next();
         UseItem u = UseItem.create(e);
         if (u != null) use_items.put(u.getName(),u);
       }
      StringBuffer regex = new StringBuffer();
      for (StringTokenizer tok = new StringTokenizer(syntax_string); tok.hasMoreTokens(); ) {
         String t = tok.nextToken();
         if (regex.length() > 0) regex.append("\\s*");
         UseItem u = use_items.get(t);
         if (u == null) regex.append(t);
         else regex.append(u.getRegex());
       }
      String r = "^" + regex.toString() + "$";
      syntax_regex = Pattern.compile(r,Pattern.CASE_INSENSITIVE);
    }

   boolean check(String s) {
      Matcher m = syntax_regex.matcher(s);
      return m.find();
    }

   String parseXml(Node xml) {
      Map<String,String> vals = new HashMap<String,String>();
      for (Iterator<UseItem> it = use_items.values().iterator(); it.hasNext(); ) {
	 UseItem u = it.next();
	 String unm = u.getName();
	 String val = IvyXml.getAttrString(xml,unm);
	 if (val == null) return null;
	 String rx = "^" + u.getRegex() + "$";
	 if (!val.matches(rx)) return null;
	 vals.put(unm,val);
       }
      StringBuffer buf = new StringBuffer();
      for (StringTokenizer tok = new StringTokenizer(syntax_string); tok.hasMoreTokens(); ) {
	 String t = tok.nextToken();
	 if (buf.length() > 0) buf.append(" ");
	 String v = vals.get(t);
	 if (v == null) buf.append(t);
	 else buf.append(v);
       }
      return buf.toString();
    }

}	// end of subclass Parsable




private static abstract class UseItem {

   protected String use_name;

   protected UseItem(Element e) {
      use_name = IvyXml.getAttrString(e,"NAME");
    }

   String getName()			{ return use_name; }

   abstract String getRegex();

   static UseItem create(Element e) {
      String typ = IvyXml.getAttrString(e,"TYPE");
      if (typ.equalsIgnoreCase("Integer")) return new UseItemInt(e);
      else if (typ.equalsIgnoreCase("Word")) return new UseItemWord(e);
      else if (typ.equalsIgnoreCase("Enum")) return new UseItemEnum(e);
      return null;
    }

}	// end of subclass UseItem



private static class UseItemInt extends UseItem {

   UseItemInt(Element e) {
      super(e);
    }

   @Override String getRegex()			{ return "\\d+"; }

}	// end of subclass UseItemInt



private static class UseItemWord extends UseItem {

   UseItemWord(Element e) {
      super(e);
    }

   @Override String getRegex()			{ return "[a-zA-Z]\\w*"; }

}	// end of subclass UseItemWord



private static class UseItemEnum extends UseItem {

   private List<String> enum_list;
   private Map<String,String> enum_items;

   UseItemEnum(Element e) {
      super(e);
      enum_list = new Vector<String>();
      enum_items = new HashMap<String,String>();
      for (Iterator<Element> it = IvyXml.getElementsByTag(e,"VALUE"); it.hasNext(); ) {
         Element v = it.next();
         String nm = IvyXml.getAttrString(v,"NAME");
         if (nm == null) continue;
         enum_list.add(nm);
         enum_items.put(nm,nm);
         String op = IvyXml.getAttrString(v,"OPTION");
         if (op != null) {
            for (StringTokenizer tok = new StringTokenizer(op); tok.hasMoreTokens(); ) {
               String ov = tok.nextToken();
               enum_items.put(ov,nm);
             }
          }
       }
    }

   @Override String getRegex() {
      StringBuffer buf = new StringBuffer();
      for (Iterator<String> it = enum_items.keySet().iterator(); it.hasNext(); ) {
	 String k = it.next();
	 if (buf.length() == 0) buf.append("(");
	 else buf.append("|");
	 buf.append("\\Q");
	 buf.append(k);
	 buf.append("\\E");
       }
      buf.append(")");
      return buf.toString();
    }

}	// end of subclass UseItemEnum




/********************************************************************************/
/*										*/
/*	Action methods/classes							*/
/*										*/
/********************************************************************************/

private static class XmlAction extends Parsable {

   XmlAction(Element e) {
      super(e);
    }

}	// end of subclass XmlAction




private static class StringAction implements PebbleConstants.Action {

   private String action_value;

   StringAction(String s) {
      action_value = s;
    }

   @Override public String getLabel()		{ return action_value; }

}	// end of subclass StringAction



/********************************************************************************/
/*										*/
/*	Condition methods/classes						*/
/*										*/
/********************************************************************************/

private static class Condition extends Parsable {

   Condition(Element e) {
      super(e);
    }

}	// end of subclass Condition



/********************************************************************************/
/*										*/
/*	Attribute methods/classes						*/
/*										*/
/********************************************************************************/

@Override public Collection<AutomataProperty> getAttributeSet()  { return attribute_set; }


private void getAttributes(Node xml)
{
   if (xml == null) return;

   NamedNodeMap nn = xml.getAttributes();
   for (int i = 0; i < nn.getLength(); ++i) {
      Attr a = (Attr) nn.item(i);
      if (a.getValue().startsWith("$")) {
	 String rhs = a.getValue().substring(1).trim();
	 defineAttribute(a.getName(),rhs);
       }
    }
   for (Node n = xml.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (IvyXml.isElement(n)) getAttributes(n);
      else if (n instanceof Text) {
	 String s = n.getNodeValue();
	 if (s.startsWith("$")) {
	    int idx = s.indexOf('=');
	    if (idx > 0) {
	       String lhs = s.substring(1,idx).trim();
	       String rhs = s.substring(idx+1).trim();
	       defineAttribute(lhs,rhs);
	     }
	  }
       }
    }
}



private Attribute getListAttribute(Node xml)
{
   NamedNodeMap nn = xml.getAttributes();
   for (int i = 0; i < nn.getLength(); ++i) {
      Attr a = (Attr) nn.item(i);
      if (a.getValue().startsWith("$")) {
	 String rhs = a.getValue().substring(1).trim();
	 int jdx = rhs.indexOf(":");
	 if (jdx >= 0) rhs = rhs.substring(0,jdx).trim();
	 for (Iterator<AutomataProperty> it = attribute_set.iterator(); it.hasNext(); ) {
	    Attribute aa = (Attribute) it.next();
	    if (aa.getLabel().equals(rhs)) {
	       if (aa.isMultiple()) return aa;
	     }
	  }
       }
    }

   return null;
}




private void defineAttribute(String nm,String val)
{
   String typ = "String";
   boolean isarray = false;

   int idx = val.indexOf(':');
   if (idx >= 0) {
      typ = val.substring(idx+1).trim();
      val = val.substring(0,idx).trim();
      idx = typ.indexOf("[]");
      if (idx >= 0) {
	 typ = typ.substring(0,idx).trim();
	 isarray = true;
       }
    }

   attribute_set.add(new Attribute(val,typ,isarray));
}




private void setAttribute(PebbleEditor pe,String lbl,String val)
{
   Attribute a = null;

   for (Iterator<AutomataProperty> it = attribute_set.iterator(); a == null && it.hasNext(); ) {
      Attribute aa = (Attribute) it.next();
      if (aa.getLabel().equals(lbl)) a = aa;
    }

   if (a == null) return;

   PebbleAutomata pa = pe.getEditModel();

   if (a.isMultiple()) {
      String s = pa.getValue(a);
      if (s != null) val = s + "\n" + val;
    }

   pa.setValue(a,val);
}



private String getAttribute(PebbleEditor pe,String lbl,int idx)
{
   Attribute a = null;

   for (Iterator<AutomataProperty> it = attribute_set.iterator(); a == null && it.hasNext(); ) {
      Attribute aa = (Attribute) it.next();
      if (aa.getLabel().equals(lbl)) a = aa;
    }

   if (a == null) return null;

   PebbleAutomata pa = pe.getEditModel();

   String s = pa.getValue(a);

   if (a.isMultiple()) {
      StringTokenizer tok = new StringTokenizer(s,"\n");
      for (int i = 0; i < idx; ++i) {
	 if (tok.hasMoreTokens()) tok.nextToken();
       }
      if (tok.hasMoreTokens()) s = tok.nextToken();
      else s = null;
    }

   return s;
}



private static class Attribute implements AutomataProperty {

   private String attr_label;
   private int attr_type;
   private boolean is_multiple;

   Attribute(String lbl,String typ,boolean mult) {
      attr_label = lbl;
      if (typ == null) attr_type = PEBBLE_TYPE_STRING;
      else if (typ.equalsIgnoreCase("Boolean")) attr_type = PEBBLE_TYPE_BOOLEAN;
      else if (typ.equalsIgnoreCase("String")) attr_type = PEBBLE_TYPE_STRING;
      else attr_type = PEBBLE_TYPE_NONE;

      is_multiple = mult;
    }

   @Override public String getLabel()			{ return attr_label; }
   @Override public String getName()			{ return attr_label; }
   @Override public boolean isMultiple()			{ return is_multiple; }
   @Override public int getType() 			{ return attr_type; }

}	// end of subclass Attribute




/********************************************************************************/
/*										*/
/*	Input methods								*/
/*										*/
/********************************************************************************/

@Override public boolean supportsOpen()			{ return format_xml != null; }


@Override public boolean handleOpen(PebbleEditor pe,Component comp)
{
   if (format_xml == null) return false;

   file_chooser.setDialogTitle("Select Automata File to Load");
   int sts = file_chooser.showOpenDialog(comp);
   if (sts != JFileChooser.APPROVE_OPTION) return false;
   File f = file_chooser.getSelectedFile();
   String file = f.getPath();

   Node root = IvyXml.loadXmlFromFile(file);
   if (root == null) return false;

   String rootname = format_xml.getNodeName();
   if (!IvyXml.isElement(root,rootname)) root = IvyXml.getElementByTag(root,rootname);
   if (root == null) return false;

   /* we need to do this such that we can recover from failure */
   pe.getEditModel().clearValues();
   pe.getEditModel().clear();

   Map<String,Object> cur = new HashMap<String,Object>();

   if (!readInputNode(pe,format_xml,root,cur)) return false;
   cur.put("PASS2",Boolean.TRUE);
   if (!readInputNode(pe,format_xml,root,cur)) return false;

   return true;
}



private boolean readInputNode(PebbleEditor pe,Node template,Node input,Map<String,Object> current)
{
   NamedNodeMap nn = template.getAttributes();
   for (int i = 0; i < nn.getLength(); ++i) {
      Attr a = (Attr) nn.item(i);
      if (a.getValue().startsWith("$")) {
	 String rhs = a.getValue().substring(1).trim();
	 int idx = rhs.indexOf(":");
	 if (idx >= 0) rhs = rhs.substring(0,idx).trim();
	 if (current.get("PASS2") == null) {
	    setAttribute(pe,rhs,IvyXml.getAttrString(input,a.getName()));
	  }
       }
      else if (a.getValue().startsWith("*")) {
	 String rhs = a.getValue().substring(1).trim();
	 setField(pe,rhs,IvyXml.getAttrString(input,a.getName()),current,input);
       }
    }

   for (Node n = template.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (IvyXml.isElement(n)) {
	 String fe = IvyXml.getAttrString(n,"FOREACH");
	 String del = IvyXml.getAttrString(n,"DELETE");
	 if (fe == null) {
	    for (Iterator<Element> it = IvyXml.getElementsByTag(input,n.getNodeName()); it.hasNext(); ) {
	       Node in = it.next();
	       if (!readInputNode(pe,n,in,current)) return false;
	     }
	  }
	 else {
	    for (Iterator<Element> it = IvyXml.getElementsByTag(input,n.getNodeName()); it.hasNext(); ) {
	       Node in = it.next();
	       clearCurrent(current,fe);
	       if (del != null) {
		  for (StringTokenizer tok = new StringTokenizer(del); tok.hasMoreTokens(); ) {
		     String ds = tok.nextToken();
		     clearCurrent(current,ds);
		   }
		}
	       if (!readInputNode(pe,n,in,current)) return false;
	       if (fe.equals("CONDITION")) {
		  readConditionNode(in,current);
		}
	       else if (fe.equals("ACTION")) {
		  readActionNode(in,current);
		}
	     }
	  }
       }
      else if (n instanceof Text) {
	 String s = n.getNodeValue();
	 if (s.startsWith("$")) {
	    int idx = s.indexOf('=');
	    if (idx > 0) {
	       String lhs = s.substring(1,idx).trim();
	       String txt = IvyXml.getText(input);
	       if (current.get("PASS2") == null) {
		  setAttribute(pe,lhs,txt);
		}
	     }
	  }
	 else if (s.startsWith("*")) {
	    String lhs = s.substring(1).trim();
	    String txt = IvyXml.getText(input);
	    setField(pe,lhs,txt,current,null);
	  }
       }
    }

   return true;
}



private void readConditionNode(Node n,Map<String,Object> current)
{
   for (Iterator<Condition> it = condition_set.iterator(); it.hasNext(); ) {
      Condition c = it.next();
      String cval = c.parseXml(n);
      if (cval != null) {
	 PebbleTransition pt = (PebbleTransition) current.get("TRANSITION");
	 String oval = pt.getCondition();
	 if (oval != null) cval = oval + "\n" + cval;
	 pt.setCondition(cval);
	 break;
       }
    }
}




private void readActionNode(Node n,Map<String,Object> current)
{
   for (XmlAction c : action_set) {
      String cval = c.parseXml(n);
      if (cval != null) {
	 StringAction sa = new StringAction(cval);
	 if (transition_actions) {
	    PebbleTransition pt = (PebbleTransition) current.get("TRANSITION");
	    if (pt != null) {
	       pt.addAction(sa);
	       continue;
	     }
	  }
	 else if (state_actions) {
	    PebbleState ps = (PebbleState) current.get("STATE");
	    if (ps != null) {
	       ps.addAction(sa);
	     }
	  }
	 break;
       }
    }
}




private void clearCurrent(Map<String,Object> cur,String id)
{
   for (Iterator<String> it = cur.keySet().iterator(); it.hasNext(); ) {
      String k = it.next();
      if (k.startsWith(id)) it.remove();
    }
}



private void setField(PebbleEditor pe,String fld,String val,Map<String,Object> cur,Node xml)
{
   if (val != null) cur.put(fld,val);

   if (cur.get("PASS2") != null) {
      setField2(pe,fld,val,cur,xml);
      return;
    }

   if (fld.equals("MATCH_ID") || fld.equals("MATCH_CLASS")) {
      checkMatchItem(pe,cur);
    }
   else if (fld.equals("EVENT_NAME") || fld.equals("EVENT_TYPE")) {
      checkEventItem(pe,cur,xml);
    }
   else if (fld.equals("EVENT_LABEL")) {
      checkEventLabel(pe,cur);
    }
   else if (fld.startsWith("EVENT_PROPERTIES")) {
      checkEventProperties(pe,cur,xml);
    }
   else if (fld.equals("MATCH_NAME") || fld.equals("MATCH_USE")) {
      checkEventMatch(pe,cur);
    }
   else if (fld.equals("MATCH_MODE")) {
      checkEventMatchMode(pe,cur);
    }
   else if (fld.equals("STATE_NAME")) {
      PebbleState ps;
      if (val != null && val.equals(cur.get("START_STATE"))) {
	 ps = pe.getEditModel().getStartState();
	 ps.setName(val);
       }
      else {
	 ps = pe.getEditModel().createState(val);
       }
      cur.put("STATE",ps);
      checkStateLabel(pe,cur);
      checkStateType(pe,cur);
    }
   else if (fld.equals("STATE_LABEL")) {
      checkStateLabel(pe,cur);
    }
   else if (fld.equals("STATE_IGNORE") || fld.equals("STATE_ACCEPT") || fld.equals("STATE_ERROR")) {
      checkStateType(pe,cur);
    }
   else if (fld.equals("TRANSITION_EVENT") || fld.equals("TRANSITION_TARGET")) ;
   else if (fld.equals("START_STATE")) ;
   else {
      System.err.println("SET FIELD " + fld + " = " + val);
    }
}




private void setField2(PebbleEditor pe,String fld,String val,Map<String,Object> cur,Node xml)
{
   if (fld.equals("STATE_NAME")) {
      PebbleState ps;
      ps = pe.getEditModel().findState(val);
      cur.put("STATE",ps);
    }
   else if (fld.equals("TRANSITION_EVENT")) {
      String tgt = (String) cur.get("TRANSITION_TARGET");
      if (tgt == null) cur.put("TRANSITION_EVENT",val);
      else {
	 PebbleState fs = (PebbleState) cur.get("STATE");
	 PebbleState ts = pe.getEditModel().findState(tgt);
	 if (fs != null && ts != null) {
	    PebbleTransition pt = pe.getEditModel().createTransition(fs,ts);
	    pt.addEvent(pe.getEditModel().findEvent(val));
	    cur.put("TRANSITION",pt);
	  }
       }
    }
   else if (fld.equals("TRANSITION_TARGET")) {
      String evt = (String) cur.get("TRANSITION_EVENT");
      if (evt == null) cur.put("TRANSITION_TARGET",val);
      else {
	 PebbleState fs = (PebbleState) cur.get("STATE");
	 PebbleState ts = pe.getEditModel().findState(val);
	 if (fs != null && ts != null) {
	    PebbleTransition pt = pe.getEditModel().createTransition(fs,ts);
	    PebbleEvent nevt = pe.getEditModel().findEvent(evt);
	    if (nevt == null) {
	       System.err.println("CAN'T FIND EVENT " + evt);
	     }
	    else {
	       pt.addEvent(nevt);
	     }
	    cur.put("TRANSITION",pt);
	  }
       }
    }
}



private void checkMatchItem(PebbleEditor pe,Map<String,Object> cur)
{
   String ecls = (String) cur.get("MATCH_CLASS");
   String eid = (String) cur.get("MATCH_ID");

   if (ecls != null && eid != null) {
      pe.getEditModel().setEventMatch(eid,ecls);
      cur.remove("MATCH_ID");
      cur.remove("MATCH_CLASS");
    }
}




private void checkEventItem(PebbleEditor pe,Map<String,Object> cur,Node xml)
{
   String etyp = (String) cur.get("EVENT_TYPE");
   String enm = (String) cur.get("EVENT_NAME");
   PebbleEvent evt = (PebbleEvent) cur.get("EVENT");

   if (evt == null) {
      if (etyp != null && enm != null) {
	 evt = new PebbleEvent(enm,getEventType(etyp));
	 cur.remove("EVENT_TYPE");
	 cur.remove("EVENT_NAME");
	 cur.put("EVENT",evt);
	 if (cur.get("EVENT_PROPERTIES") != null) checkEventProperties(pe,cur,xml);
	 checkEventLabel(pe,cur);
	 pe.getEditModel().addEvent(evt);
       }
    }
}



private void checkEventLabel(PebbleEditor pe,Map<String,Object> cur)
{
   String lbl = (String) cur.get("EVENT_LABEL");
   PebbleEvent evt = (PebbleEvent) cur.get("EVENT");
   if (evt != null && lbl != null) {
      evt.setLabel(lbl);
      cur.remove("EVENT_LABEL");
    }
}



private void checkEventProperties(PebbleEditor pe,Map<String,Object> cur,Node xml)
{
   PebbleEvent evt = (PebbleEvent) cur.get("EVENT");

   if (evt == null) {
      cur.put("EVENT_PROPERTIES","TRUE");
    }
   else {
      EventType et = evt.getEventType();
      for (EventProperty ep : et.getProperties()) {
	 switch (ep.getType()) {
	    case PEBBLE_TYPE_NONE :
	       break;
	    case PEBBLE_TYPE_BOOLEAN :
	       boolean bv = IvyXml.getAttrBool(xml,ep.getName());
	       evt.setBooleanParam(ep.getName(),bv);
	       break;
	    case PEBBLE_TYPE_STRING :
	       String sv = IvyXml.getAttrString(xml,ep.getName());
	       evt.setStringParam(ep.getName(),sv);
	       break;
	    case PEBBLE_TYPE_CLASS :
	       String cv = IvyXml.getAttrString(xml,ep.getName());
	       evt.setMatchValue(ep.getName(),cv);
	       break;
	    case PEBBLE_TYPE_MATCH :
	       break;
	  }
       }
    }

   cur.remove("EVENT_PROPERTIES");
}




private void checkEventMatch(PebbleEditor pe,Map<String,Object> cur)
{
   PebbleEvent evt = (PebbleEvent) cur.get("EVENT");
   String eid = (String) cur.get("MATCH_USE");
   String enm = (String) cur.get("MATCH_NAME");

   if (evt != null && eid != null && enm != null) {
      evt.setMatchValue(enm,eid);
      cur.remove("MATCH_USE");
      checkEventMatchMode(pe,cur);
    }
}



private void checkEventMatchMode(PebbleEditor pe,Map<String,Object> cur)
{
   PebbleEvent evt = (PebbleEvent) cur.get("EVENT");
   String emd = (String) cur.get("MATCH_MODE");
   String enm = (String) cur.get("MATCH_NAME");

   if (evt != null && emd != null && enm != null) {
      evt.setMatchMode(enm,emd);
      cur.remove("MATCH_MODE");
    }
}



private void checkStateLabel(PebbleEditor pe,Map<String,Object> cur)
{
   PebbleState ps = (PebbleState) cur.get("STATE");
   String lbl = (String) cur.get("STATE_LABEL");
   if (lbl != null && ps != null) {
      ps.setStateLabel(lbl);
      cur.remove("STATE_LABEL");
    }
}



private void checkStateType(PebbleEditor pe,Map<String,Object> cur)
{
   PebbleState ps = (PebbleState) cur.get("STATE");
   if (ps == null) return;

   String val = (String) cur.get("STATE_IGNORE");
   if (val != null && getBooleanValue(val)) ps.setStateType(StateType.IGNORE);
   val = (String) cur.get("STATE_ACCEPT");
   if (val != null && getBooleanValue(val)) ps.setStateType(StateType.ACCEPT);
   val = (String) cur.get("STATE_ERROR");
   if (val != null && getBooleanValue(val)) ps.setStateType(StateType.ERROR);

   cur.remove("STATE_IGNORE");
   cur.remove("STATE_ACCEPT");
   cur.remove("STATE_ERROR");
}




private boolean getBooleanValue(String s)
{
   if (s == null) return false;

   if (s.startsWith("t") || s.startsWith("T") || s.startsWith("1")) return true;

   return false;
}



/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

@Override public boolean supportsSave()			{ return true; }


@Override public void handleSave(PebbleEditor pe,Component comp)
{
   file_chooser.setDialogTitle("Select Where to Save Automata File");
   int sts = file_chooser.showSaveDialog(comp);
   if (sts == JFileChooser.APPROVE_OPTION) {
      File f = file_chooser.getSelectedFile();
      try {
	 pe.getEditModel().normalizeAutomata();
	 IvyXmlWriter xw = new IvyXmlWriter(new FileWriter(f));
	 Map<String,Object> current = new HashMap<String,Object>();
	 generateFormatXml(pe,xw,format_xml,current,0);
	 xw.flush();
	 xw.close();
       }
      catch (IOException ex) {
	 JOptionPane.showMessageDialog(comp,"Problem saving automata file: " +
					  ex.getMessage());
       }
    }
}



private void generateFormatXml(PebbleEditor pe,IvyXmlWriter xw,Node xml,Map<String,Object> cur,
				  int idx)
{
   xw.begin(xml.getNodeName());

   NamedNodeMap nn = xml.getAttributes();
   for (int i = 0; i < nn.getLength(); ++i) {
      Attr a = (Attr) nn.item(i);
      if (a.getValue().startsWith("$")) {
	 String rhs = a.getValue().substring(1).trim();
	 int jdx = rhs.indexOf(":");
	 if (jdx >= 0) rhs = rhs.substring(0,jdx).trim();
	 String s = getAttribute(pe,rhs,idx);
	 if (s != null) xw.field(a.getName(),s);
       }
      else if (a.getValue().equals("*EVENT_PROPERTIES")) {
	 PebbleEvent evt = (PebbleEvent) cur.get("EVENT");
	 EventType et = evt.getEventType();
	 for (EventProperty ep : et.getProperties()) {
	    switch (ep.getType()) {
	       default :
		  break;
	       case PEBBLE_TYPE_BOOLEAN :
		  xw.field(ep.getName(),evt.getBooleanParam(ep.getName()));
		  break;
	       case PEBBLE_TYPE_STRING :
		  xw.field(ep.getName(),evt.getStringParam(ep.getName()));
		  break;
	       case PEBBLE_TYPE_CLASS :
		  xw.field(ep.getName(),evt.getMatchValue(ep.getName()));
		  break;
	     }
	  }
       }
      else if (a.getValue().startsWith("*")) {
	 String rhs = a.getValue().substring(1).trim();
	 String s = getField(pe,rhs,cur);
	 if (s != null) xw.field(a.getName(),s);
       }
      else if (a.getName().equals("FOREACH")) {
	 if (a.getValue().equals("CONDITION")) {
	    String s = (String) cur.get("CONDITION");
	    if (s != null) generateCondition(xw,s);
	  }
	 else if (a.getValue().equals("ACTION")) {
	    String s = (String) cur.get("ACTION");
	    if (s != null) generateAction(xw,s);
	  }
       }
      else if (a.getName().equals("DELETE")) ;
      else xw.field(a.getName(),a.getValue());
    }

   for (Node n = xml.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Text) {
	 String s = n.getNodeValue();
	 if (s.startsWith("$")) {
	    int jdx = s.indexOf('=');
	    if (jdx > 0) {
	       String lhs = s.substring(1,jdx).trim();
	       xw.text(getAttribute(pe,lhs,idx));
	     }
	  }
	 else if (s.startsWith("*")) {
	    String lhs = s.substring(1).trim();
	    xw.text(getField(pe,lhs,cur));
	  }
       }
      else if (IvyXml.isElement(n)) {
	 String fe = IvyXml.getAttrString(n,"FOREACH");
	 if (fe == null) {
	    Attribute attr = getListAttribute(n);
	    if (attr == null) {
	       generateFormatXml(pe,xw,n,cur,idx);
	     }
	    else {
	       String s = pe.getEditModel().getValue(attr);
	       StringTokenizer tok = new StringTokenizer(s,"\n");
	       int ct = tok.countTokens();
	       for (int j = 0; j < ct; ++j) {
		  generateFormatXml(pe,xw,n,cur,j);
		}
	     }
	  }
	 else {
	    Collection<?> c = null;
	    Collection<?> d = null;
	    String dname = null;
	    if (fe.equals("MATCH")) {
	       PebbleEvent evt = (PebbleEvent) cur.get("EVENT");
	       if (evt == null) c = pe.getEditModel().getAllMatches();
	       else {
		  EventType ety = evt.getEventType();
		  Vector<String> c1 = new Vector<String>();
		  c = c1;
		  for (EventProperty ep : ety.getProperties()) {
		     if (ep.getMatch() != null && evt.getMatchValue(ep.getName()) != null) {
			c1.add(ep.getName());
		      }
		     /* XXX this will need to be changed to the real format */
		     if (ep.getType() == PEBBLE_TYPE_MULTI_MATCH &&
			     evt.getStringParam(ep.getName()) != null) {
			 int count = Integer.parseInt(evt.getStringParam(ep.getName()));
			 for (int i = 0; i < count; ++i) {
			     if (evt.getMatchValue(ep.getName() + i) != null) {
				 c1.add(ep.getName() + i);
			     }
			 }
		     }
		   }
		}
	     }
	    else if (fe.equals("EVENT")) {
	       c = pe.getEditModel().getAllEvents();
	     }
	    else if (fe.equals("STATE")) {
	       c = pe.getEditModel().getAllStates();
	     }
	    else if (fe.equals("TRANSITION")) {
	       PebbleState ps = (PebbleState) cur.get("STATE");
	       Vector<PebbleTransition> c1 = new Vector<PebbleTransition>();
	       c = c1;
	       Vector<PebbleEvent> d1 = new Vector<PebbleEvent>();
	       d = d1;
	       dname = "TEVENT";
	       for (Iterator<PebbleTransition> it = pe.getEditModel().getAllTransitions().iterator(); it.hasNext(); ) {
		  PebbleTransition pt = it.next();
		  if (pt.getSource() == ps) {
		     for (Iterator<PebbleEvent> it1 = pt.getEvents().iterator(); it1.hasNext(); ) {
			PebbleEvent pev = it1.next();
			c1.add(pt);
			d1.add(pev);
		      }
		   }
		}
	     }
	    else if (fe.equals("CONDITION")) {
	       PebbleTransition pt = (PebbleTransition) cur.get("TRANSITION");
	       String cnd = pt.getCondition();
	       if (cnd != null) {
		  Vector<String> c1 = new Vector<String>();
		  c = c1;
		  for (StringTokenizer tok = new StringTokenizer(cnd,"\n"); tok.hasMoreTokens(); ) {
		     c1.add(tok.nextToken());
		   }
		}
	     }
	    else if (fe.equals("ACTION")) {
	       PebbleTransition pt = (PebbleTransition) cur.get("TRANSITION");
	       PebbleState ps = (PebbleState) cur.get("STATE");
	       Iterable<Action> acts = null;
	       if (pt != null) acts = pt.getActions();
	       else if (ps != null) acts = ps.getActions();
	       if (acts != null) {
		  Vector<String> c1 = new Vector<String>();
		  c = c1;
		  for (Action a : acts) {
		     c1.add(a.getLabel());
		   }
		}
	     }
	    else {
	       System.err.println("UNKNOWN FOREACH " + fe);
	     }
	    if (c == null) continue;
	    Iterator<?> diter = null;
	    if (dname != null && d != null) diter = d.iterator();
	    for (Iterator<?> citer = c.iterator(); citer.hasNext(); ) {
	       Object o = citer.next();
	       cur.put(fe,o);
	       if (dname != null && diter != null && diter.hasNext()) {
		  cur.put(dname,diter.next());
		}

	       generateFormatXml(pe,xw,n,cur,idx);
	       cur.remove(fe);
	       if (dname != null) cur.remove(dname);
	     }
	  }
       }
    }

   xw.end();
}


private String getField(PebbleEditor pe,String fld,Map<String,Object> cur)
{
   if (fld.equals("MATCH_ID")) {
      return (String) cur.get("MATCH");
    }
   else if (fld.equals("MATCH_CLASS")) {
      String mid = (String) cur.get("MATCH");
      return pe.getEditModel().getEventMatch(mid);
    }
   else if (fld.equals("EVENT_NAME")) {
      PebbleEvent evt = (PebbleEvent) cur.get("EVENT");
      return evt.getName();
    }
   else if (fld.equals("EVENT_LABEL")) {
      PebbleEvent evt = (PebbleEvent) cur.get("EVENT");
      return evt.getLabel();
    }
   else if (fld.equals("EVENT_TYPE")) {
      PebbleEvent evt = (PebbleEvent) cur.get("EVENT");
      return evt.getEventType().getName();
    }
   else if (fld.equals("MATCH_NAME")) {
      String m = (String) cur.get("MATCH");
      return m;
    }
   else if (fld.equals("MATCH_USE")) {
      PebbleEvent evt = (PebbleEvent) cur.get("EVENT");
      String m = (String) cur.get("MATCH");
      return evt.getMatchValue(m);
    }
   else if (fld.equals("MATCH_MODE")) {
      PebbleEvent evt = (PebbleEvent) cur.get("EVENT");
      String m = (String) cur.get("MATCH");
      return evt.getMatchMode(m);
    }
   else if (fld.equals("STATE_NAME")) {
      PebbleState ps = (PebbleState) cur.get("STATE");
      return ps.getName();
    }
   else if (fld.equals("STATE_LABEL")) {
      PebbleState ps = (PebbleState) cur.get("STATE");
      return ps.getStateLabel();
    }
   else if (fld.equals("STATE_IGNORE")) {
      PebbleState ps = (PebbleState) cur.get("STATE");
      return Boolean.toString(ps.isIgnore());
    }
   else if (fld.equals("STATE_ACCEPT")) {
      PebbleState ps = (PebbleState) cur.get("STATE");
      return Boolean.toString(ps.isAccept());
    }
   else if (fld.equals("STATE_ERROR")) {
      PebbleState ps = (PebbleState) cur.get("STATE");
      return Boolean.toString(ps.isError());
    }
   else if (fld.equals("TRANSITION_EVENT")) {
      PebbleEvent evt = (PebbleEvent) cur.get("TEVENT");
      return evt.getName();
    }
   else if (fld.equals("TRANSITION_TARGET")) {
      PebbleTransition pt = (PebbleTransition) cur.get("TRANSITION");
      PebbleState tgt = (PebbleState) pt.getTarget();
      return tgt.getName();
    }
   else if (fld.equals("START_STATE")) {
      return pe.getEditModel().getStartState().getName();
    }

   return null;
}




private void generateCondition(IvyXmlWriter xw,String v)
{
   for (Iterator<Condition> it = condition_set.iterator(); it.hasNext(); ) {
      Condition c = it.next();
      if (c.check(v)) {
	 // output v according to c
       }
    }
}



private void generateAction(IvyXmlWriter xw,String v)
{
   for (XmlAction a : action_set) {
      if (a.check(v)) {
	 // output v according to a
       }
    }
}



}	// end of class PebbleXmlModel




/* end of PebbleXmlModel.java */


