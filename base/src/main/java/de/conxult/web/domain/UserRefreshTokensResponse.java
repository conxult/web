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
public class UserRefreshTokensResponse
    extends FailureMap<UserRefreshTokensResponse> {

    public static final String TOKENS_FAILURE = "tokens.failure";

    Token accessToken;
    Token refreshToken;

}
