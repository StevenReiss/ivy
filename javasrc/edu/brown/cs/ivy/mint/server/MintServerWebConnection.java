/********************************************************************************/
/*										*/
/*		MintServerWebConnection.java					*/
/*										*/
/*	Connection from MINT server to the web host				*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
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


/* RCS: $Header: /pro/spr_cvs/pro/ivy/javasrc/edu/brown/cs/ivy/mint/server/MintServerWebConnection.java,v 1.12 2018/12/17 14:09:00 spr Exp $ */

/*********************************************************************************
 *
 * $Log: MintServerWebConnection.java,v $
 * Revision 1.12  2018/12/17 14:09:00  spr
 * Fix up web message serving.
 *
 * Revision 1.11  2018/08/02 15:10:30  spr
 * Fix imports.
 *
 * Revision 1.10  2013/11/15 02:39:12  spr
 * Fix imports
 *
 * Revision 1.9  2012-01-12 01:26:28  spr
 * Formatting
 *
 * Revision 1.8  2011-05-27 19:32:45  spr
 * Change copyrights.
 *
 * Revision 1.7  2011-05-20 23:30:04  spr
 * Updates to avoid infinite message loops.
 *
 * Revision 1.6  2011-05-19 23:34:02  spr
 * Fix web connection reply ids.
 *
 * Revision 1.5  2011-05-18 23:33:00  spr
 * Fixes for mint web interface.
 *
 * Revision 1.4  2011-05-18 01:38:17  spr
 * Get the right names for items coming from the server.
 *
 * Revision 1.3  2011-05-18 01:02:04  spr
 * Changes to fix up web connection user id.
 *
 * Revision 1.2  2011-05-17 01:29:46  spr
 * Clean up debugging message.
 *
 * Revision 1.1  2011-05-17 01:05:07  spr
 * Mint server to support web-scale messages.
 *
 *
 ********************************************************************************/


package edu.brown.cs.ivy.mint.server;

import edu.brown.cs.ivy.exec.IvyExecQuery;

import javax.crypto.Cipher;
import javax.crypto.SecretKey; 
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.server.UID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.StringTokenizer;


