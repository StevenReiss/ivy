/************************************************************************/
/*									*/
/*		ivy_fileio.H						*/
/*									*/
/*	Local file I/O methods						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_fileio.H,v 1.3 2013/11/08 01:38:54 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_fileio.H,v $
 * Revision 1.3  2013/11/08 01:38:54  spr
 * Remove unnecesary statement
 *
 * Revision 1.2  2006/02/21 17:08:33  spr
 * Add default comparator for Sets.
 *
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_FILEIO_INCLUDED_ALREADY
#define IVY_FILEIO_INCLUDED_ALREADY


#include <ivy_datatypes.H>
#include <ivy_memory.H>

#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <stdio.h>



namespace Ivy {


/********************************************************************************/
/*										*/
/*	Forward Definitions							*/
/*										*/
/********************************************************************************/

typedef class IvyInputFileInfo *	IvyInputFile;
typedef class IvyOutputFileInfo *	IvyOutputFile;


const	char * const	IVY_FILE_DEFAULT_SHELL =	"/bin/sh";
const	char * const	IVY_FILE_SHELL_FLG =		"-c";



/********************************************************************************/
/*										*/
/*	IvyInputFileInfo -- implementation of an input file			*/
/*										*/
/********************************************************************************/


class IvyInputFileInfo {

private:
   char * file_buffer;
   int file_fd;
   int buffer_size;
   char * input_ptr;
   char * buffer_end;
   pid_t pipe_pid;
   bool end_of_file;
   LongLong base_position;

public:
   IvyInputFileInfo(int bsize = 0) {
      initialize(bsize);
    }

   IvyInputFileInfo(CStdString fname,int bsize = 0) {
      initialize(bsize);
      open(fname);
    }

   ~IvyInputFileInfo() {
      close();
    }


   Bool open(CStdString name) {
      close();
      file_fd = ::open(name.c_str(),O_RDONLY|O_LARGEFILE);
      if (file_fd < 0) return FALSE;
      end_of_file = false;
      return TRUE;
    }

   Bool open(int fd) {
      close();
      file_fd = fd;
      if (file_fd < 0) return FALSE;
      end_of_file = false;
      return TRUE;
    }

   void setBufferSize(int bsize) {
      if (file_buffer != NULL) return;
      buffer_size = bsize;
    }

   void close() {
      if (file_fd >= 0) ::close(file_fd);
      if (file_buffer != NULL) delete [] file_buffer;
      if (pipe_pid > 0) {
	 for ( ; ; ) {
	    pid_t r = waitpid(pipe_pid,0,0);
	    if (r == pipe_pid) break;
	    if (r < 0 && errno != EINTR) break;
	  }
       }
      initialize(buffer_size);
    }

   int getchr() {
      if (input_ptr == buffer_end && !fillBuffer()) return -1;
      return (*input_ptr++) & 0xff;
    }

   int read(void * buffer,int len) {
      if (len > 4 && input_ptr + len <= buffer_end) {
	 IVY_copy_region(buffer,input_ptr,len);
	 input_ptr += len;
       }
      else {
	 char * bptr = (char *) buffer;
	 int lnr = len;
	 for ( ; ; ) {
	    while (input_ptr != buffer_end && lnr-- > 0) {
	       *bptr++ = *input_ptr++;
	     }
	    if (lnr <= 0) return len;
	    if (!fillBuffer()) return len-lnr;
	  }
       }
      return len;
    }

   int readInt(int * buffer) {
      int ln = sizeof(int);
      if (input_ptr + ln < buffer_end && (((long) input_ptr)&(ln-1)) == 0) {
	 *buffer = *((int *) input_ptr);
	 input_ptr += ln;
       }
      else return read(buffer,ln);

      return ln;
    }

   int readInt(unsigned short * buffer) {
      int ln = sizeof(unsigned short);
      if (input_ptr + ln < buffer_end && (((long) input_ptr)&(ln-1)) == 0) {
	 *buffer = *((unsigned short *) input_ptr);
	 input_ptr += ln;
       }
      else return read(buffer,ln);

      return ln;
    }

   int readInt(unsigned int * buffer) {
      int ln = sizeof(unsigned int);
      if (input_ptr + ln < buffer_end && (((long) input_ptr)&(ln-1)) == 0) {
	 *buffer = *((unsigned int *) input_ptr);
	 input_ptr += ln;
       }
      else return read(buffer,ln);

      return ln;
    }

   int readInt(long * buffer) {
      int ln = sizeof(long);
      if (input_ptr + ln < buffer_end && (((long) input_ptr)&(ln-1)) == 0) {
	 *buffer = *((long *) input_ptr);
	 input_ptr += ln;
       }
      else return read(buffer,ln);

      return ln;
    }

   int readInt(unsigned long * buffer) {
      int ln = sizeof(unsigned long);
      if (input_ptr + ln < buffer_end && (((long) input_ptr)&(ln-1)) == 0) {
	 *buffer = *((unsigned long *) input_ptr);
	 input_ptr += ln;
       }
      else return read(buffer,ln);

      return ln;
    }

