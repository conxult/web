/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.web.entity.Some;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author joerg
 */

@Dependent
public class SomeController
  extends WebControllerBase {

  public List<Some> getAll() {
    return em.createQuery("select s from Some s").getResultList();
  }

  @Transactional
  public Some create(Some some) {
    em.persist(some);
    return some;
  }

  public Some find(@QueryParam("id") UUID id) {
    return em.find(Some.class, id);
  }

}
