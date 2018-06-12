package es.gob.minetad.metric;

import es.gob.minetad.model.Topic;
import es.gob.minetad.model.TopicWord;
import es.gob.minetad.model.Word;
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

    public static Double similarity(Topic t1, Topic t2){
        List<String> w1 = t1.getWords().parallelStream().map(tw -> tw.getWord().getValue()).collect(Collectors.toList());
        List<String> w2 = t2.getWords().parallelStream().map(tw -> tw.getWord().getValue()).collect(Collectors.toList());

        List<TopicWord> tw1addons = w2.parallelStream().filter(w -> !w1.contains(w)).map(w -> new TopicWord(new Word(w), 0.0)).collect(Collectors.toList());
        List<TopicWord> tw2addons = w1.parallelStream().filter(w -> !w2.contains(w)).map(w -> new TopicWord(new Word(w), 0.0)).collect(Collectors.toList());

        LOG.debug("t1.size/addons: " + t1.getWords().size() +  "/" + tw1addons.size() + " , t2.size/addons: " + t2.getWords().size() + "/" + tw2addons.size());

        t1.getWords().addAll(tw1addons);
        t2.getWords().addAll(tw2addons);


        return similarity(t1.getWords().stream().sorted((a,b)->a.getWord().getValue().compareTo(b.getWord().getValue())).collect(Collectors.toList()), t2.getWords().stream().sorted((a,b)->a.getWord().getValue().compareTo(b.getWord().getValue())).collect(Collectors.toList()));

    }

}
