/********************************************************************************/
/*										*/
/*		IvyTest.java							*/
/*										*/
/*	Simple program used to check if 64 bit java works			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/exec/IvyTest.java,v 1.7 2013/11/08 00:03:49 spr Exp $ */


/*********************************************************************************
 *
 * $Log: IvyTest.java,v $
 * Revision 1.7  2013/11/08 00:03:49  spr
 * Minor update
 *
 * Revision 1.6  2011-05-27 19:32:35  spr
 * Change copyrights.
 *
 * Revision 1.5  2011-02-17 23:15:38  spr
 * Add format test.
 *
 * Revision 1.4  2009-09-17 01:55:24  spr
 * Use jps or equivalent to find processes; add setup code for windows, etc.
 *
 * Revision 1.3  2007-05-04 01:59:26  spr
 * Change test program to test ExecQuery.
 *
 * Revision 1.2  2006/07/10 14:52:13  spr
 * Code cleanup.
 *
 * Revision 1.1  2006/06/23 02:15:54  spr
 * Test program for 64 bit java.
 *
 *
 ********************************************************************************/



package edu.brown.cs.ivy.exec;


import edu.brown.cs.ivy.file.IvyFormat;



public class IvyTest
{


public static void main(String [] args)
{
   String pid = IvyExecQuery.getProcessId();
   System.err.println("PID = " + pid);

   System.err.println("FORMAT = " + IvyFormat.formatTypeName("(QClass<*>;QList<QLocation;>;)"));

   String [] carg = IvyExecQuery.getCommandLine();
   System.err.println("Command line: ");
   for (int i = 0; i < carg.length; ++i) {
      System.err.println("\t" + i + ": " + carg[i]);
    }
   System.exit(0);
}



}	// end of class IvyTest



/* end of IvyTest.java */
