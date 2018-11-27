package load.patents;

import load.LoadDocuments;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

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
public class LoadPatentsDocuments extends LoadDocuments {

    private static final Logger LOG = LoggerFactory.getLogger(LoadPatentsDocuments.class);

    private static final Integer MAX    = 1000;//-1
    private static final Integer OFFSET = 0;
    private static final String CORPUS  = "corpora/patents/documents.csv.gz";

    public LoadPatentsDocuments() {
        super(CORPUS, MAX, OFFSET);
    }

    @Override
    protected SolrInputDocument newDocument(String row) {
        String[] values = row.split(",");
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id",values[0]);
        document.addField("name_s",values[0]);
        document.addField("lang_s",values[1]);
        document.addField("text_txt",Arrays.stream(values).skip(2).collect(Collectors.joining(",")));
        return document;
    }

    @Override
    protected void handleComplete() {
        LOG.info("done!");
    }

}
