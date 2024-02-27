/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.web.domain.MailRequest;
import de.conxult.web.util.TemplateCache;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
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

    public boolean sendMail(String template, String language, MailRequest mailRequest) {
        // get templates
        var subjectTemplate  = templateCache.getTemplate(language, "mails", template + ".subject");
        var textTemplate     = templateCache.getTemplate(language, "mails", template + ".text");
        var htmlTemplate     = templateCache.getTemplate(language, "mails", template + ".html");

        // feed data into templates
        for (Map.Entry<String, Object> entry : mailRequest.getData().entrySet()) {
            subjectTemplate  = subjectTemplate .data(entry.getKey(), entry.getValue());
            textTemplate     = textTemplate .data(entry.getKey(), entry.getValue());
            htmlTemplate     = htmlTemplate.data(entry.getKey(), entry.getValue());
        }

        var mail = new Mail()
            .setSubject(subjectTemplate.render())
            .setTo(mailRequest.getTo())
            .setCc(mailRequest.getCc())
            .setBcc(mailRequest.getBcc());

        mailer.send(mail
            .setText(textTemplate.render())
            .setHtml(htmlTemplate.render()));

        return true;
    }
}
