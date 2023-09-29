package de.conxult.web.entity;

import de.conxult.pa.annotation.PaHistoryTable;
import de.conxult.pa.entity.BaseEntity;
import de.conxult.pa.entity.IdEntity;
import de.conxult.web.WebConfiguration;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Entity
@Table(name = "pendings", schema = WebConfiguration.SCHEMA,
    uniqueConstraints = {
        @UniqueConstraint(name = "uk1", columnNames = { "kind_type", "kind_id" })
    })
@PaHistoryTable
@Getter @Setter @Accessors(chain = true)
public class Pending
    extends    BaseEntity<Pending>
    implements IdEntity<Pending> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @Embedded
    Kind kind;

    @Column(name = "kind_id")
    String kindId;

    @Column(name = "valid_until")
    OffsetDateTime validUntil;

    @Column(name = "retry_count")
    int retryCount;

    @PrePersist
    @Override
    public void onPrePersist() {
        super.onPrePersist();
        setValidUntil(OffsetDateTime.now().plusHours(1));
    }

    @PreUpdate
    @Override
    public void onPreUpdate() {
        super.onPreUpdate();
        setValidUntil(OffsetDateTime.now().plusHours(1));
    }


}
