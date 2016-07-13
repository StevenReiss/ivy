/********************************************************************************/
/*										*/
/*		PetalUndoSupport.java						*/
/*										*/
/*	Extension of UndoableEditSupport for use with Petal			*/
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/petal/PetalUndoSupport.java,v 1.8 2015/11/20 15:09:24 spr Exp $ */


/*********************************************************************************
 *
 * $Log: PetalUndoSupport.java,v $
 * Revision 1.8  2015/11/20 15:09:24  spr
 * Reformatting.
 *
 * Revision 1.7  2012-01-12 01:27:23  spr
 * Formatting
 *
 * Revision 1.6  2011-09-12 20:50:27  spr
 * Code cleanup.
 *
 * Revision 1.5  2011-05-27 19:32:49  spr
 * Change copyrights.
 *
 * Revision 1.4  2007-05-04 02:00:35  spr
 * Import fixups.
 *
 * Revision 1.3  2005/05/07 22:25:43  spr
 * Updates for java 5.0
 *
 * Revision 1.2  2004/05/05 02:28:09  spr
 * Update import lists using eclipse.
 *
 * Revision 1.1  2003/07/16 19:44:59  spr
 * Move petal from bloom to ivy.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.petal;


import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.*;




public class PetalUndoSupport extends UndoableEditSupport implements UndoableEditListener
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private int		 undoredo_count;
private PetalUndoManager undo_manager;

private static PetalUndoSupport current_support = null;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private PetalUndoSupport()
{
   undoredo_count = 0;
   undo_manager = null;
   addUndoableEditListener(this);
}



public static PetalUndoSupport getSupport()
{
   if (current_support == null) {
      current_support = new PetalUndoSupport();
    }

   return current_support;
}



/********************************************************************************/
/*										*/
/*	Methods for handling multiple undo streams				*/
/*										*/
/********************************************************************************/

public void setManager(PetalUndoManager pm)		{ undo_manager = pm; }

public PetalUndoManager getManager()			{ return undo_manager; }


@Override public void undoableEditHappened(UndoableEditEvent e)
{
   if (undo_manager != null) {
      undo_manager.addEdit(e.getEdit());
      undo_manager.handleCommandEnd();
    }
}


/********************************************************************************/
/*										*/
/*	Basic command implementations using current undo stream 		*/
/*										*/
/********************************************************************************/

public void undo()
{
   if (undo_manager != null) undo_manager.undo();
}



public void redo()
{
   if (undo_manager != null) undo_manager.redo();
}



public String getUndoCommand()
{
   if (undo_manager != null && undo_manager.canUndo()) return undo_manager.getUndoPresentationName();

   return null;
}



public String getRedoCommand()
{
   if (undo_manager != null && undo_manager.canRedo()) return undo_manager.getRedoPresentationName();

   return null;
}



public UndoableEdit getLastEdit()
{
   if (undo_manager == null) return null;

   return undo_manager.getLastEdit();
}



public boolean replaceLastEdit(UndoableEdit e)
{
   if (undo_manager == null) return false;

   return undo_manager.replaceLastEdit(e);
}



/********************************************************************************/
/*										*/
/*	Methods to handle undo/redo situations					*/
/*										*/
/********************************************************************************/

public void beginUndo() 		{ blockCommands(); }

public void beginRedo() 		{ blockCommands(); }

public void endUndo()			{ unblockCommands(); }

public void endRedo()			{ unblockCommands(); }


public void blockCommands()		{ undoredo_count++; }

public void unblockCommands()		{ if (undoredo_count > 0) --undoredo_count; }




/********************************************************************************/
/*										*/
/*	Redefinition of top-level methods					*/
/*										*/
/********************************************************************************/

@Override public synchronized void beginUpdate()
{
   if (undoredo_count <= 0) super.beginUpdate();
}



@Override public synchronized void endUpdate() 		{ endUpdate(null); }


public void endUpdate(String name)
{
   if (name != null) postEdit(new NameCommand(name));

   if (undoredo_count <= 0) super.endUpdate();
}



@Override public synchronized void postEdit(UndoableEdit e)
{
   if (undoredo_count <= 0) {
      super.postEdit(e);
    }
}



/********************************************************************************/
/*										*/
/*	Local class for dummy command for naming purposes			*/
/*										*/
/********************************************************************************/

private static class NameCommand extends AbstractUndoableEdit {

   private String command_name;
   private static final long serialVersionUID = 1;

   NameCommand(String nm) {
      command_name = nm;
    }

   @Override public String getPresentationName()		{ return command_name; }

}	// end of subclass NameCommand




}	// end of class PetalUndoSupport




/* end of PetalUndoSupport.java */
