/********************************************************************************/
/*										*/
/*		JcompGenerics.java						*/
/*										*/
/*	Handler type derivations for generic types				*/
/*										*/
/********************************************************************************/



package edu.brown.cs.ivy.jcomp;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import edu.brown.cs.ivy.file.IvyLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;




class JcompGenerics implements JcompConstants
{


/********************************************************************************/
/*										*/
/*	Methods to derive a new parameterized method type			*/
/*										*/
/********************************************************************************/

static JcompType deriveMethodType(JcompTyper typer,JcompType mty,JcompType cty,
      Map<String,JcompType> outermap)
{
   String msgn = mty.getSignature();
   if (msgn == null) return mty;
	
   SortedMap<String,JcompType> typemap = fixTypeVars(cty,outermap);

   MethodDeriver md = new MethodDeriver(typer,cty,typemap,mty);
   SignatureReader sr1 = new SignatureReader(msgn);
   
   try {
      sr1.accept(md);
    }
   catch (Throwable t) {
      System.err.println("JCOMP: Problem deriving method type: " +
            mty + " : " + cty + " " + msgn + " " + t);
      // t.printStackTrace();
      return mty;
    }

   JcompType newty = md.getNewMethodType();

   return newty;
}


/********************************************************************************/
/*										*/
/*	Methods to extract actual types from type definition			*/
/*										*/
/********************************************************************************/

static JcompType deriveSupertype(JcompTyper typer,JcompType cty,Map<String,JcompType> outermap)
{
   JcompType sty = cty.getSuperType();
   String csgn = cty.getSignature();
   if (csgn == null) return sty;
   if (!csgn.contains("<")) return sty;
   SortedMap<String,JcompType> typemap = fixTypeVars(cty,outermap);

   SuperDeriver sd = new SuperDeriver(typer,cty,typemap,sty,cty.getInterfaces());
   SignatureReader sr = new SignatureReader(csgn);
   try {
      sr.accept(sd);
    }
   catch (Throwable t) {
      System.err.println("Problem scanning signature: " + csgn);
    }

   return sd.getSuperType();
}


private static SortedMap<String,JcompType> fixTypeVars(JcompType ctyp,Map<String,JcompType> outermap)
{
   SortedMap<String,JcompType> rslt = new TreeMap<>();
   if (outermap != null) rslt.putAll(outermap);
   
   String bsgn = ctyp.getSignature();
   if (ctyp.isParameterizedType()) bsgn = ctyp.getBaseType().getSignature();
   
   if (bsgn != null) {
      // List<String> names = getTypeVariableNames(bsgn,false);
      if (ctyp.getOuterComponents() != null) {
         for (Map.Entry<String,JcompType> ent : ctyp.getOuterComponents().entrySet()) {
            String key = ent.getKey();
            JcompType typ = ent.getValue();
            // if (names.contains(key)) continue;
            rslt.put(key,typ);
          }
       }
    }
      
   String csgn = ctyp.getSignature();
   if (csgn != null) {
      List<String> names = getTypeVariableNames(csgn,false);
      for (String s : names) {
	 int idx = s.lastIndexOf(".");
	 if (idx > 0) continue;
	 JcompType jt = rslt.get(s);
	 if (jt == null) {
	    JcompType pty = JcompType.createVariableType(s);
	    rslt.put(s,pty);
	  }
       }
    }

   return rslt;
}

static void fixupTypeVariable(JcompType var,List<TypeDeriver> bounds)
{
   if (var == null || bounds == null || bounds.isEmpty()) return;
   for (TypeDeriver td : bounds) {
      // Might need to handle multiple supertypes, intersection types
      JcompType btyp = td.getResultType();
      if (btyp == null) continue;
      if (btyp.isInterfaceType()) var.addInterface(btyp);
      else var.setSuperType(btyp);
    }
}



static String deriveClassTypeSignature(JcompTyper typer,JcompType cty,
	SortedMap<String,JcompType> outermap)
{
   String csgn = cty.getSignature();
   if (csgn == null) return null;
   if (!csgn.contains("<")) return csgn;

   SortedMap<String,JcompType> typemap = fixTypeVars(cty,outermap);

   SignatureWriter sw = new SignatureWriter();
   TypeDeriver td = new TypeDeriver(typer,cty,typemap,cty,sw);
   SignatureReader sr = new SignatureReader(csgn);
   try {
      sr.accept(td);
    }
   catch (StringIndexOutOfBoundsException e) {
      e.printStackTrace();
      System.err.println("Problem scanning signature: " + csgn);
    }

   String rslt = sw.toString();
   if (rslt.endsWith("<") || rslt.contains(";;"))
      System.err.println("BAD CLASS SIGNATURE");
   
   return rslt;
}



static JcompType findDerivedClassType(JcompTyper typer,JcompType cty,
      SortedMap<String,JcompType> outermap)
{
   String csgn = cty.getSignature();
   if (csgn == null) return cty;
   List<String> vars = getTypeVariableNames(csgn,true);
   if (vars == null || vars.isEmpty()) return cty;
   boolean work = false;
   for (String s : vars) {
      if (outermap.get(s) != null) work = true;
    }
   if (!work) return cty;

   JcompType ntyp = JcompType.createParameterizedType(cty,null,outermap,typer);

   return ntyp;
}








static Collection<JcompType> deriveInterfaceTypes(JcompTyper typer,JcompType cty,
	Map<String,JcompType> outermap)
{
   String csgn = cty.getSignature();
   if (csgn == null) return cty.getInterfaces();
   if (!csgn.contains("<") && cty.getInterfaces() != null) return cty.getInterfaces();

   SortedMap<String,JcompType> typemap = fixTypeVars(cty,outermap);

   SuperDeriver sd = new SuperDeriver(typer,cty,typemap,cty.getSuperType(),cty.getInterfaces());
   SignatureReader sr = new SignatureReader(csgn);
   sr.accept(sd);

   return sd.getInterfaces();
}




/********************************************************************************/
/*										*/
/*	Methods to determine the return type of a generic method		*/
/*										*/
/********************************************************************************/

static JcompType deriveReturnType(JcompTyper typer,JcompType mty,JcompType bty,
      List<JcompType> argtypes,JcompType ctyp,
      List<JcompType> typeparams)
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
	 if (ct <= 0) rtype = mty.getComponents().get(csz-1).getBaseType();
	 else {
	    for (int i = ct0; i < argtypes.size(); ++i) {
	       if (rtype == null) rtype = argtypes.get(i);
	       else rtype = rtype.getCommonParent(typer,argtypes.get(i));
	     }
	  }
	 rtype = typer.findArrayType(rtype);
	 List<JcompType> nargs = new ArrayList<>();
         ct0 = Math.min(ct0,argtypes.size());
	 for (int i = 0; i < ct0; ++i) nargs.add(argtypes.get(i));
	 nargs.add(rtype);
	 argtypes = nargs;
       }
    }

   Map<String,JcompType> typemap = new HashMap<>();
   if (bty.getOuterComponents() != null) {
      for (Map.Entry<String,JcompType> ent : bty.getOuterComponents().entrySet()) {
	 if (ent.getValue() != null) {
	    typemap.put(ent.getKey(),ent.getValue());
	  }
	 else {
	    System.err.println("Unexpected null component");
	  }
       }
    }

   // if (bty != ctyp) {
      // JcompType nsty;
      // for (JcompType sty = bty; sty != null; sty = nsty) {
	 // nsty = sty.getSuperType();
	 // if (nsty == null) break;
	 // if (sty.getSignature() != null) {
	    // SuperParamDeriver spd = new SuperParamDeriver(typer,comps);
	    // SignatureReader sr = new SignatureReader(sty.getSignature());
	    // sr.accept(spd);
	    // nsty = spd.getSuperType();
	    // if (nsty == null) break;
	  // }
	 // JcompType nbtyp = nsty;
	 // if (nsty.isParameterizedType()) nbtyp = nsty.getBaseType();
	 // if (nsty == ctyp || nbtyp == ctyp) {
	    // if (nsty.isParameterizedType()) {
	       // TypeVarFinder tvf = new TypeVarFinder(nsty.getComponents(),outermap);
	       // SignatureReader sr = new SignatureReader(nsty.getSignature());
	       // sr.accept(tvf);
	       // typemap.putAll(tvf.getTypeMap());
	     // }
	    // break;
	  // }
       // }
    // }

   if (msgn.startsWith("<")) {
      MethodVarFinder mvf = new MethodVarFinder(typer,argtypes,typemap,typeparams);
      SignatureReader msr = new SignatureReader(msgn);
      try {
         msr.accept(mvf);
       }
      catch (Throwable t) {
         System.err.println("JCOMP: Problem deriving return type: " +
               mty + " : " + msgn + " " + t);
         // t.printStackTrace();
         return mty;
       }   
      Map<String,JcompType> rsltmap = mvf.getTypeMap();
      if (rsltmap == null) return rty;
      typemap.putAll(rsltmap);
    }

   MethodDeriver mdv = new MethodDeriver(typer,bty,typemap,mty);
   SignatureReader rsr = new SignatureReader(msgn);
   rsr.accept(mdv);
   JcompType jty = mdv.getNewMethodType();
   rty = jty.getBaseType();

   return rty;
}




