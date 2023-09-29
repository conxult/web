/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.websocket;

import jakarta.inject.Singleton;
import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author joerg
 */
@Singleton
public class WebSocketSessionCache {

    static Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public WebSocketSession getWebSocketSession(Session session) {
        return sessions.get(session.getId());
    }

    public void onOpen(WebSocketSession webSocketSession) {
        sessions.put(webSocketSession.getSession().getId(), webSocketSession);
    }

    public WebSocketSession onClose(Session session) {
        return sessions.remove(session.getId());
    }

    public WebSocketSession onError(Session session) {
        return sessions.remove(session.getId());
    }

}
