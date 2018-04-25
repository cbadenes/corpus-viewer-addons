package es.gob.minetad.doctopic;

import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.solr.similarity.TermFreqSimilarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CleanZeroEpsylonIndex implements DocTopicsIndex{

    private static final Logger LOG = LoggerFactory.getLogger(CleanZeroEpsylonIndex.class);
    private final int numTopics;
    private final float precision;
    private final float epsylon;

    public CleanZeroEpsylonIndex(int numTopics, float precision, float epsylon) {
        this.numTopics = numTopics;
        this.precision = precision;
        this.epsylon = epsylon;
    }

    @Override
    public String toString(List<Double> topicDistributions) {
        return DocTopicsUtil.getVectorString(topicDistributions, precision, epsylon);
    }

    @Override
    public List<Double> toVector(String topicRepresentation) {
        return DocTopicsUtil.getVectorFromString(topicRepresentation, precision, numTopics, epsylon);
    }

    @Override
    public String id() {
        return "clean-zeros-with-epsylon";
    }

    @Override
    public Similarity metric() {
        return new BooleanSimilarity();
    }

    @Override
    public Double similarity(List<Double> v1, List<Double> v2) {
        return JensenShannon.similarity(v1,v2);
    }
}
