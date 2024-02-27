package de.conxult.web.util;

import de.conxult.web.WebConfiguration;
import io.quarkus.arc.Unremovable;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;
import org.wildfly.security.password.util.ModularCrypt;

@ApplicationScoped
@Unremovable
public class CredentialUtil {

    @Inject
    WebConfiguration webConfiguration;

    Provider        provider = new WildFlyElytronPasswordProvider();
    PasswordFactory passwordFactory;
    SecureRandom    random;


    @PostConstruct
    public void onPostConstruct() throws Exception {
        provider = new WildFlyElytronPasswordProvider();
        passwordFactory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, provider);
        random = new SecureRandom();
    }

    public String createHash(String password) {
        byte[] salt = new byte[BCryptPassword.BCRYPT_SALT_SIZE];
        random.nextBytes(salt);

        return createHash(webConfiguration.getSaltIterationCount(), salt, password);
    }

    public boolean validateHash(String hash, String password) {
        try {
            PasswordFactory passwordFactory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, provider);
            Password userPasswordDecoded = ModularCrypt.decode(hash);
            Password userPasswordRestored = passwordFactory.translate(userPasswordDecoded);
            return passwordFactory.verify(userPasswordRestored, password.toCharArray());

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            return false;
        }
  }

    public String createRandomPin() {
        return String.format(webConfiguration.getPinFormat(), random.nextInt(webConfiguration.getPinMax()));
    }

    public String obfuscate(String someSecret) {
        return obfuscate(someSecret, 3, 3);
    }

    public String obfuscate(String someSecret, int head, int tail) {
        if (someSecret == null || someSecret.isEmpty()) {
            return someSecret;
        }

        if (someSecret.length() >= head + tail) {
            return
                someSecret.substring(0, head) +
                someSecret.substring(head, someSecret.length() - tail).replaceAll(".", "*") +
                someSecret.substring(someSecret.length() - tail);
        }

        return someSecret.replaceAll(".", "*");
    }

   public TokenBuilder createAccessTokenBuilder() {
        return new TokenBuilder(webConfiguration.getJwtIssuer(), webConfiguration.getJwtAccessTokenTtl())
            .subject(webConfiguration.getJwtSubject());
    }

    public TokenBuilder createRefreshTokenBuilder() {
        return new TokenBuilder(webConfiguration.getJwtIssuer(), webConfiguration.getJwtRefreshTokenTtl())
            .subject(webConfiguration.getJwtSubject());
    }

    public Object getTokenValue(JsonObject jsonObject, String path) {
        String[] parts = path.split("\\.");
        for (int i = 0; (i < parts.length); i++) {
            Object value = jsonObject.getValue(parts[i]);
            if (value instanceof JsonObject jo) {
                jsonObject = jo;
            } else {
                return value;
            }
        }
        return jsonObject;
    }

    String createHash(int iterationCount, byte[] salt, String password) {

        IteratedSaltedPasswordAlgorithmSpec iteratedAlgorithmSpec = new IteratedSaltedPasswordAlgorithmSpec(iterationCount, salt);
        EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(password.toCharArray(), iteratedAlgorithmSpec);

        try {
            BCryptPassword original = (BCryptPassword) passwordFactory.generatePassword(encryptableSpec);
            return ModularCrypt.encodeAsString(original);
        } catch (InvalidKeySpecException e) {
            return String.format("%s$%s$%s", "?", "?", UUID.randomUUID());
        }

    }
}
