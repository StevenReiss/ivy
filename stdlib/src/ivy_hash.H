/************************************************************************/
/*									*/
/*		ivy_hash.H						*/
/*									*/
/*	Generic hash table definitions					*/
/*									*/
/************************************************************************/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_hash.H,v 1.3 2009-01-27 00:42:57 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_hash.H,v $
 * Revision 1.3  2009-01-27 00:42:57  spr
 * Fix up hsh functions, xml output.
 *
 * Revision 1.2  2008-11-12 13:48:01  spr
 * Nicer string hash function, update xercesc
 *
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_HASH_ALREADY_INCLUDED
#define IVY_HASH_ALREADY_INCLUDED

#include <functional>
#include <string>



namespace Ivy {



/************************************************************************/
/*									*/
/*	Constants							*/
/*									*/
/************************************************************************/

class HashConstants {
public:
   enum { MAX_PERCENT_FULL = 80 };
   enum { MAX_PERCENT_ANY = 90 };

   static int hashFunction(int x,int shift_ct) {
      return ((int) ( ( ( ((unsigned)(x))*2654435769U) & 0xffffffff) >>(shift_ct)));
    }

   static int hashFunction2(int x,int shift_ct) {
      return ((int) (1|(((((unsigned)(x))*1818367229)&0xffffffff) >>(shift_ct))));
    }

   enum { HASH_UNSET = 0xffffffff };

   enum HashAction { FIND, ENTER };
};



/************************************************************************/
/*									*/
/*	Default hash function implementations				*/
/*									*/
/************************************************************************/

template<class T>
class Hash // : unary_function<T,size_t>		// unary_function no longer needed
{
public:
   unsigned long operator() (const T& key) const;
};



/********
template<class T>
class Hash<T *> : unary_function<T,size_t>
{
public:
   unsigned long operator() (T * const& key) const
      { return (unsigned long) key; }
};
*********/



template<class T>
unsigned long
Hash<T>::operator() (const T& key) const
{
   unsigned long res = 0;
   int len = sizeof(T);

   const char * p = reinterpret_cast<const char *>(&key);

   while (len--) res = (res<<1) ^ *p++;

   return res;
}


template<>
inline unsigned long
Hash<char *>::operator() (char * const& key) const
{
   unsigned long res = 0;
   const char * p = (const char *) key;

   while (*p != 0) res = (res<<5) - res + *p++;

   return res;
}



template<>
inline unsigned long
Hash<string >:: operator() (const string& key) const
{
   unsigned long res = 0;
   typedef string::const_iterator CI;
   CI p = key.begin();
   CI end = key.end();

   while (p != end) res = 31*res + *p++;

   return res;
}


template<>
inline unsigned long
Hash<int>::operator() (const int& key) const				{ return key; }


template<>
inline unsigned long
Hash<unsigned int>::operator() (const unsigned int& key) const		{ return key; }


template<>
inline unsigned long
Hash<long>::operator() (const long& key) const				{ return key; }


template<>
inline unsigned long
Hash<unsigned long>::operator() (const unsigned long& key) const	{ return key; }


template<>
inline unsigned long
Hash<void *>::operator() (void * const& key) const
{
   return (unsigned long) key;
}




/************************************************************************/
/*									*/
/*	Hash Table Template Class					*/
/*									*/
/************************************************************************/


template<class X,class Y,int N = 64, class H = Hash<X>, class EQ = equal_to<X> >
class HashMap {

/************************************************************************/
/*									*/
/*	Subclass to hold a hash entry					*/
/*									*/
/************************************************************************/

public:
   class Entry {

   private:
      bool key_valid;
      bool data_valid;

   public:
      X key;
      Y data;

   public:
      Entry()				{ key_valid = false; data_valid = false; }
      Entry(const X x,const Y y)	{ key_valid = true; data_valid = true; key = x; data = y; }
      Entry(const X x)			{ key_valid = true; data_valid = true; key = x; data = Y(); }

      void setKey(const X x)		{ key = x; key_valid = true; data_valid = true; data = Y(); }
      void setData(Y y) 		{ data_valid = true; data = y; }

      void invalidate() 		{ data_valid = false; data = Y(); }

      bool keyValid() const		{ return key_valid; }
      bool dataValid() const		{ return data_valid; }
      bool isValid() const		{ return key_valid && data_valid; }
      bool isEmpty() const		{ return !key_valid; }
      bool isAvailable() const		{ return !data_valid; }

    };		// end of subclass Entry



/************************************************************************/
/*									*/
/*	Subclass to hold iterator					*/
/*									*/
/************************************************************************/

public:
   class iterator {

   private:
      int cur_index;
      int max_index;
      Entry * table_ptr;

