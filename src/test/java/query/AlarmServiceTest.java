package query;

import com.google.common.collect.MinMaxPriorityQueue;
import es.gob.minetad.doctopic.DocTopicsIndex;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.*;
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
import java.util.concurrent.atomic.AtomicInteger;
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
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AlarmServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmServiceTest.class);
    private TestSettings settings;
    private SolrClient client;
    private Map<Integer,String> alarmTypes;



    private static final String COLLECTION = "doctopics";

    @Before
    public void setup(){
        settings    = new TestSettings();
        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
        alarmTypes  = new HashMap<>();
        alarmTypes.put(1,"critical");
        alarmTypes.put(3,"high");
        alarmTypes.put(5,"low");
    }


    @Test
    public void execute() throws IOException, SolrServerException {

        int sampleSize = 10;

        for(Integer alarmType : alarmTypes.keySet().stream().sorted().collect(Collectors.toList())){
            // Read groups of documents
            Alarm alarm = getAlarmsBy(alarmType, COLLECTION, client);
            LOG.info("'"+alarmTypes.get(alarmType) + "' similarity in documents: ");
            for(String group : alarm.getGroups().entrySet().stream().sorted((a,b) -> -a.getValue().compareTo(b.getValue())).limit(sampleSize).map(e -> e.getKey()).collect(Collectors.toList())){
                LOG.info("\t > by hashcode [" + group + "]:");
                // Read documents by hash
                getDocumentsBy(alarmType, COLLECTION, group, sampleSize, client).forEach(doc -> LOG.info("\t\t-" + doc.getFieldValue("id")));
            }

        }

    }


    /**
     * http://localhost:8983/solr/cordis-doctopics-70/terms?terms.fl=hashcode0&wt=xml&terms.mincount=2&terms.limit=100
     * @param alarmType
     * @param corpus
     * @param client
     * @return
     * @throws IOException
     * @throws SolrServerException
     */
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


    public static List<SolrDocument> getDocumentsBy(Integer alarmType, String corpus, String group, Integer max, SolrClient client) throws IOException, SolrServerException {
        String fieldName = "hashcode"+alarmType;
        SolrQuery query = new SolrQuery();
        query.set("q",fieldName+":"+group.replace("-","\\-"));
        query.setRows(max);

        QueryResponse response = client.query(corpus, query);
        return response.getResults();
    }

}
