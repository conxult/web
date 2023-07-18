/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.conxult.util.ClassCache;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author joerg
 *
 * Deriving classes should have the following annotations:
 * @ServerEndpoint("/some/endpoint")
 * @ApplicationScoped
 */

abstract public class CommandWebSocket {

    @Inject
    ObjectMapper objectMapper;

    Map<String, Method> commandMethods = new HashMap<>();

    @Inject
    WebSocketSessionCache sessionCache;

    abstract WebSocketSession createWebSocketSession();

    @PostConstruct
    public void collectCommandMethods() {
        ClassCache.instanceOf(getClass())
            .getMethods(OnCommand.class)
            .forEach((m) -> {
                m.setAccessible(true);
                for (String command : m.getAnnotation(OnCommand.class).value()) {
                    commandMethods.put(command, m);
                }
            });
    }

    @OnOpen
    public void onOpen(Session session) {
        WebSocketSession wss = (WebSocketSession)createWebSocketSession().setSession(session);
        sessionCache.onOpen(wss);
        onOpen(wss);
    }

    protected void onOpen(WebSocketSession webSocketSession) {
    }

    @OnClose
    public void onClose(Session session) {
        WebSocketSession webSocketSession = sessionCache.onClose(session);
        onClose(webSocketSession);
    }

    protected void onClose(WebSocketSession webSocketSession) {
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        WebSocketSession webSocketSession = sessionCache.onError(session);
        onError(webSocketSession);
    }

    protected void onError(WebSocketSession webSocketSession) {
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            if (jsonNode.isObject()) {
                jsonNode.fields().forEachRemaining((fieldEntry) ->
                    invokeCommand(session, fieldEntry.getKey(), fieldEntry.getValue())
                );
            }

        } catch (JsonProcessingException jpe) {
            //
        }
    }

    void invokeCommand(Session session, String command, JsonNode value) {
        Method method = commandMethods.get(command);
        if (method != null) {
            try {
                Parameter[] parameters = method.getParameters();
                Object[] arguments = new Object[parameters.length];
                for (int i = 0; (i < parameters.length); i++) {
                    Class<?> parameterType = parameters[i].getType();
                    if (WebSocketSession.class.isAssignableFrom(parameterType)) {
                        arguments[i] = sessionCache.getWebSocketSession(session);
                    } else {
                        arguments[i] = objectMapper.treeToValue(value, parameterType);
                    }
                }
                method.invoke(this, arguments);
            } catch (JsonProcessingException jsonProcessingException) {
            } catch (ReflectiveOperationException reflectiveOperationException ) {
            }
        }
    }

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.METHOD})
    public static @interface OnCommand {
        String[] value();
    }

}

