/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import de.conxult.web.annotation.WebRpcController;
import de.conxult.web.annotation.WebRpcMethod;
import de.conxult.web.annotation.WebRpcRolesAllowed;
import de.conxult.web.annotation.WebRpcTransactional;
import de.conxult.web.entity.Role;
import de.conxult.web.entity.User;
import de.conxult.web.entity.UserRole;
import jakarta.enterprise.context.RequestScoped;
import java.util.UUID;

/**
 *
 * @author joerg
 */
@RequestScoped
@WebRpcController(
    className = "de.conxult.web.boundary.RoleWebRpc",
    openapiTag = "Role Boundary",
    path = "roles"
)
public class RoleController
    extends WebController {

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
                .setCreatedBy(webContext.getUserId());
            em.persist(assign);
            return assign;
        }
        return found;
    }

    public User loadRoles(User user) {
        em.createQuery("""
            SELECT r
            FROM Role r
            JOIN UserRole ur on (ur.roleId = r.id)
            WHERE ur.userId = :userId""", Role.class)

            .setParameter("userId", user.getId())
            .getResultStream()
            .map(Role::getName)
            .forEach(roleName -> user.getRoles().add(roleName));

        return user;
    }
}
