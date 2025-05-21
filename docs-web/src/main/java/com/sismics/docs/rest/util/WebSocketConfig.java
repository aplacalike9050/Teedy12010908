package com.sismics.docs.rest.util;



import com.sismics.docs.rest.resource.ChatEndpoint;


import jakarta.websocket.DeploymentException;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.websocket.server.ServerContainer;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;


public class WebSocketConfig implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServerContainer container =
                (ServerContainer) sce.getServletContext().getAttribute("jakarta.websocket.server.ServerContainer");
        System.out.println("ServerContainer: " + container);
        try {
            container.addEndpoint(ChatEndpoint.class);
        } catch (Exception e) {
            throw new RuntimeException("WebSocket 注册失败", e);
        }
    }
}
