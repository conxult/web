/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import de.conxult.web.CxWebConstants;

/**
 *
 * @author joerg
 */
public class WebControllerBase {

  @Inject
  @PersistenceUnit(CxWebConstants.SCHEMA)
  protected EntityManager em;

}
