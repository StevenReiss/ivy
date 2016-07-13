/************************************************************************/
/*									*/
/*		ivy_unixregexpr.H					*/
/*									*/
/*	Regular expression pattern matching				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_unixregexpr.H,v 1.1 2005/06/28 17:22:11 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_unixregexpr.H,v $
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_UNIXREGEXPR_ALREADY_DEFINED
#define IVY_UNIXREGEXPR_ALREADY_DEFINED


#include <regex.h>


namespace Ivy {


/************************************************************************/
/*									*/
/*	IvyRegExpr -- regular expression holder 		*/
/*									*/
/************************************************************************/


class IvyRegExpr {

private:
   regex_t compiled_expr;
   bool is_valid;

public:
   IvyRegExpr(const string& expr) {
      if (regcomp(&compiled_expr,expr.c_str(),REG_EXTENDED|REG_NOSUB) == 0) is_valid = true;
      else is_valid = false;
    }
   IvyRegExpr() 		{ is_valid = false; }
   ~IvyRegExpr()			{ free(); }

   int create(const string& expr) {
      free();
      if (regcomp(&compiled_expr,expr.c_str(),REG_EXTENDED|REG_NOSUB) == 0) is_valid = true;
      else is_valid = false;
      return isValid();
    }
   int createSimple(const string& expr) {
      free();
      if (regcomp(&compiled_expr,expr.c_str(),REG_NOSUB) == 0) is_valid = true;
      else is_valid = false;
      return isValid();
    }
   int createIgnoreCase(const string& expr) {		// implies simple
      free();
      if (regcomp(&compiled_expr,expr.c_str(),REG_ICASE|REG_NOSUB) == 0) is_valid = true;
      else is_valid = false;
      return isValid();
    }
   void free()				{ if (is_valid) regfree(&compiled_expr); is_valid = false; }

   bool isValid() const 		{ return is_valid; }
   operator int() const 		{ return isValid(); }

   bool match(const string& str) const {
      return regexec(&compiled_expr,str.c_str(),0,NULL,0) == 0;
    }

};



/************************************************************************/
/*									*/
/*	IvyRegSubExpr -- regular expression holder for substitution	*/
/*									*/
/************************************************************************/


class IvyRegSubExpr {

private:
   regex_t compiled_expr;
   bool is_valid;

public:
   IvyRegSubExpr(const string& expr) {
      if (regcomp(&compiled_expr,expr.c_str(),REG_EXTENDED) == 0) is_valid = true;
      else is_valid = false;
    }
   IvyRegSubExpr()			{ is_valid = false; }
   ~IvyRegSubExpr()			{ free(); }

   int create(const string& expr) {
      free();
      if (regcomp(&compiled_expr,expr.c_str(),REG_EXTENDED) == 0) is_valid = true;
      else is_valid = false;
      return isValid();
    }
   int createSimple(const string& expr) {
      free();
      if (regcomp(&compiled_expr,expr.c_str(),0) == 0) is_valid = true;
      else is_valid = false;
      return isValid();
    }
   int createIgnoreCase(const string& expr) {		// implies simple
      free();
      if (regcomp(&compiled_expr,expr.c_str(),REG_ICASE) == 0) is_valid = true;
      else is_valid = false;
      return isValid();
    }
   void free()				{ if (is_valid) regfree(&compiled_expr); }

   bool isValid() const 		{ return is_valid; }
   operator int() const 		{ return isValid(); }

   bool match(const string& str) const {
      return regexec(&compiled_expr,str.c_str(),0,NULL,0) == 0;
    }

   bool match(const string& str,int& nrslt,string * rslt) const {
      regmatch_t pmatch[64];
      int ct = compiled_expr.re_nsub;
      if (ct > 64) ct = 64;
      bool fg = regexec(&compiled_expr,str.c_str(),ct,pmatch,0);
      if (!fg) {
	 nrslt = 0;
	 return false;
       }
      if (nrslt < ct) ct = nrslt;
      nrslt = ct;
      for (int i = 0; i < ct; ++i) {
	 rslt[i] = str.substr(pmatch[i].rm_so,pmatch[i].rm_eo-pmatch[i].rm_so);
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
