/********************************************************************************/
/*										*/
/*		FlowCleanup.java						*/
/*										*/
/*	Class to handle cleanup and analysis of flow data			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/FlowCleanup.java,v 1.4 2018/08/02 15:10:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: FlowCleanup.java,v $
 * Revision 1.4  2018/08/02 15:10:17  spr
 * Fix imports.
 *
 * Revision 1.3  2007-02-27 18:53:29  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.2  2006/07/10 14:52:17  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:34  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.flow;

import edu.brown.cs.ivy.jflow.JflowConstants;

import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_Opcodes;

import java.util.HashMap;
import java.util.Map;



class FlowCleanup implements JflowConstants, BT_Opcodes
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FlowMaster jflow_master;
private FlowControl	flow_control;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

FlowCleanup(FlowMaster jm,FlowControl fc)
{
   jflow_master = jm;
   flow_control = fc;
}




/********************************************************************************/
/*										*/
/*	Top level checking method						*/
/*										*/
/********************************************************************************/

void checkMethod(BT_Method bm)
{
   if (bm.isNative() || bm.isAbstract()) return;

   checkCode(bm);
}




/********************************************************************************/
/*										*/
/*	Check at the code level for null, not executed, etc.			*/
/*										*/
/********************************************************************************/

private void checkCode(BT_Method bm)
{
   Map<BT_Ins,Boolean> execset = new HashMap<BT_Ins,Boolean>();

   for (MethodBase fm : jflow_master.getAllMethods(bm)) {
      fm.clearIgnoreBlocks();
      FlowQueue wq = flow_control.getFlowQueue(fm);
      if (wq != null) {
	 doCodeChecks(wq,execset);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Helper methods for doing code checks					*/
/*										*/
/********************************************************************************/

private void doCodeChecks(FlowQueue wq,Map<BT_Ins,Boolean> execset)
{
   MethodBase method = wq.getMethod();
   StateBase st1;

   for (int ino = 0; ino < wq.getNumInstructions(); ++ino) {
      BT_Ins ins = wq.getInstruction(ino);
      st1 = wq.getState(ins);

      switch (ins.opcode) { 
         case OPC_BBLOCK :
	    Boolean bfg = execset.get(ins);
	    if (bfg == null || !bfg.booleanValue()) bfg = Boolean.valueOf(st1 != null);
	    execset.put(ins,bfg);
	    if (st1 == null)
		method.addIgnoreBlock(ins);
	    break;
	 default :
	    break;
       }
    }
}




}	// end of class FlowCleanup




/* end of FlowCleanup.java */
