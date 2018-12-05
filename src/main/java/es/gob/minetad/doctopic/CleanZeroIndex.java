package es.gob.minetad.doctopic;

import es.gob.minetad.metric.JensenShannon;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CleanZeroIndex implements DocTopicsIndex{

    private static final Logger LOG = LoggerFactory.getLogger(CleanZeroIndex.class);
    private final int numTopics;
    private final float precision;

    public CleanZeroIndex(int numTopics, float precision) {
        this.numTopics = numTopics;
        this.precision = precision;
    }

    @Override
    public String toString(List<Double> topicDistributions) {
        return DocTopicsUtil.getVectorString(topicDistributions, precision);
    }

    @Override
    public List<Double> toVector(String topicRepresentation) {
        return DocTopicsUtil.getVectorFromString(topicRepresentation, precision, numTopics);
    }

    @Override
    public String id() {
        return "clean-zeros";
    }

    @Override
    public Similarity metric() {
        return new BooleanSimilarity();
    }

    @Override
    public Double getEpsylon() {
        return 0.0;
    }

    @Override
    public Integer getPrecision() {
        return Float.valueOf(precision).intValue();
    }

    @Override
    public Double similarity(List<Double> v1, List<Double> v2) {
        return JensenShannon.similarity(v1,v2);
    }
}
