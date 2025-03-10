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


package edu.brown.cs.ivy.jflow.flow;

import edu.brown.cs.ivy.jflow.JflowConstants;
import edu.brown.cs.ivy.jflow.JflowModelSource;
import edu.brown.cs.ivy.jflow.JflowSource;
import edu.brown.cs.ivy.jflow.JflowValue;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Method;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;




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

public SourceBase mutateTo(BT_Class type)			{ return null; }

ValueBase getArrayValues(ValueBase idx)			{ return null; }

@Override public BT_Field getFieldSource()			{ return null; }

void setArrayContents(ValueBase cv)				{ }
void setFieldContents(ValueBase cv,BT_Field fld)		{ }

boolean addToArrayContents(ValueBase cv,ValueBase i)		{ return false; }
boolean addToFieldContents(ValueBase cv,BT_Field fld) 	        { return false; }
boolean markArrayAccess(AccessSafety as)			{ return false; }

ValueBase getFieldValue(BT_Field fld)				{ return null; }

boolean isArray()						{ return false; }
ValueBase getArrayLength()					{ return null; }

@Override public JflowModelSource getModelSource()		{ return null; }
@Override public boolean isModel()		                { return getModelSource() != null; }

public boolean isPrototype()					{ return false; }
public ValueBase handlePrototypeCall(BT_Method bm,List<?> args) { return null; }
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
