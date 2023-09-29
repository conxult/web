/*
 * Copyright by https://conxult.de
 */
package de.conxult.web;

import de.conxult.web.domain.BaseFailureMap;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author joerg
 */

@Singleton
@Getter
public class WebConfiguration {
    public static final String SCHEMA = "web";

    @Setter
    Class serviceStarter = WebServiceStarter.class;

    @ConfigProperty(name = "conxult.web.salt.iterationCount", defaultValue = "10")
    int saltIterationCount = 10;

    @ConfigProperty(name = "conxult.web.jwt.subject")
    String jwtSubject;

    @ConfigProperty(name = "conxult.web.jwt.issuer")
    String jwtIssuer;

    @ConfigProperty(name = "conxult.web.jwt.accessToken.ttl", defaultValue = "300") // 5m: 5*60
    Long jwtAccessTokenTtl = 5*60L;

    @ConfigProperty(name = "conxult.web.jwt.refreshToken.ttl", defaultValue = "86400") // 1d: 24*60*60
    Long jwtRefreshTokenTtl = 24*60*60L;

    @ConfigProperty(name = "conxult.web.formats.eMail")
    String  eMailFormat;
    Pattern eMailPattern;

    @ConfigProperty(name = "conxult.web.formats.nickName")
    String  nickNameFormat;
    Pattern nickNamePattern;

    @ConfigProperty(name = "conxult.web.languages", defaultValue = "en,de,du")
    List<String> languages;

    @ConfigProperty(name = "conxult.web.templates.path", defaultValue = "/templates,/web/templates")
    List<String> templatesPath;

    @ConfigProperty(name = "conxult.web.templates.signupConfirmUrls", defaultValue = "users/signupConfirm")
    List<String> signupConfirmUrls;

    @ConfigProperty(name = "conxult.web.pinSize", defaultValue = "6")
    int    pinSize   = 6;
    int    pinMax    = 999999;
    String pinFormat = "%06d";

    @PostConstruct
    public void initialize() {
        eMailPattern = Pattern.compile(eMailFormat);
        nickNamePattern = Pattern.compile(nickNameFormat);
        pinFormat = "%0" + pinSize + "d";
        pinMax = 9;
        for (int i = 1; (i < pinSize); i++) {
            pinMax = pinMax * 10 + 9;
        }
     }

    public String checkEmail(String eMail) {
        if (eMail == null || eMail.isEmpty()) {
            return BaseFailureMap.EMPTY;
        }
        if (!eMailPattern.matcher(eMail).matches()) {
            return BaseFailureMap.FORMAT;
        }
        return null;
    }

    public String checkNickName(String nickName) {
        if (nickName == null || nickName.isEmpty()) {
            return BaseFailureMap.EMPTY;
        }
        if (!nickNamePattern.matcher(nickName).matches()) {
            return BaseFailureMap.FORMAT;
        }
        return null;
    }

    public String checkPassword(String password) {
        if (password == null || password.isEmpty()) {
            return BaseFailureMap.EMPTY;
        }
        return null;
    }

    public String getDefaultLanguage() {
        return languages.get(0);
    }

    /*

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer = "issuer";

    @ConfigProperty(name = "mp.jwt.subject")
    String subject = "subject";

    @ConfigProperty(name = "mp.jwt.accessToken.ttl", defaultValue = "300") // 5m: 5*60
    Long ttlAccessToken = 5*60L;

    @ConfigProperty(name = "mp.jwt.refreshToken.ttl", defaultValue = "86400") // 1d: 24*60*60
    Long ttlRefreshToken = 24*60*60L;


    */

}
