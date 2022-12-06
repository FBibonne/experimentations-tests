package fr.insee.test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import lombok.NonNull;

import java.util.Optional;

@FunctionalInterface
public interface Filter {


    static Filter keysWithName(@NonNull String name) {
        return jsonParser -> {
            if(jsonParser.currentEvent()== JsonParser.Event.KEY_NAME && name.equals(jsonParser.getString())){
                var key=jsonParser.getString();
                jsonParser.next();
                return Optional.of(Json.createObjectBuilder().add(key, jsonParser.getValue()).build());
            }else{
                return Optional.empty();
            }
        };
    }

    Optional<JsonObject> match(JsonParser jsonParser);
}
