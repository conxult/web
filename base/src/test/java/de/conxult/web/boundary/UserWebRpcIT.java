/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.boundary;

import de.conxult.web.domain.UserSignupRequest;
import de.conxult.web.domain.UserSignupResponse;
import io.quarkus.mailer.Mail;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class UserWebRpcIT
    extends WebRpcIT {

    public static final String TO = "willi@butz.de";

    public UserWebRpcIT() {
        super(WebApplication.User.PATH);
    }

    @Test
    public void shouldSignup() throws Exception {
        // call a REST endpoint that sends email
        var response = post("signup", new UserSignupRequest()
            .setEmail(TO)
            .setNickName("hogiko")
            .setFamilyName("Butz")
            .setSurName("Willi")
            .setPassword("WillisPasswort")
            .setAcceptLicense(true))
            .statusCode(200)
            .extract().as(UserSignupResponse.class);

        // verify that it was senta
        List<Mail> sent = mailbox.getMailsSentTo(TO);
        assertEquals(1, sent.size());
//        Mail actual = sent.get(0);
//        System.out.println("actual:\n" + prettyPrint(actual));
    }

}