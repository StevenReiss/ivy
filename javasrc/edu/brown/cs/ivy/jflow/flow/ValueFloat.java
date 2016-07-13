/********************************************************************************/
/*										*/
/*		ValueFloat.java 						*/
/*										*/
/*	Representation of an real value for static checking			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/ValueFloat.java,v 1.4 2015/11/20 15:09:14 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ValueFloat.java,v $
 * Revision 1.4  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.3  2007-05-10 01:48:12  spr
 * Start adding support for local (micro) typing.
 *
 * Revision 1.2  2006/07/10 14:52:18  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:35  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.flow;

import com.ibm.jikesbt.BT_Class;




class ValueFloat extends ValueNumber
{



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ValueFloat(FlowMaster jm,BT_Class dt)
{
   super(jm,dt);
}



ValueFloat(FlowMaster jm,BT_Class dt,LocalType lt,ValueFloat vf)
{
   super(jm,dt,lt,vf);
}



/********************************************************************************/
/*										*/
/*	Methods for handling merging values					*/
/*										*/
/********************************************************************************/

@Override public ValueBase mergeValue(ValueBase cv)
{
   if (cv == this || cv == null) return this;

   if (!(cv instanceof ValueFloat)) return jflow_master.badValue();

   ValueFloat cvf = (ValueFloat) cv;

   if (data_type == BT_Class.getDouble()) return this;

   if (cvf.data_type == BT_Class.getDouble()) return cvf;

   return this;
}



@Override public ValueBase newSourcedValue(SourceBase cs)
{
   ValueFloat cv = new ValueFloat(jflow_master,data_type);
   cv.setSourceSet(jflow_master.createSingletonSet(cs));
   return cv;
}



/********************************************************************************/
/*										*/
/*	Methods for handling LocalTypes 					*/
/*										*/
/********************************************************************************/

@Override protected ValueNumber createLocalType(LocalType lt)
{
   return new ValueFloat(jflow_master,data_type,lt,this);
}




}	// end of class ValueFloat




/* end of ValueFloat.java */

