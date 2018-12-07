package load.wikipedia;

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
public class LoadWikipediaDocTopics extends LoadDocTopics {

    private static final Integer MAX    = -1;
    private static final Integer OFFSET = 0;
    private static final String CORPUS  = "corpora/wikipedia/doctopics-350.csv.gz"; //"corpora/wikipedia/doctopics-120.csv.gz"

    public LoadWikipediaDocTopics() {
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
            document.addField("text_t", solrDoc.get("text_t"));
            document.addField("url_s", solrDoc.get("url_s"));
        }

        return document;




    }
}
