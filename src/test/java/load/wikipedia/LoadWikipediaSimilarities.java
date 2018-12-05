package load.wikipedia;

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
public class LoadWikipediaSimilarities extends LoadSimilarities {

    private static final String CORPUS      = "wikipedia";
    private static final Integer NUM_TOPICS = 250;
    private static final Double THRESHOLD   = 0.9;

    public LoadWikipediaSimilarities() {
        super(CORPUS, NUM_TOPICS, THRESHOLD);

    }
}
