/********************************************************************************/
/*                                                                              */
/*              JcompAnnotation.java                                            */
/*                                                                              */
/*      Annotation information                                                  */
/*                                                                              */
/********************************************************************************/



package edu.brown.cs.ivy.jcomp;

import java.util.HashMap;
import java.util.Map;

public class JcompAnnotation implements JcompConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private JcompType       annotation_type;
private Map<String,Object> annotation_values;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JcompAnnotation(JcompType typ)
{
   annotation_type = typ;
   annotation_values = null;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public JcompType getAnnotationType()            { return annotation_type; }

public Map<String,Object> getValues()           { return annotation_values; }



/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

void addValue(String name,Object v)
{
   if (annotation_values == null) annotation_values = new HashMap<>();
   annotation_values.put(name,v);
}



}       // end of class JcompAnnotation




/* end of JcompAnnotation.java */
