/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.context;

import java.util.UUID;

/**
 *
 * @author joerg
 */
public class RequestContext {

  private static final ThreadLocal<RequestContextMap> map = new ThreadLocal<>();

  public static RequestContextMap setValue(Enum key, Object value) {
    return map().setValue(key, value);
  }

  public static UUID getCurrentUser() {
    return getValue(RequestContextKey.CURRENT_USER);
  }

  public static <T> T getValue(Enum key) {
    return map().getValue(key);
  }

  public static <T> T getValue(Enum key, T defaultValue) {
    return map().getValue(key, defaultValue);
  }

  public static RequestContextMap map() {
    if (map.get() == null) {
      map.set(new RequestContextMap());
    }
    return map.get();
  }
}
