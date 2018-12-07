package load.patents;

import load.LoadLDATopicDocs;

/**
 *
 * Create a 'topic-doc' collection for a given Corpus:
 *
 * It Requires a Topic Model API running:
 *    1. move into: src/test/docker/models
 *    2. run the service: ./docker-compose up -d
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./start.sh
 *    3. create collections: ./create-collections.sh
 *
 *
 *  http://localhost:8983/solr/#/topicdocs
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LoadPatentsLDATopicDocs extends LoadLDATopicDocs {

    private static final String CORPUS_NAME     = "patents";
    private static final String CORPUS_DIM      = "750";
    private static final String CORPUS_MODEL    = "http://localhost:8005/model";

    public LoadPatentsLDATopicDocs() {
        super(CORPUS_NAME, CORPUS_DIM, CORPUS_MODEL);
    }

}
