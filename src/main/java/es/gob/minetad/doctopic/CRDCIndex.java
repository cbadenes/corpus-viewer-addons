package es.gob.minetad.doctopic;

import com.google.common.base.Strings;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.Document;
import es.gob.minetad.model.Score;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CRDCIndex implements DocTopicsIndex {

    private static final Logger LOG = LoggerFactory.getLogger(CRDCIndex.class);

    private Integer numTopics;

    private Double threshold = 0.85;

    private Integer multiplier = 10000;

    public CRDCIndex(Integer numTopics) {
        this.numTopics = numTopics;
    }

    public CRDCIndex(Integer numTopics, Double threshold, Integer multiplier) {
        this.numTopics  = numTopics;
        this.threshold  = threshold;
        this.multiplier = multiplier;
    }

    @Override
    public String toString(List<Double> vector) {
        List<Score> sortedVector = IntStream.range(0, vector.size()).mapToObj(i -> new Score(vector.get(i), new Document(String.valueOf(i)), null)).sorted((a, b) -> -a.getValue().compareTo(b.getValue())).collect(Collectors.toList());

        StringBuilder shape = new StringBuilder();
        int index = 0;
        Double acc = 0.0;
        for(int i=0; i< vector.size();i++){
            if (acc >= threshold) break;
            Score res = sortedVector.get(i);
            Integer score = Double.valueOf(res.getValue()*multiplier).intValue();
            shape.append("t").append(res.getReference().getId()).append("_").append(i).append("|").append(score).append(" ");
            acc += res.getValue();
        }

        return shape.toString().trim();
    }

    @Override
    public List<Double> toVector(String shape) {
        if (Strings.isNullOrEmpty(shape)) return Collections.emptyList();

        Double[] vector = new Double[numTopics];
        Arrays.fill(vector,0.0);

        String[] topics = shape.split(" ");

        for(int i=0; i< topics.length; i++){

            String topic = topics[i];
            String[] topicValues = topic.split("\\|");
            Double score    = Double.valueOf(topicValues[1])/Double.valueOf(multiplier);
            Integer index   = Integer.valueOf(StringUtils.substringAfter(StringUtils.substringBefore(topicValues[0],"_"),"t"));
            vector[index] = score;
        }

        return Arrays.asList(vector);
    }

    @Override
    public String id() {
        return "CRDC";
    }

    @Override
    public Similarity metric() {
        return new BooleanSimilarity();
    }

    @Override
    public Double similarity(List<Double> v1, List<Double> v2) {
        return JensenShannon.similarity(v1, v2);
    }

}
