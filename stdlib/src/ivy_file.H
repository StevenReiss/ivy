/************************************************************************/
/*									*/
/*		ivy_file.H						*/
/*									*/
/*	Definitions for File manipulation methods			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_file.H,v 1.6 2017/09/08 18:26:07 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_file.H,v $
 * Revision 1.6  2017/09/08 18:26:07  spr
 * Updates for new version of gcc
 *
 * Revision 1.5  2013/11/08 01:32:18  spr
 * Updates for newer g++ on mac.
 *
 * Revision 1.4  2009-03-20 02:00:57  spr
 * Handle windows files/sockets/etc.
 *
 * Revision 1.3  2006-08-30 00:44:44  spr
 * Formatting changes.
 *
 * Revision 1.2  2005/07/08 20:58:28  spr
 * Minor fixups and additions.
 *
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_FILE_INCLUDED_ALREADY
#define IVY_FILE_INCLUDED_ALREADY


#include <ivy_string.H>

#ifndef WIN32
#include <pwd.h>
#endif

#include <string>
#include <iostream>
#include <fstream>


#ifndef _POSIX_PTHREAD_SEMANTICS
#define _POSIX_PTHREAD_SEMANTICS 1
#endif

#include <sys/types.h>
#include <sys/stat.h>




namespace Ivy {


/********************************************************************************/
/*										*/
/*	Forward Type Definitions						*/
/*										*/
/********************************************************************************/


class IvyDirectoryIter;



/********************************************************************************/
/*										*/
/*	IvyFileName -- methods for file name manipulation			*/
/*										*/
/********************************************************************************/


class IvyFileName {

private:
   string given_name;
   string real_name;
   string dir_name;
   struct stat * stat_buf;
   bool stat_valid;

public:
   IvyFileName(const char * n = 0) {
      init();
      if (n == 0) setName(string());
      else setName(n);
    }
   IvyFileName(const string& n) 			{ init(); setName(n); }
   IvyFileName(const string& file,const string& wd)	{ init(); setName(file,wd); }
   IvyFileName(const IvyFileName& fn)			{ init(); setName(fn.given_name); }
   ~IvyFileName()					{ freeAll(); }

   IvyFileName& operator = (const IvyFileName& fn) {
      setName(fn.given_name);
      return *this;
    }

   void setName(const string& nm) {
      freeAll();
      given_name = nm;
    }

   void setName(const string& nm,const string& wd) {
      if (nm.empty() || nm[0] == '/' || nm[0] == '~') setName(nm);
      else {
	 Character wdbuf[1024];
	 string nmb = wd;
	 if (nmb.empty()) {
	    getcwd(wdbuf,1024);
	    nmb = wdbuf;
	  }
	 nmb += "/";
	 nmb += nm;
	 setName(nmb);
       }
    }

   inline void expandName(const string& nm);

   void setSystemName(const string& nm) {
      setName(nm);
      real_name = realPathName(findSystemName(given_name,"PATH",""));
    }

   void setLibraryName(const string& nm) {
      setName(nm);
      real_name = realPathName(findSystemName(given_name,"LD_LIBRARY_PATH",""));
    }

   void setPathName(const string& nm,const string& path) {
      setName(nm);
      real_name = realPathName(findSystemName(given_name,NULL,path));
    }

   const string& name() const			{ return given_name; }
   operator const string&() const		{ return given_name; }
   operator const char *() const		{ return given_name.c_str(); }

   const string& realName() {
      if (real_name.empty() && !given_name.empty()) {
	 real_name = realPathName(given_name);
       }
      return real_name;
    }

   const string& directory() {
      if (dir_name.empty() && !given_name.empty() && !realName().empty()) {
	 int idx = real_name.rfind('/');
	 if (idx != string::npos) dir_name = real_name.substr(0,idx);
       }
      return dir_name;
    }

   string extension() {
      if (!realName().empty()) {
	 int idx0 = real_name.rfind('/');
	 int idx1 = real_name.rfind('.');
	 if (idx1 != string::npos) {
	    if (idx0 == string::npos || idx1 > idx0) return real_name.substr(idx1);
	  }
       }
      return "";
    }

   string rootName() {
      realName();
      int idx = real_name.rfind('/');
      if (idx == string::npos) return real_name;
      return real_name.substr(idx+1);
    }

