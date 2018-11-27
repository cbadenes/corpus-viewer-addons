package load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Create a 'documents' collection for a given Corpus
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./start.sh
 *    3. create collections: ./create-collections.sh
 *
 *  http://localhost:8983/solr/#/documents
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public abstract class LoadDocuments extends LoadData{

    private static final Logger LOG = LoggerFactory.getLogger(LoadDocuments.class);


    public LoadDocuments(String corpus, Integer max, Integer offset) {
        super(corpus, "documents", max, offset);
    }

}
