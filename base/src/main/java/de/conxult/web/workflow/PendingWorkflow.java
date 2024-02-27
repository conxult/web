/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.workflow;

import de.conxult.web.control.PendingController;
import de.conxult.web.entity.Kind;
import de.conxult.web.entity.Pending;
import de.conxult.workflow.WorkflowTask;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;

/**
 *
 * @author joerg
 */

@Dependent
@Unremovable
public class PendingWorkflow
    extends WebWorkflow {

    @Inject
    PendingController pendingController;

    @WorkflowTask
    public Pending createOrUpdate(Kind kind, String kindId) {

        // find pending
        var pending = pendingController.findPendingByKind(kind.getType(), kindId);

        if (pending == null) {
            pending = new Pending()
                .setKindId(kindId)
                .setKind(kind)
                .setCreatedBy(getCurrentUUID());
            em.persist(pending);
            log("create pending", pending.getId());

        } else {
            pending
              .setValidUntil(OffsetDateTime.now().plusHours(1))
              .setRetryCount(0)
              .setKind(kind);

            log("update pending", pending.getId());
        }

        return pending;
    }

}
