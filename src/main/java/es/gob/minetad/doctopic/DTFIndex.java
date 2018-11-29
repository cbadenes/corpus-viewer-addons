package es.gob.minetad.doctopic;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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
        return  values.entrySet().stream().filter(entry -> entry.getValue() > 0.0).map(entry -> entry.getKey()+"|"+Double.valueOf(entry.getValue()*multiplier).intValue()).collect(Collectors.joining(" "));
    }

    public Map<String,Double> toMap(String topicRepresentation, Boolean normalize) {
        Map<String,Double> map = new HashMap<>();
        for(String expression: topicRepresentation.split(" ")){
            String word     = StringUtils.substringBefore(expression, "|");
            Integer freq    = Integer.valueOf(StringUtils.substringAfter(expression, "|"));
            Double score    = normalize? Double.valueOf(freq)/Double.valueOf(multiplier) : Double.valueOf(freq);
            map.put(word,score);
        }
        return map;
    }



}
