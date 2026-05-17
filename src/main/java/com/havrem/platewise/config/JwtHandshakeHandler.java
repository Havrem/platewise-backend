package com.havrem.platewise.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class JwtHandshakeHandler extends DefaultHandshakeHandler {
    static final String USER_EMAIL_ATTRIBUTE = "wsUserEmail";

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Object email = attributes.get(USER_EMAIL_ATTRIBUTE);
        if (email instanceof String userEmail) {
            return () -> userEmail;
        }
        throw new HandshakeFailureException("Missing authenticated websocket user");
    }
}
