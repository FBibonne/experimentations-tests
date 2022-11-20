package fr.insee.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES;

class FileSpliteratorMethod2Test {

    private static String content;
    private static Path tempFile;

    @BeforeAll
    public static void init() throws IOException, ClassNotFoundException {
        // TEsts ok with DEFAULT_LINE_SIZE set to 10
        System.setProperty("fileSpliterator.defaultLineSize", "10");
        Class.forName("fr.insee.test.FileSpliteratorMethod2",true,FileSpliteratorMethod2Test.class.getClassLoader());

        content= """
                AAAAAAAAAA
                AAAAAAAAAA
                
                BBBBBBBBBB
                
                CCCCCCCCCCCCCCCCCCC
                
                DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD
                EEEEEEEEEEEEEEEEEEE""";
        tempFile=Files.createTempFile("test",".txt");
        Files.writeString(tempFile,content);
    }


    @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    @ParameterizedTest
    @CsvSource({
            "0, 10",
            "5, 10",
            "9, 10",
            "10, 10",
            "11, 10",
            "20, 21",
            "39, 34",
            "105, 156",
            "169, 156",
    })
    void findFirstEolAfterOrBefore_test(long position, long firstEol) throws IOException {
        var fileSpliterator = new FileSpliteratorMethod2(tempFile,0,content.length(), Optional.empty());
        assertEquals(10, FileSpliteratorMethod2.DEFAULT_LINE_SIZE);
        assertEquals(firstEol, fileSpliterator.findFirstEolAfterOrBefore(position, 0, 94).getAsLong());
    }

    @Test
    public void findFirstEolAfterOrBefore_test_empty(){
        fail();
    }

    private void viewBytes(Path tempFile) throws IOException {
        var bytes=Files.newInputStream(tempFile).readAllBytes();
        for (int i = 0; i < bytes.length; i++) {
            System.out.println(i+" : "+String.format("%02X",bytes[i]));
        }
    }

}