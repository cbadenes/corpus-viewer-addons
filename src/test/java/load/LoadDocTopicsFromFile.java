package load;

import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.model.CorporaCollection;
import es.gob.minetad.model.Corpus;
import es.gob.minetad.model.DocTopicsCollection;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.ReaderUtils;
import es.gob.minetad.utils.TimeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *  Create a 'doctopics' collection for a given Corpus
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./start.sh
 *    3. create collections: ./create-collections.sh
 *
 *  http://localhost:8983/solr/#/doctopics
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 *
 */
public abstract class LoadDocTopicsFromFile {

    private static final Logger LOG = LoggerFactory.getLogger(LoadDocTopicsFromFile.class);

    private final String corpus;
    private final Integer max;
    private final Integer offset;

    public LoadDocTopicsFromFile(String corpus, Integer max, Integer offset) {
        this.corpus = corpus;
        this.max    = max;
        this.offset = offset;
    }

    @Test
    public void execute() throws UnirestException, IOException, SolrServerException {

        TestSettings settings = new TestSettings();

        Assert.assertTrue("Solr server seems down: " + settings.getSolrUrl(), settings.isSolrUp());

        Instant testStart = Instant.now();

        CorporaCollection corporaCollection = new CorporaCollection();

        Instant colStart = Instant.now();

        String path = corpus;
        String name = StringUtils.substringBetween(corpus,"/","/");

        Integer numTopics = Integer.valueOf(StringUtils.substringBetween(corpus,"-",".csv"));

        LOG.info("Loading doctopics from corpus: '" + path +" ..");

        // Creating Solr Collection
        DocTopicsCollection collection = new DocTopicsCollection("doctopics", numTopics);

        ParallelExecutor executor = new ParallelExecutor();
        BufferedReader reader = ReaderUtils.from(path);
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger offCounter = new AtomicInteger();
        String row;
        while((row = reader.readLine()) != null){
            final String line = row;
            if ((offset>0) && (offCounter.incrementAndGet()<offset)) continue;
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
            if ((max > 0) && (counter.incrementAndGet() >= max))break;
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
