/********************************************************************************/
/*										*/
/*		JflowTest.java							*/
/*										*/
/*	Test program for Java Flow Analyzer					*/
/*										*/
/********************************************************************************/
/*	Copyright 2006 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2006, Brown University, Providence, RI.				 *
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


import edu.brown.cs.ivy.cinder.CinderManager;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_ClassRefIns;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_Opcodes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JflowTest implements JflowConstants
{


public static void main(String [] args)
{
   switch (args.length) {
      case 0 :
	 test1();
	 break;
      case 1 :
	 test2();
	 break;
      case 2 :
	 test3();
	 break;
      case 3 :
	 test4();
	 break;
      case 4 :
	 test5();
	 break;
    }
}





/********************************************************************************/
/*										*/
/*	Simple test -- flow on onsets						*/
/*										*/
/********************************************************************************/

private static void test1()
{
   JflowMaster jm = JflowFactory.createFlowMaster(new TestControl());

   jm.setClassPath("/u/spr/sampler");
   jm.addDescriptionFile("/pro/ivy/jflow/src/jflow.xml");
   jm.setStartClass("spr.onsets.OnsetMain");
   jm.setOption(FlowOption.DO_DEBUG,true);

   try {
      jm.analyze();
      jm.cleanup();
      System.err.println("FLOW DONE");
    }
   catch (JflowException e) {
      System.err.println("FLOW ERROR: " + e);
    }
}




/********************************************************************************/
/*										*/
/*	UI test -- flow on SOLAR						*/
/*										*/
/********************************************************************************/

private static void test2()
{
   TestControl tf = new TestIOControl();
   JflowMaster jm = JflowFactory.createFlowMaster(tf);
   JflowModel mdl = JflowFactory.createModelMaster(jm,tf);

   jm.setClassPath("/home/spr/solar/java:/research/ivy/java:/home/spr/jogl/jogl-linux64/jogl.jar");
   jm.addDescriptionFile("/pro/ivy/jflow/src/jflow.xml");
   jm.addDescriptionFile("/pro/ivy/jflow/src/jflowivy.xml");
   jm.setStartClass("edu.brown.cs.cs032.solar.SolarMain");

   mdl.setOption(FlowOption.DO_UNDECLARED_EXCEPTIONS,false);

   jm.setOption(FlowOption.DO_DEBUG,true);
   // mdl.setOption(FlowOption.DO_DEBUG,true);

   try {
      jm.analyze();
      jm.cleanup();
      System.err.println("FLOW DONE");
      JflowModel.Main mm = mdl.createModel();
      if (mm != null) {
	 FileWriter fw = new FileWriter("/data/fred4/spr/model.out");
	 IvyXmlWriter xw = new IvyXmlWriter(fw);
	 mm.outputProgram(xw);
	 mm.outputGlobalProgram(xw,null,null);
	 xw.close();
       }
      else System.err.println("NO MODEL CREATED");
    }
   catch (JflowException e) {
      System.err.println("FLOW ERROR: " + e);
    }
   catch (IOException e) {
      System.err.println("IO ERROR: " + e);
    }
}





/********************************************************************************/
/*										*/
/*	UI test -- flow on VUIT 						*/
/*										*/
/********************************************************************************/

private static void test3()
{
   TestControl tf = new TestIOControl();
   JflowMaster jm = JflowFactory.createFlowMaster(tf);
   JflowModel mdl = JflowFactory.createModelMaster(jm,tf);

   jm.setClassPath("/pro/ivy/java:/pro/veld/java:/pro/ivy/lib/jikesbt.jar");
   jm.addClass("edu.brown.cs.veld.vuit.VuitObjectWizard");
   jm.addClass("edu.brown.cs.veld.vuit.VuitFieldWizard");
   jm.addDescriptionFile("/pro/ivy/jflow/src/jflow.xml");
   jm.addDescriptionFile("/pro/ivy/jflow/src/jflowivy.xml");
   jm.setStartClass("edu.brown.cs.veld.vuit.VuitMain");

   // jm.setOption(FlowOption.DO_DEBUG,true);
   // mdl.setOption(FlowOption.DO_DEBUG,true);

   try {
      jm.analyze();
      jm.cleanup();
      System.err.println("FLOW DONE");
      JflowModel.Main mm = mdl.createModel();
      if (mm != null) {
	 FileWriter fw = new FileWriter("/data/fred4/spr/model.out");
	 IvyXmlWriter xw = new IvyXmlWriter(fw);
	 mm.outputProgram(xw);
	 xw.close();
       }
      else System.err.println("NO MODEL CREATED");
    }
   catch (JflowException e) {
      System.err.println("FLOW ERROR: " + e);
    }
   catch (IOException e) {
      System.err.println("IO ERROR: " + e);
    }
}





/********************************************************************************/
/*										*/
/*	Incremental test -- check if changes detected				*/
/*										*/
/********************************************************************************/

