package fr.insee.test;

import lombok.NonNull;

public record Line(long start, @NonNull String contenu) implements Comparable<Line> {
    @Override
    public int compareTo(Line o) {
        return Long.compare(start, o.start);
    }
}
