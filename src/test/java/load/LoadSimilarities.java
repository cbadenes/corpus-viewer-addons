package load;

import es.gob.minetad.doctopic.DocTopicIndexFactory;
import es.gob.minetad.doctopic.DocTopicsIndex;
import es.gob.minetad.metric.JensenShannonExt;
import es.gob.minetad.model.DocTopic;
import es.gob.minetad.model.SolrCollection;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;
import es.gob.minetad.solr.SolrUtils;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.TimeUtils;
import es.gob.minetad.utils.WriterUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
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

import static org.apache.solr.core.SolrInfoBean.Group.collection;

/**
 *
 * Calculates, by brute force, the similarity of all pairs of documents
 *
 * Before run the service you must start the Solr service with a valid doctopic collection!
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public abstract class LoadSimilarities {

    private static final Logger LOG = LoggerFactory.getLogger(LoadSimilarities.class);
    private final String corpus;
    private final Double threshold;
    private final SolrCollection collection;

    private TestSettings settings;
    private SolrClient client;

    private static final Double THRESHOLD   = 0.8;

    private DocTopicsIndex doctopicParser;


    public LoadSimilarities(String corpus, Integer numTopics, Double threshold){
        settings                    = new TestSettings();
        client                      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
        this.doctopicParser         = DocTopicIndexFactory.newFrom(numTopics);
        this.corpus                 = corpus;
        this.threshold              = threshold;

        Assert.assertTrue("Solr server seems down: " + settings.getSolrUrl(), settings.isSolrUp());

        try {
            // Creating Solr Collection
            this.collection = new SolrCollection(corpus + "-similarities");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void execute() throws IOException, SolrServerException {

        String docTopicCollection = corpus+"-doctopics";

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
                    final List<Double> v1   = doctopicParser.toVector(dt1.getTopics());
                    SolrUtils.Executor similarityComparison = d2 -> {
                        try {
                            compCounter.incrementAndGet();
                            final DocTopic dt2      = DocTopic.from(d2);

                            if (dt1.getId().equalsIgnoreCase(dt2.getId())) return;

                            SolrInputDocument document = new SolrInputDocument();
                            document.addField("d1_s",dt1.getId());
                            document.addField("name1_s",d1.getFieldValue("name_s"));
                            document.addField("d2_s",dt2.getId());
                            document.addField("name2s",d2.getFieldValue("name_s"));


                            Float score = 0.0f;
                            if (d2.containsKey("jsWeight")){
                                score = Float.valueOf((String) d2.getFieldValue("jsWeight"));
                            }else{
                                final List<Double> v2   = doctopicParser.toVector(dt2.getTopics());
                                score = Double.valueOf(JensenShannonExt.similarity(v1,v2)).floatValue();
                            }

                            if (score >= threshold){
                                document.addField("score_f",score);
                                collection.add(document);
                            }

                        } catch (Exception e) {
                            LOG.error("Unexpected error",e);
                        }
                    };
                    // Calculate all pair-wise similarities
                SolrUtils.iterateBySimilar(docTopicCollection, "*:*", dt1.getTopics(), THRESHOLD, doctopicParser.getEpsylon(), doctopicParser.getPrecision(), client, similarityComparison);
//                    SolrUtils.iterate(docTopicCollection, "*:*", client, similarityComparison);
                } catch (Exception e) {
                    LOG.error("Unexpected error", e);

                }
            });
        };

        LOG.info("Iterate on all documents ..");
        Instant s1 = Instant.now();
        SolrUtils.iterate(docTopicCollection, "*:*", client, bruteForceComparison);
        Instant e1 = Instant.now();

        parallelExecutor.awaitTermination(1, TimeUnit.HOURS);

        this.collection.commit();
        LOG.info("Total Comparisons: " + compCounter.get());
        TimeUtils.print(s1,e1,"Time Elapsed");


    }

}
