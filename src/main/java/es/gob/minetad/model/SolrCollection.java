package es.gob.minetad.model;

import es.gob.minetad.solr.SolrClientFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SolrCollection {

    private static final Logger LOG = LoggerFactory.getLogger(SolrCollection.class);

    protected  SolrClient solrClient;
    protected final String collectionName;
    protected AtomicInteger counter = new AtomicInteger();
    protected int interval = 5000;

    public SolrCollection(String name) throws IOException, SolrServerException {

        this.collectionName = name;
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/test/resources/config.properties"));


        String url  = properties.getProperty("solr.url");
        String mode = properties.getProperty("solr.mode");
       	this.solrClient = SolrClientFactory.create( url, mode );
       
        CoreAdminResponse response = CoreAdminRequest.getStatus(name, solrClient);
        NamedList<NamedList<Object>> collections = response.getCoreStatus();


        if (collections.get(name).size() < 0) throw new RuntimeException("Collection '"+ name + "' not exist in Solr. It must be created prior to this execution!");

    }

    public void add(SolrInputDocument document ) throws IOException, SolrServerException {

        
        if (solrClient instanceof  CloudSolrClient) {
        	solrClient=(CloudSolrClient)solrClient;
        	((CloudSolrClient) solrClient).setDefaultCollection(collectionName);
        	solrClient.add(document);
        }else {
        	solrClient.add(collectionName, document);
        }

        if (counter.incrementAndGet() % interval == 0) commit();

    }

    public Integer getSize() {
        return counter.get();
    }


    public void commit() throws IOException, SolrServerException {
        LOG.info("Added " + counter.get() + " documents to Solr '" + collectionName + "' ");
        this.solrClient.commit(collectionName);
    }

    public String getCollectionName() {
        return collectionName;
    }

	public SolrClient getSolrClient() {
		return solrClient;
	}


    public Optional<SolrDocument> getById(String id){
        SolrQuery query = new SolrQuery();
        query.set("q", "id:"+id);
        QueryResponse response = null;
        try {
            response = solrClient.query(collectionName,query);
            if (response.getResults().isEmpty()) return Optional.empty();
            return Optional.of(response.getResults().get(0));
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            return Optional.empty();
        }

    }

}
