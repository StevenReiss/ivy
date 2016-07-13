/********************************************************************************/
/*										*/
/*		mincemaster.C							*/
/*										*/
/*	Connection to the MINT master server service				*/
/*										*/
/********************************************************************************/
/*	Copyright 1997 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 1997, Brown University, Providence, RI.				 *
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
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,	 *
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS 	 *
 *  SOFTWARE.									 *
 *										 *
 ********************************************************************************/


#ifndef lint
static const char * const rcsid = "$Header: /pro/spr_cvs/pro/ivy/mince/src/mincemaster.C,v 1.1 2005/06/28 17:22:00 spr Exp $";
#endif


/*********************************************************************************
 *
 * $Log: mincemaster.C,v $
 * Revision 1.1  2005/06/28 17:22:00  spr
 * Move mince from tea to ivy for use in veld, etc.
 *
 *
 ********************************************************************************/


#include "mince_local.H"
#include <ivy_file.H>



/********************************************************************************/
/*										*/
/*	Local Storage								*/
/*										*/
/********************************************************************************/


	IvySocket	MinceMasterDataInfo::master_socket = NULL;
	void *		MinceMasterInfo::connect_data[4];




/********************************************************************************/
/*										*/
/*	MinceMasterInfo methods 						*/
/*										*/
/********************************************************************************/


IvySocket
MinceMasterInfo::findServer(CStdString cid,CStdString startcmd)
{
   IvySocket skt = NULL;
   StdString id = cid;

   if (id.empty()) {
      char buf[1024],buf1[1024];
      gethostname(buf,1024);
      id = MINCE_DEFAULT_SERVICE_NAME;
      id += "_";
      id += getenv("USER");
      id += "_";
      id += buf;
    }

   MinceMasterData md = new MinceMasterDataInfo();

   if (!md->isValid()) return NULL;

   for (int i = 0; i < 20; ++i) {
      skt = md->setupClient(id);
      if (skt != NULL) break;

      if (i == 0) {
	 if (startcmd.empty()) break;
	 system(startcmd);
       }

      sleep(i+1);
    }

   if (skt == NULL) {
      cerr << "MINCE: Couldn't start server " << id << endl;
    }

   delete md;

   return skt;
}



/********************************************************************************/
/*										*/
/*	MinceMasterData constructors/destructors				*/
/*										*/
/********************************************************************************/


MinceMasterDataInfo::MinceMasterDataInfo()
{
   if (master_socket != NULL) return;

   lock_file = -1;

   StdString fn = getMasterFile();
   lock(fn);

   for (int i = 0; i < 20; ++i) {
      StdString host;
      int port = 0;

      ConstText ht = getenv("MINT_MASTER_HOST");
      ConstText pt = getenv("MINT_MASTER_PORT");
      StdString h = (ht == NULL ? "" : ht);
      StdString p = (pt == NULL ? "" : pt);

      if (!h.empty() && !p.empty()) {
	 int pn = atoi(p);
	 if (pn != 0) {
	    host = h;
	    port = pn;
	  }
       }

      if (host.empty()) {
	 ifstream ifs(fn.c_str());
	 StdString hn;
	 getline(ifs,hn);
	 StringTokenizer tok(hn);
	 if (tok.hasMoreTokens()) {
	    h = trim(tok.nextToken());
	    if (tok.hasMoreTokens()) {
	       int pn = atoi(tok.nextToken());
	       if (!tok.hasMoreTokens()) {
		  host = h;
		  port = pn;
		}
	     }
	  }
       }

      if (!host.empty()) {
	 master_socket = new IvySocketInfo();
	 if (!master_socket->createClient(host.c_str(),port)) {
	    delete master_socket;
	    master_socket = NULL;
	  }
       }

      if (master_socket != NULL) break;

      if (i == 0) {
	 IvyFileName fn;
	 fn.expandName(MINCE_MASTER_CMD);
	 system(fn.name());
       }

      sleep(i+1);
    }

   if (master_socket == NULL) {
      cerr << "MINCE: Couldn't open master server socket" << endl;
    }

   unlock(fn);
}



MinceMasterDataInfo::~MinceMasterDataInfo()			{ }



/************************************************************************/
/*									*/
/*	MinceMasterData methods for the client end			*/
/*									*/
/************************************************************************/


IvySocket
MinceMasterDataInfo::setupClient(CStdString id)
{
   if (!isValid()) return NULL;

   IvySocket skt = NULL;

   StdString msg = "FIND\t" + id + "\n";
   master_socket->send(msg);

   char buf[10240];
   int len = 10240;
   int mln = master_socket->receive(buf,len);
   if (mln > 0) {
      buf[mln] = 0;
      msg = buf;
      if (strprefix(msg,"USE\t")) {
	 StringTokenizer tok(msg);
	 StdString u = tok.nextToken();
	 StdString h = tok.nextToken();
	 int p = atoi(tok.nextToken());
	 skt = new IvySocketInfo();
	 if (!skt->createClient(h.c_str(),p)) {
	    delete skt;
	    skt = NULL;
	  }
       }
    }

   return skt;
}



/************************************************************************/
/*									*/
/*	MinceMasterData methods for handling files			*/
/*									*/
/************************************************************************/


StdString
MinceMasterDataInfo::getMasterFile()
{
   IvyFileName fn;

   fn.expandName(MINCE_MASTER_FILE);

   return fn.name();
}



void
MinceMasterDataInfo::lock(CStdString file)
{
   int ctr = 1;

   IvyFileName lf = lockFile(file);

   for ( ; ; ) {
      int fd = open(file,O_CREAT|O_RDWR,0777);
      if (fd > 0) fchmod(fd,0777);
      int sts = lockf(fd,F_TLOCK,100);
      if (sts == 0) {
	 lock_file = fd;
	 break;
       }
      sleep(ctr);
      ctr *= 2;
      if (ctr > 32) break;
    }
}



void
MinceMasterDataInfo::unlock(CStdString file)
{
   if (lock_file < 0) return;

   lockf(lock_file,F_ULOCK,100);

   close(lock_file);

   lock_file = -1;
}



IvyFileName
MinceMasterDataInfo::lockFile(CStdString file)
{
   IvyFileName fn(file);

   if (fn.isDirectory()) fn.setName(fn.realName() + fn.separator() + "Lock");
   else if (strsuffix(file,".lock")) ;
   else fn.setName(fn.realName() + ".lock");

   return fn;
}



/********************************************************************************/
/*										*/
/*	MinceControlHandler methods						*/
/*										*/
/********************************************************************************/


StdString
MinceControlHandlerInfo::getMinceTrailer()
{
   return MINCE_TRAILER;
}




/* end of mincemaster.C */
