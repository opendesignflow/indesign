package org.odfi.indesign.core.module.email

import org.odfi.indesign.core.module.IndesignModule
import javax.mail.Session
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress
import javax.mail.Message
import javax.mail.Transport
import javax.mail.MessagingException
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication

object EmailModule extends IndesignModule {

  this.onStart {

  }

  def sendEmail(from: String, to: String, subject: String, content: String) = {

    var properties = System.getProperties();
    //properties.put("mail.debug", "true");
   // properties.put("mail.auth.debug", "true");

    properties.setProperty("mail.transport.protocol", "smtp")
    properties.setProperty("mail.smtp.starttls.enable", "true")
    properties.setProperty("mail.smtp.starttls.required", "true")

    // Setup mail server

    properties.setProperty("mail.smtp.host", config.get.getString("smtp", ""))
    config.get.getInt("port", 0) match {
      case 0 =>

      case p =>
        properties.setProperty("mail.smtp.port", p.toString())
    }

    //properties.setProperty("mail.transport.host", config.get.getString("smtp", ""))
    //properties.setProperty("mail.smtp.host", config.get.getString("smtp", ""));

    config.get.getString("user", "") match {
      case "" =>

      case other =>
        properties.setProperty("mail.smtp.auth", "true")
        properties.setProperty("mail.user", other)
        properties.setProperty("mail.password", config.get.getString("password", ""))
    }

    // Get the default Session object.
    var session = Session.getDefaultInstance(properties, new Authenticator {
      override def getPasswordAuthentication(): PasswordAuthentication = {
        new PasswordAuthentication(config.get.getString("user", ""), config.get.getString("password", ""))
      }
    })

    try {
      // Create a default MimeMessage object.
      var message = new MimeMessage(session);

      // Set From: header field of the header.
      message.setFrom(new InternetAddress(from));

      // Set To: header field of the header.
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

      // Set Subject: header field
      message.setSubject(subject);

      // Now set the actual message
      message.setText(content);

      // Send message
      Transport.send(message);
      System.out.println("Sent message successfully....");

    } catch {
      case mex: MessagingException => mex.printStackTrace();
    }

  }

}