/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.util;

import de.conxult.log.Log;
import de.conxult.web.WebConfiguration;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author joerg
 */
@Singleton
public class TemplateCache {

    @Inject
    Log log;

    @Inject
    WebConfiguration webConfiguration;

    @Inject
    Engine engine;

    Map<String, Template> templateCache = new HashMap<>();

    public TemplateInstance getTemplate(String language, Class<?> clazz, String name) {
        return getTemplate(language, clazz.getSimpleName(), name);
    }

    public TemplateInstance getTemplate(String language, String... path) {
        return getTemplate(language, String.join("/", path));
    }

    public TemplateInstance getTemplate(String language, String path) {
        return templateCache.computeIfAbsent(language + ":" + path, (key) -> loadTemplate(language, path)).instance();
    }

    InputStream getResourceAsStream(String resource) {
        return webConfiguration.getServiceStarter().getResourceAsStream(resource);
    }

    Template loadTemplate(String language, String path) {
        final int lastDot = path.lastIndexOf('.');
        String extension = path.substring(lastDot);
        path = path.substring(0, lastDot);
        for (String templatePath : webConfiguration.getTemplatesPath()) {
            var resource = templatePath + "/" + path + "/" + language + extension;
            var templateStream = getResourceAsStream(resource);
            if (templateStream == null) {
                log.info("not found in %s", resource);
                resource = templatePath + "/" + path + extension;
                templateStream = getResourceAsStream(resource);
            }
            if (templateStream == null) {
                log.info("not found in %s", resource);
                resource = templatePath + "/" + path + "/" + webConfiguration.getDefaultLanguage() + extension;
                templateStream = getResourceAsStream(resource);
            }
            if (templateStream == null) {
                log.info("not found in %s", resource);
            } else {
                log.info("found in %s", resource);
                StringBuilder content = new StringBuilder();
                try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(templateStream, "utf-8"));
                ) {
                    for (String line = reader.readLine(); (line != null); line = reader.readLine()) {
                        content.append(line).append("\n");
                    }
                    return engine.parse(content.toString());
                } catch (IOException ioException) {
                    log.error(ioException, "reading %s failed", resource);
                }
            }
        }

        return engine.parse(String.format("template for %s in %s not found", path + extension, language));
    }

//Template hello = engine.parse(helloHtmlContent);

}
