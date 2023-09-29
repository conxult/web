package de.conxult.web;


import io.quarkus.runtime.Quarkus;

/*
 * Copyright by https://conxult.de
 */

public class CxWebMain {

    public static void main(String... args) {
        System.setProperty("VF_SERVICE_STARTUP_DELAY_MS", "0");
        System.setProperty("VF_SERVICE_STARTUP_INIT_AND_RUN", "true");
        Quarkus.run(args);
    }

}
