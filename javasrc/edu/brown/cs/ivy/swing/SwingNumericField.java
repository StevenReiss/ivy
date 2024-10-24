/********************************************************************************/
/*										*/
/*		SwingNumericField.java						*/
/*										*/
/*	Swing generic numeric text field support code				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Redistribution and use in source and binary forms, with or without           *
 *  modification, are permitted provided that the following conditions are met:  *
 *                                                                               *
 *  + Redistributions of source code must retain the above copyright notice,     *
 *      this list of conditions and the following disclaimer.                    *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *      this list of conditions and the following disclaimer in the              *
 *      documentation and/or other materials provided with the distribution.     *
 *  + Neither the name of the Brown University nor the names of its              *
 *      contributors may be used to endorse or promote products derived from     *
 *      this software without specific prior written permission.                 *
 *                                                                               *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   *
 *  POSSIBILITY OF SUCH DAMAGE.                                                  *
 *                                                                               *
 ********************************************************************************/


package edu.brown.cs.ivy.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;




public class SwingNumericField extends SwingTextField implements FocusListener
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/


public SwingNumericField(int ln,int min,int max)
{
   this(min,ln,min,max);
}



public SwingNumericField(int val,int ln,int min,int max)
{
   super(new NumericDocument(min,max,val),Integer.toString(val),ln);

   addFocusListener(this);
}




public SwingNumericField(int ln,double min,double max)
{
   this(min,ln,min,max);
}



public SwingNumericField(double val,int ln,double min,double max)
{
   super(new NumericDocument(min,max,val),Double.toString(val),ln);

   addFocusListener(this);
}




/********************************************************************************/
/*										*/
/*	Value callbacks 							*/
/*										*/
/********************************************************************************/

public double getValue()
{
   String t = getText();
   try {
      return Double.parseDouble(t);
    }
   catch (NumberFormatException e) { }

   return 0;
}


public void setValue(double v)
{
   String s = Double.toString(v);

   setText(s);
}



public void setValue(int v)
{
   String s = Integer.toString(v);

   setText(s);
}



/********************************************************************************/
/*										*/
/*	Focus callback to generate action					*/
/*										*/
/********************************************************************************/

@Override public void focusGained(FocusEvent evt)
{ }



@Override public void focusLost(FocusEvent evt)
{
   NumericDocument nd = (NumericDocument) getDocument();
   nd.checkBounds();
   postActionEvent();
}



/********************************************************************************/
/*										*/
/*	Methods to ensure integer fields in range				*/
/*										*/
/********************************************************************************/


private static class NumericDocument extends PlainDocument {

   private double  min_value;
   private double  max_value;
   private double  dflt_value;
   private boolean int_only;
   private static final long serialVersionUID = 1;

   NumericDocument(int min,int max,int dflt) {
      min_value = min;
      max_value = max;
      dflt_value = dflt;
      int_only = true;
    }

   NumericDocument(double min,double max,double dflt) {
      min_value = min;
      max_value = max;
      dflt_value = dflt;
      int_only = false;
    }

   @Override public void insertString(int off,String str,AttributeSet a) throws BadLocationException {
      int ln = getLength();
      String num = getText(0,off) + str + getText(off,ln-off);

      if (!checkText(num)) {
	 Toolkit.getDefaultToolkit().beep();
       }
      else {
	 super.insertString(off,str,a);
       }
    }



   @Override public void remove(int off,int len) throws BadLocationException {
      int ln = getLength();
      String num = getText(0,off) + getText(off+len,ln-off-len);

      if (!checkText(num)) {
	 Toolkit.getDefaultToolkit().beep();
       }
      else {
	 super.remove(off,len);
       }
    }


   private boolean checkText(String txt) {
      boolean ok = true;

      if (txt.length() == 0) return true;

 /*************
      try {
	 Number n;
	 if (int_only) n = Integer.valueOf(txt);
	 else n = Double.valueOf(txt);
	 // do this at end to make editing easier
	 if (min_value < max_value) {
	    if (n.doubleValue() < min_value || n.doubleValue() > max_value) ok = false;
	  }
       }
      catch (NumberFormatException e) {
	 ok = false;
       }
*******************/

      return ok;
    }

   void checkBounds() {
      int ln = getLength();
      String txt = "0";
      try {
	 txt = getText(0,ln);
       }
      catch (BadLocationException e) { }
      Number n1 = null;
      try {
	 Number n;
	 if (int_only) n = Integer.valueOf(txt);
	 else n = Double.valueOf(txt);

	 if (min_value < max_value) {
	    if (n.doubleValue() < min_value) n1 = Double.valueOf(min_value);
	    else if (n.doubleValue() > max_value) n1 = Double.valueOf(max_value);
	  }
       }
      catch (NumberFormatException e) {
	 n1 = Double.valueOf(dflt_value);
       }
      if (n1 == null) return;
      String s;
      if (int_only) s = Integer.toString(n1.intValue());
      else s = Double.toString(n1.doubleValue());
      try {
	 replace(0,ln,s,null);
       }
      catch (BadLocationException e) { }
    }

}	// end of subclass NumericDocument




}	// end of SwingNumericField

