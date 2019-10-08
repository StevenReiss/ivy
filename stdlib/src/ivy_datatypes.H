/************************************************************************/
/*									*/
/*		ivy_datatypes.H 					*/
/*									*/
/*	Basic data type definitions and language extensions		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_datatypes.H,v 1.14 2013/09/24 01:07:55 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_datatypes.H,v $
 * Revision 1.14  2013/09/24 01:07:55  spr
 * data format
 *
 * Revision 1.13  2011-03-10 02:59:17  spr
 * Updates to handle timers on the mac.
 *
 * Revision 1.12  2011-01-11 00:27:15  spr
 * Fixups for lock finding code.
 *
 * Revision 1.11  2007-12-21 02:22:58  spr
 * Updates to compile on the mac
 *
 * Revision 1.10  2007-12-13 20:23:04  spr
 * code clean up
 *
 * Revision 1.9  2007-08-10 02:12:03  spr
 * Cleanups for apple and x86_64./
 *
 * Revision 1.8  2007-05-12 00:44:51  spr
 * Fix up defs to get them to work on multiple machines.
 *
 * Revision 1.7  2007-05-12 00:35:44  spr
 * fix definitions for mac.
 *
 * Revision 1.6  2007-05-04 02:28:22  spr
 * Fix up import order and xerces version.
 *
 * Revision 1.5  2006-11-09 00:33:59  spr
 * Clean up types and operations.
 *
 * Revision 1.4  2006-08-30 20:19:16  spr
 * Clean up sets.
 *
 * Revision 1.3  2006/02/21 17:08:33  spr
 * Add default comparator for Sets.
 *
 * Revision 1.2  2006/01/30 19:07:25  spr
 * Add removeIf call.
 *
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_DATATYPES_ALREADY_INCLUDED
#define IVY_DATATYPES_ALREADY_INCLUDED

#undef _XOPEN_SOURCE
#undef _POSIX_C_SOURCE

#include <sys/types.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>

#include <new>
#include <list>
#include <vector>
#include <set>
#include <map>
#include <algorithm>
#include <functional>
#include <string>
#include <exception>


#ifdef __sparc
#include "ivy_solaris.H"
#endif
#ifdef LINUX
#include "ivy_linux.H"
#endif
#ifdef APPLE
#include "ivy_apple.H"
#endif
#ifdef WIN32
#include "ivy_win32.H"
#endif


namespace Ivy {

using namespace std;



/************************************************************************/
/*									*/
/*	Basic types							*/
/*									*/
/************************************************************************/


typedef int		Integer;
typedef double		Float;
typedef double		Double;
typedef short		Short;
typedef long		Long;
typedef bool		Bool;
typedef char		Character;
typedef char *		Text;
typedef const char *	ConstText;
typedef string		StdString;
typedef const string	ConstStdString;
typedef const string&	CStdString;
typedef signed char	Byte;



/************************************************************************/
/*									*/
/*	Constants							*/
/*									*/
/************************************************************************/


#ifndef TRUE
#define TRUE true
#endif
#ifndef FALSE
#define FALSE false
#endif
#ifndef NULL
#define NULL 0
#endif




/************************************************************************/
/*									*/
/*	String comparisons						*/
/*									*/
/************************************************************************/


#ifdef STREQL
#undef STREQL
#endif
#ifdef STRNEQ
#undef STRNEQ
#endif
#ifdef STRLSS
#undef STRLSS
#endif
#ifdef STRLEQ
#undef STRLEQ
#endif
#ifdef STRGTR
#undef STRGTR
#endif
#ifdef STRGEQ
#undef STRGEQ
#endif
#ifdef STRPFX
#undef STRPFX
#endif


inline int
STREQL(const char * s1,const char * s2)
{ return (strcmp(((char *)(s1)),((char *)(s2))) == 0); }

inline int
STRNEQ(const char * s1,const char * s2)
{ return (strcmp(((char *)(s1)),((char *)(s2))) != 0); }

inline int
STRLSS(const char * s1,const char * s2)
{ return (strcmp(((char *)(s1)),((char *)(s2))) < 0); }

inline int
STRLEQ(const char * s1,const char * s2)
{ return (strcmp(((char *)(s1)),((char *)(s2))) <= 0); }

inline int
STRGTR(const char * s1,const char * s2)
{ return (strcmp(((char *)(s1)),((char *)(s2))) > 0); }

inline int
STRGEQ(const char * s1,const char * s2)
{ return (strcmp(((char *)(s1)),((char *)(s2))) >= 0); }

inline int
STRPFX(const char * s1,const char * s2)
{ return (strstr(((char *)(s1)),((char *)(s2))) == s1); }




