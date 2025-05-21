package com.sismics.docs.rest.util;

import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static Map<String, String> parseJson(String json) {
        try {
            Map<String, String> result = new HashMap<>();
            json = json.replaceAll("[{}\"]", "");
            String[] entries = json.split(",");
            for (String entry : entries) {
                String[] kv = entry.split(":", 2);
                if (kv.length == 2) result.put(kv[0].trim(), kv[1].trim());
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static String buildRoomMsg(String roomId, String partner) {
        return String.format("{\"type\":\"room\",\"roomId\":\"%s\",\"partner\":\"%s\"}", roomId, partner);
    }

    public static String buildChatMsg(String from, String msg) {
        return String.format("{\"type\":\"chat\",\"from\":\"%s\",\"msg\":\"%s\"}", from, msg);
    }
}
