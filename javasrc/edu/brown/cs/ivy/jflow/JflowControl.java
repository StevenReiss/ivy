/********************************************************************************/
/*										*/
/*		JflowControl.java						*/
/*										*/
/*	Control class for handling flow control from the user's point of view   */
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



import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_Method;

import java.util.Collection;
import java.util.List;



public interface JflowControl extends JflowConstants
{



/********************************************************************************/
/*										*/
/*	Source creation methods 						*/
/*										*/
/********************************************************************************/

JflowModelSource createModelSource(JflowMethod jm,int ino,BT_Ins ins,JflowValue base);



/********************************************************************************/
/*										*/
/*	Method data creation methods						*/
/*										*/
/********************************************************************************/

JflowMethodData createMethodData(JflowMethod jm);



/********************************************************************************/
/*										*/
/*	Model building methods							*/
/*										*/
/********************************************************************************/

boolean checkUseMethod(JflowMethod m);

JflowEvent findEvent(JflowModel jm,JflowMethod m,BT_Ins ins,boolean start,List<Object> vals);

Collection<JflowEvent> getRequiredEvents();

boolean isFieldTracked(BT_Field fld);

boolean checkUseCall(JflowMethod from,BT_Method to);


}	// end of class JflowControl




/* end of JflowControl.java */
