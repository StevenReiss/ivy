/********************************************************************************/
/*										*/
/*		MintControl.java						*/
/*										*/
/*	Controller for the Mint message interface				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Redistribution and use in source and binary forms, with or without		 *
 *  modification, are permitted provided that the following conditions are met:  *
 *										 *
 *  + Redistributions of source code must retain the above copyright notice,	 *
 *	this list of conditions and the following disclaimer.			 *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *	this list of conditions and the following disclaimer in the		 *
 *	documentation and/or other materials provided with the distribution.	 *
 *  + Neither the name of the Brown University nor the names of its		 *
 *	contributors may be used to endorse or promote products derived from	 *
 *	this software without specific prior written permission.		 *
 *										 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE	 *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE	 *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE	 *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 	 *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF	 *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS	 *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN	 *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)	 *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE	 *
 *  POSSIBILITY OF SUCH DAMAGE. 						 *
 *										 *
 ********************************************************************************/


package edu.brown.cs.ivy.mint;


import edu.brown.cs.ivy.mint.client.MintClient;

import org.w3c.dom.Element;



public abstract class MintControl implements MintConstants
{


/********************************************************************************/
/*										*/
/*	Methods for creating a controller					*/
/*										*/
/********************************************************************************/

/**
 *	This routine creates a MintControl object for communication.  The path
 *	argument should either be a complete pathname or (more likely) should
 *	be a unique name that will correspond to a file in the mint temporary
 *	directory that lists the host/port for communication.  Note that this
 *	might eventually be generallized to support a meta controller that
 *	maps that name to a host-port.
 *
 *	The second parameter is used to specify the synchronization
 *	mode for the controller. This mode determines how incoming messages
 *	are handled.  If the mode is MINT_SYNC_MULTIPLE, then a thread is created
 *	for each incoming message and the handler is called in that thread.  Here
 *	it is up to the application to synchronize things.  Otherwise, incoming
 *	messages are kept on a queue. If the mode is MINT_SYNC_SINGLE, then a single
 *	thread is used to process the messages one at a time.  When the handler
 *	for a message returns, the next message is processed.  If users wants to
 *	process multiple messages in specific instances, they can create a new
 *	thread to do the local processing.  In this case replies are still handled
 *	asynchronously (i.e. at any time).  If the mode is MINT_SYNC_REPLIES
 *	then replies are also handled by the message thread.  Finally, if the mode
 *	is MINT_SYNC_POLL, then messages are queued and it is up to the user to call
 *	the method MintControl.pollNext() to process the next message/reply.
 **/

public static MintControl create(String path,MintSyncMode syncmode)
{
   return new MintClient(path,syncmode);
}




public abstract void shutDown();



/********************************************************************************/
/*										*/
/*	Methods for creating messages						*/
/*										*/
/********************************************************************************/

/**
 *	Broadcast a simple message.  The message will not be replied to and will
 *	be sent asynchronously.  The message can either be given as an XML string
 *	or as an XML document.
 **/

public void send(String msg)
{
   send(msg,null,MINT_MSG_NO_REPLY);
}


public void send(Element msg)
{
   send(msg,null,MINT_MSG_NO_REPLY);
}




/**
 *	Standard message send.	The reply argument can either be null indicating
 *	that no reply is wanted, or it can be a MintReply callback object that
 *	will be invoked when a reply is forthcoming.  The flags value (from
 *	MINT_MSG_*) indicates how the reply should be handled.
 **/

public abstract void send(String msg,MintReply reply,int flags);

public abstract void send(Element msg,MintReply reply,int flags);




/********************************************************************************/
/*										*/
/*	Methods for registering patterns of interest with the controller	*/
/*										*/
/********************************************************************************/

/**
 *	Register the pattern specified by the first argument (either a string or
 *	an XML document) as something of interest.  If a message matching the
 *	pattern is sent, the handler specified by the second argument will be
 *	invoked.
 **/

public abstract void register(String pattern,MintHandler hdlr);

public abstract void register(Element pattern,MintHandler hdlr);



/**
 *	Unregister all patterns for a given handler.
 **/

public abstract void unregister(MintHandler hdlr);




/********************************************************************************/
/*										*/
/*	Methods for handling polling						*/
/*										*/
/********************************************************************************/


/**
 *	Return the number of queued messages
 **/

public abstract int getNumPolledMessages();



/**
 *	Handle the next message.  The message is handled in the current thread.
 *	The argument is used to determine if the thread should wait if
 *	no messages are pending or if it should return immediately.  It returns
 *	true if a message was processed.
 **/

public abstract boolean pollNext(boolean wait);


/**
 *	Process any replies that are queued up.  Normally replies will be handled
 *	in the same order as they are queued.  This method processes all currently
 *	queued replies and then returns. It is only valid in mode
 *	MINT_SYNC_POLL_REPLIES
 **/

public abstract void pollReplies();




/********************************************************************************/
/*										*/
/*	Methods for handling exceptions 					*/
/*										*/
/********************************************************************************/

/**
 *	Register a handler to capture server errors.  An I/O error from the
 *	server normally means the server has terminated and this client should
 *	probably do the same.
 **/

public abstract void registerErrorHandler(MintErrorHandler hdlr);



/********************************************************************************/
/*										*/
/*	Miscellaneous access methods						*/
/*										*/
/********************************************************************************/

/**
 *	Return the name of this mint server.
 **/

public abstract String getMintName();





}	// end of abstract class MintControl




/* end of MintControl.java */
