/********************************************************************************/
/*										*/
/*		PetalTest.java							*/
/*										*/
/*	Test driver for the Petal Graphics editor				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalTest.java,v 1.8 2018/08/02 15:10:37 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalTest.java,v $
 * Revision 1.8  2018/08/02 15:10:37  spr
 * Fix imports.
 *
 * Revision 1.7  2015/11/20 15:09:24  spr
 * Reformatting.
 *
 * Revision 1.6  2011-05-27 19:32:49  spr
 * Change copyrights.
 *
 * Revision 1.5  2006-12-01 03:22:54  spr
 * Clean up eclipse warnings.
 *
 * Revision 1.4  2005/05/07 22:25:43  spr
 * Updates for java 5.0
 *
 * Revision 1.3  2004/05/20 16:03:37  spr
 * Bug fixes for Petal related to CHIA; add oval helper.
 *
 * Revision 1.2  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1  2003/07/16 19:44:59  spr
 * Move petal from bloom to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.petal;

import edu.brown.cs.ivy.swing.SwingMenuBar;
import edu.brown.cs.ivy.swing.SwingRootMonitor;
import edu.brown.cs.ivy.swing.SwingSetup;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;




public class PetalTest extends JFrame implements PetalConstants
{

/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   new SwingSetup();

   PetalTest pt = new PetalTest();
   pt.addWindowListener(new SwingRootMonitor(true));
   pt.setVisible(true);

   pt.test1();
}


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

public PetalEditor	 petal_editor;
public PetalModelDefault petal_model;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private PetalTest()
{
   super("Petal Editor Test");

   setSize(500,500);
   setJMenuBar(new MenuBar());

   JPanel pnl = new JPanel();
   GridLayout glay = new GridLayout(1,1);
   pnl.setLayout(glay);

   petal_model = new PetalModelDefault();
   petal_editor = new PetalEditor(petal_model);

   pnl.add(petal_editor);
   setContentPane(new JScrollPane(pnl));
}



/********************************************************************************/
/*										*/
/*	Actual test programs							*/
/*										*/
/********************************************************************************/

private void test1()
{
   petal_editor.setGridSize(10);

   PetalNode n1 = addNode("Node 1",100,100);
   PetalNode n2 = addNode("Node 2",100,200);
   PetalNode n3 = addNode("Node 3",200,100);
   PetalNode n4 = addNode("Node 4",200,200);

   petal_editor.update();

   PetalArcDefault a12 = addArc(n1,n2);
   PetalArcDefault a13 = addArc(n1,n3);
   addArc(n2,n4);
   addArc(n3,n4);
   addArc(n2,n3);

   petal_editor.update();

   addLink("Arc12",a12);
   a13.setSourceEnd(new PetalArcEndDefault(0x1000));
   a13.setTargetEnd(new PetalArcEndDefault(0x1001));

   petal_editor.update();
}



private PetalNode addNode(String id,int x,int y)
{
   PetalNode nd = new PetalNodeDefault(id);

   petal_model.addNode(nd);
   nd.getComponent().setLocation(new Point(x,y));

   return nd;
}



private PetalArcDefault addArc(PetalNode n1,PetalNode n2)
{
   PetalArcDefault pa = new PetalArcDefault(n1,n2);

   petal_model.addArc(pa);

   return pa;
}




private PetalNode addLink(String id,PetalArc a)
{
   PetalNodeDefault nd = new PetalNodeDefault(id);
   PetalLink ln = new PetalLinkDefault(0.5,0,0,0,0);

   petal_model.addNode(nd);

   nd.setLink(ln,a);

   return nd;
}



/********************************************************************************/
/*										*/
/*	Menu bar for the test program						*/
/*										*/
/********************************************************************************/


private class MenuBar extends SwingMenuBar implements ActionListener {

   private static final long serialVersionUID = 1;


   MenuBar() {
      setDoubleBuffered(true);

      JMenu filemenu = new JMenu("File");
      JMenu editmenu = new JMenu("Edit");
      JMenu viewmenu = new JMenu("View");
      JMenu selectmenu = new JMenu("Select");

      addButton(filemenu,"Quit","Quit the project manager");
      super.add(filemenu);

      addButton(editmenu,"Undo","Undo previous command(s)");
      addButton(editmenu,"Redo","Redo undone command(s)");
      super.add(editmenu);

      addButton(selectmenu,"Select All","Select all nodes and arcs");
      addButton(selectmenu,"Clear Selections","Clear all selections");
      super.add(selectmenu);

      super.add(viewmenu);
    }


   @Override public void actionPerformed(ActionEvent e) {
      String btn = e.getActionCommand();
      if (btn.equals("Quit")) {
	 System.exit(0);
       }
      else if (btn.equals("Undo")) {
	 petal_editor.commandUndo();
       }
      else if (btn.equals("Redo")) {
	 petal_editor.commandRedo();
       }
      else if (btn.equals("Select All")) {
	 petal_editor.commandSelectAll();
       }
      else if (btn.equals("Clear Selections")) {
	 petal_editor.commandDeselectAll();
       }
    }

}	// end of subclass MenuBar



}	// end of class PetalTest



/* end of PetalTest.java */




