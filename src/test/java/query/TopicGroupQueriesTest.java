package query;

import com.google.common.base.Strings;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * some examples of queries to read documents by their topic distributions in a clustering way
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicGroupQueriesTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicGroupQueriesTest.class);
    private TestSettings settings;
    private SolrClient client;

    private static final String COLLECTION = "cordis-doctopics";

    @Before
    public void setup(){
        settings    = new TestSettings();
        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));

    }

    /**
     * groups of topics that are dealt with together in a document
     *
     * http://localhost:8983/solr/cordis-doctopics/terms?terms.fl=hashexpr5_txt&wt=xml&terms.mincount=2&terms.limit=100
     *
     */
    @Test
    public void getTopicGroups() throws IOException, SolrServerException {
        List<TermsResponse.Term> terms = getTopicStats("hashexpr0_txt","");

        terms.stream().filter(t -> t.getTerm().contains("_")).forEach(t -> LOG.info("Topics: " + t.getTerm() + " in " + t.getFrequency() + " docs"));
    }

    /**
     * topics in a document having similar proportion as one given
     *
     * http://localhost:8983/solr/cordis-doctopics/terms?terms.fl=hashexpr0_txt&wt=xml&terms.mincount=2&terms.limit=100&terms.regex=.*t20.*
     */
    @Test
    public void getRelatedTopics() throws IOException, SolrServerException {
        List<TermsResponse.Term> terms = getTopicStats("hashexpr0_txt",".*t20.*");

        terms.stream().filter(t -> t.getTerm().contains("_")).forEach(t -> LOG.info("Topics: " + t.getTerm() + " in " + t.getFrequency() + " docs"));

    }

    private List<TermsResponse.Term> getTopicStats(String fieldName, String regex) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/terms");
        query.setTerms(true);
        query.setTermsLimit(1000);
        query.addTermsField(fieldName);
        if (!Strings.isNullOrEmpty(regex)) query.setTermsRegex(regex);
        query.setTermsMinCount(2);

        return client.query(COLLECTION,query).getTermsResponse().getTerms(fieldName);
    }


    private String concatenate(List<String> topics){
        return topics.stream().sorted((a,b) -> -a.compareTo(b)).collect(Collectors.joining("_"));
    }

}
