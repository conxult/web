/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.util;

import com.password4j.Password;
import org.junit.jupiter.api.Test;

/**
 *
 * @author joerg
 */
public class PasswordTest {

    private static final int RUNS = 100;

    @Test
    public void shouldGenerateSCryptHash() throws Exception {
        long start = System.nanoTime();
        for (int i = 0; (i < RUNS); i++) {
            var hash = Password.hash("MyPassword!").addRandomSalt(12).addPepper("conxult").withScrypt();
        }
        long finished = System.nanoTime();
        System.out.println("took " + (finished - start) / 100_000_000);
    }

}