inline int
STRCASE(const char * s1a,const char * s2a)
{
   ConstText s1 = s1a;
   ConstText s2 = s2a;
   int ch,i;

   if (s1 == NULL) s1 = "";
   if (s2 == NULL) s2 = "";

   i = 0;
   while (*s1 != 0) {
      ch = *s1++;
      i = (ch - *s2++);
      if (i != 0) {
	 if (abs(i) != ('a' - 'A')) break;
	 else if (!isalpha(ch)) break;
	 else i = 0;
       };
    };

   if (i == 0 && *s2 != 0) i = -1;
   else if (i > 0) i = 1;
   else if (i < 0) i = -1;

   return i;
}




inline int
STRCASEEQL(const char * s1,const char * s2)
{ return STRCASE(s1,s2) == 0; }

inline int
STRCASENEQ(const char * s1,const char * s2)
{ return STRCASE(s1,s2) != 0; }

inline int
strprefix(const char * s,const char * pat)
{
   int i = (int) strlen(pat);
   if (strncmp(s,pat,i) == 0) return TRUE;
   return FALSE;
}


inline int
strsuffix(const char * s,const char * pat)
{
   int i = (int) strlen(pat);
   int j = (int) strlen(s);
   if (i <= j && strcmp(&s[j-i],pat) == 0) return TRUE;
   return FALSE;
}


inline int
STRCASE(const string& s1,const string& s2)
{ return STRCASE(s1.c_str(),s2.c_str()); }

inline int
STRCASEEQL(const string& s1,const string& s2)
{ return STRCASE(s1,s2) == 0; }

inline int
STRCASENEQ(const string& s1,const string& s2)
{ return STRCASE(s1,s2) != 0; }

inline int
strprefix(const string& s1,const string& s2)
{ return strprefix(s1.c_str(),s2.c_str()); }


inline int
strsuffix(const string& s1,const string& s2)
{ return strsuffix(s1.c_str(),s2.c_str()); }


inline int
STRCASE(const string& s1,const char * s2)
{ return STRCASE(s1.c_str(),s2); }

inline int
STRCASEEQL(const string& s1,const char * s2)
{ return STRCASE(s1,s2) == 0; }

inline int
STRCASENEQ(const string& s1,const char * s2)
{ return STRCASE(s1,s2) != 0; }

inline int
strprefix(const string& s1,const char * s2)
{ return strprefix(s1.c_str(),s2); }

inline int
strsuffix(const string& s1,const char * s2)
{ return strsuffix(s1.c_str(),s2); }



#ifdef WIN32
inline	void	strfree(const char * s)
{ if (s != NULL) delete ((void *) s); }
#else
inline	void	strfree(const char * s)
{ if (s != NULL) ::free((void *) s); }
#endif


/************************************************************************/
/*									*/
/*	Filename manipulation extensions				*/
/*									*/
/************************************************************************/


inline ConstText
IVY_adjust_path_name(Text name)
{
   ConstText s;
   Text t;

   s = name;
   t = name;
   while (*s != 0) {
      if (*s != '/') *t++ = *s++;
      else {
	 if (strncmp(s,"/./",3) == 0) s += 2;
	 else if (strncmp(s,"/../",4) == 0) {
	    if (t != name) {
	       --t;
	       while (*t != '/' && t != name) --t;
	     };
	    s += 3;
	  }
	 else if (s != name && s[1] == '/') ++s;
	 else *t++ = *s++;
       };
    };
   *t = 0;

   return name;
}




/************************************************************************/
/*									*/
/*	List template extensions					*/
/*									*/
/************************************************************************/


template<class T>
class List : public list<T> {

public:
   void pushBack(const T& x)			{ list<T>::push_back(x); }
   void pushFront(const T& x)			{ list<T>::push_front(x); }

   void popBack()				{ list<T>::pop_back(); }
   void popFront()				{ list<T>::pop_front(); }
   template <class P> void removeIf(P p)	{ list<T>::remove_if(p); }

   typename list<T>::iterator nth(int n) {
      typename list<T>::iterator iter;
      for (iter = list<T>::begin(); n > 0 && iter != list<T>::end(); ++iter) --n;
      return iter;
    }

   List * operator->()				{ return this; }
   const List * operator->() const		{ return this; }

   bool contains(const T& x) const		{ return find(list<T>::begin(),list<T>::end(),x) != list<T>::end(); }
};




/********************************************************************************/
/*										*/
/*	Vector -- modified vector template class				*/
/*										*/
/********************************************************************************/


template<class T>
class Vector : public vector<T> {

public:
   class IndexException : public exception {
   public:
      int index_value;
   public:
      IndexException(int idx)				{ index_value = idx; }
      const char * what() const throw() 		{ return "IndexOutOfBounds"; }
    };

public:
   Vector()						{ }
   Vector(int sz,const T& v = T()) : vector<T>(sz,v)	{ }

