/********************************************************************************/
/*                                                                              */
/*              ConcurrentHashSet.java                                          */
/*                                                                              */
/*      description of class                                                    */
/*                                                                              */
/*      Written by spr                                                          */
/*                                                                              */
/********************************************************************************/



package edu.brown.cs.ivy.file;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<E> implements Set<E>
{

/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<E,E> set_map;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public ConcurrentHashSet()
{
   set_map = new ConcurrentHashMap<>();
}


public ConcurrentHashSet(Collection<E> e) 
{
   this();
   
   addAll(e);
}


/********************************************************************************/
/*                                                                              */
/*      Set methods                                                             */
/*                                                                              */
/********************************************************************************/

@Override public boolean add(E pt)
{
   return set_map.putIfAbsent(pt,pt) == null;
}

@Override public boolean addAll(Collection<? extends E> c)
{
   boolean chng = false;
   for (E pt : c) {
      chng |= add(pt);
    }
   return chng;
}

public E addIfAbsent(E pt)
{
  return set_map.putIfAbsent(pt,pt);
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
   if (o instanceof ConcurrentHashSet) {
      ConcurrentHashSet<?> hs = (ConcurrentHashSet<?>) o;
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
   return set_map.remove(o) == o;
}

@Override public boolean removeAll(Collection<?> c)
{
   boolean chng = false;
   for (Object pt : c) {
      chng |= set_map.remove(pt) == pt;
    }
   return chng;
}

@Override public boolean retainAll(Collection<?> c)
{
   boolean chng = false;
   for (Iterator<?> it = set_map.keySet().iterator(); it.hasNext();) {
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

@Override public String toString()
{
   return set_map.keySet().toString();
}



}       // end of class ConcurrentHashSet




/* end of ConcurrentHashSet.java */
