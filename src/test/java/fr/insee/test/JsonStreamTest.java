package fr.insee.test;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JsonStreamTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"a\":1}",
            "{\"a\":1.1}",
            "{\"a\":true}",
            "{\"a\":false}",
            "{\"a\":\"val\"}",
            "{\"a\":null}",
            "{\"a\":[]}",
            "{\"a\":[{\"b\":1}]}",
            "{\"a\":[{\"b\":1}, {\"c\":true}, {\"d\":null, \"e\":\"test\"}]}",

    })
    void testStreams(String jsonString){
        var js=toJson(jsonString);
        // Given jsonString
        //When
        var jsonProcessed = JsonStream.process(jsonString);
        // Then
        assertThat(toJson(jsonProcessed)).isEqualTo(toJson(jsonString));
    }

    private JsonStructure toJson(String jsonString){
        var reader=Json.createReader(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)));
        return reader.read();
    }


}

