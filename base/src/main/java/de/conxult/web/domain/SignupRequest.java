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
public class SignupRequest {

  String  email;
  String  nickName;
  String  firstName;
  String  lastName;
  String  password;
  String  passwordConfirm;
  boolean acceptLicense;

}
