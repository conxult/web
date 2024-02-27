/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.domain;

import de.conxult.web.boundary.WebApplication;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.core.UriInfo;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Dependent
public class WebContext {

    private final static ThreadLocal<Map<String, Object>> data = ThreadLocal.withInitial(HashMap::new);

    public WebContext clean() {
        data.get().clear();
        return this;
    }

    public Jwt getJwt() {
        return (Jwt)data.get().computeIfAbsent("jwt", (key) -> new Jwt());
    }

    public User getUser() {
        return (User)data.get().computeIfAbsent("user", (key) -> new User());
    }

    public Request getRequest() {
        return (Request)data.get().computeIfAbsent("request", (key) -> new Request());
    }

    public UUID getUserId() {
        return getUser().getId();
    }

    @Getter @Setter @Accessors(chain = true)
    @Unremovable
    static public class Jwt {

        String              rawToken;
        String              issuer;
        Set<String>         audience;
        String              subject;
        String              name;
        OffsetDateTime      issuedAt;
        OffsetDateTime      expirationTime;
        Set<String>         groups;
        Map<String, Object> claims = new HashMap<>();

        public boolean isFilled() {
            return rawToken != null && !rawToken.isEmpty();
        }

        public <T> T getClaim(Enum claim) {
            return getClaim(claim.name());
        }

        public <T> T getClaim(String claimName) {
            return (T) getClaims().get(claimName);
        }

        public Map<String, Object> toMap() {
            return !isFilled() ? Map.of() : new MapBuilder()
                .add("rawToken"      , rawToken)
                .add("issuer"        , issuer)
                .add("audience"      , audience)
                .add("subject"       , subject)
                .add("name"          , name)
                .add("issuedAt"      , issuedAt)
                .add("expirationTime", expirationTime)
                .add("groups"        , groups)
                .add("claims"        , claims);
        }

    }

    @Getter @Setter @Accessors(chain = true)
    @Unremovable
    static public class User {

        UUID   id;
        UUID   tenantId;
        Locale locale;
        String userName;
        String familyName;
        String surName;

        public boolean isFilled() {
            return id != null;
        }

        public Map<String, Object> toMap() {
            return !isFilled() ? Map.of() : new MapBuilder()
                .add("id"        , id)
                .add("tenantId"  , tenantId)
                .add("locale"    , locale)
                .add("userName"  , userName)
                .add("familyName", familyName)
                .add("surName"   , surName);
        }
    }

    @Getter @Setter @Accessors(chain = true)
    @Unremovable
    static public class Request {

        Paths               paths;
        String              method;
        Map<String, String> headers = new HashMap<>();

        Locale              locale;
        String              language;

        public Request setLanguage(List<String> languages) {
            language = (locale != null) ? locale.getLanguage() : languages.get(0);
            return this;
        }

        public Request setPaths(UriInfo uriInfo) {
            paths = new Paths(uriInfo.getAbsolutePath().toString());
            return this;
        }

        public Map<String, Object> toMap() {
            return new MapBuilder()
                .add("paths"   , (paths == null) ? Map.of() : paths.toMap())
                .add("methods" , method)
                .add("headers" , headers)
                .add("locale"  , locale)
                .add("language", language);
        }

        @Getter @Setter @Accessors(chain = true)
        public static class Paths {

            static String API = "/" + WebApplication.PATH + "/";

            String basePath;
            String apiPath;
            String fullPath;
            String method;

            Paths(String absolutePath) {
                basePath = absolutePath.substring(0, absolutePath.indexOf(API));
                apiPath = absolutePath.substring(0, absolutePath.indexOf(API) + API.length() -1);
                fullPath = absolutePath;
                method = absolutePath.substring(apiPath.length());
            }

            public Map<String, Object> toMap() {
                return new MapBuilder()
                    .add("basePath", basePath)
                    .add("apiPath" , apiPath)
                    .add("fullPath", fullPath)
                    .add("method"  , method);
            }
        }
    }

    static class MapBuilder extends TreeMap<String, Object> {
        public MapBuilder add(String key, Object value) {
            put(key, value);
            return this;
        }
    }

}

