/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.websocket;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Getter @Setter @Accessors(chain = true)
public class AuthorizeCommand {

    String token;
}