   public:
      iterator()
	 { table_ptr = NULL; max_index = 0; cur_index = 0; }
      iterator(Entry * tbl,int mx)
	 { table_ptr = tbl; max_index = mx; cur_index = 0; findNext(); }
      iterator(Entry * tbl,int mx,int idx)
	 { table_ptr = tbl; max_index = mx; cur_index = idx; }

      int operator == (const iterator& itr)	{ return cur_index == itr.cur_index; }
      int operator <  (const iterator& itr)	{ return cur_index < itr.cur_index; }
      int operator <= (const iterator& itr)	{ return cur_index <= itr.cur_index; }
      int operator != (const iterator& itr)	{ return cur_index != itr.cur_index; }

      iterator& operator ++()			{ ++cur_index; findNext(); return *this; }

      const X& key() const			{ return table_ptr[cur_index].key; }
      Y& data() 				{ return table_ptr[cur_index].data; }
      const Y& data() const			{ return table_ptr[cur_index].data; }

      const Y& operator *()			{ return data(); }

   private:
      void findNext() {
	 if (cur_index > max_index) {
	    cur_index = max_index;
	    return;
	  }
	 while (cur_index < max_index) {
	    if (table_ptr[cur_index].isValid()) break;
	    ++cur_index;
	  }
       }

    };		// end of subclass iterator



   class const_iterator {

   private:
      int cur_index;
      int max_index;
      const Entry * table_ptr;

   public:
      const_iterator()
	 { table_ptr = NULL; max_index = 0; cur_index = 0; }
      const_iterator(const Entry * tbl,int mx)
	 { table_ptr = tbl; max_index = mx; cur_index = 0; findNext(); }
      const_iterator(const Entry * tbl,int mx,int idx)
	 { table_ptr = tbl; max_index = mx; cur_index = idx; }

      int operator == (const const_iterator& itr) const { return cur_index == itr.cur_index; }
      int operator <  (const const_iterator& itr) const { return cur_index < itr.cur_index; }
      int operator <= (const const_iterator& itr) const { return cur_index <= itr.cur_index; }
      int operator != (const const_iterator& itr) const { return cur_index != itr.cur_index; }

      const_iterator& operator ++()		{ ++cur_index; findNext(); return *this; }

      const X& key() const			{ return table_ptr[cur_index].key; }
      const Y& data() const			{ return table_ptr[cur_index].data; }

      const Y& operator *() const		{ return data(); }

   private:
      void findNext() {
	 if (cur_index > max_index) {
	    cur_index = max_index;
	    return;
	  }
	 while (cur_index < max_index) {
	    if (table_ptr[cur_index].isValid()) break;
	    ++cur_index;
	  }
       }

    };		// end of subclass iterator


   typedef Y value_type;





/************************************************************************/
/*									*/
/*	Private Storage 						*/
/*									*/
/************************************************************************/

private:
   unsigned int max_entry;
   unsigned int num_entry;
   unsigned int num_erase;
   unsigned int shift_ct;
   unsigned int mask_val;
   Entry * table_ptr;
   H hash;
   EQ equals;
   Y dummy_value;


/************************************************************************/
/*									*/
/*	Public methods							*/
/*									*/
/************************************************************************/

public:
   HashMap(int nel = N) : dummy_value()
      { hashSetup(nel); table_ptr = new Entry [max_entry]; }
   ~HashMap()				{ destroy(); }

   void create(int nel = N) {
      destroy();
      hashSetup(nel);
      table_ptr = new Entry[ max_entry];
    }

   void clear(int nel = N)		{ create(nel); }

   void destroy()
      { if (table_ptr != NULL) delete [] table_ptr; table_ptr = NULL; reset(); }

   bool contains(const X& x) const
      { return findIndex(x) >= 0; }

   const Y& find(const X& x) const {
      int i = findIndex(x);
      if (i < 0) return dummy_value;
      else return table_ptr[i].data;
    }

   const Y& find(const X& x,const Y& dflt) const {
      int i = findIndex(x);
      if (i < 0) return dflt;
      else return table_ptr[i].data;
    }

   void enter(const X& x,const Y& y) {
      int i = findNewIndex(x);
      table_ptr[i].setData(y);
    }

   int size() const			{ return num_entry; }
   bool empty() const			{ return num_entry == 0; }
   int capacity() const 		{ return max_entry; }

   Y& operator [] (const X& x) {
      int i = findNewIndex(x);
      return table_ptr[i].data;
    }

   const Y& operator [] (const X& x) const	{ return find(x); }

   iterator begin()			{ return iterator(table_ptr,max_entry); }
   iterator end()			{ return iterator(table_ptr,max_entry,max_entry); }
   const_iterator begin() const 	{ return const_iterator(table_ptr,max_entry); }
   const_iterator end() const		{ return const_iterator(table_ptr,max_entry,max_entry); }

