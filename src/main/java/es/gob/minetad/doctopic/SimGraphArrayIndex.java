package es.gob.minetad.doctopic;

import es.gob.minetad.metric.MetricsUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SimGraphArrayIndex implements DocTopicsIndex{

    private static final Logger LOG = LoggerFactory.getLogger(SimGraphArrayIndex.class);
    private final int numTopics;
    private final float precision;
    private final float epsylon;
    private final AtomicInteger emptyStringCounter;
    private final float epsylon_2_2sqrt;
    private final float epsylon_2_2sqrt_short;
    private final float epsylon_cota2_2;
    private final float epsylon20000f;

    public SimGraphArrayIndex(int numTopics, float precision, float epsylon) {
        this.numTopics = numTopics;
        this.precision = precision;
        this.epsylon = epsylon;
        this.epsylon_2_2sqrt = (float) (2*Math.sqrt(2*epsylon));
        this.epsylon_2_2sqrt_short = (float) (epsylon_2_2sqrt*10000);
        this.epsylon_cota2_2 = (float) (Math.sqrt((-72f + 24f*Math.sqrt(9+2*epsylon)))*10000);
        this.epsylon20000f = epsylon*20000f;
        this.emptyStringCounter = new AtomicInteger(1);
    }

    @Override
    public String toString(List<Double> topicDistributions) {
        String termVectorString = DocTopicsUtil.getVectorString(topicDistributions, precision, epsylon);
        try{
            Map<Integer,Integer> termVector = new HashMap<>();
            String[] topics = termVectorString.split(" ");
            for (int i=0; i< topics.length; i++){
                Integer topicId = Integer.valueOf(StringUtils.substringBefore(topics[i], "|"));
                Integer topicValue = Integer.valueOf(StringUtils.substringAfter(topics[i], "|"));
                termVector.put(topicId,topicValue);
            }
            return DocTopicsUtil.getVectorStringfromMapReduced(termVector, epsylon_cota2_2);
        }catch (Exception e){
            LOG.warn(""+ emptyStringCounter.getAndIncrement()+ " empty shapes from DocTopicUtils");
            return "";
        }
    }

    @Override
    public List<Double> toVector(String topicRepresentation) {
        List<Double> vector = DocTopicsUtil.getVectorFromString(topicRepresentation, precision, numTopics, epsylon);
        return vector;
    }

    @Override
    public String id() {
        return "sim-graph-array";
    }

    @Override
    public Similarity metric() {
        return new BooleanSimilarity();
    }

    @Override
    public Double similarity(List<Double> v1, List<Double> v2) {
        List<Double> v1Norm = v1.stream().map(v -> v * precision).collect(Collectors.toList());
        List<Double> v2Norm = v2.stream().map(v -> v * precision).collect(Collectors.toList());

        float distance = MetricsUtils.jsd_tuning2(v1Norm, v2Norm, epsylon, epsylon_2_2sqrt_short, epsylon_cota2_2);

        double similarity = 0.0f;


        if(distance < epsylon){
            // Normalize distance
            double distanceNormalized =  0.5d*(epsylon - distance)/epsylon + 0.5d;
            similarity = distanceNormalized;
        }
        return similarity;
    }
}
