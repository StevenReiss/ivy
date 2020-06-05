/********************************************************************************/
/*										*/
/*		SwingHelpFrame.java						*/
/*										*/
/*	Implementation for a generic Swing help frame				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss			*/
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

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;



public abstract class SwingHelpFrame extends JFrame implements SwingColors, ActionListener
{


/************************************************************************/
/*									*/
/*	Private Storage 						*/
/*									*/
/************************************************************************/

private static String		  help_extension;
private static String		  help_prefix;
private File			  help_dir;
private String []		  help_files;

protected JTabbedPane		  tab_area;

protected JEditorPane		  index_pane;
protected JScrollPane		  index_view;
protected JList<HelpInfo>	  index_list;

protected JEditorPane		  glossary_pane;
protected JScrollPane		  glossary_view;
protected JList<HelpInfo>	  glossary_list;

protected JTree 		  help_tree;
protected JScrollPane		  tree_view;
private SwingHelpFrameEditorPane  help_pane;
private JScrollPane		  help_view;
protected JSplitPane		  split_pane;

private JButton 		  back_button;
private JButton 		  forward_button;
private JButton 		  home_button;
private JButton 		  ok_button;

protected Vector<URL>		  prev_page;
protected Vector<URL>		  next_page;
protected URL			  default_page;

private static final long serialVersionUID = 1L;



/************************************************************************/
/*									*/
/*	Access Methods							*/
/*									*/
/************************************************************************/

public Vector<URL> getPreviousPages()
{
   return prev_page;
}

public Vector<URL> getNextPages()
{
   return next_page;
}




/************************************************************************/
/*									*/
/*	Constructors							*/
/*									*/
/************************************************************************/


public SwingHelpFrame(String title, String dir, String ext, String defau)
{
   super(title);

   help_extension = ext;
   help_dir = new File(dir);

   help_prefix = "file:" + help_dir + File.separator;
   try {
      default_page = new URL(help_prefix + defau);
    }
   catch (java.net.MalformedURLException exc) {
      System.err.println("Attempted to create a "
			    + "bad URL: " + help_prefix + defau);
    }

   prev_page = new Vector<URL>();
   next_page = new Vector<URL>();

   JRootPane jrp = getRootPane();
   jrp.setDoubleBuffered(true);

   loadHelpFiles();

   help_pane = new SwingHelpFrameEditorPane(this);
   help_pane.setEditable(false);
   help_pane.addHyperlinkListener(new Hyperactive());
   help_view = new JScrollPane(help_pane);

   home_button = new JButton("Home");
   home_button.addActionListener(this);
   home_button.setActionCommand("HOME");
   home_button.setToolTipText("Return to contents page.");

   back_button = new JButton("Back");
   back_button.addActionListener(this);
   back_button.setActionCommand("BACK");
   back_button.setToolTipText("Return to previous page.");

   forward_button = new JButton("Forward");
   forward_button.addActionListener(this);
   forward_button.setActionCommand("FORWARD");
   forward_button.setToolTipText("Go to next page.");

   enableButtons();

   Box b = Box.createHorizontalBox();
   b.add(Box.createHorizontalGlue());
   b.add(home_button);
   b.add(back_button);
   b.add(forward_button);
   b.add(Box.createHorizontalGlue());

   help_view.setColumnHeaderView(b);

   index_pane = new SwingEditorPane();
   index_pane.setEditable(false);
   index_view = new JScrollPane(index_pane);

   glossary_pane = new SwingEditorPane();
   glossary_pane.setEditable(false);
   glossary_view = new JScrollPane(glossary_pane);

   split_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
   split_pane.setRightComponent(help_view);

   tab_area = new JTabbedPane(SwingConstants.TOP);
   tab_area.addTab("Index", null, index_view, "Mural Help Index");
   tab_area.addTab("Glossary", null, glossary_view, "Glossary of Mural Terms");
   split_pane.setLeftComponent(tab_area);

   getContentPane().add(split_pane, BorderLayout.CENTER);

   JSeparator sep1 = new JSeparator();
   getContentPane().add(sep1,BorderLayout.SOUTH);

   Box b1 = Box.createHorizontalBox();
   ok_button = new JButton("OK");
   ok_button.addActionListener(this);
   ok_button.setActionCommand("OK");
   ok_button.setToolTipText("Close Mural Help.");
   b1.add(Box.createHorizontalGlue());
   b1.add(ok_button);
   b1.add(Box.createHorizontalGlue());
   getContentPane().add(b1,BorderLayout.SOUTH);
}




/************************************************************************/
/*									*/
/*	Processing methods						*/
/*									*/
/************************************************************************/

public void process()
{
   pack();
   setVisible(true);
}




public void enableButtons()
{
   if (prev_page.isEmpty()) {
      back_button.setEnabled(false);
    }
   else {
      back_button.setEnabled(true);
    }
   if (next_page.isEmpty()) {
      forward_button.setEnabled(false);
    }
   else {
      forward_button.setEnabled(true);
    }
}




/********************************************************************************/
/*										*/
/*	Help Pane methods							*/
/*										*/
/********************************************************************************/

private void loadHelpFiles()
{
   if (help_dir.isDirectory()) {
      String [] hfiles = help_dir.list(new HelpFileFilter());
      help_files = new String[hfiles.length];
      for (int i = 0; i < hfiles.length; ++i) {
	 help_files[i] = help_prefix + hfiles[i];
       }
    }
}

protected void initHelp(String name)
{
   for (int i = 0; i < help_files.length; ++i) {
      if (help_files[i].endsWith(name)) {
	 String s = help_files[i];
	 try {
	    URL help_url = new URL(s);
	    displayFile(help_url);
	  }
	 catch (Exception e) {
	    System.err.println("Couldn't create help URL: " + s);
	  }
       }
    }
}

protected void displayFile(URL url)
{
   try {
      help_pane.setPage(url);
    }
   catch (IOException e) {
      System.err.println("Attempted to read a bad URL: " + url);
    }
}


/********************************************************************************/
/*										*/
/*	Tree building methods							*/
/*										*/
/********************************************************************************/

protected abstract void createTree();

protected void setupTree(JTree tree)
{
   tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

   tree.addTreeSelectionListener(new SelectionListener());
}



private class SelectionListener implements TreeSelectionListener {

