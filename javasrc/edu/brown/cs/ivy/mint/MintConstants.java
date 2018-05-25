/********************************************************************************/
/*										*/
/*		MintConstants.java						*/
/*										*/
/*	Constants for the Mint message interface				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/MintConstants.java,v 1.16 2018/05/25 17:57:03 spr Exp $ */


/***************2*****************************************************************
 *
 * $Log: MintConstants.java,v $
 * Revision 1.16  2018/05/25 17:57:03  spr
 * Add CommandArgs for client usage.
 *
 * Revision 1.15  2016/12/09 21:46:50  spr
 * Formatting
 *
 * Revision 1.14  2013-06-11 23:14:31  spr
 * Code cleanup.
 *
 * Revision 1.13  2011-06-17 12:31:30  spr
 * Use qualified registry name.
 *
 * Revision 1.12  2011-06-06 20:55:41  spr
 * Update to try getting rmi to work better.
 *
 * Revision 1.11  2011-05-27 19:32:42  spr
 * Change copyrights.
 *
 * Revision 1.10  2011-05-17 01:04:55  spr
 * Update mint to allow web-scale messaging.
 *
 * Revision 1.9  2010-02-26 21:05:32  spr
 * Formatting issues and minor additions
 *
 * Revision 1.8  2010-02-12 00:37:05  spr
 * Move int constants to enum.	Handle nodes going down and bad read returns.
 *
 * Revision 1.7  2009-10-02 00:18:27  spr
 * Import clean up.
 *
 * Revision 1.6  2009-06-04 18:50:41  spr
 * Use ivyJava call when necessary.
 *
 * Revision 1.5  2008-11-24 23:36:22  spr
 * Provide for rmi registry based lookup.
 *
 * Revision 1.4  2007-08-10 02:11:04  spr
 * Cleanups from eclipse; bug fixes to avoid deadlock.
 *
 * Revision 1.3  2007-01-03 03:24:32  spr
 * Add combination flags.
 *
 * Revision 1.2  2006/01/30 19:05:48  spr
 * Handle sync only on replies option.
 *
 * Revision 1.1  2005/07/08 23:32:52  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint;


import edu.brown.cs.ivy.file.IvyFile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;



public interface MintConstants
{


/********************************************************************************/
/*										*/
/*	File names								*/
/*										*/
/********************************************************************************/

static final String	MINT_DEFAULT_SERVICE_NAME = "mint";
static final String	MINT_SERVER_START_CMD = IvyFile.expandName("$(IVY)/bin/startmint");




/********************************************************************************/
/*										*/
/*	Master constants							*/
/*										*/
/********************************************************************************/

static final String	MINT_MASTER_FILE = IvyFile.expandName("$(IVY)/lib/registry/mint.master");
static final String	MINT_MASTER_CMD = IvyFile.expandName("$(IVY)/bin/startmaster");



/********************************************************************************/
/*										*/
/*	Registry names								*/
/*										*/
/********************************************************************************/

static final String	MINT_REGISTRY_HOST_PROP = "edu.brown.cs.ivy.mint.registryhost";
static final String	MINT_REGISTRY_HOST_ENV = "MINT_REGISTRY_HOST";
static final String	MINT_REGISTRY_PROP = "edu.brown.cs.ivy.mint.registry";
static final String	MINT_REGISTRY_PREFIX = "edu.brown.cs.ivy.mint.MintMaster[";
static final String	MINT_MASTER_HOST_PROP = "edu.brown.cs.ivy.mint.master.host";
static final String	MINT_MASTER_PORT_PROP = "edu.brown.cs.ivy.mint.master.port";




/********************************************************************************/
/*										*/
/*	Message flags								*/
/*										*/
/********************************************************************************/

static final int	MINT_MSG_NONE = 0;
static final int	MINT_MSG_NO_REPLY = 0x1;	// no reply expected
static final int	MINT_MSG_FIRST_REPLY = 0x2;	// return first reply
static final int	MINT_MSG_ALL_REPLIES = 0x4;	// return after each reply
static final int	MINT_MSG_NON_NULL_ONLY = 0x8;	// return first non-null
static final int	MINT_MSG_WAIT_FOR_ALL = 0x10;	// wait for all replies
static final int	MINT_MSG_FIRST_NON_NULL = 0xa;



/********************************************************************************/
/*										*/
/*	Synchronization flags							*/
/*										*/
/********************************************************************************/

enum MintSyncMode {
   NONE,		      // not used
   SINGLE,		      // 1 message, 1 reply at a time (independent)
   MULTIPLE,		      // n messages, n replies at once (independent)
   REPLIES,		      // 1 message or reply at a time (not safe ?)
   ONLY_REPLIES,	      // n messages, 1 reply at a time
   POLL,		      // user poll for messages
   POLL_REPLIES,	      // poll separately for messages/replies
}




