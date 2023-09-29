/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.util.ToDo;
import de.conxult.web.annotation.WebRpcController;
import de.conxult.web.annotation.WebRpcMethod;
import de.conxult.web.annotation.WebRpcPermitAll;
import de.conxult.web.annotation.WebRpcQueryParam;
import de.conxult.web.annotation.WebRpcRolesAllowed;
import de.conxult.web.annotation.WebRpcTransactional;
import de.conxult.web.domain.BaseFailureMap;
import de.conxult.web.domain.MailRequest;
import de.conxult.web.domain.SignupConfirmRequest;
import de.conxult.web.domain.SignupConfirmResponse;
import de.conxult.web.domain.Token;
import de.conxult.web.domain.UserLoginRequest;
import de.conxult.web.domain.UserLoginResponse;
import de.conxult.web.domain.UserRefreshTokensRequest;
import de.conxult.web.domain.UserRefreshTokensResponse;
import de.conxult.web.domain.UserSignupPending;
import de.conxult.web.domain.UserSignupRequest;
import de.conxult.web.domain.UserSignupResponse;
import de.conxult.web.domain.WebContext;
import de.conxult.web.domain.WebException;
import de.conxult.web.entity.Credential;
import de.conxult.web.entity.Kind;
import de.conxult.web.entity.Pending;
import de.conxult.web.entity.PendingHist;
import de.conxult.web.entity.Role;
import static de.conxult.web.entity.Role.ADMIN;
import static de.conxult.web.entity.Role.USER;
import de.conxult.web.entity.User;
import de.conxult.web.entity.UserRole;
import de.conxult.web.util.PasswordUtil;
import de.conxult.web.util.TokenBuilder;
import de.conxult.web.util.TokenUtil;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author joerg
 */
