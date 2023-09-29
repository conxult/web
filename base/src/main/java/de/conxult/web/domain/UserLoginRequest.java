/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */

@Getter @Setter @Accessors(chain = true)
public class UserLoginRequest {

    String email;
    String password;

}