static JcompType deriveFieldType(JcompTyper typer,JcompType fty,String fsign,JcompType cty,
      Map<String,JcompType> outermap)
{
   String csgn = cty.getSignature();
   if (csgn == null) return fty;
   if (!csgn.startsWith("<")) return fty;

   SortedMap<String,JcompType> typemap = fixTypeVars(cty,outermap);

   SignatureWriter sgr = new SignatureWriter();
   TypeDeriver tdr = new TypeDeriver(typer,cty,typemap,fty,sgr);
   SignatureReader sr1 = new SignatureReader(fsign);
   sr1.accept(tdr);
   JcompType ntype = tdr.getResultType();
   if (ntype != null) return ntype;

   return fty;
}


/********************************************************************************/
/*										*/
/*	Get variable names from a signature					*/
/*										*/
/********************************************************************************/

static List<String> getTypeVariableNames(String sgn,boolean all)
{
   if (sgn == null) return new ArrayList<>();
   try {
      TypeVarNameFinder tvnf = new TypeVarNameFinder();
      SignatureReader sr = new SignatureReader(sgn);
      sr.accept(tvnf);
      if (all) return tvnf.getOtherNames();
      return tvnf.getVarNames();
    }
   catch (Throwable t) {
      System.err.println("Bad signature: " + sgn);
      t.printStackTrace();
    }
   
   return new ArrayList<>();
}


