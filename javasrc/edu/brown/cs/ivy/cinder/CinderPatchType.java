/********************************************************************************/
/*										*/
/*		CinderPatchType.java						*/
/*										*/
/*	Contains information about a patching style				*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/cinder/CinderPatchType.java,v 1.4 2009-09-17 01:54:51 spr Exp $ */


/*********************************************************************************
 *
 * $Log: CinderPatchType.java,v $
 * Revision 1.4  2009-09-17 01:54:51  spr
 * Enable patching at line number level.
 *
 * Revision 1.3  2008-11-12 13:44:40  spr
 * No changes.
 *
 * Revision 1.2  2004/05/05 02:28:08  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1  2003/03/29 03:40:25  spr
 * Move CINDER interface to JikesBT from Bloom to Ivy.
 *
 *
 ********************************************************************************/




package edu.brown.cs.ivy.cinder;







public class CinderPatchType
{




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private boolean patch_blocks;
private int	entry_args;
private int	exit_values;
private boolean patch_synch;
private boolean remote_access;
private boolean local_access;
private boolean patch_writes;
private boolean patch_allocs;
private boolean patch_calls;
private boolean patch_any;

private CinderInstrumenter patch_coder;



/********************************************************************************/
/*										*/
/*	Constructors/destructors						*/
/*										*/
/********************************************************************************/


public CinderPatchType()
{
   patch_blocks = false;
   entry_args = -1;
   exit_values = -1;
   patch_synch = false;
   remote_access = false;
   local_access = false;
   patch_writes = false;
   patch_allocs = false;
   patch_calls = false;
   patch_any = false;
   patch_coder = null;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public boolean getPatchBasicBlocks()		{ return patch_blocks; }

public boolean getPatchEntry()			{ return entry_args >= 0; }

public int getPatchEntryArgs()			{ return entry_args; }

public boolean getPatchExit()			{ return exit_values >= 0; }

public boolean getPatchExitValue()		{ return exit_values > 0; }

public boolean getPatchSynchronization()	{ return patch_synch; }

public boolean getPatchLocalAccess()		{ return local_access; }

public boolean getPatchRemoteAccess()		{ return remote_access; }

public boolean getPatchWrites() 		{ return patch_writes; }

public boolean getPatchAllocs() 		{ return patch_allocs; }

public boolean getPatchCalls()			{ return patch_calls; }

public boolean getPatchAny()			{ return patch_any; }

public CinderInstrumenter getInstrumenter()	{ return patch_coder; }




/********************************************************************************/
/*										*/
/*	Set methods								*/
/*										*/
/********************************************************************************/

public void setPatchBasicBlocks(boolean fg)		{ patch_blocks = fg; }

public void setPatchEntry(boolean fg,int args)
{
   if (!fg) entry_args = -1;
   else entry_args = args;
}

public void setPatchExit(boolean fg,boolean val)
{
   if (!fg) exit_values = -1;
   else if (!val) exit_values = 0;
   else exit_values = 1;
}

public void setPatchSynchronization(boolean fg) 	{ patch_synch = fg; }

public void setPatchLocalAccess(boolean fg)		{ local_access = fg; }

public void setPatchRemoteAccess(boolean fg)		{ remote_access = fg; }

public void setPatchWrites(boolean fg)			{ patch_writes = fg; }

public void setPatchAllocs(boolean fg)			{ patch_allocs = fg; }

public void setPatchCalls(boolean fg)			{ patch_calls = fg; }

public void setPatchAny(boolean fg)			{ patch_any = fg; }

public void setInstrumenter(CinderInstrumenter ci)	{ patch_coder = ci; }




}	// end of class CinderPatchType




/* end of CinderPatchType.java */