   int erase(const X& x) {
      int i = findIndex(x);
      if (i < 0) return 0;
      table_ptr[i].invalidate();
      --num_entry;
      ++num_erase;
      return 1;
    }

   HashMap<X,Y,N,H,EQ> * operator->()			{ return this; }
   const HashMap<X,Y,N,H,EQ> * operator->() const	{ return this; }

private:
   HashMap<X,Y,N,H,EQ>& operator =(const HashMap<X,Y,N,H,EQ>&)	{ }


/************************************************************************/
/*									*/
/*	Implementation methods (private)				*/
/*									*/
/************************************************************************/

private:
   int testEmpty(const int idx) const
      { return table_ptr[idx].isEmpty(); }
   int testAvailable(const int idx) const
      { return table_ptr[idx].isAvailable(); }
   int testPresent(const int idx) const
      { return table_ptr[idx].dataValid(); }
   int testMatch(const int idx,const X& key) const
      { return equals(table_ptr[idx].key,key); }
   void setItem(const int idx,const X& key)
      { table_ptr[idx].setKey(key); }

   void reset() { num_entry = 0; max_entry = 0; shift_ct = 0; mask_val = 0; num_erase = 0; }

   void hashSetup(const int nel = 0) {
      int sz,shft,n;
      sz = 4;
      shft = 32-2;
      n = (nel * 100)/ HashConstants::MAX_PERCENT_FULL;
      while (sz < n) { sz *= 2; shft -= 1; };
      num_entry = 0;
      num_erase = 0;
      max_entry = sz;
      shift_ct = shft;
      mask_val = max_entry - 1;
    }

   int findIndex(const X& key) const {
      long h = hash(key);
      unsigned long h1,h2,h3;

      if (max_entry == 0) return -1;
      h1 = HashConstants::hashFunction(h,shift_ct);
      h2 = HashConstants::HASH_UNSET;
      h3 = h1;
      while (!testEmpty(h1)) {
	 if (testPresent(h1) && testMatch(h1,key)) break;
	 if (h2 == HashConstants::HASH_UNSET) h2 = HashConstants::hashFunction2(h,shift_ct);
	 h1 = (h1+h2) & (mask_val);
	 if (h1 == h3) break;
       }
      return (testPresent(h1) ? ((int) h1) : -1);
    }

   int findNewIndex(const X& key) {
      long h = hash(key);
      unsigned long h1,h2;
      int nidx = -1;

      if (max_entry == 0) hashSetup();
      h1 = HashConstants::hashFunction(h,shift_ct);
      h2 = HashConstants::HASH_UNSET;
      while (!testEmpty(h1)) {
	 if (testAvailable(h1)) { if (nidx < 0) nidx = h1; else if (nidx == h1) break; }
	 else if (testMatch(h1,key)) { return h1; }
	 if (h2 == HashConstants::HASH_UNSET) h2 = HashConstants::hashFunction2(h,shift_ct);
	 h1 = (h1+h2) & (mask_val);
       }
      if (nidx >= 0) {			// reuse of deleted entry
	 h1 = nidx;
	 --num_erase;
       }
      if (testAvailable(h1)) {
	 ULongLong nent = num_entry;
	 nent = nent * 100 / HashConstants::MAX_PERCENT_FULL;
	 if (nent+1 >= (ULongLong) max_entry) {
	    expandSize();
	    return findNewIndex(key);
	  }
	 nent = num_entry;
	 nent += num_erase;
	 nent = nent * 100 / HashConstants::MAX_PERCENT_ANY;
	 if (nent+1 >= (ULongLong) max_entry) {
	    rehash();
	    return findNewIndex(key);
	  }
	 setItem(h1,key);
	 num_entry++;
       }
      return h1;
    }

   void expandSize() {
      Entry * tbl = table_ptr;
      int ct = max_entry;
      int i;

      max_entry *= 2;
      shift_ct -= 1;
      mask_val = max_entry - 1;

      table_ptr = new Entry[max_entry];
      num_entry = 0;
      num_erase = 0;
      for (i = 0; i < ct; ++i) {
	 if (tbl[i].isValid()) enter(tbl[i].key,tbl[i].data);
       }
      delete [] tbl;
    }

   void rehash() {
      Entry * tbl = table_ptr;
      int ct = max_entry;
      int i;

      table_ptr = new Entry[max_entry];
      num_entry = 0;
      num_erase = 0;
      for (i = 0; i < ct; ++i) {
	 if (tbl[i].isValid()) enter(tbl[i].key,tbl[i].data);
       }
      delete [] tbl;
    }
};





}	// end of namespace Ivy


#endif


/* end of ivy_hash.H */