private static void test4()
{
   JflowMaster jm = JflowFactory.createFlowMaster(new TestControl());

   jm.setClassPath("/u/spr/sampler:/pro/clime/lib/junit.jar:/pro/ivy/java");
   jm.addDescriptionFile("/pro/ivy/jflow/src/jflow.xml");
   jm.addClass("spr.onsets.OnsetMain");
   jm.addClass("spr.onsets.TestBitSet");
   jm.setOption(FlowOption.DO_DEBUG,true);

   try {
      jm.analyze();
      System.err.println("FLOW DONE");
    }
   catch (JflowException e) {
      System.err.println("FLOW ERROR: " + e);
    }

   try {
      File f1 = new File("/u/spr/sampler/spr/onsets/OnsetMain.class.save");
      File f2 = new File("/u/spr/sampler/spr/onsets/OnsetMain.class.new");
      File f3 = new File("/u/spr/sampler/spr/onsets/OnsetMain.class");
      String [] args = new String [] { "cp", f1.getPath(), f3.getPath() };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();
      System.err.println("NOTE CHANGE " + f3);

      jm.noteChanged(f3);

      System.err.println("START RECHECK");
      jm.updateChanged();
      System.err.println("RECHECK DONE");

      args = new String [] { "cp", f2.getPath(), f3.getPath() };
      p = Runtime.getRuntime().exec(args);
      p.waitFor();
    }
   catch (JflowException e) {
      System.err.println("FLOW RECHECK ERROR: " + e);
    }
   catch (InterruptedException e) {
      System.err.println("COPY INTERRUPTED: " + e);
    }
   catch (IOException e) {
      System.err.println("I/O ERROR IN TEST: " + e);
    }
}



/********************************************************************************/
/*										*/
/*	Test 5: Bubbles 							*/
/*										*/
/********************************************************************************/

private static void test5()
{
   TestControl tf = new TestIOControl();
   JflowMaster jm = JflowFactory.createFlowMaster(tf);
   JflowModel mdl = JflowFactory.createModelMaster(jm,tf);

   String cp = "/pro/bubbles/java:/pro/ivy/java:/pro/bubbles/lib/bubblesasm.jar";
   cp += ":/pro/bubbles/lib/gnujpdf.jar:/pro/bubbles/lib/iText.jar";
   cp += ":/pro/bubbles/lib/jsyntaxpane.jar:/pro/bubbles/lib/junit.jar";
   cp += ":/pro/bubbles/lib/smack.jar";

   jm.setClassPath(cp);
   jm.addDescriptionFile("/pro/ivy/jflow/src/jflow.xml");
   jm.addDescriptionFile("/pro/ivy/jflow/src/jflowivy.xml");
   jm.setStartClass("edu.brown.cs.bubbles.bema.BemaMain");

   mdl.setOption(FlowOption.DO_UNDECLARED_EXCEPTIONS,false);

   jm.setOption(FlowOption.DO_DEBUG,true);
   // mdl.setOption(FlowOption.DO_DEBUG,true);

   try {
      jm.analyze();
      jm.cleanup();
      System.err.println("FLOW DONE");
      JflowModel.Main mm = mdl.createModel();
      if (mm != null) {
	 FileWriter fw = new FileWriter("/data/fred4/spr/model.out");
	 IvyXmlWriter xw = new IvyXmlWriter(fw);
	 mm.outputProgram(xw);
	 mm.outputGlobalProgram(xw,null,null);
	 xw.close();
       }
      else System.err.println("NO MODEL CREATED");
    }
   catch (JflowException e) {
      System.err.println("FLOW ERROR: " + e);
    }
   catch (IOException e) {
      System.err.println("IO ERROR: " + e);
    }
}



/********************************************************************************/
/*										*/
/*	Dummy factory for test data						*/
/*										*/
/********************************************************************************/


public static class TestControl implements JflowControl {

   @Override public JflowModelSource createModelSource(JflowMethod m,int ino,BT_Ins ins,JflowValue base) {
      return null;
    }

   @Override public JflowMethodData createMethodData(JflowMethod jm) {
      return new TestData(jm);
    }

   @Override public boolean checkUseMethod(JflowMethod m) {
      return false;
    }

   @Override public JflowEvent findEvent(JflowModel jm,JflowMethod m,BT_Ins ins,boolean start,List<Object> vals) {
      return null;
    }

   @Override public Collection<JflowEvent> getRequiredEvents() {
      return null;
    }

   @Override public boolean isFieldTracked(BT_Field fld) {
      return false;
    }

   @Override public boolean checkUseCall(JflowMethod from,BT_Method to) {
      return true;
    }

}



/********************************************************************************/
/*										*/
/*	Dummy factory for test data						*/
/*										*/
/********************************************************************************/


public static class TestIOControl extends TestControl {

   private Map<String,TestEvent> event_map;

   TestIOControl() {
      event_map = new HashMap<>();
    }

