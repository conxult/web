package de.conxult.web.util;

import de.conxult.web.domain.Token;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import org.eclipse.microprofile.jwt.Claims;

/**
 *
 * @author joerg
 */
public class TokenBuilder {

    JwtClaimsBuilder claimsBuilder;

    Token token = new Token();

    TokenBuilder(String issuer, long expiresIn) {
        long now = System.currentTimeMillis();
        token.setExpires(new Date(now + expiresIn * 1000));
        claimsBuilder = Jwt.issuer(issuer)
          .issuedAt(now / 1000)
          .expiresAt(now / 1000 + expiresIn);
    }

    public TokenBuilder issuer(String issuer) {
        claimsBuilder.issuer(issuer);
        return this;
    }

    public TokenBuilder subject(String subject) {
        claimsBuilder.subject(subject);
        return this;
    }

    public TokenBuilder upn(String upn) {
        claimsBuilder.upn(upn);
        return this;
    }

    public TokenBuilder claim(Claims claim, Object value) {
        return claim(claim.name(), value);
    }

    public TokenBuilder claim(String claim, Object value) {
        claimsBuilder.claim(claim, value);
        return this;
    }

    public TokenBuilder groups(String... groups) {
        claimsBuilder.groups(new HashSet<>(Arrays.asList(groups)));
        return this;
    }

    public Token build() {
        if (token.getToken() == null) {
            token.setToken(claimsBuilder.sign());
        }
        return token;
    }

}
