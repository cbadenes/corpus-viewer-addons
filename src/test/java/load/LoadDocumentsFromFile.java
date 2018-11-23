package load;

import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.model.SolrCollection;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.ReaderUtils;
import es.gob.minetad.utils.TimeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

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
public abstract class LoadDocumentsFromFile {

    private static final Logger LOG = LoggerFactory.getLogger(LoadDocumentsFromFile.class);

    private final String corpus;
    private final Integer max;
    private final Integer offset;

    public LoadDocumentsFromFile(String corpus, Integer max, Integer offset) {
        this.corpus = corpus;
        this.max    = max;
        this.offset = offset;
    }

    protected abstract SolrInputDocument newDocument(String row);

    @Test
    public void execute() throws UnirestException, IOException, SolrServerException {

        TestSettings settings = new TestSettings();

        Assert.assertTrue("Solr server seems down: " + settings.getSolrUrl(), settings.isSolrUp());

        String path = corpus;
        String name = StringUtils.substringBetween(corpus,"/","/");

        Instant corpusStart = Instant.now();
        LOG.info("Loading documents from corpus '" + path + "' ..");

        // Creating Solr Collection
        SolrCollection collection = new SolrCollection("documents");

        ParallelExecutor executor = new ParallelExecutor();
        BufferedReader reader = ReaderUtils.from(path);
        String row;
        AtomicInteger offCounter = new AtomicInteger();
        AtomicInteger counter = new AtomicInteger();
        while((row = reader.readLine()) != null){

            final String line = row;

            if ((offset>0) && (offCounter.incrementAndGet()<offset)) continue;

            executor.submit(() -> {
                try{
                    SolrInputDocument document = newDocument(line);

                    collection.add(document);

                }catch (Exception e){
                    LOG.error("Unexpected error",e);
                }
            });

            if ((max > 0) && (counter.incrementAndGet() >= max))break;
        }

        collection.commit();

        TimeUtils.print(corpusStart, Instant.now(), "Corpus '" + name +"' saved in solr in: ");

    }
}
