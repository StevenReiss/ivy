/********************************************************************************/
/*										*/
/*		JcompGenerics.java						*/
/*										*/
/*	Handler type derivations for generic types				*/
/*										*/
/********************************************************************************/



package edu.brown.cs.ivy.jcomp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;




class JcompGenerics implements JcompConstants
{


/********************************************************************************/
/*										*/
/*	Methods to derive a new parameterized method type			*/
/*										*/
/********************************************************************************/

static JcompType deriveMethodType(JcompTyper typer,JcompType mty,JcompType cty,List<JcompType> params)
{
   String csgn = cty.getSignature();
   if (csgn == null) return mty;
   if (!csgn.startsWith("<")) return mty;
   String msgn = mty.getSignature();
   if (msgn == null) return mty;

   TypeVarFinder tvf = new TypeVarFinder(params);
   SignatureReader sr = new SignatureReader(csgn);
   sr.accept(tvf);
   Map<String,JcompType> typemap = tvf.getTypeMap();

   MethodDeriver md = new MethodDeriver(typer,typemap,mty);
   sr = new SignatureReader(msgn);
   sr.accept(md);

   JcompType newty = md.getNewMethodType();

   return newty;
}


/********************************************************************************/
/*										*/
/*	Methods to extract actual types from type definition			*/
/*										*/
/********************************************************************************/

static JcompType deriveSupertype(JcompTyper typer,JcompType cty,List<JcompType> parms)
{
   JcompType sty = cty.getSuperType();
   String csgn = cty.getSignature();
   if (csgn == null) return sty;
   if (!csgn.startsWith("<")) return sty;

   TypeVarFinder tvf = new TypeVarFinder(parms);
   SignatureReader sr = new SignatureReader(csgn);
   sr.accept(tvf);
   Map<String,JcompType> typemap = tvf.getTypeMap();
   SuperDeriver sd = new SuperDeriver(typer,typemap,sty,cty.getInterfaces());
   sr = new SignatureReader(csgn);
   sr.accept(sd);

   return sd.getSuperType();
}



static Collection<JcompType> deriveInterfaceTypes(JcompTyper typer,JcompType cty,List<JcompType> parms)
{
   String csgn = cty.getSignature();
   if (csgn == null) return cty.getInterfaces();
   if (!csgn.startsWith("<")) return cty.getInterfaces();

   TypeVarFinder tvf = new TypeVarFinder(parms);
   SignatureReader sr = new SignatureReader(csgn);
   sr.accept(tvf);
   Map<String,JcompType> typemap = tvf.getTypeMap();
   SuperDeriver sd = new SuperDeriver(typer,typemap,cty.getSuperType(),cty.getInterfaces());
   sr = new SignatureReader(csgn);
   sr.accept(sd);

   return sd.getInterfaces();
}




/********************************************************************************/
/*										*/
/*	Methods to determine the return type of a generic method		*/
/*										*/
/********************************************************************************/

static JcompType deriveReturnType(JcompTyper typer,JcompType mty,List<JcompType> argtypes)
{
   JcompType rty = mty.getBaseType();
   String msgn = mty.getSignature();
   if (msgn == null) return rty;
   if (!msgn.startsWith("<")) return rty;

   return rty;
}


/********************************************************************************/
/*										*/
/*	Visitor for finding type variables from a class signature		*/
/*										*/
/********************************************************************************/

private static class TypeVarFinder extends SignatureVisitor {

   private Map<String,JcompType> type_map;
   private List<JcompType> input_parms;
   private int cur_index;


   TypeVarFinder(List<JcompType> parms) {
      super(Opcodes.ASM5);
      type_map = new HashMap<>();
      input_parms = parms;
      cur_index = 0;
    }

   Map<String,JcompType> getTypeMap()			{ return type_map; }

   @Override public void visitFormalTypeParameter(String name) {
      if (cur_index >= 0) {
         if (cur_index >= input_parms.size()) {
            System.err.println("PROBLEM");
          }
         else {
            type_map.put(name,input_parms.get(cur_index++));
          }
       }
      super.visitFormalTypeParameter(name);
    }

}	// end of inner class TypeVarFinder




/********************************************************************************/
/*										*/
/*	Type visitor to find super type 					*/
/*										*/
/********************************************************************************/

private static class SuperDeriver extends SignatureVisitor {

   private JcompTyper type_data;
   private Map<String,JcompType> type_map;
   private JcompType super_type;
   private List<JcompType> interface_types;
   private TypeDeriver super_deriver;
   private List<TypeDeriver> iface_derivers;

