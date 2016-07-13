/************************************************************************/
/*									*/
/*		ivy_winregexpr.H					*/
/*									*/
/*	Regular expression pattern matching for windows 		*/
/*									*/
/************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 2005, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_winregexpr.H,v 1.1 2005/06/28 17:22:12 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_winregexpr.H,v $
 * Revision 1.1  2005/06/28 17:22:12  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_WINREGEXPR_ALREADY_DEFINED
#define IVY_WINREGEXPR_ALREADY_DEFINED


#include <atlrx.h>


namespace Ivy {


/************************************************************************/
/*									*/
/*	IvyRegExpr -- regular expression holder 		*/
/*									*/
/************************************************************************/


class IvyRegExpr {

private:
   CAtlRegExp<>  * reg_expr;
   bool is_valid;

public:
   IvyRegExpr(const string& expr) {
      reg_expr = NULL;
      is_valid = create(expr);
    }
   IvyRegExpr() 		{ is_valid = false; reg_expr = NULL; }
   ~IvyRegExpr()			{ free(); }

   int create(const string& expr) {
      free();
      reg_expr = new CAtlRegExp<> ();
      is_valid = reg_expr->Parse(expr.c_str());
      return isValid();
    }
   int createSimple(const string& expr) {
      return create(expr);
    }
   int createIgnoreCase(const string& expr) {		// implies simple
      free();
      reg_expr = new CAtlRegExp<> ();
      is_valid = reg_expr->Parse(expr.c_str(),FALSE);
      return isValid();
    }
   void free() {
      if (reg_expr != NULL) {
	 delete reg_expr;
	 reg_expr = NULL;
       }
    }

   bool isValid() const 		{ return is_valid; }
   operator int() const 		{ return isValid(); }

   bool match(const string& str) const {
      CAtlREMatchContext<> ctx;
      if (!is_valid || reg_expr == NULL) return false;
      return reg_expr->Match(str.c_str(),&ctx) != 0;
    }

};



/************************************************************************/
/*									*/
/*	IvyRegSubExpr -- regular expression holder for substitution	*/
/*									*/
/************************************************************************/


class IvyRegSubExpr : public IvyRegExpr {

private:
   CAtlRegExp<> * reg_expr;
   bool is_valid;

public:
   IvyRegSubExpr(const string& expr) : IvyRegExpr(expr) { }
   IvyRegSubExpr()						{ }
   ~IvyRegSubExpr()						{ }

   bool match(const string& str) const {
      return IvyRegExpr::match(str);
    }

   bool match(const string& str,int& nrslt,string * rslt) const {
      CAtlREMatchContext<> ctx;
      if (!is_valid || reg_expr == NULL) return false;
      bool fg = reg_expr->Match(str.c_str(),&ctx) != 0;
      if (!fg) return false;

      if (nrslt > (int) ctx.m_uNumGroups) nrslt = ctx.m_uNumGroups;
      for (int i = 0; i < nrslt; ++i) {
		  ConstText st,ed;
		  ctx.GetMatch(i,&st,&ed);
		  rslt[i] = string(st,ed-st);
       }
      return true;
    }

   bool match(const string& str,string& rslt) const {
      string r[1];
      int ctr = 1;
      bool fg = match(str,ctr,r);
      if (!fg || ctr == 0) rslt.resize(0);
      else rslt = r[0];
      return fg;
    }

};



}	// end of namespace Ivy


#endif


/* end of ivy_regexpr.H */
