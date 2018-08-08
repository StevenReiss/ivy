/********************************************************************************/
/*										*/
/*		FlowMaster.java 						*/
/*										*/
/*	Implementation of the control flow analyzer master			*/
/*										*/
/********************************************************************************/
/*	Copyright 2006 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2006, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/

/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/jflow/flow/FlowMaster.java,v 1.17 2018/08/02 15:10:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: FlowMaster.java,v $
 * Revision 1.17  2018/08/02 15:10:17  spr
 * Fix imports.
 *
 * Revision 1.16  2015/11/20 15:09:14  spr
 * Reformatting.
 *
 * Revision 1.15  2015/03/31 02:19:18  spr
 * Formatting cleanup
 *
 * Revision 1.14  2013/09/24 01:06:56  spr
 * Minor fix
 *
 * Revision 1.13  2012-08-29 01:40:51  spr
 * Code cleanup for new compiler.
 *
 * Revision 1.12  2011-04-16 01:02:50  spr
 * Fixes to jflow for casting.
 *
 * Revision 1.11  2008-03-14 12:26:14  spr
 * Code cleanup.
 *
 * Revision 1.10  2007-08-10 02:10:39  spr
 * Cleanups from eclipse; fixups for paca.
 *
 * Revision 1.9  2007-05-04 01:59:58  spr
 * Update jflow with generic value/source flags.
 *
 * Revision 1.8  2007-02-27 18:53:29  spr
 * Add check direct option.  Get a better null/non-null approximation.
 *
 * Revision 1.7  2007-01-03 14:04:59  spr
 * Fix imports
 *
 * Revision 1.6  2007-01-03 03:24:18  spr
 * Modifications to handle incremental update.
 *
 * Revision 1.5  2006-11-09 00:33:11  spr
 * Use common base path computation.
 *
 * Revision 1.4  2006-08-03 12:34:51  spr
 * Ensure fields of unprocessed classes handled correctly.
 *
 * Revision 1.3  2006/07/10 14:52:17  spr
 * Code cleanup.
 *
 * Revision 1.2  2006/07/03 18:15:23  spr
 * Efficiency improvements; inlining options.
 *
 * Revision 1.1  2006/06/21 02:18:34  spr
 * Initial refactoring of flow analysis from clime/chet to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.jflow.flow;


import edu.brown.cs.ivy.cinder.CinderManager;
import edu.brown.cs.ivy.cinder.CinderPatchType;
import edu.brown.cs.ivy.jflow.JflowControl;
import edu.brown.cs.ivy.jflow.JflowException;
import edu.brown.cs.ivy.jflow.JflowMaster;
import edu.brown.cs.ivy.jflow.JflowMethod;
import edu.brown.cs.ivy.jflow.JflowMethodData;
import edu.brown.cs.ivy.jflow.JflowModelSource;
import edu.brown.cs.ivy.jflow.JflowSource;
import edu.brown.cs.ivy.jflow.JflowValue;
import edu.brown.cs.ivy.xml.IvyXml;

import com.ibm.jikesbt.BT_Class;
import com.ibm.jikesbt.BT_Field;
import com.ibm.jikesbt.BT_Ins;
import com.ibm.jikesbt.BT_InsVector;
import com.ibm.jikesbt.BT_Method;
import com.ibm.jikesbt.BT_Opcodes;

import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;




public class FlowMaster implements JflowMaster, BT_Opcodes
{




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private JflowControl using_factory;
private ValueFactory value_factory;
private MethodFactory method_factory;
private SourceFactory source_factory;
private SourceSetFactory set_factory;
private ProtoFactory proto_factory;

private String start_class;
private String class_path;
private String base_path;
private Set<String> class_set;
private List<String> special_files;
private FlowDetails detail_data;
private boolean do_inlining;
private boolean do_detailing;
private boolean do_prototypes;
private boolean do_strings;
private boolean do_auto_fields;
private boolean do_check_direct;
private CinderManager cinder_manager;
private JflowMaster.ProjectFilter project_filter;
private Set<BT_Class> used_classes;
private Map<JflowModelSource,SourceBase> model_sources;

private FlowControl flow_control;
private FlowAddons flow_addons;
private FlowIncremental flow_incremental;

private static boolean do_debug = false;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public FlowMaster(JflowControl factory)
{
   using_factory = factory;
   value_factory = new ValueFactory(this);
   method_factory = new MethodFactory(this);
   source_factory = new SourceFactory(this);
   set_factory = new SourceSetFactory(this);
   proto_factory = null;

   class_path = ".";
   base_path = null;
   class_set = new LinkedHashSet<String>();
   special_files = new ArrayList<String>();
   used_classes = new HashSet<BT_Class>();
   model_sources = new HashMap<JflowModelSource,SourceBase>();
   start_class = null;

   detail_data = null;

   do_inlining = true;
   do_detailing = true;
   do_prototypes = true;
   do_strings = false;
   do_auto_fields = true;
   do_check_direct = true;

   cinder_manager = null;
   flow_control = null;
   flow_addons = null;

   project_filter = new DefaultProjectFilter();
   flow_incremental = new FlowIncremental(this);

   base_path = CinderManager.computeBasePath();
}



/********************************************************************************/
/*										*/
/*	Path management methods 						*/
/*										*/
/********************************************************************************/

