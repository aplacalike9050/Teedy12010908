package com.sismics.docs.rest.util;



import jakarta.websocket.Session;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomManager {

    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> chatRooms = new ConcurrentHashMap<>();

    public static void addUser(String username, Session session) {
        userSessions.put(username, session);
    }

    public static void removeUser(String username) {
        userSessions.remove(username);
    }

    public static void handleMessage(String fromUser, String message) {
        Map<String, String> data = JsonUtil.parseJson(message);
        if (data == null) return;

        String action = data.get("action");

        try {
            if ("create".equals(action)) {
                String toUser = data.get("to");
                String roomId = UUID.randomUUID().toString();
                chatRooms.put(roomId, Arrays.asList(fromUser, toUser));

                sendTo(fromUser, JsonUtil.buildRoomMsg(roomId, toUser));
                sendTo(toUser, JsonUtil.buildRoomMsg(roomId, fromUser));

            } else if ("chat".equals(action)) {
                String roomId = data.get("roomId");
                String msg = data.get("msg");

                List<String> members = chatRooms.get(roomId);
                if (members != null) {
                    for (String user : members) {
                        sendTo(user, JsonUtil.buildChatMsg(fromUser, msg));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("发送消息失败：" + e.getMessage());
        }
    }

    private static void sendTo(String username, String msg) throws IOException {
        Session session = userSessions.get(username);
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(msg);
        }
    }
}
