package load.wikipedia;

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
public class LoadWikipediaLDATopicDocs extends LoadLDATopicDocs {

    private static final String CORPUS_NAME     = "wikipedia";
    private static final String CORPUS_DIM      = "120";
    private static final String CORPUS_MODEL    = "http://localhost:8002/model";

    public LoadWikipediaLDATopicDocs() {
        super(CORPUS_NAME, CORPUS_DIM, CORPUS_MODEL);
    }

}
