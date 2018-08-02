/********************************************************************************/
/*										*/
/*		ModelState.java 						*/
/*										*/
/*	Class representing an event state for the generated automata		*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 1998, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/model/ModelState.java,v 1.6 2015/11/20 15:09:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ModelState.java,v $
 * Revision 1.6  2015/11/20 15:09:16  spr
 * Reformatting.
 *
 * Revision 1.5  2008-11-12 13:45:41  spr
 * Eclipse fixups.
 *
 * Revision 1.4  2007-08-10 02:10:45  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.3  2007-05-04 02:00:03  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.2  2006/07/10 14:52:19  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:37  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.model;


import edu.brown.cs.ivy.jflow.JflowEvent;
import edu.brown.cs.ivy.jflow.JflowMethod;
import edu.brown.cs.ivy.jflow.JflowModel;
import edu.brown.cs.ivy.jflow.JflowSource;
import edu.brown.cs.ivy.jflow.JflowValue;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_Ins;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;



abstract class ModelState implements JflowModel.Node, Comparable<ModelState>
{



/********************************************************************************/
/*										*/
/*	Static creation methods 						*/
/*										*/
/********************************************************************************/

static ModelState createSimpleState(JflowMethod in,int lno)
{
   return new SimpleState(in,lno);
}



static ModelState createCallState(JflowMethod in,int lno,JflowMethod call,boolean async,BT_Ins ins)
{
   return new CallState(in,lno,call,async,false,ins);
}



static ModelState createCallOnceState(JflowMethod in,int lno,JflowMethod call,BT_Ins ins)
{
   return new CallState(in,lno,call,true,true,ins);
}



static ModelState createEventState(JflowMethod in,int lno,JflowEvent evt)
{
   return new EventState(in,lno,evt);
}



static ModelState createWaitState(JflowMethod in,int lno,JflowValue sset,ModelWaitType waittype)
{
   return new WaitState(in,lno,sset,waittype);
}



static ModelState createCondState(JflowMethod in,int lno,ConditionType ctyp,JflowSource src)
{
   return new CondState(in,lno,ctyp,src);
}



static ModelState createFieldState(JflowMethod in,int lno,JflowSource src,Object val)
{
   return new FieldState(in,lno,src,val);
}



static ModelState createReturnState(JflowMethod in,int lno,ModelValue cv)
{
   return new ReturnState(in,lno,cv);
}



static ModelState createExitState(JflowMethod in,int lno)
{
   return new ExitState(in,lno);
}



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Set<JflowModel.Node> out_nodes;
private int state_id;
private JflowMethod in_method;
private int line_number;
private int end_line;
private boolean is_final;
private TreeSet<LineBlock> line_blocks;


