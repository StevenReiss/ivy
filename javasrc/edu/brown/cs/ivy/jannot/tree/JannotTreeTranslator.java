/********************************************************************************/
/*                                                                              */
/*              JannotTreeTranslator.java                                       */
/*                                                                              */
/*      Internal tree visitor with children visits                              */
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



package edu.brown.cs.ivy.jannot.tree;

import java.util.List;
import java.util.ListIterator;


public class JannotTreeTranslator extends JannotTreeVisitor
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

protected JannotTree    result;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public JannotTreeTranslator()
{ }



/********************************************************************************/
/*                                                                              */
/*      Main entries                                                            */
/*                                                                              */
/********************************************************************************/

@SuppressWarnings("unchecked")
public <T extends JannotTree> T translate(T tree)
{
   if (tree == null) return null;
   tree.accept(this);
   JannotTree rslt = this.result;
   result = null;
   return (T) rslt;
}


public <T extends JannotTree> List<T> translate(List<T> trees)
{
   if (trees == null) return null;
   for (ListIterator<T> it = trees.listIterator(); it.hasNext(); ) {
      T t = it.next();
      T newt = translate(t);
      if (newt != t) it.set(newt);
    }
   return trees;
}


public List<JannotTreeJCVariableDecl> translateVarDefs(List<JannotTreeJCVariableDecl> trees)
{
   return translate(trees);
}

public List<JannotTreeJCTypeParameter> translateTypeParams(List<JannotTreeJCTypeParameter> trees)
{
   return translate(trees);
}

public List<JannotTreeJCCase> translateCases(List<JannotTreeJCCase> trees)
{
   return translate(trees);
}

public List<JannotTreeJCCatch> translateCatcheers(List<JannotTreeJCCatch> trees)
{
   return translate(trees);
}

public List<JannotTreeJCAnnotation> translateAnnotations(List<JannotTreeJCAnnotation> trees)
{
   return translate(trees);
}


/********************************************************************************/
/*                                                                              */
/*      Visitor methods                                                         */
/*                                                                              */
/********************************************************************************/

public void visitTree(JannotTree t)
{
   result = t.translate(this);
}























}       // end of class JannotTreeTranslator




/* end of JannotTreeTranslator.java */

