package load.wikipedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import load.LoadDocuments;
import load.patents.LoadPatentsDocuments;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 * Create a 'documents' collection for a given Corpus
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./start.sh
 *    3. create collections: ./create-collections.sh
 *
 *  http://localhost:8983/solr/#/documents
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LoadWikipediaDocuments extends LoadDocuments {

    private static final Logger LOG = LoggerFactory.getLogger(LoadPatentsDocuments.class);

    private static final Integer MAX    = 1000;//-1
    private static final Integer OFFSET = 0;
    private static final String CORPUS  = "corpora/wikipedia/documents.jsonl.gz";
    private final ObjectMapper jsonMapper;

    public LoadWikipediaDocuments() {
        super(CORPUS, MAX, OFFSET);
        jsonMapper = new ObjectMapper();
    }


    @Override
    protected SolrInputDocument newDocument(String row) {
        JsonNode json;
        try {
            json = jsonMapper.readTree(row);
            SolrInputDocument document = new SolrInputDocument();
            document.addField("id",json.get("id").asText());
            document.addField("name",json.get("title").asText());
            document.addField("text",json.get("text").asText());
            document.addField("url",json.get("url").asText());
            return document;
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void handleComplete() {
        LOG.info("done!");
    }

}