   int readInt(LongLong * buffer) {
      int ln = sizeof(LongLong);
      if (input_ptr + ln < buffer_end && (((long) input_ptr)&(ln-1)) == 0) {
	 *buffer = *((LongLong *) input_ptr);
	 input_ptr += ln;
       }
      else return read(buffer,ln);

      return ln;
    }

   int readInt(ULongLong * buffer) {
      int ln = sizeof(ULongLong);
      if (input_ptr + ln < buffer_end && (((long) input_ptr)&(ln-1)) == 0) {
	 *buffer = *((ULongLong *) input_ptr);
	 input_ptr += ln;
       }
      else return read(buffer,ln);

      return ln;
    }

   int readAddr(void ** buffer) {
      int ln = sizeof(void *);
      if (input_ptr + ln < buffer_end && (((long) input_ptr)&(ln-1)) == 0) {
	 *buffer = *((void **) input_ptr);
	 input_ptr += ln;
       }
      else return read(buffer,ln);

      return ln;
    }

   int skip(int len) {
      if (input_ptr + len <= buffer_end) {
	 input_ptr += len;
       }
      else {
	 int i = buffer_end - input_ptr;
	 int nln = len - i;
	 if (!fillBuffer()) return len;
	 return i + skip(nln);
       }

      return len;
    }

   void * pointerPeek(int len) {
      for ( ; ; ) {
	 if (input_ptr + len <= buffer_end) return input_ptr;

	 int bln = buffer_end - input_ptr;
	 int boff = 0;
	 if (bln != 0) {
	    boff = (int)(((long) (input_ptr))&7);
	    IVY_copy_region(file_buffer+boff,input_ptr,bln);
	  }
	 if (!fillBuffer(bln+boff)) break;
	 input_ptr += boff;
       }

      return NULL;
    }

   void * pointerRead(int len) {
      void * rslt = pointerPeek(len);
      if (rslt != NULL) input_ptr += len;
      return rslt;
    }

   string getLine(char term = '\n') {
      string rslt;
      while (!end_of_file) {
	 if (input_ptr >= buffer_end) {
	    if (!fillBuffer()) break;
	  }
	 else {
	    char ch = *input_ptr++;
	    if (ch == term) break;
	    rslt.append(1,ch);
	  }
       }
      return rslt;
    }

#ifndef NO_PIPES
   Bool popen(CStdString cmd,Bool dostderror = FALSE) {
      int i;
      int ip[2];
      const char * sh;
      const char * shn;
      const char * args[32];
      int argc = 0;

      close();

      sh = IVY_FILE_DEFAULT_SHELL;
      shn = strrchr(sh,'/');
      if (shn != NULL) ++shn;
      else shn = sh;

      args[argc++] = shn;
      if (IVY_FILE_SHELL_FLG != NULL) args[argc++] = IVY_FILE_SHELL_FLG;
      args[argc++] = cmd.c_str();
      args[argc] = NULL;

      pipe(ip);

      pipe_pid = fork1();

      if (pipe_pid < 0) return FALSE;	   // let someone else deal with the error

      if (pipe_pid == 0) {
	 dup2(ip[1],1);
	 if (dostderror) dup2(ip[1],2);
	 for (i = 3; i < 1024; ++i) ::close(i);
	 execvp(sh,(char * const *) args);
	 _exit(1);
       }

      file_fd = ip[0];
      ::close(ip[1]);

      return TRUE;
    }
#endif

   Bool endOfFile() const			{ return end_of_file; }

   void clearEndOfFile()			{ if (file_fd >= 0) end_of_file = FALSE; }

   Bool isValid() const 			{ return file_fd >= 0; }

   void rewind() {
      if (file_fd < 0) return;
      input_ptr = file_buffer;
      buffer_end = file_buffer;
      ::lseek(file_fd,0,SEEK_SET);
      end_of_file = false;
      base_position = 0;
    }

private:
   void initialize(int bsize) {
      file_fd = -1;
      file_buffer = NULL;
      buffer_size = bsize;
      input_ptr = NULL;
      buffer_end = NULL;
      pipe_pid = -1;
      base_position = 0;
      end_of_file = false;
    }

   Bool fillBuffer(int bas = 0) {
      if (file_fd < 0 || end_of_file) return FALSE;
      if (file_buffer == NULL) {
	 if (buffer_size <= 0) buffer_size = getpagesize()*16;
	 file_buffer = new char[buffer_size];
       }
      int len = ::read(file_fd,file_buffer+bas,buffer_size-bas);
      if (len < 0 && errno == EAGAIN) {
	 end_of_file = true;
	 return FALSE;
       }
      if (len < 0) {
	 ::close(file_fd);
	 file_fd = -1;
	 end_of_file = true;
	 return FALSE;
       }
      else if (len == 0) {
	 end_of_file = true;
	 return FALSE;
       }
      base_position += len;
      input_ptr = file_buffer;
      buffer_end = file_buffer + len + bas;

      return TRUE;
    }

};




