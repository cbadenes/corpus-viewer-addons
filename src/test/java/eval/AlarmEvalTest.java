package eval;

import com.google.common.collect.MinMaxPriorityQueue;
import es.gob.minetad.doctopic.CleanZeroEpsylonIndex;
import es.gob.minetad.doctopic.DocTopicIndexFactory;
import es.gob.minetad.doctopic.DocTopicsIndex;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.*;
import es.gob.minetad.solr.SolrClientFactory;
import es.gob.minetad.solr.SolrUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.AlarmServiceTest;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

public class AlarmEvalTest {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmEvalTest.class);
    private TestSettings settings;
    private SolrClient client;


    private static final String CORPUS      = "cordis-doctopics";
    private static final Integer NUM_TOPICS = 70;

    @Before
    public void setup(){
        settings    = new TestSettings();
        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
    }


    @Test
    public void execute() throws IOException, SolrServerException {

        MinMaxPriorityQueue<Similarity> pairs = MinMaxPriorityQueue.orderedBy(new Similarity.ScoreComparator()).maximumSize(100).create();

        DocTopicsIndex indexer = DocTopicIndexFactory.newFrom(NUM_TOPICS);

        AtomicInteger bruteForceCounter = new AtomicInteger();
        SolrUtils.Executor bruteForceComparison = d1 -> {
            try {
                final DocTopic dt1      = DocTopic.from(d1);
                final List<Double> v1 = indexer.toVector(dt1.getTopics());
                SolrUtils.Executor similarityComparison = d2 -> {
                    bruteForceCounter.incrementAndGet();
                    final DocTopic dt2      = DocTopic.from(d2);
                    final List<Double> v2   = indexer.toVector(dt2.getTopics());
                    pairs.add(new Similarity(JensenShannon.similarity(v1,v2), new Document(dt1.getId(),dt1.getHash()), new Document(dt2.getId(), dt2.getHash())));
                };
                SolrUtils.iterate(CORPUS, "id:{* TO " + dt1.getId() + "}", client, similarityComparison);
            } catch (Exception e) {
                LOG.error("Unexpected error", e);

            }
        };

        LOG.info("Calculating similarity among all pairs of documents ..");
        Instant s1 = Instant.now();
        SolrUtils.iterate(CORPUS, "*:*", client, bruteForceComparison);
        Instant e1 = Instant.now();

        double similarityThreshold = 0.98;
        List<Similarity> topSimilarDocs = pairs.stream().sorted((a, b) -> -a.getScore().compareTo(b.getScore())).filter(s -> s.getScore()>similarityThreshold).collect(Collectors.toList());
        List<String> groundTruth = topSimilarDocs.stream().map(sim -> sim.getPair()).collect(Collectors.toList());
        topSimilarDocs.forEach(sim -> LOG.info("Top Similarity: " + sim));
        LOG.info("Ground Truth size: " + groundTruth.size());


        // Evaluations
        LOG.info("Brute-Force Approach: " + new Evaluation(s1, e1, groundTruth, groundTruth,bruteForceCounter.get()));
        for(Integer accuracy: Arrays.asList(1,5,10)){

            // Density-based Approach Evaluation
            for(Integer alarmType: Arrays.asList(0,1,2,3,4,5)){
                Instant start = Instant.now();
                AtomicInteger densityCounter = new AtomicInteger();
                List<Similarity> topSimilarDocsByDensity = AlarmEvalTest.getSimilarDocumentsBy(alarmType, CORPUS, indexer, densityCounter, client);
                Instant end = Instant.now();
                LOG.info("Density-based["+alarmType+"] Approach@"+accuracy+": " + new Evaluation(start, end, groundTruth, topSimilarDocsByDensity.stream().limit(accuracy).map(sim -> sim.getPair()).collect(Collectors.toList()), densityCounter.get()));
            }
            //agrupacion de alarmas calculo  
            for(Integer alarmType: Arrays.asList(0,1,2,3,4,5)){
            	AlarmServiceTest.getDocumentsList(alarmType, CORPUS, client);
            }
            
        }

        // Threshold-based Approach Evaluation
        MinMaxPriorityQueue<Similarity> p3 = MinMaxPriorityQueue.orderedBy(new Similarity.ScoreComparator()).maximumSize(10).create();
        Instant s3 = Instant.now();
        AtomicInteger normCalculus = new AtomicInteger();
        AtomicInteger simCalculus = new AtomicInteger();
        double thr      = 0.005; //0.005
        double thr_v    = Math.sqrt(8*thr);

        SolrUtils.Executor cotaBasedComparison = d1 -> {
            try {
                final DocTopic dt1      = DocTopic.from(d1);
                final List<Double> v1   = indexer.toVector(dt1.getTopics());
                SolrUtils.Executor similarityComparison = d2 -> {
                    normCalculus.incrementAndGet();
                    final DocTopic dt2      = DocTopic.from(d2);
                    final List<Double> v2   = indexer.toVector(dt2.getTopics());

                    double l1 = 0.0;
                    for(int i=0;i<v1.size();i++){
                        l1 += Math.abs(v1.get(i)-v2.get(i));
                    }

                    if (l1 < thr_v){
                        simCalculus.incrementAndGet();
                        p3.add(new Similarity(JensenShannon.similarity(v1,v2), new Document(dt1.getId(),dt1.getHash()), new Document(dt2.getId(), dt2.getHash())));
                    }
                };
                SolrUtils.iterate(CORPUS, "id:{* TO " + dt1.getId() + "}", client, similarityComparison);
            } catch (Exception e) {
                LOG.error("Unexpected error", e);

            }
        };
        SolrUtils.iterate(CORPUS, "*:*", client, cotaBasedComparison);
        Instant e3 = Instant.now();
        for(Integer accuracy: Arrays.asList(1,5,10)){
            LOG.info("Threshold-based Approach@"+accuracy+": " + new Evaluation(s3, e3, groundTruth, p3.stream().limit(accuracy).map(sim -> sim.getPair()).collect(Collectors.toList()), simCalculus.get()));
        }
    }

    public static List<Similarity> getSimilarDocumentsBy(int alarmType, String corpus, DocTopicsIndex indexer, AtomicInteger counter, SolrClient client) throws IOException, SolrServerException {
        // Density-based Approach Evaluation
        MinMaxPriorityQueue<Similarity> densityPairs = MinMaxPriorityQueue.orderedBy(new Similarity.ScoreComparator()).maximumSize(10).create();
        Alarm alarms = AlarmServiceTest.getAlarmsBy(alarmType, corpus, client);
        for(String hashcode: alarms.getGroups().keySet()){

            List<SolrDocument> docs = AlarmServiceTest.getDocumentsBy(alarmType, corpus, hashcode, 1000, client);

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
