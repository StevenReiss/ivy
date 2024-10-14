/********************************************************************************/
/*										*/
/*		SwingListSet.java						*/
/*										*/
/*	Generic set for ordered recomputed elements for a list widget		*/
/*										*/
/********************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss		      */
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



package edu.brown.cs.ivy.swing;


import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;



public class SwingListSet<T extends Object> extends AbstractListModel<T>
		implements Iterable<T>, ComboBoxModel<T>
{




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private transient Collection<T> element_set;
private transient T []  element_array;
private transient T     selected_item;


private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructor								*/
/*										*/
/********************************************************************************/

public SwingListSet()
{
   element_set = new ArrayList<T>();
   element_array = null;
   selected_item = null;
}


public SwingListSet(Collection<T> base)
{
   element_set = base;
   element_array = null;
   selected_item = null;
}


public SwingListSet(boolean ordered)
{
   element_set = (ordered ? new TreeSet<T>() : new ArrayList<T>());
   element_array = null;
   selected_item = null;
}




/********************************************************************************/
/*										*/
/*	List Model Methods							*/
/*										*/
/********************************************************************************/

@Override public synchronized T getElementAt(int idx)
{
   getArray();
   if (idx < 0 || idx >= element_array.length) return null;
   return element_array[idx];
}



@Override public int getSize() {
   getArray();
   return element_array.length;
}


public boolean isOrderable() 
{
   if (element_set instanceof Set) return false;
   if (element_set instanceof List) return true;
   
   return false;
}



/********************************************************************************/
/*										*/
/*	ComboBox Model Methods							*/
/*										*/
/********************************************************************************/

@Override public Object getSelectedItem() 		{ return selected_item; }

@Override public void setSelectedItem(Object itm) 	
{ 
   for (T x : element_set) {
      if (x.equals(itm)) {
	 selected_item = x;
	 return;
      }
   }
   
   selected_item = null;
}



/********************************************************************************/
/*										*/
/*	Methods to indicate events have been added/removed/changed		*/
/*										*/
/********************************************************************************/

public synchronized void addElement(T elt)
{
   if (elt == null) return;
   if (element_set.contains(elt)) return;

   int idx0 = element_set.size();
   element_set.add(elt);
   element_array = null;
   fireIntervalAdded(this,idx0,idx0);
}



public synchronized void removeElement(T elt)
{
   if (elt == null) return;
   int idx = getIndexOf(elt);
   if (idx < 0) return;
   element_set.remove(elt);
   element_array = null;
   fireIntervalRemoved(this,idx,idx);
}




public synchronized void removeAll()
{
   int idx = element_set.size();
   if (idx == 0) return;

   element_set.clear();
   element_array = null;
   fireIntervalRemoved(this,0,idx-1);
}




public synchronized void editElement(T elt)
{
   int idx = getIndexOf(elt);
   if (idx < 0) return;
   fireContentsChanged(this,idx,idx);
}


public void moveElement(int fromindex,int toindex)
{
   if (element_set instanceof List) {
      List<T> eset = (List<T>) element_set;
      T elt = eset.remove(fromindex);
      eset.set(toindex,elt);
      int min = Math.min(fromindex,toindex);
      int max = Math.max(fromindex,toindex);
      element_array = null;
      fireContentsChanged(this,min,max);
    }
}




public synchronized void update()
{
   element_array = null;
   int idx = element_set.size();
   fireContentsChanged(this,0,idx-1);
}


private int getIndexOf(T ve)
{
   getArray();
   for (int i = 0; i < element_array.length; ++i) {
      if (element_array[i] == ve) return i;
    }
   return -1;
}




@SuppressWarnings("unchecked") 
private void getArray()
{
   if (element_array == null) {
      element_array = (T[]) element_set.toArray();
    }
}



/********************************************************************************/
/*										*/
/*	Collection methods							*/
/*										*/
/********************************************************************************/

public boolean contains(Object o)
{
   return element_set.contains(o);
}


/********************************************************************************/
/*										*/
/*	Iterator								*/
/*										*/
/********************************************************************************/

@Override public Iterator<T> iterator()		{ return element_set.iterator(); }

public Collection<T> getElements()	{ return element_set; }



}	// end of class SwingListSet




/* end of SwingListSet.java */