static private JcompType lookupClassType(JcompTyper typer,String name) {
   int arrct = 0;
   while (name.startsWith("[")) {
      ++arrct;
      name = name.substring(1);
    }
   name = name.replace("/",".");
   JcompType typ = typer.findType(name);
   if (typ == null) {
      name = name.replace("$",".");
      typ = typer.findType(name);
    }
   if (typ == null) {
      typ = typer.findSystemType(name);
    }
   if (arrct > 0 && typ != null) {
      for (int i = 0; i < arrct; ++i) {
         typ = typer.findArrayType(typ);
       }
    }
   
   return typ;
}


private static class TypeVarNameFinder extends SignatureVisitor {

   private List<String> var_names;
   private List<String> other_names;

   TypeVarNameFinder() {
      super(Opcodes.ASM6);
      var_names = new ArrayList<>();
      other_names = new ArrayList<>();
    }

   List<String> getVarNames()			{ return var_names; }
   List<String> getOtherNames() 		{ return other_names; }

   @Override public void visitFormalTypeParameter(String name) {
      if (!var_names.contains(name)) var_names.add(name);
      if (!other_names.contains(name)) other_names.add(name);
    }

   @Override public void visitTypeVariable(String name) {
      if (!other_names.contains(name)) other_names.add(name);
    }

}	// end of inner class TypeVarFinder



/********************************************************************************/
/*										*/
/*	Visitor to ignore part of signature					*/
/*										*/
/********************************************************************************/

private static class SkipVisitor extends SignatureVisitor {

   SkipVisitor() {
      super(Opcodes.ASM6);
    }

}	// end of inner class SkipVisitor



/********************************************************************************/
/*										*/
/*	Abstract signature visitor for our use					*/
/*										*/
/********************************************************************************/

private static abstract class GenericSignatureVisitor extends SignatureVisitor {

   protected JcompTyper type_data;
   protected SortedMap<String,JcompType> type_map;
   protected JcompType base_type;
   protected SignatureWriter new_signature;
   protected boolean skip_type;
   protected JcompType formal_type;
   protected List<TypeDeriver> bound_types;
   protected Map<String,String> var_map;

   GenericSignatureVisitor(JcompTyper typer,JcompType base,Map<String,JcompType> tmap,
         SignatureWriter sgnw) {
      super(Opcodes.ASM6);
      type_data = typer;
      base_type = base;
      type_map = new TreeMap<>();
      if (tmap != null) type_map.putAll(tmap);
      new_signature = sgnw;
      skip_type = false;
      formal_type = null;
      bound_types = null;
      var_map = null;
    }

   @Override public SignatureVisitor visitArrayType() {
      if (new_signature != null) new_signature.visitArrayType();
      return super.visitArrayType();
    }

   @Override public void visitBaseType(char d) {
      if (new_signature != null) new_signature.visitBaseType(d);
      super.visitBaseType(d);
    }

