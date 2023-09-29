/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.web.entity.Pending;
import de.conxult.web.util.PasswordUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

/**
 *
 * @author joerg
 */
@RequestScoped
public class PendingController
    extends WebController {

    @Inject
    PasswordUtil passwordUtil;

    @Transactional
    public Pending createOrUpdatePending(Pending newPending) {
        var found = em.createQuery("""
            SELECT p
            FROM Pending p
            WHERE p.kind.type = :kindType AND p.kindId = :kindId
            """, Pending.class)
            .setParameter("kindType", newPending.getKind().getType())
            .setParameter("kindId", newPending.getKindId())
            .getResultStream()
            .findFirst().orElse(null);

        if (found == null) {
            em.persist(newPending);
            return newPending;
        }
        found
            .setKind(newPending.getKind())
            .setRetryCount(0)
            ;
        return found;
    }

    public Pending findPending(UUID pendingId) {
        return em.find(Pending.class, pendingId);
    }

    public Pending findPending(String token, String code) {
        return em
            .createQuery("""
                SELECT p FROM Pending p
                WHERE p.token = :token AND p.code = :code
                """, Pending.class)
            .setParameter("token", token)
            .setParameter("code", code)
            .getSingleResult();
    }

    public Pending updatePending(Pending pending, Enum state) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
