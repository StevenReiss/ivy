/********************************************************************************/
/*                                                                              */
/*              JannotModuleElement.java                                        */
/*                                                                              */
/*      Module element for annotation analysis                                  */
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



package edu.brown.cs.ivy.jannot;

import java.util.List;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;

import org.eclipse.jdt.core.dom.ASTNode;

class JannotModuleElement extends JannotElement implements ModuleElement
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotModuleElement(ASTNode n) 
{
   super(n);
}



/********************************************************************************/
/*                                                                              */
/*      Generic methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public ElementKind getKind() 
{
   return ElementKind.MODULE;
}

@Override public <R,P> R accept(ElementVisitor<R,P> v,P p) 
{
   return v.visitModule(this,p);
}



/********************************************************************************/
/*                                                                              */
/*      Module-specifc methods                                                  */
/*                                                                              */
/********************************************************************************/

@Override public List<? extends ModuleElement.Directive> getDirectives()
{
   return null;
}

@Override public Name getQualifiedName() 
{
   return null;
}

@Override public Name getSimpleName() 
{
   return null;
}

@Override public boolean isOpen() 
{
   return false;
}

@Override public boolean isUnnamed() 
{
   return true;
}



}       // end of class JannotModuleElement




/* end of JannotModuleElement.java */