   @Override public SignatureVisitor visitClassBound() {
      if (skip_type) return new SkipVisitor();
      if (new_signature != null) new_signature.visitClassBound();
      if (formal_type != null) {
         TypeDeriver td = createDeriver(null);
         bound_types.add(td);
         return td;
       }
      return super.visitClassBound();
    }

   @Override public void visitClassType(String name) {
      if (new_signature != null) new_signature.visitClassType(name);
      super.visitClassType(name);
    }

   @Override public void visitEnd() {
      if (new_signature != null) new_signature.visitEnd();
      super.visitEnd();
    }

   @Override public SignatureVisitor visitExceptionType() {
      if (new_signature != null) new_signature.visitExceptionType();
      return super.visitExceptionType();
    }

   @Override public void visitFormalTypeParameter(String name) {
      fixFormalType();
      JcompType nty = type_map.get(name);
      if (nty == null || nty == type_data.ANY_TYPE) {
         if (new_signature != null) new_signature.visitFormalTypeParameter(name);
         startFormalType(name);
       }
      else if (nty.isTypeVariable()) {
         String nm = nty.getName();
         JcompType styp = nty.getSuperType();
         Collection<JcompType> ityps = nty.getInterfaces();
         if (var_map == null) var_map = new HashMap<>();
         var_map.put(name,nm);
         if ((styp == null && (ityps == null || ityps.size() == 0))) {
            if (new_signature != null) new_signature.visitFormalTypeParameter(nm);
            skip_type = false;
          }
         else {
            if (new_signature != null) {
               new_signature.visitFormalTypeParameter(nm);
               new_signature.visitClassBound();
               if (styp != null) {
                  new_signature.visitClassType(getInternalJavaName(styp));
                }
               else new_signature.visitClassType("java/lang/Object;");
               if (ityps != null) {
                  for (JcompType ityp : ityps) {
                     new_signature.visitInterfaceBound();
                     new_signature.visitClassType(getInternalJavaName(ityp));
                   }
                }
             }
            skip_type = true;
          }
       }
      else {
         skip_type = true;
       }
      super.visitFormalTypeParameter(name);
   }
   
   private String getInternalJavaName(JcompType t)
   {
      if (t.isParameterizedType()) {
         StringBuffer buf = new StringBuffer();
         String knm = getInternalJavaName(t.getBaseType());
         int idx = knm.lastIndexOf(";");
         if (idx == knm.length()-1) knm = knm.substring(0,idx);
         buf.append(knm);
         buf.append("<");
         for (JcompType pt : t.getComponents()) {
            if (pt.isTypeVariable()) {
               String nm = pt.getName();
               buf.append("T");
               buf.append(nm);
               buf.append(";");
             }
            else {
               String s1 = getInternalJavaName(pt);
               buf.append("L");
               buf.append(s1);
             }
          }
         buf.append(">;");
         return buf.toString();
          }
      String tnm = t.getJavaTypeName();
      if (tnm == null) return null;
      if (tnm.startsWith("L") && tnm.endsWith(";")) {
         tnm = tnm.substring(1);
       }
      return tnm;
   }

   @Override public void visitInnerClassType(String name) {
      if (new_signature != null) new_signature.visitInnerClassType(name);
      super.visitInnerClassType(name);
    }

   @Override public SignatureVisitor visitInterface() {
      skip_type = false;
      if (new_signature != null) new_signature.visitInterface();
      return super.visitInterface();
    }

   @Override public SignatureVisitor visitInterfaceBound() {
      if (skip_type) return new SkipVisitor();
      if (new_signature != null) new_signature.visitInterfaceBound();
      if (formal_type != null) {
         TypeDeriver td = createDeriver(null);
         bound_types.add(td);
         return td;
       }
      return super.visitInterfaceBound();
    }

   @Override public SignatureVisitor visitParameterType() {
      fixFormalType();
      skip_type = false;
      if (new_signature != null) new_signature.visitParameterType();
      return super.visitParameterType();
    }

   @Override public SignatureVisitor visitReturnType() {
      fixFormalType();
      skip_type = false;
      if (new_signature != null) new_signature.visitReturnType();
      return super.visitReturnType();
    }


   @Override public SignatureVisitor visitSuperclass() {
      fixFormalType();
      skip_type = false;
      if (new_signature != null) new_signature.visitSuperclass();
      return super.visitSuperclass();
    }

   @Override public void visitTypeArgument() {
      if (new_signature != null) new_signature.visitTypeArgument();
      super.visitTypeArgument();
    }

   @Override public SignatureVisitor visitTypeArgument(char w) {
      if (new_signature != null) new_signature.visitTypeArgument(w);
      return super.visitTypeArgument(w);
    }

