package org.example;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        var json ="""
    {
      "k1": "v1",
      "k2": "v2"
    }
    """;
        JsonObject jsonObject = Json.createReader(new java.io.StringReader(json)).readObject();
        System.out.println(jsonObject.getString("k1"));
    }

}
