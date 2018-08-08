/********************************************************************************/
/*										*/
/*		ValueInt.java							*/
/*										*/
/*	Representation of an integer value for static checking			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/ValueInt.java,v 1.6 2018/08/02 15:10:18 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ValueInt.java,v $
 * Revision 1.6  2018/08/02 15:10:18  spr
 * Fix imports.
 *
 * Revision 1.5  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.4  2011-05-11 01:10:46  spr
 * Prototype entries had bad names.  Code cleansing.
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




class ValueInt extends ValueNumber
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private boolean      have_range;
private long	     min_value;
private long	     max_value;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ValueInt(FlowMaster jm,BT_Class dt)
{
   super(jm,dt);
   have_range = false;
   min_value = 0;
   max_value = 0;
}




private ValueInt(FlowMaster jm,BT_Class dt,LocalType lt,ValueInt vb)
{
   super(jm,dt,lt,vb);
   have_range = false;
   min_value = 0;
   max_value = 0;
}




ValueInt(FlowMaster jm,BT_Class dt,long mnv,long mxv)
{
   super(jm,dt);
   have_range = true;
   min_value = mnv;
   max_value = mxv;
}



private ValueInt(FlowMaster jm,BT_Class dt,long mnv,long mxv,LocalType lt,ValueInt vb)
{
   super(jm,dt,lt,vb);
   have_range = true;
   min_value = mnv;
   max_value = mxv;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

long	getMinValue()		 { return min_value; }
long	getMaxValue()		 { return max_value; }



/********************************************************************************/
/*										*/
/*	Methods for handling arithmetic 					*/
/*										*/
/********************************************************************************/

