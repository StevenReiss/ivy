/********************************************************************************/
/*										*/
/*		JflowConstants.java						*/
/*										*/
/*	Constants for use in the Java Flow Analyzer				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/JflowConstants.java,v 1.7 2013/09/24 01:06:55 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JflowConstants.java,v $
 * Revision 1.7  2013/09/24 01:06:55  spr
 * Minor fix
 *
 * Revision 1.6  2007-08-10 02:10:32  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.5  2007-05-04 01:59:52  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.4  2007-02-27 18:53:26  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.3  2006/07/10 14:52:15  spr
 * Code cleanup.
 *
 * Revision 1.2  2006/07/03 18:15:11  spr
 * Update flow with inlining options.  Clean up.
 *
 * Revision 1.1  2006/06/21 02:18:29  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow;


import edu.brown.cs.ivy.file.IvyFile;




public interface JflowConstants
{


/********************************************************************************/
/*										*/
/*	Flags									*/
/*										*/
/********************************************************************************/

enum FlowOption {
   DO_INLINING,
   DETAIL_FIELDS,
   DO_PROTOTYPES,
   DO_STRINGS,
   DO_UNDECLARED_EXCEPTIONS,
   DO_WAITS,
   DO_SYNCH,
   DO_AUTO_FIELDS,
   DO_DEBUG,
   DO_DEBUG_FLOW,
   DO_DEBUG_MODEL,
   DO_CHECK_DIRECT,
   DO_IGNORE_EXCEPTIONS
}



/********************************************************************************/
/*										*/
/*	Enumeration types							*/
/*										*/
/********************************************************************************/

enum TestBranch {
   ANY, ALWAYS, NEVER
}



enum ValueType {
   NULL, NON_NULL, NON_ZERO
}



enum AssociationType {
   NEW, THROW, CATCH, THISREF, FIELDSET, FIELDUSE, RETURN, ALLRETURN,
   THISARG, ARG1, ARG2, ARG3, ARG4, ARG5, ARG6, ARG7, ARG8, ARG9,
   SYNC
}



enum ConditionType {
   NONE, EQ, NE, LT, GE, GT, LE, NONNULL, NULL
}


enum ModelWaitType {
   NONE, WAIT, WAIT_TIMED, NOTIFY, NOTIFY_ALL, SYNCH, END_SYNCH
}


int JFLOW_START_LINE = -1;
int JFLOW_END_LINE = -2;


enum InlineType {
   NONE,				// don't inline
   DEFAULT,				// inline based on source set for this argument
   THIS,				// inline based on this argument value
   SOURCES,				// inline based on all source sets
   VALUES				// inline based on all values
}



enum AccessSafety {
   NONE,				// value use isn't known
   CHECKED,				// value is tested for null/non-null
   USED,				// value is used as if non-null
   CHECKED_USED;			// value is tested and used (on separate paths)

   public AccessSafety merge(AccessSafety as) {
      if (this == as || as == NONE) return this;
      if (this == NONE) return as;
      return CHECKED_USED;
    }

   public boolean isChecked() {
      return this == CHECKED || this == CHECKED_USED;
    }

   public boolean isUsed() {
      return this == USED || this == CHECKED_USED;
    }

}	// end of enum AccessSafety




/********************************************************************************/
/*										*/
/*	Settings								*/
/*										*/
/********************************************************************************/

int JFLOW_VALUE_MAX_RANGE = 3;
int JFLOW_MAX_THREADS = 4;
int JFLOW_VALUE_MAX_INCR = 2;

int opc_bblock = 186;



/********************************************************************************/
/*										*/
/*	Files									*/
/*										*/
/********************************************************************************/

String JFLOW_DEFAULT_DESCRIPTION_FILE = IvyFile.expandName("$(IVY)/lib/jflow.xml");
String JFLOW_DUMMY_JAR_FILE = IvyFile.expandName("$(IVY)/lib/jflowdummy.jar");



/********************************************************************************/
/*										*/
/*	Range subclass								*/
/*										*/
/********************************************************************************/

public class IntRange {

   private int from_value;
   private int to_value;

   public IntRange(int f,int t) {
      from_value = f;
      to_value = t;
    }

   public int getFrom() 		{ return from_value; }
   public int getTo()			{ return to_value; }

}	// end of subclass IntRange




}	// end of interface JflowConstants




/* end of JflowConstants.java */
