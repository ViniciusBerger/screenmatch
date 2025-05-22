package com.pacoca.screenmatch.services;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Converter {
    public ObjectMapper mapper = new ObjectMapper();



    public <T> T dataConverter(String json, Class<T> toConvert) {
        T convertedObject;
        try {
            convertedObject = mapper.readValue(json, toConvert);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return convertedObject;
    }
}
