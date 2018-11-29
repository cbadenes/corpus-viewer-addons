package eval;

import com.google.common.collect.MinMaxPriorityQueue;
import es.gob.minetad.doctopic.DocTopicIndexFactory;
import es.gob.minetad.doctopic.DocTopicsIndex;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.*;
import es.gob.minetad.solr.SolrClientFactory;
import es.gob.minetad.solr.SolrUtils;
import es.gob.minetad.utils.TimeUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * Analyze performance with respect to brute-force approach
 * Requirements:
 * 1. Start Solr (docker container)
 * 2. Create Collections (create-collections.sh)
 * 3. Load DocTopics (unit-test LoadDocTopics)
 *
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DensityEvalTest {

    private static final Logger LOG = LoggerFactory.getLogger(DensityEvalTest.class);
    private TestSettings settings;
    private SolrClient client;


    private static final String CORPUS          = "cordis-doctopics";
    private static final Integer NUM_TOPICS     = 70;


    private static final Integer MAX_HASH_LEVEL = 5;
    private static final Integer SAMPLE_SIZE    = 1000;
    private static final Integer TOP_SIZE       = 100;
    private DocTopicsIndex docTopicIndexer;

    @Before
    public void setup(){
        settings                = new TestSettings();
        client                  = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
        docTopicIndexer         = DocTopicIndexFactory.newFrom(NUM_TOPICS);
    }


    @Test
    public void execute() throws IOException, SolrServerException {

        MinMaxPriorityQueue<Similarity> pairs = MinMaxPriorityQueue.orderedBy(new Similarity.ScoreComparator()).maximumSize(100).create();



        // get a sample list of documents

        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/select");
        query.set("q","*:*");
        query.setRows(SAMPLE_SIZE);
        QueryResponse rsp = client.query(CORPUS, query);
        Map<Integer,List<Evaluation>> evaluations = new HashMap<>();
        IntStream.range(0,MAX_HASH_LEVEL+1).forEach(i -> evaluations.put(i,new ArrayList<>()));

        for(SolrDocument doc: rsp.getResults()) {

            Document document = new Document();
            document.setId((String) doc.getFieldValue("id"));
            document.setShape(docTopicIndexer.toVector((String) doc.getFieldValue("listaBM")));
            document.setName((String) doc.getFieldValue("name_s"));

            List<Similarity> groundTruth = getTop(document, TOP_SIZE);

            // by HashCode
            Map<Integer,List<String>> hashCodeMap = new HashMap<>();
            IntStream.range(0,MAX_HASH_LEVEL+1).forEach(i -> hashCodeMap.put(i, getDocumentsByHashCode((Integer) doc.getFieldValue("hashcode"+i+"_i"), i) ));


            for (Integer precision : Arrays.asList(10)) {

                List<Similarity> reference = groundTruth.stream().limit(precision).map(s -> s).collect(Collectors.toList());
                List<String> goldStandard = reference.stream().map(s -> s.getD2().getId()).collect(Collectors.toList());
                LOG.info("Precision@"+precision + ": ["+ reference.get(0).getScore() + " - " + reference.get(reference.size()-1).getScore() + "]");

                for(Integer hashLevel : hashCodeMap.keySet().stream().sorted().collect(Collectors.toList())){
                    Evaluation evaluation = new Evaluation(Instant.now(), Instant.now(), goldStandard, hashCodeMap.get(hashLevel), precision);
                    //LOG.debug("Evaluation by HashCode "+ hashLevel+ ": " + evaluation);
                    evaluations.get(hashLevel).add(evaluation);
                }

            }

        }

        LOG.info("Summary:");
        for(Integer hashLevel: evaluations.keySet()){
            LOG.info("\thashcode" + hashLevel+ ": ");
            List<Evaluation> hashEvaluations = evaluations.get(hashLevel);

            Double totalPrecision   = hashEvaluations.stream().map(eval -> eval.getPrecision()).reduce((a, b) -> a + b).get();
            Double totalRecall      = hashEvaluations.stream().map(eval -> eval.getRecall()).reduce((a, b) -> a + b).get();
            Double totalfMeasure    = hashEvaluations.stream().map(eval -> eval.getFMeasure()).reduce((a, b) -> a + b).get();
            Double totalEvals       = Double.valueOf(hashEvaluations.size());

            LOG.info("\t\tprecision ratio: " + totalPrecision / totalEvals);
            LOG.info("\t\trecall ratio: " + totalRecall / totalEvals);
            LOG.info("\t\tfMeasure ratio: " + totalfMeasure / totalEvals);
        }
    }


    private List<Similarity> getTop(Document doc, Integer num)  {
        try {
            MinMaxPriorityQueue<Similarity> pairs = MinMaxPriorityQueue.orderedBy(new Similarity.ScoreComparator()).maximumSize(num).create();
            List<Double> v1 = doc.getShape();

            LOG.info("Calculating similarity between "+ doc.getId() + " and all documents ..");
            Instant s1 = Instant.now();
            SolrUtils.Executor compareToDoc = d -> {
                List<Double> v2 = docTopicIndexer.toVector((String) d.getFieldValue("listaBM"));
                //pairs.add(new Similarity(JensenShannonExt.similarity(v1,v2),doc, new Document((String)d.getFieldValue("id"), (String)d.getFieldValue("hashexpr1_t"))));
                pairs.add(new Similarity(JensenShannon.similarity(v1,v2),doc, new Document((String)d.getFieldValue("id"), (String)d.getFieldValue("hashexpr1_t"))));

            };
            SolrUtils.iterate(CORPUS, "*:*", client, compareToDoc);
            Instant e1 = Instant.now();
            TimeUtils.print(s1,e1,"All similarities from document '" + doc.getId() + "' calculated in: ");
            return  pairs.stream().sorted((a,b) -> -a.getScore().compareTo(b.getScore())).collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Unexpected error",e);
            return Collections.emptyList();
        }
    }

    private List<String> getDocumentsByHashCode(Integer hashcode, Integer level)  {
        try {
            SolrQuery query = new SolrQuery();
            query.setRequestHandler("/select");
            String hashcodeString = hashcode <0 ? String.valueOf(hashcode).replace("-","\\-") : String.valueOf(hashcode);
            query.set("q","hashcode"+level+"_i:"+hashcodeString);
            query.setFields("id");
            query.setRows(Integer.MAX_VALUE);
            QueryResponse rsp = client.query(CORPUS, query);
            return rsp.getResults().parallelStream().map(d -> (String) d.getFieldValue("id")).collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Unexpected error",e);
            return Collections.emptyList();
        }
    }

}
