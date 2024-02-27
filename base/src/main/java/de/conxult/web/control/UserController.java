/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.web.annotation.WebRpcController;
import de.conxult.web.annotation.WebRpcMethod;
import de.conxult.web.annotation.WebRpcPermitAll;
import de.conxult.web.annotation.WebRpcQueryParam;
import de.conxult.web.annotation.WebRpcRolesAllowed;
import de.conxult.web.annotation.WebRpcTransactional;
import de.conxult.web.domain.UserConfirmSignupRequest;
import de.conxult.web.domain.UserConfirmSignupResponse;
import de.conxult.web.domain.UserLoginRequest;
import de.conxult.web.domain.UserLoginResponse;
import de.conxult.web.domain.UserRefreshTokensRequest;
import de.conxult.web.domain.UserRefreshTokensResponse;
import de.conxult.web.domain.UserSetPasswordRequest;
import de.conxult.web.domain.UserSetPasswordResponse;
import de.conxult.web.domain.UserSignupRequest;
import de.conxult.web.domain.UserSignupResponse;
import de.conxult.web.domain.WebException;
import de.conxult.web.entity.Credential;
import de.conxult.web.entity.Role;
import static de.conxult.web.entity.Role.ADMIN;
import static de.conxult.web.entity.Role.USER;
import de.conxult.web.entity.User;
import de.conxult.web.util.CredentialUtil;
import de.conxult.web.workflow.UserWorkflows;
import de.conxult.workflow.Workflow;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
    CredentialUtil credentialUtil;

    @WebRpcMethod(description = "find user")
    @WebRpcRolesAllowed(Role.ADMIN)
    public User findUser(UUID userId) {
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
          .setHash(credentialUtil.createHash(password))
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

    @WebRpcMethod(description = "login user")
    @WebRpcTransactional
    public UserLoginResponse loginUser(
        UserLoginRequest request
    ) {
        return Workflow.of(UserWorkflows.class).loginUser(request);
    }

    @WebRpcMethod(description = "signup user")
    @WebRpcPermitAll
    @Transactional
    public UserSignupResponse signupUser(
        UserSignupRequest request
    ) {
        return Workflow.of(UserWorkflows.class).signup(request);
    }

    @WebRpcMethod(description = "confirm signup")
    @WebRpcPermitAll
    @Transactional
    public UserConfirmSignupResponse confirmSignup(
        UserConfirmSignupRequest request
    ) {
        return Workflow.of(UserWorkflows.class).confirm(request.getPendingId(), request.getPin());
    }

    @WebRpcMethod(description = "confirm signup", useGET = true)
    @WebRpcPermitAll
    @Transactional
    public UserConfirmSignupResponse confirmSignup(
        @WebRpcQueryParam("pendingId") UUID   pendingId,
        @WebRpcQueryParam("pin")       String pin
    ) {
        return Workflow.of(UserWorkflows.class).confirm(pendingId, pin);
    }

    @WebRpcMethod(description = "refresh token")
    @WebRpcRolesAllowed({ADMIN, USER})
    @WebRpcTransactional
    public UserRefreshTokensResponse refreshToken(
        UserRefreshTokensRequest refreshRequest
    ) {
        return Workflow.of(UserWorkflows.class).refreshToken(refreshRequest);
    }

    @WebRpcMethod(description = "set password")
    @WebRpcPermitAll
    @Transactional
    public UserSetPasswordResponse setPassword(
        UserSetPasswordRequest request
    ) {
        return Workflow.of(UserWorkflows.class).setPassword(request);
    }

    public User findUser(String email, User.State state) {
        return em.createQuery("""
            SELECT u
            FROM User u
            WHERE lower(u.email) = :email AND u.state = :state
            """, User.class)
            .setParameter("email", email.toLowerCase())
            .setParameter("state", state.name())
            .setMaxResults(1)
            .getResultStream().findFirst().orElse(null);
    }

    public User findUser(String email) {
        return em.createQuery("""
            SELECT u FROM User u WHERE lower(u.email) = :email
            """, User.class)
            .setParameter("email", email.toLowerCase())
            .getResultStream()
          .findFirst().orElse(null);
    }
}
