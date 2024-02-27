/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.workflow;

import de.conxult.util.ToDo;
import de.conxult.web.control.CredentialController;
import de.conxult.web.control.MailController;
import de.conxult.web.control.PendingController;
import de.conxult.web.control.RoleController;
import de.conxult.web.control.UserController;
import de.conxult.web.domain.FailureMap;
import de.conxult.web.domain.FailureMapException;
import de.conxult.web.domain.MailRequest;
import de.conxult.web.domain.Token;
import de.conxult.web.domain.UserConfirmSignupResponse;
import de.conxult.web.domain.UserLoginRequest;
import de.conxult.web.domain.UserLoginResponse;
import de.conxult.web.domain.UserRefreshTokensRequest;
import de.conxult.web.domain.UserRefreshTokensResponse;
import de.conxult.web.domain.UserResetPasswordPending;
import de.conxult.web.domain.UserSetPasswordRequest;
import de.conxult.web.domain.UserSetPasswordResponse;
import de.conxult.web.domain.UserSignupPending;
import de.conxult.web.domain.UserSignupRequest;
import de.conxult.web.domain.UserSignupResponse;
import de.conxult.web.entity.Credential;
import de.conxult.web.entity.CredentialHist;
import de.conxult.web.entity.Kind;
import de.conxult.web.entity.Pending;
import de.conxult.web.entity.PendingHist;
import de.conxult.web.entity.Role;
import de.conxult.web.entity.User;
import de.conxult.web.util.CredentialUtil;
import de.conxult.web.util.TokenBuilder;
import de.conxult.workflow.Railway;
import de.conxult.workflow.Workflow;
import de.conxult.workflow.WorkflowTask;
import io.quarkus.arc.Unremovable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.UUID;

/**
 *
 * @author joerg
 */

