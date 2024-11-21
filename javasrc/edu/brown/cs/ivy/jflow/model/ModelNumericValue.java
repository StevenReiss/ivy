/********************************************************************************/
/*										*/
/*		ModelNumericValue.java						*/
/*										*/
/*	Representation of an integer value for model building			*/
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

package edu.brown.cs.ivy.jflow.model;

import com.ibm.jikesbt.BT_Class;




class ModelNumericValue extends ModelValue
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private boolean 	is_float;
private boolean 	have_range;
private long		min_value;
private long		max_value;
private double		min_fvalue;
private double		max_fvalue;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ModelNumericValue(BT_Class dt)
{
   super(dt);
   have_range = false;
   min_value = 0;
   max_value = 0;
   min_fvalue = 0;
   max_fvalue = 0;
   is_float = (dt == BT_Class.getFloat() || dt == BT_Class.getDouble());
}




ModelNumericValue(BT_Class dt,long mnv,long mxv)
{
   super(dt);

   have_range = true;
   min_value = mnv;
   max_value = mxv;
   min_fvalue = mnv;
   max_fvalue = mxv;
   is_float = (dt == BT_Class.getFloat() || dt == BT_Class.getDouble());
}




ModelNumericValue(BT_Class dt,double mnv,double mxv)
{
   super(dt);

   have_range = true;
   min_value = (long) mnv;
   max_value = (long) mxv;
   min_fvalue = mnv;
   max_fvalue = mxv;
   is_float = (dt == BT_Class.getFloat() || dt == BT_Class.getDouble());
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override boolean isCategory2()		{ return data_type == BT_Class.getLong() ||
				     data_type == BT_Class.getDouble(); }


boolean match(long v,long v1)
{
   if (!have_range) return false;

   if (is_float) {
      return min_fvalue == v && max_fvalue == v1;
    }

   return min_value == v && max_value == v1;
}


boolean match(double v,double v1)
{
   if (!have_range) return false;

   if (is_float) {
      return min_fvalue == v && max_fvalue == v1;
    }

   return min_value == v && max_value == v1;
}


boolean matchAny()
{
   return !have_range;
}




/********************************************************************************/
/*										*/
/*	Methods for handling arithmetic 					*/
/*										*/
/********************************************************************************/

@Override ModelValue performOperation(BT_Class typ,ModelValue rhsv,int op)
{
   if (rhsv == null) rhsv = this;

   ModelNumericValue rhs = (ModelNumericValue) rhsv;

   long v0 = 0;
   long v1 = 0;
   double d0 = 0;
   double d1 = 0;
   boolean valok = true;
   boolean newflt = is_float;

   if (is_float) {
      if (min_fvalue == max_fvalue) d0 = min_fvalue;
      else valok = false;
    }
   else {
      if (min_value == max_value) v0 = min_value;
      else valok = false;
    }

   if (rhs.is_float) {
      if (rhs.min_fvalue == rhs.max_fvalue) d1 = rhs.min_fvalue;
      else valok = false;
    }
   else {
      if (rhs.min_value == rhs.max_value) v1 = rhs.min_value;
      else valok = false;
    }

   boolean rngok = (have_range && rhs.have_range);

   long mnv = min_value;
   long mxv = max_value;
   double mnf = min_fvalue;
   double mxf = max_fvalue;

   switch (op) {
      case opc_iinc :
      case opc_iadd :
      case opc_ladd :
	 v0 += v1;
	 if (rngok) {
	    mnv += rhs.min_value;
	    mxv += rhs.max_value;
	  }
	 if (Math.abs(v0) > JFLOW_VALUE_MAX_INCR) {
	    valok = false;
	    rngok = false;
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
	 if (v1 == 0) return anyValue(typ);
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
	 if (Math.abs(v0) > JFLOW_VALUE_MAX_INCR) {
	    valok = false;
	    rngok = false;
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
      case opc_i2f :
      case opc_l2f :
	 mnf = min_value;
	 mxf = max_value;
	 newflt = true;
	 d0 = v0;
	 break;
      case opc_dcmpg : case opc_dcmpl :
      case opc_fcmpg : case opc_fcmpl :
	 mnv = -1;
	 mxv = 1;
	 if (valok) {
	    if (d0 == d1) mnv = mxv = 0;
	    else if (rngok && max_fvalue < rhs.min_fvalue) mxv = -1;
	    else if (rngok && min_fvalue > rhs.max_fvalue) mnv = 1;
	    valok = false;
	    rngok = true;
	  }
	 else {
	    rngok = true;
	    mnv = -1;
	    mxv = 1;
	  }
	 newflt = false;
	 break;
      case opc_d2f :
      case opc_f2d :
	 break;
      case opc_fneg :
      case opc_dneg :
	 d0 = -d0;
	 mnf = -mxf;
	 mxf = -mnf;
	 break;
      case opc_d2i :
      case opc_f2i :
      case opc_d2l :
      case opc_f2l :
	 newflt = false;
	 mnv = (long) mnf;
	 mxv = (long) mxf;
	 v0 = (long) d0;
	 break;
      case opc_dadd :
      case opc_fadd :
	 d0 += d1;
	 if (rngok) {
	    mnf += rhs.min_fvalue;
	    mxf += rhs.max_fvalue;
	  }
	 break;
      case opc_dsub :
      case opc_fsub :
	 d0 -= d1;
	 if (rngok) {
	    mnf -= rhs.max_fvalue;
	    mxf -= rhs.min_fvalue;
	  }
	 break;
      case opc_ddiv :
      case opc_fdiv :
	 if (valok && d1 == 0) valok = false;
	 else if (valok) d0 /= d1;
	 if (rngok) {
	    if (rhs.min_fvalue <= 0 && rhs.max_fvalue >= 0) rngok = false;
	    else {
	       mnf = Math.min(mnf/rhs.max_fvalue,mnf/rhs.min_fvalue);
	       mxf = Math.max(mxf/rhs.max_fvalue,mxf/rhs.min_fvalue);
	     }
	  }
	 break;
      case opc_dmul :
      case opc_fmul :
	 d0 *= d1;
	 if (rngok) {
	    mnf = min_fvalue*rhs.min_fvalue;
	    mnf = Math.min(mnv,min_fvalue*rhs.max_fvalue);
	    mnf = Math.min(mnv,max_fvalue*rhs.min_fvalue);
	    mnf = Math.min(mnv,max_fvalue*rhs.max_fvalue);
	    mxf = min_fvalue*rhs.min_fvalue;
	    mxf = Math.max(mxv,min_fvalue*rhs.max_fvalue);
	    mxf = Math.max(mxv,max_fvalue*rhs.min_fvalue);
	    mxf = Math.max(mxv,max_fvalue*rhs.max_fvalue);
	  }
	 break;

      default :
	 valok = false;
	 rngok = false;
	 break;
    }

   ModelValue rslt;

   if (valok) {
      if (newflt) rslt = rangeValue(typ,d0,d0);
      else rslt = rangeValue(typ,v0,v0);
    }
   else if (rngok) {
      if (newflt) rslt = rangeValue(typ,mnf,mxf);
      else rslt = rangeValue(typ,mnv,mxv);

    }
   else rslt = anyValue(typ);

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Methods to handle merging values					*/
/*										*/
/********************************************************************************/

@Override ModelValue mergeValue(ModelValue cv)
{
   if (cv == this || cv == null) return this;

   if (!(cv instanceof ModelNumericValue)) return badValue();

   ModelNumericValue cvi = (ModelNumericValue) cv;

   if (!have_range || (cvi.have_range && min_value <= cvi.min_value && max_value >= cvi.max_value)) {
      if (isBroaderType(data_type,cvi.data_type)) return this;
    }

   if (!cvi.have_range || (have_range && cvi.min_value <= min_value && cvi.max_value >= max_value)) {
      if (isBroaderType(cvi.data_type,data_type)) return cvi;
    }

   if (have_range && cvi.have_range && data_type == cvi.data_type) {
      if (is_float) {
	 return rangeValue(data_type,
			      Math.min(min_fvalue,cvi.min_fvalue),
			      Math.max(max_fvalue,cvi.max_fvalue));
       }
		return rangeValue(data_type,
				      Math.min(min_value,cvi.min_value),
				      Math.max(max_value,cvi.max_value));
    }

   BT_Class typ = data_type;
   if (!isBroaderType(data_type,cvi.data_type)) typ = cvi.data_type;

   return anyValue(typ);
}



/********************************************************************************/
/*										*/
/*	Methods to handle branch conditions					*/
/*										*/
/********************************************************************************/

@Override TestBranch branchTest(ModelValue rhs,int op)
{
   if (rhs == null) rhs = this;

   ModelNumericValue rvi = (ModelNumericValue) rhs;

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

@Override Object getProgramValue()
{
   if (have_range) {
      if (is_float) {
	 if (min_fvalue == max_fvalue) return Double.valueOf(min_fvalue);
	 if (min_fvalue > 0 || max_fvalue < 0) return ValueType.NON_ZERO;
       }
      else {
	 if (min_value == max_value) return Long.valueOf(min_value);
	 if (min_value > 0 || max_value < 0) return ValueType.NON_ZERO;
       }
    }

   return null;
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





}	// end of class ModelNumericValue




/* end of ModelNumericValue.java */
