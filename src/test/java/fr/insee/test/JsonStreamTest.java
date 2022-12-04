package fr.insee.test;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonStreamTest {

    @Test
    public void streamEmpty(){
        String emptyJson="{}";
        assertThat(JsonStream.process(emptyJson)).isEqualTo(emptyJson);
    }


}

