/********************************************************************************/
/*										*/
/*		PetalUndoManager.java						*/
/*										*/
/*	Extension of UndoManager for use with Petal and beyond			*/
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


package edu.brown.cs.ivy.petal;


import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;




public class PetalUndoManager extends UndoManager
{




/********************************************************************************/
/*										*/
/*	Interface for handling end of command callbacks 			*/
/*										*/
/********************************************************************************/

public interface CommandHandler {

   void commandEndNotify();	// called after each command

};



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private transient PetalUndoSupport undo_support;
private transient CommandHandler   command_handler;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/


public PetalUndoManager()
{
   this(null);
}



public PetalUndoManager(CommandHandler hdlr)
{
   undo_support = PetalUndoSupport.getSupport();
   command_handler = hdlr;
   setLimit(1024);
}



/********************************************************************************/
/*										*/
/*	Redefine the top-level methods for undo/redo				*/
/*										*/
/********************************************************************************/

@Override public synchronized void redo()
{
   if (!canRedo()) return;

   undo_support.beginRedo();
   super.redo();
   undo_support.endRedo();
   handleCommandEnd();
}




@Override public void redoTo(UndoableEdit e)
{
   if (!canRedo()) return;

   undo_support.beginRedo();
   super.redoTo(e);
   undo_support.endRedo();
   handleCommandEnd();
}




@Override public synchronized void undo()
{
   if (!canUndo()) return;

   undo_support.beginUndo();
   super.undo();
   undo_support.endUndo();
   handleCommandEnd();
}




@Override public void undoTo(UndoableEdit e)
{
   if (!canUndo()) return;

   undo_support.beginUndo();
   super.undoTo(e);
   undo_support.endUndo();
   handleCommandEnd();
}




@Override public synchronized void undoOrRedo() {
   if (!canUndoOrRedo()) return;

   undo_support.beginUndo();
   super.undoOrRedo();
   undo_support.endUndo();
   handleCommandEnd();
}




/********************************************************************************/
/*										*/
/*	Other access methods							*/
/*										*/
/********************************************************************************/

UndoableEdit getLastEdit()			{ return lastEdit(); }



boolean replaceLastEdit(UndoableEdit e)
{
   int ln = edits.size();

   if (ln > 0) trimEdits(ln-1,ln-1);

   addEdit(e);

   return true;
}



/********************************************************************************/
/*										*/
/*	Callbacks for PetalUndoSupport to handle end of command 		*/
/*										*/
/********************************************************************************/

void handleCommandEnd()
{
   if (command_handler != null) command_handler.commandEndNotify();
}



}	// end of class PetalUndoManager




/* end of PetalUndoManager.java */
