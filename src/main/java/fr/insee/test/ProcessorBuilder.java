package fr.insee.test;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.util.function.UnaryOperator;

public record ProcessorBuilder (Filter filter){

    public static ProcessorBuilder filter(Filter filter){
        return new ProcessorBuilder(filter);
    }

    public JsonProcessor thenProcessWith(UnaryOperator<JsonObject> processor) {
        return new JsonProcessor(this.filter, processor);
    }
}
