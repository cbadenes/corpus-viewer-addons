package load;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.doctopic.DTFIndex;
import es.gob.minetad.model.SolrCollection;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.utils.RestClient;
import es.gob.minetad.utils.TimeUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * Create a 'topic-doc' collection for a given Corpus:
 *
 * It Requires a Topic Model API running:
 *    1. move into: src/test/docker/models
 *    2. run the service: ./docker-compose up -d
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./start.sh
 *    3. create collections: ./create-collections.sh
 *
 *
 *  http://localhost:8983/solr/#/topicdocs
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public abstract class LoadLDATopicDocs {

    private static final Logger LOG = LoggerFactory.getLogger(LoadLDATopicDocs.class);

    private final String name;
    private final String dim;
    private final String endpoint;

    public LoadLDATopicDocs(String name, String dim, String endpoint) {
        this.name       = name;
        this.dim        = dim;
        this.endpoint   = endpoint;
    }

    @Test
    public void execute() throws UnirestException, IOException, SolrServerException {

        TestSettings settings = new TestSettings();

        Assert.assertTrue("Solr server seems down: " + settings.getSolrUrl(), settings.isSolrUp());

        Instant modelStart = Instant.now();

        Integer numTopics = Integer.valueOf(dim);

        Assert.assertTrue("Model Rest-API seems down: " + endpoint, settings.isServerUp(endpoint));

        LOG.info("Loading topics from '" + name + "' model '" + numTopics+"' ..");

        JsonNode response = RestClient.get(endpoint + "/settings",200);

        // Get vocabulary size
        Integer vocabularySize  = Integer.valueOf(response.getObject().getJSONObject("stats").getString("vocabulary"));

        float multiplicationFactor = Double.valueOf(1 * Math.pow(10, String.valueOf(vocabularySize).length() + 1)).floatValue();
        DTFIndex indexer = new DTFIndex(multiplicationFactor);


        // Creating Solr Collection
        SolrCollection collection = new SolrCollection(name + "-topicdocs");

        // Num Documents
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRows(0);
        solrQuery.setQuery("*:*");
        QueryResponse rsp = collection.getSolrClient().query(name+"-doctopics",solrQuery);
        long numDocs = rsp.getResults().getNumFound();
        if (numDocs == 0) {
            LOG.warn("'doctopics' collection is required to calculate topic weights.");
            return;
        }

        // Loading topics
        response = RestClient.get(endpoint + "/topics",200);
        Iterator dimIterator = response.getArray().iterator();
        while(dimIterator.hasNext()){

            JSONObject dimension = (JSONObject) dimIterator.next();

            String id           = String.valueOf(dimension.getInt("id"));
            String topicName    = dimension.getString("name");
            String description  = dimension.getString("description");

            JsonNode topicResponse = RestClient.get(endpoint + "/topics/" + id, 200);
            Double entropy      = topicResponse.getObject().getDouble("entropy");;

            // Calculate the topic weight based on documents containing that topic in hashexpr5
            solrQuery = new SolrQuery();
            solrQuery.setRows(0);
            solrQuery.setQuery("hashexpr5_txt:t"+id);
            rsp = collection.getSolrClient().query(name+"-doctopics",solrQuery);
            long numFound = rsp.getResults().getNumFound();
            Double weight       = Double.valueOf(numFound) / Double.valueOf(numDocs);

            Map<String,Double> words        = getWords(id, false);
            Map<String,Double> wordsTfIdf   = getWords(id, true);


            SolrInputDocument document = new SolrInputDocument();
            document.addField("id",id);
            document.addField("name_s",topicName);
            document.addField("description_txt",description);
            document.addField("label_s","");
            document.addField("model_s","lda");
            document.addField("weight_f",weight);
            document.addField("entropy_f",entropy);

            String dtf = indexer.toString(words);
            document.addField("words_tfdl",dtf);

            String dtfidf = indexer.toString(wordsTfIdf);
            document.addField("wordsTFIDF_tfdl",dtfidf);

            collection.add(document);

        }

        collection.commit();

        TimeUtils.print(modelStart, Instant.now(), "Topics from corpus: '" + name+ "' saved in Solr");
    }

    private Map<String,Double> getWords(String id, Boolean tfidf) throws UnirestException {
        Map<String,Double> words = new HashMap<>();
        Integer offset = 0;

        Integer maxWords = 100;

        Boolean finished = false;

        while(!finished){

            JsonNode response = RestClient.get(endpoint + "/topics/" + id + "/words", ImmutableMap.of("max", maxWords, "offset", offset, "tfidf",tfidf), 200);

            JSONArray elements = response.getArray();

            for (int j=0;j<elements.length();j++){

                String word     = elements.getJSONObject(j).getString("value");
                Double score    = elements.getJSONObject(j).getDouble("score");
                words.put(word,score);
            }

            offset += maxWords;

            finished = elements.length() != maxWords;
        }
        return words;
    }
}