/********************************************************************************/
/*										*/
/*	Xml Definitions for patterns						*/
/*										*/
/********************************************************************************/

static final String MINT_XML_ANY = "_ANY_";     // tag to match Any subtree/value
static final String MINT_XML_VAR = "_VAR_";     // prefix for variables
						// variables consist of this tag
						// followed by a number, i.e.
						// <_VAR_0 />, <_VAR_1 />, ...
						// or <X y=_VAR_0 z=_VAR_1>
						// or <X>_VAR_0</X>



/********************************************************************************/
/*										*/
/*	Local flags for handling replys 					*/
/*										*/
/********************************************************************************/

static final int MINT_REPLY_NONE = 0;		// no reply needed



/********************************************************************************/
/*										*/
/*	XML description of message mapping					*/
/*										*/
/********************************************************************************/

static final String MINT_XML_ENVIRONMENT = "edu.brown.cs.ivy.mint.XmlFile";  // file location
static final String MINT_XML_DEFAULT_FILE = "mintpolicy.xml";

static final String MINT_XML_MAPPINGS = "MAPPINGS";     // <MAPPINGS> ..

static final String MINT_XML_MAP = "MAP";               // <MAP>when actions</>
static final String MINT_XML_MAP_USER = "USER";         //    USER="spr"

static final String MINT_XML_MATCH = "MATCH";           // <MATCH><pattern></MATCH>

static final String MINT_XML_FILTER = "FILTER";         // <FILTER><pattern></>

static final String MINT_XML_WHEN_COND = "COND";        // <COND>when</>
static final String MINT_XML_COND_VAR = "VAR";          //    VAR="xyz"
static final String MINT_XML_COND_VALUE = "VALUE";      //    VALUE="abc"

static final String MINT_XML_ACTIONS = "ACTIONS";       // <ACTIONS>action*</>

static final String MINT_XML_SEND = "SEND";             // <SEND>msg</>

static final String MINT_XML_SET = "SET";               // <SET/>
static final String MINT_XML_SET_VAR = "VAR";           //    VAR="xyz"
static final String MINT_XML_SET_VALUE = "VALUE";       //    VALUE="abc"

static final String MINT_XML_YIELD = "YIELD";           // <YIELD>msg</>

static final String MINT_XML_REPLY = "REPLY";           // <REPLY>msg</>

static final String MINT_XML_ACTION_COND = "COND";      // <COND>when</>




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


static final String MINT_HEADER_SEND = "SEND";
static final String MINT_HEADER_REPLY = "RPLY";
static final String MINT_HEADER_REGISTER = "CPAT";
static final String MINT_HEADER_UNREGISTER = "UPAT";
static final String MINT_HEADER_GET = "GETM";
static final String MINT_HEADER_DONE = "DONE";

static final String MINT_TRAILER = "EMSG";


/********************************************************************************/
/*										*/
/*	Mint Context Message Strings						*/
/*										*/
/********************************************************************************/

static final String MINT_CONTEXT_ELT_MINT = "MINT";
static final String MINT_CONTEXT_ELT_SET = "CONTEXT_SET";
static final String MINT_CONTEXT_ELT_GET = "CONTEXT_GET";
static final String MINT_CONTEXT_ELT_WEB = "WEB";

static final String MINT_CONTEXT_ELT_STATE = "CONTEXT";



/********************************************************************************/
/*										*/
/*	Interface for host-port in RMI registry 				*/
/*										*/
/********************************************************************************/

interface HostPort extends Remote {

   public String getHost() throws RemoteException;
   public int getPort() throws RemoteException;

}	// end of interface HostPort



/********************************************************************************/
/*                                                                              */
/*      Short cut for handling arguments                                        */
/*                                                                              */
/********************************************************************************/

class CommandArgs extends HashMap<String,Object> {
   
   private static final long serialVersionUID = 1;
   
   public CommandArgs()                                 { }
   public CommandArgs(String key,Object ... args) {
      this();
      if (args.length == 0) return;
      put(key,args[0]);
      for (int i = 2; i < args.length; i += 2) {
         put(args[i-1].toString(),args[i]);
       }
    }
   
   public void put(String key,Object ... args) {
      if (args.length == 0) return;
      put(key,args[0]);
      for (int i = 2; i < args.length; i += 2) {
         put(args[i-1].toString(),args[i]);
       }
    }
   
}       // end of inner class CommandArgs



}	// end of interface MintConstants




/* end of MintConstants.java */
