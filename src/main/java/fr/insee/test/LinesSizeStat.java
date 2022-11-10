package fr.insee.test;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

public class LinesSizeStat {

    private final Map<Size, Count> sizes = new HashMap<>();
    public OptionalLong maxLikelyhood() {
        var retour=OptionalLong.empty();
        var maxCount=0;
        for (var sizeCount : sizes.entrySet()){
            if(maxCount<sizeCount.getValue().count()){
                maxCount=sizeCount.getValue().count();
                retour=OptionalLong.of(sizeCount.getKey().size());
            }
        }
        return retour;
    }

    public void add(long nbBytes) {
        sizes.merge(new Size(nbBytes),new Count(1),Count::sum);
    }

    private record Count(int count) {

        public Count sum(Count c){
            return new Count(c.count+this.count);
        }

    }

    private record Size(long size) {
    }
}
