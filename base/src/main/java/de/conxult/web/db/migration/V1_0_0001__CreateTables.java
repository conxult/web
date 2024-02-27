/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.db.migration;

import de.conxult.pa.db.migration.CxPaBaseJavaMigration;
import de.conxult.web.entity.Credential;
import de.conxult.web.entity.CredentialHist;
import de.conxult.web.entity.KeyPair;
import de.conxult.web.entity.KeyPairHist;
import de.conxult.web.entity.Pending;
import de.conxult.web.entity.PendingHist;
import de.conxult.web.entity.Role;
import de.conxult.web.entity.RoleHist;
import de.conxult.web.entity.User;
import de.conxult.web.entity.UserHist;
import de.conxult.web.entity.UserRole;
import de.conxult.web.entity.UserRoleHist;
import de.conxult.web.entity.WorkflowLog;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;

/**
 *
 * @author joerg
 */
@Dependent
@Unremovable
public class V1_0_0001__CreateTables
    extends CxPaBaseJavaMigration {

    @Override
    public void migrate() {
        createTable(User.class).ifNotExists();
        createTable(UserHist.class).ifNotExists();
        createTable(Role.class).ifNotExists();
        createTable(RoleHist.class).ifNotExists();
        createTable(UserRole.class).ifNotExists();
        createTable(UserRoleHist.class).ifNotExists();
        createTable(Pending.class).ifNotExists();
        createTable(PendingHist.class).ifNotExists();
        createTable(Credential.class).ifNotExists();
        createTable(CredentialHist.class).ifNotExists();
        createTable(KeyPair.class).ifNotExists();
        createTable(KeyPairHist.class).ifNotExists();
        createTable(WorkflowLog.class).ifNotExists();
    }

}