   void pushBack(const T& x)				{ vector<T>::push_back(x); }
   void popBack()					{ vector<T>::pop_back(); }

   Vector<T> * operator->()				{ return this; }
   const Vector<T> * operator->() const 		{ return this; }

   int maxSize() const					{ return vector<T>::max_size(); }

   typename vector<T>::iterator nth(int n)		{ return vector<T>::begin()+n; }

// T& operator[](int v) throw(IndexException) {
   T& operator[](int v) {
      if (v < 0 || v >= (int) vector<T>::size()) throw IndexException(v);
      else return vector<T>::operator[](v);
    }
// const T& operator[](int v) const throw(IndexException) {
   const T& operator[](int v) const {
      if (v < 0 || v >= (int) vector<T>::size()) throw IndexException(v);
      else return vector<T>::operator[](v);
    }

   bool contains(const T x)			{ return find(vector<T>::begin(),vector<T>::end(),x) != vector<T>::end(); }
};



class BoolVector : public vector<bool> {

public:
   BoolVector() 						{ }
   BoolVector(int sz,bool v = false) : vector<bool>(sz,v)	{ }

   void pushBack(bool x)				{ push_back(x); }
   void popBack()					{ pop_back(); }

   BoolVector * operator->()				{ return this; }
   const BoolVector * operator->() const		{ return this; }

   int maxSize() const					{ return (int) max_size(); }

   iterator nth(int n)					{ return begin()+n; }
};



/********************************************************************************/
/*										*/
/*	Set -- modified set template class					*/
/*										*/
/********************************************************************************/


template<class T,class C = less<T> >
class Set : public set<T,C> {

public:
   Set * operator->()				{ return this; }
   const Set * operator->() const		{ return this; }
};




/********************************************************************************/
/*										*/
/*	Map -- modified map template class					*/
/*										*/
/********************************************************************************/


template<class K,class T,class C = less<K> >
class Map : public map<K,T,C> {

private:
   typedef typename map<K,T,C>::key_type KeyType;

public:
   typename map<K,T,C>::iterator lowerBound(const KeyType& x)  { return map<K,T,C>::lower_bound(x); }
   typename map<K,T,C>::iterator upperBound(const KeyType& x)  { return map<K,T,C>::upper_bound(x); }
   typename map<K,T,C>::const_iterator lowerBound(const KeyType& x) const { return map<K,T,C>::lower_bound(x); }
   typename map<K,T,C>::const_iterator upperBound(const KeyType& x) const { return map<K,T,C>::upper_bound(x); }

   Map * operator->()				{ return this; }
   const Map * operator->() const		{ return this; }
};




/********************************************************************************/
/*										*/
/*	MultiMap -- modified multimap template class				*/
/*										*/
/********************************************************************************/


template<class K,class T,class C>
class MultiMap : public multimap<K,T,C> {

private:
   typedef typename multimap<K,T,C>::key_type KeyType;

public:
   typename multimap<K,T,C>::iterator enter(const K& key,const T& v)
      { return multimap<K,T,C>::insert(pair<const K,T>(key,v)); }
   typename multimap<K,T,C>::iterator lowerBound(const KeyType& k)     { return multimap<K,T,C>::lower_bound(k); }
   typename multimap<K,T,C>::iterator upperBound(const KeyType& k)     { return multimap<K,T,C>::upper_bound(k); }
   typename multimap<K,T,C>::const_iterator lowerBound(const KeyType& k) const { return multimap<K,T,C>::lower_bound(k); }
   typename multimap<K,T,C>::const_iterator upperBound(const KeyType& k) const { return multimap<K,T,C>::upper_bound(k); }
};




/********************************************************************************/
/*										*/
/*	Generalized iterator							*/
/*										*/
/********************************************************************************/


template<class X>
class IvyIter {

private:
   typename X::const_iterator cur_iter;
   typename X::const_iterator end_iter;

public:
   IvyIter() : cur_iter(), end_iter()	{ }
   IvyIter(const X& x) {
      cur_iter = x.begin();
      end_iter = x.end();
    }

   bool more() const				{ return cur_iter != end_iter; }
   void next()					{ ++cur_iter; }
   typename X::value_type current() const	{ return *cur_iter; }

   operator int()				{ return more(); }
   IvyIter& operator ++()			{ next(); return *this; }
   typename X::value_type operator *() const	{ return current(); }

   IvyIter& operator = (const X& x) {
      cur_iter = x.begin();
      end_iter = x.end();
    }
};




/********************************************************************************/
/*										*/
/*	Methods for byte swaping						*/
/*										*/
/********************************************************************************/

inline int
swapBytes(short& x)
{
   x = ((x&0xff) << 8) | ((x>>8) & 0xff);
   return x;
}

