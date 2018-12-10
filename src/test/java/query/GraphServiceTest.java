package query;

import es.gob.minetad.doctopic.DocTopicIndexFactory;
import es.gob.minetad.doctopic.DocTopicsIndex;
import es.gob.minetad.metric.JensenShannonExt;
import es.gob.minetad.model.DocTopic;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;
import es.gob.minetad.solr.SolrUtils;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.TimeUtils;
import es.gob.minetad.utils.WriterUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Exports a collection of documents as both a list of nodes and a list of edges based on their similarities
 *
 * in CSV format to be imported by a graph-oriented database
 *
 * Before run the query you must start the Solr service with a valid doctopic collection!
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class GraphServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(GraphServiceTest.class);
    private TestSettings settings;
    private SolrClient client;

    private static final String CORPUS  = "cordis";
    private static final String QUERY       = "*:*";
    private static final Integer NUM_TOPICS = 150;
    private static final Double THRESHOLD   = 0.9;

    private DocTopicsIndex doctopicParser;
    private String collection;


    @Before
    public void setup(){
        settings                    = new TestSettings();
        client                      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
        this.doctopicParser         = DocTopicIndexFactory.newFrom(NUM_TOPICS);
        collection                  = CORPUS + "-doctopics";
    }


    @Test
    public void execute() throws IOException, SolrServerException {

        Path nodesPath = Paths.get("output", "graphs", CORPUS, ""+NUM_TOPICS, "nodes.csv.gz");
        if (!nodesPath.getParent().toFile().exists()) nodesPath.getParent().toFile().mkdirs();

        Path nodesHeaderPath = Paths.get("output", "graphs", CORPUS, ""+NUM_TOPICS, "nodes-header.csv");
        if (!nodesHeaderPath.getParent().toFile().exists()) nodesHeaderPath.getParent().toFile().mkdirs();

        Path edgesPath = Paths.get("output", "graphs", CORPUS, ""+NUM_TOPICS, "edges.csv.gz");
        if (!edgesPath.getParent().toFile().exists()) edgesPath.getParent().toFile().mkdirs();

        Path edgesHeaderPath = Paths.get("output", "graphs", CORPUS, ""+NUM_TOPICS, "edges-header.csv");
        if (!edgesHeaderPath.getParent().toFile().exists()) edgesHeaderPath.getParent().toFile().mkdirs();

        // Header Files
        BufferedWriter nodesHeaderWriter = WriterUtils.to(nodesHeaderPath.toFile().getAbsolutePath());
        BufferedWriter edgesHeaderWriter = WriterUtils.to(edgesHeaderPath.toFile().getAbsolutePath());

        nodesHeaderWriter.write("documentId:ID(Document-ID)\n");
        edgesHeaderWriter.write(":START_ID(Document-ID),:END_ID(Document-ID),similarity,:TYPE\n");

        nodesHeaderWriter.close();
        edgesHeaderWriter.close();

        // Content files

        BufferedWriter nodesWriter = WriterUtils.to(nodesPath.toFile().getAbsolutePath());
        BufferedWriter edgesWriter = WriterUtils.to(edgesPath.toFile().getAbsolutePath());


        ParallelExecutor parallelExecutor = new ParallelExecutor();

        AtomicInteger counter = new AtomicInteger();
        AtomicInteger compCounter = new AtomicInteger();
        SolrUtils.Executor bruteForceComparison = d1 -> {
            parallelExecutor.submit(() -> {
                try {
                    final DocTopic dt1      = DocTopic.from(d1);
                    LOG.info("Getting similar docs to: " + dt1.getId() + "[" + counter.incrementAndGet()+"]");
                    StringBuffer node = new StringBuffer();
                    node.append(dt1.getId());
                    // add as many metadata as required
                    nodesWriter.write(node.toString()+"\n");
                    final List<Double> v1   = doctopicParser.toVector(dt1.getTopics());
                    SolrUtils.Executor similarityComparison = d2 -> {
                        try {
                            compCounter.incrementAndGet();
                            final DocTopic dt2      = DocTopic.from(d2);

                            if (dt1.getId().equalsIgnoreCase(dt2.getId())) return;

                            Float score = 0.0f;
                            if (d2.containsKey("jsWeight")){
                                score = Float.valueOf((String) d2.getFieldValue("jsWeight"));
                            }else{
                                final List<Double> v2   = doctopicParser.toVector(dt2.getTopics());
                                score = Double.valueOf(JensenShannonExt.similarity(v1,v2)).floatValue();
                            }

                            if (score > THRESHOLD){
                                StringBuffer edge = new StringBuffer();
                                edge.append(dt1.getId()).append(",");
                                edge.append(dt2.getId()).append(",");
                                edge.append(score).append(",");
                                edge.append("SIMILAR_TO");
                                // add as many metadata as required
                                edgesWriter.write(edge.toString() + "\n");
                            }
                        } catch (Exception e) {
                            LOG.error("Unexpected error",e);
                        }
                    };
                    SolrUtils.iterateBySimilar(collection, QUERY + " AND id:{* TO " + dt1.getId() + "}", dt1.getTopics(), String.valueOf(NUM_TOPICS), THRESHOLD, doctopicParser.getEpsylon(), doctopicParser.getPrecision(), client, similarityComparison);
                } catch (Exception e) {
                    LOG.error("Unexpected error", e);

                }
            });
        };

        LOG.info("Calculating all pair-wise similarities ..");
        Instant s1 = Instant.now();
        SolrUtils.iterate(collection, QUERY, client, bruteForceComparison);
        parallelExecutor.awaitTermination(1, TimeUnit.HOURS);
        Instant e1 = Instant.now();
        nodesWriter.close();
        edgesWriter.close();

        LOG.info("Total Comparisons: " + compCounter.get());
        TimeUtils.print(s1,e1,"Time Elapsed");


    }

}
