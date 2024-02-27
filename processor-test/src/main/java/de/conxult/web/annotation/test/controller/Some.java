/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.annotation.test.controller;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */

@Getter @Setter @Accessors(chain = true)
public class Some {

    UUID           createdBy;
    OffsetDateTime createdAt;

    UUID           id;
    String         name;

}
