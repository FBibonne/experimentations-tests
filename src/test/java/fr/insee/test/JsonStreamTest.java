package fr.insee.test;

import jakarta.json.Json;
import jakarta.json.JsonStructure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class JsonStreamTest {

    private static final String complexJson= """
                             {
                               "firstName": "John", "lastName": "Smith", "age": 25,
                               "address" : {
                                   "streetAddress": "21 2nd Street",
                                   "city": "New York",
                                   "state": "NY",
                                   "postalCode": "10021"
                               },
                               "phoneNumber": [
                                   {"type": "home", "number": "212 555-1234"},
                                   {"type": "fax", "number": "646 555-4567"}
                                ]
                             }
                    """;

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
            complexJson

    })
    void testStreamProcessIdentique(String jsonString){
        // Given jsonString
        //When
        var jsonProcessed = JsonStream.process(jsonString);
        // Then
        assertThat(toJson(jsonProcessed)).isEqualTo(toJson(jsonString));
    }

    @Test
    void testStreamProcessWithInvalidJson(){
        fail();
    }

    @Test
    void testFilter(){
        var initialJson= """
                {"nomDeFamille":"Bibonne"}
        """;
        var expectedJson="""
                {"nom":"Bibonne"}
        """;
        var jsonProcessor=ProcessorBuilder.filter(Filter.keysWithName("nomDeFamille"))
                .thenProcessWith(jsonObject-> Json.createObjectBuilder().add("nom", jsonObject.get("nomDeFamille")).build());
        assertThat(toJson(jsonProcessor.process(initialJson))).isEqualTo(toJson(expectedJson));
    }

    @Test
    void testFilterKeyNumber(){
        var initialJson= """
                {"1":"Bibonne"}
        """;
        var expectedJson="""
                {"nom1":"Bibonne"}
        """;
        var jsonProcessor=ProcessorBuilder.filter(Filter.keysWithName("1"))
                .thenProcessWith(jsonObject-> Json.createObjectBuilder().add("nom1", jsonObject.get("1")).build());
        assertThat(toJson(jsonProcessor.process(initialJson))).isEqualTo(toJson(expectedJson));
    }


    @Test
    void testFilterWithComplexJson(){
        var jsonProcessor=ProcessorBuilder.filter(Filter.keysWithName("streetAddress"))
                .thenProcessWith(jsonObject-> Json.createObjectBuilder().add("street", jsonObject.get("streetAddress")).build());
        assertThat(toJson(jsonProcessor.process(complexJson))).isEqualTo(toJson("""                       
                                             {
                                               "firstName": "John", "lastName": "Smith", "age": 25,
                                               "address" : {
                                                   "street": "21 2nd Street",
                                                   "city": "New York",
                                                   "state": "NY",
                                                   "postalCode": "10021"
                                               },
                                               "phoneNumber": [
                                                   {"type": "home", "number": "212 555-1234"},
                                                   {"type": "fax", "number": "646 555-4567"}
                                                ]
                                             }
                """));
    }

    private JsonStructure toJson(String jsonString){
        var reader=Json.createReader(new StringReader(jsonString));
        return reader.read();
    }


}

