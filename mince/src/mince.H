/********************************************************************************/
/*										*/
/*		mince.H 							*/
/*										*/
/*	External definitions for MINCE Message interface communications environ */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/mince/src/mince.H,v 1.2 2013/11/08 01:36:43 spr Exp $ */

/*********************************************************************************
 *
 * $Log: mince.H,v $
 * Revision 1.2  2013/11/08 01:36:43  spr
 * Updates for new g++
 *
 * Revision 1.1  2005/06/28 17:21:59  spr
 * Move mince from tea to ivy for use in veld, etc.
 *
 *
 ********************************************************************************/


#ifndef MINCE_INCLUDED_ALREADY
#define MINCE_INCLUDED_ALREADY

#include <ivy_datatypes.H>
#include <ivy_xml.H>
#include <ivy_socket.H>



namespace Ivy {

namespace Mince {




/************************************************************************/
/*									*/
/*	Constants							*/
/*									*/
/************************************************************************/


const char * const MINCE_XML_ANY = "_ANY_";
const char * const MINCE_XML_VAR = "_VAR_";


/************************************************************************/
/*									*/
/*	Enumerated types (these must match MintConstants.java)		*/
/*									*/
/************************************************************************/


enum MinceMessageFlag {
   MINCE_MSG_NONE = 0,
   MINCE_MSG_NO_REPLY = 0x1,
   MINCE_MSG_FIRST_REPLY = 0x2,
   MINCE_MSG_ALL_REPLIES = 0x4,
   MINCE_MSG_NON_NULL_ONLY = 0x8,
   MINCE_MSG_WAIT_FOR_ALL = 0x10,
   MINCE_MSG_FIRST_NON_NULL = 0xa
};



enum MinceSyncFlag {
   MINCE_SYNC_NONE = 0,
   MINCE_SYNC_SINGLE = 1,
   MINCE_SYNC_MULTIPLE = 2,
   MINCE_SYNC_REPLIES = 3,
   MINCE_SYNC_POLL = 4,
   MINCE_SYNC_POLL_REPLIES = 5
};




enum MinceReplyFlag {
   MINCE_REPLY_NONE = 0
};



/************************************************************************/
/*									*/
/*	Forward Type Definitions					*/
/*									*/
/************************************************************************/


typedef class MinceMasterInfo * 		MinceMaster;
typedef class MinceControlInfo *		MinceControl;
typedef class MinceControlHandlerInfo * 	MinceControlHandler;
typedef class MinceMessageInfo *		MinceMessage;
typedef class MinceReplyInfo *			MinceReply;
typedef class MinceHandlerInfo *		MinceHandler;
typedef class MinceArgumentsInfo *		MinceArguments;
typedef class MinceErrorHandlerInfo *		MinceErrorHandler;

typedef class MinceRequestInfo *		MinceRequest;




/************************************************************************/
/*									*/
/*	MinceMaster -- handle connection to MINT master server		*/
/*									*/
/************************************************************************/


class MinceMasterInfo {

public:
   static void * connect_data[4];

public:
   static IvySocket findServer(CStdString id,CStdString startcmd);
};




/************************************************************************/
/*									*/
/*	MinceControl -- basic class for accessing message server	*/
/*									*/
/************************************************************************/


class MinceControlInfo {

public:
   virtual void shutDown() = 0;

   virtual void send(CStdString msg,MinceReply = NULL,MinceMessageFlag = MINCE_MSG_NONE) = 0;
   void send(IvyXmlNode msg,MinceReply r = NULL,MinceMessageFlag f = MINCE_MSG_NONE) {
      send(msg.convertToString(),r,f);
    }

   virtual void addPattern(CStdString pat,MinceHandler) = 0;
   virtual void addPattern(IvyXmlNode pat,MinceHandler) = 0;
   virtual void removePattern(MinceHandler) = 0;

   virtual int getNumPolledMessages() const = 0;
   virtual MinceRequest pollNext(Bool wait) = 0;
   virtual MinceRequest pollReplies(Bool wait = false) = 0;
   virtual void process(MinceRequest) = 0;

   virtual void registerErrorHandler(MinceErrorHandler) = 0;

