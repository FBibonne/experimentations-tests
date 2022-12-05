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
            while (jsonParser.hasNext()){
                switch (jsonParser.next()){
                    case START_OBJECT -> jsonGenerator.writeStartObject();
                    case END_OBJECT, END_ARRAY -> jsonGenerator.writeEnd();
                    case KEY_NAME -> jsonGenerator.writeKey(jsonParser.getString());
                    case VALUE_NUMBER -> {
                        if (jsonParser.isIntegralNumber()){
                            jsonGenerator.write(jsonParser.getLong());
                        }else{
                            jsonGenerator.write(jsonParser.getBigDecimal());
                        }
                    }
                    case VALUE_TRUE -> jsonGenerator.write(true);
                    case VALUE_FALSE -> jsonGenerator.write( false);
                    case VALUE_STRING -> jsonGenerator.write(jsonParser.getString());
                    case VALUE_NULL -> jsonGenerator.writeNull();
                    case START_ARRAY -> jsonGenerator.writeStartArray();
                    default -> throw new IllegalStateException("Unexpected value: " + jsonParser.currentEvent());
                }
            }
        }
        return writer.toString();
    }
}
