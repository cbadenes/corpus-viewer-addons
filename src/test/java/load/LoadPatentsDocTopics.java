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
public class LoadPatentsDocTopics extends LoadDocTopicsFromFile {

    private static final Integer MAX    = 1000;//-1
    private static final Integer OFFSET = 0;
    private static final String CORPUS  = "corpora/patents/doctopics-250.csv.gz"; //"corpora/patents/doctopics-750.csv.gz"

    public LoadPatentsDocTopics() {
        super(CORPUS, MAX, OFFSET);
    }

}
