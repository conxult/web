/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.conxult.log.Log;
import de.conxult.pa.JPQL;
import de.conxult.web.WebConfiguration;
import de.conxult.web.domain.WebContext;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.UUID;

/**
 *
 * @author joerg
 */
public class WebController {

    public static final UUID UUID_BASE = new UUID(0x0, 0x4711);

    @Inject
    protected Log log;

    @Inject
    protected ObjectMapper objectMapper;

    @Inject
    protected WebConfiguration webConfiguration;

    @Inject
    protected WebContext webContext;

    @Inject
    @PersistenceUnit(WebConfiguration.SCHEMA)
    protected EntityManager em;

    public <T> TypedQuery<T> select(JPQL jpql, Class<T> type) {
        return jpql.setQueryParameters(em.createQuery(jpql.getJpql(), type));
    }

    protected UUID getCurrentUUID() {
        UUID uuid = webContext.getUserId();
        return uuid == null ? UUID_BASE : uuid;
    }

}
