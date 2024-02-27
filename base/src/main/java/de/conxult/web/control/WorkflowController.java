/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import de.conxult.web.annotation.WebRpcController;
import de.conxult.web.annotation.WebRpcMethod;
import de.conxult.web.entity.WorkflowLog;
import jakarta.enterprise.context.RequestScoped;
import java.util.List;

/**
 *
 * @author joerg
 */
@RequestScoped
@WebRpcController(
    className = "de.conxult.web.boundary.WorkflowWebRpc",
    openapiTag = "Workflow Boundary",
    path = "workflows"
)
public class WorkflowController
    extends WebController {

    @WebRpcMethod(description = "get all workflow names")
//    @WebRpcRolesAllowed(Role.ADMIN)
    public List<String> getWorkflowNames() {
        return em.createNativeQuery("""
            SELECT DISTINCT workflow
            FROM web.workflow_logs
            """, String.class)
            .getResultList();
    }

    @WebRpcMethod(description = "get all workflows for a workflow")
    public List<WorkflowLog> getWorkflows(String workflow) {
        var result = (List<WorkflowLog>) em.createQuery("""
            SELECT wl
            FROM WorkflowLog wl
            WHERE wl.workflow = :workflow
            ORDER BY wl.startedAt DESC
            """, WorkflowLog.class)
            .setParameter("workflow", workflow)
            .getResultList();

        result.forEach(wl -> wl.setLogEntriesJson(toJson(wl.getLogEntries())).setLogEntries(null));

        return result;
    }

    JsonNode toJson(String string) {
        try {
            return objectMapper.readTree(string);
        } catch (JsonProcessingException jpException) {
            return null;
        }
    }



}
