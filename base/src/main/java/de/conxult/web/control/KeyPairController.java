/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.web.entity.KeyPair;
import jakarta.enterprise.context.Dependent;

/**
 *
 * @author joerg
 */
@Dependent
public class KeyPairController
    extends WebController {

    public KeyPair findKeyPair(String scope, String name) {
        return em.createQuery("select kp from KeyPair kp where scope = :scope and name = :name", KeyPair.class)
            .setParameter("scope", scope)
            .setParameter("name", name)
            .getResultList()
            .stream().findFirst().orElse(null);
    }

}