   SuperDeriver(JcompTyper typer,Map<String,JcompType> tmap,JcompType sty,Collection<JcompType> ifaces) {
      super(Opcodes.ASM5);
      type_data = typer;
      type_map = tmap;
      super_type = sty;
      interface_types = null;
      if (ifaces != null) interface_types = new ArrayList<>(ifaces);
      super_deriver = null;
      iface_derivers = new ArrayList<>();
    }

   JcompType getSuperType() {
      if (super_deriver == null) return super_type;
      return super_deriver.getResultType();
    }

   List<JcompType> getInterfaces() {
       List<JcompType> rslt = new ArrayList<>();
       for (TypeDeriver td : iface_derivers) {
	  rslt.add(td.getResultType());
	}
       return rslt;
    }

   @Override public SignatureVisitor visitSuperclass() {
      super_deriver = new TypeDeriver(type_data,type_map,super_type,null);
      return super_deriver;
    }

   @Override public SignatureVisitor visitInterface() {
      if (interface_types == null) return this;
      int idx = iface_derivers.size();
      TypeDeriver td = new TypeDeriver(type_data,type_map,interface_types.get(idx),null);
      iface_derivers.add(td);
      return td;
    }

}	// end of inner class SuperDeriver




/********************************************************************************/
/*										*/
/*	Visitor to derive the actual method type given parameters		*/
/*										*/
/********************************************************************************/

private static class MethodDeriver extends SignatureVisitor {

   private JcompTyper type_data;
   private Map<String,JcompType> type_map;
   private SignatureVisitor new_signature;
   private JcompType original_type;
   private List<JcompType> arg_types;
   private JcompType return_type;
   private List<TypeDeriver> arg_derivers;
   private TypeDeriver return_deriver;
   private int arg_index;

   MethodDeriver(JcompTyper typer,Map<String,JcompType> tmap,JcompType mty) {
      super(Opcodes.ASM5);
      type_data = typer;
      type_map = tmap;
      new_signature = new SignatureWriter();
      original_type = mty;
      arg_types = new ArrayList<>(mty.getComponents());
      arg_derivers = new ArrayList<>();
      return_type = mty.getBaseType();
      arg_index = 0;
    }

   JcompType getNewMethodType() {
      if (arg_types.size() != arg_derivers.size()) return original_type;
   
      boolean chng = false;
      for (int i = 0; i < arg_types.size(); ++i) {
         JcompType oty = arg_types.get(i);
         JcompType nty = arg_derivers.get(i).getResultType();
         if (nty == null) nty = oty;
         if (oty != nty) {
            arg_types.set(i,nty);
            chng = true;
          }
         else if (arg_derivers.get(i).isChanged()) chng = true;
       }
      JcompType rty = return_deriver.getResultType();
      if (rty == null) rty = return_type;
      if (rty != return_type) {
         return_type = rty;
         chng = true;
       }
      if (return_deriver.isChanged()) chng = true;
      if (!chng) return original_type;
      JcompType jty = JcompType.createMethodType(return_type,arg_types,original_type.isVarArgs(),
            original_type.getSignature());
      return jty;
    }

   @Override public SignatureVisitor visitArrayType() {
      new_signature.visitArrayType();
      return super.visitArrayType();
    }

   @Override public void visitBaseType(char d) {
      new_signature.visitBaseType(d);
    }

   @Override public SignatureVisitor visitClassBound() {
      new_signature.visitClassBound();
      return new_signature;
    }

   @Override public void visitClassType(String name) {
      new_signature.visitClassType(name);
    }

   @Override public void visitEnd() {
      new_signature.visitEnd();
    }

   @Override public SignatureVisitor visitExceptionType() {
      new_signature.visitExceptionType();
      return super.visitExceptionType();
    }

   @Override public void visitFormalTypeParameter(String name) {
      new_signature.visitFormalTypeParameter(name);
    }

   @Override public void visitInnerClassType(String name) {
      new_signature.visitInnerClassType(name);
    }

   @Override public SignatureVisitor visitInterface() {
      new_signature.visitInterface();
      return super.visitInterface();
    }

   @Override public SignatureVisitor visitInterfaceBound() {
      new_signature.visitInterfaceBound();
      return super.visitInterfaceBound();
    }

   @Override public SignatureVisitor visitParameterType() {
      TypeDeriver tdv = new TypeDeriver(type_data,type_map,arg_types.get(arg_index++),new_signature);
      arg_derivers.add(tdv);
      new_signature.visitParameterType();
      return tdv;
    }

   @Override public SignatureVisitor visitReturnType() {
      arg_index = -1;
      return_deriver = new TypeDeriver(type_data,type_map,return_type,new_signature);
      new_signature.visitReturnType();
      return return_deriver;
    }

   @Override public SignatureVisitor visitSuperclass() {
      new_signature.visitSuperclass();
      return super.visitSuperclass();
    }

