package at.htl.boundary;


import at.htl.control.CheckpointRepository;
import at.htl.entity.Photobook;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.text.*;
import com.itextpdf.text.api.Spaceable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * The PhotobookResource boundary handles the request for creating the photobook.
 */
@Path("/photobook")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class PhotobookResource {
    @Inject
    CheckpointRepository checkpointRepository;
    final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    /**
     * This method creates the pdf document of the photo's shot by the user and sends it to the desired
     * email location of the user. It is called when the user finished his route.
     * @param pb The photobook that contains the target email location and the photo's taken by the user.
     * @return Returns a response with the document attached to it.
     */
    @POST
    public Response createPhotobook(Photobook pb){
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("JungleScoutPhotobook.pdf"));
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }
        Font font = FontFactory.getFont(FontFactory.COURIER, 20, BaseColor.BLACK);
        font.setStyle(Font.BOLD);
        // create a buffered image

        document.open();

        document.addTitle("Your Photobook!");
        try {
            document.add(new Chunk(""));
            for (var item: pb.photos) {
                //BufferedImage image = null;
                String rectifiedString = item.getImage().replace("\n","");
                byte[] imageBytes = Base64.getUrlDecoder().decode(rectifiedString);
                ImageData imgData = ImageDataFactory.createJpeg(imageBytes);
                Image img = Image.getInstance(imgData.getData());
                img.setAlignment(Element.ALIGN_CENTER);
                Paragraph paragraph = new Paragraph(checkpointRepository.findById(item.cp_id).getName(), font);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);
                document.add(new Chunk(""));
                document.add(img);
            }

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        document.close();

        // email ID of Recipient.
        String recipient = pb.email;
        // email ID of  Sender.
        String sender = "example_sender@example.invalid";
        String pw = "example_email_password";
        // using host as localhost
        String host = "localhost";

        // Getting system properties
        Properties properties = System.getProperties();
        // Setting up mail server
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.smtp.port", "465");
        properties.setProperty("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.debug", "true");
        properties.put("mail.store.protocol", "pop3");
        properties.put("mail.transport.protocol", "smtp");


        // creating session object to get properties
        Session session = Session.getDefaultInstance(properties, new Authenticator(){
            //Credentials of the Sender need to be Authenticated.
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, pw);
            }});

        try
        {
            // MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From Field: adding senders email to from field.
            message.setFrom(new InternetAddress(sender));
            // Set To Field: adding recipient's email to from field.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            // Set Subject: subject of the email
            message.setSubject("Your personal Photobook has arrived");

            // creating first MimeBodyPart object
            BodyPart messageBodyPart1 = new MimeBodyPart();
            messageBodyPart1.setText("Thank you for using our app. We hope you had a fantastic time through your journey.");

            // creating second MimeBodyPart object
            BodyPart messageBodyPart2 = new MimeBodyPart();
            String filename = "JunglePathPhotobook.pdf";
            DataSource source = new FileDataSource(filename);
            messageBodyPart2.setDataHandler(new DataHandler(source));
            messageBodyPart2.setFileName(filename);

            // creating MultiPart object
            Multipart multipartObject = new MimeMultipart();
            multipartObject.addBodyPart(messageBodyPart1);
            multipartObject.addBodyPart(messageBodyPart2);

            // set body of the email.
            message.setContent(multipartObject);
            // Send email.
            Transport.send(message);
            System.out.println("Mail successfully sent");
        }
        catch (MessagingException mex)
        {
            mex.printStackTrace();
        }
        return Response.ok(document).build();
    }
}
