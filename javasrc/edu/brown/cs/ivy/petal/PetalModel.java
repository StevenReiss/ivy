/********************************************************************************/
/*										*/
/*		PetalModel.java 						*/
/*										*/
/*	Interface defining the graph model used by Petal			*/
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



import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;




public interface PetalModel extends PetalConstants
{


void deselectAll();
void deselect(Object o);

void select(Object o);
void selectInBox(Rectangle r);
void selectAll();

PetalNode [] getSelectedNodes();
PetalArc [] getSelectedArcs();
boolean isSelected(PetalNode n);
boolean isSelected(PetalArc a);
int getNumSelections();


int	ARC_MODE_START = 0;
int	ARC_MODE_MIDDLE = 1;
int	ARC_MODE_END = 2;

boolean handleArcEndPoint(PetalNode pn,Point p,int mode,MouseEvent evt);
void handleMouseOver(PetalNode pn,PetalArc pa,Point nodept);

boolean handlePopupRequest(PetalNode pn,PetalArc pa,MouseEvent evt);

Object getCopyObject(PetalArc pa);
Object getCopyObject(PetalNode pn);
PetalNode addPasteNode(Object o,boolean dofg);
PetalNode addPasteNode(Object o,PetalClipSet pcs,boolean dofg);
PetalArc addPasteArc(Object o,PetalClipSet pcs,boolean dofg);

PetalArc [] getArcsFromNode(PetalNode n);
PetalArc [] getArcsToNode(PetalNode n);

void addModelListener(PetalModelListener l);
void removeModelListener(PetalModelListener l);
void fireModelUpdated();


//
// The following methods are only defined in PetalModelDefault
// and are not defined in PetalModelBase
//

PetalNode [] getNodes();
PetalArc [] getArcs();

void createArc(PetalNode frm,PetalNode to);

boolean dropNode(Object o,Point p,PetalNode pn,PetalArc pa);

void removeArc(PetalArc pa);
void removeNode(PetalNode pn);


}	// end of interface PetalModel




/* end of PetalModel.java */
