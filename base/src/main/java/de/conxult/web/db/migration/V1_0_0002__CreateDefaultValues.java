/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.db.migration;

import de.conxult.pa.db.migration.CxPaBaseJavaMigration;
import de.conxult.util.RSAKeyUtil;
import de.conxult.web.WebConfiguration;
import de.conxult.web.entity.KeyPair;
import de.conxult.web.entity.Role;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author joerg
 */
@Dependent
@Unremovable
public class V1_0_0002__CreateDefaultValues
    extends CxPaBaseJavaMigration {

    @Inject
    RSAKeyUtil rsaKeyUtil;

    @Override
    public void migrate() {
        createRoles();
        createJwt();
    }

    void createRoles() {
        Map<String, Role> roleMap = new HashMap<>();
        for (String roleName : Role.ROLES) {
            Role role = new Role()
                .setCreatedBy(Role.UUID_ZERO)
                .setName(roleName);
            em().persist(role);
            roleMap.put(role.getName(), role);
        }
    }

    void createJwt() {
        var rsaKeyPair = rsaKeyUtil.generateKeyPair();
        var keyPair = new KeyPair()
                .setScope("jwt")
                .setName("jwt")
                .setType("rsa")
                .setCreatedBy(KeyPair.UUID_ZERO)
                .setPublicKey(rsaKeyUtil.encode(rsaKeyPair.getPublicKey()))
                .setPublicOwnerId(KeyPair.UUID_ZERO)
                .setPrivateKey(rsaKeyUtil.encode(rsaKeyPair.getPrivateKey()))
                .setPrivateOwnerId(KeyPair.UUID_ZERO);

        em().persist(keyPair);
    }

    EntityManager em() {
        return getEntityManager(WebConfiguration.SCHEMA);
    }
}


