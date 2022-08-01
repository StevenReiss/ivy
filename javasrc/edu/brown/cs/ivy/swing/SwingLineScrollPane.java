/********************************************************************************/
/*                                                                              */
/*              SwingLineScrollPane.java                                        */
/*                                                                              */
/*      Scroll Pane for a text component  with line numbers                     */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.ivy.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

public class SwingLineScrollPane extends JScrollPane
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private LineNumbersView         line_view;

static final int MARGIN_WIDTH_PX = 35;

static final Color CURRENT_COLOR = Color.RED;

private static final long serialVersionUID = 1;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public SwingLineScrollPane(JTextComponent tc)
{
   this(tc,1,0);
}

public SwingLineScrollPane(JTextComponent tc,int line)
{
   this(tc,line,0);
}


public SwingLineScrollPane(JTextComponent tc,int line,int wd)
{
   super(tc);
   
   if (line > 0) {
      line_view = new LineNumbersView(tc,line,wd);
      setRowHeaderView(line_view);
    }
   else {
      line_view = null;
    }
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

void setLineColors(Color bkg,Color fg,Color sel)
{
   if (line_view == null) return;
   
   line_view.setColors(bkg,fg,sel);
}


void setMarginWidth(int w)
{
   if (line_view == null || w <= 0) return;
   
   line_view.setMarginWidth(w);
}



/********************************************************************************/
/*                                                                              */
/*      Line view component                                                     */
/*                                                                              */
/********************************************************************************/

private class LineNumbersView extends JComponent implements DocumentListener, CaretListener, ComponentListener {

   private static final long serialVersionUID = 1;
   
   private JTextComponent line_editor;
   private Font line_font;
   private int start_line;
   private int margin_width;
   private Color current_color;
   
   LineNumbersView(JTextComponent ed,int ln,int wd) {
      line_editor = ed;
      line_font = null;
      start_line = ln;
      if (wd <= 0) wd = MARGIN_WIDTH_PX;
      margin_width = wd;
      ed.getDocument().addDocumentListener(this);
      ed.addComponentListener(this);
      ed.addCaretListener(this);
      current_color = CURRENT_COLOR;
    }
   
   void setMarginWidth(int w)                   { margin_width = w; }
   
   void setColors(Color bg,Color fg,Color sel) {
      if (bg != null) setBackground(bg);
      if (fg != null) setForeground(fg);
      if (sel == null) current_color = getForeground();
      else current_color = sel;
    }
   
   
   @Override public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Color fg = g.getColor();
      
      Rectangle clip = g.getClipBounds();
      int startoffset = line_editor.viewToModel2D(new Point(0,clip.y));
      int endoffset = line_editor.viewToModel2D(new Point(0,clip.y + clip.height));
      
      while (startoffset <= endoffset) {
         try {
            String lno = getLineNumber(startoffset);
            if (lno != null) {
               int x = getInsets().left + 2;
               int y = getOffsetY(startoffset);
               if (line_font == null) {
                  line_font = new Font(Font.MONOSPACED, Font.BOLD, 
                        line_editor.getFont().getSize());
                }
               g.setFont(line_font);
               g.setColor(isCurrentLine(startoffset) ? current_color : fg);
               g.drawString(lno,x,y);
             }
            startoffset = Utilities.getRowEnd(line_editor,startoffset) + 1;
          }
         catch (BadLocationException e) { }
       }
    }
   
   private String getLineNumber(int offset) {
      javax.swing.text.Element root = line_editor.getDocument().getDefaultRootElement();
      int idx = root.getElementIndex(offset);
      javax.swing.text.Element line = root.getElement(idx);
      if (line.getStartOffset() != offset) return null;
      return String.format("%4d ",idx + start_line);
    }
   
   private int getOffsetY(int offset) throws BadLocationException {
      FontMetrics fm = line_editor.getFontMetrics(line_editor.getFont());
      int d = fm.getDescent();
      Rectangle r = line_editor.modelToView2D(offset).getBounds();
      int y = r.y + r.height - d;
      return y;
    }
   
   private boolean isCurrentLine(int offset) {
      int cp = line_editor.getCaretPosition();
      javax.swing.text.Element root = line_editor.getDocument().getDefaultRootElement();
      return root.getElementIndex(offset) == root.getElementIndex(cp);
    }
   
   private void documentChanged() {
      SwingUtilities.invokeLater( () -> { repaint(); } );
    }
   
   private void updateSize() {
      Dimension sz = new Dimension(margin_width,line_editor.getHeight());
      setPreferredSize(sz);
      setSize(sz);
      documentChanged();
    }
   
   @Override public void insertUpdate(DocumentEvent e)          { documentChanged(); }
   @Override public void removeUpdate(DocumentEvent e)          { documentChanged(); }
   @Override public void changedUpdate(DocumentEvent e)         { documentChanged(); }
   @Override public void caretUpdate(CaretEvent e)              { documentChanged(); }
   @Override public void componentResized(ComponentEvent e)     { updateSize(); }
   @Override public void componentShown(ComponentEvent e)       { updateSize(); }
   @Override public void componentMoved(ComponentEvent e)       { }
   @Override public void componentHidden(ComponentEvent e)      { }

}       // end of inner class LineNumbersView


}       // end of class SwingLineScrollPane




/* end of SwingLineScrollPane.java */