   @Override public void valueChanged(TreeSelectionEvent e) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
	 help_tree.getLastSelectedPathComponent();

      if (node == null) return;

      Object nodeInfo = node.getUserObject();
      if (nodeInfo instanceof HelpInfo) {
	 HelpInfo help = (HelpInfo)nodeInfo;
	 displayFile(help.fileName);
       }
    }

}	// end of subclass SelectionListener



/********************************************************************************/
/*										*/
/*	Glossary building methods						*/
/*										*/
/********************************************************************************/

protected abstract void createGlossary();



/********************************************************************************/
/*										*/
/*	Index building methods							*/
/*										*/
/********************************************************************************/

protected abstract void createIndex();




/********************************************************************************/
/*										*/
/*	Subclass for Help Objects that store URL's	                             */
/*										*/
/********************************************************************************/

protected class HelpInfo {

   public String helpName;
   public URL fileName;

   public HelpInfo(String help, String file) {
      helpName = help;
      try {
	 fileName = new URL(help_prefix + file);
       }
      catch (java.net.MalformedURLException exc) {
	 System.err.println("Attempted to create a HelpInfo "
			       + "with a bad URL: " + fileName);
	 fileName = null;
       }
    }

   @Override public String toString() {
      return helpName;
    }

}	// end of subclass HelpInfo




/************************************************************************/
/*									*/
/*	Callback methods						*/
/*									*/
/************************************************************************/

@Override public void actionPerformed(ActionEvent e)
{
   String cmd = e.getActionCommand();

   if (cmd.equals("OK")) {
      setVisible(false);
    }
   else if (cmd.equals("BACK")) {
      next_page.add(help_pane.getPage());
      URL url = prev_page.remove(prev_page.size()-1);
      try {
	 help_pane.setPage(url);
       }
      catch (Throwable t) {
	 t.printStackTrace();
       }
      enableButtons();
    }
   else if (cmd.equals("FORWARD")) {
      prev_page.add(help_pane.getPage());
      URL url = next_page.remove(next_page.size()-1);
      try {
	 help_pane.setPage(url);
       } catch (Throwable t) {
	    t.printStackTrace();
	  }
	 enableButtons();
    }
   else if (cmd.equals("HOME")) {
      if (prev_page.isEmpty()) {
	 prev_page.add(help_pane.getPage());
       }
      else {
	 URL url = help_pane.getPage();
	 URL url2 = prev_page.get(prev_page.size()-1);
	 if (! url.equals(url2)) { prev_page.add(url); }
       }
      try {
	 help_pane.setPage(default_page);
       }
      catch (Throwable t) {
	 t.printStackTrace();
       }
      enableButtons();
    }
}




/********************************************************************************/
/*										*/
/*	Subclass for filtering files						*/
/*										*/
/********************************************************************************/

static class HelpFileFilter implements FilenameFilter {

   @Override public boolean accept(File dir,String name) {
      return name.endsWith(help_extension);
    }

}	// end of subclass HelpFileFilter


/********************************************************************************/
/*										*/
/*	Extension of JEditorPane to allow access to SwingHelpFrame		*/
/*										*/
/********************************************************************************/

class SwingHelpFrameEditorPane extends SwingEditorPane {

   private SwingHelpFrame help_frame;
   private static final long serialVersionUID = 1;

   public SwingHelpFrameEditorPane(SwingHelpFrame frame) {
      help_frame = frame;
    }

   public SwingHelpFrame getHelpFrame() { return help_frame; }

}	// end of subclass SwingHelpFrameEditorPane




/********************************************************************************/
/*										*/
/*	Class to listen for Hyperlink Events					*/
/*										*/
/********************************************************************************/

class Hyperactive implements HyperlinkListener {

   @Override public void hyperlinkUpdate(HyperlinkEvent e) {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	 SwingHelpFrameEditorPane pane = (SwingHelpFrameEditorPane) e.getSource();
	 SwingHelpFrame help_frame = pane.getHelpFrame();
	 Vector<URL> prevpage = help_frame.getPreviousPages();
	 if (prevpage.isEmpty()) {
	    prevpage.add(pane.getPage());
	  }
	 else {
	    URL url = pane.getPage();
	    URL url2 = prevpage.get(prevpage.size()-1);
	    if (! url.equals(url2)) { prevpage.add(url); }
	  }
	 help_frame.enableButtons();
	 if (e instanceof HTMLFrameHyperlinkEvent) {
	    HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
	    HTMLDocument doc = (HTMLDocument)pane.getDocument();
	    doc.processHTMLFrameHyperlinkEvent(evt);
	  }
	 else {
	    try {
	       pane.setPage(e.getURL());
	     }
	    catch (Throwable t) {
	       t.printStackTrace();
	     }
	  }
       }
    }

}	// end of subclass Hyperactive



/********************************************************************************/
/*										*/
/*	Mouser class used by instances						*/
/*										*/
/********************************************************************************/

public class Mouser extends MouseAdapter {

   public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
	 HelpInfo item = glossary_list.getSelectedValue();
	 if (item != null) displayFile(item.fileName);
       }
    }

}	// end of  inner class Mouser

}	// end of class SwingHelpFrame





/* end of SwingHelpFrame.java */
