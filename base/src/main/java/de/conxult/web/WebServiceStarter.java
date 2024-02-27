/*
 * Copyright by https://conxult.de
 */
package de.conxult.web;

import de.conxult.log.Log;
import de.conxult.util.RSAKeyUtil;
import de.conxult.web.config.RSAKeyPairJwtConfiguration;
import static de.conxult.web.config.RSAKeyPairJwtConfiguration.ISSUER;
import static de.conxult.web.config.RSAKeyPairJwtConfiguration.PUBLIC_KEY;
import static de.conxult.web.config.RSAKeyPairJwtConfiguration.SIGN_KEY;
import static de.conxult.web.config.RSAKeyPairJwtConfiguration.SUBJECT;
import de.conxult.web.control.KeyPairController;
import io.quarkus.flyway.FlywayDataSource;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.flywaydb.core.Flyway;

/**
 *
 * @author joerg
 */
@ApplicationScoped
public class WebServiceStarter {

    @Inject
    Log log;

    @Inject
    @FlywayDataSource(WebConfiguration.SCHEMA)
    Flyway flyway;

    @Inject
    KeyPairController keyPairController;

    @Inject
    WebConfiguration webConfiguration;

    @Inject
    RSAKeyUtil rsaKeyUtil;

    @Inject
    RSAKeyPairJwtConfiguration rsaJwtConfig;

    public void startupService(@Observes StartupEvent startupEvent) {
        startupPersistence();
        startupJwt();
    }

    void startupPersistence() {
        var placeholders = flyway.getConfiguration().getPlaceholders();
        placeholders.put("schema", WebConfiguration.SCHEMA);
        flyway.migrate();

        log.info("startupService {0} in {1}", getClass().getSimpleName(), LaunchMode.current());
    }

    void startupJwt() {
        var jwtKeyPair = keyPairController.findKeyPair("jwt", "jwt");
        rsaJwtConfig.set(SUBJECT, webConfiguration.getJwtSubject());
        rsaJwtConfig.set(ISSUER, webConfiguration.getJwtIssuer());
        rsaJwtConfig.set(PUBLIC_KEY, jwtKeyPair.getPublicKey());
        rsaJwtConfig.set(SIGN_KEY, jwtKeyPair.getPrivateKey());
    }

}
