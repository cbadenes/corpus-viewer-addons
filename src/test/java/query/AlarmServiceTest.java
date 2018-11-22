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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * Set of queries to obtain documents as candidates to be duplicated.
 *
 * Before run the tests you must start the Solr service!
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AlarmServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmServiceTest.class);
    private TestSettings settings;
    private SolrClient client;


    private static final String CORPUS = "doctopics";

    @Before
    public void setup(){
        settings    = new TestSettings();
        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
    }


    /**
     * Given a corpus, display the number of similar documents grouped by alarm type:
     * 1: critical
     * 3: high
     * 5: low
     */
    @Test
    public void listAlarms() throws IOException, SolrServerException {

        List<Integer> alarmTypes = Arrays.asList(1,3,5);

        for(Integer alarmType : alarmTypes){

            Alarm alarm = getAlarmsBy(alarmType, CORPUS, client);
            LOG.info(""+alarm);
            alarm.getGroups().entrySet().stream().sorted((a,b) -> -a.getValue().compareTo(b.getValue())).limit(10).forEach(entry -> LOG.info("\t " + entry.getValue() + " docs grouped by hashcode: " + entry.getKey() + ""));

        }

    }

    /**
     * Given a group (hashcode) belonging to an alarm type (integer), display the list of documents for that group
     *
     */
    @Test
    public void listDocuments() throws IOException, SolrServerException {

        Integer alarmType   = 0;
        String hashcode     = "1478773850";


        List<SolrDocument> documents = getDocumentsBy(alarmType, CORPUS, hashcode, client);

        documents.forEach(doc -> LOG.info("-" + doc));
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


    public static List<SolrDocument> getDocumentsBy(Integer alarmType, String corpus, String group, SolrClient client) throws IOException, SolrServerException {
        String fieldName = "hashcode"+alarmType;
        SolrQuery query = new SolrQuery();
        query.set("q",fieldName+":"+group.replace("-","\\-"));
        query.setRows(1000);

        QueryResponse response = client.query(corpus, query);
        return response.getResults();
    }

    public static List<Similarity> getSimilarDocumentsBy(int alarmType, String corpus, DocTopicsIndex indexer, AtomicInteger counter, SolrClient client) throws IOException, SolrServerException {
        // Density-based Approach Evaluation
        MinMaxPriorityQueue<Similarity> densityPairs = MinMaxPriorityQueue.orderedBy(new Similarity.ScoreComparator()).maximumSize(10).create();
        Alarm alarms = getAlarmsBy(alarmType, corpus, client);
        for(String hashcode: alarms.getGroups().keySet()){

            List<SolrDocument> docs = getDocumentsBy(alarmType, corpus, hashcode, client);

            for(int i=0; i< docs.size(); i++){
                final DocTopic dt1      = DocTopic.from(docs.get(i));
                final List<Double> v1   = indexer.toVector(dt1.getTopics());
                for(int j=0; j<i; j++){
                    final DocTopic dt2      = DocTopic.from(docs.get(j));
                    final List<Double> v2   = indexer.toVector(dt2.getTopics());
                    densityPairs.add(new Similarity(JensenShannon.similarity(v1,v2),new Document(dt1.getId(), dt1.getHash()), new Document(dt2.getId(), dt2.getHash())));
                    counter.incrementAndGet();
                }
            }
        }
        List<Similarity> topSimilarDocsByDensity = densityPairs.stream().sorted((a, b) -> -a.getScore().compareTo(b.getScore())).collect(Collectors.toList());
        return topSimilarDocsByDensity;
    }

}
