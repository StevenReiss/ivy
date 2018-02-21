/********************************************************************************/
/*                                                                              */
/*              JcodeAnnontation.java                                           */
/*                                                                              */
/*      External representation of an annotation                                */
/*                                                                              */
/********************************************************************************/



package edu.brown.cs.ivy.jcode;

import java.util.HashMap;
import java.util.Map;


import org.objectweb.asm.tree.AnnotationNode;

public class JcodeAnnotation implements JcodeConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Stroage                                                         */
/*                                                                              */
/********************************************************************************/

private AnnotationNode  for_annotation;
private JcodeDataType   data_type;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JcodeAnnotation(AnnotationNode tn,JcodeFactory fac)
{
   for_annotation = tn;
   data_type = fac.findJavaType(tn.desc);
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public String getDescription()                  { return data_type.getName(); }

public Map<String,Object> getValues()
{
   if (for_annotation.values == null || for_annotation.values.size() == 0) return null;
   
   Map<String,Object> rslt = new HashMap<>();
   for (int i = 0; i < for_annotation.values.size(); i += 2) {
      String key = (String) for_annotation.values.get(i);
      rslt.put(key,for_annotation.values.get(i+1));
    }
   
   return rslt;
}




}       // end of class JcodeAnnontation




/* end of JcodeAnnontation.java */
