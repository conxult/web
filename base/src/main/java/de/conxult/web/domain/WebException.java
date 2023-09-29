/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.domain;

import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author joerg
 */
public class WebException
    extends Exception {

    Map<String, Object> parameters = new TreeMap<>();

    public WebException(String reason, Object... parameters) {
        super(parameters.length == 0 ? reason : MessageFormat.format(reason, parameters));
    }

    public WebException add(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

}
