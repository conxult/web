/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.web.entity.Pending;
import de.conxult.web.util.CredentialUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 *
 * @author joerg
 */
@RequestScoped
public class PendingController
  extends WebControllerBase {

  @Inject
  CredentialUtil credentialUtil;

  //  @Override
  public Pending createPending(UUID id, Enum state) {
    Pending newPending = new Pending()
      .setId(id)
      .setState(state)
      .setToken(UUID.randomUUID().toString())
      .setCode(credentialUtil.createRandomPin())
      .setValidUntil(OffsetDateTime.now().plusHours(1));
    em.persist(newPending);
    return newPending;
  }

//  @Override
  public Pending findPending(UUID pendingId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

//  @Override
  public Pending findPending(String token, String code) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

//  @Override
  public Pending updatePending(Pending pending, Enum state) {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }


}
