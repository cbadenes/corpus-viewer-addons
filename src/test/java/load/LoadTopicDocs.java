package load;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.model.CorporaCollection;
import es.gob.minetad.model.Corpus;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.model.TopicDocsCollection;
import es.gob.minetad.utils.RestClient;
import es.gob.minetad.utils.TimeUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Create a 'topic-doc' collection for each model in a Corpus:
 *
 * 1. move into: src/test/docker/models
 * 2. create (or start) the containers: ./create.sh (./start.sh)
 * 3. move into: src/test/docker/solr
 * 4. create (or start) the container: ./create.sh (./start.sh)
 *
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LoadTopicDocs {

    private static final Logger LOG = LoggerFactory.getLogger(LoadTopicDocs.class);


    @Test
    public void execute() throws UnirestException, IOException, SolrServerException {

        TestSettings settings = new TestSettings();

        Assert.assertTrue("Solr server seems down: " + settings.getSolrUrl(), settings.isSolrUp());

        Instant testStart = Instant.now();
        List<Corpus> corpora = settings.getCorpora();

        for(Corpus corpus: corpora) {
            Instant corpusStart = Instant.now();
            List<Corpus.Model> models = corpus.getModels().entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());

            for (Corpus.Model model : models) {

                Instant modelStart = Instant.now();

                Assert.assertTrue("Model Rest-API seems down: " + model.getApi(), settings.isServerUp(model.getApi()));

                LOG.info("Loading topics from '" + corpus.getName() + "' model '" + model.getNumtopics()+"' ..");

                JsonNode response = RestClient.get(model.getApi() + "/settings",200);

                // Get vocabulary size
                Integer vocabularySize  = Integer.valueOf(response.getObject().getJSONObject("stats").getString("vocabulary"));
                Integer numTopics       = Integer.valueOf(response.getObject().getJSONObject("params").getString("topics"));

                // Creating Solr Collection
                TopicDocsCollection collection = new TopicDocsCollection(corpus.getName(), numTopics, vocabularySize);

                // Loading topics
                response = RestClient.get(model.getApi() + "/dimensions",200);
                Iterator dimIterator = response.getObject().getJSONArray("dimensions").iterator();
                while(dimIterator.hasNext()){


                    JSONObject dimension = (JSONObject) dimIterator.next();

                    String id           = String.valueOf(dimension.getInt("id"));
                    String topicName    = dimension.getString("name");
                    String description  = dimension.getString("description");
                    Double entropy      = dimension.getDouble("entropy");

                    Integer offset = 0;

                    Integer maxWords = 100;

                    Boolean finished = false;

                    Map<String,Double> words = new HashMap<>();

                    while(!finished){

                        response = RestClient.get(model.getApi()+"/dimensions/"+ id, ImmutableMap.of("maxWords",maxWords,"offset",offset), 200);

                        JSONArray elements = response.getObject().getJSONArray("elements");

                        for (int j=0;j<elements.length();j++){

                            String word     = elements.getJSONObject(j).getString("value");
                            Double score    = elements.getJSONObject(j).getDouble("score");
                            words.put(word,score);
                        }

                        offset += maxWords;

                        finished = elements.length() != maxWords;
                    }
                    collection.add(id, topicName, description, entropy, words);

                }

                collection.commit();

                LOG.info("All topics added as documents in Solr successfully!");
                TimeUtils.print(modelStart, Instant.now(), "Topics from model '" + model.getNumtopics()+"' of corpus: '" + corpus.getName()+ "' saved in Solr");

            }

            TimeUtils.print(corpusStart, Instant.now(), "All models of corpus: '" + corpus.getName()+ "' saved in Solr");
        }

        TimeUtils.print(testStart, Instant.now(), "Corpora saved in Solr");

    }
}