   @Override public JflowModelSource createModelSource(JflowMethod m,int ino,BT_Ins ins,JflowValue base) {
      if (ins.opcode == BT_Opcodes.opc_new) {
	 BT_ClassRefIns crins = (BT_ClassRefIns) ins;
	 for (BT_Class cls = crins.getTarget(); cls != null; cls = cls.getSuperClass()) {
	    if (cls.getName().equals("java.awt.Component")) {
	       JflowModelSource msrc = new TestSource(m,ino,crins.getTarget().getName());
	       return msrc;
	     }
	  }
       }
      return null;
    }

   @Override public JflowMethodData createMethodData(JflowMethod jm) {
      return new TestData(jm);
    }

   @Override public boolean checkUseMethod(JflowMethod m) {
      return true;
    }

   @Override public JflowEvent findEvent(JflowModel jm,JflowMethod m,BT_Ins ins,boolean start,
				  List<Object> vals) {
      if (!start) return null;
      BT_Method mthd = m.getMethod();
      if (!jm.getFlowMaster().isProjectClass(mthd.getDeclaringClass())) return null;

      JflowValue jv = m.getAssociation(AssociationType.NEW,ins);
      if (jv == null) jv = m.getAssociation(AssociationType.THISARG,ins);
      if (jv == null) return null;
      String etyp = null;
      switch (ins.opcode) {
	 case BT_Opcodes.opc_new :
	    etyp = "NEW " + ins.getClassTarget().getName();
	    break;
	 case BT_Opcodes.opc_invokeinterface :
	 case BT_Opcodes.opc_invokestatic :
	 case BT_Opcodes.opc_invokespecial :
	 case BT_Opcodes.opc_invokevirtual :
	    BT_Method bm = ins.getMethodTarget();
	    String cnm = bm.getDeclaringClass().getName();
	    if (!cnm.startsWith("javax.swing") && !cnm.startsWith("java.awt")) return null;
	    etyp = "USE " + CinderManager.getMethodName(bm) + " (";
	    for (int i = 1; ; ++i) {
	       try {
		  AssociationType at = AssociationType.valueOf("ARG" + i);
		  JflowValue ajv = m.getAssociation(at,ins);
		  if (ajv == null) break;
		  if (i > 1) etyp += ",";
		  Object o = ajv.getProgramValue();
		  if (o == null) etyp += "?";
		  else etyp += o.toString();
		}
	       catch (IllegalArgumentException e) {
		  break;
		}
	     }
	    etyp += ")";
	    break;
	 default :
	    return null;
       }

      boolean use = false;
      for (JflowSource src : jv.getSourceCollection()) {
	 if (src.isModel()) {
	    TestSource ts = (TestSource) src.getModelSource();
	    etyp += " " + ts.getId();
	    use = true;
	  }
       }
      if (!use) return null;

      TestEvent te = event_map.get(etyp);
      if (te == null) {
	 te = new TestEvent(etyp);
	 event_map.put(etyp,te);
       }

      return te;
    }

   @Override public Collection<JflowEvent> getRequiredEvents() {
      return null;
    }

   @Override public boolean isFieldTracked(BT_Field fld) {
      return false;
    }

}


/********************************************************************************/
/*										*/
/*	Test sources								*/
/*										*/
/********************************************************************************/

private static int source_ctr = 0;


private static class TestSource implements JflowModelSource {

   private int source_id;
   private JflowMethod for_method;
   private int for_inst;
   private String base_class;

   TestSource(JflowMethod jm,int ino,String cls) {
      for_method = jm;
      for_inst = ino;
      base_class = cls;
      source_id = ++source_ctr;
    }

   @Override public String getModelSourceType() 	{ return "NEW_" + base_class; }

   int getId()					{ return source_id; }

   @Override public void outputLocalXml(IvyXmlWriter xw) {
      xw.field("CLASS",base_class);
      xw.field("METHOD",for_method.getMethodName());
      xw.field("INO",for_inst);
    }

   @Override public String toString() {
      return "TEST " + base_class + "@" + for_method.getMethodName() + "/" + for_inst;
    }

}	// end of subclass TestSource



/********************************************************************************/
/*										*/
/*	Test method data							*/
/*										*/
/********************************************************************************/

private static class TestData extends JflowDefaultMethodData {

   TestData(JflowMethod jm) {
      super(jm);
    }

   @Override protected boolean useAssociation(AssociationType typ,BT_Ins ins,JflowValue v) {
      return true;
    }

}	// end of subclass TestData



/********************************************************************************/
/*										*/
/*	Test event								*/
/*										*/
/********************************************************************************/

private static class TestEvent implements JflowEvent {

   private String event_name;

   TestEvent(String id) {
      event_name = id;
    }

   @Override public void outputXml(IvyXmlWriter xw) {
      xw.begin("EVENT");
      xw.field("ID",event_name);
      xw.end();
    }

   @Override public String toString() {
      return event_name;
    }

}	// end of subclass TestEvent


}	// end of class JflowTest



/* end of JflowTest.java */