class MintServerWebConnection implements MintServerConnection {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private MintServer	for_server;
private String		web_url;
private SecretKey	encode_key;
private WebReaderThread web_reader;

byte [] KEY_SALT = new byte [] {
   (byte)0xa9,(byte)0x9b,(byte)0xc8,(byte)0x32,
   (byte)0x56,(byte)0x35,(byte)0xe3,(byte)0x03
};

int KEY_COUNT = 19;

private static final boolean	debug_only = false;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MintServerWebConnection(MintServer svr,String url,String key)
{
   for_server = svr;

   encode_key = null;
   try {
      KeySpec ks = new PBEKeySpec(key.toCharArray(),KEY_SALT,KEY_COUNT);
      SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
      encode_key = kf.generateSecret(ks);
    }
   catch (Exception e) {
      System.err.println("MINT: Problem constructing key: " + e);
    }

   String m = encodeString(svr.getMintName());
   UID uid = new UID();
   String u = uid.toString() + "@" + IvyExecQuery.getHostName();
   try {
      MessageDigest mdi = MessageDigest.getInstance("MD5");
      mdi.update(u.getBytes());
      byte [] drslt = mdi.digest();
      long rslt = 0;
      for (int i = 0; i < drslt.length; ++i) {
	 int j = i % 8;
	 long x = (drslt[i] & 0xff);
	 rslt ^= (x << (j*8));
       }
      rslt &= 0x7fffffffffffffffL;
      u = Long.toString(rslt);
    }
   catch (NoSuchAlgorithmException e) {
      System.err.println("MINT: Problem creating unique id: " + e);
    }

   web_url = url + "?U=" + u + "&I=" + m;

   web_reader = new WebReaderThread();
}



/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

@Override public void start()
{
   web_reader.start();
}


@Override public void finish()
{
   web_reader.interrupt();

   String url = web_url + "&T=END";
   String hdr = "0 0 END\r\n";
   sendMessage(url,hdr,null);
}


@Override public void queueMessage(MintServerMessage msg)
{
   if (msg.getConnection() == this) return;

   byte [] code = encodeMessage(msg);
   String url = web_url + "&T=MSG&R=" + msg.getReplyId();
   int fgs = msg.getMessageFlags();
   url += "&F=" + fgs;

   String hdr = Integer.toString(code.length) + " " + msg.getReplyId() + " MSG\r\n";

   msg.addClient();

   sendMessage(url,hdr,code);
}


@Override public void queueReply(MintServerMessage msg,String rply)
{
   byte [] code = encodeBytes(rply);
   String typ;
   String hdr;
   if (code == null) {
      typ = "RPLYN";
      hdr = "0";
    }
   else {
      typ = "RPLY";
      hdr = Integer.toString(code.length);
    }
   String url = web_url + "&T=" + typ + "&R=" + msg.getLocalReplyId();
   hdr += " " + msg.getLocalReplyId() + " " + typ + "\r\n";

   sendMessage(url,hdr,code);
}



@Override public void queueDone(MintServerMessage msg)
{
   String url = web_url + "&T=RPLYD&R=" + msg.getLocalReplyId();
   String hdr = "0 " + msg.getLocalReplyId() + " RPLYD\r\n";

   sendMessage(url,hdr,null);
}



private void sendMessage(String url,String hdr,byte [] code)
{
   try {
      byte [] hb = hdr.getBytes("UTF-8");
      URL u = new URL(url);
      URLConnection uc = u.openConnection();
      uc.setUseCaches(false);
      uc.setDoOutput(true);
      uc.addRequestProperty("Content-Type","application/octet-stream");
      OutputStream ots = uc.getOutputStream();
      ots.write(hb);
      if (code != null) ots.write(code);
      ots.close();
      System.err.println("MINT: SEND TO " + url + " :: " + hdr);
      InputStream ins = uc.getInputStream();
      System.err.print("MINT: RESULT: ");
      for ( ; ; ) {
	 int ch = ins.read();
	 if (ch < 0) break;
	 System.err.print((char) ch);
       }
      System.err.println();
    }
   catch (Throwable t) {
      System.err.println("MINT: Problem sending web message: " + t);
      t.printStackTrace();
    }
}




/********************************************************************************/
/*										*/
/*	Encoding methods							*/
/*										*/
/********************************************************************************/

private byte [] encodeMessage(MintServerMessage msg)
{
   if (msg == null) return null;

   String t = msg.getText();
   if (t == null || t.length() == 0) return null;

   return encodeBytes(t);
}



private byte [] encodeBytes(String s)
{
   if (s == null) return null;

   byte [] tb = null;
   try {
      tb = s.getBytes("UTF-8");
      Cipher c = Cipher.getInstance(encode_key.getAlgorithm());
      AlgorithmParameterSpec ps = new PBEParameterSpec(KEY_SALT,KEY_COUNT);
      c.init(Cipher.ENCRYPT_MODE,encode_key,ps);
      tb = c.doFinal(tb);
    }
   catch (Exception e) {
      System.err.println("MINT: Problem encoding message: " + e);
    }

   return tb;
}



private String encodeString(String s)
{
   while (s.length() < 16) s += " ";

   byte [] b = encodeBytes(s);
   StringBuffer buf = new StringBuffer();
   for (int i = 0; i < b.length; ++i) {
      int x = b[i] & 0xff;
      int x1 = x & 0xf;
      int x2 = (x & 0xf0) >> 4;
      char c1 = (char)('a' + x1);
      char c2 = (char)('a' + x2);
      buf.append(c1);
      buf.append(c2);
    }

   return buf.toString();
}



private String decodeBytes(byte [] code)
{
   if (code == null) return null;

   try {
      Cipher c = Cipher.getInstance(encode_key.getAlgorithm());
      AlgorithmParameterSpec ps = new PBEParameterSpec(KEY_SALT,KEY_COUNT);
      c.init(Cipher.DECRYPT_MODE,encode_key,ps);
      code = c.doFinal(code);
    }
   catch (Exception e) {
      System.err.println("MINT: Problem decoding message: " + e);
    }

   return new String(code);
}



/********************************************************************************/
/*										*/
/*	Reader thread								*/
/*										*/
/********************************************************************************/

private void handleWebMessage(int rid,String typ,String msg,int fgs)
{
   System.err.println("MINT: WEB MESSAGE: " + rid + " " + typ + " " + fgs + " " + msg);

   // TODO: Need to handle MINT_REPLY_ALL by having remote send done and tracking that here

   if (debug_only) return;

   if (typ.equals("MSG") || typ.equals("MESSAGE")) {
      MintServerMessage smsg = new MintServerMessage(for_server,this,msg,rid,fgs);
      for_server.handleMessage(smsg);
    }
   else if (typ.equals("RPLY") || typ.equals("RPLYN") || typ.equals("REPLY")) {
      for_server.handleReply(rid,msg);
    }
}




private class WebReaderThread extends Thread {

   WebReaderThread() {
      super("MintWebReader");
    }

   @Override public void run() {
      System.err.println("STARTING READER");
      while (!isInterrupted()) {
	 String url = web_url + "&T=RECV";
	 try {
	    URL u = new URL(url);
	    System.err.println("MINT: WEB RECV " + u);
	    URLConnection uc = u.openConnection();
	    InputStream ins = uc.getInputStream();
	    for ( ; ; ) {
	       if (!readMessage(ins)) break;
	     }
	    ins.close();
	  }
	 catch (Exception e) {
	    System.err.println("MINT: problem reading web message: " + e);
	    e.printStackTrace();
	  }
       }
    }

   private boolean readMessage(InputStream ins) throws IOException {
      StringBuffer buf = new StringBuffer();
      for ( ; ; ) {
	 int ch = ins.read();
	 if (ch < 0) return false;
	 if (ch == '\r') continue;
	 if (ch == '\n') break;
	 buf.append((char) ch);
       }
      System.err.println("MINT: WEB HEADER " + buf.toString());
      StringTokenizer tok = new StringTokenizer(buf.toString());
      int len = Integer.parseInt(tok.nextToken());
      int rid = Integer.parseInt(tok.nextToken());
      String typ = tok.nextToken();
      int fgs = Integer.parseInt(tok.nextToken());
      if (typ.equals("END")) return false;
      byte [] code = null;
      if (len != 0) {
	 code = new byte[len];
	 int l = 0;
	 while (l < len) {
	    int ct = ins.read(code,l,len-l);
	    if (ct < 0) throw new IOException("Incomplete message");
	    l += ct;
	  }
       }
      String msg = decodeBytes(code);
      handleWebMessage(rid,typ,msg,fgs);
      return true;
    }

}	// end of inner class WebReaderThread



}	// end of class MintServerWebConnection




/* end of MintServerWebConnection.java */
