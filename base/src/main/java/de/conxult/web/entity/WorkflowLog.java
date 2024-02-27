package de.conxult.web.entity;

import com.fasterxml.jackson.databind.JsonNode;
import de.conxult.web.WebConfiguration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
@Table(name = "workflow_logs", schema = WebConfiguration.SCHEMA)
@Getter @Setter @Accessors(chain = true)
public class WorkflowLog {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @Column(name = "workflow", nullable = false, updatable = false)
    String workflow;

    @Column(name = "started_at", nullable = false, updatable = false)
    OffsetDateTime startedAt;

    @Column(name = "finished_at", nullable = false, updatable = false)
    OffsetDateTime finishedAt;

    @Column(name = "log_entries")
    String logEntries;

    @Transient
    JsonNode logEntriesJson;

    @PrePersist
    public void onPrePersist() {
        id = UUID.randomUUID();
    }

}
