package de.conxult.web.util;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Arrays;
import org.eclipse.microprofile.jwt.Claims;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author joerg
 */
@QuarkusTest
public class CredentialUtilTest {

    @Inject
    CredentialUtil testee;

    @Test
    public void shouldGenerateHash() throws Exception {
        String hash = testee.createHash("MyPassword!");
        assertNotNull(hash);
        assertEquals(4, hash.split("\\$").length);
    }

    @Test
    public void shouldValidateHash() throws Exception {
        String hash = testee.createHash("MyPassword!");
        assertTrue(testee.validateHash(hash, "MyPassword!"));
    }

    @Test
    public void shouldNotValidateWrongPassword() throws Exception {
        String hash = testee.createHash("MyPassword!");
        assertFalse(testee.validateHash(hash, "WrongPassword"));
    }

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
