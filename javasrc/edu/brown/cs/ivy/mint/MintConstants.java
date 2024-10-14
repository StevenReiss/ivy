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

String	MINT_DEFAULT_SERVICE_NAME = "mint";
String	MINT_SERVER_START_CMD = IvyFile.expandName("$(IVY)/bin/startmint");




/********************************************************************************/
/*										*/
/*	Master constants							*/
/*										*/
/********************************************************************************/

String	MINT_MASTER_FILE = IvyFile.expandName("$(IVY)/lib/registry/mint.master");
String	MINT_MASTER_CMD = IvyFile.expandName("$(IVY)/bin/startmaster");



/********************************************************************************/
/*										*/
/*	Registry names								*/
/*										*/
/********************************************************************************/

String	MINT_REGISTRY_HOST_PROP = "edu.brown.cs.ivy.mint.registryhost";
String	MINT_REGISTRY_HOST_ENV = "MINT_REGISTRY_HOST";
String	MINT_REGISTRY_PROP = "edu.brown.cs.ivy.mint.registry";
String	MINT_REGISTRY_PREFIX = "edu.brown.cs.ivy.mint.MintMaster[";
String	MINT_MASTER_HOST_PROP = "edu.brown.cs.ivy.mint.master.host";
String	MINT_MASTER_PORT_PROP = "edu.brown.cs.ivy.mint.master.port";




/********************************************************************************/
/*										*/
/*	Message flags								*/
/*										*/
/********************************************************************************/

int	MINT_MSG_NONE = 0;
int	MINT_MSG_NO_REPLY = 0x1;	// no reply expected
int	MINT_MSG_FIRST_REPLY = 0x2;	// return first reply
int	MINT_MSG_ALL_REPLIES = 0x4;	// return after each reply
int	MINT_MSG_NON_NULL_ONLY = 0x8;	// return first non-null
int	MINT_MSG_WAIT_FOR_ALL = 0x10;	// wait for all replies
int	MINT_MSG_FIRST_NON_NULL = 0xa;



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

String MINT_XML_ANY = "_ANY_";     // tag to match Any subtree/value
String MINT_XML_VAR = "_VAR_";     // prefix for variables
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

int MINT_REPLY_NONE = 0;		// no reply needed



/********************************************************************************/
/*										*/
/*	XML description of message mapping					*/
/*										*/
/********************************************************************************/

String MINT_XML_ENVIRONMENT = "edu.brown.cs.ivy.mint.XmlFile";  // file location
String MINT_XML_DEFAULT_FILE = "mintpolicy.xml";

String MINT_XML_MAPPINGS = "MAPPINGS";     // <MAPPINGS> ..

String MINT_XML_MAP = "MAP";               // <MAP>when actions</>
String MINT_XML_MAP_USER = "USER";         //    USER="spr"

String MINT_XML_MATCH = "MATCH";           // <MATCH><pattern></MATCH>

String MINT_XML_FILTER = "FILTER";         // <FILTER><pattern></>

String MINT_XML_WHEN_COND = "COND";        // <COND>when</>
String MINT_XML_COND_VAR = "VAR";          //    VAR="xyz"
String MINT_XML_COND_VALUE = "VALUE";      //    VALUE="abc"

String MINT_XML_ACTIONS = "ACTIONS";       // <ACTIONS>action*</>

String MINT_XML_SEND = "SEND";             // <SEND>msg</>

String MINT_XML_SET = "SET";               // <SET/>
String MINT_XML_SET_VAR = "VAR";           //    VAR="xyz"
String MINT_XML_SET_VALUE = "VALUE";       //    VALUE="abc"

String MINT_XML_YIELD = "YIELD";           // <YIELD>msg</>

String MINT_XML_REPLY = "REPLY";           // <REPLY>msg</>

String MINT_XML_ACTION_COND = "COND";      // <COND>when</>




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


String MINT_HEADER_SEND = "SEND";
String MINT_HEADER_REPLY = "RPLY";
String MINT_HEADER_REGISTER = "CPAT";
String MINT_HEADER_UNREGISTER = "UPAT";
String MINT_HEADER_GET = "GETM";
String MINT_HEADER_DONE = "DONE";

String MINT_TRAILER = "EMSG";


/********************************************************************************/
/*										*/
/*	Mint Context Message Strings						*/
/*										*/
/********************************************************************************/

String MINT_CONTEXT_ELT_MINT = "MINT";
String MINT_CONTEXT_ELT_SET = "CONTEXT_SET";
String MINT_CONTEXT_ELT_GET = "CONTEXT_GET";
String MINT_CONTEXT_ELT_WEB = "WEB";

String MINT_CONTEXT_ELT_STATE = "CONTEXT";



/********************************************************************************/
/*										*/
/*	Interface for host-port in RMI registry 				*/
/*										*/
/********************************************************************************/

interface HostPort extends Remote {

   String getHost() throws RemoteException;
   int getPort() throws RemoteException;

}	// end of interface HostPort



/********************************************************************************/
/*                                                                              */
/*      Short cut for handling arguments                                        */
/*                                                                              */
/********************************************************************************/

class CommandArgs extends HashMap<String,Object> {
   
   private static final long serialVersionUID = 1;
   
   public CommandArgs()                                 { }
   public CommandArgs(String key,Object... args) {
      this();
      if (args.length == 0) return;
      put(key,args[0]);
      for (int i = 2; i < args.length; i += 2) {
         put(args[i-1].toString(),args[i]);
       }
    }
   
   public void put(String key,Object... args) {
      if (args.length == 0) return;
      put(key,args[0]);
      for (int i = 2; i < args.length; i += 2) {
         put(args[i-1].toString(),args[i]);
       }
    }
   
}       // end of inner class CommandArgs



}	// end of interface MintConstants




/* end of MintConstants.java */
