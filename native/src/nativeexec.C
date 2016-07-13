/********************************************************************************/
/*										*/
/*		nativeexec.C							*/
/*										*/
/*	Native interface for edu.brown.cs.ivy.exec				*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 1998, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/native/src/nativeexec.C,v 1.3 2008-03-15 02:23:22 spr Exp $ */


/*********************************************************************************
 *
 * $Log: nativeexec.C,v $
 * Revision 1.3  2008-03-15 02:23:22  spr
 * New apple includes don't include kvm.
 *
 * Revision 1.2  2007-05-04 02:26:39  spr
 * Fix up apple-specific KVM code.
 *
 * Revision 1.1  2007-05-04 02:01:31  spr
 * Add code for native calls for finding arguments.
 *
 *
 ********************************************************************************/


#include "native_local.H"
#include <jni.h>


#ifdef APPLE_PPC
#include <kvm.h>
#include <sys/sysctl.h>
#define USE_KVM 1
#endif

#include <ivy_string.H>
#include <iostream>

using namespace Ivy;
using namespace Native;



/********************************************************************************/
/*										*/
/*	Forward Declarations							*/
/*										*/
/********************************************************************************/


extern "C" {
   jobjectArray Java_edu_brown_cs_ivy_exec_IvyExecQuery_getCommandLine(JNIEnv *,jclass);
}




/********************************************************************************/
/*										*/
/*	Routine to get the command line as a string				*/
/*										*/
/********************************************************************************/

jobjectArray
Java_edu_brown_cs_ivy_exec_IvyExecQuery_getCommandLine(JNIEnv * env,jclass)
{
   char ** argv = NULL;
   char * argvec[1024];
   char buf[10240];

#ifdef USE_KVM
   kvm_t * vm = kvm_open(NULL,NULL,NULL,O_RDONLY,"VJMTI: ");
   if (vm != NULL) {
      int cnt = 0;
      struct kinfo_proc * kin = kvm_getprocs(vm,KERN_PROC_PID,getpid(),&cnt);
      argv = kvm_getargv(vm,kin,1024);
    }
#endif

   if (argv == NULL) {
      char *** nxargvp = (char ***) dlsym(RTLD_DEFAULT,"_NXArgv");
      if (nxargvp == NULL) nxargvp = (char ***) dlsym(RTLD_DEFAULT,"NXArgv");
      if (nxargvp != NULL) {
	 argv = *nxargvp;
       }
    }

   if (argv == NULL) {
      StringBuffer s;
      s << "/proc/" << getpid() << "/cmdline";
      int fid = open(s.getCString(),O_RDONLY);
      if (fid >= 0) {
	 int ct = read(fid,buf,10240);
	 if (ct > 0) {
	    int act = 0;
	    argvec[act++] = &buf[0];
	    for (int i = 0; i < ct; ++i) {
	       if (buf[i] == 0 && i+1 < ct) argvec[act++] = &buf[i+1];
	     }
	    argvec[act] = NULL;
	    argv = argvec;
	  }
       }
    }

   int argc = 0;
   if (argv != NULL) {
      for (int i = 0; i < 1024; ++i) {
	 if (argv[i] == NULL) break;
	 ++argc;
       }
    }

   jclass scls = env->FindClass("java/lang/String");
   jobjectArray r = env->NewObjectArray(argc,scls,NULL);
   for (int i = 0; i < argc; ++i) {
      jstring s = env->NewStringUTF(argv[i]);
      env->SetObjectArrayElement(r,i,s);
    }

   return r;
}




/* end of nativeexec.C */
