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
public class UserResetPasswordPending {

    String pin;

    UUID   userId;

}
