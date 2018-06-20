package es.gob.minetad.metric;

import es.gob.minetad.model.Topic;
import es.gob.minetad.model.TopicWord;
import es.gob.minetad.model.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static Double similarity(List<TopicWord> tw1, List<TopicWord> tw2){
        List<Double> w1 = tw1.stream().sorted((a, b) -> a.getWord().getValue().compareTo(b.getWord().getValue())).map(w -> w.getScore()).collect(Collectors.toList());
        List<Double> w2 = tw2.stream().sorted((a, b) -> a.getWord().getValue().compareTo(b.getWord().getValue())).map(w -> w.getScore()).collect(Collectors.toList());
        return JensenShannon.similarity(w1,w2);
    }

    public static Double similarity(Topic t1, Topic t2, Boolean prune){

        Map<Word, Double> tm1 = t1.getWords().parallelStream().collect(Collectors.toMap(tw -> tw.getWord(), tw -> tw.getScore()));
        Map<Word, Double> extra1 = t1.getWords().parallelStream().collect(Collectors.toMap(tw -> tw.getWord(), tw -> 0.0));
        Map<Word, Double> tm2 = t2.getWords().parallelStream().collect(Collectors.toMap(tw -> tw.getWord(), tw -> tw.getScore()));
        Map<Word, Double> extra2 = t2.getWords().parallelStream().collect(Collectors.toMap(tw -> tw.getWord(), tw -> 0.0));

        Map<Word, Double> ut1 = Stream.concat(tm1.entrySet().stream(), extra2.entrySet().stream()).collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> entry.getValue(),
                (s1, s2) -> (s1 > 0.0)? s1 : s2
                )
        );
        if (prune && (tm1.size() < (ut1.size()/2.0))) return 0.0;

        Map<Word, Double> ut2 = Stream.concat(tm2.entrySet().stream(), extra1.entrySet().stream()).collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> entry.getValue(),
                (s1, s2) -> (s1 > 0.0)? s1 : s2
                )
        );
        if (prune && (tm2.size() < (ut2.size()/2.0))) return 0.0;

        LOG.debug(t1.getId() + ".size/all = " + t1.getWords().size()+"/"+ut1.size() + ", " + t2.getId() +".size/all = " + t2.getWords().size() + "/" + ut2.size());

        List<TopicWord> f1 = ut1.entrySet().stream().map(entry -> new TopicWord(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        List<TopicWord> f2 = ut2.entrySet().stream().map(entry -> new TopicWord(entry.getKey(), entry.getValue())).collect(Collectors.toList());

        return similarity( f1, f2);

    }




}
