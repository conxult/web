/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.boundary;

import de.conxult.web.domain.WebContext;
import de.conxult.web.entity.Role;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 *
 * @author joerg
 */
@RequestScoped
@Path("users")
@Tag(name = "User Boundary", description = "")
public class InfoResource {

    @Inject
    WebContext webContext;

    @GET
    @Path("roles/all")
    @PermitAll
    public Map<String, Object> getInfosPermitAll() {
        return Map.of(
          "jwt"    , webContext.getJwt().toMap(),
          "user"   , webContext.getUser().toMap(),
          "request", webContext.getRequest().toMap());
    }

    @GET
    @Path("roles/user")
    @RolesAllowed(Role.USER)
    public Map<String, Object> getInfosUserRole() {
        return Map.of(
          "jwt"    , webContext.getJwt().toMap(),
          "user"   , webContext.getUser().toMap(),
          "request", webContext.getRequest().toMap());
    }

}
