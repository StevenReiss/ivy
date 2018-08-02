/********************************************************************************/
/*										*/
/*		MintTest.java							*/
/*										*/
/*	Test program for the MINT messaging interface				*/
/*										*/
/********************************************************************************/
/*	Copyright 1998 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Redistribution and use in source and binary forms, with or without           *
 *  modification, are permitted provided that the following conditions are met:  *
 *                                                                               *
 *  + Redistributions of source code must retain the above copyright notice,     *
 *      this list of conditions and the following disclaimer.                    *
 *  + Redistributions in binary form must reproduce the above copyright notice,  *
 *      this list of conditions and the following disclaimer in the              *
 *      documentation and/or other materials provided with the distribution.     *
 *  + Neither the name of the Brown University nor the names of its              *
 *      contributors may be used to endorse or promote products derived from     *
 *      this software without specific prior written permission.                 *
 *                                                                               *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  *
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    *
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   *
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    *
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          *
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         *
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     *
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      *
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      *
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   *
 *  POSSIBILITY OF SUCH DAMAGE.                                                  *
 *                                                                               *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/test/MintTest.java,v 1.7 2015/11/20 15:09:21 spr Exp $ */


/*********************************************************************************
 *
 * $Log: MintTest.java,v $
 * Revision 1.7  2015/11/20 15:09:21  spr
 * Reformatting.
 *
 * Revision 1.6  2011-05-27 19:32:46  spr
 * Change copyrights.
 *
 * Revision 1.5  2011-05-17 01:05:18  spr
 * Test web scale messaging.
 *
 * Revision 1.4  2010-02-12 00:38:12  spr
 * Change test message.
 *
 * Revision 1.3  2007-05-04 02:00:26  spr
 * Fix bugs related to polling.
 *
 * Revision 1.2  2006/02/21 17:06:29  spr
 * Upgrade interface to use Element instead of Node for XML.
 *
 * Revision 1.1  2005/07/08 23:33:10  spr
 * Add mint (Java message interface) to ivy.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.mint.test;


import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintControl;
import edu.brown.cs.ivy.mint.MintHandler;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.mint.MintReply;



public class MintTest implements MintConstants
{


static public void main(String [] args)
{
   MintTest mt = new MintTest(args);

   mt.testB();
   mt.testA();
   System.exit(0);
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private int	message_count;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private MintTest(String [] args)
{
   message_count = 0;
}


/********************************************************************************/
/*										*/
/*	General routines							*/
/*										*/
/********************************************************************************/

private synchronized void waitForCompletion()
{
   while (message_count > 0) {
      try { wait(); }
      catch (InterruptedException e) { }
    }
}


private synchronized void addMessage(int ct)
{
   message_count += ct;
}



private synchronized void countMessage()
{
   --message_count;
   notifyAll();
}



/********************************************************************************/
/*										*/
/*	TestA :: basic tests							*/
/*										*/
/********************************************************************************/

private void testA()
{
   MintControl mc = MintControl.create(null,MintSyncMode.REPLIES);

   addMessage(5);	       // # messages expected back
   mc.send("<MINT><WEB KEY='hello' URL='http://conifer.cs.brown.edu:8180/mint/mint' ID='spr' /></MINT>");

   mc.register(
      "<Sample Field1='_VAR_0'> <Subarg1 SubField1='_VAR_1' /> </Sample>",
      new TestAHandler(false));
   mc.register(
      "<Sample Field1='_VAR_0'> <Subarg1 SubField1='_VAR_1' /> </Sample>",
      new TestAHandler(true));

   mc.send("<Sample Field1='xxx2'> <Subarg1 SubField1='yyy2' /> </Sample>",
	      new TestAReplyHandler(),MINT_MSG_FIRST_NON_NULL);

   mc.send("<Sample Field1='xxx1'> <Subarg1 SubField1='yyy1' /> </Sample>",
	      null,MINT_MSG_NO_REPLY);

   waitForCompletion();

   try {
      Thread.sleep(10000);
    }
   catch (InterruptedException e) { }

   mc.shutDown();
}



private void testB()
{
   MintControl mc = MintControl.create("NClient",MintSyncMode.MULTIPLE);
   mc.register("<N_SET_ANNOT File='_VAR_0' Line='_VAR_1' Type='_VAR_2' Data='_VAR_3' />",
		  null);
   mc.shutDown();
}



private class TestAHandler implements MintHandler {

   private boolean reply_flag;

   TestAHandler(boolean fg)		{ reply_flag = fg; }

   @Override public void receive(MintMessage msg,MintArguments args) {
      System.out.println("Message received: " + msg.getText());
      System.out.println("Arguments: " + args.getArgument(0) + " , " +
			    args.getArgument(1));
      if (reply_flag) msg.replyTo("<Ack />");
      else msg.replyTo();
      countMessage();
    }

}	// end of subclass TestAHandler



private class TestAReplyHandler implements MintReply {

   @Override public void handleReply(MintMessage msg,MintMessage reply) {
      System.out.println("Reply received for: " + msg.getText());
      System.out.println("Reply is: " + reply.getText());
      countMessage();
    }

   @Override public void handleReplyDone(MintMessage msg) {
      System.out.println("Reply done for: " + msg.getText());
    }

}	// end of subclass TestAReplyHandler








}	// end of class MintTest



/* end of MintTest.java */
