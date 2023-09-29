package de.conxult.web.util;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Arrays;
import org.eclipse.microprofile.jwt.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author joerg
 */
@QuarkusTest
public class TokenUtilTest {

    @Inject
    TokenUtil testee;

    @Test
    public void shouldGenerateToken1() throws Exception {
        String token = testee.createAccessTokenBuilder().build()
            .getToken();
        System.out.println(token);
        Assertions.assertEquals(3, token.split("\\.").length);
    }

    @Test
    public void shouldGenerateToken2() throws Exception {
        String token = testee.createAccessTokenBuilder()
            .upn("unique-nickname")
            .claim(Claims.email, "jdoe@miome.app")
            .claim(Claims.given_name, "Jane")
            .claim(Claims.family_name, "Doe")
            .claim(Claims.full_name, "Jane Doe")
            .claim("projects", Arrays.asList("a", "b", "c"))
            .groups("User", "Admin", "#refresh")
            .build().getToken();
        System.out.println(token);
        Assertions.assertEquals(3, token.split("\\.").length);
    }
}