   string homeDirectory() {
      string home;
      const char * hd = ::getenv("HOME");
      if (hd != NULL && *hd != 0) home = hd;
#ifndef WIN32
      else {
	 struct passwd * pwd;
	 pwd = getpwuid(getuid());
	 if (pwd != NULL) home = pwd->pw_dir;
       }
#endif
      return home;
    }

   bool isDirectory() {
      getStat();
      if (stat_buf == NULL) return false;
      return S_ISDIR(stat_buf->st_mode);
    }
   bool isSymbolicLink() {
      linkInfo();
      if (stat_buf == NULL) return false;
      return S_ISLNK(stat_buf->st_mode);
    }
   long getDateLastModified() {
      getStat();
      if (stat_buf == NULL) return 0;
      return stat_buf->st_mtime;
    }
   long getDateLastAccessed() {
      getStat();
      if (stat_buf == NULL) return 0;
      return stat_buf->st_atime;
    }
   long getSize() {
      getStat();
      if (stat_buf == NULL) return 0;
      return stat_buf->st_size;
    }

   bool exists()				{ return access(name(),F_OK) == 0; }
   bool isReadable()				{ return access(name(),R_OK) == 0; }
   bool isWritable()				{ return access(name(),W_OK) == 0; }

   bool mkdir(int mode = 0777)			{ return Ivy::mkdir(name(),mode) == 0; }
   bool unlink() {
      if (isDirectory()) return Ivy::rmdir(realName());
      return Ivy::unlink(realName()) == 0;
    }

   static StdString separator() 		{ return "/"; }
   static char separatorChar()			{ return '/'; }

   inline IvyDirectoryIter getFiles();

   void linkInfo() {
      getStat();
#ifndef WIN32
      if (stat_buf != NULL) {
	 struct stat sbuf;
	 if (lstat(realName().c_str(),&sbuf) == 0) {
	    if (S_ISLNK(sbuf.st_mode)) *stat_buf = sbuf;
	  }
       }
#endif
    }

   bool loadLibrary() {
      void * addr = dlopen(realName().c_str(),RTLD_LAZY|RTLD_NOLOAD);
      return addr != NULL;
    }

   static string getCurrentDirectory() {
      char buf[1024];
      getcwd(buf,1024);
      return buf;
    }

   void createTemporary(CStdString pfx = "/tmp/IvyTemp") {
      char file[1024];
      if (!pfx.empty() && pfx.length() < 1000) {
	 strcpy(file,pfx.c_str());
       }
      else strcpy(file,"/tmp/IvyTemp");
      strcat(file,"XXXXXX");
      close(mkstemp(file));
      setName(file);
    }

   static inline string getTraceDirectory();

private:
   inline string fixPathName(const string&);
   inline string fixLinks(const string&);
   inline string realPathName(const string&);
   inline string findSystemName(const string&,const char *,string);

   void init() {
      stat_buf = NULL;
      stat_valid = false;
    }

   void freeAll() {
      given_name.resize(0);
      real_name.resize(0);
      dir_name.resize(0);
      if (stat_buf != NULL) {
	 delete stat_buf;
	 stat_buf = NULL;
       }
      stat_valid = false;
    }

   inline void getStat() {
      if (stat_valid) return;
      stat_buf = new struct stat;
      if (stat(realName(),stat_buf) != 0) {
	 delete stat_buf;
	 stat_buf = NULL;
       }
      stat_valid = true;
    }
};





/********************************************************************************/
/*										*/
/*	IvyDirectoryIter -- iterate through the files in a directory		*/
/*										*/
/********************************************************************************/


#ifndef WIN32


#include <dirent.h>



class IvyDirectoryIter {

private:
   StdString dir_name;
   DIR * dir_handle;
   char dir_buf[10240];
   StdString cur_entry;
   StdString cur_tail;

public:
   IvyDirectoryIter(CStdString file) {
      dir_name = file;
      dir_handle = opendir(file.c_str());
      getNext();
    }
   ~IvyDirectoryIter() {
      if (dir_handle != NULL) closedir(dir_handle);
    }

   bool more() const			{ return dir_handle != NULL; }
   void next()				{ getNext(); }
   CStdString current() const		{ return cur_entry; }
   CStdString getTail() const		{ return cur_tail; }
   CStdString getPathName() const	{ return cur_entry; }

