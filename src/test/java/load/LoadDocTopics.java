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
import java.util.stream.Collectors;

/**
 *
 * Create a 'doc-topic' collection for each Model in a Corpus:
 *
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./run.sh (./start.sh)
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
            List<Corpus.Model> models = corpus.getModels().entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());

            for(Corpus.Model model: models){

                Instant colStart = Instant.now();
                String corpusName           = corpus.getName();
                String doctopicsPath        = model.getDoctopics();
                Integer numTopics           = model.getNumtopics();

                LOG.info("Loading doctopics from corpus: '" + corpusName + "' and model: '" + numTopics + "' ..");

                // Creating Solr Collection
                DocTopicsCollection collection = new DocTopicsCollection(corpusName, numTopics);

                ParallelExecutor executor = new ParallelExecutor();
                BufferedReader reader = ReaderUtils.from(doctopicsPath);
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
                model.setEntropy(corpusEntropy);
                corporaCollection.add(corpusName, model);

                TimeUtils.print(colStart, Instant.now(), "Model '" + model.getNumtopics() +"' from corpus '" + corpusName+"' created in: ");


            }
        }

        corporaCollection.commit();
        TimeUtils.print(testStart, Instant.now(), "All collections created in: ");

    }
}
