package query;

import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * some examples of queries to read documents by their topic distributions in a hierarchical way
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class HierarchicalQueriesTest {

    private static final Logger LOG = LoggerFactory.getLogger(HierarchicalQueriesTest.class);
    private TestSettings settings;
    private SolrClient client;

    private static final String COLLECTION = "cordis-doctopics";

    @Before
    public void setup(){
        settings    = new TestSettings();
        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));

    }

    @Test
    /**
     * groups of topics that are dealt with together being the main topics of a document
     *
     * http://localhost:8983/solr/cordis-doctopics/terms?terms.fl=hashexpr5_txt&wt=xml&terms.mincount=2&terms.limit=100
     *
     */
    public void getMainTopicGroups() throws IOException, SolrServerException {
        List<TermsResponse.Term> terms = getTopicStats("hashexpr5_txt");

        terms.stream().filter(t -> t.getTerm().contains("_")).forEach(t -> LOG.info("Topics: " + t.getTerm() + " as main topics in " + t.getFrequency() + " docs"));
    }

    @Test
    /**
     * groups of topics that are dealt with together being the background topics of a document
     *
     */
    public void getBackgroundTopicGroups() throws IOException, SolrServerException {
        List<TermsResponse.Term> level1 = getTopicStats("hashexpr1_txt");
        List<TermsResponse.Term> level2 = getTopicStats("hashexpr2_txt");

        Map<String,Integer> map = new HashMap<>();
        level2.stream().filter(t -> t.getTerm().contains("_")).forEach(t -> map.put(t.getTerm(),1));
        level1.stream().filter(t -> t.getTerm().contains("_")).filter(t -> !map.containsKey(t.getTerm())).forEach(t -> LOG.info("Topics: " + t.getTerm() + " as background topics in " + t.getFrequency() + " docs"));
    }


    @Test
    /**
     * documents which deal mainly some topics in the same way and which, in the background, deal with others also in similar proportions.
     */
    public void getDocumentsByGroupsOfTopics() throws IOException, SolrServerException {

        String mainTopics       = concatenate(Arrays.asList("t12","t20"));
        String backgroundTopics = concatenate(Arrays.asList("t47","t52","t58"));

        StringBuilder query = new StringBuilder();
        // main topics
        query.append("hashexpr5_txt:").append(mainTopics).append(" AND ");
        // background topics
        query.append("(hashexpr4_txt:").append(backgroundTopics).append(" OR ").append(" hashexpr3_txt:").append(backgroundTopics).append(")");

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRows(500);
        solrQuery.setQuery(query.toString());
        solrQuery.addSort("id", SolrQuery.ORDER.asc);
//        solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
        QueryResponse rsp = client.query(COLLECTION,solrQuery);

        if ((rsp.getResults() == null) || (rsp.getResults().isEmpty())) {
            LOG.info("No documents found");
            return;
        }

        LOG.info("Found " + rsp.getResults().size() + " documents: ");

        for(SolrDocument doc: rsp.getResults()){
            LOG.info("Document: " + doc);
        }

    }




    private List<TermsResponse.Term> getTopicStats(String fieldName) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/terms");
        query.setTerms(true);
        query.setTermsLimit(1000);
        query.addTermsField(fieldName);
        query.setTermsMinCount(2);

        return client.query(COLLECTION,query).getTermsResponse().getTerms(fieldName);
    }


    private String concatenate(List<String> topics){
        return topics.stream().sorted((a,b) -> -a.compareTo(b)).collect(Collectors.joining("_"));
    }

}
