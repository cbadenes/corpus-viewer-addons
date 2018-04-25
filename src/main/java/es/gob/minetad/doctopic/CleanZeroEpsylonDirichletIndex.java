package es.gob.minetad.doctopic;

import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CleanZeroEpsylonDirichletIndex extends CleanZeroEpsylonIndex{

    private static final Logger LOG = LoggerFactory.getLogger(CleanZeroEpsylonDirichletIndex.class);

    public CleanZeroEpsylonDirichletIndex(int numTopics, float precision, float epsylon) {
        super(numTopics, precision, epsylon);
    }


    @Override
    public String id() {
        return "clean-zeros-with-epsylon-and-LMDirichlet-similarity";
    }

    @Override
    public Similarity metric() {
        return new LMDirichletSimilarity();
    }

}
