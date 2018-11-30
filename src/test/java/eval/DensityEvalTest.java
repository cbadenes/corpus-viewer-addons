package eval;

import com.google.common.collect.MinMaxPriorityQueue;
import es.gob.minetad.doctopic.DocTopicIndexFactory;
import es.gob.minetad.doctopic.DocTopicsIndex;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.*;
import es.gob.minetad.solr.SolrClientFactory;
import es.gob.minetad.solr.SolrUtils;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.TimeUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.io.stream.ParallelStream;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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


    private static final Integer MAX_HASH_LEVEL = 6;
    private static final Integer SAMPLE_SIZE    = 1000;
    private static final Integer TOP_SIZE       = 10;
    private DocTopicsIndex docTopicIndexer;

    @Before
    public void setup(){
        settings                = new TestSettings();
        client                  = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
        docTopicIndexer         = DocTopicIndexFactory.newFrom(NUM_TOPICS);
    }


    @Test
    public void execute() throws IOException, SolrServerException {
        // get a sample list of documents

        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/select");
        query.set("q","*:*");
        query.setRows(SAMPLE_SIZE);
        QueryResponse rsp = client.query(CORPUS, query);
        ConcurrentHashMap<Integer,List<Evaluation>> evaluations = new ConcurrentHashMap<>();
        IntStream.range(0,MAX_HASH_LEVEL).forEach(i -> evaluations.put(i,new ArrayList<>()));

        ConcurrentLinkedQueue<Double> refSimilarities = new ConcurrentLinkedQueue<>();

        ConcurrentHashMap<Integer,List<Double>> hashSimilaritiesMap = new ConcurrentHashMap<>();
        IntStream.range(0,MAX_HASH_LEVEL).forEach(i -> hashSimilaritiesMap.put(i,new ArrayList<>()));

        ConcurrentHashMap<Integer,List<Integer>> hashSizeMap = new ConcurrentHashMap<>();
        IntStream.range(0,MAX_HASH_LEVEL).forEach(i -> hashSizeMap.put(i,new ArrayList<>()));

        AtomicInteger counter = new AtomicInteger();
        ParallelExecutor executor = new ParallelExecutor();
        for(SolrDocument doc: rsp.getResults()) {

            final SolrDocument d1 = doc;
            executor.submit(() -> {
                LOG.info("Creating statistics for document "+ counter.incrementAndGet() + "/" + SAMPLE_SIZE);
                Document document = new Document();
                document.setId((String) d1.getFieldValue("id"));
                document.setShape(docTopicIndexer.toVector((String) d1.getFieldValue("listaBM")));
                document.setName((String) d1.getFieldValue("name_s"));

                List<Similarity> groundTruth = getTop(document, TOP_SIZE);
                refSimilarities.addAll(groundTruth.stream().map(d -> d.getScore()).collect(Collectors.toList()));

                // by HashCode
                Map<Integer,List<String>> hashCodeMap = new HashMap<>();
                for(int i=0; i< MAX_HASH_LEVEL; i++){
                    List<Document> docs = getDocumentsByHashCode((Integer) d1.getFieldValue("hashcode" + i + "_i"), i);
                    hashCodeMap.put(i, docs.stream().map(d -> d.getId()).collect(Collectors.toList()));
                    // calculate similarities
                    hashSimilaritiesMap.put(i,docs.stream().map(d -> JensenShannon.similarity(d.getShape(), document.getShape())).collect(Collectors.toList()));
                    hashSizeMap.get(i).add(docs.size());
                }


                List<String> goldStandard = groundTruth.stream().map(s -> s.getD2().getId()).collect(Collectors.toList());
//                    LOG.debug("Precision@"+precision + ": ["+ reference.get(0).getScore() + " - " + reference.get(reference.size()-1).getScore() + "]");

                for(Integer hashLevel : hashCodeMap.keySet().stream().sorted().collect(Collectors.toList())){
                    Evaluation evaluation = new Evaluation(Instant.now(), Instant.now(), goldStandard, hashCodeMap.get(hashLevel), TOP_SIZE);
//                    LOG.debug("Evaluation by HashCode "+ hashLevel+ ": " + evaluation);
                    evaluations.get(hashLevel).add(evaluation);
                }
            });

        }
        executor.awaitTermination(1, TimeUnit.HOURS);

        LOG.info("Summary:");
        for(Integer hashLevel: evaluations.keySet()){
            LOG.info("\thashcode" + hashLevel+ ": ");
            List<Evaluation> hashEvaluations = evaluations.get(hashLevel);

            Double totalPrecision       = hashEvaluations.stream().map(eval -> eval.getPrecision()).reduce((a, b) -> a + b).get();
            Double totalRecall          = hashEvaluations.stream().map(eval -> eval.getRecall()).reduce((a, b) -> a + b).get();
            Double totalfMeasure        = hashEvaluations.stream().map(eval -> eval.getFMeasure()).reduce((a, b) -> a + b).get();
            Double totalEvals           = Double.valueOf(hashEvaluations.size());
            Double totalRefSimilarity   = refSimilarities.stream().reduce((a,b) -> a+b).get();
            Double totalGroupSimilarity = hashSimilaritiesMap.get(hashLevel).stream().reduce((a,b) -> a+b).get();
            Double totalGroupElements   = Double.valueOf(hashSizeMap.get(hashLevel).stream().reduce((a,b) -> a+b).get());


            LOG.info("\t\tref-similarity ratio: " + totalRefSimilarity / Double.valueOf(refSimilarities.size()));
            LOG.info("\t\thash-similarity ratio: " + totalGroupSimilarity/ Double.valueOf(hashSimilaritiesMap.get(hashLevel).size()));
            LOG.info("\t\thash-size ratio: " + totalGroupElements/ Double.valueOf(hashSizeMap.get(hashLevel).size()));
            LOG.info("\t\tprecision ratio: " + totalPrecision / totalEvals);
            LOG.info("\t\trecall ratio: " + totalRecall / totalEvals);
            LOG.info("\t\tfMeasure ratio: " + totalfMeasure / totalEvals);
        }
    }


    private List<Similarity> getTop(Document doc, Integer num)  {
        try {
            MinMaxPriorityQueue<Similarity> pairs = MinMaxPriorityQueue.orderedBy(new Similarity.ScoreComparator()).maximumSize(num).create();
            List<Double> v1 = doc.getShape();

//            LOG.debug("Calculating similarity between "+ doc.getId() + " and all documents ..");
            Instant s1 = Instant.now();
            SolrUtils.Executor compareToDoc = d -> {
                List<Double> v2 = docTopicIndexer.toVector((String) d.getFieldValue("listaBM"));
                //pairs.add(new Similarity(JensenShannonExt.similarity(v1,v2),doc, new Document((String)d.getFieldValue("id"), (String)d.getFieldValue("hashexpr1_t"))));
                pairs.add(new Similarity(JensenShannon.similarity(v1,v2),doc, new Document((String)d.getFieldValue("id"), (String)d.getFieldValue("hashexpr1_t"))));

            };
            SolrUtils.iterate(CORPUS, "*:*", client, compareToDoc);
            Instant e1 = Instant.now();
//            TimeUtils.print(s1,e1,"All similarities from document '" + doc.getId() + "' calculated in: ");
            return  pairs.stream().sorted((a,b) -> -a.getScore().compareTo(b.getScore())).collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Unexpected error",e);
            return Collections.emptyList();
        }
    }

    private List<Document> getDocumentsByHashCode(Integer hashcode, Integer level)  {
        try {
            SolrQuery query = new SolrQuery();
            query.setRequestHandler("/select");
            String hashcodeString = hashcode <0 ? String.valueOf(hashcode).replace("-","\\-") : String.valueOf(hashcode);
            query.set("q","hashcode"+level+"_i:"+hashcodeString);
            query.setFields("id","listaBM");
            query.setRows(Integer.MAX_VALUE);
            QueryResponse rsp = client.query(CORPUS, query);
            return rsp.getResults().parallelStream().map(d -> new Document((String) d.getFieldValue("id"), docTopicIndexer.toVector((String) d.getFieldValue("listaBM")))).collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Unexpected error",e);
            return Collections.emptyList();
        }
    }

}
