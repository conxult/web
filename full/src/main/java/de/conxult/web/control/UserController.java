/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.pa.JPQL;
import de.conxult.web.context.RequestContext;
import de.conxult.web.domain.BaseFailureMap;
import de.conxult.web.domain.SignupRequest;
import de.conxult.web.domain.SignupResponse;
import de.conxult.web.domain.UserState;
import de.conxult.web.entity.Credential;
import de.conxult.web.entity.Pending;
import de.conxult.web.entity.Role;
import de.conxult.web.entity.User;
import de.conxult.web.entity.UserRole;
import de.conxult.web.util.CredentialUtil;
import de.conxult.web.util.FormatChecker;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author joerg
 */
@RequestScoped
public class UserController
  extends WebControllerBase {

  @Inject
  FormatChecker formatChecker;

  @Inject
  CredentialUtil credentialUtil;

  @Inject
  PendingController pendingController;

  public User find(UUID userId) {
    return (userId == null) ? null : em.find(User.class, userId);
  }

  public User createUser(User user, String credential) {
    em.persist(user
      .setCreatedBy(RequestContext.getCurrentUser()));
    em.persist(new Credential()
      .setUserId(user.getId())
      .setCreatedBy(RequestContext.getCurrentUser())
      .setHash(credentialUtil.createHash(credential))
      .setState("created"));
    return user;
  }

  public Role createRole(Role role) {
    em.persist(role
      .setCreatedBy(RequestContext.getCurrentUser()));
    return role;
  }

  public Role findRole(String name) {
    return em.createQuery("select r from Role r where name = :name", Role.class)
      .setParameter("name", name)
      .getResultStream()
      .findFirst().orElse(null);
  }

  public UserRole assignRole(User user, Role role) {
    UserRole found = em.createQuery("select ar from UserRole ar where ar.userId = ?1 and ar.roleId = ?2", UserRole.class)
      .setParameter(1, user.getId())
      .setParameter(2, role.getId())
      .getResultStream()
      .findFirst().orElse(null);

    if (found == null) {
      UserRole assign = new UserRole()
        .setUserId(user.getId())
        .setRoleId(role.getId())
        .setCreatedBy(RequestContext.getCurrentUser());
      em.persist(assign);
      return assign;
    }
    return found;
  }

  //@Override // UserLoginController
  public User loginUser(String email, String credential) {
    List<User> users = em.createQuery("select a from User a where lower(a.email) = ?1 and a.state = ?2")
      .setParameter(1, email.toLowerCase())
      .setParameter(2, UserState.ACTIVE.name())
      .getResultList();

    if (users.size() == 1) {
      User user = users.get(0);
      List<Credential> credentials = em.createQuery("select c from Credential c where c.id = ?1")
        .setParameter(1, user.getId())
        .getResultList();

      if (credentials.size() == 1 &&
        credentialUtil.validateHash(credentials.get(0).getHash(), credential)) {

        loadRoles(user);

        return user;
      }
    }

    return null;
  }

  public User verifyUser(UUID userId) {
    return (userId == null) ? null : loadRoles(em.find(User.class, userId));
  }

  public SignupResponse signupUser(SignupRequest signupRequest) {
    SignupResponse result = new SignupResponse();

    return signupCheckUser(result, signupRequest);
  }

  SignupResponse signupCheckUser(SignupResponse result, SignupRequest signupRequest) {

    // verify email
    result.setFailure("email", formatChecker.checkEmail(signupRequest.getEmail()));
    if (!result.hasFailures()) {
      List<User> users = em.createQuery("select a from User a where lower(a.email) = ?1")
        .setParameter(1, signupRequest.getEmail().toLowerCase())
        .getResultList();
      if (!users.isEmpty()) {
        result.setFailure("email", BaseFailureMap.UNAVAILABLE);
      }
    }

    // verify password
    result.setFailure("password", formatChecker.checkPassword(signupRequest.getPassword(), signupRequest.getPasswordConfirm()));

    // verify nickName
    result.setFailure("nickName", formatChecker.checkNickName(signupRequest.getNickName()));

    // verify licenseCheck
    if (!signupRequest.isAcceptLicense()) {
      result.setFailure("license", BaseFailureMap.LICENSE);
    }

    if (result.hasFailures()) {
      return result;
    }

    return signupCreateUser(result, signupRequest);
  }

  SignupResponse signupCreateUser(SignupResponse result, SignupRequest signupRequest) {
    // create user
    User newUser = new User()
      .setCreatedBy(RequestContext.getCurrentUser())
      .setEmail(signupRequest.getEmail())
      .setNickName(signupRequest.getNickName())
      .setLastName(signupRequest.getLastName())
      .setFirstName(signupRequest.getFirstName())
      .setState(UserState.UNCONFIRMED.name());
    newUser = createUser(newUser, signupRequest.getPassword());

    // assign Role USER
    assignRole(newUser, findRole(Role.USER));

    // create pending
    Pending pending = pendingController.createPending(newUser.getId(), UserState.UNCONFIRMED);
    result.setPendingId(pending.getId());

    return signupSendEMail(result, pending, newUser);
  }

  SignupResponse signupSendEMail(SignupResponse result, Pending pending, User newUser) {
    return result;
  }

  public <T> TypedQuery<T> select(JPQL jpql, Class<T> type) {
    return jpql.setQueryParameters(em.createQuery(jpql.getJpql(), type));
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
}
