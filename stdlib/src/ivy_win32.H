/************************************************************************/
/*									*/
/*		ivy_win32.H						*/
/*									*/
/*	Definitions for compiling Ivy under windows			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_win32.H,v 1.3 2012-10-05 00:47:07 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_win32.H,v $
 * Revision 1.3  2012-10-05 00:47:07  spr
 * Try getting windows to compile (not there yet).
 *
 * Revision 1.2  2009-03-20 02:00:57  spr
 * Handle windows files/sockets/etc.
 *
 * Revision 1.1  2005/06/28 17:22:12  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_WIN32_ALREADY_INCLUDED
#define IVY_WIN32_ALREADY_INCLUDED

#ifdef WIN32

#include <fcntl.h>
#include <io.h>
#include <direct.h>
// #include <afxwin.h>
#include <process.h>
#include <sys/stat.h>
#include <sys/timeb.h>
// #include <Winsock2.h>


// #using <mscorlib.dll>



/************************************************************************/
/*									*/
/*	Set flags for other modules					*/
/*									*/
/************************************************************************/


#define NO_PIPES	1
#define _USE_MATH_DEFINES 1



/************************************************************************/
/*									*/
/*	Set options for use later on					*/
/*									*/
/************************************************************************/

typedef int pid_t;



typedef __int64 		LongLong;
typedef unsigned __int64	ULongLong;




/************************************************************************/
/*									*/
/*	Missing or renamed functions					*/
/*									*/
/************************************************************************/


inline int access(const char * nm,int md)	{ return _access(nm,md); }

inline int mkdir(const char * nm,int md)
{
   int sts = _mkdir(nm);
   if (sts == 0) sts = _chmod(nm,md);
   return sts;
}

inline int open(const char * nm,int fg,int md)	{ return _open(nm,fg,md); }

inline int rmdir(const char * nm)		{ return _rmdir(nm); }

inline long lrand48()				{ return rand() * (RAND_MAX+1) + rand(); }


inline int waitpid(int pid,int * statloc,int opts)
{
   return _cwait(statloc,pid,_WAIT_CHILD);
}

inline int getpagesize()			{ return 4096; }

inline unsigned int sleep(unsigned int sec)
{
   ::Sleep(sec*1000);
   return 0;
}



inline unsigned int usleep(unsigned int usec)
{
   ::Sleep(usec/1000);
   return 0;
}




/********************************************************************************/
/*										*/
/*	File access definitions 						*/
/*										*/
/********************************************************************************/

inline int S_ISDIR(int mode)
{
   return ((mode & _S_IFMT) == _S_IFDIR);
}


inline int S_ISLNK(int mode)
{
   return false;
}


#define F_OK	0
#define X_OK	1
#define W_OK	2
#define R_OK	4

#define O_LARGEFILE	0

#define RTLD_LAZY	1
#define RTLD_NOW	2
#define RTLD_NOLOAD	4
#define RTLD_GLOBAL	0x100
#define RTLD_LOCAL	0
#define RTLD_PARENT	0x200
#define RTLD_GROUP	0x400
#define RTLD_WORLD	0x800
#define RTLD_NODELETE	0x1000


inline void * dlopen(const char * file,int fgs)
{
   return (void *) LoadLibrary(file);
}



inline int mkstemp(char * templ)
{
   int ln = strlen(templ);
   if (ln > 6 && strcmp(&templ[ln-6],"XXXXXX") == 0) {
      templ[ln-6] = 0;
    }
   char * rslt = _tempnam("C:\tmp",templ);
   strcpy(templ,rslt);
   ::free(rslt);
   return ::open(templ,O_WRONLY|O_CREAT|O_EXCL,0666);
}



inline int pipe(int * fd)
{
   _pipe(fd,8192,O_BINARY);
}



/********************************************************************************/
/*										*/
/*	Shared memory definitions						*/
/*										*/
/********************************************************************************/


#define PROT_READ	0x1
#define PROT_WRITE	0x2

#define MAP_SHARED	1
#define MAP_PRIVATE	2
#define MAP_FAILED	((void *) -1)


inline void * mmap(void * addr,size_t len,int prot,int flags,int fildes,LongLong off)
{
   HANDLE hfile = (HANDLE) fildes;
   DWORD protect = 0;
   DWORD maxsizehigh;
   DWORD maxsizelow;

   if (prot == PROT_READ) protect = PAGE_READONLY;
   else protect = PAGE_READWRITE;
   if ((flags & MAP_PRIVATE) != 0) protect = PAGE_WRITECOPY;

   maxsizehigh = 0;
   maxsizelow = 1024*1024*1024;
   if (len > maxsizelow) maxsizelow = len;

   HANDLE hmap = CreateFileMapping(hfile,NULL,protect,maxsizehigh,maxsizelow,NULL);
   DWORD acc;

   if (prot == PROT_READ) acc = FILE_MAP_READ;
   else if ((flags & MAP_PRIVATE) != 0) acc = FILE_MAP_COPY;
   else acc = FILE_MAP_ALL_ACCESS;

   DWORD offsethigh = (DWORD)(off >> 32);
   DWORD offsetlow = (DWORD)(off & 0xffffffff);
   SIZE_T size = len;

   PVOID rslt = MapViewOfFile(hmap,acc,offsethigh,offsetlow,size);

   return rslt;
}



inline int munmap(void * addr,size_t len)
{
   BOOL fg = UnmapViewOfFile(addr);

   return (fg ? 0 : 1);
}




/********************************************************************************/
/*										*/
/*	Time routines								*/
/*										*/
/********************************************************************************/


typedef LongLong	hrtime_t;


inline LongLong
gethrtime()
{
   struct _timeb tb;

   _ftime(&tb);

   LongLong v = tb.time * 1000 * tb.millitm;
   v *= 1000*1000;

   return v;
}




/************************************************************************/
/*									*/
/*	System-specific routines					*/
/*									*/
/************************************************************************/


inline bool processExists(int pid)
{
   if (::WaitForSingleObject(((HANDLE) pid),0) == WAIT_TIMEOUT) return true;
   return false;
}



/********************************************************************************/
/*										*/
/*	GL definitions								*/
/*										*/
/********************************************************************************/

#define GL_UNSIGNED_INT_8_8_8_8 	GL_UNSIGNED_BYTE



#endif
#endif



/* end of ivy_win32.H */
