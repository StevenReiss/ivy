/********************************************************************************/
/*										*/
/*		JcompContextCode.java						*/
/*										*/
/*	Context based on JCODE factory						*/
/*										*/
/********************************************************************************/

package edu.brown.cs.ivy.jcomp;

import edu.brown.cs.ivy.jcode.JcodeClass;
import edu.brown.cs.ivy.jcode.JcodeDataType;
import edu.brown.cs.ivy.jcode.JcodeFactory;
import edu.brown.cs.ivy.jcode.JcodeField;
import edu.brown.cs.ivy.jcode.JcodeMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class JcompContextCode extends JcompContext implements JcompConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcodeFactory	jcode_control;
private Map<JcodeClass,JcompType> type_map;
private Map<JcodeClass,Set<JcompScope>> all_defined;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompContextCode(JcodeFactory jf)
{
   super(null);

   jcode_control = jf;
   type_map = new HashMap<>();
   all_defined = new HashMap<>();
}



/********************************************************************************/
/*										*/
/*	Definition methods							*/
/*										*/
/********************************************************************************/

@Override JcompType defineKnownType(JcompTyper typer,String name)
{
   if (name == null) return null;

   JcodeClass jc = jcode_control.findKnownClass(name);
   if (jc == null) {
      if (parent_context != null) return parent_context.defineKnownType(typer,name);
      return null;
    }

   JcompType jt = getJcompType(typer,jc);

   return jt;
}


@Override JcompSymbol defineKnownField(JcompTyper typer,String cls,String id,JcompType orig)
{
   JcodeField jf = jcode_control.findField(null,cls,id);
   if (jf == null) return null;

   return createField(typer,jf,orig);
}



@Override JcompSymbol defineKnownMethod(JcompTyper typer,String cls,String id,
      JcompType argtype,JcompType ctyp)
{
   String desc = "(";
   for (JcompType jt : argtype.getComponents()) {
      desc += jt.getJavaTypeName();
    }
   desc += ")";
   if (argtype.getBaseType() != null) {
      desc += argtype.getBaseType().getJavaTypeName();
    }

   JcodeDataType jdt = jcode_control.findNamedType(cls);
   List<JcodeMethod> mthds = new ArrayList<JcodeMethod>();
   addCompatibleMethods(jdt,id,desc,mthds);
   JcodeMethod best = null;
   int bestscore = 0;
   for (JcodeMethod jm1 : mthds) {
      int score = compatiblityScore(typer,argtype,jm1);
      if (score < 0) continue;
      if (best == null || score < bestscore) {
	 best = jm1;
	 bestscore = score;
       }
    }

   // JcodeMethod best = jcode_control.findMethod(null,cls,id,desc);
   if (best == null) return null;

   return createMethod(typer,best);
}


private void addCompatibleMethods(JcodeDataType jdt,String id,String desc,
      List<JcodeMethod> rslt)
{
   rslt.addAll(jcode_control.findAllMethods(jdt,id,desc));
   if (jdt.getSuperType() != null) {
      addCompatibleMethods(jdt.getSuperType(),id,desc,rslt);
    }
   for (JcodeDataType ityp : jdt.getInterfaces()) {
      addCompatibleMethods(ityp,id,desc,rslt);
    }
}


private int compatiblityScore(JcompTyper typer,JcompType argtyp,JcodeMethod jm)
{
   JcompType [] margs = new JcompType[jm.getNumArguments()];
   for (int i = 0; i < margs.length; ++i) {
      margs[i] = JcompControl.convertType(typer,jm.getArgType(i));
    }

   boolean init = jm.getName().equals("<init>");
   init &= jm.getDeclaringClass().isStatic();
   String cnm = jm.getDeclaringClass().getName();
   int idx = cnm.lastIndexOf("$");
   int idx1 = cnm.lastIndexOf(".");
   init &= idx > 0 && idx > idx1;

   return compatiblityScore(argtyp,margs,jm.isVarArgs(),init);
}


/********************************************************************************/
/*										*/
/*	Find known elements for a class 					*/
/*										*/
/********************************************************************************/

