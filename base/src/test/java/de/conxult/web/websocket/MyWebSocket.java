/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.websocket;

import jakarta.enterprise.context.Dependent;
import jakarta.websocket.server.ServerEndpoint;

/**
 *
 * @author joerg
 */
@ServerEndpoint("/my/websocket")
@Dependent
public class MyWebSocket
    extends CommandWebSocket {

    @Override
    WebSocketSession createWebSocketSession() {
        return new WebSocketSession();
    }

    @OnCommand("authorize")
    public void authorize(AuthorizeCommand command) {
    }

    @OnCommand("authorize")
    public void authorize(WebSocketSession session, AuthorizeCommand command) {
    }

    @OnCommand("authorize")
    public void authorize(AuthorizeCommand command, WebSocketSession session) {
    }


}
