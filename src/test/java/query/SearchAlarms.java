package query;

import es.gob.minetad.doctopic.CleanZeroEpsylonIndex;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.Document;
import es.gob.minetad.model.Score;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;
import es.gob.minetad.utils.ReaderUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.Hash;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * http://localhost:8983/solr/cordis-doctopics-70/terms?terms.fl=hashcode&wt=xml
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SearchAlarms {

    private static final Logger LOG = LoggerFactory.getLogger(SearchAlarms.class);


    @Test
    public void execute() throws IOException, SolrServerException {

        TestSettings settings = new TestSettings();

        String fieldName = "hashcodeQ1";

        SolrClient client = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));

        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/terms");
        query.setTerms(true);
        query.setTermsLimit(100);
//        query.setTermsLower("s");
//        query.setTermsPrefix("s");
        //query.addTermsField("hashcode");
        query.addTermsField(fieldName);
        query.setTermsMinCount(2);

//        QueryRequest request = new QueryRequest(query);
//        List<TermsResponse.Term> terms = request.process(client).getTermsResponse().getTerms("hashcode");


            int numTopics                   = 70;
            float epsylon = 1f / numTopics;
            float multiplicationFactor = Double.valueOf(1 * Math.pow(10, String.valueOf(numTopics).length() + 1)).floatValue();
            CleanZeroEpsylonIndex docTopicIndexer = new CleanZeroEpsylonIndex(numTopics, multiplicationFactor, epsylon);



        List<TermsResponse.Term> terms = client.query("cordis-doctopics-70",query).getTermsResponse().getTerms(fieldName);

        LOG.info("Terms: " + terms);

        for (TermsResponse.Term term : terms){

            String hashcode = term.getTerm();
            long numDocs = term.getFrequency();

            LOG.info("" + numDocs + " share the hashcode: " + hashcode + ":  ");

            SolrQuery q1 = new SolrQuery();
            q1.set("q",fieldName+":"+hashcode.replace("-","\\-"));
            q1.setRows(100);
//            q1.setFields("id doctopic");



            QueryResponse response = client.query("cordis-doctopics-70", q1);



            final SolrDocumentList documents = response.getResults();

            Map<String,List<Double>> vectors = new HashMap();
            for(SolrDocument document : documents) {
                String id = (String) document.getFieldValue("id");
                String doctopics = (String) document.getFieldValues("doctopic").iterator().next();
                List<Double> vector = docTopicIndexer.toVector(doctopics);

                for(Map.Entry<String,List<Double>> d2: vectors.entrySet()){
                    LOG.info(id + "<-> "+ d2.getKey() + ": " + JensenShannon.similarity(vector, d2.getValue()));
                }
                vectors.put(id, vector);
            }


        }

    }

    @Test
    public void reference() throws IOException {

        String path = "https://delicias.dia.fi.upm.es/nextcloud/index.php/s/HFQRTXt5K9CfGDf/download";

        BufferedReader reader = ReaderUtils.from(path);
        String line;
        List<Score> topSimilar = new ArrayList<>();
        Double threshold = 0.9999;
        List<Document> documents = new ArrayList();
        AtomicInteger counter = new AtomicInteger();
        while((line = reader.readLine()) != null){

            String[] vals = line.split(",");
            String id = vals[0];
            List<Double> vector = new ArrayList<>();
            for(int i=1; i<vals.length;i++){
                vector.add(Double.valueOf(vals[i]));
            }
            Document d1 = new Document(id);
            d1.setShape(vector);
            topSimilar.addAll(documents.parallelStream().map(d2 -> new Score(JensenShannon.similarity(vector,d2.getShape()), d2, d1)).filter(s -> s.getValue()>=threshold).collect(Collectors.toList()));
            documents.add(d1);
            if (counter.incrementAndGet() % 100 == 0 ) LOG.info(counter.get() + " docs analyzed");
        }

        LOG.info("Top Similar Documents: ");
        for(Score score: topSimilar){
            LOG.info("-> " + score);
        }

    }

}