inline int
swapBytes(unsigned short& x)
{
   x = ((x&0xff) << 8) | ((x>>8) & 0xff);
   return x;
}

inline int
swapBytes(int& x)
{
   x = ((x&0xff) << 24) | ((x & 0xff00) << 8) | ((x & 0xff0000) >> 8) | ((x >> 24) & 0xff);
   return x;
}

inline int
swapBytes(unsigned int& x)
{
   x = ((x&0xff) << 24) | ((x & 0xff00) << 8) | ((x & 0xff0000) >> 8) | ((x >> 24) & 0xff);
   return x;
}

inline long
swapBytes(long& x)
{
   if (sizeof(long) == 4) {
      x = ((x&0xff) << 24) | ((x & 0xff00) << 8) | ((x & 0xff0000) >> 8) | ((x >> 24) & 0xff);
    }
   else {
      long x0 = ((x >> 16) >> 16) & 0x0ffffffffL;
      x0 = ((x0&0xff) << 24) | ((x0 & 0xff00) << 8) | ((x0 & 0xff0000) >> 8) | ((x0 >> 24) & 0xff);
      long x1 = x & 0x0ffffffffL;
      x1 = ((x1&0xff) << 24) | ((x1 & 0xff00) << 8) | ((x1 & 0xff0000) >> 8) | ((x1 >> 24) & 0xff);
      x = ((x1 << 16) << 16) | (x0 & 0x0ffffffffL);
    }
   return x;
}

inline long
swapBytes(unsigned long& x)
{
   if (sizeof(long) == 4) {
      x = ((x&0xff) << 24) | ((x & 0xff00) << 8) | ((x & 0xff0000) >> 8) | ((x >> 24) & 0xff);
    }
   else {
      unsigned long x0 = ((x >> 16) >> 16) & 0x0ffffffffL;
      x0 = ((x0&0xff) << 24) | ((x0 & 0xff00) << 8) | ((x0 & 0xff0000) >> 8) | ((x0 >> 24) & 0xff);
      unsigned long x1 = x & 0x0ffffffffL;
      x1 = ((x1&0xff) << 24) | ((x1 & 0xff00) << 8) | ((x1 & 0xff0000) >> 8) | ((x1 >> 24) & 0xff);
      x = ((x0 << 16) << 16) | (x1 & 0x0ffffffffL);
    }
   return x;
}

inline LongLong
swapBytes(LongLong& x)
{
   LongLong x0 = (x >> 32) & 0x0ffffffffL;
   x0 = ((x0&0xff) << 24) | ((x0 & 0xff00) << 8) | ((x0 & 0xff0000) >> 8) | ((x0 >> 24) & 0xff);
   LongLong x1 = x & 0x0ffffffffL;
   x1 = ((x1&0xff) << 24) | ((x1 & 0xff00) << 8) | ((x1 & 0xff0000) >> 8) | ((x1 >> 24) & 0xff);
   x = x0 << 32 | (x1 & 0x0ffffffffL);

   return x;
}

inline ULongLong
swapBytes(ULongLong& x)
{
   ULongLong x0 = (x >> 32) & 0x0ffffffffL;
   x0 = ((x0&0xff) << 24) | ((x0 & 0xff00) << 8) | ((x0 & 0xff0000) >> 8) | ((x0 >> 24) & 0xff);
   ULongLong x1 = x & 0x0ffffffffL;
   x1 = ((x1&0xff) << 24) | ((x1 & 0xff00) << 8) | ((x1 & 0xff0000) >> 8) | ((x1 >> 24) & 0xff);
   x = x0 << 32 | (x1 & 0x0ffffffffL);

   return x;
}

inline int
swapBytes24(int x)
{
   x = ((x&0xff) << 16) | (x& 0x00ff00) | ((x & 0x00ff0000) >> 16);
   return x;
}



/********************************************************************************/
/*										*/
/*	Miscellaneous functions 						*/
/*										*/
/********************************************************************************/

inline long
currentTimeMillis()
{
   struct timeval tv;

   gettimeofday(&tv,NULL);

   long rslt = tv.tv_sec;
   rslt = rslt * 1000 + (tv.tv_usec + 500)/1000;

   return rslt;
}


inline LongLong
currentTimeNanos()
{
   struct timespec ts;

#ifdef CLOCK_REALTIME
   int v = clock_gettime(CLOCK_REALTIME,&ts);
#else
   struct mach_timespec mts;
   int v = clock_get_time(REALTIME_CLOCK,&mts);
   ts.tv_nsec = mts.tv_nsec;
   ts.tv_sec = mts.tv_sec;
#endif

   LongLong rslt = ts.tv_nsec + ts.tv_sec * 1000000000; 

   return rslt;
}



}	// end of namespace Ivy


#endif



/* end of ivy_datatypes.H */
