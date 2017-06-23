/********************************************************************************/
/*                                                                              */
/*              JcompAstCleaner.java                                            */
/*                                                                              */
/*      description of class                                                    */
/*                                                                              */
/*      Written by spr                                                          */
/*                                                                              */
/********************************************************************************/



package edu.brown.cs.ivy.jcomp;

import org.eclipse.jdt.core.dom.ASTNode;

public interface JcompAstCleaner extends JcompSource
{


ASTNode cleanupAst(ASTNode orig);


}       // end of interface JcompAstUpdater




/* end of JcompAstCleaner.java */
