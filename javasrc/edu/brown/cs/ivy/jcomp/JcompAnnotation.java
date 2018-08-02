/********************************************************************************/
/*                                                                              */
/*              JcompAnnotation.java                                            */
/*                                                                              */
/*      Annotation information                                                  */
/*                                                                              */
/********************************************************************************/



package edu.brown.cs.ivy.jcomp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

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



public static List<JcompAnnotation> getAnnotations(List<?> modifiers)
{
   List<JcompAnnotation> rslt = null;
   for (Object o : modifiers) {
      IExtendedModifier iem = (IExtendedModifier) o;
      if (iem.isAnnotation()) {
         Annotation an = (Annotation) iem;
         JcompType jt = JcompAst.getJavaType(an.getTypeName());
         if (jt == null) continue;
         if (rslt == null) rslt = new ArrayList<>();
         JcompAnnotation jan = new JcompAnnotation(jt);
         rslt.add(jan);
         if (an.isNormalAnnotation()) {
            NormalAnnotation na = (NormalAnnotation) an;
            for (Object o1 : na.values()) {
               MemberValuePair mvp = (MemberValuePair) o1;
               String key = mvp.getName().getIdentifier();
               Object ev = JcompSymbol.getValue(mvp.getValue());
               jan.addValue(key,ev);
             }
          }
         else if (an.isSingleMemberAnnotation()) {
            SingleMemberAnnotation na = (SingleMemberAnnotation) an;
            Object ev = JcompSymbol.getValue(na.getValue());
            jan.addValue("value",ev);
          }
       }
    }
   return rslt;
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String toString()
{
   StringBuffer buf = new StringBuffer();
   buf.append("@");
   buf.append(annotation_type);
   if (annotation_values != null) {
      buf.append("(");
      for (Map.Entry<String,Object> ent : annotation_values.entrySet()) {
         buf.append(ent.getKey());
         buf.append("=");
         buf.append(ent.getValue());
         buf.append(",");
       }
    }
   return buf.toString();
}


}       // end of class JcompAnnotation




/* end of JcompAnnotation.java */
