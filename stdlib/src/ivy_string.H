/************************************************************************/
/*									*/
/*		ivy_string.H						*/
/*									*/
/*	String class extensions 					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_string.H,v 1.1 2005/06/28 17:22:11 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_string.H,v $
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_STRING_ALREADY_INCLUDED
#define IVY_STRING_ALREADY_INCLUDED


#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>

#ifndef WIN32
#include <unistd.h>
#endif


namespace Ivy {



/************************************************************************/
/*									*/
/*	StringTokenizer class						*/
/*									*/
/************************************************************************/


class StringTokenizer {

private:
   string base_string;
   int current_index;
   string sep_chars;

public:
   StringTokenizer(const string& base) : base_string(base) {
      sep_chars = " \t\r\f\n";
      current_index = 0;
      findStart(sep_chars);
    }


   StringTokenizer(const string& base,const string& sep)
      : base_string(base), sep_chars(sep)
    {
      current_index = 0;
      findStart(sep);
    }

   ~StringTokenizer()			{ }

   bool hasMoreTokens() 		{ return current_index >= 0; }

   string nextToken()			{ return nextToken(sep_chars); }

   string nextToken(string sep) {
      string rslt;
      if (current_index >= 0) {
	 int idx = (int) base_string.find_first_of(sep,current_index);
	 if (idx < 0) {
	    rslt = base_string.substr(current_index);
	    current_index = -1;
	  }
	 else {
	    rslt = base_string.substr(current_index,idx-current_index);
	    current_index = idx;
	    findStart(sep);
	  }
       }
      return rslt;
    }

private:
   void findStart(string sep) {
      if (current_index < 0) return;
      int idx = (int) base_string.find_first_not_of(sep,current_index);
      if (idx < 0) current_index = -1;
      else current_index = idx;
    }

};	// end of class StringTokenizer




/********************************************************************************/
/*										*/
/*	StringBuffer class							*/
/*										*/
/********************************************************************************/


class StringBuffer {

private:
   int buffer_length;
   char * buffer_base;
   char * buffer_end;

public:
   StringBuffer(int sz = 0)				{ initialize(sz); }
   StringBuffer(const string& v,int sz = 0) {
      initialize(sz);
      append(v);
    }
   StringBuffer(const char * v,int sz = 0) {
      initialize(sz);
      append(v);
    }

   ~StringBuffer() {
      delete [] buffer_base;
    }

   void clear(int sz = 0) {
      delete [] buffer_base;
      initialize(sz);
    }

   string getString() const		{ return static_cast<string>(buffer_base); }
   const char * getCString() const	{ return buffer_base; }
   int getLength() const		{ return buffer_end-buffer_base; }
   bool empty() const			{ return buffer_end == buffer_base; }

   void append(const char * v)		{ if (v != NULL) addString(v); }
   void append(const string& v) 	{ addString(v.c_str()); }
   void append(int v) {
      char buf[1024];
      snprintf(buf,1024,"%d",v);
      addString(buf);
    }
   void append(long v) {
      char buf[1024];
      snprintf(buf,1024,"%ld",v);
      addString(buf);
    }
   void append(short v) {
      char buf[1024];
      snprintf(buf,1024,"%d",v);
      addString(buf);
    }
   void append(char v) {
      char buf[2];
      buf[0] = v;
      buf[1] = 0;
      addString(buf);
    }
   void append(double v) {
      char buf[1024];
      snprintf(buf,1024,"%g",v);
      addString(buf);
    }
   void append(float v) {
      char buf[1024];
      snprintf(buf,1024,"%g",v);
      addString(buf);
    }
   void append(unsigned int v) {
      char buf[1024];
      snprintf(buf,1024,"%u",v);
      addString(buf);
    }
   void append(unsigned long v) {
      char buf[1024];
      snprintf(buf,1024,"%lu",v);
      addString(buf);
    }

   void appendXml(const string& v) {
      char c;
      char cbuf[2];
      cbuf[1] = 0;
      int ln = v.length();
      for (int i = 0; i < ln; ++i) {
	 c = v[i];
	 switch (c) {
	    case '&' :
	       addString("&amp;");
	       break;
	    case '<' :
	       addString("&lt;");
	       break;
	    case '>' :
	       addString("&gt;");
	       break;
	    case '"' :
	       addString("&quot;");
	       break;
	    case '\'' :
	       addString("&apos;");
	       break;
	    default :
	       cbuf[0] = c;
	       addString(cbuf);
	       break;
	  }
       }
    }

