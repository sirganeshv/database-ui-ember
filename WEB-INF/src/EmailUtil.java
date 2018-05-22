import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import javax.mail.PasswordAuthentication;

public class EmailUtil {
	public void sendEmail(String receiverMailID,String filename) {
		//String receiverMailID = "ganeshtbk@gmail.com";
		String subject = "Event Log details";
		String msg ="Event log details attached as pdf....";
		final String FROM ="ganeshtbk@gmail.com";
		final String PASSWORD ="helloworld";


		Properties props = new Properties();  
		props.setProperty("mail.transport.protocol", "smtp");     
		props.setProperty("mail.host", "smtp.gmail.com");  
		props.put("mail.smtp.auth", "true");  
		props.put("mail.smtp.port", "465");  
		//props.put("mail.debug", "true");  
		props.put("mail.smtp.socketFactory.port", "465");  
		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");  
		props.put("mail.smtp.socketFactory.fallback", "false");  
		
		Session session = Session.getDefaultInstance(props,  
		new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {  
				return new PasswordAuthentication(FROM,PASSWORD);  
		}  
		});  

		try {
			Transport transport = session.getTransport();  
			InternetAddress addressFrom = new InternetAddress(FROM);  

			MimeMessage message = new MimeMessage(session);  
			message.setSender(addressFrom);  
			message.setSubject(subject);  
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverMailID));  
			
			BodyPart messageBodyPart1 = new MimeBodyPart();     
            messageBodyPart1.setText("Event Logs attached");          

            MimeBodyPart messageBodyPart2 = new MimeBodyPart();      
            //String filename = "eventDetails.pdf";//change accordingly     
            DataSource source = new FileDataSource(filename);    
            messageBodyPart2.setDataHandler(new DataHandler(source));    
            messageBodyPart2.setFileName("EventLogs.pdf");             

            Multipart multipart = new MimeMultipart();    
            multipart.addBodyPart(messageBodyPart1);     
            multipart.addBodyPart(messageBodyPart2);      

            message.setContent(multipart );
			
			transport.connect();  
			Transport.send(message);  
			transport.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}