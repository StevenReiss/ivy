/********************************************************************************/
/*										*/
/*		JcompMessage.java						*/
/*										*/
/*	Representation of a Error message					*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2007, Brown University, Providence, RI.				 *
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


package edu.brown.cs.ivy.jcomp;


/**
 *	This class represents an error message from either
 *	parsing or semantic analysis.  Messages for a particular
 *	file can be obtained using the getMessages method on
 *	the semantic object returned for the file.
 **/

public class JcompMessage implements JcompConstants {


private JcompSource		for_file;
private JcompMessageSeverity	message_severity;
private int			message_id;
private String			message_text;
private int			line_number;
private int			start_offset;
private int			end_offset;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JcompMessage(JcompSource rf,JcompMessageSeverity lvl,int id,String txt,
      int line,int start,int end)
{
   for_file = rf;
   message_severity = lvl;
   message_id = id;
   message_text = txt;
   line_number = line;
   start_offset = start;
   end_offset = end;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

/**
 *	Return the problem id (from eclipse) associated with this message.
 **/

public int getMessageId()		{ return message_id; }


/**
 *	Get the source file for this message
 **/

public JcompSource getSource()		{ return for_file; }


/**
 *	Get the line number where the error occurs.
 **/

public int getLineNumber()		{ return line_number; }


/**
 *	Get the file offset for the start of the error.
 **/

public int getStartOffset()		{ return start_offset; }


/**
 *	Get the file offset for the end of the error
 **/

public int getEndOffset()		{ return end_offset; }


/**
 *	Get the severity of the error
 **/

public JcompMessageSeverity getSeverity()    { return message_severity; }


/**
 *	Get the error message itself.
 **/

public String getText() 		{ return message_text; }




}	// end of class JcompMessage



/* end of JcompMessage.java */
