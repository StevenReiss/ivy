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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingListSet.java,v 1.10 2018/08/02 15:10:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingListSet.java,v $
 * Revision 1.10  2018/08/02 15:10:54  spr
 * Fix imports.  Prepare for java 10.
 *
 * Revision 1.9  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.8  2015/02/14 18:46:19  spr
 * Clean up; add helper methods.
 *
 * Revision 1.7  2013-06-03 13:03:42  spr
 * *** empty log message ***
 *
 * Revision 1.6  2012-10-05 00:46:40  spr
 * Code cleanup.
 *
 * Revision 1.5  2011-05-27 19:32:51  spr
 * Change copyrights.
 *
 * Revision 1.4  2009-09-17 02:00:45  spr
 * Add autocomplete, new grid options, fix up lists, add range scroll bar.
 *
 * Revision 1.3  2009-03-20 01:59:50  spr
 * Add enum-based choice box; add remove/update calls to lists; loosen numeric field checking.
 *
 * Revision 1.2  2007-05-04 02:00:37  spr
 * Import fixups.
 *
 * Revision 1.1  2006/07/23 02:25:33  spr
 * Move list panel and its support to swing; move range slider to swing.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;


import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
