/*
 * Copyright by https://conxult.de
 */
package de.conxult.web;

import io.quarkus.flyway.FlywayDataSource;
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
public class CxWebServiceStarter {

  @Inject
  @FlywayDataSource(CxWebConstants.SCHEMA)
  Flyway flyway;

  public void startupService(@Observes StartupEvent startupEvent) {
    var placeholders = flyway.getConfiguration().getPlaceholders();
    placeholders.put("schema", CxWebConstants.SCHEMA);
    flyway.migrate();
  }


}
