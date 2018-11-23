package load;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.common.SolrInputDocument;

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
public class LoadWikipediaDocuments extends LoadDocumentsFromFile {

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

}