   operator int() const 		{ return more(); }
   IvyDirectoryIter& operator ++()	{ next(); return *this; }
   CStdString operator *() const	{ return current(); }

private:
   void getNext() {
      dirent * dp = NULL;
      cur_entry.resize(0);
      cur_tail.resize(0);
      if (dir_handle == NULL) return;
      if ((dp = readdir(dir_handle)) == NULL) {
	 closedir(dir_handle);
	 dir_handle = NULL;
	 return;
       }

      cur_entry = dir_name;
      cur_entry += IvyFileName::separator();
      cur_entry += dp->d_name;
      cur_tail = dp->d_name;
    }
};



#else


class IvyDirectoryIter {

private:
   StdString dir_name;
   HANDLE dir_handle;
   WIN32_FIND_DATA find_data;
   StdString cur_entry;
   StdString cur_tail;

public:
   IvyDirectoryIter(CStdString file) {
      dir_name = file;
      dir_handle = NULL;
      unsigned long fattr = GetFileAttributes(dir_name.c_str());
      if (fattr != 0xffffffff && (fattr & FILE_ATTRIBUTE_DIRECTORY) == 0) {
	 StdString path = dir_name + "\\*.*";
	 dir_handle = ::FindFirstFile(path.c_str(),&find_data);
	 if (dir_handle == INVALID_HANDLE_VALUE) {
	    dir_handle = NULL;
	  }
       }
    }
   ~IvyDirectoryIter() {
      if (dir_handle != NULL) {
	 FindClose(dir_handle);
	 dir_handle = NULL;
       }
    }

   bool more() const			{ return dir_handle != NULL; }
   void next()				{ getNext(); }
   CStdString current() const		{ return cur_entry; }
   CStdString getTail() const		{ return cur_tail; }
   CStdString getPathName() const	{ return cur_entry; }

   operator int() const 		{ return more(); }
   IvyDirectoryIter& operator ++()	{ next(); return *this; }
   CStdString operator *() const	{ return current(); }

private:
   void getNext() {
      if (dir_handle == NULL) return;
      if (!::FindNextFile(dir_handle,&find_data)) {
	 FindClose(dir_handle);
	 dir_handle = NULL;
       }
      else {
	 cur_tail = find_data.cFileName;
	 cur_entry = dir_name;
	 cur_entry += IvyFileName::separator();
	 cur_entry += cur_tail;
       }
    }
};

#endif



/********************************************************************************/
/*										*/
/*	Inline methods for managing files					*/
/*										*/
/********************************************************************************/


string
IvyFileName::fixPathName(const string& s0)
{
   string s = s0;
   string pfx,ss;
   string rslt;

   if (s.empty()) return s;

   if (strncmp(s.c_str(),"/tmp_mnt/",9) == 0) s = s.substr(8);
   else if (s[0] == '~') {
      ss = s.substr(1);
      if (s[1] == '/') {
	 pfx = homeDirectory();
	 s = ss;
       }
#ifndef WIN32
      else {
	 int i = ss.find('/');
	 string n = ss.substr(0,i);
	 struct passwd * pwd = getpwnam(n.c_str());
	 if (pwd != NULL) {
	    pfx = pwd->pw_dir;
	    s = ss.substr(i);
	  }
       }
#endif
    }
   else if (s[0] != '/') {
      pfx = getCurrentDirectory();
    }

   if (!pfx.empty()) {
      rslt = pfx;
      if (pfx[pfx.size()-1] != '/' && s[0] != '/') rslt += '/';
    }

   int idx = 0;
   while (idx < (int) s.size()) {
      if (s[idx] != '/') rslt += s[idx++];
      else if (s.substr(idx,3) == "/./") idx += 2;
      else if (s.substr(idx,2) == "//") idx += 1;
      else if (s.substr(idx,4) == "/../") {
	 int ri = rslt.rfind('/');
	 if (ri == string::npos) rslt.resize(0);
	 else rslt = rslt.substr(0,ri);
	 idx += 3;
       }
      else rslt += s[idx++];
    }

   return rslt;
}




string
IvyFileName::fixLinks(const string& n)
{
   string name = n;

#ifndef WIN32
   Integer ct,len;
   Character lname[2049],dir[10240],nbuf[10240];
   Text p;

   for (ct = 0; ct < 32; ++ct) {
      len = readlink(name.c_str(),lname,2048);
      if (len < 0) break;
      lname[len] = 0;
      if (lname[0] != '/') {
	 string dir = name;
	 int idx = dir.rfind('/');
	 if (idx != string::npos) dir = dir.substr(0,idx+1);
	 name = dir + lname;
       }
      else name = lname;
    }
#endif

   return fixPathName(name);
}




