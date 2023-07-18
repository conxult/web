package de.conxult.web.util;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Random;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TokenUtil {

  @ConfigProperty(name = "mp.jwt.verify.issuer")
  String issuer = "issuer";

  @ConfigProperty(name = "mp.jwt.subject")
  String subject = "subject";

  @ConfigProperty(name = "mp.jwt.accessToken.ttl", defaultValue = "300") // 5m: 5*60
  Long ttlAccessToken = 5*60L;

  @ConfigProperty(name = "mp.jwt.refreshToken.ttl", defaultValue = "86400") // 1d: 24*60*60
  Long ttlRefreshToken = 24*60*60L;

  Random randomGenerator = new Random(System.nanoTime());

  public TokenBuilder createAccessTokenBuilder() {
    return new TokenBuilder(issuer, ttlAccessToken)
      .subject(subject)
    ;
  }

  public TokenBuilder createRefreshTokenBuilder() {
    return new TokenBuilder(issuer, ttlRefreshToken)
      .subject(subject)
    ;
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


}
