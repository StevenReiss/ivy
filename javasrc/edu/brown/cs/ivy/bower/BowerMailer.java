/********************************************************************************/
/*                                                                              */
/*              BowerMailer.java                                                */
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



package edu.brown.cs.ivy.bower;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.brown.cs.ivy.file.IvyLog;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;


public class BowerMailer implements BowerConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String mail_to;
private String subject_text;
private String body_text;
private String reply_to;
private List<File> attachment_files;
private String from_name;
private String from_user;
private String from_password;
private String smtp_host;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BowerMailer(String to,String subj,String body) 
{
   mail_to = to;
   subject_text = subj;
   body_text = body;
   reply_to = null;
   from_name = null;
   from_user = null; 
   from_password = null;
   smtp_host = "smtp.gmail.com";
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public void setToAddress(String to)                     { mail_to = to; }
public void setSubject(String s)                        { subject_text = s; }
public void setReplyTo(String s)                        { reply_to = s; }
public void setSender(String from,String user,String pwd) 
{
   from_name = from;
   from_user = user;
   from_password = pwd;
}

public void setSmtpHost(String host)                    { smtp_host = host; }

public void addBodyText(String t) 
{
   if (body_text == null) body_text = t;
   else {
      if (!body_text.endsWith("\n")) body_text += "\n";
      body_text += t;
    }
}

public void addAttachment(File f) 
{
   if (f == null) return;
   if (attachment_files == null) attachment_files = new ArrayList<>();
   attachment_files.add(f);
}

public void addAttachments(List<File> lf)
{
   if (lf == null) return;
   if (attachment_files == null) attachment_files = new ArrayList<>();
   attachment_files.addAll(lf);
}



/********************************************************************************/
/*                                                                              */
/*      Send the email                                                          */
/*                                                                              */
/********************************************************************************/

public boolean send() 
{
   // Sender's email ID needs to be mentioned
   
   if (from_name == null) return false;
   
   // Assuming you are sending email through relay.jangosmtp.net
   
   Properties props = new Properties();
   props.put("mail.smtp.auth", "true");
   props.put("mail.smtp.starttls.enable", "true");
   props.put("mail.smtp.host", smtp_host);
   props.put("mail.smtp.port", "587");
   
   // Get the Session object.
   Session session = Session.getInstance(props,
         new jakarta.mail.Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
         return new PasswordAuthentication(from_user, from_password);
       }
    });
   
   try {
      // Create a default MimeMessage object.
      Message message = new MimeMessage(session);
      
      // Set From: header field of the header.
      message.setFrom(new InternetAddress(from_name));
      
      // Set To: header field of the header.
      message.setRecipients(Message.RecipientType.TO,
            InternetAddress.parse(mail_to));
      
      // Set Subject: header field
      message.setSubject(subject_text);
      
      if (reply_to != null) {
         Address [] rply = new Address[] { new InternetAddress(reply_to) };
         message.setReplyTo(rply);
       }
      
      // Now set the actual message
      if (attachment_files == null || attachment_files.size() == 0) {
         message.setText(body_text);
       }
      else {
         message.setText(body_text);
         MimeMultipart mutlipart = new MimeMultipart();
         MimeBodyPart bp = new MimeBodyPart();
         bp.setText(body_text);
         mutlipart.addBodyPart(bp);
         for (File f : attachment_files) {
            try {
               MimeBodyPart fpt = new MimeBodyPart();
               fpt.attachFile(f);
               mutlipart.addBodyPart(fpt);
             }
            catch (IOException e) {
               IvyLog.logE("BOWER","Problem attaching file " + f + " to mail message");
             }
            // bp = new MimeBodyPart();
            // DataSource fds = new FileDataSource(f);
            // bp.setDataHandler(new DataHandler(fds));
            // mutlipart.addBodyPart(bp);
          }
         message.setContent(mutlipart);
       }
      
      // Send message
      Transport.send(message);
      
      return true;
    }
   catch (MessagingException e) {
      IvyLog.logE("BOWER","Problem sending mail",e);
    }
   
   return false;
}



}       // end of class BowerMailer




/* end of BowerMailer.java */

