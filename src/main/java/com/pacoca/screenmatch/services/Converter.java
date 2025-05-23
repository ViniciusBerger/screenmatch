package com.pacoca.screenmatch.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Converter {
    public static ObjectMapper mapper = new ObjectMapper();



    public <T> T dataConverter(String json, Class<T> toConvert) {
        T convertedObject;
        try {
            convertedObject = mapper.readValue(json, toConvert);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return convertedObject;
    }

    public static String convertFromChatGPT(String json) {
        JsonNode root;
        try {
            root = mapper.readTree(json);
            System.out.println("GPT raw response:\n" + root.toString());
            JsonNode choices = root.path("choices");


            if (!choices.isArray() || choices.isEmpty())
            {
                return "I don't know what to say, sorry. ";
            }


             JsonNode contentNode = choices.get(0).get("message").get("content");
            return contentNode.asText("I don't know what to say, sorry. ") + " .";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
