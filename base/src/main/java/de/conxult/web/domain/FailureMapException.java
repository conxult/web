/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.domain;

import lombok.Getter;

/**
 *
 * @author joerg
 */

@Getter
public class FailureMapException
    extends Exception {

    FailureMap failureMap;

    public FailureMapException(FailureMap failureMap) {
        this.failureMap = failureMap;
    }

}
