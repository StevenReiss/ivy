/********************************************************************************/
/*										*/
/*		StateCtor.java							*/
/*										*/
/*	Class to record what constructors are currently active for a state	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/StateCtor.java,v 1.5 2017/12/20 20:36:49 spr Exp $ */


/*********************************************************************************
 *
 * $Log: StateCtor.java,v $
 * Revision 1.5  2017/12/20 20:36:49  spr
 * Formatting
 *
 * Revision 1.4  2009-06-04 18:50:27  spr
 * Handle special cased methods correctly.
 *
 * Revision 1.3  2006-08-30 00:43:51  spr
 * Fix bugs with mutable sources.
 *
 * Revision 1.2  2006/07/10 14:52:18  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/21 02:18:34  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.flow;


import edu.brown.cs.ivy.jflow.JflowConstants;

import com.ibm.jikesbt.BT_Class;

import java.util.*;



public class StateCtor implements JflowConstants
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Set<BT_Class>	current_ctors;
private Set<StateCtor>	using_sets;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

StateCtor(BT_Class bc,StateCtor prv)
{
   current_ctors = new HashSet<BT_Class>(4);
   if (bc != null) current_ctors.add(bc);
   using_sets = new HashSet<StateCtor>(4);
   if (prv != null) using_sets.add(prv);
}



/********************************************************************************/
/*										*/
/*	Basic operations on constructor sets					*/
/*										*/
/********************************************************************************/

StateCtor startCtor(BT_Class bc)
{
   if (contains(bc)) return this;

   return new StateCtor(bc,this);
}



StateCtor finishCtor(BT_Class _bc)
{
   // Having extra items in the list doesn't hurt (never checked
   // once the constructor has actually returned).
   // Moreover, removing an item would require creating a new set
   // thus using more memory.  Hence we just ignore the
   // finish request.

   return this;
}


boolean testInCtor(BT_Class bc)
{
   boolean fg = contains(bc);

   return fg;
}


boolean mergeWith(StateCtor sc)
{
   if (sc == this || sc == null) return false;
   if (using_sets.contains(sc)) return false;

   using_sets.add(sc);

   return true;
}



/********************************************************************************/
/*										*/
/*	Methods to test containment and retain change links			*/
/*										*/
/********************************************************************************/

private boolean contains(BT_Class bc)
{
   if (current_ctors.contains(bc)) return true;
   if (!complete()) return false;
   if (current_ctors.contains(bc)) return true;
   return false;
}



private boolean complete()
{
   boolean chng = false;
   Set<StateCtor> done = new HashSet<StateCtor>();

   for (StateCtor sc : using_sets) {
      chng |= addAll(sc,done);
    }

   return chng;
}



private boolean addAll(StateCtor sc,Set<StateCtor> done)
{
   if (done.contains(sc)) return false;
   done.add(sc);

   boolean chng = current_ctors.addAll(sc.current_ctors);

   for (StateCtor nsc : sc.using_sets) {
      chng |= addAll(nsc,done);
    }

   // TODO: This is where we spend a lot of our time.  It needs to be fixed to avoid
   // recursion and be a lot faster.  Possibly use union-find approach

   return chng;
}




/********************************************************************************/
/*										*/
/*	Iterator over classes in this set					*/
/*										*/
/********************************************************************************/

Iterator<BT_Class> getClasses()
{
   complete();

   return current_ctors.iterator();
}



}	// end of class StateCtor




/* end of StateCtor.java */
