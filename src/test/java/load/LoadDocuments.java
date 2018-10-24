package load;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.model.CorporaCollection;
import es.gob.minetad.model.Corpus;
import es.gob.minetad.model.DocumentCollection;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.utils.ReaderUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.utilities.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Create a 'doc-topic' collection from a Corpus:
 *
 * 1.a) external solr:
 *    - update settings in src/test/resources/config.properties
 * 1.b) local solr:
 *    - move into: src/test/docker/solr
 *    - run container: ./solr_7_5.sh
 *    - run container: ./create-collection.sh cordis-documents
 * 4. run LoadDocuments.execute test
 *
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LoadDocuments {

    private static final Logger LOG = LoggerFactory.getLogger(LoadDocuments.class);

    static Map<String,String> corpora = new HashMap<>();

    static {
        corpora.put("cordis", "https://delicias.dia.fi.upm.es/nextcloud/index.php/s/woBzdYWfJtJ6sfY/download");
    }

    private static final String CORPUS = "cordis";


    @Test
    public void execute() throws UnirestException, IOException, SolrServerException {

        TestSettings settings = new TestSettings();

        Assert.that(settings.isSolrUp(), "Solr server seems down: " + settings.getSolrUrl());

        Instant testStart = Instant.now();
        List<Corpus> corpora = settings.getCorpora();
        CorporaCollection corporaCollection = new CorporaCollection();

        for(Corpus corpus: corpora){
            LOG.info("Loading documents from corpus '" + corpus + "' ..");
            String corpusPath       = corpus.getDocumentsPath();

            // Creating Solr Collection
            DocumentCollection collection = new DocumentCollection(corpus.getName());

            BufferedReader reader = ReaderUtils.from(corpusPath);
            String row;
            ObjectMapper jsonMapper = new ObjectMapper();
            while((row = reader.readLine()) != null){

                JsonNode json = jsonMapper.readTree(row);

                String area = json.get("area").asText();
                if (area.equalsIgnoreCase("en")){
                    String id = json.get("id").asText();
                    String name = json.get("title").asText();
                    String text = json.get("objective").asText();
                    collection.add(id,name,text);
                }
            }

            collection.commit();

            LOG.info("All documents indexed in '"+collection.getCollectionName()+"' successfully!");
        }



    }
}
