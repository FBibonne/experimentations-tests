package fr.insee.test;

import jakarta.json.Json;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class JsonStream {

    public static String process(@NonNull String json) {
        var writer= new StringWriter();
        try (var jsonParser = Json.createParser(new StringReader(json));
             var jsonGenerator = Json.createGenerator(writer)
        ) {
            String lastKey=null;
            while (jsonParser.hasNext()){
                switch (jsonParser.next()){
                    case START_OBJECT -> jsonGenerator.writeStartObject();
                    case END_OBJECT -> jsonGenerator.writeEnd();
                    case KEY_NAME -> lastKey=jsonParser.getString();
                    case VALUE_NUMBER -> {
                        if (jsonParser.isIntegralNumber()){
                            jsonGenerator.write(lastKey, jsonParser.getLong());
                        }else{
                            jsonGenerator.write(lastKey, jsonParser.getBigDecimal());
                        }
                        lastKey=null;
                    }
                    case VALUE_TRUE -> {
                        jsonGenerator.write(lastKey, true);
                        lastKey=null;
                    }
                    case VALUE_FALSE -> {
                        jsonGenerator.write(lastKey, false);
                        lastKey=null;
                    }
                    case VALUE_STRING -> {
                        jsonGenerator.write(lastKey, jsonParser.getString());
                        lastKey=null;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + jsonParser.currentEvent());
                }
            }
        }
        return writer.toString();
    }
}
