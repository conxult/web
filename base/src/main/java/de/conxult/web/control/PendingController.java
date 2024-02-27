/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.web.entity.Kind;
import de.conxult.web.entity.Pending;
import jakarta.enterprise.context.RequestScoped;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 *
 * @author joerg
 */
@RequestScoped
public class PendingController
    extends WebController {

    public Pending findPending(UUID pendingId) {
        return em.find(Pending.class, pendingId);
    }

    public Pending findPendingByKind(String kindType, String kindId) {
        return em.createQuery("""
            SELECT p
            FROM Pending p
            WHERE p.kind.type = :kindType AND p.kindId = :kindId
            """, Pending.class)
            .setParameter("kindType", kindType)
            .setParameter("kindId", kindId)
            .setMaxResults(1)
            .getResultStream()
            .findFirst().orElse(null);

    }

    public Pending findPendingByTokenAndCode(String token, String code) {
        return em
            .createQuery("""
                SELECT p FROM Pending p
                WHERE p.token = :token AND p.code = :code
                """, Pending.class)
            .setParameter("token", token)
            .setParameter("code", code)
            .getSingleResult();
    }

    public Pending createOrUpdate(Kind kind, String kindId) {

        // find pending
        var pending = findPendingByKind(kind.getType(), kindId);

        if (pending == null) {
            pending = new Pending()
                .setKindId(kindId)
                .setKind(kind)
                .setCreatedBy(getCurrentUUID());
            em.persist(pending);
            log.info("create pending {0}", pending.getId());

        } else {
            pending
              .setValidUntil(OffsetDateTime.now().plusHours(1))
              .setRetryCount(0)
              .setKind(kind);

            log.info("update pending {0}", pending.getId());
        }

        return pending;
    }

}
