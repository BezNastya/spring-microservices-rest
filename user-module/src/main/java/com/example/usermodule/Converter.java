package com.example.usermodule;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Converter {

    private final static String baseFile = "user.text";

    public static void toJSON(String id, Object test) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if(!id.isBlank() && id != null){
            String fileName = "user"+id+".text";
            mapper.writeValue(new File(fileName), test);
            System.out.println("file created!");
        }
    }

    public static Object toJavaObject(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(fileName), Object.class);
    }

}