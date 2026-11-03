/********************************************************************************/
/*                                                                              */
/*              IvyIncludeReader.java                                           */
/*                                                                              */
/*      description of class                                                    */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/


package edu.brown.cs.ivy.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;


public class IvyIncludeReader extends Reader {


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

// Stack to manage nested file readers for recursive inclusion
private Deque<BufferedReader> reader_stack;

// Tracks the current line being processed out to the user
private String			    current_line;
private int			    current_line_index;
private boolean		    is_closed;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public IvyIncludeReader(String rootFilePath) throws IOException
{
   this(new FileReader(rootFilePath));
}


public IvyIncludeReader(File rootFile) throws IOException
{
   this(new FileReader(rootFile));
}


public IvyIncludeReader(Reader rdr) throws IOException
{
   reader_stack = new ArrayDeque<>();
   current_line = null;
   current_line_index = 0;
   is_closed = false;
   reader_stack.push(new BufferedReader(rdr));
}


/********************************************************************************/
/*                                                                              */
/*      Read method                                                             */
/*                                                                              */
/********************************************************************************/

@Override
public int read(char[] cbuf,int off,int len) throws IOException
{
   synchronized (lock) {
      ensureOpen();
      
      if (len == 0) {
	 return 0;
       }
      
      int charsRead = 0;
      
      while (charsRead < len) {
	 // If we don't have a current line to read characters from, fetch the next one
	 if (current_line == null) {
	    if (!advanceToNextValidLine()) {
	       // Entire stream is exhausted
	       return (charsRead == 0) ? -1 : charsRead;
             }
          }
         
	 // Copy characters from the current line buffer into the target buffer
	 int available = current_line.length() - current_line_index;
	 int toCopy = Math.min(len - charsRead, available);
         
	 current_line.getChars(current_line_index, 
               current_line_index + toCopy, 
               cbuf,
               off + charsRead);
         
	 charsRead += toCopy;
	 current_line_index += toCopy;
         
	 // If we completely consumed the current line, reset the buffer pointer
	 if (current_line_index >= current_line.length()) {
	    current_line = null;
	    current_line_index = 0;
          }
       }
      
      return charsRead;
    }
}

/**
 * Loops through open readers to find the next printable text line, 
 * handling file insertions and EOF popping along the way.
 */
private boolean advanceToNextValidLine() throws IOException
{
   while (!reader_stack.isEmpty()) {
      BufferedReader currentReader = reader_stack.peek();
      String rawLine = currentReader.readLine();
      
      // Case 1: End of file reached for the current reader
      if (rawLine == null) {
	 currentReader.close();
	 reader_stack.pop();
	 continue; // Move back to the parent file in the next loop iteration
       }
      
      // Case 2: Found an insertion directive
      if (rawLine.startsWith("~")) {
	 String includePath = rawLine.trim().substring(1).trim();
	 // Push the new file onto the stack to start reading it next
	 reader_stack.push(new BufferedReader(new FileReader(includePath)));
	 continue;
       }
      
      // Case 3: A standard line of text. Append a newline character to preserve line breaks
      current_line = rawLine + System.lineSeparator();
      current_line_index = 0;
      return true;
    }
   
   return false; // All readers popped, nothing left to read
}


private void ensureOpen() throws IOException
{
   if (is_closed) {
      throw new IOException("Stream closed");
    }
}


@Override
public void close() throws IOException
{
   synchronized (lock) {
      if (!is_closed) {
	 // Safely close all remaining open streams in the stack
	 while (!reader_stack.isEmpty()) {
	    try {
	       reader_stack.pop().close();
             }
	    catch (IOException ignored) { }
          }
	 is_closed = true;
	 current_line = null;
       }
    }
}


} // end of class IvyIncludeReader


/* end of IvyIncludeReader.java */

