/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.annotation;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author joerg
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE })
@Repeatable(WebRpcAnnotation.List.class)
public @interface WebRpcAnnotation {
    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ TYPE })
    public @interface List {
        WebRpcAnnotation[] value();
    }
}
