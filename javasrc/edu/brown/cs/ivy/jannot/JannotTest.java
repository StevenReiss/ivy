/********************************************************************************/
/*										*/
/*		JannotTest.java 						*/
/*										*/
/*	Test driver for Jannot							*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
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



package edu.brown.cs.ivy.jannot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.Test;
import org.junit.Assert;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.jcomp.JcompControl;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSemantics;
import edu.brown.cs.ivy.jcomp.JcompSource;

public class JannotTest
{


/********************************************************************************/
/*										*/
/*	Main program for debugging						*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   JannotTest jt = new JannotTest();
   jt.testGen();
}




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JannotFactory jannot_factory;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public JannotTest()
{
   jannot_factory = JannotFactory.getFactory();
}



/********************************************************************************/
/*										*/
/*	Generating Processor test						*/
/*										*/
/********************************************************************************/

@Test
public void testGen()
{
   JannotProject jap = setupTestProject();
   jap.addAnnotationProcessor("spr.annotproc.JannotTestGeneratingAnnotationProcessor");
   boolean sts = jap.processProject();
   Assert.assertTrue("Annotation processor failed",sts);
   for (JcompSemantics sem : jap.getJcompProject().getSources()) {
      JcompSource src = sem.getFile();
      if (src instanceof LocalSource) continue;
      if (src.getFileName().contains("/")) continue;
      ASTNode n = sem.getAstNode();
      System.err.println("RESULT: " + src.getFileName() + "\n" + n);
    }
}




/********************************************************************************/
/*										*/
/*	Generating Processor test						*/
/*										*/
/********************************************************************************/

@Test
public void testMutate()
{
   JannotProject jap = setupTestProject();
   jap.addAnnotationProcessor("spr.annotproc.JannotTestMutatingAnnotationProcessor");
   boolean sts = false;
   try {
      sts = jap.processProject();
    }
   catch (Throwable t) {
      System.err.println("Problem with mutation processing: " + t);
      t.printStackTrace();
    }
   Assert.assertTrue("Annotation processor failed",sts);
   for (JcompSemantics sem : jap.getJcompProject().getSources()) {
      JcompSource src = sem.getFile();
      ASTNode n = sem.getAstNode();
      System.err.println("RESULT: " + src.getFileName() + "\n" + n);
    }
}




/********************************************************************************/
/*										*/
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

private JannotProject setupTestProject()
{
   JcompControl ctrl = new JcompControl();
   List<JcompSource> srcs = new ArrayList<>();
   JcompSource src = new LocalSource("/u/spr/sampler/spr/annotproc/JannotTestMutableClass.java");
   srcs.add(src);
   List<String> jars = new ArrayList<>();
   jars.add("/u/spr/sampler/spr/annotproc/annotproc.jar");
   jars.add("/pro/ivy/java");
   
   JcompProject jp = ctrl.getProject(jars, srcs);
   jp.resolve();
   JannotProject jap = jannot_factory.getProject(jp);
   return jap;
}



private static class LocalSource implements JcompSource {

   private String file_name;

   LocalSource(String file) {
      file_name = file;
    }

   @Override public String getFileContents() {
      try {
	 return IvyFile.loadFile(new File(file_name));
       }
      catch (IOException e) {
	 return null;
       }
    }

   @Override public String getFileName() {
      return file_name;
    }

}	// end of inner class LocalSource



}	// end of class JannotTest




/* end of JannotTest.java */

