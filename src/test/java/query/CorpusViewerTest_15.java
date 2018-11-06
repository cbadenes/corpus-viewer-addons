package query;

import es.gob.minetad.doctopic.CleanZeroEpsylonIndex;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.Alarm;
import es.gob.minetad.model.Document;
import es.gob.minetad.model.Score;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;
import es.gob.minetad.utils.ReaderUtils;
import es.gob.minetad.utils.WriterUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * http://localhost:8983/solr/cordis-doctopics-70/terms?terms.fl=hashcode0&wt=xml&terms.mincount=2&terms.limit=100
 * http://localhost:8983/solr/cordis-doctopics-70/terms?terms.fl=hashcode5&wt=xml&terms.mincount=2&terms.limit=100
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CorpusViewerTest_15 {

    private static final Logger LOG = LoggerFactory.getLogger(CorpusViewerTest_15.class);
    private TestSettings settings;
    private SolrClient client;

    @Before
    public void setup(){
        settings    = new TestSettings();
        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
    }
    


    /**
     * Given a corpus, display the number of similar documents grouped by alarm type:
     * 0: highest similarity
     * 1: high simmilarity
     * 2: medium-high similarity
     * 3: medium similarity
     * 4: medium-low similarity
     * 5: low similarity
     */
    @Test
    public void listAlarms() throws IOException, SolrServerException {

        String corpus = "cordis-doctopics-70";

        List<Integer> alarmTypes = Arrays.asList(0,1,2,3,4,5);

        for(Integer alarmType : alarmTypes){

            Alarm alarm = getAlarmsBy(alarmType, corpus, client);
            LOG.info(""+alarm);
            alarm.getGroups().entrySet().stream().sorted((a,b) -> -a.getValue().compareTo(b.getValue())).limit(10).forEach(entry -> LOG.info("\t " + entry.getValue() + " docs grouped by hashcode: " + entry.getKey() + ""));

        }

    }

    public static Alarm getAlarmsBy(Integer alarmType, String corpus, SolrClient client) throws IOException, SolrServerException {
        String fieldName = "hashcode"+alarmType;
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/terms");
        query.setTerms(true);
        query.setTermsLimit(1000);
        query.addTermsField(fieldName);
        query.setTermsMinCount(2);

        List<TermsResponse.Term> terms = client.query(corpus,query).getTermsResponse().getTerms(fieldName);

        Alarm alarm = new Alarm(alarmType);
        terms.forEach(term -> alarm.addGroup(term.getTerm(), term.getFrequency()));
        return alarm;

    }


    /**
     * Given a group (hashcode) belonging to an alarm type (integer), display the list of similar documents in a corpus
     */
    @Test
    public void listDocuments() throws IOException, SolrServerException {

        Integer alarmType   = 0;
        String hashcode     = "953593165";
        String corpus       = "cordis-doctopics-70";


        List<SolrDocument> documents = getDocumentsBy(alarmType, corpus, hashcode, client);

        documents.forEach(doc -> LOG.info("-" + doc));
    }


    public static List<SolrDocument> getDocumentsBy(Integer alarmType, String corpus, String group, SolrClient client) throws IOException, SolrServerException {
        String fieldName = "hashcode"+alarmType;
        SolrQuery query = new SolrQuery();
        query.set("q",fieldName+":"+group.replace("-","\\-"));
        query.setRows(1000);

        QueryResponse response = client.query(corpus, query);
        return response.getResults();
    }

}
