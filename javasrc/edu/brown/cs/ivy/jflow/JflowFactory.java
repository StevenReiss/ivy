/********************************************************************************/
/*										*/
/*		JflowFactory.java						*/
/*										*/
/*	Factory class for creating a Java flow analyzer 			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/JflowFactory.java,v 1.5 2017/12/20 20:36:41 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JflowFactory.java,v $
 * Revision 1.5  2017/12/20 20:36:41  spr
 * Formatting
 *
 * Revision 1.4  2010-02-12 00:33:07  spr
 * Headers changed.
 *
 * Revision 1.3  2006/07/10 14:52:15  spr
 * Code cleanup.
 *
 * Revision 1.2  2006/07/03 18:15:11  spr
 * Update flow with inlining options.  Clean up.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow;



public class JflowFactory implements JflowConstants
{


/********************************************************************************/
/*										*/
/*	Factory method for creating a Jflow Flow Control master 		*/
/*										*/
/********************************************************************************/

public static JflowMaster createFlowMaster(JflowControl jc)
{
   JflowMaster jm = new edu.brown.cs.ivy.jflow.flow.FlowMaster(jc);

   return jm;
}



/********************************************************************************/
/*										*/
/*	Factory method for creating a Jflow Model Control Master		*/
/*										*/
/********************************************************************************/

public static JflowModel createModelMaster(JflowMaster jm,JflowControl jc)
{
   JflowModel mm = new edu.brown.cs.ivy.jflow.model.ModelMaster(jm,jc);

   return mm;
}



}	// end of class JflowFactory



/* end of JflowFactory.java */
