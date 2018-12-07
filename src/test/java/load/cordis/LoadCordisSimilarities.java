package load.cordis;

import load.LoadSimilarities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  Create a 'similarities' collection from an existing 'doctopics' collection
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./start.sh
 *    3. create collections: ./create-collections.sh
 *
 *  http://localhost:8983/solr/#/doctopics
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 *
 */
public class LoadCordisSimilarities extends LoadSimilarities {

    private static final String CORPUS      = "cordis";
    private static final Integer NUM_TOPICS = 150;
    private static final Double THRESHOLD   = 0.8;

    public LoadCordisSimilarities() {
        super(CORPUS, NUM_TOPICS, THRESHOLD);

    }
}
