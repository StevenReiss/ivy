/********************************************************************************/
/*										*/
/*		SourceBase.java 						*/
/*										*/
/*	Base class for source point for static checking of automata		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/SourceBase.java,v 1.8 2015/11/20 15:09:14 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SourceBase.java,v $
 * Revision 1.8  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.7  2015/02/14 18:46:05  spr
 * Synchronize id counter.
 *
 * Revision 1.6  2007-08-10 02:10:39  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.5  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.4  2007-02-27 18:53:29  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.3  2007-01-03 03:24:18  spr
 * Modifications to handle incremental update.
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

import edu.brown.cs.ivy.jflow.*;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.*;

import java.util.*;




abstract class SourceBase implements JflowSource, JflowConstants
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

protected FlowMaster jflow_master;

private int	source_id;
private boolean used_inlock;

private static int source_counter = 0;
private static Vector<SourceBase> source_items = new Vector<SourceBase>();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected SourceBase(FlowMaster jm)
{
   jflow_master = jm;
   synchronized (source_items) {
      source_id = source_counter++;
      source_items.add(this);
    }
   used_inlock = false;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public int getId()						{ return source_id; }

public static SourceBase getSource(int id)
{
   if (id < 0) return null;

   return source_items.elementAt(id);
}



/********************************************************************************/
/*										*/
/*	Data access methods							*/
/*										*/
/********************************************************************************/

@Override public BT_Class getDataType()					{ return null; }

public boolean isNative()					{ return false; }

public SourceBase mutateTo(BT_Class _type)			{ return null; }

ValueBase getArrayValues(ValueBase _idx)			{ return null; }

@Override public BT_Field getFieldSource()				{ return null; }

void setArrayContents(ValueBase _cv)				{ }
void setFieldContents(ValueBase _cv,BT_Field _fld)		{ }

boolean addToArrayContents(ValueBase _cv,ValueBase _i)		{ return false; }
boolean addToFieldContents(ValueBase _cv,BT_Field _fld) 	{ return false; }
boolean markArrayAccess(AccessSafety as)			{ return false; }

ValueBase getFieldValue(BT_Field _fld)				{ return null; }

boolean isArray()						{ return false; }
ValueBase getArrayLength()					{ return null; }

@Override public JflowModelSource getModelSource()			{ return null; }
@Override public boolean isModel()		{ return getModelSource() != null; }

public boolean isPrototype()					{ return false; }
public ValueBase handlePrototypeCall(BT_Method _bm,List<?> _args)	{ return null; }
public ProtoBase getPrototype() 				{ return null; }

public String getStringValue()					{ return null; }

@Override public boolean isUsedInLock()					{ return used_inlock; }
void setUsedInLock()						{ used_inlock = true; }

MethodBase getSourceMethod()					{ return null; }
int getSourceInstruction()					{ return -1; }

boolean isUnique()						{ return false; }
SourceBase makeNonunique()					{ return this; }
SourceBase getNonunique()					{ return null; }




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public abstract void outputXml(IvyXmlWriter xw,String id);



/********************************************************************************/
/*										*/
/*	Methods for incremental update						*/
/*										*/
/********************************************************************************/

void handleUpdates(Collection<SourceBase> oldsrcs,
		      Map<SourceSet,SourceSet> srcupdates,
		      Map<JflowValue,JflowValue> valupdates)
{
}



/********************************************************************************/
/*										*/
/*	Common helper methods							*/
/*										*/
/********************************************************************************/

protected BT_Class findChildForInterface(BT_Class dt,BT_Class it)
{
   BT_Class r0 = null;
   for (Enumeration<?> e = dt.getKids().elements(); e.hasMoreElements(); ) {
      BT_Class k = (BT_Class) e.nextElement();
      BT_Class r = findChildForInterface(k,it);
      if (r == null && !k.isAbstract() && k.isDerivedFrom(it)) r = k;
      if (r != null) {
	 if (r0 == null) r0 = r;
	 else return it;
       }
    }
   return r0;
}




}	// end of abstract class SourceBase




/* end of SourceBase.java */