@Dependent
@Unremovable
public class UserWorkflows
    extends WebWorkflow {

    @Inject
    MailController mailController;

    @Inject
    RoleController roleController;

    @Inject
    UserController userController;

    @Inject
    PendingController pendingController;

    @Inject
    CredentialController credentialController;

    @Inject
    CredentialUtil credentialUtil;


    @WorkflowTask
    public UserLoginResponse loginUser(UserLoginRequest request) {
        UserLoginResponse response = new UserLoginResponse();

        Railway.of(response)
            .onSuccess((r) -> findUser(response, request))
            .onSuccess((user) -> roleController.loadRoles(user))
            .onSuccess((user) -> validateUser(response, request, user))
            .onSuccess((user) -> createTokens(response, user));

        return finishWorkflow(response);
    }

    @WorkflowTask
    public UserRefreshTokensResponse refreshToken(UserRefreshTokensRequest request) {
        UserRefreshTokensResponse response = new UserRefreshTokensResponse();

        Railway.of(response)
            .onSuccess((r)    -> findUser(response, request))
            .onSuccess((user) -> createTokens(response, user));

        return finishWorkflow(response);
    }

    @WorkflowTask
    User findUser(UserLoginResponse response, UserLoginRequest request) throws FailureMapException {
        var user = userController.findUser(request.getEmail(), User.State.ACTIVE);
        if (user == null) {
            response.addFailure(UserLoginResponse.CREDENTIALS_FAILURE, UserLoginResponse.MISMATCH);
            throw new FailureMapException(response);
        }
        return user;
    }

    @WorkflowTask
    User findUser(UserRefreshTokensResponse response, UserRefreshTokensRequest request) throws FailureMapException {
        String     accessToken         = request.getAccessToken();
        JsonObject decodedAccessToken  = JWT.parse(accessToken);
        String     refreshToken        = request.getRefreshToken();
        JsonObject decodedRefreshToken = JWT.parse(refreshToken);

        User user = null;

        if (accessToken.equals(credentialUtil.getTokenValue(decodedRefreshToken, "payload.accessToken")) &&
            credentialUtil.getTokenValue(decodedAccessToken, "payload.upn") instanceof String userId &&
            (user = em.find(User.class, userId)) != null) {
            roleController.loadRoles(user);
        } else {
             response.addFailure(UserRefreshTokensResponse.TOKENS_FAILURE, UserRefreshTokensResponse.MISMATCH);
             throw new FailureMapException(response);
        }

        return user;
    }

    @WorkflowTask
    User validateUser(UserLoginResponse response, UserLoginRequest request, User user) throws FailureMapException {
        var credential = credentialController.findCredential(user.getId());
        if (credential == null || !credentialUtil.validateHash(credential.getHash(), request.getPassword())) {
            response.addFailure(UserLoginResponse.CREDENTIALS_FAILURE, UserLoginResponse.MISMATCH);
            throw new FailureMapException(response);
        }
        return user;
    }


    @WorkflowTask
    UserLoginResponse createTokens(UserLoginResponse response, User user) {

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

    @WorkflowTask
    UserRefreshTokensResponse createTokens(UserRefreshTokensResponse response, User user) {
        // create accessToken and let application modify it
        Token newAccessToken = createAccessTokenBuilder(user)
            .build();

        // create refreshToken and let application modify it
        Token newRefreshToken = createRefreshTokenBuilder(user, newAccessToken.getToken())
            .build();

        return response
            .setAccessToken(newAccessToken)
            .setRefreshToken(newRefreshToken);
    }

    TokenBuilder createAccessTokenBuilder(User user) {
        return credentialUtil.createAccessTokenBuilder()
            .upn(user.getId().toString())
            .groups(user.getRoles().toArray(new String[0]));
    }

    TokenBuilder createRefreshTokenBuilder(User user, String accessToken) {
        return credentialUtil.createRefreshTokenBuilder()
            .upn(user.getId().toString())
            .claim("accessToken", accessToken);
    }
    @WorkflowTask
    public UserSignupResponse signup(UserSignupRequest request) {
        var response = new UserSignupResponse();

        verifyRequest(response, request);
        if (response.hasFailures()) {
            return finishWorkflow(response);
        }

        var pending = createOrUpdatePending(response, request);

        sendMail(response, request, pending);

        return finishWorkflow(response);
    }

    @WorkflowTask
    public UserConfirmSignupResponse confirm(UUID pendingId, String pin) {
        var response = new UserConfirmSignupResponse();

        var pending = findPending(response, pendingId);
        if (response.hasFailures()) {
            return finishWorkflow(response);
        }

        var signupPending = validatePending(response, pending, pin);
        if (response.hasFailures()) {
            return finishWorkflow(response);
        }

        var user = createUser(response, signupPending.getSignupRequest());

        var credential = createCredentials(response, user, signupPending.getPasswordHash());

        assignRole(response, user);

        historizePending(response, pending);

        return finishWorkflow(response);
    }

    @WorkflowTask
    Pending findPending(UserConfirmSignupResponse response, UUID pendingId) {
        var pending = pendingController.findPending(pendingId);

        if (pending == null) {
            response.addFailure("pending.failure", "not-resolved");
            log("pending not found", pendingId);
        }

        return pending;
    }

    @WorkflowTask
    UserSignupPending validatePending(UserConfirmSignupResponse response, Pending pending, String pin) {

        if (pending.getKind().getValue() instanceof UserSignupPending signupPending) {

            if (!pin.equals(signupPending.getPin())) {
                response.addFailure("pending.failure", "not-resolved");
                pending.setRetryCount(pending.getRetryCount() + 1);
                log("pin mismatch", pin, pending.getRetryCount());
            }

            return signupPending;
        }

        return null;
    }

    @WorkflowTask
    User createUser(UserConfirmSignupResponse response, UserSignupRequest signupRequest) {
        var user = new User()
            .setEmail(signupRequest.getEmail())
            .setNickName(signupRequest.getNickName())
            .setFamilyName(signupRequest.getFamilyName())
            .setSurName(signupRequest.getSurName())
            .setState(User.State.ACTIVE.name())
            .setCreatedBy(User.UUID_ZERO);
        em.persist(user);

        response
            .setUserId(user.getId())
            .setEmail(user.getEmail());

        log("created user", user.getId(), user.getEmail());

        return user;
    }

    @WorkflowTask
    Credential createCredentials(UserConfirmSignupResponse response, User user, String passwordHash) {
        var credential = new Credential()
            .setUserId(user.getId())
            .setCreatedBy(User.UUID_ZERO)
            .setHash(passwordHash)
            .setState(Credential.State.ACTIVE.name());
        em.persist(credential);

        log("created credential", user.getId(), credential.getHash());

        return credential;
    }

    @WorkflowTask
    void assignRole(UserConfirmSignupResponse response, User user) {
        var role = roleController.findRole(Role.USER);
        roleController.assignRole(user.getId(), role.getId());

        log("assigned role", user.getId(), role.getId(), role.getName());
    }

    @WorkflowTask
    void historizePending(UserConfirmSignupResponse response, Pending pending) {
        var role = roleController.findRole(Role.USER);

        em.persist(new de.conxult.web.entity.PendingHist(pending, "resolved"));
        em.remove(pending);

        log("historized pending", pending.getId());
    }
    @WorkflowTask
    void verifyRequest(UserSignupResponse response, UserSignupRequest request) {
        // verify formats
        response.addFailure(UserSignupResponse.EMAIL_FORMAT_FAILURE, webConfiguration.checkEmail(request.getEmail()));
        response.addFailure(UserSignupResponse.NICK_NAME_FORMAT_FAILURE, webConfiguration.checkNickName(request.getNickName()));
        response.addFailure(UserSignupResponse.PASSWORD_FORMAT_FAILURE, webConfiguration.checkPassword(request.getPassword()));
        if (!request.isAcceptLicense()) {
            response.addFailure(UserSignupResponse.LICENSE_CHECK_FAILURE, FailureMap.LICENSE);
        }
        // verify if user exists
        if (em.createQuery("select a from User a where lower(a.email) = :email")
            .setParameter("email", request.getEmail().toLowerCase())
            .getResultStream().findFirst().isPresent()) {
            response.addFailure("email", FailureMap.UNAVAILABLE);
        }
    }

    @WorkflowTask
    UserSignupPending createOrUpdatePending(UserSignupResponse response, UserSignupRequest request) {

        // create pending info
        UserSignupPending signupPending = new UserSignupPending()
            .setSignupRequest(request)
            .setPasswordHash(credentialUtil.createHash(request.getPassword()))
            .setPin(credentialUtil.createRandomPin());
        request.setPassword("***hashed***");

        // create pending
        Kind   kind   = Kind.of(signupPending);
        String kindId = request.getEmail().toLowerCase();

        var pending = Workflow.subOf(this, PendingWorkflow.class).createOrUpdate(kind, kindId);

        log("pending ", pending.getId(), signupPending.getPin());

        response
            .setPendingId(pending.getId());

        return signupPending;
    }

    @WorkflowTask
    void sendMail(UserSignupResponse response, UserSignupRequest request, UserSignupPending pending) {
        String signupConfirmUrl = String.join("/",
            webContext.getRequest().getPaths().getBasePath(),
            webConfiguration.getSignupConfirmUrl());

        // send pending email
        var mailRequest = new MailRequest(request.getEmail())
            .addData("application", getApplication())
            .addData("user", request)
            .addData("pendingId", response.getPendingId())
            .addData("pin", pending.getPin())
            .addData("signupConfirmUrl", signupConfirmUrl);

        log.info("signupConfirmUrl: {0}?pendingId={1}&pin={2}", signupConfirmUrl, response.getPendingId(), pending.getPin());

        log("send mail", request.getEmail());
        mailController.sendMail("userSignup", webContext.getRequest().getLanguage(), mailRequest);
        log("sent mail", request.getEmail());
    }


    @WorkflowTask
    /*
      - find pending
      - validate pin
      - verify password
      - update credential
      - send mail
      - historize pending
    */
    public UserSetPasswordResponse setPassword(UserSetPasswordRequest request) {
        var response = new UserSetPasswordResponse();

        var pending = findPending(response, request.getPendingId());
        if (response.hasFailures()) {
            return finishWorkflow(response);
        }

        var signupPending = validatePending(response, pending, request.getPin());
        if (response.hasFailures()) {
            return finishWorkflow(response);
        }

        validatePassword(response, request.getPassword());

        updateCredential(response, signupPending.getUserId(), request.getPassword());

        sendMail(response);

        historizePending(response, pending);

        return finishWorkflow(response);
    }

    @WorkflowTask
    Pending findPending(UserSetPasswordResponse response, UUID pendingId) {
        var pending = pendingController.findPending(pendingId);

        if (pending == null) {
            response.addFailure("pending.failure", "not-resolved");
            log("pending not found", pendingId);
        }

        return pending;
    }

    @WorkflowTask
    UserResetPasswordPending validatePending(UserSetPasswordResponse response, Pending pending, String pin) {

        if (pending.getKind().getValue() instanceof UserResetPasswordPending resetPending) {

            if (!pin.equals(resetPending.getPin())) {
                response.addFailure("pending.failure", "not-resolved");
                pending.setRetryCount(pending.getRetryCount() + 1);
                log("pin mismatch", pin, pending.getRetryCount());
            }

            return resetPending;
        }

        return null;
    }

    @WorkflowTask
    void validatePassword(UserSetPasswordResponse response, String password) {
        response.addFailure(UserSetPasswordResponse.PASSWORD_FORMAT_FAILURE, webConfiguration.checkPassword(password));
    }

    @WorkflowTask
    void updateCredential(UserSetPasswordResponse response, UUID userId, String password) {
        var credential = credentialController.findCredential(userId);
        em.persist(new CredentialHist(credential, "setPassword"));

        credential.setHash(credentialUtil.createHash(password));
    }

    @WorkflowTask
    @ToDo("implement")
    void sendMail(UserSetPasswordResponse response) {
    }

    @WorkflowTask
    void historizePending(UserSetPasswordResponse response, Pending pending) {
        em.persist(new PendingHist(pending, "setPassword"));

        em.remove(pending);
        log("historized pending", pending.getId());
    }
}