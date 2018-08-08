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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/JflowControl.java,v 1.8 2018/08/02 15:10:13 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JflowControl.java,v $
 * Revision 1.8  2018/08/02 15:10:13  spr
 * Fix imports.
 *
 * Revision 1.7  2017/02/15 02:09:08  spr
 * Formatting
 *
 * Revision 1.6  2009-10-02 00:18:25  spr
 * Import clean up.
 *
 * Revision 1.5  2009-09-27 23:56:43  spr
 * Updates for mac
 *
 * Revision 1.4  2007-08-10 02:10:32  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.3  2007-05-04 01:59:52  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.2  2007-02-27 18:53:26  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.1  2006/07/10 14:52:15  spr
 * Code cleanup.
 *
 *
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

public JflowModelSource createModelSource(JflowMethod jm,int ino,BT_Ins ins,JflowValue base);



/********************************************************************************/
/*										*/
/*	Method data creation methods						*/
/*										*/
/********************************************************************************/

public JflowMethodData createMethodData(JflowMethod jm);



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
