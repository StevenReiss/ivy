/********************************************************************************/
/*										*/
/*		JcodeTest.java							*/
/*										*/
/*	Test methods for byte code access					*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
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



package edu.brown.cs.ivy.jcode;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collection;



public class JcodeTest
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcodeFactory	bcode_factory;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public JcodeTest()
{
   bcode_factory = new JcodeFactory(1);
   File f1 = new File(System.getProperty("user.home"));
   File f2 = new File(f1,"sampler");
   bcode_factory.addToClassPath(f2.getPath());
// bcode_factory.findClass("spr.onsets.OnsetMain");
}




/********************************************************************************/
/*										*/
/*	Test for loading classes						*/
/*										*/
/********************************************************************************/

@Test public void loadClasses()
{
   JcodeDataType dt = bcode_factory.findClassType("spr.onsets.OnsetMain");
   Assert.assertNotNull(dt);
}



@Test public void lookupClass()
{
   JcodeDataType t1 = bcode_factory.findClassType("java.lang.Object");
   JcodeDataType t2 = bcode_factory.findClassType("java.lang.Object");
   JcodeDataType t3 = bcode_factory.findClassType("java.lang.Object[]");
   JcodeDataType t4 = bcode_factory.findJavaType("I");
   JcodeDataType t5 = bcode_factory.findJavaType("V");
   JcodeDataType t6 = bcode_factory.findJavaType("[C");
   Assert.assertNotNull(t1);
   Assert.assertEquals(t1,t2);
   Assert.assertNotNull(t3);
   Assert.assertNotNull(t4);
   Assert.assertNotNull(t5);
   Assert.assertNotNull(t6);

   JcodeDataType x1 = bcode_factory.findNamedType("int");
   JcodeDataType x2 = bcode_factory.findNamedType("spr.onsets.OnsetExprSet");
   JcodeDataType x3 = bcode_factory.findNamedType("spr.onsets.OnsetExprSet.Expr");
   JcodeDataType x4 = bcode_factory.findNamedType("char[]");
   Assert.assertNotNull(x1);
   Assert.assertNotNull(x2);
   Assert.assertNotNull(x3);
   Assert.assertNotNull(x4);

   JcodeDataType y1 = bcode_factory.findJavaType("Lspr/onsets/OnsetTypeSet;");
   Assert.assertNotNull(y1);
   JcodeClass y2 = bcode_factory.findClass("spr.onsets.OnsetTypeSet");
   Assert.assertNotNull(y2);
   Assert.assertEquals(y1,y2.getDataType());
   JcodeClass y3 = bcode_factory.findClass("spr.onsets.OnsetNumberSet");
   JcodeMethod y4 = y2.findInheritedMethod("check","(I)Z");
   JcodeMethod y5 = y3.findInheritedMethod("check","(I)Z");
   JcodeMethod y6 = y2.findInheritedMethod("checkCube","(Lspr/onsets/OnsetCube;)Z");
   Assert.assertNotNull(y4);
   Assert.assertEquals(y4,y5);
   Assert.assertNotNull(y6);

   Collection<JcodeClass> y7 = y2.getParents();
   Assert.assertEquals(y7.size(),1);
   Collection<JcodeMethod> y8 = y2.getMethods();
   Assert.assertEquals(y8.size(),4);
}


@Test public void lookupField()
{
   JcodeField f1 = bcode_factory.findField(null,"spr.onsets.OnsetMain","card_deck");
   Assert.assertNotNull(f1);

   JcodeMethod m1 = bcode_factory.findMethod(null,
	 "java/io/FileOutputStream","<init>",
	 "(Ljava/lang/String;)V");
   Assert.assertNotNull(m1);
}



}	// end of class BocdeTest




/* end of JcodeTest.java */
