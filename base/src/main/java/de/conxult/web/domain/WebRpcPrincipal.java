/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.domain;

import jakarta.enterprise.context.Dependent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author joerg
 */
@Dependent
public class WebRpcPrincipal {

    private final static ThreadLocal<Map<String, Object>> data = ThreadLocal.withInitial(HashMap::new);

    public WebRpcPrincipal clean() {
        data.get().clear();
        return this;
    }

    public WebRpcPrincipal put(String key, Object value) {
        data.get().put(key, value);
        return this;
    }

    public <T> T get(String key) {
        return (T)data.get().get(key);
    }

}
