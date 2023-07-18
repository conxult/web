/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.context;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 *
 * @author joerg
 */
@Dependent
@Provider
public class RequestContextFilter
   implements ContainerRequestFilter {

    @Inject
    JsonWebToken jwt;

    @Override
    public void filter(ContainerRequestContext crc) throws IOException {
        String userName = jwt.getName();
        if (userName != null) {
            try {
                UUID userId = UUID.fromString(userName);
                RequestContext.setValue(RequestContextKey.CURRENT_USER, userId);
            } catch (IllegalArgumentException illegalArgumentException) {


            }
        }
    }

}
