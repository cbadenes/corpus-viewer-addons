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
public class LoadCordisDocTopics extends LoadDocTopicsFromFile {

    private static final Integer MAX    = 1000; //-1
    private static final Integer OFFSET = 0;
    private static final String CORPUS  = "corpora/cordis/doctopics-70.csv.gz"; //"corpora/cordis/doctopics-70.csv.gz"

    public LoadCordisDocTopics() {
        super(CORPUS, MAX, OFFSET);
    }

}