/********************************************************************************/
/*										*/
/*	IvyOutputFileInfo -- implementation of an output file			*/
/*										*/
/********************************************************************************/


class IvyOutputFileInfo {

private:
   char * file_buffer;
   int file_fd;
   int buffer_size;
   char * output_ptr;
   char * buffer_end;
   pid_t pipe_pid;

public:
   IvyOutputFileInfo(int bsize = 0) {
      initialize(bsize);
    }

   IvyOutputFileInfo(CStdString fname,int bsize = 0,Bool read = false) {
      initialize(bsize);
      open(fname,read);
    }

   ~IvyOutputFileInfo() {
      close();
    }

   Bool isValid() const {
      return file_fd >= 0;
    }

   Bool open(CStdString name,Bool read = false) {
      int fgs = O_CREAT|O_TRUNC|O_LARGEFILE;
      fgs |= (read ? O_RDWR : O_WRONLY);
      close();
      file_fd = ::open(name.c_str(),fgs,0666);
      if (file_fd < 0) return FALSE;
      return TRUE;
    }

   Bool open(int fid) {
      close();
      file_fd = fid;
      if (file_fd < 0) return FALSE;
      return TRUE;
    }

   void setBufferSize(int bsize) {
      if (file_buffer != NULL) return;
      buffer_size = bsize;
    }

   void flush() {
      flushBuffer();
    }

   void close() {
      flushBuffer();
      if (file_fd >= 0) ::close(file_fd);
      if (file_buffer != NULL) delete [] file_buffer;
      if (pipe_pid > 0) {
	 for ( ; ; ) {
	    pid_t r = waitpid(pipe_pid,0,0);
	    if (r == pipe_pid) break;
	    if (r < 0 && errno != EINTR) break;
	  }
       }
      initialize(buffer_size);
    }

   void write(const void * buf,int len) {
      if (len == 0) ;
      else if (output_ptr + len <= buffer_end) {
	 IVY_copy_region(output_ptr,buf,len);
	 output_ptr += len;
       }
      else {
	 int ln0 = buffer_end - output_ptr;
	 char * bptr = (char *) buf;
	 if (ln0 > 0) {
	    IVY_copy_region(output_ptr,buf,ln0);
	    output_ptr += ln0;
	    bptr += ln0;
	  }
	 flushBuffer();
	 write(bptr,len-ln0);
       }
    }

   int read(void * buf,int len) {
      flushBuffer();
      int r = ::read(file_fd,buf,len);
      return r;
    }

   LongLong tell() {
      if (file_fd < 0) return -1;
      LongLong off = 0;
      LongLong bpos = ::lseek64(file_fd,off,SEEK_CUR);
      bpos += output_ptr - file_buffer;
      return bpos;
    }

   void seek(LongLong pos) {
      if (file_fd < 0) return;
      flushBuffer();
      ::lseek64(file_fd,pos,SEEK_SET);
    }


#ifndef NO_PIPES
   Bool popen(CStdString cmd) {
      int i;
      int op[2];
      const char * sh;
      const char * shn;
      const char * args[32];
      int argc = 0;

      close();

      sh = IVY_FILE_DEFAULT_SHELL;
      shn = strrchr(sh,'/');
      if (shn != NULL) ++shn;
      else shn = sh;

      args[argc++] = shn;
      if (IVY_FILE_SHELL_FLG != NULL) args[argc++] = IVY_FILE_SHELL_FLG;
      args[argc++] = cmd.c_str();
      args[argc] = NULL;

      pipe(op);

      pipe_pid = fork1();

      if (pipe_pid < 0) return FALSE;	   // let someone else deal with the error

      if (pipe_pid == 0) {
	 dup2(op[0],0);
	 for (i = 3; i < 1024; ++i) ::close(i);
	 execvp(sh,(char * const *) args);
	 _exit(1);
       }

      file_fd = op[1];
      ::close(op[0]);

      return TRUE;
    }
#endif


private:
   void initialize(int bsize) {
      file_fd = -1;
      file_buffer = NULL;
      buffer_size = bsize;
      output_ptr = NULL;
      buffer_end = NULL;
      pipe_pid = -1;
    }

   void flushBuffer() {
      if (file_fd < 0) return;
      if (file_buffer == NULL) {
	 if (buffer_size <= 0) buffer_size = getpagesize()*16;
	 file_buffer = new char[buffer_size];
	 buffer_end = file_buffer + buffer_size;
       }
      else if (output_ptr != file_buffer) {
	 if (::write(file_fd,file_buffer,output_ptr - file_buffer) < 0) {
	    ::close(file_fd);
	    file_fd = -1;
	  }
       }

      output_ptr = file_buffer;
    }

};




}	// end of namespace Ivy


#endif

/* end of ivy_fileio.H */

