package es.gob.minetad.doctopic;

import es.gob.minetad.solr.similarity.TermFreqSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CleanZeroEpsylonTFIndex extends CleanZeroEpsylonIndex{

    private static final Logger LOG = LoggerFactory.getLogger(CleanZeroEpsylonTFIndex.class);

    public CleanZeroEpsylonTFIndex(int numTopics, float precision, float epsylon) {
        super(numTopics, precision, epsylon);
    }


    @Override
    public String id() {
        return "clean-zeros-with-epsylon-and-TF-similarity";
    }

    @Override
    public Similarity metric() {
        return new TermFreqSimilarity();
    }

}
