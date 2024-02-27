/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.boundary;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 *
 * @author joerg
 */
@ApplicationPath(WebApplication.PATH)
public class WebApplication
    extends Application {

    public static final String PATH = "api";

    public interface Mail {
        public static final String PATH = "mails";
    }

    public interface User {
        public static final String PATH = "users";
    }
}