   StringBuffer& operator << (const char * v)		{ append(v); return *this; }
   StringBuffer& operator << (const string& v)		{ append(v); return *this; }
   StringBuffer& operator << (int v)			{ append(v); return *this; }
   StringBuffer& operator << (long v)			{ append(v); return *this; }
   StringBuffer& operator << (short v)			{ append(v); return *this; }
   StringBuffer& operator << (char v)			{ append(v); return *this; }
   StringBuffer& operator << (double v) 		{ append(v); return *this; }
   StringBuffer& operator << (float v)			{ append(v); return *this; }
   StringBuffer& operator << (unsigned int v)		{ append(v); return *this; }
   StringBuffer& operator << (unsigned long v)		{ append(v); return *this; }
   StringBuffer& operator << (const void * v)		{ append(((long) v)); return *this; }

   void append(LongLong v) {
      char buf[1024];
      snprintf(buf,1024,"%lld",v);
      addString(buf);
    }
   StringBuffer& operator << (LongLong v)		{ append(v); return *this; }

private:
   void initialize(int sz) {
      if (sz <= 0) sz = 1024;
      buffer_length = sz;
      buffer_base = new char[sz];
      buffer_end = buffer_base;
      *buffer_base = 0;
    }

   void addString(const char * v) {
      int i = strlen(v);
      if (i + (buffer_end - buffer_base) + 1 >= buffer_length) {
	 char * obuf = buffer_base;
	 char * p = obuf;
	 while (i + (buffer_end - buffer_base) + 1 >= buffer_length) buffer_length *= 2;
	 buffer_base = new char[buffer_length];
	 buffer_end = buffer_base;
	 while (*p != 0) *buffer_end++ = *p++;
	 *buffer_end = 0;
       }
      while (*v != 0) *buffer_end++ = *v++;
      *buffer_end = 0;
    }

};



/********************************************************************************/
/*										*/
/*	Misc functions on strings						*/
/*										*/
/********************************************************************************/


inline string
trim(const string& s)
{
   int lhs = s.find_first_not_of(" \t\r\n");
   if (lhs == s.npos) return string();
   int rhs = s.find_last_not_of(" \t\r\n");
   return s.substr(lhs,rhs-lhs+1);
}



inline string
itoa(int v)
{
   StringBuffer buf;
   buf.append(v);
   return buf.getString();
}



inline string
ltoa(long v)
{
   StringBuffer buf;
   buf.append(v);
   return buf.getString();
}



inline string
lltoa(LongLong v)
{
   StringBuffer buf;
   buf.append(v);
   return buf.getString();
}



inline string
ftoa(double v)
{
   StringBuffer buf;
   buf.append(v);
   return buf.getString();
}



inline string
toUpper(const string& ins)
{
   StringBuffer buf;

   for (int i = 0; i < (int) ins.length(); ++i) {
      buf.append(toupper(ins[i]));
    }

   return buf.getString();
}


inline string
toLower(const string& ins)
{
   StringBuffer buf;

   for (int i = 0; i < (int) ins.length(); ++i) {
      buf.append(tolower(ins[i]));
    }

   return buf.getString();
}


inline string
cleanWhiteSpace(const string& s0)
{
   StringBuffer buf;
   StringTokenizer tok(s0);
   int ct = 0;
   while (tok.hasMoreTokens()) {
      if (ct++ > 0) buf.append(" ");
      buf.append(tok.nextToken());
    }
   return buf.getString();
}




/********************************************************************************/
/*										*/
/*	Inline version of std system calls that use strings			*/
/*										*/
/********************************************************************************/


inline int
access(const string& s,int what)		{ return ::access(s.c_str(),what); }

inline int
mkdir(const string& s,int mode) 		{ return ::mkdir(s.c_str(),mode); }

inline int
stat(const string& s,struct stat * buf) 	{ return ::stat(s.c_str(),buf); }

inline int
open(const string& p,int f,int m = 0777)	{ return ::open(p.c_str(),f,m); }

inline int
atoi(const string& s)				{ return ::atoi(s.c_str()); }

inline long
atol(const string& s)				{ return ::atol(s.c_str()); }

inline double
atof(const string& s)				{ return ::atof(s.c_str()); }

inline char *
strdup(const string& s) 			{ return ::strdup(s.c_str()); }

inline const char *
getenv(const string& s) 			{ return ::getenv(s.c_str()); }

inline void
putenv(const string& s) 			{ ::putenv(strdup(s)); }

inline int
unlink(const string& s) 			{ return ::unlink(s.c_str()); }

inline int
system(const string& s) 			{ return ::system(s.c_str()); }

inline int
rmdir(const string& s)				{ return ::rmdir(s.c_str()); }




}	// end of namespace Ivy

#endif



/* end of ivy_string.H */
