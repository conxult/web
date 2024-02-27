/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.web.entity.Credential;
import de.conxult.web.util.CredentialUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/**
 *
 * @author joerg
 */
@RequestScoped
public class CredentialController
    extends WebController {

    @Inject
    CredentialUtil credentialUtil;

    public Credential findCredential(UUID userId) {
        return em.createQuery("""
            SELECT c
            FROM Credential c
            WHERE c.id = :id
            """, Credential.class)
            .setParameter("id", userId)
            .setMaxResults(1)
            .getResultStream()
            .findFirst().orElse(null);
    }

}
