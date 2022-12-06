package fr.insee.test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import lombok.NonNull;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.UnaryOperator;

public record JsonProcessor(Filter filter, UnaryOperator<JsonObject> processor) {

    public String process(@NonNull String json) {
        var writer= new StringWriter();
        try (var jsonParser = Json.createParser(new StringReader(json));
             var jsonGenerator = Json.createGenerator(writer)
        ) {
            while (jsonParser.hasNext()){
                jsonParser.next();
                var jsonObject=filter.match(jsonParser);
                if (jsonObject.isPresent()){
                    var jsonObjectProcessed=processor.apply(jsonObject.get());
                    var key=jsonObjectProcessed.keySet().stream().findFirst().get();
                    jsonGenerator.write(key, jsonObjectProcessed.get(key));
                }else {
                    switch (jsonParser.currentEvent()) {
                        case START_OBJECT -> jsonGenerator.writeStartObject();
                        case END_OBJECT, END_ARRAY -> jsonGenerator.writeEnd();
                        case KEY_NAME -> jsonGenerator.writeKey(jsonParser.getString());
                        case VALUE_NUMBER -> {
                            if (jsonParser.isIntegralNumber()) {
                                jsonGenerator.write(jsonParser.getLong());
                            } else {
                                jsonGenerator.write(jsonParser.getBigDecimal());
                            }
                        }
                        case VALUE_TRUE -> jsonGenerator.write(true);
                        case VALUE_FALSE -> jsonGenerator.write(false);
                        case VALUE_STRING -> jsonGenerator.write(jsonParser.getString());
                        case VALUE_NULL -> jsonGenerator.writeNull();
                        case START_ARRAY -> jsonGenerator.writeStartArray();
                        default -> throw new IllegalStateException("Unexpected value: " + jsonParser.currentEvent());
                    }
                }
            }
        }
        return writer.toString();
    }
}
