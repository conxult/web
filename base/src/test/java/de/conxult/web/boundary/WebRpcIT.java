/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.conxult.web.boundary.WebApplication;
import io.quarkus.mailer.MockMailbox;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;

public class WebRpcIT {

    String webRpcPath;

    @Inject
    protected MockMailbox mailbox;

    public WebRpcIT(String webRpcPath) {
        this.webRpcPath = webRpcPath;
    }
    
    @BeforeEach
    void init() {
        mailbox.clear();
    }

    protected ValidatableResponse post(String method, Object value) {
        return given().when()
            .contentType(ContentType.JSON)
            .body(value)
            .post(String.join("/", WebApplication.PATH, webRpcPath, method))
            .then();
    }

    protected String prettyPrint(Object value) throws Exception {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }
}