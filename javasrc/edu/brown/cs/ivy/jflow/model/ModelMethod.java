/********************************************************************************/
/*										*/
/*		ModelMethod.java						*/
/*										*/
/*	Subclass for holding event generation information for a method		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/model/ModelMethod.java,v 1.4 2015/11/20 15:09:15 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ModelMethod.java,v $
 * Revision 1.4  2015/11/20 15:09:15  spr
 * Reformatting.
 *
 * Revision 1.3  2007-05-04 02:00:03  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.2  2006/07/10 14:52:19  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:37  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.model;


import edu.brown.cs.ivy.jflow.JflowMethod;
import edu.brown.cs.ivy.jflow.JflowModel;

import java.util.HashSet;
import java.util.Iterator;



class ModelMethod implements JflowModel.Method
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private JflowMethod for_method;
private ModelState start_node;
private ModelState end_node;
private HashSet<JflowMethod> user_set;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelMethod(JflowMethod cm,ModelState s0,ModelState s1)
{
   for_method = cm;
   start_node = s0;
   end_node = s1;
   user_set = new HashSet<JflowMethod>();
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

ModelState getStartState()			{ return start_node; }
ModelState getEndState()			{ return end_node; }

@Override public JflowMethod getMethod()			{ return for_method; }
@Override public JflowModel.Node getStartNode()		{ return start_node; }
@Override public JflowModel.Node getEndNode()		{ return end_node; }

void addUser(JflowMethod cm)			{ user_set.add(cm); }
Iterator<JflowMethod> getUsers()		{ return user_set.iterator(); }




}	// end of ModelMethod




/* end of ModelMethod.java */
