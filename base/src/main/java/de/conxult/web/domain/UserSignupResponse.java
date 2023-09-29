/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.domain;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Getter @Setter @Accessors(chain = true)
public class UserSignupResponse
    extends BaseFailureMap<SignupConfirmResponse> {

    public static final String PASSWORD_FORMAT_FAILURE  = "password.failure";
    public static final String NICK_NAME_FORMAT_FAILURE = "nickName.failure";
    public static final String EMAIL_FORMAT_FAILURE     = "email.failure";
    public static final String LICENSE_CHECK_FAILURE    = "licenseCheck.failure";

    UUID   pendingId;
    String pin;

}