@Override public ValueBase performOperation(BT_Class typ,ValueBase rhsv,int op)
{
   if (rhsv == null) rhsv = this;

   if (!(rhsv instanceof ValueInt)) {
      System.err.println("JFLOW: Warning: integer operation performed on non-integer :: " +
			    toString() + " " + op + " " + rhsv.toString());
    }

   ValueInt rhs = (ValueInt) rhsv;

   boolean valok = true;
   long v0 = 0;
   long v1 = 0;
   if (min_value == max_value) v0 = min_value;
   else valok = false;
   if (rhs.min_value == rhs.max_value) v1 = rhs.min_value;
   else valok = false;

   boolean rngok = (have_range && rhs.have_range);
   if (!rngok) valok = false;
   long mnv = min_value;
   long mxv = max_value;

   switch (op) {
      case opc_iinc :
      case opc_iadd :
      case opc_ladd :
	 v0 += v1;
	 if (rngok) {
	    mnv += rhs.min_value;
	    mxv += rhs.max_value;
	  }
	 break;
      case opc_idiv :
      case opc_ldiv :
	 if (valok && v1 == 0) valok = false;
	 else if (valok) v0 /= v1;
	 if (rngok) {
	    if (rhs.min_value <= 0 && rhs.max_value >= 0) rngok = false;
	    else {
	       mnv = Math.min(mnv/rhs.max_value,mnv/rhs.min_value);
	       mxv = Math.max(mxv/rhs.max_value,mxv/rhs.min_value);
	     }
	  }
	 break;
      case opc_imul :
      case opc_lmul :
	 v0 *= v1;
	 if (rngok) {
	    mnv = min_value*rhs.min_value;
	    mnv = Math.min(mnv,min_value*rhs.max_value);
	    mnv = Math.min(mnv,max_value*rhs.min_value);
	    mnv = Math.min(mnv,max_value*rhs.max_value);
	    mxv = min_value*rhs.min_value;
	    mxv = Math.max(mxv,min_value*rhs.max_value);
	    mxv = Math.max(mxv,max_value*rhs.min_value);
	    mxv = Math.max(mxv,max_value*rhs.max_value);
	  }
	 break;
      case opc_irem :
      case opc_lrem :
	 if (v1 == 0) return jflow_master.anyValue(typ);
	 if (rngok) {
	    if (rhs.min_value <= mxv) rngok = false;
	  }
	 break;
      case opc_isub :
      case opc_lsub :
	 v0 -= v1;
	 if (rngok) {
	    mnv -= rhs.max_value;
	    mxv -= rhs.min_value;
	  }
	 break;
      case opc_iand :
      case opc_land :
	 v0 &= v1;
	 rngok = false;
	 break;
      case opc_ior :
      case opc_lor :
	 v0 |= v1;
	 rngok = false;
	 break;
      case opc_ixor :
      case opc_lxor :
	 v0 ^= v1;
	 rngok = false;
	 break;
      case opc_ishl :
      case opc_lshl :
	 v0 <<= v1;
	 rngok = false;
	 break;
      case opc_ishr :
      case opc_lshr :
	 v0 >>= v1;
	 rngok = false;
	 break;
      case opc_iushr :
      case opc_lushr :
	 v0 >>>= v1;
	 rngok = false;
	 break;
      case opc_int2byte :
      case opc_int2char :
      case opc_int2short :
      case opc_i2l :
      case opc_l2i :
	 break;
      case opc_ineg :
      case opc_lneg :
	 v0 = -v0;
	 mnv = -mxv;
	 mxv = -mnv;
	 break;
      case opc_i2d :
      case opc_l2d :
      default :
	 valok = false;
	 rngok = false;
	 break;
    }

   ValueBase rslt;

   if (valok) {
      rslt = jflow_master.rangeValue(typ,v0,v0);
    }
   else if (rngok) {
      rslt = jflow_master.rangeValue(typ,mnv,mxv);
    }
   else rslt = jflow_master.anyValue(typ);

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Methods to handle merging values					*/
/*										*/
/********************************************************************************/

@Override public ValueBase mergeValue(ValueBase cv)
{
   if (cv == this || cv == null) return this;

   if (!(cv instanceof ValueInt)) return jflow_master.badValue();

   ValueInt cvi = (ValueInt) cv;

   if (!have_range || (cvi.have_range && min_value <= cvi.min_value && max_value >= cvi.max_value)) {
      if (isBroaderType(data_type,cvi.data_type)) return this;
    }

   if (!cvi.have_range || (have_range && cvi.min_value <= min_value && cvi.max_value >= max_value)) {
      if (isBroaderType(cvi.data_type,data_type)) return cvi;
    }

   if (have_range && cvi.have_range && data_type == cvi.data_type) {
      return jflow_master.rangeValue(data_type,
					     Math.min(min_value,cvi.min_value),
					     Math.max(max_value,cvi.max_value));
    }

   BT_Class typ = data_type;
   if (!isBroaderType(data_type,cvi.data_type)) typ = cvi.data_type;

   return jflow_master.anyValue(typ);
}



@Override public ValueBase newSourcedValue(SourceBase cs)
{
   ValueInt nv = this;

   if (have_range) {
      nv = new ValueInt(jflow_master,data_type,min_value,max_value);
    }
   else {
      nv = new ValueInt(jflow_master,data_type);
    }

   nv.setSourceSet(jflow_master.createSingletonSet(cs));

   return nv;
}




/********************************************************************************/
/*										*/
/*	Methods to handle branch conditions					*/
/*										*/
/********************************************************************************/

@Override public TestBranch branchTest(ValueBase rhs,int op)
{
   if (rhs == null) rhs = this;

   ValueInt rvi = (ValueInt) rhs;

   TestBranch rslt = TestBranch.ANY;

   if (have_range && rvi.have_range) {
      switch (op) {
	 case opc_if_icmpeq :
	    if (min_value == max_value && min_value == rvi.min_value &&
		   max_value == rvi.max_value)
	       rslt = TestBranch.ALWAYS;
	    else if (min_value > rvi.max_value || max_value < rvi.min_value)
	       rslt = TestBranch.NEVER;
	    break;
	 case opc_if_icmpne :
	    if (min_value == max_value && min_value == rvi.min_value &&
		   max_value == rvi.max_value)
	       rslt = TestBranch.NEVER;
	    else if (min_value > rvi.max_value || max_value < rvi.min_value)
	       rslt = TestBranch.ALWAYS;
	    break;
	 case opc_if_icmplt :
	    if (max_value < rvi.min_value) rslt = TestBranch.ALWAYS;
	    else if (min_value >= rvi.max_value) rslt = TestBranch.NEVER;
	    break;
	 case opc_if_icmpge :
	    if (min_value >= rvi.max_value) rslt = TestBranch.ALWAYS;
	    else if (max_value < rvi.min_value) rslt = TestBranch.NEVER;
	    break;
	 case opc_if_icmpgt :
	    if (min_value > rvi.max_value) rslt = TestBranch.ALWAYS;
	    else if (max_value <= rvi.min_value) rslt = TestBranch.NEVER;
	    break;
	 case opc_if_icmple :
	    if (max_value <= rvi.min_value) rslt = TestBranch.ALWAYS;
	    else if (min_value > rvi.max_value) rslt = TestBranch.NEVER;
	    break;
	 case opc_ifeq :
	    if (min_value == max_value && min_value == 0) rslt = TestBranch.ALWAYS;
	    else if (min_value > 0 || max_value < 0) rslt = TestBranch.NEVER;
	    break;
	 case opc_ifne :
	    if (min_value == max_value && min_value == 0) rslt = TestBranch.NEVER;
	    else if (min_value > 0 || max_value < 0) rslt = TestBranch.ALWAYS;
	    break;
	 case opc_iflt :
	    if (max_value < 0) rslt = TestBranch.ALWAYS;
	    else if (min_value >= 0) rslt = TestBranch.NEVER;
	    break;
	 case opc_ifle :
	    if (max_value < 0) rslt = TestBranch.ALWAYS;
	    else if (min_value > 0) rslt = TestBranch.NEVER;
	    break;
	 case opc_ifgt :
	    if (min_value > 0) rslt = TestBranch.ALWAYS;
	    else if (max_value <= 0) rslt = TestBranch.NEVER;
	    break;
	 case opc_ifge :
	    if (min_value >= 0) rslt = TestBranch.ALWAYS;
	    else if (max_value < 0) rslt = TestBranch.NEVER;
	    break;
	 default :
	    rslt = TestBranch.ANY;
	    break;
       }
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

private boolean isBroaderType(BT_Class c0,BT_Class c1)
{
   if (c0 == c1) return true;

   if (c0 == BT_Class.getBoolean()) return false;
   else if (c1 == BT_Class.getBoolean()) return true;

   if (c0 == BT_Class.getChar()) return false;
   else if (c1 == BT_Class.getChar()) return true;

   int ln0 = classLength(c0);
   int ln1 = classLength(c1);

   return ln0 >= ln1;
}



private int classLength(BT_Class c0)
{
   if (c0 == BT_Class.getByte()) return 1;
   else if (c0 == BT_Class.getShort()) return 2;
   else if (c0 == BT_Class.getInt()) return 4;
   else if (c0 == BT_Class.getLong()) return 8;
   else return 4;
}




/********************************************************************************/
/*										*/
/*	Methods to handle field values						*/
/*										*/
/********************************************************************************/

@Override public Object getProgramValue()
{
   if (have_range) {
      if (min_value == max_value) return Long.valueOf(min_value);
      if (min_value > 0 || max_value < 0) return ValueType.NON_ZERO;
    }

   return null;
}



/********************************************************************************/
/*										*/
/*	LocalType values							*/
/*										*/
/********************************************************************************/

@Override protected ValueNumber createLocalType(LocalType lt)
{
   if (have_range) return new ValueInt(jflow_master,data_type,min_value,max_value,lt,this);

   return new ValueInt(jflow_master,data_type,lt,this);
}




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
   if (have_range) {
      rslt.append(" :: ");
      rslt.append(min_value);
      rslt.append(" -> ");
      rslt.append(max_value);
    }
   rslt.append("]");
   return rslt.toString();
}





}	// end of class ValueInt




/* end of ValueInt.java */
