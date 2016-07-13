/********************************************************************************/
/*										*/
/*		SwingRootMonitor.java						*/
/*										*/
/*	Simple root window monitor class that exits				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Redistribution and use in source and binary forms, with or without           *
 *  modification, are permitted provided that the following conditions are met:  *
 *                                                                               *
 *  + Redistributions of source code must retain the above copyright notice,     *
 *      this list of conditions and the following disclaimer.                    *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *      this list of conditions and the following disclaimer in the              *
 *      documentation and/or other materials provided with the distribution.     *
 *  + Neither the name of the Brown University nor the names of its              *
 *      contributors may be used to endorse or promote products derived from     *
 *      this software without specific prior written permission.                 *
 *                                                                               *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   *
 *  POSSIBILITY OF SUCH DAMAGE.                                                  *
 *                                                                               *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/swing/SwingRootMonitor.java,v 1.5 2015/11/20 15:09:26 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SwingRootMonitor.java,v $
 * Revision 1.5  2015/11/20 15:09:26  spr
 * Reformatting.
 *
 * Revision 1.4  2011-05-27 19:32:51  spr
 * Change copyrights.
 *
 * Revision 1.3  2008-11-12 13:46:51  spr
 * No change.
 *
 * Revision 1.2  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1.1.1  2003/03/17 19:38:07  spr
 * Initial version of the common code for various Brown projects.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class SwingRootMonitor extends WindowAdapter {

/****************************************************************************************/
/*											*/
/*	Private Storage 								*/
/*											*/
/****************************************************************************************/

private boolean 	exit_app;


/****************************************************************************************/
/*											*/
/*	Constructors									*/
/*											*/
/****************************************************************************************/

public SwingRootMonitor()
{
   exit_app = false;
}



public SwingRootMonitor(boolean ex)
{
   exit_app = ex;
}



/****************************************************************************************/
/*											*/
/*	Event Handlers									*/
/*											*/
/****************************************************************************************/

@Override public void windowClosing(WindowEvent e)
{
   Window w = e.getWindow();

   w.setVisible(false);

   w.dispose();

   if (exit_app) System.exit(0);
   else if (w instanceof SwingMonitoredWindow) {
      SwingMonitoredWindow smw = (SwingMonitoredWindow) w;
      smw.closeWindow();
    }
}


@Override public void windowClosed(WindowEvent e)
{
   Window w = e.getWindow();

   if (exit_app) System.exit(0);
   else if (w instanceof SwingMonitoredWindow) {
      SwingMonitoredWindow smw = (SwingMonitoredWindow) w;
      smw.closeWindow();
    }
}


}	// end of class SwingRootMonitor



/* end of SwingRootMonitor.java */
