package load;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.model.TopicDocCollection;
import es.gob.minetad.utils.RestClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 *
 * Create 'topic-doc' collection from a Topic Model:
 *
 * 1. move into: src/test/docker/models
 * 2. run container: e.g. ./cordis.70.sh
 * 3.a) external solr:
 *    - update settings in src/test/resources/config.properties
 * 3.b) local solr:
 *    - move into: src/test/docker/solr
 *    - run container: ./solr_7_5.sh
 *    - run container: ./create-collection.sh cordis-topicdoc-70
 * 4. run LoadTopics.execute test
 *
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LoadTopics {

    private static final Logger LOG = LoggerFactory.getLogger(LoadTopics.class);

    private static final String SOLR_COLLECTION= "patstat";

    private static final String MODEL_API = "http://localhost:8000/model";



    @Test
    public void execute() throws UnirestException, IOException, SolrServerException {

        LOG.info("Load Topics example");

        JsonNode response = RestClient.get(MODEL_API + "/settings",200);

        // Get vocabulary size
        Integer vocabularySize  = Integer.valueOf(response.getObject().getJSONObject("stats").getString("vocabulary"));
        Integer numTopics       = Integer.valueOf(response.getObject().getJSONObject("params").getString("topics"));

        // Creating Solr Collection
        TopicDocCollection collection = new TopicDocCollection(SOLR_COLLECTION, numTopics, vocabularySize);

        // Loading topics
        response = RestClient.get(MODEL_API + "/dimensions",200);
        Iterator dimIterator = response.getObject().getJSONArray("dimensions").iterator();
        while(dimIterator.hasNext()){


            JSONObject dimension = (JSONObject) dimIterator.next();

            String id           = String.valueOf(dimension.getInt("id"));
            String topicName    = dimension.getString("name");
            String description  = dimension.getString("description");
            Double entropy      = dimension.getDouble("entropy");

            LOG.info("Reading words from topic" + id + " ..");

            Integer offset = 0;

            Integer maxWords = 100;

            Boolean finished = false;

            Map<String,Double> words = new HashMap<>();

            while(!finished){

                response = RestClient.get(MODEL_API+"/dimensions/"+ id, ImmutableMap.of("maxWords",maxWords,"offset",offset), 200);

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

            LOG.info("Added " + topicName + " to collection");

        }

        collection.commit();

        LOG.info("All topics added as documents in Solr successfully!");

    }
}