private static int state_counter = 0;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected ModelState(JflowMethod in,int lno)
{
   out_nodes = new HashSet<JflowModel.Node>(4);
   state_id = ++state_counter;
   in_method = in;
   line_number = lno;
   end_line = lno;
   line_blocks = null;
   is_final = false;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public void addTransition(JflowModel.Node s)
{
   out_nodes.add(s);
   is_final = false;
}

@Override public void removeTransition(JflowModel.Node s)
{
   out_nodes.remove(s);
}



void setTransitions(Collection<JflowModel.Node> c)
{
   if (c == null) out_nodes = new HashSet<JflowModel.Node>(4);
   else out_nodes = new HashSet<JflowModel.Node>(c);

   if (c != null && c.size() > 0) is_final = false;
}



@Override public Iterable<JflowModel.Node> getTransitions(){ return out_nodes; }

@Override public String getName() 			{ return "S_" + state_id; }
int getId()					{ return state_id; }
int getLineNumber()				{ return line_number; }
int getEndLine()				{ return end_line; }
JflowMethod getMethod() 			{ return in_method; }

@Override public	boolean isFinal()			{ return is_final; }
void setFinal() 				{ is_final = true; }

@Override public JflowEvent getEvent()			{ return null; }

@Override public JflowMethod getCall()			{ return null; }
void clearCall()				{ }

@Override public boolean isAsync()			{ return false; }
@Override public boolean isCallOnce()			{ return false; }

@Override public ModelWaitType getWaitType()		{ return ModelWaitType.NONE; }
@Override public Collection<JflowSource> getWaitSet()			 { return null; }
public void setWaitSet(ModelSourceSet _cs)		{ }

@Override public JflowModel.Field getCondition()		{ return null; }
@Override public JflowModel.Field getFieldSet()		{ return null; }

@Override public Object getReturnValue()			{ return null; }

@Override public boolean isSimple()			{ return false; }
@Override public boolean isExit() 			{ return false; }

boolean isReturn()				{ return false; }

ModelState mergeReturn(ModelState _ms)		{ return this; }
void setUseReturn(boolean _fg)			{ }
@Override public boolean getUseReturn()			{ return false; }

Set<JflowModel.Node> getTransitionSet() 	{ return out_nodes; }

boolean hasNoTransitions()			{ return out_nodes.size() == 0; }




/********************************************************************************/
/*										*/
/*	Line number maintenance methods 					*/
/*										*/
/********************************************************************************/

void extendTo(int lno)
{
   if (line_number < 0) return;

   if (lno > end_line) end_line = lno;
   else if (lno < line_number) line_number = lno;
}



@Override public void mergeLinesFromState(JflowModel.Node nms)
{
   if (!(nms instanceof ModelState)) return;

   ModelState ms = (ModelState) nms;

   if (line_number < 0) return;

   if (line_blocks == null && ms.line_blocks == null) {
      if (ms.getLineNumber() < line_number) {
	 if (ms.getEndLine() >= line_number-1) {
	    line_number = ms.getLineNumber();
	    if (ms.getEndLine() > end_line) end_line = ms.getEndLine();
	    return;
	  }
       }
      else if (ms.getLineNumber() <= end_line+1) {
	 if (ms.getEndLine() > end_line) end_line = ms.getEndLine();
	 return;
       }
    }

   if (line_blocks == null) {
      line_blocks = new TreeSet<LineBlock>();
      LineBlock lb = new LineBlock(line_number,end_line);
      line_blocks.add(lb);
    }

   if (ms.line_blocks == null) {
      LineBlock lb = new LineBlock(ms.line_number,ms.end_line);
      addLineBlock(lb);
    }
   else {
      for (Iterator<?> it = ms.line_blocks.iterator(); it.hasNext(); ) {
	 LineBlock lb = (LineBlock) it.next();
	 addLineBlock(lb);
       }
    }
}



private void addLineBlock(LineBlock lb)
{
   boolean add = true;

   SortedSet<LineBlock> ssl = line_blocks.headSet(lb);
   if (!ssl.isEmpty() && ssl.size() > 0) {
      try {
	 LineBlock llb = ssl.last();
	 if (llb.mergeWith(lb)) {
	    add = false;
	    lb = llb;
	  }
       }
      catch (Throwable t) {
	 System.err.println("CHET: PROBLEM With TREESET: " + t);
	 System.err.println("\tVALUES: " + ssl.size() + " " + ssl.isEmpty());
	 for (Iterator<LineBlock> it = ssl.iterator(); it.hasNext(); ) {
	    System.err.println("\tVALUE " + it.next());
	  }
	 t.printStackTrace();
       }
    }

   ssl = line_blocks.tailSet(lb);
   Iterator<LineBlock> it = ssl.iterator();
   if (it.hasNext()) {
      LineBlock flb = it.next();
      if (add) {
	 if (flb.mergeWith(lb)) add = false;
       }
      else {
	 if (flb == lb) {
	    if (it.hasNext()) flb = it.next();
	    else flb = null;
	  }
	 if (flb != null) {
	    if (lb.mergeWith(flb)) {
	       line_blocks.remove(flb);
	     }
	  }
       }
    }

   if (add) line_blocks.add(lb);
}



/********************************************************************************/
/*										*/
/*	Transition query methods						*/
/*										*/
/********************************************************************************/

boolean onlyConnection(ModelState s)
{
   return out_nodes.size() == 1 && out_nodes.contains(s);
}


ModelState getSingleTransition()
{
   if (out_nodes.size() != 1) return null;

   Iterator<JflowModel.Node> it = out_nodes.iterator();
   if (!it.hasNext()) return null;
   ModelState s = (ModelState) it.next();
   if (it.hasNext()) return null;

   return s;
}



@Override public void fixTransitions(Map<JflowModel.Node,JflowModel.Node> statemap)
{
   Set<JflowModel.Node> nn = new HashSet<JflowModel.Node>(out_nodes.size());
   for (JflowModel.Node ns : out_nodes) {
      nn.add(statemap.get(ns));
    }
   out_nodes = nn;
}




/********************************************************************************/
/*										*/
/*	Compatability methods							*/
/*										*/
/********************************************************************************/

@Override public boolean isCompatibleWith(JflowModel.Node ts)
{
   if (isFinal() != ts.isFinal()) return false;

   if (isSimple() && ts.isSimple()) return true;
   else if (isSimple() || ts.isSimple()) return false;

   if (ts.getClass() != getClass()) return false;
   if (!localCompatibleWith(ts)) return false;

   return true;
}



abstract boolean localCompatibleWith(JflowModel.Node ts);




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

void outputXml(IvyXmlWriter xw,ModelGenerator ceg)
{
   xw.begin("STATE");
   xw.field("ID",getName());
   if (line_number > 0 && in_method != null) {
      String src = in_method.getMethodClass().getSourceFile();
      if (src != null) xw.field("FILE",src);
      xw.field("LINE",line_number);
      xw.field("ENDLINE",end_line);
    }

   outputLocalXml(xw,ceg);

   if (line_blocks != null) {
      for (Iterator<LineBlock> it = line_blocks.iterator(); it.hasNext(); ) {
	 LineBlock lb = it.next();
	 xw.begin("BLOCK");
	 xw.field("START",lb.getStartLine());
	 xw.field("END",lb.getEndLine());
	 xw.end();
       }
    }

   for (JflowModel.Node nst : getTransitions()) {
      xw.begin("ARCTO");
      xw.field("ID",nst.getName());
      xw.end();
    }

   xw.end();
}



protected void outputLocalXml(IvyXmlWriter _xw,ModelGenerator _ceg)	{ }




@Override public void outputEvent(IvyXmlWriter xw) {
   JflowEvent evt = getEvent();
   if (evt == null) return;
   outputLocation(xw);
}




@Override public void outputLocation(IvyXmlWriter xw) {
   xw.begin("EVENTLOC");
   xw.field("STATEID",getName());
   if (in_method != null) {
      xw.field("METHOD",in_method.getMethodName());
      xw.field("SIGNATURE",in_method.getMethodSignature());
      if (line_number > 0) {
	 String src = in_method.getMethodClass().getSourceFile();
	 if (src != null) xw.field("FILE",src);
	 xw.field("LINE",line_number);
	 xw.field("ENDLINE",end_line);
       }
      if (line_blocks != null) {
	 for (Iterator<LineBlock> it = line_blocks.iterator(); it.hasNext(); ) {
	    LineBlock lb = it.next();
	    xw.begin("BLOCK");
	    xw.field("START",lb.getStartLine());
	    xw.field("END",lb.getEndLine());
	    xw.end();
	  }
       }
    }
   JflowEvent evt = getEvent();
   if (evt != null) evt.outputXml(xw);
   xw.end();
}




/********************************************************************************/
/*										*/
/*	Comparison methods							*/
/*										*/
/********************************************************************************/

@Override public int compareTo(ModelState ms)
{
   return state_id - ms.state_id;
}




/********************************************************************************/
/*										*/
/*	Subclass for Simple states						*/
/*										*/
/********************************************************************************/

private static class SimpleState extends ModelState {

   SimpleState(JflowMethod in,int lno) {
      super(in,lno);
    }

   @Override protected boolean localCompatibleWith(JflowModel.Node _ms) {
      return true;
    }

   @Override public boolean isSimple()			{ return true; }

}	// end of subclass SimpleState



/********************************************************************************/
/*										*/
/*	Subclass for Call states						*/
/*										*/
/********************************************************************************/

private static class CallState extends ModelState {

   private JflowMethod call_method;
   private boolean is_async;
   private boolean return_used;
   private boolean is_callonce;

   CallState(JflowMethod in,int lno,JflowMethod call,boolean async,boolean once,BT_Ins _ins) {
      super(in,lno);
      call_method = call;
      is_async = async;
      is_callonce = once;
      return_used = false;
      if (call == null) is_async = false;
    }

   @Override public JflowMethod getCall() 		{ return call_method; }
   @Override public boolean isAsync()			{ return is_async; }
   @Override public boolean isCallOnce()			{ return is_callonce; }
   @Override void clearCall() {
      call_method = null;
      is_async = false;
      return_used = false;
    }

   @Override void setUseReturn(boolean fg)		{ return_used = fg; }
   @Override public boolean getUseReturn()		{ return return_used; }

   @Override protected boolean localCompatibleWith(JflowModel.Node ms) {
      CallState cs = (CallState) ms;
      return (call_method == cs.call_method && is_async == cs.is_async);
    }

   @Override public boolean isSimple()			{ return call_method == null; }

   @Override protected void outputLocalXml(IvyXmlWriter xw,ModelGenerator ceg) {
      if (isAsync()) xw.field("ASYNC",true);
      if (return_used) xw.field("USERETURN",true);
      if (call_method != null) {
	 xw.field("CALL",call_method.getMethodName());
	 xw.field("SIGNATURE",call_method.getMethodSignature());
	 JflowModel.Method cs1 = ceg.findMethod(call_method);
	 xw.field("CALLID",cs1.getStartNode().getName());
       }
    }

}	// end of subclass CallState





/********************************************************************************/
/*										*/
/*	Subclass for Event states						*/
/*										*/
/********************************************************************************/

private static class EventState extends ModelState {

   private JflowEvent state_event;

   EventState(JflowMethod in,int lno,JflowEvent evt) {
      super(in,lno);
      state_event = evt;
    }

   @Override public JflowEvent getEvent() 		{ return state_event; }

   @Override protected boolean localCompatibleWith(JflowModel.Node ms) {
      EventState es = (EventState) ms;
      return state_event == es.state_event;
    }

   @Override protected void outputLocalXml(IvyXmlWriter xw,ModelGenerator _mg) {
      if (state_event != null) {
	 xw.field("HASEVENT",true);
	 state_event.outputXml(xw);
       }
    }

}	// end of subclass EventState




/********************************************************************************/
/*										*/
/*	Subclass to handle Wait, Notify, and Synchronization			*/
/*										*/
/********************************************************************************/

private static class WaitState extends ModelState {

   private ModelSourceSet wait_set;
   private ModelWaitType wait_type;

   WaitState(JflowMethod in,int lno,JflowValue setval,ModelWaitType waittype) {
      super(in,lno);
      wait_set = new ModelSourceSet();
      for (JflowSource js : setval.getSourceCollection()) wait_set.add(js);
      if (wait_set.isEmpty()) wait_type = ModelWaitType.NONE;
      else wait_type = waittype;
    }

   @Override public ModelWaitType getWaitType()	{ return wait_type; }
   @Override public Collection<JflowSource> getWaitSet()		 { return wait_set; }
   @Override public void setWaitSet(ModelSourceSet s) {
      if (wait_set == null) return;
      if (s == null) clearCall();
      else wait_set = s;
    }

   @Override public boolean isSimple()		{ return wait_set == null; }
   @Override void clearCall()			{ wait_set = null; wait_type = ModelWaitType.NONE; }

   @Override protected boolean localCompatibleWith(JflowModel.Node ms) {
      WaitState ws = (WaitState) ms;
      return wait_set == ws.wait_set && wait_type == ws.wait_type;
    }

   @Override protected void outputLocalXml(IvyXmlWriter xw,ModelGenerator _mg) {
      if (wait_set != null) {
	 xw.begin("WAIT");
	 xw.field("TYPE",wait_type.toString());
	 for (JflowSource src : wait_set) {
	    src.outputXml(xw,null);
	  }
	 xw.end();
       }
    }

}	// end of subclass WaitState




/********************************************************************************/
/*										*/
/*	Subclass to handle conditions						*/
/*										*/
/********************************************************************************/

private static class CondState extends ModelState {

   private ModelCondition event_condition;

   CondState(JflowMethod in,int lno,ConditionType ctyp,JflowSource src) {
      super(in,lno);
      event_condition = new ModelCondition(ctyp,src);
    }

   @Override public JflowModel.Field getCondition() {
      return event_condition;
    }

   @Override protected boolean localCompatibleWith(JflowModel.Node ms) {
      CondState cs = (CondState) ms;
      return event_condition.equals(cs.event_condition);
    }

   @Override public boolean isSimple()		{ return event_condition == null; }
   @Override void clearCall()			{ event_condition = null; }

   @Override protected void outputLocalXml(IvyXmlWriter xw,ModelGenerator _mg) {
      if (event_condition != null) {
	 xw.field("CONDITION",event_condition.getConditionType().toString());
	 event_condition.getFieldSource().outputXml(xw,null);
       }
    }

}	// end of subclass CondState




/********************************************************************************/
/*										*/
/*	Subclass to deal with System.exit					*/
/*										*/
/********************************************************************************/

private static class ExitState extends ModelState {

   ExitState(JflowMethod in,int lno) {
      super (in,lno);
    }

   @Override public boolean isExit()		{ return true; }

   @Override public boolean localCompatibleWith(JflowModel.Node ms) {
      return true;
    }

   @Override protected void outputLocalXml(IvyXmlWriter xw,ModelGenerator _mg) {
      xw.field("EXIT",true);
    }

}	// end of subclass ExitState



/********************************************************************************/
/*										*/
/*	Subclass to handle fields						*/
/*										*/
/********************************************************************************/

private static class FieldState extends ModelState {

   private ModelCondition field_condition;

   FieldState(JflowMethod in,int lno,JflowSource src,Object val) {
      super(in,lno);
      field_condition = new ModelCondition(src,val);
    }

   @Override public JflowModel.Field getFieldSet() {
      return field_condition;
    }

   @Override protected boolean localCompatibleWith(JflowModel.Node ms) {
      FieldState fs = (FieldState) ms;
      return field_condition.equals(fs.field_condition);
    }

   @Override public boolean isSimple()		{ return field_condition == null; }
   @Override void clearCall()			{ field_condition = null; }

   @Override protected void outputLocalXml(IvyXmlWriter xw,ModelGenerator _mg) {
      if (field_condition != null) {
	 if (field_condition.getFieldValue() == null) xw.field("FIELDSET","null");
	 else xw.field("FIELDSET",field_condition.getFieldValue().toString());
	 field_condition.getFieldSource().outputXml(xw,null);
       }
    }

}	// end of subclass FieldState




/********************************************************************************/
/*										*/
/*	Subclass to handle return states					*/
/*										*/
/********************************************************************************/

private static class ReturnState extends ModelState {

   private ModelValue return_value;

   ReturnState(JflowMethod in,int lno,ModelValue cv) {
      super(in,lno);
      return_value = cv;
      checkValue();
    }

   @Override protected boolean localCompatibleWith(JflowModel.Node ms) {
      ReturnState rs = (ReturnState) ms;
      return return_value == rs.return_value;
    }

   @Override public boolean isSimple()		{ return return_value == null; }
   @Override boolean isReturn()			{ return true; }
   @Override void clearCall()			{ return_value = null; }

   @Override public Object getReturnValue() {
      if (return_value == null) return null;
      return return_value.getProgramValue();
    }

   @Override ModelState mergeReturn(ModelState ms) {
      ReturnState rs = (ReturnState) ms;
      if (return_value == null) return this;
      if (rs.return_value == null) return rs;
      ModelValue cv = return_value.mergeValue(rs.return_value);
      if (cv == return_value) return this;
      if (cv == rs.return_value) return rs;
      ModelState ns = new ReturnState(getMethod(),getLineNumber(),cv);
      for (JflowModel.Node nrst : getTransitions()) {
	 ModelState rst = (ModelState) nrst;
	 ns.addTransition(rst);
       }
      return ns;
    }

   @Override protected void outputLocalXml(IvyXmlWriter xw,ModelGenerator _mg) {
      if (return_value != null) {
	 xw.field("PROGRAMRETURN",return_value.getProgramValue().toString());
       }
    }

   private void checkValue() {
      Object o = return_value.getProgramValue();
      if (o == null) return_value = null;
    }

}	// end of subclass ReturnState




/********************************************************************************/
/*										*/
/*	Data structure for line number blocks					*/
/*										*/
/********************************************************************************/

private static class LineBlock implements Comparable<LineBlock> {

   private int start_line;
   private int end_line;

   LineBlock(int s,int e) {
      start_line = s;
      end_line = e;
    }

   @Override public int compareTo(LineBlock olb) {
      if (start_line == olb.start_line) return 0;
      else if (start_line < olb.start_line) return -1;
      else return 1;
    }

   int getStartLine()				{ return start_line; }
   int getEndLine()				{ return end_line; }

   boolean mergeWith(LineBlock lb) {
      if (lb.start_line < start_line) {
	 if (lb.end_line >= start_line-1) {
	    start_line = lb.start_line;
	    if (lb.end_line > end_line) end_line = lb.end_line;
	    return true;
	  }
       }
      else if (lb.start_line <= end_line+1) {
	 if (lb.end_line > end_line) end_line = lb.end_line;
	 return true;
       }
      return false;
    }

   @Override public String toString() {
      return "LINEBLOCK[" + start_line + "-" + end_line + "]";
    }

}	// end of subclass LineBlock




}	// end of class ModelState




/* end of ModelState.java */

