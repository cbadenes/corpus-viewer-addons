package query;

import es.gob.minetad.model.Alarm;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Obtain documents as candidates to be duplicated.
 *
 * Given a corpus, display the number of similar documents grouped by alarm type: (1) critical, (3) high and (5) low
 *
 * Then, given a group (hashcode) belonging to an alarm type (integer), display the list of documents for that group
 *
 * Before run the query you must start the Solr service with a valid doctopic collection!
 *
 * http://localhost:8983/solr/cordis-doctopics/terms?terms.fl=hashcode0&wt=xml&terms.mincount=2&terms.limit=100
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class HierarchicalServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(HierarchicalServiceTest.class);
    private TestSettings settings;
    private SolrClient client;

    private static final String COLLECTION = "cordis-doctopics";

    @Before
    public void setup(){
        settings    = new TestSettings();
        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));

    }


    @Test
    public void execute() throws IOException, SolrServerException {



    }

}
