/********************************************************************************/
/*										*/
/*		JcompContext.java						*/
/*										*/
/*	Generic context for access to library files				*/
/*										*/
/********************************************************************************/



package edu.brown.cs.ivy.jcomp;

import org.objectweb.asm.Type;

import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.jcode.JcodeMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



abstract class JcompContext implements JcompConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected JcompContext		parent_context;
private Map<String,JcompType>	special_types;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected JcompContext(JcompContext par)
{
   parent_context = par;
   special_types = new HashMap<>();
}




/********************************************************************************/
/*										*/
/*	Abstract methods							*/
/*										*/
/********************************************************************************/

abstract JcompType defineKnownType(JcompTyper typer,String name);
abstract JcompSymbol defineKnownField(JcompTyper typer,String cls,String id,JcompType orig);
abstract JcompSymbol defineKnownMethod(JcompTyper typer,String cls,String id,
					  JcompType argtype,JcompType ctyp);
abstract List<JcompSymbol> defineKnownStatics(JcompTyper typer,String cls,String id,JcompType ctyp);
abstract Set<JcompSymbol> defineKnownAbstracts(JcompTyper typer,String cls);
abstract List<JcompSymbol> findKnownMethods(JcompTyper typer,String cls);
abstract void defineAll(JcompTyper typer,String cls,JcompScope scp);




/********************************************************************************/
/*										*/
/*	ASM type interface							*/
/*										*/
/********************************************************************************/

protected JcompType getAsmTypeName(JcompTyper typer,String nm)
{
   nm = nm.replace('/','.');

   JcompType jt = special_types.get(nm);
   if (jt != null) return jt;

   jt = typer.findSystemType(nm);

   if (jt == null) return null;
   if (!jt.isBaseKnown()) {
      jt = typer.findType(nm);
      if (jt == null) jt = defineKnownType(typer,nm);
    }
   special_types.put(nm,jt);

   return jt;
}


protected JcompType getAsmType(JcompTyper typer,String desc)
{
   return getAsmType(typer,Type.getType(desc));
}


protected JcompType getAsmType(JcompTyper typer,Type t)
{
   String tnm = null;
   switch (t.getSort()) {
      case Type.VOID :
	 tnm = "void";
	 break;
      case Type.BOOLEAN :
	 tnm = "boolean";
	 break;
      case Type.CHAR :
	 tnm = "char";
	 break;
      case Type.BYTE :
	 tnm = "byte";
	 break;
      case Type.SHORT :
	 tnm = "short";
	 break;
      case Type.INT :
	 tnm = "int";
	 break;
      case Type.FLOAT :
	 tnm = "float";
	 break;
      case Type.LONG :
	 tnm = "long";
	 break;
      case Type.DOUBLE :
	 tnm = "double";
	 break;
      case Type.OBJECT :
	 tnm = t.getClassName();
	 break;
      case Type.ARRAY :
	 JcompType jt = getAsmType(typer,t.getElementType());
	 if (jt == null) {
	    System.err.println("JCOMP: Problem finding array base type for " + t + " " +
		  t.getElementType() + " " + t.getDimensions());
	    return null;
	  }
	 for (int i = 0; i < t.getDimensions(); ++i) {
	    jt = typer.findArrayType(jt);
	    if (jt == null) break;
	  }
	 if (jt == null) {
	    System.err.println("JCOMP: Problem finding array type for " + t + " " +
		  t.getElementType() + " " + t.getDimensions());
	  }
	 return jt;
    }

   JcompType njt = typer.findSystemType(tnm);

   if (njt == null) {
      IvyLog.logE("JCOMP","Problem finding context system type: " + tnm);
      njt = typer.findSystemType("java.lang.Object");
    }

   return njt;
}







protected int compatiblityScore(JcompType argtyp,JcompType [] margs,
      boolean varargs,boolean innerinit)
{
   if (argtyp == null) return 0;

   List<JcompType> args = argtyp.getComponents();

   boolean isok = false;
   int score = 0;
   if (margs.length == args.size()) {
      isok = true;
      for (int i = 0; i < margs.length; ++i) {
	 JcompType jt0 = margs[i];
	 JcompType jt1 = args.get(i);
	 if (!jt1.isCompatibleWith(jt0)) isok = false;
	 else score += score(jt0,jt1);
       }
    }

   if (!isok && varargs && args.size() >= margs.length-1) {
      isok = true;
      score = 400;
      for (int i = 0; i < margs.length-1; ++i) {
	 JcompType jt0 = margs[i];
	 JcompType jt1 = args.get(i);
	 if (!jt1.isCompatibleWith(jt0)) isok = false;
	 else score += score(jt0,jt1);
       }
      JcompType rjt0 = margs[margs.length-1];
      // checking for array shouldn't be required here
      if (rjt0.isArrayType()) rjt0 = rjt0.getBaseType();
      for (int i = margs.length-1; i < args.size(); ++i) {
	 JcompType jt1 = args.get(i);
	 if (!jt1.isCompatibleWith(rjt0)) isok = false;
         else score += score(rjt0,jt1);
       }
    }

   if (!isok && innerinit) {
      if (margs.length == args.size() + 1) {
	 isok = true;
	 for (int i = 0; i < args.size(); ++i) {
	    JcompType jt0 = margs[i];
	    JcompType jt1 = args.get(i);
	    if (!jt1.isCompatibleWith(jt0)) isok = false;
	    else score += score(jt0,jt1);
	  }
       }
    }

   if (!isok) score = -1;
   
   return score;
}




protected static int score(JcompType jt0,JcompType jt1)
{
   if (jt0 == jt1) return 0;
   else if (jt0.isNumericType() && jt1.isNumericType()) return 1;
   else if (jt0.isClassType() && !jt1.isClassType()) return 100;
   else if (jt1.isClassType() && !jt0.isClassType()) return 100;
   else if (jt0.isClassType() && jt1.isClassType()) {
      int ct = JcompScope.getTypeDepth(jt0,jt1);
      return 20 + ct;
    }
   return 5;
}


List<String> getClassPath()
{
   if (parent_context == null) return new ArrayList<>();
   return parent_context.getClassPath();
}

JcodeMethod getMethodCode(JcompSymbol js)
{
   return null;
}


}	// end of class JcompContext




/* end of JcompContext.java */
