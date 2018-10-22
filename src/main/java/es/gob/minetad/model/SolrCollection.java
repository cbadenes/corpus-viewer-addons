package es.gob.minetad.model;

import es.gob.minetad.solr.SolrClientFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.utilities.Assert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SolrCollection {

    private static final Logger LOG = LoggerFactory.getLogger(SolrCollection.class);

    protected final SolrClient solrClient;
    protected final String collectionName;


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


//        if (collections.get(name).size() < 1){
//
//            if (mode.equalsIgnoreCase("cloud")){
//                int numShards   = Integer.valueOf(properties.getProperty("solr.shards"));
//                int numReplicas = Integer.valueOf(properties.getProperty("solr.replicas"));
//                LOG.info("Creating collection '"+ name + "' ..");
//                CollectionAdminRequest.Create request = CollectionAdminRequest.createCollection(name,numShards, numReplicas);
//                CollectionAdminResponse adminResponse = request.process(solrClient);
//                LOG.info("Collection created succesfully [" + adminResponse.getStatus()+"]");
//
//            }else{
//
//                // Solr Standalone mode
//                LOG.info("Creating core '"+name+"' ..");
//                response = CoreAdminRequest.createCore(name,"src/test/resources/solr", solrClient);
//                LOG.info("Core created succesfully [" + response.getStatus()+"]");
//            }
//
//        }
    }

    public void commit() throws IOException, SolrServerException {
        this.solrClient.commit(collectionName);
    }

}
