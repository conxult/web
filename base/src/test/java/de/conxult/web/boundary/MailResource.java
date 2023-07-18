/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.boundary;

import de.conxult.web.util.CredentialUtil;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.io.File;

@Path("/mail")
@Dependent
public class MailResource {

  @Inject
  Mailer mailer;

  Template greeting;

  @Inject
  CredentialUtil credentialUtil;

  @GET
  @Blocking
  public void sendEmail() {
    mailer.send(
      Mail.withText("test@hogi.de",
        "Ahoy from Quarkus",
        "A simple email sent from a Quarkus application."
      )
    );
  }

  @GET
  @Path("/html")
  public void sendHtml() {
    String body = "<strong>Hello!</strong>" + "\n"
      + "<p>Here is an image for you: <img src=\"cid:my-image@quarkus.io\"/></p>"
      + "<p>Regards</p>";
    mailer.send(Mail.withHtml("test@hogi.de", "An email in HTML", body)
      .addInlineAttachment("100_2534.jpg",
        new File("100_2534.jpg"),
        "image/jpg", "<my-image@quarkus.io>"));
  }

  @GET
  @Path("/greeting")
  public void sendGreeting(
    @QueryParam("language") String language
  ) {
    mailer.send(Mail.withText("test@hogi.de",
        "Ahoy from Quarkus",
        greeting
          .data("language", language == null ? "en" : language)
          .data("name", "Hogi")
          .data("pin", credentialUtil.createRandomPin())
          .render()
      )
    );

  }

}