   @Override public void visitTypeVariable(String name) {
      JcompType nty = type_map.get(name);
      if (nty != null && nty.getJavaTypeName() != null) {
         visitJcompType(nty,true);
       }
      else {
         if (new_signature != null) {
            if (var_map != null && var_map.get(name) != null)
               new_signature.visitTypeVariable(var_map.get(name));
            else
               new_signature.visitTypeVariable(name);
          }
       }
      super.visitTypeVariable(name);
    }

   protected TypeDeriver createDeriver(JcompType orig) {
      return new TypeDeriver(type_data,base_type,type_map,orig,new_signature);
    }

   protected void startFormalType(String name) {
      formal_type = JcompType.createVariableType(name);
      bound_types = new ArrayList<>();
      skip_type = false;
      type_map.put(name,formal_type);
    }

   protected void fixFormalType() {
      if (formal_type != null) {
         fixupTypeVariable(formal_type,bound_types);
         formal_type = null;
         bound_types = null;
       }
    }

   protected JcompType lookupTypeVariable(String name) {
      JcompType nty = type_map.get(name);
      if (nty != null) return nty;
      if (name.contains(".")) {
         nty = type_data.findType(name);
       }
      else {
         for (JcompType bt = base_type; bt != null; bt = bt.getOuterType()) {
            String nm = bt.getName();
            if (bt.isParameterizedType()) nm = bt.getBaseType().getName();
            nm = nm + "." + name;
            nty = type_data.findType(name);
            if (nty != null) break;
          }
       }
      if (nty != null) return nty;
   
      return null;
    }

   

   protected void visitJcompType(JcompType typ,boolean end) {
      if (new_signature == null) return;
      if (typ.isTypeVariable()) {
         String nm = typ.getName();
         if (var_map != null && var_map.get(nm) != null)
            new_signature.visitTypeVariable(var_map.get(nm));
         else
            new_signature.visitTypeVariable(nm);
       }
      else if (typ.isArrayType()) {
         new_signature.visitArrayType();
         visitJcompType(typ.getBaseType(),end);
       }
      else if (typ.isPrimitiveType()) {
         String nm = typ.getJavaTypeName();
         new_signature.visitBaseType(nm.charAt(0));
       }
      else if (typ.isWildcardType()) {
         new_signature.visitClassType("java/lang/Object");
         if (end) new_signature.visitEnd();
       }
      else if (typ.isParameterizedType()) {
         JcompType btyp = typ.getBaseType();
         visitJcompType(btyp,false);
         for (JcompType ptyp : typ.getComponents()) {
            if (ptyp == null || ptyp.isWildcardType()) 
               new_signature.visitTypeArgument();
            else {
               new_signature.visitTypeArgument('=');
               visitJcompType(ptyp,true);
             }
          }
         if (end) new_signature.visitEnd();
       }
      else if (typ.getOuterType() != null) {
         JcompType otyp = typ.getOuterType();
         visitJcompType(otyp,false);
         String nm = typ.getName();
         int idx = nm.lastIndexOf(".");
         nm = nm.substring(idx+1);
         new_signature.visitInnerClassType(nm);
         if (end) new_signature.visitEnd();
       }
      else {
         String nm = typ.getJavaTypeName();
         if (nm.startsWith("L")) nm = nm.substring(1);
         if (nm.endsWith(";")) nm = nm.substring(0,nm.length()-1);
         new_signature.visitClassType(nm);
         if (end) new_signature.visitEnd();
       }
   }
}	// end of inner class GenericSignatureVisitor




/********************************************************************************/
/*										*/
/*	Visitor for getting type variables from a method signature		*/
/*										*/
/********************************************************************************/

private static class MethodVarFinder extends SignatureVisitor {

   private Map<String,JcompType> type_map;
   private JcompTyper type_data;
   private List<JcompType> input_parms;
   private List<JcompType> type_parms;
   private JcompType type_var;
   private List<TypeDeriver> type_bounds;
   private int cur_index; 
   private int var_index;

   MethodVarFinder(JcompTyper typer,List<JcompType> parms,Map<String,JcompType> typs,List<JcompType> typargs) {
      super(Opcodes.ASM6);
      type_data = typer;
      type_map = new HashMap<>();
      if (typs != null) type_map.putAll(typs);
      type_parms = typargs;
      type_var = null;
      type_bounds = null;
      input_parms = parms;
      cur_index = -1;
      var_index = 0;
    }

   Map<String,JcompType> getTypeMap() {
      return type_map;
    }

