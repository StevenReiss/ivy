/********************************************************************************/
/*										*/
/*		JcodeField.java 						*/
/*										*/
/*	description of class							*/
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

import org.objectweb.asm.tree.*;
import java.lang.reflect.*;


public class JcodeField extends FieldNode implements JcodeConstants
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcodeClass	in_class;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcodeField(JcodeClass cls,int a,String n,String d,String s,Object val)
{
   super(ASM5,a,n,d,s,val);

   in_class = cls;
}



/********************************************************************************/
/*										*/
/*	Access methods							       */
/*										*/
/********************************************************************************/

public String getName() 	        { return name; }
public String getFullName()             { return in_class.getName() + "." + getName(); }
public Object getConstantValue()        { return value; }

public JcodeDataType getType() {
   return in_class.getFactory().findJavaType(desc);
}

public JcodeDataType getDeclaringClass() {
   return in_class.getDataType();
}

public boolean isVolatile()
{
   if (Modifier.isVolatile(this.access)) return true;
   return false;
}

public boolean isStatic()
{
   if (Modifier.isStatic(this.access)) return true;
   return false;
}


public boolean isFinal()
{
   return Modifier.isFinal(this.access);
}



/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return getDeclaringClass().getName() + "." + name;
}




}	// end of class JcodeField




/* end of JcodeField.java */

