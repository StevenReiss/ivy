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

static JcompType deriveMethodType(JcompTyper typer,JcompType mty,JcompType cty,List<JcompType> classparams)
{
   String csgn = cty.getSignature();
   if (csgn == null) return mty;
   if (!csgn.startsWith("<")) return mty;
   String msgn = mty.getSignature();
   if (msgn == null) return mty;

   TypeVarFinder tvf = new TypeVarFinder(classparams);
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

static JcompType deriveReturnType(JcompTyper typer,JcompType mty,JcompType bty,
      List<JcompType> argtypes,JcompType ctyp,List<JcompType> typargs)
{
   JcompType rty = mty.getBaseType();
   String msgn = mty.getSignature();
   if (msgn == null) return rty;
   if (mty.isVarArgs()) {
      boolean fixvar = true;
      int csz = mty.getComponents().size();
      if (argtypes.size() == csz) {
         JcompType ltyp = argtypes.get(argtypes.size()-1);
         if (ltyp.isArrayType()) fixvar = false;
       }
      if (fixvar) {
         int ct = argtypes.size() - csz+1;
         int ct0 = argtypes.size() - ct;
         JcompType rtype = null;
         if (ct == 0) rtype = mty.getComponents().get(csz-1).getBaseType();
         else {
            for (int i = ct0; i < argtypes.size(); ++i) {
               if (rtype == null) rtype = argtypes.get(i);
               else rtype = rtype.getCommonParent(typer,argtypes.get(i));
             }
          }
         rtype = typer.findArrayType(rtype);
         List<JcompType> nargs = new ArrayList<>();
         for (int i = 0; i < ct0; ++i) nargs.add(argtypes.get(i));
         nargs.add(rtype);
         argtypes = nargs;
       }
    }
   
   Map<String,JcompType> typemap = new HashMap<>();
   
   if (bty.isParameterizedType() || (bty.getSignature() != null && bty.getSignature().startsWith("<"))) {
      List<JcompType> comps = bty.getComponents();
      TypeVarFinder tvf = new TypeVarFinder(comps);
      SignatureReader sr = new SignatureReader(bty.getSignature());
      sr.accept(tvf);
      typemap.putAll(tvf.getTypeMap());
    }
   
   if (bty != ctyp) {
      JcompType nsty;
      for (JcompType sty = bty; sty != null; sty = nsty) {
         List<JcompType> comps = null;
         if (sty.isParameterizedType()) comps = sty.getComponents();
         nsty = sty.getSuperType();
         if (nsty == null) break;
         if (sty.getSignature() != null) {
            SuperParamDeriver spd = new SuperParamDeriver(typer,comps);
            SignatureReader sr = new SignatureReader(sty.getSignature());
            sr.accept(spd);
            nsty = spd.getSuperType();
          }
         JcompType nbtyp = nsty;
         if (nsty.isParameterizedType()) nbtyp = nsty.getBaseType(); 
         if (nsty == ctyp || nbtyp == ctyp) {
            if (nsty.isParameterizedType()) {
               TypeVarFinder tvf = new TypeVarFinder(nsty.getComponents());
               SignatureReader sr = new SignatureReader(nsty.getSignature());
               sr.accept(tvf);
               typemap.putAll(tvf.getTypeMap());
             }
            break;
          }
       }
    }
   
   if (msgn.startsWith("<")) {
      MethodVarFinder mvf = new MethodVarFinder(argtypes,typargs);
      SignatureReader msr = new SignatureReader(msgn);
      msr.accept(mvf);
      typemap.putAll(mvf.getTypeMap());
    }

   MethodDeriver mdv = new MethodDeriver(typer,typemap,mty);
   SignatureReader rsr = new SignatureReader(msgn);
   rsr.accept(mdv);
   JcompType jty = mdv.getNewMethodType();
   rty = jty.getBaseType();
   
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
      if (cur_index >= 0 && input_parms != null) {
         if (cur_index >= input_parms.size()) {
            System.err.println("PROBLEM with the number of formal generic parameters");
          }
         else {
            type_map.put(name,input_parms.get(cur_index++));
          }
       }
      super.visitFormalTypeParameter(name);
    }

}	// end of inner class TypeVarFinder



/********************************************************************************/
/*                                                                              */
/*      Visitor for getting type variables from a method signature              */
/*                                                                              */
/********************************************************************************/

private static class MethodVarFinder extends SignatureVisitor {
   
   private Map<String,JcompType> type_map;
   private List<JcompType> input_parms;
   private List<JcompType> arg_types;
   private int type_index;
   private int cur_index;
   private int array_depth;
   
