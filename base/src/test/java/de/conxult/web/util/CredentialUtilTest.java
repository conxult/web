package de.conxult.web.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 *
 * @author joerg
 */
//@QuarkusTest
public class CredentialUtilTest {

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

  @BeforeEach
  void initializeTestee() throws Exception {
    testee = new CredentialUtil();
    testee.onPostConstruct();
  }
}
