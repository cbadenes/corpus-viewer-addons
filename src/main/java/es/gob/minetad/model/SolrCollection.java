package es.gob.minetad.model;

import es.gob.minetad.solr.SolrClientFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.utilities.Assert;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SolrCollection {

    private static final Logger LOG = LoggerFactory.getLogger(SolrCollection.class);

    protected final SolrClient solrClient;
    protected final String collectionName;
    protected AtomicInteger counter = new AtomicInteger();

    public SolrCollection(String name) throws IOException, SolrServerException {

        this.collectionName = name;
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/test/resources/config.properties"));


        String url  = properties.getProperty("solr.url");
        String mode = properties.getProperty("solr.mode");
        this.solrClient = SolrClientFactory.create( url, mode );

        CoreAdminResponse response = CoreAdminRequest.getStatus(name, solrClient);
        NamedList<NamedList<Object>> collections = response.getCoreStatus();

        Assert.that(collections.get(name).size() > 0, "Collection '"+ name + "' not exist in Solr. It must be created prior to this execution!");

    }

    public void add(SolrInputDocument document ) throws IOException, SolrServerException {

        solrClient.add(collectionName, document);

        if (counter.incrementAndGet() % 500 == 0) commit();

    }

    public Integer getSize() {
        return counter.get();
    }


    public void commit() throws IOException, SolrServerException {
        LOG.info("Added " + counter.get() + " documents to Solr");
        this.solrClient.commit(collectionName);
    }

    public String getCollectionName() {
        return collectionName;
    }
}
