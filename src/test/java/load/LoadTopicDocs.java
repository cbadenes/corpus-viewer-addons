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
 * It Requires a Topic Model API running:
 *    1. move into: src/test/docker/models
 *    2. run the service: ./docker-compose up -d
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./create.sh (./start.sh)
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LoadTopicDocs {

    private static final Logger LOG = LoggerFactory.getLogger(LoadTopicDocs.class);


    @Test
    public void execute() throws UnirestException, IOException, SolrServerException {

        TestSettings settings = new TestSettings();

        Assert.assertTrue("Solr server seems down: " + settings.getSolrUrl(), settings.isSolrUp());

        Instant modelStart = Instant.now();

        String name = settings.get("corpus.name");
        Integer numTopics = Integer.valueOf(settings.get("corpus.dim"));
        String api = settings.get("corpus.api");

        Assert.assertTrue("Model Rest-API seems down: " + api, settings.isServerUp(api));

        LOG.info("Loading topics from '" + name + "' model '" + numTopics+"' ..");

        JsonNode response = RestClient.get(api + "/settings",200);

        // Get vocabulary size
        Integer vocabularySize  = Integer.valueOf(response.getObject().getJSONObject("stats").getString("vocabulary"));

        // Creating Solr Collection
        TopicDocsCollection collection = new TopicDocsCollection("topicdocs", vocabularySize);

        // Loading topics
        response = RestClient.get(api + "/topics",200);
        Iterator dimIterator = response.getArray().iterator();
        while(dimIterator.hasNext()){

            JSONObject dimension = (JSONObject) dimIterator.next();

            String id           = String.valueOf(dimension.getInt("id"));
            String topicName    = dimension.getString("name");
            String description  = dimension.getString("description");

            JsonNode topicResponse = RestClient.get(api + "/topics/" + id, 200);
            Double entropy      = topicResponse.getObject().getDouble("entropy");;

            Integer offset = 0;

            Integer maxWords = 100;

            Boolean finished = false;

            Map<String,Double> words = new HashMap<>();

            while(!finished){

                response = RestClient.get(api+"/topics/"+ id+"/words", ImmutableMap.of("maxWords",maxWords,"offset",offset), 200);

                JSONArray elements = response.getArray();

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

        TimeUtils.print(modelStart, Instant.now(), "Topics from corpus: '" + name+ "' saved in Solr");
    }
}
