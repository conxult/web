/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.domain;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 *
 * @author joerg
 */
public class BaseFailureMap {

  public static final String EMPTY       = "empty";
  public static final String FORMAT      = "format";
  public static final String MISMATCH    = "mismatch";
  public static final String UNAVAILABLE = "unavailable";
  public static final String LICENSE     = "license";
  public static final String PERMISSION  = "permission";

  @Getter
  Map<String, String> failures;

  public BaseFailureMap setFailure(String name, String failure) {
    if (failure != null) {
      if (failures == null) {
        failures = new HashMap<>();
      }
      failures.put(name, failure);
    }
    return this;
  }

  public boolean hasFailures() {
    return failures != null && !failures.isEmpty();
  }
}
