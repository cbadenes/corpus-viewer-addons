package query;

import com.google.common.collect.MinMaxPriorityQueue;
import es.gob.minetad.doctopic.CleanZeroEpsylonIndex;
import es.gob.minetad.doctopic.DocTopicsIndex;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.metric.JensenShannonExt;
import es.gob.minetad.model.*;
import es.gob.minetad.solr.SolrClientFactory;
import es.gob.minetad.solr.SolrUtils;
import es.gob.minetad.utils.TimeUtils;
import es.gob.minetad.utils.WriterUtils;
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * Exports a collection of documents as a list of nodes and a list of edges based on their similarities
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

    private static final String COLLECTION  = "doctopics";
    private static final String QUERY       = "*:*";
    private static final Integer NUM_TOPICS = 70;
    private static final Double THRESHOLD   = 0.8;

    private float epsylon;
    private float multiplicationFactor;
    private CleanZeroEpsylonIndex doctopicParser;


    @Before
    public void setup(){
        settings                    = new TestSettings();
        client                      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
        this.epsylon                = 1f / NUM_TOPICS;
        this.multiplicationFactor   = Double.valueOf(1*Math.pow(10,String.valueOf(NUM_TOPICS).length()+1)).floatValue();
        this.doctopicParser         = new CleanZeroEpsylonIndex(NUM_TOPICS, multiplicationFactor, epsylon);
    }


    @Test
    public void execute() throws IOException, SolrServerException {

        Path nodesPath = Paths.get("output", "nodes.csv.gz");
        if (!nodesPath.getParent().toFile().exists()) nodesPath.getParent().toFile().mkdirs();

        Path edgesPath = Paths.get("output", "edges.csv.gz");
        if (!edgesPath.getParent().toFile().exists()) edgesPath.getParent().toFile().mkdirs();

        BufferedWriter nodesWriter = WriterUtils.to(nodesPath.toFile().getAbsolutePath());
        BufferedWriter edgesWriter = WriterUtils.to(edgesPath.toFile().getAbsolutePath());

        nodesWriter.write("id\n");
        edgesWriter.write("id1,id2,similarity\n");


        AtomicInteger counter = new AtomicInteger();
        AtomicInteger compCounter = new AtomicInteger();
        SolrUtils.Executor bruteForceComparison = d1 -> {
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
                        final List<Double> v2   = doctopicParser.toVector(dt2.getTopics());

                        // use of JSD with l1-based threshold
                        double similarity = JensenShannonExt.similarity(v1, v2);

                        if (similarity > THRESHOLD){
                            StringBuffer edge = new StringBuffer();
                            edge.append(dt1.getId()).append(",");
                            edge.append(dt2.getId()).append(",");
                            edge.append(similarity);
                            // add as many metadata as required
                            edgesWriter.write(edge.toString() + "\n");
                        }
                    } catch (Exception e) {
                        LOG.error("Unexpected error",e);
                    }
                };
                SolrUtils.iterate(COLLECTION, "id:{* TO " + dt1.getId() + "}", client, similarityComparison);
            } catch (Exception e) {
                LOG.error("Unexpected error", e);

            }
        };

        LOG.info("Calculating all pair-wise similarities ..");
        Instant s1 = Instant.now();
        SolrUtils.iterate(COLLECTION, QUERY, client, bruteForceComparison);
        Instant e1 = Instant.now();
        nodesWriter.close();
        edgesWriter.close();

        LOG.info("Total Comparisons: " + compCounter.get());
        TimeUtils.print(s1,e1,"Time Elapsed");


    }

}