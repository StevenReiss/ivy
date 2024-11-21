/********************************************************************************/
/*										*/
/*		ValueNumber.java						*/
/*										*/
/*	Common code for all numeric values					*/
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

import com.ibm.jikesbt.BT_Class;

import java.util.HashMap;
import java.util.Map;


abstract class ValueNumber extends ValueBase
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected LocalType	local_type;
private Map<LocalType,ValueNumber> local_map;
private ValueNumber	nonlocal_type;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ValueNumber(FlowMaster jm,BT_Class dt)
{
   super(jm,dt);

   local_type = null;
   local_map = null;
   nonlocal_type = this;
}



ValueNumber(FlowMaster jm,BT_Class dt,LocalType lt,ValueNumber base)
{
   super(jm,dt);

   local_type = lt;
   local_map = null;
   nonlocal_type = base;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public boolean isCategory2()
{
   return data_type.getSizeForLocal() == 2;
}


/********************************************************************************/
/*										*/
/*	Method for handling local type						*/
/*										*/
/********************************************************************************/

@Override public ValueBase setLocalType(LocalType lt)
{
   if (lt == local_type) return this;

   if (local_type != null) {
      if (lt == null) return nonlocal_type;
      return nonlocal_type.setLocalType(lt);
    }

   if (local_map == null) {
      local_map = new HashMap<LocalType,ValueNumber>(4);
    }
   ValueNumber r = local_map.get(lt);
   if (r != null) return r;
   r = createLocalType(lt);
   local_map.put(lt,r);

   return r;
}


protected abstract ValueNumber createLocalType(LocalType lt);





}	// end of class ValueNumber



/* end of ValueNumber.java */
