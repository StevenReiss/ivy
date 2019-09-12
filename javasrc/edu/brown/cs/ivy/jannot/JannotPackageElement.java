/********************************************************************************/
/*                                                                              */
/*              JannotPackageElement.java                                       */
/*                                                                              */
/*      description of class                                                    */
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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

class JannotPackageElement extends JannotElement implements PackageElement
{



/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private static final String EMPTY_NAME = "$$EMPTY$$";
private static final String MISSING_NAME = "MISSING";


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JannotPackageElement(PackageDeclaration n) 
{
   super(n);
}

JannotPackageElement(CompilationUnit cu) 
{
   super(getCompUnitPackage(cu));
}



private static PackageDeclaration getCompUnitPackage(CompilationUnit cu)
{
   PackageDeclaration pd = cu.getPackage();
   if (pd != null) return pd;
   pd = cu.getAST().newPackageDeclaration();
   pd.setName(cu.getAST().newSimpleName(EMPTY_NAME));
   return pd;
   
}

/********************************************************************************/
/*                                                                              */
/*      Generic methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public ElementKind getKind() 
{
   return ElementKind.PACKAGE;
}

@Override public <R,P> R accept(ElementVisitor<R,P> v,P p)
{
   return v.visitPackage(this,p);
}



/********************************************************************************/
/*                                                                              */
/*      Package specific methods                                                */
/*                                                                              */
/********************************************************************************/

@Override public Name getSimpleName() 
{
   String s = getPackageName();
   if (s == null) return new JannotName("");
   int idx = s.lastIndexOf(".");
   if (idx > 0) s = s.substring(idx+1);
   return new JannotName(s);
}



@Override public Name getQualifiedName() 
{
   String s = getPackageName();
   if (s == null) return new JannotName("");
   return new JannotName(s);
}



@Override public boolean isUnnamed()
{
   String s = getPackageName();
   return s == null;
}



private String getPackageName()
{
   PackageDeclaration pk = (PackageDeclaration) ast_node;
   if (pk == null || pk.getName() == null) return null;
   String s = pk.getName().getFullyQualifiedName();
   if (s.equals(EMPTY_NAME) || s.equals(MISSING_NAME)) return null;
   return s;
}



/********************************************************************************/
/*                                                                              */
/*      Equality methods                                                        */
/*                                                                              */
/********************************************************************************/

@Override public boolean equals(Object o)
{
   if (o instanceof JannotPackageElement) {
      JannotPackageElement pe = (JannotPackageElement) o;
      String s1 = getPackageName();
      String s2 = pe.getPackageName();
      if (s1 == null) return s2 == null;
      else if (s2 == null) return false;
      return s1.equals(s2);
    }
   return false;
}



@Override public int hashCode()
{
   String s = getPackageName();
   if (s == null) return 0;
   return s.hashCode();
}


}       // end of class JannotPackageElement




/* end of JannotPackageElement.java */

