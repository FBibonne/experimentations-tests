package fr.insee.test;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * # Lecture rapide de fichier
 *
 * Offrir une méthode qui lit rapidement les lignes d'un fichier en alimentant une stream :
 * 1. 1 thread accède au fichier en non bloquant et fournit des Lignes à décoder :
 *   - des threads indépendants décodent les lignes (encodage à voir) et alimentent une autre stream unordered avec les lignes décodées
 * 2. des threads indépendants accèdent parallèlement à différentes parties du fichier (cohérent avec le split de Spliterator et
 * le parallélisme des threads) :
 *   - ils lisent puis décodent
 * 3. plusieurs threads lisent le fichier et alimentent une queue de bytes (une queue par partie lue)
 *   - des threads indépendants (un par queue) consomment la queue et calculent les lignes
 *   - chaque queue est associée à un spliterator
 *   - si on split, on divise la partie couverte par le spliterator en deux
 *
 * - caractères séparateurs à déterminer avant
 *
 * # Fail fast, late binding => report IMMUTABLE
 * - **guaranty that source file is immutable**
 *
 * # References
 * - https://stackoverflow.com/questions/11867348/concurrent-reading-of-a-file-java-preferred
 * - https://blogs.oracle.com/javamagazine/post/java-nio-nio2-buffers-channels-async-future-callback , section  SeekableByteChannel
 *
 *
 *
 */
public class QuickFile
{

    private static final System.Logger log = System.getLogger(QuickFile.class.getName());

    public static final byte LF = 0x0A;
    public static final int NB_BYTES_BY_CHAR=1;


    public static void main( String[] args ) throws IOException {
        System.out.println();
        try(SeekableByteChannel seekableByteChannel = Files.newByteChannel(Path.of("C:/Users/xrmfux/test.txt"))){
            seekableByteChannel.position(0);
            byte[] bytes = new byte[1024];
            int read = seekableByteChannel.read(ByteBuffer.wrap(bytes));
            System.out.println( "read : " + read);
            for (byte b : bytes){
                System.out.print(hexa(b)+" ");
            }
        }
    }

    private static String hexa(byte b) {
        return String.format("%02X", b);
    }

    public static int nbBytesByChar() {
        return NB_BYTES_BY_CHAR;
    }
}
