/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.conxult.web.annotation.handler;

import de.conxult.annotation.processor.ClassHandler;
import de.conxult.annotation.processor.ConxultAnnotationHandler;
import de.conxult.annotation.processor.MethodHandler;
import de.conxult.util.NameBuilder;
import de.conxult.util.StringUtil;
import de.conxult.web.annotation.WebRpcAnnotation;
import de.conxult.web.annotation.WebRpcController;
import de.conxult.web.annotation.WebRpcDenyAll;
import de.conxult.web.annotation.WebRpcHeaderParam;
import de.conxult.web.annotation.WebRpcImport;
import de.conxult.web.annotation.WebRpcMethod;
import de.conxult.web.annotation.WebRpcPathParam;
import de.conxult.web.annotation.WebRpcPermitAll;
import de.conxult.web.annotation.WebRpcPrincipalParam;
import de.conxult.web.annotation.WebRpcQueryParam;
import de.conxult.web.annotation.WebRpcRolesAllowed;
import de.conxult.web.annotation.WebRpcTransactional;
import io.quarkus.arc.Unremovable;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import java.lang.annotation.Annotation;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaClassSource;

/**
 *
 * @author joerg
 */
public class WebAnnotationHandler
    extends ConxultAnnotationHandler {

    Map<String, JavaClassSource>        javaClasses = new HashMap<>();
    Map<String, Map<Class, Annotation>> javaClassesAnnotations = new HashMap<>();

    @ClassHandler
    public void handleRcpController(WebRpcController webRpcController, TypeElement controllerType) {
        String controllerClassName = controllerType.asType().toString();
        String webRpcClassName     = determineWebRpcClassName(controllerClassName, webRpcController.className());

        var javaClass = javaClasses.computeIfAbsent(controllerClassName, (key) -> createClass(webRpcClassName));
        collectAnnotationsOnClass(
            javaClassesAnnotations.computeIfAbsent(controllerClassName, (key) -> new HashMap()),
            controllerType,
            WebRpcPermitAll.class,
            WebRpcRolesAllowed.class,
            WebRpcDenyAll.class,
            WebRpcController.class
        );

        log.info("handle {0}", webRpcController);
        log.info("  for {0} to {1}", controllerClassName, webRpcClassName);

        addImports(javaClass, controllerType.getAnnotationsByType(WebRpcImport.class));

        javaClass.addAnnotation(Generated.class)
            .setStringValue("value", getClass().getName())
            .setStringValue("date", OffsetDateTime.now().toString())
            .setStringValue("comments", "from: " + controllerClassName + ", with: " + webRpcController);

        addAnnotations(javaClass, controllerType.getAnnotationsByType(WebRpcAnnotation.class));

        javaClass.addAnnotation(Tag.class)
            .setStringValue("name", webRpcController.openapiTag())
            .setStringValue("description", webRpcController.description());
        javaClass.addAnnotation(Path.class)
            .setStringValue(webRpcController.pathIsPrefix() ? "" : webRpcController.path());
        javaClass.addAnnotation(RequestScoped.class);

        javaClass.setPublic();

        if (webRpcController.fillWebRpcPrincipal()) {
            javaClass.addInterface("de.conxult.web.boundary.WebRpcPrincipalBoundary");

            javaClass.addField()
               .setType(HttpHeaders.class)
               .setName("httpHeaders")
               .addAnnotation(Context.class);
            javaClass.addMethod()
                .setReturnType(HttpHeaders.class)
                .setName("getHttpHeaders")
                .setPublic()
                .setBody("return httpHeaders;");

            javaClass.addField()
               .setType(SecurityContext.class)
               .setName("securityContext")
               .addAnnotation(Context.class);
            javaClass.addMethod()
                .setReturnType(SecurityContext.class)
                .setName("getSecurityContext")
                .setPublic()
                .setBody("return securityContext;");
        }

        var controllerField = javaClass.addField()
            .setType(controllerClassName)
            .setName("controller")
            .addAnnotation(Inject.class);
    }

    @MethodHandler
    public void handleRcpMethod(WebRpcMethod webRpcMethod, ExecutableElement controllerMethod) {
        String className   = controllerMethod.getEnclosingElement().asType().toString();
        String methodName  = controllerMethod.getSimpleName().toString();
        String path        = (!webRpcMethod.path().isEmpty()) ? webRpcMethod.path() : methodName;

        if (javaClassesAnnotations.get(className).get(WebRpcController.class) instanceof WebRpcController webRpcController) {
            if (webRpcController.pathIsPrefix()) {
                path = webRpcController.path() + path;
            }
        }

        log.info("handle {0}", webRpcMethod);
        log.info("  for {0}.{1}", className, methodName);

        var methodBuilder = new MethodBuilder()
            .setReturnTypeName(controllerMethod.getReturnType().toString());

        controllerMethod.getParameters().forEach(methodBuilder::addParameter);

        var javaClass = javaClasses.get(className);
        var javaMethod = javaClass.addMethod()
            .setReturnType(methodBuilder.getReturnTypeName())
            .setName(methodName)
            .setPublic();

        javaMethod.addAnnotation(Generated.class)
            .setStringValue("value", className + "." + methodName)
            .setStringValue("comments", "from: " + methodName + ", with: " + webRpcMethod);


        methodBuilder.getParameters(WebRpcHeaderParam.class).forEach((p) -> {
            WebRpcHeaderParam param = (WebRpcHeaderParam)p.annotation;
            javaMethod
                .addParameter(p.className, p.name)
                .addAnnotation(HeaderParam.class).setStringValue(methodBuilder.getOrDefault(param.value(), p.name));
        });
        methodBuilder.getParameters(WebRpcPathParam.class).forEach((p) -> {
            WebRpcPathParam param = (WebRpcPathParam)p.annotation;
            javaMethod
                .addParameter(p.className, p.name)
                .addAnnotation(PathParam.class).setStringValue(methodBuilder.getOrDefault(param.value(), p.name));
        });
        methodBuilder.getParameters(WebRpcQueryParam.class).forEach((p) -> {
            WebRpcQueryParam param = (WebRpcQueryParam)p.annotation;
            javaMethod
                .addParameter(p.className, p.name)
                .addAnnotation(QueryParam.class).setStringValue(methodBuilder.getOrDefault(param.value(), p.name));
        });
        methodBuilder.getParameters(WebRpcPrincipalParam.class).forEach((p) -> {
            WebRpcPrincipalParam param = (WebRpcPrincipalParam)p.annotation;
//            javaMethod
//                .addParameter(p.className, p.name)
//                .addAnnotation(QueryParam.class).setStringValue(methodBuilder.getOrDefault(param.value(), p.name));
        });
        var bodyParameters = methodBuilder.getBodyParameters();
        if (bodyParameters.size() == 1) {
            javaMethod.addParameter(bodyParameters.get(0).className, bodyParameters.get(0).name);
        } else if (bodyParameters.size() > 1) {
            String bodyClassName = "BodyParamFor" + StringUtil.firstUpper(methodName);
            JavaClassSource javaBodyClass = javaClass.addNestedType("static public class " + bodyClassName + "{}");
            javaBodyClass
                .addAnnotation(Unremovable.class);
            bodyParameters.forEach((p) -> {
                javaBodyClass.addField()
                    .setPublic()
                    .setType(p.className)
                    .setName(p.name);
                p.name = "body." + p.name;
            });
            javaMethod.addParameter(bodyClassName, "body");
        }

        javaMethod.addAnnotation(POST.class);
        javaMethod.addAnnotation(Path.class)
            .setStringValue(path + methodBuilder.getPathParams());
        javaMethod.addAnnotation(Operation.class)
            .setStringValue("summary", webRpcMethod.description());
        if (controllerMethod.getAnnotation(WebRpcTransactional.class) != null) {
            javaMethod.addAnnotation(Transactional.class);
        }
        if (controllerMethod.getAnnotation(WebRpcPermitAll.class) != null) {
            javaMethod.addAnnotation(PermitAll.class);
        }
        if (controllerMethod.getAnnotation(WebRpcRolesAllowed.class) != null) {
            javaMethod.addAnnotation(RolesAllowed.class)
                .setStringArrayValue(controllerMethod.getAnnotation(WebRpcRolesAllowed.class).value());
        }

        StringBuilder body = new StringBuilder();
        if (!methodBuilder.returnsVoid()) {
            body.append("return ");
        }
        body.append("controller." + methodName + (methodBuilder.getParameters().isEmpty() ? "(" : ""));
        String delim = "(";
        for (MethodBuilder.Parameter parameter : methodBuilder.getParameters()) {
            body
                .append(delim)
                .append(parameter.name);
            delim = ",";
        }
        body.append(");\n");

        javaMethod
            .setBody(body.toString());
    }

    String determineWebRpcClassName(String controllerClassName, String template) {

        if (template.startsWith("..")) {
            NameBuilder nameBuilder = new NameBuilder()
                .add(controllerClassName.split("\\."))
                .removeLast();
            while (template.startsWith("..")) {
                nameBuilder.removeLast();
                template = template.substring(2);
            }

            return nameBuilder
                .add(template.split("\\."))
                .join(".");
        }

        return template;
    }

    void collectAnnotationsOnClass(Map<Class, Annotation> annotations, TypeElement controllerClass, Class<? extends Annotation>... annotationClasses) {
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            var annotation = controllerClass.getAnnotation(annotationClass);
            if (annotation != null) {
                annotations.put(annotationClass, annotation);
            }
        }
    }

    void addImports(JavaClassSource javaClass, WebRpcImport[] imports) {
        for (WebRpcImport webRpcImport : imports) {
            String importClassName = webRpcImport.toString().substring(1 + WebRpcImport.class.getName().length() + 1);
            importClassName = importClassName.substring(0, importClassName.length() - 7);
            javaClass.addImport(importClassName);
        }
    }

    void addAnnotations(JavaClassSource javaClass, WebRpcAnnotation[] annotations) {
        for (WebRpcAnnotation webRpcAnnotation : annotations) {
            String value = webRpcAnnotation.value();
            String annotationClassName = value.split("\\(")[0].substring(1);

            if (!annotationClassName.contains(".")) {
                // find full qualified class name in imports
                for (Import javaImport : javaClass.getImports()) {
                    if (javaImport.getSimpleName().equals(annotationClassName)) {
                        annotationClassName = javaImport.getQualifiedName();
                    }
                }
            }

            if (value.contains("(")) {
                javaClass
                    .addAnnotation(annotationClassName)
                    .setLiteralValue(value.substring(0, value.length() - 1).substring(value.indexOf("(") + 1));
            } else {
                // no literal
                javaClass
                    .addAnnotation(annotationClassName);
            }

        }

    }

}

/*
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(
    name = "configurations",
    description = "Configurations")
@Path("")
@RolesAllowed({VfServiceRoles.MANAGER, VfServiceRoles.USER})
public interface ConfigurationApi
    extends VfServiceRoles {

    @GET
    @Path("configurations")
    @Operation(summary = "Find configuration items")
    public List<ConfigurationItemDTO> findConfigurations(
        @BeanParam
        ConfigurationFilter searchFilter
    );

    @GET
    @Path("configurations/{type}/{typeId}/{configurationId}")
    @Operation(summary = "Get an entity configuration")
    public ConfigurationItemDTO getConfiguration(

*/