package load;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.model.CorporaCollection;
import es.gob.minetad.model.Corpus;
import es.gob.minetad.model.DocumentCollection;
import es.gob.minetad.model.TestSettings;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Create a 'documents' collection for each Corpus:
 *
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./run.sh (./start.sh)
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

        Assert.assertTrue("Solr server seems down: " + settings.getSolrUrl(), settings.isSolrUp());

        Instant testStart = Instant.now();
        List<Corpus> corpora = settings.getCorpora();
        CorporaCollection corporaCollection = new CorporaCollection();

        for(Corpus corpus: corpora){
            Instant corpusStart = Instant.now();
            LOG.info("Loading documents from corpus '" + corpus + "' ..");
            String corpusPath       = corpus.getPath();

            // Creating Solr Collection
            DocumentCollection collection = new DocumentCollection(corpus.getName());

            BufferedReader reader = ReaderUtils.from(corpusPath);
            String row;
            ObjectMapper jsonMapper = new ObjectMapper();
            while((row = reader.readLine()) != null){

                JsonNode json = jsonMapper.readTree(row);
                //TODO add metainformation
                String id = json.get(settings.get("corpus."+corpus.getName()+".id")).asText();
                String name = json.get(settings.get("corpus."+corpus.getName()+".name")).asText();
                String text = json.get(settings.get("corpus."+corpus.getName()+".text")).asText();
                collection.add(id,name,text);

            }

            collection.commit();

            TimeUtils.print(corpusStart, Instant.now(), "Corpus '" + corpus.getName() +"' saved in solr in: ");
        }

        TimeUtils.print(testStart, Instant.now(), "Corpora saved in solr in: ");

    }
}
