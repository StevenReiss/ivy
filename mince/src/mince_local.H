/********************************************************************************/
/*										*/
/*		mince_local.H							*/
/*										*/
/*	Internal definitions for MINCE Message interface communications environ */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/mince/src/mince_local.H,v 1.2 2013/11/08 01:34:08 spr Exp $ */

/*********************************************************************************
 *
 * $Log: mince_local.H,v $
 * Revision 1.2  2013/11/08 01:34:08  spr
 * Updates for new g++.
 *
 * Revision 1.1  2005/06/28 17:21:59  spr
 * Move mince from tea to ivy for use in veld, etc.
 *
 *
 ********************************************************************************/


#ifndef MINCE_LOCAL_INCLUDED_ALREADY
#define MINCE_LOCAL_INCLUDED_ALREADY


#include "mince.H"

#include <ivy_datatypes.H>
#include <ivy_hash.H>
#include <ivy_string.H>
#include <ivy_file.H>




using namespace Ivy;
using namespace Ivy::Mince;


namespace Ivy {

namespace Mince {



/************************************************************************/
/*									*/
/*	Constants							*/
/*									*/
/************************************************************************/


const char * const MINCE_SERVER_START_CMD = "$(IVY)/bin/startmint";
const char * const MINCE_DEFAULT_SERVICE_NAME = "mint";
const char * const MINCE_MASTER_CMD = "$(IVY)/bin/startmaster";
const char * const MINCE_MASTER_FILE = "$(IVY)/lib/registry/mint.master";





/********************************************************************************/
/*										*/
/*	Mint Context Message Strings						*/
/*										*/
/********************************************************************************/

const char * const	MINCE_CONTEXT_ELT_MINT = "MINT";
const char * const	MINCE_CONTEXT_ELT_SET = "CONTEXT_SET";
const char * const	MINCE_CONTEXT_ELT_GET = "CONTEXT_GET";

const char * const	MINCE_CONTEXT_ELT_STATE = "CONTEXT";




/********************************************************************************/
/*										*/
/*	Forward Type Definitions						*/
/*										*/
/********************************************************************************/


typedef class MinceClientInfo * 		MinceClient;
typedef class MinceActiveDataInfo *		MinceActiveData;
typedef class MincePatternDataInfo *		MincePatternData;

typedef class MinceMessageDataInfo *		MinceMessageData;
typedef class MinceReplyDataInfo *		MinceReplyData;
typedef class MinceDoneDataInfo *		MinceDoneData;

typedef class MinceClientMessageInfo *		MinceClientMessage;

typedef class MinceMatchSelectorInfo *		MinceMatchSelector;
typedef class MinceMatchArgumentsInfo * 	MinceMatchArguments;
typedef class MinceArgDataInfo *		MinceArgData;

typedef class MinceMasterDataInfo *		MinceMasterData;



typedef HashMap<int,MinceActiveData>		MinceReplyTable;

typedef HashMap<int,MincePatternData>		MincePatternTable;
typedef MincePatternTable::iterator		MincePatternTableIter;

typedef List<MinceRequest>			MinceQueueList;

typedef Vector<MinceArgData>			MinceArgumentList;
typedef MinceArgumentList::iterator		MinceArgumentListIter;



/********************************************************************************/
/*										*/
/*	MinceClient -- local implementation of MinceControl			*/
/*										*/
/********************************************************************************/


class MinceClientInfo : public MinceControlInfo {

private:
   MinceSyncFlag synch_mode;
   MinceControlHandler control_handler;
   int reply_counter;
   int pat_counter;
   MinceErrorHandler error_handler;
   StdString mince_name;
   MinceReplyTable reply_hash;
   MincePatternTable pattern_hash;
   MinceQueueList message_queue;
   MinceQueueList reply_queue;
   IvySocket server_socket;

public:
   MinceClientInfo(CStdString id,MinceSyncFlag mode,MinceControlHandler);
   ~MinceClientInfo();

   void shutDown();

   CStdString getMinceName() const			{ return mince_name; }

   void send(CStdString msg,MinceReply rply,MinceMessageFlag);

   void addPattern(CStdString pat,MinceHandler hdlr) {
      handleRegister(pat,IvyXml::convertStringToXml(pat),hdlr);
    }
   void addPattern(IvyXmlNode pat,MinceHandler hdlr) {
      handleRegister(pat.convertToString(),pat,hdlr);
    }
   void removePattern(MinceHandler);

   void registerErrorHandler(MinceErrorHandler hdlr)	{ error_handler = hdlr; }

   int getNumPolledMessages() const			{ return message_queue.size(); }
   MinceRequest pollNext(Bool wait);
   MinceRequest pollReplies(Bool wait = false);