string
IvyFileName::realPathName(const string& n)
{
   string name = n;
   string rslt;

   if (name.empty()) return n;
   else if (name[0] == '~') name = fixPathName(name);

   for (int i = 0; i < (int) name.size(); ++i) {
      if (name[i] != '/') rslt += name[i];
      else rslt = fixLinks(rslt) + '/';
    }

   return fixLinks(rslt);
}




string
IvyFileName::findSystemName(const string& s0,const char * pathname,string path)
{
   string sys = s0;

   if (pathname != NULL) {
      const char * penv = getenv(pathname);
      if (penv != NULL) path = penv;
    }

   if (sys.empty() || sys[0] == '/') return sys;

   if (sys.find('/') == string::npos) {
      while (!path.empty()) {
	 string pfx;
	 int pos = path.find(':');
	 if (pos == string::npos) {
	    pfx = path;
	    path.resize(0);
	  }
	 else {
	    pfx = path.substr(0,pos);
	    path = path.substr(pos+1);
	  }
	 if (pfx[pfx.size()-1] != '/') pfx += '/';
	 pfx += sys;
	 if (access(pfx.c_str(),X_OK) >= 0) return pfx;
       }
    }

   ConstText wd = getenv("PWD");
   if (wd == NULL) wd = ".";
   string rslt = wd;
   rslt += '/';
   rslt += sys;

   return rslt;
}




IvyDirectoryIter
IvyFileName::getFiles() {
   return IvyDirectoryIter(realName());
}



void
IvyFileName::expandName(const string& src)
{
   StringBuffer buf;

   for (int i = 0; i < (int) src.length(); ++i) {
      char c = src[i];
      if (c == '$' && src[i+1] == '(') {
	 StringBuffer tok;
	 for (i = i+2; i < (int) src.length() && src[i] != ')'; ++i) {
	    tok.append(src[i]);
	  }
	 if (i >= (int) src.length()) break;
	 ConstText erslt = NULL;
	 if (tok.getString() == "PRO") erslt = getenv("BROWN_IVY_ROOT");
	 else if (tok.getString() == "IVY") erslt = getenv("BROWN_IVY_IVY");
	 else if (tok.getString() == "TEA") erslt = getenv("BROWN_TEA_TEA");
	 else if (tok.getString() == "CLIME") erslt = getenv("BROWN_CLIME_CLIME");
	 else if (tok.getString() == "ARCH") erslt = getenv("BROWN_IVY_ARCH");
	 else if (tok.getString() == "BLOOM") erslt = getenv("BROWN_BLOOM_BLOOM");
	 else if (tok.getString() == "TRACE") erslt = getenv("BROWN_IVY_TRACE");
	 else if (tok.getString() == "VELD") erslt = getenv("BROWN_VELD_VELD");
	 if (erslt == NULL) {
	    string s = "BROWN_IVY_" + tok.getString();
	    erslt = getenv(s);
	  }
	 if (erslt == NULL) erslt = getenv(tok.getString());
	 if (erslt != NULL) buf.append(erslt);
       }
#ifdef WIN32
      else if (c == '/') buf.append('\\');
#endif
      else buf.append(c);
    }

   setName(buf.getString());
}




string
IvyFileName::getTraceDirectory()
{
   ConstText er = getenv("BROWN_IVY_TRACE");
   if (er != NULL) return er;

   char buf[1024];
   gethostname(buf,1024);
   StdString host = buf;
   IvyFileName tf;
   tf.expandName("$(IVY)/lib/tracefiles");
   ifstream ifs((const char *) tf);
   while (ifs) {
      string line;
      getline(ifs,line);
      if (ifs.eof()) break;
      StringTokenizer tok(line);
      if (tok.hasMoreTokens()) {
	 string hn = tok.nextToken();
	 if (hn[0] == '#') continue;
	 if (tok.hasMoreTokens()) {
	    string p = tok.nextToken();
	    if (tok.hasMoreTokens()) continue;
	    if (hn == "*" || hn == host) {
	       ifs.close();
	       return p;
	     }
	  }
       }
    }
   ifs.close();

   cerr << "Couldn't find directory for trace files" << endl;
   exit(1);
}



}	// end of namespace Ivy


#endif

/* end of ivy_file.H */
