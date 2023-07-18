/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.control;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

/**
 *
 * @author joerg
 */
@ApplicationScoped
public class EmailController
  extends WebControllerBase {

//  @Override
  public boolean sendEmail(String templateName, Map<String, Object> parameters) {
    return false;
  }

}
