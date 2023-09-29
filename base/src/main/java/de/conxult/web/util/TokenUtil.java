package de.conxult.web.util;

import de.conxult.web.WebConfiguration;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TokenUtil {

    @Inject
    WebConfiguration webConfiguration;

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

}
