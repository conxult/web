/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.filter;

import de.conxult.web.WebConfiguration;
import de.conxult.web.domain.WebContext;
import de.conxult.web.domain.WebContext.Jwt;
import de.conxult.web.domain.WebContext.Request;
import de.conxult.web.domain.WebContext.User;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 *
 * @author joerg
 */
@Dependent
@Provider
public class WebContextFilter
   implements ContainerRequestFilter, ContainerResponseFilter {

    public static final UUID UUID_WEB = new UUID(0x0, 0x0443);

    @Inject
    WebConfiguration webConfiguration;

    @Inject
    JsonWebToken jsonWebToken;

    @Inject
    WebContext webContext;

    @Override
    public void filter(
        ContainerRequestContext containerRequestContext
    ) throws IOException {
        if (jsonWebToken != null) { // && jsonWebToken.getRawToken() != null) {
            fillJwt(webContext.getJwt(), containerRequestContext.getHeaders());
        } else {
            webContext.getUser().setId(UUID_WEB);
        }
        fillUser(webContext.getUser(), containerRequestContext.getHeaders());
        fillRequest(webContext.getRequest(), containerRequestContext);
    }

    @Override
    public void filter(
        ContainerRequestContext  containerRequestContext,
        ContainerResponseContext containerResponseContext
    ) throws IOException {
        webContext.clean();
    }

    void fillJwt(Jwt jwt, MultivaluedMap<String, String> headers) {
        jwt.setRawToken(jsonWebToken.getRawToken());
        jwt.setIssuer(jsonWebToken.getIssuer());
        var iat = jsonWebToken.claim(Claims.iat);
        if (iat.isPresent()) {
            var iatval = iat.get();
//            jwt.setIssuedAt(OffsetDateTimeUtil.of(iat.get()jsonWebToken.getIssuedAtTime() * 1000));
        }
        var exp = jsonWebToken.claim(Claims.exp);
        if (exp.isPresent()) {
            var expval = iat.get();
//        jwt.setExpirationTime(OffsetDateTimeUtil.of(jsonWebToken.getExpirationTime() * 1000));
        }
        jwt.setAudience(jsonWebToken.getAudience());
        jwt.setSubject(jsonWebToken.getSubject());
        jwt.setName(jsonWebToken.getName());
        jwt.setGroups(jsonWebToken.getGroups());
        if (jsonWebToken.getClaimNames() != null) {
            for (String claimName : jsonWebToken.getClaimNames()) {
                jwt.getClaims().put(claimName, getClaim(claimName));
            }
        }
    }

    void fillUser(User user, MultivaluedMap<String, String> headers) {
        try {
            user.setId(UUID.fromString(jsonWebToken.getName()));
        } catch (Exception any) {
        }
        try {
            user.setTenantId(UUID.fromString(getClaim("tenant_id")));
        } catch (Exception any) {
        }
        try {
            String headerLocale = headers.getFirst("X-Locale");
            if (headerLocale != null) {
                String[] localeParts = headerLocale.concat("_").split("_");
                user.setLocale(new Locale(localeParts[0], localeParts[1]));
            }
        } catch (Exception any) {
        }
        user.setUserName(getClaim(Claims.nickname.name()));
        user.setFamilyName(getClaim(Claims.family_name.name()));
        user.setSurName(getClaim("sur_name"));
    }

    <T> T getClaim(String claimName) {
        return (T) jsonWebToken.claim(claimName).orElse(null);
    }

    void fillRequest(Request request, ContainerRequestContext requestContext) {
        request.setPaths(requestContext.getUriInfo());
        request.setMethod(requestContext.getMethod());
        request.setLocale(requestContext.getLanguage());
        requestContext.getMethod();
        request.setLanguage(webConfiguration.getLanguages());
    }
}
