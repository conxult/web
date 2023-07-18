/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.boundary;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;

/**
 *
 * @author joerg
 */
public interface WebRpcPrincipalBoundary {

    HttpHeaders getHttpHeaders();

    SecurityContext getSecurityContext();

}
