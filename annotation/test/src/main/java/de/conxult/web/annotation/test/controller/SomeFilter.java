/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.annotation.test.controller;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */

@Getter @Setter @Accessors(chain = true)
public class SomeFilter {

    UUID id;

    String name;

}
