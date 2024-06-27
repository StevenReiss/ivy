/********************************************************************************/
/*										*/
/*		SwingEventListenerList.java					*/
/*										*/
/*	Listener list for a particular type of listener 			*/
/*										*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingEventListenerList.java,v 1.4 2015/11/20 15:09:26 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingEventListenerList.java,v $
 * Revision 1.4  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.3  2013/11/15 02:38:19  spr
 * Update imports; add features to combo box.
 *
 * Revision 1.2  2011-05-27 19:32:50  spr
 * Change copyrights.
 *
 * Revision 1.1  2010-10-06 23:33:06  spr
 * Add event listener list implementation.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.swing;



import javax.swing.event.EventListenerList;

import java.util.EventListener;
import java.util.Iterator;




public class SwingEventListenerList<T extends EventListener> extends EventListenerList
	implements Iterable<T>
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Class<T>	listener_class;

private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingEventListenerList(Class<T> c)
{
   listener_class = c;
}



/********************************************************************************/
/*										*/
/*	Specialized methods							*/
/*										*/
/********************************************************************************/

public void add(T l)				{ super.add(listener_class,l); }

public void remove(T l) 			{ super.remove(listener_class,l); }

@Override public int getListenerCount() 	{ return super.getListenerCount(listener_class); }

public T [] getListeners()			{ return super.getListeners(listener_class); }



/********************************************************************************/
/*										*/
/*	Iterable methods							*/
/*										*/
/********************************************************************************/

@Override public Iterator<T> iterator()
{
   return new ListenerIter();
}



private class ListenerIter implements Iterator<T> {

   private T []  event_set;
   private int	 cur_index;

   ListenerIter() {
      event_set = getListeners();
      cur_index = 0;
    }

   @Override public boolean hasNext()	{ return cur_index < event_set.length; }

   @Override public T next()		{ return event_set[cur_index++]; }

   @Override public void remove()	{ throw new UnsupportedOperationException(); }

}	// end of inner class ListenerIter





}	// end of class SwingEventListenerList




/* end of SwingEventListenerList.java */