   void processInput(CStdString hdr,CStdString body);

   void process(MinceRequest mr);
   void processMessage(MinceMessageData md);
   void processReply(MinceReplyData rd);
   void processDone(MinceDoneData dd);

public:
   void generateReply(int rid,CStdString r);
   void generateReply(int rid,IvyXmlNode r) {
      generateReply(rid,r.convertToString());
    }

private:
   void handleRegister(CStdString pat,IvyXmlNode xpat,MinceHandler hdlr);

   void serverError(CStdString what);

   void handleMessage(int mid,int rid,IvyXmlNode xml);
   void handleReply(int rid,IvyXmlNode doc);
   void handleDone(int rid);

   void queueMessage(MinceRequest);
   void queueReply(MinceRequest);

   MinceRequest getNextMessage(Bool wait);
   MinceRequest getNextReply(Bool wait);
};




/********************************************************************************/
/*										*/
/*	MinceActiveData -- holder of information on active messages		*/
/*										*/
/********************************************************************************/


class MinceActiveDataInfo {

private:
   MinceMessage for_message;
   MinceReply reply_handler;

public:
   MinceActiveDataInfo(MinceMessage mm,MinceReply mr) {
      for_message = mm;
      reply_handler = mr;
    }
   ~MinceActiveDataInfo() {
      delete for_message;
    }

   MinceMessage getMessage() const		{ return for_message; }
   MinceReply getReplyHandler() const		{ return reply_handler; }
};




/********************************************************************************/
/*										*/
/*	MincePatternData -- holder of information on an active pattern		*/
/*										*/
/********************************************************************************/


class MincePatternDataInfo {

private:
   IvyXmlNode pattern_xml;
   MinceHandler use_handler;
   MinceMatchSelector pattern_matcher;

public:
   MincePatternDataInfo(IvyXmlNode n,MinceHandler hdlr);
   ~MincePatternDataInfo();

   IvyXmlNode getXml() const			{ return pattern_xml; }
   MinceHandler getHandler() const		{ return use_handler; }

   Bool match(MinceMessage);
};




/********************************************************************************/
/*										*/
/*	MinceRequest -- holder of data for queued message			*/
/*										*/
/********************************************************************************/


class MinceRequestInfo {

private:
   int reply_id;

protected:
   MinceRequestInfo(int rid)				{ reply_id = rid; }

public:
   virtual ~MinceRequestInfo()			{ }

   virtual void process(MinceClient) = 0;

   int getReplyId() const			{ return reply_id; }
};



class MinceMessageDataInfo : public MinceRequestInfo {

private:
   int message_id;
   IvyXmlNode message_body;

public:
   MinceMessageDataInfo(int mid,int rid,IvyXmlNode x) : MinceRequestInfo(rid) {
      message_id = mid;
      message_body = x;
    }
   ~MinceMessageDataInfo()			{ }

   void process(MinceClient mc) 		{ mc->processMessage(this); }

   int getMessageId() const			{ return message_id; }
   IvyXmlNode getMessageBody() const		{ return message_body; }
};



class MinceReplyDataInfo : public MinceRequestInfo {

private:
   IvyXmlNode reply_body;

public:
   MinceReplyDataInfo(int rid,IvyXmlNode x) : MinceRequestInfo(rid) {
      reply_body = x;
    }
   ~MinceReplyDataInfo()			{ }

   void process(MinceClient mc) 		{ mc->processReply(this); }

   IvyXmlNode getReplyBody() const		{ return reply_body; }
};



class MinceDoneDataInfo : public MinceRequestInfo {

public:
   MinceDoneDataInfo(int rid) : MinceRequestInfo(rid)		{ }
   ~MinceDoneDataInfo() 			{ }

   void process(MinceClient mc) 		{ mc->processDone(this); }
};



/********************************************************************************/
/*										*/
/*	MinceClientMessage -- message implementation				*/
/*										*/
/********************************************************************************/


class MinceClientMessageInfo : public MinceMessageInfo {

private:
   StdString message_text;
   IvyXmlNode message_xml;
   MinceClient for_client;
   int reply_id;

public:
   MinceClientMessageInfo(MinceClient,CStdString,int rid);
   MinceClientMessageInfo(MinceClient,IvyXmlNode,int rid);
   virtual ~MinceClientMessageInfo();

   CStdString getText();
   IvyXmlNode getXml();

   void replyTo();
   void replyTo(CStdString);
   void replyTo(IvyXmlNode);
};




/********************************************************************************/
/*										*/
/*	MinceMatchSelector -- pattern matcher					*/
/*										*/
/********************************************************************************/


class MinceMatchSelectorInfo {

private:
   IvyXmlNode pattern_root;
   StdString pattern_text;

public:
   MinceMatchSelectorInfo(CStdString pat);
   MinceMatchSelectorInfo(IvyXmlNode);
   ~MinceMatchSelectorInfo();