@Override public void setClassPath(String cp)
{
   class_path = cp;
}



@Override public void addToClassPath(String cp)
{
   if (cp == null) return;
   else if (class_path == null) class_path = cp;
   else {
      if (!class_path.endsWith(File.pathSeparator)) class_path += File.pathSeparator;
      class_path += cp;
    }
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

@Override public void setStartClass(String c)
{
   if (c == null) return;

   start_class = c;
   addClass(c);
}



@Override public void setOption(FlowOption opt,boolean fg)
{
   System.err.println("FLOW: Set option " + opt + " = " + fg);

   switch (opt) {
      case DO_INLINING :
	 do_inlining = fg;
	 break;
      case DETAIL_FIELDS :
	 do_detailing = fg;
	 break;
      case DO_PROTOTYPES :
	 do_prototypes = fg;
	 break;
      case DO_STRINGS :
	 do_strings = fg;
	 break;
      case DO_AUTO_FIELDS :
	 do_auto_fields = fg;
	 break;
      case DO_DEBUG :
      case DO_DEBUG_FLOW :
	 do_debug = fg;
	 break;
      case DO_CHECK_DIRECT :
	 do_check_direct = fg;
	 break;
      default:
	 break;
    }
}




@Override public void addDescriptionFile(String f)
{
   if (f == null) return;

   special_files.add(f);
}



@Override public void addDefaultDescriptionFile()
{
   addDescriptionFile(JFLOW_DEFAULT_DESCRIPTION_FILE);
}



@Override public void addClass(String cls)
{
   if (cls == null) return;

   class_set.add(cls);
}



@Override public void setProjectFilter(JflowMaster.ProjectFilter pf)
{
   if (pf == null) pf = new DefaultProjectFilter();
   project_filter = pf;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public Iterable<BT_Class> getAllClasses()
{
   return cinder_manager.getAllClassSet();
}


@Override public Collection<JflowMethod> getStartMethods()
{
   Collection<JflowMethod> rslt = new ArrayList<JflowMethod>();

   for (BT_Method bm : flow_control.getStartMethods()) {
      for (MethodBase mb : getAllMethods(bm)) {
	 rslt.add(mb);
       }
    }

   return rslt;
}



@Override public boolean isStartMethod(JflowMethod jm)
{
   return flow_control.isStartMethod(jm);
}


@Override public int findLineNumber(JflowMethod cm,int ino)
{
   return flow_control.findLineNumber((MethodBase) cm,ino);
}


@Override public Iterable<JflowMethod> getCallbacks(String id)
{
   return flow_control.getCallbacks(id);
}



@Override public Iterable<JflowMethod> getStaticInitializers()
{
   return flow_control.getStaticInitializers();
}



@Override public boolean isMethodUsed(JflowMethod jm)
{
   return flow_control.getFlowQueue((MethodBase) jm) != null;
}



@Override public boolean isInstructionUsed(JflowMethod jm,BT_Ins ins)
{
   FlowQueue fq = flow_control.getFlowQueue((MethodBase) jm);
   if (fq == null) return false;

   StateBase st1 = fq.getState(ins);

   return st1 != null;
}



/********************************************************************************/
/*										*/
/*	Analysis methods							*/
/*										*/
/********************************************************************************/

@Override public void setupAnalysis()
{
   if (cinder_manager == null) {
      CinderPatchType cpt = new CinderPatchType();
      cpt.setPatchEntry(true,0);

      String ncp = class_path;
      if (ncp == null) ncp = ".";
      ncp += File.pathSeparator + JFLOW_DUMMY_JAR_FILE;

      if (do_debug) {
	 System.err.println("Base path: " + base_path);
	 System.err.println("Class path: " + ncp);
       }

      cinder_manager = new CinderManager();
      CinderManager.setClassPath(base_path,ncp);
      cinder_manager.setPatchAll(true);
      cinder_manager.setPatchType(cpt);
      proto_factory = new ProtoFactory(this);
    }
}




@Override public void analyze() throws JflowException
{
   setupAnalysis();

   for (Iterator<String> it = class_set.iterator(); it.hasNext(); ) {
      String s = it.next();
      if (!CinderManager.checkIfClassExists(s)) {	// forces class to be loaded
	 System.err.println("JFLOW: Class " + s + " not found");
	 it.remove();
       }
    }

   if (class_set.isEmpty()) throw new JflowException("No valid classes to analyze");

   processSpecialFiles();

   flow_control = new FlowControl(this);
   flow_addons = new FlowAddons(this);

   cinder_manager.getPatchType().setInstrumenter(flow_control);

   for (String cnm : class_set) {
      cinder_manager.processClass(cnm,null);
    }

   flow_control.finish();

   checkAnalysis();
}



void analyzeUpdate(Collection<SourceBase> oldsrcs) throws JflowException
{
   if (cinder_manager == null) {
      throw new JflowException("Update called without prior analyze");
    }

   if (oldsrcs != null) {
      Map<SourceSet,SourceSet> srcupdates = set_factory.removeSources(oldsrcs);
      if (srcupdates != null) {
	 Map<JflowValue,JflowValue> valupdates = value_factory.handleSourceSetUpdate(srcupdates);
	 for (BT_Class bc : used_classes) {
	    for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
	       BT_Method bm = (BT_Method) e.nextElement();
	       for (MethodBase mb : getAllMethods(bm)) {
		  mb.handleUpdates(oldsrcs,srcupdates,valupdates);
		}
	     }
	  }
	 source_factory.handleUpdates(oldsrcs,srcupdates,valupdates);
	 flow_control.handleUpdates(oldsrcs,srcupdates,valupdates);
       }
    }

   flow_control.finish();

   checkAnalysis();
}



@Override public void cleanup(){
   flow_control.cleanup();
   value_factory.resetValues();
}




private void checkAnalysis()
{
   FlowCleanup fc = new FlowCleanup(this,flow_control);

   for (String s : class_set) {
      BT_Class bc = BT_Class.forName(s);
      for (Enumeration<?> e = bc.getMethods().elements(); e.hasMoreElements(); ) {
	 BT_Method bm = (BT_Method) e.nextElement();
	 fc.checkMethod(bm);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Incremental update methods						*/
/*										*/
/********************************************************************************/

@Override public void noteChanged(File f)
{
   if (flow_incremental != null) flow_incremental.noteChanged(f);
}



@Override public void updateChanged() throws JflowException
{
   if (flow_incremental != null) flow_incremental.updateChanged();
}




@Override public void noteChanged(BT_Method bm)
{
   if (flow_incremental != null) flow_incremental.noteChanged(bm);
}



@Override public void addFieldChecks(BT_Field f)
{
   flow_addons.addFieldChecks(f);
}


void handleChangedMethod(BT_Method bm)
{
   flow_control.queueChangedMethod(bm);
}



void handleChangedArray(ValueBase vb)
{
   flow_control.noteArrayChange(vb);
}


/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

private void processSpecialFiles()
{
   if (detail_data == null) detail_data = new FlowDetails(this);

   for (String f : special_files) {
      File ff = new File(f);
      if (ff.exists()) {
	 Element xml = IvyXml.loadXmlFromFile(f);
	 if (xml != null && !IvyXml.isElement(xml,"JFLOW")) {
	    xml = IvyXml.getElementByTag(xml,"JFLOW");
	  }
	 if (xml != null) {
	    try {
	       method_factory.addSpecialFile(this,cinder_manager,xml);
	       detail_data.addDetails(xml);
	       proto_factory.addPrototypes(xml);
	     }
	    catch (JflowException e) {
	       System.err.println("JFLOW: Problem reading special file " + f + ": " +
				     e.getMessage());
	     }
	  }
       }
    }

   special_files.clear();
}



/********************************************************************************/
/*										*/
/*	Internal access methods 						*/
/*										*/
/********************************************************************************/

@Override public boolean isProjectClass(BT_Class bc)
{
   while (bc.isArray()) bc = bc.arrayType;
   if (bc.isPrimitive()) return false;

   return project_filter.isProjectClass(bc.getName());
}



InlineType inlineMethod(BT_Method bm,List<ValueBase> _args)
{
   if (!do_inlining) return InlineType.DEFAULT;

   if (bm.isClassMethod()) return InlineType.NONE;
   if (bm.isStaticInitializer()) return InlineType.NONE;
   if (bm.isNative() || bm.isMain()) return InlineType.NONE;
   if (bm.isAbstract()) return InlineType.DEFAULT;
   if (canBeCallback(bm)) return InlineType.DEFAULT;
   if (bm.isStatic() && bm.getName().equals("main")) return InlineType.NONE;

   return detail_data.isMethodInlined(bm);
}



boolean detailField(BT_Field bf)
{
   if (!do_detailing) return false;

   if (bf.isStatic()) return false;

   return detail_data.isFieldDetailed(bf);
}



static boolean doDebug()			{ return do_debug; }



boolean useLocalSource(MethodBase m,BT_Class c)
{
   return detail_data.useLocalSource(m.getMethod(),c);
}



boolean isFieldSourceNeeded(MethodBase m,BT_Ins ins)
{
   BT_Field fld = ins.getFieldTarget();
   if (fld == null) return false;

   if (using_factory.isFieldTracked(fld)) return true;

   BT_Class fcls = fld.getDeclaringClass();
   if (!isProjectClass(fcls)) return false;
   if (!do_auto_fields) return false;

   BT_InsVector iv = m.getCodeVector();
   int idx = iv.indexOf(ins);
   if (idx+1 >= iv.size()) return false;
   BT_Ins nins = iv.elementAt(idx+1);
   switch (nins.opcode) {
      case opc_ifeq :
      case opc_ifne :
      case opc_ifnull :
      case opc_ifnonnull :
	 break;
      default :
	 return false;
    }

   // check for if following and possible event after that

   return false;
}


boolean useCheckDirect()				{ return do_check_direct; }



/********************************************************************************/
/*										*/
/*	Used class set maintenance						*/
/*										*/
/********************************************************************************/

public void noteClassUsed(BT_Class bc)
{
   if (used_classes.add(bc)) {
      for (Enumeration<?> e = bc.getParents().elements(); e.hasMoreElements(); ) {
	 BT_Class pbc = (BT_Class) e.nextElement();
	 noteClassUsed(pbc);
       }
    }

   if (isProjectClass(bc)) {
      class_set.add(bc.getName());
    }
}



@Override public boolean isMethodAccessible(BT_Method bm)
{
   if (bm.isStatic() || bm.isAbstract()) return true;

   BT_Class bc = bm.getDeclaringClass();

   if (used_classes.contains(bc)) return true;
   if (bc.isInterface()) return true;

   return false;
}



public boolean isValidStart(BT_Class bc)
{
   if (start_class == null) return true;

   if (bc.getName().equals(start_class)) return true;

   return false;
}



void noteMethodUsed(BT_Method bm)
{
   if (flow_incremental != null) flow_incremental.noteMethodUsed(bm);
}



/********************************************************************************/
/*										*/
/*	Factory methods for sources						*/
/*										*/
/********************************************************************************/

@Override public JflowSource findProgramReturnSource()
{
   return source_factory.getProgramReturnSource();
}


@Override public JflowSource findProgramThreadSource(String id)
{
   return source_factory.getProgramThreadSource(id);
}


@Override public JflowSource findFieldSource(BT_Field fld,JflowValue v)
{
   return source_factory.createFieldSource(fld,(ValueBase) v);
}



SourceBase createArraySource(BT_Class base,ValueBase v)
{
   return source_factory.createArraySource(base,v);
}


SourceBase createFieldSource(BT_Field fld,ValueBase v)
{
   return source_factory.createFieldSource(fld,v);
}


SourceBase createFixedSource(BT_Class cls)
{
   return source_factory.createFixedSource(cls);
}


SourceBase createMutableSource(BT_Class cls)
{
   return source_factory.createMutableSource(cls);
}



List<SourceBase> getLocalSources(BT_Class c)
{
   return source_factory.getLocalSources(c);
}



@Override public JflowSource createBadSource()
{
   return createLocalSource(null,0,BT_Class.getVoid(),false);
}


SourceBase createLocalSource(MethodBase m,int ino,BT_Class bc,boolean uniq)
{
   return source_factory.createLocalSource(m,ino,bc,uniq);
}


SourceBase createStringSource(String v)
{
   return source_factory.createStringSource(v);
}



SourceBase createPrototypeSource(MethodBase m,int ino,BT_Class bc,ProtoBase bp)
{
   return source_factory.createPrototypeSource(m,ino,bc,bp);
}



SourceBase createPrototypeSource(BT_Class c,ProtoBase p)
{
   return source_factory.createPrototypeSource(null,-1,c,p);
}



SourceBase getSource(int id)
{
   return source_factory.getSource(id);
}



JflowModelSource createModelSource(MethodBase jm,int ino,BT_Ins ins,ValueBase base)
{
   if (using_factory != null) {
      return using_factory.createModelSource(jm,ino,ins,base);
    }

   return null;
}


void recordModelSource(JflowModelSource ms,SourceBase sb)
{
   model_sources.put(ms,sb);
}



@Override public JflowSource findModelSource(JflowModelSource ms)
{
   return model_sources.get(ms);
}




JflowMethodData createMethodData(MethodBase m)
{
   if (using_factory != null) {
      return using_factory.createMethodData(m);
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Factory methods for values						*/
/*										*/
/********************************************************************************/

ValueBase anyValue(BT_Class typ)
{
   return value_factory.anyValue(typ);
}


ValueBase rangeValue(BT_Class typ,long v0,long v1)
{
   return value_factory.rangeValue(typ,v0,v1);
}


ValueBase objectValue(BT_Class typ,SourceSet cs,short flags)
{
   return value_factory.objectValue(typ,cs,flags);
}


ValueBase emptyValue(BT_Class typ,short flags)
{
   return value_factory.emptyValue(typ,flags);
}


ValueBase constantString(String v)
{
   if (do_strings) return value_factory.constantString(v);

   return value_factory.constantString();
}


ValueBase mainArgs()
{
   return value_factory.mainArgs();
}



ValueBase nullValue()
{
   return value_factory.nullValue();
}


ValueBase nullValue(BT_Class typ)
{
   return value_factory.nullValue(typ);
}



ValueBase badValue()
{
   return value_factory.badValue();
}



ValueBase nativeValue(BT_Class typ)
{
   return value_factory.nativeValue(typ);
}


ValueBase mutableValue(BT_Class typ)
{
   return value_factory.mutableValue(typ);
}


ValueBase anyObject(BT_Class typ)
{
   return value_factory.anyObject();
}


ValueBase anyNewObject(BT_Class typ)
{
   return value_factory.anyNewObject();
}


ValueBase initialFieldValue(BT_Field fld,boolean nat)
{
   return value_factory.initialFieldValue(fld,nat);
}



ValueBase getExitException()
{
   return value_factory.nativeValue(BT_Class.forName("java.lang.ExitException"));
}



/********************************************************************************/
/*										*/
/*	Method factory methods							*/
/*										*/
/********************************************************************************/

MethodBase findMethod(BT_Method bm,List<ValueBase> args,InlineType inline)
{
   return method_factory.findMethod(bm,args,inline);
}


MethodBase findMethod(BT_Method bm)
{
   return method_factory.findMethod(bm,null,InlineType.NONE);
}


MethodBase findPrototypeMethod(BT_Method bm)
{
   return method_factory.findPrototypeMethod(bm);
}


Iterable<MethodBase> getAllMethods(BT_Method bm)
{
   return method_factory.getAllMethods(bm);
}


@Override public JflowMethod createMetaMethod(BT_Method bm)
{
   return method_factory.createMetaMethod(bm);
}


@Override public Iterable<JflowMethod> findAllMethods(BT_Method bm)
{
   Collection<JflowMethod> rslt = new ArrayList<JflowMethod>();
   for (MethodBase mb : method_factory.getAllMethods(bm)) rslt.add(mb);
   return rslt;
}



/********************************************************************************/
/*										*/
/*	Source Set factory methods						*/
/*										*/
/********************************************************************************/

SourceSet createEmptySet()
{
   return set_factory.createEmptySet();
}



SourceSet createSingletonSet(SourceBase s)
{
   return set_factory.createSingletonSet(s);
}



/********************************************************************************/
/*										*/
/*	Prototype factory methods						*/
/*										*/
/********************************************************************************/

ProtoBase createPrototype(BT_Class bc)
{
   if (!do_prototypes) return null;

   return proto_factory.createPrototype(bc);
}



/********************************************************************************/
/*										*/
/*	Special method query methods						*/
/*										*/
/********************************************************************************/

boolean canBeCallback(BT_Method bm)
{
   return MethodSpecial.canBeCallback(bm);
}


boolean getIsArrayCopy(BT_Method bm)
{
   MethodSpecial ms = MethodSpecial.getSpecial(bm);
   if (ms != null) return ms.getIsArrayCopy();

   return false;
}



boolean getUseReflection(BT_Method bm,MethodBase caller,int ino)
{
   MethodSpecial ms = MethodSpecial.getSpecial(bm);
   if (ms != null) return ms.getUseReflection(caller,ino);

   return false;
}



@Override public boolean canBeReplaced(BT_Method bm)
{
   MethodSpecial ms = MethodSpecial.getSpecial(bm);
   if (ms != null) return ms.getReplaceName() != null;

   return false;
}


@Override public String getCallbackStart(BT_Method bm)
{
   MethodSpecial ms = MethodSpecial.getSpecial(bm);
   if (ms != null && ms.getCallbacks() != null) {
      return ms.getCallbackId();
    }

   return null;
}


/********************************************************************************/
/*										*/
/*	Default project filter class						*/
/*										*/
/********************************************************************************/


private static String [] library_prefix = new String [] {
   "java.","javax.","com.","org.","sun."
};


private static class DefaultProjectFilter implements JflowMaster.ProjectFilter {

   @Override public boolean isProjectClass(String cls) {
      for (int i = 0; i < library_prefix.length; ++i) {
	 if (cls.startsWith(library_prefix[i])) return false;
       }
      return true;
    }

}	// end of subclass DefaultProjectFilter





}	// end of interface FlowMaster





/* end of FlowMaster.java */

