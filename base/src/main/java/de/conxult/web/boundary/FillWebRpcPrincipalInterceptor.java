/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.boundary;

import de.conxult.web.annotation.FillWebRcpPrincipal;
import de.conxult.web.domain.WebRpcPrincipal;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.time.OffsetDateTime;

/**
 *
 * @author joerg
 */
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@FillWebRcpPrincipal
public class FillWebRpcPrincipalInterceptor {

    @Inject
    WebRpcPrincipal webRcpPrincipal;

    @AroundInvoke
    protected Object aroundInvoke(InvocationContext ctx) throws Exception {

        webRcpPrincipal.put("now", OffsetDateTime.now());

        try {
            return ctx.proceed();
        } finally {
            webRcpPrincipal.clean();
        }

    }
}


