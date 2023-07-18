/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.context;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author joerg
 */
public class RequestContextMap {

  Map<Enum, Object> values = new HashMap<>();

  public RequestContextMap setValue(Enum key, Object value) {
    values.put(key, value);
    return this;
  }

  public <T> T getValue(Enum key) {
    return (T) values.get(key);
  }

  public <T> T getValue(Enum key, T defaultValue) {
    T result = (T) values.get(key);
    return (result != null) ? result : defaultValue;
  }

}
