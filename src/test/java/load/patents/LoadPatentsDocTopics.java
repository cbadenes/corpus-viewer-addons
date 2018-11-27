package load.patents;

import load.LoadDocTopics;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.util.Optional;

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
public class LoadPatentsDocTopics extends LoadDocTopics {

    private static final Integer MAX    = 1000;//-1
    private static final Integer OFFSET = 0;
    private static final String CORPUS  = "corpora/patents/doctopics-250.csv.gz"; //"corpora/patents/doctopics-750.csv.gz"

    public LoadPatentsDocTopics() {
        super(CORPUS, MAX, OFFSET);
    }

    @Override
    protected SolrInputDocument newDocTopic(String id) {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id",id);

        Optional<SolrDocument> doc=documentCollection.getById(id);

        if (doc.isPresent()) {
            SolrDocument solrDoc = doc.get();
            document.addField("name_s", solrDoc.get("name_s"));
            document.addField("lang_s", solrDoc.get("lang_s"));
            document.addField("text_txt", solrDoc.get("text_txt"));
        }

        return document;
    }
}
