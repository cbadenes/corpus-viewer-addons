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
import sun.jvm.hotspot.utilities.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * Create a 'doc-topic' collection from a Corpus:
 *
 *    - move into: src/test/docker/solr
 *    - run container: ./run.sh
 *
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LoadDocTopics {

    private static final Logger LOG = LoggerFactory.getLogger(LoadDocTopics.class);

    @Test
    public void execute() throws UnirestException, IOException, SolrServerException {

        TestSettings settings = new TestSettings();

        Assert.that(settings.isSolrUp(), "Solr server seems down: " + settings.getSolrUrl());

        Instant testStart = Instant.now();
        List<Corpus> corpora = settings.getCorpora();
        CorporaCollection corporaCollection = new CorporaCollection();

        for(Corpus corpus: corpora){

            LOG.info("Loading doctopics from corpus: '" + corpus.getFullName() + "' ..");
            Instant colStart = Instant.now();
            String corpusPath       = corpus.getDoctopicsPath();
            String collectionName   = corpus.getName();
            Integer numTopics       = corpus.getNumTopics();

            // Creating Solr Collection
            DocTopicsCollection collection = new DocTopicsCollection(collectionName, numTopics);

            ParallelExecutor executor = new ParallelExecutor();
            BufferedReader reader = ReaderUtils.from(corpusPath);
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

            double corpusEntropy = collection.getEntropy() / collection.getSize();
            corpus.setEntropy(corpusEntropy);
            corporaCollection.add(corpus);

            TimeUtils.print(colStart, Instant.now(), "Collections   '" + collection.getCollectionName()+"' created in: ");
        }

        corporaCollection.commit();
        TimeUtils.print(testStart, Instant.now(), "All collections created in: ");

    }
}