@Override List<JcompSymbol> defineKnownStatics(JcompTyper typer,String cls,String id,
						  JcompType ctyp)
{
   JcodeDataType jdt = jcode_control.findJavaType(cls);
   Iterable<JcodeMethod> mthds = jcode_control.findAllMethods(jdt,id,null);
   List<JcompSymbol> rslt = new ArrayList<>();
   if (mthds != null) {
      for (JcodeMethod jm : mthds) {
	 JcompSymbol js = createMethod(typer,jm);
	 if (js.isStatic()) rslt.add(js);
       }
    }
   if (id == null) {
      Iterable<JcodeField> flds = jcode_control.findAllFields(jdt,id);
      if (flds != null) {
	 for (JcodeField jcf : flds) {
	    if (jcf.isStatic()) {
	       JcompSymbol js = createField(typer,jcf,null);
	       rslt.add(js);
	     }
	  }
       }
    }
   else {
      JcompSymbol js = defineKnownField(typer,cls,id,null);
      if (js != null && js.isStatic()) rslt.add(js);
    }

   if (rslt.isEmpty()) return null;

   return rslt;
}



@Override Set<JcompSymbol> defineKnownAbstracts(JcompTyper typer,String cls)
{
   JcodeDataType jdt = jcode_control.findJavaType(cls);
   Iterable<JcodeMethod> mthds = jcode_control.findAllMethods(jdt,null,null);
   Set<JcompSymbol> rslt = new HashSet<>();
   if (mthds != null) {
      for (JcodeMethod jm : mthds) {
	 JcompSymbol js = createMethod(typer,jm);
	 if (js.isAbstract()) rslt.add(js);
       }
    }

   return rslt;
}



@Override List<JcompSymbol> findKnownMethods(JcompTyper typer,String cls)
{
   List<JcompSymbol> rslt = new ArrayList<>();

   JcodeClass jc = jcode_control.findClass(cls);
   for (JcodeMethod jm : jc.findAllMethods(null,null)) {
      JcompSymbol js = createMethod(typer,jm);
      if (js != null) rslt.add(js);
    }

   return rslt;
}




@Override synchronized void defineAll(JcompTyper typer,String cls,JcompScope scp)
{
   JcodeClass jc = jcode_control.findClass(cls);

   Set<JcompScope> defd = all_defined.get(jc);
   if (defd == null) {
      defd = new HashSet<>();
      all_defined.put(jc,defd);
    }
   if (!defd.add(scp)) return;
   
   for (JcodeField jf : jc.findAllFields(null)) {
      JcompSymbol js = createField(typer,jf,null);
      if (scp.lookupVariable(jf.getName()) == null) {
	 scp.defineVar(js);
       }
      else {
	 scp.defineDupVar(js);
       }
    }
   for (JcodeMethod jm : jc.findAllMethods(null,null)) {
      JcompType atyp = getMethodType(typer,jm,null);
      if (scp.lookupExactMethod(jm.getName(),atyp) == null) {
	 JcompSymbol js = createMethod(typer,jm);
	 scp.defineMethod(js);
       }
    }
   for (JcodeClass pc : jc.getParents()) {
      defineAll(typer,pc.getName(),scp);
    }
}




/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

