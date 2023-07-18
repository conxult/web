package de.conxult.web.boundary;

import de.conxult.web.control.UserController;
import de.conxult.web.domain.LoginRequest;
import de.conxult.web.domain.LoginResponse;
import de.conxult.web.domain.RefreshRequest;
import de.conxult.web.domain.RefreshResponse;
import de.conxult.web.domain.SignupRequest;
import de.conxult.web.domain.SignupResponse;
import de.conxult.web.domain.Token;
import de.conxult.web.entity.User;
import de.conxult.web.util.TokenBuilder;
import de.conxult.web.util.TokenUtil;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.util.UUID;

@Path("users")
@RequestScoped
public class UserResource {

  @Inject
  UserController userController;

  @Inject
  TokenUtil tokenUtil;

  @POST
  @PermitAll
  @Path("login")
  @Transactional
  public LoginResponse login(
    LoginRequest loginRequest
  ) {
    // retrieve the corresponding user
    User user = userController
      .loginUser(loginRequest.getEmail(), loginRequest.getPassword());

    if (user == null) {
      return new LoginResponse();
    }

    // create accessToken and let application modify it
    Token newAccessToken = createAccessTokenBuilder(user)
      .build();

    // create refreshToken and let application modify it
    Token newRefreshToken = createRefreshTokenBuilder(user, newAccessToken.getToken())
      .build();

    return new LoginResponse()
      .setUser(user)
      .setAccessToken(newAccessToken)
      .setRefreshToken(newRefreshToken)
    ;
  }

  @POST
  @PermitAll
  @Path("refresh")
  @Transactional
  public RefreshResponse refresh(
    RefreshRequest refreshRequest
  ) {
    String accessToken = refreshRequest.getAccessToken();

    // try {
    JsonObject decodedAccessToken = JWT.parse(accessToken);
    String refreshToken = refreshRequest.getRefreshToken();
    JsonObject decodedRefreshToken = JWT.parse(refreshToken);

    if (accessToken.equals(tokenUtil.getTokenValue(decodedRefreshToken, "payload.accessToken"))) {
      Object id   = tokenUtil.getTokenValue(decodedAccessToken, "payload.upn");
      User   user = userController.verifyUser(UUID.fromString((String)id));

      // create accessToken and let application modify it
      Token newAccessToken = createAccessTokenBuilder(user)
        .build();

      // create refreshToken and let application modify it
      Token newRefreshToken = createRefreshTokenBuilder(user, newAccessToken.getToken())
        .build();

      return new RefreshResponse()
        .setAccessToken(newAccessToken)
        .setRefreshToken(newRefreshToken)
      ;
    }

    return null;
  }

  @POST
  @Path("signup")
  @Transactional
  public SignupResponse signup(SignupRequest signupRequest) {
    return userController.signupUser(signupRequest);
  }

  TokenBuilder createAccessTokenBuilder(User user) {
    return tokenUtil.createAccessTokenBuilder()
      .upn(user.getId().toString())
      .groups(user.getRoles().toArray(new String[0]))
    ;
  }

  TokenBuilder createRefreshTokenBuilder(User user, String accessToken) {
    return tokenUtil.createRefreshTokenBuilder()
      .upn(user.getId().toString())
      .claim("accessToken", accessToken)
    ;
  }

}
