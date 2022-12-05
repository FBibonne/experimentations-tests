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
            "{\"a\":1}"
    })
    void testStreams(String jsonString){
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

