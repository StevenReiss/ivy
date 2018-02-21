/********************************************************************************/
/*                                                                              */
/*              JcodeLocalVariable.java                                         */
/*                                                                              */
/*      External representation of a local variable                             */
/*                                                                              */
/********************************************************************************/



package edu.brown.cs.ivy.jcode;

import org.objectweb.asm.tree.LocalVariableNode;

public class JcodeLocalVariable implements JcodeConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private LocalVariableNode for_variable;
private JcodeDataType data_type;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JcodeLocalVariable(LocalVariableNode lvn,JcodeFactory fac)
{
   for_variable = lvn;
   data_type = fac.findJavaType(lvn.desc);
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public JcodeDataType getDataType()              { return data_type; }
public String getName()                         { return for_variable.name; }
public String getSignature()                    { return for_variable.signature; }

LocalVariableNode getLocal()                    { return for_variable; }



}       // end of class JcodeLocalVariable




/* end of JcodeLocalVariable.java */
