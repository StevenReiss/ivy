/********************************************************************************/
/*                                                                              */
/*              IdentityHashSet.java                                            */
/*                                                                              */
/*      Hash Set using object identity                                          */
/*                                                                              */
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Redistribution and use in source and binary forms, with or without		 *
 *  modification, are permitted provided that the following conditions are met:  *
 *										 *
 *  + Redistributions of source code must retain the above copyright notice,	 *
 *	this list of conditions and the following disclaimer.			 *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *	this list of conditions and the following disclaimer in the		 *
 *	documentation and/or other materials provided with the distribution.	 *
 *  + Neither the name of the Brown University nor the names of its		 *
 *	contributors may be used to endorse or promote products derived from	 *
 *	this software without specific prior written permission.		 *
 *										 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE	 *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE	 *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE	 *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 	 *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF	 *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS	 *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN	 *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)	 *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE	 *
 *  POSSIBILITY OF SUCH DAMAGE. 						 *
 *										 *
 ********************************************************************************/


package edu.brown.cs.ivy.file;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;


public class IdentityHashSet<E> implements Set<E> {


private Map<E,Boolean> set_map;

IdentityHashSet()
{
   set_map = new IdentityHashMap<>();
}

@Override public boolean add(E pt)
{
   return set_map.put(pt, Boolean.TRUE);
}

@Override public boolean addAll(Collection<? extends E> c)
{
   boolean chng = false;
   for (E pt : c) {
      chng |= set_map.put(pt, Boolean.TRUE);
    }
   return chng;
}

@Override public void clear()
{
   set_map.clear();
}

@Override public boolean contains(Object pt)
{
   return set_map.containsKey(pt);
}

@Override public boolean containsAll(Collection<?> c)
{
   for (Object pt : c) {
      if (!set_map.containsKey(pt)) return false;
   }
   return true;
}

@Override public boolean equals(Object o)
{
   if (o instanceof IdentityHashSet) {
      IdentityHashSet<?> hs = (IdentityHashSet<?>) o;
      return set_map.equals(hs.set_map);
   }
   return false;
}

@Override public int hashCode()
{
   return set_map.hashCode();
}

@Override public boolean isEmpty()
{
   return set_map.isEmpty();
}

@Override public Iterator<E> iterator()
{
   return set_map.keySet().iterator();
}

@Override public boolean remove(Object o)
{
   return set_map.remove(o);
}

@Override public boolean removeAll(Collection<?> c)
{
   boolean chng = false;
   for (Object pt : c) {
      chng |= set_map.remove(pt);
    }
   return chng;
}

@Override public boolean retainAll(Collection<?> c)
{
   boolean chng = false;
   for (Iterator<?> it = set_map.keySet().iterator(); it.hasNext(); ) {
      Object pt = it.next();
      if (!c.contains(pt)) {
	 it.remove();
	 chng = true;
      }
   }
   return chng;
}

@Override public int size()
{
   return set_map.size();
}

@Override public Spliterator<E> spliterator()
{
   return set_map.keySet().spliterator();
}

@Override public Object[] toArray()
{
   return set_map.keySet().toArray();
}

@Override public <T> T[] toArray(T[] a)
{
   return set_map.keySet().toArray(a);
}


}       // end of class IdentityHashSet


/* end of IdentityHashSet.java */
