/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author joerg
 */

@QuarkusTest
public class KeyPairControllerIT {

    @Inject
    KeyPairController testee;

    @Test
    public void shouldGenerateKeyPair() throws Exception {
        var keyPair = testee.findKeyPair("jwt", "jwt");
        assertNotNull(keyPair);
    }
}
