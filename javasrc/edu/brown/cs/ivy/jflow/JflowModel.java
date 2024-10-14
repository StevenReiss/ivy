/********************************************************************************/
/*										*/
/*		JflowModel.java 						*/
/*										*/
/*	Interface for abstract model of a program wrt an event automaton	*/
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


package edu.brown.cs.ivy.jflow;


import edu.brown.cs.ivy.xml.IvyXmlWriter;

import java.util.Collection;
import java.util.Map;



public interface JflowModel extends JflowConstants
{

   Main createModel();
   void setOption(FlowOption opt,boolean fg);
   void setStartClass(String cls);
   JflowMaster getFlowMaster();

   interface Main extends JflowConstants {
      Iterable<JflowModel.Method> getStartMethods();
      JflowModel.Method findMethod(JflowMethod bm);
      void outputEvents(IvyXmlWriter xw);
      void outputProgram(IvyXmlWriter xw);
      void outputGlobalProgram(IvyXmlWriter xw,Collection<Main> merges,Editor editor);
      JflowModel.Node getThreadStartNode(JflowMethod m);
      JflowMaster getFlowMaster();
      JflowModel getModel();
      Threads createThreadModel();
    }

   interface Node extends JflowConstants {
      String getName();
      JflowMethod getCall();
      Field getCondition();
      Field getFieldSet();
      JflowEvent getEvent();
      Iterable<JflowModel.Node> getTransitions();
      boolean isFinal();
      ModelWaitType getWaitType();
      Collection<JflowSource> getWaitSet();
      boolean isAsync();
      boolean isCallOnce();
      Object getReturnValue();
      boolean isSimple();
      boolean getUseReturn();
      boolean isExit();
      void outputEvent(IvyXmlWriter xw);
      void outputLocation(IvyXmlWriter xw);
      void addTransition(JflowModel.Node n);
      void removeTransition(JflowModel.Node n);
      boolean isCompatibleWith(JflowModel.Node n);
      void fixTransitions(Map<JflowModel.Node,JflowModel.Node> statemap);
      void mergeLinesFromState(JflowModel.Node n);
    }

   interface Method extends JflowConstants {
      JflowMethod getMethod();
      Node getStartNode();
      Node getEndNode();
    }

   interface Field extends JflowConstants {
      ConditionType getConditionType();
      JflowSource getFieldSource();
      Object getFieldValue();
    }

   interface Threads extends JflowConstants {
      Node getStartNode(JflowMethod jm);
    }

   interface Editor extends JflowConstants {
      void editModel(JflowModel.Node start);
    }

}	// end of interface JflowModel




/* end of JflowModel.java */
