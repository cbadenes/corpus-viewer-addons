package es.gob.minetad.doctopic;

import org.apache.lucene.search.similarities.Similarity;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public interface DocTopicsIndex {

    String toString(List<Double> topicDistributions);

    List<Double> toVector(String topicRepresentation);

    String id();

    Similarity metric();

    Double getEpsylon();

    Double getPrecision();

    Double similarity(List<Double> v1, List<Double> v2);

}
