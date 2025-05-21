package com.sismics.docs.rest.resource;

import com.fasterxml.jackson.databind.ObjectMapper;


import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/chat")
public class ChatEndpoint {

    // 在线用户 WebSocket 会话
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();

    // 聊天历史记录，key 为 user1_user2 形式（按字典序）
    private static final Map<String, List<String>> chatHistory = new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String getChatKey(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    @OnOpen
    public void onOpen(Session session) {
        String query = session.getQueryString();
        if (query != null && query.contains("from=")) {
            String currentUser = query.replaceAll(".*from=([^&]*).*", "$1");
            session.getUserProperties().put("username", currentUser);
            userSessions.put(currentUser, session);
            System.out.println("用户连接: " + currentUser);
        } else {
            System.out.println("连接中缺少参数");
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "缺少from参数"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        Map<String, Object> msgMap = objectMapper.readValue(message, Map.class);
        String fromUser = (String) session.getUserProperties().get("username");
        String toUser = (String) msgMap.get("to");
        String type = (String) msgMap.get("type");

        if ("history".equals(type)) {
            // 拉取历史记录
            String chatKey = getChatKey(fromUser, toUser);
            List<String> history = chatHistory.getOrDefault(chatKey, new ArrayList<>());
            for (String msg : history) {
                session.getAsyncRemote().sendText(msg);
            }
            return;
        }

        // 保存聊天记录
        String chatKey = getChatKey(fromUser, toUser);
        chatHistory.computeIfAbsent(chatKey, k -> new ArrayList<>()).add(message);

        // 发送消息
        Session targetSession = userSessions.get(toUser);
        if (targetSession != null && targetSession.isOpen()) {
            targetSession.getAsyncRemote().sendText(message);
        } else {
            System.out.println("用户 " + toUser + " 不在线，消息保存到历史记录");
        }
    }

    @OnClose
    public void onClose(Session session) {
        String currentUser = (String) session.getUserProperties().get("username");
        if (currentUser != null) {
            userSessions.remove(currentUser);
            System.out.println("连接关闭: " + currentUser);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket 错误: " + throwable.getMessage());
    }
}
