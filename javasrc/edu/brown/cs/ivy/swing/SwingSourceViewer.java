/********************************************************************************/
/*										*/
/*		SwingSourceViewer.java						*/
/*										*/
/*	Swing source code text viewer						*/
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


package edu.brown.cs.ivy.swing;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import java.awt.Color;
import java.awt.Font;
import java.io.FileReader;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SwingSourceViewer extends SwingTextPane implements SwingColors
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private String		source_file;
private transient StyledDocument source_doc;
private transient List<Integer> line_map;
private transient Style our_style;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SwingSourceViewer()
{
   source_file = null;
   setContentType("text/plain");
   setEditable(false);

   Font ft = new Font("Monospaced",Font.PLAIN,10);
   setFont(ft);

   line_map = null;

   Style s = getLogicalStyle();
   TabStop [] tbs = new TabStop[20];
   for (int i = 0; i < tbs.length; ++i) {
      tbs[i] = new TabStop(6*(i*8+8));
    }
   TabSet ts = new TabSet(tbs);
   StyleConstants.setTabSet(s,ts);
   setLogicalStyle(s);
   our_style = s;

   source_doc = getStyledDocument();

   Style ns = source_doc.addStyle("PRIMARY",our_style);
   StyleConstants.setForeground(ns,Color.BLUE);
   StyleConstants.setBackground(ns,Color.YELLOW);
   StyleConstants.setBold(ns,true);

   ns = source_doc.addStyle("SECONDARY",our_style);
   StyleConstants.setForeground(ns,Color.GREEN);
}




/********************************************************************************/
/*										*/
/*	File set methods							*/
/*										*/
/********************************************************************************/

public boolean openFile(String file)
{
   if (file == null && source_file == null) return false;
   else if (file != null && file.equals(source_file)) return true;
   else if (file != null) {
      String txt = "";
      setLogicalStyle(our_style);
      try {
	 StringBuffer sbuf = new StringBuffer();
	 FileReader fr = new FileReader(file);
         try {
            char [] buf = new char[4096];
            for ( ; ; ) {
               int ct = fr.read(buf);
               if (ct < 0) break;
               sbuf.append(buf,0,ct);
             }
            txt = sbuf.toString();
          }
         finally {
            fr.close();
          }
       }
      catch (IOException e) {
	 file = null;
       }
      setLogicalStyle(our_style);
      setText(txt);
      clearHighlights();
      source_file = file;
    }
   if (file == null) {
      source_file = null;
      setText("");
    }

   if (source_doc != getStyledDocument()) {
      System.err.println("DOCUMENT CHANGED");
    }

   setupDocument();

   return source_file != null;
}




/********************************************************************************/
/*										*/
/*	Compatability methods							*/
/*										*/
/********************************************************************************/

public void setTabSize(int sz)					{ }


public int getLineStartOffset(int ln) throws BadLocationException
{
   if (ln < 0 || ln > line_map.size()) throw new BadLocationException("Bad Line",ln);

   Integer ivl = line_map.get(ln);
   return ivl.intValue();
}



public int getLineEndOffset(int ln) throws BadLocationException
{
   if (ln < 0 || ln > line_map.size()) throw new BadLocationException("Bad Line",ln);

   if (ln == line_map.size()-1) {
      String s = getText();
      return s.length();
    }
   Integer ivl = line_map.get(ln+1);
   return ivl.intValue()-1;
}



/********************************************************************************/
/*										*/
/*	Additional access methods						*/
/*										*/
/********************************************************************************/

public int getLineAt(int pos)
{
   int v = Collections.binarySearch(line_map,Integer.valueOf(pos));
   if (v < 0) v = -v-2;
   return v;
}



public String getSourceFile()			{ return source_file; }



/********************************************************************************/
/*										*/
/*	Methods to setup the document						*/
/*										*/
/********************************************************************************/

private void setupDocument()
{
   setupLineMap();
}



private void setupLineMap()
{
   line_map = new ArrayList<Integer>();
   String s = getText();
   line_map.add(Integer.valueOf(0));
   CharacterIterator ci = new StringCharacterIterator(s);

   int delta = 0;
   for (char c = ci.first(); c != CharacterIterator.DONE; c = ci.next()) {
      if (c == '\n') {
	 line_map.add(Integer.valueOf(ci.getIndex()+1-delta));
       }
      else if (c == '\r') ++delta;
    }
}



/********************************************************************************/
/*										*/
/*	Methods to do highlighting						*/
/*										*/
/********************************************************************************/

public void clearHighlights()
{
   String s = getText();
   source_doc.setParagraphAttributes(0,s.length(),our_style,true);
   source_doc.setCharacterAttributes(0,s.length(),our_style,true);
}



public void addHighlight(int startline,int thruline,String nm) throws BadLocationException
{
   Style ns = source_doc.getStyle(nm);
   if (ns == null) return;

   int st = getLineStartOffset(startline);
   int en = getLineEndOffset(thruline);

   source_doc.setParagraphAttributes(st,en-st,ns,false);
   source_doc.setCharacterAttributes(st,en-st,ns,false);
}




/********************************************************************************/
/*										*/
/*	Default editor kit to prevent line wrapping				*/
/*										*/
/********************************************************************************/

@Override public EditorKit createDefaultEditorKit() {
   return new LocalEditorKit();
}


@Override public boolean getScrollableTracksViewportWidth()		{ return false; }



private static final class LocalEditorKit extends StyledEditorKit {

   private static final long serialVersionUID = 1;

   @Override public ViewFactory getViewFactory() {
      return new LocalViewFactory();
    }

}	// end of subclass LocalEditorKit


private static final class LocalViewFactory implements ViewFactory {

   @Override public View create(Element elem) {
      String kind = elem.getName();
      if (kind != null) {
         if (kind.equals(AbstractDocument.ContentElementName)) {
            return new LocalLabelView(elem);
          }
         else if (kind.equals(AbstractDocument.ParagraphElementName)) {
            return new ParagraphView(elem);
          }
         else if (kind.equals(AbstractDocument.SectionElementName)) {
            return new BoxView(elem, View.Y_AXIS);
          }
         else if (kind.equals(StyleConstants.ComponentElementName)) {
            return new ComponentView(elem);
          }
         else if (kind.equals(StyleConstants.IconElementName)) {
            return new IconView(elem);
          }
       }
      return new LabelView(elem);
    }

}	// end of subclass LocalViewFactory



private static class LocalLabelView extends LabelView {

   LocalLabelView(Element elem) {
      super(elem);
    }

   @Override public View breakView(int axis, int p0, float pos, float len) {
      return super.breakView(axis,p0,pos,999999f);
    }

}	// end of subclass LocalLabelView





}	// end of class SwingSourceViewer




/* end of SwingSourceViewer.java */
