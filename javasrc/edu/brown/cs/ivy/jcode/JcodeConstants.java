/********************************************************************************/
/*										*/
/*		JcodeConstants.java						*/
/*										*/
/*	Byte code definitions constants 					*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
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



package edu.brown.cs.ivy.jcode;

import org.objectweb.asm.Opcodes;



public interface JcodeConstants extends Opcodes
{


int ASM_API = Opcodes.ASM9;


/********************************************************************************/
/*										*/
/*	Other op codes (skipped in ASM)					*/
/*										*/
/********************************************************************************/

int LDC_W = 19;
int LDC2_W = 20;

int ILOAD_0 = 26;
int ILOAD_1 = 27;
int ILOAD_2 = 28;
int ILOAD_3 = 29;
int LLOAD_0 = 30;
int LLOAD_1 = 31;
int LLOAD_2 = 32;
int LLOAD_3 = 33;
int FLOAD_0 = 34;
int FLOAD_1 = 35;
int FLOAD_2 = 36;
int FLOAD_3 = 37;
int DLOAD_0 = 38;
int DLOAD_1 = 39;
int DLOAD_2 = 40;
int DLOAD_3 = 41;
int ALOAD_0 = 42;
int ALOAD_1 = 43;
int ALOAD_2 = 44;
int ALOAD_3 = 45;

int ISTORE_0 = 59;
int ISTORE_1 = 60;
int ISTORE_2 = 61;
int ISTORE_3 = 62;
int LSTORE_0 = 63;
int LSTORE_1 = 64;
int LSTORE_2 = 65;
int LSTORE_3 = 66;
int FSTORE_0 = 67;
int FSTORE_1 = 68;
int FSTORE_2 = 69;
int FSTORE_3 = 70;
int DSTORE_0 = 71;
int DSTORE_1 = 72;
int DSTORE_2 = 73;
int DSTORE_3 = 74;
int ASTORE_0 = 75;
int ASTORE_1 = 76;
int ASTORE_2 = 77;
int ASTORE_3 = 78;

int WIDE = 196;
int GOTO_W = 200;
int JSR_W = 201;



}	// end of interface JcodeConstants




/* end of JcodeConstants.java */

