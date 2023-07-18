/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.util;

import de.conxult.web.domain.BaseFailureMap;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.regex.Pattern;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author joerg
 */
@ApplicationScoped
public class FormatChecker {

  @ConfigProperty(name = "conxult.web.formats.eMail")
  String  eMailFormat;
  Pattern eMailPattern;

  @ConfigProperty(name = "conxult.web.formats.nickName")
  String  nickNameFormat;
  Pattern nickNamePattern;

  @PostConstruct
  public void parseFormats() {
    eMailPattern = Pattern.compile(eMailFormat);
    nickNamePattern = Pattern.compile(nickNameFormat);
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

  public String checkPassword(String password, String passwordConfirm) {
    if (password == null || password.isEmpty()) {
      return BaseFailureMap.EMPTY;
    }
    if (!password.equals(passwordConfirm)) {
      return BaseFailureMap.MISMATCH;
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

}
