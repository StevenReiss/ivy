/********************************************************************************/
/*										*/
/*		JflowMaster.java						*/
/*										*/
/*	Master control interface for Java Flow Analyzer 			*/
/*										*/
/*	Use FAIT instead of JFLOW.  This is being retained for now only 	*/
/*	to support existing calls in wadi.					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/JflowMaster.java,v 1.8 2015/07/02 19:01:30 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JflowMaster.java,v $
 * Revision 1.8  2015/07/02 19:01:30  spr
 * Minor bug fixes
 *
 * Revision 1.7  2007-08-10 02:10:32  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.6  2007-02-27 18:53:26  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.5  2007-01-03 14:04:57  spr
 * Fix imports
 *
 * Revision 1.4  2007-01-03 03:24:15  spr
 * Modifications to handle incremental update.
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



import com.ibm.jikesbt.*;

import java.io.File;
import java.util.Collection;



public interface JflowMaster extends JflowConstants
{


/********************************************************************************/
/*										*/
/*	Methods for setting up for flow analysis				*/
/*										*/
/********************************************************************************/

/**
 *	Set the class path so that the flow analyzer can find classes.
 *
 **/

public void setClassPath(String classpath);

public void addToClassPath(String classorjar);



/**
 *	Add a class to the set to be analyzed.	This will also implicitly add
 *	all classes that this class depends upon (e.g. calls, inherits, or uses
 *	in any way) transitively.  It should be called with the starting class
 *	and any other class that might be used that cannot be found explicitly
 *	(e.g. classes loaded using Class.forName()). It can be called with all
 *	the classes to be analyzed as well.
 *
 **/

public void addClass(String cls);



/**
 *	Load XML file describing special cases.
 **/

public void addDescriptionFile(String xmlfile);

public void addDefaultDescriptionFile();




/**
 *	Set options for control flow
 **/

public void setOption(FlowOption opt,boolean fg);



/**
 *	Set the start class (otherwise all starts are considered).
 **/

public void setStartClass(String cls);



/********************************************************************************/
/*										*/
/*	Methods for handling project-specific queries				*/
/*										*/
/********************************************************************************/

/**
 *	Set the project filter for the flow analysis.  The project filter determines
 *	whether a class should be considered a system or a project class.  Project
 *	classes are analyzed in more detail than system classes.
 **/

public void setProjectFilter(ProjectFilter pf);



public interface ProjectFilter {

   boolean isProjectClass(String cls);


}	// end of subinterface ProjectFilter


public boolean isProjectClass(BT_Class bc);



/********************************************************************************/
/*										*/
/*	Methods for doing the flow analysis					*/
/*										*/
/********************************************************************************/

public void setupAnalysis();




/**
 *	This method does the flow analysis, returning when everything has been
 *	computed.  It can be called multiple times in which case it will do
 *	incremental analysis, only looking at those that were noted by the
 *	add new source methods and whatever they affect.  The factory is used
 *	to create appropriate instances of methods, sources, and values.
 *
 **/

public void analyze() throws JflowException;



public void cleanup();




/********************************************************************************/
/*										*/
/*	Incremental methods							*/
/*										*/
/********************************************************************************/

/**
 *	Note that a give file might have changed
 **/

public void noteChanged(File file);
public void noteChanged(BT_Method bm);

public void updateChanged() throws JflowException;




/********************************************************************************/
/*										*/
/*	Access methods returning sets of found methods				*/
/*										*/
/********************************************************************************/


/**
 *	Return the set of all classes
 **/

Iterable<BT_Class> getAllClasses();



/**
 *	Return the set of static initializers that were found
 **/

// Iterable<JflowMethod> getStaticInitializers();



/**
 *	Return the set of starting methods that were found
 **/

Collection<JflowMethod> getStartMethods();
boolean isStartMethod(JflowMethod jm);



/**
 *	Return the set of methods associated with a given callback.  Callbacks
 *	are defined in the XML definitions files and given an appropriate ID.
 **/

Iterable<JflowMethod> getCallbacks(String cbid);


/**
 *	Return the callback id for the given method.
 **/

String getCallbackStart(BT_Method bm);




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

boolean isMethodAccessible(BT_Method bm);

JflowMethod createMetaMethod(BT_Method bm);

Iterable<JflowMethod> getStaticInitializers();

boolean isMethodUsed(JflowMethod m);
boolean isInstructionUsed(JflowMethod jm,BT_Ins ins);

int findLineNumber(JflowMethod m,int ino);

Iterable<JflowMethod> findAllMethods(BT_Method bm);

void addFieldChecks(BT_Field f);

boolean canBeReplaced(BT_Method bm);

JflowSource findProgramReturnSource();
JflowSource findProgramThreadSource(String id);
JflowSource createBadSource();

JflowSource findFieldSource(BT_Field fld,JflowValue v);

JflowSource findModelSource(JflowModelSource ms);



}	// end of interface JflowMaster





/* end of JflowMaster.java */
