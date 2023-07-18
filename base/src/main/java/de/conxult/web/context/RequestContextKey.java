/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.context;

import java.util.UUID;

/**
 *
 * @author joerg
 */
public enum RequestContextKey {

  CURRENT_USER(UUID.class),
  ;

  Class<?> type;

  RequestContextKey(Class<?> type) {
    this.type = type;
  }
  
}
