package de.conxult.web.entity;

import de.conxult.pa.annotation.PaHistoryTable;
import de.conxult.pa.entity.VersionEntity;
import de.conxult.web.WebConfiguration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Entity
@Table(name = "credentials", schema = WebConfiguration.SCHEMA)
@PaHistoryTable
@Getter @Setter @Accessors(chain = true)
public class Credential
  extends VersionEntity<Credential> {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    UUID userId;

    @Column(name = "hash", nullable = false, length = 128)
    String hash;

    @Column(name = "state", length = 64)
    String state;

    public static enum State {

        ACTIVE,
        DISABLED,
        DELETED,

    }

}
