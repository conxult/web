/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.domain;

import de.conxult.web.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Getter @Setter @Accessors(chain = true)
public class LoginResponse {

  User  user;
  Token accessToken;
  Token refreshToken;

}
