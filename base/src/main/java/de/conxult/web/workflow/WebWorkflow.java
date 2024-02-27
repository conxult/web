/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.conxult.log.Log;
import de.conxult.util.ClassUtil;
import de.conxult.web.WebConfiguration;
import static de.conxult.web.control.WebController.UUID_BASE;
import de.conxult.web.domain.WebContext;
import de.conxult.web.entity.WorkflowLog;
import de.conxult.workflow.Workflow;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author joerg
 */

public class WebWorkflow
    extends Workflow {

    @Inject
    Log log;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    WebConfiguration webConfiguration;

    @Inject
    WebContext webContext;

    @Inject
    @PersistenceUnit(WebConfiguration.SCHEMA)
    EntityManager em;

    public Map<String, String> getApplication() {
        return Map.of(
            "id"         , webConfiguration.getApplicationId(),
            "name"       , webConfiguration.getApplicationName(),
            "description", webConfiguration.getApplicationDescription());
    }



    <T> T finishWorkflow(T response) {
        finishWorkflow();
        return response;
    }

    void finishWorkflow() {
        var workflowLog = new WorkflowLog()
            .setWorkflow(ClassUtil.normalize(getClass()).getName())
            .setStartedAt(getStartedAt())
            .setFinishedAt(OffsetDateTime.now())
            .setLogEntries(toJsonString(getLogEntries()))
            ;

        em.persist(workflowLog);
    }

    UUID getCurrentUUID() {
        UUID uuid = webContext.getUserId();
        return uuid == null ? UUID_BASE : uuid;
    }

    String toJsonString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException jsonProcessingException) {
            return Map.of(
              "exception", jsonProcessingException,
              "value", value).toString();
        }
    }

}
