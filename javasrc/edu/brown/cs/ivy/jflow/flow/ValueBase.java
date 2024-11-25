/********************************************************************************/
/*										*/
/*		ValueBase.java							*/
/*										*/
/*	Generic representation of a value set for static checking		*/
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

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Opcodes;

import java.util.ArrayList;
import java.util.Collection;



abstract class ValueBase implements JflowValue, JflowConstants, BT_Opcodes
{



/********************************************************************************/
/*										*/
/*	Local storage								*/
/*										*/
/********************************************************************************/

protected FlowMaster	jflow_master;
protected BT_Class	data_type;
protected SourceSet	source_set;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected ValueBase(FlowMaster jm,BT_Class dt)
{
   jflow_master = jm;
   source_set = jflow_master.createEmptySet();
   data_type = dt;
}




/********************************************************************************/
/*										*/
/*	Abstract methods to be implemented on types				*/
/*										*/
/********************************************************************************/

public ValueBase restrictByClass(BT_Class cls,boolean pfg)	{ return this; }
public ValueBase removeByClass(BT_Class cls)			{ return this; }

public abstract ValueBase mergeValue(ValueBase cv);
@Override public boolean	 isCategory2()			{ return false; }
@Override public boolean	 canBeNull()			{ return false; }
@Override public boolean	 mustBeNull()			{ return false; }
@Override public boolean	nullExplicitlySet()		{ return false; }
@Override public short	 getFlags()				{ return 0; }
public TestBranch branchTest(ValueBase rhs,int op)		{ return TestBranch.ANY; }
public SourceSet getSourceSet() 				{ return source_set; }

public ValueBase forceNonNull() 				{ return this; }
public ValueBase allowNull()					{ return this; }
public ValueBase makeSubclass(BT_Class c)			{ return this; }
public ValueBase setTestNull()					{ return this; }

public ValueBase setLocalType(LocalType lt)			{ return this; }


public ValueBase getSourcedValue(SourceBase cs)
{
   if (cs == null || source_set.contains(cs)) return this;
   return newSourcedValue(cs);
}
public ValueBase newSourcedValue(SourceBase cs)		{ return this; }

public ValueBase performOperation(BT_Class t,ValueBase v,int o)
{
   return jflow_master.anyValue(t);
}


public Collection<String> getStringValues()			{ return null; }

ValueBase makeNonunique()					{ return this; }
ValueBase getNonunique()					{ return this; }
boolean isUnique()						{ return false; }

@Override public JflowValue merge(JflowValue v)
{
   return mergeValue((ValueBase) v);
}


ValueBase getArrayContents()					{ return null; }
boolean markArrayNonNull()					{ return false; }
boolean markArrayCanbeNull()					{ return false; }
boolean markArrayAccess(AccessSafety as)			{ return false; }

boolean markFieldAccess(AccessSafety as)			{ return false; }




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public BT_Class  getDataType()			{ return data_type; }

public Iterable<SourceBase> getSources()	{ return getSourceSet().getSources(); }

@Override public boolean isEmptySourceSet()		{ return getSourceSet().isEmpty(); }

public boolean isBad()				{ return false; }

public boolean goodSourceSet()			{ return true; }

public boolean isNative()			{ return false; }
public boolean allNative()			{ return false; }

@Override public Iterable<JflowSource> getSourceCollection()
{
   Collection<JflowSource> rslt = new ArrayList<JflowSource>();
   for (SourceBase sb : getSources()) rslt.add(sb);
   return rslt;
}
@Override public boolean hasFieldSource() 		{ return getSourceSet().hasFieldSource(); }

@Override public boolean containsSource(JflowSource s)
{
   return getSourceSet().contains((SourceBase) s);
}

@Override public boolean containsModelSource(JflowModelSource s)
{
   for (SourceBase sb : getSources()) {
      if (sb.getModelSource() == s) return true;
    }

   return false;
}



/********************************************************************************/
/*										*/
/*	Methods to obtain field values from a value				*/
/*										*/
/********************************************************************************/

@Override public Object getProgramValue() 		{ return null; }





/********************************************************************************/
/*										*/
/*	Subclass access methods 						*/
/*										*/
/********************************************************************************/

protected void setSourceSet(SourceSet cs)	{ source_set = cs; }



/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   StringBuffer rslt = new StringBuffer();
   rslt.append("[");
   rslt.append(data_type.getName());
   rslt.append("]");
   return rslt.toString();
}




}	// end of class ValueBase




/* end of ValueBase.java */