   @Override public void visitTypeArgument() {
      new_signature.visitTypeArgument();
    }

   @Override public SignatureVisitor visitTypeArgument(char w) {
      new_signature.visitTypeArgument(w);
      return super.visitTypeArgument(w);
    }

   @Override public void visitTypeVariable(String name) {
      new_signature.visitTypeVariable(name);
    }

}	// end of inner class MethodDeriver




/********************************************************************************/
/*										*/
/*	Type visitor to get an actual type					*/
/*										*/
/********************************************************************************/

private static class TypeDeriver extends SignatureVisitor {

   private JcompTyper type_data;
   private Map<String,JcompType> type_map;
   private SignatureVisitor new_signature;
   private JcompType original_type;
   private JcompType new_type;
   private int array_count;
   private List<TypeDeriver> param_types;
   private boolean is_changed;

   TypeDeriver(JcompTyper typer,Map<String,JcompType> tmap, JcompType bty,SignatureVisitor writer) {
      super(Opcodes.ASM5);
      type_data = typer;
      type_map = tmap;
      new_signature = writer;
      original_type = bty;
      param_types = null;
      array_count = 0;
      is_changed = false;
    }

   JcompType getResultType() {
      if (new_type == null) new_type = original_type;
      if (param_types == null) return new_type;
      List<JcompType> ptys = new ArrayList<>();
      int nvar = 0;
      for (TypeDeriver tdv : param_types) {
         if (tdv.isChanged()) is_changed = true;
         JcompType jty = tdv.getResultType();
         if (jty == null || jty.isTypeVariable()) ++nvar;
         if (jty == null)
            return new_type;
         ptys.add(jty);
       }
      if (!is_changed) {
         if (original_type == null) return new_type;
         return original_type;
       }
      if (nvar != 0) {
         if (nvar == param_types.size()) return new_type;
       }
      if (ptys.size() > 0){
         JcompType jty = new_type;
         if (jty.isParameterizedType()) jty = jty.getBaseType();
         return JcompType.createParameterizedType(jty,ptys,type_data);
       }
      for (int i = 0; i < array_count; ++i) {
         new_type = JcompType.createArrayType(new_type);
       }
      return new_type;
    }

   boolean isChanged()				{ return is_changed; }

   @Override public SignatureVisitor visitArrayType() {
      ++array_count;
      if (new_signature != null) new_signature.visitArrayType();
      return super.visitArrayType();
    }

   @Override public void visitBaseType(char d) {
      if (new_signature != null) new_signature.visitBaseType(d);
    }

   @Override public void visitClassType(String name) {
      if (new_signature != null) new_signature.visitClassType(name);
      name = name.replace("/",".");
      new_type = type_data.findType(name);
      if (new_type == null) {
         name = name.replace("$",".");
         new_type = type_data.findType(name);
       }
      if (new_type == null) {
         new_type = type_data.findSystemType(name);
       }
      if (new_type == null) {
         System.err.println("TYPE " + name + " not found");
       }
    }

   @Override public void visitEnd() {
      if (new_signature != null) new_signature.visitEnd();
    }

   @Override public void visitInnerClassType(String name) {
      new_type = type_data.findType(name);
      if (new_signature != null) new_signature.visitInnerClassType(name);
    }

   @Override public void visitTypeArgument() {
      JcompType jty = type_data.findSystemType("java.lang.Object");
      TypeDeriver tdv = new TypeDeriver(type_data,type_map,jty,new_signature);
      if (param_types == null) param_types = new ArrayList<>();
      param_types.add(tdv);
      if (new_signature != null) new_signature.visitTypeArgument();
    }

   @Override public SignatureVisitor visitTypeArgument(char w) {
      if (new_signature != null) new_signature.visitTypeArgument(w);
      TypeDeriver tdv = new TypeDeriver(type_data,type_map,null,new_signature);
      if (param_types == null) param_types = new ArrayList<>();
      param_types.add(tdv);
      return tdv;
    }
   
   @Override public void visitTypeVariable(String name) {
      JcompType nty = type_map.get(name);
      if (nty != null) {
         String tnm = nty.getJavaTypeName();
         if (tnm.startsWith("L")) tnm = tnm.substring(1);
         if (tnm.endsWith(";")) tnm = tnm.substring(0,tnm.length()-1);
         if (new_signature != null) {
            new_signature.visitClassType(tnm);
            new_signature.visitEnd();
          }
         new_type = nty;
         is_changed = true;
       }
      else {
         // new_type = JcompType.createVariableType(name);
         new_type = type_data.findType("java.lang.Object");
         if (new_signature != null) new_signature.visitTypeVariable(name);
       }
    }

}	// end of inner class TypeDeriver




}	// end of class JcompGenerics




/* end of JcompGenerics.java */
