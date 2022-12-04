package fr.insee.test;

import jakarta.json.Json;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class JsonStream {

    public static String process(@NonNull String emptyJson) {
        var outputStream= new ByteArrayOutputStream();
        try (var jsonParser = Json.createParser(
                new ByteArrayInputStream(emptyJson.getBytes(StandardCharsets.UTF_8)));
             var jsonGenerator = Json.createGenerator(outputStream)
        ) {
            while (jsonParser.hasNext()){
                switch (jsonParser.next()){
                    case START_OBJECT -> jsonGenerator.writeStartObject();
                    case END_OBJECT -> jsonGenerator.writeEnd();
                    default -> throw new IllegalStateException("Unexpected value: " + jsonParser.next());
                }
            }
        }
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
