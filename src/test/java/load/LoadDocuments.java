package load;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.model.SolrCollection;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.utils.ReaderUtils;
import es.gob.minetad.utils.TimeUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.time.Instant;

/**
 *
 * Create a 'documents' collection for each Corpus
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./create.sh (./start.sh)
 *
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LoadDocuments {

    private static final Logger LOG = LoggerFactory.getLogger(LoadDocuments.class);

    @Test
    public void execute() throws UnirestException, IOException, SolrServerException {

        TestSettings settings = new TestSettings();

      //  Assert.assertTrue("Solr server seems down: " + settings.getSolrUrl(), settings.isSolrUp());

        String path = settings.get("corpus.documents");
        String name = settings.get("corpus.name");

        Instant corpusStart = Instant.now();
        LOG.info("Loading documents from corpus '" + path + "' ..");

        // Creating Solr Collection
        
       
        SolrCollection collection = new SolrCollection("cordis-documents");
       
       
        BufferedReader reader = ReaderUtils.from(path);
        String row;
        ObjectMapper jsonMapper = new ObjectMapper();
        while((row = reader.readLine()) != null){

            JsonNode json = jsonMapper.readTree(row);
            
      //    for(JsonNode json:jsonn) {

            SolrInputDocument document = new SolrInputDocument();
            if (!json.get("objective").asText().trim().isEmpty()) {
            document.addField("id",json.get("id").asText());
            document.addField("name_s",json.get("title").asText());
            document.addField("text_txt",json.get("objective").asText());
            document.addField("instrument_s",json.get("instrument").asText());
            document.addField("startDate_dt",json.get("startDate").asText());
            document.addField("endDate_dt",json.get("endDate").asText());
            document.addField("totalCost_f",json.get("totalCost").asText());
            document.addField("area_s",json.get("area").asText());
            document.addField("topicWater_i",json.get("topicWater").asText());

            collection.add(document);
            }
          }
       // }

        collection.commit();

        TimeUtils.print(corpusStart, Instant.now(), "Corpus '" + name +"' saved in solr in: ");

    }
}