@RequestScoped
@WebRpcController(
    className = "de.conxult.web.boundary.UserWebRpc",
    openapiTag = "User Boundary",
    path = "users"
)
public class UserController
    extends WebController {

    @Inject
    PasswordUtil passwordUtil;

    @Inject
    TokenUtil tokenUtil;

    @Inject
    PendingController pendingController;

    @Inject
    MailController mailController;

    @Inject
    WebContext webContext;

    @WebRpcMethod(description = "find user")
    @WebRpcRolesAllowed(Role.ADMIN)
    public User findUser(UUID userId) throws IOException {
        return (userId == null) ? null : em.find(User.class, userId);
    }

    @WebRpcMethod(description = "create user")
    @WebRpcRolesAllowed(Role.ADMIN)
    @WebRpcTransactional
    public User createUser(User user, String password) {
        em.persist(user
          .setCreatedBy(User.UUID_ZERO));
        em.persist(new Credential()
          .setUserId(user.getId())
          .setCreatedBy(User.UUID_ZERO)
          .setHash(passwordUtil.createHash(password))
          .setState("created"));
        return user;
    }

    @WebRpcMethod(description = "update user")
    @WebRpcRolesAllowed(Role.ADMIN)
    @WebRpcTransactional
    public User updateUser(User user) throws WebException {
        User found = em.find(User.class, user.getId());
        if (found == null) {
            throw new WebException("user not found {0}", user.getId())
                .add("userId", user.getId());
        }
        em.merge(user);
        return user;
    }

    @WebRpcMethod(description = "create role")
    @WebRpcRolesAllowed(Role.ADMIN)
    @WebRpcTransactional
    public Role createRole(Role role) {
        em.persist(role
            .setCreatedBy(User.UUID_ZERO));
        return role;
    }

    @WebRpcMethod(description = "find role")
    @WebRpcRolesAllowed({Role.ADMIN, Role.USER})
    public Role findRole(String name) {
        return em.createQuery("""
            SELECT r
            FROM Role r
            WHERE name = :name
            """, Role.class)
            .setParameter("name", name)
            .getResultStream()
            .findFirst().orElse(null);
    }

    @WebRpcMethod(description = "assign role")
    @WebRpcRolesAllowed(Role.ADMIN)
    @WebRpcTransactional
    public UserRole assignRole(UUID userId, UUID roleId) {
        UserRole found = em.createQuery("""
            SELECT ur
            FROM UserRole ur
            WHERE ur.userId = :userId AND ur.roleId = :roleId
            """, UserRole.class)
            .setParameter("userId", userId)
            .setParameter("roleId", roleId)
            .setMaxResults(1)
            .getResultStream()
            .findFirst().orElse(null);

        if (found == null) {
            UserRole assign = new UserRole()
                .setUserId(userId)
                .setRoleId(roleId)
                .setCreatedBy(webPrincipal.getUserId());
            em.persist(assign);
            return assign;
        }
        return found;
    }

    @WebRpcMethod(description = "login user")
    @WebRpcTransactional
    public UserLoginResponse login(UserLoginRequest loginRequest) {
        UserLoginResponse response = new UserLoginResponse();

        var user = em.createQuery("""
            SELECT u
            FROM User u
            WHERE lower(u.email) = :email AND u.state = :state
            """, User.class)
            .setParameter("email", loginRequest.getEmail().toLowerCase())
            .setParameter("state", User.State.ACTIVE.name())
            .setMaxResults(1)
            .getResultStream().findFirst().orElse(null);

        if (user == null) {
            return response.addFailure(UserLoginResponse.CREDENTIALS_FAILURE, UserLoginResponse.MISMATCH);
        }

        var credential = em.createQuery("""
            SELECT c
            FROM Credential c
            WHERE c.id = :id
            """, Credential.class)
            .setParameter(1, user.getId())
            .setMaxResults(1)
            .getResultStream().findFirst().orElse(null);

        if (credential == null || !passwordUtil.validateHash(credential.getHash(), loginRequest.getPassword())) {
            return response.addFailure(UserLoginResponse.CREDENTIALS_FAILURE, UserLoginResponse.MISMATCH);
        }

        loadRoles(user);

        // create accessToken and let application modify it
        Token newAccessToken = createAccessTokenBuilder(user)
            .build();

        // create refreshToken and let application modify it
        Token newRefreshToken = createRefreshTokenBuilder(user, newAccessToken.getToken())
            .build();

        return response
            .setUser(user)
            .setAccessToken(newAccessToken)
            .setRefreshToken(newRefreshToken);
    }

    @WebRpcMethod(description = "verify user")
    @WebRpcRolesAllowed({ADMIN, USER})
    @WebRpcTransactional
    public UserRefreshTokensResponse refreshToken(
        UserRefreshTokensRequest refreshRequest
    ) {
        UserRefreshTokensResponse response = new UserRefreshTokensResponse();

        String accessToken = refreshRequest.getAccessToken();

        // try {
        JsonObject decodedAccessToken = JWT.parse(accessToken);
        String refreshToken = refreshRequest.getRefreshToken();
        JsonObject decodedRefreshToken = JWT.parse(refreshToken);

        if (accessToken.equals(tokenUtil.getTokenValue(decodedRefreshToken, "payload.accessToken"))) {
            Object id = tokenUtil.getTokenValue(decodedAccessToken, "payload.upn");
            User user = verifyUser(UUID.fromString((String) id));

            // create accessToken and let application modify it
            Token newAccessToken = createAccessTokenBuilder(user).build();

            // create refreshToken and let application modify it
            Token newRefreshToken = createRefreshTokenBuilder(user, newAccessToken.getToken()).build();

            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
        }

        return response;
    }

    @WebRpcMethod(description = "user signup")
    @WebRpcPermitAll
    @Transactional
    public UserSignupResponse signup(UserSignupRequest signupRequest) {
        var response = new UserSignupResponse();

        // verify request:
        // - verify formats
        response.addFailure(UserSignupResponse.EMAIL_FORMAT_FAILURE, webConfiguration.checkEmail(signupRequest.getEmail()));
        response.addFailure(UserSignupResponse.NICK_NAME_FORMAT_FAILURE, webConfiguration.checkNickName(signupRequest.getNickName()));
        response.addFailure(UserSignupResponse.PASSWORD_FORMAT_FAILURE, webConfiguration.checkPassword(signupRequest.getPassword()));
        if (!signupRequest.isAcceptLicense()) {
            response.addFailure(UserSignupResponse.LICENSE_CHECK_FAILURE, BaseFailureMap.LICENSE);
        }
        // - verify if user exists
        List<User> users = em.createQuery("select a from User a where lower(a.email) = ?1")
                .setParameter(1, signupRequest.getEmail().toLowerCase())
                .getResultList();
        if (!users.isEmpty()) {
            response.addFailure("email", BaseFailureMap.UNAVAILABLE);
        }

        if (response.hasFailures()) {
            return response;
        }

        // create pending
        signupRequest.setPassword(passwordUtil.createHash(signupRequest.getPassword())); // encrypt password for usage in pendings
        UserSignupPending signupPending = new UserSignupPending()
            .setSignupRequest(signupRequest)
            .setPin(passwordUtil.createRandomPin());

        Pending pending = new Pending()
            .setKindId(signupRequest.getEmail().toLowerCase())
            .setKind(Kind.of(signupPending))
            .setCreatedBy(getCurrentUUID())
        ;

        pending = pendingController.createOrUpdatePending(pending);

        response
            .setPendingId(pending.getId())
            .setPin(signupPending.getPin());

        // send pending email
        var mailRequest = new MailRequest("userSignup", signupRequest.getEmail())
            .addData("pendingId", response.getPendingId())
            .addData("pin", response.getPin())
            .addData("signupConfirmUrls", webConfiguration.getSignupConfirmUrls())
            .addData("paths", webContext.getRequest().getPaths());

        mailController.sendMail("userSignup", webContext.getRequest().getLanguage(), mailRequest);

        return response;
    }

    @WebRpcMethod(description = "signup confirm")
    @WebRpcPermitAll
    @Transactional
    public SignupConfirmResponse signupConfirm(
        SignupConfirmRequest signupConfirmRequest
    ) {
        return signupConfirm(signupConfirmRequest.getPendingId(), signupConfirmRequest.getPin());
    }

    @WebRpcMethod(description = "signup confirm", useGET = true)
    @WebRpcPermitAll
    @Transactional
    @ToDo("check validUntil")
    @ToDo("check retryCount")
    @ToDo("write to history")
    @ToDo("send mail")
    public SignupConfirmResponse signupConfirm(
        @WebRpcQueryParam("pendingId") UUID   pendingId,
        @WebRpcQueryParam("pin")       String pin
    ) {
        var response = new SignupConfirmResponse();
        var pending = pendingController.findPending(pendingId);

        if (pending != null &&
            pending.getKind().getValue() instanceof UserSignupPending signupPending &&
            pin.equals(signupPending.getPin())) {

            var signupRequest = signupPending.getSignupRequest();

            var user = new User()
                .setEmail(signupRequest.getEmail())
                .setNickName(signupRequest.getNickName())
                .setFamilyName(signupRequest.getFamilyName())
                .setSurName(signupRequest.getSurName())
                .setState(User.State.ACTIVE.name())
                .setCreatedBy(User.UUID_ZERO);
            em.persist(user);

            var credential = new Credential()
                .setUserId(user.getId())
                .setCreatedBy(User.UUID_ZERO)
                .setHash(signupRequest.getPassword()) // this is already encrypted
                .setState(Credential.State.ACTIVE.name());
            em.persist(credential);

            var role = findRole(Role.USER);
            assignRole(user.getId(), role.getId());

            em.persist(new PendingHist(pending, "resolved"));
            em.remove(pending);

            response
                .setUserId(user.getId())
                .setEmail(user.getEmail());

        } else {
            if (pending != null) {
                pending.setRetryCount(pending.getRetryCount() + 1);
            }
            response.addFailure("pending.failure", "not-resolved");
        }

        return response;
    }

    User loadRoles(User user) {
        if (user != null) {
            em.createQuery("select r from Role r join UserRole ar on (ar.roleId = r.id) where ar.userId = ?1", Role.class)
                .setParameter(1, user.getId())
                .getResultStream()
                .map(Role::getName)
                .forEach(roleName -> user.getRoles().add(roleName));
        }
        return user;
    }

    User verifyUser(UUID userId) {
        return (userId == null) ? null : loadRoles(em.find(User.class, userId));
    }

    TokenBuilder createAccessTokenBuilder(User user) {
        return tokenUtil.createAccessTokenBuilder()
            .upn(user.getId().toString())
            .groups(user.getRoles().toArray(new String[0]));
    }

    TokenBuilder createRefreshTokenBuilder(User user, String accessToken) {
        return tokenUtil.createRefreshTokenBuilder()
          .upn(user.getId().toString())
          .claim("accessToken", accessToken);
    }

}
