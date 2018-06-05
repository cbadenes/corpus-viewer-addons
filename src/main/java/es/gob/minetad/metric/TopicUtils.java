package es.gob.minetad.metric;

import es.gob.minetad.model.Topic;
import es.gob.minetad.model.TopicWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TopicUtils.class);


    public static Integer multiplier(Integer numTopics){

        int length = String.valueOf(numTopics).length();

        int ratio = 3;

        double value = Math.pow(10.0, Double.valueOf(length+ratio));

        return Double.valueOf(value).intValue();
    }

    public static Double similarity(List<TopicWord> tw1, List<TopicWord> tw2){
        List<Double> w1 = tw1.stream().sorted((a, b) -> a.getWord().getValue().compareTo(b.getWord().getValue())).map(w -> w.getScore()).collect(Collectors.toList());
        List<Double> w2 = tw2.stream().sorted((a, b) -> a.getWord().getValue().compareTo(b.getWord().getValue())).map(w -> w.getScore()).collect(Collectors.toList());
        return JensenShannon.similarity(w1,w2);
    }

}
