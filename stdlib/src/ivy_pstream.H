/************************************************************************/
/*									*/
/*		ivy_pstream.H						*/
/*									*/
/*	Definitions for pipe-based stream				*/
/*									*/
/************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 2005, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_pstream.H,v 1.1 2005/06/28 17:22:11 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_pstream.H,v $
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_PSTREAM_ALREADY_INCLUDED
#define IVY_PSTREAM_ALREADY_INCLUDED

#include <ivy_datatypes.H>


#include <fstream>
#include <sys/types.h>
#include <sys/wait.h>
#include <errno.h>




/********************************************************************************/
/*										*/
/*	Constants								*/
/*										*/
/********************************************************************************/


const	char * const	IVY_PSTREAM_DFLT_SHELL = "/bin/sh";
const	char * const	IVY_PSTREAM_SHELL_FLG = "-f";
const	char * const	IVY_PSTREAM_SHELL_FLG1 = "-c";



/************************************************************************/
/*									*/
/*	IvyPstream -- generic pipe superclass			*/
/*									*/
/************************************************************************/


class IvyPipeStreamBase {

private:
   int in_fid;
   int out_fid;
   pid_t pipe_pid;

protected:
   inline IvyPipeStreamBase(const char * cmd,int mode,int std_err = 0);
   inline IvyPipeStreamBase(const char * exec,const char * const * args,int mode,int std_err = 0);
   ~IvyPipeStreamBase() 	 { pipeClose(); }

   int pipeInFileId() const		{ return in_fid; }
   int pipeOutFileId() const		{ return out_fid; }

   inline void pipeClose();
};




/************************************************************************/
/*									*/
/*	IvyIpstream -- use pipe for input from a command		*/
/*									*/
/************************************************************************/


class IvyIpstream : public IvyPipeStreamBase, public ifstream {

public:
   IvyIpstream(const char * cmd,int use_std_err = 0) :
      IvyPipeStreamBase(cmd,ios::in,use_std_err), ifstream(pipeInFileId()) { }
   IvyIpstream(const char * exec,const char * const * args,int use_std_err = 0) :
      IvyPipeStreamBase(exec,args,ios::in,use_std_err), ifstream(pipeInFileId()) { }

   void close()  {
      ifstream::close();
      pipeClose();
    }
};



/************************************************************************/
/*									*/
/*	IvyOpstream -- use pipe for output to a command 	*/
/*									*/
/************************************************************************/


class IvyOpstream : public IvyPipeStreamBase, public ofstream {

public:
   IvyOpstream(const char * cmd) :
      IvyPipeStreamBase(cmd,ios::out), ofstream(pipeOutFileId())  { }
   IvyOpstream(const char * exec,const char * const * args) :
      IvyPipeStreamBase(exec,args,ios::out), ofstream(pipeOutFileId()) { }

   void close()  {
      ofstream::close();
      pipeClose();
    }
};


/********************************************************************************/
/*										*/
/*	Inline implementations							*/
/*										*/
/********************************************************************************/

inline
IvyPipeStreamBase::IvyPipeStreamBase(const char * cmd,int mode,int ster)
{
   int i;
   int ip[2],op[2];
   const char * sh;
   const char *  shn;
   const char * args[32];
   int argc = 0;

   in_fid = -1;
   out_fid = -1;
   pipe_pid = -1;

   sh = getenv("SHELL");
   if (sh == NULL) sh = IVY_PSTREAM_DFLT_SHELL;
   shn = strrchr(sh,'/');
   if (shn != NULL) ++shn;
   else shn = sh;

   args[argc++] = shn;
   if (IVY_PSTREAM_SHELL_FLG != NULL) args[argc++] = IVY_PSTREAM_SHELL_FLG;
   if (IVY_PSTREAM_SHELL_FLG1 != NULL) args[argc++] = IVY_PSTREAM_SHELL_FLG1;
   args[argc++] = cmd;
   args[argc] = NULL;

   if (mode & ios::in) pipe(ip);
   if (mode & ios::out) pipe(op);

   pipe_pid = fork1();

   if (pipe_pid < 0) return;		// let someone else deal with the error

   if (pipe_pid == 0) {
      if (mode&ios::in) {
	 dup2(ip[1],1);
	 if (ster) dup2(ip[1],2);
       }
      if (mode&ios::out) {
	 dup2(op[0],0);
       }
      for (i = 3; i < 256; ++i) close(i);
      execvp(sh,(char * const *) args);
      _exit(1);
    }

   if (mode&ios::in) {
      in_fid = ip[0];
      close(ip[1]);
    }
   if (mode&ios::out) {
      out_fid = op[1];
      close(op[0]);
    }
}



inline
IvyPipeStreamBase::IvyPipeStreamBase(const char * exec,const char * const * args,int mode,
					      int ster)
{
   int i;
   int ip[2],op[2];

   in_fid = -1;
   out_fid = -1;
   pipe_pid = -1;

   if (mode & ios::in) pipe(ip);
   if (mode & ios::out) pipe(op);

   pipe_pid = fork1();

   if (pipe_pid < 0) return;		// let someone else deal with the error

   if (pipe_pid == 0) {
      if (mode&ios::in) {
	 dup2(ip[1],1);
	 if (ster) dup2(ip[1],2);
       }
      if (mode&ios::out) {
	 dup2(op[0],0);
       };
      for (i = 3; i < 256; ++i) close(i);
      execvp(exec,(char * const *) args);
      _exit(1);
    }

   if (mode&ios::in) {
      in_fid = ip[0];
      close(ip[1]);
    }
   if (mode&ios::out) {
      out_fid = op[1];
      close(op[0]);
    }
}




inline void
IvyPipeStreamBase::pipeClose()
{
   pid_t r;

   if (out_fid >= 0) {
      close(out_fid);
      out_fid = -1;
    }

   if (pipe_pid > 0) {
      for ( ; ; ) {
	 r = waitpid(pipe_pid,0,0);
	 if (r == pipe_pid) break;
	 if (r < 0 && errno != EINTR) break;
       };
      pipe_pid = -1;
    }

   if (in_fid >= 0) {
      close(in_fid);
      in_fid = -1;
    }
}




#endif


/* end of ivy_pstream.H */