   virtual CStdString getMinceName() const = 0;

public:
   virtual void serverError(CStdString) = 0;
   virtual void processInput(CStdString hdr,CStdString body) = 0;
};




/************************************************************************/
/*									*/
/*	MinceControlHandler -- handler for different controls		*/
/*									*/
/************************************************************************/


class MinceControlHandlerInfo {

public:
   virtual ~MinceControlHandlerInfo()			{ }

   virtual void registerFile(MinceControl,int fd) = 0;

   virtual void lockMessage() = 0;			// lock the message queue
   virtual void unlockMessage() = 0;			// unlock the message queue
   virtual void handleAddMessage() = 0; 		// add to message queue
   virtual bool handleWaitMessage(bool wait) = 0;	// wait for message queue & use result

   virtual void lockReply() = 0;			// lock the reply queue
   virtual void unlockReply() = 0;			// unlock the reply queue
   virtual void handleAddReply() = 0;			// add to reply queue
   virtual bool handleWaitReply(bool wait) = 0; 	// wait for reply queue & use result

public:
   static StdString getMinceTrailer();
};





/************************************************************************/
/*									*/
/*	MinceMessage -- abstract class for handling messages		*/
/*									*/
/************************************************************************/


class MinceMessageInfo {

public:
   virtual ~MinceMessageInfo()		{ }

   virtual void replyTo(CStdString reply) = 0;
   virtual void replyTo(IvyXmlNode reply) = 0;
   virtual void replyTo() = 0;

   virtual CStdString getText() = 0;
   virtual IvyXmlNode getXml() = 0;
};




/************************************************************************/
/*									*/
/*	MinceReply -- abstract class for handling replies		*/
/*									*/
/************************************************************************/


class MinceReplyInfo {

public:
   virtual void handleReply(MinceMessage msg,MinceMessage reply) = 0;
};




/************************************************************************/
/*									*/
/*	MinceHandler -- abstract class for handling incoming messages	*/
/*									*/
/************************************************************************/


class MinceHandlerInfo {

public:
   virtual void receive(MinceMessage msg,MinceArguments args) = 0;
};




/************************************************************************/
/*									*/
/*	MinceArguments -- abstract class for handling arguments 	*/
/*									*/
/************************************************************************/


class MinceArgumentsInfo {

public:
   virtual ~MinceArgumentsInfo()	 { }

   virtual int getNumArguments() const = 0;

   virtual StdString getArgument(int idx) const = 0;
   virtual Integer getIntArgument(int idx) const = 0;
   virtual Float getRealArgument(int idx) const = 0;
   virtual IvyXmlNode getXmlArgument(int idx) const = 0;
};





/************************************************************************/
/*									*/
/*	MinceErrorHandler -- abstract class for handling errors 	*/
/*									*/
/************************************************************************/


class MinceErrorHandlerInfo {

public:
   virtual void handleServerError(CStdString what) = 0;
};



/************************************************************************/
/*									*/
/*	Miscellaneous definitions for internal use			*/
/*									*/
/************************************************************************/

extern MinceControl MINCE__createClient(CStdString id,MinceSyncFlag fg,MinceControlHandler);



/********************************************************************************/
/*										*/
/*	Message formats 							*/
/*										*/
/********************************************************************************/

// Send:  SEND <replyid> <flags>
//	  <xml text>
//	  EMSG

// Reply: RPLY <replyid>
//	  <xml text>
//	  EMSG

// Register: CPAT <regid>
//	     <xml pattern text>
//	     EMSG

// Get:   GETM <regid> <replyid>
//	  <xml text>
//	  EMSG

// Unregister: UPAT <regid>
//	       EMSG

// Done:  DONE <replyid>
//	  EMSG


const char * const	MINCE_HEADER_SEND = "SEND";
const char * const	MINCE_HEADER_REPLY = "RPLY";
const char * const	MINCE_HEADER_REGISTER = "CPAT";
const char * const	MINCE_HEADER_UNREGISTER = "UPAT";
const char * const	MINCE_HEADER_GET = "GETM";
const char * const	MINCE_HEADER_DONE = "DONE";

const char * const	MINCE_TRAILER = "EMSG";




} }	// end of namespace Ivy::Mince



#endif


/* end of mince.H */