private synchronized JcompType getJcompType(JcompTyper typer,JcodeClass jc)
{
   if (jc == null) return null;
   JcompType jt = type_map.get(jc);
   if (jt == null) {
      String jnm = jc.getName();
      jnm = jnm.replace("/",".");
      jnm = jnm.replace("$",".");
      if (jc.isInterface()) {
	 jt = JcompType.createBinaryInterfaceType(jnm,jc.signature);
       }
      else if (jc.isAnnotation()) {
	 jt = JcompType.createBinaryAnnotationType(jnm,jc.signature);
	 if (jc.isAbstract()) jt.setAbstract(true);
       }
      else if (jc.isEnum()) {
	 jt = JcompType.createBinaryEnumType(jnm,jc.signature);
	 if (jc.isAbstract()) jt.setAbstract(true);
       }
      else {
	 jt = JcompType.createBinaryClassType(jnm,jc.signature);
	 if (jc.isAbstract()) jt.setAbstract(true);
       }
      String xnm = jc.getName();
      int idx = xnm.lastIndexOf("/");
      if (idx < 0) idx = 0;
      int idx1 = xnm.indexOf("$");
      if (idx1 > 0) {
	 String ojtnm = xnm.substring(0,idx1);
	 JcompType oty = getAsmTypeName(typer,ojtnm);
	 if (oty != null)
	    jt.setOuterType(oty);
	 if (!jc.isStatic() && oty != null && !oty.isInterfaceType()) {
	    jt.setInnerNonStatic(true);
	  }
       }
      jt.setContextType(false);

      // first set up type using non-generic values
      if (jc.superName != null) {
	 JcompType sty = getAsmTypeName(typer,jc.superName);
	 if (sty == null) {
	    System.err.println("SUPER TYPE IS UNKNOWN IN CODE: " + jc.superName);
	  }
	 if (sty != null) jt.setSuperType(sty);
       }
      if (jc.interfaces != null) {
	 for (String inm : jc.interfaces) {
	    JcompType ijt = getAsmTypeName(typer,inm);
	    if (ijt != null) jt.addInterface(ijt);
	  }
       }
      // then define the type in case of recursion in generic definitions
      jt = typer.fixJavaType(jt);
      jt.setDefinition(JcompSymbol.createSymbol(jt,jc.getModifiers()));
      type_map.put(jc,jt);

      // finally handle generic specialization
      if (jt.getSignature() != null && jt.getSignature().contains("<")) {
	 // System.err.println("CHECK DERIVE " + jt + " " + jt.getSignature());
	 Map<String,JcompType> outermap = computeOutermap(jt);
	 // compute the outermap here by looking at outer type...
	 JcompType sty = JcompGenerics.deriveSupertype(typer,jt,outermap);
	 if (sty != null) jt.setSuperType(sty);
	 Collection<JcompType> njt = jt.getInterfaces();
	 if (njt != null) {
	    Collection<JcompType> lty1 = null;
	    lty1 = JcompGenerics.deriveInterfaceTypes(typer,jt,outermap);
	    if (lty1 != null && njt != lty1) {
	       njt.clear();
	       for (JcompType ijt : lty1) {
		  jt.addInterface(ijt);
		}
	     }
	  }
       }
    }

   JcompType jt1 = typer.fixJavaType(jt);
   if (jt1 != jt) {
      type_map.put(jc,jt1);
    }
   return jt1;
}



private Map<String,JcompType> computeOutermap(JcompType jt)
{
   JcompType oty = jt.getOuterType();
   if (oty == null) return null;

   String osgn = oty.getSignature();
   if (osgn == null || !osgn.contains("<")) return null;

   return jt.getOuterComponents();
}




private JcompSymbol createField(JcompTyper typer,JcodeField jf,JcompType orig)
{
   JcompType fty = getAsmType(typer,jf.desc);

   JcompSymbol fs = JcompSymbol.createBinaryField(jf.getName(),fty,
	 JcompControl.convertType(typer,jf.getDeclaringClass()),jf.access,jf.signature);

   return fs;
}


private JcompSymbol createMethod(JcompTyper typer,JcodeMethod jm)
{
   if (jm == null) return null;
   JcompType rt = JcompControl.convertType(typer,jm.getReturnType());
   boolean gen = false;
   String gsgn = jm.getSignature();
   if (gsgn != null) gen = true;
   JcompType ct = JcompControl.convertType(typer,jm.getDeclaringClass());
   // handle generics

   JcompType mt = getMethodType(typer,jm,rt);

   List<JcompType> excs = new ArrayList<JcompType>();
   for (JcodeDataType jdt : jm.getExceptionTypes()) {
      excs.add(JcompControl.convertType(typer,jdt));
    }

   return JcompSymbol.createBinaryMethod(jm.getName(),mt,ct,jm.access,excs,gen);
}




private JcompType getMethodType(JcompTyper typer,JcodeMethod jm,JcompType rt)
{
   if (rt == null) rt = JcompControl.convertType(typer,jm.getReturnType());

   List<JcompType> atys = new ArrayList<JcompType>();
   for (int i = 0; i < jm.getNumArguments(); ++i) {
      atys.add(JcompControl.convertType(typer,jm.getArgType(i)));
    }
   boolean var = jm.isVarArgs();
   JcompType mt = typer.createMethodType(rt,atys,var,jm.signature);

   return mt;
}


List<String> getClassPath()
{
   List<String> rslt;
   if (parent_context != null) {
      rslt = parent_context.getClassPath();
    }
   else rslt = new ArrayList<>();
   List<String> cp = jcode_control.getUserClassPath();
   if (cp != null) rslt.addAll(cp);
   return rslt;
}


}	// end of class JcompContextCode




/* end of JcompContextCode.java */
