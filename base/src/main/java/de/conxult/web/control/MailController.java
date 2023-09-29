/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.web.domain.MailRequest;
import de.conxult.web.util.TemplateCache;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;

/**
 *
 * @author joerg
 */
@ApplicationScoped
public class MailController
    extends WebController {

    @Inject
    TemplateCache templateCache;

    @Inject
    Mailer mailer;

    @Blocking
    public boolean sendMail(String template, String language, MailRequest mailRequest) {
        // get templates
        var txtTemplate = templateCache.getTemplate(language, MailController.class, template + ".txt");
        var htmlTemplate = templateCache.getTemplate(language, MailController.class, template + ".html");

        // feed data into templates
        for (Map.Entry<String, Object> entry : mailRequest.getData().entrySet()) {
            txtTemplate = txtTemplate.data(entry.getKey(), entry.getValue());
            htmlTemplate = htmlTemplate.data(entry.getKey(), entry.getValue());
        }

        txtTemplate = txtTemplate.data("signupConfirmUrls", webConfiguration.getSignupConfirmUrls());
        htmlTemplate = htmlTemplate.data("signupConfirmUrls", webConfiguration.getSignupConfirmUrls());

        var mail = new Mail()
            .setSubject(mailRequest.getSubject())
            .setTo(mailRequest.getTo())
            .setCc(mailRequest.getCc())
            .setBcc(mailRequest.getBcc());

        mailer.send(mail
            .setText(txtTemplate.render())
            .setHtml(htmlTemplate.render()));

        return true;
    }
}