   @Override public void visitFormalTypeParameter(String name) {
      super.visitFormalTypeParameter(name);
      fixTypeVariable();
      if (type_parms != null && type_parms.size() > var_index) {
         type_map.put(name,type_parms.get(var_index));
         ++var_index;
       }
      else {
         type_var = JcompType.createVariableType(name);
         type_bounds = new ArrayList<>();
         type_map.put(name,type_var);
         ++var_index;
       }
    }
   
   @Override public SignatureVisitor visitClassBound() {
      if (type_var == null) return new SkipVisitor();
      TypeDeriver td = new TypeDeriver(type_data,null,type_map,null,null);
      type_bounds.add(td);
      return td;
    }
   
   @Override public SignatureVisitor visitInterfaceBound() {
      if (type_var == null) return new SkipVisitor();
      TypeDeriver td = new TypeDeriver(type_data,null,type_map,null,null);
      type_bounds.add(td);
      return td;
    }
   
   
   @Override public SignatureVisitor visitParameterType() {
      fixTypeVariable();
      ++cur_index;
      if (input_parms != null && cur_index < input_parms.size()) {
         JcompType bty = input_parms.get(cur_index);
         return new MethodVarTypeFinder(type_data,bty,type_map);
       }
      return new SkipVisitor();
    }

   @Override public SignatureVisitor visitReturnType() {
      fixTypeVariable();
      cur_index = -1;
      return new SkipVisitor();
    }

   private void fixTypeVariable() {
      if (type_var != null) {
         fixupTypeVariable(type_var,type_bounds);
       }
      type_var = null;
      type_bounds = null;
    }
   
}       // end of inner class MethodVarFinder


private static class MethodVarTypeFinder extends SignatureVisitor {

   private JcompTyper type_data;
   private Map<String,JcompType> type_map;
   private int parm_index;
   private JcompType base_type;
   private JcompType cur_type;
    
   MethodVarTypeFinder(JcompTyper typer,JcompType typ,Map<String,JcompType> typs) {
      super(Opcodes.ASM6);
      type_data = typer;
      type_map = typs;
      if (typ == null) typ = typer.findSystemType("java.lang.Object");
      if (typ.isFunctionRef()) typ = typ.getBaseType();
      base_type = typ;
      cur_type = base_type;
      parm_index = -1;
    }
   
   @Override public void visitTypeVariable(String name) {
      JcompType prior = type_map.get(name);
      if (prior != null && !prior.isTypeVariable()) return;
      else if (prior != null && prior.isTypeVariable() && base_type.isTypeVariable()) {
         // decide whether to use prior or new value
         if (prior == base_type) return;
       }
      type_map.put(name,base_type);
    }
   
   @Override public SignatureVisitor visitArrayType() {
      if (base_type.isArrayType()) {
         return new MethodVarTypeFinder(type_data,base_type.getBaseType(),type_map);
       }
      else return new SkipVisitor();
    }
   
   @Override public void visitClassType(String name) {
       JcompType ct = lookupClassType(type_data,name);
       cur_type = base_type;
       for (JcompType xt = base_type; xt != null; xt = xt.getOuterType()) {
          if (xt.isCompatibleWith(ct)) {
             cur_type = xt;
             break;
           }
        }
       parm_index = 0;
    }
   
   @Override public void visitTypeArgument() {
      ++parm_index;
    }
   
   @Override public SignatureVisitor visitTypeArgument(char wildcard) {
      int pidx = parm_index++;
      if (cur_type.isParameterizedType()) {
         List<JcompType> parms = cur_type.getComponents();
         if (pidx < parms.size()) {
            return new MethodVarTypeFinder(type_data,parms.get(pidx),type_map);
          }
       }
      else if (cur_type.isMethodType()) {
         List<JcompType> parms = cur_type.getComponents();
         if (pidx < parms.size()) {
            return new MethodVarTypeFinder(type_data,parms.get(pidx),type_map);
          }
         else if (pidx == parms.size()) {
            return new MethodVarTypeFinder(type_data,cur_type.getBaseType(),type_map);
          }
       }
      
      return new SkipVisitor();
    }
   
   @Override public void visitInnerClassType(String name) {
      JcompType ct = lookupClassType(type_data,name);
      cur_type = base_type;
      for (JcompType xt = base_type; xt != null; xt = xt.getOuterType()) {
         if (xt.isCompatibleWith(ct)) {
            cur_type = xt;
            parm_index = 0;
            break;
          }
       }
    }

}       // end of inner class MethodVarTypeFinder





/********************************************************************************/
/*										*/
/*	Type visitor to find super type 					*/
/*										*/
/********************************************************************************/

private static class SuperDeriver extends GenericSignatureVisitor {

