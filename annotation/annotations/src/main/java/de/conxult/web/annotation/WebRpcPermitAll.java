/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author joerg
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, METHOD })
public @interface WebRpcPermitAll {
}
