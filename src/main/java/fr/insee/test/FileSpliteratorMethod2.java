package fr.insee.test;

import lombok.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;

public class FileSpliteratorMethod2 implements Spliterator<Line> {

    private static final int DEFAULT_LINE_SIZE = 80;
    private static final int DEFAULT_CHUNK_SIZE = DEFAULT_LINE_SIZE * QuickFile.nbBytesByChar();
    private static final int CHARACTERISTICS = IMMUTABLE | NONNULL | ORDERED | SORTED;

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final SeekableByteChannel seekableByteChannel;
    private long end;
    private long nextStartLinePosition;
    private final CharsetDecoder decoder;
    private final LinesSizeStat linesSizesStat;
    private final Path filePath;


    /**
     * 1. le spliterator démarre forcément (valeur de start) après un EOL ou en position 0
     * 2. le spliterator finit forcément (valeur de end) sur un EOL ou juste avant un EOF(dernière position du fichier)
     *
     */
    public FileSpliteratorMethod2(@NonNull Path filePath, long start, long end, @NonNull Optional<Charset> optionalCharset) throws IOException {
        if (start < 0 || start > end) {
            throw new IllegalArgumentException("start must be in [0 , end] : " + start + " , " + end);
        }
        this.seekableByteChannel = Files.newByteChannel(filePath, StandardOpenOption.READ);
        this.seekableByteChannel.position(start);
        this.nextStartLinePosition=start;
        this.end = end;
        this.linesSizesStat=new LinesSizeStat();
        decoder = optionalCharset.orElse(DEFAULT_CHARSET).newDecoder();
        this.filePath = filePath;
    }

    @Override
    /*
     * 1. Détecter qu'on n'est pas à la fin (position après le dernier byte) => false avant de lire
     * 2. lire les bytes
     * 3. vérifier qu'on était pas à la fin du fichier => false
     * 4. traiter les bytes lus : chercher EOL
     */
    public boolean tryAdvance(@NonNull Consumer<? super Line> action) {
        var allChunks = new ArrayList<Chunk>();
        long startLinePosition=-1;
        try {
            startLinePosition = calcStartLinePosition();
            var nextChunkStartPosition = startLinePosition;
            while (nextChunkStartPosition <= end) {
                var chunk = readChunk(nextChunkStartPosition);
                allChunks.add(chunk);
                if (chunk.containsEOL()) {
                    this.linesSizesStat.add(calcNbBytes(allChunks));
                    action.accept(new Line(startLinePosition, decode(allChunks)));
                    seekableByteChannel.position(chunk.absoluteEOLPosition());
                    nextStartLinePosition=chunk.absoluteEOLPosition();
                    return true;
                }
                nextChunkStartPosition = chunk.absoluteEndPosition() + 1;
            }
        } catch (IOException e) {
            allChunks.clear();
            System.err.println(e);
            e.printStackTrace(System.err);
            action.accept(new Line(startLinePosition,  "ERROR READING FILE : "+e.getMessage()));
            this.seekableByteChannel.close();
            return false;
        }
        if (!allChunks.isEmpty()) {
            action.accept(new Line(startLinePosition, decode(allChunks)));
            // this.linesSizesStat.add(Line) : last line : won't call anymore computeChunkSize
            nextStartLinePosition=end+1;
            return true;
        }
        this.seekableByteChannel.close();
        return false;
    }


    private long calcStartLinePosition() throws IOException {
        return seekableByteChannel.position();
    }


    private Chunk readChunk(long chunkStartPosition) throws IOException {

        int nbBytesRead;
        ByteBuffer buffer;


        buffer = ByteBuffer.wrap(new byte[castToInt(computeChunkSize(chunkStartPosition))]);

        nbBytesRead = seekableByteChannel.read(buffer);

        return new Chunk(buffer, chunkStartPosition, nbBytesRead);


    }

    private int castToInt(long computeChunkSize) {
        if (computeChunkSize>Integer.MAX_VALUE){
            throw new Error("Impossible to cast "+computeChunkSize+" to int");
        }
        return (int) computeChunkSize;
    }

    private String decode(List<Chunk> chunks) {
        StringBuilder sb = new StringBuilder();
        for (Chunk chunk : chunks) {
            sb.append(decode(chunk));
        }
        return sb.toString();
    }

    private long calcNbBytes(ArrayList<Chunk> allChunks) {
        return allChunks.stream()
                .peek(Chunk::setBufferPositionForDecode)
                .mapToLong(c->((long)c.end())+1)
                .sum();
    }

    private char[] decode(Chunk chunk) {

        if (chunk.start() > chunk.end() || chunk.start() < 0) {
            return new char[0];
        }

        chunk.setBufferPositionForDecode();
        try {
            return decoder.decode(chunk.buffer()).array();
        } catch (CharacterCodingException e) {
            System.err.println(e);
            e.printStackTrace(System.err);
            return ("ERROR DECODING : " + e.getMessage()).toCharArray();
        }
    }

    /**
     * Compute the best size of the next chunk to read in the file :
     * 1. the likelyhood size of a line
     * 2. or the default size if there is no likelyhood line size
     * 3. or the size to the end position for this FileSpliterator if the size found at 1 or 2 is too high
     * @return the size of the next chunk to be read
     */
    private long computeChunkSize(long startPosition) {
        var retour=this.linesSizesStat.maxLikelyhood().orElse(DEFAULT_CHUNK_SIZE);
        return Math.min (retour, end - startPosition +1 );
    }

    /*
     * 1. Déterminer le milieu des données restantes
     * 2. déterminer le premier EOL qui suit ou qui précède, milieu inclus
     * 3. si existe, le nouveau spliterator retourné démarre juste après ce EOL. this.end = EOL
     *
     *
     */
    @Override
    public Spliterator<Line> trySplit() {
        var newEnd = findFirstEolAfterOrBefore((this.end+this.nextStartLinePosition)/2, this.nextStartLinePosition, this.end);
        if (newEnd.isPresent()){
            var oldEnd=this.end;
            this.end=newEnd.getAsLong();
            try {
                return new FileSpliteratorMethod2(this.filePath, newEnd.getAsLong()+1, oldEnd, Optional.ofNullable(this.decoder.charset()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private OptionalLong findFirstEolAfterOrBefore(long position, long start, long end) {
        var maxRange=Math.max(position-start, end-position);
        var minRange=Math.min(position-start, end-position);
        for(long i=0;i<=minRange;i++){
            if seekableByteChannel.
        }
        for (long i=minRange+1;i<=maxRange;i++){

        }
        return OptionalLong.empty();
    }

    @Override
    public long estimateSize() {
        return (this.end-this.nextStartLinePosition)/this.linesSizesStat.maxLikelyhood().orElse(DEFAULT_CHUNK_SIZE);
    }

    @Override
    public int characteristics() {
        return CHARACTERISTICS;
    }


}
