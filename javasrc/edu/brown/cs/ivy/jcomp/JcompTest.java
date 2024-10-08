package edu.brown.cs.ivy.jcomp;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.jcode.JcodeFactory;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JcompTest {



/********************************************************************************/
/*										*/
/*	Static definitions							*/
/*										*/
/********************************************************************************/

private JcompControl	 jcomp_control;

private static String test_1 = "class Simple {\n" +
	"static void main() {\n" +
	"   System.out.println();\n" +
	"}\n" +
	"}\n";


private static String test_2 = "public class A {\n" +
	"   int x = 3;\n" +
	"   B b;\n" +
	"   double x = Math.PI;\n" +
        "   double y = 0x1.0p-52d;\n" +
	"}\n";


private static String test_3 = "import java.util.*;\n" +
	"import java.io.*;\n" +
	"import java.util.PriorityQueue;\n" +
	"public class Node implements Comparable<Node> {\n" +
	"    Node left;\n" +
	"    Node right;\n" +
	"    Node parent;\n" +
	"    String text;\n" +
	"    Float frequency;\n" +
	"    public Node(String textIn, Float frequencies) {\n" +
	"        text = textIn;\n" +
	"        frequency = frequencies;\n" +
	"    }\n" +
	"    public Node(Float d) {\n" +
	"        text = \"\";\n" +
	"        frequency = d;\n" +
	"    }\n" +
	"    public int compareTo(Node n) {\n" +
	"        if (frequency < n.frequency) {\n" +
	"            return -1;\n" +
	"        } else if (frequency > n.frequency) {\n" +
	"            return 1;\n" +
	"        }\n" +
	"        return 0;\n" +
	"    }\n" +
	"    public static void buildPath(Node root,String code)\n" +
	"    {\n" +
	"        if (root!=null)\n" +
	"            {\n" +
	"                if (root.left!=null)\n" +
	"                    buildPath(root.left, code+\"0\");\n" +
	"                if (root.right!=null)\n" +
	"                    buildPath(root.right,code+\"1\");\n" +
	"                if (root.left==null && root.right==null)\n" +
	"                    System.out.println(root.text+\": \"+code);\n" +
	"            }\n" +
	"    }\n" +
	"    public static Node makeHuffmanTree(Float[] frequencies, String[] text) {\n" +
	"        PriorityQueue<Node> queue = new PriorityQueue<Node>();\n" +
	"        for (int i = 0; i < text.length; i++) {\n" +
	"            Node n = new Node(text[i], frequencies[i]);\n" +
	"            queue.add(n);\n" +
	"        }\n" +
	"        Node root = null;\n" +
	"        while (queue.size() > 1) {\n" +
	"            Node least1 = queue.poll();\n" +
	"            Node least2 = queue.poll();\n" +
	"            Node combined = new Node(least1.frequency + least2.frequency);\n" +
	"            combined.right = least1;\n" +
	"            combined.left = least2;\n" +
	"            least1.parent = combined;\n" +
	"            least2.parent = combined;\n" +
	"            queue.add(combined);\n" +
	"            // Keep track until we actually find the root\n" +
	"            root = combined;\n" +
	"        }\n" +
	"        return root;\n" +
	"    }\n" +
	"}\n";


private static String test_4 = "public class Tester {\n" +
	"   private String the_string;\n" +
	"   private static enum TEST { A, B, C };\n" +
	"   Tester(String s) {\n" +
	"      the_string = s;\n" +
	"    }\n" +
	"   Tester() {\n" +
	"      this(\"Hello World\");\n" +
	"    }\n" +
	"   private void method() {\n" +
	"      Character c = 'c';\n" +
	"      Character.digit(c,16);\n" +
	"      char_to_int('c');\n" +
	"      int x = 0;\n" +
	"      x += c;\n" +
	"      TEST.values();\n" +
	"      TEST.A.ordinal();\n" +
	"    }\n" +
	"   private int char_to_int(Character c) { return 0; }\n" +
	"}\n";




private static String test_5 = "public class TwoTypePair<T1, T2>\n" +
	"{\n" +
	"   private T1 first;\n" +
	"   private T2 second;\n" +
	"   public TwoTypePair()\n" +
	"      {\n" +
	"      first = null;\n" +
	"      second = null;\n" +
	"    }\n" +
	"   public TwoTypePair(T1 firstItem, T2 secondItem)\n" +
	"      {\n" +
	"      first = firstItem;\n" +
	"      second = secondItem;\n" +
	"    }\n" +
	"   public void setFirst(T1 newFirst)\n" +
	"      {\n" +
	"      first = newFirst;\n" +
	"    }\n" +
	"   public void setSecond(T2 newSecond)\n" +
	"      {\n" +
	"      second = newSecond;\n" +
	"    }\n" +
	"   public T1 getFirst()\n" +
	"      {\n" +
	"      return first;\n" +
	"    }\n" +
	"   public T2 getSecond()\n" +
	"      {\n" +
	"      return second;\n" +
	"    }\n" +
	"   public boolean equals(Object otherObject)\n" +
	"      {\n" +
	"      if (otherObject == null)\n" +
	" return true;\n" +
	"      else if (getClass( ) != otherObject.getClass( ))\n" +
	" return false;\n" +
	"      else\n" +
	" {\n" +
	" TwoTypePair<T1, T2> otherPair =\n" +
	" (TwoTypePair<T1, T2>)otherObject;\n" +
	" return (first.equals(otherPair.first)\n" +
	"    && second.equals(otherPair.second));\n" +
	"       }\n" +
	"    }\n" +
	"}\n";



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public JcompTest()
{
   jcomp_control = new JcompControl();
}



/********************************************************************************/
/*										*/
/*	Common test code							*/
/*										*/
/********************************************************************************/

private static int showMessages(String what,JcompProject proj)
{
   int ct = 0;

   System.err.println("FOR TEST " + what);
   try {
      proj.resolve();
    }
   catch (Throwable t) {
      t.printStackTrace();
      return -1;
    }

   for (JcompMessage msg : proj.getMessages()) {
      System.err.println("MSG:" + msg.getSeverity() + " " + msg.getSource() + ":" +
			    msg.getLineNumber() + " (" +
			    msg.getStartOffset() + "-" + msg.getEndOffset() + ") " +
			    msg.getText());
      ++ct;
    }

   return ct;
}



/********************************************************************************/
/*										*/
/*	Actual test cases							*/
/*										*/
/********************************************************************************/

@Test
public void jcompTest1()
{
   JcompSource s1 = new StringSource("test1",test_1);
   List<JcompSource> srcs = Collections.singletonList(s1);
   JcompProject proj = jcomp_control.getProject(srcs);
   int ct = showMessages("test1",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest2()
{
   StringSource s2 = new StringSource("test2",test_2);
   List<JcompSource> srcs = Collections.singletonList(s2);
   JcompProject proj = jcomp_control.getProject("/pro/ivy/jcomp/src/test.jar",srcs);
   int ct = showMessages("test2",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest3()
{
   StringSource s3 = new StringSource("test3",test_3);
   List<JcompSource> srcs = Collections.singletonList(s3);
   JcompProject proj = jcomp_control.getProject(srcs);
   int ct = showMessages("test3",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest4()
{
   StringSource s4 = new StringSource("test4",test_4);
   List<JcompSource> srcs = Collections.singletonList(s4);
   JcompProject proj = jcomp_control.getProject(srcs);
   int ct = showMessages("test4",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest5()
{
   StringSource s5 = new StringSource("test5",test_5);
   List<JcompSource> srcs = Collections.singletonList(s5);
   JcompProject proj = jcomp_control.getProject(srcs);
   int ct = showMessages("test5",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest6() throws Exception
{
   File f6 = new File("/pro/ivy/jcomp/src/test6");
   String cnts = IvyFile.loadFile(f6);
   StringSource s6 = new StringSource("test6",cnts);
   List<JcompSource> srcs = Collections.singletonList(s6);
   JcompProject proj = jcomp_control.getProject(srcs);
   int ct = showMessages("test6",proj);
   Assert.assertEquals(73,ct);
}




@Test
public void jcompTest7() throws Exception
{
   File f7 = new File("/pro/ivy/jcomp/src/test7");
   String cnts = IvyFile.loadFile(f7);
   StringSource s7 = new StringSource("test7",cnts);
   List<JcompSource> srcs = Collections.singletonList(s7);
   String jar = "/pro/ivy/jcomp/src/test7.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test7",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest8() throws Exception
{
   File f8 = new File("/pro/ivy/jcomp/src/test8");
   String cnts = IvyFile.loadFile(f8);
   StringSource s8 = new StringSource("test8",cnts);
   List<JcompSource> srcs = Collections.singletonList(s8);
   String jar = "/pro/ivy/jcomp/src/test8.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test8",proj);
   Assert.assertEquals(ct,0);
}



@Test
public void jcompTest9() throws Exception
{
   File f9 = new File("/pro/ivy/jcomp/src/test9");
   String cnts = IvyFile.loadFile(f9);
   StringSource s9 = new StringSource("test9",cnts);
   List<JcompSource> srcs = Collections.singletonList(s9);
   String jar = "/pro/ivy/jcomp/src/test9.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test9",proj);
   Assert.assertEquals(ct,0);
}



@Test
public void jcompTest10() throws Exception
{
   File f10 = new File("/pro/ivy/jcomp/src/test10");
   String cnts = IvyFile.loadFile(f10);
   StringSource s10 = new StringSource("test10",cnts);
   List<JcompSource> srcs = Collections.singletonList(s10);
   String jar = "/pro/ivy/jcomp/src/test10.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test10",proj);
   Assert.assertEquals(ct,0);
}



@Test
public void jcompTest11() throws Exception
{
   File f11 = new File("/pro/ivy/jcomp/src/test11");
   String cnts = IvyFile.loadFile(f11);
   StringSource s11 = new StringSource("test11",cnts);
   List<JcompSource> srcs = Collections.singletonList(s11);
   String jar = "/pro/ivy/jcomp/src/test11.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test11",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest12() throws Exception
{
   File f12 = new File("/pro/ivy/jcomp/src/test12");
   String cnts = IvyFile.loadFile(f12);
   StringSource s12 = new StringSource("test12",cnts);
   List<JcompSource> srcs = Collections.singletonList(s12);
   String jar = "/pro/ivy/jcomp/src/test12.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test12",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest13() throws Exception
{
   File f13 = new File("/pro/ivy/jcomp/src/test13");
   String cnts = IvyFile.loadFile(f13);
   StringSource s13 = new StringSource("test13",cnts);
   List<JcompSource> srcs = Collections.singletonList(s13);
   String jar = "/pro/ivy/jcomp/src/test13.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test13",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest14() throws Exception
{
   File f14 = new File("/pro/ivy/jcomp/src/test14");
   String cnts = IvyFile.loadFile(f14);
   StringSource s14 = new StringSource("test14",cnts);
   List<JcompSource> srcs = Collections.singletonList(s14);
   String jar = "/pro/ivy/jcomp/src/test14.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test14",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest15() throws Exception
{
   File f15 = new File("/pro/ivy/jcomp/src/test15");
   String cnts = IvyFile.loadFile(f15);
   StringSource s15 = new StringSource("test15",cnts);
   List<JcompSource> srcs = Collections.singletonList(s15);
   String jar = "/pro/ivy/jcomp/src/test15.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test15",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest16() throws Exception
{
   File f16 = new File("/pro/ivy/jcomp/src/test16");
   String cnts = IvyFile.loadFile(f16);
   StringSource s16 = new StringSource("test16",cnts);
   List<JcompSource> srcs = Collections.singletonList(s16);
   String jar = "/pro/ivy/jcomp/src/test16.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test16",proj);
   Assert.assertEquals(0,ct);
}


@Test
public void jcompTest17() throws Exception
{
   File f15 = new File("/pro/ivy/jcomp/src/test15");
   String cnts = IvyFile.loadFile(f15);
   StringSource s15 = new StringSource("test15",cnts);
   List<JcompSource> srcs = Collections.singletonList(s15);
   String jar = "/pro/ivy/jcomp/src/test15.jar";
   JcodeFactory jf = new JcodeFactory();
   jf.addToClassPath(jar);
   JcompProject proj = jcomp_control.getProject(jf,srcs);
   int ct = showMessages("test17",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest20() throws Exception
{
   File f20 = new File("/pro/ivy/jcomp/src/test20");
   String cnts = IvyFile.loadFile(f20);
   StringSource s20 = new StringSource("test20",cnts);
   List<JcompSource> srcs = Collections.singletonList(s20);
   String jar = "/pro/ivy/jcomp/src/test20.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test20",proj);
   Assert.assertEquals(0,ct);
}


@Test
public void jcompTest21() throws Exception
{
   File f21 = new File("/pro/ivy/jcomp/src/test21");
   String cnts = IvyFile.loadFile(f21);
   StringSource s21 = new StringSource("test21",cnts);
   List<JcompSource> srcs = Collections.singletonList(s21);
   String jar = "/pro/ivy/jcomp/src/test21.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test21",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest22() throws Exception
{
   File f22 = new File("/pro/ivy/jcomp/src/test22");
   String cnts = IvyFile.loadFile(f22);
   StringSource s22 = new StringSource("test22",cnts);
   List<JcompSource> srcs = Collections.singletonList(s22);
   String jar = "/pro/ivy/jcomp/src/test22.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test22",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest23() throws Exception
{
   File f23 = new File("/pro/ivy/jcomp/src/test23");
   String cnts = IvyFile.loadFile(f23);
   StringSource s23 = new StringSource("test23",cnts);
   List<JcompSource> srcs = Collections.singletonList(s23);
   String jar = "/pro/ivy/jcomp/src/test23.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test23",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest24() throws Exception
{
   File f24 = new File("/pro/ivy/jcomp/src/test24");
   String cnts = IvyFile.loadFile(f24);
   StringSource s24 = new StringSource("test24",cnts);
   List<JcompSource> srcs = Collections.singletonList(s24);
   String jar = "/pro/ivy/jcomp/src/test24.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test24",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest25() throws Exception
{
   File f25 = new File("/pro/ivy/jcomp/src/test25");
   String cnts = IvyFile.loadFile(f25);
   StringSource s25 = new StringSource("test25",cnts);
   List<JcompSource> srcs = Collections.singletonList(s25);
   String jar = "/pro/ivy/jcomp/src/test25.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test25",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest26() throws Exception
{
   File f26 = new File("/pro/ivy/jcomp/src/test26");
   String cnts = IvyFile.loadFile(f26);
   StringSource s26 = new StringSource("test26",cnts);
   List<JcompSource> srcs = Collections.singletonList(s26);
   String jar = "/pro/ivy/jcomp/src/test26.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test26",proj);
   Assert.assertEquals(0,ct);
}




@Test
public void jcompTest27() throws Exception
{
   File f27 = new File("/pro/ivy/jcomp/src/test27");
   String cnts = IvyFile.loadFile(f27);
   StringSource s27 = new StringSource("test27",cnts);
   List<JcompSource> srcs = Collections.singletonList(s27);
   String jar = "/pro/ivy/jcomp/src/test27.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test27",proj);
   Assert.assertEquals(0,ct);
}




@Test
public void jcompTest28() throws Exception
{
   File f28 = new File("/pro/ivy/jcomp/src/test28");
   String cnts = IvyFile.loadFile(f28);
   StringSource s28 = new StringSource("test28",cnts);
   List<JcompSource> srcs = Collections.singletonList(s28);
   String jar = "/pro/ivy/jcomp/src/test28.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test28",proj);
   Assert.assertEquals(8,ct);
}



@Test
public void jcompTest29() throws Exception
{
   File f29 = new File("/pro/ivy/jcomp/src/test29");
   File f29a = new File("/pro/ivy/jcomp/src/test29a");

   String cnts = IvyFile.loadFile(f29);
   String cntsa = IvyFile.loadFile(f29a);

   StringSource s29 = new StringSource("test29",cnts);
   StringSource s29a = new StringSource("test29a",cntsa);

   List<JcompSource> srcs = new ArrayList<>();
   srcs.add(s29);
   srcs.add(s29a);
   String jar = "/pro/ivy/jcomp/src/test29.jar";
   JcompProject proj = jcomp_control.getProject(jar,srcs);
   int ct = showMessages("test29",proj);
   Assert.assertEquals(0,ct);
}



@Test
public void jcompTest30() throws Exception
{
   File f30 = new File("/pro/ivy/jcomp/src/test30");
   String cnts = IvyFile.loadFile(f30);
   StringSource s30 = new StringSource("test30",cnts);
   List<JcompSource> srcs = new ArrayList<>();
   srcs.add(s30);
   JcompProject proj = jcomp_control.getProject(srcs);
   int ct = showMessages("test30",proj);
   Assert.assertEquals(0,ct);
}


@Test
public void jcompTest31() throws Exception
{
   File f31 = new File("/pro/ivy/jcomp/src/test31");
   String cnts = IvyFile.loadFile(f31);
   StringSource s31 = new StringSource("test31",cnts);
   List<JcompSource> srcs = new ArrayList<>();
   srcs.add(s31);
   JcompProject proj = jcomp_control.getProject(srcs);
   int ct = showMessages("test31",proj);
   Assert.assertEquals(0,ct);
}


@Test
public void jcompTest32() throws Exception
{
   File f32 = new File("/pro/ivy/jcomp/src/test32");
   String cnts = IvyFile.loadFile(f32);
   StringSource s32 = new StringSource("test32",cnts);
   List<JcompSource> srcs = new ArrayList<>();
   srcs.add(s32);
   JcompProject proj = jcomp_control.getProject(srcs);
   int ct = showMessages("test32",proj);
   Assert.assertEquals(0,ct);
}




/********************************************************************************/
/*										*/
/*	Source from a string							*/
/*										*/
/********************************************************************************/

private static class StringSource implements JcompSource {

   private String base_name;
   private String base_string;

   StringSource(String nm,String s) {
      base_name = nm;
      base_string = s;
    }

   @Override public String getFileContents()		{ return base_string; }

   @Override public String getFileName()		{ return base_name; }

}	// end of inner class String Source




}	// end of class  JcompTest




/* end of JcompTest.java */
