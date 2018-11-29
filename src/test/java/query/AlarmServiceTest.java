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

public class AlarmServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmServiceTest.class);
    private TestSettings settings;
    private SolrClient client;
    private Map<Integer,String> alarmTypes;



    private static final String COLLECTION = "cordis-doctopics";

    @Before
    public void setup(){
        settings    = new TestSettings();
        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
        alarmTypes  = new HashMap<>();
        alarmTypes.put(0,"critical");
        alarmTypes.put(1,"high");
        alarmTypes.put(2,"medium");
        alarmTypes.put(3,"low");
    }


    @Test
    public void execute() throws IOException, SolrServerException {

        int sampleSize = 0;

        for(Integer alarmType : alarmTypes.keySet().stream().sorted().collect(Collectors.toList())){
            // Read groups of documents
            Alarm alarm = getAlarmsBy(alarmType, COLLECTION, client);
            LOG.info(alarm.getTotal() + " '"+alarmTypes.get(alarmType) + "' similarities found: ");
            for(String group : alarm.getGroups().entrySet().stream().sorted((a,b) -> -a.getValue().compareTo(b.getValue())).limit(sampleSize).map(e -> e.getKey()).collect(Collectors.toList())){
                LOG.info("\t > similar docs by hashcode [" + group + "]:");
                // Read documents by hash
                List<SolrDocument> docs = getDocumentsBy(alarmType, COLLECTION, group, sampleSize, client);
                docs.forEach(doc -> LOG.info("\t\t-" + doc.getFieldValue("id") + " - '"+ doc.getFieldValue("name_s") + "'"));
            }

        }

    }


    public static Alarm getAlarmsBy(Integer alarmType, String corpus, SolrClient client) throws IOException, SolrServerException {
        String fieldName = "hashcode"+alarmType+"_i";
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
    /*
     * calculo agrupacion alarmas  
     */
    public static void getDocumentsList(Integer alarmType, String corpus, SolrClient client) throws IOException, SolrServerException{
    	String fieldName = "hashcode"+alarmType+"_i";
    	SolrQuery query = new SolrQuery();
    	query.setRequestHandler("/select");
    	query.set("q","*:*");
    	query.set("qt", "terms");
    	query.set("terms.fl", fieldName);
    	query.set("terms","true");
    	query.set("fieldName","name_s");
    	query.addField("alar:[alarmas]");
    	query.set("terms.mincount", "2");
    	query.setRows(1);
    	List<SolrDocument> p=client.query(corpus,query).getResults();
    	System.out.println();
    	p.forEach(doc -> LOG.info("-" + doc.getFieldValue("alar")));
    
    }

    public static List<SolrDocument> getDocumentsBy(Integer alarmType, String corpus, String group, Integer max, SolrClient client) throws IOException, SolrServerException {
        String fieldName = "hashcode"+alarmType+"_i";
        SolrQuery query = new SolrQuery();
        query.set("q",fieldName+":"+group.replace("-","\\-"));
        query.setFields("id","name_s","hashcode1_i","listaBO");
        query.setRows(max);

        QueryResponse response = client.query(corpus, query);
        return response.getResults();
    }

}
