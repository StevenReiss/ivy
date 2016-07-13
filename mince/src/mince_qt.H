/********************************************************************************/
/*										*/
/*		mince_qt.H							*/
/*										*/
/*	External definitions for MINCE using qt to handle messages		*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/mince/src/mince_qt.H,v 1.1 2005/06/28 17:22:00 spr Exp $ */

/*********************************************************************************
 *
 * $Log: mince_qt.H,v $
 * Revision 1.1  2005/06/28 17:22:00  spr
 * Move mince from tea to ivy for use in veld, etc.
 *
 *
 ********************************************************************************/


#ifndef MINCE_QT_INCLUDED_ALREADY
#define MINCE_QT_INCLUDED_ALREADY


#include "mince.H"

#include <ivy_datatypes.H>

#include <fstream>

#include <qsocknot.h>
#include <qmetaobject.h>



namespace Ivy {

namespace Mince {


/************************************************************************/
/*									*/
/*	Forward type definitions					*/
/*									*/
/************************************************************************/


typedef class MinceQtControlInfo *	MinceQtControl;


#define MINCE_QT_META_OBJECT		MinceMasterInfo::connect_data[0]




/********************************************************************************/
/*										*/
/*	MinceQtControl -- class for qt-based message access			*/
/*										*/
/********************************************************************************/


class MinceQtControlInfo : public MinceControlHandlerInfo, public QObject {

private:
   QSocketNotifier * socket_notifier;
   int message_count;
   int reply_count;
   ifstream * input_stream;
   MinceControl for_control;

public:
   static MinceControl create(MinceSyncFlag fg = MINCE_SYNC_POLL) {
      StdString id;
      MinceQtControl qc = new MinceQtControlInfo();
      MinceControl mc = MINCE__createClient(id,fg,qc);
      qc->setControl(mc,fg);
      return mc;
    }
   static MinceControl create(CStdString id,MinceSyncFlag fg = MINCE_SYNC_POLL) {
      MinceQtControl qc = new MinceQtControlInfo();
      MinceControl mc = MINCE__createClient(id,fg,qc);
      qc->setControl(mc,fg);
      return mc;
    }

public:
   MinceQtControlInfo() {
      message_count = 0;
      reply_count = 0;
      socket_notifier = NULL;
      input_stream = NULL;
      for_control = NULL;
    }
   ~MinceQtControlInfo() {
      delete socket_notifier;
      delete input_stream;
      delete for_control;
    }

   void lockMessage()				{ }
   void unlockMessage() 			{ }
   void handleAddMessage()			{ ++message_count; }
   bool handleWaitMessage(bool w) {
      if (message_count == 0) return false;
      --message_count;
      return true;
    }

   void lockReply()				{ }
   void unlockReply()				{ }
   void handleAddReply()			{ ++reply_count; }
   bool handleWaitReply(bool w) {
      if (reply_count == 0) return false;
      --reply_count;
      return true;
    }

   void registerFile(MinceControl mc,int fd) {
      IvySocketInfo sock(fd);
      sock.enableNonblocking(true);
      socket_notifier = new QSocketNotifier(fd,QSocketNotifier::Read);
      QObject::connect(socket_notifier,SIGNAL(activated(int)),this,SLOT(dataArrived()));
      input_stream = new ifstream(fd);
    }

   void setControl(MinceControl mc,MinceSyncFlag fg) {
      for_control = mc;
    }

private slots:
   inline void dataArrived();

public:
   QMetaObject * metaObject() const		{ return staticMetaObject(); }
   const char * className() const		{ return "Ivy::Mince::MinceQtControlInfo"; }
   void initMetaObject() {
      if (MINCE_QT_META_OBJECT == NULL) staticMetaObject();
    }
   static QMetaObject * staticMetaObject() {
      if (MINCE_QT_META_OBJECT != NULL) return (QMetaObject *) MINCE_QT_META_OBJECT;
      QObject::staticMetaObject();
      typedef void(MinceQtControlInfo::*m1_t0)();
      m1_t0 v1_0 = &MinceQtControlInfo::dataArrived;
      QMetaData * slot_tbl = QMetaObject::new_metadata(1);
      QMetaData::Access *slot_tbl_access = QMetaObject::new_metaaccess(1);
      slot_tbl[0].name = "dataArrived()";
      slot_tbl[0].ptr = (QMember)v1_0;
      QMetaObject * meta = QMetaObject::new_metaobject(
	 "Ivy::Mince::MinceQtControlInfo","QObject", slot_tbl,1,0,0,0,0,0,0,0,0);
      meta->set_slot_access(slot_tbl_access);
      MINCE_QT_META_OBJECT = meta;
      return meta;
    }

};



/************************************************************************/
/*									*/
/*	Method implementations						*/
/*									*/
/************************************************************************/


void
MinceQtControlInfo::dataArrived()
{
   for ( ; ; ) {
      StdString hdr,s;
      getline(*input_stream,hdr);
      if (input_stream->eof()) {
	 input_stream->clear();
	 break;
       }
      else if (input_stream->fail()) break;

      StringBuffer body;
      int ct = 0;
      for ( ; ; ) {
	 getline(*input_stream,s);
	 if (input_stream->eof()) {
	    input_stream->clear();
	    continue;			// we must wait here for the rest of the message
	  }
	 else if (input_stream->fail()) {
	    for_control->serverError("I/O error reading input stream");
	    break;
	  }
	 if (s == getMinceTrailer()) break;
	 if (ct++ != 0) body.append(' ');
	 body.append(s);
       }
      for_control->processInput(hdr,body.getString());
    }

   for ( ; ; ) {
      MinceRequest r = for_control->pollNext(false);
      if (r == NULL) break;
      for_control->process(r);
    }
}




} }	// end of namespace Ivy::Mince



#endif


/* end of mince_qt.H */
