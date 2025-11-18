/* 
 * Converter class responsible convert and format data, so manipulation is possible
 *  from json and also read through chatGPT tree response
 * 
 *
 *  such:
 *  - Convert data to specific object type (such records and classes)
 *  - handle OPENAI response, read through node tree and return model choices
 * 
*/
package com.pacoca.screenmatch.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Converter {
    // creating a translator that knows how to read JSON into Java objects and write Java objects as JSON
    public static ObjectMapper mapper = new ObjectMapper();


    //Convert data to specific object type
    public <T> T dataConverter(String json, Class<T> toConvert) {
        T convertedObject; // result object
        
        try {
            // read json and transform to <toConvert> object
            convertedObject = mapper.readValue(json, toConvert);
        
        } catch (Exception e) {
            // catch and throw runtimeException
            throw new RuntimeException(e);
        }

        // return result object
        return convertedObject;
    }


    // read through json response and return model choices 
    public static String convertFromChatGPT(String json) {
        JsonNode root; // start from the root
        try {
            // read the tree 
            root = mapper.readTree(json);
            // save choices as Node
            JsonNode choices = root.path("choices");

            // if choice is empty or not the data type expected
            if (!choices.isArray() || choices.isEmpty()) {
                return "I don't know what to say, sorry. "; // give an excuse
            }

            // Use path() instead of get() to avoid nulls
            JsonNode contentNode = choices.get(0)
                                        .path("message")
                                        .path("content");

            // return the content, or if empty will return default value
            return contentNode.asText("I don't know what to say, sorry. ") + " .";
        } catch (Exception e) {
            // catch and throw runtimeException
            throw new RuntimeException(e);
        }
    }

}
