package load;

import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.model.*;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.ReaderUtils;
import es.gob.minetad.utils.TimeUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 *  Create a 'doc-topic' collection for each Model in a Corpus.
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./create.sh (./start.sh)
 *
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LoadDocTopics {

    private static final Logger LOG = LoggerFactory.getLogger(LoadDocTopics.class);

    @Test
    public void execute() throws UnirestException, IOException, SolrServerException {

        TestSettings settings = new TestSettings();

        Assert.assertTrue("Solr server seems down: " + settings.getSolrUrl(), settings.isSolrUp());

        Instant testStart = Instant.now();

        CorporaCollection corporaCollection = new CorporaCollection();

        Instant colStart = Instant.now();

        String path = settings.get("corpus.doctopics");
        String name = settings.get("corpus.name");
        Integer numTopics = Integer.valueOf(settings.get("corpus.dim"));

        LOG.info("Loading doctopics from corpus: '" + path +" ..");

        // Creating Solr Collection
        DocTopicsCollection collection = new DocTopicsCollection("doctopics", numTopics);

        ParallelExecutor executor = new ParallelExecutor();
        BufferedReader reader = ReaderUtils.from(path);
        String row;
        while((row = reader.readLine()) != null){
            final String line = row;
            executor.submit(() -> {
                try{
                    String[] values = line.split(",");
                    String id = values[0];
                    List<Double> vector = new ArrayList<>();
                    for(int i=1;i<values.length;i++){
                        vector.add(Double.valueOf(values[i]));
                    }

                    collection.add(id,vector);

                }catch (Exception e){
                    LOG.error("Unexpected error",e);
                }
            });
        }
        executor.awaitTermination(1, TimeUnit.HOURS);

        collection.commit();

        Corpus corpus = new Corpus(name+"-"+numTopics);
        double corpusEntropy = collection.getEntropy() / collection.getSize();
        corpus.setEntropy(corpusEntropy);
        corporaCollection.add(corpus);

        TimeUtils.print(colStart, Instant.now(), "Corpus '" + corpus +"' created in: ");

        corporaCollection.commit();
        TimeUtils.print(testStart, Instant.now(), "All collections created in: ");

    }
}
