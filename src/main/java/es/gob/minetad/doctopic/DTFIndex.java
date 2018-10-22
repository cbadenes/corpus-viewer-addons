package es.gob.minetad.doctopic;

import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.TopicWord;
import es.gob.minetad.model.Word;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DTFIndex {

    private static final Logger LOG = LoggerFactory.getLogger(DTFIndex.class);

    private final float multiplier;

    public DTFIndex(float multiplier) {
        this.multiplier = multiplier;
    }


    public String toString(Map<String,Double> values) {
        return values.entrySet().stream().map(entry -> entry.getKey()+"|"+Double.valueOf(entry.getValue()*multiplier).intValue()).collect(Collectors.joining(" "));
    }

    public Map<String,Double> toMap(String topicRepresentation) {
        Map<String,Double> map = new HashMap<>();
        for(String expression: topicRepresentation.split(" ")){
            String[] values = expression.split("|");
            map.put(values[0],Double.valueOf(values[1])/Double.valueOf(multiplier));
        }
        return map;
    }


}
