package fr.insee.test;

import lombok.NonNull;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

public record Chunk(

        ByteBuffer buffer,

        int nbBytesRead,

        OptionalInt eolPosition,
        long absoluteStartPosition
) {
    public Chunk(@NonNull ByteBuffer buffer, long chunkStartPosition, int nbBytesRead) {
        this(buffer, nbBytesRead, computeEOL(nbBytesRead, buffer.array()), chunkStartPosition);
    }

    public void setBufferPositionForDecode() {
        buffer.position(0);
        buffer.limit(end() + 1);
    }

    /**
     * Position to end decode in buffer :
     * - eol-1  if contains EOL
     * - else last byte read from file for this chunk
     */
    public int end() {
        return eolPosition().orElse(nbBytesRead() )- 1;
    }

    public int start(){
        return 0;
    }


    public boolean containsEOL() {
        return eolPosition.isPresent();
    }

    private static OptionalInt computeEOL(int nbBytesRead, byte[] bytes){
        for (int i = 0; i < nbBytesRead; i++) {
            if (bytes[i] == QuickFile.LF) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }

    public long absoluteEOLPosition() {
        return eolPosition.isPresent()?(absoluteStartPosition + eolPosition.getAsInt()):-1;
    }

    public long absoluteEndPosition() {
        return eolPosition.isPresent()?(absoluteStartPosition + eolPosition.getAsInt()):absoluteStartPosition+end();
    }
}
