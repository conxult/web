package de.conxult.web.util;

import io.quarkus.arc.Unremovable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

  Provider        provider = new WildFlyElytronPasswordProvider();
  PasswordFactory passwordFactory;
  SecureRandom    random;

  @ConfigProperty(name = "quarkus.security.bcrypt.salt.iterationCount", defaultValue = "10")
  int             iterationCount = 10;

  @PostConstruct
  public void onPostConstruct() throws Exception {
    provider = new WildFlyElytronPasswordProvider();
    passwordFactory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, provider);
    random = new SecureRandom();
  }

  public String createHash(String credential) {
    byte[] salt = new byte[BCryptPassword.BCRYPT_SALT_SIZE];
    random.nextBytes(salt);

    return createHash(iterationCount, salt, credential);
  }

  public boolean validateHash(String hash, String credential) {
    try {
      PasswordFactory passwordFactory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, provider);
      Password userPasswordDecoded = ModularCrypt.decode(hash);
      Password userPasswordRestored = passwordFactory.translate(userPasswordDecoded);
      return passwordFactory.verify(userPasswordRestored, credential.toCharArray());

    } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
      return false;
    }

  }

  public String createRandomPin() {
    return String.format("%06d", random.nextInt(999999));
  }

  public String obfuscate(String someSecret) {
    return obfuscate(someSecret, 3, 3);
  }

  public String obfuscate(String someSecret, int head, int tail) {
    if (someSecret == null || someSecret.isEmpty()) {
      return someSecret;
    }

    if (someSecret.length() >= head + tail) {
      return someSecret.substring(0, head)
        + someSecret.substring(head, someSecret.length() - tail).replaceAll(".", "*")
        + someSecret.substring(someSecret.length() - tail);
    }

    return someSecret.replaceAll(".", "*");
  }

  String createHash(int iterationCount, byte[] salt, String credential) {

      IteratedSaltedPasswordAlgorithmSpec iteratedAlgorithmSpec = new IteratedSaltedPasswordAlgorithmSpec(iterationCount, salt);
      EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(credential.toCharArray(), iteratedAlgorithmSpec);

      try {
        BCryptPassword original = (BCryptPassword) passwordFactory.generatePassword(encryptableSpec);
        return ModularCrypt.encodeAsString(original);
      } catch (InvalidKeySpecException e) {
        return String.format("%s$%s$%s", "?", "?", UUID.randomUUID());
    }

  }
}
