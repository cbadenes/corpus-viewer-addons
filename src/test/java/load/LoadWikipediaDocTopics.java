package load;

/**
 *
 *  Create a 'doctopics' collection for a given Corpus
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
public class LoadWikipediaDocTopics extends LoadDocTopicsFromFile {

    private static final Integer MAX    = 1000;//-1
    private static final Integer OFFSET = 0;
    private static final String CORPUS  = "corpora/wikipedia/doctopics-120.csv.gz"; //"corpora/wikipedia/doctopics-350.csv.gz"

    public LoadWikipediaDocTopics() {
        super(CORPUS, MAX, OFFSET);
    }

}