   CStdString getText();
   IvyXmlNode getXml() const			{ return pattern_root; }

   MinceArguments matchMessage(MinceMessage msg) const;
   Bool testMatchMessage(MinceMessage msg) const;

private:
   Bool doMatch(IvyXmlNode msg,MinceMatchArguments args) const;
   Bool matchElement(IvyXmlNode pat,IvyXmlNode msg,MinceMatchArguments args) const;
   Bool matchAttrs(IvyXmlNode pat,IvyXmlNode msg,MinceMatchArguments args) const;
   Bool matchChildren(IvyXmlNode pat,IvyXmlNode msg,MinceMatchArguments args) const;
};




/********************************************************************************/
/*										*/
/*	MinceMatchArguments -- implementation of argument set for matching	*/
/*										*/
/********************************************************************************/


class MinceMatchArgumentsInfo : public MinceArgumentsInfo {

private:
   MinceArgumentList arg_list;

public:
   MinceMatchArgumentsInfo();
   virtual ~MinceMatchArgumentsInfo();

   int getNumArguments() const			{ return arg_list.size(); }

   void setArgument(int idx,Integer v);
   void setArgument(int idx,Float v);
   void setArgument(int idx,IvyXmlNode);
   void setArgument(int idx,CStdString);

   StdString getArgument(int idx) const;
   Integer getIntArgument(int idx) const;
   Float getRealArgument(int idx) const;
   IvyXmlNode getXmlArgument(int idx) const;

private:
   void setArgument(int idx,MinceArgData);
   MinceArgData getArgumentData(int idx) const;
};




/********************************************************************************/
/*										*/
/*	MinceArgData -- holder of data for an argument				*/
/*										*/
/********************************************************************************/


class MinceArgDataInfo {

protected:
   MinceArgDataInfo()					{ }

public:
   virtual ~MinceArgDataInfo()				{ }

   virtual Integer getIntValue() const = 0;
   virtual Float getFloatValue() const = 0;
   virtual StdString getStringValue() const = 0;
   virtual IvyXmlNode getXmlValue() const = 0;
};



class MinceArgDataIntInfo : public MinceArgDataInfo {

private:
   Integer int_value;

public:
   MinceArgDataIntInfo(Integer v)			{ int_value = v; }

   Integer getIntValue() const				{ return int_value; }
   Float getFloatValue() const				{ return int_value; }
   StdString getStringValue() const			{ return ltoa(int_value); }
   IvyXmlNode getXmlValue() const			{ return IvyXmlNode(); }
};




class MinceArgDataRealInfo : public MinceArgDataInfo {

private:
   Float float_value;

public:
   MinceArgDataRealInfo(Float v)			{ float_value = v; }

   Integer getIntValue() const				{ return (int) float_value; }
   Float getFloatValue() const				{ return float_value; }
   StdString getStringValue() const			{ return ftoa(float_value); }
   IvyXmlNode getXmlValue() const			{ return IvyXmlNode(); }
};




class MinceArgDataStringInfo : public MinceArgDataInfo {

private:
   StdString string_value;

public:
   MinceArgDataStringInfo(CStdString v) 		{ string_value = v; }

   Integer getIntValue() const				{ return atoi(string_value); }
   Float getFloatValue() const				{ return atof(string_value); }
   StdString getStringValue() const			{ return string_value; }
   IvyXmlNode getXmlValue() const			{ return IvyXmlNode(); }
};




class MinceArgDataXmlInfo : public MinceArgDataInfo {

private:
   IvyXmlNode xml_value;

public:
   MinceArgDataXmlInfo(IvyXmlNode v)			{ xml_value = v; }

   Integer getIntValue() const				{ return 0; }
   Float getFloatValue() const				{ return 0; }
   StdString getStringValue() const			{ return xml_value.convertToString(); }
   IvyXmlNode getXmlValue() const			{ return xml_value; }
};




/************************************************************************/
/*									*/
/*	MinceMasterData -- support for master socket			*/
/*									*/
/************************************************************************/


class MinceMasterDataInfo {

private:
   int lock_file;

   static IvySocket master_socket;

public:
   MinceMasterDataInfo();
   ~MinceMasterDataInfo();

   Bool isValid()			{ return master_socket != NULL; }

   IvySocket setupClient(CStdString id);

private:
   StdString getMasterFile();
   void lock(CStdString);
   void unlock(CStdString);

   IvyFileName lockFile(CStdString file);
};




} }	// end of namespace Ivy::Mince


#endif


/* end of mince_local.H */


