package load.cordis;

import load.LoadDocTopics;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class LoadCordisDocTopics extends LoadDocTopics {

    private static final Logger LOG = LoggerFactory.getLogger(LoadCordisDocTopics.class);

    private static final Integer MAX    = 1000; //-1
    private static final Integer OFFSET = 0;
    private static final String CORPUS  = "corpora/cordis/doctopics-70.csv.gz"; //"corpora/cordis/doctopics-70.csv.gz"

    public LoadCordisDocTopics() {
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
            document.addField("instrument_s", solrDoc.get("instrument_s"));
            document.addField("startDate_dt", solrDoc.get("startDate_dt"));
            document.addField("endDate_dt", solrDoc.get("endDate_dt"));
            document.addField("totalCost_f", solrDoc.get("totalCost_f"));
            document.addField("area_s", solrDoc.get("area_s"));
            document.addField("topicWater_i", solrDoc.get("topicWater_i"));
        }

        return document;
    }
}
