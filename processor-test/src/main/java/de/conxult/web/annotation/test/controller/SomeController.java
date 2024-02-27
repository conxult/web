/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.conxult.web.annotation.test.controller;

import de.conxult.web.annotation.WebRpcController;
import de.conxult.web.annotation.WebRpcHeaderParam;
import de.conxult.web.annotation.WebRpcMethod;
import de.conxult.web.annotation.WebRpcPathParam;
import de.conxult.web.annotation.WebRpcPermitAll;
import de.conxult.web.annotation.WebRpcQueryParam;
import de.conxult.web.annotation.WebRpcRolesAllowed;
import de.conxult.web.annotation.WebRpcTransactional;
import de.conxult.web.entity.User;
import jakarta.enterprise.context.Dependent;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author joerg
 */
@WebRpcController(
    path = "somes.",

    description = "Some Test Controller",
    openapiTag = "Some Boundary",

    className = "de.conxult.web.annotation.test.boundary.SomeWebRpc"
)
@Dependent
public class SomeController {

    public static final String HEADER_1 = "X-Header-1";
    public static final String HEADER_2 = "X-Header-2";

    public static final String ROLE_ADMIN       = "admin";
    public static final String ROLE_DELETE_SOME = "delete-some";

    @WebRpcMethod(
        description = "transactional"
    )
    @WebRpcTransactional
    public Some create(Some newSome) {
        return newSome;
    }

    @WebRpcMethod(
        description = "without return value"
    )
    public void doSomething() {
    }

    @WebRpcMethod(
        description = "with single parameter"
    )
    public User find(UUID id) {
        return new User().setId(id).setNickName(id.toString());
    }

    @WebRpcMethod(
        description = "with multiple parameters"
    )
    public void assign(UUID userId, List<UUID> projectIds) {
    }

    @WebRpcMethod(
        description = "with roles"
    )
    @WebRpcRolesAllowed({ ROLE_ADMIN, ROLE_DELETE_SOME })
    public void delete(Some some) {
    }

    @WebRpcMethod(
        description = "for all roles"
    )
    @WebRpcPermitAll
    public double getTemperature(String location) {
        return 15 + Math.random()*10;
    }

    @WebRpcMethod(
        description = "with header parameters",
        useGET = true
    )
    public List<String> withHeaderParameter(
        @WebRpcHeaderParam(HEADER_1) String header1,
        @WebRpcHeaderParam(HEADER_2) String header2
    ) {
        return List.of(header1, header2);
    }

    @WebRpcMethod(
        description = "with path parameters"
    )
    public List<String> withPathParameter(
        @WebRpcPathParam("path") String path1,
        @WebRpcPathParam()       String path2
    ) {
        return List.of(path1, path2);
    }

    @WebRpcMethod(
        description = "with query parameters"
    )
    public List<String> withQueryParameter(
        @WebRpcQueryParam("query") String query1,
        @WebRpcQueryParam()        String query2
    ) {
        return List.of(query1, query2);
    }

}
