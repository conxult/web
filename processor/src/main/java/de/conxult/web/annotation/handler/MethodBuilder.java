/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.annotation.handler;

import de.conxult.web.annotation.WebRpcHeaderParam;
import de.conxult.web.annotation.WebRpcPathParam;
import de.conxult.web.annotation.WebRpcQueryParam;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.lang.model.element.VariableElement;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Getter @Setter @Accessors(chain = true)
class MethodBuilder {

    String          returnTypeName;
    List<Parameter> parameters = new ArrayList<>();

    /**
     * @param parameter
     *
     * <code>addParameter></code> collects <ul>
     * <li> ReST parameter annotation (if given)
     * <li> parameter type
     * <li> parameter name
     *
     * </ul>
     */
    void addParameter(VariableElement parameter) {
        String parameterType = parameter.asType().toString();
        String parameterName = parameter.getSimpleName().toString();

        var headerParam    = parameter.getAnnotation(WebRpcHeaderParam.class);
        var pathParam      = parameter.getAnnotation(WebRpcPathParam.class);
        var queryParam     = parameter.getAnnotation(WebRpcQueryParam.class);

        if (headerParam != null) {
            parameters.add(new Parameter(headerParam, parameterType, parameterName));
        } else if (pathParam != null) {
            parameters.add(new Parameter(pathParam, parameterType, parameterName));
        } else if (queryParam != null) {
            parameters.add(new Parameter(queryParam, parameterType, parameterName));
        } else {
            parameters.add(new Parameter(null, parameterType, parameterName));
        }
    }

    Stream<Parameter> getParameters(Class<? extends Annotation> annotationClass) {
        if (annotationClass == null) {
            return parameters.stream().filter((p) -> p.annotation == null);
        } else {
            return parameters.stream().filter((p) -> p.annotation != null && annotationClass.isAssignableFrom(p.annotation.getClass()));
        }
    }

    List<Parameter> getBodyParameters() {
        return getParameters(null).toList();
    }

    boolean returnsVoid() {
        return returnTypeName.equals("void");
    }

    String getPathParams() {
        StringBuilder pathParams = new StringBuilder();
        getParameters(WebRpcPathParam.class)
            .forEach((p) -> pathParams
                .append("/{")
                .append(getOrDefault(((WebRpcPathParam)p.annotation).value(), p.name))
                .append("}"));
        return pathParams.toString();
    }

    String getOrDefault(String value, String ifEmpty) {
        return (value.isEmpty()) ? ifEmpty : value;
    }

    static class Parameter<A extends Annotation> {
        A      annotation;
        String className;
        String name;

        Parameter(Annotation annotation, String className, String name) {
            this.annotation = (A)annotation;
            this.className = className;
            this.name = name;
        }

    }
}
