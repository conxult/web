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
public class FailureMap<THIS extends FailureMap> {

    public static final String EMPTY       = "empty";
    public static final String FORMAT      = "format";
    public static final String MISMATCH    = "mismatch";
    public static final String MISSING     = "missing";
    public static final String UNAVAILABLE = "unavailable";
    public static final String LICENSE     = "license";
    public static final String PERMISSION  = "permission";

    public static final String PASSWORD_FORMAT_FAILURE  = "password.failure";
    public static final String NICK_NAME_FORMAT_FAILURE = "nickName.failure";
    public static final String EMAIL_FORMAT_FAILURE     = "email.failure";
    public static final String LICENSE_CHECK_FAILURE    = "licenseCheck.failure";


    @Getter
    Map<String, String> failures = new HashMap<>();

    public THIS addFailure(String name, String failure) {
        if (failure != null) {
            failures.put(name, failure);
        }
        return (THIS) this;
    }

    public boolean hasFailure(String name) {
        return failures.containsKey(name);
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }
}