   MethodVarFinder(List<JcompType> parms,List<JcompType> argtyps) {
      super(Opcodes.ASM5);
      type_map = new HashMap<>();
      input_parms = parms;
      arg_types = argtyps;
      cur_index = -1;
      array_depth = 0;
      type_index = 0;
    }
   
   Map<String,JcompType> getTypeMap()                   { return type_map; }
   
   @Override public void visitFormalTypeParameter(String name) {
      if (arg_types != null && type_index < arg_types.size()) {
         type_map.put(name,arg_types.get(type_index++));
       }
    }
   
   @Override public SignatureVisitor visitParameterType() {
      ++cur_index;
      array_depth = 0;
      return this;
    }
   
   @Override public SignatureVisitor visitReturnType() {
      cur_index = -1;
      return this;
    }
   
   @Override public SignatureVisitor visitArrayType() {
      ++array_depth;
      return this;
    }
   
   @Override public void visitTypeVariable(String name) {
      if (cur_index < 0) return;
      JcompType bty = input_parms.get(cur_index);
      for (int i = 0; i < array_depth; ++i) bty = bty.getBaseType();
      type_map.put(name,bty);
    }
   
}





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

   TypeDeriver(JcompTyper typer,Map<String,JcompType> tmap,JcompType bty,SignatureVisitor writer) {
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
      else {
         for (int i = 0; i < array_count; ++i) {
            new_type = type_data.findArrayType(new_type);
          }
       }
      if (param_types == null) return new_type;
      List<JcompType> ptys = new ArrayList<>();
      int nvar = 0;
      for (TypeDeriver tdv : param_types) {
         if (tdv.isChanged()) is_changed = true;
         JcompType jty = tdv.getResultType();
         if (jty == null || jty.isTypeVariable()) ++nvar;
         if (jty == null) return new_type;
         ptys.add(jty);
       }
      if (ptys.size() > 0 && !new_type.isParameterizedType()) is_changed = true;
      if (original_type == null) is_changed = true;
     
      if (!is_changed) {
         if (original_type == null) return new_type;
         return original_type;
       }
      
      int act = 0;
      while (new_type.isArrayType()) {
         ++act;
         new_type = new_type.getBaseType();
       }
       
      if (nvar != 0 && nvar != param_types.size()) ;
      else if (ptys.size() > 0){
         JcompType jty = new_type;
         if (jty.isParameterizedType()) jty = jty.getBaseType();
         new_type = JcompType.createParameterizedType(jty,ptys,type_data);
       }
      
      for (int i = 0; i < act; ++i) {
         new_type = type_data.findArrayType(new_type);
       }
      
      return new_type;
    }
   
   boolean isChanged()				{ return is_changed; }

   @Override public SignatureVisitor visitArrayType() {
      ++array_count;
      if (new_signature != null) new_signature.visitArrayType();
      return this;
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
      if (nty != null && nty.getJavaTypeName() != null) {
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



/********************************************************************************/
/*                                                                              */
/*      Derive type parameters from a type                                      */
/*                                                                              */
/********************************************************************************/

private static class SuperParamDeriver extends SignatureVisitor {
   
   private JcompTyper type_base;
   private List<JcompType> input_parms;
   private Map<String,JcompType> local_map;
   private TypeDeriver super_deriver;
   private List<TypeDeriver> iface_derivers;
   
   SuperParamDeriver(JcompTyper typer,List<JcompType> parms) {
      super(Opcodes.ASM5);
      type_base = typer;
      local_map = new HashMap<>();
      if (parms != null) input_parms = parms;
      else input_parms = new ArrayList<>();
      super_deriver = null;
      iface_derivers = null;
    }
   
   JcompType getSuperType() {
      return super_deriver.getResultType();
    }
   
   @Override public void visitFormalTypeParameter(String name) {
      int idx = local_map.size();
      if (idx < input_parms.size()) local_map.put(name,input_parms.get(idx));
    }
   
   @Override public SignatureVisitor visitSuperclass() {
      super_deriver = new TypeDeriver(type_base,local_map,null,null);
      return super_deriver;
    }
   
   @Override public SignatureVisitor visitInterface() {
      TypeDeriver td = new TypeDeriver(type_base,local_map,null,null);
      if (iface_derivers == null) iface_derivers = new ArrayList<TypeDeriver>();
      iface_derivers.add(td);
      return td;
    }
      
}       // end of inner class SuperParamDeriver



}	// end of class JcompGenerics




/* end of JcompGenerics.java */