   private JcompType super_type;
   private List<JcompType> interface_types;
   private TypeDeriver super_deriver;
   private List<TypeDeriver> iface_derivers;

   SuperDeriver(JcompTyper typer,JcompType base,SortedMap<String,JcompType> tmap,
         JcompType sty,Collection<JcompType> ifaces) {
      super(typer,base,tmap,null);
      if (base != null) {
         String tnm = base.getName();
         if (base.isParameterizedType()) tnm = base.getBaseType().getName();
         type_map.put(tnm,base);
       }
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
      super.visitSuperclass();
      super_deriver = createDeriver(super_type);
      return super_deriver;
    }

   @Override public SignatureVisitor visitInterface() {
      SignatureVisitor rslt = super.visitInterface();
      if (interface_types == null) return rslt;
      int idx = iface_derivers.size();
      TypeDeriver td = createDeriver(interface_types.get(idx));
      iface_derivers.add(td);
      return td;
    }

}	// end of inner class SuperDeriver




/********************************************************************************/
/*										*/
/*	Visitor to derive the actual method type given parameters		*/
/*										*/
/********************************************************************************/

private static class MethodDeriver extends GenericSignatureVisitor {

   private JcompType original_type;
   private List<JcompType> arg_types;
   private JcompType return_type;
   private List<TypeDeriver> arg_derivers;
   private TypeDeriver return_deriver;
   private int arg_index;

   MethodDeriver(JcompTyper typer,JcompType base,Map<String,JcompType> tmap,JcompType mty) {
      super(typer,base,tmap,new SignatureWriter());
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
      String nsgn = new_signature.toString();
      if (nsgn != null && !nsgn.equals(original_type.getSignature())) chng = true;
      if (return_deriver.isChanged()) chng = true;
      
      if (!chng) return original_type;
      // System.err.println("METHOD " + nsgn + " " + original_type.getSignature());
      
      JcompType jty = type_data.createMethodType(return_type,arg_types,
            original_type.isVarArgs(),nsgn);
      return jty;
    }

   @Override public SignatureVisitor visitParameterType() {
      super.visitParameterType();
      TypeDeriver tdv = createDeriver(arg_types.get(arg_index++));
      arg_derivers.add(tdv);
      return tdv;
    }

   @Override public SignatureVisitor visitReturnType() {
      super.visitReturnType();
      return_deriver = createDeriver(return_type);
      return return_deriver;
    }

   @Override public SignatureVisitor visitTypeArgument(char w) {
      new_signature.visitTypeArgument(w);
      return super.visitTypeArgument(w);
    }

}	// end of inner class MethodDeriver




/********************************************************************************/
/*										*/
/*	Type visitor to get an actual type					*/
/*										*/
/********************************************************************************/

private static class TypeDeriver extends GenericSignatureVisitor {

   private JcompType original_type;
   private JcompType new_type;
   private JcompType outer_type;
   private int array_count;
   private List<TypeDeriver> param_types;
   private boolean is_changed;

   TypeDeriver(JcompTyper typer,JcompType base,Map<String,JcompType> tmap,JcompType bty,SignatureWriter writer) {
      super(typer,base,tmap,writer);
      original_type = bty;
      param_types = null;
      array_count = 0;
      is_changed = false;
      skip_type = false;
      var_map = null;
      formal_type = null;
      bound_types = null;
      outer_type = null;
    }

   JcompType getResultType() {
      if (new_type == null) new_type = original_type;
      else {
         for (int i = 0; i < array_count; ++i) {
            new_type = type_data.findArrayType(new_type);
          }
       }
      if (new_type == null) return null;
      
      boolean needouter = false;
      if (outer_type != null && outer_type.isParameterizedType() &&
            (new_type.getOuterType() != outer_type || !new_type.isParameterizedType()))
         needouter = true;
      
      if (!needouter) {
         String tsign = new_type.getSignature();
         if (tsign == null || !tsign.contains("<") || 
               (new_type.isComplete() && new_type.isParameterizedType())) {
            if (!needouter) return new_type;
          }
         
         if (param_types == null || param_types.isEmpty()) {
            if (type_map == null || type_map.isEmpty()) return new_type;
          }
       }
      
      String xtnm = new_type.getName();
      if (new_type.isParameterizedType()) xtnm = new_type.getBaseType().getName();
      JcompType xjty = type_map.get(xtnm);
      if (xjty != null && !xjty.isTypeVariable()) 
         return xjty;
      
      List<JcompType> ptys = new ArrayList<>();
      int nvar = 0;
      if (param_types != null) {
         for (TypeDeriver tdv : param_types) {
            if (tdv.isChanged()) is_changed = true;
            JcompType jty = tdv.getResultType();
            if (jty == null || jty.isTypeVariable() || jty.isWildcardType()) ++nvar;
            if (jty == null || jty.isWildcardType())
               jty = type_data.ANY_TYPE;
            ptys.add(jty);
          }
       }
      if (ptys.size() > 0 && !new_type.isParameterizedType()) is_changed = true;
      if (original_type == null) is_changed = true;
   
      int act = 0;
      while (new_type.isArrayType()) {
         ++act;
         new_type = new_type.getBaseType();
       }
   
      if (nvar != 0 && nvar == param_types.size() && !needouter) ;
      else if (needouter) {
         SortedMap<String,JcompType> tmap = outer_type.getOuterComponents();
         JcompType jty = JcompType.createParameterizedType(new_type,ptys,tmap,type_data);
         jty.setOuterType(outer_type);
         new_type = jty; 
       }
      else if (ptys.size() > 0) {
         JcompType jty = new_type;
         if (jty.isParameterizedType()) jty = jty.getBaseType();
         new_type = JcompType.createParameterizedType(jty,ptys,type_map,type_data);
       }
      else {
         JcompType rtyp = JcompGenerics.findDerivedClassType(type_data,new_type,type_map);
         if (rtyp != null && rtyp != new_type)
            new_type = rtyp;
         // System.err.println("IGNORE " + new_type + " " + new_type.getSignature());
       }
   
      for (int i = 0; i < act; ++i) {
         new_type = type_data.findArrayType(new_type);
       }
   
      return new_type;
    }

   boolean isChanged()				{ return is_changed; }

   @Override public SignatureVisitor visitArrayType() {
      ++array_count;
      return super.visitArrayType();
    }


   @Override public void visitClassType(String name) {
      super.visitClassType(name);
      new_type = lookupClassType(type_data,name);
      if (new_type == null) {
         IvyLog.logD("JCOMP","TYPE " + name + " not found");
       }
    }

   @Override public void visitInnerClassType(String name) {
      if (param_types != null) {
         outer_type = getResultType();
         param_types = null;
       }
      else {
         outer_type = new_type;
       }
      if (outer_type != null) {
         String nm = outer_type.getName();
         if (outer_type.isParameterizedType()) nm = outer_type.getBaseType().getName();
         
         String fnm = nm + "." + name;
         JcompType nty = type_data.findSystemType(fnm);
         if (nty == null) {
            fnm = nm + "$" + name;
            nty = type_data.findSystemType(fnm);
          }
         if (nty == null) {
            System.err.println("Can't find inner type " + name + " " + fnm);
          }
         new_type = nty;
       }
      
      super.visitInnerClassType(name);
    }

   @Override public void visitTypeArgument() {
      super.visitTypeArgument();
      JcompType jty = type_data.findSystemType("java.lang.Object");
      TypeDeriver tdv = createDeriver(jty);
      if (param_types == null) param_types = new ArrayList<>();
      param_types.add(tdv);
    }

   @Override public SignatureVisitor visitTypeArgument(char w) {
      super.visitTypeArgument(w);
      TypeDeriver tdv = createDeriver(null);
      if (param_types == null) param_types = new ArrayList<>();
      param_types.add(tdv);
      return tdv;
    }

   @Override public void visitTypeVariable(String name) {
      super.visitTypeVariable(name);
      JcompType nty = lookupTypeVariable(name);
      if (nty == null) {
         nty = type_data.findType("java.lang.Object");
       }
      if (!nty.isTypeVariable() && nty.getJavaTypeName() != null)
         is_changed = true;
      new_type = nty;
    }


}	// end of inner class TypeDeriver




// /********************************************************************************/
// /*										   */
// /*	   Compute type variable type						   */
// /*										   */
// /********************************************************************************/
//
// private static class TypeVarDeriver extends GenericSignatureVisitor {
   //
   // private JcompType type_var;
   // private String var_name;
   //
   // TypeVarDeriver(JcompTyper typer,JcompType base,SortedMap<String,JcompType> tmap,String nm) {
      // super(typer,base,tmap,null);
      // var_name = nm;
      // type_var = null;
    // }
   //
   // JcompType getResultType() {
      // return type_var;
    // }
   //
   // @Override public void visitFormalTypeParameter(String name) {
      // super.visitFormalTypeParameter(name);
      // if (name.equals(var_name)) {
	 // type_var = formal_type;
       // }
      // else formal_type = null;
    // }
   //
// }	   // end of inner class TypeVarDeriver


}	// end of class JcompGenerics




/* end of JcompGenerics.java */
